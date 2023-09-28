package com.catrammslib.helper;

import com.catrammslib.CatraMMSAPI;
import com.catrammslib.CatraMMSWorkflow;
import com.catrammslib.entity.CDN77ChannelConf;
import com.catrammslib.entity.IngestionJob;
import com.catrammslib.entity.Stream;
import com.catrammslib.entity.WorkflowVariable;
import com.catrammslib.helper.entity.LiveStreamInfo;
import com.catrammslib.utility.IngestionResult;
import com.catrammslib.utility.OutputStream;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class LiveStreams {

    private static final Logger mLogger = Logger.getLogger(LiveStreams.class);

    public static String statusALL = "All";
    public static String statusOK = "Working";      // finished OK
    public static String statusKO = "Not Working";  // finished NOT OK
    public static String statusRunning = "Running"; // Running

    // 2021-02-01: Il chiamante capisce se il canale è attivo o meno in base alla presenza del canale in liveStreamInfoList.
    //  E' IMPORTANTE quindi che, se in questa procedura abbiamo una eccezione su un canale, tutta la procedura ritorni
    //  una eccezione. Questo perchè altrimenti, il chiamante pensa che quel canale non è attivo e lo fa ripartire
    // invece il canale era attivo ma abbiamo avuto una eccezione.
    // questo è il caso quando il componente API viene restartato, il canale è attivo ma semplicemente API non sta rispondendo
    static public void fillLastLiveStreamsInfo(
            CatraMMSAPI catraMMS,
            String userName,
            String password,
            List<LiveStreamInfo> liveStreamInfoList,
            String statusFilter,
            boolean gridInfo,
            boolean proxyInfo,
            boolean recorderInfo
    )
            throws Exception
    {
        try
        {
            mLogger.info("fillLastLiveStreamsInfo"
                    + ", liveStreamInfoList.size: " + liveStreamInfoList.size()
                    + ", userName: " + userName
                    + ", password: " + password
                    + ", statusFilter: " + statusFilter
                    + ", gridInfo: " + gridInfo
                    + ", proxyInfo: " + proxyInfo
                    + ", recorderInfo: " + recorderInfo
            );

            {
                Date liveStreamInfoStartingPoint = new Date();

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                // Status
                {
                    List<LiveStreamInfo> liveStreamInfoToBeFiltered = new ArrayList<>();

                    long channelIndex = 0;
                    for (LiveStreamInfo liveStreamInfo: liveStreamInfoList)
                    {
                        Date channelStartingPoint = new Date();

                        mLogger.info("Looking Channel"
                                + ", label: " + liveStreamInfo.getStream().getLabel()
                                + ", ConfKey: " + liveStreamInfo.getStream().getConfKey()
                        );

                        liveStreamInfo.setLiveGridChannelStatus(LiveStreamInfo.LiveGridWorkflowNotRunning);
                        liveStreamInfo.setLiveProxyChannelStatus(LiveStreamInfo.LiveProxyWorkflowNotRunning);
                        liveStreamInfo.setLiveRecorderChannelStatus(LiveStreamInfo.LiveRecorderWorkflowNotRunning);

                        int aaa = 0;
                        mLogger.info(aaa++);
                        // liveGrid
                        IngestionJob liveGridIngestionJob = null;
                        if(gridInfo)
                        {
                            {
                                List<IngestionJob> liveGridIngestionJobList = new ArrayList<>();
                                {
                                    Date startingPoint = new Date();

                                    // String jsonParametersCondition = "JSON_EXTRACT(ij.metaDataContent, '$.OutputChannelLabel') = '"
                                    //        + liveStreamInfo.getStream().getLabel().replace("'", "''") + "'";
                                    boolean ascending = false;

                                    Date start = null;
                                    Date end = null;
                                    String localIngestionType = "Live-Grid";

                                    int localStartIndex = 0;
                                    int localPageSize = 50;

                                    List<IngestionJob> ingestionJobList = new ArrayList<>();
                                    // do
                                    {
                                        ingestionJobList.clear();

                                        // 2021-02-01: we will NOT consider just the last one, may be from the last one it seems down,
                                        // the system will start a new one and we will have the mess....
                                        // So we will look at the last not completed, it means status "notCompleted" and, if it is missing,
                                        // we will take the last "completed" to get his status
                                        Long ingestionJobsNumber = catraMMS.getIngestionJobs(
                                                userName, password,
                                                // ((Integer.parseInt(currentPage) - 1) * pageSize), pageSize,
                                                localStartIndex, localPageSize,
                                                null, null, null, start, end, null,
                                                null, localIngestionType,
                                                null, liveStreamInfo.getStream().getLabel(), null, null, // jsonParametersCondition,
                                                ascending,
                                                false, false,
                                                // 2022-12-18: normal list
                                                false,
                                                ingestionJobList);
                                        for (IngestionJob ingestionJob: ingestionJobList)
                                        {
                                            if (!ingestionJob.getStatus().startsWith("End_"))
                                            {
                                                ingestionJobList.clear();
                                                ingestionJobList.add(ingestionJob);

                                                break;
                                            }
                                        }

                                        liveGridIngestionJobList.addAll(ingestionJobList);
                                        localStartIndex += localPageSize;
                                    }
                                    // while(ingestionJobList.size() > 0);

                                    mLogger.info("liveStreamsFillList statistics (liveGrid)"
                                            + ", localIngestionType: " + localIngestionType
                                            + ", liveGridIngestionJobList.size: " + liveGridIngestionJobList.size()
                                            + ", elapsed (secs): " + ((new Date().getTime() - startingPoint.getTime()) / 1000)
                                    );
                                }

                                if (liveGridIngestionJobList.size() > 0) {
                                    liveGridIngestionJob = liveGridIngestionJobList.get(0);

                                    mLogger.info("Found LiveGrid"
                                            + ", label: " + liveStreamInfo.getStream().getLabel()
                                            + ", ConfKey: " + liveStreamInfo.getStream().getConfKey()
                                            + ", IngestionJobKey: " + liveGridIngestionJob.getIngestionJobKey()
                                            + ", Ingestion Job Label: " + liveGridIngestionJob.getLabel()
                                            + ", ingestionStatus: " + liveGridIngestionJob.getStatus()
                                            + ", Start Processing: " + (liveGridIngestionJob.getStartProcessing() != null
                                            ? simpleDateFormat.format(liveGridIngestionJob.getStartProcessing()) : "null")
                                    );
                                } else {
                                    mLogger.info("NOT Found LiveGrid Not Completed"
                                            + ", label: " + liveStreamInfo.getStream().getLabel()
                                            + ", ConfKey: " + liveStreamInfo.getStream().getConfKey()
                                    );
                                }
                            }

                            {
                                liveStreamInfo.setLiveGridIngestionJob(liveGridIngestionJob);

                                boolean toBeFiltered = false;

                                if (liveGridIngestionJob != null)
                                {
                                    setLiveGridChannelStatus(liveGridIngestionJob, liveStreamInfo);

                                    liveStreamInfo.setLiveGridErrorInfo(
                                            (liveGridIngestionJob.getStartProcessing() != null ? simpleDateFormat.format(liveGridIngestionJob.getStartProcessing()) : "")
                                                    + (liveGridIngestionJob.getErrorMessage() == null ? "" : (" " + liveGridIngestionJob.getErrorMessage()))
                                    );

                                    if (liveStreamInfo.getLiveGridChannelStatus().equalsIgnoreCase(LiveStreamInfo.liveGridStreamingNotWorking))
                                    {
                                        if (statusFilter == null ||
                                                statusFilter.equalsIgnoreCase(statusALL)
                                                || statusFilter.equalsIgnoreCase(statusKO))
                                            ;
                                        else
                                            toBeFiltered = true;
                                    }
                                    else if (liveStreamInfo.getLiveGridChannelStatus().equalsIgnoreCase(LiveStreamInfo.liveGridStreamingStoppedyMMS))
                                    {
                                        if (statusFilter == null ||
                                                statusFilter.equalsIgnoreCase(statusALL)
                                                || statusFilter.equalsIgnoreCase(statusKO))
                                            ;
                                        else
                                            toBeFiltered = true;
                                    }
                                    else if (liveStreamInfo.getLiveGridChannelStatus().equalsIgnoreCase(LiveStreamInfo.liveGridStreamingStoppedyUser))
                                    {
                                        if (statusFilter == null ||
                                                statusFilter.equalsIgnoreCase(statusALL)
                                                || statusFilter.equalsIgnoreCase(statusOK))
                                            ;
                                        else
                                            toBeFiltered = true;
                                    }
                                    else if (liveStreamInfo.getLiveGridChannelStatus().equalsIgnoreCase(LiveStreamInfo.liveGridStreamingWithoutTranscoderYet))
                                    {
                                        if (statusFilter == null
                                                || statusFilter.equalsIgnoreCase(statusALL)
                                                || statusFilter.equalsIgnoreCase(statusOK))
                                            ;
                                        else
                                            toBeFiltered = true;
                                    }
                                    else if (liveStreamInfo.getLiveGridChannelStatus().equalsIgnoreCase(LiveStreamInfo.liveGridStreamingRunning))
                                    {
                                        if (statusFilter == null
                                                || statusFilter.equalsIgnoreCase(statusALL)
                                                || statusFilter.equalsIgnoreCase(statusRunning))
                                            ;
                                        else
                                            toBeFiltered = true;
                                    }
                                    else if (liveStreamInfo.getLiveGridChannelStatus().equalsIgnoreCase(LiveStreamInfo.liveGridStreamingWithoutEncodingJob))
                                    {
                                        if (statusFilter == null
                                                || statusFilter.equalsIgnoreCase(statusALL)
                                                || statusFilter.equalsIgnoreCase(statusKO))
                                            ;
                                        else
                                            toBeFiltered = true;
                                    }
                                }
                                else
                                {
                                    mLogger.info("Not Found IngestionJob LiveGrid"
                                            + ", label: " + liveStreamInfo.getStream().getLabel()
                                            + ", ConfKey: " + liveStreamInfo.getStream().getConfKey()
                                    );

                                    liveStreamInfo.setLiveGridChannelStatus(LiveStreamInfo.LiveGridWorkflowNotRunning);
                                    liveStreamInfo.setLiveGridErrorInfo(null);

                                    if (statusFilter == null
                                            || statusFilter.equalsIgnoreCase(statusALL))
                                        ;
                                    else
                                        toBeFiltered = true;
                                }

                                mLogger.info("LiveGrid: statusFilter check"
                                        + ", label: " + liveStreamInfo.getStream().getLabel()
                                        + ", ConfKey: " + liveStreamInfo.getStream().getConfKey()
                                        + ", ingestionJobKey: " + (liveGridIngestionJob == null ? "null" : liveGridIngestionJob.getIngestionJobKey())
                                        + ", liveGridChannelStatus: " + liveStreamInfo.getLiveGridChannelStatus()
                                        + ", statusFilter: " + statusFilter
                                        + ", toBeFiltered: " + toBeFiltered
                                );

                                if (toBeFiltered)
                                    liveStreamInfoToBeFiltered.add(liveStreamInfo);
                            }
                        }

                        mLogger.info(aaa++);
                        // LiveProxy
                        IngestionJob liveProxyIngestionJob = null;
                        if (proxyInfo)
                        {
                            mLogger.info(aaa++);
                            {
                                List<IngestionJob> liveProxyIngestionJobList = new ArrayList<>();
                                {
                                    Date startingPoint = new Date();

                                    // 2021-07-26: In case of IP_PULL (or TV), it is possible to understand if
                                    //  a Live-Proxy ingestion Job is running for a specified channel.
                                    //  That because we can compare the following information:
                                    //  - channel side we have the configuration label
                                    //  - ingestion job side we have the parameters (metadataContent) containing the configurationLabel field
                                    // In case of CaptureLive or IP_MMSAsServer does not have sense that
                                    // because we do not have the channel, I mean the Live-Proxy Ingestion Job just get the stream
                                    // from Capture or from a socket, so we do not have the channel anymore.

                                    // In case of CiborTV, we have a scenario where we have defined an IP channel and we get it from CaptureLive
                                    // In this case, to support the auto-restart of the channel in case of failure, we need to find out the
                                    // associated IngestionJob using the following assumption:
                                    //      - the IngestionJob label has to have the following format: <channel name> Proxy (<channel label>)
                                    boolean ascending = false;
                                    Date start = null;
                                    Date end = null;
                                    String localIngestionType = "Live-Proxy";
                                    int localStartIndex = 0;
                                    int localPageSize = 50;
                                    // int localPageSize = 1;

                                    List<IngestionJob> ingestionJobList = new ArrayList<>();

                                    // String jsonParametersCondition = "JSON_EXTRACT(ij.metaDataContent, '$.ConfigurationLabel') = '"
                                    //                + liveStreamInfo.getStream().getLabel().replace("'", "''") + "'";

                                    // 2021-02-01: we will NOT consider just the last one, may be from the last one it seems down,
                                    // the system will start a new one and we will have the mess....
                                    // So we will look at the last not completed, it means status "notCompleted" and, if it is missing,
                                    // we will take the last "completed" to get his status
                                    Long ingestionJobsNumber = catraMMS.getIngestionJobs(
                                            userName, password,
                                            // ((Integer.parseInt(currentPage) - 1) * pageSize), pageSize,
                                            localStartIndex, localPageSize,
                                            null, null, null, start, end, null,
                                            null, localIngestionType,
                                            liveStreamInfo.getStream().getLabel(), null, null, null, // jsonParametersCondition,
                                            ascending,
                                            true, false,
                                            // 2022-12-18: this is important because it could generare a new Workflow in case status is 'not running'
                                            true,
                                            ingestionJobList);
                                    mLogger.info("liveStreamsFillList statistics (liveProxy)"
                                            + ", localIngestionType: " + localIngestionType
                                            + ", ingestionJobList.size: " + ingestionJobList.size()
                                            + ", elapsed (secs): " + ((new Date().getTime() - startingPoint.getTime()) / 1000)
                                    );
                                    IngestionJob notEndedIngestionJob = null;
                                    for (IngestionJob ingestionJob: ingestionJobList)
                                    {
                                        mLogger.info("test"
                                                + ", ingestionJob.getStatus(): " + ingestionJob.getStatus()
                                        );
                                        if (!ingestionJob.getStatus().startsWith("End_"))
                                        {
                                            notEndedIngestionJob = ingestionJob;
                                            // ingestionJobList.clear();
                                            // ingestionJobList.add(ingestionJob);

                                            break;
                                        }
                                    }

                                    if (notEndedIngestionJob != null)
                                        liveProxyIngestionJobList.add(notEndedIngestionJob);

                                    mLogger.info("liveStreamsFillList statistics (liveProxy)"
                                            + ", localIngestionType: " + localIngestionType
                                            + ", liveProxyIngestionJobList.size: " + liveProxyIngestionJobList.size()
                                            + ", elapsed (secs): " + ((new Date().getTime() - startingPoint.getTime()) / 1000)
                                    );
                                }

                                if (liveProxyIngestionJobList.size() > 0)
                                {
                                    liveProxyIngestionJob = liveProxyIngestionJobList.get(0);

                                    mLogger.info("Found LiveProxy"
                                            + ", label: " + liveStreamInfo.getStream().getLabel()
                                            + ", ConfKey: " + liveStreamInfo.getStream().getConfKey()
                                            + ", IngestionJobKey: " + liveProxyIngestionJob.getIngestionJobKey()
                                            + ", Ingestion Job Label: " + liveProxyIngestionJob.getLabel()
                                            + ", ingestionStatus: " + liveProxyIngestionJob.getStatus()
                                            + ", Start Processing: " + (liveProxyIngestionJob.getStartProcessing() != null
                                            ? simpleDateFormat.format(liveProxyIngestionJob.getStartProcessing()) : "null")
                                    );
                                }
                                else
                                {
                                    mLogger.info("NOT Found LiveProxy"
                                            + ", label: " + liveStreamInfo.getStream().getLabel()
                                            + ", ConfKey: " + liveStreamInfo.getStream().getConfKey()
                                    );
                                }
                            }

                            mLogger.info(aaa++);
                            {
                                liveStreamInfo.setLiveProxyIngestionJob(liveProxyIngestionJob);

                                boolean toBeFiltered = false;

                                if (liveProxyIngestionJob != null)
                                {
                                    setLiveProxyChannelStatus(liveProxyIngestionJob, liveStreamInfo);

                                    liveStreamInfo.setLiveProxyErrorInfo(
                                            (liveProxyIngestionJob.getStartProcessing() != null ? simpleDateFormat.format(liveProxyIngestionJob.getStartProcessing()) : "")
                                                    + (liveProxyIngestionJob.getErrorMessage() == null ? "" : (" " + liveProxyIngestionJob.getErrorMessage()))
                                    );

                                    if (liveStreamInfo.getLiveProxyChannelStatus().equalsIgnoreCase(LiveStreamInfo.liveProxyStreamingNotWorking))
                                    {
                                        if (statusFilter == null
                                                || statusFilter.equalsIgnoreCase(statusALL)
                                                || statusFilter.equalsIgnoreCase(statusKO))
                                            ;
                                        else
                                            toBeFiltered = true;
                                    }
                                    else if (liveStreamInfo.getLiveProxyChannelStatus().equalsIgnoreCase(LiveStreamInfo.liveProxyStreamingStoppedyMMS))
                                    {
                                        if (statusFilter == null
                                                || statusFilter.equalsIgnoreCase(statusALL)
                                                || statusFilter.equalsIgnoreCase(statusKO))
                                            ;
                                        else
                                            toBeFiltered = true;
                                    }
                                    else if (liveStreamInfo.getLiveProxyChannelStatus().equalsIgnoreCase(LiveStreamInfo.liveProxyStreamingStoppedyUser))
                                    {
                                        if (statusFilter == null
                                                || statusFilter.equalsIgnoreCase(statusALL)
                                                || statusFilter.equalsIgnoreCase(statusOK))
                                            ;
                                        else
                                            toBeFiltered = true;
                                    }
                                    else if (liveStreamInfo.getLiveProxyChannelStatus().equalsIgnoreCase(LiveStreamInfo.liveProxyStreamingWithoutTranscoderYet))
                                    {
                                        if (statusFilter == null
                                                || statusFilter.equalsIgnoreCase(statusALL)
                                                // lo considero running per evitare creazione di nuovi LiveProxy
                                                || statusFilter.equalsIgnoreCase(statusRunning)
                                                || statusFilter.equalsIgnoreCase(statusOK))
                                            ;
                                        else
                                            toBeFiltered = true;
                                    }
                                    else if (liveStreamInfo.getLiveProxyChannelStatus().equalsIgnoreCase(LiveStreamInfo.liveProxyStreamingRunningButSomeRequestsFailed))
                                    {
                                        if (statusFilter == null
                                                || statusFilter.equalsIgnoreCase(statusALL)
                                                || statusFilter.equalsIgnoreCase(statusRunning)
                                                || statusFilter.equalsIgnoreCase(statusOK))
                                            ;
                                        else
                                            toBeFiltered = true;
                                    }
                                    else if (liveStreamInfo.getLiveProxyChannelStatus().equalsIgnoreCase(LiveStreamInfo.liveProxyStreamingRunning))
                                    {
                                        if (statusFilter == null
                                                || statusFilter.equalsIgnoreCase(statusALL)
                                                || statusFilter.equalsIgnoreCase(statusRunning))
                                            ;
                                        else
                                            toBeFiltered = true;
                                    }
                                    else if (liveStreamInfo.getLiveProxyChannelStatus().equalsIgnoreCase(LiveStreamInfo.liveProxyStreamingWithoutEncodingJob))
                                    {
                                        if (statusFilter == null
                                                || statusFilter.equalsIgnoreCase(statusALL)
                                                || statusFilter.equalsIgnoreCase(statusKO))
                                            ;
                                        else
                                            toBeFiltered = true;
                                    }
                                }
                                else
                                {
                                    mLogger.info("Not Found IngestionJob LiveProxy"
                                            + ", label: " + liveStreamInfo.getStream().getLabel()
                                            + ", ConfKey: " + liveStreamInfo.getStream().getConfKey()
                                    );

                                    liveStreamInfo.setLiveProxyChannelStatus(LiveStreamInfo.LiveProxyWorkflowNotRunning);
                                    liveStreamInfo.setLiveProxyErrorInfo(null);

                                    if (statusFilter == null
                                            || statusFilter.equalsIgnoreCase(statusALL))
                                        ;
                                    else
                                        toBeFiltered = true;
                                }

                                mLogger.info("LiveProxy: statusFilter check"
                                        + ", label: " + liveStreamInfo.getStream().getLabel()
                                        + ", ConfKey: " + liveStreamInfo.getStream().getConfKey()
                                        + ", ingestionJobKey: " + (liveProxyIngestionJob == null ? "null" : liveProxyIngestionJob.getIngestionJobKey())
                                        + ", liveProxyChannelStatus: " + liveStreamInfo.getLiveProxyChannelStatus()
                                        + ", statusFilter: " + statusFilter
                                        + ", toBeFiltered: " + toBeFiltered
                                );

                                if (toBeFiltered)
                                    liveStreamInfoToBeFiltered.add(liveStreamInfo);
                            }
                            mLogger.info(aaa++);
                        }

                        mLogger.info(aaa++);
                        // Live-Recorder
                        IngestionJob liveRecorderIngestionJob = null;
                        if (recorderInfo)
                        {
                            mLogger.info(aaa++);
                            {
                                List<IngestionJob> liveRecorderIngestionJobList = new ArrayList<>();
                                {
                                    Date startingPoint = new Date();

                                    // 2021-07-26: In case of IP_PULL (or TV), it is possible to understand if
                                    //  a Live-Recorder ingestion Job is running for a specified channel.
                                    //  That because we can compare the following information:
                                    //  - channel side we have the configuration label
                                    //  - ingestion job side we have the parameters (metadataContent) containing the configurationLabel field
                                    // String jsonParametersCondition = "JSON_EXTRACT(ij.metaDataContent, '$.DeliveryCode') = "
                                    //        + liveStreamInfo.getStream().getConfKey();
                                    boolean ascending = false;

                                    Date start = null;
                                    Date end = null;
									/*
                                    {
                                        Calendar calendar = Calendar.getInstance();
                                        end = calendar.getTime();

                                        int startingFromNumberOfDaysBefore = 4;
                                        calendar.add(Calendar.DAY_OF_MONTH, -startingFromNumberOfDaysBefore);
                                        start = calendar.getTime();
                                    }
									*/
                                    String localIngestionType = "Live-Recorder";

                                    int localStartIndex = 0;
                                    int localPageSize = 50;

                                    List<IngestionJob> ingestionJobList = new ArrayList<>();
                                    // do
                                    {
                                        // ingestionJobList.clear();

                                        // 2021-02-01: we will NOT consider just the last one, may be from the last one it seems down,
                                        // the system will start a new one and we will have the mess....
                                        // So we will look at the last not completed, it means status "notCompleted" and, if it is missing,
                                        // we will take the last "completed" to get his status
                                        Long ingestionJobsNumber = catraMMS.getIngestionJobs(
                                                userName, password,
                                                // ((Integer.parseInt(currentPage) - 1) * pageSize), pageSize,
                                                localStartIndex, localPageSize,
                                                null, null, null, start, end, null,
                                                null, localIngestionType,
                                                liveStreamInfo.getStream().getLabel(), null, null, null, // jsonParametersCondition,
                                                ascending,
                                                true, false,
                                                // 2022-12-18: this is important because it could generare a new Workflow in case status is 'not running'
                                                true,
                                                ingestionJobList);
                                        IngestionJob notEndedIngestionJob = null;
                                        for (IngestionJob ingestionJob: ingestionJobList)
                                        {
                                            if (!ingestionJob.getStatus().startsWith("End_"))
                                            {
                                                notEndedIngestionJob = ingestionJob;
                                                // ingestionJobList.clear();
                                                // ingestionJobList.add(ingestionJob);

                                                break;
                                            }
                                        }

                                        if (notEndedIngestionJob != null)
                                            liveRecorderIngestionJobList.add(notEndedIngestionJob);

                                        mLogger.info("liveStreamsFillList statistics (liveProxy)"
                                                + ", localIngestionType: " + localIngestionType
                                                + ", liveRecorderIngestionJobList.size: " + liveRecorderIngestionJobList.size()
                                                + ", elapsed (secs): " + ((new Date().getTime() - startingPoint.getTime()) / 1000)
                                        );
                                        // liveRecorderLastIngestionJobList.addAll(ingestionJobList);
                                        // localStartIndex += localPageSize;
                                    }
                                    // while(ingestionJobList.size() > 0);

                                    mLogger.info("liveStreamsFillList statistics (Live-Recorder completed and not completed)"
                                            + ", localIngestionType: " + localIngestionType
                                            + ", liveRecorderIngestionJobList.size: " + liveRecorderIngestionJobList.size()
                                            + ", elapsed (secs): " + ((new Date().getTime() - startingPoint.getTime()) / 1000)
                                    );
                                }

                                if (liveRecorderIngestionJobList.size() > 0)
                                {
                                    liveRecorderIngestionJob = liveRecorderIngestionJobList.get(0);

                                    mLogger.info("Found LiveRecorder"
                                            + ", label: " + liveStreamInfo.getStream().getLabel()
                                            + ", ConfKey: " + liveStreamInfo.getStream().getConfKey()
                                            + ", IngestionJobKey: " + liveRecorderIngestionJob.getIngestionJobKey()
                                            + ", Ingestion Job Label: " + liveRecorderIngestionJob.getLabel()
                                            + ", ingestionStatus: " + liveRecorderIngestionJob.getStatus()
                                            + ", Start Processing: " + (liveRecorderIngestionJob.getStartProcessing() != null
                                            ? simpleDateFormat.format(liveRecorderIngestionJob.getStartProcessing()) : "null")
                                    );
                                }
                                else
                                {
                                    mLogger.info("NOT Found LiveRecorder"
                                            + ", label: " + liveStreamInfo.getStream().getLabel()
                                            + ", ConfKey: " + liveStreamInfo.getStream().getConfKey()
                                    );
                                }
                            }

                            mLogger.info(aaa++);
                            {
                                liveStreamInfo.setLiveRecorderIngestionJob(liveRecorderIngestionJob);

                                boolean toBeFiltered = false;

                                if (liveRecorderIngestionJob != null)
                                {
                                    setLiveRecorderChannelStatus(liveRecorderIngestionJob, liveStreamInfo);

                                    liveStreamInfo.setLiveRecorderErrorInfo(
                                            (liveRecorderIngestionJob.getStartProcessing() != null ? simpleDateFormat.format(liveRecorderIngestionJob.getStartProcessing()) : "")
                                                    + (liveRecorderIngestionJob.getErrorMessage() == null ? "" : (" " + liveRecorderIngestionJob.getErrorMessage()))
                                    );

                                    if (liveStreamInfo.getLiveRecorderChannelStatus().startsWith(LiveStreamInfo.liveRecorderFinishedOK))
                                    {
                                        if (statusFilter == null
                                                || statusFilter.equalsIgnoreCase(statusALL)
                                                || statusFilter.equalsIgnoreCase(statusOK))
                                            ;
                                        else
                                            toBeFiltered = true;
                                    }
                                    else if (liveStreamInfo.getLiveRecorderChannelStatus().equalsIgnoreCase(LiveStreamInfo.liveRecorderStreamingWithoutTranscoderYet))
                                    {
                                        if (statusFilter == null
                                                || statusFilter.equalsIgnoreCase(statusALL)
                                                || statusFilter.equalsIgnoreCase(statusRunning))    // liveRecorderStreamingWithoutTranscoderYet is as it is running
                                            ;
                                        else
                                            toBeFiltered = true;
                                    }
                                    else if (liveStreamInfo.getLiveRecorderChannelStatus().equalsIgnoreCase(LiveStreamInfo.liveRecorderRunning))
                                    {
                                        if (statusFilter == null
                                                || statusFilter.equalsIgnoreCase(statusALL)
                                                || statusFilter.equalsIgnoreCase(statusRunning))
                                            ;
                                        else
                                            toBeFiltered = true;
                                    }
                                    else if (liveStreamInfo.getLiveRecorderChannelStatus().equalsIgnoreCase(LiveStreamInfo.liveRecorderFailed))
                                    {
                                        if (statusFilter == null
                                                || statusFilter.equalsIgnoreCase(statusALL)
                                                || statusFilter.equalsIgnoreCase(statusKO))
                                            ;
                                        else
                                            toBeFiltered = true;
                                    }
                                    else if (liveStreamInfo.getLiveRecorderChannelStatus().equalsIgnoreCase(LiveStreamInfo.liveRecorderWithoutEncodingJob))
                                    {
                                        if (statusFilter == null
                                                || statusFilter.equalsIgnoreCase(statusALL)
                                                || statusFilter.equalsIgnoreCase(statusKO))
                                            ;
                                        else
                                            toBeFiltered = true;
                                    }
                                }
                                else
                                {
                                    mLogger.info("Not Found IngestionJob LiveRecorder"
                                            + ", label: " + liveStreamInfo.getStream().getLabel()
                                            + ", ConfKey: " + liveStreamInfo.getStream().getConfKey()
                                    );

                                    liveStreamInfo.setLiveRecorderChannelStatus(LiveStreamInfo.LiveRecorderWorkflowNotRunning);
                                    liveStreamInfo.setLiveRecorderErrorInfo(null);

                                    if (statusFilter == null
                                            || statusFilter.equalsIgnoreCase(statusALL))
                                        ;
                                    else
                                        toBeFiltered = true;
                                }

                                mLogger.info("LiveRecorder: statusFilter check"
                                        + ", label: " + liveStreamInfo.getStream().getLabel()
                                        + ", ConfKey: " + liveStreamInfo.getStream().getConfKey()
                                        + ", ingestionJobKey: " + (liveRecorderIngestionJob == null ? "null" : liveRecorderIngestionJob.getIngestionJobKey())
                                        + ", liveRecorderChannelStatus: " + liveStreamInfo.getLiveRecorderChannelStatus()
                                        + ", statusFilter: " + statusFilter
                                        + ", toBeFiltered: " + toBeFiltered
                                );

                                if (toBeFiltered)
                                    liveStreamInfoToBeFiltered.add(liveStreamInfo);
                            }
                            mLogger.info(aaa++);
                        }

                        mLogger.info(aaa++);
                        {
                            IngestionJob lastIngestionJob = null;

                            mLogger.info(aaa++);
                            // last among liveGridIngestionJob and liveProxyIngestionJob
                            {
                                if (liveProxyIngestionJob != null && liveGridIngestionJob == null)
                                    lastIngestionJob = liveProxyIngestionJob;
                                else if (liveProxyIngestionJob == null && liveGridIngestionJob != null)
                                    lastIngestionJob = liveGridIngestionJob;
                                else if (liveProxyIngestionJob != null && liveGridIngestionJob != null)
                                {
                                    if (liveProxyIngestionJob.getStartProcessing() != null && liveGridIngestionJob.getStartProcessing() != null)
                                    {
                                        // use the last one
                                        if (liveProxyIngestionJob.getStartProcessing().getTime() < liveGridIngestionJob.getStartProcessing().getTime())
                                            lastIngestionJob = liveGridIngestionJob;
                                        else
                                            lastIngestionJob = liveProxyIngestionJob;
                                    }
                                    else if (liveProxyIngestionJob.getStartProcessing() != null && liveGridIngestionJob.getStartProcessing() == null)
                                        lastIngestionJob = liveProxyIngestionJob;
                                    else
                                        lastIngestionJob = liveGridIngestionJob;
                                }
                            }

                            mLogger.info(aaa++);
                            // last among lastIngestionJob and liveRecorderIngestionJob
                            {
                                if (lastIngestionJob != null && liveRecorderIngestionJob == null)
                                    ;   // lastIngestionJob = lastIngestionJob;
                                else if (lastIngestionJob == null && liveRecorderIngestionJob != null)
                                    lastIngestionJob = liveRecorderIngestionJob;
                                else if (lastIngestionJob != null && liveRecorderIngestionJob != null)
                                {
                                    if (lastIngestionJob.getStartProcessing() != null && liveRecorderIngestionJob.getStartProcessing() != null)
                                    {
                                        // use the last one
                                        if (lastIngestionJob.getStartProcessing().getTime() < liveRecorderIngestionJob.getStartProcessing().getTime())
                                            lastIngestionJob = liveRecorderIngestionJob;
                                        else
                                            ; // lastIngestionJob = lastIngestionJob;
                                    }
                                    else if (lastIngestionJob.getStartProcessing() != null && liveRecorderIngestionJob.getStartProcessing() == null)
                                        ; // lastIngestionJob = lastIngestionJob;
                                    else
                                        lastIngestionJob = liveRecorderIngestionJob;
                                }
                            }

                            liveStreamInfo.setLastIngestionJob(lastIngestionJob);

                            mLogger.info(aaa++);
                            if (lastIngestionJob != null)
                            {
                                if (lastIngestionJob.getIngestionType().equalsIgnoreCase("Live-Grid"))
                                {
                                    liveStreamInfo.setLastChannelStatus(liveStreamInfo.getLiveGridChannelStatus());
                                    liveStreamInfo.setLastErrorInfo(liveStreamInfo.getLiveGridErrorInfo());
                                }
                                else if (lastIngestionJob.getIngestionType().equalsIgnoreCase("Live-Proxy"))
                                {
                                    liveStreamInfo.setLastChannelStatus(liveStreamInfo.getLiveProxyChannelStatus());
                                    liveStreamInfo.setLastErrorInfo(liveStreamInfo.getLiveProxyErrorInfo());
                                }
                                else if (lastIngestionJob.getIngestionType().equalsIgnoreCase("Live-Recorder"))
                                {
                                    liveStreamInfo.setLastChannelStatus(liveStreamInfo.getLiveRecorderChannelStatus());
                                    liveStreamInfo.setLastErrorInfo(liveStreamInfo.getLiveRecorderErrorInfo());
                                }
                            }
                            else
                            {
                                liveStreamInfo.setLastChannelStatus("");
                            }

                            mLogger.info(aaa++);
                            // liveGridIngestionJob and liveProxyIngestionJob and liveRecorderIngestionJob
                            {
                                String htmlChannelStatus;
                                int channelStatusPresent = 0;

                                htmlChannelStatus = "<ul>";

                                if (liveGridIngestionJob != null
                                        && !liveStreamInfo.getLiveGridChannelStatus().equalsIgnoreCase(LiveStreamInfo.LiveGridWorkflowNotRunning))
                                {
                                    htmlChannelStatus += (
                                            "<li style=\"color:"
                                                    + liveStreamInfo.getStatusStyleColor(liveStreamInfo.getLiveGridChannelStatus()) + "\">"
                                                    + liveStreamInfo.getLiveGridChannelStatus() + "</li>"
                                    );

                                    channelStatusPresent++;
                                }
                                if (liveProxyIngestionJob != null
                                        && !liveStreamInfo.getLiveProxyChannelStatus().equalsIgnoreCase(LiveStreamInfo.LiveProxyWorkflowNotRunning))
                                {
                                    htmlChannelStatus += (
                                            "<li style=\"color:"
                                                    + liveStreamInfo.getStatusStyleColor(liveStreamInfo.getLiveProxyChannelStatus()) + "\">"
                                                    + liveStreamInfo.getLiveProxyChannelStatus() + "</li>"
                                    );

                                    channelStatusPresent++;
                                }
                                if (liveRecorderIngestionJob != null
                                        && !liveStreamInfo.getLiveRecorderChannelStatus().equalsIgnoreCase(LiveStreamInfo.LiveRecorderWorkflowNotRunning))
                                {
                                    htmlChannelStatus += (
                                            "<li style=\"color:"
                                                    + liveStreamInfo.getStatusStyleColor(liveStreamInfo.getLiveRecorderChannelStatus()) + "\">"
                                                    + liveStreamInfo.getLiveRecorderChannelStatus() + "</li>"
                                    );

                                    channelStatusPresent++;
                                }
                                htmlChannelStatus += "</ul>";

                                if (channelStatusPresent == 0)
                                    liveStreamInfo.setHtmlChannelStatus(null);
                                    // else if (channelStatusPresent == 1)
                                    //    liveStreamInfo.setHtmlChannelStatus(liveStreamInfo.getLastChannelStatus());
                                else // if (channelStatusPresent > 0)
                                    liveStreamInfo.setHtmlChannelStatus(htmlChannelStatus);

                                /*
                                mLogger.info("htmlChannelStatus"
                                        + ", label: " + liveStreamInfo.getStream().getLabel()
                                        + ", ConfKey: " + liveStreamInfo.getStream().getConfKey()
                                        + ", liveGridIngestionJob: " + liveGridIngestionJob
                                        + ", liveStreamInfo.getLiveGridChannelStatus(): " + liveStreamInfo.getLiveGridChannelStatus()
                                        + ", liveProxyIngestionJob: " + liveProxyIngestionJob
                                        + ", liveStreamInfo.getLiveProxyChannelStatus(): " + liveStreamInfo.getLiveProxyChannelStatus()
                                        + ", liveRecorderIngestionJob: " + liveRecorderIngestionJob
                                        + ", liveStreamInfo.getLiveRecorderChannelStatus(): " + liveStreamInfo.getLiveRecorderChannelStatus()
                                        + ", htmlChannelStatus: " + liveStreamInfo.getHtmlChannelStatus()
                                );
                                 */
                            }
                        }

                        mLogger.info(aaa++);
                        Date channelEndPoint = new Date();
                        mLogger.info("liveStreamsFillList statistics (Channel) " + channelIndex + "/" + liveStreamInfoList.size()
                                + ", label: " + liveStreamInfo.getStream().getLabel()
                                + ", ConfKey: " + liveStreamInfo.getStream().getConfKey()
                                + ", elapsed (secs): "+ ((channelEndPoint.getTime() - channelStartingPoint.getTime()) / 1000)
                        );

                        channelIndex++;
                    }

                    mLogger.info("Number of liveStreamInfoToBeFiltered: " + liveStreamInfoToBeFiltered.size());
                    liveStreamInfoList.removeAll(liveStreamInfoToBeFiltered);

                    // setting of the playable flag
                    for (LiveStreamInfo liveStreamInfo: liveStreamInfoList)
                    {
                        mLogger.info("playable flag. Looking status for " + liveStreamInfo.getStream().getLabel()
                                + ", ConfKey: " + liveStreamInfo.getStream().getConfKey()
                        );
                        setLiveStreamPlayable(liveStreamInfo);
                    }
                }

                Date liveStreamInfoEndPoint = new Date();
                mLogger.info("liveStreamsFillList statistics (total)"
                        + ", liveStreamInfoList.size: " + liveStreamInfoList.size()
                        + ", elapsed (secs): "+ ((liveStreamInfoEndPoint.getTime() - liveStreamInfoStartingPoint.getTime()) / 1000)
                );
            }
        }
        catch (Exception e)
        {
            // 2021-02-01: Il chiamante capisce se il canale è attivo o meno in base alla presenza del canale in liveStreamInfoList.
            //  E' IMPORTANTE quindi che, se in questa procedura abbiamo una eccezione su un canale, tutta la procedura ritorni
            //  una eccezione. Questo perchè altrimenti, il chiamante pensa che quel canale non è attivo e lo fa ripartire
            // invece il canale era attivo ma abbiamo avuto una eccezione.
            // questo è il caso quando il componente API viene restartato, il canale è attivo ma semplicemente API non sta rispondendo

            String errorMessage = "Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    public static void setLiveGridChannelStatus(IngestionJob liveIngestionJob, LiveStreamInfo liveStreamInfo)
    {
        // if (liveIngestionJob.getErrorMessage() != null) // || liveIngestionJob.getEncodingJob().getFailuresNumber() > 0)
        if (liveIngestionJob.getStatus() == null
                || liveIngestionJob.getStatus().equalsIgnoreCase("End_IngestionFailure"))
        {
            liveStreamInfo.setLiveGridChannelStatus(LiveStreamInfo.liveGridStreamingNotWorking);
        }
        else
        {
            // no error
            if (liveIngestionJob.getStatus().equalsIgnoreCase("End_CanceledByUser"))
            {
                liveStreamInfo.setLiveGridChannelStatus(LiveStreamInfo.liveGridStreamingStoppedyUser);
            }
            else if (liveIngestionJob.getStatus().equalsIgnoreCase("End_CanceledByMMS"))
            {
                liveStreamInfo.setLiveGridChannelStatus(LiveStreamInfo.liveGridStreamingStoppedyMMS);
            }
            else
            {
                if (liveIngestionJob.getEncodingJob() != null)
                {
                    if (liveIngestionJob.getEncodingJob().getEncoderKey() == null ||
                            liveIngestionJob.getEncodingJob().getEncoderKey() == -1
                    ) {
                        liveStreamInfo.setLiveGridChannelStatus(LiveStreamInfo.liveGridStreamingWithoutTranscoderYet);
                    } else {
                        if (liveIngestionJob.getEncodingJob().getFailuresNumber() > 0) {
                            mLogger.warn("LiveGrid StreamingNotWorking because of failures number"
                                    + ", ingestionJobKey: " + liveIngestionJob.getIngestionJobKey()
                                    + ", encodingJobKey: " + liveIngestionJob.getEncodingJob().getEncodingJobKey()
                                    + ", failuresNumber: " + liveIngestionJob.getEncodingJob().getFailuresNumber()
                            );
                            liveStreamInfo.setLiveGridChannelStatus(LiveStreamInfo.liveGridStreamingNotWorking);
                        } else {
                            liveStreamInfo.setLiveGridChannelStatus(LiveStreamInfo.liveGridStreamingRunning);
                        }
                    }
                }
                else
                {
                    // it should never been here
                    liveStreamInfo.setLiveGridChannelStatus(LiveStreamInfo.liveGridStreamingWithoutEncodingJob);
                }
            }
        }
    }

    public static void setLiveProxyChannelStatus(IngestionJob liveIngestionJob, LiveStreamInfo liveStreamInfo)
    {
        // if (liveIngestionJob.getErrorMessage() != null) // || liveIngestionJob.getEncodingJob().getFailuresNumber() > 0)
        if (liveIngestionJob.getStatus() == null
                || liveIngestionJob.getStatus().equalsIgnoreCase("End_IngestionFailure"))
        {
            liveStreamInfo.setLiveProxyChannelStatus(LiveStreamInfo.liveProxyStreamingNotWorking);
        }
        else
        {
            // no error
            if (liveIngestionJob.getStatus().equalsIgnoreCase("End_CanceledByUser"))
            {
                liveStreamInfo.setLiveProxyChannelStatus(LiveStreamInfo.liveProxyStreamingStoppedyUser);
            }
            else if (liveIngestionJob.getStatus().equalsIgnoreCase("End_CanceledByMMS"))
            {
                liveStreamInfo.setLiveProxyChannelStatus(LiveStreamInfo.liveProxyStreamingStoppedyMMS);
            }
            else
            {
                if (liveIngestionJob.getEncodingJob() != null)
                {
                    if (liveIngestionJob.getEncodingJob().getEncoderKey() == null ||
                            liveIngestionJob.getEncodingJob().getEncoderKey() == -1)
                    {
                        liveStreamInfo.setLiveProxyChannelStatus(LiveStreamInfo.liveProxyStreamingWithoutTranscoderYet);
                    }
                    else
                    {
                        // Transcoder is assigned

                        if (liveIngestionJob.getEncodingJob().getStatus() != null
                                && liveIngestionJob.getEncodingJob().getStatus().startsWith("End_"))
                        {
                            // 2021-01-22: trovato scenario in cui lo stato dell'encoding è End_failed
                            //      e lo stato dell'ingestion è EncodingQueued.
                            //      Non penso dovrebbe mai accadere questo scenario, probabilmente dovuto al
                            //      precedente deploy dell'MMS (restart).
                            //      Comunque aggiungo questo scenario mettendo liveProxyStreamingNotWorking
                            //      La conseguenza è che verrà creato un nuovo IngestionJob per lo stesso canale
                            liveStreamInfo.setLiveProxyChannelStatus(LiveStreamInfo.liveProxyStreamingNotWorking);
                        }
                        else
                        {
                            if (liveIngestionJob.getEncodingJob().getFailuresNumber() > 0)
                            {
                                mLogger.warn("LiveProxy StreamingWorking but some failures"
                                        + ", ingestionJobKey: " + liveIngestionJob.getIngestionJobKey()
                                        + ", encodingJobKey: " + liveIngestionJob.getEncodingJob().getEncodingJobKey()
                                        + ", failuresNumber: " + liveIngestionJob.getEncodingJob().getFailuresNumber()
                                );
                                // liveStreamInfo.setChannelStatus(streamingNotWorking);
                                liveStreamInfo.setLiveProxyChannelStatus(LiveStreamInfo.liveProxyStreamingRunningButSomeRequestsFailed);
                            }
                            else
                            {
                                liveStreamInfo.setLiveProxyChannelStatus(LiveStreamInfo.liveProxyStreamingRunning);
                            }
                        }
                    }
                }
                else
                {
                    // it should never been here
                    liveStreamInfo.setLiveProxyChannelStatus(LiveStreamInfo.liveProxyStreamingWithoutEncodingJob);
                }
            }
        }
    }

    public static void setLiveRecorderChannelStatus(IngestionJob liveIngestionJob, LiveStreamInfo liveStreamInfo)
    {
        if (liveIngestionJob.getStatus() == null
                || liveIngestionJob.getStatus().equalsIgnoreCase("End_TaskSuccess"))
        {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String liveRecorderChannelStatus;
            liveRecorderChannelStatus = LiveStreamInfo.liveRecorderFinishedOK;
            // 2021-06-21: aggiunto check EndProcessingEstimate anche se per i live non sarebbe necessario
            if (liveIngestionJob.getEndProcessing() != null && !liveIngestionJob.getEndProcessingEstimate())
                liveRecorderChannelStatus += (" (" + simpleDateFormat.format(liveIngestionJob.getEndProcessing()) + ")");
            liveStreamInfo.setLiveRecorderChannelStatus(liveRecorderChannelStatus);
        }
        else if (
                liveIngestionJob.getStatus().equalsIgnoreCase("Start_TaskQueued") // setting up
                        || liveIngestionJob.getStatus().equalsIgnoreCase("EncodingQueued") // running
        )
        {
            if (liveIngestionJob.getEncodingJob() != null)
            {
                if (liveIngestionJob.getEncodingJob().getEncoderKey() == null ||
                        liveIngestionJob.getEncodingJob().getEncoderKey() == -1
                )
                    liveStreamInfo.setLiveRecorderChannelStatus(LiveStreamInfo.liveRecorderStreamingWithoutTranscoderYet);
                else
                    liveStreamInfo.setLiveRecorderChannelStatus(LiveStreamInfo.liveRecorderRunning);
            }
            else
            {
                // it should never been here
                liveStreamInfo.setLiveRecorderChannelStatus(LiveStreamInfo.liveRecorderStreamingWithoutTranscoderYet);
            }
        }
        else
        {
            liveStreamInfo.setLiveRecorderChannelStatus(LiveStreamInfo.liveRecorderFailed);
        }
    }

    public static void setLiveStreamPlayable(LiveStreamInfo liveStreamInfo)
    {
        if (liveStreamInfo.getStream().getSourceType().equals("IP_PULL"))
        {
            if (liveStreamInfo.getStream().getUrl().indexOf(".m3u8") != -1
                    || liveStreamInfo.getStream().getUrl().indexOf(".mpd") != -1)
                liveStreamInfo.setPlayable(true);
            else
            {
                if (liveStreamInfo.getLiveGridIngestionJob() != null
                        || liveStreamInfo.getLiveProxyIngestionJob() != null)
                {
                    liveStreamInfo.setPlayable(true);
					/*
					else if (liveStreamInfo.getIngestionJob().getIngestionType().equalsIgnoreCase("Live-Recorder"))
					{
						if (liveStreamInfo.getStream().getUrl().indexOf(".m3u8") != -1
								|| liveStreamInfo.getStream().getUrl().indexOf(".mpd") != -1)
							liveStreamInfo.setPlayable(true);
						else
							liveStreamInfo.setPlayable(false);
					}
					else
						liveStreamInfo.setPlayable(false);
					*/
                }
                else
                    liveStreamInfo.setPlayable(false);
            }
        }
        else if (liveStreamInfo.getStream().getSourceType().equals("IP_PUSH"))
        {
            liveStreamInfo.setPlayable(false);
        }
        else if (liveStreamInfo.getStream().getSourceType().equals("CaptureLive"))
        {
            liveStreamInfo.setPlayable(false);
        }
        else // if (liveStreamInfo.getStream().getSourceType().equals("TV"))
        {
            liveStreamInfo.setPlayable(false);
        }
    }

    public static void startLiveRecorder(
            CatraMMSAPI catraMMS,
            Long recordingCode,
            boolean checkIfAlreadyRunning,
            Long channelKey,            // channelKey or configurationLabel has to be initialized
            String configurationLabel,
            Date startRecording,    // if null from 4am to 3:59:59.999am
            Date stopRecording,    // if null from 4am to 3:59:59.999am
            Boolean buildVODAtTheEnd,
            String liveRecorderIngestionJobLabel,
            Long segmentDurationInSeconds,
            String ingester,
            String userName,
            String password,
            boolean thumbnail,
            String retentionChunk, String retentionBuildVODAtTheEnd,
            String encodersPool,
            boolean autoRenew,
            boolean monitorHLS,
            CDN77ChannelConf cdn77Channel,
            boolean virtualVOD,
            Long virtualVODMaxDurationInMinutes,
            Boolean monitoringFrameIncreasingEnabled)
            throws Exception
    {
        mLogger.info("Received startLiveRecorder"
                + ", recordingCode: " + recordingCode
                + ", channelKey: " + channelKey
                + ", configurationLabel: " + configurationLabel
                + ", startRecording: " + startRecording
                + ", stopRecording: " + stopRecording
                + ", buildVODAtTheEnd: " + buildVODAtTheEnd
                + ", liveRecorderIngestionJobLabel: " + liveRecorderIngestionJobLabel
                + ", segmentDurationInSeconds: " + segmentDurationInSeconds
                + ", ingester: " + ingester
                + ", userName: " + userName
                + ", password: " + password
                + ", thumbnail: " + thumbnail
                + ", retentionChunk: " + retentionChunk
                + ", retentionBuildVODAtTheEnd: " + retentionBuildVODAtTheEnd
                + ", encodersPool: " + encodersPool
                + ", autoRenew: " + autoRenew
                + ", monitorHLS: " + monitorHLS
                + ", cdn77Channel: " + cdn77Channel
                + ", virtualVOD: " + virtualVOD
                + ", virtualVODMaxDurationInMinutes: " + virtualVODMaxDurationInMinutes
                + ", monitoringFrameIncreasingEnabled: " + monitoringFrameIncreasingEnabled
        );

        try
        {
            if (channelKey == null && configurationLabel == null)
            {
                mLogger.error("Wrong IP channel input");

                throw new Exception("Wrong chanel input");
            }

            Stream channelConf = null;
            {
                List<Stream> channelConfList = new ArrayList<>();
                catraMMS.getStream(userName, password,
                        0, 1, channelKey, configurationLabel, false,
                        null, null, null, null, null, null,
                        null, channelConfList);

                if (channelConfList.size() != 1)
                {
                    String errorMessage = "Channel not found"
                            + ", channelKey: " + channelKey
                            + ", configurationLabel: " + configurationLabel
                            ;
                    mLogger.error(errorMessage);

                    throw new Exception(errorMessage);
                }

                channelConf = channelConfList.get(0);
            }

            // check if already running
            if (checkIfAlreadyRunning)
            {
                List<LiveStreamInfo> liveStreamInfoList = new ArrayList<>();
                liveStreamInfoList.add(new LiveStreamInfo(channelConf));

                boolean gridInfo = false;
                boolean proxyInfo = false;
                boolean recorderInfo = true;
                String statusFilter = LiveStreams.statusRunning;
                LiveStreams.fillLastLiveStreamsInfo(catraMMS, userName, password, liveStreamInfoList, statusFilter,
                        gridInfo, proxyInfo, recorderInfo);
                mLogger.info("Live-Recorder status"
                        + ", channelKey: " + channelKey
                        + ", channelName: " + (liveStreamInfoList.size() > 0 ? liveStreamInfoList.get(0).getStream().getName() : "")
                        + ", liveRecorderChannelStatus: " + (liveStreamInfoList.size() > 0 ? liveStreamInfoList.get(0).getLiveRecorderChannelStatus() : "")
                );
                if (liveStreamInfoList.size() > 0
                        && liveStreamInfoList.get(0).getLiveRecorderChannelStatus() != null &&
                        (
                                // liveStreamInfoList.get(0).getChannelStatus().equalsIgnoreCase(Channels.streamingRunning)
                                // || liveStreamInfoList.get(0).getChannelStatus().equalsIgnoreCase(Channels.streamingRunningButSomeRequestsFailed)
                                liveStreamInfoList.get(0).getLiveRecorderChannelStatus().equalsIgnoreCase(LiveStreamInfo.liveRecorderRunning)
                                        || liveStreamInfoList.get(0).getLiveRecorderChannelStatus().equalsIgnoreCase(LiveStreamInfo.liveRecorderStreamingWithoutTranscoderYet)
                        )
                )
                {
                    String errorMessage = "Live-Recorder already running"
                            + ", channelKey: " + channelKey
                            + ", channelName: " + liveStreamInfoList.get(0).getStream().getName()
                            + ", liveRecorderChannelStatus: " + liveStreamInfoList.get(0).getLiveRecorderChannelStatus()
                            + ", ingestionJobKey: " + (liveStreamInfoList.get(0).getLiveRecorderIngestionJob() != null
                            ? liveStreamInfoList.get(0).getLiveRecorderIngestionJob().getIngestionJobKey() : "null")
                            ;
                    mLogger.error(errorMessage);

                    throw new Exception(errorMessage);
                }
            }

            String localLiveRecorderIngestionJobLabel = liveRecorderIngestionJobLabel;
            if (localLiveRecorderIngestionJobLabel == null || localLiveRecorderIngestionJobLabel.isEmpty())
                localLiveRecorderIngestionJobLabel = channelKey + ": " + channelConf.getLabel();

            JSONObject joWorkflow = buildLiveRecorderJson(recordingCode,
                    channelConf.getLabel(), channelConf.getLabel(), startRecording, stopRecording, buildVODAtTheEnd,
                    localLiveRecorderIngestionJobLabel,
                    segmentDurationInSeconds, retentionChunk, retentionBuildVODAtTheEnd, ingester, thumbnail,
                    autoRenew, monitorHLS, cdn77Channel,
                    virtualVOD, virtualVODMaxDurationInMinutes, encodersPool,
                    monitoringFrameIncreasingEnabled);
            mLogger.info("joWorkflow: " + joWorkflow.toString(4));

            {
                List<IngestionResult> ingestionJobList = new ArrayList<>();


                IngestionResult workflowRoot = catraMMS.ingestWorkflow(userName, password,
                        joWorkflow.toString(4), ingestionJobList);

                /*
                Long cutIngestionJobKey = null;
                for (IngestionResult ingestionResult: ingestionJobList)
                {
                    if (ingestionResult.getLabel().equals(title))
                    {
                        cutIngestionJobKey = ingestionResult.getKey();

                        break;
                    }
                }

                // here cutIngestionJobKey should be != null, anyway we will do the check
                if (cutIngestionJobKey == null)
                {
                    String errorMessage = "eventId: " + eventId + ", cutIngestionJobKey is null!!!";
                    mLogger.error(errorMessage);

                    throw new Exception(errorMessage);
                }

                String successMessage = "{\"status\": \"Success\", "
                        + "\"eventId\": " + eventId + ", "
                        + "\"cutIngestionJobKey\": " + cutIngestionJobKey + ", "
                        + "\"errMsg\": null }";
                mLogger.info("cutMedia: " + successMessage);
                */
            }
        }
        catch (Exception e)
        {
            String errorMessage = "startLiveRecorder failed"
                    + ", Exception: " + e
                    ;
            mLogger.error(errorMessage);

            throw e;
        }
    }

    private static JSONObject buildLiveRecorderJson(Long recordingCode,
                                                    String channel, String liveConfigurationLabel, Date startRecording, Date stopRecording, Boolean buildVODAtTheEnd,
                                                    String liveRecorderIngestionJobLabel,
                                                    Long segmentDurationInSeconds, String retentionChunk, String retentionBuildVODAtTheEnd, String ingester,
                                                    boolean chunkThumbnail,
                                                    boolean autoRenew, boolean monitorHLS,
                                                    CDN77ChannelConf cdn77Channel,
                                                    boolean liveRecorderVirtualVOD,
                                                    Long liveRecorderVirtualVODMaxDurationInMinutes,
                                                    String encodersPool,
                                                    Boolean monitoringFrameIncreasingEnabled
    )
            throws Exception
    {
        try
        {
            JSONObject joWorkflow = CatraMMSWorkflow.buildWorkflowRootJson(
                    "Live Recorder: " + channel);

            Long utcLiveRecorderStart;
            Long utcLiveRecorderEnd;
            {
                if (startRecording == null || stopRecording == null)
                {
                    Calendar calendar = Calendar.getInstance();

                    if (calendar.get(Calendar.HOUR_OF_DAY) < 4)
                        calendar.add(Calendar.DAY_OF_MONTH, -1);

                    calendar.set(Calendar.HOUR_OF_DAY, 4);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    utcLiveRecorderStart = calendar.getTime().getTime();

                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    utcLiveRecorderEnd = calendar.getTime().getTime();
                } else {
                    utcLiveRecorderStart = startRecording.getTime();
                    utcLiveRecorderEnd = stopRecording.getTime();
                }
            }

            JSONObject joLiveRecorderGroupOfTasks = null;
            JSONArray jaLiveRecorderGroupOfTasks = null;
            if (buildVODAtTheEnd != null && buildVODAtTheEnd)
            {
                List<Object> oLiveRecorderGroupOfTasks = CatraMMSWorkflow.buildGroupOfTasks(
                        "Live Recorder (GOP)", "parallel", null,
                        utcLiveRecorderStart);
                joLiveRecorderGroupOfTasks = (JSONObject) oLiveRecorderGroupOfTasks.get(0);
                jaLiveRecorderGroupOfTasks = (JSONArray) oLiveRecorderGroupOfTasks.get(1);

                joWorkflow.put("task", joLiveRecorderGroupOfTasks);
            }

            JSONObject joLiveRecorder;
            {
                List<String> chunkTags = new ArrayList<>();
                chunkTags.add("CHUNK_" + channel);
                chunkTags.add(liveRecorderIngestionJobLabel);

                String monitorHLSEncodingProfileLabel = "MMS_HLS_H264_800Kb_veryfast_360p25_high422_AAC_92";

                List<OutputStream> outputStreamList = new ArrayList<>();
                if (cdn77Channel != null)
                {
                    OutputStream outputStream = new OutputStream(false, null);

                    outputStream.setOutputType("CDN_CDN77");
                    outputStream.setCdn77Channel(cdn77Channel);

                    // liveProxyOutput.setEncodingProfileLabel(monitorHLSEncodingProfileLabel);

					/*
					{
						JSONObject joFilters = new JSONObject();
						{
							{
								JSONArray jaVideo = new JSONArray();
								joFilters.put("video", jaVideo);

								{
									JSONObject joBlackdetect = new JSONObject();
									jaVideo.put(joBlackdetect);

									joBlackdetect.put("type", "blackdetect");
									joBlackdetect.put("black_min_duration", (float) 2.0);
									joBlackdetect.put("pixel_black_th", (float) 0.0);
								}

								{
									JSONObject joBlackframe = new JSONObject();
									jaVideo.put(joBlackframe);

									joBlackframe.put("type", "blackframe");
									joBlackframe.put("amount", 98);
									joBlackframe.put("threshold", 32);
								}

								{
									JSONObject joFreezedetect = new JSONObject();
									jaVideo.put(joFreezedetect);

									joFreezedetect.put("type", "freezedetect");
									joFreezedetect.put("noiseInDb", -60);
									joFreezedetect.put("duration", 2);
								}
							}
							{
								JSONArray jaAudio = new JSONArray();
								joFilters.put("audio", jaAudio);

								{
									JSONObject joSilencedetect = new JSONObject();
									jaAudio.put(joSilencedetect);

									joSilencedetect.put("type", "silencedetect");
									joSilencedetect.put("noise", (float) 0.0001);
								}
							}
							liveProxyOutput.setFilters(joFilters);
						}
					}
					*/

                    outputStreamList.add(outputStream);
                }

                String liveRecorderVirtualVODEncodingProfileLabel = null;

                joLiveRecorder = CatraMMSWorkflow.buildLiveRecorderJson(
                        liveRecorderIngestionJobLabel,

                        recordingCode,

                        liveConfigurationLabel,

                        chunkTags,
                        ingester,
                        retentionChunk,
                        null,
                        "Medium",
                        // highAvailability,
                        autoRenew,
                        null,
                        monitorHLS, // monitorHLS,
                        monitorHLSEncodingProfileLabel,
                        "ts",
                        segmentDurationInSeconds,
                        liveRecorderVirtualVOD,
                        liveRecorderVirtualVODMaxDurationInMinutes,
                        liveRecorderVirtualVODEncodingProfileLabel,
                        utcLiveRecorderStart,
                        utcLiveRecorderEnd,
                        encodersPool,
                        null,
                        outputStreamList,
                        null,
                        monitoringFrameIncreasingEnabled
                );

                if (buildVODAtTheEnd != null && buildVODAtTheEnd)
                    jaLiveRecorderGroupOfTasks.put(joLiveRecorder);
                else
                    joWorkflow.put("task", joLiveRecorder);
            }

            boolean chunkChangeFormatChunk = true;
            if (chunkThumbnail || chunkChangeFormatChunk)
            {
                JSONObject joLiveRecorderGOT;
                JSONArray jaLiveRecorderOnSuccessGOT;
                {
                    String liveRecorderGOTLabel = "Chunk Image & FileFormat (GOT)";

					/*
						2022-10-16
						scenario:
							- ProcessingStartingFrom viene inizializzata con lo start del registrazione.
							- la registrazione è molto lunga oppure renew è true
							- anche dopo 10 giorni, il ProcessingStartingFrom del 'GroupOfTasks'
								rimane all'inizio della registrazione
							- Problemi quindi perchè
								- il metodo getIngestionJobs non considera IngestionJob troppo vecchi (-7 gg)
								- il metodo retention degli IngestionJob elimina i jobs troppo vecchi

							- Per questo motivo, bisogna non inizializzare ProcessingStartingFrom,
								questo parametro infatti non è mandatory nel Task
					*/
                    List<Object> oLiveRecorderGroupOfTasks = CatraMMSWorkflow.buildGroupOfTasks(
                            liveRecorderGOTLabel, "parallel",
                            null, null); // utcLiveRecorderStart);
                    joLiveRecorderGOT = (JSONObject) oLiveRecorderGroupOfTasks.get(0);
                    jaLiveRecorderOnSuccessGOT = (JSONArray) oLiveRecorderGroupOfTasks.get(1);
                }

                {
                    JSONObject joLiveRecorderOnSuccess = CatraMMSWorkflow.buildEventJson(joLiveRecorder, "onSuccess");
                    joLiveRecorderOnSuccess.put("task", joLiveRecorderGOT);
                }

                if (chunkThumbnail)
                {
                    // IngestionJobLabel and CurrentUtcChunkStartTime_HHMISS are variable introduced by the LiveRecorder Task
                    // where it creates the Workflow to generate the Chunks
                    String liveChunkImageLabel = "${IngestionJobLabel} ${CurrentUtcChunkStartTime_HHMISS}";

                    /*
                    JSONObject joUserData = new JSONObject();
                    joUserData.put("matchId", match.getMatchId());
                    joUserData.put("groupName", match.getGroupName());

                    JSONArray jaThumbnailTags = new JSONArray();
                    jaThumbnailTags.put(match.getMatchId().toString());
                    jaThumbnailTags.put(match.getHomeTeamShortCode());
                    jaThumbnailTags.put(match.getHomeTeamName());
                    jaThumbnailTags.put(match.getGroupName());
                     */

                    List<WorkflowVariable> workflowVariableList = new ArrayList<>();
                    workflowVariableList.add(new WorkflowVariable("label", liveChunkImageLabel));
                    workflowVariableList.add(new WorkflowVariable("title", liveChunkImageLabel));
                    // workflowVariableList.add(new WorkflowVariable("imageUserData", joUserData.toString()));
                    // workflowVariableList.add(new WorkflowVariable("imageTags", jaThumbnailTags.toString()));
                    workflowVariableList.add(new WorkflowVariable("instantInSeconds", 30));

                    //workflowVariableList.add(new WorkflowVariable("uniqueName", "Channel_" + channelKey));
                    // workflowVariableList.add(new WorkflowVariable("allowUniqueNameOverride", true));
                    workflowVariableList.add(new WorkflowVariable("imageRetention", retentionChunk));
                    workflowVariableList.add(new WorkflowVariable("ingester", ingester));
                    workflowVariableList.add(new WorkflowVariable("initialFramesNumberToBeSkipped", (long) 0));
					/*
						2022-10-04.
						scenario:
							- ProcessingStartingFrom viene inizializzata con lo start del registrazione.
							- la registrazione è molto lunga oppure renew è true
							- anche dopo 10 giorni, il ProcessingStartingFrom del 'Best Picture of the Video'
								rimane all'inizio della registrazione
							- Problemi quindi perchè
								- il metodo getIngestionJobs non considera IngestionJob troppo vecchi (-7 gg)
								- il metodo retention degli IngestionJob elimina i jobs troppo vecchi

							- Per questo motivo, meglio non inizializzare ProcessingStartingFrom,
								questo parametro infatti non è mandatory nel Task
                    {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                        workflowVariableList.add(new WorkflowVariable("processingStartingFrom",
                                dateFormat.format(utcLiveRecorderStart)));
                    }
					*/

                    JSONObject joBestPictureOfVideo = CatraMMSWorkflow.buildWorkflowAsLibrary(
                            liveChunkImageLabel,
                            "MMS",
                            "Best Picture of the Video",
                            workflowVariableList
                    );

                    jaLiveRecorderOnSuccessGOT.put(joBestPictureOfVideo);
                }

                if (chunkChangeFormatChunk)
                {
					/*
						2022-10-04.
						scenario:
							- ProcessingStartingFrom viene inizializzata con lo start del registrazione.
							- la registrazione è molto lunga oppure renew è true
							- anche dopo 10 giorni, il ProcessingStartingFrom del 'ChangeFileFormat'
								rimane all'inizio della registrazione
							- Problemi quindi perchè
								- il metodo getIngestionJobs non considera IngestionJob troppo vecchi (-7 gg)
								- il metodo retention degli IngestionJob elimina i jobs troppo vecchi

							- Per questo motivo, meglio non inizializzare ProcessingStartingFrom,
								questo parametro infatti non è mandatory nel Task
					*/
                    JSONObject joChangeFileFormat = CatraMMSWorkflow.buildChangeFileFormat(
                            "Change File Format Chunk Content",
                            "mp4",
                            null, null); // utcLiveRecorderStart);
                    jaLiveRecorderOnSuccessGOT.put(joChangeFileFormat);
                }
            }

            if (buildVODAtTheEnd != null && buildVODAtTheEnd)
            {
                JSONObject joLiveRecorderGOTOnSuccess = CatraMMSWorkflow.buildEventJson(
                        joLiveRecorderGroupOfTasks, "onSuccess");

                String label = "Concat Live Recorder Chunks: " + channel;

                JSONObject joLiveRecorderGOTConcat = CatraMMSWorkflow.buildConcatDemuxJson(
                        label,
                        liveRecorderIngestionJobLabel,  // "Live Recorder: " + channel,
                        null, ingester,
                        retentionBuildVODAtTheEnd, null, null,
                        null, null, null, null,
                        null, null, null,
                        null,
                        null, null); // utcLiveRecorderStart);
                joLiveRecorderGOTOnSuccess.put("task", joLiveRecorderGOTConcat);

                JSONObject joConcatGOT;
                JSONArray jaConcatOnSuccessGOT;
                {
                    String concatGOTLabel = "Concat Image & FileFormat (GOT)";

                    List<Object> oConcatGroupOfTasks = CatraMMSWorkflow.buildGroupOfTasks(
                            concatGOTLabel, "parallel", null,
                            null); // utcLiveRecorderStart);
                    joConcatGOT = (JSONObject) oConcatGroupOfTasks.get(0);
                    jaConcatOnSuccessGOT = (JSONArray) oConcatGroupOfTasks.get(1);
                }

                {
                    JSONObject joConcatOnSuccess = CatraMMSWorkflow.buildEventJson(joLiveRecorderGOTConcat, "onSuccess");
                    joConcatOnSuccess.put("task", joConcatGOT);
                }

                {
                    // IngestionJobLabel and CurrentUtcChunkStartTime_HHMISS are variable introduced by the LiveRecorder Task
                    // where it creates the Workflow to generate the Chunks
                    String liveConcatImageLabel = "Concat " + liveRecorderIngestionJobLabel;

                    /*
                    JSONObject joUserData = new JSONObject();
                    joUserData.put("matchId", match.getMatchId());
                    joUserData.put("groupName", match.getGroupName());

                    JSONArray jaThumbnailTags = new JSONArray();
                    jaThumbnailTags.put(match.getMatchId().toString());
                    jaThumbnailTags.put(match.getHomeTeamShortCode());
                    jaThumbnailTags.put(match.getHomeTeamName());
                    jaThumbnailTags.put(match.getGroupName());
                     */

                    List<WorkflowVariable> workflowVariableList = new ArrayList<>();
                    workflowVariableList.add(new WorkflowVariable("label", liveConcatImageLabel));
                    workflowVariableList.add(new WorkflowVariable("title", liveConcatImageLabel));
                    // workflowVariableList.add(new WorkflowVariable("imageUserData", joUserData.toString()));
                    // workflowVariableList.add(new WorkflowVariable("imageTags", jaThumbnailTags.toString()));
                    workflowVariableList.add(new WorkflowVariable("instantInSeconds", 30));

                    //workflowVariableList.add(new WorkflowVariable("uniqueName", "Channel_" + channelKey));
                    // workflowVariableList.add(new WorkflowVariable("allowUniqueNameOverride", true));
                    workflowVariableList.add(new WorkflowVariable("imageRetention", retentionBuildVODAtTheEnd));
                    workflowVariableList.add(new WorkflowVariable("ingester", ingester));
                    workflowVariableList.add(new WorkflowVariable("initialFramesNumberToBeSkipped",
                            (long) (30 * 25)));
					/*
						2022-10-16.
						scenario:
							- ProcessingStartingFrom viene inizializzata con lo start del registrazione.
							- la registrazione è molto lunga oppure renew è true
							- anche dopo 10 giorni, il ProcessingStartingFrom del 'Best Picture of the Video'
								rimane all'inizio della registrazione
							- Problemi quindi perchè
								- il metodo getIngestionJobs non considera IngestionJob troppo vecchi (-7 gg)
								- il metodo retention degli IngestionJob elimina i jobs troppo vecchi

							- Per questo motivo, meglio non inizializzare ProcessingStartingFrom,
								questo parametro infatti non è mandatory nel Task
                    {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                        workflowVariableList.add(new WorkflowVariable("processingStartingFrom",
                                dateFormat.format(utcLiveRecorderStart)));
                    }
					*/

                    JSONObject joBestPictureOfVideo = CatraMMSWorkflow.buildWorkflowAsLibrary(
                            liveConcatImageLabel,
                            "MMS",
                            "Best Picture of the Video",
                            workflowVariableList
                    );

                    jaConcatOnSuccessGOT.put(joBestPictureOfVideo);
                }

                {
                    String encodingProfileLabel = "MMS_HLS_H264_800Kb_veryfast_360p25_high422_AAC_92";

                    JSONObject joEncode = CatraMMSWorkflow.buildEncodeJson(
                            "Encode Concat", null, null,
                            "Medium", encodingProfileLabel, null,
                            null,
                            null, null,
                            null,
                            null); // utcLiveRecorderStart);

                    jaConcatOnSuccessGOT.put(joEncode);
                }
            }

            mLogger.info("Ready for the ingest"
                    + ", json Workflow: " + joWorkflow.toString(4));

            return joWorkflow;
        }
        catch (Exception e)
        {
            String errorMessage = "buildLiveRecorderJson failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw e;
        }
    }

}
