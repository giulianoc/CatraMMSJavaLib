package com.catrammslib.entity;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Objects;

public class VideoBitRate implements Serializable {

    // private final Logger mLogger = Logger.getLogger(this.getClass());

    private Long width;
    private String sWidth;

    private Long height;
    private String sHeight;

    private String forceOriginalAspectRatio;    // decrease, increase
    private Boolean pad;

    private Long kBitRate;
    private String sKBitRate;

    private Long kMaxRate;
    private Long kBufferSize;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoBitRate that = (VideoBitRate) o;
        return kBitRate.equals(that.kBitRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kBitRate);
    }

    public String getsWidth() {
        return sWidth;
    }

    public void setsWidth(String sWidth) {
        this.sWidth = sWidth;

        width = Long.parseLong(sWidth);
    }

    public String getsHeight() {
        return sHeight;
    }

    public void setsHeight(String sHeight) {
        this.sHeight = sHeight;

        height = Long.parseLong(sHeight);
    }

    public String getsKBitRate() {
        return sKBitRate;
    }

    public void setsKBitRate(String sKBitRate) {
        this.sKBitRate = sKBitRate;

        kBitRate = Long.parseLong(sKBitRate);
    }

    public Long getWidth() {
        return width;
    }

    public void setWidth(Long width) {
        this.width = width;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }

    public String getForceOriginalAspectRatio() {
        return forceOriginalAspectRatio;
    }

    public void setForceOriginalAspectRatio(String forceOriginalAspectRatio) {
        this.forceOriginalAspectRatio = forceOriginalAspectRatio;
    }

    public Boolean getPad() {
        return pad;
    }

    public void setPad(Boolean pad) {
        this.pad = pad;
    }

    public Long getkBitRate() {
        return kBitRate;
    }

    public void setkBitRate(Long kBitRate) {
        this.kBitRate = kBitRate;
    }

    public Long getkMaxRate() {
        return kMaxRate;
    }

    public void setkMaxRate(Long kMaxRate) {
        this.kMaxRate = kMaxRate;
    }

    public Long getkBufferSize() {
        return kBufferSize;
    }

    public void setkBufferSize(Long kBufferSize) {
        this.kBufferSize = kBufferSize;
    }
}
