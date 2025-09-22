package com.catrammslib;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.catrammslib.entity.WorkflowVariable;
import com.catrammslib.utility.filters.DrawTextDetails;
import com.catrammslib.utility.filters.Filter;
import com.catrammslib.utility.OutputStream;
import com.catrammslib.utility.MediaItemReference;

import com.catrammslib.utility.filters.Filters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONArray;
import org.json.JSONObject;

public class CatraMMSWorkflow {

    private static final Logger mLogger = LoggerFactory.getLogger(CatraMMSWorkflow.class);

    static public JSONObject buildWorkflowRootJson(String label, boolean hidden)
            throws Exception
    {
        try
        {
            JSONObject joWorkflow = new JSONObject();
            joWorkflow.put("label", label);
            joWorkflow.put("type", "Workflow");
            joWorkflow.put("hidden", hidden);

            return joWorkflow;
        }
        catch (Exception e)
        {
            String errorMessage = "buildWorkflowRootJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildEventJson(JSONObject joTarget, String eventType)
            throws Exception
    {
        try
        {
            JSONObject joEvent = new JSONObject();
            joTarget.put(eventType, joEvent);

            return joEvent;
        }
        catch (Exception e)
        {
            String errorMessage = "buildEventJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public List<Object> buildGroupOfTasks(
            String label, String executionType,
            List<String> referencesOutput,
            Long utcProcessingStartingFrom)
            throws Exception
    {
        try
        {
            JSONObject joGroupOfTasks = new JSONObject();

            joGroupOfTasks.put("label", label);
            joGroupOfTasks.put("type", "GroupOfTasks");
            JSONArray jaTasks;
            JSONArray jaReferencesOutput = new JSONArray();
            {
                JSONObject joParameters = new JSONObject();
                joGroupOfTasks.put("parameters", joParameters);

                joParameters.put("executionType", executionType);

                jaTasks = new JSONArray();
                joParameters.put("tasks", jaTasks);

                if (referencesOutput != null && referencesOutput.size() > 0)
                {
                    // jaReferencesOutput = new JSONArray();
                    joParameters.put("referencesOutput", jaReferencesOutput);

                    for(String referenceOutput: referencesOutput)
                    {
                        JSONObject joReferenceOutput = new JSONObject();
                        jaReferencesOutput.put(joReferenceOutput);

                        joReferenceOutput.put("label", referenceOutput);
                    }
                }

                if (utcProcessingStartingFrom != null)
                {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                    joParameters.put("processingStartingFrom", dateFormat.format(utcProcessingStartingFrom));
                }
            }

            List<Object> objects = new ArrayList<>();
            objects.add(joGroupOfTasks);
            objects.add(jaTasks);
            objects.add(jaReferencesOutput);

            return objects;
        }
        catch (Exception e)
        {
            String errorMessage = "buildGroupOfTasks failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildWorkflowAsLibrary(
            String label,
            String workflowAsLibraryType,
            String workflowAsLibraryLabel,
            List<WorkflowVariable> workflowVariableList
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "Workflow-As-Library");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

            joParameters.put("workflowAsLibraryLabel", workflowAsLibraryLabel);
            joParameters.put("workflowAsLibraryType", workflowAsLibraryType);

            for (WorkflowVariable workflowVariable: workflowVariableList)
            {
                if(workflowVariable.getType().equalsIgnoreCase("string"))
                    joParameters.put(workflowVariable.getName(), workflowVariable.getStringValue());
                else if(workflowVariable.getType().equalsIgnoreCase("integer"))
                    joParameters.put(workflowVariable.getName(), workflowVariable.getLongValue());
                else if(workflowVariable.getType().equalsIgnoreCase("decimal"))
                    joParameters.put(workflowVariable.getName(), workflowVariable.getDoubleValue());
                else if(workflowVariable.getType().equalsIgnoreCase("boolean"))
                    joParameters.put(workflowVariable.getName(), workflowVariable.isBooleanValue());
                else if(workflowVariable.getType().equalsIgnoreCase("datetime"))
                {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                    joParameters.put(workflowVariable.getName(), dateFormat.format(workflowVariable.getDatetimeValue()));
                }
                else if(workflowVariable.getType().equalsIgnoreCase("datetime-millisecs"))
                {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                    joParameters.put(workflowVariable.getName(), dateFormat.format(workflowVariable.getDatetimeValue()));
                }
                else if(workflowVariable.getType().equalsIgnoreCase("jsonObject"))
                    joParameters.put(workflowVariable.getName(), workflowVariable.getJsonObjectValue());
                else if(workflowVariable.getType().equalsIgnoreCase("jsonArray"))
                    joParameters.put(workflowVariable.getName(), workflowVariable.getJsonArrayValue());
                else
                {
                    String errorMessage = "WorkflowVariable type not managed"
                            + ", workflowVariable.getName: " + workflowVariable.getName()
                            + ", workflowVariable.getType: " + workflowVariable.getType()
                            ;
                    mLogger.error(errorMessage);

                    throw new Exception(errorMessage);
                }
            }

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildWorkflowAsLibrary failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildLiveRecorderJson(
            String label,

            Long recordingCode,        // mandatory

            String liveConfigurationLabel,

            List<String> chunkTags, String ingester,
            String chunkRetention, JSONObject joUserData,
            String encodingPriority,
            Boolean autoRenew,
            Boolean utcTimeOverlay,
			String otherInputOptions,
            Boolean monitorHLS,
            String monitorHLSEncodingProfileLabel,
            String outputFileFormat,
            Long segmentDurationInSeconds,
            boolean liveRecorderVirtualVOD,
            Long liveRecorderVirtualVODMaxDurationInMinutes,
            String liveRecorderVirtualVODEncodingProfileLabel,
            Long utcLiveRecorderStart,
            Long utcLiveRecorderEnd,
            String encodersPool,
            String userAgent,
            List<OutputStream> liveRecorderOutputList,
			JSONArray jaFramesToBeDetected,
			Boolean monitoringFrameIncreasingEnabled
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "Live-Recorder");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

            setContentParameters(joParameters,
                    null, ingester, chunkRetention, null,
                    chunkTags, joUserData,
                    null, null,
                    null, null);

            /*
            setCommonParameters(joParameters,
                    null,
                    null, null,
                    null,
                    null, null,
                    null);
            */

            joParameters.put("recordingCode", recordingCode);

            joParameters.put("configurationLabel", liveConfigurationLabel);

            joParameters.put("EncoderPriority", encodingPriority);
            // joParameters.put("HighAvailability", highAvailability);
            joParameters.put("outputFileFormat", outputFileFormat);
            joParameters.put("segmentDuration", segmentDurationInSeconds);

            if (otherInputOptions != null && !otherInputOptions.isEmpty())
                joParameters.put("otherInputOptions", otherInputOptions);

            if (encodersPool != null && !encodersPool.isEmpty())
                joParameters.put("encodersPool", encodersPool);

            if (userAgent != null && !userAgent.isEmpty())
                joParameters.put("userAgent", userAgent);

            {
                JSONObject joRecordingPeriod = new JSONObject();
                joParameters.put("schedule", joRecordingPeriod);

                if (autoRenew != null)
                    joRecordingPeriod.put("autoRenew", autoRenew);

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                joRecordingPeriod.put("start", dateFormat.format(utcLiveRecorderStart));
                joRecordingPeriod.put("end", dateFormat.format(utcLiveRecorderEnd));
            }

            if (utcTimeOverlay != null)
                joParameters.put("utcTimeOverlay", utcTimeOverlay);

            if (monitorHLS != null && monitorHLS)
            {
                JSONObject joMonitorHLS = new JSONObject();
                joParameters.put("monitorHLS", joMonitorHLS);

                if (monitorHLSEncodingProfileLabel != null && !monitorHLSEncodingProfileLabel.isEmpty())
                    joMonitorHLS.put("encodingProfileLabel", monitorHLSEncodingProfileLabel);
            }
            if (liveRecorderVirtualVOD)
            {
                JSONObject joVirtualVOD = new JSONObject();
                joParameters.put("liveRecorderVirtualVOD", joVirtualVOD);

                if (liveRecorderVirtualVODMaxDurationInMinutes != null)
                    joVirtualVOD.put("maxDuration", liveRecorderVirtualVODMaxDurationInMinutes);
                if (liveRecorderVirtualVODEncodingProfileLabel != null && !liveRecorderVirtualVODEncodingProfileLabel.isEmpty())
                    joVirtualVOD.put("encodingProfileLabel", liveRecorderVirtualVODEncodingProfileLabel);
            }

            if (liveRecorderOutputList != null && liveRecorderOutputList.size() > 0)
            {
                JSONArray jaOutputs = new JSONArray();
                joParameters.put("outputs", jaOutputs);

                for(OutputStream outputStream : liveRecorderOutputList)
                {
                    JSONObject joOutput = outputStream.toJson();
                    jaOutputs.put(joOutput);
                }
            }

			if (jaFramesToBeDetected != null)
				joParameters.put("framesToBeDetected", jaFramesToBeDetected);

			if (monitoringFrameIncreasingEnabled != null)
				joParameters.put("monitoringFrameIncreasingEnabled", monitoringFrameIncreasingEnabled);

			return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildLiveRecorderJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildLiveProxyJson(
            String label,

            String liveConfigurationLabel,
            Long useVideoTrackFromMediaItemKey,

            String encodersPool,
            Date proxyStartTime, Date proxyEndTime, Long maxStreamingDurationInMinutes,
            String userAgent,
            Long maxWidth,
            String otherInputOptions,
            Long maxAttemptsNumberInCaseOfErrors,
            Long waitingSecondsBetweenAttemptsInCaseOfErrors,
            List<OutputStream> outputStreamList,
			JSONObject joInternalMMSParameters,
			Boolean defaultBroadcast,
            Boolean monitoringFrameIncreasingEnabled
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "Live-Proxy");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

			joParameters.put("configurationLabel", liveConfigurationLabel);
            if (useVideoTrackFromMediaItemKey != null)
                joParameters.put("useVideoTrackFromMediaItemKey", useVideoTrackFromMediaItemKey);

			// this is a parameter used ONLY for the 'live channel/broadcaster' application.
			if (defaultBroadcast != null && defaultBroadcast)
				joParameters.put("defaultBroadcast", defaultBroadcast);

            if (encodersPool != null && !encodersPool.isEmpty())
                joParameters.put("encodersPool", encodersPool);

            if (userAgent != null && !userAgent.isEmpty())
                joParameters.put("userAgent", userAgent);

            if (maxWidth != null)
                joParameters.put("maxWidth", maxWidth);

            if (otherInputOptions != null && !otherInputOptions.isEmpty())
                joParameters.put("otherInputOptions", otherInputOptions);

            if (maxAttemptsNumberInCaseOfErrors != null)
                joParameters.put("maxAttemptsNumberInCaseOfErrors", maxAttemptsNumberInCaseOfErrors);
            if (waitingSecondsBetweenAttemptsInCaseOfErrors != null)
                joParameters.put("waitingSecondsBetweenAttemptsInCaseOfErrors", waitingSecondsBetweenAttemptsInCaseOfErrors);

            if (proxyStartTime != null && proxyEndTime != null)
            {
                joParameters.put("timePeriod", true);

                JSONObject joProxyPeriod = new JSONObject();
                joParameters.put("schedule", joProxyPeriod);

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                joProxyPeriod.put("start", dateFormat.format(proxyStartTime));
                joProxyPeriod.put("end", dateFormat.format(proxyEndTime));
            }
            else
                joParameters.put("timePeriod", false);

            if (maxStreamingDurationInMinutes != null)
                joParameters.put("maxStreamingDurationInMinutes", maxStreamingDurationInMinutes);

            if (outputStreamList == null || outputStreamList.size() == 0)
            {
                String errorMessage = "At least one liveProxyOutput has to be present";
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            JSONArray jaOutputs = new JSONArray();
            joParameters.put("outputs", jaOutputs);

            for(OutputStream outputStream : outputStreamList)
            {
				JSONObject joOutput = outputStream.toJson();
                jaOutputs.put(joOutput);
            }

			if (joInternalMMSParameters != null)
				joParameters.put("internalMMS", joInternalMMSParameters);

            if (monitoringFrameIncreasingEnabled != null)
                joParameters.put("monitoringFrameIncreasingEnabled", monitoringFrameIncreasingEnabled);

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildLiveProxyJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildLiveProxyJsonForBroadcast(
            String label,

            String liveConfigurationLabel,

            String encodersPool,
            Date proxyStartTime, Date proxyEndTime,
            String userAgent,
            Long maxWidth,
            String otherInputOptions,
            Long maxAttemptsNumberInCaseOfErrors,
            Long waitingSecondsBetweenAttemptsInCaseOfErrors,

			// 2022-09-12: in genere i parametri del 'draw text' vengono inizializzati
			//		all'interno di LiveProxyOutput.
			//		Nel caso del Broadcast (Live Channel), LiveProxyOutput è comune a tutta la playlist,
			//		per cui non possiamo utilizzare LiveProxyOutput altrimenti avremmo il draw text
			//		anche per gli altri item della playlist quali LiveProxy, VODProxy, ...
			//		Per questo motivo:
			//			1. aggiungiamo questi parametri in forma eccezionale per il Broadcast
			//			2. questi parametri saranno gestiti dall'engine
			Filters filters,

            List<OutputStream> outputStreamList,
			JSONObject joInternalMMSParameters,
			Boolean defaultBroadcast
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = buildLiveProxyJson(label, liveConfigurationLabel, null,
                encodersPool, proxyStartTime, proxyEndTime, null, userAgent, maxWidth, otherInputOptions,
				maxAttemptsNumberInCaseOfErrors, waitingSecondsBetweenAttemptsInCaseOfErrors,
                    outputStreamList, joInternalMMSParameters, defaultBroadcast, null);

			if (filters != null)
			{
				JSONObject joParameters = joTask.getJSONObject("parameters");

				joParameters.put("broadcastFilters", filters.toJson());
			}

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildLiveProxyJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

	static public JSONObject buildVODProxyJson(
            String label,

            List<MediaItemReference> mediaItemReferenceList,

            String encodersPool,
            Date proxyStartTime, Date proxyEndTime,
            String otherInputOptions,
            List<OutputStream> outputStreamList,
			Boolean defaultBroadcast
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "VOD-Proxy");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

            setCommonParameters(joParameters,
				null,
				mediaItemReferenceList,
				null);

			// this is a parameter used ONLY for the 'live channel/broadcaster' application.
			if (defaultBroadcast != null && defaultBroadcast)
				joParameters.put("defaultBroadcast", defaultBroadcast);

			if (encodersPool != null && !encodersPool.isEmpty())
                joParameters.put("encodersPool", encodersPool);

            if (otherInputOptions != null && !otherInputOptions.isEmpty())
                joParameters.put("otherInputOptions", otherInputOptions);

            if (proxyStartTime != null && proxyEndTime != null)
            {
                joParameters.put("timePeriod", true);

                JSONObject joProxyPeriod = new JSONObject();
                joParameters.put("schedule", joProxyPeriod);

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                joProxyPeriod.put("start", dateFormat.format(proxyStartTime));
                joProxyPeriod.put("end", dateFormat.format(proxyEndTime));
            }
            else
                joParameters.put("timePeriod", false);

            if (outputStreamList == null || outputStreamList.size() == 0)
            {
                String errorMessage = "At least one liveProxyOutput has to be present";
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            JSONArray jaOutputs = new JSONArray();
            joParameters.put("outputs", jaOutputs);

            for(OutputStream outputStream : outputStreamList)
            {
				JSONObject joOutput = outputStream.toJson();
                jaOutputs.put(joOutput);
			}


            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildVODProxyJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildVODProxyJsonForBroadcast(
            String label,

            List<MediaItemReference> mediaItemReferenceList,

            String encodersPool,
            Date proxyStartTime, Date proxyEndTime,
            String otherInputOptions,

			// 2022-09-12: in genere i parametri del 'draw text' vengono inizializzati
			//		all'interno di LiveProxyOutput.
			//		Nel caso del Broadcast (Live Channel), LiveProxyOutput è comune a tutta la playlist,
			//		per cui non possiamo utilizzare LiveProxyOutput altrimenti avremmo il draw text
			//		anche per gli altri item della playlist quali LiveProxy, VODProxy, ...
			//		Per questo motivo:
			//			1. aggiungiamo questi parametri in forma eccezionale per il Broadcast
			//			2. questi parametri saranno gestiti dall'engine
			Filters filters,
			
            List<OutputStream> outputStreamList,
			Boolean defaultBroadcast
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = buildVODProxyJson(label, mediaItemReferenceList, encodersPool, 
				proxyStartTime, proxyEndTime, otherInputOptions, outputStreamList, defaultBroadcast);

			if (filters != null)
			{
				JSONObject joParameters = joTask.getJSONObject("parameters");

				joParameters.put("broadcastFilters", filters.toJson());
			}

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildVODProxyJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

	static public JSONObject buildCountdownJson(
            String label,

            List<MediaItemReference> mediaItemReferenceList,

            String encodersPool,
            Date proxyStartTime, Date proxyEndTime,

            List<OutputStream> outputStreamList,
			Boolean defaultBroadcast
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "Countdown");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

            setCommonParameters(joParameters,
				null,
				mediaItemReferenceList,
				null);

            {
                JSONObject joProxyPeriod = new JSONObject();
                joParameters.put("schedule", joProxyPeriod);

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                joProxyPeriod.put("start", dateFormat.format(proxyStartTime));
                joProxyPeriod.put("end", dateFormat.format(proxyEndTime));
            }

			// this is a parameter used ONLY for the 'live channel/broadcaster' application.
			if (defaultBroadcast != null && defaultBroadcast)
				joParameters.put("defaultBroadcast", defaultBroadcast);
			
			if (encodersPool != null && !encodersPool.isEmpty())
                joParameters.put("encodersPool", encodersPool);

            if (outputStreamList == null || outputStreamList.size() == 0)
            {
                String errorMessage = "At least one liveProxyOutput has to be present";
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            JSONArray jaOutputs = new JSONArray();
            joParameters.put("outputs", jaOutputs);

            for(OutputStream outputStream : outputStreamList)
            {
				JSONObject joOutput = outputStream.toJson();
                jaOutputs.put(joOutput);
            }


            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildCountdownJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

	static public JSONObject buildCountdownJsonForBroadcast(
            String label,

            List<MediaItemReference> mediaItemReferenceList,

            String encodersPool,
            Date proxyStartTime, Date proxyEndTime,

			// 2022-09-12: in genere i parametri del 'draw text' vengono inizializzati
			//		all'interno di LiveProxyOutput.
			//		Nel caso del Broadcast (Live Channel), LiveProxyOutput è comune a tutta la playlist,
			//		per cui non possiamo utilizzare LiveProxyOutput altrimenti avremmo il draw text
			//		anche per gli altri item della playlist quali LiveProxy, VODProxy, ...
			//		Per questo motivo:
			//			1. aggiungiamo questi parametri in forma eccezionale per il Broadcast
			//			2. questi parametri saranno gestiti dall'engine
			Filters filters,

			List<OutputStream> outputStreamList,
			Boolean defaultBroadcast
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = buildCountdownJson(label, mediaItemReferenceList, encodersPool, 
				proxyStartTime, proxyEndTime, outputStreamList, defaultBroadcast);

			if (filters != null)
			{
				JSONObject joParameters = joTask.getJSONObject("parameters");

				joParameters.put("broadcastFilters", filters.toJson());
			}
	
            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildCountdownJsonForBroadcast failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

	static public JSONObject buildPostOnYouTube(
            String label,

			String youTubeConfigurationLabel,
            String title,
            String description,
            List<String> tags,
			Long categoryId,
			String privacyStatus,
            Boolean madeForKids,

            List<MediaItemReference> mediaItemReferenceList
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "Post-On-YouTube");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

            setCommonParameters(joParameters,
                    null,
                    mediaItemReferenceList,
                    null);

			joParameters.put("configurationLabel", youTubeConfigurationLabel);

            if (title != null && !title.isEmpty())
                joParameters.put("title", title);

			if (description != null && !description.isEmpty())
                joParameters.put("description", description);

			if (tags != null)
			{
				JSONArray jaTags = new JSONArray();
				joParameters.put("tags", jaTags);

				for (String tag: tags)
				{
					if (!tag.isEmpty())
						jaTags.put(tag);
				}
			}

			if (categoryId != null)
                joParameters.put("categoryId", categoryId);

			if (privacyStatus != null && !privacyStatus.isEmpty())
                joParameters.put("privacyStatus", privacyStatus);

            if (madeForKids != null)
                joParameters.put("madeForKids", madeForKids);

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildPostOnYouTube failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildYouTubeLiveBroadcast(
            String label,

			String youTubeConfigurationLabel,
            String title,
            String description,
			String privacyStatus,
			Boolean madeForKids,
			String latencyPreference,
			String encodersPool,
			Date startTime, Date endTime,

			// only one of the two below parameters has to be initialized, the other will be null
			String channelConfigurationLabel,
            List<MediaItemReference> mediaItemReferenceList
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "YouTube-Live-Broadcast");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

			joParameters.put("youTubeConfigurationLabel", youTubeConfigurationLabel);

            if (title != null && !title.isEmpty())
                joParameters.put("title", title);

			if (description != null && !description.isEmpty())
                joParameters.put("description", description);

			if (privacyStatus != null && !privacyStatus.isEmpty())
                joParameters.put("privacyStatus", privacyStatus);

			if (madeForKids != null)
                joParameters.put("madeForKids", madeForKids);

			if (latencyPreference != null && !latencyPreference.isEmpty())
                joParameters.put("latencyPreference", latencyPreference);

			if (channelConfigurationLabel != null)
			{
                joParameters.put("sourceType", "Live");
                joParameters.put("configurationLabel", channelConfigurationLabel);
			}
			else
			{
                joParameters.put("sourceType", "MediaItem");

				setCommonParameters(joParameters,
					null,
					mediaItemReferenceList,
					null);
			}

			if (encodersPool != null && !encodersPool.isEmpty())
                joParameters.put("encodersPool", encodersPool);

			{
				JSONObject joProxyPeriod = new JSONObject();
				joParameters.put("youTubeSchedule", joProxyPeriod);

				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

				joProxyPeriod.put("start", dateFormat.format(startTime));
				joProxyPeriod.put("end", dateFormat.format(endTime));
			}
	
            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildPostOnYouTube failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildPostOnFacebook(
            String label,

			String facebookConfigurationLabel,
            String facebookNodeType, String facebookNodeId,

            List<MediaItemReference> mediaItemReferenceList
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "Post-On-Facebook");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

            setCommonParameters(joParameters,
                    null,
                    mediaItemReferenceList,
                    null);

			joParameters.put("facebookConfigurationLabel", facebookConfigurationLabel);

			joParameters.put("facebookNodeType", facebookNodeType);
			joParameters.put("facebookNodeId", facebookNodeId);

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildPostOnFacebook failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildFacebookLiveBroadcast(
            String label,

			String facebookConfigurationLabel,
			String facebookNodeType,
			String facebookNodeId,
            String title,
            String description,
			String encodersPool,
			String facebookLiveType,
			Date startTime, Date endTime,

			// only one of the two below parameters has to be initialized, the other will be null
			String channelConfigurationLabel,
            List<MediaItemReference> mediaItemReferenceList
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "Facebook-Live-Broadcast");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

			joParameters.put("facebookConfigurationLabel", facebookConfigurationLabel);

			joParameters.put("facebookNodeType", facebookNodeType);
			joParameters.put("facebookNodeId", facebookNodeId);

            if (title != null && !title.isEmpty())
                joParameters.put("title", title);

			if (description != null && !description.isEmpty())
                joParameters.put("description", description);

			if (channelConfigurationLabel != null)
			{
                joParameters.put("sourceType", "Live");
                joParameters.put("configurationLabel", channelConfigurationLabel);
			}
			else
			{
                joParameters.put("sourceType", "MediaItem");

				setCommonParameters(joParameters,
					null,
					mediaItemReferenceList,
					null);
			}

			if (encodersPool != null && !encodersPool.isEmpty())
                joParameters.put("encodersPool", encodersPool);

			joParameters.put("facebookLiveType", facebookLiveType);
			{
				JSONObject joProxyPeriod = new JSONObject();
				joParameters.put("facebookSchedule", joProxyPeriod);

				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

				joProxyPeriod.put("start", dateFormat.format(startTime));
				joProxyPeriod.put("end", dateFormat.format(endTime));
			}
	
            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildPostOnYouTube failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

	static public JSONObject buildFaceRecognitionJson(
            String label, String title, List<String> tags, String ingester,
            String retention, JSONObject joUserData,
            Date startPublishing, Date endPublishing,
            List<MediaItemReference> mediaItemReferenceList,
            long faceRecognition_InitialFramesNumberToBeSkipped
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "Face-Recognition");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

            setContentParameters(joParameters,
                    title, ingester, retention, null,
                    tags, joUserData,
                    startPublishing, endPublishing,
                    null, null);

            setCommonParameters(joParameters,
                    null,
                    mediaItemReferenceList,
                    null);

            joParameters.put("cascadeName", "haarcascade_frontalface_alt_tree");
            joParameters.put("encodingPriority", "High");    // takes a lot of time by the MMSEngine
            joParameters.put("initialFramesNumberToBeSkipped", faceRecognition_InitialFramesNumberToBeSkipped);
            joParameters.put("oneFramePerSecond", true);
            joParameters.put("output", "FrameContainingFace");

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildFaceRecognitionJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public List<Object> buildFaceRecognitionJsonAndFrameIfNotFound(
            JSONObject joParentOnSuccess,    // joParentOnSuccess or jaParent has to be null
            JSONArray jaParent,
            String imageGroupOfTasksReferenceLabel,

            String title, List<String> imageTags, String ingester,
            String imageRetention,
            long faceRecognition_InitialFramesNumberToBeSkipped,
            Float frameCaptureSeconds,
            Long utcProcessingStartingFrom
    )
            throws Exception
    {
        try
        {
            List<Object> oImageGroupOfTasks;

            String frameContainingFaceImageReferenceLabel = "Frame Containing Face: " + title;
            String frameImageReferenceLabel = "Frame: " + title;

            JSONArray jaImageGroupOfTasks;
            {
                List<String> referencesOutput = new ArrayList<>();
                referencesOutput.add(frameContainingFaceImageReferenceLabel);
                referencesOutput.add(frameImageReferenceLabel);

                oImageGroupOfTasks = CatraMMSWorkflow.buildGroupOfTasks(
                        imageGroupOfTasksReferenceLabel, "parallel",
                        referencesOutput, utcProcessingStartingFrom);
                JSONObject joImageGroupOfTasks = (JSONObject) oImageGroupOfTasks.get(0);
                jaImageGroupOfTasks = (JSONArray) oImageGroupOfTasks.get(1);
                JSONArray jaReferencesOutput = (JSONArray) oImageGroupOfTasks.get(2);

                if (jaParent != null)
                    jaParent.put(joImageGroupOfTasks);
                else if (joParentOnSuccess != null)
                    joParentOnSuccess.put("task", joImageGroupOfTasks);
            }

            JSONObject joFrameContainingFace = CatraMMSWorkflow.buildFaceRecognitionJson(
                    frameContainingFaceImageReferenceLabel, title, imageTags, ingester,
                    imageRetention, null, null, null,
                    null, faceRecognition_InitialFramesNumberToBeSkipped);
            jaImageGroupOfTasks.put(joFrameContainingFace);

            {
                JSONObject joFrameContainingFaceOnSuccess = CatraMMSWorkflow.buildEventJson(joFrameContainingFace, "onSuccess");

                joFrameContainingFaceOnSuccess.put("task", CatraMMSWorkflow.buildEncodeJson(
                        "Encode image (FrameContainingFace): " + title,
                        ingester, "image",
                        "High",
                        "MMS_JPG_W240_H-200", null,
                        null,
                        null, null,
                        null, null,
                        utcProcessingStartingFrom
                ));
            }

            JSONObject joFrame;
            {
                JSONObject joFrameContainingFaceOnError = CatraMMSWorkflow.buildEventJson(joFrameContainingFace, "onError");

                joFrame = CatraMMSWorkflow.buildFrameJson(
                        frameImageReferenceLabel, title,
                        imageTags, ingester, frameCaptureSeconds, imageRetention,
                        null, null, null, null
                );
                joFrameContainingFaceOnError.put("task", joFrame);
            }

            {
                JSONObject joFrameOnSuccess = CatraMMSWorkflow.buildEventJson(joFrame, "onSuccess");

                joFrameOnSuccess.put("task", CatraMMSWorkflow.buildEncodeJson(
                        "Encode image (Frame): " + title,
                        ingester, "image",
                        "High",
                        "MMS_JPG_W240_H-200", null,
                        null,
                        null, null,
                        null, null,
                        utcProcessingStartingFrom
                ));
            }

            return oImageGroupOfTasks;
        }
        catch (Exception e)
        {
            String errorMessage = "buildFaceRecognitionJsonAndFrameIfNotFound failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildEmailNotificationJson(
            String label, String configurationLabel,
            List<String> userSubstitutionsToBeReplaced,
            List<String> userSubstitutionsReplaceWith
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "Email-Notification");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

            joParameters.put("configurationLabel", configurationLabel);

            if (userSubstitutionsToBeReplaced != null
                    && userSubstitutionsReplaceWith != null
                    && userSubstitutionsToBeReplaced.size() == userSubstitutionsReplaceWith.size())
            {
                JSONArray jaUserSubstitutions = new JSONArray();
                joParameters.put("UserSubstitutions", jaUserSubstitutions);

                for (int userSubstitutionIndex = 0;
                     userSubstitutionIndex < userSubstitutionsToBeReplaced.size();
                     userSubstitutionIndex++)
                {
                    String toBeReplaced = userSubstitutionsToBeReplaced.get(userSubstitutionIndex);
                    String replaceWith = userSubstitutionsReplaceWith.get(userSubstitutionIndex);

                    if (toBeReplaced != null && !toBeReplaced.isEmpty()
                            && replaceWith != null && !replaceWith.isEmpty()
                    )
                    {
                        JSONObject joUserSubstitution = new JSONObject();
                        jaUserSubstitutions.put(joUserSubstitution);

                        joUserSubstitution.put("ToBeReplaced", toBeReplaced);
                        joUserSubstitution.put("ReplaceWith", replaceWith);
                    }
                }
            }

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildEmailNotificationJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildFrameJson(
            String label, String title, List<String> tags, String ingester,
            Float instantInSeconds,
            String retention, JSONObject joUserData,
            Date startPublishing, Date endPublishing,
            List<MediaItemReference> mediaItemReferenceList
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "Frame");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

            setContentParameters(joParameters,
                    title, ingester, retention, null,
                    tags, joUserData,
                    startPublishing, endPublishing,
                    null, null);

            setCommonParameters(joParameters,
                    null,
                    mediaItemReferenceList,
                    null);

            joParameters.put("instantInSeconds", instantInSeconds);

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildFrameJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildExtractTracksJson(
            String label, String title, List<String> tags, String ingester,
            Long videoTrackNumber, Long audioTrackNumber, boolean forTheWorkflowEditor,
            String outputFileFormat,
            String retention, JSONObject joUserData,
            Date startPublishing, Date endPublishing,
            List<MediaItemReference> mediaItemReferenceList
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "Extract-Tracks");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

            setContentParameters(joParameters,
                    title, ingester, retention, null,
                    tags, joUserData,
                    startPublishing, endPublishing,
                    null, null);

            setCommonParameters(joParameters,
                    null,
                    mediaItemReferenceList,
                    null);

            if (outputFileFormat == null || outputFileFormat.isEmpty())
                joParameters.put("outputFileFormat", "mp4");
            else
                joParameters.put("outputFileFormat", outputFileFormat);

            if (forTheWorkflowEditor)
            {
                if (videoTrackNumber != null)
                    joParameters.put("VideoTrackNumber", videoTrackNumber);
                if (audioTrackNumber != null)
                    joParameters.put("AudioTrackNumber", audioTrackNumber);
            }
            else
            {
                JSONArray jaTracks = new JSONArray();
                joParameters.put("Tracks", jaTracks);

                if (videoTrackNumber != null)
                {
                    JSONObject joTrack = new JSONObject();
                    jaTracks.put(joTrack);

                    joTrack.put("TrackType", "video");
                    joTrack.put("TrackNumber", videoTrackNumber);
                }

                if (audioTrackNumber != null)
                {
                    JSONObject joTrack = new JSONObject();
                    jaTracks.put(joTrack);

                    joTrack.put("TrackType", "audio");
                    joTrack.put("TrackNumber", audioTrackNumber);
                }
            }

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildExtractTracksJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildAddContentJson(
            String label, String title, String fileFormat, List<String> tags, String ingester,
            String sourceURL, String pushBinaryFileName, Boolean regenerateTimestamps,
            String variantOfReferencedLabel,
            String externalDeliveryTechnology, String externalDeliveryURL,
            String uniqueName, Boolean allowUniqueNameOverride,
            String mediaItemRetention, String physicalItemRetention,
            JSONObject joUserData,
            Date startPublishing, Date endPublishing
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "Add-Content");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

            if (variantOfReferencedLabel != null && !variantOfReferencedLabel.isEmpty())
            {
                joParameters.put("ingester", ingester);

                joParameters.put("fileFormat", fileFormat);

                // see docs/TASK_01_Add_Content_JSON_Format.txt
                if (regenerateTimestamps != null)
                    joParameters.put("regenerateTimestamps", regenerateTimestamps);

                if (sourceURL != null && !sourceURL.isEmpty())
                    joParameters.put("sourceURL", sourceURL);
                else if (pushBinaryFileName != null)
                {
                    // this is a parameter just to manage the scenario described on AddContentProperties.setData(JSONObject)
                    joParameters.put("PushBinaryFileName", pushBinaryFileName);
                }

                joParameters.put("VariantOfReferencedLabel", variantOfReferencedLabel);

                if (externalDeliveryTechnology != null && !externalDeliveryTechnology.isEmpty())
                    joParameters.put("externalDeliveryTechnology", externalDeliveryTechnology);

                if (externalDeliveryURL != null && !externalDeliveryURL.isEmpty())
                    joParameters.put("externalDeliveryURL", externalDeliveryURL);
            }
            else
            {
                setContentParameters(joParameters,
                        title, ingester, mediaItemRetention, physicalItemRetention,
                        tags, joUserData,
                        startPublishing, endPublishing,
                        null, null);

                setCommonParameters(joParameters,
                        null,
                        null,
                        null);

                joParameters.put("fileFormat", fileFormat);

                // see docs/TASK_01_Add_Content_JSON_Format.txt
                if (regenerateTimestamps != null)
                    joParameters.put("regenerateTimestamps", regenerateTimestamps);

                if (sourceURL != null && !sourceURL.isEmpty())
                    joParameters.put("sourceURL", sourceURL);
                else if (pushBinaryFileName != null)
                {
                    // this is a parameter just to manage the scenario described on AddContentProperties.setData(JSONObject)
                    joParameters.put("PushBinaryFileName", pushBinaryFileName);
                }

                if (uniqueName != null && !uniqueName.isEmpty())
                {
                    joParameters.put("uniqueName", uniqueName);
                    if (allowUniqueNameOverride != null)
                        joParameters.put("allowUniqueNameOverride", allowUniqueNameOverride);
                }

                if (externalDeliveryTechnology != null && !externalDeliveryTechnology.isEmpty())
                    joParameters.put("externalDeliveryTechnology", externalDeliveryTechnology);

                if (externalDeliveryURL != null && !externalDeliveryURL.isEmpty())
                    joParameters.put("externalDeliveryURL", externalDeliveryURL);
            }

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildAddContentJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildMediaCrossReferenceJson(
            String label,
            String mediaCrossReferenceType,
            String firstReferenceLabel, Long firstReferenceMediaItemKey, // solo uno dei due è necessario
            String secondReferenceLabel, Long secondReferenceMediaItemKey, // solo uno dei due è necessario
            String dependenciesToBeAddedToReferencesAt  // Beginning, End, 0, 1, ...
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "Media-Cross-Reference");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

            if (mediaCrossReferenceType == null)
            {
                String errorMessage = "mediaCrossReferenceType cannot be null";
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            joParameters.put("type", mediaCrossReferenceType);

            if (firstReferenceLabel != null || secondReferenceLabel != null
                || firstReferenceMediaItemKey != null || secondReferenceMediaItemKey != null)
            {
                JSONArray joReferences = new JSONArray();
                joParameters.put("references", joReferences);

                if (firstReferenceMediaItemKey != null)
                {
                    JSONObject joReference = new JSONObject();
                    joReferences.put(joReference);

                    joReference.put("mediaItemKey", firstReferenceMediaItemKey);
                }
                else if (firstReferenceLabel != null)
                {
                    JSONObject joReference = new JSONObject();
                    joReferences.put(joReference);

                    joReference.put("label", firstReferenceLabel);
                }

                if (secondReferenceMediaItemKey != null)
                {
                    JSONObject joReference = new JSONObject();
                    joReferences.put(joReference);

                    joReference.put("mediaItemKey", secondReferenceMediaItemKey);
                }
                else if (secondReferenceLabel != null)
                {
                    JSONObject joReference = new JSONObject();
                    joReferences.put(joReference);

                    joReference.put("label", secondReferenceLabel);
                }
            }

            if (dependenciesToBeAddedToReferencesAt != null && !dependenciesToBeAddedToReferencesAt.isBlank())
                joParameters.put("dependenciesToBeAddedToReferencesAt", dependenciesToBeAddedToReferencesAt);


            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildMediaCrossReferenceJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildConcatDemuxJson(
            String label, String title, List<String> tags, String ingester,
            String mediaItemRetention, String physicalItemRetention,
            JSONObject joUserData,
            Date startPublishing, Date endPublishing,
            Float maxDurationInSeconds, Float extraSecondsToCutWhenMaxDurationIsReached,
            String dependenciesToBeAddedToReferencesAt,   // Beginning, End or an integer
            String uniqueName,
            Boolean allowUniqueNameOverride,
            List<MediaItemReference> mediaItemReferenceList,
            String waitForGlobalIngestionLabel,
            Long utcProcessingStartingFrom
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "Concat-Demuxer");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

            setContentParameters(joParameters,
                    title, ingester, mediaItemRetention, physicalItemRetention,
                    tags, joUserData,
                    startPublishing, endPublishing,
                    uniqueName, allowUniqueNameOverride);

            setCommonParameters(joParameters,
                    dependenciesToBeAddedToReferencesAt,
                    mediaItemReferenceList,
                    waitForGlobalIngestionLabel);

            if (maxDurationInSeconds != null)
                joParameters.put("MaxDurationInSeconds", maxDurationInSeconds);

            if (extraSecondsToCutWhenMaxDurationIsReached != null)
                joParameters.put("ExtraSecondsToCutWhenMaxDurationIsReached", extraSecondsToCutWhenMaxDurationIsReached);

            if (utcProcessingStartingFrom != null)
            {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                joParameters.put("processingStartingFrom", dateFormat.format(utcProcessingStartingFrom));
            }

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildConcatDemuxJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildCheckStreamingJson(
            String label,
            String inputType,           // Stream or URL
            String configurationLabel,  // only if inputType is Stream
            String streamingName,       // only if inputType is URL
            String streamingUrl,        // only if inputType is URL
            Boolean isVideo,
            String waitForGlobalIngestionLabel,
            Long utcProcessingStartingFrom
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "Check-Streaming");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

            setCommonParameters(joParameters,
                    null,
                    null,
                    waitForGlobalIngestionLabel);

            joParameters.put("inputType", inputType);

            if (inputType != null && inputType.equalsIgnoreCase("Stream"))
                joParameters.put("configurationLabel", configurationLabel);
            else
            {
                joParameters.put("streamingName", streamingName);
                joParameters.put("streamingUrl", streamingUrl);

            }

            if (isVideo != null)
                joParameters.put("isVideo", isVideo);

            if (utcProcessingStartingFrom != null)
            {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                joParameters.put("processingStartingFrom", dateFormat.format(utcProcessingStartingFrom));
            }

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildCheckStreamingJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildIntroOutroOverlayJson(
            String label, String title, List<String> tags, String ingester,
            String mediaItemRetention, String physicalItemRetention,
            JSONObject joUserData,
            Date startPublishing, Date endPublishing,
            String encodingPriority, String encodingProfileLabel, String encodersPool,
            Long introOverlayDurationInSeconds, Long outroOverlayDurationInSeconds,
            Boolean muteIntroOverlay, Boolean muteOutroOverlay,
            String dependenciesToBeAddedToReferencesAt,   // Beginning, End or an integer
            String uniqueName,
            Boolean allowUniqueNameOverride,
            List<MediaItemReference> mediaItemReferenceList,
            String waitForGlobalIngestionLabel
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "Intro-Outro-Overlay");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

            setContentParameters(joParameters,
                    title, ingester, mediaItemRetention, physicalItemRetention,
                    tags, joUserData,
                    startPublishing, endPublishing,
                    uniqueName, allowUniqueNameOverride);

            setCommonParameters(joParameters,
                    dependenciesToBeAddedToReferencesAt,
                    mediaItemReferenceList,
                    waitForGlobalIngestionLabel);

            if (encodingPriority != null && !encodingPriority.isEmpty())
                joParameters.put("encodingPriority", encodingPriority);

            if (encodingProfileLabel != null && !encodingProfileLabel.isEmpty())
                joParameters.put("encodingProfileLabel", encodingProfileLabel);

            if (encodersPool != null && !encodersPool.isEmpty())
                joParameters.put("encodersPool", encodersPool);

            if (introOverlayDurationInSeconds != null)
                joParameters.put("introOverlayDurationInSeconds", introOverlayDurationInSeconds);

            if (outroOverlayDurationInSeconds != null)
                joParameters.put("outroOverlayDurationInSeconds", outroOverlayDurationInSeconds);

            if (muteIntroOverlay != null)
                joParameters.put("muteIntroOverlay", muteIntroOverlay);

            if (muteOutroOverlay != null)
                joParameters.put("muteOutroOverlay", muteOutroOverlay);

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildIntroOutroOverlayJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildCutJson(
            String label, String title, List<String> tags, String ingester,
            String outputFileFormat,
            String startTime, String endTime, String cutType,
            String timesRelativeToMetaDataField,
            Boolean fixEndTimeIfOvercomeDuration,
            String retention, JSONObject joUserData,
            Date startPublishing, Date endPublishing,
            List<MediaItemReference> mediaItemReferenceList
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "Cut");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

            setContentParameters(joParameters,
                    title, ingester, retention, null,
                    tags, joUserData,
                    startPublishing, endPublishing,
                    null, null);

            setCommonParameters(joParameters,
                    null,
                    mediaItemReferenceList,
                    null);

            if (outputFileFormat != null)
                joParameters.put("outputFileFormat", outputFileFormat);
            joParameters.put("startTime", startTime);
            joParameters.put("endTime", endTime);
            joParameters.put("cutType", cutType);
            if (timesRelativeToMetaDataField != null && !timesRelativeToMetaDataField.isBlank())
                joParameters.put("timesRelativeToMetaDataField", timesRelativeToMetaDataField);
            if (fixEndTimeIfOvercomeDuration != null)
                joParameters.put("fixEndTimeIfOvercomeDuration", fixEndTimeIfOvercomeDuration);


            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildCutJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildLiveCutJson(
            String label, String title, String uniqueName, Boolean allowUniqueNameOverride,
            List<String> tags, String ingester,

            Long recordingCode,

            Long utcLiveCutStartInMilliSecs,
            Long utcLiveCutEndInMilliSecs,
            Long chunkEncodingProfileKey,
            int maxWaitingForLastChunkInSeconds,
            String mediaItemRetention, String physicalItemRetention,
            JSONObject joUserData,
            Date startPublishing, Date endPublishing,
			Long utcProcessingStartingFrom
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "Live-Cut");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

            setContentParameters(joParameters,
                    title, ingester, mediaItemRetention, physicalItemRetention,
                    tags, joUserData,
                    startPublishing, endPublishing,
                    uniqueName, allowUniqueNameOverride);

            joParameters.put("recordingCode", recordingCode);

            if (chunkEncodingProfileKey != null)
                joParameters.put("chunkEncodingProfileKey", chunkEncodingProfileKey);

            {
                JSONObject joCutPeriod = new JSONObject();
                joParameters.put("cutPeriod", joCutPeriod);

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                joCutPeriod.put("start", dateFormat.format(utcLiveCutStartInMilliSecs));
                joCutPeriod.put("end", dateFormat.format(utcLiveCutEndInMilliSecs));
            }
            joParameters.put("maxWaitingForLastChunkInSeconds", maxWaitingForLastChunkInSeconds);

			if (utcProcessingStartingFrom != null)
            {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                joParameters.put("processingStartingFrom", dateFormat.format(utcProcessingStartingFrom));
            }


            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildLiveCutJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildChangeFileFormat(
            String label, String outputFileFormat,
            List<MediaItemReference> mediaItemReferenceList,
            Long utcProcessingStartingFrom
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "Change-File-Format");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

            setCommonParameters(joParameters,
                    null,
                    mediaItemReferenceList,
                    null);

            joParameters.put("outputFileFormat", outputFileFormat);

            if (utcProcessingStartingFrom != null)
            {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                joParameters.put("processingStartingFrom", dateFormat.format(utcProcessingStartingFrom));
            }

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildChangeFileFormat failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildEncodeJson(
            String label, String ingester,
			// video, audio, image (it has to used only in case the workflow is sent to the workflow editor page)
            String contentType,
            String encodingPriority,	// Low, Medium, High 
			// encodingProfileLabel or encodingProfileSetLabel has to be present
			String encodingProfileLabel, String encodingProfilesSetLabel,
            String encodersPool,
            Long videoTrackIndex, Long audioTrackIndex, // null se non si vuole specificare la traccia
            Filters filters,
            List<MediaItemReference> mediaItemReferenceList,
            Long utcProcessingStartingFrom
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "Encode");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

            joParameters.put("ingester", ingester);

            if (contentType != null)
                joParameters.put("contentType", contentType);

            joParameters.put("encodingPriority", encodingPriority);
            if (encodingProfileLabel != null)
                joParameters.put("encodingProfileLabel", encodingProfileLabel);
			else if (encodingProfilesSetLabel != null)
                joParameters.put("encodingProfilesSetLabel", encodingProfilesSetLabel);

            if (encodersPool != null && !encodersPool.isEmpty())
                joParameters.put("encodersPool", encodersPool);

            if (videoTrackIndex != null && videoTrackIndex >= 0)
                joParameters.put("videoTrackIndex", videoTrackIndex);
            if (audioTrackIndex != null && audioTrackIndex >= 0)
                joParameters.put("audioTrackIndex", audioTrackIndex);

            if (filters != null)
                joParameters.put("filters", filters.toJson());

            if (utcProcessingStartingFrom != null)
            {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                joParameters.put("processingStartingFrom", dateFormat.format(utcProcessingStartingFrom));
            }

            setCommonParameters(joParameters,
                    null,
                    mediaItemReferenceList,
                    null);

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildEncodeJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildLocalCopy(
            String label, String destinationLocalPath, String destinationLocalFileName,
            List<MediaItemReference> mediaItemReferenceList
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "Local-Copy");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

            joParameters.put("LocalPath", destinationLocalPath);
            joParameters.put("LocalFileName", destinationLocalFileName);

            setCommonParameters(joParameters,
                    null,
                    mediaItemReferenceList,
                    null);

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildLocalCopy failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildFTPDelivery(
            String label, String ftpConfigurationLabel
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "FTP-Delivery");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

            joParameters.put("configurationLabel", ftpConfigurationLabel);

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildFTPDelivery failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildHTTPCallback(
            String label,
			String userName, String password,
            String method, // GET, POST, PUT
            boolean addMediaData,
            String protocol, String hostName, String uri, 
			String parameters,	// ?param1=value1&....
            boolean formData,
			String httpBody,
            int timeoutInSeconds, int maxRetries,
            List<MediaItemReference> mediaItemReferenceList
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "HTTP-Callback");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

			if (userName != null && !userName.isEmpty()
				&& password != null && !password.isEmpty())
			{
				joParameters.put("userName", userName);
				joParameters.put("password", password);	
			}

            joParameters.put("addMediaData", addMediaData);

			joParameters.put("protocol", protocol);
            joParameters.put("hostName", hostName);
            joParameters.put("uri", uri);
            if (parameters != null)
                joParameters.put("parameters", parameters);
            joParameters.put("formData", formData);
            joParameters.put("method", method);
			if (httpBody != null && httpBody.length() > 0)
	            joParameters.put("httpBody", httpBody);
            joParameters.put("timeout", timeoutInSeconds);
            joParameters.put("maxRetries", maxRetries);

            setCommonParameters(joParameters,
                    null,
                    mediaItemReferenceList,
                    null);

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildHTTPPOSTCallback failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildRemoveJson(
            String label, String ingester,
            List<MediaItemReference> mediaItemReferenceList
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "Remove-Content");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

            joParameters.put("ingester", ingester);

            setCommonParameters(joParameters,
                    null,
                    mediaItemReferenceList,
                    null);

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildRemoveJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildVideoSpeedJson(
            String label, String title, List<String> tags, String ingester,
            String videoSpeedType, int videoSpeedSize,
            String encodingPriority,
            String mediaItemRetention, String physicalItemRetention,
            JSONObject joUserData,
            Date startPublishing, Date endPublishing,
            List<MediaItemReference> mediaItemReferenceList
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "Video-Speed");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

            setContentParameters(joParameters,
                    title, ingester, mediaItemRetention, physicalItemRetention,
                    tags, joUserData,
                    startPublishing, endPublishing,
                    null, null);

            setCommonParameters(joParameters,
                    null,
                    mediaItemReferenceList,
                    null);

            joParameters.put("VideoSpeedType", videoSpeedType);
            joParameters.put("VideoSpeedSize", videoSpeedSize);
            joParameters.put("encodingPriority", encodingPriority);

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildVideoSpeedJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }
    
	static public JSONObject buildOverlayTextOnVideo(
            String label, String ingester,

            DrawTextDetails drawTextDetails,

			String encodingPriority,	// Low, Medium, High 
			String encodingProfileLabel,
            String encodersPool,

			List<MediaItemReference> mediaItemReferenceList,

            String title,
            String mediaItemRetention,

			Long utcProcessingStartingFrom
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("label", label);
            joTask.put("type", "Overlay-Text-On-Video");

            JSONObject joParameters = new JSONObject();
            joTask.put("parameters", joParameters);

            joParameters.put("ingester", ingester);

            joParameters.put("drawTextDetails", drawTextDetails.toJson());

			joParameters.put("encodingPriority", encodingPriority);

            if (encodingProfileLabel != null && !encodingProfileLabel.isEmpty())
                joParameters.put("encodingProfileLabel", encodingProfileLabel);

            if (encodersPool != null && !encodersPool.isEmpty())
                joParameters.put("encodersPool", encodersPool);

			joParameters.put("title", title);
			joParameters.put("retention", mediaItemRetention);

            if (utcProcessingStartingFrom != null)
            {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                joParameters.put("processingStartingFrom", dateFormat.format(utcProcessingStartingFrom));
            }

            setCommonParameters(joParameters,
                    null,
                    mediaItemReferenceList,
                    null);

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildOverlayTextOnVideo failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static private void setContentParameters(
            JSONObject joParameters,

            String title, String ingester,
            String mediaItemRetention, String physicalItemRetention,
            List<String> tags,
            JSONObject joUserData,
            Date startPublishing, Date endPublishing,
            String uniqueName, Boolean allowUniqueNameOverride
    )
            throws Exception
    {
        try
        {
            joParameters.put("title", title);
            joParameters.put("ingester", ingester);
            if (mediaItemRetention != null && !mediaItemRetention.isEmpty())
                joParameters.put("retention", mediaItemRetention);
            if (physicalItemRetention != null && !physicalItemRetention.isEmpty())
                joParameters.put("PhysicalItemRetention", physicalItemRetention);

            if (tags != null && tags.size() > 0)
            {
                JSONArray jaTags = new JSONArray();
                joParameters.put("tags", jaTags);

                for(String tag: tags)
                {
                    if (!tag.trim().isEmpty())
                        jaTags.put(tag.trim());
                }
            }

            if (joUserData != null)
                joParameters.put("userData", joUserData);

            if (startPublishing!= null && endPublishing != null)
            {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                JSONObject joPublishing = new JSONObject();
                joParameters.put("publishing", joPublishing);

                joPublishing.put("startPublishing", dateFormat.format(startPublishing));
                joPublishing.put("endPublishing", dateFormat.format(endPublishing));
            }

            if (uniqueName != null)
                joParameters.put("uniqueName", uniqueName);

            if (allowUniqueNameOverride != null)
                joParameters.put("allowUniqueNameOverride", allowUniqueNameOverride);
        }
        catch (Exception e)
        {
            String errorMessage = "setContentParameters failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static private void setCommonParameters(
            JSONObject joParameters,

            String dependenciesToBeAddedToReferencesAt,
            List<MediaItemReference> mediaItemReferenceList,
            String waitForGlobalIngestionLabel
    )
            throws Exception
    {
        try
        {
            if (waitForGlobalIngestionLabel != null && !waitForGlobalIngestionLabel.isEmpty())
            {
                JSONArray jaWaitForArray = new JSONArray();
                joParameters.put("waitFor", jaWaitForArray);

                JSONObject joWaitForLabel = new JSONObject();
                joWaitForLabel.put("globalIngestionLabel", waitForGlobalIngestionLabel);

                jaWaitForArray.put(joWaitForLabel);
            }

            if (dependenciesToBeAddedToReferencesAt != null
                    && !dependenciesToBeAddedToReferencesAt.isEmpty())
                joParameters.put("dependenciesToBeAddedToReferencesAt", dependenciesToBeAddedToReferencesAt);

            if (mediaItemReferenceList != null && mediaItemReferenceList.size() > 0)
            {
                JSONArray jsonReferencesArray = new JSONArray();
                joParameters.put("references", jsonReferencesArray);

                for(MediaItemReference mediaItemReference: mediaItemReferenceList)
                {
                    JSONObject joReference = new JSONObject();
                    jsonReferencesArray.put(joReference);

                    if (mediaItemReference.getMediaItemKey() != null)
                    {
                        joReference.put("mediaItemKey", mediaItemReference.getMediaItemKey());
                        if (mediaItemReference.getEncodingProfileKey() != null)
                            joReference.put("encodingProfileKey", mediaItemReference.getEncodingProfileKey());
                        else if (mediaItemReference.getEncodingProfileLabel() != null)
                            joReference.put("encodingProfileLabel", mediaItemReference.getEncodingProfileLabel());

                        Boolean stopIfReferenceProcessingError = false;
                        if (mediaItemReference.getStopIfReferenceProcessingError() != null)
                            stopIfReferenceProcessingError = mediaItemReference.getStopIfReferenceProcessingError();
                        joReference.put("stopIfReferenceProcessingError", stopIfReferenceProcessingError);
                    }
                    else if (mediaItemReference.getUniqueName() != null)
                    {
                        joReference.put("uniqueName", mediaItemReference.getUniqueName());
                        if (mediaItemReference.getEncodingProfileKey() != null)
                            joReference.put("encodingProfileKey", mediaItemReference.getEncodingProfileKey());
                        else if (mediaItemReference.getEncodingProfileLabel() != null)
                            joReference.put("encodingProfileLabel", mediaItemReference.getEncodingProfileLabel());

                        Boolean stopIfReferenceProcessingError = false;
                        if (mediaItemReference.getStopIfReferenceProcessingError() != null)
                            stopIfReferenceProcessingError = mediaItemReference.getStopIfReferenceProcessingError();
                        joReference.put("stopIfReferenceProcessingError", stopIfReferenceProcessingError);
                    }
                    else if (mediaItemReference.getPhysicalPathKey() != null)
                    {
                        joReference.put("physicalPathKey", mediaItemReference.getPhysicalPathKey());
                    }
                    else if (mediaItemReference.getIngestionLabel() != null)
                    {
                        joReference.put("label", mediaItemReference.getIngestionLabel());
                    }
                }
            }
        }
        catch (Exception e)
        {
            String errorMessage = "setCommonParameters failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }
}
