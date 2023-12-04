package com.catrammslib.utility;

import java.io.Serializable;
import java.util.List;

import com.catrammslib.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.JSONArray;
import org.json.JSONObject;

public class OutputStream implements Serializable {
    
    private static final Logger mLogger = LoggerFactory.getLogger(OutputStream.class);

	// default: nessuna indicazione, ffmpeg seleziona una traccia video in base a https://ffmpeg.org/ffmpeg.html#Automatic-stream-selection
	// all video tracks (0:v)
	// first video track (0:v:0)
	// second video track (0:v:1)
	// third video track (0:v:2)
	private String videoMap;

	// default: nessuna indicazione, ffmpeg seleziona una traccia audio in base a https://ffmpeg.org/ffmpeg.html#Automatic-stream-selection
	// all audio tracks (0:a)
	// first audio track (0:a:0)
	// second audio track (0:a:1)
	// third audio track (0:a:2)
	private String audioMap;

	// RTMP_Channel, HLS_Channel, CDN_AWS, CDN_CDN77, HLS, UDP_Stream
	private String outputType;

	// UDP_Stream
    private String udpURL;

	// CDN_AWS
	private AWSChannelConf awsChannel;	// to be started/stopped
	// CDN_AWS
	private Boolean awsSignedURL;
	// CDN_AWS
	private Long awsExpirationInMinutes;

	// CDN_CDN77
	private CDN77ChannelConf cdn77Channel;
	// CDN_CDN77
	private Long cdn77ExpirationInMinutes;

	// RTMP_Channel
	private RTMPChannelConf rtmpChannel;

	// HLS_Channel
	private HLSChannelConf hlsChannel;

	// drawTextEnable serve per la GUI (altrimenti sarebbe bastato il controllo (drawTextDetails != null)
	private Boolean drawTextEnable;
	private DrawTextDetails drawTextDetails;

	// HLS
	private String otherOutputOptions;

	// 2023-09-26: serve la new in fase di instanziazione della classe altrimenti avrei una eccezione in outputStream.xhtml
	private EncodingProfile encodingProfile = new EncodingProfile();
	// In alcuni casi abbiamo solamente encodingProfileLabel e non l'EncodingProfile, per cui lasciamo la doppia opzione
    private String encodingProfileLabel;

	// filters
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

	public OutputStream(boolean drawTextEnable, DrawTextDetails drawTextDetails)
	{
		this.drawTextEnable = drawTextEnable;
		this.drawTextDetails = drawTextDetails;

		awsSignedURL = false;
		awsExpirationInMinutes = (long) 1440;	// 1 day
		cdn77ExpirationInMinutes = (long) 1440;	// 1 day

		videoMap = "default";
		audioMap = "default";

		resetFilter();
	}

	private void resetFilter()
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

	public OutputStream clone()
	{
		OutputStream outputStream = new OutputStream(getDrawTextEnable(), getDrawTextDetails());

		outputStream.setVideoMap(getVideoMap());
		outputStream.setAudioMap(getAudioMap());
		outputStream.setOutputType(getOutputType());
		outputStream.setUdpURL(getUdpURL());
		outputStream.setAwsChannel(getAwsChannel());
		outputStream.setAwsSignedURL(getAwsSignedURL());
		outputStream.setAwsExpirationInMinutes(getAwsExpirationInMinutes());
		outputStream.setCdn77Channel(getCdn77Channel());
		outputStream.setCdn77ExpirationInMinutes(getCdn77ExpirationInMinutes());
		outputStream.setRtmpChannel(getRtmpChannel());
		outputStream.setHlsChannel(getHlsChannel());
		outputStream.setOtherOutputOptions(getOtherOutputOptions());
		outputStream.setEncodingProfile(getEncodingProfile());
		outputStream.setEncodingProfileLabel(getEncodingProfileLabel());

		outputStream.setAudioVolumeChange(getAudioVolumeChange());

		outputStream.setBlackdetect(getBlackdetect());
		outputStream.setBlackdetect_BlackMinDuration(getBlackdetect_BlackMinDuration());
		outputStream.setBlackdetect_PixelBlackTh(getBlackdetect_PixelBlackTh());

		outputStream.setBlackframe(getBlackframe());
		outputStream.setBlackframe_Amount(getBlackframe_Amount());
		outputStream.setBlackframe_Threshold(getBlackframe_Threshold());

		outputStream.setFreezedetect(getFreezedetect());
		outputStream.setFreezedetect_Duration(getFreezedetect_Duration());
		outputStream.setFreezedetect_NoiseInDb(getFreezedetect_NoiseInDb());

		outputStream.setSilencedetect(getSilencedetect());
		outputStream.setSilencedetect_Noise(getSilencedetect_Noise());

		outputStream.setFade(getFade());
		outputStream.setFade_Duration(getFade_Duration());

		return outputStream;
	}

