package com.qiu.mobileoa.checkinout.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Articles {
    private String Title;
    private String Description;
    private String Url;
    private String PicUrl;

    @JacksonXmlProperty(localName = "Title")
    @JacksonXmlCData
    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    @JacksonXmlProperty(localName = "Description")
    @JacksonXmlCData
    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    @JacksonXmlProperty(localName = "Url")
    @JacksonXmlCData
    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    @JacksonXmlProperty(localName = "PicUrl")
    @JacksonXmlCData
    public String getPicUrl() {
        return PicUrl;
    }

    public void setPicUrl(String picUrl) {
        PicUrl = picUrl;
    }
}
