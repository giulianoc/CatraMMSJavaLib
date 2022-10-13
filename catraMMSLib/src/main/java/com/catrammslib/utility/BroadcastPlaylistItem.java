package com.catrammslib.utility;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.*;

import com.catrammslib.CatraMMSAPI;
import com.catrammslib.entity.Stream;
import com.catrammslib.entity.MediaItem;

public class BroadcastPlaylistItem implements Serializable, Comparable<BroadcastPlaylistItem> {

    static private final Logger mLogger = Logger.getLogger(BroadcastPlaylistItem.class);

	private List<String> mediaTypeList = new ArrayList<>();

	private Date timestamp;

    private Date start;
    private Date end;
	
	private String mediaType;					// Stream, Media, Countdown, Direct URL

	private String streamConfigurationLabel;	// in case of Stream
	private Stream stream;			// got from streamConfigurationLabel

	private List<Long> physicalPathKeys = new ArrayList<>();		// in case of Media
	private Long physicalPathKey;									// in case of Countdown
	private List<MediaItem> mediaItems = new ArrayList<>();			// got from physicalPathKey
	private String text;						// in case of Countdown
	private String textPosition_X_InPixel;		// in case of Countdown
	private String textPosition_Y_InPixel;		// in case of Countdown

	private String url;							// in case of Direct URL

	private CatraMMSAPI catraMMS;
	private String username;
	private String password;

    public BroadcastPlaylistItem(CatraMMSAPI catraMMS, String username, String password)
    {
		this.catraMMS = catraMMS;
		this.username = username;
		this.password = password;

		timestamp = new Date();
		
		mediaTypeList.add("Stream");
		mediaTypeList.add("Media");
		mediaTypeList.add("Countdown");

		mediaType = "Stream";

		text = "days_counter days hours_counter:mins_counter:secs_counter.cents_counter";
		textPosition_X_InPixel = "(video_width-text_width)/2";
		textPosition_Y_InPixel = "(video_height-text_height)/2";
	}

	@Override
	public int compareTo(BroadcastPlaylistItem broadcastPlaylistItem) {
        return getStart().compareTo(broadcastPlaylistItem.getStart());
	}

	@Override
	public String toString() 
	{
		String str = "";

		if (mediaType.equals("Stream"))
			str = streamConfigurationLabel;
		else if (mediaType.equals("Media"))
		{
			for (int physicalPathKeyIndex = 0; physicalPathKeyIndex < physicalPathKeys.size(); physicalPathKeyIndex++)
			{
				Long localPhysicalPathKey = physicalPathKeys.get(physicalPathKeyIndex);
				MediaItem mediaItem = null;
				if (mediaItems.size() > physicalPathKeyIndex)
					mediaItem = mediaItems.get(physicalPathKeyIndex);

				if (str != "")
					str += "</br>";	// str += " / ";
				str += ("<b>" + localPhysicalPathKey.toString() + "</b>" + (mediaItem != null ? (": " + mediaItem.getTitle()) : ""));
			}
		}
		else if (mediaType.equals("Countdown"))
		{
			MediaItem mediaItem = null;
			if (mediaItems.size() > 0)
				mediaItem = mediaItems.get(0);

			str = "<b>" + (physicalPathKey == null ? "" : physicalPathKey.toString()) + "</b>" 
				+ (mediaItem != null ? (": " + mediaItem.getTitle()) : "");
		}
		else if (mediaType.equals("Direct URL"))
			str = url;

		return str;
	}

