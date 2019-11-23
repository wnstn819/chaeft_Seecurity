package com.chaeft.seecurity;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JsonParser {

    String userId;
    String flag;
    String domain;
    String result;
    String dsc;
    String reportCount;


    String jsonData;
    ArrayList<String> resultList = new ArrayList<>();

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    public boolean userIdParser(){
        Log.d("jsonstring",jsonData);

        StringBuffer sb_list = new StringBuffer();

        try {
            JSONArray jsonArray = new JSONArray(jsonData);

            for(int i=0; i<jsonArray.length(); i++){
                JSONObject resultObject = jsonArray.getJSONObject(i);

                userId = resultObject.getString("USERID");
                sb_list.setLength(0);

                resultList.add(sb_list.toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
            userId  = "아이디 생성에 실패했습니다.";
            return false;
        }
        return true;
    }

    public boolean flagParser(){
        Log.d("jsonstring",jsonData);

        StringBuffer sb_list = new StringBuffer();

        try {
            JSONArray jsonArray = new JSONArray(jsonData);

            for(int i=0; i<jsonArray.length(); i++){
                JSONObject resultObject = jsonArray.getJSONObject(i);

                flag = resultObject.getString("FLAG");

                sb_list.setLength(0);

                resultList.add(sb_list.toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
            flag  = "FLAG 값 획득을 실패했습니다.";
            return false;
        }
        return true;
    }


    public boolean resultParser(){
        Log.d("jsonstring",jsonData);

        StringBuffer sb_list = new StringBuffer();

        try {
            JSONArray jsonArray = new JSONArray(jsonData);

            for(int i=0; i<jsonArray.length(); i++){
                JSONObject resultObject = jsonArray.getJSONObject(i);

                result = resultObject.getString("RST");

                sb_list.setLength(0);

                resultList.add(sb_list.toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
            result  = "전송 결과 값 획득을 실패했습니다.";
            return false;
        }
        return true;
    }


    public boolean domainParser(){
       Log.d("jsonstring",jsonData);

        StringBuffer sb_list = new StringBuffer();

        try {
            JSONArray jsonArray = new JSONArray(jsonData);

            for(int i=0; i<jsonArray.length(); i++){
                JSONObject resultObject = jsonArray.getJSONObject(i);

                domain = resultObject.getString("DOMAIN");

                sb_list.setLength(0);

                resultList.add(sb_list.toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
            domain  = "도메인 주소 획득을 실패했습니다.";
            return false;
        }


        return true;
    }

    public boolean dscParser(){
        Log.d("jsonstring",jsonData);

        StringBuffer sb_list = new StringBuffer();

        try {
            JSONArray jsonArray = new JSONArray(jsonData);

            for(int i=0; i<jsonArray.length(); i++){
                JSONObject resultObject = jsonArray.getJSONObject(i);

                Log.d("씨1발", jsonArray.getJSONObject(0).toString());

                dsc = resultObject.getString("DSC");

                sb_list.setLength(0);

                resultList.add(sb_list.toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
            dsc  = "신고유형 획득을 실패했습니다.";
            return false;
        }

        return true;
    }

    public boolean reportCountParser(){
        Log.d("jsonstring",jsonData);

        StringBuffer sb_list = new StringBuffer();

        try {
            JSONArray jsonArray = new JSONArray(jsonData);

            for(int i=0; i<jsonArray.length(); i++){
                JSONObject resultObject = jsonArray.getJSONObject(i);

                Log.d("카운트", jsonArray.getJSONObject(0).toString());

                reportCount = resultObject.getString("REPORT_COUNT");

                sb_list.setLength(0);

                resultList.add(sb_list.toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
            reportCount = "신고횟수 획득을 실패했습니다.";
            return false;
        }


        return true;
    }

    public ArrayList<String> getList() {
        return resultList;
    }

    public String getUserId() {
        return userId;
    }

    public String getFlag() {
        return flag;
    }

    public String getDomain() {
        return domain;
    }

    public String getResult() {
        return result;
    }

    public String getDsc() { return dsc; }

    public String getReportCount() { return reportCount; }
}
