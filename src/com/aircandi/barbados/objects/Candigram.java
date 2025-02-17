package com.aircandi.barbados.objects;

import java.io.Serializable;
import java.util.Map;

import com.aircandi.objects.Entity;
import com.aircandi.service.Expose;


/**
 * @author Jayma
 */
@SuppressWarnings("ucd")
public class Candigram extends Entity implements Cloneable, Serializable {

	private static final long	serialVersionUID	= 4362288672244719448L;
	public static final String	collectionId		= "candigrams";

	// --------------------------------------------------------------------------------------------
	// service fields
	// --------------------------------------------------------------------------------------------

	@Expose
	public Number				range;
	@Expose
	public Number				duration;
	@Expose
	public Boolean				stopped				= false;
	@Expose
	public Number				hopsMax;

	@Expose(serialize = false, deserialize = true)
	public Number				hopLastDate;
	@Expose(serialize = false, deserialize = true)
	public Number				hopNextDate;
	@Expose(serialize = false, deserialize = true)
	public Number				hopCount;

	// --------------------------------------------------------------------------------------------
	// client fields (NONE are transferred)
	// --------------------------------------------------------------------------------------------

	public Candigram() {}

	public static Candigram setPropertiesFromMap(Candigram entity, Map map, Boolean nameMapping) {
		/*
		 * Properties involved with editing are copied from one entity to another.
		 */
		synchronized (entity) {
			entity = (Candigram) Entity.setPropertiesFromMap(entity, map, nameMapping);

			entity.range = (Number) map.get("range");
			entity.duration = (Number) map.get("duration");
			entity.stopped = (Boolean) map.get("stopped");
			entity.hopsMax = (Number) map.get("hopsMax");
			entity.hopCount = (Number) map.get("hopCount");
			entity.hopLastDate = (Number) map.get("hopLastDate");
			entity.hopNextDate = (Number) map.get("hopNextDate");
		}
		return entity;
	}

	@Override
	public Candigram clone() {
		final Candigram clone = (Candigram) super.clone();
		return clone;
	}

	@Override
	public String getCollection() {
		return collectionId;
	}
}