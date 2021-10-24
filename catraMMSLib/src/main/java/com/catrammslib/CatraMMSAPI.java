package com.catrammslib;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import com.catrammslib.entity.AudioBitRate;
import com.catrammslib.entity.AudioTrack;
import com.catrammslib.entity.EMailConf;
import com.catrammslib.entity.Encoder;
import com.catrammslib.entity.EncodersPool;
import com.catrammslib.entity.EncodingJob;
import com.catrammslib.entity.EncodingProfile;
import com.catrammslib.entity.EncodingProfilesSet;
import com.catrammslib.entity.FTPConf;
import com.catrammslib.entity.FacebookConf;
import com.catrammslib.entity.IPChannelConf;
import com.catrammslib.entity.IngestionJob;
import com.catrammslib.entity.IngestionJobMediaItem;
import com.catrammslib.entity.IngestionWorkflow;
import com.catrammslib.entity.MediaItem;
import com.catrammslib.entity.MediaItemCrossReference;
import com.catrammslib.entity.PhysicalPath;
import com.catrammslib.entity.SATChannelConf;
import com.catrammslib.entity.SourceSATChannelConf;
import com.catrammslib.entity.UserProfile;
import com.catrammslib.entity.VideoBitRate;
import com.catrammslib.entity.VideoTrack;
import com.catrammslib.entity.WorkflowLibrary;
import com.catrammslib.entity.WorkflowVariable;
import com.catrammslib.entity.WorkspaceDetails;
import com.catrammslib.entity.YouTubeConf;
import com.catrammslib.utility.BulkOfDeliveryURLData;
import com.catrammslib.utility.HttpFeedFetcher;
import com.catrammslib.utility.IngestionResult;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by multi on 08.06.18.
 */
public class CatraMMSAPI {

    private final Logger mLogger = Logger.getLogger(this.getClass());

    private int timeoutInSeconds;
    private int maxRetriesNumber;
    private String mmsAPIProtocol;
    private String mmsAPIHostName;
    private int mmsAPIPort;
    private String mmsBinaryProtocol;
    private String mmsBinaryHostName;
    private int mmsBinaryPort;
    private Boolean authorizationThroughPath;

    public CatraMMSAPI(Properties configurationProperties)
    {
        try
        {
            // mLogger.info("getConfigurationParameters...");
            // Properties configurationProperties = Login.getConfigurationParameters();

            {
                String tmpTimeoutInSeconds = configurationProperties.getProperty("catramms.mms.timeoutInSeconds");
                if (tmpTimeoutInSeconds == null)
                {
                    String errorMessage = "No catramms.mms.timeoutInSeconds configuration found";
                    mLogger.error(errorMessage);

                    return;
                }
                timeoutInSeconds = Integer.parseInt(tmpTimeoutInSeconds);

                String tmpMaxRetriesNumber = configurationProperties.getProperty("catramms.mms.delivery.maxRetriesNumber");
                if (tmpMaxRetriesNumber == null)
                {
                    String errorMessage = "No catramms.mms.delivery.maxRetriesNumber configuration found";
                    mLogger.error(errorMessage);

                    return;
                }
                maxRetriesNumber = Integer.parseInt(tmpMaxRetriesNumber);

                String tmpAuthorizationThroughPath = configurationProperties.getProperty("catramms.mms.delivery.authorizationThroughPath");
                if (tmpAuthorizationThroughPath == null)
                {
                    String errorMessage = "No catramms.mms.authorizationThroughPath configuration found";
                    mLogger.error(errorMessage);

                    return;
                }
                authorizationThroughPath = Boolean.parseBoolean(tmpAuthorizationThroughPath);

                mmsAPIProtocol = configurationProperties.getProperty("catramms.mms.api.protocol");
                if (mmsAPIProtocol == null)
                {
                    String errorMessage = "No catramms.mms.api.protocol configuration found";
                    mLogger.error(errorMessage);

                    return;
                }

                mmsAPIHostName = configurationProperties.getProperty("catramms.mms.api.hostname");
                if (mmsAPIHostName == null)
                {
                    String errorMessage = "No catramms.mms.api.hostname configuration found";
                    mLogger.error(errorMessage);

                    return;
                }

                String tmpMmsAPIPort = configurationProperties.getProperty("catramms.mms.api.port");
                if (tmpMmsAPIPort == null)
                {
                    String errorMessage = "No catramms.mms.api.port configuration found";
                    mLogger.error(errorMessage);

                    return;
                }
                mmsAPIPort = Integer.parseInt(tmpMmsAPIPort);

                mmsBinaryProtocol = configurationProperties.getProperty("catramms.mms.binary.protocol");
                if (mmsBinaryProtocol == null)
                {
                    String errorMessage = "No catramms.mms.binary.protocol configuration found";
                    mLogger.error(errorMessage);

                    return;
                }

                mmsBinaryHostName = configurationProperties.getProperty("catramms.mms.binary.hostname");
                if (mmsBinaryHostName == null)
                {
                    String errorMessage = "No catramms.mms.binary.hostname configuration found";
                    mLogger.error(errorMessage);

                    return;
                }

                String tmpMmsBinaryPort = configurationProperties.getProperty("catramms.mms.binary.port");
                if (tmpMmsBinaryPort == null)
                {
                    String errorMessage = "No catramms.mms.binary.port configuration found";
                    mLogger.error(errorMessage);

                    return;
                }
                mmsBinaryPort = Integer.parseInt(tmpMmsBinaryPort);
                }
        }
        catch (Exception e)
        {
            String errorMessage = "Problems to get configuration. Exception: " + e;
            mLogger.error(errorMessage);

            return;
        }
    }

    public String getMmsAPIHostName() {
        return mmsAPIHostName;
    }

