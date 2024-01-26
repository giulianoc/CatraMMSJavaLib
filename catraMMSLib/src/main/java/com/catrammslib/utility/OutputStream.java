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

	// HLS
	private String otherOutputOptions;

	// 2023-09-26: serve la new in fase di instanziazione della classe altrimenti avrei una eccezione in outputStream.xhtml
	private EncodingProfile encodingProfile = new EncodingProfile();
	// In alcuni casi abbiamo solamente encodingProfileLabel e non l'EncodingProfile, per cui lasciamo la doppia opzione
    private String encodingProfileLabel;

	private Filters filters = new Filters();

	public OutputStream()
	{
		awsSignedURL = false;
		awsExpirationInMinutes = (long) 1440;	// 1 day
		cdn77ExpirationInMinutes = (long) 1440;	// 1 day

		videoMap = "default";
		audioMap = "default";
	}

	public OutputStream clone()
	{
		OutputStream outputStream = new OutputStream();

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

		outputStream.setFilters(filters.clone());

		return outputStream;
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
				JSONObject joFilters = filters.toJson();
				if (joFilters != null)
					joOutput.put("filters", joFilters);
			}

			// if (filters.getDrawTextEnable() && filters.getDrawTextDetails() != null)
			//	joOutput.put("drawTextDetails", filters.getDrawTextDetails().toJson());
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
				filters.fromJson(joFilters);
			}

			/*
			{
				if (joOutputStream.has("drawTextDetails"))
				{
					filters.setDrawTextEnable(true);

					JSONObject joDrawTextDetails = joOutputStream.getJSONObject("drawTextDetails");

					filters.getDrawTextDetails().fromJson(joDrawTextDetails);
				}
				else
				{
					filters.setDrawTextEnable(false);
				}
			}
			 */
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

	public Long getAwsExpirationInMinutes() {
		return awsExpirationInMinutes;
	}

	public void setAwsExpirationInMinutes(Long awsExpirationInMinutes) {
		this.awsExpirationInMinutes = awsExpirationInMinutes;
	}

	public String getOtherOutputOptions() {
        return otherOutputOptions;
    }

    public void setOtherOutputOptions(String otherOutputOptions) {
        this.otherOutputOptions = otherOutputOptions;
    }


	public Filters getFilters() {
		return filters;
	}

	public void setFilters(Filters filters) {
		this.filters = filters;
	}

}
