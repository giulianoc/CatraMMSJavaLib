package com.catrammslib.entity;

import java.io.Serializable;
import java.util.Objects;

public class AudioBitRate implements Serializable {
    private Long kBitRate;
    private String sKBitRate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudioBitRate that = (AudioBitRate) o;
        return kBitRate.equals(that.kBitRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kBitRate);
    }

    public String getsKBitRate() {
        return sKBitRate;
    }

    public void setsKBitRate(String sKBitRate) {
        this.sKBitRate = sKBitRate;

        kBitRate = Long.parseLong(sKBitRate);
    }

    public Long getkBitRate() {
        return kBitRate;
    }

    public void setkBitRate(Long kBitRate) {
        this.kBitRate = kBitRate;
    }

}
