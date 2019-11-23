package com.chaeft.seecurity;

import android.text.Html;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GetHtml {

    private String toUrl;
    private String pageTitle = "페이지의 이름을 읽어올 수 없습니다.";
    private String receiveMsg;

    protected String getHtmlCode(String... strings) {
        try {
            URL url = new URL("http://www.chaeft.com/seecurity/getWebcode.asp?url="+toUrl);

            Log.d("url00000000",url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET"); //get방식으로 요청

            BufferedReader rd;
            if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream())); // 정상연결
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream())); // 연결실패 -> getError
            }

            StringBuilder sb = new StringBuilder();
            String line;
            while((line=rd.readLine()) != null) { //한줄씩 읽어오기
                if(line.contains("<title>")){
                    pageTitle = stripHtml(line);
                    Log.d("Title0000", pageTitle);
                }//line이 <title> 을 포함하면 String만 추출 (== 웹페이지 이름)
                sb.append(line + "\n"); //읽어온 데이터 stringBuilder에 한줄씩 더하기
            }

            receiveMsg = sb.toString();
            Log.d("receiveMsg",receiveMsg);

            rd.close();
            conn.disconnect();

        } catch (MalformedURLException e) {
            Log.d("씨발련아1","잠좀자자");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("씨발련아2","ㅗㅗㅗㅗㅗ");
            e.printStackTrace();
        }
        return receiveMsg;
    }

    private String stripHtml(String html) {
        return Html.fromHtml(html).toString();
    }

    public String getToUrl() {
        return toUrl;
    }

    public void setToUrl(String toUrl) {
        this.toUrl = toUrl;
        Log.d("toUrl11111", toUrl);
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public String getReceiveMsg() {
        return receiveMsg;
    }

}
