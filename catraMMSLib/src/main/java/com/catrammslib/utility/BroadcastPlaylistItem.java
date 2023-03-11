package com.catrammslib.utility;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
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

	// in case of Stream
	private Stream stream;

	// in case of Media
	private Boolean endBasedOnMediaDuration;
	private StringBuilder referencePhysicalPathKeys = new StringBuilder();		// in case of Media (JSONArray of References come i Task)

	// in case of Countdown
	private Long physicalPathKey;
	// drawTextDetails is mandatory in case of Countdown, it is optional for the other media.
	// Per i media 'non-countdown', drawTextEnable indica se drawTextDetails deve essere usato oppure no
	private DrawTextDetails drawTextDetails = new DrawTextDetails(null);
	private Boolean drawTextEnable;


	// got from physicalPathKey or referencePhysicalPathKeys
	private List<MediaItem> mediaItems = new ArrayList<>();

	// in case of Direct URL
	private String url;


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
		mediaType = "Media";

		drawTextEnable = false;

		endBasedOnMediaDuration = false;
	}

	@Override
	public int compareTo(BroadcastPlaylistItem broadcastPlaylistItem) {
        return getStart().compareTo(broadcastPlaylistItem.getStart());
	}

	@Override
	public String toString() // used in broadcasterEditorPlaylist.xhtml and broadcaster.xhtml
	{
		String str = "";

		if (mediaType.equals("Stream"))
		{
			if (stream != null) {
				str = "<b>" + stream.getConfKey() + "</b>: ";
				str += stream.getLabel();
			}
		}
		else if (mediaType.equals("Media"))
		{
			try
			{
				if (!referencePhysicalPathKeys.toString().trim().isEmpty())
				{
					JSONArray jaReferencePhysicalPathKeys = new JSONArray(referencePhysicalPathKeys.toString());
					for (int physicalPathKeyIndex = 0; physicalPathKeyIndex < jaReferencePhysicalPathKeys.length(); physicalPathKeyIndex++)
					{
						JSONObject joReferencePhysicalPathKey = jaReferencePhysicalPathKeys.getJSONObject(physicalPathKeyIndex);
	
						Long localPhysicalPathKey = joReferencePhysicalPathKey.getLong("physicalPathKey");
						MediaItem mediaItem = null;
						if (mediaItems.size() > physicalPathKeyIndex)
							mediaItem = mediaItems.get(physicalPathKeyIndex);
		
						String mediaItemDetails = "";
						if (mediaItem != null)
						{
							mediaItemDetails = ": " + mediaItem.getTitle();
		
							if (mediaItem.getSourcePhysicalPath() != null && mediaItem.getSourcePhysicalPath().getDurationInMilliSeconds() != null)
							{
								SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
								dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
								Date durationDate = new Date(mediaItem.getSourcePhysicalPath().getDurationInMilliSeconds());
		
								mediaItemDetails += (" - <b>Duration</b>: " + dateFormat.format(durationDate));
							}
						}
		
						if (str != "")
							str += "</br>";
						str += (
							"<b>" + localPhysicalPathKey.toString() + "</b>"
							+ mediaItemDetails
						);	
					}
				}
			}
			catch(Exception e)
			{
				mLogger.error("Exception: " + e);
			}
		}
		else if (mediaType.equals("Countdown"))
		{
			MediaItem mediaItem = null;
			if (mediaItems.size() > 0)
				mediaItem = mediaItems.get(0);

			String mediaItemDetails = "";
			if (mediaItem != null)
			{
				mediaItemDetails = ": " + mediaItem.getTitle();

				if (mediaItem.getSourcePhysicalPath() != null && mediaItem.getSourcePhysicalPath().getDurationInMilliSeconds() != null)
					mediaItemDetails += (", <b>Dur (secs)</b>: " + (mediaItem.getSourcePhysicalPath().getDurationInMilliSeconds() / 1000));
			}
			
			str = "<b>" + (physicalPathKey == null ? "" : physicalPathKey.toString()) + "</b>" 
				+ mediaItemDetails;
		}
		else if (mediaType.equals("Direct URL"))
			str = url;

		return str;
	}

	public String durationAsString()
	{
		try
		{
			if (start != null && end != null)
			{
				SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
				dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

				Date durationDate = new Date(end.getTime() - start.getTime());	// 1970-01-01 + (end-start)
	
				Date epochDate = new SimpleDateFormat("yyyy-MM-dd").parse("1970-01-01");
	
				Long days = (durationDate.getTime() - epochDate.getTime()) / (1000 * 60 * 60 * 24);

				if (TimeZone.getDefault().inDaylightTime(new Date()))
					durationDate = new Date(durationDate.getTime() - (3600 * 1000));

				return (days + " days " + dateFormat.format(durationDate));
			}
	
			return "";	
		}
		catch (Exception e)
		{
			mLogger.error("Exception: " + e);

			return "";	
		}
	}

	public boolean isEqualsTo(JSONObject joBroadcastPlaylistItem) 
	{
		try
		{
			if (!mediaType.equals(joBroadcastPlaylistItem.getString("mediaType")))
				return false;

			if (mediaType.equals("Stream"))
			{
				if (stream == null
					|| !stream.getLabel().equals(joBroadcastPlaylistItem.getString("streamConfigurationLabel")))
					return false;
			}
			else if (mediaType.equals("Media"))
			{
				if (!joBroadcastPlaylistItem.has("referencePhysicalPathKeys"))
					return false;

				JSONArray jaReferencePhysicalPathKeys = joBroadcastPlaylistItem.getJSONArray("referencePhysicalPathKeys");
				JSONArray jaLocalReferencePhysicalPathKeys = new JSONArray(referencePhysicalPathKeys.toString());

				if (jaLocalReferencePhysicalPathKeys.length() != jaReferencePhysicalPathKeys.length())
					return false;

				for (int physicalPathKeyIndex = 0; physicalPathKeyIndex < jaLocalReferencePhysicalPathKeys.length(); physicalPathKeyIndex++)
				{
					Long physicalPathKey_1 = jaLocalReferencePhysicalPathKeys.getJSONObject(physicalPathKeyIndex).getLong("physicalPathKey");
					Long physicalPathKey_2 = jaReferencePhysicalPathKeys.getJSONObject(physicalPathKeyIndex).getLong("physicalPathKey");

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
					/*
					|| !drawTextDetails.getText().equals(joBroadcastPlaylistItem.getString("text"))
					|| !drawTextDetails.getPositionXInPixel().equals(joBroadcastPlaylistItem.getString("textPosition_X_InPixel"))
					|| !drawTextDetails.getPositionYInPixel().equals(joBroadcastPlaylistItem.getString("textPosition_Y_InPixel"))
					*/
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
				if (stream != null)
					joBroadcastPlaylistItem.put("streamConfigurationLabel", stream.getLabel());

				if (drawTextEnable)
					joBroadcastPlaylistItem.put("drawTextDetails", drawTextDetails.toJson());
			}
			else if (mediaType.equalsIgnoreCase("Media"))
			{
				JSONArray jaLocalReferencePhysicalPathKeys = new JSONArray(referencePhysicalPathKeys.toString());
				joBroadcastPlaylistItem.put("referencePhysicalPathKeys", jaLocalReferencePhysicalPathKeys);

				if (drawTextEnable)
					joBroadcastPlaylistItem.put("drawTextDetails", drawTextDetails.toJson());
			}
			else if (mediaType.equalsIgnoreCase("Countdown"))
			{
				joBroadcastPlaylistItem.put("physicalPathKey", physicalPathKey);

				joBroadcastPlaylistItem.put("drawTextDetails", drawTextDetails.toJson());
			}
			else if (mediaType.equalsIgnoreCase("Direct URL"))
			{
				joBroadcastPlaylistItem.put("url", url);

				if (drawTextEnable)
					joBroadcastPlaylistItem.put("drawTextDetails", drawTextDetails.toJson());
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
	
	// this method is called to fill BroadcastPlaylistItem when the IngestionJob
	// is loaded from DB
	static public BroadcastPlaylistItem fromBroadcasterJson(JSONObject joBroadcastPlaylistItem,
		CatraMMSAPI localCatraMMS, String localUsername, String localPassword)
	{
		BroadcastPlaylistItem broadcastPlaylistItem = 
			new BroadcastPlaylistItem(localCatraMMS, localUsername, localPassword);

		try
		{
			broadcastPlaylistItem.setMediaType(joBroadcastPlaylistItem.getString("mediaType"));
			if (broadcastPlaylistItem.getMediaType().equalsIgnoreCase("Stream"))
			{
				String streamConfigurationLabel = joBroadcastPlaylistItem.getString("streamConfigurationLabel");
				// broadcastPlaylistItem.setStreamConfigurationLabel(joBroadcastPlaylistItem.getString("streamConfigurationLabel"));
				{
					List<Stream> streamList = new ArrayList<>();
					localCatraMMS.getStream(localUsername, localPassword, 0, 1, null,
							streamConfigurationLabel, false,
							null, null, null, null, null, null, null, streamList);
					if (streamList.size() > 0)
						broadcastPlaylistItem.setStream(streamList.get(0));
				}

				if (joBroadcastPlaylistItem.has("drawTextDetails"))
				{
					broadcastPlaylistItem.getDrawTextDetails().fromJson(
						joBroadcastPlaylistItem.getJSONObject("drawTextDetails"));
					broadcastPlaylistItem.setDrawTextEnable(true);
				}
				else
					broadcastPlaylistItem.setDrawTextEnable(false);
			}
			else if (broadcastPlaylistItem.getMediaType().equalsIgnoreCase("Media"))
			{
				if (joBroadcastPlaylistItem.has("referencePhysicalPathKeys"))
				{
					JSONArray jaReferencePhysicalPathKeys = joBroadcastPlaylistItem.getJSONArray("referencePhysicalPathKeys");
					for (int physicalPathKeyIndex = 0; physicalPathKeyIndex < jaReferencePhysicalPathKeys.length(); physicalPathKeyIndex++)
					{
						JSONObject joReferencePhysicalPathKey = jaReferencePhysicalPathKeys.getJSONObject(physicalPathKeyIndex);

						mLogger.info("addReferencePhysicalPathKey"
							+ ", joReferencePhysicalPathKey.toString: " + joReferencePhysicalPathKey.toString()
						);
						broadcastPlaylistItem.addReferencePhysicalPathKey(joReferencePhysicalPathKey);
					}
				}

				if (joBroadcastPlaylistItem.has("drawTextDetails"))
				{
					broadcastPlaylistItem.getDrawTextDetails().fromJson(
						joBroadcastPlaylistItem.getJSONObject("drawTextDetails"));
					broadcastPlaylistItem.setDrawTextEnable(true);
				}
				else
					broadcastPlaylistItem.setDrawTextEnable(false);
			}
			else if (broadcastPlaylistItem.getMediaType().equalsIgnoreCase("Countdown"))
			{
				broadcastPlaylistItem.setPhysicalPathKey(joBroadcastPlaylistItem.getLong("physicalPathKey"));

				if (joBroadcastPlaylistItem.has("drawTextDetails"))
				{
					broadcastPlaylistItem.getDrawTextDetails().fromJson(
						joBroadcastPlaylistItem.getJSONObject("drawTextDetails"));
				}
			}
			else if (broadcastPlaylistItem.getMediaType().equalsIgnoreCase("Direct URL"))
			{
				broadcastPlaylistItem.setUrl(joBroadcastPlaylistItem.getString("url"));

				if (joBroadcastPlaylistItem.has("drawTextDetails"))
				{
					broadcastPlaylistItem.getDrawTextDetails().fromJson(
						joBroadcastPlaylistItem.getJSONObject("drawTextDetails"));
					broadcastPlaylistItem.setDrawTextEnable(true);
				}
				else
					broadcastPlaylistItem.setDrawTextEnable(false);
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

	static public BroadcastPlaylistItem fromBroadcastJson(JSONObject joInputRoot,
		Date broadcastInputStart, Date broadcastInputEnd,
		CatraMMSAPI catraMMS, String username, String password)
	throws Exception
	{
		mLogger.info("fromBroadcastJson"
			+ ", joInputRoot: " + joInputRoot.toString()
		);

		BroadcastPlaylistItem broadcastPlaylistItem = new BroadcastPlaylistItem(catraMMS, username, password);

		if (joInputRoot.has("streamInput"))
		{
			JSONObject joBroadcastStreamInput = joInputRoot.getJSONObject("streamInput");

			broadcastPlaylistItem.setMediaType("Stream");

			// broadcastPlaylistItem.setStreamConfigurationLabel(
			//	joBroadcastStreamInput.getString("streamConfigurationLabel"));

			Stream stream = catraMMS.getStream(username, password,
				joBroadcastStreamInput.getLong("streamConfKey"));
			broadcastPlaylistItem.setStream(stream);

			if (joBroadcastStreamInput.has("drawTextDetails") && !joBroadcastStreamInput.isNull("drawTextDetails"))
			{
				broadcastPlaylistItem.setDrawTextEnable(true);

				broadcastPlaylistItem.getDrawTextDetails().fromJson(joBroadcastStreamInput.getJSONObject("drawTextDetails"));
			}
			else
				broadcastPlaylistItem.setDrawTextEnable(false);

			broadcastPlaylistItem.setStart(broadcastInputStart);
			broadcastPlaylistItem.setEnd(broadcastInputEnd);
		}
		else if (joInputRoot.has("vodInput"))
		{
			/*
				{ "timePeriod": true, "utcScheduleEnd": 1654318500, "utcScheduleStart": 1654235700, 
					"vodInput": { 
						"sources": [{ "physicalPathKey": 339474, "sourcePhysicalPathName": "/var/catramms/storage/MMSRepository/MMS_0005/1/000/002/646/826490_overlayedtext.mp4" }], 
						"vodContentType": "Video" 
					} 
				}
			*/
			JSONObject joBroadcastVODInput = joInputRoot.getJSONObject("vodInput");

			broadcastPlaylistItem.setMediaType("Media");

			if (joBroadcastVODInput.has("sources"))
			{
				JSONArray jaSources = joBroadcastVODInput.getJSONArray("sources");

				for(int sourceIndex = 0; sourceIndex < jaSources.length(); sourceIndex++)
				{
					JSONObject joSource = jaSources.getJSONObject(sourceIndex);

					if (joSource.has("physicalPathKey"))
					{
						JSONObject joReferencePhysicalPathKey = new JSONObject();
						joReferencePhysicalPathKey.put("physicalPathKey", joSource.getLong("physicalPathKey"));
						if (joSource.has("mediaItemTitle") && !joSource.isNull("mediaItemTitle"))
							joReferencePhysicalPathKey.put("mediaItemTitle", joSource.getString("mediaItemTitle"));

						broadcastPlaylistItem.addReferencePhysicalPathKey(joReferencePhysicalPathKey);
					}
				}
			}

			if (joBroadcastVODInput.has("drawTextDetails") && !joBroadcastVODInput.isNull("drawTextDetails"))
			{
				broadcastPlaylistItem.setDrawTextEnable(true);

				broadcastPlaylistItem.getDrawTextDetails().fromJson(joBroadcastVODInput.getJSONObject("drawTextDetails"));
			}
			else
			{
				broadcastPlaylistItem.setDrawTextEnable(false);
			}

			broadcastPlaylistItem.setStart(broadcastInputStart);
			broadcastPlaylistItem.setEnd(broadcastInputEnd);
		}
		else if (joInputRoot.has("countdownInput"))
		{
			JSONObject joBroadcastCountdownInput = joInputRoot.getJSONObject("countdownInput");

			broadcastPlaylistItem.setMediaType("Countdown");

			broadcastPlaylistItem.setPhysicalPathKey(joBroadcastCountdownInput.getLong("physicalPathKey"));
			if (joBroadcastCountdownInput.has("drawTextDetails"))
				broadcastPlaylistItem.getDrawTextDetails().fromJson(
					joBroadcastCountdownInput.getJSONObject("drawTextDetails"));

			{
				broadcastPlaylistItem.setDrawTextEnable(true);

				broadcastPlaylistItem.getDrawTextDetails().fromJson(joBroadcastCountdownInput.getJSONObject("drawTextDetails"));
			}

			broadcastPlaylistItem.setStart(broadcastInputStart);
			broadcastPlaylistItem.setEnd(broadcastInputEnd);
		}
		else if (joInputRoot.has("directURLInput"))
		{
			JSONObject joBroadcastDirectURLInput = joInputRoot.getJSONObject("directURLInput");

			broadcastPlaylistItem.setMediaType("Direct URL");

			broadcastPlaylistItem.setUrl(joBroadcastDirectURLInput.getString("url"));

			if (joBroadcastDirectURLInput.has("drawTextDetails") && !joBroadcastDirectURLInput.isNull("drawTextDetails"))
			{
				broadcastPlaylistItem.setDrawTextEnable(true);

				broadcastPlaylistItem.getDrawTextDetails().fromJson(joBroadcastDirectURLInput.getJSONObject("drawTextDetails"));
			}
			else
				broadcastPlaylistItem.setDrawTextEnable(false);

			broadcastPlaylistItem.setStart(broadcastInputStart);
			broadcastPlaylistItem.setEnd(broadcastInputEnd);
		}
		else
		{
			mLogger.error("Unknown input type: " + joInputRoot.toString());

			throw new Exception("Unknown input type: " + joInputRoot.toString());
		}

		return broadcastPlaylistItem;
	}

	public BroadcastPlaylistItem clone()
	{
		BroadcastPlaylistItem broadcastPlaylistItem = new BroadcastPlaylistItem(catraMMS, username, password);

		broadcastPlaylistItem.setTimestamp(getTimestamp());
		broadcastPlaylistItem.setStart(getStart());
		broadcastPlaylistItem.setEnd(getEnd());
		// broadcastPlaylistItem.setStreamConfigurationLabel(getStreamConfigurationLabel());
		broadcastPlaylistItem.setStream(getStream());
		broadcastPlaylistItem.setMediaType(getMediaType());
		// setEndBasedOnMediaDuration deve essere prima di setPhysicalPathKeys (che usa questo flag)
		broadcastPlaylistItem.setEndBasedOnMediaDuration(getEndBasedOnMediaDuration());
		broadcastPlaylistItem.setReferencePhysicalPathKeys(getReferencePhysicalPathKeys());
		broadcastPlaylistItem.setPhysicalPathKey(getPhysicalPathKey());
		broadcastPlaylistItem.setUrl(getUrl());
		broadcastPlaylistItem.setDrawTextEnable(getDrawTextEnable());
		broadcastPlaylistItem.setDrawTextDetails(getDrawTextDetails().clone());

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

					if (drawTextEnable)
						joStreamInput.put("drawTextDetails", drawTextDetails.toJson());	
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

				JSONArray jaReferencePhysicalPathKeys = new JSONArray(getReferencePhysicalPathKeys());
				for (int physicalPathKeyIndex = 0; physicalPathKeyIndex < jaReferencePhysicalPathKeys.length(); physicalPathKeyIndex++)
				{
					JSONObject joReferencePhysicalPathKey = jaReferencePhysicalPathKeys.getJSONObject(physicalPathKeyIndex);

					JSONObject joSource = new JSONObject();
					jaSources.put(joSource);
	
					joSource.put("physicalPathKey", joReferencePhysicalPathKey.getLong("physicalPathKey"));
					if (joReferencePhysicalPathKey.has("mediaItemTitle") && !joReferencePhysicalPathKey.isNull("mediaItemTitle"))
						joSource.put("mediaItemTitle", joReferencePhysicalPathKey.getString("mediaItemTitle"));
				}

				if (drawTextEnable)
					joVODInput.put("drawTextDetails", drawTextDetails.toJson());	
			}
			else if (getMediaType().equalsIgnoreCase("Countdown"))
			{
				JSONObject joCountdownInput = new JSONObject();
				joInputRoot.put("countdownInput", joCountdownInput);

				joCountdownInput.put("physicalPathKey", getPhysicalPathKey());

				joCountdownInput.put("drawTextDetails", drawTextDetails.toJson());
			}
			else if (getMediaType().equalsIgnoreCase("Direct URL"))
			{
				JSONObject joDirectURLInput = new JSONObject();
				joInputRoot.put("directURLInput", joDirectURLInput);

				joDirectURLInput.put("url", getUrl());

				if (drawTextEnable)
					joDirectURLInput.put("drawTextDetails", drawTextDetails.toJson());	
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

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
		if (mediaType.equals("Countdown"))
			drawTextDetails.setCountdown(true);
		else
			drawTextDetails.setCountdown(false);
	}

	/*
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
	 */

	public void setReferencePhysicalPathKeys(String localReferencesPhysicalPathKeys) 
	{

		referencePhysicalPathKeys.delete(0, referencePhysicalPathKeys.length());
		mediaItems.clear();

		if (localReferencesPhysicalPathKeys != null && !localReferencesPhysicalPathKeys.isEmpty())
		{
			try
			{
				JSONArray jaReferencePhysicalPathKeys = new JSONArray(localReferencesPhysicalPathKeys);

				for (int physicalPathKeyIndex = 0; physicalPathKeyIndex < jaReferencePhysicalPathKeys.length(); physicalPathKeyIndex++)
				{
					JSONObject joReferencePhysicalPathKey = jaReferencePhysicalPathKeys.getJSONObject(physicalPathKeyIndex);
				
					mLogger.info("addReferencePhysicalPathKey"
						+ ", joReferencePhysicalPathKey: " + joReferencePhysicalPathKey.toString()
					);
	
					addReferencePhysicalPathKey(joReferencePhysicalPathKey);
				}	
			}
			catch (Exception e)
			{
				mLogger.error("Exception: " + e.getMessage());
			}
		}
	}

	public void setPhysicalPathKey(Long localPhysicalPathKey) 
	{
		this.physicalPathKey = localPhysicalPathKey;

		if (localPhysicalPathKey == null)
		{
			mLogger.warn("localPhysicalPathKey is null"
			);

			return;
		}

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

		if (mediaItem != null)
		{
			if (mediaItems.size() <= positionIndex)
				mediaItems.add(mediaItem);
			else
				mediaItems.set(positionIndex, mediaItem);	
		}
		else
			mLogger.error("MediaItem is not found"
				+ ", localPhysicalPathKey: " + localPhysicalPathKey
			);
	}

	public int addReferencePhysicalPathKey(JSONObject joReferencePhysicalPathKey)
	{
		try
		{
			int positionIndex;

			if (joReferencePhysicalPathKey == null 
				|| !joReferencePhysicalPathKey.has("physicalPathKey")
				|| joReferencePhysicalPathKey.isNull("physicalPathKey")
			)
			{
				mLogger.warn("localPhysicalPathKey is null"
				);

				return 0;
			}

			// inizializzo jaReferencePhysicalPathKeys con i dati attuali (referencePhysicalPathKeys)
			JSONArray jaReferencePhysicalPathKeys = new JSONArray();
			if (!referencePhysicalPathKeys.toString().isEmpty())
				jaReferencePhysicalPathKeys = new JSONArray(referencePhysicalPathKeys.toString());

			// aggiungo joReferencePhysicalPathKey
			jaReferencePhysicalPathKeys.put(joReferencePhysicalPathKey);

			referencePhysicalPathKeys.delete(0, referencePhysicalPathKeys.length());
			referencePhysicalPathKeys.append(jaReferencePhysicalPathKeys.toString(2));

			positionIndex = jaReferencePhysicalPathKeys.length() - 1;

			Long physicalPathKey = joReferencePhysicalPathKey.getLong("physicalPathKey");

			MediaItem mediaItem = catraMMS.getMediaItemByPhysicalPathKey(username, password, physicalPathKey);

			if (mediaItem != null)
			{
				if (mediaItems.size() <= positionIndex)
					mediaItems.add(mediaItem);
				else
					mediaItems.set(positionIndex, mediaItem);
			}
			else
				mLogger.error("MediaItem is not found"
					+ ", physicalPathKey: " + physicalPathKey
				);
			
			if (endBasedOnMediaDuration != null && endBasedOnMediaDuration
				&& mediaItems != null && start != null)
			{
				Long durationInMilliSeconds = (long) 0;
				for(MediaItem localMediaItem: mediaItems)
				{
					if (localMediaItem.getSourcePhysicalPath() != null
						&& localMediaItem.getSourcePhysicalPath().getDurationInMilliSeconds() != null)
						durationInMilliSeconds += localMediaItem.getSourcePhysicalPath().getDurationInMilliSeconds();
				}

				Calendar calendar = Calendar.getInstance();
				calendar.setTime(start);
				calendar.add(Calendar.MILLISECOND, durationInMilliSeconds.intValue());
				end = calendar.getTime();
			}

			return positionIndex;
		}
		catch (Exception e)
		{
			mLogger.error("Exception: " + e.getMessage());

			return 0;
		}
	}

	public StringBuilder getStringBuilderReferencePhysicalPathKeys() {
		return referencePhysicalPathKeys;
	}

	public String getReferencePhysicalPathKeys() {
		return referencePhysicalPathKeys.toString();
	}

	/*
	public String getStreamConfigurationLabel() {
		return streamConfigurationLabel;
	}
	 */

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

	public Boolean getEndBasedOnMediaDuration() {
		return endBasedOnMediaDuration;
	}

	public void setEndBasedOnMediaDuration(Boolean endBasedOnMediaDuration) {
		this.endBasedOnMediaDuration = endBasedOnMediaDuration;
	}

	public DrawTextDetails getDrawTextDetails() {
		return drawTextDetails;
	}

	public void setDrawTextDetails(DrawTextDetails drawTextDetails) {
		this.drawTextDetails = drawTextDetails;
	}

	public Boolean getDrawTextEnable() {
		return drawTextEnable;
	}

	public void setDrawTextEnable(Boolean drawTextEnable) {
		this.drawTextEnable = drawTextEnable;
	}

	public Long getPhysicalPathKey() {
		return physicalPathKey;
	}
}
