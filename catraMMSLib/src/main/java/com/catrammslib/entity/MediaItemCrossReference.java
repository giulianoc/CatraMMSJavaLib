package com.catrammslib.entity;

import java.io.Serializable;

/**
 * Created by multi on 08.06.18.
 */
public class MediaItemCrossReference implements Serializable{

    private Long sourceMediaItemKey;
    private String type;
    private Long targetMediaItemKey;
    private String parameters;

    public MediaItemCrossReference clone()
    {
        MediaItemCrossReference mediaItemCrossReference = new MediaItemCrossReference();

        mediaItemCrossReference.setSourceMediaItemKey(sourceMediaItemKey);
        mediaItemCrossReference.setType(type);
        mediaItemCrossReference.setTargetMediaItemKey(targetMediaItemKey);
        mediaItemCrossReference.setParameters(parameters);

        return mediaItemCrossReference;
    }

    @Override
    public String toString() {
        return "{" +
                "sourceMediaItemKey=" + sourceMediaItemKey +
                ", type='" + type + '\'' +
                ", targetMediaItemKey=" + targetMediaItemKey +
                ", parameters='" + parameters + '\'' +
                '}';
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getSourceMediaItemKey() {
        return sourceMediaItemKey;
    }

    public void setSourceMediaItemKey(Long sourceMediaItemKey) {
        this.sourceMediaItemKey = sourceMediaItemKey;
    }

    public Long getTargetMediaItemKey() {
        return targetMediaItemKey;
    }

    public void setTargetMediaItemKey(Long targetMediaItemKey) {
        this.targetMediaItemKey = targetMediaItemKey;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
}
