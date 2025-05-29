package com.catrammslib.utility;

import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.List;


public class HttpFeedFetcher {

    private static final Logger mLogger = LoggerFactory.getLogger(HttpFeedFetcher.class);

    public static final String configFileName = "mpCommon.properties";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    static public String GET(String endpoint, int timeoutInSeconds, int maxRetriesNumber,
                                           String user, String password, String authorizationHeader,
                                           boolean outputToBeCompressed)
            throws Exception
    {
        String body = null;

        // HttpClient client = HttpClient.newHttpClient();

        int retryIndex = 0;
        int maxRequestNumber = maxRetriesNumber + 1;

        while(retryIndex < maxRequestNumber) {
            retryIndex++;

            try {
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .timeout(Duration.ofSeconds(timeoutInSeconds));
                if (authorizationHeader != null)
                    requestBuilder.header("Authorization", authorizationHeader);
                else if (user != null && password != null)
                {
                    String encoded = Base64.getEncoder().encodeToString((user + ":" + password).getBytes("utf-8"));
                    requestBuilder.header("Authorization", "Basic " + encoded);
                }

                if (outputToBeCompressed)
                    requestBuilder.header("X-ResponseBodyCompressed", "true");

                HttpRequest request = requestBuilder
                    .GET()
                    .build();

                body = getResponseBody(CLIENT, request, outputToBeCompressed);

                break; // exit from the retry loop
            } catch (Exception e) {
                String errorMessage = "HttpFeedFetcher. fetchGetHttpsJson"
                        + ", endpoint: " + endpoint
                        + ", Fatal transport error: " + e
                        + ", maxRequestNumber: " + maxRequestNumber
                        + ", retryIndex: " + (retryIndex - 1);
                mLogger.error(errorMessage);

                if (retryIndex >= maxRequestNumber)
                    throw e;
                else
                    Thread.sleep(100);  // half second
            }
        }

        return body;
    }

    static public String fetchGetBearerHttpsJson(String endpoint, String acceptHeader,
                                                 int timeoutInSeconds, int maxRetriesNumber,
                                                 String authorization)
            throws Exception
    {
        String body = null;

        // HttpClient client = HttpClient.newHttpClient();

        int retryIndex = 0;
        int maxRequestNumber = maxRetriesNumber + 1;

        while(retryIndex < maxRequestNumber) {
            retryIndex++;

            try {
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .timeout(Duration.ofSeconds(timeoutInSeconds));
                requestBuilder.header("Authorization", "Bearer " + authorization);
                requestBuilder.header("Accept", acceptHeader);

                HttpRequest request = requestBuilder
                        .GET()
                        .build();

                body = getResponseBody(CLIENT, request, false);

                break; // exit from the retry loop
            } catch (Exception e) {
                String errorMessage = "HttpFeedFetcher. fetchGetBearerHttpsJson"
                        + ", endpoint: " + endpoint
                        + ", Fatal transport error: " + e
                        + ", maxRequestNumber: " + maxRequestNumber
                        + ", retryIndex: " + (retryIndex - 1);
                mLogger.error(errorMessage);

                if (retryIndex >= maxRequestNumber)
                    throw e;
                else
                    Thread.sleep(100);  // half second
            }
        }

        return body;
    }