	public boolean isEqualsTo(JSONObject joBroadcastPlaylistItem) 
	{
		try
		{
			if (!mediaType.equals(joBroadcastPlaylistItem.getString("mediaType")))
				return false;

			if (mediaType.equals("Stream"))
			{
				if (streamConfigurationLabel != null 
					&& !streamConfigurationLabel.equals(joBroadcastPlaylistItem.getString("streamConfigurationLabel")))
					return false;
			}
			else if (mediaType.equals("Media"))
			{
				if (!joBroadcastPlaylistItem.has("physicalPathKeys"))
					return false;

				JSONArray jaPhysicalPathKeys = joBroadcastPlaylistItem.getJSONArray("physicalPathKeys");

				if (physicalPathKeys.size() != jaPhysicalPathKeys.length())
					return false;

				for (int physicalPathKeyIndex = 0; physicalPathKeyIndex < physicalPathKeys.size(); physicalPathKeyIndex++)
				{
					Long physicalPathKey_1 = physicalPathKeys.get(physicalPathKeyIndex);
					Long physicalPathKey_2 = jaPhysicalPathKeys.getLong(physicalPathKeyIndex);

					if (physicalPathKey_1.longValue() != physicalPathKey_2.longValue())
						return false;
				}
			}
			else if (mediaType.equals("Countdown"))
			{
				if (!joBroadcastPlaylistItem.has("physicalPathKey"))
					return false;

				Long localPhysicalPathKey = joBroadcastPlaylistItem.getLong("physicalPathKey");

				if (physicalPathKey == null 
					|| physicalPathKey.longValue() != localPhysicalPathKey.longValue()
					|| !text.equals(joBroadcastPlaylistItem.getString("text"))
					|| !textPosition_X_InPixel.equals(joBroadcastPlaylistItem.getString("textPosition_X_InPixel"))
					|| !textPosition_Y_InPixel.equals(joBroadcastPlaylistItem.getString("textPosition_Y_InPixel"))
				)
					return false;
			}
			else if (mediaType.equals("Direct URL"))
			{
				if (!url.equals(joBroadcastPlaylistItem.getString("url")))
					return false;
			}
			else
			{
				mLogger.error("Unknown mediaType: " + mediaType);

				return false;
			}
		}
		catch(Exception e)
		{
			mLogger.error("Exception: " + e.getMessage());

			return false;
		}

		return true;
	}

	// this json will be saved within the Parameters of the Broadcaster IngestionJob
	// and is used when a new Playlist is received (by the API engine)
	public JSONObject getJson()
	{
		JSONObject joBroadcastPlaylistItem = new JSONObject();
		try
		{
			joBroadcastPlaylistItem.put("mediaType", mediaType);
			if (mediaType.equalsIgnoreCase("Stream"))
			{
				if (streamConfigurationLabel != null)
					joBroadcastPlaylistItem.put("streamConfigurationLabel", streamConfigurationLabel);
			}
			else if (mediaType.equalsIgnoreCase("Media"))
			{
				JSONArray jaPhysicalPathKeys = new JSONArray();
				joBroadcastPlaylistItem.put("physicalPathKeys", jaPhysicalPathKeys);

				for (Long localPhysicalPathKey: physicalPathKeys)
					jaPhysicalPathKeys.put(localPhysicalPathKey);
			}
			else if (mediaType.equalsIgnoreCase("Countdown"))
			{
				joBroadcastPlaylistItem.put("physicalPathKey", physicalPathKey);
				joBroadcastPlaylistItem.put("text", text);
				joBroadcastPlaylistItem.put("textPosition_X_InPixel", textPosition_X_InPixel);
				joBroadcastPlaylistItem.put("textPosition_Y_InPixel", textPosition_Y_InPixel);
			}
			else if (mediaType.equalsIgnoreCase("Direct URL"))
				joBroadcastPlaylistItem.put("url", url);
			else
			{
				mLogger.error("Unknown mediaType: " + mediaType);
			}
		}
		catch(Exception e)
		{
			mLogger.error("Exception: " + e);
		}

		return joBroadcastPlaylistItem;
	}
	
