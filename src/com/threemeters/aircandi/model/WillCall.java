package com.threemeters.aircandi.model;

import com.google.gson.annotations.Expose;
import com.threemeters.sdk.android.core.RippleService;

/**
 * @author Jayma
 */
public class WillCall
{
	// Annotation syntax: @Expose (serialize = false, deserialize = false)
	@Expose
	public String	willCallId;
	@Expose
	public String	referrerId;
	@Expose
	public String	promotionId;
	@Expose
	public String	willCallMax;

	public WillCall() {}

	public String getUriOdata()
	{
		String root = RippleService.URL_RIPPLE_SERVICE_ODATA;
		String entity = "WillCalls";
		String uri = root + entity + "(guid'" + this.willCallId + "')";
		return uri;
	}
}