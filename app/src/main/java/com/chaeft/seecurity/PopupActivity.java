package com.chaeft.seecurity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;

//https://ghj1001020.tistory.com/9 참조

public class PopupActivity extends Activity {

    private CustomDialog customDialog;

    int redirectCount, openCount, urlCount , reportNum;
    String title, htmlTitle, orgUrl, dsc, reportCount;
    String htmlCode;
    String userId, newUserId;
    String isWhite, isWhiteUser; //isWhite는 isWhiteDomain 입니다.
    String orgDomain;
    String getDomain;

    ArrayList<String> hrefList = new ArrayList<>();

    Button redirectBtn, reportBtn, cancelBtn, connectBtn;
    TextView  popUpTitle, originUrl, pageName, fileCount, downCount, urlGoCount, reportLog;

    //GetHtml getHtml;
    GetJson getJson;
    JsonParser jsonParser;
    WebAnalysis webAnalysis;
    UrlAnalysis urlAnalysis;

    TextView tv_safe, tv_danger, tv_default;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); //removeTitleBar
        setContentView(R.layout.dialog_check);

        //getHtml = new GetHtml();
        webAnalysis = new WebAnalysis();
        getJson = new GetJson();
        jsonParser = new JsonParser();
        urlAnalysis = new UrlAnalysis();

        tv_safe = findViewById(R.id.tv_safe);
        tv_default = findViewById(R.id.tv_default);
        tv_danger = findViewById(R.id.tv_danger);


        popUpTitle = findViewById(R.id.tv_shortUrl);
        //title : 사용자가 복사하거나 클릭한 (단축된)URL
        originUrl = findViewById(R.id.tv_originUrl);
        reportLog = findViewById(R.id.tv_reportLog);
        pageName = findViewById(R.id.tv_pageName);
        fileCount = findViewById(R.id.tv_fileCount);
        downCount = findViewById(R.id.tv_downCount);
        urlGoCount = findViewById(R.id.tv_urlCount);

        redirectBtn = findViewById(R.id.bt_redirect);
        reportBtn = findViewById(R.id.bt_report); //신고 버튼
        cancelBtn = findViewById(R.id.bt_cancel); //취소 버튼
        connectBtn = findViewById(R.id.bt_connect); //연결 버튼

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.7f;
        getWindow().setAttributes(layoutParams);
        //배경 블러처리

        //setText
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        popUpTitle.setText(title);
        //클립보드 복사의 경우, ClipboardService 에서 복사된 URL 가져와 setText

        //Check UserId
        SharedPreferences sf = getSharedPreferences("sFile",MODE_PRIVATE);
        //text라는 key에 저장된 값이 있는지 확인. 아무값도 들어있지 않으면 ""를 반환
        userId = sf.getString("userId",null);
        if(TextUtils.isEmpty(userId)){
          //  Toast.makeText(PopupActivity.this,"신규유저입니다..",Toast.LENGTH_SHORT).show();
        }else {
         //   Toast.makeText(PopupActivity.this,"신규유저가 아닙니다. id : " + userId,Toast.LENGTH_SHORT).show();
        }//TEST용 toast



        //PAGE THREAD
        new Thread(){
            public void run(){
                Bundle bundle = new Bundle();

                //get html code, title
                GetHtml getHtml = new GetHtml();
                getHtml.setToUrl(title);
                Log.d("ㅅㅄㅄㅄ", title);

                htmlCode = getHtml.getHtmlCode();


                htmlTitle = getHtml.getPageTitle();
                bundle.putString("htmlTitle", htmlTitle);
                bundle.putString("htmlCode", htmlCode);

                //get Expanded(Original) Url
                try {
                    orgUrl = getOrgUrl(title);
                    bundle.putString("orgUrl", orgUrl);
                } catch(Exception e) {
                    orgUrl = "원본 URL을 읽을 수 없습니다.";
                    e.printStackTrace();
                }

                // 필살기
                try {
                    webAnalysis.extract(htmlCode);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Handler mHandler = new Handler(Looper.getMainLooper());
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 사용하고자 하는 코드
                            Toast.makeText(getApplicationContext(), "서버 통신이 원활하지 않거나 올바른 URL 형식이 아닙니다.\n잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                        }
                    }, 0);
                     finish();
                     return;
                }
                hrefList = webAnalysis.getDangerList();
                redirectCount = webAnalysis.getRedirectCount();
                Log.d("test",Integer.toString(redirectCount));
                openCount = webAnalysis.getOpenCount();
                urlCount = webAnalysis.getUrlCount();
                bundle.putStringArrayList("hrefList", hrefList);
                bundle.putInt("redirectCount", redirectCount);
                bundle.putInt("openCount", openCount);
                bundle.putInt("urlCount", urlCount);

                //Send msg to Handler
                Message msg = handler.obtainMessage();
                msg.setData(bundle);
                handler.sendMessage(msg);
                }
        }.start();

        //USER THREAD
        new Thread(){
            public void run(){
                Bundle bundle = new Bundle();
                //userId가 존재하지 않으면 newUserId를 받아옴
                if(TextUtils.isEmpty(userId)) {
                    //get Json data
                    GetJson getJson = new GetJson();
                    getJson.setToUrl("http://www.chaeft.com/seecurity/createUser.asp");
                    String jsonData = getJson.getJsonData();

                    //Json parsing
                    JsonParser jsonParser = new JsonParser();
                    jsonParser.setJsonData(jsonData);
                    jsonParser.userIdParser();
                    newUserId = jsonParser.getUserId();
                    bundle.putString("newUserId", newUserId);
                    userId = newUserId; }

                getJson.setToUrl("http://chaeft.com/seecurity/userIsWhite.asp?userid=" + userId);
                String jsonData = getJson.getJsonData();

                JsonParser jsonParser = new JsonParser();
                jsonParser.setJsonData(jsonData);
                jsonParser.flagParser();
                isWhiteUser = jsonParser.getFlag();
                bundle.putString("isWhiteUser", isWhiteUser);

                getJson.setToUrl("http://chaeft.com/seecurity/updateLastLogin.asp?userid=" + userId);
                getJson.getJsonData();

                //Send msg to Handler
                Message msgUser = handlerUser.obtainMessage();
                msgUser.setData(bundle);
                handler.sendMessage(msgUser);
            }
        }.start();

        //isWhiteDomainTHREAD
        new Thread(){
            public void run(){
                Bundle bundle = new Bundle();

                //isWhiteDomain
                urlAnalysis = new UrlAnalysis();
                urlAnalysis.setTitle(title);
                urlAnalysis.doAnalysis();

                GetJson getJson = new GetJson();
                getJson.setToUrl("http://chaeft.com/seecurity/domainIsWhite.asp?domain="+urlAnalysis.getDomain());
                String jsonData = getJson.getJsonData();

                JsonParser jsonParser = new JsonParser();
                jsonParser.setJsonData(jsonData);
                jsonParser.flagParser();
                isWhite = jsonParser.getFlag();
                bundle.putString("isWhite", isWhite);

                //if(isWhite.equals("2")){}

                //Send msg to Handler
                Message msgTest = handlerTest.obtainMessage();
                msgTest.setData(bundle);
                handlerTest.sendMessage(msgTest);
            }
        }.start();

        //REPORT LOG THREAD
        new Thread(){
            public void run(){
                Bundle bundle = new Bundle();

                urlAnalysis = new UrlAnalysis();
                urlAnalysis.setTitle(title);
                urlAnalysis.doAnalysis();

                GetJson getJson = new GetJson();
                getJson.setToUrl("http://chaeft.com/seecurity/readReportLog.asp?domain="+urlAnalysis.getDomain());
                String jsonData = getJson.getJsonData();

                JsonParser jsonParser = new JsonParser();
                jsonParser.setJsonData(jsonData);
                jsonParser.dscParser();
                jsonParser.reportCountParser();
                dsc = jsonParser.getDsc();
                reportCount = jsonParser.getReportCount();
                bundle.putString("dsc", dsc);
                bundle.putString("reportCount", reportCount);

                //Send msg to Handler
                Message msgReport = handlerReport.obtainMessage();
                msgReport.setData(bundle);
                handlerReport.sendMessage(msgReport);
            }
        }.start();

        redirectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = "";
                for(int i=0; i<hrefList.size() ;i++){
                    str = str + (i+1) + ".  " + hrefList.get(i) + "\n";
                }
                Dialog(str);
               /* Log.d("redirect1111", str);
                AlertDialog.Builder builder = new AlertDialog.Builder(PopupActivity.this);
                builder.setTitle("잠재적 위험 리스트").setMessage(str);

                AlertDialog alertDialog = builder.create();
                alertDialog.show();*/
                str="";
            }
        }); //redirectList 버튼

        //연결 버튼 START
        connectBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                new Thread(){
                    public void run(){
                        Bundle bundle = new Bundle();
                        urlAnalysis = new UrlAnalysis();
                        urlAnalysis.setTitle(title);
                        urlAnalysis.doAnalysis();
                        String logUrl = "http://chaeft.com/seecurity/createLog.asp?domain=" +
                                urlAnalysis.getDomain() + "&protocol=" + urlAnalysis.getProtocol()+
                                "&folder_path="+urlAnalysis.getPath()+"&param="+urlAnalysis.getParameter()
                                +"&flagment="+urlAnalysis.getFlag();
                        getJson.setToUrl(logUrl);
                        getJson.getJsonData();
                        Log.d("제발요ㅠㅠㅠㅠㅠㅠㅠㅠㅠ", logUrl);

                        GetJson getJson = new GetJson();
                        getJson.setToUrl("http://chaeft.com/seecurity/readOrgDomain.asp?ps_domain=" + urlAnalysis.getDomain());
                        String jsonData = getJson.getJsonData();

                        JsonParser jsonParser = new JsonParser();
                        jsonParser.setJsonData(jsonData);
                        jsonParser.domainParser();
                        getDomain = jsonParser.getDomain();
                        bundle.putString("getDomain", getDomain);

                        //Send msg to Handler
                        Message msgDomain = handlerDomain.obtainMessage();
                        msgDomain.setData(bundle);
                        handlerDomain.sendMessage(msgDomain);

                    }
                }.start();
                Toast.makeText(getApplicationContext(), "웹사이트로 이동합니다.", Toast.LENGTH_LONG).show();
                Uri toUrl = Uri.parse(title); //원래 String 값 : title
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(toUrl);
                startActivity(intent);
                finish();
                //title String 값 -> Uri , 해당 URL 로 연결
            }//web 으로 연결
        });//연결 버튼 END

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });//취소 버튼

        reportBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final int[] selectItem = {0};
                final String[] items = new String[]{"피싱","랜섬웨어","광고",
                        "에러 페이지","파일 강제 다운로드","파일 강제 업로드", "단말기 해킹"};

                AlertDialog.Builder dialog = new AlertDialog.Builder(PopupActivity.this);
                final AlertDialog.Builder insertUrl = new AlertDialog.Builder(PopupActivity.this);

                LayoutInflater inflater=getLayoutInflater();


                final View dialogView= inflater.inflate(R.layout.background_custom_radio, null);

               //dialog.setView(dialogView);

                dialog.setTitle("신고 유형을 선택하세요")
                        .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selectItem[0] = which;
                                reportNum = which +1;
                                Log.d("제발.", reportNum+"");

                            }
                        })
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //신고유형이 피싣일 때
                                if (selectItem[0] == 0){
                                    insertUrl.setTitle(items[selectItem[0]]);
                                    insertUrl.setMessage("피싱 사이트의 실제 URL을 알고 있다면 제보해주세요.");

                                    final EditText et = new EditText(PopupActivity.this);
                                    insertUrl.setView(et);

                                    insertUrl.setPositiveButton("제보", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) { //확인 버튼 눌렀을때 이벤트
                                            orgDomain = et.getText().toString();
                                           // Toast.makeText(PopupActivity.this,orgDomain,Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                            new Thread(){
                                                public void run(){
                                                    GetJson getJson = new GetJson();
                                                    urlAnalysis = new UrlAnalysis();
                                                    urlAnalysis.setTitle(title);
                                                    urlAnalysis.doAnalysis();
                                                    String urlDomain = urlAnalysis.getDomain();
                                                        getJson.setToUrl("http://chaeft.com/seecurity/createReport.asp?userid="
                                                                + userId + "&domain=" + urlDomain + "&report_code=" + 1
                                                                + "&org_domain=" + orgDomain);
                                                        getJson.getJsonData();
                                                }
                                            }.start();
                                            Toast.makeText(getApplicationContext(), "제보가 접수되었습니다.", Toast.LENGTH_LONG).show();
                                            finish();
                                            //dialog.dismiss();

                                        }
                                    });
                                    //모름
                                    insertUrl.setNegativeButton("모름", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            finish();
                                        }
                                    });
                                    insertUrl.show();

                                }else{
                                    new Thread(){
                                        public void run(){
                                            GetJson getJson = new GetJson();
                                            urlAnalysis = new UrlAnalysis();
                                            urlAnalysis.setTitle(title);
                                            urlAnalysis.doAnalysis();
                                            String urlDomain = urlAnalysis.getDomain();
                                            Log.d("신고코드", reportNum+"");

                                            getJson.setToUrl("http://chaeft.com/seecurity/createReport.asp?userid="
                                                    + userId + "&domain=" + urlDomain + "&report_code=" + reportNum);
                                            getJson.getJsonData();
                                        }
                                    }.start();
                                finish();
                                }
                            } //신고유형 다이얼로그 끝

                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(PopupActivity.this,"신고를 취소합니다."
                                        ,Toast.LENGTH_SHORT).show();
                            }
                        });
                dialog.create();
                dialog.show();
            }
        });//신고 버튼

    }

    Handler handler = new Handler(){
        public void handleMessage(Message msg){
            Bundle bun = msg.getData();
            originUrl.setText(orgUrl);
            pageName.setText(htmlTitle);
            htmlCode = bun.getString("htmlCode");
            hrefList = bun.getStringArrayList("hrefList");
            fileCount.setText(""+redirectCount);
            downCount.setText(""+openCount);
            urlGoCount.setText(""+urlCount);
            originUrl.setText(orgUrl);
            pageName.setText(htmlTitle);
        }
    }; //memory leak solution : https://mainia.tistory.com/1393 (시간나면,,)


    Handler handlerUser = new Handler(){
        public void handleMessage(Message msgUser){

            Bundle bunUser = msgUser.getData();
            isWhiteUser = bunUser.getString("isWhiteUser");

            if(isWhiteUser.equals("2")){
                reportBtn.setVisibility(View.INVISIBLE);
            }

        }
    };

    Handler handlerTest = new Handler(){
        public void handleMessage(Message msgTest){

            Bundle bunTest = msgTest.getData();
            isWhite = bunTest.getString("isWhite");

            if(isWhite.equals("1")){
                tv_safe.setVisibility(View.VISIBLE);
            }
            else if(isWhite.equals("2")){
                tv_danger.setVisibility(View.VISIBLE);
            }else {
                tv_default.setVisibility(View.VISIBLE);
            }

        }
    };

    Handler handlerReport = new Handler(){
        public void handleMessage(Message msgReport){
            Bundle bunReport = msgReport.getData();
            dsc = bunReport.getString("dsc");
            reportCount = bunReport.getString("reportCount");

            if (!TextUtils.isEmpty(dsc) && !TextUtils.isEmpty(reportCount)){
                reportLog.setText(dsc + " " + reportCount + "회");
            }else {
                reportLog.setText("");
            }
        }
    };

    Handler handlerDomain = new Handler(){
        public void handleMessage(Message msgDomain){
            Bundle bunDomain = msgDomain.getData();
            getDomain = bunDomain.getString("getDomain");

            if (!TextUtils.isEmpty(getDomain)){
                AlertDialog.Builder builder = new AlertDialog.Builder(PopupActivity.this);
                builder.setTitle("권장 연결 URL은 아래와 같습니다.").setMessage(getDomain + "\n로 연결하시겠습니까?");
                builder.setPositiveButton("권장 URL 연결", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri toUrl = Uri.parse(getDomain);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(toUrl);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton("기존 URL 연결", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }else{
                //Toast.makeText(PopupActivity.this,"받아온 도메인이 null이애요.",Toast.LENGTH_SHORT).show();
            }
        }
    };


    @Override
    protected void onStop() {
        super.onStop();
        // Activity가 종료되기 전에 저장한다.
        //SharedPreferences를 sFile이름, 기본모드로 설정
        SharedPreferences sharedPreferences = getSharedPreferences("sFile",MODE_PRIVATE);

        //저장을 하기위해 editor를 이용하여 값을 저장시켜준다.
        SharedPreferences.Editor editor = sharedPreferences.edit(); // 사용자가 입력한 저장할 데이터
        editor.putString("userId", userId); // key : userId, value : userId

       // Toast.makeText(PopupActivity.this, userId + ", Id가 저장되었습니다.", Toast.LENGTH_SHORT).show();

        //최종 커밋
        editor.apply();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥 레이어 클릭시 닫히지 않게
        if(event.getAction()== MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼으로 종료할 수 없게
        return;
    }

    private String getOrgUrl(String shortenedUrl) throws IOException {
        URL url = new URL(shortenedUrl);
        // open connection
        HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection(Proxy.NO_PROXY);
        httpURLConnection.setRequestMethod("GET");
        // stop following browser redirect
        httpURLConnection.setInstanceFollowRedirects(false);

        // extract location header containing the actual destination URL
        String result = httpURLConnection.getHeaderField("Location");
        httpURLConnection.disconnect();
        if(result == null) {result = title;}
        // 단축되지 않은 URL 의 경우, header 에 location 이 없으므로 result 가 null -> 원래 url result
        return result;
    }
    public void Dialog(String str){
        customDialog = new CustomDialog(PopupActivity.this,
                "주의", // 제목
                str, // 내용
                leftListener); // 왼쪽 버튼 이벤트
        // 오른쪽 버튼 이벤트

        //요청 이 다이어로그를 종료할 수 있게 지정함
        customDialog.setCancelable(true);
        customDialog.getWindow().setGravity(Gravity.CENTER);
        customDialog.show();
    }
    //다이얼로그 클릭이벤트
    private View.OnClickListener leftListener = new View.OnClickListener() {
        public void onClick(View v) {
            //  Toast.makeText(PopupActivity.this, "버튼을 클릭하였습니다.", Toast.LENGTH_SHORT).show();
            customDialog.dismiss();
        }
    };


}