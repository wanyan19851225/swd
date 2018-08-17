package swd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

public class IOHttp {

	public static JSONObject GetJson(HttpServletRequest req) throws UnsupportedEncodingException, IOException{
		
		BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream(),"utf-8"));  
        String line = null;  
        StringBuilder sb = new StringBuilder();  
        while ((line = br.readLine()) != null)
            sb.append(line);  
          
        //将json字符串转换为json对象  
        GZipUntils gzip=new GZipUntils();
        String re=gzip.Gzip2S(sb.toString());
        JSONObject json=JSONObject.fromObject(re);  
        
        return json;
	}
	
}
