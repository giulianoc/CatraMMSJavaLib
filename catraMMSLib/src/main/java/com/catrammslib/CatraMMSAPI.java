package com.catrammslib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import com.amazonaws.services.cloudfront.util.SignerUtils.Protocol;
import com.catrammslib.entity.AWSChannelConf;
import com.catrammslib.entity.AudioBitRate;
import com.catrammslib.entity.AudioTrack;
import com.catrammslib.entity.Stream;
import com.catrammslib.entity.EMailConf;
import com.catrammslib.entity.Encoder;
import com.catrammslib.entity.EncodersPool;
import com.catrammslib.entity.EncodingJob;
import com.catrammslib.entity.EncodingProfile;
import com.catrammslib.entity.EncodingProfilesSet;
import com.catrammslib.entity.FTPConf;
import com.catrammslib.entity.FacebookConf;
import com.catrammslib.entity.IngestionJob;
import com.catrammslib.entity.IngestionJobMediaItem;
import com.catrammslib.entity.IngestionWorkflow;
import com.catrammslib.entity.MediaItem;
import com.catrammslib.entity.MediaItemCrossReference;
import com.catrammslib.entity.PhysicalPath;
import com.catrammslib.entity.RequestPerContentStatistic;
import com.catrammslib.entity.RequestPerMonthStatistic;
import com.catrammslib.entity.RequestPerDayStatistic;
import com.catrammslib.entity.RequestPerHourStatistic;
import com.catrammslib.entity.RequestStatistic;
import com.catrammslib.entity.SourceSATStream;
import com.catrammslib.entity.UserProfile;
import com.catrammslib.entity.VideoBitRate;
import com.catrammslib.entity.VideoTrack;
import com.catrammslib.entity.WorkflowLibrary;
import com.catrammslib.entity.WorkflowVariable;
import com.catrammslib.entity.WorkspaceDetails;
import com.catrammslib.entity.YouTubeConf;
import com.catrammslib.utility.BroadcastPlaylistItem;
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
	private Boolean outputToBeCompressed;

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

                String tmpOutputToBeCompressed = configurationProperties.getProperty("catramms.mms.outputToBeCompressed");
                if (tmpOutputToBeCompressed == null)
                {
                    String errorMessage = "No catramms.mms.outputToBeCompressed configuration found";
                    mLogger.error(errorMessage);

                    return;
                }
                outputToBeCompressed = Boolean.parseBoolean(tmpOutputToBeCompressed);
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

	public String getAWSSignedURLWithCustomPolicy(
		// the DNS name of your CloudFront distribution, or a registered alias
		String cloudFrontHostName,	// i.e.: ddkdknki5kdya.cloudfront.net
		final String uri,			// i.e.: out/v1/444d83fd79124a55be06bd9489134652/index.m3u8
		final Date expirationDate,
		String awsCloudFrontKeyPairId,
		File cloudFrontPrivateKeyFile) 
		throws URISyntaxException, InvalidKeySpecException, IOException
	{
		// the unique ID assigned to your CloudFront key pair in the console
		// String cloudFrontKeyPairId = "APKAUYWFOBAADUMU4IGK";
		// the private key you created in the AWS Management Console
		// URL resource = CiborTVService_v1.class.getClassLoader().getResource("pk-APKAUYWFOBAADUMU4IGK.pem");
		// File cloudFrontPrivateKeyFile = new File(resource.toURI());

        mLogger.info("Received getAWSSignedURLWithCustomPolicy"
			+ ", cloudFrontHostName: " + cloudFrontHostName
			+ ", uri: " + uri
			+ ", expirationDate: " + expirationDate + " (" + expirationDate.getTime() + ")"
			+ ", awsCloudFrontKeyPairId: " + awsCloudFrontKeyPairId
        );

		String signedUrl = CloudFrontUrlSigner.getSignedURLWithCannedPolicy(
           Protocol.https, 
           cloudFrontHostName, 
           cloudFrontPrivateKeyFile,   
           uri,
           awsCloudFrontKeyPairId,
           expirationDate);

		return signedUrl;
	}

	public String getCDN77SignedUrlPath(
		String cdnResourceUrl, String filePath,
        String secureToken, Long expiryTimestamp)
    {
        try {
            mLogger.info("Received getCDN77SignedUrlPath"
                    + ", cdnResourceUrl: " + cdnResourceUrl	// i.e.: 1011683079.rsc.cdn77.org
                    + ", filePath: " + filePath				// i.e.: /1011683079/1/index.m3u8
                    + ", secureToken: " + secureToken		// i.e.: dp4h5ek2mx5tmcsf
                    + ", expiryTimestamp: " + expiryTimestamp
            );

            // because of hls/dash, anything included after the last slash (e.g. playlist/{chunk}) shouldn't be part of the path string,
            // for which we generate the secure token. Because of that, everything included after the last slash is stripped.
            // PHP:         $strippedPath = substr($filePath, 0, strrpos($filePath, '/'));
            String strippedPath = filePath.substring(0, filePath.lastIndexOf("/"));

            if (strippedPath.charAt(0) != '/')
                strippedPath = "/" + strippedPath;

            int pos;
            if ((pos = strippedPath.indexOf("?")) != -1)
                filePath = strippedPath.substring(0, pos);

            mLogger.info("strippedPath: " + strippedPath);
            String hashStr = strippedPath + secureToken;

            String sExpiryTimestamp;
            if (expiryTimestamp != null)
            {
                hashStr = expiryTimestamp + hashStr;
                sExpiryTimestamp = "," + expiryTimestamp;
            }
            else
                sExpiryTimestamp = expiryTimestamp.toString();

            mLogger.info("sExpiryTimestamp: " + sExpiryTimestamp);

            String base64HashStr = Base64.getEncoder().encodeToString(md5(hashStr));
            mLogger.info("base64HashStr 1: " + base64HashStr);
            base64HashStr = base64HashStr.replace("+", "-").replace("/", "_");
            mLogger.info("base64HashStr 2: " + base64HashStr);

            // the URL is however, intensionaly returned with the previously stripped parts (eg. playlist/{chunk}..)
            String signedURL = "https://" + cdnResourceUrl + "/" + base64HashStr + sExpiryTimestamp + filePath;
            mLogger.info("getCDN77SignedUrlPath"
				+ ", signedURL: " + signedURL);

            return signedURL;
        }
        catch (Exception e)
        {
            mLogger.error("getCDN77SignedUrlPath"
				+ ", exception: " + e);

            return null;
        }
    }

	private byte[] md5(String text)
    {
        try
        {
            MessageDigest digester = MessageDigest.getInstance("MD5");
            digester.update(text.getBytes());
            byte[] md5Bytes = digester.digest();
            String md5Text = new String(md5Bytes); // if you need in String format
            // better use md5Bytes if applying further processing to the generated md5.
            // Otherwise it may give undesired results.
            return md5Bytes;
            // return md5Text;

        }
        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
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
			{
				JSONObject joObj = new JSONObject();
				joObj.put("email", emailAddressToShare);

				postBodyRequest = joObj.toString();
			}
            else
			{
				JSONObject joObj = new JSONObject();
				joObj.put("name", userNameToShare);
				joObj.put("email", emailAddressToShare);
				joObj.put("password", passwordToShare);
				joObj.put("country", countryToShare);

				postBodyRequest = joObj.toString();
			}

            mLogger.info("shareWorkspace"
                            + ", mmsURL: " + mmsURL
                            + ", postBodyRequest: " + postBodyRequest
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, postBodyRequest, outputToBeCompressed);
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
                    username, password, null, postBodyRequest, outputToBeCompressed);
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

			// only email and password are mandatory
			JSONObject joObject = new JSONObject();
			if (userNameToRegister != null && !userNameToRegister.isEmpty())
				joObject.put("name", userNameToRegister);
			joObject.put("email", emailAddressToRegister);
			joObject.put("password", passwordToRegister);
			if (countryToRegister != null && !countryToRegister.isEmpty())
				joObject.put("country", countryToRegister);
			if (workspaceNameToRegister != null && !workspaceNameToRegister.isEmpty())
				joObject.put("workspaceName", workspaceNameToRegister);

			String postBodyRequest = joObject.toString();

            mLogger.info("register"
                            + ", mmsURL: " + mmsURL
                            + ", postBodyRequest: " + postBodyRequest
            );

            String username = null;
            String password = null;

            Date now = new Date();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, postBodyRequest, outputToBeCompressed);
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
			// 2022-01-03: it has to be GET because same link is sent inside the email
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
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

    public void forgotPassword(String email)
            throws Exception
    {
        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
            	+ "/catramms/1.0.1/user/password/reset"
				+ "?email=" + java.net.URLEncoder.encode(email, "UTF-8") // requires unescape server side
			;

            String username = null;
            String password = null;

            mLogger.info("forgotPassword"
                + ", mmsURL: " + mmsURL
            );

            Date now = new Date();
            String contentType = null;
			// 2022-01-03: it has to be GET because same link is sent inside the email
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, null, outputToBeCompressed);
            mLogger.info("forgotPassword. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "forgotPassword failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void resetPassword(String resetPasswordToken, String newPassword)
            throws Exception
    {
        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
            	+ "/catramms/1.0.1/user/password/reset"
			;

			JSONObject joObject = new JSONObject();
			joObject.put("resetPasswordToken", resetPasswordToken);
			joObject.put("newPassword", newPassword);

			String postBodyRequest = joObject.toString();

			String username = null;
            String password = null;

            mLogger.info("forgotPassword"
                + ", mmsURL: " + mmsURL
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, postBodyRequest, outputToBeCompressed);
            mLogger.info("resetPassword. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "resetPassword failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
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
                joBody.put("name", username);
                joBody.put("password", password);
                if (remoteClientIPAddress != null && !remoteClientIPAddress.isEmpty())
                    joBody.put("remoteClientIPAddress", remoteClientIPAddress);
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
                joBody.put("email", username);
                joBody.put("password", password);
                if (remoteClientIPAddress != null && !remoteClientIPAddress.isEmpty())
                    joBody.put("remoteClientIPAddress", remoteClientIPAddress);
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
                    username, password, null, postBodyRequest, outputToBeCompressed);
            mLogger.info("login. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "Login MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        UserProfile userProfile = new UserProfile();
        WorkspaceDetails workspaceDetails = null;

        try
        {
            JSONObject joWMMSInfo = new JSONObject(mmsInfo);

            fillUserProfile(userProfile, joWMMSInfo);
            userProfile.setPassword(password);

			if (joWMMSInfo.has("workspace") && !joWMMSInfo.isNull("workspace"))
			{
				workspaceDetails = new WorkspaceDetails();
				JSONObject joWorkspaceInfo = joWMMSInfo.getJSONObject("workspace");
				fillWorkspaceDetails(workspaceDetails, joWorkspaceInfo);
			}
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing workspaceDetails failed" 
				+ ", Exception: " + e
			;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        List<Object> objects = new ArrayList<>();
        objects.add(userProfile);
        objects.add(workspaceDetails);

        return objects;
    }

    public UserProfile updateUserProfile(String username, String password,
		String newName,
		String newEmailAddress,
		String newCountry,
		Date newExpirationDate,	// backend update this field only if you are an admin
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

			JSONObject joUser = new JSONObject();
			if (newName != null)
				joUser.put("name", newName);
			if (newEmailAddress != null)
				joUser.put("email", newEmailAddress);
			if (newPassword != null && !newPassword.isEmpty())
				joUser.put("newPassword", newPassword);
			if (oldPassword != null && !oldPassword.isEmpty())
				joUser.put("oldPassword", oldPassword);
			if (newCountry != null)
				joUser.put("country", newCountry);
			if (newExpirationDate != null)
				joUser.put("expirationDate", simpleDateFormat.format(newExpirationDate));

			String bodyRequest = joUser.toString();

            mLogger.info("updateUser"
                            + ", mmsURL: " + mmsURL
                            // + ", bodyRequest: " + bodyRequest
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, bodyRequest, outputToBeCompressed);
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

	public Long createWorkspace(String username, String password,
                                String workspaceNameToRegister)
            throws Exception
    {
        String mmsInfo;
        String mmsURL = null;
        try
        {
            mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/workspace"
				+ "?workspaceName=" + java.net.URLEncoder.encode(workspaceNameToRegister, "UTF-8") // requires unescape server side
			;

            String postBodyRequest = null;

            mLogger.info("createWorkspace"
                    + ", mmsURL: " + mmsURL
                    + ", postBodyRequest: " + postBodyRequest
            );

            Date now = new Date();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, postBodyRequest, outputToBeCompressed);
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
		Boolean newEnabled, String newName, String newMaxEncodingPriority,
		String newEncodingPeriod, Long newMaxIngestionsNumber,
		Long newMaxStorageInMB, String newLanguageCode, Date newExpirationDate,
		Boolean newCreateRemoveWorkspace, Boolean newIngestWorkflow, Boolean newCreateProfiles,
		Boolean newDeliveryAuthorization, Boolean newShareWorkspace,
		Boolean newEditMedia, Boolean newEditConfiguration, Boolean newKillEncoding,
		Boolean newCancelIngestionJob, Boolean newEditEncodersPool, Boolean newApplicationRecorder)
        throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/workspace";

            JSONObject joBodyRequest = new JSONObject();
			if (newName != null)
				joBodyRequest.put("workspaceName", newName);
			if (newEnabled != null)
            	joBodyRequest.put("isEnabled", newEnabled);
			if (newMaxEncodingPriority != null)
				joBodyRequest.put("maxEncodingPriority", newMaxEncodingPriority);
			if (newEncodingPeriod != null)
				joBodyRequest.put("encodingPeriod", newEncodingPeriod);
			if (newMaxIngestionsNumber != null)
				joBodyRequest.put("maxIngestionsNumber", newMaxIngestionsNumber);
			if (newMaxStorageInMB != null)
				joBodyRequest.put("maxStorageInMB", newMaxStorageInMB);
			if (newLanguageCode != null)
				joBodyRequest.put("languageCode", newLanguageCode);

			JSONObject joUserAPIKey = new JSONObject();
			joBodyRequest.put("userAPIKey", joUserAPIKey);

			if (newExpirationDate != null)
				joUserAPIKey.put("expirationDate", simpleDateFormat.format(newExpirationDate));
			// all the next fields aare mandatory
			joUserAPIKey.put("createRemoveWorkspace", newCreateRemoveWorkspace);
			joUserAPIKey.put("ingestWorkflow", newIngestWorkflow);
			joUserAPIKey.put("createProfiles", newCreateProfiles);
			joUserAPIKey.put("deliveryAuthorization", newDeliveryAuthorization);
			joUserAPIKey.put("shareWorkspace", newShareWorkspace);
			joUserAPIKey.put("editMedia", newEditMedia);
			joUserAPIKey.put("editConfiguration", newEditConfiguration);
			joUserAPIKey.put("killEncoding", newKillEncoding);
			joUserAPIKey.put("cancelIngestionJob", newCancelIngestionJob);
			joUserAPIKey.put("editEncodersPool", newEditEncodersPool);
			joUserAPIKey.put("applicationRecorder", newApplicationRecorder);

            String bodyRequest = joBodyRequest.toString();

            mLogger.info("updateWorkspace"
				+ ", mmsURL: " + mmsURL
				+ ", bodyRequest: " + bodyRequest
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, bodyRequest, outputToBeCompressed);
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
                    username, password, null, postBodyRequest, outputToBeCompressed);
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
                    username, password, null, jsonWorkflow, outputToBeCompressed);
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
                    username, password, null, outputToBeCompressed);
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
                    username, password, null, jsonEncodingProfile, outputToBeCompressed);
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
                    username, password, null, jsonEncodingProfilesSet, outputToBeCompressed);
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
                    username, password, null, putBodyRequest, outputToBeCompressed);
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
                    username, password, null, putBodyRequest, outputToBeCompressed);
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
						   String publicServerName, String internalServerName,
						   Long port)
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
			joEncoder.put("PublicServerName", publicServerName);
			joEncoder.put("InternalServerName", internalServerName);
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
                    username, password, null, joEncoder.toString(), outputToBeCompressed);
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
							  String publicServerName, String internalServerName,
							  Long port)
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
			joEncoder.put("PublicServerName", publicServerName);
			joEncoder.put("InternalServerName", internalServerName);
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
                    username, password, null, joEncoder.toString(), outputToBeCompressed);
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
                            Boolean runningInfo,	// running and cpu usage info 
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
				+ (runningInfo == null ? "" : ("&runningInfo=" + runningInfo))
                + urlAdminParameters
            ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
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
                    username, password, null, outputToBeCompressed);
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
                    username, password, null, outputToBeCompressed);
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
                    username, password, null, joEncodersPool.toString(), outputToBeCompressed);
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
                    username, password, null, joEncodersPool.toString(), outputToBeCompressed);
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
                    username, password, null, null, outputToBeCompressed);
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
                    username, password, null, outputToBeCompressed);
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
                              Long deliveryCode, String jsonCondition,
                              String orderBy, String jsonOrderBy,
							  JSONObject joResponseFields,
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
                    + (deliveryCode == null ? "" : ("&deliveryCode=" + deliveryCode))
                    + (jsonCondition == null ? "" : ("&jsonCondition=" +  java.net.URLEncoder.encode(jsonCondition, "UTF-8")))
                    + "&orderBy=" + (orderBy == null ? "" : java.net.URLEncoder.encode(orderBy, "UTF-8"))
                    + "&jsonOrderBy=" + (jsonOrderBy == null ? "" : java.net.URLEncoder.encode(jsonOrderBy, "UTF-8"))
                    ;

			String body = null;
			// if ((tagsIn != null && tagsIn.size() > 0)
			//	|| (tagsNotIn != null && tagsNotIn.size() > 0)
			//	|| joResponseFields != null)
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

					if (joResponseFields != null)
						joOtherInputs.put("responseFields", joResponseFields);
				}
				body = joOtherInputs.toString(4);
			}

            mLogger.info("mmsURL: " + mmsURL
                    + ", body: " + body
                    + ", username: " + username
            );

            Date now = new Date();
			if (newMediaItemKey == null && body != null && body.length() > 0)
			{
				String postContentType = null;
				mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, postContentType, timeoutInSeconds, maxRetriesNumber,
						username, password, null, body, outputToBeCompressed);
			}
			else
			{
				mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
						username, password, null, outputToBeCompressed);
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
                    username, password, null, outputToBeCompressed);
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
			if (physicalPathKey == null)
			{
				String errorMessage = "physicalPathKey is null";
				mLogger.error(errorMessage);

				throw new Exception(errorMessage);
			}

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/mediaItem"
				+ "?physicalPathKey=" + physicalPathKey
				;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber, 
				username, password, null, outputToBeCompressed);
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
                    username, password, null, outputToBeCompressed);
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
                    username, password, null, sEdit, outputToBeCompressed);
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
                    username, password, null, sEdit, outputToBeCompressed);
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
                    username, password, null, outputToBeCompressed);
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
                        Boolean bLiveRecordingChunk, String tagNameFilter, List<String> tagsList)
            throws Exception
    {
        Long numFound;

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

			String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/tag"
				+ "?start=" + startIndex
				+ "&rows=" + pageSize
				+ liveRecordingChunkParameter
				+ "&contentType=" + (contentType == null ? "" : contentType)
				+ (tagNameFilter != null ? ("&tagNameFilter=" + tagNameFilter) : "")
            ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
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
                    username, password, null, outputToBeCompressed);
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
                    username, password, null, outputToBeCompressed);
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
								 Date startScheduleDate,
                                 String status,             // completed or notCompleted
                                 String ingestionType, 
								 String configurationLabel,	// used in case of Live-Proxy
								 String outputChannelLabel,	// used in case of Live-Grid
								 Long deliveryCode,	// used in case of Live-Recorder
								 Boolean broadcastIngestionJobKeyNotNull,	// used in case of Broadcaster
								 // String jsonParametersCondition, // altamente sconsigliato perch?? poco performante
                                 boolean ingestionDateAscending,
                                 boolean dependencyInfo,
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
                    + (configurationLabel == null || configurationLabel.isEmpty() ? "" : ("&configurationLabel=" + 
                    	java.net.URLEncoder.encode(configurationLabel, "UTF-8"))) // requires unescape server side
					+ (outputChannelLabel == null || outputChannelLabel.isEmpty() ? "" : ("&outputChannelLabel=" + 
                    	java.net.URLEncoder.encode(outputChannelLabel, "UTF-8"))) // requires unescape server side
					+ (deliveryCode == null ? "" : ("&deliveryCode=" + deliveryCode))
					+ (broadcastIngestionJobKeyNotNull == null ? "" : ("&broadcastIngestionJobKeyNotNull=" + broadcastIngestionJobKeyNotNull))
                    // + "&jsonParametersCondition=" + (jsonParametersCondition == null || jsonParametersCondition.isEmpty()
                    //	? "" : java.net.URLEncoder.encode(jsonParametersCondition, "UTF-8")) // requires unescape server side
                    + "&asc=" + (ingestionDateAscending ? "true" : "false")
                    + "&dependencyInfo=" + (dependencyInfo ? "true" : "false")
                    + "&ingestionJobOutputs=" + (ingestionJobOutputs ? "true" : "false")
                    + (start == null ? "" : ("&startIngestionDate=" + simpleDateFormat.format(start)))
                    + (end == null ? "" : ("&endIngestionDate=" + simpleDateFormat.format(end)))
                    + (startScheduleDate == null ? "" : ("&startScheduleDate=" + simpleDateFormat.format(startScheduleDate)))
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            /*
            2020-06-07: MMS GUI asks for LiveRecorder (ingestionJobs), the return contains all the MediaItems Output that,
                for each live recorder, are really a lot.
                So, in this scenario, make sure to have a long timeoutInSeconds, otherwise it will raise a timeout exception
             */
            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
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
                    username, password, null, outputToBeCompressed);
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
            joBodyRequest.put("scheduleStart", simpleDateFormat.format(newRecordingStart));
            joBodyRequest.put("scheduleEnd", simpleDateFormat.format(newRecordingEnd));
            joBodyRequest.put("RecordingVirtualVOD", newRecordingVirtualVod);

            String bodyRequest = joBodyRequest.toString();

            mLogger.info("updateIngestionJob_LiveRecorder"
                    + ", mmsURL: " + mmsURL
                    + ", bodyRequest: " + bodyRequest
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, bodyRequest, outputToBeCompressed);
            mLogger.info("updateWorkspace. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "updateIngestionJob_LiveRecorder failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void changeLiveProxyPlaylist(String username, String password,
        Long broadcasterIngestionJobKey,
        List<BroadcastPlaylistItem> broadcastPlaylistItems)
        throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/ingestionJob/liveProxy/playlist/" + broadcasterIngestionJobKey;

            JSONArray jaBodyRequest = new JSONArray();

			for (BroadcastPlaylistItem broadcastPlaylistItem: broadcastPlaylistItems)
				jaBodyRequest.put(broadcastPlaylistItem.getJson2());

            String bodyRequest = jaBodyRequest.toString();

            mLogger.info("changeLiveProxyPlaylist"
                    + ", mmsURL: " + mmsURL
                    + ", bodyRequest: " + bodyRequest
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, bodyRequest, outputToBeCompressed);
            mLogger.info("changeLiveProxyPlaylist. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "changeLiveProxyPlaylist failed. Exception: " + e;
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
                    username, password, null, outputToBeCompressed);
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
                    username, password, null, outputToBeCompressed);
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
                    username, password, null, outputToBeCompressed);
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
                    username, password, null, outputToBeCompressed);
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
                    username, password, null, outputToBeCompressed);
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
                    username, password, null, outputToBeCompressed);
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
        Map<String, BulkOfDeliveryURLData> bulkOfDeliveryURLDataMapByUniqueName = new HashMap<>();
        Map<Long, BulkOfDeliveryURLData> bulkOfDeliveryURLDataMapByMediaItemKey = new HashMap<>();
        Map<Long, BulkOfDeliveryURLData> bulkOfDeliveryURLDataMapByLiveIngestionJobKey = new HashMap<>();
        try
        {
            /*
            {
                "mediaItemKeyList" : [
                    {
                        "mediaItemKey": 123,
                        "encodingProfileKey": 123,
						"encodingProfileLabel": "..."
                    },
                    ...
                ],
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
                JSONArray jaMediaItemList = new JSONArray();
                joDeliveryAuthorizationDetails.put("mediaItemKeyList", jaMediaItemList);

				JSONArray jaUniqueNameList = new JSONArray();
                joDeliveryAuthorizationDetails.put("uniqueNameList", jaUniqueNameList);

                JSONArray jaLiveIngestionJobKeyList = new JSONArray();
                joDeliveryAuthorizationDetails.put("liveIngestionJobKeyList", jaLiveIngestionJobKeyList);

                for (BulkOfDeliveryURLData bulkOfDeliveryURLData: bulkOfDeliveryURLDataList)
                {
                    if (bulkOfDeliveryURLData.getMediaItemKey() != null)
                    {
                        JSONObject joMediaItemKey = new JSONObject();
                        jaMediaItemList.put(joMediaItemKey);

                        joMediaItemKey.put("mediaItemKey", bulkOfDeliveryURLData.getMediaItemKey());
						if (bulkOfDeliveryURLData.getEncodingProfileKey() != null
							&& bulkOfDeliveryURLData.getEncodingProfileKey() != -1)
							joMediaItemKey.put("encodingProfileKey", bulkOfDeliveryURLData.getEncodingProfileKey());
						else
							joMediaItemKey.put("encodingProfileLabel", bulkOfDeliveryURLData.getEncodingProfileLabel());

						if (bulkOfDeliveryURLData.getDeliveryType() != null)
							joMediaItemKey.put("deliveryType", bulkOfDeliveryURLData.getDeliveryType());

						if (bulkOfDeliveryURLData.getFilteredByStatistic() != null)
							joMediaItemKey.put("filteredByStatistic", bulkOfDeliveryURLData.getFilteredByStatistic());

						bulkOfDeliveryURLDataMapByMediaItemKey.put(bulkOfDeliveryURLData.getMediaItemKey(), bulkOfDeliveryURLData);
                    }
                    else if (bulkOfDeliveryURLData.getUniqueName() != null)
                    {
                        JSONObject joUniqueName = new JSONObject();
                        jaUniqueNameList.put(joUniqueName);

                        joUniqueName.put("uniqueName", bulkOfDeliveryURLData.getUniqueName());
						if (bulkOfDeliveryURLData.getEncodingProfileKey() != null
							&& bulkOfDeliveryURLData.getEncodingProfileKey() != -1)
							joUniqueName.put("encodingProfileKey", bulkOfDeliveryURLData.getEncodingProfileKey());
						else
							joUniqueName.put("encodingProfileLabel", bulkOfDeliveryURLData.getEncodingProfileLabel());

						if (bulkOfDeliveryURLData.getDeliveryType() != null)
							joUniqueName.put("deliveryType", bulkOfDeliveryURLData.getDeliveryType());

						if (bulkOfDeliveryURLData.getFilteredByStatistic() != null)
							joUniqueName.put("filteredByStatistic", bulkOfDeliveryURLData.getFilteredByStatistic());

						bulkOfDeliveryURLDataMapByUniqueName.put(bulkOfDeliveryURLData.getUniqueName(), bulkOfDeliveryURLData);
                    }
                    else if (bulkOfDeliveryURLData.getLiveIngestionJobKey() != null)
                    {
                        JSONObject joLiveIngestionJobKey = new JSONObject();
                        jaLiveIngestionJobKeyList.put(joLiveIngestionJobKey);

                        joLiveIngestionJobKey.put("ingestionJobKey", bulkOfDeliveryURLData.getLiveIngestionJobKey());
                        if (bulkOfDeliveryURLData.getLiveDeliveryCode() != null)
                            joLiveIngestionJobKey.put("deliveryCode", bulkOfDeliveryURLData.getLiveDeliveryCode());

						if (bulkOfDeliveryURLData.getDeliveryType() != null)
							joLiveIngestionJobKey.put("deliveryType", bulkOfDeliveryURLData.getDeliveryType());

						if (bulkOfDeliveryURLData.getFilteredByStatistic() != null)
							joLiveIngestionJobKey.put("filteredByStatistic", bulkOfDeliveryURLData.getFilteredByStatistic());

                        bulkOfDeliveryURLDataMapByLiveIngestionJobKey.put(bulkOfDeliveryURLData.getLiveIngestionJobKey(), bulkOfDeliveryURLData);
                    }
                }
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/delivery/bulk"
                    + "?ttlInSeconds=" + ttlInSeconds
                    + "&maxRetries=" + maxRetries
            ;
            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            String postContentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, postContentType,
                    timeoutInSeconds, maxRetriesNumber,
                    username, password, null,
                    joDeliveryAuthorizationDetails.toString(), outputToBeCompressed);
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

            if (joDeliveryURLList.has("mediaItemKeyList"))
            {
                JSONArray jaMediaItemKeyList = joDeliveryURLList.getJSONArray("mediaItemKeyList");
                for (int mediaItemKeyIndex = 0; mediaItemKeyIndex < jaMediaItemKeyList.length(); mediaItemKeyIndex++)
                {
                    JSONObject joMediaItemKey = jaMediaItemKeyList.getJSONObject(mediaItemKeyIndex);

                    if (joMediaItemKey.has("mediaItemKey") && joMediaItemKey.has("deliveryURL")
                        && !joMediaItemKey.isNull("mediaItemKey") && !joMediaItemKey.isNull("deliveryURL"))
                    {
                        BulkOfDeliveryURLData bulkOfDeliveryURLData
                                = bulkOfDeliveryURLDataMapByMediaItemKey.get(joMediaItemKey.getLong("mediaItemKey"));
                        bulkOfDeliveryURLData.setDeliveryURL(joMediaItemKey.getString("deliveryURL"));
                    }
                }
            }

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
                                = bulkOfDeliveryURLDataMapByUniqueName.get(joUniqueName.getString("uniqueName"));
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
                                = bulkOfDeliveryURLDataMapByLiveIngestionJobKey.get(joLiveIngestionJobKey.getLong("ingestionJobKey"));
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
                                 
		long ttlInSeconds, int maxRetries,
		// MMS_Token: delivery by MMS with a Token
		// MMS_SignedToken: delivery by MMS with a signed URL
		// AWSCloudFront: delivery by AWS CloudFront without a signed URL
		// AWSCloudFront_Signed: delivery by AWS CloudFront with a signed URL
		String deliveryType,

		Boolean save,
		Boolean filteredByStatistic)
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
                        + "&filteredByStatistic=" + (filteredByStatistic == null ? false : filteredByStatistic.toString())
						+ "&deliveryType=" + (deliveryType == null || deliveryType.isEmpty() ? "MMS_SignedToken" : deliveryType)
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
								+ "&filteredByStatistic=" + (filteredByStatistic == null ? false : filteredByStatistic.toString())
								+ "&deliveryType=" + (deliveryType == null || deliveryType.isEmpty() ? "MMS_SignedToken" : deliveryType)
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
								+ "&filteredByStatistic=" + (filteredByStatistic == null ? false : filteredByStatistic.toString())
								+ "&deliveryType=" + (deliveryType == null || deliveryType.isEmpty() ? "MMS_SignedToken" : deliveryType)
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
							+ "&filteredByStatistic=" + (filteredByStatistic == null ? false : filteredByStatistic.toString())
							+ "&deliveryType=" + (deliveryType == null || deliveryType.isEmpty() ? "MMS_SignedToken" : deliveryType)
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
								+ "&filteredByStatistic=" + (filteredByStatistic == null ? false : filteredByStatistic.toString())
								+ "&deliveryType=" + (deliveryType == null || deliveryType.isEmpty() ? "MMS_SignedToken" : deliveryType)
                                + "&redirect=false"
                        ;
                    }
                }
            }

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
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
			mLogger.info("deliveryURL: " + deliveryURL);
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
		Long deliveryCode,
		Long ttlInSeconds, // if null -> 3600 * 24
		Long maxRetries,    // if null -> 3600 * 24 / 5

		// IN REALTA' deliveryType nel caso di live viene utilizzato parzialmente perch??:
		//	- in caso di RTMP, il campo PlayURL nell'IngestionJob decide l'utl di delivery.
		//		Infatti in questo scenario, solo chi crea il Task pu?? sapere la deliveryURL  
		//	- in caso di HLS viene invece utilizzato questo campo che potr?? variare tra
		//		tra MMS_Token o MMS_SignedToken
		// MMS_Token: delivery by MMS with a Token
		// MMS_SignedToken: delivery by MMS with a signed URL
		// AWSCloudFront: delivery by AWS CloudFront with a signed URL
		// AWSCloudFront_Signed: delivery by AWS CloudFront with a signed URL
		String deliveryType,
		Boolean filteredByStatistic
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
					+ "&deliveryType=" + (deliveryType == null || deliveryType.isEmpty() ? "MMS_SignedToken" : deliveryType)
                    + (deliveryCode != null ? ("&deliveryCode=" +  deliveryCode) : "")
                    + "&redirect=false"
					+ "&filteredByStatistic=" + (filteredByStatistic == null ? false : filteredByStatistic.toString())
					;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
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
                    username, password, null, outputToBeCompressed);
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
                    username, password, null, outputToBeCompressed);
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
                    username, password, null, workflowAsLibrary, outputToBeCompressed);
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
                               String label, String tokenType,
							   String refreshToken, String accessToken)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonYouTubeConf;
            {
                JSONObject joYouTubeConf = new JSONObject();

                joYouTubeConf.put("label", label);
                joYouTubeConf.put("tokenType", tokenType);
				if (tokenType.equalsIgnoreCase("RefreshToken"))
                	joYouTubeConf.put("refreshToken", refreshToken);
				else // if (tokenType.equalsIgnoreCase("AccessToken"))
					joYouTubeConf.put("accessToken", accessToken);

                jsonYouTubeConf = joYouTubeConf.toString(1);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/youtube";

            mLogger.info("addYouTubeConf"
                            + ", mmsURL: " + mmsURL
                            + ", jsonYouTubeConf: " + jsonYouTubeConf
            );

            Date now = new Date();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonYouTubeConf, outputToBeCompressed);
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
                               Long confKey, String label, 
							   String tokenType, String refreshToken, String accessToken)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonYouTubeConf;
            {
                JSONObject joYouTubeConf = new JSONObject();

				if (label != null)
                	joYouTubeConf.put("label", label);
				if (tokenType != null)
					joYouTubeConf.put("tokenType", tokenType);
				if (refreshToken != null)
					joYouTubeConf.put("refreshToken", refreshToken);
				if (accessToken != null)
					joYouTubeConf.put("accessToken", accessToken);

                jsonYouTubeConf = joYouTubeConf.toString(1);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/youtube/" + confKey;

            mLogger.info("modifyYouTubeConf"
                    + ", mmsURL: " + mmsURL
                    + ", jsonYouTubeConf: " + jsonYouTubeConf
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonYouTubeConf, outputToBeCompressed);
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
                    username, password, null, outputToBeCompressed);
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
                    username, password, null, jsonFacebookConf, outputToBeCompressed);
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
                    username, password, null, jsonFacebookConf, outputToBeCompressed);
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
                    username, password, null, outputToBeCompressed);
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

	public Long addStream(String username, String password,
		String label, 
		String sourceType,
		String encodersPoolLabel,
		String url, 
		String pushProtocol,
		Long pushEncoderKey,
		String pushServerName,
		Long pushServerPort,
		String pushURI,
		Long pushListenTimeout,
		Long captureLiveVideoDeviceNumber,
		String captureLiveVideoInputFormat,
		Long captureLiveFrameRate,
		Long captureLiveWidth,
		Long captureLiveHeight,
		Long captureLiveAudioDeviceNumber,
		Long captureLiveChannelsNumber,
		Long sourceSATConfKey,
		String type, String description,
        String name, String region, String country,
        Long imageMediaItemKey, String imageUniqueName, Long position,
        String userData)
        throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonStream;
            {
                JSONObject joStreamConf = new JSONObject();

                joStreamConf.put("label", label);
                joStreamConf.put("sourceType", sourceType);
				if (encodersPoolLabel != null)
                	joStreamConf.put("encodersPoolLabel", encodersPoolLabel);
                if (url != null)
                	joStreamConf.put("url", url);
				if (pushProtocol != null)
                	joStreamConf.put("pushProtocol", pushProtocol);
				if (pushEncoderKey != null)
                	joStreamConf.put("pushEncoderKey", pushEncoderKey);
				if (pushServerName != null)
                	joStreamConf.put("pushServerName", pushServerName);
				if (pushServerPort != null)
                	joStreamConf.put("pushServerPort", pushServerPort);
				if (pushURI != null)
                	joStreamConf.put("pushURI", pushURI);
				if (pushListenTimeout != null)
                	joStreamConf.put("pushListenTimeout", pushListenTimeout);
				if (captureLiveVideoDeviceNumber != null)
                	joStreamConf.put("captureLiveVideoDeviceNumber", captureLiveVideoDeviceNumber);
				if (captureLiveVideoInputFormat != null)
                	joStreamConf.put("captureLiveVideoInputFormat", captureLiveVideoInputFormat);
				if (captureLiveFrameRate != null)
                	joStreamConf.put("captureLiveFrameRate", captureLiveFrameRate);
				if (captureLiveWidth != null)
                	joStreamConf.put("captureLiveWidth", captureLiveWidth);
				if (captureLiveHeight != null)
                	joStreamConf.put("captureLiveHeight", captureLiveHeight);
				if (captureLiveAudioDeviceNumber != null)
                	joStreamConf.put("captureLiveAudioDeviceNumber", captureLiveAudioDeviceNumber);
				if (captureLiveChannelsNumber != null)
                	joStreamConf.put("captureLiveChannelsNumber", captureLiveChannelsNumber);
				if (sourceSATConfKey != null)
                	joStreamConf.put("sourceSATConfKey", sourceSATConfKey);
				if (type != null)
                    joStreamConf.put("type", type);
                if (description != null)
                    joStreamConf.put("description", description);
                if (name != null)
                    joStreamConf.put("name", name);
                if (region != null)
                    joStreamConf.put("region", region);
                if (country != null)
                    joStreamConf.put("country", country);
                if (imageMediaItemKey != null)
                    joStreamConf.put("imageMediaItemKey", imageMediaItemKey);
                if (imageUniqueName != null)
                    joStreamConf.put("imageUniqueName", imageUniqueName);
                if (position != null)
                    joStreamConf.put("position", position);
                if (userData != null && !userData.isEmpty())
                    joStreamConf.put("userData", new JSONObject(userData));

					jsonStream = joStreamConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/stream";

            mLogger.info("addStream"
                    + ", mmsURL: " + mmsURL
                    + ", jsonStream: " + jsonStream
            );

            Date now = new Date();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonStream, outputToBeCompressed);
            mLogger.info("addStream. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addStream MMS failed. Exception: " + e;
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

    public void modifyStream(String username, String password,
                                  Long confKey, String label, 
								  String sourceType,
								  String encodersPoolLabel,
								  String url, 
								  String pushProtocol,
								  Long pushEncoderKey,
								  String pushServerName,
								  Long pushServerPort,
								  String pushURI,
								  Long pushListenTimeout,
								  Long captureLiveVideoDeviceNumber,
								  String captureLiveVideoInputFormat,
								  Long captureLiveFrameRate,
								  Long captureLiveWidth,
								  Long captureLiveHeight,
								  Long captureLiveAudioDeviceNumber,
								  Long captureLiveChannelsNumber,
								  Long sourceSATConfKey,
								String type, String description,
                                  String name, String region, String country,
                                  Long imageMediaItemKey, String imageUniqueName, Long position,
                                  String userData)
            throws Exception
    {

        String mmsInfo;
        try
        {
            mLogger.info("modifyStream"
                    + ", username: " + username
                    + ", label: " + label
                    + ", url: " + url
                    + ", type: " + type
                    + ", sourceType: " + sourceType
                    + ", description: " + description
                    + ", name: " + name
                    + ", region: " + region
                    + ", country: " + country
                    + ", imageMediaItemKey: " + imageMediaItemKey
                    + ", imageUniqueName: " + imageUniqueName
                    + ", position: " + position
                    + ", userData: " + userData
            );

            String jsonStreamConf;
            {
                JSONObject joStreamConf = new JSONObject();

				if (label != null)
					joStreamConf.put("label", label);
				if (sourceType != null)
					joStreamConf.put("sourceType", sourceType);
				if (encodersPoolLabel != null)
                	joStreamConf.put("encodersPoolLabel", encodersPoolLabel);
                if (url != null)
                	joStreamConf.put("url", url);
				if (pushProtocol != null)
                	joStreamConf.put("pushProtocol", pushProtocol);
				if (pushEncoderKey != null)
                	joStreamConf.put("pushEncoderKey", pushEncoderKey);
				if (pushServerName != null)
                	joStreamConf.put("pushServerName", pushServerName);
				if (pushServerPort != null)
                	joStreamConf.put("pushServerPort", pushServerPort);
				if (pushURI != null)
                	joStreamConf.put("pushURI", pushURI);
				if (pushListenTimeout != null)
                	joStreamConf.put("pushListenTimeout", pushListenTimeout);
				if (captureLiveVideoDeviceNumber != null)
                	joStreamConf.put("captureLiveVideoDeviceNumber", captureLiveVideoDeviceNumber);
				if (captureLiveVideoInputFormat != null)
                	joStreamConf.put("captureLiveVideoInputFormat", captureLiveVideoInputFormat);
				if (captureLiveFrameRate != null)
                	joStreamConf.put("captureLiveFrameRate", captureLiveFrameRate);
				if (captureLiveWidth != null)
                	joStreamConf.put("captureLiveWidth", captureLiveWidth);
				if (captureLiveHeight != null)
                	joStreamConf.put("captureLiveHeight", captureLiveHeight);
				if (captureLiveAudioDeviceNumber != null)
                	joStreamConf.put("captureLiveAudioDeviceNumber", captureLiveAudioDeviceNumber);
				if (captureLiveChannelsNumber != null)
                	joStreamConf.put("captureLiveChannelsNumber", captureLiveChannelsNumber);
				if (sourceSATConfKey != null)
                	joStreamConf.put("sourceSATConfKey", sourceSATConfKey);
                if (type != null)
                    joStreamConf.put("type", type);
                if (description != null)
                    joStreamConf.put("description", description);
                if (name != null)
                    joStreamConf.put("name", name);
                if (region != null)
                    joStreamConf.put("region", region);
                if (country != null)
                    joStreamConf.put("country", country);
                if (imageMediaItemKey != null)
                    joStreamConf.put("imageMediaItemKey", imageMediaItemKey);
                if (imageUniqueName != null)
                    joStreamConf.put("imageUniqueName", imageUniqueName);
                if (position != null)
                    joStreamConf.put("position", position);
				else	// in order to be able to set position as null into DB
					joStreamConf.put("position", JSONObject.NULL);
                if (userData != null && !userData.isEmpty())
                    joStreamConf.put("userData", new JSONObject(userData));

                jsonStreamConf = joStreamConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/stream/" + confKey;

            mLogger.info("modifyStream"
                    + ", mmsURL: " + mmsURL
                    + ", jsonStreamConf: " + jsonStreamConf
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonStreamConf, outputToBeCompressed);
            mLogger.info("modifyStream. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "modifyStream MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void removeStream(String username, String password,
                                   Long confKey)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/stream/" + confKey;

            mLogger.info("removeStream"
                    + ", mmsURL: " + mmsURL
                    + ", confKey: " + confKey
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null);
            mLogger.info("removeStream. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeStream MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public Long getStream(String username, String password,
                               long startIndex, long pageSize,
                               Long confKey, String label,
                               String url,
                               String sourceType, String type, 
							   String name, String region, String country,
                               String labelOrder,   // asc or desc
                               List<Stream> streamList)
            throws Exception
    {
        Long numFound;

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/stream"
                    + (confKey == null ? "" : ("/" + confKey))
                    + "?start=" + startIndex
                    + "&rows=" + pageSize
                    + "&label=" + (label == null ? "" : java.net.URLEncoder.encode(label, "UTF-8")) // requires unescape server side
                    + "&url=" + (url == null ? "" : java.net.URLEncoder.encode(url, "UTF-8"))
                    + "&sourceType=" + (sourceType == null ? "" : sourceType)
                    + "&type=" + (type == null ? "" : java.net.URLEncoder.encode(type, "UTF-8"))
                    + "&name=" + (name == null ? "" : java.net.URLEncoder.encode(name, "UTF-8"))
                    + "&region=" + (region == null ? "" : java.net.URLEncoder.encode(region, "UTF-8"))
                    + "&country=" + (country == null ? "" : java.net.URLEncoder.encode(country, "UTF-8"))
                    + "&labelOrder=" + (labelOrder == null ? "" : labelOrder)
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getStream. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
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

            JSONArray jaStreams = joResponse.getJSONArray("streams");

            mLogger.info("jaStreams.length(): " + jaStreams.length()
            );

            streamList.clear();

            for (int streamIndex = 0; streamIndex < jaStreams.length(); streamIndex++)
            {
                Stream stream = new Stream();

                JSONObject streamInfo = jaStreams.getJSONObject(streamIndex);

                fillStream(stream, streamInfo);

                streamList.add(stream);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing streams failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return numFound;
    }

    public Stream getStream(String username, String password,
                               Long confKey)
            throws Exception
    {
        Stream stream = null;

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/stream"
                    + "/" + confKey
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getStream. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
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

            JSONArray jaStreams = joResponse.getJSONArray("streams");

            mLogger.info("jaStreams.length(): " + jaStreams.length()
            );

            if (numFound > 1 || jaStreams.length() > 1)
            {
                String errorMessage = "Wrong API response"
                        + ", numFound: " + numFound
                        + ", jaStreams.length: " + jaStreams.length()
                        ;
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            if (jaStreams.length() == 1)
            {
                stream = new Stream();

                JSONObject streamInfo = jaStreams.getJSONObject(0);

                fillStream(stream, streamInfo);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing streams failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return stream;
    }

    public Long addSourceSATStream(String username, String password,
                                        Long serviceId, Long networkId, Long transportStreamId,
                                        String name, String satellite, Long frequency, String lnb,
                                        Long videoPid, String audioPids, Long audioItalianPid, Long audioEnglishPid, Long teletextPid,
                                        String modulation, String polarization, Long symbolRate, String country, String deliverySystem)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonStream;
            {
                JSONObject joStream = new JSONObject();

                joStream.put("serviceId", serviceId);
                joStream.put("networkId", networkId);
                joStream.put("transportStreamId", transportStreamId);
                joStream.put("name", name);
                joStream.put("satellite", satellite);
                joStream.put("frequency", frequency);
                joStream.put("lnb", lnb);
                joStream.put("videoPid", videoPid);
                joStream.put("audioPids", audioPids);
                if (audioItalianPid != null)
                    joStream.put("audioItalianPid", audioItalianPid);
                if (audioEnglishPid != null)
                    joStream.put("audioEnglishPid", audioEnglishPid);
                if (teletextPid != null)
                    joStream.put("teletextPid", teletextPid);
                if (modulation != null && !modulation.isEmpty())
                    joStream.put("modulation", modulation);
                joStream.put("polarization", polarization);
                joStream.put("symbolRate", symbolRate);
                if (country != null && !country.isEmpty())
                    joStream.put("country", country);
                if (deliverySystem != null && !deliverySystem.isEmpty())
                    joStream.put("deliverySystem", deliverySystem);

                jsonStream = joStream.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/sourceSatStream";

            mLogger.info("addSourceSATStream"
                    + ", mmsURL: " + mmsURL
                    + ", jsonStream: " + jsonStream
            );

            Date now = new Date();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonStream, outputToBeCompressed);
            mLogger.info("addSourceSATStream. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addSourceSATStream MMS failed. Exception: " + e;
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

    public void modifySourceSATStream(String username, String password,
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
            mLogger.info("modifySourceSATStream"
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

            String jsonStream;
            {
                JSONObject joStream = new JSONObject();

                if (serviceId != null)
                    joStream.put("serviceId", serviceId);
                if (networkId != null)
                    joStream.put("networkId", networkId);
                if (transportStreamId != null)
                    joStream.put("transportStreamId", transportStreamId);
                if (name != null && !name.isEmpty())
                    joStream.put("name", name);
                if (satellite != null && !satellite.isEmpty())
                    joStream.put("satellite", satellite);
                if (frequency != null)
                    joStream.put("frequency", frequency);
                if (lnb != null && !lnb.isEmpty())
                    joStream.put("lnb", lnb);
                if (videoPid != null)
                    joStream.put("videoPid", videoPid);
                if (audioPids != null && !audioPids.isEmpty())
                    joStream.put("audioPids", audioPids);
                if (audioItalianPid != null)
                    joStream.put("audioItalianPid", audioItalianPid);
                if (audioEnglishPid != null)
                    joStream.put("audioEnglishPid", audioEnglishPid);
                if (teletextPid != null)
                    joStream.put("teletextPid", teletextPid);
                if (modulation != null && !modulation.isEmpty())
                    joStream.put("modulation", modulation);
                if (polarization != null && !polarization.isEmpty())
                    joStream.put("polarization", polarization);
                if (symbolRate != null)
                    joStream.put("symbolRate", symbolRate);
                if (country != null && !country.isEmpty())
                    joStream.put("country", country);
                if (deliverySystem != null && !deliverySystem.isEmpty())
                    joStream.put("deliverySystem", deliverySystem);

                jsonStream = joStream.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/sourceSatStream/" + confKey;

            mLogger.info("modifySourceSATStream"
                    + ", mmsURL: " + mmsURL
                    + ", jsonStream: " + jsonStream
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonStream, outputToBeCompressed);
            mLogger.info("modifySourceSATStream. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "modifySourceSATStream MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void removeSourceSATStream(String username, String password,
                                     Long confKey)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/sourceSatStream/" + confKey;

            mLogger.info("removeSATStream"
                    + ", mmsURL: " + mmsURL
                    + ", confKey: " + confKey
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null);
            mLogger.info("removeSourceSATStream. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeSourceSATStream MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public Long getSourceSATStream(String username, String password,
                                  long startIndex, long pageSize,
                                  Long confKey,
                                  Long serviceId, String name, Long frequency, String lnb,
                                  Long videoPid, String audioPids,
                                  String nameOrder,   // asc or desc
                                  List<SourceSATStream> sourceSATStream)
            throws Exception
    {
        Long numFound;

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/sourceSatStream"
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
                    username, password, null, outputToBeCompressed);
            mLogger.info("getSourceSATStream. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
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

            JSONArray jaStreams = joResponse.getJSONArray("sourceSATStreams");

            mLogger.info("jaStreams.length(): " + jaStreams.length()
            );

            sourceSATStream.clear();

            for (int streamIndex = 0; streamIndex < jaStreams.length(); streamIndex++)
            {
                SourceSATStream sourceSatStream = new SourceSATStream();

                JSONObject streamInfo = jaStreams.getJSONObject(streamIndex);

                fillSourceSATStream(sourceSatStream, streamInfo);

                sourceSATStream.add(sourceSatStream);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing sourceSATStreams failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return numFound;
    }

    public SourceSATStream getSourceSATStream(String username, String password,
                                            Long serviceId)
            throws Exception
    {
        SourceSATStream stream = null;

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/sourceSatStream"
                    + "/" + serviceId
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getSourceSATStream. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
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

            JSONArray jaStreams = joResponse.getJSONArray("sourceSATStreams");

            mLogger.info("jaStreams.length(): " + jaStreams.length()
            );

            if (numFound > 1 || jaStreams.length() > 1)
            {
                String errorMessage = "Wrong API response"
                        + ", numFound: " + numFound
                        + ", jaStreams.length: " + jaStreams.length()
                        ;
                mLogger.error(errorMessage);

                throw new Exception(errorMessage);
            }

            if (jaStreams.length() == 1)
            {
                stream = new SourceSATStream();

                JSONObject streamInfo = jaStreams.getJSONObject(0);

                fillSourceSATStream(stream, streamInfo);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing sourceSATStreams failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return stream;
    }

    public void addAWSChannelConf(String username, String password,
        String label, String channelId, String rtmpURL, String playURL, String type)
 		throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonAWSChannelConf;
            {
                JSONObject joAWSChannelConf = new JSONObject();

                joAWSChannelConf.put("label", label);
                joAWSChannelConf.put("channelId", channelId);
                joAWSChannelConf.put("rtmpURL", rtmpURL);
                joAWSChannelConf.put("playURL", playURL);
                joAWSChannelConf.put("type", type);

                jsonAWSChannelConf = joAWSChannelConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/aws/channel";

            mLogger.info("addAWSChannelConf"
                            + ", mmsURL: " + mmsURL
                            + ", jsonAWSChannelConf: " + jsonAWSChannelConf
            );

            Date now = new Date();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonAWSChannelConf, outputToBeCompressed);
            mLogger.info("addAWSChannelConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addAWSChannelConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void modifyAWSChannelConf(String username, String password,
        Long confKey, String label, String channelId, String rtmpURL, String playURL, String type)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonAWSChannelConf;
            {
                JSONObject joAWSChannelConf = new JSONObject();

                joAWSChannelConf.put("label", label);
                joAWSChannelConf.put("channelId", channelId);
                joAWSChannelConf.put("rtmpURL", rtmpURL);
                joAWSChannelConf.put("playURL", playURL);
                joAWSChannelConf.put("type", type);

                jsonAWSChannelConf = joAWSChannelConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/aws/channel/" + confKey;

            mLogger.info("modifyAWSChannelConf"
                            + ", mmsURL: " + mmsURL
                            + ", jsonAWSChannelConf: " + jsonAWSChannelConf
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonAWSChannelConf, outputToBeCompressed);
            mLogger.info("modifyAWSChannelConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "modifyAWSChannelConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void removeAWSChannelConf(String username, String password,
        Long confKey)
        throws Exception
    {

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/aws/channel/" + confKey;

            mLogger.info("removeAWSChannelConf"
                + ", mmsURL: " + mmsURL
                + ", confKey: " + confKey
            );

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null);
            mLogger.info("removeAWSChannelConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeAWSChannelConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public List<AWSChannelConf> getAWSChannelConf(String username, String password)
            throws Exception
    {
        List<AWSChannelConf> awsChannelConfList = new ArrayList<>();

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/aws/channel";

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getAWSChannelConf. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
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
            JSONArray jaAWSChannelConf = joResponse.getJSONArray("awsChannelConf");

            mLogger.info("jaAWSChannelConf.length(): " + jaAWSChannelConf.length());

            awsChannelConfList.clear();

            for (int confIndex = 0; confIndex < jaAWSChannelConf.length(); confIndex++)
            {
                AWSChannelConf awsChannelConf = new AWSChannelConf();

                JSONObject awsChannelConfInfo = jaAWSChannelConf.getJSONObject(confIndex);

                fillAWSChannelConf(awsChannelConf, awsChannelConfInfo);

                awsChannelConfList.add(awsChannelConf);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing awsChannelConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return awsChannelConfList;
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
                    username, password, null, jsonFTPConf, outputToBeCompressed);
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
                    username, password, null, jsonFTPConf, outputToBeCompressed);
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
                    username, password, null, outputToBeCompressed);
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
                    username, password, null, jsonEMailConf, outputToBeCompressed);
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
                    username, password, null, jsonEMailConf, outputToBeCompressed);
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
                    username, password, null, outputToBeCompressed);
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

	public void addRequestStatistic(String username, String password,
		String userId, 
		// physicalPathKey or confStreamKey has to be present
		Long physicalPathKey, Long confStreamKey,
		String title
	)
        throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonStatistic;
            {
                JSONObject joStatistic = new JSONObject();

                joStatistic.put("userId", userId);
				if (physicalPathKey != null)
					joStatistic.put("physicalPathKey", physicalPathKey);
				if (confStreamKey != null)
					joStatistic.put("confStreamKey", confStreamKey);
				joStatistic.put("title", title);

				jsonStatistic = joStatistic.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/statistic/request";

            mLogger.info("addRequestStatistic"
                + ", mmsURL: " + mmsURL
                + ", jsonStatistic: " + jsonStatistic
            );

            Date now = new Date();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonStatistic, outputToBeCompressed);
            mLogger.info("addRequestStatistic. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addRequestStatistic MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public Long getRequestStatistics(String username, String password,
		String userId, String title, Date startStatisticDate, Date endStatisticDate,
		long startIndex, long pageSize,
		List<RequestStatistic> requestStatisticsList)
		throws Exception
    {
		Long numFound;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/statistic/request"
				+ "?start=" + startIndex
				+ "&rows=" + pageSize
				+ (userId != null ? ("&userId=" + userId) : "")
				+ (title != null ? ("&title=" + title) : "")
				+ (startStatisticDate != null ? ("&startStatisticDate=" + simpleDateFormat.format(startStatisticDate)) : "")
				+ (endStatisticDate != null ? ("&endStatisticDate=" + simpleDateFormat.format(endStatisticDate)) : "")
			;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
				username, password, null, outputToBeCompressed);
            mLogger.info("getRequestStatistics. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "getRequestStatistics MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            requestStatisticsList.clear();

            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            numFound = joResponse.getLong("numFound");
            JSONArray jaRequestStatistics = joResponse.getJSONArray("requestStatistics");

            for (int requestStatisticIndex = 0; requestStatisticIndex < jaRequestStatistics.length(); requestStatisticIndex++)
            {
                JSONObject requestStatisticInfo = jaRequestStatistics.getJSONObject(requestStatisticIndex);

                RequestStatistic requestStatistic = new RequestStatistic();

                fillRequestStatistic(requestStatistic, requestStatisticInfo);

                requestStatisticsList.add(requestStatistic);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "getRequestStatistics failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return numFound;
    }

    public Long getRequestPerContentStatistics(String username, String password,
		String title, Date startStatisticDate, Date endStatisticDate,
		long startIndex, long pageSize,
		List<RequestPerContentStatistic> requestPerContentStatisticsList)
		throws Exception
    {
		Long numFound;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/statistic/request/perContent"
				+ "?start=" + startIndex
				+ "&rows=" + pageSize
				+ (title != null ? ("&title=" + title) : "")
				+ (startStatisticDate != null ? ("&startStatisticDate=" + simpleDateFormat.format(startStatisticDate)) : "")
				+ (endStatisticDate != null ? ("&endStatisticDate=" + simpleDateFormat.format(endStatisticDate)) : "")
			;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
				username, password, null, outputToBeCompressed);
            mLogger.info("getRequestPerContentStatistics. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "getRequestPerContentStatistics MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            requestPerContentStatisticsList.clear();

            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            numFound = joResponse.getLong("numFound");
            JSONArray jaRequestStatistics = joResponse.getJSONArray("requestStatistics");

            for (int requestStatisticIndex = 0; requestStatisticIndex < jaRequestStatistics.length(); requestStatisticIndex++)
            {
                JSONObject requestPerContentStatisticInfo = jaRequestStatistics.getJSONObject(requestStatisticIndex);

                RequestPerContentStatistic requestPerContentStatistic = new RequestPerContentStatistic();

                fillRequestPerContentStatistic(requestPerContentStatistic, requestPerContentStatisticInfo);

                requestPerContentStatisticsList.add(requestPerContentStatistic);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "getRequestPerContentStatistics failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return numFound;
    }

    public Long getRequestPerMonthStatistics(String username, String password,
		String title, Date startStatisticDate, Date endStatisticDate,
		long startIndex, long pageSize,
		List<RequestPerMonthStatistic> requestPerMonthStatisticsList)
		throws Exception
    {
		Long numFound;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/statistic/request/perMonth"
				+ "?start=" + startIndex
				+ "&rows=" + pageSize
				+ (title != null ? ("&title=" + title) : "")
				+ (startStatisticDate != null ? ("&startStatisticDate=" + simpleDateFormat.format(startStatisticDate)) : "")
				+ (endStatisticDate != null ? ("&endStatisticDate=" + simpleDateFormat.format(endStatisticDate)) : "")
			;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
				username, password, null, outputToBeCompressed);
            mLogger.info("getRequestPerMonthStatistics. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "getRequestPerMonthStatistics MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            requestPerMonthStatisticsList.clear();

            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            numFound = joResponse.getLong("numFound");
            JSONArray jaRequestStatistics = joResponse.getJSONArray("requestStatistics");

            for (int requestStatisticIndex = 0; requestStatisticIndex < jaRequestStatistics.length(); requestStatisticIndex++)
            {
                JSONObject requestPerMonthStatisticInfo = jaRequestStatistics.getJSONObject(requestStatisticIndex);

                RequestPerMonthStatistic requestPerMonthStatistic = new RequestPerMonthStatistic();

                fillRequestPerMonthStatistic(requestPerMonthStatistic, requestPerMonthStatisticInfo);

                requestPerMonthStatisticsList.add(requestPerMonthStatistic);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "getRequestPerMonthStatistics failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return numFound;
    }

    public Long getRequestPerDayStatistics(String username, String password,
		String title, Date startStatisticDate, Date endStatisticDate,
		long startIndex, long pageSize,
		List<RequestPerDayStatistic> requestPerDayStatisticsList)
		throws Exception
    {
		Long numFound;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/statistic/request/perDay"
				+ "?start=" + startIndex
				+ "&rows=" + pageSize
				+ (title != null ? ("&title=" + title) : "")
				+ (startStatisticDate != null ? ("&startStatisticDate=" + simpleDateFormat.format(startStatisticDate)) : "")
				+ (endStatisticDate != null ? ("&endStatisticDate=" + simpleDateFormat.format(endStatisticDate)) : "")
			;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
				username, password, null, outputToBeCompressed);
            mLogger.info("getRequestPerDayStatistics. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "getRequestPerDayStatistics MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            requestPerDayStatisticsList.clear();

            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            numFound = joResponse.getLong("numFound");
            JSONArray jaRequestStatistics = joResponse.getJSONArray("requestStatistics");

            for (int requestStatisticIndex = 0; requestStatisticIndex < jaRequestStatistics.length(); requestStatisticIndex++)
            {
                JSONObject requestPerDayStatisticInfo = jaRequestStatistics.getJSONObject(requestStatisticIndex);

                RequestPerDayStatistic requestPerDayStatistic = new RequestPerDayStatistic();

                fillRequestPerDayStatistic(requestPerDayStatistic, requestPerDayStatisticInfo);

                requestPerDayStatisticsList.add(requestPerDayStatistic);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "getRequestPerDayStatistics failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return numFound;
    }

    public Long getRequestPerHourStatistics(String username, String password,
		String title, Date startStatisticDate, Date endStatisticDate,
		long startIndex, long pageSize,
		List<RequestPerHourStatistic> requestPerHourStatisticsList)
		throws Exception
    {
		Long numFound;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/statistic/request/perHour"
				+ "?start=" + startIndex
				+ "&rows=" + pageSize
				+ (title != null ? ("&title=" + title) : "")
				+ (startStatisticDate != null ? ("&startStatisticDate=" + simpleDateFormat.format(startStatisticDate)) : "")
				+ (endStatisticDate != null ? ("&endStatisticDate=" + simpleDateFormat.format(endStatisticDate)) : "")
			;

            mLogger.info("mmsURL: " + mmsURL);

            Date now = new Date();
            mmsInfo = HttpFeedFetcher.fetchGetHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
				username, password, null, outputToBeCompressed);
            mLogger.info("getRequestPerHourStatistics. Elapsed (@" + mmsURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "getRequestPerHourStatistics MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            requestPerHourStatisticsList.clear();

            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            numFound = joResponse.getLong("numFound");
            JSONArray jaRequestStatistics = joResponse.getJSONArray("requestStatistics");

            for (int requestStatisticIndex = 0; requestStatisticIndex < jaRequestStatistics.length(); requestStatisticIndex++)
            {
                JSONObject requestPerHourStatisticInfo = jaRequestStatistics.getJSONObject(requestStatisticIndex);

                RequestPerHourStatistic requestPerHourStatistic = new RequestPerHourStatistic();

                fillRequestPerHourStatistic(requestPerHourStatistic, requestPerHourStatisticInfo);

                requestPerHourStatisticsList.add(requestPerHourStatistic);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "getRequestPerHourStatistics failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return numFound;
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
            userProfile.setEmail(joUserProfileInfo.getString("email"));
            userProfile.setCreationDate(simpleDateFormat.parse(joUserProfileInfo.getString("creationDate")));
            userProfile.setExpirationDate(simpleDateFormat.parse(joUserProfileInfo.getString("expirationDate")));
        }
        catch (Exception e)
        {
            String errorMessage = "fillUserProfile failed" 
				+ ", Exception: " + e
				+ ", joUserProfileInfo: " + joUserProfileInfo.toString()
			;
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
				workspaceDetails.setExpirationDate(simpleDateFormat.parse(joUserAPIKey.getString("expirationDate")));
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

			encodingJob.setEndEstimate(false);

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
					// end processing estimation
					{
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

                    encodingJob.setEncodingProfileKey(joParameters.getLong("encodingProfileKey"));
                    if (joParameters.getJSONArray("sourcesToBeEncodedRoot").length() > 0)
                        encodingJob.setSourcePhysicalPathKey(
                            joParameters.getJSONArray("sourcesToBeEncodedRoot").getJSONObject(0)
                                    .getLong("sourcePhysicalPathKey"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("OverlayImageOnVideo"))
                {
					// end processing estimation
					{
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

					encodingJob.setSourceVideoPhysicalPathKey(joParameters.getLong("sourceVideoPhysicalPathKey"));
                    encodingJob.setSourceImagePhysicalPathKey(joParameters.getLong("sourceImagePhysicalPathKey"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("OverlayTextOnVideo"))
                {
					// end processing estimation
					{
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

					// encodingJob.setSourceVideoPhysicalPathKey(joParameters.getLong("sourceVideoPhysicalPathKey"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("GenerateFrames")
                        )
                {
					// end processing estimation
					{
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

					encodingJob.setSourceVideoPhysicalPathKey(joParameters.getLong("sourceVideoPhysicalPathKey"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("FaceRecognition")
                )
                {
					// end processing estimation
					{
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

					if (joParameters.has("sourceVideoPhysicalPathKey"))
                        encodingJob.setSourceVideoPhysicalPathKey(joParameters.getLong("sourceVideoPhysicalPathKey"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("LiveGrid")
                )
                {
					// end processing estimation
					{
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

					encodingJob.setInputChannels(joParameters.getJSONArray("inputChannels").toString());
                    encodingJob.setEncodingProfileKey(joParameters.getLong("encodingProfileKey"));
                    // encodingJob.setDeliveryCode(joParameters.getLong("deliveryCode"));
                    encodingJob.setSegmentDurationInSeconds(joParameters.getLong("segmentDurationInSeconds"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("VideoSpeed")
                )
                {
					// end processing estimation
					{
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

					encodingJob.setSourceVideoPhysicalPathKey(joParameters.getLong("sourceVideoPhysicalPathKey"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("PictureInPicture"))
                {
					// end processing estimation
					{
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

					encodingJob.setMainVideoPhysicalPathKey(joParameters.getLong("mainVideoPhysicalPathKey"));
                    encodingJob.setOverlayVideoPhysicalPathKey(joParameters.getLong("overlayVideoPhysicalPathKey"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("IntroOutroOverlay"))
                {
					// end processing estimation
					{
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

					encodingJob.setIntroVideoPhysicalPathKey(joParameters.getLong("introVideoPhysicalPathKey"));
                    encodingJob.setMainVideoPhysicalPathKey(joParameters.getLong("mainVideoPhysicalPathKey"));
                    encodingJob.setOutroVideoPhysicalPathKey(joParameters.getLong("outroVideoPhysicalPathKey"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("CutFrameAccurate"))
                {
					// end processing estimation
					{
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

					encodingJob.setEncodingProfileKey(joParameters.getLong("encodingProfileKey"));
                    encodingJob.setSourcePhysicalPathKey(joParameters.getLong("sourceVideoPhysicalPathKey"));
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
					
					if (joParameters.has("utcScheduleEnd"))
                    	encodingJob.setRecordingPeriodEnd(new Date(1000 * joParameters.getLong("utcScheduleEnd")));
					if (joParameters.has("utcScheduleStart"))
						encodingJob.setRecordingPeriodStart(new Date(1000 * joParameters.getLong("utcScheduleStart")));

					encodingJob.setEndEstimate(true);
					encodingJob.setEnd(new Date(1000 * joParameters.getLong("utcScheduleEnd")));
                }
                else if (encodingJob.getType().equalsIgnoreCase("LiveProxy")
                )
                {
					if (joParameters.has("inputsRoot"))
					{
						JSONArray jaInputsRoot = joParameters.getJSONArray("inputsRoot");
						if (jaInputsRoot.length() > 0)
						{
							JSONObject joFirstInputRoot = jaInputsRoot.getJSONObject(0);
							JSONObject joLastInputRoot = jaInputsRoot.getJSONObject(jaInputsRoot.length() - 1);

							if (joFirstInputRoot.has("url"))
								encodingJob.setLiveURL(joFirstInputRoot.getString("url"));

							if (joFirstInputRoot.has("timePeriod") && joFirstInputRoot.getBoolean("timePeriod")
								&& joFirstInputRoot.has("utcScheduleStart"))
								encodingJob.setProxyPeriodStart(new Date(1000 * joFirstInputRoot.getLong("utcScheduleStart")));

							if (joLastInputRoot.has("timePeriod") && joLastInputRoot.getBoolean("timePeriod")
								&& joLastInputRoot.has("utcScheduleEnd"))
								encodingJob.setProxyPeriodEnd(new Date(1000 * joLastInputRoot.getLong("utcScheduleEnd")));

							if (joLastInputRoot.has("timePeriod") && joLastInputRoot.getBoolean("timePeriod")
								&& joLastInputRoot.has("utcScheduleEnd"))
							{
								encodingJob.setEndEstimate(true);
								encodingJob.setEnd(new Date(1000 * joLastInputRoot.getLong("utcScheduleEnd")));
							}
						}
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
					if (joParameters.has("inputsRoot"))
					{
						JSONArray jaInputsRoot = joParameters.getJSONArray("inputsRoot");
						if (jaInputsRoot.length() > 0)
						{
							JSONObject joFirstInputRoot = jaInputsRoot.getJSONObject(0);
							JSONObject joLastInputRoot = jaInputsRoot.getJSONObject(jaInputsRoot.length() - 1);

							if (joFirstInputRoot.has("url"))
								encodingJob.setLiveURL(joFirstInputRoot.getString("url"));

							if (joFirstInputRoot.has("timePeriod") && joFirstInputRoot.getBoolean("timePeriod")
								&& joFirstInputRoot.has("utcScheduleStart"))
								encodingJob.setProxyPeriodStart(new Date(1000 * joFirstInputRoot.getLong("utcScheduleStart")));

							if (joLastInputRoot.has("timePeriod") && joLastInputRoot.getBoolean("timePeriod")
								&& joLastInputRoot.has("utcScheduleEnd"))
								encodingJob.setProxyPeriodEnd(new Date(1000 * joLastInputRoot.getLong("utcScheduleEnd")));

							if (joLastInputRoot.has("timePeriod") && joLastInputRoot.getBoolean("timePeriod")
								&& joLastInputRoot.has("utcScheduleEnd"))
							{
								encodingJob.setEndEstimate(true);
								encodingJob.setEnd(new Date(1000 * joLastInputRoot.getLong("utcScheduleEnd")));
							}
						}
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
                else if (encodingJob.getType().equalsIgnoreCase("Countdown")
                )
                {
					if (joParameters.has("inputsRoot"))
					{
						JSONArray jaInputsRoot = joParameters.getJSONArray("inputsRoot");
						if (jaInputsRoot.length() > 0)
						{
							JSONObject joFirstInputRoot = jaInputsRoot.getJSONObject(0);
							JSONObject joLastInputRoot = jaInputsRoot.getJSONObject(jaInputsRoot.length() - 1);

							if (joFirstInputRoot.has("url"))
								encodingJob.setLiveURL(joFirstInputRoot.getString("url"));

							if (joFirstInputRoot.has("timePeriod") && joFirstInputRoot.getBoolean("timePeriod")
								&& joFirstInputRoot.has("utcScheduleStart"))
								encodingJob.setProxyPeriodStart(new Date(1000 * joFirstInputRoot.getLong("utcScheduleStart")));

							if (joLastInputRoot.has("timePeriod") && joLastInputRoot.getBoolean("timePeriod")
								&& joLastInputRoot.has("utcScheduleEnd"))
								encodingJob.setProxyPeriodEnd(new Date(1000 * joLastInputRoot.getLong("utcScheduleEnd")));

							if (joLastInputRoot.has("timePeriod") && joLastInputRoot.getBoolean("timePeriod")
								&& joLastInputRoot.has("utcScheduleEnd"))
							{
								encodingJob.setEndEstimate(true);
								encodingJob.setEnd(new Date(1000 * joLastInputRoot.getLong("utcScheduleEnd")));
							}
						}
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

    private void fillRequestStatistic(RequestStatistic requestStatistic, JSONObject requestStatisticInfo)
		throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try
        {
			requestStatistic.setRequestStatisticKey(requestStatisticInfo.getLong("requestStatisticKey"));
			requestStatistic.setUserId(requestStatisticInfo.getString("userId"));
            if (requestStatisticInfo.has("physicalPathKey") && !requestStatisticInfo.isNull("physicalPathKey"))
				requestStatistic.setPhysicalPathKey(requestStatisticInfo.getLong("physicalPathKey"));
			if (requestStatisticInfo.has("confStreamKey") && !requestStatisticInfo.isNull("confStreamKey"))
				requestStatistic.setConfStreamKey(requestStatisticInfo.getLong("confStreamKey"));
			requestStatistic.setRequestTimestamp(simpleDateFormat.parse(requestStatisticInfo.getString("requestTimestamp")));
			requestStatistic.setTitle(requestStatisticInfo.getString("title"));
			}
        catch (Exception e)
        {
            String errorMessage = "fillRequestStatistic failed. Exception: " + e
				+ ", requestStatisticInfo: " + requestStatisticInfo.toString()
			;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillRequestPerContentStatistic(RequestPerContentStatistic requestPerContentStatistic, 
		JSONObject requestPerContentStatisticInfo)
		throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try
        {
			requestPerContentStatistic.setTitle(requestPerContentStatisticInfo.getString("title"));
            requestPerContentStatistic.setCount(requestPerContentStatisticInfo.getLong("count"));
		}
        catch (Exception e)
        {
            String errorMessage = "fillRequestPerContentStatistic failed. Exception: " + e
				+ ", requestPerContentStatisticInfo: " + requestPerContentStatisticInfo.toString()
			;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillRequestPerMonthStatistic(RequestPerMonthStatistic requestPerMonthStatistic, 
		JSONObject requestPerMonthStatisticInfo)
		throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try
        {
			requestPerMonthStatistic.setDate(requestPerMonthStatisticInfo.getString("date"));
            requestPerMonthStatistic.setCount(requestPerMonthStatisticInfo.getLong("count"));
		}
        catch (Exception e)
        {
            String errorMessage = "fillRequestPerMonthStatistic failed. Exception: " + e
				+ ", requestPerMonthStatisticInfo: " + requestPerMonthStatisticInfo.toString()
			;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillRequestPerDayStatistic(RequestPerDayStatistic requestPerDayStatistic, 
		JSONObject requestPerDayStatisticInfo)
		throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try
        {
			requestPerDayStatistic.setDate(requestPerDayStatisticInfo.getString("date"));
            requestPerDayStatistic.setCount(requestPerDayStatisticInfo.getLong("count"));
		}
        catch (Exception e)
        {
            String errorMessage = "fillRequestPerDayStatistic failed. Exception: " + e
				+ ", requestPerDayStatisticInfo: " + requestPerDayStatisticInfo.toString()
			;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillRequestPerHourStatistic(RequestPerHourStatistic requestPerHourStatistic, 
		JSONObject requestPerHourStatisticInfo)
		throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try
        {
			requestPerHourStatistic.setDate(requestPerHourStatisticInfo.getString("date"));
            requestPerHourStatistic.setCount(requestPerHourStatisticInfo.getLong("count"));
		}
        catch (Exception e)
        {
            String errorMessage = "fillRequestPerHourStatistic failed. Exception: " + e
				+ ", requestPerHourStatisticInfo: " + requestPerHourStatisticInfo.toString()
			;
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
            encoder.setPublicServerName(encoderInfo.getString("publicServerName"));
            encoder.setInternalServerName(encoderInfo.getString("internalServerName"));
            encoder.setPort(encoderInfo.getLong("port"));
			if (encoderInfo.has("running"))
				encoder.setRunning(encoderInfo.getBoolean("running"));
			if (encoderInfo.has("cpuUsage"))
				encoder.setCpuUsage(encoderInfo.getLong("cpuUsage"));
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
            if (mediaItemInfo.has("mediaItemKey") && !mediaItemInfo.isNull("mediaItemKey"))
            	mediaItem.setMediaItemKey(mediaItemInfo.getLong("mediaItemKey"));
			if (mediaItemInfo.has("contentType") && !mediaItemInfo.isNull("contentType"))
				mediaItem.setContentType(mediaItemInfo.getString("contentType"));
			if (mediaItemInfo.has("title") && !mediaItemInfo.isNull("title"))
				mediaItem.setTitle(mediaItemInfo.getString("title"));
            if (mediaItemInfo.has("deliveryFileName") && !mediaItemInfo.isNull("deliveryFileName"))
                mediaItem.setDeliveryFileName(mediaItemInfo.getString("deliveryFileName"));
			if (mediaItemInfo.has("ingestionDate") && !mediaItemInfo.isNull("ingestionDate"))
				mediaItem.setIngestionDate(simpleDateFormat.parse(mediaItemInfo.getString("ingestionDate")));
			if (mediaItemInfo.has("startPublishing") && !mediaItemInfo.isNull("startPublishing"))
				mediaItem.setStartPublishing(simpleDateFormat.parse(mediaItemInfo.getString("startPublishing")));
			if (mediaItemInfo.has("endPublishing") && !mediaItemInfo.isNull("endPublishing"))
				mediaItem.setEndPublishing(simpleDateFormat.parse(mediaItemInfo.getString("endPublishing")));
            if (mediaItemInfo.has("uniqueName") && !mediaItemInfo.isNull("uniqueName"))
                mediaItem.setUniqueName(mediaItemInfo.getString("uniqueName"));
            if (mediaItemInfo.has("ingester") && !mediaItemInfo.isNull("ingester"))
                mediaItem.setIngester(mediaItemInfo.getString("ingester"));
			if (mediaItemInfo.has("tags"))
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
			if (mediaItemInfo.has("providerName") && !mediaItemInfo.isNull("providerName"))
				mediaItem.setProviderName(mediaItemInfo.getString("providerName"));
			if (mediaItemInfo.has("retentionInMinutes") && !mediaItemInfo.isNull("retentionInMinutes"))
				mediaItem.setRetentionInMinutes(mediaItemInfo.getLong("retentionInMinutes"));

            if (deep && mediaItemInfo.has("physicalPaths"))
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
                    if (joMetadataContent.has("schedule") && !joMetadataContent.isNull("schedule"))
                    {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                        JSONObject joRecordingPeriod = joMetadataContent.getJSONObject("schedule");

                        if (joRecordingPeriod.has("start") && !joRecordingPeriod.isNull("start"))
                            ingestionJob.setRecordingPeriodStart(dateFormat.parse(joRecordingPeriod.getString("start")));

                        if (joRecordingPeriod.has("end") && !joRecordingPeriod.isNull("end"))
                            ingestionJob.setRecordingPeriodEnd(dateFormat.parse(joRecordingPeriod.getString("end")));
                    }

                    if (joMetadataContent.has("LiveRecorderVirtualVOD"))
                        ingestionJob.setRecordingVirtualVOD(true);
                    else
                        ingestionJob.setRecordingVirtualVOD(false);

                    if (joMetadataContent.has("MonitorHLS"))
                        ingestionJob.setRecordingMonitorHLS(true);
                    else
                        ingestionJob.setRecordingMonitorHLS(false);

					if (joMetadataContent.has("ConfigurationLabel") && !joMetadataContent.isNull("ConfigurationLabel"))
						ingestionJob.setChannelLabel(joMetadataContent.getString("ConfigurationLabel"));
                }
                else if (ingestionJob.getIngestionType().equalsIgnoreCase("Live-Proxy")
                    && joMetadataContent != null)
                {
					if (joMetadataContent.has("ConfigurationLabel") && !joMetadataContent.isNull("ConfigurationLabel"))
						ingestionJob.setChannelLabel(joMetadataContent.getString("ConfigurationLabel"));

					if (joMetadataContent.has("TimePeriod") && !joMetadataContent.isNull("TimePeriod")
                        && joMetadataContent.getBoolean("TimePeriod") && joMetadataContent.has("schedule"))
                    {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                        JSONObject joProxyPeriod = joMetadataContent.getJSONObject("schedule");

                        if (joProxyPeriod.has("start") && !joProxyPeriod.isNull("start"))
                            ingestionJob.setProxyPeriodStart(dateFormat.parse(joProxyPeriod.getString("start")));

                        if (joProxyPeriod.has("end") && !joProxyPeriod.isNull("end"))
                            ingestionJob.setProxyPeriodEnd(dateFormat.parse(joProxyPeriod.getString("end")));
                    }
                }
                else if (ingestionJob.getIngestionType().equalsIgnoreCase("VOD-Proxy")
                    && joMetadataContent != null)
                {
					if (joMetadataContent.has("TimePeriod") && !joMetadataContent.isNull("TimePeriod")
                        && joMetadataContent.getBoolean("TimePeriod") && joMetadataContent.has("schedule"))
                    {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                        JSONObject joProxyPeriod = joMetadataContent.getJSONObject("schedule");

                        if (joProxyPeriod.has("start") && !joProxyPeriod.isNull("start"))
                            ingestionJob.setProxyPeriodStart(dateFormat.parse(joProxyPeriod.getString("start")));

                        if (joProxyPeriod.has("end") && !joProxyPeriod.isNull("end"))
                            ingestionJob.setProxyPeriodEnd(dateFormat.parse(joProxyPeriod.getString("end")));
                    }
                }
                else if (ingestionJob.getIngestionType().equalsIgnoreCase("Countdown")
                    && joMetadataContent != null)
                {
					if (joMetadataContent.has("schedule"))
                    {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                        JSONObject joProxyPeriod = joMetadataContent.getJSONObject("schedule");

                        if (joProxyPeriod.has("start") && !joProxyPeriod.isNull("start"))
                            ingestionJob.setProxyPeriodStart(dateFormat.parse(joProxyPeriod.getString("start")));

                        if (joProxyPeriod.has("end") && !joProxyPeriod.isNull("end"))
                            ingestionJob.setProxyPeriodEnd(dateFormat.parse(joProxyPeriod.getString("end")));
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

    private void fillEncodingProfile(EncodingProfile encodingProfile, 
		JSONObject encodingProfileInfo, boolean deep)
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
            String errorMessage = "fillEncodingProfile failed" 
				+ ", Exception: " + e
				+ ", encodingProfileInfo: " + encodingProfileInfo.toString()
			;
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
            youTubeConf.setTokenType(youTubeConfInfo.getString("tokenType"));
			if (youTubeConfInfo.has("refreshToken") && !youTubeConfInfo.isNull("refreshToken"))
	            youTubeConf.setRefreshToken(youTubeConfInfo.getString("refreshToken"));
			if (youTubeConfInfo.has("accessToken") && !youTubeConfInfo.isNull("accessToken"))
				youTubeConf.setAccessToken(youTubeConfInfo.getString("accessToken"));
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

    private void fillStream(Stream stream, JSONObject streamInfo)
            throws Exception
    {
        try {
            stream.setConfKey(streamInfo.getLong("confKey"));
            stream.setLabel(streamInfo.getString("label"));
            stream.setSourceType(streamInfo.getString("sourceType"));
			if (streamInfo.has("encodersPoolLabel") && !streamInfo.isNull("encodersPoolLabel"))
            	stream.setEncodersPoolLabel(streamInfo.getString("encodersPoolLabel"));
            if (streamInfo.has("url") && !streamInfo.isNull("url"))
            	stream.setUrl(streamInfo.getString("url"));
			if (streamInfo.has("pushProtocol") && !streamInfo.isNull("pushProtocol"))
            	stream.setPushProtocol(streamInfo.getString("pushProtocol"));
			if (streamInfo.has("pushEncoderKey") && !streamInfo.isNull("pushEncoderKey"))
                stream.setPushEncoderKey(streamInfo.getLong("pushEncoderKey"));
			if (streamInfo.has("pushServerName") && !streamInfo.isNull("pushServerName"))
            	stream.setPushServerName(streamInfo.getString("pushServerName"));
			if (streamInfo.has("pushServerPort") && !streamInfo.isNull("pushServerPort"))
                stream.setPushServerPort(streamInfo.getLong("pushServerPort"));
			if (streamInfo.has("pushUri") && !streamInfo.isNull("pushUri"))
            	stream.setPushURI(streamInfo.getString("pushUri"));
			if (streamInfo.has("pushListenTimeout") && !streamInfo.isNull("pushListenTimeout"))
                stream.setPushListenTimeout(streamInfo.getLong("pushListenTimeout"));
			if (streamInfo.has("captureLiveVideoDeviceNumber") && !streamInfo.isNull("captureLiveVideoDeviceNumber"))
                stream.setCaptureLiveVideoDeviceNumber(streamInfo.getLong("captureLiveVideoDeviceNumber"));
			if (streamInfo.has("captureLiveVideoInputFormat") && !streamInfo.isNull("captureLiveVideoInputFormat"))
            	stream.setCaptureLiveVideoInputFormat(streamInfo.getString("captureLiveVideoInputFormat"));
			if (streamInfo.has("captureLiveFrameRate") && !streamInfo.isNull("captureLiveFrameRate"))
            	stream.setCaptureLiveFrameRate(streamInfo.getLong("captureLiveFrameRate"));
			if (streamInfo.has("captureLiveWidth") && !streamInfo.isNull("captureLiveWidth"))
            	stream.setCaptureLiveWidth(streamInfo.getLong("captureLiveWidth"));
			if (streamInfo.has("captureLiveHeight") && !streamInfo.isNull("captureLiveHeight"))
            	stream.setCaptureLiveHeight(streamInfo.getLong("captureLiveHeight"));
			if (streamInfo.has("captureLiveAudioDeviceNumber") && !streamInfo.isNull("captureLiveAudioDeviceNumber"))
            	stream.setCaptureLiveAudioDeviceNumber(streamInfo.getLong("captureLiveAudioDeviceNumber"));
			if (streamInfo.has("captureLiveChannelsNumber") && !streamInfo.isNull("captureLiveChannelsNumber"))
            	stream.setCaptureLiveChannelsNumber(streamInfo.getLong("captureLiveChannelsNumber"));
			if (streamInfo.has("satSourceSATConfKey") && !streamInfo.isNull("satSourceSATConfKey"))
            	stream.setSourceSATConfKey(streamInfo.getLong("satSourceSATConfKey"));
			if (streamInfo.has("type") && !streamInfo.isNull("type"))
                stream.setType(streamInfo.getString("type"));
            if (streamInfo.has("description") && !streamInfo.isNull("description"))
                stream.setDescription(streamInfo.getString("description"));
            if (streamInfo.has("name") && !streamInfo.isNull("name"))
                stream.setName(streamInfo.getString("name"));
            if (streamInfo.has("region") && !streamInfo.isNull("region"))
                stream.setRegion(streamInfo.getString("region"));

            if (streamInfo.has("country") && !streamInfo.isNull("country"))
                stream.setCountry(streamInfo.getString("country"));

            if (streamInfo.has("imageMediaItemKey") && !streamInfo.isNull("imageMediaItemKey"))
                stream.setImageMediaItemKey(streamInfo.getLong("imageMediaItemKey"));

            if (streamInfo.has("imageUniqueName") && !streamInfo.isNull("imageUniqueName"))
                stream.setImageUniqueName(streamInfo.getString("imageUniqueName"));

            if (streamInfo.has("position") && !streamInfo.isNull("position"))
                stream.setPosition(streamInfo.getLong("position"));
            if (streamInfo.has("userData") && !streamInfo.isNull("userData"))
                stream.setUserData(streamInfo.getString("userData"));
        }
        catch (Exception e)
        {
            String errorMessage = "fillStream failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillSourceSATStream(SourceSATStream stream, JSONObject streamInfo)
            throws Exception
    {
        try {
            stream.setConfKey(streamInfo.getLong("confKey"));
            if (streamInfo.has("serviceId") && !streamInfo.isNull("serviceId"))
                stream.setServiceId(streamInfo.getLong("serviceId"));
            if (streamInfo.has("networkId") && !streamInfo.isNull("networkId"))
                stream.setNetworkId(streamInfo.getLong("networkId"));
            if (streamInfo.has("transportStreamId") && !streamInfo.isNull("transportStreamId"))
                stream.setTransportStreamId(streamInfo.getLong("transportStreamId"));
            stream.setName(streamInfo.getString("name"));
            stream.setSatellite(streamInfo.getString("satellite"));
            stream.setFrequency(streamInfo.getLong("frequency"));
            if (streamInfo.has("lnb") && !streamInfo.isNull("lnb"))
                stream.setLnb(streamInfo.getString("lnb"));
            if (streamInfo.has("videoPid") && !streamInfo.isNull("videoPid"))
                stream.setVideoPid(streamInfo.getLong("videoPid"));
            if (streamInfo.has("audioPids") && !streamInfo.isNull("audioPids"))
                stream.setAudioPids(streamInfo.getString("audioPids"));
            if (streamInfo.has("audioItalianPid") && !streamInfo.isNull("audioItalianPid"))
                stream.setAudioItalianPid(streamInfo.getLong("audioItalianPid"));
            if (streamInfo.has("audioEnglishPid") && !streamInfo.isNull("audioEnglishPid"))
                stream.setAudioEnglishPid(streamInfo.getLong("audioEnglishPid"));
            if (streamInfo.has("teletextPid") && !streamInfo.isNull("teletextPid"))
                stream.setTeletextPid(streamInfo.getLong("teletextPid"));
            if (streamInfo.has("modulation") && !streamInfo.isNull("modulation"))
                stream.setModulation(streamInfo.getString("modulation"));
            if (streamInfo.has("polarization") && !streamInfo.isNull("polarization"))
                stream.setPolarization(streamInfo.getString("polarization"));
            if (streamInfo.has("symbolRate") && !streamInfo.isNull("symbolRate"))
                stream.setSymbolRate(streamInfo.getLong("symbolRate"));
            if (streamInfo.has("country") && !streamInfo.isNull("country"))
                stream.setCountry(streamInfo.getString("country"));
            if (streamInfo.has("deliverySystem") && !streamInfo.isNull("deliverySystem"))
                stream.setDeliverySystem(streamInfo.getString("deliverySystem"));
        }
        catch (Exception e)
        {
            String errorMessage = "fillSourceSATStream failed. Exception: " + e;
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

    private void fillAWSChannelConf(AWSChannelConf awsChannelConf, JSONObject awsChannelConfInfo)
            throws Exception
    {
        try {
            awsChannelConf.setConfKey(awsChannelConfInfo.getLong("confKey"));
            awsChannelConf.setLabel(awsChannelConfInfo.getString("label"));
            awsChannelConf.setChannelId(awsChannelConfInfo.getString("channelId"));
            awsChannelConf.setRtmpURL(awsChannelConfInfo.getString("rtmpURL"));
            awsChannelConf.setPlayURL(awsChannelConfInfo.getString("playURL"));
            awsChannelConf.setType(awsChannelConfInfo.getString("type"));
			if (awsChannelConfInfo.isNull("reservedByIngestionJobKey"))
				awsChannelConf.setReservedByIngestionJobKey(null);
			else
				awsChannelConf.setReservedByIngestionJobKey(awsChannelConfInfo.getLong("reservedByIngestionJobKey"));
        }
        catch (Exception e)
        {
            String errorMessage = "fillAWSChannelConf failed. Exception: " + e;
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