	public void filtersFromJson(JSONObject joFilters)
	{
		try
		{
			resetFilter();

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
		JSONObject joOutput = new JSONObject();

		try
		{
			joOutput.put("videoMap", getVideoMap());
			joOutput.put("audioMap", getAudioMap());

			joOutput.put("outputType", getOutputType());
			if (getOutputType().equalsIgnoreCase("CDN_AWS"))
			{
				if (getAwsChannel() != null && getAwsChannel().getLabel() != null && !getAwsChannel().getLabel().isBlank())
					joOutput.put("awsChannelConfigurationLabel", getAwsChannel().getLabel());
				if (getAwsSignedURL() != null)
					joOutput.put("awsSignedURL", getAwsSignedURL());
				if (getAwsExpirationInMinutes() != null)
					joOutput.put("awsExpirationInMinutes", getAwsExpirationInMinutes());
			}
			else if (getOutputType().equalsIgnoreCase("CDN_CDN77"))
			{
				if (getCdn77Channel() != null && getCdn77Channel().getLabel() != null && !getCdn77Channel().getLabel().isBlank())
					joOutput.put("cdn77ChannelConfigurationLabel", getCdn77Channel().getLabel());
				if (getCdn77ExpirationInMinutes() != null)
					joOutput.put("cdn77ExpirationInMinutes", getCdn77ExpirationInMinutes());
			}
			else if (getOutputType().equalsIgnoreCase("UDP_Stream"))
				joOutput.put("udpUrl", getUdpURL());
			else if (getOutputType().equalsIgnoreCase("RTMP_Channel"))
			{
				if (getRtmpChannel() != null && getRtmpChannel().getLabel() != null && !getRtmpChannel().getLabel().isBlank())
					joOutput.put("rtmpChannelConfigurationLabel", getRtmpChannel().getLabel());
			}
			else if (getOutputType().equalsIgnoreCase("HLS_Channel"))
			{
				if (getHlsChannel() != null && getHlsChannel().getLabel() != null && !getHlsChannel().getLabel().isBlank())
					joOutput.put("hlsChannelConfigurationLabel", getHlsChannel().getLabel());
			}
			else
			{
				mLogger.error("Unknown outputType"
						+ ", outputType: " + getOutputType()
				);
			}

			mLogger.info("getEncodingProfile"
					+ ", getEncodingProfile: " + getEncodingProfile()
			);
			if (getEncodingProfile() != null && getEncodingProfile().getLabel() != null && !getEncodingProfile().getLabel().isBlank())
				joOutput.put("encodingProfileLabel", getEncodingProfile().getLabel());
			else if (getEncodingProfileLabel() != null && !getEncodingProfileLabel().isBlank())
				joOutput.put("encodingProfileLabel", getEncodingProfileLabel());
	
			if (getOtherOutputOptions() != null && !getOtherOutputOptions().isBlank())
				joOutput.put("otherOutputOptions", getOtherOutputOptions());

			// filters
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
					JSONObject joFilters = new JSONObject();
					joOutput.put("filters", joFilters);

					if (videoFilterPresent)
						joFilters.put("video", jaVideo);
					if (audioFilterPresent)
						joFilters.put("audio", jaAudio);
				}
			}

			if (drawTextEnable && drawTextDetails != null)
				joOutput.put("drawTextDetails", drawTextDetails.toJson());
		}
		catch(Exception e)
		{
			mLogger.error("Exception: " + e);
		}
		