    static public String fetchPostHttpsJson(String endpoint, String contentType, int timeoutInSeconds, int maxRetriesNumber,
                                            String user, String password, String authorizationHeader, String postBodyRequest,
                                            boolean outputToBeCompressed)
            throws Exception
    {
        String body = null;

        // HttpClient client = HttpClient.newHttpClient();

        int retryIndex = 0;
        int maxRequestNumber = maxRetriesNumber + 1;

        while(retryIndex < maxRequestNumber) {
            retryIndex++;

            try {
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .timeout(Duration.ofSeconds(timeoutInSeconds));
                if (authorizationHeader != null)
                    requestBuilder.header("Authorization", authorizationHeader);
                else if (user != null && password != null)
                {
                    String encoded = Base64.getEncoder().encodeToString((user + ":" + password).getBytes("utf-8"));
                    requestBuilder.header("Authorization", "Basic " + encoded);
                }
                if (outputToBeCompressed)
                    requestBuilder.header("X-ResponseBodyCompressed", "true");
                if (contentType == null)
                    requestBuilder.header("Content-Type", "application/json");
                else
                    requestBuilder.header("Content-Type", contentType);

                HttpRequest request = requestBuilder
                        .POST(HttpRequest.BodyPublishers.ofString(postBodyRequest == null ? "" : postBodyRequest, StandardCharsets.UTF_8))
                        .build();

                body = getResponseBody(CLIENT, request, outputToBeCompressed);

                break; // exit from the retry loop
            } catch (Exception e) {
                String errorMessage = "HttpFeedFetcher. fetchPostHttpsJson"
                        + ", endpoint: " + endpoint
                        + ", Fatal transport error: " + e
                        + ", maxRequestNumber: " + maxRequestNumber
                        + ", retryIndex: " + (retryIndex - 1);
                mLogger.error(errorMessage);

                if (retryIndex >= maxRequestNumber)
                    throw e;
                else
                    Thread.sleep(100);  // half second
            }
        }

        return body;
    }

    static public String fetchPatchHttpsJson(String endpoint, String contentType, int timeoutInSeconds, int maxRetriesNumber,
                                            String user, String password, String authorizationHeader, String patchBodyRequest,
                                            boolean outputToBeCompressed)
            throws Exception
    {
        String body = null;

        // HttpClient client = HttpClient.newHttpClient();

        int retryIndex = 0;
        int maxRequestNumber = maxRetriesNumber + 1;

        while(retryIndex < maxRequestNumber) {
            retryIndex++;

            try {
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .timeout(Duration.ofSeconds(timeoutInSeconds));
                if (authorizationHeader != null)
                    requestBuilder.header("Authorization", authorizationHeader);
                else if (user != null && password != null)
                {
                    String encoded = Base64.getEncoder().encodeToString((user + ":" + password).getBytes("utf-8"));
                    requestBuilder.header("Authorization", "Basic " + encoded);
                }
                if (outputToBeCompressed)
                    requestBuilder.header("X-ResponseBodyCompressed", "true");
                if (contentType == null)
                    requestBuilder.header("Content-Type", "application/json");
                else
                    requestBuilder.header("Content-Type", contentType);

                HttpRequest request = requestBuilder
                        .method("PATCH", HttpRequest.BodyPublishers.ofString(patchBodyRequest, StandardCharsets.UTF_8))
                        .build();

                body = getResponseBody(CLIENT, request, outputToBeCompressed);

                break; // exit from the retry loop
            } catch (Exception e) {
                String errorMessage = "HttpFeedFetcher. fetchPatchHttpsJson"
                        + ", endpoint: " + endpoint
                        + ", Fatal transport error: " + e
                        + ", maxRequestNumber: " + maxRequestNumber
                        + ", retryIndex: " + (retryIndex - 1);
                mLogger.error(errorMessage);

                if (retryIndex >= maxRequestNumber)
                    throw e;
                else
                    Thread.sleep(100);  // half second
            }
        }

        return body;
    }

