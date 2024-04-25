package com.catrammslib.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.catrammslib.entity.CDN77ChannelConf;
import com.catrammslib.entity.HLSChannelConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.catrammslib.CatraMMSAPI;
import com.catrammslib.CatraMMSWorkflow;
import com.catrammslib.entity.IngestionJob;
import com.catrammslib.entity.Stream;
import com.catrammslib.utility.BroadcastPlaylistItem;
import com.catrammslib.utility.DrawTextDetails;
import com.catrammslib.utility.IngestionResult;
import com.catrammslib.utility.OutputStream;
import com.catrammslib.utility.MediaItemReference;

public class CatraMMSBroadcaster {

    private static final Logger mLogger = LoggerFactory.getLogger(CatraMMSBroadcaster.class);

	static public Long addBroadcaster(Stream broadcasterStream,
		String broadcasterName, Date broadcasterStart, Date broadcasterEnd,
		String broadcasterIngestionJobLabel,
		DrawTextDetails drawTextDetails,
		String broadcastIngestionJobLabel,
		BroadcastPlaylistItem broadcastDefaultPlaylistItem,
		String broadcastEncodersPoolLabel,
		String editBroadcasterDeliveryType, // HLS_Channel, CDN, CDN77
		CDN77ChannelConf editBroadcasterCdn77Channel,
		HLSChannelConf editBroadcasterHlsChannel,
		String encodingProfileLabel,
		List<BroadcastPlaylistItem> broadcastPlaylistItems,
		CatraMMSAPI catraMMS, String username, String password)
		throws Exception
	{
		Long broadcasterIngestionJobKey = null;

		try
		{
			mLogger.info("Received addBroadcaster"
				+ ", broadcasterStream.getLabel: " + broadcasterStream.getLabel()
				+ ", broadcasterName: " + broadcasterName
				+ ", broadcasterStart: " + broadcasterStart
				+ ", broadcasterEnd: " + broadcasterEnd
				+ ", broadcasterIngestionJobLabel: " + broadcasterIngestionJobLabel
				+ ", broadcastIngestionJobLabel: " + broadcastIngestionJobLabel
				+ ", broadcastEncodersPoolLabel: " + broadcastEncodersPoolLabel
				+ ", editBroadcasterDeliveryType: " + editBroadcasterDeliveryType
				+ ", editBroadcasterCdn77Channel: " + editBroadcasterCdn77Channel
				+ ", editBroadcasterHlsChannel: " + editBroadcasterHlsChannel
				+ ", encodingProfileLabel: " + encodingProfileLabel
			);

			String broadcastURL;
			{
				// it could be udp:// (we might have corrupt packets) or better rtmp://
				// 2022-10-22: it cannot be rtmp because rtmp is tcp protocol and the ffmpeg server command
				//	will disconnect at every playlist change causing
				//	- player disconnect
				//	- ffmpeg server exit causing time to reactivate
				//	So it HAS to be UDP
				if (!broadcasterStream.getPushProtocol().toLowerCase().equals("udp"))
				{
					String errorMessage = "Stream Push Protocol has to be 'udp'"
						+ ", broadcasterStream.getPushProtocol: " + broadcasterStream.getPushProtocol()
					;
					mLogger.error(errorMessage);

					throw new Exception(errorMessage);
				}
				broadcastURL = broadcasterStream.getPushProtocol() + "://" 
					+ broadcasterStream.getPushEncoderName()
					+ ":" + broadcasterStream.getPushServerPort();
			}

			Long broadcastIngestionJobKey = null;
			{
				// String broadcastIngestionJobLabel = "Broadcast: " + broadcasterName;

				mLogger.info("buildBroadcastJson"
					+ ", broadcasterStart: " + broadcasterStart
					+ ", broadcasterEnd: " + broadcasterEnd
					+ ", encodingProfileLabel: " + encodingProfileLabel
					+ ", broadcastIngestionJobLabel: " + broadcastIngestionJobLabel
					+ ", broadcastURL: " + broadcastURL
					+ ", broadcastDefaultPlaylistItem: " + broadcastDefaultPlaylistItem
				);
				JSONObject joWorkflow = buildBroadcastJson(
					broadcasterStart, broadcasterEnd, encodingProfileLabel,

					// 2021-12-27: we are forcing here the broadcast to use the same encodersPool of the broadcaster
					// This is not mandatory but, since they communicate through udp, it is recommended
					// 2022-05-18: encoderspool removed because the broadcaster channel is IP_PUSH, for this reason it does not use
					//		the encoderspool but only pushEncoderName
					// 2022-05-20: added the broadcastEncodersPool parameter
					broadcastEncodersPoolLabel,
					broadcastIngestionJobLabel,
					broadcastURL,

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
				// String broadcasterIngestionJobLabel = "Broadcaster: " + broadcasterName;

				mLogger.info("buildBroadcasterJson"
					+ ", broadcasterIngestionJobLabel: " + broadcasterIngestionJobLabel
					+ ", broadcasterStream.getLabel: " + broadcasterStream.getLabel()
					+ ", broadcasterStart: " + broadcasterStart
					+ ", broadcasterEnd: " + broadcasterEnd
					+ ", encodingProfileLabel: " + encodingProfileLabel
					+ ", broadcastIngestionJobKey: " + broadcastIngestionJobKey
					+ ", broadcastDefaultPlaylistItem: " + broadcastDefaultPlaylistItem
				);
				JSONObject joWorkflow = buildBroadcasterJson(
					broadcasterIngestionJobLabel,
					broadcasterStream.getLabel(),	// udp://<server>:<port>
					drawTextDetails,
					broadcasterStart, broadcasterEnd, encodingProfileLabel,
					editBroadcasterDeliveryType,
					editBroadcasterCdn77Channel, editBroadcasterHlsChannel,
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
					broadcasterIngestionJobKey, broadcastPlaylistItems,
					true);
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

    static public void killBroadcaster(IngestionJob broadcasterIngestionJob,
		CatraMMSAPI catraMMS, String username, String password)
		throws Exception
    {
        try
        {
            mLogger.info("Received killBroadcaster"
				+ ", broadcasterIngestionJob: " + broadcasterIngestionJob
			);

			if (broadcasterIngestionJob == null)
            {
                String errorMessage = "No IngestionJob found";
				mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }
            else if (broadcasterIngestionJob.getEncodingJob() == null)
            {
                String errorMessage = "No EncodingJob found";
				mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }
            else if (broadcasterIngestionJob.getEncodingJob().getEncodingJobKey() == null
            )
            {
                String errorMessage = "No EncodingJob found";
				mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }
            else if (broadcasterIngestionJob.getMetaDataContent() == null
				|| broadcasterIngestionJob.getMetaDataContent().isEmpty()
            )
            {
                String errorMessage = "No Broadcaster MetaDataContent found";
				mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

			JSONObject joBroadcasterMetadataContent = new JSONObject(broadcasterIngestionJob.getMetaDataContent());
			if (!joBroadcasterMetadataContent.has("internalMMS"))
			{
                String errorMessage = "No Broadcaster internalMMS found";
				mLogger.error(errorMessage);

                throw new Exception(errorMessage);
			}
			JSONObject joBroadcasterInternalMMS = joBroadcasterMetadataContent.getJSONObject("internalMMS");

			if (!joBroadcasterInternalMMS.has("broadcaster"))
			{
                String errorMessage = "No Broadcaster internalMMS->broadcaster found";
				mLogger.error(errorMessage);

                throw new Exception(errorMessage);
			}
			JSONObject joBroadcaster = joBroadcasterInternalMMS.getJSONObject("broadcaster");

			if (!joBroadcaster.has("broadcastIngestionJobKey"))
			{
                String errorMessage = "No Broadcaster internalMMS->broadcaster->broadcastIngestionJobKey found";
				mLogger.error(errorMessage);

                throw new Exception(errorMessage);
			}
			Long broadcastIngestionJobKey = joBroadcaster.getLong("broadcastIngestionJobKey");

			mLogger.info("catraMMS.getIngestionJob"
				+ ", broadcastIngestionJobKey: " + broadcastIngestionJobKey
			);

			boolean ingestionJobOutputs = false;
			IngestionJob broadcastIngestionJob = catraMMS.getIngestionJob(username, password,
				broadcastIngestionJobKey, ingestionJobOutputs,
				// 2022-12-18: Poichè si vuole killare, l'IngestionJob è già presente da un po
				false
			);

			if (broadcastIngestionJob.getEncodingJob() == null
				|| broadcastIngestionJob.getEncodingJob().getEncodingJobKey() == null
			)
			{
                String errorMessage = "No Broadcast EncodingJobKey found";
				mLogger.error(errorMessage);

                throw new Exception(errorMessage);
			}

			catraMMS.killEncodingJob(username, password,
				broadcastIngestionJob.getEncodingJob().getEncodingJobKey(),
				false);

            catraMMS.killEncodingJob(username, password,
                broadcasterIngestionJob.getEncodingJob().getEncodingJobKey(),
				false);
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
                List<OutputStream> outputStreamList = new ArrayList<>();
				{
					OutputStream outputStream = new OutputStream();
					// if (broadcastURL.startsWith("udp://"))
					{
						outputStream.setOutputType("UDP_Stream");
						outputStream.setUdpURL(broadcastUdpURL);
					}
					/*
					else // if (broadcastURL.startsWith("rtmp://"))
					{
						liveProxyOutput.setOutputType("RTMP_Stream");
						liveProxyOutput.setRtmpURL(broadcastURL);
					}
					*/
					outputStream.setEncodingProfileLabel(encodingProfileLabel);
					{
						outputStream.getFilters().setFade(true);
						outputStream.getFilters().setFade_Duration(3L);
						/*
						JSONObject joFilters = new JSONObject();
						
						JSONArray jaVideo = new JSONArray();
						joFilters.put("video", jaVideo);

						JSONObject joFade = new JSONObject();
						jaVideo.put(joFade);

						joFade.put("type", "fade");
						joFade.put("duration", 3);

						outputStream.setFilters(joFilters);
						 */
					}

					outputStreamList.add(outputStream);
				}
				if (broadcastDefaultPlaylistItem.getMediaType().equalsIgnoreCase("Stream"))
				{
	                joBroadcast = CatraMMSWorkflow.buildLiveProxyJsonForBroadcast(
						broadcastIngestionJobLabel,

						broadcastDefaultPlaylistItem.getStream().getLabel(),
						encodersPoolLabel,

						broadcasterStart, broadcasterEnd,

						null,
						null,
						null,
						null,
						null,

						broadcastDefaultPlaylistItem.getDrawTextEnable() ?
							broadcastDefaultPlaylistItem.getDrawTextDetails() : null,

							outputStreamList,
						null,
						true
        	        );
				}
				else if (broadcastDefaultPlaylistItem.getMediaType().equalsIgnoreCase("Media"))
				{
					List<MediaItemReference> mediaItemReferenceList = new ArrayList<>();

					JSONArray jaReferencePhysicalPathKeys = new JSONArray();
					if (broadcastDefaultPlaylistItem.getReferencePhysicalPathKeys() != null 
						&& !broadcastDefaultPlaylistItem.getReferencePhysicalPathKeys().isEmpty())
						jaReferencePhysicalPathKeys = new JSONArray(broadcastDefaultPlaylistItem.getReferencePhysicalPathKeys());
					for(int referenceIndex = 0; referenceIndex < jaReferencePhysicalPathKeys.length(); referenceIndex++)
					{
						JSONObject joReferencePhysicalPathKey = jaReferencePhysicalPathKeys.getJSONObject(referenceIndex);

						MediaItemReference mediaItemReference = new MediaItemReference();

						mediaItemReference.setPhysicalPathKey(joReferencePhysicalPathKey.getLong("physicalPathKey"));
						if (joReferencePhysicalPathKey.has("title") && !joReferencePhysicalPathKey.isNull("title"))
							mediaItemReference.setTitle(joReferencePhysicalPathKey.getString("title"));

						mediaItemReferenceList.add(mediaItemReference);	
					}

	                joBroadcast = CatraMMSWorkflow.buildVODProxyJsonForBroadcast(
						broadcastIngestionJobLabel,

						mediaItemReferenceList,
						encodersPoolLabel,

						broadcasterStart, broadcasterEnd,

						null,
						broadcastDefaultPlaylistItem.getDrawTextEnable() ?
							broadcastDefaultPlaylistItem.getDrawTextDetails() : null,
							outputStreamList,
						true
        	        );
				}
				else if (broadcastDefaultPlaylistItem.getMediaType().equalsIgnoreCase("Countdown"))
				{
					MediaItemReference mediaItemReference = new MediaItemReference();
					mediaItemReference.setPhysicalPathKey(broadcastDefaultPlaylistItem.getPhysicalPathKey());

					List<MediaItemReference> mediaItemReferenceList = new ArrayList<>();
					mediaItemReferenceList.add(mediaItemReference);

					joBroadcast = CatraMMSWorkflow.buildCountdownJsonForBroadcast(
						broadcastIngestionJobLabel,

						mediaItemReferenceList,
						encodersPoolLabel,

						broadcasterStart, broadcasterEnd,

						broadcastDefaultPlaylistItem.getDrawTextDetails(),

							outputStreamList,
						true
        	        );
				}
				else
				{
					mLogger.info("broadcastDefaultMedia has a wrong value"
                    	+ ", broadcastDefaultPlaylistItem.getMediaType: " + broadcastDefaultPlaylistItem.getMediaType());
				}

                joWorkflow.put("task", joBroadcast);
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
		DrawTextDetails drawTextDetails,
		Date broadcasterStart, 
		Date broadcasterEnd,
		String encodingProfileLabel,
		String editBroadcasterDeliveryType, // HLS_Channel, CDN, CDN77
		CDN77ChannelConf editBroadcasterCdn77Channel,
		HLSChannelConf editBroadcasterHlsChannel,

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
				+ ", editBroadcasterDeliveryType: " + editBroadcasterDeliveryType
				+ ", editBroadcasterCdn77Channel: " + editBroadcasterCdn77Channel
				+ ", editBroadcasterHlsChannel: " + editBroadcasterHlsChannel
				+ ", broadcastIngestionJobKey: " + broadcastIngestionJobKey
			);

            JSONObject joWorkflow = CatraMMSWorkflow.buildWorkflowRootJson(broadcasterIngestionJobLabel);

            JSONObject joBroadcaster;
            {
				JSONObject joExtraLiveProxyBroadcasterParameters = new JSONObject();
				joExtraLiveProxyBroadcasterParameters.put("broadcastDefaultPlaylistItem", 
					broadcastDefaultPlaylistItem.getJson());
				joExtraLiveProxyBroadcasterParameters.put("broadcastIngestionJobKey", broadcastIngestionJobKey);

				if (editBroadcasterDeliveryType != null && !editBroadcasterDeliveryType.isEmpty())
					joExtraLiveProxyBroadcasterParameters.put("deliveryType", editBroadcasterDeliveryType);
				if (editBroadcasterDeliveryType.equals("HLS_Channel"))
				{
					if (editBroadcasterHlsChannel != null)
						joExtraLiveProxyBroadcasterParameters.put("hlsConfigurationLabel", editBroadcasterHlsChannel.getLabel());
				}
				else if (editBroadcasterDeliveryType.equals("CDN77"))
				{
					if (editBroadcasterCdn77Channel != null)
						joExtraLiveProxyBroadcasterParameters.put("cdn77ConfigurationLabel", editBroadcasterCdn77Channel.getLabel());
				}
				else
				{
					mLogger.error("unknown editBroadcasterDeliveryType"
							+ ", editBroadcasterDeliveryType: " + editBroadcasterDeliveryType
					);
				}

				JSONObject joExtraLiveProxyInternalMMSParameters = new JSONObject();
				joExtraLiveProxyInternalMMSParameters.put("broadcaster", joExtraLiveProxyBroadcasterParameters);

                List<OutputStream> outputStreamList = new ArrayList<>();

				// 2022-09-14: l'output HLS crea problemi nel caso di encoder esterno.
				if (editBroadcasterDeliveryType.equals("HLS_Channel"))
				{
					OutputStream outputStream = new OutputStream();

					outputStream.setOutputType("HLS_Channel");
					if (editBroadcasterHlsChannel != null)
						outputStream.setHlsChannel(editBroadcasterHlsChannel);

					outputStream.setEncodingProfileLabel(encodingProfileLabel);

					if (drawTextDetails != null){
						outputStream.getFilters().setDrawText(true);
						outputStream.getFilters().setDrawTextDetails(drawTextDetails);
					}

					outputStreamList.add(outputStream);
				}
				else if (editBroadcasterDeliveryType.equals("CDN77"))
				{
					OutputStream outputStream = new OutputStream();

					outputStream.setOutputType("CDN_CDN77");
					if (editBroadcasterCdn77Channel != null)
						outputStream.setCdn77Channel(editBroadcasterCdn77Channel);

					outputStream.setEncodingProfileLabel(encodingProfileLabel);

					if (drawTextDetails != null){
						outputStream.getFilters().setDrawText(true);
						outputStream.getFilters().setDrawTextDetails(drawTextDetails);
					}

					outputStreamList.add(outputStream);
				}
				else
				{
					mLogger.error("unknown editBroadcasterDeliveryType"
							+ ", editBroadcasterDeliveryType: " + editBroadcasterDeliveryType
					);
				}

                joBroadcaster = CatraMMSWorkflow.buildLiveProxyJson(
					broadcasterIngestionJobLabel,

					// i parametri Label e encodersPool sono legati tra loro
					broadcasterStreamConfigurationLabel, null,
					null,	// encodersPool,

					broadcasterStart, broadcasterEnd,

					null,
					null,
					null,
					null,
					null,

						outputStreamList,
					joExtraLiveProxyInternalMMSParameters,
					null, null
                );
                joWorkflow.put("task", joBroadcaster);
            }

            mLogger.info("buildBroadcasterJson, ready for the ingest"
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