		return joOutput;
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
			setVideoMap("default");
			if (joOutputStream.has("videoMap") && !joOutputStream.getString("videoMap").isBlank())
				setVideoMap(joOutputStream.getString("videoMap"));
			setAudioMap("default");
			if (joOutputStream.has("audioMap") && !joOutputStream.getString("audioMap").isBlank())
				setAudioMap(joOutputStream.getString("audioMap"));

			if (joOutputStream.has("outputType") && !joOutputStream.getString("outputType").equalsIgnoreCase(""))
			{
				String sField = joOutputStream.getString("outputType");

				setOutputType(sField);
			}

			if (joOutputStream.has("encodingProfileKey") && !joOutputStream.isNull("encodingProfileKey"))
			{
				Long localEncodingProfileKey = joOutputStream.getLong("encodingProfileKey");

				{
					for (EncodingProfile encodingProfile: encodingProfileList)
					{
						if (encodingProfile.getEncodingProfileKey() == localEncodingProfileKey)
						{
							setEncodingProfile(encodingProfile);

							break;
						}
					}
				}
			}
			else if (joOutputStream.has("encodingProfileLabel") && !joOutputStream.isNull("encodingProfileLabel"))
			{
				String localEncodingProfileLabel = joOutputStream.getString("encodingProfileLabel");

				{
					for (EncodingProfile encodingProfile: encodingProfileList)
					{
						if (encodingProfile.getLabel().equals(localEncodingProfileLabel))
						{
							setEncodingProfile(encodingProfile);

							break;
						}
					}
				}
			}
			else
			{
				setEncodingProfile(null);
			}

			if (getOutputType().equalsIgnoreCase("CDN_AWS"))
			{
				if (joOutputStream.has("awsChannelConfigurationLabel") && !joOutputStream.getString("awsChannelConfigurationLabel").isEmpty())
				{
					String awsChannelConfigurationLabel = joOutputStream.getString("awsChannelConfigurationLabel");

					{
						for (AWSChannelConf awsChannelConf: awsChannelList)
						{
							if (awsChannelConf.getLabel().equals(awsChannelConfigurationLabel))
							{
								setAwsChannel(awsChannelConf);

								break;
							}
						}
					}
				}
				else
					setAwsChannel(null);
				if (joOutputStream.has("awsSignedURL"))
					setAwsSignedURL(joOutputStream.getBoolean("awsSignedURL"));
				if (joOutputStream.has("awsExpirationInMinutes"))
					setAwsExpirationInMinutes(joOutputStream.getLong("awsExpirationInMinutes"));
			}
			else if (getOutputType().equalsIgnoreCase("CDN_CDN77"))
			{
				if (joOutputStream.has("cdn77ChannelConfigurationLabel") && !joOutputStream.getString("cdn77ChannelConfigurationLabel").isEmpty())
				{
					String cdn77ChannelConfigurationLabel = joOutputStream.getString("cdn77ChannelConfigurationLabel");

					{
						for (CDN77ChannelConf cdn77ChannelConf: cdn77ChannelList)
						{
							if (cdn77ChannelConf.getLabel().equals(cdn77ChannelConfigurationLabel))
							{
								setCdn77Channel(cdn77ChannelConf);

								break;
							}
						}
					}
				}
				else
					setCdn77Channel(null);
				if (joOutputStream.has("cdn77ExpirationInMinutes"))
					setCdn77ExpirationInMinutes(joOutputStream.getLong("cdn77ExpirationInMinutes"));
			}
			else if (getOutputType().equalsIgnoreCase("RTMP_Channel"))
			{
				if (joOutputStream.has("rtmpChannelConfigurationLabel") && !joOutputStream.getString("rtmpChannelConfigurationLabel").isEmpty())
				{
					String rtmpChannelConfigurationLabel = joOutputStream.getString("rtmpChannelConfigurationLabel");

					{
						for (RTMPChannelConf rtmpChannelConf: rtmpChannelList)
						{
							if (rtmpChannelConf.getLabel().equals(rtmpChannelConfigurationLabel))
							{
								setRtmpChannel(rtmpChannelConf);

								break;
							}
						}
					}
				}
				else
					setRtmpChannel(null);
			}
			else if (getOutputType().equalsIgnoreCase("HLS_Channel"))
			{
				if (joOutputStream.has("hlsChannelConfigurationLabel") && !joOutputStream.getString("hlsChannelConfigurationLabel").isEmpty())
				{
					String hlsChannelConfigurationLabel = joOutputStream.getString("hlsChannelConfigurationLabel");

					{
						for (HLSChannelConf hlsChannelConf: hlsChannelList)
						{
							if (hlsChannelConf.getLabel().equals(hlsChannelConfigurationLabel))
							{
								setHlsChannel(hlsChannelConf);

								break;
							}
						}
					}
				}
				else
					setHlsChannel(null);
			}

