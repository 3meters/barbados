package com.proxibase.service.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.proxibase.aircandi.components.CommandType;
import com.proxibase.aircandi.components.EntityList;
import com.proxibase.aircandi.components.Utilities;
import com.proxibase.service.ProxiConstants;

/**
 * Entity as described by the proxi protocol standards.
 * 
 * @author Jayma
 */
public class Entity extends ServiceEntry implements Cloneable, Serializable {

	private static final long	serialVersionUID	= -3902834532692561618L;
	protected String			mServiceUri			= ProxiConstants.URL_PROXIBASE_SERVICE;

	/* Annotation syntax: @Expose (serialize = false, deserialize = false) */

	/* Database fields */

	@Expose
	public String				type;
	@Expose
	public Boolean				root;
	@Expose
	public String				uri;
	@Expose
	public String				label;
	@Expose
	public String				title;
	@Expose
	public String				subtitle;
	@Expose
	public String				description;
	@Expose
	public String				imageUri;
	@Expose
	public String				imagePreviewUri;
	@Expose
	public String				linkUri;
	@Expose
	public Boolean				linkZoom;
	@Expose
	public Boolean				linkJavascriptEnabled;
	@Expose
	public Number				signalFence			= -200f;
	@Expose
	public Boolean				locked;
	@Expose
	public Boolean				enabled;
	@Expose
	public String				visibility			= "public";
	@Expose
	public List<Comment>		comments;
	@Expose(serialize = false, deserialize = true)
	public Number				activityDate;

	/* Synthetic service fields */

	@Expose(serialize = false, deserialize = true)
	@SerializedName("_beacon")
	public String				beaconId;													// Used to connect beacon object

	@Expose(serialize = false, deserialize = true)
	public GeoLocation			location;

	@Expose(serialize = false, deserialize = true)
	public Integer				commentCount;

	@Expose(serialize = false, deserialize = true)
	public Boolean				commentsMore;

	@Expose(serialize = false, deserialize = true)
	public EntityList<Entity>	children;

	@Expose(serialize = false, deserialize = true)
	public EntityList<Entity>	parents;

	/*
	 * For client use only
	 * 
	 * The service can return multiple parents but we but we always work in the context
	 * of a single parent and beacon
	 */

	public Beacon				beacon;
	public Entity				parent;
	public String				parentId;													// Instead of serializing parent
	public Boolean				superRoot			= false;

	public Boolean				hidden				= false;
	public Boolean				dirty				= false;
	public Boolean				rookie				= true;

	public CommandType			commandType;												// For command entities
	public String				data;
	public EntityState			state				= EntityState.Normal;
	public Date					discoveryTime;

	public Entity() {}

	@Override
	public Entity clone() {
		try {
			final Entity entity = (Entity) super.clone();
			if (this.children != null) {
				entity.children = this.children.clone();
			}
			if (this.parents != null) {
				entity.parents = this.parents.clone();
			}
			if (this.comments != null) {
				entity.comments = (List<Comment>) ((ArrayList) this.comments).clone();
			}
			return entity;
		}
		catch (final CloneNotSupportedException ex) {
			throw new AssertionError();
		}
	}

	public Entity copy() {
		/*
		 * We make sure that all entities in the children and parents
		 * collections are new entity objects. Any other object properties on
		 * the entity object are still references to the same instance including:
		 * 
		 * - Beacon object
		 * - Comments in the comment list
		 * - GeoLocation
		 */
		try {
			final Entity entity = (Entity) super.clone();
			if (this.children != null) {
				entity.children = this.children.copy();
			}
			if (this.parents != null) {
				entity.parents = this.parents.copy();
			}
			if (this.comments != null) {
				entity.comments = (List<Comment>) ((ArrayList) this.comments).clone();
			}
			return entity;
		}
		catch (final CloneNotSupportedException ex) {
			throw new AssertionError();
		}
	}

	public static void copyEntityProperties(Entity fromEntity, Entity toEntity) {
		/*
		 * Properties involved with editing are copied from one entity to another.
		 */
		toEntity.title = fromEntity.title;
		toEntity.label = fromEntity.label;
		toEntity.description = fromEntity.description;
		toEntity.linkJavascriptEnabled = fromEntity.linkJavascriptEnabled;
		toEntity.linkZoom = fromEntity.linkZoom;
		toEntity.locked = fromEntity.locked;
		toEntity.modifierId = fromEntity.modifierId;
		toEntity.modifiedDate = fromEntity.modifiedDate;
		toEntity.imagePreviewUri = fromEntity.imagePreviewUri;
		toEntity.imageUri = fromEntity.imageUri;
		toEntity.linkUri = fromEntity.linkUri;
	}

	public Entity deepCopy() {
		/*
		 * A deep copy is created of the entire entity object using
		 * serialization/deserialization. All object properties are
		 * recreated as new instances
		 */
		Entity entityCopy = (Entity) Utilities.deepCopy(this);
		return entityCopy;
	}

	public String getCollection() {
		return "entities";
	}

	public boolean hasVisibleChildren() {
		for (Entity childEntity : this.children) {
			if (!childEntity.hidden) {
				return true;
			}
		}
		return false;
	}

	public String getMasterImageUri() {
		String masterImageUri = null;
		if (imagePreviewUri != null && !imagePreviewUri.equals("")) {
			masterImageUri = imagePreviewUri;
		}
		else if (linkUri != null && !linkUri.equals("")) {
			masterImageUri = linkUri;
		}
		else if (creator != null) {
			if (creator.imageUri != null && !creator.imageUri.equals("")) {
				masterImageUri = creator.imageUri;
			}
			else if (creator.linkUri != null && !creator.linkUri.equals("")) {
				masterImageUri = creator.linkUri;
			}
		}
		return masterImageUri;
	}

	public ImageFormat getMasterImageFormat() {
		ImageFormat imageFormat = ImageFormat.Binary;
		if (imagePreviewUri != null && !imagePreviewUri.equals("")) {
			imageFormat = ImageFormat.Binary;
		}
		else if (linkUri != null && !linkUri.equals("")) {
			imageFormat = ImageFormat.Html;
		}
		else if (creator != null) {
			if (creator.imageUri != null && !creator.imageUri.equals("")) {
				imageFormat = ImageFormat.Binary;
			}
			else if (creator.linkUri != null && !creator.linkUri.equals("")) {
				imageFormat = ImageFormat.Html;
			}
		}
		return imageFormat;
	}

	public static enum ImageFormat {
		Binary, Html
	}

	public static enum EntityState {
		Normal,
		New,
		Refreshed,
		Missing
	}

	public static enum Visibility {
		Public,
		Private
	}
}