	static public BroadcastPlaylistItem fromJson(JSONObject joBroadcastPlaylistItem,
		CatraMMSAPI localCatraMMS, String localUsername, String localPassword)
	{
		BroadcastPlaylistItem broadcastPlaylistItem = 
			new BroadcastPlaylistItem(localCatraMMS, localUsername, localPassword);

		try
		{
			broadcastPlaylistItem.setMediaType(joBroadcastPlaylistItem.getString("mediaType"));
			if (broadcastPlaylistItem.getMediaType().equalsIgnoreCase("Stream"))
				broadcastPlaylistItem.setStreamConfigurationLabel(joBroadcastPlaylistItem.getString("streamConfigurationLabel"));
			else if (broadcastPlaylistItem.getMediaType().equalsIgnoreCase("Media"))
			{
				if (joBroadcastPlaylistItem.has("physicalPathKeys"))
				{
					JSONArray jaPhysicalPathKeys = joBroadcastPlaylistItem.getJSONArray("physicalPathKeys");
					for (int physicalPathKeyIndex = 0; physicalPathKeyIndex < jaPhysicalPathKeys.length(); physicalPathKeyIndex++)
						broadcastPlaylistItem.addPhysicalPathKey(jaPhysicalPathKeys.getLong(physicalPathKeyIndex));
				}
			}
			else if (broadcastPlaylistItem.getMediaType().equalsIgnoreCase("Countdown"))
			{
				broadcastPlaylistItem.setPhysicalPathKey(joBroadcastPlaylistItem.getLong("physicalPathKey"));
				broadcastPlaylistItem.setText(joBroadcastPlaylistItem.getString("text"));
				broadcastPlaylistItem.setTextPosition_X_InPixel(joBroadcastPlaylistItem.getString("textPosition_X_InPixel"));
				broadcastPlaylistItem.setTextPosition_Y_InPixel(joBroadcastPlaylistItem.getString("textPosition_Y_InPixel"));
			}
			else if (broadcastPlaylistItem.getMediaType().equalsIgnoreCase("Direct URL"))
				broadcastPlaylistItem.setUrl(joBroadcastPlaylistItem.getString("url"));
			else
			{
				mLogger.error("Unknown mediaType: " + broadcastPlaylistItem.getMediaType());
			}
		}
		catch(Exception e)
		{
			mLogger.error("Exception: " + e);
		}

		return broadcastPlaylistItem;
	}

	public JSONObject getJson2()
	throws Exception
	{
		try
		{
			JSONObject joInputRoot = new JSONObject();

			joInputRoot.put("timePeriod", true);
			joInputRoot.put("utcScheduleStart", getStart().getTime() / 1000);
			joInputRoot.put("utcScheduleEnd", getEnd().getTime() / 1000);
	
			if (getMediaType().equalsIgnoreCase("Stream"))
			{
				JSONObject joStreamInput = new JSONObject();
				joInputRoot.put("streamInput", joStreamInput);
	
				if (getStream() != null)
				{
					joStreamInput.put("streamConfKey", getStream().getConfKey());
					joStreamInput.put("streamConfigurationLabel", getStream().getLabel());
		
					if (getStream().getEncodersPoolLabel() != null 
						&& !getStream().getEncodersPoolLabel().isEmpty())
						joStreamInput.put("encodersPoolLabel", getStream().getEncodersPoolLabel());
		
					joStreamInput.put("streamSourceType", getStream().getSourceType());
					if (getStream().getSourceType().equalsIgnoreCase("IP_PULL"))
						joStreamInput.put("url", getStream().getUrl());	
				}
				else
				{
					mLogger.info("getStream() is null!!!");
				}
			}
			else if (getMediaType().equalsIgnoreCase("Media"))
			{
				JSONObject joVODInput = new JSONObject();
				joInputRoot.put("vodInput", joVODInput);
	
				JSONArray jaSources = new JSONArray();
				joVODInput.put("sources", jaSources);

				for (Long localPhysicalPathKey: getPhysicalPathKeys())
				{
					JSONObject joSource = new JSONObject();
					jaSources.put(joSource);
	
					joSource.put("physicalPathKey", localPhysicalPathKey);
				}
			}
			else if (getMediaType().equalsIgnoreCase("Countdown"))
			{
				JSONObject joCountdownInput = new JSONObject();
				joInputRoot.put("countdownInput", joCountdownInput);

				joCountdownInput.put("physicalPathKey", getPhysicalPathKey());
				joCountdownInput.put("text", getText());
				joCountdownInput.put("textPosition_X_InPixel", textPosition_X_InPixel);
				joCountdownInput.put("textPosition_Y_InPixel", textPosition_Y_InPixel);
			}
			else if (getMediaType().equalsIgnoreCase("Direct URL"))
			{
				JSONObject joDirectURLInput = new JSONObject();
				joInputRoot.put("directURLInput", joDirectURLInput);

				joDirectURLInput.put("url", getUrl());
			}
			else
			{
				String errorMessage = "Unknown mediaType: " + getMediaType();
				mLogger.error(errorMessage);

				throw new Exception(errorMessage);
			}
	
			return joInputRoot;	
		}
		catch (Exception e)
		{
			mLogger.error("getJson2" 
				+ ", exception: " + e
			);

			throw e;
		}
	}

