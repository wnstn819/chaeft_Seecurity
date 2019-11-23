package com.chaeft.seecurity;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class WebAnalysis {

	private static int hrefCount = 0; //href 개수
	private static int submitCount =0; //submit 개수
	private static int replaceCount =0; //replace 개수
	private static int openCount = 0; //open 개수
	private static int goCount = 0; //go 개수
	private static int urlCount = 0; //url 개수

	ArrayList<String> dangerList = new ArrayList<>(); // href 주소 저장
//	ArrayList<String> submitList = new ArrayList<>();  //submit 주소 저장
//	ArrayList<String> urlList = new ArrayList<>();  //url 주소 저장

	public void extract(String args)  {

		//TODO html code에서 String만 추출 (stripHtml)

		String substr = null; // 페이지 이동 함수 뒤의 " 까지 자르고 그 뒤부터 저장
		String substrResult= null; // substr에 저장된 문자부터 다음 " 나올 때 까지 저장

	    try {

			hrefCount = 0; //href 개수
			 submitCount =0; //submit 개수
			replaceCount =0; //replace 개수
			openCount = 0; //open 개수
			goCount = 0; //go 개수
			urlCount = 0; //url 개수
			Log.d("씨1발련아", args);
	    	BufferedReader bufReader = new BufferedReader(new StringReader(args));
	    	String line="";
	    	while((line = bufReader.readLine()) != null) {

	    			if(line.contains("a href=")) { // href 있는지 검사
						Log.d("테스투",line);
	    				//System.out.println(line);
	    				//href가 몇 번째로 나왔는지 찾고 href=" 까지 자른 뒷부분을 substr에 저장

	    			    substr = line.substring(line.indexOf("href=")+6);

						//substr에 저장된 문자열에서 따옴표가 나올때까지 찾은 다음 substrResult에 따옴표 전까지 저장
						//여기서 substrReuslt에 http:// 와 같은 url 주소가 저장
						if(substr.contains("\"")) {
	    			    substrResult = substr.substring(0,substr.indexOf("\""));  //
	    			    dangerList.add(substrResult);
	    			    }
	    				hrefCount++; //href 개수
	    				
	    			}else if(line.contains("submit")){ // submit 있는지 검사사
						//onclick이 어디로 이동하는지 보기 위해 onclick 뒤에 나오는 "까지 앞에 다 자르고 저장
						substr = line.substring(line.indexOf("onclick=")+9);

						//잘려진 substr을 다음 따옴표까지 가져오고 그 뒤는 다 자른뒤 submitList에 저장
						if(substr.indexOf("\"")>-1) {
		    			    substrResult = substr.substring(0,substr.indexOf("\""));  //
                            dangerList.add(substrResult);
		    			    }
	    				submitCount++; //submit 개수
	    				
	    			}else if(line.contains("location.replace")) { // replace 있는지 검사
	    				//System.out.println(line);

                        substr = line.substring(line.indexOf("location.replace=")+18);

                        //substr에 저장된 문자열에서 따옴표가 나올때까지 찾은 다음 substrResult에 따옴표 전까지 저장
                        //여기서 substrReuslt에 http:// 와 같은 url 주소가 저장
                        if(substr.contains("\"")) {
                            substrResult = substr.substring(0,substr.indexOf("\""));  //
                            dangerList.add(substrResult);
                        }
                        
	    				replaceCount++; //replace 개수

	    			}else if(line.contains("window.open")) {
	    				openCount++; //open(팝업) 개수

	    			}else if(line.contains("history.go")) {
	    				goCount++; //go 개수

	    			}else if(line.contains("url=")){
						//url이 어디로 이동하는지 보기 위해 url 뒤에 나오는 "까지 앞에 다 자르고 저장
						substr = line.substring(line.indexOf("url=")+5);

						//잘려진 substr을 다음 따옴표까지 가져오고 그 뒤는 다 자른뒤 submitList에 저장
						if(substr.contains("\"")) {
		    			    substrResult = substr.substring(0,substr.indexOf("\""));  //
                            dangerList.add(substrResult);
		    			    }
	    				urlCount++;
	    			}

	    	}
	    	bufReader.close();

	    }
	    catch (FileNotFoundException e) {
	    	e.getStackTrace();
	    }
	    catch(IOException e) {
	    	e.getStackTrace();
	    }
	}

//	private String stripHtml(String html) {
//		return Html.fromHtml(html).toString();
//	}
//
//	public int getHrefCount() {
//		return hrefCount;
//	}
//
//	public  int getSubmitCount() {
//		return submitCount;
//	}
//
//	public int getReplaceCount() {
//		return replaceCount;
//	}

	public int getGoCount() {
		return goCount;
	}

	public int getOpenCount() {
		return openCount;
	}

	public int getUrlCount() {
		return urlCount; // 자동 이동
	}

	public int getRedirectCount(){
		return hrefCount + submitCount + replaceCount + urlCount; // 잠재적 위험
	}

	public ArrayList<String> getDangerList() {
		return dangerList;
	}
}


