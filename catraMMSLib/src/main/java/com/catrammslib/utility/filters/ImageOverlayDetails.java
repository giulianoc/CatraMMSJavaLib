package com.catrammslib.utility.filters;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class ImageOverlayDetails implements Serializable {

    private static final Logger mLogger = LoggerFactory.getLogger(ImageOverlayDetails.class);

	private Long imagePhysicalPathKey;
    private String positionXInPixel;
    private String positionYInPixel;

	public ImageOverlayDetails()
	{
		imagePhysicalPathKey = null;
		setPositionXInPixel("(video_width-image_width)/2");
		setPositionYInPixel("(video_height-image_height)/2");
	}
	
	public JSONObject toJson()
	{
		JSONObject joOutput = new JSONObject();

		try
		{
			joOutput.put("type", "imageoverlay");

			if (imagePhysicalPathKey != null)
				joOutput.put("imagePhysicalPathKey", imagePhysicalPathKey);

			if (positionXInPixel != null && !positionXInPixel.isEmpty())
				joOutput.put("imagePosition_X_InPixel", positionXInPixel);

			if (positionYInPixel != null && !positionYInPixel.isEmpty())
				joOutput.put("imagePosition_Y_InPixel", positionYInPixel);
		}
		catch(Exception e)
		{
			mLogger.error("Exception: " + e);
		}
		
		return joOutput;
	}

	public void fromJson(JSONObject joDetails)
	{
		try
		{
			if (joDetails.has("imagePhysicalPathKey") && !joDetails.isNull("imagePhysicalPathKey"))
				imagePhysicalPathKey = joDetails.getLong("imagePhysicalPathKey");
			else
				imagePhysicalPathKey = null;

			if (joDetails.has("imagePosition_X_InPixel") && !joDetails.isNull("imagePosition_X_InPixel"))
				positionXInPixel = joDetails.getString("imagePosition_X_InPixel");
			else
				positionXInPixel = "";

			if (joDetails.has("imagePosition_Y_InPixel") && !joDetails.isNull("imagePosition_Y_InPixel"))
				positionYInPixel = joDetails.getString("imagePosition_Y_InPixel");
			else
				positionYInPixel = "";
		}
		catch(Exception e)
		{
			mLogger.error("Exception: " + e);
		}
	}

	public ImageOverlayDetails clone()
	{
		ImageOverlayDetails imageOverlayDetails = new ImageOverlayDetails();

		imageOverlayDetails.setImagePhysicalPathKey(imagePhysicalPathKey);
		imageOverlayDetails.setPositionXInPixel(positionXInPixel);
		imageOverlayDetails.setPositionYInPixel(positionYInPixel);

		return imageOverlayDetails;
	}

	public Long getImagePhysicalPathKey() {
		return imagePhysicalPathKey;
	}

	public void setImagePhysicalPathKey(Long imagePhysicalPathKey) {
		this.imagePhysicalPathKey = imagePhysicalPathKey;
	}

	public String getPositionXInPixel() {
		return positionXInPixel;
	}

	public void setPositionXInPixel(String positionXInPixel) {
		this.positionXInPixel = positionXInPixel;
	}

	public String getPositionYInPixel() {
		return positionYInPixel;
	}

	public void setPositionYInPixel(String positionYInPixel) {
		this.positionYInPixel = positionYInPixel;
	}

}