	public void setStreamConfigurationLabel(String streamConfigurationLabel) 
	{
		this.streamConfigurationLabel = streamConfigurationLabel;

		try
		{
			if (streamConfigurationLabel != null)
			{
				List<Stream> streamList = new ArrayList<>();
				catraMMS.getStream(username, password, 0, 1, null, 
					streamConfigurationLabel, false,
					null, null, null, null, null, null, null, streamList);
				if (streamList.size() > 0)
					stream = streamList.get(0);	
			}
		}
		catch (Exception e)
		{
			mLogger.error("Exception: " + e.getMessage());
		}
	}

	public void setPhysicalPathKeys(List<Long> localPhysicalPathKeys) 
	{
		physicalPathKeys.clear();

		if (localPhysicalPathKeys != null)
		{
			for(Long localPhysicalPathKey: localPhysicalPathKeys)
				addPhysicalPathKey(localPhysicalPathKey);
		}
	}

	public void setPhysicalPathKey(Long localPhysicalPathKey) 
	{
		this.physicalPathKey = localPhysicalPathKey;

		if (localPhysicalPathKey != null)
		{
			int positionIndex = 0;

			MediaItem mediaItem = null;
			try
			{
				mediaItem = catraMMS.getMediaItemByPhysicalPathKey(username, password, localPhysicalPathKey);
			}
			catch (Exception e)
			{
				mLogger.error("Exception: " + e.getMessage());
			}
	
			if (mediaItems.size() <= positionIndex)
				mediaItems.add(mediaItem);
			else
				mediaItems.set(positionIndex, mediaItem);	
		}
	}

	public int addPhysicalPathKey(Long localPhysicalPathKey)
	{
		int positionIndex;

		physicalPathKeys.add(localPhysicalPathKey);

		positionIndex = physicalPathKeys.size() - 1;

		MediaItem mediaItem = null;
		try
		{
			mediaItem = catraMMS.getMediaItemByPhysicalPathKey(username, password, localPhysicalPathKey);
		}
		catch (Exception e)
		{
			mLogger.error("Exception: " + e.getMessage());
		}

		if (mediaItems.size() <= positionIndex)
			mediaItems.add(mediaItem);
		else
			mediaItems.set(positionIndex, mediaItem);

		return positionIndex;
	}

	public String getStreamConfigurationLabel() {
		return streamConfigurationLabel;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Date getTimestamp() {
		return timestamp;
	}


	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}


	public List<Long> getPhysicalPathKeys() {
		return physicalPathKeys;
	}

	public Stream getStream() {
		return stream;
	}

	public void setStream(Stream stream) {
		this.stream = stream;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public List<String> getMediaTypeList() {
		return mediaTypeList;
	}

	public void setMediaTypeList(List<String> mediaTypeList) {
		this.mediaTypeList = mediaTypeList;
	}

	public String getMediaType() {
		return mediaType;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTextPosition_X_InPixel() {
		return textPosition_X_InPixel;
	}

	public void setTextPosition_X_InPixel(String textPosition_X_InPixel) {
		this.textPosition_X_InPixel = textPosition_X_InPixel;
	}

	public String getTextPosition_Y_InPixel() {
		return textPosition_Y_InPixel;
	}

	public void setTextPosition_Y_InPixel(String textPosition_Y_InPixel) {
		this.textPosition_Y_InPixel = textPosition_Y_InPixel;
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	public Long getPhysicalPathKey() {
		return physicalPathKey;
	}
}
