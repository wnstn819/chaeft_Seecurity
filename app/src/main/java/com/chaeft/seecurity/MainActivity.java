package com.chaeft.seecurity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;


public class MainActivity extends Activity {

    String title;
    String exUrl, orgDomain;
    Button redirect, report, mail;
    TextView inputUrl;
    String input, serviceInput;
    int reportNum;
    UrlAnalysis urlAnalysis;
    String userId, newUserId, isWhiteUser;
    GetJson getJson;
    private AdView mAdView;


    String getDomain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); //removeTitleBar
        setContentView(R.layout.activity_main);

        startService(new Intent(this, ClipboardService.class));

        Intent intent = getIntent();
        serviceInput = intent.getStringExtra("title");
        if(!TextUtils.isEmpty(serviceInput)) {
            if (serviceInput.contains("http") || serviceInput.matches("(.*)://(.*)")
                    || serviceInput.matches("(.*).co(.*)") || serviceInput.matches("(.*).net(.*)")) {
                if (!serviceInput.contains("http")) {
                    serviceInput = "http://" + serviceInput;
                }
              //  Toast.makeText(getApplicationContext(), serviceInput, Toast.LENGTH_LONG).show();
                //newClipText 에 http 나 :// 가 포함돼 있으면 URL 로 간주 추후 URL 감지 코드 추가
                //https://stackoverflow.com/questions/5713558/detect-and-extract-url-from-a-string

                //TODO URL 이 포함된 SMS Text 전체를 복사한 경우, SMS 의 모든 Text 가 title 에 뜸

                new Thread(){
                    public void run(){
                        Bundle bundle = new Bundle();

                        UrlAnalysis urlAnalysis = new UrlAnalysis();
                        urlAnalysis.setTitle(serviceInput);
                        urlAnalysis.doAnalysis();

                        GetJson getJson = new GetJson();
                        getJson.setToUrl("http://chaeft.com/seecurity/readOrgDomain.asp?ps_domain=" + urlAnalysis.getDomain());
                        Log.d("씨1바려ㅏㅇ", urlAnalysis.getDomain());
                        String jsonData = getJson.getJsonData();

                        JsonParser jsonParser = new JsonParser();
                        Log.d("테스투투1", jsonData);
                        jsonParser.setJsonData(jsonData);
                        jsonParser.domainParser();
                        getDomain = jsonParser.getDomain();
                        bundle.putString("getDomain", getDomain);
                        Log.d("테스투투2",getDomain);


                        if(getDomain.equals("null")) {
                            Intent intent = new Intent(MainActivity.this, PopupActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("title", serviceInput);
                            startActivity(intent);
                        }else{
                            if (!TextUtils.isEmpty(getDomain)){
                                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("경고").setMessage("해당 URL은 피싱 도메인 신고 이력이 있습니다. 이 URL 대신 권장 URL로의 연결을 권장합니다.\n"+getDomain + "로 연결하시겠습니까?");
                                builder.setPositiveButton("권장 URL 연결", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (!getDomain.contains("http")) {
                                            getDomain = "http://" + getDomain;
                                        }

                                        Intent intent = new Intent(MainActivity.this, PopupActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.putExtra("title", getDomain);
                                        startActivity(intent);
                                    }
                                });
                                builder.setNegativeButton("기존 URL 연결", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(MainActivity.this, PopupActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.putExtra("title", serviceInput);
                                        startActivity(intent);
                                    }
                                });



                                Handler mHandler = new Handler(Looper.getMainLooper());
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialog alertDialog = builder.create();
                                        alertDialog.show();

                                    }
                                }, 0);

                            }else{
                                //Toast.makeText(PopupActivity.this,"받아온 도메인이 null이애요.",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }.start();


            } else {
                Toast.makeText(getApplicationContext(),"URL 형식이 올바르지 않습니다.",Toast.LENGTH_SHORT).show();
            }
        }



        MobileAds.initialize(this, getString(R.string.admob_app_id));
        mAdView = findViewById(R.id.av_main);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        getJson = new GetJson();
        urlAnalysis = new UrlAnalysis();
        SharedPreferences sf = getSharedPreferences("sFile", MODE_PRIVATE);
        //text라는 key에 저장된 값이 있는지 확인. 아무값도 들어있지 않으면 ""를 반환
        userId = sf.getString("userId", null);

        redirect = findViewById(R.id.bt_main_redirect);
        report = findViewById(R.id.bt_main_report);
        mail = findViewById(R.id.bt_main_mail);
        inputUrl = findViewById(R.id.et_url);





        //연결버튼 START
        redirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input = inputUrl.getText().toString();
                if (input.contains("http") || input.matches("(.*)://(.*)")
                        || input.matches("(.*).co(.*)") || input.matches("(.*).net(.*)") || input.matches("(.*).kr(.*)") ) {
                    if (!input.contains("http")) {
                        input = "http://" + input;
                    }
                   // Toast.makeText(getApplicationContext(), input, Toast.LENGTH_LONG).show();
                    //newClipText 에 http 나 :// 가 포함돼 있으면 URL 로 간주 추후 URL 감지 코드 추가
                    //https://stackoverflow.com/questions/5713558/detect-and-extract-url-from-a-string

                    //TODO URL 이 포함된 SMS Text 전체를 복사한 경우, SMS 의 모든 Text 가 title 에 뜸

                    new Thread(){
                        public void run(){
                            Bundle bundle = new Bundle();

                            UrlAnalysis urlAnalysis = new UrlAnalysis();
                            urlAnalysis.setTitle(input);
                            urlAnalysis.doAnalysis();

                            GetJson getJson = new GetJson();
                            getJson.setToUrl("http://chaeft.com/seecurity/readOrgDomain.asp?ps_domain=" + urlAnalysis.getDomain());
                            Log.d("씨1바려ㅏㅇ", urlAnalysis.getDomain());
                            String jsonData = getJson.getJsonData();

                            JsonParser jsonParser = new JsonParser();
                            Log.d("테스투투1", jsonData);
                            jsonParser.setJsonData(jsonData);
                            jsonParser.domainParser();
                            getDomain = jsonParser.getDomain();
                            bundle.putString("getDomain", getDomain);
                            Log.d("테스투투2",getDomain);


                            if(getDomain.equals("null")) {
                                Intent intent = new Intent(MainActivity.this, PopupActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("title", input);
                                startActivity(intent);
                            }else{
                                if (!TextUtils.isEmpty(getDomain)){
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle("경고").setMessage("해당 URL은 피싱 도메인 신고 이력이 있습니다. 이 URL 대신 권장 URL로의 연결을 권장합니다.\n"+getDomain + "로 연결하시겠습니까?");
                                    builder.setPositiveButton("권장 URL 연결", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (!getDomain.contains("http")) {
                                                getDomain = "http://" + getDomain;
                                            }

                                            Intent intent = new Intent(MainActivity.this, PopupActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intent.putExtra("title", getDomain);
                                            startActivity(intent);
                                        }
                                    });
                                    builder.setNegativeButton("기존 URL 연결", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(MainActivity.this, PopupActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intent.putExtra("title", input);
                                            startActivity(intent);
                                        }
                                    });



                                    Handler mHandler = new Handler(Looper.getMainLooper());
                                    mHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            AlertDialog alertDialog = builder.create();
                                            alertDialog.show();

                                        }
                                    }, 0);

                                }else{
                                    //Toast.makeText(PopupActivity.this,"받아온 도메인이 null이애요.",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }.start();


                } else {
                    Toast.makeText(getApplicationContext(),"URL 형식이 올바르지 않습니다.",Toast.LENGTH_SHORT).show();
                }
            }
        }); //연결버튼 END

        //신고버튼 START
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int[] selectItem = {0};
                final String[] items = new String[]{"피싱", "랜섬웨어", "광고",
                        "에러 페이지", "파일 강제 다운로드", "파일 강제 업로드", "단말기 해킹"};

                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                final AlertDialog.Builder insertUrl = new AlertDialog.Builder(MainActivity.this);

                dialog.setTitle("신고 유형을 선택하세요")
                        .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selectItem[0] = which;
                                reportNum = which + 1;
                                Log.d("제발.", reportNum + "");

                            }
                        }) //래디오버튼
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //신고유형이 피싣일 때
                                if (selectItem[0] == 0) {
                                    insertUrl.setTitle(items[selectItem[0]]);
                                    insertUrl.setMessage("피싱 사이트의 실제 URL을 알고 있다면 제보해주세요.");

                                    final EditText et = new EditText(MainActivity.this);
                                    insertUrl.setView(et);

                                    insertUrl.setPositiveButton("제보", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) { //확인 버튼 눌렀을때 이벤트
                                            orgDomain = et.getText().toString();
                                        //    Toast.makeText(MainActivity.this, orgDomain, Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                            new Thread() {
                                                public void run() {
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
                                        }
                                    });
                                    //모름
                                    insertUrl.setNegativeButton("모름", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    insertUrl.show();

                                } else {
                                    new Thread() {
                                        public void run() {
                                            GetJson getJson = new GetJson();
                                            urlAnalysis = new UrlAnalysis();
                                            urlAnalysis.setTitle(title);
                                            urlAnalysis.doAnalysis();
                                            String urlDomain = urlAnalysis.getDomain();
                                            Log.d("신고코드", reportNum + "");

                                            getJson.setToUrl("http://chaeft.com/seecurity/createReport.asp?userid="
                                                    + userId + "&domain=" + urlDomain + "&report_code=" + reportNum);
                                            getJson.getJsonData();
                                        }
                                    }.start();
                                }
                            } //신고유형 다이얼로그 끝

                            {
                                Toast.makeText(MainActivity.this, "신고가 접수되었습니다.", Toast.LENGTH_SHORT).show();
                                //신고 접수 THREAD

                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MainActivity.this, "신고를 취소합니다."
                                        , Toast.LENGTH_SHORT).show();
                            }
                        });
                dialog.create();
                dialog.show();
            }
        });//신고 버튼 END

        mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMail();
            }
        });
    }

    void sendMail() {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"chaeft2019@gmail.com"});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "About 'Seecurity' application");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Version Code : " + getAppVersionCode()+"\nVersion Name : "+getAppVersionName()+"\n\n");
        try {

            emailIntent.setType("text/html");
            emailIntent.setPackage("com.google.android.gm");
            if(emailIntent.resolveActivity(getPackageManager())!=null)
                startActivity(emailIntent);

            startActivity(emailIntent);
        } catch (Exception e) {
            e.printStackTrace();

            emailIntent.setType("text/html");

            startActivity(Intent.createChooser(emailIntent, "Send Email"));
        }
    }

    public int getAppVersionCode(){
        PackageInfo packageInfo = null;         //패키지에 대한 전반적인 정보

        //PackageInfo 초기화
        try{
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
            return -1;
        }

        return packageInfo.versionCode;
    }

    public String getAppVersionName(){
        PackageInfo packageInfo = null;         //패키지에 대한 전반적인 정보

        //PackageInfo 초기화
        try{
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
            return "";
        }

        return packageInfo.versionName;
    }
}
