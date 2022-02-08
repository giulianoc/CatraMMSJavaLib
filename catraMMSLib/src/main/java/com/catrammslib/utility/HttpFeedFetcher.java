package com.catrammslib.utility;

// import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.util.Base64;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sun.net.www.protocol.http.HttpURLConnection;


public class HttpFeedFetcher {

    private static final Logger mLogger = Logger.getLogger(HttpFeedFetcher.class);

    public static final String configFileName = "mpCommon.properties";

    static public String fetchGetHttpsJson(String url, int timeoutInSeconds, int maxRetriesNumber,
                                            String user, String password, String authorizationHeader)
            throws Exception
    {
        // fetchWebPage
        mLogger.debug(String.format("fetchWebPage(%s) ", url));
        String result = "";
        // Date startTimestamp = new Date();
        if (StringUtils.isNotEmpty(url))
        {
            if (url.startsWith("https"))
            {
                SSLContext ctx = SSLContext.getInstance("SSL");
                ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
                SSLContext.setDefault(ctx);
                HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());

                // Create all-trusting host name verifier
                HostnameVerifier allHostsValid = new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                };

                // Install the all-trusting host verifier
                HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            }

            // GetMethod method = null;
            int retryIndex = 0;

            while(retryIndex < maxRetriesNumber)
            {
                retryIndex++;

                try
                {
                    mLogger.info("url: " + url);
                    URL uUrl = new URL(url);
                    // HttpsURLConnection conn = (HttpsURLConnection) uUrl.openConnection();
                    URLConnection conn;
                    if (url.startsWith("https"))
                    {
                        conn = (HttpsURLConnection) uUrl.openConnection();
                        /*
                        conn.setHostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String arg0, SSLSession arg1) {
                                return true;
                            }
                        });
                        */
                    }
                    else
                        conn = uUrl.openConnection();
                    conn.setConnectTimeout(timeoutInSeconds * 1000);
                    conn.setReadTimeout(timeoutInSeconds * 1000);

                    if (authorizationHeader != null)
                    {
                        conn.setRequestProperty("Authorization", authorizationHeader);
                        mLogger.info("Add Header, Authorization: " + authorizationHeader);
                    }
                    else if (user != null && password != null)
                    {
                        // String encoded = DatatypeConverter.printBase64Binary((user + ":" + password).getBytes("utf-8"));
                        String encoded = Base64.getEncoder().encodeToString((user + ":" + password).getBytes("utf-8"));
                        conn.setRequestProperty("Authorization", "Basic " + encoded);
                        mLogger.info("Add Header (user " + user + "). " + "Authorization: " + "Basic " + encoded);
                        // mLogger.info("Add Header (password " + password + "). " + "Authorization: " + "Basic " + encoded);
                    }

                    mLogger.info("conn.getResponseCode...");
                    int statusCode;
                    int contentLength;
                    if (url.startsWith("https"))
                    {
                        statusCode = ((HttpsURLConnection) conn).getResponseCode();
                        contentLength = ((HttpsURLConnection) conn).getContentLength();
                    }
                    else
                    {
                        statusCode = ((HttpURLConnection) conn).getResponseCode();
                        contentLength = ((HttpURLConnection) conn).getContentLength();
                    }

                    mLogger.info("conn.getResponseCode. statusCode: " + statusCode
                            + ", contentLength: " + contentLength
                    );
                    if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_CREATED)
                    {
						mLogger.error("Response"
								+ ", url: " + url
								+ ", statusCode: " + statusCode
						);

						/*
						String responseMessage;

						does not work
						if (url.startsWith("https"))
                            responseMessage = ((HttpsURLConnection) conn).getResponseMessage();
                        else
                            responseMessage = ((HttpURLConnection) conn).getResponseMessage();

						mLogger.error("Response"
							+ ", responseMessage: " + responseMessage
						);

                        result = null;

						does not work
						InputStream is = conn.getInputStream();
						mLogger.error("Response"
								+ ", is: " + is
						);
						InputStreamReader isr = new InputStreamReader(is);
						mLogger.error("Response"
								+ ", isr: " + isr
						);
	
						int numCharsRead;
						int totalCharsRead = 0;
						char[] charArray = new char[1024 * 10];
						StringBuffer sb = new StringBuffer();
						while ((numCharsRead = isr.read(charArray)) != -1)
						{
							sb.append(charArray, 0, numCharsRead);
							totalCharsRead += numCharsRead;
							// mLogger.info("content read: " + totalCharsRead + "/" + contentLength + "(" + (contentLength - totalCharsRead) + ")");
						}
	
						mLogger.error("Response"
								+ ", isr: " + isr
						);

						responseMessage = sb.toString();
	
						mLogger.error("Response"
								+ ", url: " + url
								+ ", statusCode: " + statusCode
								+ ", contentLength: " + contentLength
								+ ", responseMessage: " + responseMessage
						);
						*/
		
                        throw new Exception("Method failed, statusCode: " + statusCode);
                    }

                    // Read the response body.
                    // result = method.getResponseBodyAsString();
                    InputStream is = conn.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);

