package com.catrammslib.utility;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.*;

import com.catrammslib.CatraMMSAPI;
import com.catrammslib.entity.ChannelConf;
import com.catrammslib.entity.MediaItem;

public class BroadcastPlaylistItem implements Serializable, Comparable<BroadcastPlaylistItem> {

    static private final Logger mLogger = Logger.getLogger(BroadcastPlaylistItem.class);

	private List<String> mediaTypeList = new ArrayList<>();

	private Date timestamp;

    private Date start;
    private Date end;
	
	private String mediaType;					// Live Channel, Media, Countdown, Direct URL

	private String channelConfigurationLabel;	// in case of Live Channel
	private ChannelConf channelConf;			// got from channelConfigurationLabel

	private Long physicalPathKey;				// in case of Media, Countdown
	private MediaItem mediaItem;				// got from physicalPathKey
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
		
		mediaTypeList.add("Live Channel");
		mediaTypeList.add("Media");
		mediaTypeList.add("Countdown");

		mediaType = "Live Channel";

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

		if (mediaType.equals("Live Channel"))
			str = channelConfigurationLabel;
		else if (mediaType.equals("Media"))
			str = physicalPathKey.toString() + (mediaItem != null ? (" - " + mediaItem.getTitle()) : "");
		else if (mediaType.equals("Countdown"))
			str = physicalPathKey.toString() + " - " + text;
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

			if (mediaType.equals("Live Channel"))
			{
				if (!channelConfigurationLabel.equals(joBroadcastPlaylistItem.getString("channelConfigurationLabel")))
					return false;
			}
			else if (mediaType.equals("Media"))
			{
				if (physicalPathKey.longValue() != joBroadcastPlaylistItem.getLong("physicalPathKey"))
					return false;
			}
			else if (mediaType.equals("Countdown"))
			{
				if (physicalPathKey.longValue() != joBroadcastPlaylistItem.getLong("physicalPathKey")
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
			if (mediaType.equalsIgnoreCase("Live Channel"))
				joBroadcastPlaylistItem.put("channelConfigurationLabel", channelConfigurationLabel);
			else if (mediaType.equalsIgnoreCase("Media"))
				joBroadcastPlaylistItem.put("physicalPathKey", physicalPathKey);
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
			if (broadcastPlaylistItem.getMediaType().equalsIgnoreCase("Live Channel"))
				broadcastPlaylistItem.setChannelConfigurationLabel(joBroadcastPlaylistItem.getString("channelConfigurationLabel"));
			else if (broadcastPlaylistItem.getMediaType().equalsIgnoreCase("Media"))
				broadcastPlaylistItem.setPhysicalPathKey(joBroadcastPlaylistItem.getLong("physicalPathKey"));
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

	public void setChannelConfigurationLabel(String channelConfigurationLabel) 
	{
		this.channelConfigurationLabel = channelConfigurationLabel;

		try
		{
			List<ChannelConf> channelConfList = new ArrayList<>();
			catraMMS.getChannelConf(username, password, 0, 1, null, channelConfigurationLabel, 
				null, null, null, null, null, null, null, channelConfList);
			if (channelConfList.size() > 0)
				channelConf = channelConfList.get(0);
		}
		catch (Exception e)
		{
			mLogger.error("Exception: " + e.getMessage());
		}
	}

	public void setPhysicalPathKey(Long physicalPathKey) {
		this.physicalPathKey = physicalPathKey;

		try
		{
			mediaItem = catraMMS.getMediaItemByPhysicalPathKey(username, password, physicalPathKey);
		}
		catch (Exception e)
		{
			mLogger.error("Exception: " + e.getMessage());
		}
	}

	public String getChannelConfigurationLabel() {
		return channelConfigurationLabel;
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

	public ChannelConf getChannelConf() {
		return channelConf;
	}

	public void setChannelConf(ChannelConf channelConf) {
		this.channelConf = channelConf;
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

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	public Long getPhysicalPathKey() {
		return physicalPathKey;
	}


}