    static public String fetchPutHttpsJson(String endpoint, int timeoutInSeconds, int maxRetriesNumber,
                                            String user, String password, String authorizationHeader, String putBodyRequest,
                                            boolean outputToBeCompressed)
            throws Exception
    {
        String body = null;

        // HttpClient client = HttpClient.newHttpClient();

        int retryIndex = 0;
        int maxRequestNumber = maxRetriesNumber + 1;

        while(retryIndex < maxRequestNumber) {
            retryIndex++;

            try {
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .timeout(Duration.ofSeconds(timeoutInSeconds));
                if (authorizationHeader != null)
                    requestBuilder.header("Authorization", authorizationHeader);
                else if (user != null && password != null)
                {
                    String encoded = Base64.getEncoder().encodeToString((user + ":" + password).getBytes("utf-8"));
                    requestBuilder.header("Authorization", "Basic " + encoded);
                }
                if (outputToBeCompressed)
                    requestBuilder.header("X-ResponseBodyCompressed", "true");
                requestBuilder.header("Content-Type", "application/json");

                HttpRequest request = requestBuilder
                        .PUT(HttpRequest.BodyPublishers.ofString(putBodyRequest == null ? "" : putBodyRequest, StandardCharsets.UTF_8))
                        .build();

                body = getResponseBody(CLIENT, request, outputToBeCompressed);

                break; // exit from the retry loop
            } catch (Exception e) {
                String errorMessage = "HttpFeedFetcher. fetchPutHttpsJson"
                        + ", endpoint: " + endpoint
                        + ", Fatal transport error: " + e
                        + ", maxRequestNumber: " + maxRequestNumber
                        + ", retryIndex: " + (retryIndex - 1);
                mLogger.error(errorMessage);

                if (retryIndex >= maxRequestNumber)
                    throw e;
                else
                    Thread.sleep(100);  // half second
            }
        }

        return body;
    }

    static public String fetchDeleteHttpsJson(String endpoint, int timeoutInSeconds, int maxRetriesNumber,
                                           String user, String password) //, String deleteBodyRequest)
            throws Exception
    {
        String body = null;

        // HttpClient client = HttpClient.newHttpClient();

        int retryIndex = 0;
        int maxRequestNumber = maxRetriesNumber + 1;

        while(retryIndex < maxRequestNumber) {
            retryIndex++;

            try {
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .timeout(Duration.ofSeconds(timeoutInSeconds));
                if (user != null && password != null)
                {
                    String encoded = Base64.getEncoder().encodeToString((user + ":" + password).getBytes("utf-8"));
                    requestBuilder.header("Authorization", "Basic " + encoded);
                }
                requestBuilder.header("Content-Type", "application/json");

                HttpRequest request = requestBuilder
                        .DELETE()
                        .build();

                body = getResponseBody(CLIENT, request, false);

                break; // exit from the retry loop
            } catch (Exception e) {
                String errorMessage = "HttpFeedFetcher. fetchDeleteHttpsJson"
                        + ", endpoint: " + endpoint
                        + ", Fatal transport error: " + e
                        + ", maxRequestNumber: " + maxRequestNumber
                        + ", retryIndex: " + (retryIndex - 1);
                mLogger.error(errorMessage);

                if (retryIndex >= maxRequestNumber)
                    throw e;
                else
                    Thread.sleep(100);  // half second
            }
        }

        return body;
    }

