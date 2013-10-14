package com.aircandi.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;

import android.os.Build;

import com.aircandi.Aircandi;
import com.aircandi.Constants;
import com.aircandi.ServiceConstants;
import com.aircandi.components.Logger;
import com.aircandi.components.NetworkManager.ResponseCode;
import com.aircandi.components.Stopwatch;
import com.aircandi.service.objects.ServiceData;
import com.aircandi.ui.AircandiForm;
import com.aircandi.utilities.Json;
import com.squareup.okhttp.OkHttpClient;

public class OkHttpUrlConnection extends BaseConnection {
	/*
	 * Seems to throw SocketTimeout for everything
	 * 
	 * - All the normal reasons for a read timeout while input/output streaming
	 * - Service host reachable but service not running.
	 * 
	 * Retry handling
	 * 
	 * - Only applies to trying to establish a connection and sending the request body.
	 * - Doesn't retry if there is no connection (service was never reached).
	 * - Doesn't retry if request body == null or request stream isn't retryable.
	 * - Doesn't retry if SSLHandshakeException, CertificateException, ProtocolException.
	 * - Doesn't retry if IOException is during response streaming.
	 */

	private OkHttpClient	client;

	public OkHttpUrlConnection() {
		client = new OkHttpClient();
		client.setFollowProtocolRedirects(true);
		client.setConnectTimeout(ServiceConstants.TIMEOUT_CONNECTION, TimeUnit.MILLISECONDS);
		client.setReadTimeout(ServiceConstants.TIMEOUT_SOCKET_QUERIES, TimeUnit.MILLISECONDS);
		/*
		 * Hack to deal with ssl context conflict that blows up any other
		 * software we are using that also works with ssl.
		 */
		SSLContext sslContext;
		try {
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, null, null);
		}
		catch (GeneralSecurityException e) {
			throw new AssertionError(); // The system has no TLS. Just give up.
		}
		client.setSslSocketFactory(sslContext.getSocketFactory());
	}

	@Override
	public ServiceResponse request(ServiceRequest serviceRequest, Stopwatch stopwatch) {

		ServiceResponse serviceResponse = new ServiceResponse();
		serviceResponse.activityName = serviceRequest.getActivityName();
		HttpURLConnection connection = null;
		InputStream inputStream = null;

		try {

			AirHttpRequest request = OkHttpUrlConnection.buildHttpRequest(serviceRequest, stopwatch);
			URL url = new URL(request.uri);
			connection = client.open(url);

			if (request.requestType == RequestType.GET) {
				connection.setRequestMethod("GET");
			}
			else if (request.requestType == RequestType.INSERT) {
				connection.setRequestMethod("POST");
			}
			else if (serviceRequest.getRequestType() == RequestType.UPDATE) {
				connection.setRequestMethod("POST");
			}
			else if (serviceRequest.getRequestType() == RequestType.DELETE) {
				connection.setRequestMethod("DELETE");
			}
			else if (serviceRequest.getRequestType() == RequestType.METHOD) {
				connection.setRequestMethod("POST");
			}

			for (AirHttpRequest.Header header : request.headers) {
				connection.addRequestProperty(header.key, header.value);
			}

			/*
			 * Execute the request
			 */
			if (request.requestType == RequestType.GET) {
				inputStream = get(connection, serviceResponse);
			}
			else {
				inputStream = post(connection, request.requestBody, serviceResponse);
			}

			if (stopwatch != null) {
				stopwatch.segmentTime("Http service: request execute completed");
			}

			serviceResponse.statusCode = connection.getResponseCode();
			serviceResponse.statusMessage = connection.getResponseMessage();

			if ((Integer) connection.getResponseCode() / 100 == HttpURLConnection.HTTP_OK / 100) {
				/*
				 * Any 2.XX status code is considered success.
				 */
				if (inputStream != null) {

					Long contentLength = null;
					String contentLengthField = connection.getHeaderField("Content-Length");
					if (contentLengthField != null) {
						contentLength = Long.parseLong(contentLengthField);
						serviceResponse.contentLength = contentLength;
						if (request.responseFormat == ResponseFormat.BYTES && contentLength > Constants.IMAGE_DOWNLOAD_BYTES_MAX) {
							return new ServiceResponse(ResponseCode.FAILED, null, new ImageSizeException());
						}
						else if (request.responseFormat == ResponseFormat.BYTES && contentLength < Constants.IMAGE_DOWNLOAD_BYTES_MIN) {
							return new ServiceResponse(ResponseCode.FAILED, null, new ImageUnusableException());
						}
					}

					Object response = handleResponse(inputStream, request, serviceResponse, serviceRequest.getRequestListener());

					if (stopwatch != null) {
						stopwatch.segmentTime("Http service: response content captured");
					}

					/* Check for valid client version even if the call was successful */
					if (request.responseFormat == ResponseFormat.JSON && !serviceRequest.getIgnoreResponseData()) {
						/*
						 * We think anything json is coming from the Aircandi service (except Bing)
						 */
						ServiceData serviceData = (ServiceData) Json.jsonToObject((String) response, Json.ObjectType.NONE, Json.ServiceDataWrapper.TRUE);

						if (stopwatch != null) {
							stopwatch.segmentTime("Http service: response content json ("
									+ String.valueOf(((String) response).length())
									+ " bytes) decoded to object");
						}

						Integer clientVersionCode = Aircandi.getVersionCode(Aircandi.applicationContext, AircandiForm.class);
						if (serviceData != null && serviceData.androidMinimumVersion != null) {
							if (serviceData.androidMinimumVersion.intValue() > clientVersionCode) {
								return new ServiceResponse(ResponseCode.FAILED, null, new ClientVersionException());
							}
						}
					}

					serviceResponse.data = response;
				}

				return serviceResponse;
			}
			else {
				/*
				 * We got a non-success http status code so break it down.
				 */
				if (inputStream != null) {

					final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
					final StringBuilder stringBuilder = new StringBuilder(); // $codepro.audit.disable defineInitialCapacity

					String line = null;
					while ((line = bufferedReader.readLine()) != null) {
						stringBuilder.append(line + System.getProperty("line.separator"));
					}
					bufferedReader.close();

					String responseContent = stringBuilder.toString();
					Logger.d(this, responseContent);

					if (request.responseFormat == ResponseFormat.JSON) {
						/*
						 * We think anything json is coming from the Aircandi service.
						 */
						ServiceData serviceData = (ServiceData) Json.jsonToObject(responseContent, Json.ObjectType.NONE, Json.ServiceDataWrapper.TRUE);
						Integer clientVersionCode = Aircandi.getVersionCode(Aircandi.applicationContext, AircandiForm.class);
						if (serviceData != null) {
							if (serviceData.androidMinimumVersion != null && serviceData.androidMinimumVersion.intValue() > clientVersionCode) {
								return new ServiceResponse(ResponseCode.FAILED, null, new ClientVersionException());
							}
							else if (serviceData.error != null && serviceData.error.code != null) {
								serviceResponse.statusCodeService = serviceData.error.code.floatValue();
							}
						}
					}
				}

				serviceResponse.responseCode = ResponseCode.FAILED;
				return serviceResponse;
			}
		}
		catch (IOException exception) {
			try {
				if (connection != null) {
					serviceResponse.statusCode = connection.getResponseCode();
					serviceResponse.statusMessage = connection.getResponseMessage();
					serviceResponse.exception = exception;
					serviceResponse.responseCode = ResponseCode.FAILED;
					return serviceResponse;
				}
				else {
					return new ServiceResponse(ResponseCode.FAILED, null, exception);
				}
			}
			catch (IOException secondException) {
				return new ServiceResponse(ResponseCode.FAILED, null, exception);
			}
		}
		finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				}
				catch (IOException exception) {
					return new ServiceResponse(ResponseCode.FAILED, null, exception);
				}
			}
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private Object handleResponse(InputStream inputStream, AirHttpRequest airHttpRequest, ServiceResponse serviceResponse, RequestListener listener)
			throws IOException {

		if (airHttpRequest.responseFormat == ResponseFormat.BYTES) {
			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
			
			// this dynamically extends to take the bytes you read
			ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

			// this is storage overwritten on each iteration with bytes
			int bufferSize = 1024;
			byte[] buffer = new byte[bufferSize];

			// we need to know how may bytes were read to write them to the byteBuffer
			int len = 0;
			while ((len = bufferedInputStream.read(buffer)) != -1) {
				byteBuffer.write(buffer, 0, len);

				if (listener != null && serviceResponse.contentLength != null && serviceResponse.contentLength > 0) {
					listener.onProgressChanged(((int) (len * 100 / serviceResponse.contentLength)));
				}
			}

			// and then we can return your byte array.
			serviceResponse.contentLength = (long) byteBuffer.size();
			return byteBuffer.toByteArray();
		}
		else if (airHttpRequest.responseFormat == ResponseFormat.NONE) {
			return null;
		}
		else {
			final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			final StringBuilder stringBuilder = new StringBuilder(); // $codepro.audit.disable defineInitialCapacity

			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line + System.getProperty("line.separator"));
			}
			bufferedReader.close();
			serviceResponse.contentLength = (long) stringBuilder.length();

			return stringBuilder.toString();
		}
	}

	// --------------------------------------------------------------------------------------------
	// Post
	// --------------------------------------------------------------------------------------------

	private InputStream post(HttpURLConnection connection, String string, ServiceResponse serviceResponse) throws IOException {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			return post_Gingerbread(connection, string, serviceResponse);
		}
		else {
			return post_Froyo(connection, string, serviceResponse);
		}
	}

	private InputStream post_Gingerbread(HttpURLConnection connection, String string, ServiceResponse serviceResponse) throws IOException {
		/*
		 * Gingerbread and above support Gzip natively.
		 */
		OutputStream outputStream = null;
		InputStream inputStream = null;
		try {
			byte[] data = string.getBytes();

			connection.setDoOutput(true);
			connection.setFixedLengthStreamingMode(data.length);
			connection.setRequestProperty("Accept-Encoding", "gzip");

			// Set the output stream
			outputStream = connection.getOutputStream();
			outputStream.write(data);
			outputStream.flush();
			outputStream.close();

			if ((Integer) connection.getResponseCode() / 100 == HttpURLConnection.HTTP_OK / 100) {
				inputStream = connection.getInputStream();
			}
			else {
				inputStream = connection.getErrorStream();
			}

			serviceResponse.contentType = connection.getContentType();
			if (connection.getContentEncoding() != null && connection.getContentEncoding().equals("gzip")) {
				serviceResponse.contentEncoding = "gzip";
				return new GZIPInputStream(inputStream);
			}
			else {
				serviceResponse.contentEncoding = "none";
				return inputStream;
			}
		}
		catch (IOException exception) {
			throw exception;
		}
		finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				}
				catch (IOException e) {}
			}
		}
	}

	private InputStream post_Froyo(HttpURLConnection connection, String string, ServiceResponse serviceResponse) throws IOException {
		/*
		 * Gingerbread and above support Gzip natively.
		 */
		boolean useGzip = false;
		OutputStream outputStream = null;
		InputStream inputStream = null;

		try {
			byte[] data = string.getBytes();

			connection.setDoOutput(true);
			connection.setFixedLengthStreamingMode(data.length);
			connection.setRequestProperty("Accept-Encoding", "gzip");

			// Set the output stream
			outputStream = connection.getOutputStream();
			outputStream.write(data);
			outputStream.flush();
			outputStream.close();

			if ((Integer) connection.getResponseCode() / 100 == HttpURLConnection.HTTP_OK / 100) {
				inputStream = connection.getInputStream();
			}
			else {
				inputStream = connection.getErrorStream();
			}

			final Map<String, List<String>> headers = connection.getHeaderFields();
			// This is a map, but we can't assume the key we're looking for
			// is in normal casing. So it's really not a good map, is it?
			final Set<Map.Entry<String, List<String>>> set = headers.entrySet();
			for (Iterator<Map.Entry<String, List<String>>> i = set.iterator(); i.hasNext();) {
				Map.Entry<String, List<String>> entry = i.next();
				if ("Content-Encoding".equalsIgnoreCase(entry.getKey())) {
					for (Iterator<String> j = entry.getValue().iterator(); j.hasNext();) {
						String str = j.next();
						if (str.equalsIgnoreCase("gzip")) {
							useGzip = true;
							break;
						}
					}
					// Break out of outer loop.
					if (useGzip) {
						break;
					}
				}
			}

			serviceResponse.contentType = connection.getContentType();
			if (useGzip) {
				serviceResponse.contentEncoding = "gzip";
				return new GZIPInputStream(inputStream);
			}
			else {
				serviceResponse.contentEncoding = "none";
				return inputStream;
			}
		}
		catch (IOException exception) {
			throw exception;
		}
		finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				}
				catch (IOException e) {}
			}
		}
	}

	// --------------------------------------------------------------------------------------------
	// Get
	// --------------------------------------------------------------------------------------------

	private InputStream get(HttpURLConnection connection, ServiceResponse serviceResponse) throws IOException {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			return get_Gingerbread(connection, serviceResponse);
		}
		else {
			return get_Froyo(connection, serviceResponse);
		}
	}

	private InputStream get_Gingerbread(HttpURLConnection connection, ServiceResponse serviceResponse) throws IOException {

		InputStream inputStream = connection.getInputStream();

		serviceResponse.contentType = connection.getContentType();
		if (connection.getContentEncoding() != null && connection.getContentEncoding().equals("gzip")) {
			serviceResponse.contentEncoding = "gzip";
			return new GZIPInputStream(inputStream);
		}
		else {
			serviceResponse.contentEncoding = "none";
			return inputStream;
		}
	}

	private InputStream get_Froyo(HttpURLConnection connection, ServiceResponse serviceResponse) throws IOException {
		boolean useGzip = false;
		connection.setRequestProperty("Accept-Encoding", "gzip");

		InputStream inputStream = connection.getInputStream();

		final Map<String, List<String>> headers = connection.getHeaderFields();
		// This is a map, but we can't assume the key we're looking for
		// is in normal casing. So it's really not a good map, is it?
		final Set<Map.Entry<String, List<String>>> set = headers.entrySet();
		for (Iterator<Map.Entry<String, List<String>>> i = set.iterator(); i.hasNext();) {
			Map.Entry<String, List<String>> entry = i.next();
			if ("Content-Encoding".equalsIgnoreCase(entry.getKey())) {
				for (Iterator<String> j = entry.getValue().iterator(); j.hasNext();) {
					String str = j.next();
					if (str.equalsIgnoreCase("gzip")) {
						useGzip = true;
						break;
					}
				}
				// Break out of outer loop.
				if (useGzip) {
					break;
				}
			}
		}

		serviceResponse.contentType = connection.getContentType();
		if (useGzip) {
			serviceResponse.contentEncoding = "gzip";
			return new GZIPInputStream(inputStream);
		}
		else {
			serviceResponse.contentEncoding = "none";
			return inputStream;
		}
	}

}
