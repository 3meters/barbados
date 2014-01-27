package com.aircandi.barbados.components;

// import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;

import android.os.Bundle;

import com.aircandi.Aircandi;
import com.aircandi.ServiceConstants;
import com.aircandi.components.NetworkManager.ResponseCode;
import com.aircandi.components.ProximityManager.ModelResult;
import com.aircandi.components.TrackerBase.TrackerCategory;
import com.aircandi.objects.Entity;
import com.aircandi.objects.ServiceData;
import com.aircandi.service.RequestType;
import com.aircandi.service.ResponseFormat;
import com.aircandi.service.ServiceRequest;
import com.aircandi.utilities.Json;
import com.aircandi.utilities.Type;

public class EntityManager extends com.aircandi.components.EntityManager {

	public ModelResult moveCandigram(Entity entity, Boolean forward, Boolean skipMove, String toId) {
		/*
		 * moveCandigrams updates activityDate in the database:
		 * - on the candigram
		 * - on the old place candigram was linked to
		 * - on the new place candigram is linked to
		 * - on any other upstream entities with valid links
		 * - inactive links are not followed
		 * - like/create/watch/proximity links are not followed
		 */

		final ModelResult result = new ModelResult();

		/* Construct entity, link, and observation */
		final Bundle parameters = new Bundle();
		if (toId != null) {
			parameters.putString("toId", toId);
		}
		if (skipMove != null) {
			parameters.putBoolean("skipMove", skipMove);
		}
		if (Type.isTrue(forward)) {
			parameters.putBoolean("forward", forward);
		}
		parameters.putStringArrayList("entityIds", new ArrayList(Arrays.asList(entity.id)));

		final ServiceRequest serviceRequest = new ServiceRequest()
				.setUri(ServiceConstants.URL_PROXIBASE_SERVICE_METHOD + "moveCandigrams")
				.setRequestType(RequestType.METHOD)
				.setParameters(parameters)
				.setResponseFormat(ResponseFormat.JSON);

		if (!Aircandi.getInstance().getCurrentUser().isAnonymous()) {
			serviceRequest.setSession(Aircandi.getInstance().getCurrentUser().session);
		}

		result.serviceResponse = dispatch(serviceRequest);

		/* Return the new place the candigram has moved to */
		if (result.serviceResponse.responseCode == ResponseCode.SUCCESS) {
			if (!skipMove) {
				String action = forward ? "entity_forward" : "entity_bounce";
				Aircandi.tracker.sendEvent(TrackerCategory.LINK, action, null, 0);
			}
			else {
				Aircandi.tracker.sendEvent(TrackerCategory.UX, "entity_preview", null, 0);
			}

			final String jsonResponse = (String) result.serviceResponse.data;
			final ServiceData serviceData = (ServiceData) Json.jsonToObjects(jsonResponse, Json.ObjectType.ENTITY, Json.ServiceDataWrapper.TRUE);
			result.data = serviceData.data;
		}

		return result;
	}
}