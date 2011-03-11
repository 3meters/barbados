package com.threemeters.aircandi.model;

import com.google.gson.annotations.Expose;
import com.threemeters.sdk.android.core.RippleService;

/**
 * @author Jayma
 */
public class Hookup
{
	// Annotation syntax: @Expose (serialize = false, deserialize = false)
	@Expose
	public String	hookupId;
	@Expose
	public String	userId;
	@Expose
	public String	entityId;
	@Expose
	public String	hookupDate;
	@Expose
	public boolean	confirmed				= false;
	@Expose
	public String	confirmedDate;
	

	public Hookup() {}
	
	public Hookup(String entityId, String userId, String hookupDate)
	{
		this.entityId = entityId;
		this.userId = userId;
		this.hookupDate = hookupDate;
	}

	public String getUriOdata()
	{
		String root = RippleService.URL_RIPPLE_SERVICE_ODATA;
		String entity = "Hookups";
		String uri = root + entity + "(guid'" + this.hookupId + "')";
		return uri;
	}
}