package com.catrammslib;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.catrammslib.entity.WorkflowVariable;
import com.catrammslib.utility.LiveProxyOutput;
import com.catrammslib.utility.MediaItemReference;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class CatraMMSWorkflow {

    private static final Logger mLogger = Logger.getLogger(CatraMMSWorkflow.class);

    static public JSONObject buildWorkflowRootJson(String label)
            throws Exception
    {
        try
        {
            JSONObject joWorkflow = new JSONObject();
            joWorkflow.put("Label", label);
            joWorkflow.put("Type", "Workflow");

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

            joGroupOfTasks.put("Label", label);
            joGroupOfTasks.put("Type", "GroupOfTasks");
            JSONArray jaTasks;
            JSONArray jaReferencesOutput = new JSONArray();
            {
                JSONObject joParameters = new JSONObject();
                joGroupOfTasks.put("Parameters", joParameters);

                joParameters.put("ExecutionType", executionType);

                jaTasks = new JSONArray();
                joParameters.put("Tasks", jaTasks);

                if (referencesOutput != null && referencesOutput.size() > 0)
                {
                    // jaReferencesOutput = new JSONArray();
                    joParameters.put("ReferencesOutput", jaReferencesOutput);

                    for(String referenceOutput: referencesOutput)
                    {
                        JSONObject joReferenceOutput = new JSONObject();
                        jaReferencesOutput.put(joReferenceOutput);

                        joReferenceOutput.put("ReferenceLabel", referenceOutput);
                    }
                }

                if (utcProcessingStartingFrom != null)
                {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                    joParameters.put("ProcessingStartingFrom", dateFormat.format(utcProcessingStartingFrom));
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

            joTask.put("Label", label);
            joTask.put("Type", "Workflow-As-Library");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

            joParameters.put("WorkflowAsLibraryLabel", workflowAsLibraryLabel);
            joParameters.put("WorkflowAsLibraryType", workflowAsLibraryType);

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
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                    joParameters.put(workflowVariable.getName(), dateFormat.format(workflowVariable.getDatetimeValue()));
                }
                else if(workflowVariable.getType().equalsIgnoreCase("datetime-millisecs"))
                {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                    joParameters.put(workflowVariable.getName(), dateFormat.format(workflowVariable.getDatetimeValue()));
                }
                else if(workflowVariable.getType().equalsIgnoreCase("jsonObject"))
                    joParameters.put(workflowVariable.getName(), workflowVariable.getJsonObjectValue());
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

            Long deliveryCode,        // mandatory

            String liveConfigurationLabel,

            List<String> chunkTags, String ingester,
            String chunkRetention, JSONObject joUserData,
            // String liveRecorderConfiguration,
            String encodingPriority,
            // boolean highAvailability,
            Boolean autoRenew,
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
            List<LiveProxyOutput> liveRecorderOutputList
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("Label", label);
            joTask.put("Type", "Live-Recorder");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

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

            joParameters.put("DeliveryCode", deliveryCode);

            joParameters.put("ConfigurationLabel", liveConfigurationLabel);

            joParameters.put("EncoderPriority", encodingPriority);
            // joParameters.put("HighAvailability", highAvailability);
            joParameters.put("OutputFileFormat", outputFileFormat);
            joParameters.put("SegmentDuration", segmentDurationInSeconds);

            if (encodersPool != null && !encodersPool.isEmpty())
                joParameters.put("EncodersPool", encodersPool);

            if (userAgent != null && !userAgent.isEmpty())
                joParameters.put("UserAgent", userAgent);

            {
                JSONObject joRecordingPeriod = new JSONObject();
                joParameters.put("RecordingPeriod", joRecordingPeriod);

                if (autoRenew != null)
                    joRecordingPeriod.put("AutoRenew", autoRenew);

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                joRecordingPeriod.put("Start", dateFormat.format(utcLiveRecorderStart));
                joRecordingPeriod.put("End", dateFormat.format(utcLiveRecorderEnd));
            }

            if (monitorHLS != null && monitorHLS)
            {
                JSONObject joMonitorHLS = new JSONObject();
                joParameters.put("MonitorHLS", joMonitorHLS);

                if (monitorHLSEncodingProfileLabel != null && !monitorHLSEncodingProfileLabel.isEmpty())
                    joMonitorHLS.put("EncodingProfileLabel", monitorHLSEncodingProfileLabel);
            }
            if (liveRecorderVirtualVOD)
            {
                JSONObject joVirtualVOD = new JSONObject();
                joParameters.put("LiveRecorderVirtualVOD", joVirtualVOD);

                if (liveRecorderVirtualVODMaxDurationInMinutes != null)
                    joVirtualVOD.put("LiveRecorderVirtualVODMaxDuration", liveRecorderVirtualVODMaxDurationInMinutes);
                if (liveRecorderVirtualVODEncodingProfileLabel != null && !liveRecorderVirtualVODEncodingProfileLabel.isEmpty())
                    joVirtualVOD.put("EncodingProfileLabel", liveRecorderVirtualVODEncodingProfileLabel);
            }

            if (liveRecorderOutputList != null && liveRecorderOutputList.size() > 0)
            {
                JSONArray jaOutputs = new JSONArray();
                joParameters.put("Outputs", jaOutputs);

                for(LiveProxyOutput liveProxyOutput: liveRecorderOutputList)
                {
                    JSONObject joOutput = new JSONObject();
                    jaOutputs.put(joOutput);

                    joOutput.put("OutputType", liveProxyOutput.getOutputType());
                    if (liveProxyOutput.getOutputType().equalsIgnoreCase("RTMP_Stream"))
                        joOutput.put("RtmpUrl", liveProxyOutput.getRtmpURL());
                    else
                    {
                        joOutput.put("DeliveryCode", liveProxyOutput.getDeliveryCode());
                        if (liveProxyOutput.getSegmentDurationInSeconds() != null)
                            joOutput.put("SegmentDurationInSeconds", liveProxyOutput.getSegmentDurationInSeconds());
                    }

                    if (liveProxyOutput.getEncodingProfileLabel() != null)
                        joOutput.put("EncodingProfileLabel", liveProxyOutput.getEncodingProfileLabel());

                    if (liveProxyOutput.getOtherOutputOptions() != null && !liveProxyOutput.getOtherOutputOptions().isEmpty())
                        joOutput.put("OtherOutputOptions", liveProxyOutput.getOtherOutputOptions());

                    if (liveProxyOutput.getAudioVolumeChange() != null && !liveProxyOutput.getAudioVolumeChange().isEmpty())
                        joOutput.put("AudioVolumeChange", liveProxyOutput.getAudioVolumeChange());
                }
            }

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

            String encodersPool,
            Date proxyStartTime, Date proxyEndTime,
            String userAgent,
            Long maxWidth,
            String otherInputOptions,
            Long maxAttemptsNumberInCaseOfErrors,
            Long waitingSecondsBetweenAttemptsInCaseOfErrors,
            List<LiveProxyOutput> liveProxyOutputList,
			JSONObject joBroadcasterParameters
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("Label", label);
            joTask.put("Type", "Live-Proxy");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

			joParameters.put("ConfigurationLabel", liveConfigurationLabel);

            if (encodersPool != null && !encodersPool.isEmpty())
                joParameters.put("EncodersPool", encodersPool);

            if (userAgent != null && !userAgent.isEmpty())
                joParameters.put("UserAgent", userAgent);

            if (maxWidth != null)
                joParameters.put("MaxWidth", maxWidth);

            if (otherInputOptions != null && !otherInputOptions.isEmpty())
                joParameters.put("OtherInputOptions", otherInputOptions);

            if (maxAttemptsNumberInCaseOfErrors != null)
                joParameters.put("MaxAttemptsNumberInCaseOfErrors", maxAttemptsNumberInCaseOfErrors);
            if (waitingSecondsBetweenAttemptsInCaseOfErrors != null)
                joParameters.put("WaitingSecondsBetweenAttemptsInCaseOfErrors", waitingSecondsBetweenAttemptsInCaseOfErrors);

            if (proxyStartTime != null && proxyEndTime != null)
            {
                joParameters.put("TimePeriod", true);

                JSONObject joProxyPeriod = new JSONObject();
                joParameters.put("ProxyPeriod", joProxyPeriod);

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                joProxyPeriod.put("Start", dateFormat.format(proxyStartTime));
                joProxyPeriod.put("End", dateFormat.format(proxyEndTime));
            }
            else
                joParameters.put("TimePeriod", false);

            if (liveProxyOutputList == null || liveProxyOutputList.size() == 0)
            {
                String errorMessage = "At least one liveProxyOutput has to be present";
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            JSONArray jaOutputs = new JSONArray();
            joParameters.put("Outputs", jaOutputs);

            for(LiveProxyOutput liveProxyOutput: liveProxyOutputList)
            {
                JSONObject joOutput = new JSONObject();
                jaOutputs.put(joOutput);

                joOutput.put("OutputType", liveProxyOutput.getOutputType());
                if (liveProxyOutput.getOutputType().equalsIgnoreCase("RTMP_Stream"))
                    joOutput.put("RtmpUrl", liveProxyOutput.getRtmpURL());
				else if (liveProxyOutput.getOutputType().equalsIgnoreCase("UDP_Stream"))
                    joOutput.put("udpUrl", liveProxyOutput.getUdpURL());
                else
                {
                    joOutput.put("DeliveryCode", liveProxyOutput.getDeliveryCode());
                    if (liveProxyOutput.getSegmentDurationInSeconds() != null)
                        joOutput.put("SegmentDurationInSeconds", liveProxyOutput.getSegmentDurationInSeconds());
                }

                if (liveProxyOutput.getEncodingProfileLabel() != null)
                    joOutput.put("EncodingProfileLabel", liveProxyOutput.getEncodingProfileLabel());

                if (liveProxyOutput.getOtherOutputOptions() != null && !liveProxyOutput.getOtherOutputOptions().isEmpty())
                    joOutput.put("OtherOutputOptions", liveProxyOutput.getOtherOutputOptions());

                if (liveProxyOutput.getAudioVolumeChange() != null && !liveProxyOutput.getAudioVolumeChange().isEmpty())
                    joOutput.put("AudioVolumeChange", liveProxyOutput.getAudioVolumeChange());
            }

			if (joBroadcasterParameters != null)
				joParameters.put("broadcaster", joBroadcasterParameters);

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
            List<LiveProxyOutput> liveProxyOutputList
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("Label", label);
            joTask.put("Type", "VOD-Proxy");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

            setCommonParameters(joParameters,
				null,
				mediaItemReferenceList,
				null);

            if (encodersPool != null && !encodersPool.isEmpty())
                joParameters.put("EncodersPool", encodersPool);

            if (otherInputOptions != null && !otherInputOptions.isEmpty())
                joParameters.put("OtherInputOptions", otherInputOptions);

            if (proxyStartTime != null && proxyEndTime != null)
            {
                joParameters.put("TimePeriod", true);

                JSONObject joProxyPeriod = new JSONObject();
                joParameters.put("ProxyPeriod", joProxyPeriod);

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                joProxyPeriod.put("Start", dateFormat.format(proxyStartTime));
                joProxyPeriod.put("End", dateFormat.format(proxyEndTime));
            }
            else
                joParameters.put("TimePeriod", false);

            if (liveProxyOutputList == null || liveProxyOutputList.size() == 0)
            {
                String errorMessage = "At least one liveProxyOutput has to be present";
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            JSONArray jaOutputs = new JSONArray();
            joParameters.put("Outputs", jaOutputs);

            for(LiveProxyOutput liveProxyOutput: liveProxyOutputList)
            {
                JSONObject joOutput = new JSONObject();
                jaOutputs.put(joOutput);

                joOutput.put("OutputType", liveProxyOutput.getOutputType());
                if (liveProxyOutput.getOutputType().equalsIgnoreCase("RTMP_Stream"))
                    joOutput.put("RtmpUrl", liveProxyOutput.getRtmpURL());
				else if (liveProxyOutput.getOutputType().equalsIgnoreCase("UDP_Stream"))
                    joOutput.put("udpUrl", liveProxyOutput.getUdpURL());
                else
                {
                    joOutput.put("DeliveryCode", liveProxyOutput.getDeliveryCode());
                    if (liveProxyOutput.getSegmentDurationInSeconds() != null)
                        joOutput.put("SegmentDurationInSeconds", liveProxyOutput.getSegmentDurationInSeconds());
                }

                if (liveProxyOutput.getEncodingProfileLabel() != null)
                    joOutput.put("EncodingProfileLabel", liveProxyOutput.getEncodingProfileLabel());

                if (liveProxyOutput.getOtherOutputOptions() != null && !liveProxyOutput.getOtherOutputOptions().isEmpty())
                    joOutput.put("OtherOutputOptions", liveProxyOutput.getOtherOutputOptions());

                if (liveProxyOutput.getAudioVolumeChange() != null && !liveProxyOutput.getAudioVolumeChange().isEmpty())
                    joOutput.put("AudioVolumeChange", liveProxyOutput.getAudioVolumeChange());
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

	static public JSONObject buildPostOnYouTube(
            String label,

			String youTubeConfigurationLabel,
            String title,
            String description,
            List<String> tags,
			Long categoryId,
			String privacyStatus,

            List<MediaItemReference> mediaItemReferenceList
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("Label", label);
            joTask.put("Type", "Post-On-YouTube");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

            setCommonParameters(joParameters,
                    null,
                    mediaItemReferenceList,
                    null);

			joParameters.put("ConfigurationLabel", youTubeConfigurationLabel);

            if (title != null && !title.isEmpty())
                joParameters.put("Title", title);

			if (description != null && !description.isEmpty())
                joParameters.put("Description", description);

			if (tags != null)
			{
				JSONArray jaTags = new JSONArray();
				joParameters.put("Tags", jaTags);

				for (String tag: tags)
				{
					if (!tag.isEmpty())
						jaTags.put(tag);
				}
			}

			if (categoryId != null)
                joParameters.put("CategoryId", categoryId);

			if (privacyStatus != null && !privacyStatus.isEmpty())
                joParameters.put("Privacy", privacyStatus);

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

            joTask.put("Label", label);
            joTask.put("Type", "YouTube-Live-Broadcast");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

			joParameters.put("YouTubeConfigurationLabel", youTubeConfigurationLabel);

            if (title != null && !title.isEmpty())
                joParameters.put("Title", title);

			if (description != null && !description.isEmpty())
                joParameters.put("Description", description);

			if (privacyStatus != null && !privacyStatus.isEmpty())
                joParameters.put("Privacy", privacyStatus);

			if (madeForKids != null)
                joParameters.put("MadeForKids", madeForKids);

			if (latencyPreference != null && !latencyPreference.isEmpty())
                joParameters.put("LatencyPreference", latencyPreference);

			if (channelConfigurationLabel != null)
			{
                joParameters.put("SourceType", "Live");
                joParameters.put("ConfigurationLabel", channelConfigurationLabel);
			}
			else
			{
                joParameters.put("SourceType", "MediaItem");

				setCommonParameters(joParameters,
					null,
					mediaItemReferenceList,
					null);
			}

			if (encodersPool != null && !encodersPool.isEmpty())
                joParameters.put("EncodersPool", encodersPool);

			{
				JSONObject joProxyPeriod = new JSONObject();
				joParameters.put("ProxyPeriod", joProxyPeriod);

				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

				joProxyPeriod.put("Start", dateFormat.format(startTime));
				joProxyPeriod.put("End", dateFormat.format(endTime));
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
            String nodeId,

            List<MediaItemReference> mediaItemReferenceList
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("Label", label);
            joTask.put("Type", "Post-On-Facebook");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

            setCommonParameters(joParameters,
                    null,
                    mediaItemReferenceList,
                    null);

			joParameters.put("ConfigurationLabel", facebookConfigurationLabel);

            if (nodeId != null && !nodeId.isEmpty())
                joParameters.put("NodeId", nodeId);

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildPostOnFacebook failed. Exception: " + e;
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

            joTask.put("Label", label);
            joTask.put("Type", "Face-Recognition");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

            setContentParameters(joParameters,
                    title, ingester, retention, null,
                    tags, joUserData,
                    startPublishing, endPublishing,
                    null, null);

            setCommonParameters(joParameters,
                    null,
                    mediaItemReferenceList,
                    null);

            joParameters.put("CascadeName", "haarcascade_frontalface_alt_tree");
            joParameters.put("EncodingPriority", "High");    // takes a lot of time by the MMSEngine
            joParameters.put("InitialFramesNumberToBeSkipped", faceRecognition_InitialFramesNumberToBeSkipped);
            joParameters.put("OneFramePerSecond", true);
            joParameters.put("Output", "FrameContainingFace");

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
                    joParentOnSuccess.put("Task", joImageGroupOfTasks);
            }

            JSONObject joFrameContainingFace = CatraMMSWorkflow.buildFaceRecognitionJson(
                    frameContainingFaceImageReferenceLabel, title, imageTags, ingester,
                    imageRetention, null, null, null,
                    null, faceRecognition_InitialFramesNumberToBeSkipped);
            jaImageGroupOfTasks.put(joFrameContainingFace);

            {
                JSONObject joFrameContainingFaceOnSuccess = CatraMMSWorkflow.buildEventJson(joFrameContainingFace, "OnSuccess");

                joFrameContainingFaceOnSuccess.put("Task", CatraMMSWorkflow.buildEncodeJson(
                        "Encode image (FrameContainingFace): " + title,
                        ingester, "image",
                        "High",
                        "MMS_JPG_W240", null,
                        null,
                        null, null,
                        null,
                        utcProcessingStartingFrom
                ));
            }

            JSONObject joFrame;
            {
                JSONObject joFrameContainingFaceOnError = CatraMMSWorkflow.buildEventJson(joFrameContainingFace, "OnError");

                joFrame = CatraMMSWorkflow.buildFrameJson(
                        frameImageReferenceLabel, title,
                        imageTags, ingester, frameCaptureSeconds, imageRetention,
                        null, null, null, null
                );
                joFrameContainingFaceOnError.put("Task", joFrame);
            }

            {
                JSONObject joFrameOnSuccess = CatraMMSWorkflow.buildEventJson(joFrame, "OnSuccess");

                joFrameOnSuccess.put("Task", CatraMMSWorkflow.buildEncodeJson(
                        "Encode image (Frame): " + title,
                        ingester, "image",
                        "High",
                        "MMS_JPG_W240", null,
                        null,
                        null, null,
                        null,
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

            joTask.put("Label", label);
            joTask.put("Type", "Email-Notification");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

            joParameters.put("ConfigurationLabel", configurationLabel);

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

            joTask.put("Label", label);
            joTask.put("Type", "Frame");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

            setContentParameters(joParameters,
                    title, ingester, retention, null,
                    tags, joUserData,
                    startPublishing, endPublishing,
                    null, null);

            setCommonParameters(joParameters,
                    null,
                    mediaItemReferenceList,
                    null);

            joParameters.put("InstantInSeconds", instantInSeconds);

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

            joTask.put("Label", label);
            joTask.put("Type", "Extract-Tracks");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

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
                joParameters.put("OutputFileFormat", "mp4");
            else
                joParameters.put("OutputFileFormat", outputFileFormat);

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

            joTask.put("Label", label);
            joTask.put("Type", "Add-Content");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

            if (variantOfReferencedLabel != null && !variantOfReferencedLabel.isEmpty())
            {
                joParameters.put("Ingester", ingester);

                joParameters.put("FileFormat", fileFormat);

                // see docs/TASK_01_Add_Content_JSON_Format.txt
                if (regenerateTimestamps != null)
                    joParameters.put("RegenerateTimestamps", regenerateTimestamps);

                if (sourceURL != null && !sourceURL.isEmpty())
                    joParameters.put("SourceURL", sourceURL);
                else if (pushBinaryFileName != null)
                {
                    // this is a parameter just to manage the scenario described on AddContentProperties.setData(JSONObject)
                    joParameters.put("PushBinaryFileName", pushBinaryFileName);
                }

                joParameters.put("VariantOfReferencedLabel", variantOfReferencedLabel);

                if (externalDeliveryTechnology != null && !externalDeliveryTechnology.isEmpty())
                    joParameters.put("ExternalDeliveryTechnology", externalDeliveryTechnology);

                if (externalDeliveryURL != null && !externalDeliveryURL.isEmpty())
                    joParameters.put("ExternalDeliveryURL", externalDeliveryURL);
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

                joParameters.put("FileFormat", fileFormat);

                // see docs/TASK_01_Add_Content_JSON_Format.txt
                if (regenerateTimestamps != null)
                    joParameters.put("RegenerateTimestamps", regenerateTimestamps);

                if (sourceURL != null && !sourceURL.isEmpty())
                    joParameters.put("SourceURL", sourceURL);
                else if (pushBinaryFileName != null)
                {
                    // this is a parameter just to manage the scenario described on AddContentProperties.setData(JSONObject)
                    joParameters.put("PushBinaryFileName", pushBinaryFileName);
                }

                if (uniqueName != null && !uniqueName.isEmpty())
                {
                    joParameters.put("UniqueName", uniqueName);
                    if (allowUniqueNameOverride != null)
                        joParameters.put("AllowUniqueNameOverride", allowUniqueNameOverride);
                }

                if (externalDeliveryTechnology != null && !externalDeliveryTechnology.isEmpty())
                    joParameters.put("ExternalDeliveryTechnology", externalDeliveryTechnology);

                if (externalDeliveryURL != null && !externalDeliveryURL.isEmpty())
                    joParameters.put("ExternalDeliveryURL", externalDeliveryURL);
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
            String firstReferenceLabel, String secondReferenceLabel
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("Label", label);
            joTask.put("Type", "Media-Cross-Reference");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

            joParameters.put("Type", mediaCrossReferenceType);

            if (firstReferenceLabel != null && secondReferenceLabel != null)
            {
                JSONArray joReferences = new JSONArray();
                joParameters.put("References", joReferences);

                {
                    JSONObject joReference = new JSONObject();
                    joReferences.put(joReference);

                    joReference.put("ReferenceLabel", firstReferenceLabel);
                }

                {
                    JSONObject joReference = new JSONObject();
                    joReferences.put(joReference);

                    joReference.put("ReferenceLabel", secondReferenceLabel);
                }
            }


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

            joTask.put("Label", label);
            joTask.put("Type", "Concat-Demuxer");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

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
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                joParameters.put("ProcessingStartingFrom", dateFormat.format(utcProcessingStartingFrom));
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
            String inputType,           // Channel or StreamingUrl
            String configurationLabel,  // only if inputType is Channel
            String streamingName,       // only if inputType is StreamingUrl
            String streamingUrl,        // only if inputType is StreamingUrl
            String waitForGlobalIngestionLabel,
            Long utcProcessingStartingFrom
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("Label", label);
            joTask.put("Type", "Check-Streaming");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

            setCommonParameters(joParameters,
                    null,
                    null,
                    waitForGlobalIngestionLabel);

            joParameters.put("InputType", inputType);

            if (inputType != null && inputType.equalsIgnoreCase("Channel"))
            {
                joParameters.put("ConfigurationLabel", configurationLabel);
            }
            else
            {
                joParameters.put("StreamingName", streamingName);
                joParameters.put("StreamingUrl", streamingUrl);

            }

            if (utcProcessingStartingFrom != null)
            {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                joParameters.put("ProcessingStartingFrom", dateFormat.format(utcProcessingStartingFrom));
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
            String encodingPriority, String encodingProfileLabel,
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

            joTask.put("Label", label);
            joTask.put("Type", "Intro-Outro-Overlay");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

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
                joParameters.put("EncodingPriority", encodingPriority);

            if (encodingProfileLabel != null && !encodingProfileLabel.isEmpty())
                joParameters.put("EncodingProfileLabel", encodingProfileLabel);

            if (introOverlayDurationInSeconds != null)
                joParameters.put("IntroOverlayDurationInSeconds", introOverlayDurationInSeconds);

            if (outroOverlayDurationInSeconds != null)
                joParameters.put("OutroOverlayDurationInSeconds", outroOverlayDurationInSeconds);

            if (muteIntroOverlay != null)
                joParameters.put("MuteIntroOverlay", muteIntroOverlay);

            if (muteOutroOverlay != null)
                joParameters.put("MuteOutroOverlay", muteOutroOverlay);

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
            double startTimeInSeconds, double endTimeInSeconds, String cutType,
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

            joTask.put("Label", label);
            joTask.put("Type", "Cut");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

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
                joParameters.put("OutputFileFormat", outputFileFormat);
            joParameters.put("StartTimeInSeconds", startTimeInSeconds);
            joParameters.put("EndTimeInSeconds", endTimeInSeconds);
            joParameters.put("CutType", cutType);
            if (fixEndTimeIfOvercomeDuration != null)
                joParameters.put("FixEndTimeIfOvercomeDuration", fixEndTimeIfOvercomeDuration);


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

            Long deliveryCode,

            Long utcLiveCutStartInMilliSecs,
            Long utcLiveCutEndInMilliSecs,
            Long chunkEncodingProfileKey,
            int maxWaitingForLastChunkInSeconds,
            String mediaItemRetention, String physicalItemRetention,
            JSONObject joUserData,
            Date startPublishing, Date endPublishing
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("Label", label);
            joTask.put("Type", "Live-Cut");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

            setContentParameters(joParameters,
                    title, ingester, mediaItemRetention, physicalItemRetention,
                    tags, joUserData,
                    startPublishing, endPublishing,
                    uniqueName, allowUniqueNameOverride);

            joParameters.put("DeliveryCode", deliveryCode);

            if (chunkEncodingProfileKey != null)
                joParameters.put("ChunkEncodingProfileKey", chunkEncodingProfileKey);

            {
                JSONObject joCutPeriod = new JSONObject();
                joParameters.put("CutPeriod", joCutPeriod);

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                joCutPeriod.put("Start", dateFormat.format(utcLiveCutStartInMilliSecs));
                joCutPeriod.put("End", dateFormat.format(utcLiveCutEndInMilliSecs));
            }
            joParameters.put("MaxWaitingForLastChunkInSeconds", maxWaitingForLastChunkInSeconds);


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

            joTask.put("Label", label);
            joTask.put("Type", "Change-File-Format");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

            setCommonParameters(joParameters,
                    null,
                    mediaItemReferenceList,
                    null);

            joParameters.put("OutputFileFormat", outputFileFormat);

            if (utcProcessingStartingFrom != null)
            {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                joParameters.put("ProcessingStartingFrom", dateFormat.format(utcProcessingStartingFrom));
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
			String encodingProfileLabel, String encodingProfileSetLabel,
            String encodersPool,
            Long videoTrackIndex, Long audioTrackIndex,
            List<MediaItemReference> mediaItemReferenceList,
            Long utcProcessingStartingFrom
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("Label", label);
            joTask.put("Type", "Encode");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

            joParameters.put("Ingester", ingester);

            if (contentType != null)
                joParameters.put("ContentType", contentType);

            joParameters.put("EncodingPriority", encodingPriority);
            if (encodingProfileLabel != null)
                joParameters.put("EncodingProfileLabel", encodingProfileLabel);
			else if (encodingProfileSetLabel != null)
                joParameters.put("EncodingProfileSetLabel", encodingProfileSetLabel);

            if (encodersPool != null && !encodersPool.isEmpty())
                joParameters.put("EncodersPool", encodersPool);

            if (videoTrackIndex != null && videoTrackIndex >= 0)
                joParameters.put("VideoTrackIndex", videoTrackIndex);
            if (audioTrackIndex != null && audioTrackIndex >= 0)
                joParameters.put("AudioTrackIndex", audioTrackIndex);

            if (utcProcessingStartingFrom != null)
            {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                joParameters.put("ProcessingStartingFrom", dateFormat.format(utcProcessingStartingFrom));
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

            joTask.put("Label", label);
            joTask.put("Type", "Local-Copy");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

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

            joTask.put("Label", label);
            joTask.put("Type", "FTP-Delivery");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

            joParameters.put("ConfigurationLabel", ftpConfigurationLabel);

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
            String method, // GET, POST
            String protocol, String hostName, String uri, String parameters,
            Long timeoutInSeconds, Long maxRetries,
            List<MediaItemReference> mediaItemReferenceList
    )
            throws Exception
    {
        try
        {
            JSONObject joTask = new JSONObject();

            joTask.put("Label", label);
            joTask.put("Type", "HTTP-Callback");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

            joParameters.put("Protocol", protocol);
            joParameters.put("HostName", hostName);
            joParameters.put("URI", uri);
            if (parameters != null)
                joParameters.put("Parameters", parameters);
            joParameters.put("Method", method);
            joParameters.put("Timeout", timeoutInSeconds);
            joParameters.put("MaxRetries", maxRetries);

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

            joTask.put("Label", label);
            joTask.put("Type", "Remove-Content");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

            joParameters.put("Ingester", ingester);

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

            joTask.put("Label", label);
            joTask.put("Type", "Video-Speed");

            JSONObject joParameters = new JSONObject();
            joTask.put("Parameters", joParameters);

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
            joParameters.put("EncodingPriority", encodingPriority);

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildVideoSpeedJson failed. Exception: " + e;
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
            joParameters.put("Title", title);
            joParameters.put("Ingester", ingester);
            if (mediaItemRetention != null && !mediaItemRetention.isEmpty())
                joParameters.put("Retention", mediaItemRetention);
            if (physicalItemRetention != null && !physicalItemRetention.isEmpty())
                joParameters.put("PhysicalItemRetention", physicalItemRetention);

            if (tags != null && tags.size() > 0)
            {
                JSONArray jaTags = new JSONArray();
                joParameters.put("Tags", jaTags);

                for(String tag: tags)
                {
                    if (!tag.trim().isEmpty())
                        jaTags.put(tag.trim());
                }
            }

            if (joUserData != null)
                joParameters.put("UserData", joUserData);

            if (startPublishing!= null && endPublishing != null)
            {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                joParameters.put("StartPublishing", dateFormat.format(startPublishing));
                joParameters.put("EndPublishing", dateFormat.format(endPublishing));
            }

            if (uniqueName != null)
                joParameters.put("UniqueName", uniqueName);

            if (allowUniqueNameOverride != null)
                joParameters.put("AllowUniqueNameOverride", allowUniqueNameOverride);
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
                joParameters.put("WaitFor", jaWaitForArray);

                JSONObject joWaitForLabel = new JSONObject();
                joWaitForLabel.put("GlobalIngestionLabel", waitForGlobalIngestionLabel);

                jaWaitForArray.put(joWaitForLabel);
            }

            if (dependenciesToBeAddedToReferencesAt != null
                    && !dependenciesToBeAddedToReferencesAt.isEmpty())
                joParameters.put("DependenciesToBeAddedToReferencesAt", dependenciesToBeAddedToReferencesAt);

            if (mediaItemReferenceList != null && mediaItemReferenceList.size() > 0)
            {
                JSONArray jsonReferencesArray = new JSONArray();
                joParameters.put("References", jsonReferencesArray);

                for(MediaItemReference mediaItemReference: mediaItemReferenceList)
                {
                    JSONObject joReference = new JSONObject();
                    jsonReferencesArray.put(joReference);

                    if (mediaItemReference.getMediaItemKey() != null)
                    {
                        joReference.put("ReferenceMediaItemKey", mediaItemReference.getMediaItemKey());
                        if (mediaItemReference.getEncodingProfileKey() != null)
                            joReference.put("ReferenceEncodingProfileKey", mediaItemReference.getEncodingProfileKey());
                        else if (mediaItemReference.getEncodingProfileLabel() != null)
                            joReference.put("ReferenceEncodingProfileLabel", mediaItemReference.getEncodingProfileLabel());

                        Boolean stopIfReferenceProcessingError = false;
                        if (mediaItemReference.getStopIfReferenceProcessingError() != null)
                            stopIfReferenceProcessingError = mediaItemReference.getStopIfReferenceProcessingError();
                        joReference.put("StopIfReferenceProcessingError", stopIfReferenceProcessingError);
                    }
                    else if (mediaItemReference.getUniqueName() != null)
                    {
                        joReference.put("ReferenceUniqueName", mediaItemReference.getUniqueName());
                        if (mediaItemReference.getEncodingProfileKey() != null)
                            joReference.put("ReferenceEncodingProfileKey", mediaItemReference.getEncodingProfileKey());
                        else if (mediaItemReference.getEncodingProfileLabel() != null)
                            joReference.put("ReferenceEncodingProfileLabel", mediaItemReference.getEncodingProfileLabel());

                        Boolean stopIfReferenceProcessingError = false;
                        if (mediaItemReference.getStopIfReferenceProcessingError() != null)
                            stopIfReferenceProcessingError = mediaItemReference.getStopIfReferenceProcessingError();
                        joReference.put("StopIfReferenceProcessingError", stopIfReferenceProcessingError);
                    }
                    else if (mediaItemReference.getPhysicalPathKey() != null)
                    {
                        joReference.put("ReferencePhysicalPathKey", mediaItemReference.getPhysicalPathKey());
                    }
                    else if (mediaItemReference.getIngestionLabel() != null)
                    {
                        joReference.put("ReferenceLabel", mediaItemReference.getIngestionLabel());
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
