package com.catrammslib.helper;

import java.util.*;

import com.catrammslib.entity.*;
import com.catrammslib.utility.*;
import com.catrammslib.utility.filters.Filter;
import com.catrammslib.utility.filters.Filters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.catrammslib.CatraMMSAPI;
import com.catrammslib.CatraMMSWorkflow;

public class CatraMMSBroadcaster {

    private static final Logger mLogger = LoggerFactory.getLogger(CatraMMSBroadcaster.class);

	static public Long addBroadcaster(Stream broadcasterStream,
		String broadcasterName, Date broadcasterStart, Date broadcasterEnd,
		String broadcasterIngestionJobLabel,
		Filters filters,
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

			// check if the broadcastDefaultPlaylistItem has the media/stream/countdown/DirectUrl
			{
				if ((broadcastDefaultPlaylistItem.getMediaType().equalsIgnoreCase("Stream")
						&& broadcastDefaultPlaylistItem.getStream() == null)
					|| (broadcastDefaultPlaylistItem.getMediaType().equalsIgnoreCase("Media")
						&& (broadcastDefaultPlaylistItem.getMediaItems() == null || broadcastDefaultPlaylistItem.getMediaItems().size() == 0))
					|| (broadcastDefaultPlaylistItem.getMediaType().equalsIgnoreCase("Countdown")
						&& (broadcastDefaultPlaylistItem.getMediaItems() == null || broadcastDefaultPlaylistItem.getMediaItems().size() == 0))
					|| (broadcastDefaultPlaylistItem.getMediaType().equalsIgnoreCase("Direct URL")
						&& (broadcastDefaultPlaylistItem.getUrl() == null || broadcastDefaultPlaylistItem.getUrl().isBlank()))
				)
				{
					// non cambiare il messaggio sotto perchè viene verificato nel catch del metodo chiamamnte (Broadcaster::startBroadcaster)
					String errorMessage = "No default is present"
						+ ", mediaType: " + broadcastDefaultPlaylistItem.getMediaType()
					;
					mLogger.error(errorMessage);

					throw new Exception(errorMessage);
				}
			}

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
					filters,
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
				try {
					mLogger.info("changeLiveProxyPlaylist"
							+ ", broadcasterIngestionJobKey: " + broadcasterIngestionJobKey
							+ ", broadcastPlaylistItems: " + broadcastPlaylistItems
					);
					catraMMS.changeLiveProxyPlaylist(username, password,
							broadcasterIngestionJobKey, broadcastPlaylistItems,
							true);
				}
				catch (Exception e)
				{
					mLogger.error("changeLiveProxyPlaylist failed, retrieving broadcasterIngestionJob to kill it"
							+ ", broadcasterIngestionJobKey: " + broadcasterIngestionJobKey
							+ ", exception: " + e.getMessage()
					);
					IngestionJob broadcasterIngestionJob = catraMMS.getIngestionJob(username, password,
							broadcasterIngestionJobKey, false, true, false);
					if (broadcasterIngestionJob != null)
					{
						mLogger.info("Killing broadcasterIngestionJob"
								+ ", broadcasterIngestionJobKey: " + broadcasterIngestionJobKey
						);
						killBroadcaster(broadcasterIngestionJob, catraMMS, username, password);
					}
					else
						mLogger.error("broadcasterIngestionJob is null"
								+ ", broadcasterIngestionJobKey: " + broadcasterIngestionJobKey
						);

					throw e;
				}
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
				false, true
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
				broadcastIngestionJob.getEncodingJob().getEncodingJobKey(), "kill");

