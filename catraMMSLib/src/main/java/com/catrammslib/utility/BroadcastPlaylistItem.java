package com.catrammslib.utility;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.*;

import com.catrammslib.entity.ChannelConf;

public class BroadcastPlaylistItem implements Serializable, Comparable<BroadcastPlaylistItem> {

    static private final Logger mLogger = Logger.getLogger(BroadcastPlaylistItem.class);

	private List<String> mediaTypeList = new ArrayList<>();

	private Date timestamp;

    private Date start;
    private Date end;
	
	private String mediaType;					// Live Channel, Media, Countdown

	private ChannelConf channelConf;			// in case of Live Channel
	// I know channelConfigurationLabel is within channelConf but we need it anyway
	private String channelConfigurationLabel;	// in case of Live Channel

	private Long physicalPathKey;				// in case of Media, Countdown
	private String text;						// in case of Countdown

	private List<ChannelConf> channelConfigurationList;

    public BroadcastPlaylistItem(List<ChannelConf> channelConfigurationList)
    {
		this.channelConfigurationList = channelConfigurationList;

		timestamp = new Date();
		
		mediaTypeList.add("Live Channel");
		mediaTypeList.add("Media");
		mediaTypeList.add("Countdown");

		mediaType = "Live Channel";

		text = "days_counter days hours_counter:mins_counter:secs_counter.cents_counter";
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
			str = physicalPathKey.toString();
		else if (mediaType.equals("Countdown"))
			str = physicalPathKey.toString() + " - " + text;

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
					|| !text.equals(joBroadcastPlaylistItem.getString("text")))
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
				joBroadcastPlaylistItem.put("channelConfigurationLabel", channelConf.getLabel());
			else if (mediaType.equalsIgnoreCase("Media"))
				joBroadcastPlaylistItem.put("physicalPathKey", physicalPathKey);
			else if (mediaType.equalsIgnoreCase("Countdown"))
			{
				joBroadcastPlaylistItem.put("physicalPathKey", physicalPathKey);
				joBroadcastPlaylistItem.put("text", text);
			}
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
		List<ChannelConf> localChannelConfigurationList)
	{
		BroadcastPlaylistItem broadcastPlaylistItem = new BroadcastPlaylistItem(localChannelConfigurationList);

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
			}
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

		channelConf = null;
		for (ChannelConf localChannelConf: channelConfigurationList)
		{
			if (localChannelConf.getLabel().equalsIgnoreCase(channelConfigurationLabel))
			{
				channelConf = localChannelConf;

				break;
			}
		}
		if (channelConf == null)
		{
			mLogger.error("ChannelConf not found"
				+ ", channelConfigurationLabel: " + channelConfigurationLabel);
		}
	}

	public String getChannelConfigurationLabel() {
		return channelConfigurationLabel;
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


	public ChannelConf getChannelConf() {
		return channelConf;
	}


	public void setChannelConf(ChannelConf channelConf) {
		this.channelConf = channelConf;
	}


	public Long getPhysicalPathKey() {
		return physicalPathKey;
	}

	public void setPhysicalPathKey(Long physicalPathKey) {
		this.physicalPathKey = physicalPathKey;
	}

}
