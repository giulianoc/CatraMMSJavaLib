package com.catrammslib;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.catrammslib.entity.ChannelConf;
import com.catrammslib.utility.BroadcastPlaylistItem;
import com.catrammslib.utility.IngestionResult;
import com.catrammslib.utility.LiveProxyOutput;
import com.catrammslib.utility.MediaItemReference;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class CatraMMSBroadcaster {

    private static final Logger mLogger = Logger.getLogger(CatraMMSBroadcaster.class);

	static public Long addBroadcaster(String broadcasterConfigurationLabel,
		String broadcasterName, Date broadcasterStart, Date broadcasterEnd,
		BroadcastPlaylistItem broadcastDefaultPlaylistItem,
		String broadcasterCdnRtmp, String encodingProfileLabel,
		List<BroadcastPlaylistItem> broadcastPlaylistItems,
		CatraMMSAPI catraMMS, String username, String password)
		throws Exception
	{
		Long broadcasterIngestionJobKey = null;

		try
		{
			ChannelConf broadcasterChannelConf = null;
			String broadcastUdpURL;
			Long broadcasterConfKey;
			{
				List<ChannelConf> channelConfList = new ArrayList<>();
				catraMMS.getChannelConf(username, password, 0, 1,
						null, broadcasterConfigurationLabel, null, null,
						null, null, null, null, null,
						channelConfList);

				if (channelConfList.size() != 1)
				{
					mLogger.error("catraMMS.getChannelConf failed"
							+ ", broadcasterConfigurationLabel: " + broadcasterConfigurationLabel
					);

					throw new Exception("catraMMS.getChannelConf failed");
				}

				broadcasterChannelConf = channelConfList.get(0);
				broadcasterConfKey = broadcasterChannelConf.getConfKey();
				broadcastUdpURL = broadcasterChannelConf.getPushProtocol() + "://" 
					+ broadcasterChannelConf.getPushServerName() 
					+ ":" + broadcasterChannelConf.getPushServerPort();
			}

			Long broadcastIngestionJobKey = null;
			{
				String broadcastIngestionJobLabel = "Broadcast: " + broadcasterName;

				JSONObject joWorkflow = buildBroadcastJson(
					broadcasterStart, broadcasterEnd, encodingProfileLabel,
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

				JSONObject joWorkflow = buildBroadcasterJson(
					broadcasterIngestionJobLabel,		
					broadcasterConfigurationLabel,	// udp://<server>:<port>
					broadcasterStart, broadcasterEnd, encodingProfileLabel,
					broadcasterCdnRtmp,

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

		String broadcastIngestionJobLabel,
		String broadcastUdpURL,
		BroadcastPlaylistItem broadcastDefaultPlaylistItem
    )
	throws Exception
    {
		try
        {
            JSONObject joWorkflow = CatraMMSWorkflow.buildWorkflowRootJson(broadcastIngestionJobLabel);

            JSONObject joBroadcast = null;
            {
                List<LiveProxyOutput> liveProxyOutputList = new ArrayList<>();
				{
					LiveProxyOutput liveProxyOutput = new LiveProxyOutput();
					liveProxyOutput.setOutputType("UDP_Stream");
					liveProxyOutput.setUdpURL(broadcastUdpURL);
					liveProxyOutput.setEncodingProfileLabel(encodingProfileLabel);
					liveProxyOutput.setFadeDuration((long) 3);	// seconds

					liveProxyOutputList.add(liveProxyOutput);
				}
				if (broadcastDefaultPlaylistItem.getMediaType().equalsIgnoreCase("Live Channel"))
	                joBroadcast = CatraMMSWorkflow.buildLiveProxyJson(
						broadcastIngestionJobLabel,

						broadcastDefaultPlaylistItem.getChannelConfigurationLabel(),
						null,	// encodersPool,

						broadcasterStart, broadcasterEnd,

						null,
						null,
						null,
						null,
						null,

						liveProxyOutputList,
						null
        	        );
				else if (broadcastDefaultPlaylistItem.getMediaType().equalsIgnoreCase("Media"))
				{
					MediaItemReference mediaItemReference = new MediaItemReference();
					mediaItemReference.setPhysicalPathKey(broadcastDefaultPlaylistItem.getPhysicalPathKey());

					List<MediaItemReference> mediaItemReferenceList = new ArrayList<>();
					mediaItemReferenceList.add(mediaItemReference);

	                joBroadcast = CatraMMSWorkflow.buildVODProxyJson(
						broadcastIngestionJobLabel,

						mediaItemReferenceList,
						null,	// encodersPool,

						broadcasterStart, broadcasterEnd,

						null,
						liveProxyOutputList
        	        );
				}
				else if (broadcastDefaultPlaylistItem.getMediaType().equalsIgnoreCase("Countdown"))
				{
					MediaItemReference mediaItemReference = new MediaItemReference();
					mediaItemReference.setPhysicalPathKey(broadcastDefaultPlaylistItem.getPhysicalPathKey());

					List<MediaItemReference> mediaItemReferenceList = new ArrayList<>();
					mediaItemReferenceList.add(mediaItemReference);

					String textPosition_X_InPixel = "(video_width-text_width)/2";
					String textPosition_Y_InPixel = "(video_height-text_height)/2";
					String fontType = "OpenSans-ExtraBold.ttf";
					Long fontSize = (long) 48;
					String fontColor = "orange";
					Long textPercentageOpacity = (long) 100;
					Boolean boxEnable = false;
					String boxColor = null;
					Long boxPercentageOpacity = null;

					joBroadcast = CatraMMSWorkflow.buildCountdownJson(
						broadcastIngestionJobLabel,

						mediaItemReferenceList,
						null,	// encodersPool,

						broadcasterStart, broadcasterEnd,

						broadcastDefaultPlaylistItem.getText(),
						textPosition_X_InPixel,
						textPosition_Y_InPixel,
						fontType,
						fontSize,
						fontColor,
						textPercentageOpacity,
						boxEnable,
						boxColor,
						boxPercentageOpacity,

						liveProxyOutputList
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
		String broadcasterChannelConfigurationLabel,	// udp://<server>:<port>
		Date broadcasterStart, 
		Date broadcasterEnd,
		String encodingProfileLabel,
		String broadcasterCdnRtmp,

		Long broadcastIngestionJobKey,
		BroadcastPlaylistItem broadcastDefaultPlaylistItem
    )
	throws Exception
    {
		try
        {
            JSONObject joWorkflow = CatraMMSWorkflow.buildWorkflowRootJson(broadcasterIngestionJobLabel);

            JSONObject joBroadcaster;
            {
				JSONObject joExtraLiveProxyBroadcasterParameters = new JSONObject();
				joExtraLiveProxyBroadcasterParameters.put("broadcastDefaultPlaylistItem", 
					broadcastDefaultPlaylistItem.getJson());
				joExtraLiveProxyBroadcasterParameters.put("broadcastIngestionJobKey", broadcastIngestionJobKey);
				if (broadcasterCdnRtmp != null && !broadcasterCdnRtmp.isEmpty())
					joExtraLiveProxyBroadcasterParameters.put("cdnRtmp", broadcasterCdnRtmp);

				JSONObject joExtraLiveProxyInternalMMSParameters = new JSONObject();
				joExtraLiveProxyInternalMMSParameters.put("broadcaster", joExtraLiveProxyBroadcasterParameters);

                List<LiveProxyOutput> liveProxyOutputList = new ArrayList<>();
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
				if (broadcasterCdnRtmp != null && !broadcasterCdnRtmp.isEmpty())
				{
					LiveProxyOutput liveProxyOutput = new LiveProxyOutput();

					liveProxyOutput.setOutputType("RTMP_Stream");
					liveProxyOutput.setRtmpURL(broadcasterCdnRtmp);
					// liveProxyOutput.setPlayURL("https://1909844812.rsc.cdn77.org/1909844812/12376543/index.m3u8");

					liveProxyOutput.setEncodingProfileLabel(encodingProfileLabel);

					liveProxyOutputList.add(liveProxyOutput);
				}

                joBroadcaster = CatraMMSWorkflow.buildLiveProxyJson(
					broadcasterIngestionJobLabel,

					// i prossimi due parametri sono legati tra loro
					broadcasterChannelConfigurationLabel,
					null,	// encodersPool,

					broadcasterStart, broadcasterEnd,

					null,
					null,
					null,
					null,
					null,

					liveProxyOutputList,
					joExtraLiveProxyInternalMMSParameters
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
