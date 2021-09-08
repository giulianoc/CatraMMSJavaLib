package com.catrammslib.entity;

import java.io.Serializable;
import java.util.Objects;

public class VideoBitRate implements Serializable {
    private Long width;
    private Long height;
    private Long kBitRate;
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
