package com.catrammslib.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.catrammslib.helper.entity.FacebookPage;
import com.catrammslib.utility.HttpFeedFetcher;

public class CatraMMSSocial {

    private static final Logger mLogger = Logger.getLogger(CatraMMSSocial.class);

	static public List<FacebookPage> getFacebookUserPagesList(Properties configurationProperties,
		String userAccessToken)
	throws Exception
	{
		List<FacebookPage> facebookPages = new ArrayList<>();

        String facebookInfo;
        try
        {
            // https://developers.facebook.com/docs/pages/managing
            String facebookURL = configurationProperties.getProperty("facebook.pages-list.url")
				.replace("{user-access-token}", userAccessToken)
			;

            mLogger.info("facebookURL: " + facebookURL);

			int timeoutInSeconds = 60;
			int maxRetriesNumber = 1;

            long start = System.currentTimeMillis();
            facebookInfo = HttpFeedFetcher.GET(facebookURL, timeoutInSeconds, maxRetriesNumber,
                    null, null, null, false);
            mLogger.info("getFacebookConf. Elapsed (@" + facebookURL + "@): @" + (System.currentTimeMillis() - start) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
			/*
			{
              "data": [
                {
                  "id": "581410318976233",
                  "name": "Test"
                }
              ],
              "paging": {
                "cursors": {
                  "before": "QVFIUk9OMWFuaFRTVERYVTJQOXlkVHhIb19YeFdTWHZA2OFhDZAUp4VEVBaGRfa2Y5Ulo5bGphMEJvajI0MHg3V3pmQl8tZADRadXl0LXRjZAzFWMDhUODlrSUVB",
                  "after": "QVFIUk9OMWFuaFRTVERYVTJQOXlkVHhIb19YeFdTWHZA2OFhDZAUp4VEVBaGRfa2Y5Ulo5bGphMEJvajI0MHg3V3pmQl8tZADRadXl0LXRjZAzFWMDhUODlrSUVB"
                }
              }
            }
			 */

            JSONObject joFacebookInfo = new JSONObject(facebookInfo);
			JSONArray jaData = joFacebookInfo.getJSONArray("data");
			for(int pageIndex = 0; pageIndex < jaData.length(); pageIndex++)
			{
				JSONObject joPage = jaData.getJSONObject(pageIndex);

				FacebookPage facebookPage = new FacebookPage();
				facebookPage.setId(joPage.getString("id"));
				facebookPage.setName(joPage.getString("name"));
				facebookPages.add(facebookPage);
			}
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing facebookInfo failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

		return facebookPages;
	}
}