    static public String fetchPostHttpBinary(String endpoint, int timeoutInSeconds, int maxRetriesNumber,
                                             String user, String password,
                                             InputStream inputStreamBinary, long contentLength,
                                             long contentRangeStart, // -1 se non deve essere usato
                                             long contentRangeEnd_Excluded // -1 se non deve essere usato
    )
            throws Exception
    {
        String body = null;

        // HttpClient client = HttpClient.newHttpClient();

        int retryIndex = 0;
        int maxRequestNumber = maxRetriesNumber + 1;

        while(retryIndex < maxRequestNumber) {
            retryIndex++;

            Path tempFile = null;

            try {
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .timeout(Duration.ofSeconds(timeoutInSeconds));
                {
                    String encoded = Base64.getEncoder().encodeToString((user + ":" + password).getBytes("utf-8"));
                    requestBuilder.header("Authorization", "Basic " + encoded);
                }

                if (contentRangeStart >= 0 && contentRangeEnd_Excluded > 0)
                    requestBuilder.header("Content-Range", "bytes " + contentRangeStart + "-" + (contentRangeEnd_Excluded - 1) + "/" + contentLength);
                /*
                2023-07-22: ho dovuto commentare il setting dell'header content length perchè in jdk11 non è permesso.
                    In jdk12 hanno aggiunto una property per sovrascrivere questo comportamento.
                    Dalla docs: # By default, the following request headers are not allowed to be set by user code
                                # in HttpRequests: "connection", "content-length", "expect", "host" and "upgrade".
                                # The 'jdk.httpclient.allowRestrictedHeaders' property allows one or more of these
                                # headers to be specified as a comma separated list to override the default restriction.
                */
                // else
                //    requestBuilder.header("Content-Length", String.valueOf(contentLength));

                // crea un file temporaneo
                tempFile = Files.createTempFile("postBinary_", ".bin");
                mLogger.info("HttpFeedFetcher. fetchPostHttpBinary. write to temporary file"
                        + "tmpdir: " + System.getProperty("java.io.tmpdir")
                        + ", tempFile: " + tempFile
                );

                OutputStream outputStream = Files.newOutputStream(tempFile);
                try
                {
                    int bufferSize = 1024 * 10;
                    byte[] buffer = new byte[bufferSize];

                    long currentStart = 0;
                    if (contentRangeStart > 0)
                    {
                        currentStart = contentRangeStart;

                        // no skip is needed because the InputStream is not 'reopened'
                        // inputStreamBinary.skip(currentStart);
                    }

                    long currentEnd = contentLength;
                    if (contentRangeEnd_Excluded > 0)
                        currentEnd = contentRangeEnd_Excluded;

                    while(currentStart < currentEnd)
                    {
                        int len;
                        if (currentStart + bufferSize <= currentEnd)
                            len = bufferSize;
                        else
                            len = (int) (currentEnd - currentStart);
                        int bytesRead = inputStreamBinary.read(buffer, 0, len);
                        if (bytesRead > 0)
                        {
                            currentStart += bytesRead;
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                }
                catch (Exception ex)
                {
                    mLogger.error("HttpFeedFetcher. fetchPostHttpBinary"
                            + ", tempFile: " + tempFile
                            + ", Exception: " + ex);
                }
                finally {
                    if (outputStream != null)
                        outputStream.close(); // IOUtils.closeQuietly(outputStream);
                }

                mLogger.info("HttpFeedFetcher. fetchPostHttpBinary. check length"
                        + ", contentLength: " + contentLength
                        + ", contentRangeStart: " + contentRangeStart
                        + ", contentRangeEnd_Excluded: " + contentRangeEnd_Excluded
                        + ", tempFile.toFile().length: " + tempFile.toFile().length()
                );

                HttpRequest request = requestBuilder
                        .POST(HttpRequest.BodyPublishers.ofFile(tempFile))
                        .build();

                body = getResponseBody(CLIENT, request, false);

                if (tempFile.toFile().exists())
                    tempFile.toFile().delete();

                break; // exit from the retry loop
            } catch (Exception e) {
                String errorMessage = "HttpFeedFetcher. fetchPostHttpBinary"
                        + ", endpoint: " + endpoint
                        + ", contentLength: " + contentLength
                        + ", contentRangeStart: " + contentRangeStart
                        + ", contentRangeEnd_Excluded: " + contentRangeEnd_Excluded
                        + ", Fatal transport error: " + e
                        + ", maxRequestNumber: " + maxRequestNumber
                        + ", retryIndex: " + (retryIndex - 1);
                mLogger.error(errorMessage);

                if (tempFile != null && tempFile.toFile().exists())
                    tempFile.toFile().delete();

                if (retryIndex >= maxRequestNumber)
                    throw e;
                else
                    Thread.sleep(100);  // half second
            }
        }

        return body;
    }

    static public String fetchFormDataHttpsJson(String requestType, String endpoint, int timeoutInSeconds, int maxRetriesNumber,
                                    List<String> formNames, List<String> formValues)
            throws Exception
    {
        String body = null;

        // HttpClient client = HttpClient.newHttpClient();

        int retryIndex = 0;
        int maxRequestNumber = maxRetriesNumber + 1;

        while(retryIndex < maxRequestNumber) {
            retryIndex++;

            try {
                String boundary = String.valueOf(System.currentTimeMillis());
                String dashes =               "--------------------------";
                String dashesForContentType = "------------------------";
                String endOfLine = "\r\n";
                // String endOfLine = "\n";
                String formData = "";
                for(int nameIndex = 0; nameIndex < formNames.size(); nameIndex++)
                {
                    formData += (dashes + boundary + endOfLine);
                    formData += ("Content-Disposition: form-data; name=\"" + formNames.get(nameIndex) + "\"" + endOfLine + endOfLine
                            + formValues.get(nameIndex) + endOfLine);
                }
                formData += (dashes + boundary + "--" + endOfLine);

                // mLogger.info("endpoint: " + endpoint);
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .version(HttpClient.Version.HTTP_1_1)
                        .timeout(Duration.ofSeconds(timeoutInSeconds));

                // mLogger.info("Content-Type: " + "multipart/form-data; boundary=" + dashesForContentType + boundary);
                requestBuilder.header("Content-Type", "multipart/form-data; boundary=" + dashesForContentType + boundary);

                // mLogger.info("Accept: */*");
                requestBuilder.header("Accept", "*/*");

                if (requestType.equalsIgnoreCase("POST"))
                    requestBuilder.POST(HttpRequest.BodyPublishers.ofString(formData, StandardCharsets.UTF_8));
                else // if (requestType.equalsIgnoreCase("GET"))
                    requestBuilder.method("GET", HttpRequest.BodyPublishers.ofString(formData, StandardCharsets.UTF_8));

                // mLogger.info("Body: " + formData);
                HttpRequest request = requestBuilder.build();

                body = getResponseBody(CLIENT, request, false);

                break; // exit from the retry loop
            } catch (Exception e) {
                String errorMessage = "HttpFeedFetcher. fetchFormDataHttpsJson"
                        + ", endpoint: " + endpoint
                        + ", Fatal transport error: " + e
                        + ", maxRequestNumber: " + maxRequestNumber
                        + ", retryIndex: " + (retryIndex - 1);
                mLogger.error(errorMessage);

                if (retryIndex >= maxRequestNumber)
                    throw e;
                else
                    Thread.sleep(100);  // half second
            }
        }

        return body;
    }

    static private String getResponseBody(HttpClient client, HttpRequest request, boolean outputToBeCompressed)
            throws Exception
    {
        String body = null;

        try {
            if (outputToBeCompressed)
            {
                HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

                int statusCode = response.statusCode();
                if (!(statusCode >= 200 && statusCode <= 210))
                {
                    String errorMessage = "HttpFeedFetcher. getResponseBody failed"
                            + ", statusCode: " + statusCode
                            ;
                    mLogger.error(errorMessage);

                    throw new Exception(errorMessage);
                }

                InputStream inputStream = response.body();

                {
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                    DeflateCompressorInputStream defIn = new DeflateCompressorInputStream(bufferedInputStream);

                    final StringBuilder outStr = new StringBuilder();

                    int buffersize = 1024 * 20;
                    final byte[] buffer = new byte[buffersize];
                    long uncompressedLength = 0;
                    int n = 0;
                    while (-1 != (n = defIn.read(buffer))) {
                        outStr.append(new String(buffer, 0, n));

                        uncompressedLength += n;
                    }
                    defIn.close();

                    body = outStr.toString();
                }
            }
            else
            {
                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());
                int statusCode = response.statusCode();
                if (!(statusCode >= 200 && statusCode <= 210))
                {
                    String errorMessage = "HttpFeedFetcher, getResponseBody failed"
                            + ", statusCode: " + statusCode
                            + ", body: " + response.body()
                            ;
                    mLogger.error(errorMessage);

                    throw new Exception(errorMessage);
                }

                body = response.body();
            }
        }
        catch (Exception e)
        {
            throw e;
        }

        return body;
    }
}