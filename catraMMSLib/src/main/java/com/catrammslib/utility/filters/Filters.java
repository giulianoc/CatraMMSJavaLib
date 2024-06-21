package com.catrammslib.utility.filters;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Filters {
    private static final Logger mLogger = LoggerFactory.getLogger(Filters.class);

    private List<Filter> filters = new ArrayList<>();

    // servono per la GUI
    private Filter editFilter;
    private String tableToBeUpdatedOnSave;


    /*
    public Filter appendFilter() {
        Filter filter = new Filter();
        filters.add(filter);

        return filter;
    }

    public void removeLastFilter() {
        if (filters.size() > 0)
            filters.remove(filters.size() - 1);
    }
     */

    public void prepareFilterDialog(String tableToBeUpdatedOnSave)
    {
        editFilter = new Filter();
        this.tableToBeUpdatedOnSave = tableToBeUpdatedOnSave;
    }

    public void remove(int index)
    {
        mLogger.info("Received remove"
                + ", index: " + index
                + ", filters.size: " + filters.size()
        );

        if (index < getFilters().size())
            filters.remove(index);
    }

    public void saveFilter()
    {
        getFilters().add(editFilter);
    }

    public Filters clone() {
        Filters newFilters = new Filters();
        for (Filter filter : filters)
            newFilters.getFilters().add(filter.clone());

        return newFilters;
    }

    public void fromJson(JSONObject joFilters) {
        try {
            filters.clear();

            if (joFilters.has("video")) {
                JSONArray jaVideo = joFilters.getJSONArray("video");

                for (int filterIndex = 0; filterIndex < jaVideo.length(); filterIndex++) {
                    JSONObject joFilter = jaVideo.getJSONObject(filterIndex);

                    if (joFilter.has("type")) {
                        Filter filter = new Filter();
                        filters.add(filter);

                        switch (joFilter.getString("type").toLowerCase()) {
                            case "blackdetect":
                                filter.setFilterName("Black Detect");
                                break;
                            case "blackframe":
                                filter.setFilterName("Black Frame");
                                break;
                            case "crop":
                                filter.setFilterName("Crop");
                                if (joFilter.has("out_w"))
                                    filter.setCrop_OutputWidth(joFilter.getString("out_w"));
                                if (joFilter.has("out_h"))
                                    filter.setCrop_OutputHeight(joFilter.getString("out_h"));
                                if (joFilter.has("x"))
                                    filter.setCrop_X(joFilter.getString("x"));
                                if (joFilter.has("y"))
                                    filter.setCrop_Y(joFilter.getString("y"));
                                if (joFilter.has("keep_aspect"))
                                    filter.setCrop_KeepAspect(joFilter.getBoolean("keep_aspect"));
                                if (joFilter.has("exact"))
                                    filter.setCrop_Exact(joFilter.getBoolean("exact"));
                                break;
                            case "drawbox":
                                filter.setFilterName("Draw Box");
                                if (joFilter.has("x"))
                                    filter.setDrawBox_X(joFilter.getString("x"));
                                if (joFilter.has("y"))
                                    filter.setDrawBox_Y(joFilter.getString("y"));
                                if (joFilter.has("width"))
                                    filter.setDrawBox_Width(joFilter.getString("width"));
                                if (joFilter.has("height"))
                                    filter.setDrawBox_Height(joFilter.getString("height"));
                                if (joFilter.has("fontColor"))
                                    filter.setDrawBox_FontColor(joFilter.getString("fontColor"));
                                if (joFilter.has("percentageOpacity"))
                                    filter.setDrawBox_PercentageOpacity(joFilter.getLong("percentageOpacity"));
                                if (joFilter.has("thickness"))
                                    filter.setDrawBox_Thickness(joFilter.getString("thickness"));
                                break;
                            case "drawtext":
                                filter.setFilterName("Text Overlay");
                                filter.getDrawTextDetails().fromJson(joFilter);
                                break;
                            case "fade":
                                filter.setFilterName("Fade");
                                if (joFilter.has("duration")) {
                                    Object o = joFilter.get("duration");
                                    filter.setFade_Duration(joFilter.getLong("duration"));
                                }
                                break;
                            case "freezedetect":
                                filter.setFilterName("Freeze Detect");
                                if (joFilter.has("duration")) {
                                    Object o = joFilter.get("duration");
                                    filter.setFreezedetect_Duration(joFilter.getLong("duration"));
                                }
                                break;
                            default:
                                String errorMessage = "Unknown video filter type: " + joFilter.getString("type");
                                mLogger.error(errorMessage);

                                throw new Exception(errorMessage);
                        }
                    }
                }
            }

            if (joFilters.has("audio")) {
                JSONArray jaAudio = joFilters.getJSONArray("audio");

                for (int filterIndex = 0; filterIndex < jaAudio.length(); filterIndex++) {
                    JSONObject joFilter = jaAudio.getJSONObject(filterIndex);

                    if (joFilter.has("type")) {
                        Filter filter = new Filter();
                        filters.add(filter);

                        switch (joFilter.getString("type").toLowerCase()) {
                            case "volume":
                                filter.setFilterName("Audio Volume Change");
                                if (joFilter.has("factor") && !joFilter.isNull("factor"))
                                    filter.setAudioVolumeChange(joFilter.getDouble("factor"));
                                break;
                            case "silencedetect":
                                filter.setFilterName("Silence Detect");
                                break;
                            default:
                                String errorMessage = "Unknown audio filter type: " + joFilter.getString("type");
                                mLogger.error(errorMessage);

                                throw new Exception(errorMessage);
                        }
                    }
                }
            }

            if (joFilters.has("complex")) {
                JSONArray jaComplex = joFilters.getJSONArray("complex");

                for (int filterIndex = 0; filterIndex < jaComplex.length(); filterIndex++) {
                    JSONObject joFilter = jaComplex.getJSONObject(filterIndex);

                    if (joFilter.has("type")) {
                        Filter filter = new Filter();
                        filters.add(filter);

                        switch (joFilter.getString("type").toLowerCase()) {
                            case "imageoverlay":
                                filter.setFilterName("Image Overlay");
                                filter.getImageOverlayDetails().fromJson(joFilter);
                                break;
                            default:
                                String errorMessage = "Unknown complex filter type: " + joFilter.getString("type");
                                mLogger.error(errorMessage);

                                throw new Exception(errorMessage);
                        }
                    }
                }
            }
        } catch (Exception e) {
            mLogger.error("Exception: " + e);
        }
    }

    public JSONObject toJson()
            throws Exception
    {
        JSONObject joFilters = new JSONObject();

        try {
            boolean videoFilterPresent = false;
            JSONArray jaVideo = new JSONArray();

            boolean audioFilterPresent = false;
            JSONArray jaAudio = new JSONArray();

            boolean complexFilterPresent = false;
            JSONArray jaComplex = new JSONArray();

            for (Filter filter : filters) {
                switch (filter.getFilterName()) {
                    case "Black Detect":
                        JSONObject joBlackDetect = new JSONObject();
                        jaVideo.put(joBlackDetect);

                        joBlackDetect.put("type", "blackdetect");
                        if (filter.getBlackdetect_BlackMinDuration() != null)
                            joBlackDetect.put("black_min_duration", filter.getBlackdetect_BlackMinDuration());
                        if (filter.getBlackdetect_PixelBlackTh() != null)
                            joBlackDetect.put("pixel_black_th", filter.getBlackdetect_PixelBlackTh());
                        break;
                    case "Black Frame":
                        JSONObject joBlackFrame = new JSONObject();
                        jaVideo.put(joBlackFrame);

                        joBlackFrame.put("type", "blackframe");
                        if (filter.getBlackframe_Amount() != null)
                            joBlackFrame.put("amount", filter.getBlackframe_Amount());
                        if (filter.getBlackframe_Threshold() != null)
                            joBlackFrame.put("threshold", filter.getBlackframe_Threshold());

                        break;
                    case "Crop":
                        JSONObject joCrop = new JSONObject();
                        jaVideo.put(joCrop);

                        joCrop.put("type", "crop");

                        if (filter.getCrop_OutputWidth() != null)
                            joCrop.put("out_w", filter.getCrop_OutputWidth());
                        if (filter.getCrop_OutputHeight() != null)
                            joCrop.put("out_h", filter.getCrop_OutputHeight());
                        if (filter.getCrop_X() != null)
                            joCrop.put("x", filter.getCrop_X());
                        if (filter.getCrop_Y() != null)
                            joCrop.put("y", filter.getCrop_Y());
                        if (filter.getCrop_KeepAspect() != null)
                            joCrop.put("keep_aspect", filter.getCrop_KeepAspect());
                        if (filter.getCrop_Exact() != null)
                            joCrop.put("exact", filter.getCrop_Exact());

                        break;
                    case "Draw Box":
                        JSONObject joDrawBox = new JSONObject();
                        jaVideo.put(joDrawBox);

                        joDrawBox.put("type", "drawbox");

                        if (filter.getDrawBox_X() != null)
                            joDrawBox.put("x", filter.getDrawBox_X());
                        if (filter.getDrawBox_Y() != null)
                            joDrawBox.put("y", filter.getDrawBox_Y());
                        if (filter.getDrawBox_Width() != null)
                            joDrawBox.put("width", filter.getDrawBox_Width());
                        if (filter.getDrawBox_Height() != null)
                            joDrawBox.put("height", filter.getDrawBox_Height());
                        if (filter.getDrawBox_FontColor() != null)
                            joDrawBox.put("fontColor", filter.getDrawBox_FontColor());
                        if (filter.getDrawBox_PercentageOpacity() != null)
                            joDrawBox.put("percentageOpacity", filter.getDrawBox_PercentageOpacity());
                        if (filter.getDrawBox_Thickness() != null)
                            joDrawBox.put("thickness", filter.getDrawBox_Thickness());

                        break;
                    case "Text Overlay":
                        jaVideo.put(filter.getDrawTextDetails().toJson());

                        break;
                    case "Fade":
                        JSONObject joFade = new JSONObject();
                        jaVideo.put(joFade);

                        joFade.put("type", "fade");

                        if (filter.getFade_Duration() != null && filter.getFade_Duration() > 0)
                            joFade.put("duration", filter.getFade_Duration());

                        break;
                    case "Freeze Detect":
                        JSONObject joFreezeDetect = new JSONObject();
                        jaVideo.put(joFreezeDetect);

                        joFreezeDetect.put("type", "freezedetect");
                        if (filter.getFreezedetect_Duration() != null && filter.getFreezedetect_Duration() > 0)
                            joFreezeDetect.put("duration", filter.getFreezedetect_Duration());
                        if (filter.getFreezedetect_NoiseInDb() != null)
                            joFreezeDetect.put("noiseInDb", filter.getFreezedetect_NoiseInDb());

                        break;
                    case "Audio Volume Change":
                        JSONObject joVolume = new JSONObject();
                        jaAudio.put(joVolume);

                        joVolume.put("type", "volume");
                        joVolume.put("factor", filter.getAudioVolumeChange());

                        break;
                    case "Silence Detect":
                        JSONObject joSilenceDetect = new JSONObject();
                        jaAudio.put(joSilenceDetect);

                        joSilenceDetect.put("type", "silencedetect");
                        if (filter.getSilencedetect_Noise() != null)
                            joSilenceDetect.put("noise", filter.getSilencedetect_Noise());

                        break;
                    case "Image Overlay":
                        jaComplex.put(filter.getImageOverlayDetails().toJson());

                        break;
                    default:
                        String errorMessage = "Unknown filter name: " + filter.getFilterName();
                        mLogger.error(errorMessage);

                        throw new Exception(errorMessage);
                }
            }

            // build the filters json
            if (jaVideo.length() > 0)
                joFilters.put("video", jaVideo);
            if (jaAudio.length() > 0)
                joFilters.put("audio", jaAudio);
            if (jaComplex.length() > 0)
                joFilters.put("complex", jaComplex);

        } catch (Exception e) {
            mLogger.error("Exception: " + e);
        }

        return joFilters;
    }

    public boolean isTextOverlayPresent()
    {
        for(Filter filter: filters)
        {
            if (filter.getFilterName().equalsIgnoreCase("Text Overlay"))
                return true;
        }

        return false;
    }

    public Filter getFirstTextOverlay()
    {
        Filter textOverlayFilter = null;

        for(Filter filter: filters)
        {
            if (filter.getFilterName().equalsIgnoreCase("Text Overlay"))
            {
                textOverlayFilter = filter;
                break;
            }
        }

        return textOverlayFilter;
    }

    public String getTableToBeUpdatedOnSave() {
        return tableToBeUpdatedOnSave;
    }

    public void setTableToBeUpdatedOnSave(String tableToBeUpdatedOnSave) {
        this.tableToBeUpdatedOnSave = tableToBeUpdatedOnSave;
    }

    public Filter getEditFilter() {
        return editFilter;
    }

    public void setEditFilter(Filter editFilter) {
        this.editFilter = editFilter;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }
}
