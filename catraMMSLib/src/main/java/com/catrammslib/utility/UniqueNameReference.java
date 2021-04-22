package com.catrammslib.utility;

import java.io.Serializable;

/**
 * Created by multi on 13.06.18.
 */
public class UniqueNameReference implements Serializable {
    private String uniqueName;
    private Boolean errorIfContentNotFound;
    private String encodingProfileLabel;
    private Long encodingProfileKey;


    public UniqueNameReference()
    {
        errorIfContentNotFound = null;
        encodingProfileKey = null;
        encodingProfileLabel = null;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public Boolean getErrorIfContentNotFound() {
        return errorIfContentNotFound;
    }

    public void setErrorIfContentNotFound(Boolean errorIfContentNotFound) {
        this.errorIfContentNotFound = errorIfContentNotFound;
    }

    public String getEncodingProfileLabel() {
        return encodingProfileLabel;
    }

    public void setEncodingProfileLabel(String encodingProfileLabel) {
        this.encodingProfileLabel = encodingProfileLabel;
    }

    public Long getEncodingProfileKey() {
        return encodingProfileKey;
    }

    public void setEncodingProfileKey(Long encodingProfileKey) {
        this.encodingProfileKey = encodingProfileKey;
    }
}
