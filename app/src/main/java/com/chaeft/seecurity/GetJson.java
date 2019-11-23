package com.chaeft.seecurity;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GetJson {

    private String toUrl;
    private String pageTitle;
    private String receiveMsg;

    protected String getJsonData(String... strings) {
        try {
            URL url = new URL(toUrl);

            Log.d("url00000000",url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET"); //get방식으로 요청
            conn.setRequestProperty("Content-type","application/json"); //json형식으로 전송

            BufferedReader rd;
            if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream())); // 정상연결
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream())); // 연결실패 -> getError
            }

            StringBuilder sb = new StringBuilder();
            String line;
            while((line=rd.readLine()) != null) { //한줄씩 읽어오기
                sb.append(line); //읽어온 데이터 stringbuilder에 한줄씩 더하기
            }

            receiveMsg = sb.toString();
            Log.d("receiveMsg",receiveMsg);

            rd.close();
            conn.disconnect();


        } catch (MalformedURLException e) {
            Log.d("씨발1","잠좀자자");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("씨발2","ㅗㅗㅗㅗㅗ");
            e.printStackTrace();
        }
        return receiveMsg;
    }

    public String getToUrl() {
        return toUrl;
    }

    public void setToUrl(String toUrl) {
        this.toUrl = toUrl;
        Log.d("toUrl222", toUrl);
    }

    public String getReceiveMsg() {
        return receiveMsg;
    }

}
