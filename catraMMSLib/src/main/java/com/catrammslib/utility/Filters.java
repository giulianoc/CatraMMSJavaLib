package com.catrammslib.utility;

import com.catrammslib.entity.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

public class Filters implements Serializable {

    private static final Logger mLogger = LoggerFactory.getLogger(Filters.class);

	private Long timeInSecondsDecimalsPrecision;

	// drawTextEnable serve per la GUI (altrimenti sarebbe bastato il controllo (drawTextDetails != null)
	private Boolean drawTextEnable;
	private DrawTextDetails drawTextDetails;

	private Double audioVolumeChange;

	private Boolean blackdetect;
	private Float blackdetect_BlackMinDuration;
	private Float blackdetect_PixelBlackTh;

	private Boolean blackframe;
	private Long blackframe_Amount;
	private Long blackframe_Threshold;

	private Boolean freezedetect;
	private Long freezedetect_Duration;
	private Long freezedetect_NoiseInDb;

	private Boolean silencedetect;
	private Float silencedetect_Noise;

	private Boolean fade;
	private Long fade_Duration;

	public Filters(boolean drawTextEnable, DrawTextDetails drawTextDetails)
	{
		this.drawTextEnable = drawTextEnable;
		this.drawTextDetails = drawTextDetails;

		timeInSecondsDecimalsPrecision = (long) 1;

		reset();
	}

	private void reset()
	{
		audioVolumeChange = null;

		blackdetect = false;
		blackdetect_BlackMinDuration = null;
		blackdetect_PixelBlackTh = null;

		blackframe = false;
		blackframe_Amount = null;
		blackframe_Threshold = null;

		freezedetect = false;
		freezedetect_Duration = null;
		freezedetect_NoiseInDb = null;

		silencedetect = false;
		silencedetect_Noise = null;

		fade = false;
		fade_Duration = null;
	}

	public Filters clone()
	{
		Filters filters = new Filters(getDrawTextEnable(), getDrawTextDetails());

		filters.setAudioVolumeChange(getAudioVolumeChange());

		filters.setBlackdetect(getBlackdetect());
		filters.setBlackdetect_BlackMinDuration(getBlackdetect_BlackMinDuration());
		filters.setBlackdetect_PixelBlackTh(getBlackdetect_PixelBlackTh());

		filters.setBlackframe(getBlackframe());
		filters.setBlackframe_Amount(getBlackframe_Amount());
		filters.setBlackframe_Threshold(getBlackframe_Threshold());

		filters.setFreezedetect(getFreezedetect());
		filters.setFreezedetect_Duration(getFreezedetect_Duration());
		filters.setFreezedetect_NoiseInDb(getFreezedetect_NoiseInDb());

		filters.setSilencedetect(getSilencedetect());
		filters.setSilencedetect_Noise(getSilencedetect_Noise());

		filters.setFade(getFade());
		filters.setFade_Duration(getFade_Duration());

		return filters;
	}

	public void filtersFromJson(JSONObject joFilters)
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

					if (joFilter.has("type") && joFilter.getString("type").equalsIgnoreCase("freezedetect"))
					{
						setFreezedetect(true);

						if (joFilter.has("duration"))
						{
							Object o = joFilter.get("duration");
								setFreezedetect_Duration(joFilter.getLong("duration"));
						}
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
		}
		catch(Exception e)
		{
			mLogger.error("Exception: " + e);
		}
	}

	public JSONObject toJson()
	{
		JSONObject joFilters = null;

		try
		{
			boolean videoFilterPresent = false;
			JSONArray jaVideo = new JSONArray();

			boolean audioFilterPresent = false;
			JSONArray jaAudio = new JSONArray();

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

			if (getFade() != null && getFade())
			{
				videoFilterPresent = true;

				JSONObject joFade = new JSONObject();
				jaVideo.put(joFade);

				joFade.put("type", "fade");

				if (getFade_Duration() != null && getFade_Duration() > 0)
					joFade.put("duration", getFade_Duration());
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

			if (videoFilterPresent || audioFilterPresent)
			{
				joFilters = new JSONObject();

				if (videoFilterPresent)
					joFilters.put("video", jaVideo);
				if (audioFilterPresent)
					joFilters.put("audio", jaAudio);
			}
		}
		catch(Exception e)
		{
			mLogger.error("Exception: " + e);
		}

		return joFilters;
	}

	public void fromJson(JSONObject joOutputStream,
						 List<EncodingProfile> encodingProfileList,
						 List<HLSChannelConf> hlsChannelList,
						 List<RTMPChannelConf> rtmpChannelList,
						 List<CDN77ChannelConf> cdn77ChannelList,
						 List<AWSChannelConf> awsChannelList
	)
	{
		try
		{
			if (joOutputStream.has("filters"))
			{
				JSONObject joFilters = joOutputStream.getJSONObject("filters");
				filtersFromJson(joFilters);
			}

			{
				if (joOutputStream.has("drawTextDetails"))
				{
					setDrawTextEnable(true);

					JSONObject joDrawTextDetails = joOutputStream.getJSONObject("drawTextDetails");

					getDrawTextDetails().fromJson(joDrawTextDetails);
				}
				else
				{
					setDrawTextEnable(false);
				}
			}
		}
		catch(Exception e)
		{
			mLogger.error("Exception: " + e);
		}
	}

	public Double getAudioVolumeChange() {
		return audioVolumeChange;
	}

	public void setAudioVolumeChange(Double audioVolumeChange) {
		this.audioVolumeChange = audioVolumeChange;
	}

	public Boolean getBlackdetect() {
		return blackdetect;
	}

	public void setBlackdetect(Boolean blackdetect) {
		this.blackdetect = blackdetect;
	}

	public Boolean getBlackframe() {
		return blackframe;
	}

	public void setBlackframe(Boolean blackframe) {
		this.blackframe = blackframe;
	}

	public Boolean getFreezedetect() {
		return freezedetect;
	}

	public void setFreezedetect(Boolean freezedetect) {
		this.freezedetect = freezedetect;
	}

	public Boolean getSilencedetect() {
		return silencedetect;
	}

	public void setSilencedetect(Boolean silencedetect) {
		this.silencedetect = silencedetect;
	}

	public Boolean getDrawTextEnable() {
		return drawTextEnable;
	}

	public void setDrawTextEnable(Boolean drawTextEnable) {
		this.drawTextEnable = drawTextEnable;
	}

	public DrawTextDetails getDrawTextDetails() {
		return drawTextDetails;
	}

	public void setDrawTextDetails(DrawTextDetails drawTextDetails) {
		this.drawTextDetails = drawTextDetails;
	}

	public Boolean getFade() {
		return fade;
	}

	public void setFade(Boolean fade) {
		this.fade = fade;
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

	public Float getSilencedetect_Noise() {
		return silencedetect_Noise;
	}

	public void setSilencedetect_Noise(Float silencedetect_Noise) {
		this.silencedetect_Noise = silencedetect_Noise;
	}
}
