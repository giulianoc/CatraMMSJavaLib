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

	static public String getFacebookUserId(Properties configurationProperties, String userAccessToken)
	throws Exception
	{
		String userId;

        String facebookInfo;
        try
        {
            String facebookURL = configurationProperties.getProperty("facebook.userid.url")
				.replace("{user-access-token}", userAccessToken);

            mLogger.info("facebookURL: " + facebookURL);

			int timeoutInSeconds = 60;
			int maxRetriesNumber = 1;

            Date now = new Date();
            facebookInfo = HttpFeedFetcher.fetchGetHttpsJson(facebookURL, timeoutInSeconds, maxRetriesNumber,
                    null, null, null, false);
            mLogger.info("getFacebookConf. Elapsed (@" + facebookURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
        }
        catch (Exception e)
        {
            String errorMessage = "MMS API failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

        try
        {
			// {"id":"2231443363556360"}

            JSONObject joFacebookInfo = new JSONObject(facebookInfo);
            userId = joFacebookInfo.getString("id");
        }
        catch (Exception e)
        {
            String errorMessage = "Parsing facebookInfo failed. Exception: " + e;
            mLogger.error(errorMessage);

            throw new Exception(errorMessage);
        }

		return userId;
	}

	static public List<FacebookPage> getFacebookUserPagesList(Properties configurationProperties,
		String userId, String userAccessToken)
	throws Exception
	{
		List<FacebookPage> facebookPages = new ArrayList<>();

        String facebookInfo;
        try
        {
            String facebookURL = configurationProperties.getProperty("facebook.pages-list.url")
				.replace("{user-id}", userId)
				.replace("{user-access-token}", userAccessToken)
			;

            mLogger.info("facebookURL: " + facebookURL);

			int timeoutInSeconds = 60;
			int maxRetriesNumber = 1;

            Date now = new Date();
            facebookInfo = HttpFeedFetcher.fetchGetHttpsJson(facebookURL, timeoutInSeconds, maxRetriesNumber,
                    null, null, null, false);
            mLogger.info("getFacebookConf. Elapsed (@" + facebookURL + "@): @" + (new Date().getTime() - now.getTime()) + "@ millisecs.");
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
				"data":[                                                                                                  
					{                                                                                                     
						"access_token":"EAAad2ZC8dnYsBAFOdxQ7C11jfnc9lSL0LZCrRtFpeBK8tQAZApkHHtE6da17WFsUbNYgvA43L7QPkSa0t6gMNTOZBQdTUNjAZBkS8NlV8FLurWAv3jo8XqixxJIsI0YoaIJBALhguTanJEeVNzt3FnJMshRIUpdfxldgWbJRUkMiJQmgZBMVLo",
						"category":"Software",                                                                            
						"category_list":[{"id":"2211","name":"Software"}],                                                
						"name":"Test",                                                                                    
						"id":"581410318976233",                                                                           
						"tasks":["ANALYZE","ADVERTISE","MESSAGING","MODERATE","CREATE_CONTENT","MANAGE"]                  
					}                                                                                                     
				],                                                                                                        
				"paging":{"cursors":{"before":"NTgxNDEwMzE4OTc2MjMz","after":"NTgxNDEwMzE4OTc2MjMz"}}                     
			}                                                                                                             
			 */

            JSONObject joFacebookInfo = new JSONObject(facebookInfo);
			JSONArray jaData = joFacebookInfo.getJSONArray("data");
			for(int pageIndex = 0; pageIndex < jaData.length(); pageIndex++)
			{
				JSONObject joPage = jaData.getJSONObject(pageIndex);

				FacebookPage facebookPage = new FacebookPage();
				facebookPage.setId(joPage.getString("userId"));
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
