package com.catrammslib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
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
import com.catrammslib.entity.*;
import com.catrammslib.utility.BroadcastPlaylistItem;
import com.catrammslib.utility.BulkOfDeliveryURLData;
import com.catrammslib.utility.HttpFeedFetcher;
import com.catrammslib.utility.IngestionResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by multi on 08.06.18.
 */
public class CatraMMSAPI implements Serializable {

    private final Logger mLogger = LoggerFactory.getLogger(this.getClass());

    private int timeoutInSeconds;
    public int statisticsTimeoutInSeconds;
    private int maxRetriesNumber;
    private String mmsAPIProtocol;
    private String mmsAPIHostName;
    private int mmsAPIPort;
    private String mmsBinaryProtocol;
    private String mmsBinaryHostName;
    private int mmsBinaryPort;
    private int binaryTimeoutInSeconds;
	private Boolean outputToBeCompressed;

    public CatraMMSAPI(int timeoutInSeconds, int statisticsTimeoutInSeconds, int maxRetriesNumber,
                       String mmsAPIProtocol, String mmsAPIHostName, int mmsAPIPort,
                       String mmsBinaryProtocol, String mmsBinaryHostName, int mmsBinaryPort,
                       int binaryTimeoutInSeconds, Boolean outputToBeCompressed)
    {
        this.timeoutInSeconds = timeoutInSeconds;
        this.statisticsTimeoutInSeconds = statisticsTimeoutInSeconds;
        this.maxRetriesNumber = maxRetriesNumber;
        this.mmsAPIProtocol = mmsAPIProtocol;
        this.mmsAPIHostName = mmsAPIHostName;
        this.mmsAPIPort = mmsAPIPort;
        this.mmsBinaryProtocol = mmsBinaryProtocol;
        this.mmsBinaryHostName = mmsBinaryHostName;
        this.mmsBinaryPort = mmsBinaryPort;
        this.binaryTimeoutInSeconds = binaryTimeoutInSeconds;
        this.outputToBeCompressed = outputToBeCompressed;
    }

