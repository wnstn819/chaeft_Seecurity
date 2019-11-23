package com.chaeft.seecurity;

import android.app.AlertDialog;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class ClipboardService extends Service {
    private ClipboardManager mCM;
    IBinder mBinder;
    int mStartMode;

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (intent == null){
            return Service.START_STICKY; //service 종료 시 자동으로 다시 시작
        } else {
            mCM = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            mCM.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {

                @Override
                public void onPrimaryClipChanged() {
                    ClipData clip = mCM.getPrimaryClip();
                    ClipData.Item clipItem = clip.getItemAt(0);
                    String newClipText = clipItem.getText().toString();
                    //primaryClip 을 갖고와 clipItem 에 저장, 스트링 형식으로 newClipText 에 저장.

                    if (newClipText.contains("http") || newClipText.matches("(.*)://(.*)")
                    || newClipText.matches("(.*).co(.*)")|| newClipText.matches("(.*).net(.*)")  || newClipText.matches("(.*).kr(.*)"))  {
                        if(TextUtils.isEmpty(newClipText)) {
                            Toast.makeText(getApplicationContext(), "URL 복사를 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if(!newClipText.contains("http")){
                            newClipText = "http://" + newClipText;
                        }
                        //Toast.makeText(getApplicationContext(), newClipText, Toast.LENGTH_LONG).show();
                        //newClipText 에 http 나 :// 가 포함돼 있으면 URL 로 간주 추후 URL 감지 코드 추가
                        //https://stackoverflow.com/questions/5713558/detect-and-extract-url-from-a-string

                        //TODO URL 이 포함된 SMS Text 전체를 복사한 경우, SMS 의 모든 Text 가 title 에 뜸

                        UrlAnalysis urlAnalysis = new UrlAnalysis();
                        urlAnalysis.setTitle(newClipText);
                        urlAnalysis.doAnalysis();
                        final String domain = urlAnalysis.getDomain();

                        final String finalNewClipText = newClipText;

                        new Thread(){
                            public void run(){
                                Bundle bundle = new Bundle();



                                GetJson getJson = new GetJson();
                                getJson.setToUrl("http://chaeft.com/seecurity/readOrgDomain.asp?ps_domain=" + domain);
                                Log.d("씨1바려ㅏㅇ", domain);
                                String jsonData = getJson.getJsonData();

                                JsonParser jsonParser = new JsonParser();
                                Log.d("테스투투1", jsonData);
                                jsonParser.setJsonData(jsonData);
                                jsonParser.domainParser();
                                final String getDomain = jsonParser.getDomain();
                                bundle.putString("getDomain", getDomain);
                                Log.d("테스투투2",getDomain);


                                if(getDomain.equals("null")) {
                                    Intent intent = new Intent(ClipboardService.this, PopupActivity.class);
                                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("title", finalNewClipText);
                                    startActivity(intent);
                                }else{

                                    if (!TextUtils.isEmpty(getDomain)){
                                        final AlertDialog.Builder builder = new AlertDialog.Builder(ClipboardService.this);
                                        builder.setTitle("경고").setMessage("해당 URL은 피싱 도메인 신고 이력이 있습니다. 이 URL 대신 권장 URL로의 연결을 권장합니다.\n"+getDomain + "로 연결하시겠습니까?");
                                        builder.setPositiveButton("권장 URL 연결", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                final String finalGetDomain = getDomain;
                                                final String finalGetDomainResult;

                                                if (!finalGetDomain.contains("http")) {
                                                    finalGetDomainResult = "http://" + finalGetDomain;
                                                } else {
                                                    finalGetDomainResult = finalGetDomain;
                                                }

                                                Intent intent = new Intent(ClipboardService.this, PopupActivity.class);
                                                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                                intent.putExtra("title", finalGetDomainResult);
                                                startActivity(intent);
                                            }
                                        });
                                        builder.setNegativeButton("기존 URL 연결", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                final String finalGetDomain = getDomain;
                                                final String finalGetDomainResult;

                                                if (!finalGetDomain.contains("http")) {
                                                    finalGetDomainResult = "http://" + finalGetDomain;
                                                } else {
                                                    finalGetDomainResult = finalGetDomain;
                                                }

                                                Intent intent = new Intent(ClipboardService.this, PopupActivity.class);
                                                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                                intent.putExtra("title", finalGetDomainResult);
                                                startActivity(intent);
                                            }
                                        });



//                                        Handler mHandler = new Handler(Looper.getMainLooper());
//                                        mHandler.postDelayed(new Runnable() {
//                                            @Override
//                                            public void run() {
//
//
//
//
//                                                AlertDialog alertDialog = builder.create();
//                                                alertDialog.show();
//
//                                            }
//                                        }, 0);

                                        Intent intent = new Intent(ClipboardService.this, MainActivity.class);
                                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                        intent.putExtra("title", finalNewClipText);
                                        startActivity(intent);
                                    }else{
                                        //Toast.makeText(PopupActivity.this,"받아온 도메인이 null이애요.",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }.start();

//                        Intent intent = new Intent(ClipboardService.this, PopupActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    //    intent.putExtra("title", newClipText);



                    }
                }
            });
        }
        return mStartMode;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
