package com.catrammslib;

import com.catrammslib.entity.WorkflowVariable;
import com.catrammslib.utility.LiveProxyOutput;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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
            List<String> referencesOutput)
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

            String channelType,     // IP, Satellite or IP_MMSAsServer.
            String ipLiveConfigurationLabel,    // mandatory if channelType is IP
            String satLiveConfigurationLabel,   // mandatory if channelType is Satellite

            Long actAsServerPort, String actAsServerBindIP, String actAsServerURI, Long actAsServerListenTimeout,
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
            String userAgent
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

            joParameters.put("ChannelType", channelType);
            if (channelType.equals("IP_MMSAsClient"))
                joParameters.put("ConfigurationLabel", ipLiveConfigurationLabel);
            else if (channelType.equals("Satellite"))
                joParameters.put("ConfigurationLabel", satLiveConfigurationLabel);
            else if (channelType.equals("IP_MMSAsServer"))
            {
                joParameters.put("ActAsServerProtocol", "rtmp");
                joParameters.put("ActAsServerBindIP", actAsServerBindIP);
                joParameters.put("ActAsServerPort", actAsServerPort);
                joParameters.put("ActAsServerURI", actAsServerURI);
                joParameters.put("ActAsServerListenTimeout", actAsServerListenTimeout);
            }

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

            String channelType,     // IP_MMSAsClient, Satellite or IP_MMSAsServer.
            String ipLiveConfigurationLabel,    // mandatory if channelType is IP_MMSAsClient
            String satLiveConfigurationLabel,   // mandatory if channelType is Satellite

            Long actAsServerPort, String actAsServerBindIP, String actAsServerURI, Long actAsServerListenTimeout,
            String encodersPool,
            Date proxyStartTime, Date proxyEndTime,
            String userAgent,
            Long maxWidth,
            String otherInputOptions,
            Long maxAttemptsNumberInCaseOfErrors,
            Long waitingSecondsBetweenAttemptsInCaseOfErrors,
            List<LiveProxyOutput> liveProxyOutputList
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

            joParameters.put("ChannelType", channelType);
            if (channelType.equals("IP_MMSAsClient"))
                joParameters.put("ConfigurationLabel", ipLiveConfigurationLabel);
            else if (channelType.equals("Satellite"))
                joParameters.put("ConfigurationLabel", satLiveConfigurationLabel);
            else if (channelType.equals("IP_MMSAsServer"))
            {
                joParameters.put("ActAsServerProtocol", "rtmp");
                joParameters.put("ActAsServerBindIP", actAsServerBindIP);
                joParameters.put("ActAsServerPort", actAsServerPort);
                joParameters.put("ActAsServerURI", actAsServerURI);
                joParameters.put("ActAsServerListenTimeout", actAsServerListenTimeout);
            }

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

    static public JSONObject buildFaceRecognitionJson(
            String label, String title, List<String> tags, String ingester,
            String retention, JSONObject joUserData,
            String startPublishing, String endPublishing,
            List<String> referenceLabelList,
            List<Long> referenceMediaItemKeyList,
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
                    referenceLabelList, referenceMediaItemKeyList,
                    null,
                    null, null,
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
            Float frameCaptureSeconds
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
                        referencesOutput);
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
                    null, null, faceRecognition_InitialFramesNumberToBeSkipped);
            jaImageGroupOfTasks.put(joFrameContainingFace);

            {
                JSONObject joFrameContainingFaceOnSuccess = CatraMMSWorkflow.buildEventJson(joFrameContainingFace, "OnSuccess");

                joFrameContainingFaceOnSuccess.put("Task", CatraMMSWorkflow.buildEncodeJson(
                        "Encode image (FrameContainingFace): " + title,
                        ingester, "image",
                        "High",
                        "MMS_JPG_240",
                        null,
                        null,
                        null
                ));
            }

            JSONObject joFrame;
            {
                JSONObject joFrameContainingFaceOnError = CatraMMSWorkflow.buildEventJson(joFrameContainingFace, "OnError");

                joFrame = CatraMMSWorkflow.buildFrameJson(
                        frameImageReferenceLabel, title,
                        imageTags, ingester, frameCaptureSeconds, imageRetention,
                        null, null, null,
                        null, null, null
                );
                joFrameContainingFaceOnError.put("Task", joFrame);
            }

            {
                JSONObject joFrameOnSuccess = CatraMMSWorkflow.buildEventJson(joFrame, "OnSuccess");

                joFrameOnSuccess.put("Task", CatraMMSWorkflow.buildEncodeJson(
                        "Encode image (Frame): " + title,
                        ingester, "image",
                        "High",
                        "MMS_JPG_240",
                        null,
                        null,
                        null
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
            String label, String configurationLabel
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
            String startPublishing, String endPublishing,
            List<String> referenceLabelList,
            List<Long> referenceMediaItemKeyList,
            List<Long> referencePhysicalPathKeyList
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
                    referenceLabelList, referenceMediaItemKeyList, referencePhysicalPathKeyList,
                    null, null,
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
            String startPublishing, String endPublishing,
            List<String> referenceLabelList,
            List<Long> referenceMediaItemKeyList,
            List<Long> referencePhysicalPathKeyList
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
                    referenceLabelList, referenceMediaItemKeyList, referencePhysicalPathKeyList,
                    null, null,
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
            String sourceURL, String pushBinaryFileName,
            String variantOfReferencedLabel,
            String externalDeliveryTechnology, String externalDeliveryURL,
            String uniqueName, Boolean allowUniqueNameOverride, String retention, JSONObject joUserData,
            String startPublishing, String endPublishing
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
                        title, ingester, retention, null,
                        tags, joUserData,
                        startPublishing, endPublishing,
                        null, null);

                setCommonParameters(joParameters,
                        null,
                        null, null, null,
                        null, null,
                        null);

                joParameters.put("FileFormat", fileFormat);

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
            String errorMessage = "buildAddContentJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildConcatDemuxJson(
            String label, String title, List<String> tags, String ingester,
            String mediaItemRetention, String physicalItemRetention,
            JSONObject joUserData,
            String startPublishing, String endPublishing,
            Float maxDurationInSeconds, Float extraSecondsToCutWhenMaxDurationIsReached,
            String dependenciesToBeAddedToReferences,   // atTheBeginning, atTheEnd
            String uniqueName,
            Boolean allowUniqueNameOverride,
            List<String> referenceLabelList,
            List<Long> referenceMediaItemKeyList,
            List<String> referenceUniqueNameList, List<Boolean> errorIfContentNotFoundListForReferenceUniqueName,
            String waitForGlobalIngestionLabel
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
                    dependenciesToBeAddedToReferences,
                    referenceLabelList, referenceMediaItemKeyList, null,
                    referenceUniqueNameList, errorIfContentNotFoundListForReferenceUniqueName,
                    waitForGlobalIngestionLabel);

            if (maxDurationInSeconds != null)
                joParameters.put("MaxDurationInSeconds", maxDurationInSeconds);

            if (extraSecondsToCutWhenMaxDurationIsReached != null)
                joParameters.put("ExtraSecondsToCutWhenMaxDurationIsReached", extraSecondsToCutWhenMaxDurationIsReached);

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildAddContentJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    static public JSONObject buildCutJson(
            String label, String title, List<String> tags, String ingester,
            String outputFileFormat,
            double startTimeInSeconds, double endTimeInSeconds, boolean keyFrameSeeking,
            Boolean fixEndTimeIfOvercomeDuration,
            String retention, JSONObject joUserData,
            String startPublishing, String endPublishing,
            List<String> referenceLabelList,
            List<Long> referenceMediaItemKeyList,
            List<Long> referencePhysicalPathKeyList
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
                    referenceLabelList, referenceMediaItemKeyList, referencePhysicalPathKeyList,
                    null, null,
                    null);

            if (outputFileFormat != null)
                joParameters.put("OutputFileFormat", outputFileFormat);
            joParameters.put("StartTimeInSeconds", startTimeInSeconds);
            joParameters.put("EndTimeInSeconds", endTimeInSeconds);
            joParameters.put("KeyFrameSeeking", keyFrameSeeking);
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
            int maxWaitingForLastChunkInSeconds,
            String mediaItemRetention, String physicalItemRetention,
            JSONObject joUserData,
            String startPublishing, String endPublishing
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
            List<String> referenceLabelList,
            List<Long> referenceMediaItemKeyList,
            List<Long> referencePhysicalPathKeyList
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
                    referenceLabelList, referenceMediaItemKeyList, referencePhysicalPathKeyList,
                    null, null,
                    null);

            joParameters.put("OutputFileFormat", outputFileFormat);

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
            String contentType, // video, audio, image
            String encodingPriority, String encodingProfileLabel,
            String encodersPool,
            Date processingStartingFrom,
            Long referencePhysicalPathKey
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

            if (contentType != null) // it has to used only in case the workflow is sent to the workflow editor page
                joParameters.put("ContentType", contentType);

            joParameters.put("EncodingPriority", encodingPriority);
            if (encodingProfileLabel != null)
                joParameters.put("EncodingProfileLabel", encodingProfileLabel);

            if (encodersPool != null && !encodersPool.isEmpty())
                joParameters.put("EncodersPool", encodersPool);

            if (processingStartingFrom != null)
            {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                joParameters.put("ProcessingStartingFrom", dateFormat.format(processingStartingFrom));
            }

            if (referencePhysicalPathKey != null)
            {
                JSONArray jaReferences = new JSONArray();
                joParameters.put("References", jaReferences);

                JSONObject joReference = new JSONObject();
                jaReferences.put(joReference);

                joReference.put("ReferencePhysicalPathKey", referencePhysicalPathKey);
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

    static public JSONObject buildLocalCopy(
            String label, String destinationLocalPath, String destinationLocalFileName,
            List<Long> referenceMediaItemKeyList, List<Long> referencePhysicalPathKeyList
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

            if (referenceMediaItemKeyList != null || referencePhysicalPathKeyList != null)
            {
                JSONArray jsonReferencesArray = new JSONArray();
                joParameters.put("References", jsonReferencesArray);

                if (referenceMediaItemKeyList != null)
                {
                    for(Long referenceMediaItemKey: referenceMediaItemKeyList)
                    {
                        JSONObject joReference = new JSONObject();
                        jsonReferencesArray.put(joReference);

                        joReference.put("ReferenceMediaItemKey", referenceMediaItemKey);
                    }
                }

                if (referencePhysicalPathKeyList != null)
                {
                    for(Long referencePhysicalPathKey: referencePhysicalPathKeyList)
                    {
                        JSONObject joReference = new JSONObject();
                        jsonReferencesArray.put(joReference);

                        joReference.put("ReferencePhysicalPathKey", referencePhysicalPathKey);
                    }
                }
            }

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
            List<Long> referenceMediaItemKeyList, List<Long> referencePhysicalPathKeyList
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

            if (referenceMediaItemKeyList != null || referencePhysicalPathKeyList != null)
            {
                JSONArray jsonReferencesArray = new JSONArray();
                joParameters.put("References", jsonReferencesArray);

                if (referenceMediaItemKeyList != null)
                {
                    for(Long referenceMediaItemKey: referenceMediaItemKeyList)
                    {
                        JSONObject joReference = new JSONObject();
                        jsonReferencesArray.put(joReference);

                        joReference.put("ReferenceMediaItemKey", referenceMediaItemKey);
                    }
                }

                if (referencePhysicalPathKeyList != null)
                {
                    for(Long referencePhysicalPathKey: referencePhysicalPathKeyList)
                    {
                        JSONObject joReference = new JSONObject();
                        jsonReferencesArray.put(joReference);

                        joReference.put("ReferencePhysicalPathKey", referencePhysicalPathKey);
                    }
                }
            }

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
            List<Long> referenceMediaItemKeyList,
            List<Long> referencePhysicalPathKeyList
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

            if (referenceMediaItemKeyList != null && referenceMediaItemKeyList.size() > 0)
            {
                JSONArray jsonReferencesArray = new JSONArray();
                joParameters.put("References", jsonReferencesArray);

                for(Long referenceMediaItemKey: referenceMediaItemKeyList)
                {
                    JSONObject joReference = new JSONObject();
                    jsonReferencesArray.put(joReference);

                    joReference.put("ReferenceMediaItemKey", referenceMediaItemKey);
                }
            }
            else if (referencePhysicalPathKeyList != null && referencePhysicalPathKeyList.size() > 0)
            {
                JSONArray jsonReferencesArray = new JSONArray();
                joParameters.put("References", jsonReferencesArray);

                for(Long referencePhysicalPathKey: referencePhysicalPathKeyList)
                {
                    JSONObject joReference = new JSONObject();
                    jsonReferencesArray.put(joReference);

                    joReference.put("ReferencePhysicalPathKey", referencePhysicalPathKey);
                }
            }
            else
            {
                String errorMessage = "Wrong input";
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

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
            String startPublishing, String endPublishing,
            List<String> referenceLabelList,
            List<Long> referenceMediaItemKeyList
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
                    referenceLabelList, referenceMediaItemKeyList, null,
                    null, null,
                    null);

            joParameters.put("VideoSpeedType", videoSpeedType);
            joParameters.put("VideoSpeedSize", videoSpeedSize);
            joParameters.put("EncodingPriority", encodingPriority);

            return joTask;
        }
        catch (Exception e)
        {
            String errorMessage = "buildAddContentJson failed. Exception: " + e;
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
            String startPublishing, String endPublishing,
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
                joParameters.put("StartPublishing", startPublishing);
                joParameters.put("EndPublishing", endPublishing);
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

            String dependenciesToBeAddedToReferences,
            List<String> referenceLabelList,
            List<Long> referenceMediaItemKeyList,
            List<Long> referencePhysicalPathKeyList,
            List<String> referenceUniqueNameList, List<Boolean> errorIfContentNotFoundListForReferenceUniqueName,
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

            if (dependenciesToBeAddedToReferences != null)
                joParameters.put("DependenciesToBeAddedToReferences", dependenciesToBeAddedToReferences);

            if (referenceLabelList != null && referenceLabelList.size() > 0)
            {
                JSONArray jsonReferencesArray = new JSONArray();
                joParameters.put("References", jsonReferencesArray);

                for(String referenceLabel: referenceLabelList)
                {
                    JSONObject joReference = new JSONObject();
                    jsonReferencesArray.put(joReference);

                    joReference.put("ReferenceLabel", referenceLabel);
                }
            }

            if (referenceMediaItemKeyList != null && referenceMediaItemKeyList.size() > 0)
            {
                JSONArray jsonReferencesArray = new JSONArray();
                joParameters.put("References", jsonReferencesArray);

                for(Long referenceMediaItemKey: referenceMediaItemKeyList)
                {
                    JSONObject joReference = new JSONObject();
                    jsonReferencesArray.put(joReference);

                    joReference.put("ReferenceMediaItemKey", referenceMediaItemKey);
                }
            }

            if (referencePhysicalPathKeyList != null && referencePhysicalPathKeyList.size() > 0)
            {
                JSONArray jsonReferencesArray = new JSONArray();
                joParameters.put("References", jsonReferencesArray);

                for(Long referencePhysicalPathKey: referencePhysicalPathKeyList)
                {
                    JSONObject joReference = new JSONObject();
                    jsonReferencesArray.put(joReference);

                    joReference.put("ReferencePhysicalPathKey", referencePhysicalPathKey);
                }
            }

            if (referenceUniqueNameList != null && referenceUniqueNameList.size() > 0)
            {
                JSONArray jsonReferencesArray = new JSONArray();
                joParameters.put("References", jsonReferencesArray);

                for(int index = 0; index < referenceUniqueNameList.size(); index++)
                {
                    String referenceUniqueName = referenceUniqueNameList.get(index);
                    Boolean errorIfContentNotFound = true;
                    if (errorIfContentNotFoundListForReferenceUniqueName != null)
                        errorIfContentNotFound = errorIfContentNotFoundListForReferenceUniqueName.get(index);

                    JSONObject joReference = new JSONObject();
                    jsonReferencesArray.put(joReference);

                    joReference.put("ReferenceUniqueName", referenceUniqueName);
                    joReference.put("ErrorIfContentNotFound", errorIfContentNotFound);
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