    public Long shareWorkspace(String username, String password,
                               Boolean userAlreadyPresent,
                               String userNameToShare, String emailAddressToShare,
                               String passwordToShare, String countryToShare,

                               Boolean createRemoveWorkspace, Boolean ingestWorkflow, Boolean createProfiles,
                               Boolean deliveryAuthorization, Boolean shareWorkspace,
                               Boolean editMedia, Boolean editConfiguration, Boolean killEncoding,
                               Boolean cancelIngestionJob, Boolean editEncodersPool, Boolean applicationRecorder)
            throws Exception
    {
        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/workspace/share"
                    + "?userAlreadyPresent=" + userAlreadyPresent.toString()
                    + "&createRemoveWorkspace=" + createRemoveWorkspace.toString()
                    + "&ingestWorkflow=" + ingestWorkflow.toString()
                    + "&createProfiles=" + createProfiles.toString()
                    + "&deliveryAuthorization=" + deliveryAuthorization.toString()
                    + "&shareWorkspace=" + shareWorkspace.toString()
                    + "&editMedia=" + editMedia.toString()
                    + "&editConfiguration=" + editConfiguration.toString()
                    + "&killEncoding=" + killEncoding.toString()
                    + "&cancelIngestionJob=" + cancelIngestionJob.toString()
                    + "&editEncodersPool=" + editEncodersPool.toString()
                    + "&applicationRecorder=" + applicationRecorder.toString()
            ;

            String postBodyRequest;
            if (userAlreadyPresent)
                postBodyRequest = "{ "
                    + "\"EMail\": \"" + emailAddressToShare + "\" "
                    + "} "
                    ;
            else
                postBodyRequest = "{ "
                        + "\"Name\": \"" + userNameToShare + "\", "
                        + "\"EMail\": \"" + emailAddressToShare + "\", "
                        + "\"Password\": \"" + passwordToShare + "\", "
                        + "\"Country\": \"" + countryToShare + "\" "
                        + "} "
                        ;

            mLogger.info("shareWorkspace"
                            + ", mmsURL: " + mmsURL
                            + ", postBodyRequest: " + postBodyRequest
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, postBodyRequest);
            mLogger.info("shareWorkspace. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "shareWorkspace failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        Long userKey;

        try
        {
            JSONObject joWMMSInfo = new JSONObject(mmsInfo);
            userKey = joWMMSInfo.getLong("userKey");
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing workspaceDetails failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return userKey;
    }

    public void setWorkspaceAsDefault(String username, String password,
                               Long workspaceKeyToBeSetAsDefault)
            throws Exception
    {
        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/workspace/default/" + workspaceKeyToBeSetAsDefault
                    ;

            String postBodyRequest = "";

            mLogger.info("setWorkspaceAsDefault"
                    + ", mmsURL: " + mmsURL
                    + ", postBodyRequest: " + postBodyRequest
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, postBodyRequest);
            mLogger.info("setWorkspaceAsDefault. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "setWorkspaceAsDefault failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public Long register(String userNameToRegister, String emailAddressToRegister,
                         String passwordToRegister, String countryToRegister,
                         String workspaceNameToRegister)
            throws Exception
    {
        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/user";

            String postBodyRequest = "{ "
                    + "\"Name\": \"" + userNameToRegister + "\", "
                    + "\"EMail\": \"" + emailAddressToRegister + "\", "
                    + "\"Password\": \"" + passwordToRegister + "\", "
                    + "\"Country\": \"" + countryToRegister + "\", "
                    + "\"WorkspaceName\": \"" + workspaceNameToRegister + "\" "
                    + "} "
                    ;

            mLogger.info("register"
                            + ", mmsURL: " + mmsURL
                            + ", postBodyRequest: " + postBodyRequest
            );

            String username = null;
            String password = null;

            Date now = new Date();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, postBodyRequest);
            mLogger.info("register. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "shareWorkspace failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        Long userKey;

        try
        {
            JSONObject joWMMSInfo = new JSONObject(mmsInfo);
            userKey = joWMMSInfo.getLong("userKey");
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing workspaceDetails failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return userKey;
    }

    public String confirmRegistration(Long userKey, String confirmationCode)
            throws Exception
    {
        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/user/" + userKey + "/" + confirmationCode;

            String username = null;
            String password = null;

            mLogger.info("confirmRegistration"
                            + ", mmsURL: " + mmsURL
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("confirmRegistration. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "confirmRegistration failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        String apiKey;

        try
        {
            JSONObject joWMMSInfo = new JSONObject(mmsInfo);
            apiKey = joWMMSInfo.getString("apiKey");
        }
        catch (Exception e)
        {
            String errorMessage = "confirmRegistration failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return apiKey;
    }

    public List<Object> login(boolean ldapEnabled, String username, String password, String remoteClientIPAddress)
            throws Exception
    {
        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/login";

            String postBodyRequest = "";

            JSONObject joBody = new JSONObject();
            if (ldapEnabled)
            {
                joBody.put("Name", username);
                joBody.put("Password", password);
                if (remoteClientIPAddress != null && !remoteClientIPAddress.isEmpty())
                    joBody.put("RemoteClientIPAddress", remoteClientIPAddress);
                /*
                postBodyRequest =
                        "{ "
                                + "\"Name\": \"" + username + "\", "
                                + "\"Password\": \"" + password + "\" "
                                + "} "
                ;
                */
            }
            else
            {
                joBody.put("EMail", username);
                joBody.put("Password", password);
                if (remoteClientIPAddress != null && !remoteClientIPAddress.isEmpty())
                    joBody.put("RemoteClientIPAddress", remoteClientIPAddress);
                /*
                postBodyRequest =
                        "{ "
                                + "\"EMail\": \"" + username + "\", "
                                + "\"Password\": \"" + password + "\" "
                                + "} "
                ;
                */
            }
            postBodyRequest = joBody.toString(2);

            mLogger.info("login"
                    + ", mmsURL: " + mmsURL
                    // commmented because of the password
                    // + ", postBodyRequest: " + postBodyRequest
            );

            Date now = new Date();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, postBodyRequest);
            mLogger.info("login. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "Login MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        UserProfile userProfile = new UserProfile();
        WorkspaceDetails workspaceDetails = new WorkspaceDetails();

        try
        {
            JSONObject joWMMSInfo = new JSONObject(mmsInfo);

            fillUserProfile(userProfile, joWMMSInfo);
            userProfile.setPassword(password);

            JSONObject joWorkspaceInfo = joWMMSInfo.getJSONObject("loginWorkspace");
            fillWorkspaceDetails(workspaceDetails, joWorkspaceInfo);
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing workspaceDetails failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        List<Object> objects = new ArrayList<>();
        objects.add(userProfile);
        objects.add(workspaceDetails);

        return objects;
    }

    public Long createWorkspace(String username, String password,
                                String workspaceNameToRegister)
            throws Exception
    {
        String mmsInfo;
        String mmsURL = null;
        try
        {
            mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/workspace";

            JSONObject joNewWorkspace = new JSONObject();
            joNewWorkspace.put("WorkspaceName", workspaceNameToRegister);

            String postBodyRequest = joNewWorkspace.toString();

            mLogger.info("createWorkspace"
                    + ", mmsURL: " + mmsURL
                    + ", postBodyRequest: " + postBodyRequest
            );

            Date now = new Date();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, postBodyRequest);
            mLogger.info("createWorkspace. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "createWorkspace failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        Long workspaceKey;

        try
        {
            JSONObject joWMMSInfo = new JSONObject(mmsInfo);
            workspaceKey = joWMMSInfo.getLong("workspaceKey");
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing workspaceDetails failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return workspaceKey;
    }

    public WorkspaceDetails updateWorkspace(String username, String password,
                                       boolean newEnabled, String newName, String newMaxEncodingPriority,
                                       String newEncodingPeriod, Long newMaxIngestionsNumber,
                                       Long newMaxStorageInMB, String newLanguageCode,
                                       boolean newCreateRemoveWorkspace, boolean newIngestWorkflow, boolean newCreateProfiles,
                                       boolean newDeliveryAuthorization, boolean newShareWorkspace,
                                       boolean newEditMedia, boolean newEditConfiguration, boolean newKillEncoding,
                                            boolean newCancelIngestionJob, boolean newEditEncodersPool, boolean newApplicationRecorder)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/workspace";

            JSONObject joBodyRequest = new JSONObject();
            joBodyRequest.put("Enabled", newEnabled);
            joBodyRequest.put("Name", newName);
            joBodyRequest.put("MaxEncodingPriority", newMaxEncodingPriority);
            joBodyRequest.put("EncodingPeriod", newEncodingPeriod);
            joBodyRequest.put("MaxIngestionsNumber", newMaxIngestionsNumber);
            joBodyRequest.put("MaxStorageInMB", newMaxStorageInMB);
            joBodyRequest.put("LanguageCode", newLanguageCode);
            joBodyRequest.put("CreateRemoveWorkspace", newCreateRemoveWorkspace);
            joBodyRequest.put("IngestWorkflow", newIngestWorkflow);
            joBodyRequest.put("CreateProfiles", newCreateProfiles);
            joBodyRequest.put("DeliveryAuthorization", newDeliveryAuthorization);
            joBodyRequest.put("ShareWorkspace", newShareWorkspace);
            joBodyRequest.put("EditMedia", newEditMedia);
            joBodyRequest.put("EditConfiguration", newEditConfiguration);
            joBodyRequest.put("KillEncoding", newKillEncoding);
            joBodyRequest.put("CancelIngestionJob", newCancelIngestionJob);
            joBodyRequest.put("EditEncodersPool", newEditEncodersPool);
            joBodyRequest.put("ApplicationRecorder", newApplicationRecorder);

            String bodyRequest = joBodyRequest.toString();

            mLogger.info("updateUser"
                            + ", mmsURL: " + mmsURL
                            + ", bodyRequest: " + bodyRequest
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, bodyRequest);
            mLogger.info("updateWorkspace. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "updateWorkspace failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        WorkspaceDetails workspaceDetails = new WorkspaceDetails();

        try
        {
            JSONObject joWMMSInfo = new JSONObject(mmsInfo);

            // JSONObject jaWorkspaceInfo = joWMMSInfo.getJSONObject("workspace");

            fillWorkspaceDetails(workspaceDetails, joWMMSInfo);
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing WorkspaceDetails failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return workspaceDetails;
    }

    public void deleteWorkspace(String username, String password)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/workspace";

            mLogger.info("deleteWorkspace"
                    + ", mmsURL: " + mmsURL
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null);
            mLogger.info("deleteWorkspace. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "deleteWorkspace failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public UserProfile updateUserProfile(String username, String password,
                                         String newName,
                                         String newEmailAddress,
                                         String newCountry,
                                         String oldPassword,
                                         String newPassword)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/user";

            String bodyRequest = "{ "
                    + "\"Name\": \"" + newName + "\", "
                    + "\"EMail\": \"" + newEmailAddress + "\", "
                    + ((newPassword != null && !newPassword.isEmpty()) ? ("\"NewPassword\": \"" + newPassword + "\", ") : "")
                    + ((oldPassword != null && !oldPassword.isEmpty()) ? ("\"OldPassword\": \"" + oldPassword + "\", ") : "")
                    + "\"Country\": \"" + newCountry + "\" "
                    + "} "
                    ;

            mLogger.info("updateUser"
                            + ", mmsURL: " + mmsURL
                            // + ", bodyRequest: " + bodyRequest
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, bodyRequest);
            mLogger.info("updateUserProfile. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "updateUser failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        UserProfile userProfile = new UserProfile();

        try
        {
            JSONObject joWMMSInfo = new JSONObject(mmsInfo);

            fillUserProfile(userProfile, joWMMSInfo);

            if (newPassword != null && !newPassword.isEmpty())
                userProfile.setPassword(newPassword);
            else
                userProfile.setPassword(password);
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing userProfile failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return userProfile;
    }

    public void mmsSupport(String username, String password,
                           String userEmailAddress,
                           String subject,
                           String text)
            throws Exception
    {
        String mmsInfo;
        String mmsURL = null;
        try
        {
            mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/support";

            JSONObject joMMSSupport = new JSONObject();
            joMMSSupport.put("UserEmailAddress", userEmailAddress);
            joMMSSupport.put("Subject", subject);
            joMMSSupport.put("Text", text);

            String postBodyRequest = joMMSSupport.toString(4);

            mLogger.info("mmsSupport"
                    + ", mmsURL: " + mmsURL
                    + ", postBodyRequest: " + postBodyRequest
            );

            Date now = new Date();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, postBodyRequest);
            mLogger.info("mmsSupport. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "mmsSupport failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public IngestionResult ingestWorkflow(String username, String password,
                                          String jsonWorkflow, List<IngestionResult> ingestionJobList)
            throws Exception
    {
        String mmsInfo;
        String mmsURL = null;
        try
        {
            mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/workflow";

            mLogger.info("ingestWorkflow"
                    + ", mmsURL: " + mmsURL
                            + ", jsonWorkflow: " + jsonWorkflow
            );

            Date now = new Date();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonWorkflow);
            mLogger.info("ingestWorkflow. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "ingestWorkflow MMS failed"
                    + ", mmsURL: " + mmsURL
                    + ", Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        IngestionResult workflowRoot = new IngestionResult();

        try
        {
            JSONObject joWMMSInfo = new JSONObject(mmsInfo);
            JSONObject workflowInfo = joWMMSInfo.getJSONObject("workflow");
            JSONArray jaTasksInfo = joWMMSInfo.getJSONArray("tasks");

            workflowRoot.setKey(workflowInfo.getLong("ingestionRootKey"));
            workflowRoot.setLabel(workflowInfo.getString("label"));

            for (int taskIndex = 0; taskIndex < jaTasksInfo.length(); taskIndex++)
            {
                JSONObject taskInfo = jaTasksInfo.getJSONObject(taskIndex);

                IngestionResult ingestionJob = new IngestionResult();
                ingestionJob.setKey(taskInfo.getLong("ingestionJobKey"));
                ingestionJob.setLabel(taskInfo.getString("label"));

                ingestionJobList.add(ingestionJob);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "ingestWorkflow failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return workflowRoot;
    }

    public String getMetaDataContent(String username, String password,
                                     Long ingestionRootKey, Boolean processedMetadata)
            throws Exception
    {
        String metaDataContent;
        String mmsURL = null;
        try
        {
            mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/workflow/metaDataContent/" + ingestionRootKey
                    + "?processedMetadata=" + (processedMetadata == null ? "false" : processedMetadata)
            ;

            mLogger.info("getMetaDataContent"
                    + ", mmsURL: " + mmsURL
                    + ", ingestionRootKey: " + ingestionRootKey
                    + ", processedMetadata: " + processedMetadata
            );

            Date now = new Date();
            metaDataContent = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getMetaDataContent. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "getMetaDataContent MMS failed"
                    + ", mmsURL: " + mmsURL
                    + ", Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }


        return metaDataContent;
    }

    public Long addEncodingProfile(String username, String password,
                                   String contentType, String jsonEncodingProfile)
            throws Exception
    {
        Long encodingProfileKey;

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/encodingProfile/" + contentType;

            mLogger.info("addEncodingProfile"
                            + ", mmsURL: " + mmsURL
                            + ", contentType: " + contentType
                            + ", jsonEncodingProfile: " + jsonEncodingProfile
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, jsonEncodingProfile);
            mLogger.info("addEncodingProfile. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addEncodingProfile MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joWMMSInfo = new JSONObject(mmsInfo);

            encodingProfileKey = joWMMSInfo.getLong("encodingProfileKey");
            String encodingProfileLabel = joWMMSInfo.getString("label");
        }
        catch (Exception e)
        {
            String errorMessage = "addEncodingProfile failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return encodingProfileKey;
    }

    public void removeEncodingProfile(String username, String password,
                                   Long encodingProfileKey)
            throws Exception
    {
        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/encodingProfile/" + encodingProfileKey;

            mLogger.info("removeEncodingProfile"
                            + ", mmsURL: " + mmsURL
                            + ", encodingProfileKey: " + encodingProfileKey
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null);
            mLogger.info("removeEncodingProfile. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeEncodingProfile MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public Long addEncodingProfilesSet(String username, String password,
                                   String contentType, String jsonEncodingProfilesSet)
            throws Exception
    {
        Long encodingProfilesSetKey;

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/encodingProfilesSet/" + contentType;

            mLogger.info("addEncodingProfilesSet"
                            + ", mmsURL: " + mmsURL
                            + ", contentType: " + contentType
                            + ", jsonEncodingProfilesSet: " + jsonEncodingProfilesSet
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, jsonEncodingProfilesSet);
            mLogger.info("addEncodingProfilesSet. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addEncodingProfile MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joWMMSInfo = new JSONObject(mmsInfo);

            JSONObject joEncodingProfilesSet = joWMMSInfo.getJSONObject("encodingProfilesSet");

            encodingProfilesSetKey = joEncodingProfilesSet.getLong("encodingProfilesSetKey");
            String encodingProfilesSetLabel = joEncodingProfilesSet.getString("label");

            // ...
        }
        catch (Exception e)
        {
            String errorMessage = "addEncodingProfile failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return encodingProfilesSetKey;
    }

    public void removeEncodingProfilesSet(String username, String password,
                                      Long encodingProfilesSetKey)
            throws Exception
    {
        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/encodingProfilesSet/" + encodingProfilesSetKey;

            mLogger.info("removeEncodingProfilesSet"
                            + ", mmsURL: " + mmsURL
                            + ", encodingProfilesSetKey: " + encodingProfilesSetKey
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null);
            mLogger.info("removeEncodingProfilesSet. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeEncodingProfilesSet MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void updateEncodingJobPriority(String username, String password,
                                          Long encodingJobKey, int newEncodingJobPriorityCode)
            throws Exception
    {
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/encodingJob/" + encodingJobKey + "?newEncodingJobPriorityCode=" + newEncodingJobPriorityCode;

            mLogger.info("updateEncodingJobPriority"
                            + ", mmsURL: " + mmsURL
            );

            String putBodyRequest = "";
            Date now = new Date();
            HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, putBodyRequest);
            mLogger.info("updateEncodingJobPriority. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "updateEncodingJobPriority MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void killEncodingJob(String username, String password, Long encodingJobKey)
            throws Exception
    {
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/encodingJob/" + encodingJobKey;

            mLogger.info("killEncodingJob"
                    + ", mmsURL: " + mmsURL
            );

            Date now = new Date();
            HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null);
            mLogger.info("killEncodingJob. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "killEncodingJob MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void updateEncodingJobTryAgain(String username, String password,
                                          Long encodingJobKey)
            throws Exception
    {
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/encodingJob/" + encodingJobKey + "?tryEncodingAgain=true";

            mLogger.info("updateEncodingJobTryAgain"
                    + ", mmsURL: " + mmsURL
            );

            String putBodyRequest = "";
            Date now = new Date();
            HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, putBodyRequest);
            mLogger.info("updateEncodingJobTryAgain. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "updateEncodingJobTryAgain MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public Long addEncoder(String username, String password,
                           String label, boolean external,
						   boolean enabled, String protocol,
						   String serverName, Long port)
            throws Exception
    {
        Long encoderKey;

        String mmsInfo;
        try
        {
			JSONObject joEncoder = new JSONObject();

			joEncoder.put("Label", label);
			joEncoder.put("External", external);
			joEncoder.put("Enabled", enabled);
			joEncoder.put("Protocol", protocol);
			joEncoder.put("ServerName", serverName);
			joEncoder.put("Port", port);

			String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/encoder";

            mLogger.info("addEncoder"
                    + ", mmsURL: " + mmsURL
                    + ", joEncoder: " + joEncoder.toString()
            );

            Date now = new Date();
            String postContentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, postContentType,
                    timeoutInSeconds, maxRetriesNumber,
                    username, password, null, joEncoder.toString());
            mLogger.info("addEncoder. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addEncodingProfile MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joWMMSInfo = new JSONObject(mmsInfo);

            encoderKey = joWMMSInfo.getLong("EncoderKey");
        }
        catch (Exception e)
        {
            String errorMessage = "addEncoder failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return encoderKey;
    }

    public void modifyEncoder(String username, String password,
                              Long encoderKey,
							  String label, boolean external,
							  boolean enabled, String protocol,
							  String serverName, Long port)
			   throws Exception
    {
        String mmsInfo;
        try
        {
			JSONObject joEncoder = new JSONObject();

			joEncoder.put("Label", label);
			joEncoder.put("External", external);
			joEncoder.put("Enabled", enabled);
			joEncoder.put("Protocol", protocol);
			joEncoder.put("ServerName", serverName);
			joEncoder.put("Port", port);

			String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/encoder/" + encoderKey;

            mLogger.info("modifyEncoder"
                    + ", mmsURL: " + mmsURL
                    + ", encoderKey: " + encoderKey
                    + ", joEncoder: " + joEncoder.toString()
            );

            Date now = new Date();
            String postContentType = null;
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL,
                    timeoutInSeconds, maxRetriesNumber,
                    username, password, joEncoder.toString());
            mLogger.info("modifyEncoder. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "modifyEncoder MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        /*
        try
        {
            JSONObject joWMMSInfo = new JSONObject(mmsInfo);

            encoderKey = joWMMSInfo.getLong("EncoderKey");
        }
        catch (Exception e)
        {
            String errorMessage = "addEncoder failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
        */

        // return encoderKey;
    }

    public void removeEncoder(String username, String password,
                              Long encoderKey)
            throws Exception
    {
        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/encoder/" + encoderKey;

            mLogger.info("removeEncoder"
                    + ", mmsURL: " + mmsURL
                    + ", encoderKey: " + encoderKey
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null);
            mLogger.info("removeEncoder. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeEncoder MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void getEncoders(String username, String password,
                            Boolean allEncoders, Long workspaceKey,
                            List<Encoder> encoderList)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            // server API will use allEncoders and workspaceKey only in case of admin
            String urlAdminParameters = "";
            if (allEncoders != null)
                urlAdminParameters += ("&allEncoders=" + (allEncoders ? "true" : "false"));
            if (workspaceKey != null)
            {
                // in case allEncoders is true (we are admin and want all the encoders),
                //      workspaceKey, asking the encoder for a specific workspace, does not have sense.
                //  Anyway MMS API will take care...
                urlAdminParameters += ("&workspaceKey=" + workspaceKey);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                + "/catramms/1.0.1/encoder?labelOrder=" + "asc"
                    + urlAdminParameters
            ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getEncoders. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            JSONArray jaEncoders = joResponse.getJSONArray("encoders");

            mLogger.info("jaEncoders.length(): " + jaEncoders.length());

            encoderList.clear();

            for (int encoderIndex = 0;
                 encoderIndex < jaEncoders.length();
                 encoderIndex++)
            {
                Encoder encoder = new Encoder();

                JSONObject encoderInfo = jaEncoders.getJSONObject(encoderIndex);

                fillEncoder(encoder, encoderInfo);

                encoderList.add(encoder);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public Encoder getEncoder(String username, String password,
                              Boolean allEncoders, Long encoderKey)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            if (encoderKey == null)
            {
                String errorMessage = "getEncoder, encoderKey cannot be null"
                        + ", encoderKey: " + encoderKey
                ;
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/encoder/" + encoderKey
                    ;

            // server API will use allEncoders only in case of admin.
            // This is the scenario where User is admin, all the encoders are showed,
            // the User needs to edit one encoder the, may be, is not associated to his workspace,
            // So it is needed allEncoders true otherwise only the encoders of te user workspace are considered
            // and this call will fails
            if (allEncoders != null)
                mmsURL += ("?allEncoders=" + (allEncoders ? "true" : "false"));

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getEncoder. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        Encoder encoder = null;
        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            JSONArray jaEncoders = joResponse.getJSONArray("encoders");

            mLogger.info("jaEncoders.length(): " + jaEncoders.length());

            if (jaEncoders.length() == 1)
            {
                encoder = new Encoder();

                JSONObject encoderInfo = jaEncoders.getJSONObject(0);

                fillEncoder(encoder, encoderInfo);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return encoder;
    }

    public void getEncodersPool(String username, String password,
                            List<EncodersPool> encodersPoolList)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/encodersPool?labelOrder=" + "asc"
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getEncodersPool. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            JSONArray jaEncodersPool = joResponse.getJSONArray("encodersPool");

            mLogger.info("jaEncodersPool.length(): " + jaEncodersPool.length());

            encodersPoolList.clear();

            for (int encodersPoolIndex = 0;
                 encodersPoolIndex < jaEncodersPool.length();
                 encodersPoolIndex++)
            {
                EncodersPool encodersPool = new EncodersPool();

                JSONObject encodersPoolInfo = jaEncodersPool.getJSONObject(encodersPoolIndex);

                fillEncodersPool(encodersPool, encodersPoolInfo);

                encodersPoolList.add(encodersPool);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public Long addEncodersPool(String username, String password,
		String label, List<Encoder> encoderList)
	throws Exception
    {
        Long encoderKey;

        String mmsInfo;
        try
        {
            JSONObject joEncodersPool = new JSONObject();
            joEncodersPool.put("Label", label);

            JSONArray jaEncoderKeys = new JSONArray();
            joEncodersPool.put("encoderKeys", jaEncoderKeys);

            for (Encoder encoder: encoderList)
                jaEncoderKeys.put(encoder.getEncoderKey());

			String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/encodersPool";

            mLogger.info("addEncoder"
                    + ", mmsURL: " + mmsURL
                    + ", joEncodersPool: " + joEncodersPool.toString()
            );

            Date now = new Date();
            String postContentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, postContentType,
                    timeoutInSeconds, maxRetriesNumber,
                    username, password, null, joEncodersPool.toString());
            mLogger.info("addEncodersPool. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addEncodersPool MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joWMMSInfo = new JSONObject(mmsInfo);

            encoderKey = joWMMSInfo.getLong("EncodersPoolKey");
        }
        catch (Exception e)
        {
            String errorMessage = "addEncodersPool failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return encoderKey;
    }

    public void modifyEncodersPool(String username, String password,
                              Long encodersPoolKey, 
							  String label, List<Encoder> encoderList)
            throws Exception
    {
        String mmsInfo;
        try
        {
            JSONObject joEncodersPool = new JSONObject();
            joEncodersPool.put("Label", label);

            JSONArray jaEncoderKeys = new JSONArray();
            joEncodersPool.put("encoderKeys", jaEncoderKeys);

            for (Encoder encoder: encoderList)
                jaEncoderKeys.put(encoder.getEncoderKey());

			String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/encodersPool/" + encodersPoolKey;

            mLogger.info("modifyEncoder"
                    + ", mmsURL: " + mmsURL
                    + ", encodersPoolKey: " + encodersPoolKey
                    + ", joEncodersPool: " + joEncodersPool.toString()
            );

            Date now = new Date();
            String postContentType = null;
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL,
                    timeoutInSeconds, maxRetriesNumber,
                    username, password, joEncodersPool.toString());
            mLogger.info("modifyEncodersPool. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "modifyEncodersPool MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        /*
        try
        {
            JSONObject joWMMSInfo = new JSONObject(mmsInfo);

            encoderKey = joWMMSInfo.getLong("EncoderKey");
        }
        catch (Exception e)
        {
            String errorMessage = "addEncoder failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
        */

        // return encoderKey;
    }

    public void removeEncodersPool(String username, String password,
                              Long encodersPoolKey)
            throws Exception
    {
        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/encodersPool/" + encodersPoolKey;

            mLogger.info("removeEncoder"
                    + ", mmsURL: " + mmsURL
                    + ", encodersPoolKey: " + encodersPoolKey
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null);
            mLogger.info("removeEncodersPool. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeEncodersPool MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void assignEncoderToWorkspace(String username, String password,
                           Long workspaceKey, Long encoderKey)
            throws Exception
    {
        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/workspace-encoder/" + workspaceKey + "/" + encoderKey
                    ;

            mLogger.info("assignEncoderToWorkspace"
                    + ", mmsURL: " + mmsURL
                    + ", workspaceKey: " + workspaceKey
                    + ", encoderKey: " + encoderKey
            );

            Date now = new Date();
            String postContentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, postContentType,
                    timeoutInSeconds, maxRetriesNumber,
                    username, password, null, null);
            mLogger.info("assignEncoderToWorkspace. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "assignEncoderToWorkspace MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joWMMSInfo = new JSONObject(mmsInfo);

            // aaencoderKey = joWMMSInfo.getLong("EncoderKey");
        }
        catch (Exception e)
        {
            String errorMessage = "assignEncoderToWorkspace failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void removeEncoderFromWorkspace(String username, String password,
                                         Long workspaceKey, Long encoderKey)
            throws Exception
    {
        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/workspace-encoder/" + workspaceKey + "/" + encoderKey
                    ;

            mLogger.info("removeEncoderFromWorkspace"
                    + ", mmsURL: " + mmsURL
                    + ", workspaceKey: " + workspaceKey
                    + ", encoderKey: " + encoderKey
            );

            Date now = new Date();
            String postContentType = null;
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL,
                    timeoutInSeconds, maxRetriesNumber,
                    username, password, null);
            mLogger.info("removeEncoderFromWorkspace. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeEncoderFromWorkspace MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joWMMSInfo = new JSONObject(mmsInfo);

            // aaencoderKey = joWMMSInfo.getLong("EncoderKey");
        }
        catch (Exception e)
        {
            String errorMessage = "removeEncoderFromWorkspace failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void getWorkspaceList(String username, String password,
                            List<WorkspaceDetails> workspaceDetailsList)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/workspace"
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getWorkspaceList. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            JSONArray jaWorkspaces = joResponse.getJSONArray("workspaces");

            mLogger.info("jaWorkspaces.length(): " + jaWorkspaces.length());

            workspaceDetailsList.clear();

            for (int workspaceIndex = 0;
                 workspaceIndex < jaWorkspaces.length();
                 workspaceIndex++)
            {
                WorkspaceDetails workspaceDetails = new WorkspaceDetails();

                JSONObject workspaceInfo = jaWorkspaces.getJSONObject(workspaceIndex);

                fillWorkspaceDetails(workspaceDetails, workspaceInfo);

                workspaceDetailsList.add(workspaceDetails);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public Long getMediaItems(String username, String password,
                              long startIndex, long pageSize,
                              Long mediaItemKey, String uniqueName,
                              List<Long> otherMediaItemsKey,
                              String contentType,   // video, audio, image
                              Date ingestionStart, Date ingestionEnd,
                              String title, Boolean bLiveRecordingChunk,
                              List<String> tagsIn, List<String> tagsNotIn,
                              String jsonCondition,
                              String orderBy, String jsonOrderBy,
                              List<MediaItem> mediaItemsList    // has to be initialized (new ArrayList<>())
    )
            throws Exception
    {
        Long numFound;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String liveRecordingChunkParameter = "";
            if (bLiveRecordingChunk != null)
            {
                if (bLiveRecordingChunk == true)
                    liveRecordingChunkParameter = "&liveRecordingChunk=true";
                else
                    liveRecordingChunkParameter = "&liveRecordingChunk=false";
            }

            Long newMediaItemKey;
            if (mediaItemKey == null && otherMediaItemsKey != null && otherMediaItemsKey.size() > 0)
            {
                newMediaItemKey = otherMediaItemsKey.get(0);
                otherMediaItemsKey.remove(0);
            }
            else
            {
                newMediaItemKey = mediaItemKey;
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/mediaItem"
                    + (newMediaItemKey == null ? "" : ("/" + newMediaItemKey))
                    + "?start=" + startIndex
                    + "&rows=" + pageSize
                    + "&contentType=" + contentType
                    + (uniqueName == null || uniqueName.isEmpty() ? "" : ("&uniqueName=" + java.net.URLEncoder.encode(uniqueName, "UTF-8")))
                    + "&title=" + (title == null ? "" : java.net.URLEncoder.encode(title, "UTF-8")) // requires unescape server side
                    + liveRecordingChunkParameter
                    // + "&tags=" + (tags == null ? "" : java.net.URLEncoder.encode(tags, "UTF-8"))
                    + (ingestionStart != null ? ("&startIngestionDate=" + simpleDateFormat.format(ingestionStart)) : "")
                    + (ingestionEnd != null ? ("&endIngestionDate=" + simpleDateFormat.format(ingestionEnd)) : "")
                    + "&jsonCondition=" + (jsonCondition == null ? "" : java.net.URLEncoder.encode(jsonCondition, "UTF-8"))
                    + "&orderBy=" + (orderBy == null ? "" : java.net.URLEncoder.encode(orderBy, "UTF-8"))
                    + "&jsonOrderBy=" + (jsonOrderBy == null ? "" : java.net.URLEncoder.encode(jsonOrderBy, "UTF-8"))
                    ;

			String body = null;
			if (tagsIn != null && tagsIn.size() > 0
				|| (tagsNotIn != null && tagsNotIn.size() > 0))
			{
				JSONObject joOtherInputs = new JSONObject();
				{
					JSONArray jaTagsIn = new JSONArray();
					if (tagsIn != null)
					{
						for(String tag: tagsIn)
						{
							jaTagsIn.put(tag);
						}
					}
					joOtherInputs.put("tagsIn", jaTagsIn);
	
					JSONArray jaTagsNotIn = new JSONArray();
					if (tagsNotIn != null)
					{
						for(String tag: tagsNotIn)
						{
							jaTagsNotIn.put(tag);
						}
					}
					joOtherInputs.put("tagsNotIn", jaTagsNotIn);
	
					JSONArray jaOtherMediaItemsKey = new JSONArray();
					if (newMediaItemKey != null && otherMediaItemsKey != null)
					{
						for(Long localMediaItemKey: otherMediaItemsKey)
						{
							jaOtherMediaItemsKey.put(localMediaItemKey);
						}
					}
					joOtherInputs.put("otherMediaItemsKey", jaOtherMediaItemsKey);
				}
				body = joOtherInputs.toString(4);
			}

            mLogger.info("mmsURL: " + mmsURL
                    + ", body: " + body
                    + ", username: " + username
            );

            Date now = new Date();
			if (body != null && body.length() > 0)
			{
				String postContentType = null;
				mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, postContentType, timeoutInSeconds, maxRetriesNumber,
						username, password, null, body);
			}
			else
			{
				mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
						username, password);
			}
            mLogger.info("getMediaItems. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "getMediaItems MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            mediaItemsList.clear();

            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            numFound = joResponse.getLong("numFound");

            JSONArray jaMediaItems = joResponse.getJSONArray("mediaItems");

            for (int mediaItemIndex = 0; mediaItemIndex < jaMediaItems.length(); mediaItemIndex++)
            {
                JSONObject mediaItemInfo = jaMediaItems.getJSONObject(mediaItemIndex);

                MediaItem mediaItem = new MediaItem();

                boolean deep = true;
                fillMediaItem(mediaItem, mediaItemInfo, deep);

                mediaItemsList.add(mediaItem);
            }

            mLogger.info("getMediaItems. mediaItemsList.size: " + mediaItemsList.size());
        }
        catch (Exception e)
        {
            String errorMessage = "getMediaItems failed"
                    + ", Exception: " + e
                    + ", mmsInfo: " + mmsInfo
                    ;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return numFound;
    }

    public MediaItem getMediaItemByMediaItemKey(String username, String password,
                                  Long mediaItemKey)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/mediaItem/" + mediaItemKey;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getMediaItemByMediaItemKey. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            // throw new Exception(errorMessage);
            return null;
        }

        MediaItem mediaItem = new MediaItem();

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            JSONArray jaMediaItems = joResponse.getJSONArray("mediaItems");

            if (jaMediaItems.length() != 1)
            {
                String errorMessage = "Wrong MediaItems number returned, expected one. jaMediaItems.length: " + jaMediaItems.length();
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            {
                JSONObject mediaItemInfo = jaMediaItems.getJSONObject(0);

                boolean deep = true;
                fillMediaItem(mediaItem, mediaItemInfo, deep);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return mediaItem;
    }

    public MediaItem getMediaItemByPhysicalPathKey(String username, String password,
		Long physicalPathKey)
        throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/mediaItem"
				+ "?physicalPathKey=" + physicalPathKey
				;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber, username, password);
            mLogger.info("getMediaItemByPhysicalPathKey. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            // throw new Exception(errorMessage);
            return null;
        }

        MediaItem mediaItem = new MediaItem();

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            JSONArray jaMediaItems = joResponse.getJSONArray("mediaItems");

            if (jaMediaItems.length() != 1)
            {
                String errorMessage = "Wrong MediaItems number returned, expected one. jaMediaItems.length: " + jaMediaItems.length();
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            {
                JSONObject mediaItemInfo = jaMediaItems.getJSONObject(0);

                boolean deep = true;
                fillMediaItem(mediaItem, mediaItemInfo, deep);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return mediaItem;
    }

	public MediaItem getMediaItemByUniqueName(String username, String password,
		String uniqueName)
        throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/mediaItem"
                    + "?uniqueName=" + java.net.URLEncoder.encode(uniqueName, "UTF-8") // requires unescape server side
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getMediaItemByUniqueName. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            // throw new Exception(errorMessage);
            return null;
        }

        MediaItem mediaItem = new MediaItem();

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            JSONArray jaMediaItems = joResponse.getJSONArray("mediaItems");

            if (jaMediaItems.length() != 1)
            {
                String errorMessage = "Wrong MediaItems number returned, expected one. jaMediaItems.length: " + jaMediaItems.length();
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            {
                JSONObject mediaItemInfo = jaMediaItems.getJSONObject(0);

                boolean deep = true;
                fillMediaItem(mediaItem, mediaItemInfo, deep);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return mediaItem;
    }

    public MediaItem updateMediaItem(String username, String password,
                                     Long mediaItemKey, // mandatory
                                     String newTitle,
                                     String newUserData,
                                     String newTags,    // json array of string
                                     Long newRetentionInMinutes,
                                     String newUniqueName
    )
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            if (newTitle == null
                    && newUserData == null
                    && newTags == null
                    && newRetentionInMinutes == null
                    && newUniqueName == null
            )
            {
                String errorMessage = "No updates has to be done";
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            JSONArray jaTags = null;
            if (newTags != null)
            {
                try {
                    jaTags = new JSONArray(newTags);
                } catch (Exception ex) {
                    String errorMessage = "updateMediaItem. Wrong tags format"
                            + ", newTags: " + newTags;
                    mLogger.error(errorMessage);

                    throw new Exception(errorMessage);
                }
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/mediaItem/" + mediaItemKey
                    ;

            JSONObject joEdit = new JSONObject();
            if (newTitle != null)
                joEdit.put("Title", newTitle);
            if (newUserData != null)
                joEdit.put("UserData", newUserData); // mms backend manages this field as string since it saves it as string into DB
            if (jaTags != null)
                joEdit.put("Tags", jaTags); // mms backend manages this field as a json array to get the tags
            if (newRetentionInMinutes != null)
                joEdit.put("RetentionInMinutes", newRetentionInMinutes);
            if (newUniqueName != null)
                joEdit.put("UniqueName", newUniqueName);

            String sEdit = joEdit.toString(4);

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, sEdit);
            mLogger.info("updateMediaItem. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        MediaItem mediaItem = new MediaItem();

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            JSONArray jaMediaItems = joResponse.getJSONArray("mediaItems");

            if (jaMediaItems.length() != 1)
            {
                String errorMessage = "Wrong MediaItems number returned, expected one. jaMediaItems.length: " + jaMediaItems.length();
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            {
                JSONObject mediaItemInfo = jaMediaItems.getJSONObject(0);

                boolean deep = true;
                fillMediaItem(mediaItem, mediaItemInfo, deep);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return mediaItem;
    }

    public MediaItem updatePhysicalPath(String username, String password,
                                        Long mediaItemKey,
                                        Long physicalPathKey,
                                        Long newRetentionInMinutes
    )
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/mediaItem/"
                    + mediaItemKey + "/" + physicalPathKey
                    ;

            JSONObject joEdit = new JSONObject();
            joEdit.put("RetentionInMinutes", newRetentionInMinutes == null ? -1 : newRetentionInMinutes);

            String sEdit = joEdit.toString(4);

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, sEdit);
            mLogger.info("updatePhysicalPath. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        MediaItem mediaItem = new MediaItem();

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            JSONArray jaMediaItems = joResponse.getJSONArray("mediaItems");

            if (jaMediaItems.length() != 1)
            {
                String errorMessage = "Wrong MediaItems number returned, expected one. jaMediaItems.length: " + jaMediaItems.length();
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            {
                JSONObject mediaItemInfo = jaMediaItems.getJSONObject(0);

                boolean deep = true;
                fillMediaItem(mediaItem, mediaItemInfo, deep);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return mediaItem;
    }

    public Long getWorkspaceUsageInMB(String username, String password)
            throws Exception
    {
        Long workSpaceUsageInMB;


        String mmsInfo;
        try
        {
            String ingestionDatesParameters = "";

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/workspace/usage"
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getWorkspaceUsageInMB. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "getWorkspaceUsageInMB MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            workSpaceUsageInMB = joResponse.getLong("usageInMB");
        }
        catch (Exception e)
        {
            String errorMessage = "getStorageUsage failed"
                    + ", Exception: " + e
                    + ", mmsInfo: " + mmsInfo
                    ;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return workSpaceUsageInMB;
    }

    public Long getTags(String username, String password,
                        long startIndex, long pageSize, String contentType,
                        List<String> tagsList)
            throws Exception
    {
        Long numFound;

        String mmsInfo;
        try
        {

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/tag"
                    + "?start=" + startIndex
                    + "&rows=" + pageSize
                    + "&contentType=" + (contentType == null ? "" : contentType)
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getTags. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "getTags MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            tagsList.clear();

            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            numFound = joResponse.getLong("numFound");
            JSONArray jaTags = joResponse.getJSONArray("tags");

            for (int tagIndex = 0; tagIndex < jaTags.length(); tagIndex++)
            {
                tagsList.add(jaTags.getString(tagIndex));
            }

            mLogger.info("getTags"
                    + ", contentType: " + contentType
                    + ", tagsList.size: " + tagsList.size()
                    + ", numFound: " + numFound
            );
        }
        catch (Exception e)
        {
            String errorMessage = "getTags failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return numFound;
    }

    public Long getIngestionWorkflows(String username, String password,
                                      long startIndex, long pageSize,
                                      Date start, Date end, String label,
                                      Long ingestionRootKey, Long mediaItemKey,
                                      String status, boolean ascending,
                                      boolean ingestionJobOutputs,
                                      List<IngestionWorkflow> ingestionWorkflowsList)
            throws Exception
    {
        Long numFound;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/workflow"
                    + "?start=" + startIndex
                    + "&rows=" + pageSize
                    + "&label=" + (label == null ? "" : java.net.URLEncoder.encode(label, "UTF-8")) // requires unescape server side
                    + "&status=" + (status == null ? "" : status)
                    + "&ingestionRootKey=" + (ingestionRootKey == null ? "" : ingestionRootKey)
                    + "&mediaItemKey=" + (mediaItemKey == null ? "" : mediaItemKey)
                    + "&asc=" + (ascending ? "true" : "false")
                    + "&ingestionJobOutputs=" + (ingestionJobOutputs ? "true" : "false")
                    + "&startIngestionDate=" + simpleDateFormat.format(start)
                    + "&endIngestionDate=" + simpleDateFormat.format(end);

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getIngestionWorkflows. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "getIngestionWorkflows MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            ingestionWorkflowsList.clear();

            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            numFound = joResponse.getLong("numFound");
            JSONArray jaWorkflows = joResponse.getJSONArray("workflows");

            for (int ingestionWorkflowIndex = 0; ingestionWorkflowIndex < jaWorkflows.length(); ingestionWorkflowIndex++)
            {
                JSONObject ingestionWorkflowInfo = jaWorkflows.getJSONObject(ingestionWorkflowIndex);

                IngestionWorkflow ingestionWorkflow = new IngestionWorkflow();

                boolean deep = true;
                fillIngestionWorkflow(ingestionWorkflow, ingestionWorkflowInfo, deep);

                ingestionWorkflowsList.add(ingestionWorkflow);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "getIngestionWorkflows failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return numFound;
    }

    public IngestionWorkflow getIngestionWorkflow(String username, String password,
                                  Long ingestionRootKey, boolean ingestionJobOutputs)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            if (ingestionRootKey == null)
            {
                String errorMessage = "getIngestionWorkflow. ingestionRootKey is null";
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/workflow/"
                    + ingestionRootKey
                    + "?ingestionJobOutputs=" + (ingestionJobOutputs ? "true" : "false")
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getIngestionWorkflow. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        IngestionWorkflow ingestionWorkflow = new IngestionWorkflow();

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            JSONArray jaWorkflows = joResponse.getJSONArray("workflows");

            if (jaWorkflows.length() != 1)
            {
                String errorMessage = "Wrong Workflows number returned. jaWorkflows.length: " + jaWorkflows.length();
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            {
                JSONObject ingestionWorkflowInfo = jaWorkflows.getJSONObject(0);

                boolean deep = true;
                fillIngestionWorkflow(ingestionWorkflow, ingestionWorkflowInfo, deep);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "getIngestionWorkflow failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return ingestionWorkflow;
    }

    public Long getIngestionJobs(String username, String password,
                                 long startIndex, long pageSize,
                                 String label, Long ingestionJobKey,
                                 Date start, Date end,
                                 String status,             // completed or notCompleted
                                 String ingestionType, String jsonParametersCondition,
                                 boolean ingestionDateAscending,
                                 boolean ingestionJobOutputs,
                                 List<IngestionJob> ingestionJobsList)
            throws Exception
    {
        Long numFound;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/ingestionJob"
                    + (ingestionJobKey == null ? "" : ("/" + ingestionJobKey))
                    + "?start=" + startIndex
                    + "&rows=" + pageSize
                    + "&label=" + (label == null ? "" : java.net.URLEncoder.encode(label, "UTF-8")) // requires unescape server side
                    + "&status=" + (status == null ? "" : status)
                    + ((ingestionType == null || ingestionType.equalsIgnoreCase("all")) ? "" : ("&ingestionType=" + ingestionType))
                    + "&jsonParametersCondition=" + (jsonParametersCondition == null || jsonParametersCondition.isEmpty()
                        ? "" : java.net.URLEncoder.encode(jsonParametersCondition, "UTF-8")) // requires unescape server side
                    + "&asc=" + (ingestionDateAscending ? "true" : "false")
                    + "&ingestionJobOutputs=" + (ingestionJobOutputs ? "true" : "false")
                    + (start == null ? "" : ("&startIngestionDate=" + simpleDateFormat.format(start)))
                    + (end == null ? "" : ("&endIngestionDate=" + simpleDateFormat.format(end)))
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            /*
            2020-06-07: MMS GUI asks for LiveRecorder (ingestionJobs), the return contains all the MediaItems Output that,
                for each live recorder, are really a lot.
                So, in this scenario, make sure to have a long timeoutInSeconds, otherwise it will raise a timeout exception
             */
            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getIngestionJobs. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");

            if (mmsInfo == null || mmsInfo.isEmpty())
            {
                String errorMessage = "mmsInfo is wrong"
                        + ", mmsInfo: " + mmsInfo
                ;
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "getIngestionJobs MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        JSONObject ingestionJobInfo = null;
        try
        {
            ingestionJobsList.clear();

            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            numFound = joResponse.getLong("numFound");
            JSONArray jaIngestionJobs = joResponse.getJSONArray("ingestionJobs");

            // mLogger.info("jaIngestionJobs: " + jaIngestionJobs.toString(4));

            for (int ingestionJobIndex = 0; ingestionJobIndex < jaIngestionJobs.length(); ingestionJobIndex++)
            {
                ingestionJobInfo = jaIngestionJobs.getJSONObject(ingestionJobIndex);

                IngestionJob ingestionJob = new IngestionJob();

                fillIngestionJob(ingestionJob, ingestionJobInfo);

                ingestionJobsList.add(ingestionJob);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "getIngestionJobs failed"
                    + ", ingestionJobInfo: " + (ingestionJobInfo == null ? "null" : ingestionJobInfo.toString())
                    + ", Exception: " + e
                    ;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return numFound;
    }

    public IngestionJob getIngestionJob(String username, String password,
                                 Long ingestionJobKey, boolean ingestionJobOutputs)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/ingestionJob/" + ingestionJobKey
                    + "?ingestionJobOutputs=" + (ingestionJobOutputs ? "true" : "false")
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getIngestionJob. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "getIngestionJob MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        IngestionJob ingestionJob = new IngestionJob();

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            JSONArray jaIngestionJobs = joResponse.getJSONArray("ingestionJobs");

            if (jaIngestionJobs.length() != 1)
            {
                String errorMessage = "Wrong Jobs number returned. jaIngestionJobs.length: " + jaIngestionJobs.length();
                mLogger.error(errorMessage);

                return null;
            }

            {
                JSONObject ingestionJobInfo = jaIngestionJobs.getJSONObject(0);

                fillIngestionJob(ingestionJob, ingestionJobInfo);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "getIngestionWorkflows failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return ingestionJob;
    }

    public void cancelIngestionJob(String username, String password, Long ingestionJobKey, Boolean forceCancel)
            throws Exception
    {
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/ingestionJob/" + ingestionJobKey
                    + "?forceCancel=" + (forceCancel == null ? "false" : forceCancel)
                    ;

            mLogger.info("cancelIngestionJob"
                    + ", mmsURL: " + mmsURL
            );

            Date now = new Date();
            HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null);
            mLogger.info("cancelIngestionJob. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "cancelIngestionJob MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void updateIngestionJob_LiveRecorder(String username, String password,
                                                Long ingestionJobKey,
                                                String newLabel, Date newRecordingStart, Date newRecordingEnd,
                                                Boolean newRecordingVirtualVod,
                                                String newChannelLabel)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/ingestionJob/" + ingestionJobKey;

            JSONObject joBodyRequest = new JSONObject();
            joBodyRequest.put("IngestionType", "Live-Recorder");
            joBodyRequest.put("IngestionJobLabel", newLabel);
            joBodyRequest.put("ChannelLabel", newChannelLabel);
            joBodyRequest.put("RecordingPeriodStart", simpleDateFormat.format(newRecordingStart));
            joBodyRequest.put("RecordingPeriodEnd", simpleDateFormat.format(newRecordingEnd));
            joBodyRequest.put("RecordingVirtualVOD", newRecordingVirtualVod);

            String bodyRequest = joBodyRequest.toString();

            mLogger.info("updateIngestionJob_LiveRecorder"
                    + ", mmsURL: " + mmsURL
                    + ", bodyRequest: " + bodyRequest
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, bodyRequest);
            mLogger.info("updateWorkspace. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "updateIngestionJob_LiveRecorder failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public Long getEncodingJobs(String username, String password,
                                long startIndex, long pageSize,
                                Date ingestionStart, Date ingestionEnd,
                                Date encodingStart, Date encodingEnd,
                                Long encoderKey, Boolean alsoEncodingJobsFromOtherWorkspaces,
                                String status, String typesCommaSeparated,
                                boolean ascending,
                                List<EncodingJob> encodingJobsList)
            throws Exception
    {
        Long numFound;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/encodingJob"
                    + "?start=" + startIndex
                    + "&rows=" + pageSize
                    + "&status=" + status
                    + "&types=" + (typesCommaSeparated == null || typesCommaSeparated.isEmpty() ? "" : typesCommaSeparated)
                    + "&asc=" + (ascending ? "true" : "false")
                    + (ingestionStart != null ? ("&startIngestionDate=" + simpleDateFormat.format(ingestionStart)) : "")
                    + (ingestionEnd != null ? ("&endIngestionDate=" + simpleDateFormat.format(ingestionEnd)) : "")
                    + (encodingStart != null ? ("&startEncodingDate=" + simpleDateFormat.format(encodingStart)) : "")
                    + (encodingEnd != null ? ("&endEncodingDate=" + simpleDateFormat.format(encodingEnd)) : "")
                    + (encoderKey != null ? ("&encoderKey=" + encoderKey) : "")
                    + (alsoEncodingJobsFromOtherWorkspaces != null
                        ? ("&alsoEncodingJobsFromOtherWorkspaces=" + alsoEncodingJobsFromOtherWorkspaces) : "")
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getEncodingJobs. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "getEncodingJobs MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            encodingJobsList.clear();

            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            numFound = joResponse.getLong("numFound");
            JSONArray jaEncodingJobs = joResponse.getJSONArray("encodingJobs");

            for (int encodingJobIndex = 0; encodingJobIndex < jaEncodingJobs.length(); encodingJobIndex++)
            {
                JSONObject encodingJobInfo = jaEncodingJobs.getJSONObject(encodingJobIndex);

                EncodingJob encodingJob = new EncodingJob();

                fillEncodingJob(encodingJob, encodingJobInfo);

                encodingJobsList.add(encodingJob);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "getEncodingJobs failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return numFound;
    }

    public EncodingJob getEncodingJob(String username, String password,
                                Long encodingJobKey)
            throws Exception
    {
        Long numFound;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/encodingJob/" + encodingJobKey;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getEncodingJob. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "getEncodingJob MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        EncodingJob encodingJob = new EncodingJob();

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            JSONArray jaEncodingJobs = joResponse.getJSONArray("encodingJobs");

            if (jaEncodingJobs.length() != 1)
            {
                String errorMessage = "Wrong Jobs number returned. jaEncodingJobs.length: " + jaEncodingJobs.length();
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            {
                JSONObject encodingJobInfo = jaEncodingJobs.getJSONObject(0);

                fillEncodingJob(encodingJob, encodingJobInfo);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "getEncodingJobs failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return encodingJob;
    }

    public EncodingProfile getEncodingProfile(String username, String password,
                                  Long encodingProfileKey)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/encodingProfile/" + encodingProfileKey;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getEncodingProfile. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        EncodingProfile encodingProfile = new EncodingProfile();

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            JSONArray jaEncodingProfiles = joResponse.getJSONArray("encodingProfiles");

            if (jaEncodingProfiles.length() != 1)
            {
                String errorMessage = "Wrong EncodingProfiles number returned. jaEncodingProfiles.length: " + jaEncodingProfiles.length();
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            {
                JSONObject encodingProfileInfo = jaEncodingProfiles.getJSONObject(0);

                boolean deep = true;
                fillEncodingProfile(encodingProfile, encodingProfileInfo, deep);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return encodingProfile;
    }

    public void getEncodingProfiles(String username, String password,
                                    String contentType,
                                    Long encodingProfileKey,
                                    String label,
                                    List<EncodingProfile> encodingProfileList)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/encodingProfiles/" + contentType;
            if (encodingProfileKey != null)
                mmsURL += ("/" + encodingProfileKey);
            if (label != null && !label.isEmpty())
                mmsURL += ("?label=" + java.net.URLEncoder.encode(label, "UTF-8")); // requires unescape server side

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getEncodingProfiles. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            JSONArray jaEncodingProfiles = joResponse.getJSONArray("encodingProfiles");

            mLogger.info("jaEncodingProfiles.length(): " + jaEncodingProfiles.length());

            encodingProfileList.clear();

            for (int encodingProfileIndex = 0;
                 encodingProfileIndex < jaEncodingProfiles.length();
                 encodingProfileIndex++)
            {
                EncodingProfile encodingProfile = new EncodingProfile();

                JSONObject encodingProfileInfo = jaEncodingProfiles.getJSONObject(encodingProfileIndex);

                boolean deep = false;
                fillEncodingProfile(encodingProfile, encodingProfileInfo, deep);

                encodingProfileList.add(encodingProfile);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public EncodingProfilesSet getEncodingProfilesSet(String username, String password,
                                              Long encodingProfilesSetKey)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/encodingProfilesSet/" + encodingProfilesSetKey;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getEncodingProfilesSet. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        EncodingProfilesSet encodingProfilesSet = new EncodingProfilesSet();

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            JSONArray jaEncodingProfilesSets = joResponse.getJSONArray("encodingProfilesSets");

            if (jaEncodingProfilesSets.length() != 1)
            {
                String errorMessage = "Wrong EncodingProfilesSet number returned. jaEncodingProfilesSets.length: " + jaEncodingProfilesSets.length();
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            {
                JSONObject encodingProfilesSetInfo = jaEncodingProfilesSets.getJSONObject(0);

                boolean deep = true;
                fillEncodingProfilesSet(encodingProfilesSet, encodingProfilesSetInfo, deep);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return encodingProfilesSet;
    }

    public void getEncodingProfilesSets(String username, String password,
                                    String contentType,
                                    List<EncodingProfilesSet> encodingProfilesSetList)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/encodingProfilesSets/" + contentType;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getEncodingProfilesSets. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            JSONArray jaEncodingProfilesSets = joResponse.getJSONArray("encodingProfilesSets");

            encodingProfilesSetList.clear();

            for (int encodingProfilesSetIndex = 0;
                 encodingProfilesSetIndex < jaEncodingProfilesSets.length();
                 encodingProfilesSetIndex++)
            {
                EncodingProfilesSet encodingProfilesSet = new EncodingProfilesSet();

                JSONObject encodingProfilesSetInfo = jaEncodingProfilesSets.getJSONObject(encodingProfilesSetIndex);

                boolean deep = false;
                fillEncodingProfilesSet(encodingProfilesSet, encodingProfilesSetInfo, deep);

                encodingProfilesSetList.add(encodingProfilesSet);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void getBulkOfDeliveryURL(
            String username, String password,
            List<BulkOfDeliveryURLData> bulkOfDeliveryURLDataList,  // IN and OUT (deliveryURL)
            long ttlInSeconds, int maxRetries
    )
            throws Exception
    {
        String mmsInfo;
        Map<String, BulkOfDeliveryURLData> bulkOfDeliveryURLDataMap = new HashMap<>();
        Map<Long, BulkOfDeliveryURLData> liveBulkOfDeliveryURLDataMap = new HashMap<>();
        try
        {
            /*
            {
                "uniqueNameList" : [
                    {
                        "uniqueName": "...",
                        "encodingProfileKey": 123,
						"encodingProfileLabel": "..."
                    },
                    ...
                ],
                "liveIngestionJobKeyList" : [
                    {
                        "ingestionJobKey": 1234
                    },
                    ...
                ]
             }
             */

            JSONObject joDeliveryAuthorizationDetails = new JSONObject();
            {
                JSONArray jaUniqueNameList = new JSONArray();
                joDeliveryAuthorizationDetails.put("uniqueNameList", jaUniqueNameList);

                JSONArray jaLiveIngestionJobKeyList = new JSONArray();
                joDeliveryAuthorizationDetails.put("liveIngestionJobKeyList", jaLiveIngestionJobKeyList);

                for (BulkOfDeliveryURLData bulkOfDeliveryURLData: bulkOfDeliveryURLDataList)
                {
                    if (bulkOfDeliveryURLData.getUniqueName() != null)
                    {
                        JSONObject joUniqueName = new JSONObject();
                        jaUniqueNameList.put(joUniqueName);

                        joUniqueName.put("uniqueName", bulkOfDeliveryURLData.getUniqueName());
						if (bulkOfDeliveryURLData.getEncodingProfileKey() != null
							&& bulkOfDeliveryURLData.getEncodingProfileKey() != -1)
							joUniqueName.put("encodingProfileKey", bulkOfDeliveryURLData.getEncodingProfileKey());
						else
							joUniqueName.put("encodingProfileLabel", bulkOfDeliveryURLData.getEncodingProfileLabel());

                        bulkOfDeliveryURLDataMap.put(bulkOfDeliveryURLData.getUniqueName(), bulkOfDeliveryURLData);
                    }
                    else if (bulkOfDeliveryURLData.getLiveIngestionJobKey() != null)
                    {
                        JSONObject joLiveIngestionJobKey = new JSONObject();
                        jaLiveIngestionJobKeyList.put(joLiveIngestionJobKey);

                        joLiveIngestionJobKey.put("ingestionJobKey", bulkOfDeliveryURLData.getLiveIngestionJobKey());
                        if (bulkOfDeliveryURLData.getLiveDeliveryCode() != null)
                            joLiveIngestionJobKey.put("deliveryCode", bulkOfDeliveryURLData.getLiveDeliveryCode());

                        liveBulkOfDeliveryURLDataMap.put(bulkOfDeliveryURLData.getLiveIngestionJobKey(), bulkOfDeliveryURLData);
                    }
                }
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/delivery/bulk"
                    + "?ttlInSeconds=" + ttlInSeconds
                    + "&maxRetries=" + maxRetries
                    + "&authorizationThroughPath=" + (authorizationThroughPath != null ? authorizationThroughPath : "true")
            ;
            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            String postContentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, postContentType,
                    timeoutInSeconds, maxRetriesNumber,
                    username, password, null,
                    joDeliveryAuthorizationDetails.toString());
            mLogger.info("getBulkOfDeliveryURL. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        JSONObject joDeliveryURLList;
        try
        {
            joDeliveryURLList = new JSONObject(mmsInfo);

            /*
            {
                "uniqueNameList" : [
                    {
                        "uniqueName": "...",
                        "encodingProfileKey": 123,
                        "deliveryURL", "..."
                    },
                    ...
                ],
                "liveIngestionJobKeyList" : [
                    {
                        "ingestionJobKey": 1234,
                        "deliveryCode": 1234,
                        "deliveryURL", "..."
                    },
                    ...
                ]
             }
             */

            if (joDeliveryURLList.has("uniqueNameList"))
            {
                JSONArray jaUniqueNameList = joDeliveryURLList.getJSONArray("uniqueNameList");
                for (int uniqueNameIndex = 0; uniqueNameIndex < jaUniqueNameList.length(); uniqueNameIndex++)
                {
                    JSONObject joUniqueName = jaUniqueNameList.getJSONObject(uniqueNameIndex);

                    if (joUniqueName.has("uniqueName") && joUniqueName.has("deliveryURL")
                        && !joUniqueName.isNull("uniqueName") && !joUniqueName.isNull("deliveryURL"))
                    {
                        BulkOfDeliveryURLData bulkOfDeliveryURLData
                                = bulkOfDeliveryURLDataMap.get(joUniqueName.getString("uniqueName"));
                        bulkOfDeliveryURLData.setDeliveryURL(joUniqueName.getString("deliveryURL"));
                    }
                }
            }

            if (joDeliveryURLList.has("liveIngestionJobKeyList"))
            {
                JSONArray jaLiveIngestionJobKeyList = joDeliveryURLList.getJSONArray("liveIngestionJobKeyList");
                for (int liveIngestionJobKeyIndex = 0; liveIngestionJobKeyIndex < jaLiveIngestionJobKeyList.length(); liveIngestionJobKeyIndex++)
                {
                    JSONObject joLiveIngestionJobKey = jaLiveIngestionJobKeyList.getJSONObject(liveIngestionJobKeyIndex);

                    if (joLiveIngestionJobKey.has("ingestionJobKey") && joLiveIngestionJobKey.has("deliveryURL")
                            && !joLiveIngestionJobKey.isNull("ingestionJobKey") && !joLiveIngestionJobKey.isNull("deliveryURL"))
                    {
                        BulkOfDeliveryURLData bulkOfDeliveryURLData
                                = liveBulkOfDeliveryURLDataMap.get(joLiveIngestionJobKey.getLong("ingestionJobKey"));
                        bulkOfDeliveryURLData.setDeliveryURL(joLiveIngestionJobKey.getString("deliveryURL"));
                    }
                }
            }
        }
        catch (Exception e)
        {
            String errorMessage = "deliveryURLList processing failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public String getVODDeliveryURL(String username, String password,

                                    // first option (encodingProfileKey or encodingProfileLabel)
                                    Long mediaItemKey, String uniqueName, Long encodingProfileKey, String encodingProfileLabel,

                                    // second option
                                    PhysicalPath physicalPath,
                                 long ttlInSeconds, int maxRetries, Boolean save)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            if (physicalPath == null
                    && ((mediaItemKey == null && uniqueName == null)
                        || (encodingProfileKey == null && (encodingProfileLabel == null || encodingProfileLabel.isEmpty())))
            )
            {
                String errorMessage = "physicalPath or (mediaItemKey-uniqueName)/encodingProfileKey have to be present";
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            String mmsURL;

            if (physicalPath != null)
            {
                if (physicalPath.getDeliveryTechnology() != null
                        && physicalPath.getDeliveryTechnology().equalsIgnoreCase("HTTPStreaming"))
                {
                    ttlInSeconds = 3600 * 24;
                    maxRetries = 3600 * 24 / 5;
                }

                mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                        + "/catramms/1.0.1/delivery/vod/" + physicalPath.getPhysicalPathKey()
                        + "?ttlInSeconds=" + ttlInSeconds
                        + "&maxRetries=" + maxRetries
                        + "&save=" + save.toString()
                        + "&authorizationThroughPath=" + (authorizationThroughPath != null ? authorizationThroughPath : "true")
                        + "&redirect=false"
                ;
            }
            else // if (mediaItemKey != null && encodingProfileKey-encodingProfileLabel != null)
            {
                // in this case mediaItemKey or uniqueName has to be valid and encodingProfileKey-encodingProfileLabel has to be valid
                if (mediaItemKey == null)
                {
                    if (encodingProfileKey == null)
                    {
                        mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                                + "/catramms/1.0.1/delivery/vod/0/0"
                                + "?uniqueName=" + java.net.URLEncoder.encode(uniqueName, "UTF-8") // requires unescape server side
                                + "&encodingProfileLabel=" + java.net.URLEncoder.encode(encodingProfileLabel, "UTF-8") // requires unescape server side
                                + "&ttlInSeconds=" + ttlInSeconds
                                + "&maxRetries=" + maxRetries
                                + "&save=" + save.toString()
                                + "&authorizationThroughPath=" + (authorizationThroughPath != null ? authorizationThroughPath : "true")
                                + "&redirect=false"
                        ;
                    }
                    else
                    {
                        mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                                + "/catramms/1.0.1/delivery/vod/0/" + encodingProfileKey
                                + "?uniqueName=" + java.net.URLEncoder.encode(uniqueName, "UTF-8") // requires unescape server side
                                + "&ttlInSeconds=" + ttlInSeconds
                                + "&maxRetries=" + maxRetries
                                + "&save=" + save.toString()
                                + "&authorizationThroughPath=" + (authorizationThroughPath != null ? authorizationThroughPath : "true")
                                + "&redirect=false"
                        ;
                    }
                }
                else
                {
                    if (encodingProfileKey == null)
                    {
                        mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                            + "/catramms/1.0.1/delivery/vod/" + mediaItemKey + "/0"
                            + "?encodingProfileLabel=" + java.net.URLEncoder.encode(encodingProfileLabel, "UTF-8") // requires unescape server side
                            + "&ttlInSeconds=" + ttlInSeconds
                            + "&maxRetries=" + maxRetries
                            + "&save=" + save.toString()
                            + "&authorizationThroughPath=" + (authorizationThroughPath != null ? authorizationThroughPath : "true")
                            + "&redirect=false"
                        ;
                    }
                    else
                    {
                        mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                                + "/catramms/1.0.1/delivery/vod/" + mediaItemKey + "/" + encodingProfileKey
                                + "?ttlInSeconds=" + ttlInSeconds
                                + "&maxRetries=" + maxRetries
                                + "&save=" + save.toString()
                                + "&authorizationThroughPath=" + (authorizationThroughPath != null ? authorizationThroughPath : "true")
                                + "&redirect=false"
                        ;
                    }
                }
            }

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getVODDeliveryURL. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        String deliveryURL;
        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);

            deliveryURL = joMMSInfo.getString("deliveryURL");
        }
        catch (Exception e)
        {
            String errorMessage = "getDeliveryURL failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return deliveryURL;
    }

    public String getLiveDeliveryURL(String username, String password,
                                    Long ingestionJobKey,
                                    Long ttlInSeconds, // if null -> 3600 * 24
                                    Long maxRetries    // if null -> 3600 * 24 / 5
    )
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            if (ingestionJobKey == null)
            {
                String errorMessage = "ingestionJobKey have to be present";
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            String mmsURL;

            long lTtlInSeconds;
            long lMaxRetries;

            if(ttlInSeconds == null)
                lTtlInSeconds = 3600 * 24;
            else
                lTtlInSeconds = ttlInSeconds;

            if(maxRetries == null)
                lMaxRetries = 3600 * 24 / 5;
            else
                lMaxRetries = maxRetries;

            mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/delivery/live/" + ingestionJobKey
                    + "?ttlInSeconds=" + lTtlInSeconds
                    + "&maxRetries=" + lMaxRetries
                    + "&authorizationThroughPath=" + (authorizationThroughPath != null ? authorizationThroughPath : "true")
                    + "&redirect=false"
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getLiveDeliveryURL. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        String deliveryURL;
        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);

            deliveryURL = joMMSInfo.getString("deliveryURL");
        }
        catch (Exception e)
        {
            String errorMessage = "getDeliveryURL failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return deliveryURL;
    }

    /*
    public void ingestBinaryContent(String username, String password,
                                    File binaryPathName, Long ingestionJobKey)
            throws Exception
    {
        try
        {
            String mmsURL = mmsBinaryProtocol + "://" + mmsBinaryHostName + ":" + mmsBinaryPort
                    + "/catramms/1.0.1/binary/" + ingestionJobKey;

            mLogger.info("ingestBinaryContentAndRemoveLocalFile"
                            + ", mmsURL: " + mmsURL
                            + ", binaryPathName: " + binaryPathName
            );

            Date now = new Date();
            HttpFeedFetcher.fetchPostHttpBinary(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, binaryPathName);
            mLogger.info("ingestBinaryContent. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "ingestWorkflow MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }
    */

    public void ingestBinaryContent(String username, String password,
                                    InputStream fileInputStream, long contentSize,
                                    Long ingestionJobKey)
            throws Exception
    {
        try
        {
            String mmsURL = mmsBinaryProtocol + "://" + mmsBinaryHostName + ":" + mmsBinaryPort
                    + "/catramms/1.0.1/binary/" + ingestionJobKey;

            mLogger.info("ingestBinaryContent"
                            + ", mmsURL: " + mmsURL
                            + ", contentSize: " + contentSize
                            + ", ingestionJobKey: " + ingestionJobKey
            );

            Date now = new Date();
            HttpFeedFetcher.fetchPostHttpBinary(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, fileInputStream, contentSize);
            mLogger.info("ingestBinaryContent. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "ingestWorkflow MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public Long getWorkflowsLibrary(String username, String password,
                                    List<WorkflowLibrary> mmsWorkflowLibraryList,
                                    List<WorkflowLibrary> userWorkflowLibraryList
    ) throws Exception
    {
        Long numFound;

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/workflowAsLibrary"
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getWorkflowsLibrary. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
            // mLogger.info("mmsInfo: " + mmsInfo);
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            numFound = joResponse.getLong("numFound");

            JSONArray jaWorkflowsLibrary = joResponse.getJSONArray("workflowsLibrary");

            mLogger.info("jaWorkflowsLibrary.length(): " + jaWorkflowsLibrary.length()
            );

            mmsWorkflowLibraryList.clear();
            userWorkflowLibraryList.clear();

            for (int workflowIndex = 0;
                 workflowIndex < jaWorkflowsLibrary.length();
                 workflowIndex++)
            {
                WorkflowLibrary workflowLibrary = new WorkflowLibrary();

                JSONObject workflowLibraryInfo = jaWorkflowsLibrary.getJSONObject(workflowIndex);

                mLogger.info("Parsing workflowLibraryInfo: " + workflowLibraryInfo.toString(4));
                fillWorkflowLibrary(workflowLibrary, workflowLibraryInfo);

                if (workflowLibrary.getGlobal())
                    mmsWorkflowLibraryList.add(workflowLibrary);
                else
                    userWorkflowLibraryList.add(workflowLibrary);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing workflowLibrary failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return numFound;
    }

    public String getWorkflowLibraryContent(String username, String password,
                                          Long workflowLibraryKey
    ) throws Exception
    {
        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/workflowAsLibrary/" + workflowLibraryKey
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getWorkflowLibraryContent. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
            // mLogger.info("mmsInfo: " + mmsInfo);
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return mmsInfo;
    }

    public void saveWorkflowAsLibrary(String username, String password,
                                      String workflowAsLibraryScope, String workflowAsLibrary
    ) throws Exception
    {

        String mmsInfo;

        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/workflowAsLibrary"
                    + "?scope=" + workflowAsLibraryScope
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, workflowAsLibrary);
            mLogger.info("saveWorkflowAsLibrary. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
            // mLogger.info("mmsInfo: " + mmsInfo);
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void removeWorkflowAsLibrary(String username, String password,
                                        String workflowAsLibraryScope, Long workflowLibraryKey
    ) throws Exception
    {

        String mmsInfo;

        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/workflowAsLibrary/" + workflowLibraryKey
                    + "?scope=" + workflowAsLibraryScope
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null);
            mLogger.info("removeWorkflowAsLibrary. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
            // mLogger.info("mmsInfo: " + mmsInfo);
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void addYouTubeConf(String username, String password,
                               String label, String refreshToken)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonYouTubeConf;
            {
                JSONObject joYouTubeConf = new JSONObject();

                joYouTubeConf.put("Label", label);
                joYouTubeConf.put("RefreshToken", refreshToken);

                jsonYouTubeConf = joYouTubeConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/youtube";

            mLogger.info("addYouTubeConf"
                            + ", mmsURL: " + mmsURL
                            + ", jsonYouTubeConf: " + jsonYouTubeConf
            );

            Date now = new Date();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonYouTubeConf);
            mLogger.info("addYouTubeConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addYouTubeConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void modifyYouTubeConf(String username, String password,
                               Long confKey, String label, String refreshToken)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonYouTubeConf;
            {
                JSONObject joYouTubeConf = new JSONObject();

                joYouTubeConf.put("Label", label);
                joYouTubeConf.put("RefreshToken", refreshToken);

                jsonYouTubeConf = joYouTubeConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/youtube/" + confKey;

            mLogger.info("modifyYouTubeConf"
                    + ", mmsURL: " + mmsURL
                    + ", jsonYouTubeConf: " + jsonYouTubeConf
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, jsonYouTubeConf);
            mLogger.info("modifyYouTubeConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "modifyYouTubeConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void removeYouTubeConf(String username, String password,
                                        Long confKey)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/youtube/" + confKey;

            mLogger.info("removeYouTubeConf"
                            + ", mmsURL: " + mmsURL
                            + ", confKey: " + confKey
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null);
            mLogger.info("removeYouTubeConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeYouTubeConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public List<YouTubeConf> getYouTubeConf(String username, String password)
            throws Exception
    {
        List<YouTubeConf> youTubeConfList = new ArrayList<>();

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/youtube";

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getYouTubeConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            JSONArray jaYouTubeConf = joResponse.getJSONArray("youTubeConf");

            mLogger.info("jaYouTubeConf.length(): " + jaYouTubeConf.length());

            youTubeConfList.clear();

            for (int youTubeConfIndex = 0;
                 youTubeConfIndex < jaYouTubeConf.length();
                 youTubeConfIndex++)
            {
                YouTubeConf youTubeConf = new YouTubeConf();

                JSONObject youTubeConfInfo = jaYouTubeConf.getJSONObject(youTubeConfIndex);

                fillYouTubeConf(youTubeConf, youTubeConfInfo);

                youTubeConfList.add(youTubeConf);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing youTubeConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return youTubeConfList;
    }

    public void addFacebookConf(String username, String password,
                               String label, String pageToken)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonFacebookConf;
            {
                JSONObject joFacebookConf = new JSONObject();

                joFacebookConf.put("Label", label);
                joFacebookConf.put("PageToken", pageToken);

                jsonFacebookConf = joFacebookConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/facebook";

            mLogger.info("addFacebookConf"
                    + ", mmsURL: " + mmsURL
                    + ", jsonFacebookConf: " + jsonFacebookConf
            );

            Date now = new Date();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonFacebookConf);
            mLogger.info("addFacebookConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addFacebookConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void modifyFacebookConf(String username, String password,
                                  Long confKey, String label, String pageToken)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonFacebookConf;
            {
                JSONObject joFacebookConf = new JSONObject();

                joFacebookConf.put("Label", label);
                joFacebookConf.put("PageToken", pageToken);

                jsonFacebookConf = joFacebookConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/facebook/" + confKey;

            mLogger.info("modifyFacebookConf"
                    + ", mmsURL: " + mmsURL
                    + ", jsonFacebookConf: " + jsonFacebookConf
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, jsonFacebookConf);
            mLogger.info("modifyFacebookConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "modifyFacebookConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void removeFacebookConf(String username, String password,
                                  Long confKey)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/facebook/" + confKey;

            mLogger.info("removeFacebookConf"
                    + ", mmsURL: " + mmsURL
                    + ", confKey: " + confKey
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null);
            mLogger.info("removeFacebookConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeFacebookConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public List<FacebookConf> getFacebookConf(String username, String password)
            throws Exception
    {
        List<FacebookConf> facebookConfList = new ArrayList<>();

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/facebook";

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getFacebookConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            JSONArray jaFacebookConf = joResponse.getJSONArray("facebookConf");

            mLogger.info("jaFacebookConf.length(): " + jaFacebookConf.length());

            facebookConfList.clear();

            for (int facebookConfIndex = 0;
                 facebookConfIndex < jaFacebookConf.length();
                 facebookConfIndex++)
            {
                FacebookConf facebookConf = new FacebookConf();

                JSONObject facebookConfInfo = jaFacebookConf.getJSONObject(facebookConfIndex);

                fillFacebookConf(facebookConf, facebookConfInfo);

                facebookConfList.add(facebookConf);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing facebookConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return facebookConfList;
    }

    public Long addIPChannelConf(String username, String password,
                               String label, String url, String type, String description,
                               String name, String region, String country,
                               Long imageMediaItemKey, String imageUniqueName, Long position,
                               String channelData)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonChannelConf;
            {
                JSONObject joChannelConf = new JSONObject();

                joChannelConf.put("Label", label);
                joChannelConf.put("Url", url);
                if (type != null)
                    joChannelConf.put("Type", type);
                if (description != null)
                    joChannelConf.put("Description", description);
                if (name != null)
                    joChannelConf.put("Name", name);
                if (region != null)
                    joChannelConf.put("Region", region);
                if (country != null)
                    joChannelConf.put("Country", country);
                if (imageMediaItemKey != null)
                    joChannelConf.put("ImageMediaItemKey", imageMediaItemKey);
                if (imageUniqueName != null)
                    joChannelConf.put("ImageUniqueName", imageUniqueName);
                if (position != null)
                    joChannelConf.put("Position", position);
                if (channelData != null && !channelData.isEmpty())
                    joChannelConf.put("ChannelData", new JSONObject(channelData));

                jsonChannelConf = joChannelConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/ipChannel";

            mLogger.info("addIPChannelConf"
                    + ", mmsURL: " + mmsURL
                    + ", jsonChannelConf: " + jsonChannelConf
            );

            Date now = new Date();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonChannelConf);
            mLogger.info("addIPChannelConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addChannelConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        Long confKey;
        try {
            JSONObject jsonObject = new JSONObject(mmsInfo);

            confKey = jsonObject.getLong("confKey");
        }
        catch (Exception e)
        {
            String errorMessage = "retrieving confKey failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return confKey;
    }

    public void modifyIPChannelConf(String username, String password,
                                  Long confKey, String label, String url, String type, String description,
                                  String name, String region, String country,
                                  Long imageMediaItemKey, String imageUniqueName, Long position,
                                  String channelData)
            throws Exception
    {

        String mmsInfo;
        try
        {
            mLogger.info("modifyChannelConf"
                    + ", username: " + username
                    + ", label: " + label
                    + ", url: " + url
                    + ", type: " + type
                    + ", description: " + description
                    + ", name: " + name
                    + ", region: " + region
                    + ", country: " + country
                    + ", imageMediaItemKey: " + imageMediaItemKey
                    + ", imageUniqueName: " + imageUniqueName
                    + ", position: " + position
                    + ", channelData: " + channelData
            );

            String jsonChannelConf;
            {
                JSONObject joChannelConf = new JSONObject();

                joChannelConf.put("Label", label);
                joChannelConf.put("Url", url);
                if (type != null)
                    joChannelConf.put("Type", type);
                if (description != null)
                    joChannelConf.put("Description", description);
                if (name != null)
                    joChannelConf.put("Name", name);
                if (region != null)
                    joChannelConf.put("Region", region);
                if (country != null)
                    joChannelConf.put("Country", country);
                if (imageMediaItemKey != null)
                    joChannelConf.put("ImageMediaItemKey", imageMediaItemKey);
                if (imageUniqueName != null)
                    joChannelConf.put("ImageUniqueName", imageUniqueName);
                if (position != null)
                    joChannelConf.put("Position", position);
                if (channelData != null && !channelData.isEmpty())
                    joChannelConf.put("ChannelData", new JSONObject(channelData));

                jsonChannelConf = joChannelConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/ipChannel/" + confKey;

            mLogger.info("modifyIPChannelConf"
                    + ", mmsURL: " + mmsURL
                    + ", jsonChannelConf: " + jsonChannelConf
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, jsonChannelConf);
            mLogger.info("modifyIPChannelConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "modifyChannelConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void removeIPChannelConf(String username, String password,
                                   Long confKey)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/ipChannel/" + confKey;

            mLogger.info("removeIPChannelConf"
                    + ", mmsURL: " + mmsURL
                    + ", confKey: " + confKey
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null);
            mLogger.info("removeIPChannelConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeChannelConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public Long getIPChannelConf(String username, String password,
                               long startIndex, long pageSize,
                               Long confKey, String label,
                               String url,
                               String type, String name, String region, String country,
                               String labelOrder,   // asc or desc
                               List<IPChannelConf> channelConfList)
            throws Exception
    {
        Long numFound;

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/ipChannel"
                    + (confKey == null ? "" : ("/" + confKey))
                    + "?start=" + startIndex
                    + "&rows=" + pageSize
                    + "&label=" + (label == null ? "" : java.net.URLEncoder.encode(label, "UTF-8")) // requires unescape server side
                    + "&url=" + (url == null ? "" : java.net.URLEncoder.encode(url, "UTF-8"))
                    + "&type=" + (type == null ? "" : java.net.URLEncoder.encode(type, "UTF-8"))
                    + "&name=" + (name == null ? "" : java.net.URLEncoder.encode(name, "UTF-8"))
                    + "&region=" + (region == null ? "" : java.net.URLEncoder.encode(region, "UTF-8"))
                    + "&country=" + (country == null ? "" : java.net.URLEncoder.encode(country, "UTF-8"))
                    + "&labelOrder=" + (labelOrder == null ? "" : labelOrder)
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getIPChannelConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
            // mLogger.info("mmsInfo: " + mmsInfo);
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            numFound = joResponse.getLong("numFound");

            JSONArray jaChannelConf = joResponse.getJSONArray("channelConf");

            mLogger.info("jaChannelConf.length(): " + jaChannelConf.length()
            );

            channelConfList.clear();

            for (int channelConfIndex = 0;
                 channelConfIndex < jaChannelConf.length();
                 channelConfIndex++)
            {
                IPChannelConf ipChannelConf = new IPChannelConf();

                JSONObject channelConfInfo = jaChannelConf.getJSONObject(channelConfIndex);

                fillIPChannelConf(ipChannelConf, channelConfInfo);

                channelConfList.add(ipChannelConf);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing channelConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return numFound;
    }

    public IPChannelConf getIPChannelConf(String username, String password,
                               Long confKey)
            throws Exception
    {
        IPChannelConf channelConf = null;

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/ipChannel"
                    + "/" + confKey
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getIPChannelConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
            // mLogger.info("mmsInfo: " + mmsInfo);
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            Long numFound = joResponse.getLong("numFound");

            JSONArray jaChannelConf = joResponse.getJSONArray("channelConf");

            mLogger.info("jaChannelConf.length(): " + jaChannelConf.length()
            );

            if (numFound > 1 || jaChannelConf.length() > 1)
            {
                String errorMessage = "Wrong API response"
                        + ", numFound: " + numFound
                        + ", jaChannelConf.length: " + jaChannelConf.length()
                        ;
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            if (jaChannelConf.length() == 1)
            {
                channelConf = new IPChannelConf();

                JSONObject channelConfInfo = jaChannelConf.getJSONObject(0);

                fillIPChannelConf(channelConf, channelConfInfo);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing channelConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return channelConf;
    }

    public Long addSATChannelConf(String username, String password, String label,
                                 Long sourceSATConfKey, String region, String country,
                                 Long imageMediaItemKey, String imageUniqueName, Long position,
                                 String channelData)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonChannelConf;
            {
                JSONObject joChannelConf = new JSONObject();

                joChannelConf.put("Label", label);
                joChannelConf.put("SourceSATConfKey", sourceSATConfKey);
                if (region != null)
                    joChannelConf.put("Region", region);
                if (country != null)
                    joChannelConf.put("Country", country);
                if (imageMediaItemKey != null)
                    joChannelConf.put("ImageMediaItemKey", imageMediaItemKey);
                if (imageUniqueName != null)
                    joChannelConf.put("ImageUniqueName", imageUniqueName);
                if (position != null)
                    joChannelConf.put("Position", position);
                if (channelData != null && !channelData.isEmpty())
                    joChannelConf.put("ChannelData", new JSONObject(channelData));

                jsonChannelConf = joChannelConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/satChannel";

            mLogger.info("addSATChannelConf"
                    + ", mmsURL: " + mmsURL
                    + ", jsonChannelConf: " + jsonChannelConf
            );

            Date now = new Date();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonChannelConf);
            mLogger.info("addSATChannelConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addChannelConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        Long confKey;
        try {
            JSONObject jsonObject = new JSONObject(mmsInfo);

            confKey = jsonObject.getLong("confKey");
        }
        catch (Exception e)
        {
            String errorMessage = "retrieving confKey failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return confKey;
    }

    public void modifySATChannelConf(String username, String password,
                                    Long confKey, String label, Long sourceSATConfKey, String region, String country,
                                    Long imageMediaItemKey, String imageUniqueName, Long position,
                                    String channelData)
            throws Exception
    {

        String mmsInfo;
        try
        {
            mLogger.info("modifyChannelConf"
                    + ", username: " + username
                    + ", label: " + label
                    + ", sourceSATConfKey: " + sourceSATConfKey
                    + ", region: " + region
                    + ", country: " + country
                    + ", imageMediaItemKey: " + imageMediaItemKey
                    + ", imageUniqueName: " + imageUniqueName
                    + ", position: " + position
                    + ", channelData: " + channelData
            );

            String jsonChannelConf;
            {
                JSONObject joChannelConf = new JSONObject();

                joChannelConf.put("Label", label);
                joChannelConf.put("SourceSATConfKey", sourceSATConfKey);
                if (region != null)
                    joChannelConf.put("Region", region);
                if (country != null)
                    joChannelConf.put("Country", country);
                if (imageMediaItemKey != null)
                    joChannelConf.put("ImageMediaItemKey", imageMediaItemKey);
                if (imageUniqueName != null)
                    joChannelConf.put("ImageUniqueName", imageUniqueName);
                if (position != null)
                    joChannelConf.put("Position", position);
                if (channelData != null && !channelData.isEmpty())
                    joChannelConf.put("ChannelData", new JSONObject(channelData));

                jsonChannelConf = joChannelConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/satChannel/" + confKey;

            mLogger.info("modifyIPChannelConf"
                    + ", mmsURL: " + mmsURL
                    + ", jsonChannelConf: " + jsonChannelConf
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, jsonChannelConf);
            mLogger.info("modifySATChannelConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "modifyChannelConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void removeSATChannelConf(String username, String password,
                                    Long confKey)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/satChannel/" + confKey;

            mLogger.info("removeSATChannelConf"
                    + ", mmsURL: " + mmsURL
                    + ", confKey: " + confKey
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null);
            mLogger.info("removeSATChannelConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeChannelConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public Long getSATChannelConf(String username, String password,
                                 long startIndex, long pageSize,
                                 Long confKey,
                                 String label, String region, String country,
                                  String labelOrder,   // asc or desc
                                 List<SATChannelConf> channelConfList)
            throws Exception
    {
        Long numFound;

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/satChannel"
                    + (confKey == null ? "" : ("/" + confKey))
                    + "?start=" + startIndex
                    + "&rows=" + pageSize
                    + (label == null || label.isEmpty() ? "" : ("&label=" + java.net.URLEncoder.encode(label, "UTF-8"))) // requires unescape server side
                    + (region == null || region.isEmpty() ? "" : ("&region=" + java.net.URLEncoder.encode(region, "UTF-8")))
                    + (region == null || region.isEmpty() ? "" : ("&country=" + java.net.URLEncoder.encode(country, "UTF-8")))
                    + (labelOrder == null || labelOrder.isEmpty() ? "" : ("&labelOrder=" + labelOrder))
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getSATChannelConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
            // mLogger.info("mmsInfo: " + mmsInfo);
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            numFound = joResponse.getLong("numFound");

            JSONArray jaChannelConf = joResponse.getJSONArray("channelConf");

            mLogger.info("jaChannelConf.length(): " + jaChannelConf.length()
            );

            channelConfList.clear();

            for (int channelConfIndex = 0;
                 channelConfIndex < jaChannelConf.length();
                 channelConfIndex++)
            {
                SATChannelConf satChannelConf = new SATChannelConf();

                JSONObject channelConfInfo = jaChannelConf.getJSONObject(channelConfIndex);

                fillSATChannelConf(satChannelConf, channelConfInfo);

                channelConfList.add(satChannelConf);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing channelConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return numFound;
    }

    public SATChannelConf getSATChannelConf(String username, String password,
                                          Long confKey)
            throws Exception
    {
        SATChannelConf channelConf = null;

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/satChannel"
                    + "/" + confKey
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getSATChannelConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
            // mLogger.info("mmsInfo: " + mmsInfo);
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            Long numFound = joResponse.getLong("numFound");

            JSONArray jaChannelConf = joResponse.getJSONArray("channelConf");

            mLogger.info("jaChannelConf.length(): " + jaChannelConf.length()
            );

            if (numFound > 1 || jaChannelConf.length() > 1)
            {
                String errorMessage = "Wrong API response"
                        + ", numFound: " + numFound
                        + ", jaChannelConf.length: " + jaChannelConf.length()
                        ;
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            if (jaChannelConf.length() == 1)
            {
                channelConf = new SATChannelConf();

                JSONObject channelConfInfo = jaChannelConf.getJSONObject(0);

                fillSATChannelConf(channelConf, channelConfInfo);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing channelConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return channelConf;
    }

    public Long addSourceSATChannelConf(String username, String password,
                                        Long serviceId, Long networkId, Long transportStreamId,
                                        String name, String satellite, Long frequency, String lnb,
                                        Long videoPid, String audioPids, Long audioItalianPid, Long audioEnglishPid, Long teletextPid,
                                        String modulation, String polarization, Long symbolRate, String country, String deliverySystem)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonChannelConf;
            {
                JSONObject joChannelConf = new JSONObject();

                joChannelConf.put("ServiceId", serviceId);
                joChannelConf.put("NetworkId", networkId);
                joChannelConf.put("TransportStreamId", transportStreamId);
                joChannelConf.put("Name", name);
                joChannelConf.put("Satellite", satellite);
                joChannelConf.put("Frequency", frequency);
                joChannelConf.put("Lnb", lnb);
                joChannelConf.put("VideoPid", videoPid);
                joChannelConf.put("AudioPids", audioPids);
                if (audioItalianPid != null)
                    joChannelConf.put("AudioItalianPid", audioItalianPid);
                if (audioEnglishPid != null)
                    joChannelConf.put("AudioEnglishPid", audioEnglishPid);
                if (teletextPid != null)
                    joChannelConf.put("TeletextPid", teletextPid);
                if (modulation != null && !modulation.isEmpty())
                    joChannelConf.put("Modulation", modulation);
                joChannelConf.put("Polarization", polarization);
                joChannelConf.put("SymbolRate", symbolRate);
                if (country != null && !country.isEmpty())
                    joChannelConf.put("Country", country);
                if (deliverySystem != null && !deliverySystem.isEmpty())
                    joChannelConf.put("DeliverySystem", deliverySystem);

                jsonChannelConf = joChannelConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/sourceSatChannel";

            mLogger.info("addSourceSATChannel"
                    + ", mmsURL: " + mmsURL
                    + ", jsonChannelConf: " + jsonChannelConf
            );

            Date now = new Date();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonChannelConf);
            mLogger.info("addSourceSATChannelConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addChannel MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        Long confKey;
        try {
            JSONObject jsonObject = new JSONObject(mmsInfo);

            confKey = jsonObject.getLong("confKey");
        }
        catch (Exception e)
        {
            String errorMessage = "retrieving serviceId failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return confKey;
    }

    public void modifySourceSATChannelConf(String username, String password,
                                           Long confKey, Long serviceId, Long networkId, Long transportStreamId,
                                           String name, String satellite, Long frequency, String lnb,
                                           Long videoPid, String audioPids, Long audioItalianPid, Long audioEnglishPid, Long teletextPid,
                                           String modulation, String polarization, Long symbolRate, String country, String deliverySystem
    )
            throws Exception
    {

        String mmsInfo;
        try
        {
            mLogger.info("modifyChannelConf"
                    + ", username: " + username
                    + ", serviceId: " + serviceId
                    + ", networkId: " + networkId
                    + ", transportStreamId: " + transportStreamId
                    + ", name: " + name
                    + ", satellite: " + satellite
                    + ", frequency: " + frequency
                    + ", lnb: " + lnb
                    + ", videoPid: " + videoPid
                    + ", audioPids: " + audioPids
                    + ", audioItalianPid: " + audioItalianPid
                    + ", audioEnglishPid: " + audioEnglishPid
                    + ", teletextPid: " + teletextPid
                    + ", modulation: " + modulation
                    + ", polarization: " + polarization
                    + ", symbolRate: " + symbolRate
                    + ", country: " + country
                    + ", deliverySystem: " + deliverySystem
            );

            String jsonChannelConf;
            {
                JSONObject joChannelConf = new JSONObject();

                if (serviceId != null)
                    joChannelConf.put("ServiceId", serviceId);
                if (networkId != null)
                    joChannelConf.put("NetworkId", networkId);
                if (transportStreamId != null)
                    joChannelConf.put("TransportStreamId", transportStreamId);
                if (name != null && !name.isEmpty())
                    joChannelConf.put("Name", name);
                if (satellite != null && !satellite.isEmpty())
                    joChannelConf.put("Satellite", satellite);
                if (frequency != null)
                    joChannelConf.put("Frequency", frequency);
                if (lnb != null && !lnb.isEmpty())
                    joChannelConf.put("Lnb", lnb);
                if (videoPid != null)
                    joChannelConf.put("VideoPid", videoPid);
                if (audioPids != null && !audioPids.isEmpty())
                    joChannelConf.put("AudioPids", audioPids);
                if (audioItalianPid != null)
                    joChannelConf.put("AudioItalianPid", audioItalianPid);
                if (audioEnglishPid != null)
                    joChannelConf.put("AudioEnglishPid", audioEnglishPid);
                if (teletextPid != null)
                    joChannelConf.put("TeletextPid", teletextPid);
                if (modulation != null && !modulation.isEmpty())
                    joChannelConf.put("Modulation", modulation);
                if (polarization != null && !polarization.isEmpty())
                    joChannelConf.put("Polarization", polarization);
                if (symbolRate != null)
                    joChannelConf.put("SymbolRate", symbolRate);
                if (country != null && !country.isEmpty())
                    joChannelConf.put("Country", country);
                if (deliverySystem != null && !deliverySystem.isEmpty())
                    joChannelConf.put("DeliverySystem", deliverySystem);

                jsonChannelConf = joChannelConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/sourceSatChannel/" + confKey;

            mLogger.info("modifySourceSATChannelConf"
                    + ", mmsURL: " + mmsURL
                    + ", jsonChannelConf: " + jsonChannelConf
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, jsonChannelConf);
            mLogger.info("modifySourceSATChannelConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "modifyChannelConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void removeSourceSATChannelConf(String username, String password,
                                     Long confKey)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/sourceSatChannel/" + confKey;

            mLogger.info("removeSATChannelConf"
                    + ", mmsURL: " + mmsURL
                    + ", confKey: " + confKey
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null);
            mLogger.info("removeSourceSATChannelConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeChannelConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public Long getSourceSATChannelConf(String username, String password,
                                  long startIndex, long pageSize,
                                  Long confKey,
                                  Long serviceId, String name, Long frequency, String lnb,
                                  Long videoPid, String audioPids,
                                  String nameOrder,   // asc or desc
                                  List<SourceSATChannelConf> channelConfList)
            throws Exception
    {
        Long numFound;

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/sourceSatChannel"
                    + (confKey == null ? "" : ("/" + confKey))
                    + "?start=" + startIndex
                    + "&rows=" + pageSize
                    + (serviceId == null  ? "" : ("&serviceId=" + serviceId))
                    + (name == null || name.isEmpty() ? "" : ("&name=" + java.net.URLEncoder.encode(name, "UTF-8"))) // requires unescape server side
                    + (frequency == null  ? "" : ("&frequency=" + frequency))
                    + (lnb == null || lnb.isEmpty() ? "" : ("&lnb=" + java.net.URLEncoder.encode(lnb, "UTF-8"))) // requires unescape server side
                    + (videoPid == null  ? "" : ("&videoPid=" + videoPid))
                    + (audioPids == null || audioPids.isEmpty() ? "" : ("&audioPids=" + java.net.URLEncoder.encode(audioPids, "UTF-8")))
                    + (nameOrder == null || nameOrder.isEmpty() ? "" : ("&nameOrder=" + nameOrder))
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getSourceSATChannelConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
            // mLogger.info("mmsInfo: " + mmsInfo);
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            numFound = joResponse.getLong("numFound");

            JSONArray jaChannelConf = joResponse.getJSONArray("channelConf");

            mLogger.info("jaChannelConf.length(): " + jaChannelConf.length()
            );

            channelConfList.clear();

            for (int channelConfIndex = 0;
                 channelConfIndex < jaChannelConf.length();
                 channelConfIndex++)
            {
                SourceSATChannelConf sourceSatChannelConf = new SourceSATChannelConf();

                JSONObject channelConfInfo = jaChannelConf.getJSONObject(channelConfIndex);

                fillSourceSATChannelConf(sourceSatChannelConf, channelConfInfo);

                channelConfList.add(sourceSatChannelConf);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing channelConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return numFound;
    }

    public SourceSATChannelConf getSourceSATChannelConf(String username, String password,
                                            Long serviceId)
            throws Exception
    {
        SourceSATChannelConf channelConf = null;

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/sourceSatChannel"
                    + "/" + serviceId
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getSourceSATChannelConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
            // mLogger.info("mmsInfo: " + mmsInfo);
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            Long numFound = joResponse.getLong("numFound");

            JSONArray jaChannelConf = joResponse.getJSONArray("channelConf");

            mLogger.info("jaChannelConf.length(): " + jaChannelConf.length()
            );

            if (numFound > 1 || jaChannelConf.length() > 1)
            {
                String errorMessage = "Wrong API response"
                        + ", numFound: " + numFound
                        + ", jaChannelConf.length: " + jaChannelConf.length()
                        ;
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            if (jaChannelConf.length() == 1)
            {
                channelConf = new SourceSATChannelConf();

                JSONObject channelConfInfo = jaChannelConf.getJSONObject(0);

                fillSourceSATChannelConf(channelConf, channelConfInfo);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing channelConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return channelConf;
    }

    public void addFTPConf(String username, String password,
                               String label, String ftpServer,
                           Long ftpPort, String ftpUserName, String ftpPassword,
                           String ftpRemoteDirectory)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonFTPConf;
            {
                JSONObject joFTPConf = new JSONObject();

                joFTPConf.put("Label", label);
                joFTPConf.put("Server", ftpServer);
                joFTPConf.put("Port", ftpPort);
                joFTPConf.put("UserName", ftpUserName);
                joFTPConf.put("Password", ftpPassword);
                joFTPConf.put("RemoteDirectory", ftpRemoteDirectory);

                jsonFTPConf = joFTPConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/ftp";

            mLogger.info("addFTPConf"
                            + ", mmsURL: " + mmsURL
                            + ", jsonFTPConf: " + jsonFTPConf
            );

            Date now = new Date();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonFTPConf);
            mLogger.info("addFTPConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addFTPConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void modifyFTPConf(String username, String password,
                                  Long confKey, String label, String ftpServer,
                              Long ftpPort, String ftpUserName, String ftpPassword,
                              String ftpRemoteDirectory)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonFTPConf;
            {
                JSONObject joFTPConf = new JSONObject();

                joFTPConf.put("Label", label);
                joFTPConf.put("Server", ftpServer);
                joFTPConf.put("Port", ftpPort);
                joFTPConf.put("UserName", ftpUserName);
                joFTPConf.put("Password", ftpPassword);
                joFTPConf.put("RemoteDirectory", ftpRemoteDirectory);

                jsonFTPConf = joFTPConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/ftp/" + confKey;

            mLogger.info("modifyFTPConf"
                            + ", mmsURL: " + mmsURL
                            + ", jsonFTPConf: " + jsonFTPConf
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, jsonFTPConf);
            mLogger.info("modifyFTPConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "modifyFTPConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void removeFTPConf(String username, String password,
                                  Long confKey)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/ftp/" + confKey;

            mLogger.info("removeFTPConf"
                            + ", mmsURL: " + mmsURL
                            + ", confKey: " + confKey
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null);
            mLogger.info("removeFTPConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeFTPConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public List<FTPConf> getFTPConf(String username, String password)
            throws Exception
    {
        List<FTPConf> ftpConfList = new ArrayList<>();

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/ftp";

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getFTPConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            JSONArray jaFTPConf = joResponse.getJSONArray("ftpConf");

            mLogger.info("jaFTPConf.length(): " + jaFTPConf.length());

            ftpConfList.clear();

            for (int ftpConfIndex = 0;
                 ftpConfIndex < jaFTPConf.length();
                 ftpConfIndex++)
            {
                FTPConf ftpConf = new FTPConf();

                JSONObject ftpConfInfo = jaFTPConf.getJSONObject(ftpConfIndex);

                fillFTPConf(ftpConf, ftpConfInfo);

                ftpConfList.add(ftpConf);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing ftpConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return ftpConfList;
    }

    public void addEMailConf(String username, String password,
                           String label, String addresses,
                           String subject, String message)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonEMailConf;
            {
                JSONObject joEMailConf = new JSONObject();

                joEMailConf.put("Label", label);
                joEMailConf.put("Addresses", addresses);
                joEMailConf.put("Subject", subject);
                joEMailConf.put("Message", message);

                jsonEMailConf = joEMailConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/email";

            mLogger.info("addEMailConf"
                            + ", mmsURL: " + mmsURL
                            + ", jsonEMailConf: " + jsonEMailConf
            );

            Date now = new Date();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonEMailConf);
            mLogger.info("addEMailConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addEMailConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void modifyEMailConf(String username, String password,
                              Long confKey, String label, String addresses,
                                String subject, String message)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonEMailConf;
            {
                JSONObject joEMailConf = new JSONObject();

                joEMailConf.put("Label", label);
                joEMailConf.put("Addresses", addresses);
                joEMailConf.put("Subject", subject);
                joEMailConf.put("Message", message);

                jsonEMailConf = joEMailConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/email/" + confKey;

            mLogger.info("modifyEMailConf"
                            + ", mmsURL: " + mmsURL
                            + ", jsonEMailConf: " + jsonEMailConf
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, jsonEMailConf);
            mLogger.info("modifyEMailConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "modifyEMailConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void removeEMailConf(String username, String password,
                              Long confKey)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/email/" + confKey;

            mLogger.info("removeEMailConf"
                            + ", mmsURL: " + mmsURL
                            + ", confKey: " + confKey
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null);
            mLogger.info("removeEMailConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeEMailConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public List<EMailConf> getEMailConf(String username, String password)
            throws Exception
    {
        List<EMailConf> emailConfList = new ArrayList<>();

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/email";

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("getEMailConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            JSONArray jaEMailConf = joResponse.getJSONArray("emailConf");

            mLogger.info("jaEMailConf.length(): " + jaEMailConf.length());

            emailConfList.clear();

            for (int emailConfIndex = 0;
                 emailConfIndex < jaEMailConf.length();
                 emailConfIndex++)
            {
                EMailConf emailConf = new EMailConf();

                JSONObject emailConfInfo = jaEMailConf.getJSONObject(emailConfIndex);

                fillEMailConf(emailConf, emailConfInfo);

                emailConfList.add(emailConf);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing emailConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return emailConfList;
    }

    private void fillUserProfile(UserProfile userProfile, JSONObject joUserProfileInfo)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try
        {
            userProfile.setUserKey(joUserProfileInfo.getLong("userKey"));
            userProfile.setLdapEnabled(joUserProfileInfo.getBoolean("ldapEnabled"));
            userProfile.setName(joUserProfileInfo.getString("name"));
            userProfile.setCountry(joUserProfileInfo.getString("country"));
            userProfile.setEmailAddress(joUserProfileInfo.getString("eMailAddress"));
            userProfile.setCreationDate(simpleDateFormat.parse(joUserProfileInfo.getString("creationDate")));
            userProfile.setExpirationDate(simpleDateFormat.parse(joUserProfileInfo.getString("expirationDate")));
        }
        catch (Exception e)
        {
            String errorMessage = "fillUserProfile failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    /*
    private void fillWorkspaceDetailsList(List<WorkspaceDetails> workspaceDetailsList, JSONArray jaWorkspacesInfo)
            throws Exception
    {
        try
        {
            for (int workspaceIndex = 0; workspaceIndex < jaWorkspacesInfo.length(); workspaceIndex++)
            {
                JSONObject joWorkspaceInfo = jaWorkspacesInfo.getJSONObject(workspaceIndex);

                WorkspaceDetails workspaceDetails = new WorkspaceDetails();

                fillWorkspaceDetails(workspaceDetails, joWorkspaceInfo);

                workspaceDetailsList.add(workspaceDetails);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "fillWorkspaceDetailsList failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }
     */

    private void fillWorkspaceDetails(WorkspaceDetails workspaceDetails, JSONObject jaWorkspaceInfo)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try
        {
            workspaceDetails.setWorkspaceKey(jaWorkspaceInfo.getLong("workspaceKey"));
            workspaceDetails.setEnabled(jaWorkspaceInfo.getBoolean("isEnabled"));
            workspaceDetails.setName(jaWorkspaceInfo.getString("workspaceName"));
            workspaceDetails.setMaxEncodingPriority(jaWorkspaceInfo.getString("maxEncodingPriority"));
            workspaceDetails.setEncodingPeriod(jaWorkspaceInfo.getString("encodingPeriod"));
            workspaceDetails.setMaxIngestionsNumber(jaWorkspaceInfo.getLong("maxIngestionsNumber"));
            workspaceDetails.setMaxStorageInMB(jaWorkspaceInfo.getLong("maxStorageInMB"));
            workspaceDetails.setUsageInMB(jaWorkspaceInfo.getLong("workSpaceUsageInMB"));
            workspaceDetails.setLanguageCode(jaWorkspaceInfo.getString("languageCode"));
            workspaceDetails.setCreationDate(simpleDateFormat.parse(jaWorkspaceInfo.getString("creationDate")));

            if(jaWorkspaceInfo.has("userAPIKey"))
            {
                JSONObject joUserAPIKey = jaWorkspaceInfo.getJSONObject("userAPIKey");

                workspaceDetails.setApiKey(joUserAPIKey.getString("apiKey"));
                workspaceDetails.setOwner(joUserAPIKey.getBoolean("owner"));
                workspaceDetails.setDefaultWorkspace(joUserAPIKey.getBoolean("default"));
                workspaceDetails.setAdmin(joUserAPIKey.getBoolean("admin"));
                workspaceDetails.setCreateRemoveWorkspace(joUserAPIKey.getBoolean("createRemoveWorkspace"));
                workspaceDetails.setIngestWorkflow(joUserAPIKey.getBoolean("ingestWorkflow"));
                workspaceDetails.setCreateProfiles(joUserAPIKey.getBoolean("createProfiles"));
                workspaceDetails.setDeliveryAuthorization(joUserAPIKey.getBoolean("deliveryAuthorization"));
                workspaceDetails.setShareWorkspace(joUserAPIKey.getBoolean("shareWorkspace"));
                workspaceDetails.setEditMedia(joUserAPIKey.getBoolean("editMedia"));
                workspaceDetails.setEditConfiguration(joUserAPIKey.getBoolean("editConfiguration"));
                workspaceDetails.setKillEncoding(joUserAPIKey.getBoolean("killEncoding"));
                workspaceDetails.setCancelIngestionJob(joUserAPIKey.getBoolean("cancelIngestionJob"));
                workspaceDetails.setEditEncodersPool(joUserAPIKey.getBoolean("editEncodersPool"));
                workspaceDetails.setApplicationRecorder(joUserAPIKey.getBoolean("applicationRecorder"));
            }

            /*
            if(jaWorkspaceInfo.has("encoders"))
            {
                JSONArray jaEncoders = jaWorkspaceInfo.getJSONArray("encoders");

                for (int encoderIndex = 0; encoderIndex < jaEncoders.length(); encoderIndex++)
                {
                    JSONObject encoderInfo = jaEncoders.getJSONObject(encoderIndex);

                    Encoder encoder = new Encoder();

                    fillEncoder(encoder, encoderInfo);

                    workspaceDetails.getEncoderList().add(encoder);
                }
            }
             */
        }
        catch (Exception e)
        {
            String errorMessage = "fillWorkspaceDetails failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillEncodingJob(EncodingJob encodingJob, JSONObject encodingJobInfo)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        JSONObject joParameters = null;
        try
        {
            encodingJob.setEncodingJobKey(encodingJobInfo.getLong("encodingJobKey"));
            if (encodingJobInfo.has("ownedByCurrentWorkspace") && !encodingJobInfo.isNull("ownedByCurrentWorkspace"))
                encodingJob.setOwnedByCurrentWorkspace(encodingJobInfo.getBoolean("ownedByCurrentWorkspace"));
            if (!encodingJobInfo.isNull("ingestionJobKey"))
                encodingJob.setIngestionJobKey(encodingJobInfo.getLong("ingestionJobKey"));
            encodingJob.setType(encodingJobInfo.getString("type"));
            encodingJob.setStatus(encodingJobInfo.getString("status"));
            encodingJob.setEncoderKey(encodingJobInfo.getLong("encoderKey"));
            encodingJob.setEncodingPid(encodingJobInfo.getLong("encodingPid"));

            if (encodingJobInfo.isNull("processorMMS"))
                encodingJob.setProcessorMMS(null);
            else
                encodingJob.setProcessorMMS(encodingJobInfo.getString("processorMMS"));

            encodingJob.setEncodingPriority(encodingJobInfo.getString("encodingPriority"));
            encodingJob.setEncodingPriorityCode(encodingJobInfo.getInt("encodingPriorityCode"));
            encodingJob.setMaxEncodingPriorityCode(encodingJobInfo.getInt("maxEncodingPriorityCode"));

            if (encodingJobInfo.isNull("start"))
                encodingJob.setStart(null);
            else
                encodingJob.setStart(simpleDateFormat.parse(encodingJobInfo.getString("start")));
            if (encodingJobInfo.isNull("end"))
                encodingJob.setEnd(null);
            else
                encodingJob.setEnd(simpleDateFormat.parse(encodingJobInfo.getString("end")));
            if (encodingJobInfo.isNull("progress"))
                encodingJob.setProgress(null);
            else
                encodingJob.setProgress(encodingJobInfo.getLong("progress"));

            // end processing estimation
            {
                encodingJob.setEndEstimate(false);

                Date now = new Date();

                if (encodingJob.getEnd() == null
                        && encodingJob.getStart() != null && encodingJob.getStart().getTime() < now.getTime()
                        && encodingJob.getProgress() != null && encodingJob.getProgress() != 0 && encodingJob.getProgress() != -1)
                {
                    Long elapsedInMillisecs = now.getTime() - encodingJob.getStart().getTime();

                    // elapsedInMillisecs : actual percentage = X (estimateMillisecs) : 100
                    Long estimateMillisecs = elapsedInMillisecs * 100 / encodingJob.getProgress();

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(encodingJob.getStart());
                    calendar.add(Calendar.MILLISECOND, estimateMillisecs.intValue());

                    encodingJob.setEndEstimate(true);
                    encodingJob.setEnd(calendar.getTime());
                }
            }

            if (encodingJobInfo.isNull("failuresNumber"))
                encodingJob.setFailuresNumber(null);
            else
                encodingJob.setFailuresNumber(encodingJobInfo.getLong("failuresNumber"));
            if (encodingJobInfo.has("parameters") && !encodingJobInfo.isNull("parameters"))
            {
                encodingJob.setParameters(encodingJobInfo.getJSONObject("parameters").toString(4));

                joParameters = new JSONObject(encodingJob.getParameters());

                if (encodingJob.getType().equalsIgnoreCase("EncodeVideoAudio")
                        || encodingJob.getType().equalsIgnoreCase("EncodeImage"))
                {
                    encodingJob.setEncodingProfileKey(joParameters.getLong("encodingProfileKey"));
                    if (joParameters.getJSONArray("sourcesToBeEncodedRoot").length() > 0)
                        encodingJob.setSourcePhysicalPathKey(
                            joParameters.getJSONArray("sourcesToBeEncodedRoot").getJSONObject(0)
                                    .getLong("sourcePhysicalPathKey"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("OverlayImageOnVideo"))
                {
                    encodingJob.setSourceVideoPhysicalPathKey(joParameters.getLong("sourceVideoPhysicalPathKey"));
                    encodingJob.setSourceImagePhysicalPathKey(joParameters.getLong("sourceImagePhysicalPathKey"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("OverlayTextOnVideo"))
                {
                    encodingJob.setSourceVideoPhysicalPathKey(joParameters.getLong("sourceVideoPhysicalPathKey"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("GenerateFrames")
                        )
                {
                    encodingJob.setSourceVideoPhysicalPathKey(joParameters.getLong("sourceVideoPhysicalPathKey"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("FaceRecognition")
                )
                {
                    if (joParameters.has("sourceVideoPhysicalPathKey"))
                        encodingJob.setSourceVideoPhysicalPathKey(joParameters.getLong("sourceVideoPhysicalPathKey"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("LiveRecorder")
                )
                {
                    if (joParameters.has("liveURL"))    // previous one
                        encodingJob.setLiveURL(joParameters.getString("liveURL"));
                    else if (joParameters.has("url"))   // new one
                        encodingJob.setLiveURL(joParameters.getString("url"));
                    encodingJob.setOutputFileFormat(joParameters.getString("outputFileFormat"));
                    encodingJob.setSegmentDurationInSeconds(joParameters.getLong("segmentDurationInSeconds"));
                    encodingJob.setRecordingPeriodEnd(new Date(1000 * joParameters.getLong("utcRecordingPeriodEnd")));
                    encodingJob.setRecordingPeriodStart(new Date(1000 * joParameters.getLong("utcRecordingPeriodStart")));
                }
                else if (encodingJob.getType().equalsIgnoreCase("LiveGrid")
                )
                {
                    encodingJob.setInputChannels(joParameters.getJSONArray("inputChannels").toString());
                    encodingJob.setEncodingProfileKey(joParameters.getLong("encodingProfileKey"));
                    // encodingJob.setDeliveryCode(joParameters.getLong("deliveryCode"));
                    encodingJob.setSegmentDurationInSeconds(joParameters.getLong("segmentDurationInSeconds"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("LiveProxy")
                )
                {
                    if (joParameters.has("liveURL"))    // previous one
                        encodingJob.setLiveURL(joParameters.getString("liveURL"));
                    else if (joParameters.has("url"))   // new one
                        encodingJob.setLiveURL(joParameters.getString("url"));

                    if (joParameters.has("timePeriod") && joParameters.getBoolean("timePeriod"))
                    {
                        encodingJob.setProxyPeriodEnd(new Date(1000 * joParameters.getLong("utcProxyPeriodEnd")));
                        encodingJob.setProxyPeriodStart(new Date(1000 * joParameters.getLong("utcProxyPeriodStart")));
                    }

                    // Outputs
                    {
                        String outputTypes = "";
                        String segmentsDurationInSeconds = "";
                        if (joParameters.has("outputsRoot"))
                        {
                            JSONArray jaOutputsRoot = joParameters.getJSONArray("outputsRoot");
                            for (int outputIndex = 0; outputIndex < jaOutputsRoot.length(); outputIndex++)
                            {
                                JSONObject joOutput = jaOutputsRoot.getJSONObject(outputIndex);
                                outputTypes += joOutput.getString("outputType");
                                if (joOutput.has("segmentDurationInSeconds"))
                                    segmentsDurationInSeconds += joOutput.getLong("segmentDurationInSeconds");
                            }
                        }
                        encodingJob.setLiveProxyOutputTypes(outputTypes);
                        encodingJob.setLiveProxySegmentsDurationInSeconds(segmentsDurationInSeconds);
                    }
                }
                else if (encodingJob.getType().equalsIgnoreCase("VODProxy")
                )
                {
                    if (joParameters.has("timePeriod") && joParameters.getBoolean("timePeriod"))
                    {
                        encodingJob.setProxyPeriodEnd(new Date(1000 * joParameters.getLong("utcProxyPeriodEnd")));
                        encodingJob.setProxyPeriodStart(new Date(1000 * joParameters.getLong("utcProxyPeriodStart")));
                    }

                    // Outputs
                    {
                        String outputTypes = "";
                        String segmentsDurationInSeconds = "";
                        if (joParameters.has("outputsRoot"))
                        {
                            JSONArray jaOutputsRoot = joParameters.getJSONArray("outputsRoot");
                            for (int outputIndex = 0; outputIndex < jaOutputsRoot.length(); outputIndex++)
                            {
                                JSONObject joOutput = jaOutputsRoot.getJSONObject(outputIndex);
                                outputTypes += joOutput.getString("outputType");
                                if (joOutput.has("segmentDurationInSeconds"))
                                    segmentsDurationInSeconds += joOutput.getLong("segmentDurationInSeconds");
                            }
                        }
                        encodingJob.setLiveProxyOutputTypes(outputTypes);
                        encodingJob.setLiveProxySegmentsDurationInSeconds(segmentsDurationInSeconds);
                    }
                }
                else if (encodingJob.getType().equalsIgnoreCase("AwaitingTheBeginning")
                )
                {
                    encodingJob.setCountDownEnd(new Date(1000 * joParameters.getLong("utcCountDownEnd")));

                    encodingJob.setAwaitingTheBeginningOutputType(joParameters.getString("outputType"));
                    encodingJob.setAwaitingTheBeginningSegmentDurationInSeconds(joParameters.getLong("segmentDurationInSeconds"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("VideoSpeed")
                )
                {
                    encodingJob.setSourceVideoPhysicalPathKey(joParameters.getLong("sourceVideoPhysicalPathKey"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("PictureInPicture"))
                {
                    encodingJob.setMainVideoPhysicalPathKey(joParameters.getLong("mainVideoPhysicalPathKey"));
                    encodingJob.setOverlayVideoPhysicalPathKey(joParameters.getLong("overlayVideoPhysicalPathKey"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("IntroOutroOverlay"))
                {
                    encodingJob.setIntroVideoPhysicalPathKey(joParameters.getLong("introVideoPhysicalPathKey"));
                    encodingJob.setMainVideoPhysicalPathKey(joParameters.getLong("mainVideoPhysicalPathKey"));
                    encodingJob.setOutroVideoPhysicalPathKey(joParameters.getLong("outroVideoPhysicalPathKey"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("CutFrameAccurate"))
                {
                    encodingJob.setEncodingProfileKey(joParameters.getLong("encodingProfileKey"));
                    encodingJob.setSourcePhysicalPathKey(joParameters.getLong("sourceVideoPhysicalPathKey"));
                }
                else
                {
                    mLogger.error("Wrong encodingJob.getType(): " + encodingJob.getType());
                }
            }
        }
        catch (Exception e)
        {
            String errorMessage = "fillEncodingJob failed. Exception: " + e
                    + ", joParameters: " + (joParameters == null ? "null" : joParameters.toString());
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillEncoder(Encoder encoder, JSONObject encoderInfo)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try
        {
            encoder.setEncoderKey(encoderInfo.getLong("encoderKey"));
            encoder.setLabel(encoderInfo.getString("label"));
            encoder.setExternal(encoderInfo.getBoolean("external"));
            encoder.setEnabled(encoderInfo.getBoolean("enabled"));
            encoder.setProtocol(encoderInfo.getString("protocol"));
            encoder.setServerName(encoderInfo.getString("serverName"));
            encoder.setPort(encoderInfo.getLong("port"));
            // encoder.setMaxTranscodingCapability(encoderInfo.getLong("maxTranscodingCapability"));
            // encoder.setMaxLiveProxiesCapabilities(encoderInfo.getLong("maxLiveProxiesCapabilities"));
            // encoder.setMaxLiveRecordingCapabilities(encoderInfo.getLong("maxLiveRecordingCapabilities"));
        }
        catch (Exception e)
        {
            String errorMessage = "fillEncoder failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillEncodersPool(EncodersPool encodersPool, JSONObject encodersPoolInfo)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try
        {
            encodersPool.setEncodersPoolKey(encodersPoolInfo.getLong("encodersPoolKey"));
            encodersPool.setLabel(encodersPoolInfo.getString("label"));
            if (encodersPoolInfo.has("encoders"))
            {
                JSONArray jaEncodersInfo = encodersPoolInfo.getJSONArray("encoders");
                for(int encoderIndex = 0; encoderIndex < jaEncodersInfo.length(); encoderIndex++)
                {
                    JSONObject joEncoderInfo = jaEncodersInfo.getJSONObject(encoderIndex);

                    Encoder encoder = new Encoder();

                    fillEncoder(encoder, joEncoderInfo);

                    encodersPool.getEncoderList().add(encoder);
                }
            }
        }
        catch (Exception e)
        {
            String errorMessage = "fillEncodersPool failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillMediaItem(MediaItem mediaItem, JSONObject mediaItemInfo, boolean deep)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            mediaItem.setMediaItemKey(mediaItemInfo.getLong("mediaItemKey"));
            // mLogger.info("fillMediaItem. mediaItemKey: " + mediaItem.getMediaItemKey());
            mediaItem.setContentType(mediaItemInfo.getString("contentType"));
            mediaItem.setTitle(mediaItemInfo.getString("title"));
            if (!mediaItemInfo.isNull("deliveryFileName"))
                mediaItem.setDeliveryFileName(mediaItemInfo.getString("deliveryFileName"));
            mediaItem.setIngestionDate(simpleDateFormat.parse(mediaItemInfo.getString("ingestionDate")));
            mediaItem.setStartPublishing(simpleDateFormat.parse(mediaItemInfo.getString("startPublishing")));
            mediaItem.setEndPublishing(simpleDateFormat.parse(mediaItemInfo.getString("endPublishing")));
            if (!mediaItemInfo.isNull("uniqueName"))
                mediaItem.setUniqueName(mediaItemInfo.getString("uniqueName"));
            if (!mediaItemInfo.isNull("ingester"))
                mediaItem.setIngester(mediaItemInfo.getString("ingester"));
            {
                JSONArray jaTags = mediaItemInfo.getJSONArray("tags");

                for (int tagIndex = 0; tagIndex < jaTags.length(); tagIndex++)
                    mediaItem.getTags().add(jaTags.getString(tagIndex));
            }
            if (mediaItemInfo.has("crossReferences"))
            {
                JSONArray jaCrossReferences = mediaItemInfo.getJSONArray("crossReferences");

                for (int crossReferenceIndex = 0; crossReferenceIndex < jaCrossReferences.length(); crossReferenceIndex++)
                {
                    JSONObject joCrossReference = jaCrossReferences.getJSONObject(crossReferenceIndex);

                    MediaItemCrossReference mediaItemCrossReference = new MediaItemCrossReference();
                    mediaItemCrossReference.setType(joCrossReference.getString("type"));
                    if (joCrossReference.has("sourceMediaItemKey"))
                        mediaItemCrossReference.setSourceMediaItemKey(joCrossReference.getLong("sourceMediaItemKey"));
                    else
                        mediaItemCrossReference.setSourceMediaItemKey(null);
                    if (joCrossReference.has("targetMediaItemKey"))
                        mediaItemCrossReference.setTargetMediaItemKey(joCrossReference.getLong("targetMediaItemKey"));
                    else
                        mediaItemCrossReference.setTargetMediaItemKey(null);

                    if (joCrossReference.has("parameters") && !joCrossReference.isNull("parameters"))
                        mediaItemCrossReference.setParameters(joCrossReference.getJSONObject("parameters").toString());
                    else
                        mediaItemCrossReference.setParameters(null);

                    mediaItem.getCrossReferences().add(mediaItemCrossReference);
                }
            }
            if (mediaItemInfo.has("userData") && !mediaItemInfo.isNull("userData"))
                mediaItem.setUserData(mediaItemInfo.getString("userData"));
            mediaItem.setProviderName(mediaItemInfo.getString("providerName"));
            mediaItem.setRetentionInMinutes(mediaItemInfo.getLong("retentionInMinutes"));

            if (deep)
            {
                JSONArray jaPhysicalPaths = mediaItemInfo.getJSONArray("physicalPaths");

                mediaItem.setSourcePhysicalPath(null);

                for (int physicalPathIndex = 0; physicalPathIndex < jaPhysicalPaths.length(); physicalPathIndex++)
                {
                    JSONObject physicalPathInfo = jaPhysicalPaths.getJSONObject(physicalPathIndex);

                    PhysicalPath physicalPath = new PhysicalPath();
                    physicalPath.setPhysicalPathKey(physicalPathInfo.getLong("physicalPathKey"));
                    if (physicalPathInfo.isNull("fileFormat"))
                        physicalPath.setFileFormat(null);
                    else
                        physicalPath.setFileFormat(physicalPathInfo.getString("fileFormat"));
                    if (physicalPathInfo.isNull("deliveryTechnology"))
                        physicalPath.setDeliveryTechnology(null);
                    else
                        physicalPath.setDeliveryTechnology(physicalPathInfo.getString("deliveryTechnology"));

                    // partitionNumber, relativePath and fileName are present only if the APIKey has the admin rights
                    try {
                        physicalPath.setPartitionNumber(physicalPathInfo.getLong("partitionNumber"));
                        physicalPath.setRelativePath(physicalPathInfo.getString("relativePath"));
                        physicalPath.setFileName(physicalPathInfo.getString("fileName"));
                    }
                    catch (Exception e)
                    {

                    }

                    physicalPath.setExternalReadOnlyStorage(physicalPathInfo.getBoolean("externalReadOnlyStorage"));

                    if (physicalPathInfo.isNull("externalDeliveryTechnology"))
                        physicalPath.setExternalDeliveryTechnology(null);
                    else
                        physicalPath.setExternalDeliveryTechnology(physicalPathInfo.getString("externalDeliveryTechnology"));

                    if (physicalPathInfo.isNull("externalDeliveryURL"))
                        physicalPath.setExternalDeliveryURL(null);
                    else
                        physicalPath.setExternalDeliveryURL(physicalPathInfo.getString("externalDeliveryURL"));

                    physicalPath.setCreationDate(simpleDateFormat.parse(physicalPathInfo.getString("creationDate")));
                    if (physicalPathInfo.isNull("encodingProfileKey"))
                    {
                        physicalPath.setEncodingProfileKey(null);
                        mediaItem.setSourcePhysicalPath(physicalPath);
                    }
                    else
                        physicalPath.setEncodingProfileKey(physicalPathInfo.getLong("encodingProfileKey"));
                    physicalPath.setSizeInBytes(physicalPathInfo.getLong("sizeInBytes"));

                    if (physicalPathInfo.isNull("retentionInMinutes"))
                        physicalPath.setRetentionInMinutes(null);
                    else
                        physicalPath.setRetentionInMinutes(physicalPathInfo.getLong("retentionInMinutes"));

                    if (physicalPathInfo.has("durationInMilliSeconds") && !physicalPathInfo.isNull("durationInMilliSeconds"))
                        physicalPath.setDurationInMilliSeconds(physicalPathInfo.getLong("durationInMilliSeconds"));
                    else
                        physicalPath.setDurationInMilliSeconds(null);

                    if (physicalPathInfo.has("bitRate") && !physicalPathInfo.isNull("bitRate"))
                        physicalPath.setBitRate(physicalPathInfo.getLong("bitRate"));
                    else
                        physicalPath.setBitRate(null);

                    if (mediaItem.getContentType().equalsIgnoreCase("video"))
                    {
                        {
                            JSONArray jaVideoTracks = physicalPathInfo.getJSONArray("videoTracks");

                            for (int videoTrackIndex = 0; videoTrackIndex < jaVideoTracks.length(); videoTrackIndex++)
                            {
                                JSONObject joVideoTrack = jaVideoTracks.getJSONObject(videoTrackIndex);

                                VideoTrack videoTrack = new VideoTrack();
                                physicalPath.getVideoTracks().add(videoTrack);

                                videoTrack.setVideoTrackKey(joVideoTrack.getLong("videoTrackKey"));
                                videoTrack.setTrackIndex(joVideoTrack.getLong("trackIndex"));
                                videoTrack.setDurationInMilliSeconds(joVideoTrack.getLong("durationInMilliSeconds"));
                                videoTrack.setWidth(joVideoTrack.getLong("width"));
                                videoTrack.setHeight(joVideoTrack.getLong("height"));
                                videoTrack.setAvgFrameRate(joVideoTrack.getString("avgFrameRate"));
                                videoTrack.setCodecName(joVideoTrack.getString("codecName"));
                                videoTrack.setBitRate(joVideoTrack.getLong("bitRate"));
                                videoTrack.setProfile(joVideoTrack.getString("profile"));
                            }
                        }

                        {
                            JSONArray jaAudioTracks = physicalPathInfo.getJSONArray("audioTracks");

                            for (int audioTrackIndex = 0; audioTrackIndex < jaAudioTracks.length(); audioTrackIndex++)
                            {
                                JSONObject joAudioTrack = jaAudioTracks.getJSONObject(audioTrackIndex);

                                AudioTrack audioTrack = new AudioTrack();
                                physicalPath.getAudioTracks().add(audioTrack);

                                audioTrack.setAudioTrackKey(joAudioTrack.getLong("audioTrackKey"));
                                audioTrack.setTrackIndex(joAudioTrack.getLong("trackIndex"));
                                audioTrack.setDurationInMilliSeconds(joAudioTrack.getLong("durationInMilliSeconds"));
                                audioTrack.setBitRate(joAudioTrack.getLong("bitRate"));
                                audioTrack.setCodecName(joAudioTrack.getString("codecName"));
                                audioTrack.setSampleRate(joAudioTrack.getLong("sampleRate"));
                                audioTrack.setChannels(joAudioTrack.getLong("channels"));
                                audioTrack.setLanguage(joAudioTrack.getString("language"));
                            }
                        }
                    }
                    else if (mediaItem.getContentType().equalsIgnoreCase("audio"))
                    {
                        {
                            JSONArray jaAudioTracks = physicalPathInfo.getJSONArray("audioTracks");

                            for (int audioTrackIndex = 0; audioTrackIndex < jaAudioTracks.length(); audioTrackIndex++)
                            {
                                JSONObject joAudioTrack = jaAudioTracks.getJSONObject(audioTrackIndex);

                                AudioTrack audioTrack = new AudioTrack();
                                physicalPath.getAudioTracks().add(audioTrack);

                                audioTrack.setAudioTrackKey(joAudioTrack.getLong("audioTrackKey"));
                                audioTrack.setDurationInMilliSeconds(joAudioTrack.getLong("durationInMilliSeconds"));
                                audioTrack.setBitRate(joAudioTrack.getLong("bitRate"));
                                audioTrack.setCodecName(joAudioTrack.getString("codecName"));
                                audioTrack.setSampleRate(joAudioTrack.getLong("sampleRate"));
                                audioTrack.setChannels(joAudioTrack.getLong("channels"));
                            }
                        }
                    }
                    else if (mediaItem.getContentType().equalsIgnoreCase("image"))
                    {
                        JSONObject imageDetailsInfo = physicalPathInfo.getJSONObject("imageDetails");

                        physicalPath.getImageDetails().setWidth(imageDetailsInfo.getLong("width"));
                        physicalPath.getImageDetails().setHeight(imageDetailsInfo.getLong("height"));
                        physicalPath.getImageDetails().setFormat(imageDetailsInfo.getString("format"));
                        physicalPath.getImageDetails().setQuality(imageDetailsInfo.getLong("quality"));
                    }

                    mediaItem.getPhysicalPathList().add(physicalPath);
                }
            }
        }
        catch (Exception e)
        {
            String errorMessage = "fillMediaItem failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillIngestionWorkflow(IngestionWorkflow ingestionWorkflow, JSONObject ingestionWorkflowInfo, boolean deep)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            ingestionWorkflow.setIngestionRootKey(ingestionWorkflowInfo.getLong("ingestionRootKey"));
            ingestionWorkflow.setUserKey(ingestionWorkflowInfo.getLong("userKey"));
            ingestionWorkflow.setUserName(ingestionWorkflowInfo.getString("userName"));
            ingestionWorkflow.setLabel(ingestionWorkflowInfo.getString("label"));
            ingestionWorkflow.setStatus(ingestionWorkflowInfo.getString("status"));
            ingestionWorkflow.setIngestionDate(simpleDateFormat.parse(ingestionWorkflowInfo.getString("ingestionDate")));
            ingestionWorkflow.setLastUpdate(simpleDateFormat.parse(ingestionWorkflowInfo.getString("lastUpdate")));

            if (deep)
            {
                JSONArray jaIngestionJobs = ingestionWorkflowInfo.getJSONArray("ingestionJobs");

                for (int ingestionJobIndex = 0; ingestionJobIndex < jaIngestionJobs.length(); ingestionJobIndex++)
                {
                    JSONObject ingestionJobInfo = jaIngestionJobs.getJSONObject(ingestionJobIndex);

                    IngestionJob ingestionJob = new IngestionJob();

                    fillIngestionJob(ingestionJob, ingestionJobInfo);

                    ingestionWorkflow.getIngestionJobList().add(ingestionJob);
                }
            }
        }
        catch (Exception e)
        {
            String errorMessage = "fillIngestionWorkflow failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillIngestionJob(IngestionJob ingestionJob, JSONObject ingestionJobInfo)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            ingestionJob.setIngestionJobKey(ingestionJobInfo.getLong("ingestionJobKey"));
            ingestionJob.setLabel(ingestionJobInfo.getString("label"));
            ingestionJob.setIngestionType(ingestionJobInfo.getString("ingestionType"));
            ingestionJob.setMetaDataContent(ingestionJobInfo.getString("metaDataContent"));

            try
            {
                JSONObject joMetadataContent = null;

                if (ingestionJob.getMetaDataContent() != null && !ingestionJob.getMetaDataContent().isEmpty())
                    joMetadataContent = new JSONObject(ingestionJob.getMetaDataContent());

                if (joMetadataContent.has("Ingester") && !joMetadataContent.isNull("Ingester"))
                    ingestionJob.setIngester(joMetadataContent.getString("Ingester"));

                if (ingestionJob.getIngestionType().equalsIgnoreCase("Live-Recorder")
                        && joMetadataContent != null)
                {
                    if (joMetadataContent.has("RecordingPeriod") && !joMetadataContent.isNull("RecordingPeriod"))
                    {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                        JSONObject joRecordingPeriod = joMetadataContent.getJSONObject("RecordingPeriod");

                        if (joRecordingPeriod.has("Start") && !joRecordingPeriod.isNull("Start"))
                            ingestionJob.setRecordingPeriodStart(dateFormat.parse(joRecordingPeriod.getString("Start")));

                        if (joRecordingPeriod.has("End") && !joRecordingPeriod.isNull("End"))
                            ingestionJob.setRecordingPeriodEnd(dateFormat.parse(joRecordingPeriod.getString("End")));
                    }

                    if (joMetadataContent.has("LiveRecorderVirtualVOD"))
                        ingestionJob.setRecordingVirtualVOD(true);
                    else
                        ingestionJob.setRecordingVirtualVOD(false);

                    if (joMetadataContent.has("MonitorHLS"))
                        ingestionJob.setRecordingMonitorHLS(true);
                    else
                        ingestionJob.setRecordingMonitorHLS(false);

                    if (joMetadataContent.has("ChannelType") && !joMetadataContent.isNull("ChannelType"))
                    {
                        if (joMetadataContent.getString("ChannelType").equalsIgnoreCase("IP_MMSAsClient")
                            || joMetadataContent.getString("ChannelType").equalsIgnoreCase("Satellite"))
                        {
                            if (joMetadataContent.has("ConfigurationLabel") && !joMetadataContent.isNull("ConfigurationLabel"))
                                ingestionJob.setChannelLabel(joMetadataContent.getString("ConfigurationLabel"));
                        }
                    }
                }
                else if (ingestionJob.getIngestionType().equalsIgnoreCase("Live-Proxy")
                        && joMetadataContent != null)
                {
                    if (joMetadataContent.has("TimePeriod") && !joMetadataContent.isNull("TimePeriod")
                            && joMetadataContent.getBoolean("TimePeriod"))
                    {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                        JSONObject joProxyPeriod = joMetadataContent.getJSONObject("ProxyPeriod");

                        if (joProxyPeriod.has("Start") && !joProxyPeriod.isNull("Start"))
                            ingestionJob.setProxyPeriodStart(dateFormat.parse(joProxyPeriod.getString("Start")));

                        if (joProxyPeriod.has("End") && !joProxyPeriod.isNull("End"))
                            ingestionJob.setProxyPeriodEnd(dateFormat.parse(joProxyPeriod.getString("End")));
                    }
                }
            }
            catch (Exception e)
            {
                mLogger.error("wrong json format"
                        + ", ingestionJob.getMetaDataContent: " + ingestionJob.getMetaDataContent()
                );
            }

            if (!ingestionJobInfo.has("processingStartingFrom") || ingestionJobInfo.isNull("processingStartingFrom"))
                ingestionJob.setProcessingStartingFrom(null);
            else
                ingestionJob.setProcessingStartingFrom(simpleDateFormat.parse(ingestionJobInfo.getString("processingStartingFrom")));
            if (ingestionJobInfo.isNull("startProcessing"))
                ingestionJob.setStartProcessing(null);
            else
                ingestionJob.setStartProcessing(simpleDateFormat.parse(ingestionJobInfo.getString("startProcessing")));
            if (ingestionJobInfo.isNull("startProcessing"))
                ingestionJob.setStartProcessing(null);
            else
                ingestionJob.setStartProcessing(simpleDateFormat.parse(ingestionJobInfo.getString("startProcessing")));
            if (ingestionJobInfo.isNull("endProcessing"))
                ingestionJob.setEndProcessing(null);
            else
                ingestionJob.setEndProcessing(simpleDateFormat.parse(ingestionJobInfo.getString("endProcessing")));
            ingestionJob.setStatus(ingestionJobInfo.getString("status"));
            if (ingestionJobInfo.isNull("errorMessage"))
                ingestionJob.setErrorMessage(null);
            else
                ingestionJob.setErrorMessage(ingestionJobInfo.getString("errorMessage"));
            if (ingestionJobInfo.isNull("errorMessageTruncated"))
                ingestionJob.setErrorMessageTruncated(null);
            else
                ingestionJob.setErrorMessageTruncated(ingestionJobInfo.getBoolean("errorMessageTruncated"));
            if (ingestionJobInfo.isNull("processorMMS"))
                ingestionJob.setProcessorMMS(null);
            else
                ingestionJob.setProcessorMMS(ingestionJobInfo.getString("processorMMS"));
            if (ingestionJobInfo.isNull("downloadingProgress"))
                ingestionJob.setDownloadingProgress(null);
            else
                ingestionJob.setDownloadingProgress(ingestionJobInfo.getLong("downloadingProgress"));
            if (ingestionJobInfo.isNull("uploadingProgress"))
                ingestionJob.setUploadingProgress(null);
            else
                ingestionJob.setUploadingProgress(ingestionJobInfo.getLong("uploadingProgress"));

            if (ingestionJobInfo.isNull("ingestionRootKey"))
                ingestionJob.setIngestionRootKey(null);
            else
                ingestionJob.setIngestionRootKey(ingestionJobInfo.getLong("ingestionRootKey"));

            if (ingestionJobInfo.isNull("dependOnIngestionJobKey"))
                ingestionJob.setDependOnIngestionJobKey(null);
            else
                ingestionJob.setDependOnIngestionJobKey(ingestionJobInfo.getLong("dependOnIngestionJobKey"));

            if (ingestionJobInfo.isNull("dependOnSuccess"))
                ingestionJob.setDependOnSuccess(0);
            else
                ingestionJob.setDependOnSuccess(ingestionJobInfo.getInt("dependOnSuccess"));

            if (ingestionJobInfo.isNull("dependencyIngestionStatus"))
                ingestionJob.setDependencyIngestionStatus(null);
            else
                ingestionJob.setDependencyIngestionStatus(ingestionJobInfo.getString("dependencyIngestionStatus"));

            JSONArray jaMediaItems = ingestionJobInfo.getJSONArray("mediaItems");
            for (int mediaItemIndex = 0; mediaItemIndex < jaMediaItems.length(); mediaItemIndex++)
            {
                JSONObject joMediaItem = jaMediaItems.getJSONObject(mediaItemIndex);

                IngestionJobMediaItem ingestionJobMediaItem = new IngestionJobMediaItem();
                ingestionJobMediaItem.setMediaItemKey(joMediaItem.getLong("mediaItemKey"));
                ingestionJobMediaItem.setPhysicalPathKey(joMediaItem.getLong("physicalPathKey"));

                ingestionJob.getIngestionJobMediaItemList().add(ingestionJobMediaItem);
            }

            if (ingestionJobInfo.has("encodingJob"))
            {
                JSONObject encodingJobInfo = ingestionJobInfo.getJSONObject("encodingJob");

                EncodingJob encodingJob = new EncodingJob();

                fillEncodingJob(encodingJob, encodingJobInfo);

                // end processing estimation
                {
                    ingestionJob.setEndProcessingEstimate(false);

                    Date now = new Date();

                    if (ingestionJob.getEndProcessing() == null
                            && ingestionJob.getStartProcessing() != null && ingestionJob.getStartProcessing().getTime() < now.getTime()
                            && encodingJob.getProgress() != null && encodingJob.getProgress() != 0 && encodingJob.getProgress() != -1)
                    {
                        Long elapsedInMillisecs = now.getTime() - ingestionJob.getStartProcessing().getTime();

                        // elapsedInMillisecs : actual percentage = X (estimateMillisecs) : 100
                        Long estimateMillisecs = elapsedInMillisecs * 100 / encodingJob.getProgress();

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(ingestionJob.getStartProcessing());
                        calendar.add(Calendar.MILLISECOND, estimateMillisecs.intValue());

                        ingestionJob.setEndProcessingEstimate(true);
                        ingestionJob.setEndProcessing(calendar.getTime());
                    }
                }

                ingestionJob.setEncodingJob(encodingJob);
            }
            else
                ingestionJob.setEndProcessingEstimate(false);
        }
        catch (Exception e)
        {
            String errorMessage = "fillIngestionJob failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillEncodingProfile(EncodingProfile encodingProfile, JSONObject encodingProfileInfo, boolean deep)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try
        {
            encodingProfile.setGlobal(encodingProfileInfo.getBoolean("global"));
            encodingProfile.setEncodingProfileKey(encodingProfileInfo.getLong("encodingProfileKey"));
            encodingProfile.setLabel(encodingProfileInfo.getString("label"));
            encodingProfile.setContentType(encodingProfileInfo.getString("contentType"));

            JSONObject joProfileInfo = encodingProfileInfo.getJSONObject("profile");
            encodingProfile.setFileFormat(joProfileInfo.getString("FileFormat"));

            if (deep)
            {
                if (encodingProfile.getContentType().equalsIgnoreCase("video"))
                {
                    JSONObject joVideoInfo = joProfileInfo.getJSONObject("Video");

                    encodingProfile.getVideoDetails().setCodec(joVideoInfo.getString("Codec"));
                    if (joVideoInfo.isNull("Profile"))
                        encodingProfile.getVideoDetails().setProfile(null);
                    else
                        encodingProfile.getVideoDetails().setProfile(joVideoInfo.getString("Profile"));
                    encodingProfile.getVideoDetails().setTwoPasses(joVideoInfo.getBoolean("TwoPasses"));
                    if (joVideoInfo.isNull("OtherOutputParameters"))
                        encodingProfile.getVideoDetails().setOtherOutputParameters(null);
                    else
                        encodingProfile.getVideoDetails().setOtherOutputParameters(joVideoInfo.getString("OtherOutputParameters"));
                    if (joVideoInfo.isNull("FrameRate"))
                        encodingProfile.getVideoDetails().setFrameRate(null);
                    else
                        encodingProfile.getVideoDetails().setFrameRate(joVideoInfo.getLong("FrameRate"));
                    if (joVideoInfo.isNull("KeyFrameIntervalInSeconds"))
                        encodingProfile.getVideoDetails().setKeyFrameIntervalInSeconds(null);
                    else
                        encodingProfile.getVideoDetails().setKeyFrameIntervalInSeconds(joVideoInfo.getLong("KeyFrameIntervalInSeconds"));

                    if (joVideoInfo.has("BitRates"))
                    {
                        JSONArray jaBitRates = joVideoInfo.getJSONArray("BitRates");
                        for (int bitRateIndex = 0; bitRateIndex < jaBitRates.length(); bitRateIndex++)
                        {
                            JSONObject joBitRate = jaBitRates.getJSONObject(bitRateIndex);

                            VideoBitRate videoBitRate = new VideoBitRate();
                            encodingProfile.getVideoDetails().getVideoBitRateList().add(videoBitRate);

                            videoBitRate.setWidth(joBitRate.getLong("Width"));
                            videoBitRate.setHeight(joBitRate.getLong("Height"));
                            videoBitRate.setkBitRate(joBitRate.getLong("KBitRate"));
                            if (!joBitRate.has("ForceOriginalAspectRatio") || joBitRate.isNull("ForceOriginalAspectRatio"))
                                videoBitRate.setForceOriginalAspectRatio(null);
                            else
                                videoBitRate.setForceOriginalAspectRatio(joBitRate.getString("ForceOriginalAspectRatio"));
                            if (!joBitRate.has("Pad") || joBitRate.isNull("Pad"))
                                videoBitRate.setPad(null);
                            else
                                videoBitRate.setPad(joBitRate.getBoolean("Pad"));
                            if (!joBitRate.has("KMaxRate") || joBitRate.isNull("KMaxRate"))
                                videoBitRate.setkMaxRate(null);
                            else
                                videoBitRate.setkMaxRate(joBitRate.getLong("KMaxRate"));
                            if (!joBitRate.has("KBufferSize") || joBitRate.isNull("KBufferSize"))
                                videoBitRate.setkBufferSize(null);
                            else
                                videoBitRate.setkBufferSize(joBitRate.getLong("KBufferSize"));
                        }
                    }

                    JSONObject joAudioInfo = joProfileInfo.getJSONObject("Audio");

                    encodingProfile.getAudioDetails().setCodec(joAudioInfo.getString("Codec"));
                    if (joAudioInfo.isNull("OtherOutputParameters"))
                        encodingProfile.getAudioDetails().setOtherOutputParameters(null);
                    else
                        encodingProfile.getAudioDetails().setOtherOutputParameters(joAudioInfo.getString("OtherOutputParameters"));
                    if (joAudioInfo.isNull("ChannelsNumber"))
                        encodingProfile.getAudioDetails().setChannelsNumber(null);
                    else
                        encodingProfile.getAudioDetails().setChannelsNumber(joAudioInfo.getLong("ChannelsNumber"));
                    if (joAudioInfo.isNull("SampleRate"))
                        encodingProfile.getAudioDetails().setSampleRate(null);
                    else
                        encodingProfile.getAudioDetails().setSampleRate(joAudioInfo.getLong("SampleRate"));

                    if (joAudioInfo.has("BitRates"))
                    {
                        JSONArray jaBitRates = joAudioInfo.getJSONArray("BitRates");
                        for (int bitRateIndex = 0; bitRateIndex < jaBitRates.length(); bitRateIndex++)
                        {
                            JSONObject joBitRate = jaBitRates.getJSONObject(bitRateIndex);

                            AudioBitRate audioBitRate = new AudioBitRate();
                            encodingProfile.getVideoDetails().getAudioBitRateList().add(audioBitRate);

                            if (!joBitRate.has("KBitRate") || joBitRate.isNull("KBitRate"))
                                audioBitRate.setkBitRate(null);
                            else
                                audioBitRate.setkBitRate(joBitRate.getLong("KBitRate"));
                        }
                    }
                }
                else if (encodingProfile.getContentType().equalsIgnoreCase("audio"))
                {
                    JSONObject joAudioInfo = joProfileInfo.getJSONObject("Audio");

                    encodingProfile.getAudioDetails().setCodec(joAudioInfo.getString("Codec"));
                    if (joAudioInfo.isNull("OtherOutputParameters"))
                        encodingProfile.getAudioDetails().setOtherOutputParameters(null);
                    else
                        encodingProfile.getAudioDetails().setOtherOutputParameters(joAudioInfo.getString("OtherOutputParameters"));
                    if (joAudioInfo.isNull("ChannelsNumber"))
                        encodingProfile.getAudioDetails().setChannelsNumber(null);
                    else
                        encodingProfile.getAudioDetails().setChannelsNumber(joAudioInfo.getLong("ChannelsNumber"));
                    if (joAudioInfo.isNull("SampleRate"))
                        encodingProfile.getAudioDetails().setSampleRate(null);
                    else
                        encodingProfile.getAudioDetails().setSampleRate(joAudioInfo.getLong("SampleRate"));

                    if (joAudioInfo.has("BitRates"))
                    {
                        JSONArray jaBitRates = joAudioInfo.getJSONArray("BitRates");
                        for (int bitRateIndex = 0; bitRateIndex < jaBitRates.length(); bitRateIndex++)
                        {
                            JSONObject joBitRate = jaBitRates.getJSONObject(bitRateIndex);

                            AudioBitRate audioBitRate = new AudioBitRate();
                            encodingProfile.getAudioDetails().getAudioBitRateList().add(audioBitRate);

                            if (joBitRate.isNull("KBitRate"))
                                audioBitRate.setkBitRate(null);
                            else
                                audioBitRate.setkBitRate(joBitRate.getLong("KBitRate"));
                        }
                    }
                }
                else if (encodingProfile.getContentType().equalsIgnoreCase("image"))
                {
                    JSONObject joImageInfo = joProfileInfo.getJSONObject("Image");

                    encodingProfile.getImageDetails().setWidth(joImageInfo.getLong("Width"));
                    encodingProfile.getImageDetails().setHeight(joImageInfo.getLong("Height"));
                    encodingProfile.getImageDetails().setAspectRatio(joImageInfo.getBoolean("AspectRatio"));
                    encodingProfile.getImageDetails().setInterlaceType(joImageInfo.getString("InterlaceType"));
                }
            }
        }
        catch (Exception e)
        {
            String errorMessage = "fillEncodingProfile failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillEncodingProfilesSet(EncodingProfilesSet encodingProfilesSet,
                                         JSONObject encodingProfilesSetInfo, boolean deep)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            encodingProfilesSet.setEncodingProfilesSetKey(encodingProfilesSetInfo.getLong("encodingProfilesSetKey"));
            encodingProfilesSet.setContentType(encodingProfilesSetInfo.getString("contentType"));
            encodingProfilesSet.setLabel(encodingProfilesSetInfo.getString("label"));

            if (deep)
            {
                JSONArray jaEncodingProfiles = encodingProfilesSetInfo.getJSONArray("encodingProfiles");

                for (int encodingProfileIndex = 0; encodingProfileIndex < jaEncodingProfiles.length(); encodingProfileIndex++)
                {
                    JSONObject encodingProfileInfo = jaEncodingProfiles.getJSONObject(encodingProfileIndex);

                    EncodingProfile encodingProfile = new EncodingProfile();

                    fillEncodingProfile(encodingProfile, encodingProfileInfo, deep);

                    encodingProfilesSet.getEncodingProfileList().add(encodingProfile);
                }
            }
        }
        catch (Exception e)
        {
            String errorMessage = "fillEncodingProfilesSet failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillYouTubeConf(YouTubeConf youTubeConf, JSONObject youTubeConfInfo)
            throws Exception
    {
        try {
            youTubeConf.setConfKey(youTubeConfInfo.getLong("confKey"));
            youTubeConf.setLabel(youTubeConfInfo.getString("label"));
            youTubeConf.setRefreshToken(youTubeConfInfo.getString("refreshToken"));
        }
        catch (Exception e)
        {
            String errorMessage = "fillYouTubeConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillFacebookConf(FacebookConf facebookConf, JSONObject facebookConfInfo)
            throws Exception
    {
        try {
            facebookConf.setConfKey(facebookConfInfo.getLong("confKey"));
            facebookConf.setLabel(facebookConfInfo.getString("label"));
            facebookConf.setPageToken(facebookConfInfo.getString("pageToken"));
        }
        catch (Exception e)
        {
            String errorMessage = "fillFacebookConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillIPChannelConf(IPChannelConf channelConf, JSONObject channelConfInfo)
            throws Exception
    {
        try {
            channelConf.setConfKey(channelConfInfo.getLong("confKey"));
            channelConf.setLabel(channelConfInfo.getString("label"));
            channelConf.setUrl(channelConfInfo.getString("url"));
            if (channelConfInfo.has("type") && !channelConfInfo.isNull("type"))
                channelConf.setType(channelConfInfo.getString("type"));
            if (channelConfInfo.has("description") && !channelConfInfo.isNull("description"))
                channelConf.setDescription(channelConfInfo.getString("description"));
            if (channelConfInfo.has("name") && !channelConfInfo.isNull("name"))
                channelConf.setName(channelConfInfo.getString("name"));
            if (channelConfInfo.has("region") && !channelConfInfo.isNull("region"))
                channelConf.setRegion(channelConfInfo.getString("region"));

            if (channelConfInfo.has("country") && !channelConfInfo.isNull("country"))
                channelConf.setCountry(channelConfInfo.getString("country"));

            if (channelConfInfo.has("imageMediaItemKey") && !channelConfInfo.isNull("imageMediaItemKey"))
                channelConf.setImageMediaItemKey(channelConfInfo.getLong("imageMediaItemKey"));

            if (channelConfInfo.has("imageUniqueName") && !channelConfInfo.isNull("imageUniqueName"))
                channelConf.setImageUniqueName(channelConfInfo.getString("imageUniqueName"));

            if (channelConfInfo.has("position") && !channelConfInfo.isNull("position"))
                channelConf.setPosition(channelConfInfo.getLong("position"));
            if (channelConfInfo.has("channelData") && !channelConfInfo.isNull("channelData"))
                channelConf.setChannelData(channelConfInfo.getString("channelData"));
        }
        catch (Exception e)
        {
            String errorMessage = "fillIPChannelConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillSATChannelConf(SATChannelConf channelConf, JSONObject channelConfInfo)
            throws Exception
    {
        try {
            if (channelConfInfo.has("confKey") && !channelConfInfo.isNull("confKey"))
                channelConf.setConfKey(channelConfInfo.getLong("confKey"));
            channelConf.setSourceSATConfKey(channelConfInfo.getLong("sourceSATConfKey"));
            channelConf.setLabel(channelConfInfo.getString("label"));

            if (channelConfInfo.has("region") && !channelConfInfo.isNull("region"))
                channelConf.setRegion(channelConfInfo.getString("region"));
            if (channelConfInfo.has("country") && !channelConfInfo.isNull("country"))
                channelConf.setCountry(channelConfInfo.getString("country"));

            if (channelConfInfo.has("imageMediaItemKey") && !channelConfInfo.isNull("imageMediaItemKey"))
                channelConf.setImageMediaItemKey(channelConfInfo.getLong("imageMediaItemKey"));

            if (channelConfInfo.has("imageUniqueName") && !channelConfInfo.isNull("imageUniqueName"))
                channelConf.setImageUniqueName(channelConfInfo.getString("imageUniqueName"));

            if (channelConfInfo.has("position") && !channelConfInfo.isNull("position"))
                channelConf.setPosition(channelConfInfo.getLong("position"));
            if (channelConfInfo.has("channelData") && !channelConfInfo.isNull("channelData"))
                channelConf.setChannelData(channelConfInfo.getString("channelData"));
        }
        catch (Exception e)
        {
            String errorMessage = "fillSATChannelConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillSourceSATChannelConf(SourceSATChannelConf channelConf, JSONObject channelConfInfo)
            throws Exception
    {
        try {
            channelConf.setConfKey(channelConfInfo.getLong("confKey"));
            if (channelConfInfo.has("serviceId") && !channelConfInfo.isNull("serviceId"))
                channelConf.setServiceId(channelConfInfo.getLong("serviceId"));
            if (channelConfInfo.has("networkId") && !channelConfInfo.isNull("networkId"))
                channelConf.setNetworkId(channelConfInfo.getLong("networkId"));
            if (channelConfInfo.has("transportStreamId") && !channelConfInfo.isNull("transportStreamId"))
                channelConf.setTransportStreamId(channelConfInfo.getLong("transportStreamId"));
            channelConf.setName(channelConfInfo.getString("name"));
            channelConf.setSatellite(channelConfInfo.getString("satellite"));
            channelConf.setFrequency(channelConfInfo.getLong("frequency"));
            if (channelConfInfo.has("lnb") && !channelConfInfo.isNull("lnb"))
                channelConf.setLnb(channelConfInfo.getString("lnb"));
            if (channelConfInfo.has("videoPid") && !channelConfInfo.isNull("videoPid"))
                channelConf.setVideoPid(channelConfInfo.getLong("videoPid"));
            if (channelConfInfo.has("audioPids") && !channelConfInfo.isNull("audioPids"))
                channelConf.setAudioPids(channelConfInfo.getString("audioPids"));
            if (channelConfInfo.has("audioItalianPid") && !channelConfInfo.isNull("audioItalianPid"))
                channelConf.setAudioItalianPid(channelConfInfo.getLong("audioItalianPid"));
            if (channelConfInfo.has("audioEnglishPid") && !channelConfInfo.isNull("audioEnglishPid"))
                channelConf.setAudioEnglishPid(channelConfInfo.getLong("audioEnglishPid"));
            if (channelConfInfo.has("teletextPid") && !channelConfInfo.isNull("teletextPid"))
                channelConf.setTeletextPid(channelConfInfo.getLong("teletextPid"));
            if (channelConfInfo.has("modulation") && !channelConfInfo.isNull("modulation"))
                channelConf.setModulation(channelConfInfo.getString("modulation"));
            if (channelConfInfo.has("polarization") && !channelConfInfo.isNull("polarization"))
                channelConf.setPolarization(channelConfInfo.getString("polarization"));
            if (channelConfInfo.has("symbolRate") && !channelConfInfo.isNull("symbolRate"))
                channelConf.setSymbolRate(channelConfInfo.getLong("symbolRate"));
            if (channelConfInfo.has("country") && !channelConfInfo.isNull("country"))
                channelConf.setCountry(channelConfInfo.getString("country"));
            if (channelConfInfo.has("deliverySystem") && !channelConfInfo.isNull("deliverySystem"))
                channelConf.setDeliverySystem(channelConfInfo.getString("deliverySystem"));
        }
        catch (Exception e)
        {
            String errorMessage = "fillSourceSATChannelConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillWorkflowLibrary(WorkflowLibrary workflowLibrary, JSONObject workflowLibraryInfo)
            throws Exception
    {
        try {
            workflowLibrary.setWorkflowLibraryKey(workflowLibraryInfo.getLong("workflowLibraryKey"));
            workflowLibrary.setGlobal(workflowLibraryInfo.getBoolean("global"));
            if (workflowLibraryInfo.has("creatorUserKey"))
                workflowLibrary.setCreatorUserKey(workflowLibraryInfo.getLong("creatorUserKey"));
            else
                workflowLibrary.setCreatorUserKey(null);
            workflowLibrary.setLabel(workflowLibraryInfo.getString("label"));
            if (workflowLibraryInfo.isNull("thumbnailMediaItemKey"))
                workflowLibrary.setThumbnailMediaItemKey(null);
            else
                workflowLibrary.setThumbnailMediaItemKey(workflowLibraryInfo.getLong("thumbnailMediaItemKey"));

            if (workflowLibraryInfo.has("variables") && !workflowLibraryInfo.isNull("variables"))
            {
                JSONObject joVariables = workflowLibraryInfo.getJSONObject("variables");

                workflowLibrary.setWorkflowVariableList(new ArrayList<>());

                Iterator<String> variables = joVariables.keys();
                while(variables.hasNext())
                {
                    String variableName = variables.next();
                    workflowLibrary.getWorkflowVariableList().add(null);
                }

                int defaultPosition = 0;
                variables = joVariables.keys();
                while(variables.hasNext())
                {
                    String variableName = variables.next();

                    JSONObject joWorkflowVariable = joVariables.getJSONObject(variableName);

                    WorkflowVariable workflowVariable = new WorkflowVariable();

                    workflowVariable.setName(variableName);

                    if (joWorkflowVariable.has("IsNull"))
                        workflowVariable.setNullVariable(joWorkflowVariable.getBoolean("IsNull"));
                    else
                        workflowVariable.setNullVariable(false);

                    if (joWorkflowVariable.has("Description"))
                        workflowVariable.setDescription(joWorkflowVariable.getString("Description"));
                    if (joWorkflowVariable.has("Type"))
                        workflowVariable.setType(joWorkflowVariable.getString("Type"));
                    else
                        workflowVariable.setType("string");
                    if (!workflowVariable.isNullVariable() && joWorkflowVariable.has("Value"))
                    {
                        if (workflowVariable.getType().equalsIgnoreCase("string"))
                            workflowVariable.setStringValue(joWorkflowVariable.getString("Value"));
                        else if (workflowVariable.getType().equalsIgnoreCase("integer"))
                            workflowVariable.setLongValue(joWorkflowVariable.getLong("Value"));
                        else if (workflowVariable.getType().equalsIgnoreCase("decimal"))
                            workflowVariable.setDoubleValue(joWorkflowVariable.getDouble("Value"));
                        else if (workflowVariable.getType().equalsIgnoreCase("boolean"))
                            workflowVariable.setBooleanValue(joWorkflowVariable.getBoolean("Value"));
                        else if (workflowVariable.getType().equalsIgnoreCase("datetime"))
                        {
                            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                            workflowVariable.setDatetimeValue(dateFormat.parse(joWorkflowVariable.getString("Value")));
                        }
                        else if (workflowVariable.getType().equalsIgnoreCase("datetime-millisecs"))
                        {
                            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                            workflowVariable.setDatetimeValue(dateFormat.parse(joWorkflowVariable.getString("Value")));
                        }
                        else if (workflowVariable.getType().equalsIgnoreCase("jsonObject"))
                            workflowVariable.setJsonObjectValue(joWorkflowVariable.getJSONObject("Value"));
                        else if (workflowVariable.getType().equalsIgnoreCase("jsonArray"))
                            workflowVariable.setJsonArrayValue(joWorkflowVariable.getJSONArray("Value"));
                        else
                            mLogger.error("Unknown type: " + workflowVariable.getType());
                    }

                    if (joWorkflowVariable.has("Position"))
                    {
                        int position = joWorkflowVariable.getInt("Position");
                        workflowLibrary.getWorkflowVariableList().set(position, workflowVariable);
                    }
                    else
                        workflowLibrary.getWorkflowVariableList().set(defaultPosition++, workflowVariable);
                }
            }
        }
        catch (Exception e)
        {
            String errorMessage = "fillWorkflowLibrary failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillFTPConf(FTPConf ftpConf, JSONObject ftpConfInfo)
            throws Exception
    {
        try {
            ftpConf.setConfKey(ftpConfInfo.getLong("confKey"));
            ftpConf.setLabel(ftpConfInfo.getString("label"));
            ftpConf.setServer(ftpConfInfo.getString("server"));
            ftpConf.setPort(ftpConfInfo.getLong("port"));
            ftpConf.setUserName(ftpConfInfo.getString("userName"));
            ftpConf.setPassword(ftpConfInfo.getString("password"));
            ftpConf.setRemoteDirectory(ftpConfInfo.getString("remoteDirectory"));
        }
        catch (Exception e)
        {
            String errorMessage = "fillFTPConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillEMailConf(EMailConf emailConf, JSONObject emailConfInfo)
            throws Exception
    {
        try {
            emailConf.setConfKey(emailConfInfo.getLong("confKey"));
            emailConf.setLabel(emailConfInfo.getString("label"));
            emailConf.setAddresses(emailConfInfo.getString("addresses"));
            emailConf.setSubject(emailConfInfo.getString("subject"));
            emailConf.setMessage(emailConfInfo.getString("message"));
        }
        catch (Exception e)
        {
            String errorMessage = "fillEMailConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

}