            catraMMS.killEncodingJob(username, password,
                broadcasterIngestionJob.getEncodingJob().getEncodingJobKey(), "kill");
        }
        catch (Exception e)
        {
            String errorMessage = "Exception: " + e;
            mLogger.error(errorMessage);

			throw e;
        }
    }

	static public BroadcastPlaylistItem fillBroadcastPlaylistItemFromIngestionJob(
			CatraMMSAPI catraMMS, String username, String password,
			IngestionJob broadcasterIngestionJob,	// in
			List<BroadcastPlaylistItem> playlistItemList	// out
	)
			throws Exception
	{
		BroadcastPlaylistItem defaultBroadcastPlaylistItem = null;

		try
		{
			Date startBroadcaster = broadcasterIngestionJob.getProxyPeriodStart();
			Date stopBroadcaster = broadcasterIngestionJob.getProxyPeriodEnd();

			JSONObject joBroadcasterMetadataContent = new JSONObject(broadcasterIngestionJob.getMetaDataContent());

			if (joBroadcasterMetadataContent.has("internalMMS"))
			{
				JSONObject joBroadcasterInternalMMS = joBroadcasterMetadataContent.getJSONObject("internalMMS");
				if (joBroadcasterInternalMMS.has("broadcaster")) {
					JSONObject joBroadcaster = joBroadcasterInternalMMS.getJSONObject("broadcaster");

					if (joBroadcaster.has("broadcastDefaultPlaylistItem")
							&& joBroadcaster.has("broadcastIngestionJobKey"))
					{
						// broadcasterElement.setDefaultBroadcastPlaylistItem
						JSONObject joBroadcastDefaultPlaylistItem;
						{
							joBroadcastDefaultPlaylistItem = joBroadcaster.getJSONObject("broadcastDefaultPlaylistItem");
							defaultBroadcastPlaylistItem = BroadcastPlaylistItem.fromBroadcasterJson(
									joBroadcastDefaultPlaylistItem, catraMMS, username, password);
						}

						IngestionJob broadcastIngestionJob;
						{
							Long broadcastIngestionJobKey = joBroadcaster.getLong("broadcastIngestionJobKey");
							mLogger.info("catraMMS.getIngestionJob"
									+ ", broadcastIngestionJobKey: " + broadcastIngestionJobKey
							);
							Date start = new Date();
							broadcastIngestionJob = catraMMS.getIngestionJob(username, password,
									broadcastIngestionJobKey, false,
									// 2022-12-18: IngestionJob dovrebbe essere già presente da un po
									false, true);
							mLogger.info("catraMMS.getIngestionJob"
									+ ", elapsed (millisecs): " + (new Date().getTime() - start.getTime())
							);
						}

						mLogger.info("Broadcast metadata"
								+ ", broadcastIngestionJob.getMetaDataContent(): " + broadcastIngestionJob.getMetaDataContent()
						);
						JSONArray jaBroadcastInputsRoot = null;
						{
							// Option 1: inputsRoot is into the EncodingJob metadata
							if (broadcastIngestionJob.getEncodingJob() != null
									&& broadcastIngestionJob.getEncodingJob().getParameters() != null
									&& !broadcastIngestionJob.getEncodingJob().getParameters().isEmpty()) {
								JSONObject joBroadcastParameters = new JSONObject(broadcastIngestionJob.getEncodingJob().getParameters());
								if (joBroadcastParameters.has("inputsRoot"))
									jaBroadcastInputsRoot = joBroadcastParameters.getJSONArray("inputsRoot");
							}
							// Option 2: inputsRoot is into the IngestionJob metadata (Broadcaster in the future)
							else {
								JSONObject joBroadcastParameters = new JSONObject(broadcastIngestionJob.getMetaDataContent());
								if (joBroadcastParameters.has("internalMMS")) {
									JSONObject joInternalMMS = joBroadcastParameters.getJSONObject("internalMMS");
									if (joInternalMMS.has("broadcaster")) {
										JSONObject joLocalBroadcaster = joInternalMMS.getJSONObject("broadcaster");
										if (joLocalBroadcaster.has("broadcasterInputsRoot"))
											jaBroadcastInputsRoot = joLocalBroadcaster.getJSONArray("broadcasterInputsRoot");
									}
								}
							}
						}

						if (jaBroadcastInputsRoot != null)
						{
							playlistItemList.clear();

							Date start = new Date();
							for (int broadcastInputsIndex = 0; broadcastInputsIndex < jaBroadcastInputsRoot.length();
								 broadcastInputsIndex++)
							{
								JSONObject jobroadcastInputRoot = jaBroadcastInputsRoot.getJSONObject(
										broadcastInputsIndex);

								{
									if (jobroadcastInputRoot.has("defaultBroadcast")
											&& jobroadcastInputRoot.getBoolean("defaultBroadcast"))
										continue;
								}

								Date broadcastInputStart = new Date(jobroadcastInputRoot.getLong("utcScheduleStart") * 1000);
								Date broadcastInputEnd = new Date(jobroadcastInputRoot.getLong("utcScheduleEnd") * 1000);

								BroadcastPlaylistItem broadcastPlaylistItem = BroadcastPlaylistItem.fromBroadcastJson(
										jobroadcastInputRoot, broadcastInputStart, broadcastInputEnd,
										catraMMS, username, password);

								{
									// 2021-12-20: scenario: the first time the broadcast is created,
									//		it is created an inputsRoot with the default 'countdown/liveproxychannel/vodproxy'
									//		that it is not labelled with the flag 'addedAsDefault'.
									//		The result is that the WEB APP display the playlist with this entry
									//		whilst it should not.
									//		For this reason, we will check and, if the playlist will have just one entry,
									//		the same as the default and with the duration of the broadcaster,
									//		we will remove it considering it as addedAsDefault
									{
										if (
												jaBroadcastInputsRoot.length() == 1    // just one entry
														&& broadcastPlaylistItem.isEqualsTo(joBroadcastDefaultPlaylistItem)
														&& broadcastPlaylistItem.getStart().getTime() == startBroadcaster.getTime()
														&& broadcastPlaylistItem.getEnd().getTime() == stopBroadcaster.getTime()
										)
											continue;
									}
								}

								playlistItemList.add(broadcastPlaylistItem);
							}
							mLogger.info("broadcastInputs"
									+ ", jaBroadcastInputsRoot.length: " + jaBroadcastInputsRoot.length()
									+ ", elapsed (millisecs): " + (new Date().getTime() - start.getTime())
							);

							Collections.sort(playlistItemList);
						} else {
							mLogger.warn("InputsRoot was not found");
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			mLogger.error("Exception: " + e.getMessage(), e);

			throw e;
		}

		return defaultBroadcastPlaylistItem;
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

            JSONObject joWorkflow = CatraMMSWorkflow.buildWorkflowRootJson(broadcastIngestionJobLabel, false);

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
						Filter fadeFilter = new Filter("Fade");
						// fadeFilter.setFilterName("Fade");
						fadeFilter.setFade_Duration(3L);

						outputStream.getFilters().getFilters().add(fadeFilter);
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

							broadcastDefaultPlaylistItem.getFilters(),

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
							broadcastDefaultPlaylistItem.getFilters(),
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

						broadcastDefaultPlaylistItem.getFilters(),

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
		Filters filters,
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

            JSONObject joWorkflow = CatraMMSWorkflow.buildWorkflowRootJson(broadcasterIngestionJobLabel, false);

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

					if (filters != null)
						outputStream.setFilters(filters);

					outputStreamList.add(outputStream);
				}
				else if (editBroadcasterDeliveryType.equals("CDN77"))
				{
					OutputStream outputStream = new OutputStream();

					outputStream.setOutputType("CDN_CDN77");
					if (editBroadcasterCdn77Channel != null)
						outputStream.setCdn77Channel(editBroadcasterCdn77Channel);

					outputStream.setEncodingProfileLabel(encodingProfileLabel);

					if (filters != null)
						outputStream.setFilters(filters);

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

					broadcasterStart, broadcasterEnd, null,

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

	public static String createBroadcastEncodersPoolIfNeeded(CatraMMSAPI catraMMS, String username, String password, Stream broadcasterStream)
			throws Exception {
		// - look for an encodersPool containing ONLY the 'push server' of the broadcaster
		// - if it is not found, create one and add it

		String broadcastEncodersPoolLabel = null;

		List<EncodersPool> encodersPoolsList = new ArrayList<>();
		catraMMS.getEncodersPool(username, password, null, encodersPoolsList);

		List<String> encodersPoolLabels = new ArrayList<>();

		for (EncodersPool encodersPool : encodersPoolsList) {
			encodersPoolLabels.add(encodersPool.getLabel());

			if (encodersPool.getEncoderList().size() != 1)
				continue;

			if (encodersPool.getEncoderList().get(0).getEncoderKey() == broadcasterStream.getPushEncoderKey()) {
				broadcastEncodersPoolLabel = encodersPool.getLabel();

				break;
			}
		}

		if (broadcastEncodersPoolLabel == null) {
			broadcastEncodersPoolLabel = broadcasterStream.getPushEncoderLabel();
			long counter = 1;
			while (encodersPoolLabels.indexOf(broadcastEncodersPoolLabel) != -1)
				broadcastEncodersPoolLabel = broadcasterStream.getPushEncoderLabel() + "_" + counter++;

			List<Long> encordersPoolKeys = new ArrayList<>();
			encordersPoolKeys.add(broadcasterStream.getPushEncoderKey());
			catraMMS.addEncodersPoolByKeys(username, password, broadcastEncodersPoolLabel, encordersPoolKeys);
		}

		return broadcastEncodersPoolLabel;
	}

	// copia gli items del Live Channel e li incolla nello stesso Live Channel a partire da 'startPaste'
	public static void broadcasterCopyAndPasteItems(
			// Vengono copiati gli items aventi gli start >= startCopy e < endCopy
			Date startCopy, // data locale
			Date endCopy, // data locale

			// tutti gli items della playlist >= startPaste vengono shift-ati
			Date startPaste, // data locale

			List<BroadcastPlaylistItem> playlistItemList // in/out
	) {
		if (startCopy == null || endCopy == null || startPaste == null || startCopy.getTime() > endCopy.getTime())
		{
			mLogger.error("broadcasterCopyAndPasteItems. wrong input"
					+ ", startCopy: " + startCopy
					+ ", endCopy: " + endCopy
					+ ", startPaste: " + startPaste
			);
			return;
		}

		if (playlistItemList.size() == 0)
			return;

		// caso 1:          startCopy                endCopy           firstPlaylistStart
		if (playlistItemList.get(0).getStart().getTime() > endCopy.getTime())
			return;

		// caso 2:          lastPlaylistStart       startCopy         endCopy
		if (playlistItemList.get(playlistItemList.size() - 1).getStart().getTime() < startCopy.getTime())
			return;

		// caso 3:          playlistStart     startCopy     playlistStart     playlistStart      endCopy     playlistStart
		int startCopyIndex = -1; // indici inclusivi
		int endCopyIndex = -1;   // indici inclusivi
		for (int broadcastPlaylistIndex = 0; broadcastPlaylistIndex < playlistItemList.size(); broadcastPlaylistIndex++)
		{
			BroadcastPlaylistItem broadcastPlaylistItem = playlistItemList.get(broadcastPlaylistIndex);

			if (startCopyIndex == -1 && broadcastPlaylistItem.getStart().getTime() >= startCopy.getTime())
				startCopyIndex = broadcastPlaylistIndex;

			if (startCopyIndex != -1)
			{
				if (broadcastPlaylistItem.getStart().getTime() < endCopy.getTime())
					endCopyIndex = broadcastPlaylistIndex;
				else
					break;
			}
		}
		if (startCopyIndex == -1 || endCopyIndex == -1)
		{
			mLogger.error("broadcasterCopyAndPasteItems. startCopyIndex and endCopyIndex are both -1"
			);
			return;
		}

		int startPasteIndex = -1; // vengono spostati gli items a partire da questo indiceo
		// caso 1:          startPaste           firstPlaylistStart
		// caso 2:          lastPlaylistStart   startPaste
		// caso 3:          firstPlaylistStart  startPaste    lastPlaylistStart
		if (startPaste.getTime() < playlistItemList.get(0).getStart().getTime())
			startPasteIndex = 0;
		else if (playlistItemList.get(playlistItemList.size() - 1).getStart().getTime() < startPaste.getTime())
			startPasteIndex = playlistItemList.size();
		else
		{
			for (int broadcastPlaylistIndex = 0; broadcastPlaylistIndex < playlistItemList.size(); broadcastPlaylistIndex++)
			{
				BroadcastPlaylistItem broadcastPlaylistItem = playlistItemList.get(broadcastPlaylistIndex);

				if (broadcastPlaylistItem.getStart().getTime() >= startPaste.getTime()) {
					startPasteIndex = broadcastPlaylistIndex;
					break;
				}
			}
		}

		// shift-o gli items successivi a startPaste
		long shiftAmount = playlistItemList.get(endCopyIndex).getEnd().getTime() - playlistItemList.get(startCopyIndex).getStart().getTime();
		for (int broadcastPlaylistIndex = startPasteIndex; broadcastPlaylistIndex < playlistItemList.size(); broadcastPlaylistIndex++)
		{
			BroadcastPlaylistItem broadcastPlaylistItem = playlistItemList.get(broadcastPlaylistIndex);
			broadcastPlaylistItem.setStart(new Date(
					broadcastPlaylistItem.getStart().getTime() + shiftAmount));
			broadcastPlaylistItem.setEnd(new Date(
					broadcastPlaylistItem.getEnd().getTime() + shiftAmount));
		}

		// inserisco
		{
			// clone degli items da inserire
			List<BroadcastPlaylistItem> newPlaylistItemList = new ArrayList<>();
			for (int broadcastPlaylistIndex = startCopyIndex; broadcastPlaylistIndex <= endCopyIndex; broadcastPlaylistIndex++)
				newPlaylistItemList.add(playlistItemList.get(broadcastPlaylistIndex).clone());

			// aggiorno i times e inserisco a partire da startPasteIndex
			Date newStart = startPaste;
			for (BroadcastPlaylistItem newBroadcastPlaylistItem: newPlaylistItemList)
			{
				long elapsed = newBroadcastPlaylistItem.getEnd().getTime() - newBroadcastPlaylistItem.getStart().getTime();
				newBroadcastPlaylistItem.setStart(newStart);
				newBroadcastPlaylistItem.setEnd(new Date(newStart.getTime() + elapsed));

				playlistItemList.add(startPasteIndex, newBroadcastPlaylistItem);

				newStart = newBroadcastPlaylistItem.getEnd();
				startPasteIndex++;
			}
		}
	}
}
