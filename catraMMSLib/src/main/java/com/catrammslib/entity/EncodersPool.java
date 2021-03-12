package com.catrammslib.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by multi on 09.06.18.
 */
public class EncodersPool implements Serializable{
    private Long encodersPoolKey;
    private String label;
    private List<Encoder> encoderList = new ArrayList<>();


    public String getEncodersInfo()
    {
        String encodersInfo = "";
        for (Encoder encoder: encoderList)
            encodersInfo += (encoder.getLabel() + "<br/>");

        return encodersInfo;
    }

    public Long getEncodersPoolKey() {
        return encodersPoolKey;
    }

    public void setEncodersPoolKey(Long encodersPoolKey) {
        this.encodersPoolKey = encodersPoolKey;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Encoder> getEncoderList() {
        return encoderList;
    }

    public void setEncoderList(List<Encoder> encoderList) {
        this.encoderList = encoderList;
    }
}
