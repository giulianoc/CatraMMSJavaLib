package com.catrammslib.utility;

import java.io.Serializable;
import org.apache.log4j.Logger;

import org.json.JSONObject;

public class DrawTextDetails implements Serializable {
    
    private static final Logger mLogger = Logger.getLogger(DrawTextDetails.class);

    private String text;
    private String positionXInPixel;
    private String positionYInPixel;
    private String fontType;
    private Long fontSize;
    private String sFontSize; //it's String because I need taskFontSizesList as String
    private String fontColor;
    private Long textPercentageOpacity;
	private Long shadowX;
	private Long shadowY;
    private Boolean boxEnable;
    private String boxColor;
    private Long boxPercentageOpacity;

	public void DrawTextDetails()
	{
		setText("days_counter days hours_counter:mins_counter:secs_counter.cents_counter");

		setPositionXInPixel("(video_width-text_width)/2");
		setPositionYInPixel("(video_height-text_height)/2");

		setFontType("OpenSans-ExtraBold.ttf");

		setsFontSize("48");

		setFontColor("orange");

		setTextPercentageOpacity((long) 100);
		setShadowX((long) 0);
		setShadowY((long) 0);

		setBoxEnable(false);

		setBoxColor("black");

		setBoxPercentageOpacity((long) 20);
	}
	
	public JSONObject toJson()
	{
		JSONObject joOutput = new JSONObject();

		try
		{
			if (text != null && !text.isEmpty())
				joOutput.put("text", text);

			if (positionXInPixel != null && !positionXInPixel.isEmpty())
				joOutput.put("textPosition_X_InPixel", positionXInPixel);

			if (positionYInPixel != null && !positionYInPixel.isEmpty())
				joOutput.put("textPosition_Y_InPixel", positionYInPixel);

			if (fontType != null && !fontType.isEmpty())
				joOutput.put("fontType", fontType);

			if (fontSize != null)
				joOutput.put("fontSize", fontSize);

			if (fontColor != null && !fontColor.isEmpty())
				joOutput.put("fontColor", fontColor);

			if (textPercentageOpacity != null)
				joOutput.put("textPercentageOpacity", textPercentageOpacity);

			if (shadowX != null)
				joOutput.put("shadowX", shadowX);

			if (shadowY != null)
				joOutput.put("shadowY", shadowY);

			if (boxEnable != null)
				joOutput.put("boxEnable", boxEnable);

			if (boxColor != null && !boxColor.isEmpty())
				joOutput.put("boxColor", boxColor);

			if (boxPercentageOpacity != null)
				joOutput.put("boxPercentageOpacity", boxPercentageOpacity);
		}
		catch(Exception e)
		{
			mLogger.error("Exception: " + e);
		}
		
		return joOutput;
	}

