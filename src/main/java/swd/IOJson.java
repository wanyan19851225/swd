package swd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class IOJson {

	private HttpServletRequest req;
	
	public IOJson(HttpServletRequest request){
		this.req=request;
	}
	public IOJson(){
		
	}
		
	@SuppressWarnings("unchecked")
	public static void Json2Map(JSONObject obj,Map<String,String> m){
//		Map<String ,String> r=new HashMap<String ,String>();
		Iterator<String> keys = obj.keys();
		while(keys.hasNext()){
			String key = keys.next().toString();
			Object o=obj.get(key);
			m.put(key,o.toString());
			if(o instanceof JSONObject){
				JSONObject jo=(JSONObject)o;
				Json2Map(jo,m);
			}
			
		}	
	}
	
	public JSONObject Map2Json(String token,String cmd,Map<String,List<String[]>> content){
		JSONObject j=new JSONObject();
		j.put("token",token);
		j.put("cmd",cmd);
		
		StringBuffer text=new StringBuffer();
		int total=0;
		for(Entry<String,List<String[]>> entry: content.entrySet()){
			for(int i=0;i<entry.getValue().size();i++){
				text.append("  ");
				String[] item=entry.getValue().get(i);
				text.append(item[1]);
				text.append(" "+"<i>"+"--ÕªÂ¼×Ô");
				text.append("<a href=\"file:///D:/Lucene/src/"+entry.getKey()+"\">"+entry.getKey()+"</a>");
				text.append(" "+item[0]+"</i>");
				text.append("<br/>");
				text.append("<br/>");
				total++;
				}
		}
		
		j.put("data",text.toString());
		j.put("total",total);
		
		return j;
	}
	
	public JSONObject String2Json(String token,String cmd,String content){
		JSONObject j=new JSONObject();
		j.put("token",token);
		j.put("cmd",cmd);
		j.put("data",content);

		return j;
	}
	
	public static void main(String[] args){
		String s="{\"token\":\"\","
				+ "\"cmd\":{\"command\":\"10102\"},"
				+ "\"data\":{\"user_name\":\"13466528372\","
				+ "\"user_password\":\"34c0200ecd6bfc8505b54ceeadf448b8\","
				+ "\"device_token\":\"as1d5f49sd841we51f919as48dfasd\","
				+ "\"app_id\":\"0\","
				+ "\"user_imei\":\"123456\","
				+ "\"user_imsi\":\"22222222222222\","
				+ "\"user_phone\":\"13466528372\","
				+ "\"phone_version\":\"andriod5.0\","
				+ "\"phone_model\":\"HTC_9Mu\","
				+ "\"app_version\":\"1.0.0\"}"
				+ "}";
		IOJson ioj=new IOJson(null);
		JSONObject j=JSONObject.fromObject(s);
		Map<String ,String> r=new HashMap<String ,String>();
		ioj.Json2Map(j,r);
		
		for(String key:r.keySet()){
			System.out.println(key+":"+r.get(key));
		}
	}
}
