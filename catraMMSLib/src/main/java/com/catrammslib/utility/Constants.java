package com.catrammslib.utility;

import com.catrammslib.utility.Cost;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Constants implements Serializable {

    private List<String> videoFileFormatsList;
    private List<String> audioFileFormatsList;
    private List<String> imageFileFormatsList;
    private List<String> videoCodecsList;
    private List<String> videoX264ProfilesList;
    private List<String> videoVPXProfilesList;
    private List<String> videoMPEG4ProfilesList;
    private List<String> audioCodecsList;
    private List<Long> audioSampleRateList;
    private List<String> imageInterlaceTypeList;
    private List<String> encodingPrioritiesList;
    private List<String> contentTypesList;
    private List<String> fontTypesList;
    private List<String> fontSizesList;
    private List<String> colorsList;
    private List<String> crossReferenceTypesList;
    private List<String> dependenciesToBeAddedToReferencesAtOptions;
    private List<String> streamSourceTypeList;
    private List<String> mediaProtocolsList;
    private List<String> frameRateModeList;
    private List<String> overlayTextXList;
    private List<String> overlayTextYList;
    private List<String> overlayImageXList;
    private List<String> overlayImageYList;
    private List<String> addSilentAudioTypes;
    private List<Cost> dedicatedResources;
    private List<String> filtersList;
    private List<String> timecodeList;
    private List<String> ptsTimecodeRateList;
    private List<String> deliveryServerTypeList;

    public Constants()
    {
        {
            videoFileFormatsList = new ArrayList<>();
            videoFileFormatsList.add("mp4");
            videoFileFormatsList.add("m4v");
            videoFileFormatsList.add("mkv");
            videoFileFormatsList.add("mov");
            videoFileFormatsList.add("ts");
            videoFileFormatsList.add("wmv");
            videoFileFormatsList.add("mpeg");
            videoFileFormatsList.add("mxf");
            videoFileFormatsList.add("mts");
            videoFileFormatsList.add("avi");
            videoFileFormatsList.add("webm");
            videoFileFormatsList.add("hls");
        }

        {
            audioFileFormatsList = new ArrayList<>();
            audioFileFormatsList.add("mp3");
            audioFileFormatsList.add("aac");
            audioFileFormatsList.add("m4a");
            audioFileFormatsList.add("wav");
            audioFileFormatsList.add("hls");
        }

        {
            imageFileFormatsList = new ArrayList<>();
            imageFileFormatsList.add("jpg");
            audioFileFormatsList.add("jpeg");
            imageFileFormatsList.add("png");
            imageFileFormatsList.add("gif");
            audioFileFormatsList.add("tif");
            imageFileFormatsList.add("tga");
        }

        {
            videoCodecsList = new ArrayList<>();
            videoCodecsList.add("libx264");
            videoCodecsList.add("libvpx");
            videoCodecsList.add("mpeg4");   // internal ffmpeg implementation of mpeg4
            videoCodecsList.add("xvid");    // external ffmpeg implementation of mpeg4
        }

        {
            videoX264ProfilesList = new ArrayList<>();
            videoX264ProfilesList.add("high");
            videoX264ProfilesList.add("high422");
            videoX264ProfilesList.add("baseline");
            videoX264ProfilesList.add("main");
        }

        {
            videoVPXProfilesList = new ArrayList<>();
            videoVPXProfilesList.add("best");
            videoVPXProfilesList.add("good");
        }

        {
            // 1-31
            videoMPEG4ProfilesList = new ArrayList<>();
            videoMPEG4ProfilesList.add("");     // default
            videoMPEG4ProfilesList.add("1");    // libxvid will take much more space than the same video compressed with the native mpeg4 encoder
            videoMPEG4ProfilesList.add("10");
            videoMPEG4ProfilesList.add("15");
            videoMPEG4ProfilesList.add("20");
            videoMPEG4ProfilesList.add("31");
        }

        {
            audioCodecsList = new ArrayList<>();
            audioCodecsList.add("aac");
            audioCodecsList.add("pcm_s16le");
        }

        {
            imageInterlaceTypeList = new ArrayList<>();
            imageInterlaceTypeList.add("NoInterlace");
            imageInterlaceTypeList.add("LineInterlace");
            imageInterlaceTypeList.add("PlaneInterlace");
            imageInterlaceTypeList.add("PartitionInterlace");
        }

        {
            audioSampleRateList = new ArrayList<>();
            audioSampleRateList.add(new Long(8000));
            audioSampleRateList.add(new Long(11025));
            audioSampleRateList.add(new Long(12000));
            audioSampleRateList.add(new Long(16000));
            audioSampleRateList.add(new Long(22050));
            audioSampleRateList.add(new Long(24000));
            audioSampleRateList.add(new Long(32000));
            audioSampleRateList.add(new Long(44100));
            audioSampleRateList.add(new Long(48000));
            audioSampleRateList.add(new Long(64000));
            audioSampleRateList.add(new Long(88200));
            audioSampleRateList.add(new Long(96000));
        }

        {
            encodingPrioritiesList = new ArrayList<>();
            encodingPrioritiesList.add("Low");
            encodingPrioritiesList.add("Medium");
            encodingPrioritiesList.add("High");
        }

        {
            contentTypesList = new ArrayList<>();
            contentTypesList.add("video");
            contentTypesList.add("audio");
            contentTypesList.add("image");
        }

        {
            fontTypesList = new ArrayList<>();
            fontTypesList.add("cac_champagne.ttf");
            fontTypesList.add("DancingScript-Regular.otf");
            fontTypesList.add("OpenSans-BoldItalic.ttf");
            fontTypesList.add("OpenSans-Bold.ttf");
            fontTypesList.add("OpenSans-ExtraBoldItalic.ttf");
            fontTypesList.add("OpenSans-ExtraBold.ttf");
            fontTypesList.add("OpenSans-Italic.ttf");
            fontTypesList.add("OpenSans-LightItalic.ttf");
            fontTypesList.add("OpenSans-Light.ttf");
            fontTypesList.add("OpenSans-Regular.ttf");
            fontTypesList.add("OpenSans-SemiboldItalic.ttf");
            fontTypesList.add("OpenSans-Semibold.ttf");
            fontTypesList.add("Pacifico.ttf");
            fontTypesList.add("Sofia-Regular.otf");
            fontTypesList.add("Windsong.ttf");
        }

        {
            fontSizesList = new ArrayList<>();
            fontSizesList.add("10");
            fontSizesList.add("12");
            fontSizesList.add("14");
            fontSizesList.add("18");
            fontSizesList.add("20");
            fontSizesList.add("22");
            fontSizesList.add("24");
            fontSizesList.add("30");
            fontSizesList.add("36");
            fontSizesList.add("40");
            fontSizesList.add("48");
            fontSizesList.add("60");
            fontSizesList.add("72");
            fontSizesList.add("84");
            fontSizesList.add("96");
            fontSizesList.add("108");
        }

        {
            colorsList = new ArrayList<>();
            colorsList.add("black");
            colorsList.add("blue");
            colorsList.add("gray");
            colorsList.add("green");
            colorsList.add("orange");
            colorsList.add("purple");
            colorsList.add("red");
            colorsList.add("violet");
            colorsList.add("white");
            colorsList.add("yellow");
        }

        {
            crossReferenceTypesList = new ArrayList<>();
            crossReferenceTypesList.add("NoCrossReference");
            crossReferenceTypesList.add("ImageOfVideo");
            crossReferenceTypesList.add("VideoOfImage");
            crossReferenceTypesList.add("ImageOfAudio");
            crossReferenceTypesList.add("AudioOfImage");
            crossReferenceTypesList.add("PosterOfVideo");
            crossReferenceTypesList.add("VideoOfPoster");
            crossReferenceTypesList.add("SlideShowOfImage");
            crossReferenceTypesList.add("ImageForSlideShow");
            crossReferenceTypesList.add("SlideShowOfAudio");
            crossReferenceTypesList.add("AudioForSlideShow");
            crossReferenceTypesList.add("FaceOfVideo");
            crossReferenceTypesList.add("CutOfVideo");
            crossReferenceTypesList.add("CutOfAudio");
        }

        {
            dependenciesToBeAddedToReferencesAtOptions = new ArrayList<>();
            dependenciesToBeAddedToReferencesAtOptions.add("");
            dependenciesToBeAddedToReferencesAtOptions.add("Beginning");
            dependenciesToBeAddedToReferencesAtOptions.add("End");
            dependenciesToBeAddedToReferencesAtOptions.add("0");
            dependenciesToBeAddedToReferencesAtOptions.add("1");
            dependenciesToBeAddedToReferencesAtOptions.add("2");
        }

        {
            streamSourceTypeList = new ArrayList<>();
            streamSourceTypeList.add("IP_PULL");
            streamSourceTypeList.add("IP_PUSH");
            streamSourceTypeList.add("CaptureLive");
            streamSourceTypeList.add("TV");
        }

        {
            mediaProtocolsList = new ArrayList<>();
            mediaProtocolsList.add("rtmp");
            mediaProtocolsList.add("udp");
            mediaProtocolsList.add("srt");
        }

        {
            frameRateModeList = new ArrayList<>();
            frameRateModeList.add("passthrough");
            frameRateModeList.add("cfr");
            frameRateModeList.add("vfr");
            frameRateModeList.add("drop");
            frameRateModeList.add("auto");
        }

        {
            timecodeList = new ArrayList<>();
            timecodeList.add("none");
            timecodeList.add("editorialTimecode");
            timecodeList.add("ptsTimecode");
        }

        {
            ptsTimecodeRateList = new ArrayList<>();
            ptsTimecodeRateList.add("25");
            ptsTimecodeRateList.add("50");
        }

        {
            overlayTextXList = new ArrayList<>();
            overlayTextXList.add("left");
            overlayTextXList.add("center");
            overlayTextXList.add("right");
            overlayTextXList.add("leftToRight_5");
            overlayTextXList.add("leftToRight_10");
            overlayTextXList.add("loopLeftToRight_5");
            overlayTextXList.add("loopLeftToRight_10");
            overlayTextXList.add("rightToLeft_15");
            overlayTextXList.add("rightToLeft_30");
            overlayTextXList.add("loopRightToLeft_15");
            overlayTextXList.add("loopRightToLeft_30");
            overlayTextXList.add("loopRightToLeft_60");
            overlayTextXList.add("loopRightToLeft_90");
            overlayTextXList.add("loopRightToLeft_120");
            overlayTextXList.add("loopRightToLeft_150");
            overlayTextXList.add("loopRightToLeft_180");
            overlayTextXList.add("loopRightToLeft_210");
		}

        {
            overlayTextYList = new ArrayList<>();
            overlayTextYList.add("below");
            overlayTextYList.add("center");
            overlayTextYList.add("high");
            overlayTextYList.add("bottomToTop_50");
            overlayTextYList.add("bottomToTop_100");
            overlayTextYList.add("loopBottomToTop_50");
            overlayTextYList.add("loopBottomToTop_100");
            overlayTextYList.add("topToBottom_50");
            overlayTextYList.add("topToBottom_100");
            overlayTextYList.add("loopTopToBottom_50");
            overlayTextYList.add("loopTopToBottom_100");
		}

        {
            overlayImageXList = new ArrayList<>();
            overlayImageXList.add("left");
            overlayImageXList.add("center");
            overlayImageXList.add("right");
        }

        {
            overlayImageYList = new ArrayList<>();
            overlayImageYList.add("below");
            overlayImageYList.add("center");
            overlayImageYList.add("high");
        }

        {
            addSilentAudioTypes = new ArrayList<>();
            addSilentAudioTypes.add("entireTrack");
            addSilentAudioTypes.add("begin");
            addSilentAudioTypes.add("end");
        }

        {
            deliveryServerTypeList = new ArrayList<>();
            deliveryServerTypeList.add("origin");
            deliveryServerTypeList.add("mid-origin");
            deliveryServerTypeList.add("edge");
        }

        {
            dedicatedResources = new ArrayList<>();
            {
                Cost cost = new Cost();
                cost.setDescription("Storage 100 GB");
                cost.setType("storage");
                cost.setMonthlyCost((long) (0.4 * 25L));    // cost del fornitore per 100GB * moltiplicatore margine
                cost.setStepFactor(1L);
                // cost.setMinAmount(0L);
                cost.setMaxAmount(200L);

                dedicatedResources.add(cost);
            }
            {
                Cost cost = new Cost();
                cost.setDescription("Encoder based on AMD Ryzen 5 3600 Hexa-Core \"Matisse\" (Zen2), 64 GB DDR4 RAM");
                cost.setType("encoder");
                cost.setMonthlyCost(50 * 4L);
                cost.setStepFactor(1L);
                cost.setMinAmount(0L);
                cost.setMaxAmount(20L);

                dedicatedResources.add(cost);
            }

            {
                Cost cost = new Cost();
                cost.setDescription("Encoder based on AMD Ryzen 9 7950X3D 16-Core \"Raphael\" (Zen 4), 128 GB DDR5 ECC RAM");
                cost.setType("encoder");
                cost.setMonthlyCost(130 * 4L);
                cost.setStepFactor(1L);
                cost.setMinAmount(0L);
                cost.setMaxAmount(20L);

                dedicatedResources.add(cost);
            }

            {
                Cost cost = new Cost();
                cost.setDescription("Encoder based on Intel® Core™ i9-13900 24 Core \"Raptor Lake-S\", 64 GB DDR5 ECC RAM");
                cost.setType("encoder");
                cost.setMonthlyCost(105 * 4L);
                cost.setStepFactor(1L);
                cost.setMinAmount(0L);
                cost.setMaxAmount(20L);

                dedicatedResources.add(cost);
            }

            {
                Cost cost = new Cost();
                cost.setDescription("CDN (77)");
                cost.setType("cdn");
                cost.setMonthlyCost(50L);
                cost.setStepFactor(1L);
                cost.setMinAmount(0L);
                cost.setMaxAmount(100L);

                dedicatedResources.add(cost);
            }

            {
                Cost cost = new Cost();
                cost.setDescription("Support on Telegram 9h (from 8am to 5pm CET time), response time within 1h");
                cost.setType("support");
                cost.setMonthlyCost(1000L);
                cost.setStepFactor(1L);
                cost.setMinAmount(0L);
                cost.setMaxAmount(1L);

                dedicatedResources.add(cost);
            }
        }

        {
            filtersList = new ArrayList<>();

            // video
            filtersList.add("Black Detect");
            filtersList.add("Black Frame");
            filtersList.add("Crop");
            filtersList.add("Draw Box");
            filtersList.add("Fade");
            filtersList.add("Freeze Detect");
            filtersList.add("Text Overlay");

            // audio
            filtersList.add("Audio Resample");
            filtersList.add("Audio Volume Change");
            filtersList.add("Silence Detect");

            // complex
            filtersList.add("Image Overlay");
        }
    }

    public List<String> getVideoFileFormatsList() {
        return videoFileFormatsList;
    }

    public List<String> getAudioFileFormatsList() {
        return audioFileFormatsList;
    }

    public List<String> getImageFileFormatsList() {
        return imageFileFormatsList;
    }

    public List<String> getFileFormatsList() {

        List<String> fileFormatsList = new ArrayList<>();

        fileFormatsList.add("");    // in some cases the OutputFileFormat parameter is optional

        fileFormatsList.addAll(videoFileFormatsList);

        fileFormatsList.remove("hls");  // hls è presente sia nei video che negli audio file format
        fileFormatsList.add("m3u8-tar.gz");
        fileFormatsList.add("streaming-to-mp4");

        fileFormatsList.addAll(audioFileFormatsList);
        fileFormatsList.remove("hls");  // hls è presente sia nei video che negli audio file format

        fileFormatsList.addAll(imageFileFormatsList);

        return fileFormatsList;
    }

    public List<String> getVideoCodecsList() {
        return videoCodecsList;
    }

    public List<String> getAudioCodecsList() {
        return audioCodecsList;
    }

    public List<String> getVideoX264ProfilesList() {
        return videoX264ProfilesList;
    }

    public List<String> getVideoVPXProfilesList() {
        return videoVPXProfilesList;
    }

    public List<String> getVideoMPEG4ProfilesList() {
        return videoMPEG4ProfilesList;
    }

    public List<Long> getAudioSampleRateList() {
        return audioSampleRateList;
    }

    public List<String> getEncodingPrioritiesList() {
        return encodingPrioritiesList;
    }

    public List<String> getContentTypesList() {
        return contentTypesList;
    }

    public List<String> getFontTypesList() {
        return fontTypesList;
    }

    public List<String> getFontSizesList() {
        return fontSizesList;
    }

    public List<String> getColorsList() {
        return colorsList;
    }

    public List<String> getCrossReferenceTypesList() {
        return crossReferenceTypesList;
    }

    public List<String> getDependenciesToBeAddedToReferencesAtOptions() {
        return dependenciesToBeAddedToReferencesAtOptions;
    }

    public List<String> getStreamSourceTypeList() {
		return streamSourceTypeList;
	}

	public List<String> getMediaProtocolsList() {
        return mediaProtocolsList;
    }

    public List<String> getImageInterlaceTypeList() {
        return imageInterlaceTypeList;
    }

    public List<String> getFrameRateModeList() {
        return frameRateModeList;
    }

    public List<String> getTimecodeList() {
        return timecodeList;
    }

    public List<String> getPtsTimecodeRateList() {
        return ptsTimecodeRateList;
    }

    public List<String> getOverlayTextXList() {
		return overlayTextXList;
	}

	public List<String> getOverlayTextYList() {
		return overlayTextYList;
	}

    public List<String> getOverlayImageXList() {
        return overlayImageXList;
    }

    public List<String> getOverlayImageYList() {
        return overlayImageYList;
    }

    public List<String> getAddSilentAudioTypes() {
        return addSilentAudioTypes;
    }

    public List<Cost> getDedicatedResources() {
        return dedicatedResources;
    }

    public List<String> getFiltersList() {
        return filtersList;
    }
}
