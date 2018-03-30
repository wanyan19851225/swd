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
          
        //��json�ַ���ת��Ϊjson����  
        JSONObject json=JSONObject.fromObject(sb.toString());  
        
        return json;
	}
	
}
