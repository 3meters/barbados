package com.aircandi.service.objects;

import java.util.Map;

import com.aircandi.service.Expose;

/**
 * @author Jayma
 */
@SuppressWarnings("ucd")
public class Session extends ServiceBase {

	private static final long	serialVersionUID	= 127428776257201066L;
	public static final String	collectionId		= "sessions";

	@Expose
	public String				key;

	/* Dates */

	@Expose
	public Number				expirationDate;

	public Session() {}

	public static Session setPropertiesFromMap(Session session, Map map, Boolean nameMapping) {
		/*
		 * Properties involved with editing are copied from one entity to another.
		 */
		session = (Session) ServiceBase.setPropertiesFromMap(session, map, nameMapping);
		session.key = (String) map.get("key");
		session.expirationDate = (Number) map.get("expirationDate");

		return session;
	}

	@Override
	public String getCollection() {
		return collectionId;
	}

}