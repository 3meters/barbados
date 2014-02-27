package com.aircandi.barbados.objects;

public class EventType extends com.aircandi.objects.EventType {

	public static String	INSERT_CANDIGRAM_TO_PLACE	= "insert_entity_candigram_to_place";
	public static String	INSERT_PICTURE_TO_CANDIGRAM	= "insert_entity_post_to_candigram";
	public static String	INSERT_COMMENT_TO_CANDIGRAM	= "insert_entity_comment_to_candigram";
	
	public static String	UPDATE_CANDIGRAM			= "update_entity_candigram";
	public static String	DELETE_CANDIGRAM			= "delete_candigram";
	
	public static String	WATCH_CANDIGRAM				= "watch_entity_candigram";
	public static String	UNWATCH_CANDIGRAM			= "unwatch_entity_candigram";
	public static String	LIKE_CANDIGRAM				= "like_entity_candigram";
	public static String	UNLIKE_CANDIGRAM			= "unlike_entity_candigram";

	public static String	MOVE_CANDIGRAM				= "move_candigram";
	public static String	FORWARD_CANDIGRAM			= "forward_candigram";
	public static String	RESTART_CANDIGRAM			= "restart_candigram";
}