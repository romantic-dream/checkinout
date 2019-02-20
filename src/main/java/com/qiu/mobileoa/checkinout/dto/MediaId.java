package com.qiu.mobileoa.checkinout.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class MediaId {

    private String meidaId;

    @JacksonXmlProperty(localName = "MediaId")
    @JacksonXmlCData
    public String getMeidaId() {
        return meidaId;
    }

    public void setMeidaId(String meidaId) {
        this.meidaId = meidaId;
    }
}