			if (joOutputStream.has("otherOutputOptions") && !joOutputStream.getString("otherOutputOptions").isEmpty())
				setOtherOutputOptions(joOutputStream.getString("otherOutputOptions"));

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

	public void setEncodingProfile(EncodingProfile encodingProfile) {
		this.encodingProfile = encodingProfile;

		// 2023-09-26: vedi commento sopra quando encodingProfile viene definito
		if (this.encodingProfile == null)
			this.encodingProfile = new EncodingProfile();
	}
    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

	public String getVideoMap() {
		return videoMap;
	}

	public void setVideoMap(String videoMap) {
		this.videoMap = videoMap;
	}

	public String getAudioMap() {
		return audioMap;
	}

	public void setAudioMap(String audioMap) {
		this.audioMap = audioMap;
	}

	public EncodingProfile getEncodingProfile() {
		return encodingProfile;
	}

	public RTMPChannelConf getRtmpChannel() {
		return rtmpChannel;
	}

	public void setRtmpChannel(RTMPChannelConf rtmpChannel) {
		this.rtmpChannel = rtmpChannel;
	}

	public String getUdpURL() {
		return udpURL;
	}

	public void setUdpURL(String udpURL) {
		this.udpURL = udpURL;
	}

	public CDN77ChannelConf getCdn77Channel() {
		return cdn77Channel;
	}

	public void setCdn77Channel(CDN77ChannelConf cdn77Channel) {
		this.cdn77Channel = cdn77Channel;
	}

	public Long getCdn77ExpirationInMinutes() {
		return cdn77ExpirationInMinutes;
	}

	public void setCdn77ExpirationInMinutes(Long cdn77ExpirationInMinutes) {
		this.cdn77ExpirationInMinutes = cdn77ExpirationInMinutes;
	}

	public HLSChannelConf getHlsChannel() {
		return hlsChannel;
	}

	public void setHlsChannel(HLSChannelConf hlsChannel) {
		this.hlsChannel = hlsChannel;
	}

	public AWSChannelConf getAwsChannel() {
		return awsChannel;
	}

	public void setAwsChannel(AWSChannelConf awsChannel) {
		this.awsChannel = awsChannel;
	}

	public Boolean getAwsSignedURL() {
		return awsSignedURL;
	}

	public void setAwsSignedURL(Boolean awsSignedURL) {
		this.awsSignedURL = awsSignedURL;
	}

    public String getEncodingProfileLabel() {
        return encodingProfileLabel;
    }

    public void setEncodingProfileLabel(String encodingProfileLabel) {
        this.encodingProfileLabel = encodingProfileLabel;
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

	public Long getAwsExpirationInMinutes() {
		return awsExpirationInMinutes;
	}

	public void setAwsExpirationInMinutes(Long awsExpirationInMinutes) {
		this.awsExpirationInMinutes = awsExpirationInMinutes;
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

	public String getOtherOutputOptions() {
        return otherOutputOptions;
    }

    public void setOtherOutputOptions(String otherOutputOptions) {
        this.otherOutputOptions = otherOutputOptions;
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

	public Float getSilencedetect_Noise() {
		return silencedetect_Noise;
	}

	public void setSilencedetect_Noise(Float silencedetect_Noise) {
		this.silencedetect_Noise = silencedetect_Noise;
	}
}
