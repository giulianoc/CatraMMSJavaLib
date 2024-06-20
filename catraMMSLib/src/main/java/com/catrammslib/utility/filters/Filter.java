package com.catrammslib.utility.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class Filter implements Serializable {

    private static final Logger mLogger = LoggerFactory.getLogger(Filter.class);

	private String filterName;


	private Long timeInSecondsDecimalsPrecision;

	private Double audioVolumeChange;


	private Float blackdetect_BlackMinDuration;
	private Float blackdetect_PixelBlackTh;


	private Long blackframe_Amount;
	private Long blackframe_Threshold;


	private String crop_OutputWidth;
	private String crop_OutputHeight;
	private String crop_X;
	private String crop_Y;
	private Boolean crop_KeepAspect;
	private Boolean crop_Exact;


	private String drawBox_X;
	private String drawBox_Y;
	private String drawBox_Width;
	private String drawBox_Height;
	private String drawBox_FontColor;
	private Long drawBox_PercentageOpacity;
	private String drawBox_Thickness;


	private DrawTextDetails drawTextDetails;


	private ImageOverlayDetails imageOverlayDetails;


	private Long fade_Duration;


	private Long freezedetect_Duration;
	private Long freezedetect_NoiseInDb;


	private Float silencedetect_Noise;


	public Filter()
	{
		this.drawTextDetails = new DrawTextDetails(null);
		this.imageOverlayDetails = new ImageOverlayDetails();

		timeInSecondsDecimalsPrecision = (long) 1;

		reset();
	}

	private void reset()
	{
		filterName = null;

		audioVolumeChange = null;

		blackdetect_BlackMinDuration = null;
		blackdetect_PixelBlackTh = null;

		blackframe_Amount = null;
		blackframe_Threshold = null;

		crop_OutputWidth = "in_w";
		crop_OutputHeight = "in_h";
		crop_X = "(in_w-out_w)/2";
		crop_Y = "(in_h-out_h)/2";
		crop_KeepAspect = false;
		crop_Exact = false;

		drawBox_X = "0";
		drawBox_Y = "0";
		drawBox_Width = "in_w";
		drawBox_Height = "in_h";
		drawBox_FontColor = "black";
		drawBox_PercentageOpacity = 100L;
		drawBox_Thickness = "fill";

		fade_Duration = null;

		freezedetect_Duration = null;
		freezedetect_NoiseInDb = null;

		silencedetect_Noise = null;
	}

	public Filter clone()
	{
		Filter filter = new Filter();

		filter.setFilterName(getFilterName());

		filter.setAudioVolumeChange(getAudioVolumeChange());

		filter.setBlackdetect_BlackMinDuration(getBlackdetect_BlackMinDuration());
		filter.setBlackdetect_PixelBlackTh(getBlackdetect_PixelBlackTh());

		filter.setBlackframe_Amount(getBlackframe_Amount());
		filter.setBlackframe_Threshold(getBlackframe_Threshold());

		filter.setCrop_OutputWidth(getCrop_OutputWidth());
		filter.setCrop_OutputHeight(getCrop_OutputHeight());
		filter.setCrop_X(getCrop_X());
		filter.setCrop_Y(getCrop_Y());
		filter.setCrop_KeepAspect(getCrop_KeepAspect());
		filter.setCrop_Exact(getCrop_Exact());

		filter.setDrawBox_X(getDrawBox_X());
		filter.setDrawBox_Y(getDrawBox_Y());
		filter.setDrawBox_Width(getDrawBox_Width());
		filter.setDrawBox_Height(getDrawBox_Height());
		filter.setDrawBox_FontColor(getDrawBox_FontColor());
		filter.setDrawBox_PercentageOpacity(getDrawBox_PercentageOpacity());
		filter.setDrawBox_Thickness(getDrawBox_Thickness());

		filter.setDrawTextDetails(getDrawTextDetails().clone());

		filter.setImageOverlayDetails(getImageOverlayDetails().clone());

		filter.setFade_Duration(getFade_Duration());

		filter.setFreezedetect_Duration(getFreezedetect_Duration());
		filter.setFreezedetect_NoiseInDb(getFreezedetect_NoiseInDb());

		filter.setSilencedetect_Noise(getSilencedetect_Noise());

		return filter;
	}

	/*
	public void fromJson(JSONObject joFilters)
	{
		try
		{
			reset();

			if (joFilters.has("video"))
			{
				JSONArray jaVideo = joFilters.getJSONArray("video");

				for (int filterIndex = 0; filterIndex < jaVideo.length(); filterIndex++)
				{
					JSONObject joFilter = jaVideo.getJSONObject(filterIndex);

					if (joFilter.has("type") && joFilter.getString("type").equalsIgnoreCase("blackdetect"))
						setBlackdetect(true);

					if (joFilter.has("type") && joFilter.getString("type").equalsIgnoreCase("blackframe"))
						setBlackframe(true);

					if (joFilter.has("type") && joFilter.getString("type").equalsIgnoreCase("crop"))
					{
						setCrop(true);

						if (joFilter.has("out_w"))
							setCrop_OutputWidth(joFilter.getString("out_w"));
						if (joFilter.has("out_h"))
							setCrop_OutputHeight(joFilter.getString("out_h"));
						if (joFilter.has("x"))
							setCrop_X(joFilter.getString("x"));
						if (joFilter.has("y"))
							setCrop_Y(joFilter.getString("y"));
						if (joFilter.has("keep_aspect"))
							setCrop_KeepAspect(joFilter.getBoolean("keep_aspect"));
						if (joFilter.has("exact"))
							setCrop_Exact(joFilter.getBoolean("exact"));
					}

					if (joFilter.has("type") && joFilter.getString("type").equalsIgnoreCase("drawbox"))
					{
						setDrawBox(true);

						if (joFilter.has("x"))
							setDrawBox_X(joFilter.getString("x"));
						if (joFilter.has("y"))
							setDrawBox_Y(joFilter.getString("y"));
						if (joFilter.has("width"))
							setDrawBox_Width(joFilter.getString("width"));
						if (joFilter.has("height"))
							setDrawBox_Height(joFilter.getString("height"));
						if (joFilter.has("fontColor"))
							setDrawBox_FontColor(joFilter.getString("fontColor"));
						if (joFilter.has("percentageOpacity"))
							setDrawBox_PercentageOpacity(joFilter.getLong("percentageOpacity"));
						if (joFilter.has("thickness"))
							setDrawBox_Thickness(joFilter.getString("thickness"));
					}

					if (joFilter.has("type") && joFilter.getString("type").equalsIgnoreCase("drawtext"))
					{
						setDrawText(true);

						getDrawTextDetails().fromJson(joFilter);
					}

					if (joFilter.has("type") && joFilter.getString("type").equalsIgnoreCase("fade"))
					{
						setFade(true);

						if (joFilter.has("duration"))
						{
							Object o = joFilter.get("duration");
							setFade_Duration(joFilter.getLong("duration"));
						}
					}

					if (joFilter.has("type") && joFilter.getString("type").equalsIgnoreCase("freezedetect"))
					{
						setFreezedetect(true);

						if (joFilter.has("duration"))
						{
							Object o = joFilter.get("duration");
								setFreezedetect_Duration(joFilter.getLong("duration"));
						}
					}
				}
			}

			if (joFilters.has("audio"))
			{
				JSONArray jaAudio = joFilters.getJSONArray("audio");

				for (int filterIndex = 0; filterIndex < jaAudio.length(); filterIndex++)
				{
					JSONObject joFilter = jaAudio.getJSONObject(filterIndex);

					if (joFilter.has("type") && joFilter.getString("type").equalsIgnoreCase("volume"))
					{
						if (joFilter.has("factor") && !joFilter.isNull("factor"))
							setAudioVolumeChange(joFilter.getDouble("factor"));
					}
					else if (joFilter.has("type") && joFilter.getString("type").equalsIgnoreCase("silencedetect"))
					{
						setSilencedetect(true);
					}
				}
			}

			if (joFilters.has("complex"))
			{
				JSONArray jaComplex = joFilters.getJSONArray("complex");

				for (int filterIndex = 0; filterIndex < jaComplex.length(); filterIndex++)
				{
					JSONObject joFilter = jaComplex.getJSONObject(filterIndex);

					if (joFilter.has("type") && joFilter.getString("type").equalsIgnoreCase("imageoverlay"))
					{
						setImageOverlay(true);

						getImageOverlayDetails().fromJson(joFilter);
					}
				}
			}
		}
		catch(Exception e)
		{
			mLogger.error("Exception: " + e);
		}
	}

	public JSONObject toJson()
	{
		// JSONObject joFilters = null;
		JSONObject joFilters = new JSONObject();

		try
		{
			boolean videoFilterPresent = false;
			JSONArray jaVideo = new JSONArray();

			boolean audioFilterPresent = false;
			JSONArray jaAudio = new JSONArray();

			boolean complexFilterPresent = false;
			JSONArray jaComplex = new JSONArray();

			// video filters
			if (getBlackdetect() != null && getBlackdetect())
			{
				videoFilterPresent = true;

				JSONObject joBlackDetect = new JSONObject();
				jaVideo.put(joBlackDetect);

				joBlackDetect.put("type", "blackdetect");
				if (getBlackdetect_BlackMinDuration() != null)
					joBlackDetect.put("black_min_duration", getBlackdetect_BlackMinDuration());
				if (getBlackdetect_PixelBlackTh() != null)
					joBlackDetect.put("pixel_black_th", getBlackdetect_PixelBlackTh());
			}

			if (getBlackframe() != null && getBlackframe())
			{
				videoFilterPresent = true;

				JSONObject joBlackFrame = new JSONObject();
				jaVideo.put(joBlackFrame);

				joBlackFrame.put("type", "blackframe");
				if (getBlackframe_Amount() != null)
					joBlackFrame.put("amount", getBlackframe_Amount());
				if (getBlackframe_Threshold() != null)
					joBlackFrame.put("threshold", getBlackframe_Threshold());
			}

			if (getCrop() != null && getCrop())
			{
				videoFilterPresent = true;

				JSONObject joCrop = new JSONObject();
				jaVideo.put(joCrop);

				joCrop.put("type", "crop");

				if (getCrop_OutputWidth() != null)
					joCrop.put("out_w", getCrop_OutputWidth());
				if (getCrop_OutputHeight() != null)
					joCrop.put("out_h", getCrop_OutputHeight());
				if (getCrop_X() != null)
					joCrop.put("x", getCrop_X());
				if (getCrop_Y() != null)
					joCrop.put("y", getCrop_Y());
				if (getCrop_KeepAspect() != null)
					joCrop.put("keep_aspect", getCrop_KeepAspect());
				if (getCrop_Exact() != null)
					joCrop.put("exact", getCrop_Exact());
			}

			if (getDrawBox() != null && getDrawBox())
			{
				videoFilterPresent = true;

				JSONObject joDrawBox = new JSONObject();
				jaVideo.put(joDrawBox);

				joDrawBox.put("type", "drawbox");

				if (getDrawBox_X() != null)
					joDrawBox.put("x", getDrawBox_X());
				if (getDrawBox_Y() != null)
					joDrawBox.put("y", getDrawBox_Y());
				if (getDrawBox_Width() != null)
					joDrawBox.put("width", getDrawBox_Width());
				if (getDrawBox_Height() != null)
					joDrawBox.put("height", getDrawBox_Height());
				if (getDrawBox_FontColor() != null)
					joDrawBox.put("fontColor", getDrawBox_FontColor());
				if (getDrawBox_PercentageOpacity() != null)
					joDrawBox.put("percentageOpacity", getDrawBox_PercentageOpacity());
				if (getDrawBox_Thickness() != null)
					joDrawBox.put("thickness", getDrawBox_Thickness());
			}

			if (getDrawText() != null && getDrawText()) {
				videoFilterPresent = true;

				jaVideo.put(getDrawTextDetails().toJson());
			}

			if (getFade() != null && getFade())
			{
				videoFilterPresent = true;

				JSONObject joFade = new JSONObject();
				jaVideo.put(joFade);

				joFade.put("type", "fade");

				if (getFade_Duration() != null && getFade_Duration() > 0)
					joFade.put("duration", getFade_Duration());
			}

			if (getFreezedetect() != null && getFreezedetect())
			{
				videoFilterPresent = true;

				JSONObject joFreezeDetect = new JSONObject();
				jaVideo.put(joFreezeDetect);

				joFreezeDetect.put("type", "freezedetect");
				if (getFreezedetect_Duration() != null && getFreezedetect_Duration() > 0)
					joFreezeDetect.put("duration", getFreezedetect_Duration());
				if (getFreezedetect_NoiseInDb() != null)
					joFreezeDetect.put("noiseInDb", getFreezedetect_NoiseInDb());
			}

			// audio filters
			if (getSilencedetect() != null && getSilencedetect())
			{
				audioFilterPresent = true;

				JSONObject joSilenceDetect = new JSONObject();
				jaAudio.put(joSilenceDetect);

				joSilenceDetect.put("type", "silencedetect");
				if (getSilencedetect_Noise() != null)
					joSilenceDetect.put("noise", getSilencedetect_Noise());
			}

			if (getAudioVolumeChange() != null)
			{
				audioFilterPresent = true;

				JSONObject joVolume = new JSONObject();
				jaAudio.put(joVolume);

				joVolume.put("type", "volume");
				joVolume.put("factor", getAudioVolumeChange());
			}

			// complex filter
			if (getImageOverlay() != null && getImageOverlay()) {
				complexFilterPresent = true;

				jaComplex.put(getImageOverlayDetails().toJson());
			}

			// build the filters json
			if (videoFilterPresent || audioFilterPresent || complexFilterPresent)
			{
				// joFilters = new JSONObject();

				if (videoFilterPresent)
					joFilters.put("video", jaVideo);
				if (audioFilterPresent)
					joFilters.put("audio", jaAudio);
				if (complexFilterPresent)
					joFilters.put("complex", jaComplex);
			}
		}
		catch(Exception e)
		{
			mLogger.error("Exception: " + e);
		}

		return joFilters;
	}
	 */

	public Double getAudioVolumeChange() {
		return audioVolumeChange;
	}

	public void setAudioVolumeChange(Double audioVolumeChange) {
		this.audioVolumeChange = audioVolumeChange;
	}

	public DrawTextDetails getDrawTextDetails() {
		return drawTextDetails;
	}

	public void setDrawTextDetails(DrawTextDetails drawTextDetails) {
		this.drawTextDetails = drawTextDetails;
	}


	public Float getBlackdetect_BlackMinDuration() {
		return blackdetect_BlackMinDuration;
	}

	public void setBlackdetect_BlackMinDuration(Float blackdetect_BlackMinDuration) {
		this.blackdetect_BlackMinDuration = blackdetect_BlackMinDuration;
	}

	public Float getBlackdetect_PixelBlackTh() {
		return blackdetect_PixelBlackTh;
	}

	public void setBlackdetect_PixelBlackTh(Float blackdetect_PixelBlackTh) {
		this.blackdetect_PixelBlackTh = blackdetect_PixelBlackTh;
	}

	public Long getFreezedetect_Duration() {
		return freezedetect_Duration;
	}

	public void setFreezedetect_Duration(Long freezedetect_Duration) {
		this.freezedetect_Duration = freezedetect_Duration;
	}

	public Long getFade_Duration() {
		return fade_Duration;
	}

	public void setFade_Duration(Long fade_Duration) {
		this.fade_Duration = fade_Duration;
	}

	public Long getBlackframe_Amount() {
		return blackframe_Amount;
	}

	public void setBlackframe_Amount(Long blackframe_Amount) {
		this.blackframe_Amount = blackframe_Amount;
	}

	public Long getBlackframe_Threshold() {
		return blackframe_Threshold;
	}

	public void setBlackframe_Threshold(Long blackframe_Threshold) {
		this.blackframe_Threshold = blackframe_Threshold;
	}

	public Long getFreezedetect_NoiseInDb() {
		return freezedetect_NoiseInDb;
	}

	public void setFreezedetect_NoiseInDb(Long freezedetect_NoiseInDb) {
		this.freezedetect_NoiseInDb = freezedetect_NoiseInDb;
	}

	public Long getTimeInSecondsDecimalsPrecision() {
		return timeInSecondsDecimalsPrecision;
	}

	public void setTimeInSecondsDecimalsPrecision(Long timeInSecondsDecimalsPrecision) {
		this.timeInSecondsDecimalsPrecision = timeInSecondsDecimalsPrecision;
	}


	public String getCrop_OutputWidth() {
		return crop_OutputWidth;
	}

	public void setCrop_OutputWidth(String crop_OutputWidth) {
		this.crop_OutputWidth = crop_OutputWidth;
	}

	public String getCrop_OutputHeight() {
		return crop_OutputHeight;
	}

	public void setCrop_OutputHeight(String crop_OutputHeight) {
		this.crop_OutputHeight = crop_OutputHeight;
	}

	public String getCrop_X() {
		return crop_X;
	}

	public void setCrop_X(String crop_X) {
		this.crop_X = crop_X;
	}

	public String getCrop_Y() {
		return crop_Y;
	}

	public void setCrop_Y(String crop_Y) {
		this.crop_Y = crop_Y;
	}

	public Boolean getCrop_KeepAspect() {
		return crop_KeepAspect;
	}

	public void setCrop_KeepAspect(Boolean crop_KeepAspect) {
		this.crop_KeepAspect = crop_KeepAspect;
	}

	public Boolean getCrop_Exact() {
		return crop_Exact;
	}

	public void setCrop_Exact(Boolean crop_Exact) {
		this.crop_Exact = crop_Exact;
	}


	public String getDrawBox_X() {
		return drawBox_X;
	}

	public void setDrawBox_X(String drawBox_X) {
		this.drawBox_X = drawBox_X;
	}

	public String getDrawBox_Y() {
		return drawBox_Y;
	}

	public void setDrawBox_Y(String drawBox_Y) {
		this.drawBox_Y = drawBox_Y;
	}

	public String getDrawBox_Width() {
		return drawBox_Width;
	}

	public void setDrawBox_Width(String drawBox_Width) {
		this.drawBox_Width = drawBox_Width;
	}

	public String getDrawBox_Height() {
		return drawBox_Height;
	}

	public void setDrawBox_Height(String drawBox_Height) {
		this.drawBox_Height = drawBox_Height;
	}

	public String getDrawBox_FontColor() {
		return drawBox_FontColor;
	}

	public void setDrawBox_FontColor(String drawBox_FontColor) {
		this.drawBox_FontColor = drawBox_FontColor;
	}

	public Long getDrawBox_PercentageOpacity() {
		return drawBox_PercentageOpacity;
	}

	public void setDrawBox_PercentageOpacity(Long drawBox_PercentageOpacity) {
		this.drawBox_PercentageOpacity = drawBox_PercentageOpacity;
	}

	public String getDrawBox_Thickness() {
		return drawBox_Thickness;
	}

	public void setDrawBox_Thickness(String drawBox_Thickness) {
		this.drawBox_Thickness = drawBox_Thickness;
	}


	public ImageOverlayDetails getImageOverlayDetails() {
		return imageOverlayDetails;
	}

	public void setImageOverlayDetails(ImageOverlayDetails imageOverlayDetails) {
		this.imageOverlayDetails = imageOverlayDetails;
	}

	public Float getSilencedetect_Noise() {
		return silencedetect_Noise;
	}

	public void setSilencedetect_Noise(Float silencedetect_Noise) {
		this.silencedetect_Noise = silencedetect_Noise;
	}

	public String getFilterName() {
		return filterName;
	}

	public void setFilterName(String filterName) {
		this.filterName = filterName;
	}
}