    public CatraMMSAPI(Properties configurationProperties, String prefix)
    {
        try
        {
            // mLogger.info("getConfigurationParameters...");
            // Properties configurationProperties = Login.getConfigurationParameters();

			if (prefix == null)
				prefix = "catramms";

            {
                String tmpTimeoutInSeconds = configurationProperties.getProperty(prefix + ".mms.timeoutInSeconds");
                if (tmpTimeoutInSeconds == null)
					timeoutInSeconds = 15;
				else
	                timeoutInSeconds = Integer.parseInt(tmpTimeoutInSeconds);

                String tmpStatisticsTimeoutInSeconds = configurationProperties.getProperty(prefix + ".mms.statistics.timeoutInSeconds");
                if (tmpStatisticsTimeoutInSeconds == null)
                    statisticsTimeoutInSeconds = 30;
                else
                    statisticsTimeoutInSeconds = Integer.parseInt(tmpStatisticsTimeoutInSeconds);

                String tmpMaxRetriesNumber = configurationProperties.getProperty(prefix + ".mms.delivery.maxRetriesNumber");
                if (tmpMaxRetriesNumber == null)
					maxRetriesNumber = 2;
				else
	                maxRetriesNumber = Integer.parseInt(tmpMaxRetriesNumber);

                mmsAPIProtocol = configurationProperties.getProperty(prefix + ".mms.api.protocol");
                if (mmsAPIProtocol == null)
					mmsAPIProtocol = "https";

                mmsAPIHostName = configurationProperties.getProperty(prefix + ".mms.api.hostname");
                if (mmsAPIHostName == null)
					mmsAPIHostName = "mms-api.catramms-cloud.com";

                String tmpMmsAPIPort = configurationProperties.getProperty(prefix + ".mms.api.port");
                if (tmpMmsAPIPort == null)
					mmsAPIPort = 443;
				else
	                mmsAPIPort = Integer.parseInt(tmpMmsAPIPort);

                mmsBinaryProtocol = configurationProperties.getProperty(prefix + ".mms.binary.protocol");
                if (mmsBinaryProtocol == null)
					mmsBinaryProtocol = "http";

                mmsBinaryHostName = configurationProperties.getProperty(prefix + ".mms.binary.hostname");
                if (mmsBinaryHostName == null)
					mmsBinaryHostName = "mms-binary.catramms-cloud.com";

                String tmpMmsBinaryPort = configurationProperties.getProperty(prefix + ".mms.binary.port");
                if (tmpMmsBinaryPort == null)
					mmsBinaryPort = 80;
				else
	                mmsBinaryPort = Integer.parseInt(tmpMmsBinaryPort);

                String tmpBinaryTimeoutInSeconds = configurationProperties.getProperty(prefix + ".mms.binary.timeoutInSeconds");
                if (tmpBinaryTimeoutInSeconds == null)
                    binaryTimeoutInSeconds = 180;
                else
                    binaryTimeoutInSeconds = Integer.parseInt(tmpBinaryTimeoutInSeconds);

                String tmpOutputToBeCompressed = configurationProperties.getProperty(prefix + ".mms.outputToBeCompressed");
                if (tmpOutputToBeCompressed == null)
					outputToBeCompressed = true;
				else
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

            String hashStr = strippedPath + secureToken;

            String sExpiryTimestamp;
            if (expiryTimestamp != null)
            {
                hashStr = expiryTimestamp + hashStr;
                sExpiryTimestamp = "," + expiryTimestamp;
            }
            else
                sExpiryTimestamp = expiryTimestamp.toString();

            String base64HashStr = Base64.getEncoder().encodeToString(md5(hashStr));
            mLogger.info("getCDN77SignedUrlPath"
                    + ", strippedPath: " + strippedPath
                    + ", hashStr: " + hashStr
                    + ", sExpiryTimestamp: " + sExpiryTimestamp
                    + ", base64HashStr: " + base64HashStr
            );
            String base64HashStr_2 = base64HashStr.replace("+", "-").replace("/", "_");

            // the URL is however, intensionaly returned with the previously stripped parts (eg. playlist/{chunk}..)
            String signedURL = "https://" + cdnResourceUrl + "/" + base64HashStr_2 + sExpiryTimestamp + filePath;
            mLogger.info("getCDN77SignedUrlPath"
                    + ", strippedPath: " + strippedPath
                    + ", hashStr: " + hashStr
                    + ", sExpiryTimestamp: " + sExpiryTimestamp
                    + ", base64HashStr: " + base64HashStr
                    + ", base64HashStr_2: " + base64HashStr_2
                    + ", signedURL: " + signedURL
            );

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

    public void shareWorkspace(String username, String password,
        String emailAddressToShare,

        Boolean createRemoveWorkspace, Boolean ingestWorkflow, Boolean createProfiles,
        Boolean deliveryAuthorization, Boolean shareWorkspace,
        Boolean editMedia, Boolean editConfiguration, Boolean killEncoding,
        Boolean cancelIngestionJob, Boolean editEncodersPool, Boolean applicationRecorder)
        throws Exception
    {
        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/workspace/share";

            String postBodyRequest;
			{
				JSONObject joObj = new JSONObject();
				joObj.put("email", emailAddressToShare);
				joObj.put("createRemoveWorkspace", createRemoveWorkspace);
				joObj.put("ingestWorkflow", ingestWorkflow);
				joObj.put("createProfiles", createProfiles);
				joObj.put("deliveryAuthorization", deliveryAuthorization);
				joObj.put("shareWorkspace", shareWorkspace);
				joObj.put("editMedia", editMedia);
				joObj.put("editConfiguration", editConfiguration);
				joObj.put("killEncoding", killEncoding);
				joObj.put("cancelIngestionJob", cancelIngestionJob);
				joObj.put("editEncodersPool", editEncodersPool);
				joObj.put("applicationRecorder", applicationRecorder);

				postBodyRequest = joObj.toString();
			}

            mLogger.info("shareWorkspace"
                            + ", mmsURL: " + mmsURL
                            + ", postBodyRequest: " + postBodyRequest
            );

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, postBodyRequest, outputToBeCompressed);
            mLogger.info("shareWorkspace. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "shareWorkspace failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        // 2023-03-01: in case the user is not present, no 'userKey' is returned.
        // An email with a confirmation code is sent to invite the user to register
        /*
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
        */
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, postBodyRequest, outputToBeCompressed);
            mLogger.info("setWorkspaceAsDefault. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "setWorkspaceAsDefault failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public Long registerAndAddWorkspace(String userNameToRegister, String emailAddressToRegister,
        String passwordToRegister, String countryToRegister, String timezoneToRegister,
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
            if (timezoneToRegister != null && !timezoneToRegister.isEmpty())
                joObject.put("timezone", timezoneToRegister);
			if (workspaceNameToRegister != null && !workspaceNameToRegister.isEmpty())
				joObject.put("workspaceName", workspaceNameToRegister);

			String postBodyRequest = joObject.toString();

            mLogger.info("register"
                            + ", mmsURL: " + mmsURL
                            + ", postBodyRequest: " + postBodyRequest
            );

            String username = null;
            String password = null;

            long start = System.currentTimeMillis();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, postBodyRequest, outputToBeCompressed);
            mLogger.info("register. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

    public Long registerAndShareWorkspace(String userNameToRegister, String emailAddressToRegister,
        String passwordToRegister, String countryToRegister, String timezoneToRegister,
        String shareWorkspaceCode)
        throws Exception
    {
        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/user";

			// only email and password are mandatory
			JSONObject joObject = new JSONObject();
			joObject.put("email", emailAddressToRegister);
			joObject.put("password", passwordToRegister);
			joObject.put("shareWorkspaceCode", shareWorkspaceCode);
			if (userNameToRegister != null && !userNameToRegister.isEmpty())
				joObject.put("name", userNameToRegister);
			if (countryToRegister != null && !countryToRegister.isEmpty())
				joObject.put("country", countryToRegister);
            if (timezoneToRegister != null && !timezoneToRegister.isEmpty())
                joObject.put("timezone", timezoneToRegister);

			String postBodyRequest = joObject.toString();

            mLogger.info("register"
                + ", mmsURL: " + mmsURL
                + ", postBodyRequest: " + postBodyRequest
            );

            String username = null;
            String password = null;

            long start = System.currentTimeMillis();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, postBodyRequest, outputToBeCompressed);
            mLogger.info("register. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
			// 2022-01-03: it has to be GET because same link is sent inside the email
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("confirmRegistration. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            String contentType = null;
			// 2022-01-03: it has to be GET because same link is sent inside the email
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, null, outputToBeCompressed);
            mLogger.info("forgotPassword. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, postBodyRequest, outputToBeCompressed);
            mLogger.info("resetPassword. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, postBodyRequest, outputToBeCompressed);
            mLogger.info("login. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "Login MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        UserProfile userProfile = new UserProfile();
        WorkspaceDetails workspaceDetails = null;
        String mmsVersion = null;

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
            if (joWMMSInfo.has("mmsVersion") && !joWMMSInfo.isNull("mmsVersion"))
                mmsVersion = joWMMSInfo.getString("mmsVersion");
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
        objects.add(mmsVersion);

        return objects;
    }

    public UserProfile updateUserProfile(String username, String password,
		String newName,
		String newEmailAddress,
		String newCountry,
        String newTimezone,
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
            if (newTimezone != null)
                joUser.put("timezone", newTimezone);
			if (newExpirationDate != null)
				joUser.put("expirationDate", simpleDateFormat.format(newExpirationDate));

			String bodyRequest = joUser.toString();

            mLogger.info("updateUser"
                            + ", mmsURL: " + mmsURL
                            // + ", bodyRequest: " + bodyRequest
            );

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, bodyRequest, outputToBeCompressed);
            mLogger.info("updateUserProfile. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, postBodyRequest, outputToBeCompressed);
            mLogger.info("createWorkspace. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
		String newLanguageCode, Date newExpirationDate,

        Long maxStorageInGB, Long currentCostForStorage,
        Long dedicatedEncoder_power_1, Long currentCostForDedicatedEncoder_power_1,
        Long dedicatedEncoder_power_2, Long currentCostForDedicatedEncoder_power_2,
        Long dedicatedEncoder_power_3, Long currentCostForDedicatedEncoder_power_3,
        Long CDN_type_1, Long currentCostForCDN_type_1,
        Boolean support_type_1, Long currentCostForSupport_type_1,

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
            {
                joBodyRequest.put("isEnabled", newEnabled); // da eliminare dopo upgrade a Postgres
                joBodyRequest.put("enabled", newEnabled);
            }
			if (newMaxEncodingPriority != null)
				joBodyRequest.put("maxEncodingPriority", newMaxEncodingPriority);
			if (newEncodingPeriod != null)
				joBodyRequest.put("encodingPeriod", newEncodingPeriod);
			if (newMaxIngestionsNumber != null)
				joBodyRequest.put("maxIngestionsNumber", newMaxIngestionsNumber);
			if (newLanguageCode != null)
				joBodyRequest.put("languageCode", newLanguageCode);

            if (maxStorageInGB != null)
                joBodyRequest.put("maxStorageInGB", maxStorageInGB);
            if (currentCostForStorage != null)
                joBodyRequest.put("currentCostForStorage", currentCostForStorage);

            if (dedicatedEncoder_power_1 != null)
                joBodyRequest.put("dedicatedEncoder_power_1", dedicatedEncoder_power_1);
            if (currentCostForDedicatedEncoder_power_1 != null)
                joBodyRequest.put("currentCostForDedicatedEncoder_power_1", currentCostForDedicatedEncoder_power_1);
            if (dedicatedEncoder_power_2 != null)
                joBodyRequest.put("dedicatedEncoder_power_2", dedicatedEncoder_power_2);
            if (currentCostForDedicatedEncoder_power_2 != null)
                joBodyRequest.put("currentCostForDedicatedEncoder_power_2", currentCostForDedicatedEncoder_power_2);
            if (dedicatedEncoder_power_3 != null)
                joBodyRequest.put("dedicatedEncoder_power_3", dedicatedEncoder_power_3);
            if (currentCostForDedicatedEncoder_power_3 != null)
                joBodyRequest.put("currentCostForDedicatedEncoder_power_3", currentCostForDedicatedEncoder_power_3);

            if (CDN_type_1 != null)
                joBodyRequest.put("CDN_type_1", CDN_type_1);
            if (currentCostForCDN_type_1 != null)
                joBodyRequest.put("currentCostForCDN_type_1", currentCostForCDN_type_1);

            if (support_type_1 != null)
                joBodyRequest.put("support_type_1", support_type_1);
            if (currentCostForSupport_type_1 != null)
                joBodyRequest.put("currentCostForSupport_type_1", currentCostForSupport_type_1);

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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, bodyRequest, outputToBeCompressed);
            mLogger.info("updateWorkspace. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
			// 2022-12-22: chenged URL from API to Binary because API could have noFileSystemAccess = true
            String mmsURL = mmsBinaryProtocol + "://" + mmsBinaryHostName + ":" + mmsBinaryPort + "/catramms/1.0.1/workspace";

            mLogger.info("deleteWorkspace"
                    + ", mmsURL: " + mmsURL
            );

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("deleteWorkspace. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, postBodyRequest, outputToBeCompressed);
            mLogger.info("mmsSupport. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonWorkflow, outputToBeCompressed);
            mLogger.info("ingestWorkflow. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            metaDataContent = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getMetaDataContent. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonEncodingProfile, outputToBeCompressed);
            mLogger.info("addEncodingProfile. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("removeEncodingProfile. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeEncodingProfile MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public Long addUpdateEncodingProfilesSet(String username, String password,
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonEncodingProfilesSet, outputToBeCompressed);
            mLogger.info("addEncodingProfilesSet. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addUpdateEncodingProfilesSet MMS failed. Exception: " + e;
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
            String errorMessage = "addUpdateEncodingProfilesSet failed. Exception: " + e;
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("removeEncodingProfilesSet. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
            long start = System.currentTimeMillis();
            HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, putBodyRequest, outputToBeCompressed);
            mLogger.info("updateEncodingJobPriority. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "updateEncodingJobPriority MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void killEncodingJob(String username, String password, Long encodingJobKey, Boolean lightKill)
            throws Exception
    {
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
				+ "/catramms/1.0.1/encodingJob/" + encodingJobKey
				+ "?lightKill=" + (lightKill == null ? "false" : lightKill.toString())
			;

            mLogger.info("killEncodingJob"
                    + ", mmsURL: " + mmsURL
            );

            long start = System.currentTimeMillis();
            HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("killEncodingJob. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
            long start = System.currentTimeMillis();
            HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, putBodyRequest, outputToBeCompressed);
            mLogger.info("updateEncodingJobTryAgain. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

			joEncoder.put("label", label);
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

            long start = System.currentTimeMillis();
            String postContentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, postContentType,
                    timeoutInSeconds, maxRetriesNumber,
                    username, password, null, joEncoder.toString(), outputToBeCompressed);
            mLogger.info("addEncoder. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

			joEncoder.put("label", label);
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

            long start = System.currentTimeMillis();
            String postContentType = null;
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL,
                    timeoutInSeconds, maxRetriesNumber,
                    username, password, null, joEncoder.toString(), outputToBeCompressed);
            mLogger.info("modifyEncoder. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("removeEncoder. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getEncoders. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getEncoder. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getEncodersPool. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
		String label, List<Encoder> encoders)
	throws Exception
    {
		List<Long> encoderKeys = new ArrayList<>();
		
		for(Encoder encoder: encoders)
			encoderKeys.add(encoder.getEncoderKey());

		return addEncodersPoolByKeys(username, password, label, encoderKeys);
    }

    public Long addEncodersPoolByKeys(String username, String password,
		String label, List<Long> encoderListKeys)
	throws Exception
    {
        Long encoderPoolsKey;

        String mmsInfo;
        try
        {
            JSONObject joEncodersPool = new JSONObject();
            joEncodersPool.put("label", label);

            JSONArray jaEncoderKeys = new JSONArray();
            joEncodersPool.put("encoderKeys", jaEncoderKeys);

            for (Long encoderKey: encoderListKeys)
                jaEncoderKeys.put(encoderKey);

			String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/encodersPool";

            mLogger.info("addEncoder"
                    + ", mmsURL: " + mmsURL
                    + ", joEncodersPool: " + joEncodersPool.toString()
            );

            long start = System.currentTimeMillis();
            String postContentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, postContentType,
                    timeoutInSeconds, maxRetriesNumber,
                    username, password, null, joEncodersPool.toString(), outputToBeCompressed);
            mLogger.info("addEncodersPoolByKeys. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addEncodersPoolByKeys MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joWMMSInfo = new JSONObject(mmsInfo);

            encoderPoolsKey = joWMMSInfo.getLong("EncodersPoolKey");
        }
        catch (Exception e)
        {
            String errorMessage = "addEncodersPoolByKeys failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return encoderPoolsKey;
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
            joEncodersPool.put("label", label);

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

            long start = System.currentTimeMillis();
            String postContentType = null;
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL,
                    timeoutInSeconds, maxRetriesNumber,
                    username, password, null, joEncodersPool.toString(), outputToBeCompressed);
            mLogger.info("modifyEncodersPool. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("removeEncodersPool. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            String postContentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, postContentType,
                    timeoutInSeconds, maxRetriesNumber,
                    username, password, null, null, outputToBeCompressed);
            mLogger.info("assignEncoderToWorkspace. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            String postContentType = null;
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL,
                    timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("removeEncoderFromWorkspace. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

    public void getWorkspaceList(String username, String password, Boolean costDetails,
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
                    + (costDetails != null && costDetails ? "?costDetails=true" : "")
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getWorkspaceList. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

    public void getInvoiceList(String username, String password,
                                 List<Invoice> invoicesList)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/invoice"
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getInvoiceList. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
            JSONArray jaInvoices = joResponse.getJSONArray("invoices");

            mLogger.info("jaInvoices.length(): " + jaInvoices.length());

            invoicesList.clear();

            for (int invoiceIndex = 0;
                 invoiceIndex < jaInvoices.length();
                 invoiceIndex++)
            {
                Invoice invoice = new Invoice();

                JSONObject invoiceInfo = jaInvoices.getJSONObject(invoiceIndex);

                fillInvoice(invoice, invoiceInfo);

                invoicesList.add(invoice);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public Long addInvoice(String username, String password,
                           Long userKey, String description,
                           Long amount, Date expirationDate)
            throws Exception
    {
        Long invoiceKey;

        String mmsInfo;
        try
        {
            JSONObject joInvoice = new JSONObject();

            joInvoice.put("userKey", userKey);
            joInvoice.put("description", description);
            joInvoice.put("amount", amount);
            joInvoice.put("expirationDate", expirationDate);

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/invoice";

            mLogger.info("addInvoice"
                    + ", mmsURL: " + mmsURL
                    + ", joInvoice: " + joInvoice.toString()
            );

            long start = System.currentTimeMillis();
            String postContentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, postContentType,
                    timeoutInSeconds, maxRetriesNumber,
                    username, password, null, joInvoice.toString(), outputToBeCompressed);
            mLogger.info("addInvoice. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addInvoice MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            JSONObject joWMMSInfo = new JSONObject(mmsInfo);

            invoiceKey = joWMMSInfo.getLong("invoiceKey");
        }
        catch (Exception e)
        {
            String errorMessage = "addInvoice failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return invoiceKey;
    }

    public Long getMediaItems(String username, String password,
                              long startIndex, long pageSize,
                              Long mediaItemKey, String uniqueName,
                              List<Long> otherMediaItemsKey,
                              String contentType,   // video, audio, image
                              Date ingestionStart, Date ingestionEnd,
                              String title, Boolean bLiveRecordingChunk,
                              List<String> tagsIn, List<String> tagsNotIn,
                              Long recordingCode, String jsonCondition,
                              String orderBy, String jsonOrderBy,
							  JSONObject joResponseFields,
                              List<MediaItem> mediaItemsList    // has to be already initialized (new ArrayList<>())
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
                    + (recordingCode == null ? "" : ("&recordingCode=" + recordingCode))
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

            long start = System.currentTimeMillis();
			if (newMediaItemKey == null && body != null && body.length() > 0)
			{
				String postContentType = null;
				mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, postContentType, timeoutInSeconds, maxRetriesNumber,
						username, password, null, body, outputToBeCompressed);
			}
			else
			{
				mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
						username, password, null, outputToBeCompressed);
			}
            mLogger.info("getMediaItems. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getMediaItemByMediaItemKey. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
				username, password, null, outputToBeCompressed);
            mLogger.info("getMediaItemByPhysicalPathKey. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getMediaItemByUniqueName. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
                joEdit.put("title", newTitle);
            if (newUserData != null)
                joEdit.put("userData", newUserData); // mms backend manages this field as string since it saves it as string into DB
            if (jaTags != null)
                joEdit.put("tags", jaTags); // mms backend manages this field as a json array to get the tags
            if (newRetentionInMinutes != null)
                joEdit.put("RetentionInMinutes", newRetentionInMinutes);
            if (newUniqueName != null)
                joEdit.put("uniqueName", newUniqueName);

            String sEdit = joEdit.toString(4);

            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, sEdit, outputToBeCompressed);
            mLogger.info("updateMediaItem. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, sEdit, outputToBeCompressed);
            mLogger.info("updatePhysicalPath. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getWorkspaceUsageInMB. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getTags. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start1 = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getIngestionWorkflows. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start1) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getIngestionWorkflow. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
                                 String label, Boolean labelLike, Long ingestionJobKey,
                                 Date start, Date end,
								 Date startScheduleDate,
                                 String status,             // completed or notCompleted
                                 String ingestionType, 
								 String configurationLabel,	// used in case of Live-Proxy
								 String outputChannelLabel,	// used in case of Live-Grid
								 Long recordingCode,	// used in case of Live-Recorder
								 Boolean broadcastIngestionJobKeyNotNull,	// used in case of Broadcaster
								 // String jsonParametersCondition, // altamente sconsigliato perchè poco performante
                                 boolean ingestionDateAscending,
                                 boolean dependencyInfo,	// if true adds: dependOnIngestionJobKey, dependOnSuccess, dependencyIngestionStatus				 
                                 boolean ingestionJobOutputs,
								 Boolean fromMaster,
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
                    + (labelLike == null ? "" : "&labelLike=" + labelLike)
                    + "&status=" + (status == null ? "" : status)
                    + ((ingestionType == null || ingestionType.equalsIgnoreCase("all")) ? "" : ("&ingestionType=" + ingestionType))
                    + (configurationLabel == null || configurationLabel.isEmpty() ? "" : ("&configurationLabel=" + 
                    	java.net.URLEncoder.encode(configurationLabel, "UTF-8"))) // requires unescape server side
					+ (outputChannelLabel == null || outputChannelLabel.isEmpty() ? "" : ("&outputChannelLabel=" + 
                    	java.net.URLEncoder.encode(outputChannelLabel, "UTF-8"))) // requires unescape server side
					+ (recordingCode == null ? "" : ("&recordingCode=" + recordingCode))
					+ (broadcastIngestionJobKeyNotNull == null ? "" : ("&broadcastIngestionJobKeyNotNull=" + broadcastIngestionJobKeyNotNull))
                    // + "&jsonParametersCondition=" + (jsonParametersCondition == null || jsonParametersCondition.isEmpty()
                    //	? "" : java.net.URLEncoder.encode(jsonParametersCondition, "UTF-8")) // requires unescape server side
                    + "&asc=" + (ingestionDateAscending ? "true" : "false")
                    + "&dependencyInfo=" + (dependencyInfo ? "true" : "false")
                    + "&ingestionJobOutputs=" + (ingestionJobOutputs ? "true" : "false")
                    + (start == null ? "" : ("&startIngestionDate=" + simpleDateFormat.format(start)))
                    + (end == null ? "" : ("&endIngestionDate=" + simpleDateFormat.format(end)))
                    + (startScheduleDate == null ? "" : ("&startScheduleDate=" + simpleDateFormat.format(startScheduleDate)))
					+ (fromMaster == null ? "" : "&fromMaster=" + fromMaster)
					;

            mLogger.info("mmsURL: " + mmsURL);

            /*
            2020-06-07: MMS GUI asks for LiveRecorder (ingestionJobs), the return contains all the MediaItems Output that,
                for each live recorder, are really a lot.
                So, in this scenario, make sure to have a long timeoutInSeconds, otherwise it will raise a timeout exception
             */
            long start1 = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getIngestionJobs. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start1) + "@ millisecs.");

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
        Long ingestionJobKey, boolean ingestionJobOutputs,
		Boolean fromMaster)
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
				+ (fromMaster == null ? "" : "&fromMaster=" + fromMaster)
            ;

            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getIngestionJob. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("cancelIngestionJob. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, bodyRequest, outputToBeCompressed);
            mLogger.info("updateIngestionJob_LiveRecorder. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "updateIngestionJob_LiveRecorder failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public String ingestBinaryContentSplittingInChunks(String username, String password,
	InputStream binaryFileInputStream, long fileSizeInBytes,
        Long ingestionJobKey)
        throws Exception
    {
		String httpReturn = null;
		// InputStream binaryFileInputStream = null;
        long chunksNumber = 0;
        long chunkIndex = 0;
        try
        {
			String mmsURL = mmsBinaryProtocol + "://" + mmsBinaryHostName + ":" + mmsBinaryPort
				+ "/catramms/1.0.1/binary/" + ingestionJobKey;
			
			mLogger.info("ingestBinaryContent"
				+ ", mmsURL: " + mmsURL
				+ ", fileSizeInBytes: " + fileSizeInBytes
				+ ", ingestionJobKey: " + ingestionJobKey
			);

			// binaryFileInputStream = new DataInputStream(new FileInputStream(mediaFile));

			long chunkSize = 100 * 1000 * 1000;

			if (fileSizeInBytes <= chunkSize)
			{
				long start = System.currentTimeMillis();
				httpReturn = HttpFeedFetcher.fetchPostHttpBinary(mmsURL, binaryTimeoutInSeconds, maxRetriesNumber,
						username, password, binaryFileInputStream, fileSizeInBytes, -1, -1);
				mLogger.info("ingestBinaryContent. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");

				return httpReturn;
			}

			chunksNumber = (long) (fileSizeInBytes / chunkSize);
			if (fileSizeInBytes % chunkSize != 0)                                                                     
				chunksNumber++;

			for(chunkIndex = 0; chunkIndex < chunksNumber; chunkIndex++)
			{                                                                                                         
				long contentRangeStart = chunkIndex * chunkSize;                                                   
				long contentRangeEnd_Excluded = chunkIndex + 1 < chunksNumber ?                                    
					(chunkIndex + 1) * chunkSize :                                                                    
					fileSizeInBytes;

                mLogger.info("ingestBinaryContent"
                    + ", mmsURL: " + mmsURL
                    + ", fileSizeInBytes: " + fileSizeInBytes
                    + ", contentRangeStart: " + contentRangeStart
                    + ", contentRangeEnd_Excluded: " + contentRangeEnd_Excluded
                );

				long start = System.currentTimeMillis();
				httpReturn = HttpFeedFetcher.fetchPostHttpBinary(mmsURL, binaryTimeoutInSeconds, maxRetriesNumber,
					username, password, binaryFileInputStream, fileSizeInBytes,
					contentRangeStart, contentRangeEnd_Excluded);
				mLogger.info("ingestBinaryContent. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
			}
        }
        catch (Exception e)
        {
            String errorMessage = "ingestWorkflow MMS failed"
                + ", chunkIndex: " + chunkIndex
                + ", chunksNumber: " + chunksNumber
                + ", Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
		/*
		finally{
			if (binaryFileInputStream != null)
				binaryFileInputStream.close();
		}
		*/

		return httpReturn;
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

            long start = System.currentTimeMillis();
            HttpFeedFetcher.fetchPostHttpBinary(mmsURL, binaryTimeoutInSeconds, maxRetriesNumber,
                    username, password, fileInputStream, contentSize,
					-1, -1);
            mLogger.info("ingestBinaryContent. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "ingestWorkflow MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void changeLiveProxyPlaylist(String username, String password,
        Long broadcasterIngestionJobKey,
        List<BroadcastPlaylistItem> broadcastPlaylistItems,
		String switchBehaviour	// applyNewPlaylistNow or applyNewPlaylistAtTheEndOfCurrentMedia
	)
        throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/ingestionJob/liveProxy/playlist/" + broadcasterIngestionJobKey
					+ "?switchBehaviour=" + switchBehaviour;

            JSONArray jaBodyRequest = new JSONArray();

			for (BroadcastPlaylistItem broadcastPlaylistItem: broadcastPlaylistItems)
				jaBodyRequest.put(broadcastPlaylistItem.getJson2());

            String bodyRequest = jaBodyRequest.toString();

            mLogger.info("changeLiveProxyPlaylist"
                    + ", mmsURL: " + mmsURL
                    + ", bodyRequest: " + bodyRequest
            );

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, bodyRequest, outputToBeCompressed);
            mLogger.info("changeLiveProxyPlaylist. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
								Boolean fromMaster,
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
				+ (fromMaster == null ? "" : "&fromMaster=" + fromMaster)
			;

            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getEncodingJobs. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
        Long encodingJobKey, Boolean fromMaster)
        throws Exception
    {
        Long numFound;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/encodingJob/" + encodingJobKey
				+ (fromMaster == null ? "" : "?fromMaster=" + fromMaster)
			;

            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getEncodingJob. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getEncodingProfile. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getEncodingProfiles. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

                // 2023-05-06: cambiato a true perchè serve in EncodingProfiles.java
                boolean deep = true;
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getEncodingProfilesSet. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getEncodingProfilesSets. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

                boolean deep = true;
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
        // non posso usare mappe perchè se viene richiesto piu volte lo stesso mediaItemKey, la mappa sovrascrive il dato.
        // Per cui uso delle list, inoltre il ritorno mandiene l'ordine di input e quindi non ci sono problemi
        List<BulkOfDeliveryURLData> bulkOfDeliveryURLDataMapByUniqueName = new ArrayList<>();
        List<BulkOfDeliveryURLData> bulkOfDeliveryURLDataMapByMediaItemKey = new ArrayList<>();
        List<BulkOfDeliveryURLData> bulkOfDeliveryURLDataMapByLiveIngestionJobKey = new ArrayList<>();
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
						if (bulkOfDeliveryURLData.getUserId() != null && !bulkOfDeliveryURLData.getUserId().isEmpty())
							joMediaItemKey.put("userId", bulkOfDeliveryURLData.getUserId());

						bulkOfDeliveryURLDataMapByMediaItemKey.add(bulkOfDeliveryURLData);
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
						if (bulkOfDeliveryURLData.getUserId() != null && !bulkOfDeliveryURLData.getUserId().isEmpty())
							joUniqueName.put("userId", bulkOfDeliveryURLData.getUserId());

						bulkOfDeliveryURLDataMapByUniqueName.add(bulkOfDeliveryURLData);
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
						if (bulkOfDeliveryURLData.getUserId() != null && !bulkOfDeliveryURLData.getUserId().isEmpty())
							joLiveIngestionJobKey.put("userId", bulkOfDeliveryURLData.getUserId());

                        bulkOfDeliveryURLDataMapByLiveIngestionJobKey.add(bulkOfDeliveryURLData);
                    }
                }
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort
                    + "/catramms/1.0.1/delivery/bulk"
                    + "?ttlInSeconds=" + ttlInSeconds
                    + "&maxRetries=" + maxRetries
			;
            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            String postContentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, postContentType,
                    timeoutInSeconds, maxRetriesNumber,
                    username, password, null,
                    joDeliveryAuthorizationDetails.toString(), outputToBeCompressed);
            mLogger.info("getBulkOfDeliveryURL. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
                    /*
                    if (joMediaItemKey.has("mediaItemKey") && joMediaItemKey.has("deliveryURL")
                        && !joMediaItemKey.isNull("mediaItemKey") && !joMediaItemKey.isNull("deliveryURL"))
                    {
                        BulkOfDeliveryURLData bulkOfDeliveryURLData
                                = bulkOfDeliveryURLDataMapByMediaItemKey.get(joMediaItemKey.getLong("mediaItemKey"));
                        bulkOfDeliveryURLData.setDeliveryURL(joMediaItemKey.getString("deliveryURL"));
                    }
                    */
                    if (joMediaItemKey.has("deliveryURL") && !joMediaItemKey.isNull("deliveryURL"))
                        bulkOfDeliveryURLDataMapByMediaItemKey.get(mediaItemKeyIndex).setDeliveryURL(
                                joMediaItemKey.getString("deliveryURL"));
                }
            }

			if (joDeliveryURLList.has("uniqueNameList"))
            {
                JSONArray jaUniqueNameList = joDeliveryURLList.getJSONArray("uniqueNameList");
                for (int uniqueNameIndex = 0; uniqueNameIndex < jaUniqueNameList.length(); uniqueNameIndex++)
                {
                    JSONObject joUniqueName = jaUniqueNameList.getJSONObject(uniqueNameIndex);

                    /*
                    if (joUniqueName.has("uniqueName") && joUniqueName.has("deliveryURL")
                        && !joUniqueName.isNull("uniqueName") && !joUniqueName.isNull("deliveryURL"))
                    {
                        BulkOfDeliveryURLData bulkOfDeliveryURLData
                                = bulkOfDeliveryURLDataMapByUniqueName.get(joUniqueName.getString("uniqueName"));
                        bulkOfDeliveryURLData.setDeliveryURL(joUniqueName.getString("deliveryURL"));
                    }
                    */
                    if (joUniqueName.has("deliveryURL") && !joUniqueName.isNull("deliveryURL"))
                        bulkOfDeliveryURLDataMapByUniqueName.get(uniqueNameIndex).setDeliveryURL(
                                joUniqueName.getString("deliveryURL"));
                }
            }

            if (joDeliveryURLList.has("liveIngestionJobKeyList"))
            {
                JSONArray jaLiveIngestionJobKeyList = joDeliveryURLList.getJSONArray("liveIngestionJobKeyList");
                for (int liveIngestionJobKeyIndex = 0; liveIngestionJobKeyIndex < jaLiveIngestionJobKeyList.length(); liveIngestionJobKeyIndex++)
                {
                    JSONObject joLiveIngestionJobKey = jaLiveIngestionJobKeyList.getJSONObject(liveIngestionJobKeyIndex);

                    /*
                    if (joLiveIngestionJobKey.has("ingestionJobKey") && joLiveIngestionJobKey.has("deliveryURL")
                            && !joLiveIngestionJobKey.isNull("ingestionJobKey") && !joLiveIngestionJobKey.isNull("deliveryURL"))
                    {
                        BulkOfDeliveryURLData bulkOfDeliveryURLData
                                = bulkOfDeliveryURLDataMapByLiveIngestionJobKey.get(joLiveIngestionJobKey.getLong("ingestionJobKey"));
                        bulkOfDeliveryURLData.setDeliveryURL(joLiveIngestionJobKey.getString("deliveryURL"));
                    }
                    */
                    if (joLiveIngestionJobKey.has("deliveryURL") && !joLiveIngestionJobKey.isNull("deliveryURL"))
                        bulkOfDeliveryURLDataMapByLiveIngestionJobKey.get(liveIngestionJobKeyIndex).setDeliveryURL(
                                joLiveIngestionJobKey.getString("deliveryURL"));
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

        // first option (encodingProfileKey or encodingProfileLabel potrebbe essere anche null, vedi commento sotto)
        Long mediaItemKey, String uniqueName, Long encodingProfileKey, String encodingProfileLabel,

        // second option
        PhysicalPath physicalPath,

		long ttlInSeconds, int maxRetries,
		// MMS_Token: delivery by MMS with a Token
		// MMS_SignedToken: delivery by MMS with a signed URL
		// AWSCloudFront: delivery by AWS CloudFront without a signed URL
		// AWSCloudFront_Signed: delivery by AWS CloudFront with a signed URL
		String deliveryType,

		Boolean save,					// true: file name will be the title of the Media
		Boolean filteredByStatistic,		// true: not saved as statistic
		String userId		// used only in case filteredByStatistic is false
	)
		throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            if (physicalPath == null
                    && ((mediaItemKey == null && uniqueName == null)
                        // commentato perchè profile == -1 indica che si vuole il source profile
                        // || (encodingProfileKey == null && (encodingProfileLabel == null || encodingProfileLabel.isEmpty()))
                    )
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
						+ (userId == null || userId.isEmpty() ? "" : ("&userId=" + java.net.URLEncoder.encode(userId, "UTF-8")))
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
                                + "&encodingProfileLabel=" + (encodingProfileLabel == null ? "" : java.net.URLEncoder.encode(encodingProfileLabel, "UTF-8")) // requires unescape server side
                                + "&ttlInSeconds=" + ttlInSeconds
                                + "&maxRetries=" + maxRetries
                                + "&save=" + save.toString()
								+ "&filteredByStatistic=" + (filteredByStatistic == null ? false : filteredByStatistic.toString())
								+ (userId == null || userId.isEmpty() ? "" : ("&userId=" + java.net.URLEncoder.encode(userId, "UTF-8")))
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
								+ (userId == null || userId.isEmpty() ? "" : ("&userId=" + java.net.URLEncoder.encode(userId, "UTF-8")))
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
                            + "?encodingProfileLabel=" + (encodingProfileLabel == null ? "" : java.net.URLEncoder.encode(encodingProfileLabel, "UTF-8")) // requires unescape server side
                            + "&ttlInSeconds=" + ttlInSeconds
                            + "&maxRetries=" + maxRetries
                            + "&save=" + save.toString()
							+ "&filteredByStatistic=" + (filteredByStatistic == null ? false : filteredByStatistic.toString())
							+ (userId == null || userId.isEmpty() ? "" : ("&userId=" + java.net.URLEncoder.encode(userId, "UTF-8")))
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
								+ (userId == null || userId.isEmpty() ? "" : ("&userId=" + java.net.URLEncoder.encode(userId, "UTF-8")))
								+ "&deliveryType=" + (deliveryType == null || deliveryType.isEmpty() ? "MMS_SignedToken" : deliveryType)
                                + "&redirect=false"
                        ;
                    }
                }
            }

            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getVODDeliveryURL. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
            String errorMessage = "getVODDeliveryURL failed. Exception: " + e;
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

		// IN REALTA' deliveryType nel caso di live viene utilizzato parzialmente perchè:
		//	- in caso di RTMP, il campo PlayURL nell'IngestionJob decide l'utl di delivery.
		//		Infatti in questo scenario, solo chi crea il Task può sapere la deliveryURL  
		//	- in caso di HLS viene invece utilizzato questo campo che potrà variare tra
		//		tra MMS_Token o MMS_SignedToken
		// MMS_Token: delivery by MMS with a Token
		// MMS_SignedToken: delivery by MMS with a signed URL
		// AWSCloudFront: delivery by AWS CloudFront with a signed URL
		// AWSCloudFront_Signed: delivery by AWS CloudFront with a signed URL
		String deliveryType,
		Boolean filteredByStatistic,
		String userId		// used only in case filteredByStatistic is false
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
					+ (userId == null || userId.isEmpty() ? "" : ("&userId=" + java.net.URLEncoder.encode(userId, "UTF-8")))
					;

            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getLiveDeliveryURL. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
            String errorMessage = "getLiveDeliveryURL failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return deliveryURL;
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getWorkflowsLibrary. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getWorkflowLibraryContent. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, workflowAsLibrary, outputToBeCompressed);
            mLogger.info("saveWorkflowAsLibrary. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("removeWorkflowAsLibrary. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonYouTubeConf, outputToBeCompressed);
            mLogger.info("addYouTubeConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonYouTubeConf, outputToBeCompressed);
            mLogger.info("modifyYouTubeConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("removeYouTubeConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeYouTubeConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public List<YouTubeConf> getYouTubeConf(String username, String password, String label)
            throws Exception
    {
        List<YouTubeConf> youTubeConfList = new ArrayList<>();

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/youtube"
                + "?label=" + (label == null ? "" : java.net.URLEncoder.encode(label, "UTF-8")) // requires unescape server side
            ;

            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getYouTubeConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
                               String label, String userAccessToken)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonFacebookConf;
            {
                JSONObject joFacebookConf = new JSONObject();

                joFacebookConf.put("label", label);
                joFacebookConf.put("UserAccessToken", userAccessToken);

                jsonFacebookConf = joFacebookConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/facebook";

            mLogger.info("addFacebookConf"
                    + ", mmsURL: " + mmsURL
                    + ", jsonFacebookConf: " + jsonFacebookConf
            );

            long start = System.currentTimeMillis();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonFacebookConf, outputToBeCompressed);
            mLogger.info("addFacebookConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addFacebookConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void modifyFacebookConf(String username, String password,
        Long confKey, String label, String userAccessToken)
        throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonFacebookConf;
            {
                JSONObject joFacebookConf = new JSONObject();

                joFacebookConf.put("label", label);
                joFacebookConf.put("UserAccessToken", userAccessToken);

                jsonFacebookConf = joFacebookConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/facebook/" + confKey;

            mLogger.info("modifyFacebookConf"
                    + ", mmsURL: " + mmsURL
                    + ", jsonFacebookConf: " + jsonFacebookConf
            );

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonFacebookConf, outputToBeCompressed);
            mLogger.info("modifyFacebookConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("removeFacebookConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeFacebookConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public List<FacebookConf> getFacebookConf(String username, String password,
		Long confKey,	// optional
		String label	// optional
	)
        throws Exception
    {
        List<FacebookConf> facebookConfList = new ArrayList<>();

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/facebook";
			if (confKey != null)
				mmsURL += "/" + confKey;
			else if (label != null && !label.isEmpty())
				mmsURL += ("?label=" + java.net.URLEncoder.encode(label, "UTF-8")); // requires unescape server side

            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getFacebookConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

    public void addTwitchConf(String username, String password,
                               String label, String refreshToken)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonTwitchConf;
            {
                JSONObject joTwitchConf = new JSONObject();

                joTwitchConf.put("label", label);
                joTwitchConf.put("RefreshToken", refreshToken);

                jsonTwitchConf = joTwitchConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/twitch";

            mLogger.info("addTwitchConf"
                    + ", mmsURL: " + mmsURL
                    + ", jsonTwitchConf: " + jsonTwitchConf
            );

            long start = System.currentTimeMillis();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonTwitchConf, outputToBeCompressed);
            mLogger.info("addTwitchConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addTwitchConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void modifyTwitchConf(String username, String password,
        Long confKey, String label, String refreshToken)
        throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonTwitchConf;
            {
                JSONObject joTwitchConf = new JSONObject();

                joTwitchConf.put("label", label);
                joTwitchConf.put("RefreshToken", refreshToken);

                jsonTwitchConf = joTwitchConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/twitch/" + confKey;

            mLogger.info("modifyTwitchConf"
                    + ", mmsURL: " + mmsURL
                    + ", jsonTwitchConf: " + jsonTwitchConf
            );

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonTwitchConf, outputToBeCompressed);
            mLogger.info("modifyTwitchConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "modifyTwitchConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void removeTwitchConf(String username, String password,
                                  Long confKey)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/twitch/" + confKey;

            mLogger.info("removeTwitchConf"
                    + ", mmsURL: " + mmsURL
                    + ", confKey: " + confKey
            );

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("removeTwitchConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeTwitchConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public List<TwitchConf> getTwitchConf(String username, String password,
		Long confKey,	// optional
		String label	// optional
	)
        throws Exception
    {
        List<TwitchConf> twitchConfList = new ArrayList<>();

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/twitch";
			if (confKey != null)
				mmsURL += "/" + confKey;
			else if (label != null && !label.isEmpty())
				mmsURL += ("?label=" + java.net.URLEncoder.encode(label, "UTF-8")); // requires unescape server side

            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getTwitchConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
            JSONArray jaTwitchConf = joResponse.getJSONArray("twitchConf");

            mLogger.info("jaTwitchConf.length(): " + jaTwitchConf.length());

            twitchConfList.clear();

            for (int twitchConfIndex = 0;
                 twitchConfIndex < jaTwitchConf.length();
                 twitchConfIndex++)
            {
                TwitchConf twitchConf = new TwitchConf();

                JSONObject twitchConfInfo = jaTwitchConf.getJSONObject(twitchConfIndex);

                fillTwitchConf(twitchConf, twitchConfInfo);

                twitchConfList.add(twitchConf);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing twitchConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return twitchConfList;
    }

    public List<CostConf> getCostsConf(String username, String password
    )
            throws Exception
    {
        List<CostConf> costsConfList = new ArrayList<>();

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/costs";

            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getCostsConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
            JSONArray jaCostsConf = joResponse.getJSONArray("costsConf");

            mLogger.info("jaCostsConf.length(): " + jaCostsConf.length());

            costsConfList.clear();

            for (int costsConfIndex = 0;
                 costsConfIndex < jaCostsConf.length();
                 costsConfIndex++)
            {
                CostConf costConf = new CostConf();

                JSONObject costConfInfo = jaCostsConf.getJSONObject(costsConfIndex);

                fillCostConf(costConf, costConfInfo);

                costsConfList.add(costConf);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing costConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return costsConfList;
    }

    public void addTiktokConf(String username, String password,
                               String label, String token)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonTiktokConf;
            {
                JSONObject joTiktokConf = new JSONObject();

                joTiktokConf.put("label", label);
                joTiktokConf.put("Token", token);

                jsonTiktokConf = joTiktokConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/tiktok";

            mLogger.info("addTiktokConf"
                    + ", mmsURL: " + mmsURL
                    + ", jsonTiktokConf: " + jsonTiktokConf
            );

            long start = System.currentTimeMillis();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonTiktokConf, outputToBeCompressed);
            mLogger.info("addTiktokConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addTiktokConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void modifyTiktokConf(String username, String password,
        Long confKey, String label, String token)
        throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonTiktokConf;
            {
                JSONObject joTiktokConf = new JSONObject();

                joTiktokConf.put("label", label);
                joTiktokConf.put("Token", token);

                jsonTiktokConf = joTiktokConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/tiktok/" + confKey;

            mLogger.info("modifyTiktokConf"
                    + ", mmsURL: " + mmsURL
                    + ", jsonTiktokConf: " + jsonTiktokConf
            );

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonTiktokConf, outputToBeCompressed);
            mLogger.info("modifyTiktokConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "modifyTiktokConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void removeTiktokConf(String username, String password,
                                  Long confKey)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/tiktok/" + confKey;

            mLogger.info("removeTiktokConf"
                    + ", mmsURL: " + mmsURL
                    + ", confKey: " + confKey
            );

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("removeTiktokConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeTiktokConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public List<TiktokConf> getTiktokConf(String username, String password,
		Long confKey,	// optional
		String label	// optional
	)
        throws Exception
    {
        List<TiktokConf> tiktokConfList = new ArrayList<>();

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/tiktok";
			if (confKey != null)
				mmsURL += "/" + confKey;
			else if (label != null && !label.isEmpty())
				mmsURL += ("?label=" + java.net.URLEncoder.encode(label, "UTF-8")); // requires unescape server side

            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getTiktokConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
            JSONArray jaTiktokConf = joResponse.getJSONArray("tiktokConf");

            mLogger.info("jaTiktokConf.length(): " + jaTiktokConf.length());

            tiktokConfList.clear();

            for (int tiktokConfIndex = 0;
                 tiktokConfIndex < jaTiktokConf.length();
                 tiktokConfIndex++)
            {
                TiktokConf tiktokConf = new TiktokConf();

                JSONObject tiktokConfInfo = jaTiktokConf.getJSONObject(tiktokConfIndex);

                fillTiktokConf(tiktokConf, tiktokConfInfo);

                tiktokConfList.add(tiktokConf);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing tiktokConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return tiktokConfList;
    }

	public Long addStream(String username, String password,
		String label, 
		String sourceType,
		Long encodersPoolKey,
		String url, 
		String pushProtocol,
		Long pushEncoderKey,
		String pushServerName,	// indica il nome del server (public or internal)
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
		Long sourceTVConfKey,
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
				if (encodersPoolKey != null)
                	joStreamConf.put("encodersPoolKey", encodersPoolKey);
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
				if (sourceTVConfKey != null)
                	joStreamConf.put("sourceTVConfKey", sourceTVConfKey);
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

            long start = System.currentTimeMillis();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonStream, outputToBeCompressed);
            mLogger.info("addStream. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
		Long encodersPoolKey,
		String url, 
		String pushProtocol,
		Long pushEncoderKey,
		String pushServerName,	// indica il nome del server (public or internal)
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
		Long sourceTVConfKey,
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
				if (encodersPoolKey != null)
                	joStreamConf.put("encodersPoolKey", encodersPoolKey);
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
				if (sourceTVConfKey != null)
                	joStreamConf.put("sourceTVConfKey", sourceTVConfKey);
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonStreamConf, outputToBeCompressed);
            mLogger.info("modifyStream. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("removeStream. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
                               Long confKey, String label, Boolean labelLike,
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
                    + (labelLike == null ? "" : ("&labelLike=" + labelLike.toString().toLowerCase()))
                    + "&url=" + (url == null ? "" : java.net.URLEncoder.encode(url, "UTF-8"))
                    + "&sourceType=" + (sourceType == null ? "" : sourceType)
                    + "&type=" + (type == null ? "" : java.net.URLEncoder.encode(type, "UTF-8"))
                    + "&name=" + (name == null ? "" : java.net.URLEncoder.encode(name, "UTF-8"))
                    + "&region=" + (region == null ? "" : java.net.URLEncoder.encode(region, "UTF-8"))
                    + "&country=" + (country == null ? "" : java.net.URLEncoder.encode(country, "UTF-8"))
                    + "&labelOrder=" + (labelOrder == null ? "" : labelOrder)
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getStream. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getStream. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

    public Long addSourceTVStream(String username, String password,
                                        String type, Long serviceId, Long networkId, Long transportStreamId,
                                        String name, String satellite, Long frequency, String lnb,
                                        Long videoPid, String audioPids, Long audioItalianPid, Long audioEnglishPid, Long teletextPid,
                                        String modulation, String polarization, Long symbolRate, Long bandwidthInHz,
										String country, String deliverySystem)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonStream;
            {
                JSONObject joStream = new JSONObject();

                joStream.put("type", type);
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
                joStream.put("bandwidthInHz", bandwidthInHz);
                if (country != null && !country.isEmpty())
                    joStream.put("country", country);
                if (deliverySystem != null && !deliverySystem.isEmpty())
                    joStream.put("deliverySystem", deliverySystem);

                jsonStream = joStream.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/sourceTVStream";

            mLogger.info("addSourceTVStream"
                    + ", mmsURL: " + mmsURL
                    + ", jsonStream: " + jsonStream
            );

            long start = System.currentTimeMillis();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonStream, outputToBeCompressed);
            mLogger.info("addSourceTVStream. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addSourceTVStream MMS failed. Exception: " + e;
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

    public void modifySourceTVStream(String username, String password,
                                           Long confKey, String type, Long serviceId, Long networkId, Long transportStreamId,
                                           String name, String satellite, Long frequency, String lnb,
                                           Long videoPid, String audioPids, Long audioItalianPid, Long audioEnglishPid, Long teletextPid,
                                           String modulation, String polarization, Long symbolRate, Long bandwidthInHz, String country, String deliverySystem
    )
            throws Exception
    {

        String mmsInfo;
        try
        {
            mLogger.info("modifySourceTVStream"
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

                if (type != null)
                    joStream.put("type", type);
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
				if (bandwidthInHz != null)
                    joStream.put("bandwidthInHz", bandwidthInHz);
                if (country != null && !country.isEmpty())
                    joStream.put("country", country);
                if (deliverySystem != null && !deliverySystem.isEmpty())
                    joStream.put("deliverySystem", deliverySystem);

                jsonStream = joStream.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/sourceTVStream/" + confKey;

            mLogger.info("modifySourceTVStream"
                    + ", mmsURL: " + mmsURL
                    + ", jsonStream: " + jsonStream
            );

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonStream, outputToBeCompressed);
            mLogger.info("modifySourceTVStream. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "modifySourceTVStream MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void removeSourceTVStream(String username, String password,
                                     Long confKey)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/sourceTVStream/" + confKey;

            mLogger.info("removeTVStream"
                    + ", mmsURL: " + mmsURL
                    + ", confKey: " + confKey
            );

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("removeSourceTVStream. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeSourceTVStream MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public Long getSourceTVStreams(String username, String password,
                                  long startIndex, long pageSize,
                                  Long confKey,
                                  String type, Long serviceId, String name, Long frequency, String lnb,
                                  Long videoPid, String audioPids,
                                  String nameOrder,   // asc or desc
                                  List<SourceTVStream> sourceTVStreams)
            throws Exception
    {
        Long numFound;

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/sourceTVStream"
                    + (confKey == null ? "" : ("/" + confKey))
                    + "?start=" + startIndex
                    + "&rows=" + pageSize
                    + (type == null  ? "" : ("&type=" + type))
                    + (serviceId == null  ? "" : ("&serviceId=" + serviceId))
                    + (name == null || name.isEmpty() ? "" : ("&name=" + java.net.URLEncoder.encode(name, "UTF-8"))) // requires unescape server side
                    + (frequency == null  ? "" : ("&frequency=" + frequency))
                    + (lnb == null || lnb.isEmpty() ? "" : ("&lnb=" + java.net.URLEncoder.encode(lnb, "UTF-8"))) // requires unescape server side
                    + (videoPid == null  ? "" : ("&videoPid=" + videoPid))
                    + (audioPids == null || audioPids.isEmpty() ? "" : ("&audioPids=" + java.net.URLEncoder.encode(audioPids, "UTF-8")))
                    + (nameOrder == null || nameOrder.isEmpty() ? "" : ("&nameOrder=" + nameOrder))
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getSourceTVStreams. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            JSONArray jaStreams = joResponse.getJSONArray("sourceTVStreams");

            mLogger.info("jaStreams.length(): " + jaStreams.length()
            );

            sourceTVStreams.clear();

            for (int streamIndex = 0; streamIndex < jaStreams.length(); streamIndex++)
            {
                SourceTVStream sourceTVStream = new SourceTVStream();

                JSONObject streamInfo = jaStreams.getJSONObject(streamIndex);

                fillSourceTVStream(sourceTVStream, streamInfo);

                sourceTVStreams.add(sourceTVStream);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing sourceTVStreams failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return numFound;
    }

    public SourceTVStream getSourceTVStream(String username, String password,
                                            Long serviceId)
            throws Exception
    {
        SourceTVStream stream = null;

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/sourceTVStream"
                    + "/" + serviceId
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getSourceTVStream. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            JSONArray jaStreams = joResponse.getJSONArray("sourceTVStreams");

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
                stream = new SourceTVStream();

                JSONObject streamInfo = jaStreams.getJSONObject(0);

                fillSourceTVStream(stream, streamInfo);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing sourceTVStreams failed. Exception: " + e;
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

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/cdn/aws/channel";

            mLogger.info("addAWSChannelConf"
                            + ", mmsURL: " + mmsURL
                            + ", jsonAWSChannelConf: " + jsonAWSChannelConf
            );

            long start = System.currentTimeMillis();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonAWSChannelConf, outputToBeCompressed);
            mLogger.info("addAWSChannelConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/cdn/aws/channel/" + confKey;

            mLogger.info("modifyAWSChannelConf"
                            + ", mmsURL: " + mmsURL
                            + ", jsonAWSChannelConf: " + jsonAWSChannelConf
            );

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonAWSChannelConf, outputToBeCompressed);
            mLogger.info("modifyAWSChannelConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/cdn/aws/channel/" + confKey;

            mLogger.info("removeAWSChannelConf"
                + ", mmsURL: " + mmsURL
                + ", confKey: " + confKey
            );

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("removeAWSChannelConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeAWSChannelConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public List<AWSChannelConf> getAWSChannelConf(String username, String password, String label, String type)
            throws Exception
    {
        List<AWSChannelConf> awsChannelConfList = new ArrayList<>();

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/cdn/aws/channel"
                + (label == null || label.isEmpty() ? "" : ("?label=" +  java.net.URLEncoder.encode(label, "UTF-8"))) // requires unescape server side
            ;
            if (type != null && !type.isBlank())
            {
                if (mmsURL.indexOf("channel?") != -1)
                    mmsURL += "&";
                else
                    mmsURL += "?";
                mmsURL += ("type=" + type);
            }

            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getAWSChannelConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

    public void addCDN77ChannelConf(String username, String password,
                                    String label, String rtmpURL, String resourceURL, String filePath, String secureToken, String type)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonCDN77ChannelConf;
            {
                JSONObject joCDN77ChannelConf = new JSONObject();

                joCDN77ChannelConf.put("label", label);
                joCDN77ChannelConf.put("rtmpURL", rtmpURL);
                joCDN77ChannelConf.put("resourceURL", resourceURL);
                joCDN77ChannelConf.put("filePath", filePath);
                if (secureToken != null)
                    joCDN77ChannelConf.put("secureToken", secureToken);
                joCDN77ChannelConf.put("type", type);

                jsonCDN77ChannelConf = joCDN77ChannelConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/cdn/cdn77/channel";

            mLogger.info("addCDN77ChannelConf"
                    + ", mmsURL: " + mmsURL
                    + ", jsonCDN77ChannelConf: " + jsonCDN77ChannelConf
            );

            long start = System.currentTimeMillis();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonCDN77ChannelConf, outputToBeCompressed);
            mLogger.info("addCDN77ChannelConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addCDN77ChannelConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void modifyCDN77ChannelConf(String username, String password, Long confKey,
                                       String label, String rtmpURL, String resourceURL, String filePath, String secureToken, String type)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonCDN77ChannelConf;
            {
                JSONObject joCDN77ChannelConf = new JSONObject();

                joCDN77ChannelConf.put("label", label);
                joCDN77ChannelConf.put("rtmpURL", rtmpURL);
                joCDN77ChannelConf.put("resourceURL", resourceURL);
                joCDN77ChannelConf.put("filePath", filePath);
                if (secureToken != null)
                    joCDN77ChannelConf.put("secureToken", secureToken);
                joCDN77ChannelConf.put("type", type);

                jsonCDN77ChannelConf = joCDN77ChannelConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/cdn/cdn77/channel/" + confKey;

            mLogger.info("modifyCDN77ChannelConf"
                    + ", mmsURL: " + mmsURL
                    + ", jsonCDN77ChannelConf: " + jsonCDN77ChannelConf
            );

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonCDN77ChannelConf, outputToBeCompressed);
            mLogger.info("modifyCDN77ChannelConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "modifyCDN77ChannelConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void removeCDN77ChannelConf(String username, String password,
                                     Long confKey)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/cdn/cdn77/channel/" + confKey;

            mLogger.info("removeCDN77ChannelConf"
                    + ", mmsURL: " + mmsURL
                    + ", confKey: " + confKey
            );

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("removeCDN77ChannelConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeCDN77ChannelConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public List<CDN77ChannelConf> getCDN77ChannelConf(String username, String password, String label, String type)
            throws Exception
    {
        List<CDN77ChannelConf> cdn77ChannelConfList = new ArrayList<>();

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/cdn/cdn77/channel"
                + (label == null || label.isEmpty() ? "" : ("?label=" +  java.net.URLEncoder.encode(label, "UTF-8"))) // requires unescape server side
            ;
            if (type != null && !type.isBlank())
            {
                if (mmsURL.indexOf("channel?") != -1)
                    mmsURL += "&";
                else
                    mmsURL += "?";
                mmsURL += ("type=" + type);
            }
            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getCDN77ChannelConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
            JSONArray jaCDN77ChannelConf = joResponse.getJSONArray("cdn77ChannelConf");

            mLogger.info("jaCDN77ChannelConf.length(): " + jaCDN77ChannelConf.length());

            cdn77ChannelConfList.clear();

            for (int confIndex = 0; confIndex < jaCDN77ChannelConf.length(); confIndex++)
            {
                CDN77ChannelConf cdn77ChannelConf = new CDN77ChannelConf();

                JSONObject cdn77ChannelConfInfo = jaCDN77ChannelConf.getJSONObject(confIndex);

                fillCDN77ChannelConf(cdn77ChannelConf, cdn77ChannelConfInfo);

                cdn77ChannelConfList.add(cdn77ChannelConf);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing cdb77ChannelConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return cdn77ChannelConfList;
    }

    public void addRTMPChannelConf(String username, String password,
                                   String label, String rtmpURL, String streamName, String userName, String rtmpPassword,
                                   String playURL, String type)
            throws Exception
    {
        String mmsInfo;
        try
        {
            String jsonRTMPChannelConf;
            {
                JSONObject joRTMPChannelConf = new JSONObject();

                joRTMPChannelConf.put("label", label);
                joRTMPChannelConf.put("rtmpURL", rtmpURL);
                if (streamName != null)
                    joRTMPChannelConf.put("streamName", streamName);
                if (userName != null)
                    joRTMPChannelConf.put("userName", userName);
                if (rtmpPassword != null)
                    joRTMPChannelConf.put("password", rtmpPassword);
                if (playURL != null)
                    joRTMPChannelConf.put("playURL", playURL);
                joRTMPChannelConf.put("type", type);

                jsonRTMPChannelConf = joRTMPChannelConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/cdn/rtmp/channel";

            mLogger.info("addRTMPChannelConf"
                    + ", mmsURL: " + mmsURL
                    + ", jsonRTMPChannelConf: " + jsonRTMPChannelConf
            );

            long start = System.currentTimeMillis();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonRTMPChannelConf, outputToBeCompressed);
            mLogger.info("addRTMPChannelConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addRTMPChannelConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void modifyRTMPChannelConf(String username, String password, Long confKey,
                                      String label, String rtmpURL, String streamName, String userName,
                                      String rtmpPassword, String playURL, String type)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonRTMPChannelConf;
            {
                JSONObject joRTMPChannelConf = new JSONObject();

                joRTMPChannelConf.put("label", label);
                joRTMPChannelConf.put("rtmpURL", rtmpURL);
                if (streamName != null)
                    joRTMPChannelConf.put("streamName", streamName);
                else
                    joRTMPChannelConf.put("streamName", "");
                if (userName != null)
                    joRTMPChannelConf.put("userName", userName);
                else
                    joRTMPChannelConf.put("userName", "");
                if (rtmpPassword != null)
                    joRTMPChannelConf.put("password", rtmpPassword);
                else
                    joRTMPChannelConf.put("password", "");
                if (playURL != null)
                    joRTMPChannelConf.put("playURL", playURL);
                else
                    joRTMPChannelConf.put("playURL", "");
                joRTMPChannelConf.put("type", type);

                jsonRTMPChannelConf = joRTMPChannelConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/cdn/rtmp/channel/" + confKey;

            mLogger.info("modifyRTMPChannelConf"
                    + ", mmsURL: " + mmsURL
                    + ", jsonRTMPChannelConf: " + jsonRTMPChannelConf
            );

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonRTMPChannelConf, outputToBeCompressed);
            mLogger.info("modifyRTMPChannelConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "modifyRTMPChannelConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void removeRTMPChannelConf(String username, String password,
                                      Long confKey)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/cdn/rtmp/channel/" + confKey;

            mLogger.info("removeRTMPChannelConf"
                    + ", mmsURL: " + mmsURL
                    + ", confKey: " + confKey
            );

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("removeRTMPChannelConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeRTMPChannelConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public List<RTMPChannelConf> getRTMPChannelConf(String username, String password, String label, String type)
            throws Exception
    {
        List<RTMPChannelConf> rtmpChannelConfList = new ArrayList<>();

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/cdn/rtmp/channel"
                    + (label == null || label.isEmpty() ? "" : ("?label=" +  java.net.URLEncoder.encode(label, "UTF-8"))) // requires unescape server side
                    ;
            if (type != null && !type.isBlank())
            {
                if (mmsURL.indexOf("channel?") != -1)
                    mmsURL += "&";
                else
                    mmsURL += "?";
                mmsURL += ("type=" + type);
            }
            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getRTMPChannelConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
            JSONArray jaRTMPChannelConf = joResponse.getJSONArray("rtmpChannelConf");

            mLogger.info("jaRTMPChannelConf.length(): " + jaRTMPChannelConf.length());

            rtmpChannelConfList.clear();

            for (int confIndex = 0; confIndex < jaRTMPChannelConf.length(); confIndex++)
            {
                RTMPChannelConf rtmpChannelConf = new RTMPChannelConf();

                JSONObject rtmpChannelConfInfo = jaRTMPChannelConf.getJSONObject(confIndex);

                fillRTMPChannelConf(rtmpChannelConf, rtmpChannelConfInfo);

                rtmpChannelConfList.add(rtmpChannelConf);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing rtmpChannelConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return rtmpChannelConfList;
    }

    public void addHLSChannelConf(String username, String password,
                                   String label, Long deliveryCode, Long segmentDuration, Long playlistEntriesNumber,
                                   String type)
            throws Exception
    {
        String mmsInfo;
        try
        {
            String jsonHLSChannelConf;
            {
                JSONObject joHLSChannelConf = new JSONObject();

                joHLSChannelConf.put("label", label);
                joHLSChannelConf.put("deliveryCode", deliveryCode);
                if (segmentDuration != null)
                    joHLSChannelConf.put("segmentDuration", segmentDuration);
                if (playlistEntriesNumber != null)
                    joHLSChannelConf.put("playlistEntriesNumber", playlistEntriesNumber);
                joHLSChannelConf.put("type", type);

                jsonHLSChannelConf = joHLSChannelConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/cdn/hls/channel";

            mLogger.info("addHLSChannelConf"
                    + ", mmsURL: " + mmsURL
                    + ", jsonHLSChannelConf: " + jsonHLSChannelConf
            );

            long start = System.currentTimeMillis();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonHLSChannelConf, outputToBeCompressed);
            mLogger.info("addHLSChannelConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addHLSChannelConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void modifyHLSChannelConf(String username, String password, Long confKey,
                                      String label, Long deliveryCode, Long segmentDuration, Long playlistEntriesNumber,
                                      String type)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String jsonHLSChannelConf;
            {
                JSONObject joHLSChannelConf = new JSONObject();

                joHLSChannelConf.put("label", label);
                joHLSChannelConf.put("deliveryCode", deliveryCode);
                if (segmentDuration != null)
                    joHLSChannelConf.put("segmentDuration", segmentDuration);
                else
                    joHLSChannelConf.put("segmentDuration", -1);
                if (playlistEntriesNumber != null)
                    joHLSChannelConf.put("playlistEntriesNumber", playlistEntriesNumber);
                else
                    joHLSChannelConf.put("playlistEntriesNumber", -1);
                joHLSChannelConf.put("type", type);

                jsonHLSChannelConf = joHLSChannelConf.toString(4);
            }

            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/cdn/hls/channel/" + confKey;

            mLogger.info("modifyHLSChannelConf"
                    + ", mmsURL: " + mmsURL
                    + ", jsonHLSChannelConf: " + jsonHLSChannelConf
            );

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonHLSChannelConf, outputToBeCompressed);
            mLogger.info("modifyHLSChannelConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "modifyHLSChannelConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public void removeHLSChannelConf(String username, String password,
                                      Long confKey)
            throws Exception
    {

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/cdn/hls/channel/" + confKey;

            mLogger.info("removeHLSChannelConf"
                    + ", mmsURL: " + mmsURL
                    + ", confKey: " + confKey
            );

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("removeHLSChannelConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "removeHLSChannelConf MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public List<HLSChannelConf> getHLSChannelConf(String username, String password, String label, String type)
            throws Exception
    {
        List<HLSChannelConf> hlsChannelConfList = new ArrayList<>();

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/conf/cdn/hls/channel"
                    + (label == null || label.isEmpty() ? "" : ("?label=" +  java.net.URLEncoder.encode(label, "UTF-8"))) // requires unescape server side
                    ;
            if (type != null && !type.isBlank())
            {
                if (mmsURL.indexOf("channel?") != -1)
                    mmsURL += "&";
                else
                    mmsURL += "?";
                mmsURL += ("type=" + type);
            }
            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getHLSChannelConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
            JSONArray jaHLSChannelConf = joResponse.getJSONArray("hlsChannelConf");

            mLogger.info("jaHLSChannelConf.length(): " + jaHLSChannelConf.length());

            hlsChannelConfList.clear();

            for (int confIndex = 0; confIndex < jaHLSChannelConf.length(); confIndex++)
            {
                HLSChannelConf hlsChannelConf = new HLSChannelConf();

                JSONObject hlsChannelConfInfo = jaHLSChannelConf.getJSONObject(confIndex);

                fillHLSChannelConf(hlsChannelConf, hlsChannelConfInfo);

                hlsChannelConfList.add(hlsChannelConf);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing hlsChannelConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return hlsChannelConfList;
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

                joFTPConf.put("label", label);
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

            long start = System.currentTimeMillis();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonFTPConf, outputToBeCompressed);
            mLogger.info("addFTPConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

                joFTPConf.put("label", label);
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonFTPConf, outputToBeCompressed);
            mLogger.info("modifyFTPConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("removeFTPConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getFTPConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

                joEMailConf.put("label", label);
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

            long start = System.currentTimeMillis();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonEMailConf, outputToBeCompressed);
            mLogger.info("addEMailConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

                joEMailConf.put("label", label);
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchPutHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonEMailConf, outputToBeCompressed);
            mLogger.info("modifyEMailConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.fetchDeleteHttpsJson(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password);
            mLogger.info("removeEMailConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getEMailConf. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
        String ipAddress,
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

                if (ipAddress != null && !ipAddress.isBlank())
                    joStatistic.put("ipAddress", ipAddress);
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

            long start = System.currentTimeMillis();
            String contentType = null;
            mmsInfo = HttpFeedFetcher.fetchPostHttpsJson(mmsURL, contentType, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, jsonStatistic, outputToBeCompressed);
            mLogger.info("addRequestStatistic. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "addRequestStatistic MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    public Long getLoginStatistics(String username, String password,
                                     Date startStatisticDate, Date endStatisticDate,
                                     long startIndex, long pageSize,
                                     List<LoginStatistic> loginStatisticsList)
            throws Exception
    {
        Long numFound;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/statistic/login"
                    + "?start=" + startIndex
                    + "&rows=" + pageSize
                    + (startStatisticDate != null ? ("&startStatisticDate=" + simpleDateFormat.format(startStatisticDate)) : "")
                    + (endStatisticDate != null ? ("&endStatisticDate=" + simpleDateFormat.format(endStatisticDate)) : "")
                    ;

            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
                    username, password, null, outputToBeCompressed);
            mLogger.info("getLoginStatistics. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "getLoginStatistics MMS failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            loginStatisticsList.clear();

            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            numFound = joResponse.getLong("numFound");
            JSONArray jaLoginStatistics = joResponse.getJSONArray("loginStatistics");

            for (int loginStatisticIndex = 0; loginStatisticIndex < jaLoginStatistics.length(); loginStatisticIndex++)
            {
                JSONObject loginStatisticInfo = jaLoginStatistics.getJSONObject(loginStatisticIndex);

                LoginStatistic loginStatistic = new LoginStatistic();

                fillLoginStatistic(loginStatistic, loginStatisticInfo);

                loginStatisticsList.add(loginStatistic);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "getLoginStatistics failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return numFound;
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

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, timeoutInSeconds, maxRetriesNumber,
				username, password, null, outputToBeCompressed);
            mLogger.info("getRequestStatistics. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
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
		String title, String userId, Long minimalNextRequestDistanceInSeconds,
		Date startStatisticDate, Date endStatisticDate,
		Long totalNumFoundToBeCalculated,
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
				+ (title != null && !title.isEmpty() ? ("&title=" + java.net.URLEncoder.encode(title, "UTF-8")) : "")
				+ (userId != null && !userId.isEmpty() ? ("&userId=" + java.net.URLEncoder.encode(userId, "UTF-8")) : "")
				+ (minimalNextRequestDistanceInSeconds != null ? ("&minimalNextRequestDistanceInSeconds=" + minimalNextRequestDistanceInSeconds) : "")
				+ (totalNumFoundToBeCalculated != null ? ("&totalNumFoundToBeCalculated=" + totalNumFoundToBeCalculated) : "")
				+ (startStatisticDate != null ? ("&startStatisticDate=" + simpleDateFormat.format(startStatisticDate)) : "")
				+ (endStatisticDate != null ? ("&endStatisticDate=" + simpleDateFormat.format(endStatisticDate)) : "")
			;

            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, statisticsTimeoutInSeconds, maxRetriesNumber,
				username, password, null, outputToBeCompressed);
            mLogger.info("getRequestPerContentStatistics. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "getRequestPerContentStatistics MMS failed"
                    + ", statisticsTimeoutInSeconds: " + statisticsTimeoutInSeconds
                    + ", exception: " + e
                    ;
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

    public Long getRequestPerUserStatistics(String username, String password,
		String title, String userId, Long minimalNextRequestDistanceInSeconds,
		Date startStatisticDate, Date endStatisticDate,
		Long totalNumFoundToBeCalculated,
		long startIndex, long pageSize,
		List<RequestPerUserStatistic> requestPerUserStatisticsList)
		throws Exception
    {
		Long numFound;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String mmsInfo;
        try
        {
            String mmsURL = mmsAPIProtocol + "://" + mmsAPIHostName + ":" + mmsAPIPort + "/catramms/1.0.1/statistic/request/perUser"
				+ "?start=" + startIndex
				+ "&rows=" + pageSize
				+ (title != null && !title.isEmpty() ? ("&title=" + java.net.URLEncoder.encode(title, "UTF-8")) : "")
				+ (userId != null && !userId.isEmpty() ? ("&userId=" + java.net.URLEncoder.encode(userId, "UTF-8")) : "")
				+ (minimalNextRequestDistanceInSeconds != null ? ("&minimalNextRequestDistanceInSeconds=" + minimalNextRequestDistanceInSeconds) : "")
				+ (totalNumFoundToBeCalculated != null ? ("&totalNumFoundToBeCalculated=" + totalNumFoundToBeCalculated) : "")
				+ (startStatisticDate != null ? ("&startStatisticDate=" + simpleDateFormat.format(startStatisticDate)) : "")
				+ (endStatisticDate != null ? ("&endStatisticDate=" + simpleDateFormat.format(endStatisticDate)) : "")
			;

            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, statisticsTimeoutInSeconds, maxRetriesNumber,
				username, password, null, outputToBeCompressed);
            mLogger.info("getRequestPerUserStatistics. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "getRequestPerUserStatistics MMS failed"
                    + ", statisticsTimeoutInSeconds: " + statisticsTimeoutInSeconds
                    + ", exception: " + e
                    ;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
            requestPerUserStatisticsList.clear();

            JSONObject joMMSInfo = new JSONObject(mmsInfo);
            JSONObject joResponse = joMMSInfo.getJSONObject("response");
            numFound = joResponse.getLong("numFound");
            JSONArray jaRequestStatistics = joResponse.getJSONArray("requestStatistics");

            for (int requestStatisticIndex = 0; requestStatisticIndex < jaRequestStatistics.length(); requestStatisticIndex++)
            {
                JSONObject requestPerUserStatisticInfo = jaRequestStatistics.getJSONObject(requestStatisticIndex);

                RequestPerUserStatistic requestPerUserStatistic = new RequestPerUserStatistic();

                fillRequestPerUserStatistic(requestPerUserStatistic, requestPerUserStatisticInfo);

                requestPerUserStatisticsList.add(requestPerUserStatistic);
            }
        }
        catch (Exception e)
        {
            String errorMessage = "getRequestPerUserStatistics failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        return numFound;
    }

    public Long getRequestPerMonthStatistics(String username, String password,
		String title, String userId, Long minimalNextRequestDistanceInSeconds,
		Date startStatisticDate, Date endStatisticDate,
		Long totalNumFoundToBeCalculated,
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
				+ (title != null && !title.isEmpty() ? ("&title=" + java.net.URLEncoder.encode(title, "UTF-8")) : "")
				+ (userId != null && !userId.isEmpty() ? ("&userId=" + java.net.URLEncoder.encode(userId, "UTF-8")) : "")
				+ (minimalNextRequestDistanceInSeconds != null ? ("&minimalNextRequestDistanceInSeconds=" + minimalNextRequestDistanceInSeconds) : "")
				+ (totalNumFoundToBeCalculated != null ? ("&totalNumFoundToBeCalculated=" + totalNumFoundToBeCalculated) : "")
				+ (startStatisticDate != null ? ("&startStatisticDate=" + simpleDateFormat.format(startStatisticDate)) : "")
				+ (endStatisticDate != null ? ("&endStatisticDate=" + simpleDateFormat.format(endStatisticDate)) : "")
			;

            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, statisticsTimeoutInSeconds, maxRetriesNumber,
				username, password, null, outputToBeCompressed);
            mLogger.info("getRequestPerMonthStatistics. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "getRequestPerMonthStatistics MMS failed"
                    + ", statisticsTimeoutInSeconds: " + statisticsTimeoutInSeconds
                    + ", exception: " + e
                    ;
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
		String title, String userId, Long minimalNextRequestDistanceInSeconds,
		Date startStatisticDate, Date endStatisticDate,
		Long totalNumFoundToBeCalculated,
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
				+ (title != null && !title.isEmpty() ? ("&title=" + java.net.URLEncoder.encode(title, "UTF-8")) : "")
				+ (userId != null && !userId.isEmpty() ? ("&userId=" + java.net.URLEncoder.encode(userId, "UTF-8")) : "")
				+ (minimalNextRequestDistanceInSeconds != null ? ("&minimalNextRequestDistanceInSeconds=" + minimalNextRequestDistanceInSeconds) : "")
				+ (totalNumFoundToBeCalculated != null ? ("&totalNumFoundToBeCalculated=" + totalNumFoundToBeCalculated) : "")
				+ (startStatisticDate != null ? ("&startStatisticDate=" + simpleDateFormat.format(startStatisticDate)) : "")
				+ (endStatisticDate != null ? ("&endStatisticDate=" + simpleDateFormat.format(endStatisticDate)) : "")
			;

            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, statisticsTimeoutInSeconds, maxRetriesNumber,
				username, password, null, outputToBeCompressed);
            mLogger.info("getRequestPerDayStatistics. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "getRequestPerDayStatistics MMS failed"
                    + ", statisticsTimeoutInSeconds: " + statisticsTimeoutInSeconds
                    + ", exception: " + e
                    ;
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
		String title, String userId, Long minimalNextRequestDistanceInSeconds,
		Date startStatisticDate, Date endStatisticDate,
		Long totalNumFoundToBeCalculated,
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
				+ (title != null && !title.isEmpty() ? ("&title=" + java.net.URLEncoder.encode(title, "UTF-8")) : "")
				+ (userId != null && !userId.isEmpty() ? ("&userId=" + java.net.URLEncoder.encode(userId, "UTF-8")) : "")
				+ (minimalNextRequestDistanceInSeconds != null ? ("&minimalNextRequestDistanceInSeconds=" + minimalNextRequestDistanceInSeconds) : "")
				+ (totalNumFoundToBeCalculated != null ? ("&totalNumFoundToBeCalculated=" + totalNumFoundToBeCalculated) : "")
				+ (startStatisticDate != null ? ("&startStatisticDate=" + simpleDateFormat.format(startStatisticDate)) : "")
				+ (endStatisticDate != null ? ("&endStatisticDate=" + simpleDateFormat.format(endStatisticDate)) : "")
			;

            mLogger.info("mmsURL: " + mmsURL);

            long start = System.currentTimeMillis();
            mmsInfo = HttpFeedFetcher.GET(mmsURL, statisticsTimeoutInSeconds, maxRetriesNumber,
				username, password, null, outputToBeCompressed);
            mLogger.info("getRequestPerHourStatistics. Elapsed (@" + mmsURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "getRequestPerHourStatistics MMS failed"
                    + ", statisticsTimeoutInSeconds: " + statisticsTimeoutInSeconds
                    + ", exception: " + e
                    ;
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
            if (joUserProfileInfo.has("timezone") && !joUserProfileInfo.isNull("timezone"))
                userProfile.setTimezone(joUserProfileInfo.getString("timezone"));
            userProfile.setEmail(joUserProfileInfo.getString("email"));
            userProfile.setCreationDate(simpleDateFormat.parse(joUserProfileInfo.getString("creationDate")));
            userProfile.setInsolvent(joUserProfileInfo.getBoolean("insolvent"));
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

    private void fillInvoice(Invoice invoice, JSONObject joInvoiceInfo)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try
        {
            invoice.setInvoiceKey(joInvoiceInfo.getLong("invoiceKey"));
            invoice.setUserKey(joInvoiceInfo.getLong("userKey"));
            invoice.setCreationDate(simpleDateFormat.parse(joInvoiceInfo.getString("creationDate")));
            invoice.setDescription(joInvoiceInfo.getString("description"));
            invoice.setAmount(joInvoiceInfo.getLong("amount"));
            invoice.setExpirationDate(simpleDateFormat.parse(joInvoiceInfo.getString("expirationDate")));
            invoice.setPaid(joInvoiceInfo.getBoolean("paid"));
            if (joInvoiceInfo.has("paymentDate") && !joInvoiceInfo.isNull("paymentDate"))
                invoice.setPaymentDate(simpleDateFormat.parse(joInvoiceInfo.getString("paymentDate")));
        }
        catch (Exception e)
        {
            String errorMessage = "fillInvoice failed"
                    + ", Exception: " + e
                    + ", joInvoiceInfo: " + joInvoiceInfo.toString()
                    ;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillWorkspaceDetails(WorkspaceDetails workspaceDetails, JSONObject jaWorkspaceInfo)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try
        {
            workspaceDetails.setWorkspaceKey(jaWorkspaceInfo.getLong("workspaceKey"));
            if (jaWorkspaceInfo.has("isEnabled")) // da eliminare dopo upgrade a Postgres
                workspaceDetails.setEnabled(jaWorkspaceInfo.getBoolean("isEnabled")); // da eliminare dopo upgrade a Postgres
            else if (jaWorkspaceInfo.has("enabled")) // da eliminare dopo upgrade a Postgres
                workspaceDetails.setEnabled(jaWorkspaceInfo.getBoolean("enabled"));

            workspaceDetails.setName(jaWorkspaceInfo.getString("workspaceName"));
            workspaceDetails.setMaxEncodingPriority(jaWorkspaceInfo.getString("maxEncodingPriority"));
            workspaceDetails.setEncodingPeriod(jaWorkspaceInfo.getString("encodingPeriod"));
            workspaceDetails.setMaxIngestionsNumber(jaWorkspaceInfo.getLong("maxIngestionsNumber"));
            workspaceDetails.setUsageInMB(jaWorkspaceInfo.getLong("workSpaceUsageInMB"));
            workspaceDetails.setLanguageCode(jaWorkspaceInfo.getString("languageCode"));
            workspaceDetails.setCreationDate(simpleDateFormat.parse(jaWorkspaceInfo.getString("creationDate")));
			if (jaWorkspaceInfo.has("workspaceOwnerUserKey"))
	            workspaceDetails.setWorkspaceOwnerUserKey(jaWorkspaceInfo.getLong("workspaceOwnerUserKey"));
			if (jaWorkspaceInfo.has("workspaceOwnerUserName"))
	            workspaceDetails.setWorkspaceOwnerUserName(jaWorkspaceInfo.getString("workspaceOwnerUserName"));

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

            if(jaWorkspaceInfo.has("cost"))
            {
                JSONObject joCostInfo = jaWorkspaceInfo.getJSONObject("cost");

                workspaceDetails.setMaxStorageInGB(joCostInfo.getLong("maxStorageInGB"));
                workspaceDetails.setCurrentCostForStorage(joCostInfo.getLong("currentCostForStorage"));
                workspaceDetails.setDedicatedEncoder_power_1(joCostInfo.getLong("dedicatedEncoder_power_1"));
                workspaceDetails.setCurrentCostForDedicatedEncoder_power_1(joCostInfo.getLong("currentCostForDedicatedEncoder_power_1"));
                workspaceDetails.setDedicatedEncoder_power_2(joCostInfo.getLong("dedicatedEncoder_power_2"));
                workspaceDetails.setCurrentCostForDedicatedEncoder_power_2(joCostInfo.getLong("currentCostForDedicatedEncoder_power_2"));
                workspaceDetails.setDedicatedEncoder_power_3(joCostInfo.getLong("dedicatedEncoder_power_3"));
                workspaceDetails.setCurrentCostForDedicatedEncoder_power_3(joCostInfo.getLong("currentCostForDedicatedEncoder_power_3"));
                workspaceDetails.setCDN_type_1(joCostInfo.getLong("CDN_type_1"));
                workspaceDetails.setCurrentCostForCDN_type_1(joCostInfo.getLong("currentCostForCDN_type_1"));
                workspaceDetails.setSupport_type_1(joCostInfo.getBoolean("support_type_1"));
                workspaceDetails.setCurrentCostForSupport_type_1(joCostInfo.getLong("currentCostForSupport_type_1"));
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
                encodingJob.setProgress(encodingJobInfo.getDouble("progress"));

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
						long now = System.currentTimeMillis();

						if (encodingJob.getEnd() == null
								&& encodingJob.getStart() != null && encodingJob.getStart().getTime() < now
								&& encodingJob.getProgress() != null && encodingJob.getProgress() != 0.0 && encodingJob.getProgress() != -1.0)
						{
							Long elapsedInMillisecs = now - encodingJob.getStart().getTime();

							// elapsedInMillisecs : actual percentage = X (estimateMillisecs) : 100
							Double estimateMillisecs = elapsedInMillisecs * 100 / encodingJob.getProgress();

							Calendar calendar = Calendar.getInstance();
							calendar.setTime(encodingJob.getStart());
							calendar.add(Calendar.MILLISECOND, estimateMillisecs.intValue());

							encodingJob.setEndEstimate(true);
							encodingJob.setEnd(calendar.getTime());
						}
					}

                    encodingJob.setEncodingProfileKey(joParameters.getLong("encodingProfileKey"));
                    if (joParameters.has("sourcesToBeEncoded") 
						&& joParameters.getJSONArray("sourcesToBeEncoded").length() > 0)
                        encodingJob.setSourcePhysicalPathKey(
                            joParameters.getJSONArray("sourcesToBeEncoded").getJSONObject(0)
                                    .getLong("sourcePhysicalPathKey"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("OverlayImageOnVideo"))
                {
					// end processing estimation
					{
						long now = System.currentTimeMillis();

						if (encodingJob.getEnd() == null
								&& encodingJob.getStart() != null && encodingJob.getStart().getTime() < now
								&& encodingJob.getProgress() != null && encodingJob.getProgress() != 0.0 && encodingJob.getProgress() != -1.0)
						{
							Long elapsedInMillisecs = now - encodingJob.getStart().getTime();

							// elapsedInMillisecs : actual percentage = X (estimateMillisecs) : 100
							Double estimateMillisecs = elapsedInMillisecs * 100 / encodingJob.getProgress();

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
						long now = System.currentTimeMillis();

						if (encodingJob.getEnd() == null
								&& encodingJob.getStart() != null && encodingJob.getStart().getTime() < now
								&& encodingJob.getProgress() != null && encodingJob.getProgress() != 0.0 && encodingJob.getProgress() != -1.0)
						{
							Long elapsedInMillisecs = now - encodingJob.getStart().getTime();

							// elapsedInMillisecs : actual percentage = X (estimateMillisecs) : 100
							Double estimateMillisecs = elapsedInMillisecs * 100 / encodingJob.getProgress();

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
						long now = System.currentTimeMillis();

						if (encodingJob.getEnd() == null
								&& encodingJob.getStart() != null && encodingJob.getStart().getTime() < now
								&& encodingJob.getProgress() != null && encodingJob.getProgress() != 0.0 && encodingJob.getProgress() != -1.0)
						{
							Long elapsedInMillisecs = now - encodingJob.getStart().getTime();

							// elapsedInMillisecs : actual percentage = X (estimateMillisecs) : 100
							Double estimateMillisecs = elapsedInMillisecs * 100 / encodingJob.getProgress();

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
						long now = System.currentTimeMillis();

						if (encodingJob.getEnd() == null
								&& encodingJob.getStart() != null && encodingJob.getStart().getTime() < now
								&& encodingJob.getProgress() != null && encodingJob.getProgress() != 0.0 && encodingJob.getProgress() != -1.0)
						{
							Long elapsedInMillisecs = now - encodingJob.getStart().getTime();

							// elapsedInMillisecs : actual percentage = X (estimateMillisecs) : 100
							Double estimateMillisecs = elapsedInMillisecs * 100 / encodingJob.getProgress();

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
					encodingJob.setInputChannels(joParameters.getJSONArray("inputChannels").toString());
                    encodingJob.setEncodingProfileKey(joParameters.getLong("encodingProfileKey"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("VideoSpeed")
                )
                {
					// end processing estimation
					{
						long now = System.currentTimeMillis();

						if (encodingJob.getEnd() == null
								&& encodingJob.getStart() != null && encodingJob.getStart().getTime() < now
								&& encodingJob.getProgress() != null && encodingJob.getProgress() != 0.0 && encodingJob.getProgress() != -1.0)
						{
							Long elapsedInMillisecs = now - encodingJob.getStart().getTime();

							// elapsedInMillisecs : actual percentage = X (estimateMillisecs) : 100
							Double estimateMillisecs = elapsedInMillisecs * 100 / encodingJob.getProgress();

							Calendar calendar = Calendar.getInstance();
							calendar.setTime(encodingJob.getStart());
							calendar.add(Calendar.MILLISECOND, estimateMillisecs.intValue());

							encodingJob.setEndEstimate(true);
							encodingJob.setEnd(calendar.getTime());
						}
					}

					encodingJob.setSourcePhysicalPathKey(joParameters.getLong("sourcePhysicalPathKey"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("PictureInPicture"))
                {
					// end processing estimation
					{
						long now = System.currentTimeMillis();

						if (encodingJob.getEnd() == null
								&& encodingJob.getStart() != null && encodingJob.getStart().getTime() < now
								&& encodingJob.getProgress() != null && encodingJob.getProgress() != 0.0 && encodingJob.getProgress() != -1.0)
						{
							Long elapsedInMillisecs = now - encodingJob.getStart().getTime();

							// elapsedInMillisecs : actual percentage = X (estimateMillisecs) : 100
							Double estimateMillisecs = elapsedInMillisecs * 100 / encodingJob.getProgress();

							Calendar calendar = Calendar.getInstance();
							calendar.setTime(encodingJob.getStart());
							calendar.add(Calendar.MILLISECOND, estimateMillisecs.intValue());

							encodingJob.setEndEstimate(true);
							encodingJob.setEnd(calendar.getTime());
						}
					}

					encodingJob.setMainSourcePhysicalPathKey(joParameters.getLong("mainSourcePhysicalPathKey"));
                    encodingJob.setOverlaySourcePhysicalPathKey(joParameters.getLong("overlaySourcePhysicalPathKey"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("IntroOutroOverlay"))
                {
					// end processing estimation
					{
						long now = System.currentTimeMillis();

						if (encodingJob.getEnd() == null
								&& encodingJob.getStart() != null && encodingJob.getStart().getTime() < now
								&& encodingJob.getProgress() != null && encodingJob.getProgress() != 0.0 && encodingJob.getProgress() != -1.0)
						{
							Long elapsedInMillisecs = now - encodingJob.getStart().getTime();

							// elapsedInMillisecs : actual percentage = X (estimateMillisecs) : 100
							Double estimateMillisecs = elapsedInMillisecs * 100 / encodingJob.getProgress();

							Calendar calendar = Calendar.getInstance();
							calendar.setTime(encodingJob.getStart());
							calendar.add(Calendar.MILLISECOND, estimateMillisecs.intValue());

							encodingJob.setEndEstimate(true);
							encodingJob.setEnd(calendar.getTime());
						}
					}

					encodingJob.setIntroSourcePhysicalPathKey(joParameters.getLong("introSourcePhysicalPathKey"));
                    encodingJob.setMainSourcePhysicalPathKey(joParameters.getLong("mainSourcePhysicalPathKey"));
                    encodingJob.setOutroSourcePhysicalPathKey(joParameters.getLong("outroSourcePhysicalPathKey"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("CutFrameAccurate"))
                {
					// end processing estimation
					{
						long now = System.currentTimeMillis();

						if (encodingJob.getEnd() == null
							&& encodingJob.getStart() != null && encodingJob.getStart().getTime() < now
							&& encodingJob.getProgress() != null && encodingJob.getProgress() != 0.0 && encodingJob.getProgress() != -1.0)
						{
							Long elapsedInMillisecs = now - encodingJob.getStart().getTime();

							// elapsedInMillisecs : actual percentage = X (estimateMillisecs) : 100
							Double estimateMillisecs = elapsedInMillisecs * 100 / encodingJob.getProgress();

							Calendar calendar = Calendar.getInstance();
							calendar.setTime(encodingJob.getStart());
							calendar.add(Calendar.MILLISECOND, estimateMillisecs.intValue());

							encodingJob.setEndEstimate(true);
							encodingJob.setEnd(calendar.getTime());
						}
					}

					encodingJob.setEncodingProfileKey(joParameters.getLong("encodingProfileKey"));
                    encodingJob.setSourcePhysicalPathKey(joParameters.getLong("sourcePhysicalPathKey"));
                }
                else if (encodingJob.getType().equalsIgnoreCase("LiveRecorder")
                )
                {
					if (joParameters.has("liveURL"))    // previous one
                        encodingJob.setLiveURL(joParameters.getString("liveURL"));
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
                + ", encodingJob.getType: " + (encodingJob == null ? "null" : encodingJob.getType())
                + ", joParameters: " + (joParameters == null ? "null" : joParameters.toString());
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillLoginStatistic(LoginStatistic loginStatistic, JSONObject loginStatisticInfo)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try
        {
            loginStatistic.setLoginStatisticKey(loginStatisticInfo.getLong("loginStatisticKey"));

            if (loginStatisticInfo.has("userName") && !loginStatisticInfo.isNull("userName"))
                loginStatistic.setUserName(loginStatisticInfo.getString("userName"));
            if (loginStatisticInfo.has("emailAddress") && !loginStatisticInfo.isNull("emailAddress"))
                loginStatistic.setEmailAddress(loginStatisticInfo.getString("emailAddress"));
            if (loginStatisticInfo.has("userKey") && !loginStatisticInfo.isNull("userKey"))
                loginStatistic.setUserKey(loginStatisticInfo.getLong("userKey"));
            if (loginStatisticInfo.has("ip") && !loginStatisticInfo.isNull("ip"))
                loginStatistic.setIp(loginStatisticInfo.getString("ip"));
            if (loginStatisticInfo.has("continent") && !loginStatisticInfo.isNull("continent"))
                loginStatistic.setContinent(loginStatisticInfo.getString("continent"));
            if (loginStatisticInfo.has("continentCode") && !loginStatisticInfo.isNull("continentCode"))
                loginStatistic.setContinentCode(loginStatisticInfo.getString("continentCode"));
            if (loginStatisticInfo.has("country") && !loginStatisticInfo.isNull("country"))
                loginStatistic.setCountry(loginStatisticInfo.getString("country"));
            if (loginStatisticInfo.has("countryCode") && !loginStatisticInfo.isNull("countryCode"))
                loginStatistic.setCountryCode(loginStatisticInfo.getString("countryCode"));
            if (loginStatisticInfo.has("region") && !loginStatisticInfo.isNull("region"))
                loginStatistic.setRegion(loginStatisticInfo.getString("region"));
            if (loginStatisticInfo.has("city") && !loginStatisticInfo.isNull("city"))
                loginStatistic.setCity(loginStatisticInfo.getString("city"));
            if (loginStatisticInfo.has("org") && !loginStatisticInfo.isNull("org"))
                loginStatistic.setOrg(loginStatisticInfo.getString("org"));
            if (loginStatisticInfo.has("isp") && !loginStatisticInfo.isNull("isp"))
                loginStatistic.setIsp(loginStatisticInfo.getString("isp"));
            if (loginStatisticInfo.has("timezoneGMTOffset") && !loginStatisticInfo.isNull("timezoneGMTOffset"))
                loginStatistic.setTimezoneGMTOffset(loginStatisticInfo.getLong("timezoneGMTOffset"));
            if (loginStatisticInfo.has("successfulLogin") && !loginStatisticInfo.isNull("successfulLogin"))
                loginStatistic.setSuccessfulLogin(simpleDateFormat.parse(loginStatisticInfo.getString("successfulLogin")));
        }
        catch (Exception e)
        {
            String errorMessage = "fillLoginStatistic failed. Exception: " + e
                    + ", loginStatisticInfo: " + loginStatisticInfo.toString()
                    ;
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
            if (requestStatisticInfo.has("ipAddress") && !requestStatisticInfo.isNull("ipAddress"))
                requestStatistic.setIpAddress(requestStatisticInfo.getString("ipAddress"));
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

    private void fillRequestPerUserStatistic(RequestPerUserStatistic requestPerUserStatistic, 
		JSONObject requestPerUserStatisticInfo)
		throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try
        {
			requestPerUserStatistic.setUserId(requestPerUserStatisticInfo.getString("userId"));
            requestPerUserStatistic.setCount(requestPerUserStatisticInfo.getLong("count"));
		}
        catch (Exception e)
        {
            String errorMessage = "fillRequestPerUserStatistic failed. Exception: " + e
				+ ", requestPerUserStatisticInfo: " + requestPerUserStatisticInfo.toString()
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
			if (encoderInfo.has("workspacesAssociated"))
				encoder.setWorkspacesAssociated(encoderInfo.getJSONArray("workspacesAssociated"));
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
			if (mediaItemInfo.has("retentionInMinutes") && !mediaItemInfo.isNull("retentionInMinutes"))
				mediaItem.setRetentionInMinutes(mediaItemInfo.getLong("retentionInMinutes"));
            if (mediaItemInfo.has("willBeRemovedAt") && !mediaItemInfo.isNull("willBeRemovedAt"))
                mediaItem.setWillBeRemovedAt(simpleDateFormat.parse(mediaItemInfo.getString("willBeRemovedAt")));

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

                    if (physicalPathInfo.has("metaData") && !physicalPathInfo.isNull("metaData"))
                        physicalPath.setMetaData(physicalPathInfo.getString("metaData"));

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
                        physicalPath.setEncodingProfileLabel(null);
                        mediaItem.setSourcePhysicalPath(physicalPath);
                    }
                    else
					{
                        physicalPath.setEncodingProfileKey(physicalPathInfo.getLong("encodingProfileKey"));
						if (physicalPathInfo.has("encodingProfileLabel") && !physicalPathInfo.isNull("encodingProfileLabel"))
	                        physicalPath.setEncodingProfileLabel(physicalPathInfo.getString("encodingProfileLabel"));
						else
	                        physicalPath.setEncodingProfileLabel(null);
					}
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

                if (joMetadataContent.has("ingester") && !joMetadataContent.isNull("ingester"))
                    ingestionJob.setIngester(joMetadataContent.getString("ingester"));

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

                    if (joMetadataContent.has("liveRecorderVirtualVOD"))
                        ingestionJob.setRecordingVirtualVOD(true);
                    else
                        ingestionJob.setRecordingVirtualVOD(false);

                    if (joMetadataContent.has("monitorHLS"))
                        ingestionJob.setRecordingMonitorHLS(true);
                    else
                        ingestionJob.setRecordingMonitorHLS(false);

					if (joMetadataContent.has("configurationLabel") && !joMetadataContent.isNull("configurationLabel"))
						ingestionJob.setChannelLabel(joMetadataContent.getString("configurationLabel"));
                    else if (joMetadataContent.has("ConfigurationLabel") && !joMetadataContent.isNull("ConfigurationLabel"))
                        ingestionJob.setChannelLabel(joMetadataContent.getString("ConfigurationLabel"));
                }
                else if (ingestionJob.getIngestionType().equalsIgnoreCase("Live-Proxy")
                    && joMetadataContent != null)
                {
					if (joMetadataContent.has("configurationLabel") && !joMetadataContent.isNull("configurationLabel"))
						ingestionJob.setChannelLabel(joMetadataContent.getString("configurationLabel"));
                    else if (joMetadataContent.has("ConfigurationLabel") && !joMetadataContent.isNull("ConfigurationLabel"))
                        ingestionJob.setChannelLabel(joMetadataContent.getString("ConfigurationLabel"));

					if ((joMetadataContent.has("timePeriod") && !joMetadataContent.isNull("timePeriod")
                        && joMetadataContent.getBoolean("timePeriod") && joMetadataContent.has("schedule"))
                            ||
                        (joMetadataContent.has("TimePeriod") && !joMetadataContent.isNull("TimePeriod")
                        && joMetadataContent.getBoolean("TimePeriod") && joMetadataContent.has("schedule"))
                    )
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
					if ((joMetadataContent.has("timePeriod") && !joMetadataContent.isNull("timePeriod")
                        && joMetadataContent.getBoolean("timePeriod") && joMetadataContent.has("schedule"))
                            ||
                            (joMetadataContent.has("TimePeriod") && !joMetadataContent.isNull("TimePeriod")
                                    && joMetadataContent.getBoolean("TimePeriod") && joMetadataContent.has("schedule"))
                    )
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
                ingestionJob.setDownloadingProgress(ingestionJobInfo.getDouble("downloadingProgress"));
            if (ingestionJobInfo.isNull("uploadingProgress"))
                ingestionJob.setUploadingProgress(null);
            else
                ingestionJob.setUploadingProgress(ingestionJobInfo.getDouble("uploadingProgress"));

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
                if (joMediaItem.has("position") && !joMediaItem.isNull("position"))
                    ingestionJobMediaItem.setPosition(joMediaItem.getLong("position"));

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

                    long now = System.currentTimeMillis();

                    if (ingestionJob.getEndProcessing() == null
                            && ingestionJob.getStartProcessing() != null && ingestionJob.getStartProcessing().getTime() < now)
					{
						if (ingestionJob.getIngestionType().equalsIgnoreCase("Live-Recorder"))
						{
                            if (ingestionJob.getRecordingPeriodEnd() != null)
							{
								ingestionJob.setEndProcessingEstimate(true);
								ingestionJob.setEndProcessing(ingestionJob.getRecordingPeriodEnd());
							}
						}
		                else if (ingestionJob.getIngestionType().equalsIgnoreCase("Live-Proxy")
							|| ingestionJob.getIngestionType().equalsIgnoreCase("VOD-Proxy")
							|| ingestionJob.getIngestionType().equalsIgnoreCase("Countdown")
						)
						{
                            if (ingestionJob.getProxyPeriodEnd() != null)
							{
								ingestionJob.setEndProcessingEstimate(true);
								ingestionJob.setEndProcessing(ingestionJob.getProxyPeriodEnd());
							}
						}
						else if (encodingJob.getProgress() != null && encodingJob.getProgress() != 0.0 && encodingJob.getProgress() != -1.0)
						{
							Long elapsedInMillisecs = now - ingestionJob.getStartProcessing().getTime();
	
							// elapsedInMillisecs : actual percentage = X (estimateMillisecs) : 100
							Double estimateMillisecs = elapsedInMillisecs * 100 / encodingJob.getProgress();
	
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(ingestionJob.getStartProcessing());
							calendar.add(Calendar.MILLISECOND, estimateMillisecs.intValue());
	
							ingestionJob.setEndProcessingEstimate(true);
							ingestionJob.setEndProcessing(calendar.getTime());
						}
                        else if (ingestionJob.getDownloadingProgress() != null && ingestionJob.getDownloadingProgress() != 0 && ingestionJob.getDownloadingProgress() != -1)
                        {
                            Long elapsedInMillisecs = now - ingestionJob.getStartProcessing().getTime();

                            // elapsedInMillisecs : actual percentage = X (estimateMillisecs) : 100
                            Double estimateMillisecs = elapsedInMillisecs * 100 / ingestionJob.getDownloadingProgress();

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(ingestionJob.getStartProcessing());
                            calendar.add(Calendar.MILLISECOND, estimateMillisecs.intValue());

                            ingestionJob.setEndProcessingEstimate(true);
                            ingestionJob.setEndProcessing(calendar.getTime());
                        }
					}
                }

                ingestionJob.setEncodingJob(encodingJob);
            }
            else
            {
                // end processing estimation
                {
                    ingestionJob.setEndProcessingEstimate(false);

                    long now = System.currentTimeMillis();

                    if (ingestionJob.getEndProcessing() == null
                            && ingestionJob.getStartProcessing() != null && ingestionJob.getStartProcessing().getTime() < now)
                    {
                        if (ingestionJob.getDownloadingProgress() != null && ingestionJob.getDownloadingProgress() != 0
                                && ingestionJob.getDownloadingProgress() != -1)
                        {
                            Long elapsedInMillisecs = now - ingestionJob.getStartProcessing().getTime();

                            // elapsedInMillisecs : actual percentage = X (estimateMillisecs) : 100
                            Double estimateMillisecs = elapsedInMillisecs * 100 / ingestionJob.getDownloadingProgress();

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(ingestionJob.getStartProcessing());
                            calendar.add(Calendar.MILLISECOND, estimateMillisecs.intValue());

                            ingestionJob.setEndProcessingEstimate(true);
                            ingestionJob.setEndProcessing(calendar.getTime());
                        }
                        else if (ingestionJob.getUploadingProgress() != null && ingestionJob.getUploadingProgress() != 0
                                && ingestionJob.getUploadingProgress() != -1)
                        {
                            Long elapsedInMillisecs = now - ingestionJob.getStartProcessing().getTime();

                            // elapsedInMillisecs : actual percentage = X (estimateMillisecs) : 100
                            Double estimateMillisecs = elapsedInMillisecs * 100 / ingestionJob.getUploadingProgress();

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(ingestionJob.getStartProcessing());
                            calendar.add(Calendar.MILLISECOND, estimateMillisecs.intValue());

                            ingestionJob.setEndProcessingEstimate(true);
                            ingestionJob.setEndProcessing(calendar.getTime());
                        }
                    }
                }
            }
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
            encodingProfile.setFileFormat(joProfileInfo.getString("fileFormat"));

            if (deep)
            {
                if (encodingProfile.getContentType().equalsIgnoreCase("video"))
                {
                    JSONObject joVideoInfo = joProfileInfo.getJSONObject("video");

                    encodingProfile.getVideoDetails().setCodec(joVideoInfo.getString("codec"));
                    if (joVideoInfo.isNull("profile"))
                        encodingProfile.getVideoDetails().setProfile(null);
                    else
                        encodingProfile.getVideoDetails().setProfile(joVideoInfo.getString("profile"));
                    encodingProfile.getVideoDetails().setTwoPasses(joVideoInfo.getBoolean("twoPasses"));
                    if (joVideoInfo.isNull("otherOutputParameters"))
                        encodingProfile.getVideoDetails().setOtherOutputParameters(null);
                    else
                        encodingProfile.getVideoDetails().setOtherOutputParameters(joVideoInfo.getString("otherOutputParameters"));
                    if (joVideoInfo.isNull("frameRate"))
                        encodingProfile.getVideoDetails().setFrameRate(null);
                    else
                        encodingProfile.getVideoDetails().setFrameRate(joVideoInfo.getLong("frameRate"));
                    if (joVideoInfo.has("keyFrameIntervalInSeconds") && !joVideoInfo.isNull("keyFrameIntervalInSeconds"))
                        encodingProfile.getVideoDetails().setKeyFrameIntervalInSeconds(joVideoInfo.getLong("keyFrameIntervalInSeconds"));
                    else
                        encodingProfile.getVideoDetails().setKeyFrameIntervalInSeconds(null);

                    if (joVideoInfo.has("bitRates"))
                    {
                        JSONArray jaBitRates = joVideoInfo.getJSONArray("bitRates");
                        for (int bitRateIndex = 0; bitRateIndex < jaBitRates.length(); bitRateIndex++)
                        {
                            JSONObject joBitRate = jaBitRates.getJSONObject(bitRateIndex);

                            VideoBitRate videoBitRate = new VideoBitRate();
                            encodingProfile.getVideoDetails().getVideoBitRateList().add(videoBitRate);

                            videoBitRate.setWidth(joBitRate.getLong("width"));
                            videoBitRate.setHeight(joBitRate.getLong("height"));
                            videoBitRate.setkBitRate(joBitRate.getLong("kBitRate"));
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

                    JSONObject joAudioInfo = joProfileInfo.getJSONObject("audio");

                    encodingProfile.getAudioDetails().setCodec(joAudioInfo.getString("codec"));
                    if (joAudioInfo.isNull("otherOutputParameters"))
                        encodingProfile.getAudioDetails().setOtherOutputParameters(null);
                    else
                        encodingProfile.getAudioDetails().setOtherOutputParameters(joAudioInfo.getString("otherOutputParameters"));
                    if (joAudioInfo.isNull("channelsNumber"))
                        encodingProfile.getAudioDetails().setChannelsNumber(null);
                    else
                        encodingProfile.getAudioDetails().setChannelsNumber(joAudioInfo.getLong("channelsNumber"));
                    if (joAudioInfo.isNull("sampleRate"))
                        encodingProfile.getAudioDetails().setSampleRate(null);
                    else
                        encodingProfile.getAudioDetails().setSampleRate(joAudioInfo.getLong("sampleRate"));

                    if (joAudioInfo.has("bitRates"))
                    {
                        JSONArray jaBitRates = joAudioInfo.getJSONArray("bitRates");
                        for (int bitRateIndex = 0; bitRateIndex < jaBitRates.length(); bitRateIndex++)
                        {
                            JSONObject joBitRate = jaBitRates.getJSONObject(bitRateIndex);

                            AudioBitRate audioBitRate = new AudioBitRate();
                            encodingProfile.getVideoDetails().getAudioBitRateList().add(audioBitRate);

                            if (!joBitRate.has("kBitRate") || joBitRate.isNull("kBitRate"))
                                audioBitRate.setkBitRate(null);
                            else
                                audioBitRate.setkBitRate(joBitRate.getLong("kBitRate"));
                        }
                    }
                }
                else if (encodingProfile.getContentType().equalsIgnoreCase("audio"))
                {
                    JSONObject joAudioInfo = joProfileInfo.getJSONObject("audio");

                    encodingProfile.getAudioDetails().setCodec(joAudioInfo.getString("codec"));
                    if (joAudioInfo.isNull("otherOutputParameters"))
                        encodingProfile.getAudioDetails().setOtherOutputParameters(null);
                    else
                        encodingProfile.getAudioDetails().setOtherOutputParameters(joAudioInfo.getString("otherOutputParameters"));
                    if (joAudioInfo.isNull("channelsNumber"))
                        encodingProfile.getAudioDetails().setChannelsNumber(null);
                    else
                        encodingProfile.getAudioDetails().setChannelsNumber(joAudioInfo.getLong("channelsNumber"));
                    if (joAudioInfo.isNull("sampleRate"))
                        encodingProfile.getAudioDetails().setSampleRate(null);
                    else
                        encodingProfile.getAudioDetails().setSampleRate(joAudioInfo.getLong("sampleRate"));

                    if (joAudioInfo.has("bitRates"))
                    {
                        JSONArray jaBitRates = joAudioInfo.getJSONArray("bitRates");
                        for (int bitRateIndex = 0; bitRateIndex < jaBitRates.length(); bitRateIndex++)
                        {
                            JSONObject joBitRate = jaBitRates.getJSONObject(bitRateIndex);

                            AudioBitRate audioBitRate = new AudioBitRate();
                            encodingProfile.getAudioDetails().getAudioBitRateList().add(audioBitRate);

                            if (joBitRate.isNull("kBitRate"))
                                audioBitRate.setkBitRate(null);
                            else
                                audioBitRate.setkBitRate(joBitRate.getLong("kBitRate"));
                        }
                    }
                }
                else if (encodingProfile.getContentType().equalsIgnoreCase("image"))
                {
                    JSONObject joImageInfo = joProfileInfo.getJSONObject("Image");

                    encodingProfile.getImageDetails().setWidth(joImageInfo.getLong("width"));
                    encodingProfile.getImageDetails().setHeight(joImageInfo.getLong("height"));
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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            facebookConf.setConfKey(facebookConfInfo.getLong("confKey"));
            facebookConf.setLabel(facebookConfInfo.getString("label"));
            facebookConf.setUserAccessToken(facebookConfInfo.getString("userAccessToken"));
            facebookConf.setModificationDate(simpleDateFormat.parse(facebookConfInfo.getString("modificationDate")));
        }
        catch (Exception e)
        {
            String errorMessage = "fillFacebookConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillTwitchConf(TwitchConf twitchConf, JSONObject twitchConfInfo)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            twitchConf.setConfKey(twitchConfInfo.getLong("confKey"));
            twitchConf.setLabel(twitchConfInfo.getString("label"));
            twitchConf.setRefreshToken(twitchConfInfo.getString("refreshToken"));
            twitchConf.setModificationDate(simpleDateFormat.parse(twitchConfInfo.getString("modificationDate")));
        }
        catch (Exception e)
        {
            String errorMessage = "fillTwitchConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillCostConf(CostConf costConf, JSONObject costConfInfo)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            costConf.setConfKey(costConfInfo.getLong("confKey"));
            costConf.setType(costConfInfo.getString("type"));
            costConf.setQuantity(costConfInfo.getLong("quantity"));
            costConf.setOrderTimestamp(simpleDateFormat.parse(costConfInfo.getString("orderTimestamp")));
            costConf.setExpiration(simpleDateFormat.parse(costConfInfo.getString("expiration")));
        }
        catch (Exception e)
        {
            String errorMessage = "fillCostConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillTiktokConf(TiktokConf tiktokConf, JSONObject tiktokConfInfo)
            throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            tiktokConf.setConfKey(tiktokConfInfo.getLong("confKey"));
            tiktokConf.setLabel(tiktokConfInfo.getString("label"));
            tiktokConf.setToken(tiktokConfInfo.getString("token"));
            tiktokConf.setModificationDate(simpleDateFormat.parse(tiktokConfInfo.getString("modificationDate")));
        }
        catch (Exception e)
        {
            String errorMessage = "fillTiktokConf failed. Exception: " + e;
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
			if (streamInfo.has("encodersPoolKey") && !streamInfo.isNull("encodersPoolKey"))
            	stream.setEncodersPoolKey(streamInfo.getLong("encodersPoolKey"));
			if (streamInfo.has("encodersPoolLabel") && !streamInfo.isNull("encodersPoolLabel"))
            	stream.setEncodersPoolLabel(streamInfo.getString("encodersPoolLabel"));
            if (streamInfo.has("url") && !streamInfo.isNull("url"))
            	stream.setUrl(streamInfo.getString("url"));
			if (streamInfo.has("pushProtocol") && !streamInfo.isNull("pushProtocol"))
            	stream.setPushProtocol(streamInfo.getString("pushProtocol"));
			if (streamInfo.has("pushEncoderKey") && !streamInfo.isNull("pushEncoderKey"))
                stream.setPushEncoderKey(streamInfo.getLong("pushEncoderKey"));
			if (streamInfo.has("pushEncoderLabel") && !streamInfo.isNull("pushEncoderLabel"))
                stream.setPushEncoderLabel(streamInfo.getString("pushEncoderLabel"));
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
			if (streamInfo.has("tvSourceTVConfKey") && !streamInfo.isNull("tvSourceTVConfKey"))
            	stream.setTvSourceTVConfKey(streamInfo.getLong("tvSourceTVConfKey"));
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

    private void fillSourceTVStream(SourceTVStream stream, JSONObject streamInfo)
            throws Exception
    {
        try {
            stream.setConfKey(streamInfo.getLong("confKey"));
            stream.setType(streamInfo.getString("type"));
            if (streamInfo.has("serviceId") && !streamInfo.isNull("serviceId"))
                stream.setServiceId(streamInfo.getLong("serviceId"));
            if (streamInfo.has("networkId") && !streamInfo.isNull("networkId"))
                stream.setNetworkId(streamInfo.getLong("networkId"));
            if (streamInfo.has("transportStreamId") && !streamInfo.isNull("transportStreamId"))
                stream.setTransportStreamId(streamInfo.getLong("transportStreamId"));
            stream.setName(streamInfo.getString("name"));
            if (streamInfo.has("satellite") && !streamInfo.isNull("satellite"))
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
			if (streamInfo.has("bandwidthInHz") && !streamInfo.isNull("bandwidthInHz"))
                stream.setBandwidthInHz(streamInfo.getLong("bandwidthInHz"));
            if (streamInfo.has("country") && !streamInfo.isNull("country"))
                stream.setCountry(streamInfo.getString("country"));
            if (streamInfo.has("deliverySystem") && !streamInfo.isNull("deliverySystem"))
                stream.setDeliverySystem(streamInfo.getString("deliverySystem"));
        }
        catch (Exception e)
        {
            String errorMessage = "fillSourceTVStream failed. Exception: " + e;
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

                    if (joWorkflowVariable.has("isNull"))
                        workflowVariable.setNullVariable(joWorkflowVariable.getBoolean("isNull"));
                    else
                        workflowVariable.setNullVariable(false);

                    if (joWorkflowVariable.has("description"))
                        workflowVariable.setDescription(joWorkflowVariable.getString("description"));
                    if (joWorkflowVariable.has("type"))
                        workflowVariable.setType(joWorkflowVariable.getString("type"));
                    else
                        workflowVariable.setType("string");
                    if (!workflowVariable.isNullVariable() && joWorkflowVariable.has("value"))
                    {
                        if (workflowVariable.getType().equalsIgnoreCase("string"))
                            workflowVariable.setStringValue(joWorkflowVariable.getString("value"));
                        else if (workflowVariable.getType().equalsIgnoreCase("integer"))
                            workflowVariable.setLongValue(joWorkflowVariable.getLong("value"));
                        else if (workflowVariable.getType().equalsIgnoreCase("decimal"))
                            workflowVariable.setDoubleValue(joWorkflowVariable.getDouble("value"));
                        else if (workflowVariable.getType().equalsIgnoreCase("boolean"))
                            workflowVariable.setBooleanValue(joWorkflowVariable.getBoolean("value"));
                        else if (workflowVariable.getType().equalsIgnoreCase("datetime"))
                        {
                            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                            workflowVariable.setDatetimeValue(dateFormat.parse(joWorkflowVariable.getString("value")));
                        }
                        else if (workflowVariable.getType().equalsIgnoreCase("datetime-millisecs"))
                        {
                            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                            workflowVariable.setDatetimeValue(dateFormat.parse(joWorkflowVariable.getString("value")));
                        }
                        else if (workflowVariable.getType().equalsIgnoreCase("jsonObject"))
                            workflowVariable.setJsonObjectValue(joWorkflowVariable.getJSONObject("value"));
                        else if (workflowVariable.getType().equalsIgnoreCase("jsonArray"))
                            workflowVariable.setJsonArrayValue(joWorkflowVariable.getJSONArray("value"));
                        else
                            mLogger.error("Unknown type: " + workflowVariable.getType());
                    }

                    if (joWorkflowVariable.has("position"))
                    {
                        int position = joWorkflowVariable.getInt("position");
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
            if (awsChannelConfInfo.isNull("outputIndex"))
                awsChannelConf.setOutputIndex(null);
            else
                awsChannelConf.setOutputIndex(awsChannelConfInfo.getLong("outputIndex"));
			if (awsChannelConfInfo.isNull("reservedByIngestionJobKey"))
				awsChannelConf.setReservedByIngestionJobKey(null);
			else
				awsChannelConf.setReservedByIngestionJobKey(awsChannelConfInfo.getLong("reservedByIngestionJobKey"));
            if (awsChannelConfInfo.isNull("configurationLabel"))
                awsChannelConf.setConfigurationLabel(null);
            else
                awsChannelConf.setConfigurationLabel(awsChannelConfInfo.getString("configurationLabel"));
        }
        catch (Exception e)
        {
            String errorMessage = "fillAWSChannelConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillCDN77ChannelConf(CDN77ChannelConf cdn77ChannelConf, JSONObject cdn77ChannelConfInfo)
            throws Exception
    {
        try {
            cdn77ChannelConf.setConfKey(cdn77ChannelConfInfo.getLong("confKey"));
            cdn77ChannelConf.setLabel(cdn77ChannelConfInfo.getString("label"));
            cdn77ChannelConf.setRtmpURL(cdn77ChannelConfInfo.getString("rtmpURL"));
            cdn77ChannelConf.setResourceURL(cdn77ChannelConfInfo.getString("resourceURL"));
            cdn77ChannelConf.setFilePath(cdn77ChannelConfInfo.getString("filePath"));
            if (cdn77ChannelConfInfo.isNull("secureToken"))
                cdn77ChannelConf.setSecureToken(null);
            else
                cdn77ChannelConf.setSecureToken(cdn77ChannelConfInfo.getString("secureToken"));
            cdn77ChannelConf.setType(cdn77ChannelConfInfo.getString("type"));
            if (cdn77ChannelConfInfo.isNull("outputIndex"))
                cdn77ChannelConf.setOutputIndex(null);
            else
                cdn77ChannelConf.setOutputIndex(cdn77ChannelConfInfo.getLong("outputIndex"));
            if (cdn77ChannelConfInfo.isNull("reservedByIngestionJobKey"))
                cdn77ChannelConf.setReservedByIngestionJobKey(null);
            else
                cdn77ChannelConf.setReservedByIngestionJobKey(cdn77ChannelConfInfo.getLong("reservedByIngestionJobKey"));
            if (cdn77ChannelConfInfo.isNull("configurationLabel"))
                cdn77ChannelConf.setConfigurationLabel(null);
            else
                cdn77ChannelConf.setConfigurationLabel(cdn77ChannelConfInfo.getString("configurationLabel"));
        }
        catch (Exception e)
        {
            String errorMessage = "fillCDN77ChannelConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillRTMPChannelConf(RTMPChannelConf rtmpChannelConf, JSONObject rtmpChannelConfInfo)
            throws Exception
    {
        try {
            rtmpChannelConf.setConfKey(rtmpChannelConfInfo.getLong("confKey"));
            rtmpChannelConf.setLabel(rtmpChannelConfInfo.getString("label"));
            rtmpChannelConf.setRtmpURL(rtmpChannelConfInfo.getString("rtmpURL"));
            if (rtmpChannelConfInfo.isNull("streamName"))
                rtmpChannelConf.setStreamName(null);
            else
                rtmpChannelConf.setStreamName(rtmpChannelConfInfo.getString("streamName"));
            if (rtmpChannelConfInfo.isNull("userName"))
                rtmpChannelConf.setUserName(null);
            else
                rtmpChannelConf.setUserName(rtmpChannelConfInfo.getString("userName"));
            if (rtmpChannelConfInfo.isNull("password"))
                rtmpChannelConf.setPassword(null);
            else
                rtmpChannelConf.setPassword(rtmpChannelConfInfo.getString("password"));
            if (rtmpChannelConfInfo.isNull("playURL"))
                rtmpChannelConf.setPlayURL(null);
            else
                rtmpChannelConf.setPlayURL(rtmpChannelConfInfo.getString("playURL"));
            rtmpChannelConf.setType(rtmpChannelConfInfo.getString("type"));
            if (rtmpChannelConfInfo.isNull("outputIndex"))
                rtmpChannelConf.setOutputIndex(null);
            else
                rtmpChannelConf.setOutputIndex(rtmpChannelConfInfo.getLong("outputIndex"));
            if (rtmpChannelConfInfo.isNull("reservedByIngestionJobKey"))
                rtmpChannelConf.setReservedByIngestionJobKey(null);
            else
                rtmpChannelConf.setReservedByIngestionJobKey(rtmpChannelConfInfo.getLong("reservedByIngestionJobKey"));
            if (rtmpChannelConfInfo.isNull("configurationLabel"))
                rtmpChannelConf.setConfigurationLabel(null);
            else
                rtmpChannelConf.setConfigurationLabel(rtmpChannelConfInfo.getString("configurationLabel"));
        }
        catch (Exception e)
        {
            String errorMessage = "fillRTMPChannelConf failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }
    }

    private void fillHLSChannelConf(HLSChannelConf hlsChannelConf, JSONObject hlsChannelConfInfo)
            throws Exception
    {
        try {
            hlsChannelConf.setConfKey(hlsChannelConfInfo.getLong("confKey"));
            hlsChannelConf.setLabel(hlsChannelConfInfo.getString("label"));
            hlsChannelConf.setDeliveryCode(hlsChannelConfInfo.getLong("deliveryCode"));
            if (hlsChannelConfInfo.isNull("segmentDuration"))
                hlsChannelConf.setSegmentDuration(null);
            else
                hlsChannelConf.setSegmentDuration(hlsChannelConfInfo.getLong("segmentDuration"));
            if (hlsChannelConfInfo.isNull("playlistEntriesNumber"))
                hlsChannelConf.setPlaylistEntriesNumber(null);
            else
                hlsChannelConf.setPlaylistEntriesNumber(hlsChannelConfInfo.getLong("playlistEntriesNumber"));
            hlsChannelConf.setType(hlsChannelConfInfo.getString("type"));
            if (hlsChannelConfInfo.isNull("outputIndex"))
                hlsChannelConf.setOutputIndex(null);
            else
                hlsChannelConf.setOutputIndex(hlsChannelConfInfo.getLong("outputIndex"));
            if (hlsChannelConfInfo.isNull("reservedByIngestionJobKey"))
                hlsChannelConf.setReservedByIngestionJobKey(null);
            else
                hlsChannelConf.setReservedByIngestionJobKey(hlsChannelConfInfo.getLong("reservedByIngestionJobKey"));
            if (hlsChannelConfInfo.isNull("configurationLabel"))
                hlsChannelConf.setConfigurationLabel(null);
            else
                hlsChannelConf.setConfigurationLabel(hlsChannelConfInfo.getString("configurationLabel"));
        }
        catch (Exception e)
        {
            String errorMessage = "fillHLSChannelConf failed. Exception: " + e;
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
