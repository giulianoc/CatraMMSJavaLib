package com.catrammslib.utility;

import java.io.Serializable;
import java.util.List;

import com.catrammslib.entity.*;
import com.catrammslib.utility.filters.Filters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	// RTMP_Channel, SRT_Channel, HLS_Channel, HLS, UDP_Stream
	private String outputType;

	// UDP_Stream
    private String udpURL;

	// RTMP_Channel
	private RTMPChannelConf rtmpChannel;

	// SRT_Channel
	private SRTChannelConf srtChannel;

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
		outputStream.setRtmpChannel(getRtmpChannel());
		outputStream.setSrtChannel(getSrtChannel());
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
			if (getOutputType().equalsIgnoreCase("UDP_Stream"))
				joOutput.put("udpUrl", getUdpURL());
			else if (getOutputType().equalsIgnoreCase("RTMP_Channel"))
			{
				if (getRtmpChannel() != null && getRtmpChannel().getLabel() != null && !getRtmpChannel().getLabel().isBlank())
					joOutput.put("rtmpChannelConfigurationLabel", getRtmpChannel().getLabel());
			}
			else if (getOutputType().equalsIgnoreCase("SRT_Channel"))
			{
				if (getSrtChannel() != null && getSrtChannel().getLabel() != null && !getSrtChannel().getLabel().isBlank())
					joOutput.put("srtChannelConfigurationLabel", getSrtChannel().getLabel());
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
						 List<SRTChannelConf> srtChannelList
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

			if (getOutputType().equalsIgnoreCase("RTMP_Channel"))
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
			else if (getOutputType().equalsIgnoreCase("SRT_Channel"))
			{
				if (joOutputStream.has("srtChannelConfigurationLabel") && !joOutputStream.getString("srtChannelConfigurationLabel").isEmpty())
				{
					String srtChannelConfigurationLabel = joOutputStream.getString("srtChannelConfigurationLabel");

					{
						for (SRTChannelConf srtChannelConf: srtChannelList)
						{
							if (srtChannelConf.getLabel().equals(srtChannelConfigurationLabel))
							{
								setSrtChannel(srtChannelConf);

								break;
							}
						}
					}
				}
				else
					setSrtChannel(null);
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

	public SRTChannelConf getSrtChannel() {
		return srtChannel;
	}

	public void setSrtChannel(SRTChannelConf srtChannel) {
		this.srtChannel = srtChannel;
	}

	public String getUdpURL() {
		return udpURL;
	}

	public void setUdpURL(String udpURL) {
		this.udpURL = udpURL;
	}

	public HLSChannelConf getHlsChannel() {
		return hlsChannel;
	}

	public void setHlsChannel(HLSChannelConf hlsChannel) {
		this.hlsChannel = hlsChannel;
	}

    public String getEncodingProfileLabel() {
        return encodingProfileLabel;
    }

    public void setEncodingProfileLabel(String encodingProfileLabel) {
        this.encodingProfileLabel = encodingProfileLabel;
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
