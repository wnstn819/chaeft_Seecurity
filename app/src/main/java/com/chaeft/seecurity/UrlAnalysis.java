package com.chaeft.seecurity;

import android.util.Log;

public class  UrlAnalysis {

    private String substr =null;
    private String title;
    private String Protocol =null;
    private String Domain =null;
    private String Path =null;
    private String Parameter =null;
    private String Flag =null;

    public void doAnalysis() {

        Log.d("씨ㅣㅣㅣ1ㅏㄹ", title);

        if(title.contains("http")) {
            substr = title.substring(0,title.indexOf("://"));
            Protocol = substr;
            title = title.substring(title.indexOf("://")+3);

            if(title.contains("/")) {
                substr = title.substring(0,title.indexOf("/"));
                Domain = substr;
                title = title.substring(title.indexOf("/")+1);
            }else {
                Domain = title;
                title= "?";
            }

            if(title.contains("?")) {
                substr = title.substring(0,title.indexOf("?"));
                Path = substr;
                title = title.substring(title.indexOf("?")+1);
            }else {
                Path = title;
                title = "#";

            }

            if(title.contains("#")) {
                substr = title.substring(0,title.indexOf("#"));
                Parameter = substr;
                Flag = title.substring(title.indexOf("#")+1);

            }else {
                Parameter = title;
                Flag = "";
            }

            Log.d("제발요.", Domain);

        }

    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProtocol() {
        return Protocol;
    }

    public String getDomain() {
        return Domain;
    }

    public String getPath() {
        return Path;
    }

    public String getParameter() {
        return Parameter;
    }

    public String getFlag() {
        return Flag;
    }

}