                    int numCharsRead;
                    int totalCharsRead = 0;
                    char[] charArray = new char[1024 * 10];
                    StringBuffer sb = new StringBuffer();
                    while ((numCharsRead = isr.read(charArray)) != -1)
                    {
                        sb.append(charArray, 0, numCharsRead);
                        totalCharsRead += numCharsRead;
                        // mLogger.info("content read: " + totalCharsRead + "/" + contentLength + "(" + (contentLength - totalCharsRead) + ")");
                    }

                    result = sb.toString();

                    mLogger.info("Response"
                            + ", url: " + url
                            + ", contentLength: " + contentLength
                            + ", result.length: " + result.length()
                    );

                    mLogger.debug("result: " + result);

                    break; // exit from the retry loop
                }
                catch (HttpException e) {
                    String errorMessage = "URL: " + url
                            + ", Fatal protocol violation: " + e
                            + ", maxRetriesNumber: " + maxRetriesNumber
                            + ", retryIndex: " + (retryIndex - 1)
                            ;
                    mLogger.error(errorMessage);

                    if (retryIndex >= maxRetriesNumber)
                        throw e;
                    else
                        Thread.sleep(100);  // half second
                }
                catch (IOException e) {
                    String errorMessage = "URL: " + url
                            + ", Fatal transport error: " + e
                            // + ", last chars read: " + sb.toString().substring(sb.toString().length() - 100)
                            + ", maxRetriesNumber: " + maxRetriesNumber
                            + ", retryIndex: " + (retryIndex - 1)
                            ;
                    mLogger.error(errorMessage);

                    if (retryIndex >= maxRetriesNumber)
                        throw e;
                    else
                        Thread.sleep(100);  // half second
                }
                catch (Exception e) {
                    String errorMessage = "URL: " + url
                            + ", Fatal transport error: " + e
                            + ", maxRetriesNumber: " + maxRetriesNumber
                            + ", retryIndex: " + (retryIndex - 1)
                            ;
                    mLogger.error(errorMessage);

                    if (retryIndex >= maxRetriesNumber)
                        throw e;
                    else
                        Thread.sleep(100);  // half second
                }
                /*
                finally {
                    // Release the connection.
                    if (method != null)
                        method.releaseConnection();
                }
                */
            }
        }

        // elapsed time saved in the calling method
        // mLogger.info("@fetchHttpsJson " + url + "@ elapsed (milliseconds): @" + (new Date().getTime() - startTimestamp.getTime()) + "@");

        return result;
    }

    static public String fetchGetBearerHttpsJson(String url, String acceptHeader,
                                                 int timeoutInSeconds, int maxRetriesNumber,
                                                 String authorization)
            throws Exception
    {
        // fetchWebPage
        mLogger.debug(String.format("fetchWebPage(%s) ", url));
        String result = "";
        // Date startTimestamp = new Date();
        if (StringUtils.isNotEmpty(url))
        {
            if (url.startsWith("https"))
            {
                SSLContext ctx = SSLContext.getInstance("SSL");
                ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
                SSLContext.setDefault(ctx);
                HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());

                // Create all-trusting host name verifier
                HostnameVerifier allHostsValid = new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                };

                // Install the all-trusting host verifier
                HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            }

            // GetMethod method = null;
            int retryIndex = 0;

            while(retryIndex < maxRetriesNumber)
            {
                retryIndex++;

                try
                {
                    mLogger.info("url: " + url);
                    URL uUrl = new URL(url);
                    // HttpsURLConnection conn = (HttpsURLConnection) uUrl.openConnection();
                    URLConnection conn;
                    if (url.startsWith("https"))
                    {
                        conn = (HttpsURLConnection) uUrl.openConnection();
                        /*
                        conn.setHostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String arg0, SSLSession arg1) {
                                return true;
                            }
                        });
                        */
                    }
                    else
                        conn = uUrl.openConnection();
                    conn.setConnectTimeout(timeoutInSeconds * 1000);
                    conn.setReadTimeout(timeoutInSeconds * 1000);

                    {
                        conn.setRequestProperty("Authorization", "Bearer " + authorization);
                        mLogger.info("Add Header. " + "Authorization: " + "Bearer " + authorization);
                        // mLogger.info("Add Header (password " + password + "). " + "Authorization: " + "Basic " + encoded);
                    }

                    conn.setRequestProperty("Accept", acceptHeader);
                    mLogger.info("Header. " + "Accept: " + acceptHeader);

                    mLogger.info("conn.getResponseCode...");
                    int statusCode;
                    int contentLength;
                    if (url.startsWith("https"))
                    {
                        statusCode = ((HttpsURLConnection) conn).getResponseCode();
                        contentLength = ((HttpsURLConnection) conn).getContentLength();
                    }
                    else
                    {
                        statusCode = ((HttpURLConnection) conn).getResponseCode();
                        contentLength = ((HttpURLConnection) conn).getContentLength();
                    }

                    mLogger.info("conn.getResponseCode. statusCode: " + statusCode
                            + ", contentLength: " + contentLength
                    );
                    if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_CREATED)
                    {
						/*
						does not work
                        String responseMessage;
                        if (url.startsWith("https"))
                            responseMessage = ((HttpsURLConnection) conn).getResponseMessage();
                        else
                            responseMessage = ((HttpURLConnection) conn).getResponseMessage();

                        mLogger.debug("Method failed: " + responseMessage);
						*/

                        result = null;

                        throw new Exception("Method failed, statusCode: " + statusCode);
                    }

                    // Read the response body.
                    // result = method.getResponseBodyAsString();
                    InputStream is = conn.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);

                    int numCharsRead;
                    int totalCharsRead = 0;
                    char[] charArray = new char[1024 * 10];
                    StringBuffer sb = new StringBuffer();
                    while ((numCharsRead = isr.read(charArray)) != -1)
                    {
                        sb.append(charArray, 0, numCharsRead);
                        totalCharsRead += numCharsRead;
                        // mLogger.info("content read: " + totalCharsRead + "/" + contentLength + "(" + (contentLength - totalCharsRead) + ")");
                    }

                    result = sb.toString();

                    mLogger.info("Response"
                            + ", url: " + url
                            + ", contentLength: " + contentLength
                            + ", result.length: " + result.length()
                    );

                    mLogger.debug("result: " + result);

                    break; // exit from the retry loop
                }
                catch (HttpException e) {
                    String errorMessage = "URL: " + url
                            + ", Fatal protocol violation: " + e
                            + ", maxRetriesNumber: " + maxRetriesNumber
                            + ", retryIndex: " + (retryIndex - 1)
                            ;
                    mLogger.error(errorMessage);

                    if (retryIndex >= maxRetriesNumber)
                        throw e;
                    else
                        Thread.sleep(100);  // half second
                }
                catch (IOException e) {
                    String errorMessage = "URL: " + url
                            + ", Fatal transport error: " + e
                            // + ", last chars read: " + sb.toString().substring(sb.toString().length() - 100)
                            + ", maxRetriesNumber: " + maxRetriesNumber
                            + ", retryIndex: " + (retryIndex - 1)
                            ;
                    mLogger.error(errorMessage);

                    if (retryIndex >= maxRetriesNumber)
                        throw e;
                    else
                        Thread.sleep(100);  // half second
                }
                catch (Exception e) {
                    String errorMessage = "URL: " + url
                            + ", Fatal transport error: " + e
                            + ", maxRetriesNumber: " + maxRetriesNumber
                            + ", retryIndex: " + (retryIndex - 1)
                            ;
                    mLogger.error(errorMessage);

                    if (retryIndex >= maxRetriesNumber)
                        throw e;
                    else
                        Thread.sleep(100);  // half second
                }
                /*
                finally {
                    // Release the connection.
                    if (method != null)
                        method.releaseConnection();
                }
                */
            }
        }

        // elapsed time saved in the calling method
        // mLogger.info("@fetchHttpsJson " + url + "@ elapsed (milliseconds): @" + (new Date().getTime() - startTimestamp.getTime()) + "@");

        return result;
    }

    static public String fetchPostHttpsJson(String url, String contentType, int timeoutInSeconds, int maxRetriesNumber,
                                            String user, String password, String authorizationHeader, String postBodyRequest)
            throws Exception
    {
        return fetchBodyHttpsJson("POST", url, contentType, timeoutInSeconds, maxRetriesNumber,
                user, password, authorizationHeader, postBodyRequest);
    }

    static public String fetchPutHttpsJson(String url, int timeoutInSeconds, int maxRetriesNumber,
                                            String user, String password, String authorizationHeader,
											String putBodyRequest)
            throws Exception
    {
        String contentType = null;
        return fetchBodyHttpsJson("PUT", url, contentType, timeoutInSeconds, maxRetriesNumber, 
			user, password, authorizationHeader, putBodyRequest);
    }

    static public String fetchDeleteHttpsJson(String url, int timeoutInSeconds, int maxRetriesNumber,
                                           String user, String password, String deleteBodyRequest)
            throws Exception
    {
        String contentType = null;
        // String deleteBodyRequest = null;
        return fetchBodyHttpsJson("DELETE", url, contentType, timeoutInSeconds, maxRetriesNumber,
                user, password, null, deleteBodyRequest);
    }

    static private String fetchBodyHttpsJson(String httpMethod, String url, String contentType,
                                             int timeoutInSeconds, int maxRetriesNumber,
                                             String user, String password, String authorizationHeader,
                                             String postBodyRequest)
            throws Exception
    {
        // fetchWebPage
        mLogger.debug(String.format("fetchWebPage(%s) ", url));
        String result = "";
        // Date startTimestamp = new Date();
        if (StringUtils.isNotEmpty(url))
        {
            if (url.startsWith("https"))
            {
                SSLContext ctx = SSLContext.getInstance("SSL");
                ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
                SSLContext.setDefault(ctx);
                HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());

                // Create all-trusting host name verifier
                HostnameVerifier allHostsValid = new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                };

                // Install the all-trusting host verifier
                HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            }

            // GetMethod method = null;
            int retryIndex = 0;

            while(retryIndex < maxRetriesNumber)
            {
                retryIndex++;

                try
                {
                    /*
                    method = new GetMethod(url);

                    // Provide custom retry handler is necessary
                    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
                    // method.addRequestHeader("X-Inline", "describedby");
                    // Credentials credentials = new UsernamePasswordCredentials("admin", "admin");

                    HttpClient httpClient = new HttpClient();
                    // httpClient.getState().setCredentials(AuthScope.ANY, credentials);

                    // Execute the method.
                    int statusCode = httpClient.executeMethod(method);
                    */

                    mLogger.info("url: " + url);
                    URL uUrl = new URL(url);
                    // HttpsURLConnection conn = (HttpsURLConnection) uUrl.openConnection();
                    URLConnection conn;
                    if (url.startsWith("https"))
                    {
                        conn = (HttpsURLConnection) uUrl.openConnection();
                        /*
                        conn.setHostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String arg0, SSLSession arg1) {
                                return true;
                            }
                        });
                        */
                    }
                    else
                        conn = uUrl.openConnection();
                    conn.setConnectTimeout(timeoutInSeconds * 1000);
                    conn.setReadTimeout(timeoutInSeconds * 1000);

                    if (authorizationHeader != null)
                    {
                        conn.setRequestProperty("Authorization", authorizationHeader);
                        mLogger.info("Add Header, Authorization: " + authorizationHeader);
                    }
                    else if (user != null && password != null)
                    {
                        // String encoded = DatatypeConverter.printBase64Binary((user + ":" + password).getBytes("utf-8"));
                        String encoded = Base64.getEncoder().encodeToString((user + ":" + password).getBytes("utf-8"));
                        conn.setRequestProperty("Authorization", "Basic " + encoded);
                        mLogger.info("Add Header (user " + user + ", password " + "..." + "). " + "Authorization: " + "Basic " + encoded);
                    }

                    conn.setDoOutput(true); // false because I do not need to append any data to this request
                    if (url.startsWith("https"))
                        ((HttpsURLConnection) conn).setRequestMethod(httpMethod);
                    else
                        ((HttpURLConnection) conn).setRequestMethod(httpMethod);
                    {
                        int clength;
                        byte[] bytes = null;
                        if (postBodyRequest != null && postBodyRequest.length() > 0)
                        {
                            bytes = postBodyRequest.getBytes("UTF-8");
                            clength = bytes.length;
                        }
                        else
                        {
                            clength = 0;
                        }

                        if (clength > 0)
                        {
                            if (contentType != null)
                            {
                                conn.setRequestProperty("Content-Type", contentType);
                                mLogger.info("Header. " + "Content-Type: " + contentType);
                            }
                            else
                            {
                                conn.setRequestProperty("Content-Type", "application/json");
                                mLogger.info("Header. " + "Content-Type: " + "application/json");
                            }
                        }

                        conn.setRequestProperty("Content-Length", String.valueOf(clength));
                        mLogger.info("Header. " + "Content-Length: " + String.valueOf(clength));

                        conn.setDoInput(true); // false means the response is ignored

                        if (clength > 0)
                        {
                            // con.getOutputStream().write(postBodyRequest.getBytes(), 0, clength);
                            OutputStream outputStream = conn.getOutputStream();
                            outputStream.write(bytes);
                            outputStream.flush();
                            outputStream.close();
                        }
                    }

                    mLogger.info("conn.getResponseCode...");
                    int statusCode;
                    if (url.startsWith("https"))
                        statusCode = ((HttpsURLConnection) conn).getResponseCode();
                    else
                        statusCode = ((HttpURLConnection) conn).getResponseCode();

                    mLogger.info("conn.getResponseCode. statusCode: " + statusCode);
                    if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_CREATED)
                    {
						/*
						does not work
                        String responseMessage;
                        if (url.startsWith("https"))
                            responseMessage = ((HttpsURLConnection) conn).getResponseMessage();
                        else
                            responseMessage = ((HttpURLConnection) conn).getResponseMessage();

                        // I do not know how to read the body in case of error,
                        // the below code does not read it
                        {
                            // Read the response body.
                            InputStream is;
                            if (url.startsWith("https"))
                                is = ((HttpsURLConnection) conn).getErrorStream();
                            else
                                is = ((HttpURLConnection) conn).getErrorStream();

                            if (is != null)
                            {
                                InputStreamReader isr = new InputStreamReader(is);

                                int numCharsRead;
                                char[] charArray = new char[1024 * 10];
                                StringBuffer sb = new StringBuffer();
                                while ((numCharsRead = isr.read(charArray)) > 0)
                                    sb.append(charArray, 0, numCharsRead);

                                responseMessage += sb.toString();
                            }
                        }

                        mLogger.debug("Method failed: " + responseMessage);

                        result = null;
						*/

                        throw new Exception("Method failed, statusCode: " + statusCode);
                    }

                    // Read the response body.
                    // result = method.getResponseBodyAsString();
                    InputStream is = conn.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);

                    int numCharsRead;
                    char[] charArray = new char[1024 * 10];
                    StringBuffer sb = new StringBuffer();
                    while ((numCharsRead = isr.read(charArray)) > 0)
                        sb.append(charArray, 0, numCharsRead);

                    result = sb.toString();

                    mLogger.debug("result: " + result);

                    break;  // exit from the retry loop
                }
                catch (HttpException e) {
                    String errorMessage = "URL: " + url
                            + ", Fatal protocol violation: " + e
                            + ", maxRetriesNumber: " + maxRetriesNumber
                            + ", retryIndex: " + (retryIndex - 1)
                            ;
                    mLogger.error(errorMessage);

                    if (retryIndex >= maxRetriesNumber)
                        throw e;
                    else
                        Thread.sleep(100);  // half second
                }
                catch (IOException e) {
                    String errorMessage = "URL: " + url
                            + ", Fatal transport error: " + e
                            + ", maxRetriesNumber: " + maxRetriesNumber
                            + ", retryIndex: " + (retryIndex - 1)
                            ;
                    mLogger.error(errorMessage);

                    if (retryIndex >= maxRetriesNumber)
                        throw e;
                    else
                        Thread.sleep(100);  // half second
                }
                catch (Exception e) {
                    String errorMessage = "URL: " + url
                            + ", Fatal transport error: " + e
                            + ", maxRetriesNumber: " + maxRetriesNumber
                            + ", retryIndex: " + (retryIndex - 1)
                            ;
                    mLogger.error(errorMessage);

                    if (retryIndex >= maxRetriesNumber)
                        throw e;
                    else
                        Thread.sleep(100);  // half second
                }
                /*
                finally {
                    // Release the connection.
                    if (method != null)
                        method.releaseConnection();
                }
                */
            }
        }

        // elapsed time saved in the calling method
        // mLogger.info("@fetchHttpsJson " + url + "@ elapsed (milliseconds): @" + (new Date().getTime() - startTimestamp.getTime()) + "@");

        return result;
    }

    static public void fetchPostHttpBinary(String url, int timeoutInSeconds, int maxRetriesNumber,
                                            String user, String password,
                                           InputStream inputStreamBinary, long contentLength)
            throws Exception
    {
        // fetchWebPage
        mLogger.debug(String.format("fetchWebPage(%s) ", url));
        // Date startTimestamp = new Date();
        if (StringUtils.isNotEmpty(url))
        {
            if (url.startsWith("https"))
            {
                SSLContext ctx = SSLContext.getInstance("SSL");
                ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
                SSLContext.setDefault(ctx);
                HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());

                // Create all-trusting host name verifier
                HostnameVerifier allHostsValid = new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                };

                // Install the all-trusting host verifier
                HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            }

            // GetMethod method = null;
            int retryIndex = 0;

            while(retryIndex < maxRetriesNumber)
            {
                retryIndex++;

                try
                {
                    /*
                    method = new GetMethod(url);

                    // Provide custom retry handler is necessary
                    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
                    // method.addRequestHeader("X-Inline", "describedby");
                    // Credentials credentials = new UsernamePasswordCredentials("admin", "admin");

                    HttpClient httpClient = new HttpClient();
                    // httpClient.getState().setCredentials(AuthScope.ANY, credentials);

                    // Execute the method.
                    int statusCode = httpClient.executeMethod(method);
                    */

                    mLogger.info("url: " + url);
                    URL uUrl = new URL(url);
                    URLConnection conn;
                    if (url.startsWith("https"))
                    {
                        conn = (HttpsURLConnection) uUrl.openConnection();
                        /*
                        conn.setHostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String arg0, SSLSession arg1) {
                                return true;
                            }
                        });
                        */
                    }
                    else
                        conn = uUrl.openConnection();
                    conn.setConnectTimeout(timeoutInSeconds * 1000);
                    conn.setReadTimeout(timeoutInSeconds * 1000);

                    {
                        // String encoded = DatatypeConverter.printBase64Binary((user + ":" + password).getBytes("utf-8"));
                        String encoded = Base64.getEncoder().encodeToString((user + ":" + password).getBytes("utf-8"));
                        conn.setRequestProperty("Authorization", "Basic " + encoded);
                        mLogger.info("Add Header (user " + user + "). " + "Authorization: " + "Basic " + encoded);
                        mLogger.info("Add Header (password " + password + "). " + "Authorization: " + "Basic " + encoded);
                    }

                    conn.setDoOutput(true); // false because I do not need to append any data to this request
                    if (url.startsWith("https"))
                        ((HttpsURLConnection) conn).setRequestMethod("POST");
                    else
                        ((HttpURLConnection) conn).setRequestMethod("POST");
                    {
                        // long clength = binaryPathName.length();

                        conn.setRequestProperty("Content-Length", String.valueOf(contentLength));
                        mLogger.info("Header. " + "Content-Length: " + String.valueOf(contentLength));

                        conn.setDoInput(true); // false means the response is ignored

                        OutputStream outputStream = null;
                        // InputStream inputStream = null;
                        try {
                            outputStream = conn.getOutputStream();
                            // inputStream = new FileInputStream(binaryPathName);

                            IOUtils.copy(inputStreamBinary, outputStream);
                        }
                        catch (Exception ex)
                        {
                            mLogger.error("Exception: " + ex);
                        }
                        finally {
                            // if (inputStream != null)
                            //    IOUtils.closeQuietly(inputStream);
                            if (outputStream != null)
                                IOUtils.closeQuietly(outputStream);
                        }
                    }

                    mLogger.info("conn.getResponseCode...");
                    int statusCode;
                    if (url.startsWith("https"))
                        statusCode = ((HttpsURLConnection) conn).getResponseCode();
                    else
                        statusCode = ((HttpURLConnection) conn).getResponseCode();

                    mLogger.info("conn.getResponseCode. statusCode: " + statusCode);
                    if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_CREATED)
                    {
						/*
						does not work
                        String responseMessage;
                        if (url.startsWith("https"))
                            responseMessage = ((HttpsURLConnection) conn).getResponseMessage();
                        else
                            responseMessage = ((HttpURLConnection) conn).getResponseMessage();

                        mLogger.debug("Method failed: " + responseMessage);
						*/

                        throw new Exception("Method failed, statusCode: " + statusCode);
                    }

                    mLogger.debug("POST successful. statusCode: " + statusCode);

                    break; // exit from the retry loop
                }
                catch (HttpException e) {
                    String errorMessage = "URL: " + url
                            + ", Fatal protocol violation: " + e
                            + ", maxRetriesNumber: " + maxRetriesNumber
                            + ", retryIndex: " + (retryIndex - 1)
                            ;
                    mLogger.error(errorMessage);

                    if (retryIndex >= maxRetriesNumber)
                        throw e;
                    else
                        Thread.sleep(100);  // half second
                }
                catch (IOException e) {
                    String errorMessage = "URL: " + url
                            + ", Fatal transport error: " + e
                            + ", maxRetriesNumber: " + maxRetriesNumber
                            + ", retryIndex: " + (retryIndex - 1)
                            ;
                    mLogger.error(errorMessage);

                    if (retryIndex >= maxRetriesNumber)
                        throw e;
                    else
                        Thread.sleep(100);  // half second
                }
                catch (Exception e) {
                    String errorMessage = "URL: " + url
                            + ", Fatal transport error: " + e
                            + ", maxRetriesNumber: " + maxRetriesNumber
                            + ", retryIndex: " + (retryIndex - 1)
                            ;
                    mLogger.error(errorMessage);

                    if (retryIndex >= maxRetriesNumber)
                        throw e;
                    else
                        Thread.sleep(100);  // half second
                }
                /*
                finally {
                    // Release the connection.
                    if (method != null)
                        method.releaseConnection();
                }
                */
            }
        }

        // elapsed time saved in the calling method
        // mLogger.info("@fetchHttpsJson " + url + "@ elapsed (milliseconds): @" + (new Date().getTime() - startTimestamp.getTime()) + "@");
    }
}