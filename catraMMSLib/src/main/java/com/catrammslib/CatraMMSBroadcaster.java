package com.catrammslib;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.catrammslib.entity.Stream;
import com.catrammslib.utility.BroadcastPlaylistItem;
import com.catrammslib.utility.DrawTextDetails;
import com.catrammslib.utility.IngestionResult;
import com.catrammslib.utility.LiveProxyOutput;
import com.catrammslib.utility.MediaItemReference;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class CatraMMSBroadcaster {

    private static final Logger mLogger = Logger.getLogger(CatraMMSBroadcaster.class);

	static public Long addBroadcaster(String broadcasterConfigurationLabel,
		String broadcasterName, Date broadcasterStart, Date broadcasterEnd,
		BroadcastPlaylistItem broadcastDefaultPlaylistItem,
		String broadcastEncodersPool,
		String broadcasterCdnRtmp, String broadcasterCdnPlayURL, 
		String encodingProfileLabel,
		List<BroadcastPlaylistItem> broadcastPlaylistItems,
		CatraMMSAPI catraMMS, String username, String password)
		throws Exception
	{
		Long broadcasterIngestionJobKey = null;

		try
		{
			mLogger.info("Received addBroadcaster"
				+ ", broadcasterConfigurationLabel: " + broadcasterConfigurationLabel
				+ ", broadcasterName: " + broadcasterName
				+ ", broadcasterStart: " + broadcasterStart
				+ ", broadcasterEnd: " + broadcasterEnd
				+ ", broadcastEncodersPool: " + broadcastEncodersPool
				+ ", broadcasterCdnRtmp: " + broadcasterCdnRtmp
				+ ", broadcasterCdnPlayURL: " + broadcasterCdnPlayURL
				+ ", encodingProfileLabel: " + encodingProfileLabel
			);

			mLogger.info("looking for catraMMS.getChannelConf"
				+ ", broadcasterConfigurationLabel: " + broadcasterConfigurationLabel
			);
			Stream broadcasterStream = null;
			String broadcastUdpURL;
			{
				List<Stream> streamList = new ArrayList<>();
				catraMMS.getStream(username, password, 0, 1,
						null, broadcasterConfigurationLabel, false, null, null,
						null, null, null, null, null,
						streamList);

				if (streamList.size() != 1)
				{
					mLogger.error("catraMMS.getStream failed"
							+ ", broadcasterConfigurationLabel: " + broadcasterConfigurationLabel
					);

					throw new Exception("catraMMS.getStream failed");
				}

				broadcasterStream = streamList.get(0);
				broadcastUdpURL = broadcasterStream.getPushProtocol() + "://" 
					+ broadcasterStream.getPushServerName() 
					+ ":" + broadcasterStream.getPushServerPort();
			}

			Long broadcastIngestionJobKey = null;
			{
				String broadcastIngestionJobLabel = "Broadcast: " + broadcasterName;

				mLogger.info("buildBroadcastJson"
					+ ", broadcasterStart: " + broadcasterStart
					+ ", broadcasterEnd: " + broadcasterEnd
					+ ", encodingProfileLabel: " + encodingProfileLabel
					+ ", broadcastIngestionJobLabel: " + broadcastIngestionJobLabel
					+ ", broadcastUdpURL: " + broadcastUdpURL
					+ ", broadcastDefaultPlaylistItem: " + broadcastDefaultPlaylistItem
				);
				JSONObject joWorkflow = buildBroadcastJson(
					broadcasterStart, broadcasterEnd, encodingProfileLabel,

					// 2021-12-27: we are forcing here the broadcast to use the same encodersPool of the broadcaster
					// This is not mandatory but, since they communicate through udp, it is recommended
					// 2022-05-18: encoderspool removed because the broadcaster channel is IP_PUSH, for this reason it does not use
					//		the encoderspool but only pushServerName
					// 2022-05-20: added the broadcastEncodersPool parameter
					broadcastEncodersPool,
					broadcastIngestionJobLabel,
					broadcastUdpURL,

					broadcastDefaultPlaylistItem
				);
				mLogger.info("joWorkflow: " + joWorkflow.toString(4));

				List<IngestionResult> ingestionJobList = new ArrayList<>();

				IngestionResult workflowRoot = catraMMS.ingestWorkflow(username, password,
						joWorkflow.toString(4), ingestionJobList);

				for (IngestionResult ingestionResult: ingestionJobList)
				{
					if (ingestionResult.getLabel().equals(broadcastIngestionJobLabel))
					{
						broadcastIngestionJobKey = ingestionResult.getKey();

						break;
					}
				}

				if (broadcastIngestionJobKey == null)
				{
					String errorMessage = "broadcastIngestionJobKey is null!!!";
					mLogger.error(errorMessage);

					throw new Exception(errorMessage);
				}
			}

			{
				String broadcasterIngestionJobLabel = "Broadcaster: " + broadcasterName;

				mLogger.info("buildBroadcasterJson"
					+ ", broadcasterIngestionJobLabel: " + broadcasterIngestionJobLabel
					+ ", broadcasterConfigurationLabel: " + broadcasterConfigurationLabel
					+ ", broadcasterStart: " + broadcasterStart
					+ ", broadcasterEnd: " + broadcasterEnd
					+ ", encodingProfileLabel: " + encodingProfileLabel
					+ ", broadcasterCdnRtmp: " + broadcasterCdnRtmp
					+ ", broadcasterCdnPlayURL: " + broadcasterCdnPlayURL
					+ ", broadcastIngestionJobKey: " + broadcastIngestionJobKey
					+ ", broadcastDefaultPlaylistItem: " + broadcastDefaultPlaylistItem
				);
				JSONObject joWorkflow = buildBroadcasterJson(
					broadcasterIngestionJobLabel,		
					broadcasterConfigurationLabel,	// udp://<server>:<port>
					broadcasterStart, broadcasterEnd, encodingProfileLabel,
					broadcasterCdnRtmp, broadcasterCdnPlayURL,

					broadcastIngestionJobKey,
					broadcastDefaultPlaylistItem
				);
				mLogger.info("joWorkflow: " + joWorkflow.toString(4));

				List<IngestionResult> ingestionJobList = new ArrayList<>();

				IngestionResult workflowRoot = catraMMS.ingestWorkflow(username, password,
						joWorkflow.toString(4), ingestionJobList);

				for (IngestionResult ingestionResult: ingestionJobList)
				{
					if (ingestionResult.getLabel().equals(broadcasterIngestionJobLabel))
					{
						broadcasterIngestionJobKey = ingestionResult.getKey();

						break;
					}
				}

				if (broadcasterIngestionJobKey == null)
				{
					String errorMessage = "broadcasterIngestionJobKey is null!!!";
					mLogger.error(errorMessage);

					throw new Exception(errorMessage);
				}
			}

			if (broadcastPlaylistItems != null && broadcastPlaylistItems.size() > 0)
			{
				mLogger.info("changeLiveProxyPlaylist"
					+ ", broadcasterIngestionJobKey: " + broadcasterIngestionJobKey
					+ ", broadcastPlaylistItems: " + broadcastPlaylistItems
				);
				catraMMS.changeLiveProxyPlaylist(username, password, 
					broadcasterIngestionJobKey, broadcastPlaylistItems);
			}

			return broadcasterIngestionJobKey;
		}
        catch (Exception e)
        {
            String errorMessage = "Exception: " + e;
            mLogger.error(errorMessage);

			throw e;
        }
	}

    private static JSONObject buildBroadcastJson(
		Date broadcasterStart, 
		Date broadcasterEnd,
		String encodingProfileLabel,
		String encodersPoolLabel,

		String broadcastIngestionJobLabel,
		String broadcastUdpURL,
		BroadcastPlaylistItem broadcastDefaultPlaylistItem
    )
	throws Exception
    {
		try
        {
			mLogger.info("Received buildBroadcastJson"
				+ ", broadcasterStart: " + broadcasterStart
				+ ", broadcasterEnd: " + broadcasterEnd
				+ ", encodingProfileLabel: " + encodingProfileLabel
				+ ", encodersPoolLabel: " + encodersPoolLabel
				+ ", broadcastIngestionJobLabel: " + broadcastIngestionJobLabel
				+ ", broadcastUdpURL: " + broadcastUdpURL
			);

            JSONObject joWorkflow = CatraMMSWorkflow.buildWorkflowRootJson(broadcastIngestionJobLabel);

            JSONObject joBroadcast = null;
            {
                List<LiveProxyOutput> liveProxyOutputList = new ArrayList<>();
				{
					LiveProxyOutput liveProxyOutput = new LiveProxyOutput();
					liveProxyOutput.setOutputType("UDP_Stream");
					liveProxyOutput.setUdpURL(broadcastUdpURL);
					liveProxyOutput.setEncodingProfileLabel(encodingProfileLabel);
					{
						JSONObject joFilters = new JSONObject();
						
						JSONArray jaVideo = new JSONArray();
						joFilters.put("video", jaVideo);

						JSONObject joFade = new JSONObject();
						jaVideo.put(joFade);

						joFade.put("type", "fade");
						joFade.put("duration", 3);

						liveProxyOutput.setFilters(joFilters);
					}

					liveProxyOutputList.add(liveProxyOutput);
				}
				if (broadcastDefaultPlaylistItem.getMediaType().equalsIgnoreCase("Stream"))
				{
	                joBroadcast = CatraMMSWorkflow.buildLiveProxyJson(
						broadcastIngestionJobLabel,

						broadcastDefaultPlaylistItem.getStreamConfigurationLabel(),
						encodersPoolLabel,

						broadcasterStart, broadcasterEnd,

						null,
						null,
						null,
						null,
						null,

						liveProxyOutputList,
						null,
						true
        	        );
				}
				else if (broadcastDefaultPlaylistItem.getMediaType().equalsIgnoreCase("Media"))
				{
					List<MediaItemReference> mediaItemReferenceList = new ArrayList<>();

					for(Long physicalPathKey: broadcastDefaultPlaylistItem.getPhysicalPathKeys())
					{
						MediaItemReference mediaItemReference = new MediaItemReference();
						mediaItemReference.setPhysicalPathKey(physicalPathKey);
	
						mediaItemReferenceList.add(mediaItemReference);	
					}

	                joBroadcast = CatraMMSWorkflow.buildVODProxyJson(
						broadcastIngestionJobLabel,

						mediaItemReferenceList,
						encodersPoolLabel,

						broadcasterStart, broadcasterEnd,

						null,
						liveProxyOutputList,
						true
        	        );
				}
				else if (broadcastDefaultPlaylistItem.getMediaType().equalsIgnoreCase("Countdown"))
				{
					MediaItemReference mediaItemReference = new MediaItemReference();
					mediaItemReference.setPhysicalPathKey(broadcastDefaultPlaylistItem.getPhysicalPathKey());

					List<MediaItemReference> mediaItemReferenceList = new ArrayList<>();
					mediaItemReferenceList.add(mediaItemReference);

					String fontType = "OpenSans-ExtraBold.ttf";
					Long fontSize = (long) 48;
					String fontColor = "orange";
					Long textPercentageOpacity = (long) 100;
					Boolean boxEnable = false;
					String boxColor = null;
					Long boxPercentageOpacity = null;

					joBroadcast = CatraMMSWorkflow.buildCountdownJsonForBroadcast(
						broadcastIngestionJobLabel,

						mediaItemReferenceList,
						encodersPoolLabel,

						broadcasterStart, broadcasterEnd,

						broadcastDefaultPlaylistItem.getText(),
						broadcastDefaultPlaylistItem.getTextPosition_X_InPixel(),
						broadcastDefaultPlaylistItem.getTextPosition_Y_InPixel(),
						fontType,
						fontSize,
						fontColor,
						textPercentageOpacity,
						boxEnable,
						boxColor,
						boxPercentageOpacity,

						liveProxyOutputList,
						true
        	        );
				}
				else
				{
					mLogger.info("broadcastDefaultMedia has a wrong value"
                    	+ ", broadcastDefaultPlaylistItem.getMediaType: " + broadcastDefaultPlaylistItem.getMediaType());
				}

                joWorkflow.put("Task", joBroadcast);
            }

            mLogger.info("Ready for the ingest"
                    + ", json Workflow: " + joWorkflow.toString(4));

            return joWorkflow;
        }
        catch (Exception e)
        {
            String errorMessage = "buildBroadcastJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

	private static JSONObject buildBroadcasterJson(
		String broadcasterIngestionJobLabel,
		String broadcasterStreamConfigurationLabel,	
		Date broadcasterStart, 
		Date broadcasterEnd,
		String encodingProfileLabel,
		String broadcasterCdnRtmp, String broadcasterCdnPlayURL,

		Long broadcastIngestionJobKey,
		BroadcastPlaylistItem broadcastDefaultPlaylistItem
    )
	throws Exception
    {
		try
        {
			mLogger.info("Received buildBroadcasterJson"
				+ ", broadcasterIngestionJobLabel: " + broadcasterIngestionJobLabel
				+ ", broadcasterStreamConfigurationLabel: " + broadcasterStreamConfigurationLabel
				+ ", broadcasterStart: " + broadcasterStart
				+ ", broadcasterEnd: " + broadcasterEnd
				+ ", encodingProfileLabel: " + encodingProfileLabel
				+ ", broadcasterCdnRtmp: " + broadcasterCdnRtmp
				+ ", broadcasterCdnPlayURL: " + broadcasterCdnPlayURL
				+ ", broadcastIngestionJobKey: " + broadcastIngestionJobKey
			);

            JSONObject joWorkflow = CatraMMSWorkflow.buildWorkflowRootJson(broadcasterIngestionJobLabel);

            JSONObject joBroadcaster;
            {
				JSONObject joExtraLiveProxyBroadcasterParameters = new JSONObject();
				joExtraLiveProxyBroadcasterParameters.put("broadcastDefaultPlaylistItem", 
					broadcastDefaultPlaylistItem.getJson());
				joExtraLiveProxyBroadcasterParameters.put("broadcastIngestionJobKey", broadcastIngestionJobKey);
				if (broadcasterCdnRtmp != null && !broadcasterCdnRtmp.isEmpty())
					joExtraLiveProxyBroadcasterParameters.put("cdnRtmp", broadcasterCdnRtmp);
				if (broadcasterCdnPlayURL != null && !broadcasterCdnPlayURL.isEmpty())
					joExtraLiveProxyBroadcasterParameters.put("cdnPlayURL", broadcasterCdnPlayURL);

				JSONObject joExtraLiveProxyInternalMMSParameters = new JSONObject();
				joExtraLiveProxyInternalMMSParameters.put("broadcaster", joExtraLiveProxyBroadcasterParameters);

                List<LiveProxyOutput> liveProxyOutputList = new ArrayList<>();

				// 2022-09-14: l'output HLS crea problemi nel caso di encoder esterno.
				if (broadcasterCdnRtmp == null || broadcasterCdnRtmp.isEmpty())
				{
					LiveProxyOutput liveProxyOutput = new LiveProxyOutput();

					liveProxyOutput.setOutputType("HLS");
					liveProxyOutput.setDeliveryCode(new Date().getTime());
					// liveProxyOutput.setOutputType("RTMP_Stream");
					// liveProxyOutput.setRtmpURL("rtmp://prg-1.s.cdn77.com:1936/1909844812/12376543");
					// liveProxyOutput.setPlayURL("https://1909844812.rsc.cdn77.org/1909844812/12376543/index.m3u8");

					liveProxyOutput.setEncodingProfileLabel(encodingProfileLabel);

					liveProxyOutputList.add(liveProxyOutput);
				}
				else
				{
					LiveProxyOutput liveProxyOutput = new LiveProxyOutput();

					liveProxyOutput.setOutputType("RTMP_Stream");
					liveProxyOutput.setRtmpURL(broadcasterCdnRtmp);
					liveProxyOutput.setPlayURL(broadcasterCdnPlayURL);

					liveProxyOutput.setEncodingProfileLabel(encodingProfileLabel);

					liveProxyOutputList.add(liveProxyOutput);
				}

                joBroadcaster = CatraMMSWorkflow.buildLiveProxyJson(
					broadcasterIngestionJobLabel,

					// i prossimi due parametri sono legati tra loro
					broadcasterStreamConfigurationLabel,
					null,	// encodersPool,

					broadcasterStart, broadcasterEnd,

					null,
					null,
					null,
					null,
					null,

					liveProxyOutputList,
					joExtraLiveProxyInternalMMSParameters,
					null
                );
                joWorkflow.put("Task", joBroadcaster);
            }

            mLogger.info("Ready for the ingest"
                    + ", json Workflow: " + joWorkflow.toString(4));

            return joWorkflow;
        }
        catch (Exception e)
        {
            String errorMessage = "buildBroadcasterJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }
}
