package swd;

import java.io.IOException;
import java.io.Writer;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ExecuteRequest {
		
	public void Send(JSONObject j,HttpServletResponse response) throws ParseException, IOException, InvalidTokenOffsetsException, InterruptedException{
		
//		Map<String,String> r=new HashMap<String,String>();
//		IOJson.Json2Map(j,r);
//		JSONObject obj=j.getJSONObject("cmd");
		String cmd=j.getString("command");
//		String cmd=r.get("cmd");
		Writer out = response.getWriter();
		switch (Integer.parseInt(cmd)) {
		case 101:{
			HandleLucene handle=new HandleLucene();
			IOJson ioj=new IOJson();
			Map<String,List<String[]>> content=handle.GetSearch(Paths.repositorypath,j.getString("keywords"),100);
			JSONObject send=ioj.Map2Json("","101",content);
			out.write(send.toString());
			break;
		}
		case 102:{
			HandleLucene handle=new HandleLucene();
			//Map<String,Integer> fre=handle.GetTermFreq(Paths.repositorypath);
			Map<String,String[]> fre=handle.GetFileInfo(Paths.repositorypath);
	        JSONArray filelist=new JSONArray();
			for(Entry<String,String[]> entry: fre.entrySet()){
				JSONObject tem=new JSONObject();
				tem.accumulate("file",entry.getKey());			//文档名称
				String[] info=entry.getValue();
				tem.accumulate("lawnum",info[2]);		//法条总数
				tem.accumulate("author",info[0]);		//法条作者
				tem.accumulate("time",info[1]);			//法条创建日期			
				filelist.add(tem);
			}
	        JSONObject send=new JSONObject();
	        send.put("token","");
	        send.put("command","102");
	        send.put("FileList",filelist);
	        out.write(send.toString());
	        break;
		}
		case 103:{		//提交索引
	        JSONArray objarry=j.getJSONArray("lawslist");
	        String author=j.getString("user");					//法条作者
			Date d=new Date(System.currentTimeMillis());		//法条创建日期		
			DateFormat df=new SimpleDateFormat("yyyy-MM-dd");
	        JSONObject tem=new JSONObject();
	        List<String[]> laws=new ArrayList<String[]>();
	        for(int i=0;i<objarry.size();i++){		
	        	tem=objarry.getJSONObject(i);
		        String[] law=new String[2];
	        	law[0]=tem.getString("path");
	        	law[1]=tem.getString("law");
	        	laws.add(law);
	        }
	        Map<String,List<String[]>> content=new HashMap<String,List<String[]>>();
	        content.put(j.getString("file"),laws);
	        StringBuffer indexpath=new StringBuffer(Paths.itempath);
	        indexpath.append(author+"\\");
	        indexpath.append(UUID.randomUUID().toString().replace("-","")+"\\");
//	        indexpath.append(j.getString("file"));
	        HandleLucene handle=new HandleLucene();
	        int tatol=handle.AddIndex(content,author,df.format(d),indexpath.toString());
	        JSONObject send=new JSONObject();
	        send.put("token","");
	        send.put("command","103");
	        send.accumulate("result",tatol);
			out.write(send.toString());
			String[] s={"a",indexpath.toString()};
	        ServletDemo.item.put(s);
			break;
		}
		case 104:{
	        JSONArray objarry=j.getJSONArray("fileslist");
	        JSONObject tem=new JSONObject();
	        JSONObject send=new JSONObject();
	        send.put("token","");
	        send.put("command","104");
	        send.accumulate("result",1);
			out.write(send.toString());	
	        for(int i=0;i<objarry.size();i++){		
	        	tem=objarry.getJSONObject(i);
	        	String[] s={"d",tem.getString("file")};
	        	ServletDemo.item.put(s);
		       // handle.DeleteIndex(tem.getString("file"),Paths.repositorypath);
	        }
			break;
		}
		case 105:{
			String file=j.getString("file");
			//System.out.println(file);
			HandleLucene handle=new HandleLucene();  
			Map<String,List<String[]>> content=handle.GetTermSearch(swd.Paths.repositorypath,file);
			JSONObject send=new JSONObject();
			if(!content.isEmpty()){
				List<String[]> laws=content.get(file);
				int count = laws.size();		//传给服务器的法条总数
				send.accumulate("count",count);
		        JSONArray lawslist=new JSONArray();
				for(int i=0;i<count;i++){
					String[] law=laws.get(i);
					JSONObject tem=new JSONObject();
					tem.accumulate("number",i);
					tem.accumulate("path",law[0]);
					tem.accumulate("law",law[1]);
					lawslist.add(tem);
				}
				send.accumulate("lawslist",lawslist);
			}
	        send.put("token","");
	        send.put("command","105");
	        send.put("file",file);
	        send.accumulate("result",1);
	        out.write(send.toString());
	        break;
		}
		case 106:{
			String file=j.getString("file");
			int top=j.getInt("top");
			HandleLucene handle=new HandleLucene();  
			Map<String,List<String[]>> content=handle.GetTermSearch(swd.Paths.repositorypath,file,top);
			JSONObject send=new JSONObject();
			if(!content.isEmpty()){
				List<String[]> laws=content.get(file);
				int count = laws.size();		//传给服务器的法条总数
				send.accumulate("count",count);
		        JSONArray lawslist=new JSONArray();
				for(int i=0;i<count;i++){
					String[] law=laws.get(i);
					JSONObject tem=new JSONObject();
					tem.accumulate("number",i);
					tem.accumulate("path",law[0]);
					tem.accumulate("law",law[1]);
					tem.accumulate("author",law[2]);
					tem.accumulate("time",law[3]);
					lawslist.add(tem);
				}
				send.accumulate("lawslist",lawslist);
				send.accumulate("result",1);
			}
			else{
				send.accumulate("lawslist","");
				send.accumulate("result",0);
			}
	        send.put("token","");
	        send.put("command","105");
	        send.put("file",file);
	        out.write(send.toString());
	        break;
		}
		
		
		}
	}


}