	public void setData(JSONObject joDrawTextDetails)
	{
		try
		{
			if (joDrawTextDetails.has("text") && !joDrawTextDetails.isNull("text"))
				text = joDrawTextDetails.getString("text");
			else
				text = "";

			if (joDrawTextDetails.has("textPosition_X_InPixel") && !joDrawTextDetails.isNull("textPosition_X_InPixel"))
				positionXInPixel = joDrawTextDetails.getString("textPosition_X_InPixel");
			else
				positionXInPixel = "";

			if (joDrawTextDetails.has("textPosition_Y_InPixel") && !joDrawTextDetails.isNull("textPosition_Y_InPixel"))
				positionYInPixel = joDrawTextDetails.getString("textPosition_Y_InPixel");
			else
				positionYInPixel = "";

			if (joDrawTextDetails.has("fontType") && !joDrawTextDetails.isNull("fontType"))
				fontType = joDrawTextDetails.getString("fontType");
			else
				fontType = "";

			if (joDrawTextDetails.has("fontSize") && !joDrawTextDetails.isNull("fontSize"))
			{
				fontSize = joDrawTextDetails.getLong("fontSize");
				sFontSize = fontSize.toString();
			}
			else
			{
				fontSize = null;
				sFontSize = "";
			}

			if (joDrawTextDetails.has("fontColor") && !joDrawTextDetails.isNull("fontColor"))
				fontColor = joDrawTextDetails.getString("fontColor");
			else
				fontColor = "";

			if (joDrawTextDetails.has("textPercentageOpacity") && !joDrawTextDetails.isNull("textPercentageOpacity"))
				textPercentageOpacity = joDrawTextDetails.getLong("textPercentageOpacity");
			else
				textPercentageOpacity = null;

			if (joDrawTextDetails.has("shadowX") && !joDrawTextDetails.isNull("shadowX"))
				shadowX = joDrawTextDetails.getLong("shadowX");
			else
				shadowX = null;

			if (joDrawTextDetails.has("shadowY") && !joDrawTextDetails.isNull("shadowY"))
				shadowY = joDrawTextDetails.getLong("shadowY");
			else
				shadowY = null;

			if (joDrawTextDetails.has("boxEnable") && !joDrawTextDetails.isNull("boxEnable"))
				boxEnable = joDrawTextDetails.getBoolean("boxEnable");
			else
				boxEnable = null;

			if (joDrawTextDetails.has("boxColor") && !joDrawTextDetails.isNull("boxColor"))
				boxColor = joDrawTextDetails.getString("boxColor");
			else
				boxColor = "";

			if (joDrawTextDetails.has("boxPercentageOpacity") && !joDrawTextDetails.isNull("boxPercentageOpacity"))
				boxPercentageOpacity = joDrawTextDetails.getLong("boxPercentageOpacity");
			else
				boxPercentageOpacity = null;
		}
		catch(Exception e)
		{
			mLogger.error("Exception: " + e);
		}
	}

	public DrawTextDetails clone()
	{
		DrawTextDetails drawTextDetails = new DrawTextDetails();

		drawTextDetails.setText(text);
		drawTextDetails.setPositionXInPixel(positionXInPixel);
		drawTextDetails.setPositionYInPixel(positionYInPixel);
		drawTextDetails.setFontType(fontType);
		drawTextDetails.setFontSize(fontSize);
		drawTextDetails.setsFontSize(sFontSize);
		drawTextDetails.setFontColor(fontColor);
		drawTextDetails.setTextPercentageOpacity(textPercentageOpacity);
		drawTextDetails.setShadowX(shadowX);
		drawTextDetails.setShadowY(shadowY);
		drawTextDetails.setBoxEnable(boxEnable);
		drawTextDetails.setBoxColor(boxColor);
		drawTextDetails.setBoxPercentageOpacity(boxPercentageOpacity);

		return drawTextDetails;
	}

	public void setsFontSize(String sFontSize) 
	{
		this.sFontSize = sFontSize;
		try
		{
			fontSize = Long.parseLong(sFontSize);
		}
		catch(Exception e)
		{

		}
	}

	public String getsFontSize() {
		return sFontSize;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
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

	public String getFontType() {
		return fontType;
	}

	public void setFontType(String fontType) {
		this.fontType = fontType;
	}


	public Long getFontSize() {
		return fontSize;
	}

	public void setFontSize(Long fontSize) {
		this.fontSize = fontSize;
	}

	public String getFontColor() {
		return fontColor;
	}

	public void setFontColor(String fontColor) {
		this.fontColor = fontColor;
	}

	public Long getTextPercentageOpacity() {
		return textPercentageOpacity;
	}

	public void setTextPercentageOpacity(Long textPercentageOpacity) {
		this.textPercentageOpacity = textPercentageOpacity;
	}

	public Long getShadowX() {
		return shadowX;
	}

	public void setShadowX(Long shadowX) {
		this.shadowX = shadowX;
	}

	public Long getShadowY() {
		return shadowY;
	}

	public void setShadowY(Long shadowY) {
		this.shadowY = shadowY;
	}

	public Boolean getBoxEnable() {
		return boxEnable;
	}

	public void setBoxEnable(Boolean boxEnable) {
		this.boxEnable = boxEnable;
	}

	public String getBoxColor() {
		return boxColor;
	}

	public void setBoxColor(String boxColor) {
		this.boxColor = boxColor;
	}

	public Long getBoxPercentageOpacity() {
		return boxPercentageOpacity;
	}

	public void setBoxPercentageOpacity(Long boxPercentageOpacity) {
		this.boxPercentageOpacity = boxPercentageOpacity;
	}

	
}
