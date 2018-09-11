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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ExecuteRequest {
	private Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
	public void Send(JSONObject j,HttpServletResponse response) throws ParseException, IOException, InvalidTokenOffsetsException, InterruptedException{
		
		HandleLucene handle=new HandleLucene();
		GZipUntils gz=new GZipUntils();
		String cmd=j.getString("command");
		Writer out = response.getWriter();
		switch (Integer.parseInt(cmd)) {
		case 101:{		//检索仓库段落内容
//			Map<String,List<String[]>> content=handle.GetSearch(Paths.repositorypath,j.getString("keywords"),100);
			Map<String,List<String[]>> content=handle.QuerySegments(Paths.repositorypath,j.getString("keywords"));
			JSONArray results=new JSONArray();
			JSONObject send=new JSONObject();
			for(Entry<String,List<String[]>> entry: content.entrySet()){
				JSONArray segments=new JSONArray();
				for(int i=0;i<entry.getValue().size();i++){
					JSONObject tem1=new JSONObject();
					tem1.accumulate("path",entry.getValue().get(i)[0]);
					tem1.accumulate("segment",entry.getValue().get(i)[1]);
					segments.add(tem1);
				}
				JSONObject tem=new JSONObject();
				FileIndexs findexs=new FileIndexs();
				Map<String,String[]> finfo=findexs.QueryFiles(Paths.filepath,"\""+entry.getKey()+"\"");
				String fpath="";
				String type="";
				for(String[] v : finfo.values()){
					fpath=v[4];
					type=v[5];
				}
				tem.accumulate("file",entry.getKey());
				tem.accumulate("fpath",fpath);
				tem.accumulate("type",type);
				tem.accumulate("segments",segments);
				results.add(tem);
			}
			send.accumulate("command","101");
	        send.put("token","");
	        send.accumulate("ResultList", results);
	        String body=gz.S2Gzip(send.toString());
			out.write(body);
			
			break;
		}
		case 102:{		//获取所有文档信息
			FileIndexs fileindex=new FileIndexs();
			Map<String,String[]> fre=fileindex.GetFileInfo(Paths.filepath);
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
	        String body=gz.S2Gzip(send.toString());
	        out.write(body);
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
	        StringBuilder indexpath=new StringBuilder(Paths.itempath);
	        indexpath.append("\\"+author+"\\");
	        indexpath.append(UUID.randomUUID().toString().replace("-","")+"\\");
	        ItemIndexs itemindex=new ItemIndexs();
	        int tatol=itemindex.AddIndexs(content,indexpath.toString());
	        JSONObject send=new JSONObject();
	        send.put("token","");
	        send.put("command","103");
	        send.accumulate("result",tatol);
	        String body=gz.S2Gzip(send.toString());
			out.write(body);
			String itempath=indexpath.toString();
			String fname=j.getString("file");
			String fpath=j.getString("fpath");
			String ftype=j.getString("type");
			String forder=j.getString("forder");
			String fsum=j.getString("fsum");
			String[] s={Action.Add,itempath,Paths.filepath,fname,author,df.format(d),String.valueOf(tatol),fpath,ftype,Paths.repositorypath,forder,fsum};
	        ServletDemo.item.put(s);
			break;
		}
		case 104:{		//删除索引
	        JSONArray objarry=j.getJSONArray("fileslist");
	        JSONObject tem=new JSONObject();
	        JSONObject send=new JSONObject();
	        send.put("token","");
	        send.put("command","104");
	        send.accumulate("result",1);
	        String body=gz.S2Gzip(send.toString());
	        int size=objarry.size();
			out.write(body);	
	        for(int i=0;i<size;i++){		
	        	tem=objarry.getJSONObject(i);
	        	String[] s={Action.Delete,tem.getString("file"),Paths.filepath,Paths.repositorypath,String.valueOf(i),String.valueOf(size-1)};
	        	ServletDemo.item.put(s);
	        }
			break;
		}
		case 105:{		//导入检索
			String file=j.getString("file");
			Map<String,List<String[]>> content=handle.GetAllSegments(Paths.repositorypath,file);
			JSONObject send=new JSONObject();
	        JSONArray lawslist=new JSONArray();
			if(!content.isEmpty()){
				FileIndexs findexs=new FileIndexs();
				Map<String,String[]> finfo=new HashMap<String,String[]>();
				for(String keywords : content.keySet()){
					finfo=findexs.QueryFiles(Paths.filepath,"\""+keywords+"\"");
				}
				String fpath="";
				String type="";
				for(String[] v : finfo.values()){
					fpath=v[4];
					type=v[5];
				}
				send.accumulate("fpath",fpath);
				send.accumulate("type",type);
				List<String[]> laws=content.get(file);
				int count = laws.size();
				for(int i=0;i<count;i++){
					String[] law=laws.get(i);
					JSONObject tem=new JSONObject();
					tem.accumulate("number",i);
					tem.accumulate("path",law[0]);
					tem.accumulate("law",law[1]);
					lawslist.add(tem);
				}
				send.accumulate("count",count);
			}
			else
				send.accumulate("count",0);
			send.accumulate("lawslist",lawslist);
	        send.put("token","");
	        send.put("command","105");
	        send.put("file",file);
	        String body=gz.S2Gzip(send.toString());
	        out.write(body);
	        break;
		}
		case 106:{		//查询文档段落
			String file=j.getString("file");
			int top=j.getInt("top"); 
			Map<String,List<String[]>> content=handle.GetAllSegments(swd.Paths.repositorypath,file,top);
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
	        send.put("command","106");
	        send.put("file",file);
	        String body=gz.S2Gzip(send.toString());
	        out.write(body);
	        break;
		}
		case 107:{		//多条件查询文档信息索引	
			JSONArray objarry=j.getJSONArray("FileList");
			String keywords=j.getString("keywords");
	        JSONObject tem=new JSONObject();
			JSONObject send=new JSONObject();
			List<String> range=new ArrayList<String>();
	        for(int i=0;i<objarry.size();i++){		
	        	tem=objarry.getJSONObject(i);
	        	range.add(tem.getString("fname"));
	        }
	        String[] fields={"file","findex"};
	        FileIndexs fileindex=new FileIndexs();
	        Map<String,String[]> finfo=fileindex.QueryFiles(Paths.filepath, fields, range, keywords);
	        JSONArray FileList=new JSONArray();
	        if(!finfo.isEmpty()){
	        	for(Entry<String,String[]> entry: finfo.entrySet()){
					String[] infos=entry.getValue();
					JSONObject tem1=new JSONObject();
					tem1.accumulate("file",entry.getKey());
					tem1.accumulate("author",infos[0]);
					tem1.accumulate("time",infos[1]);
					tem1.accumulate("segments",infos[2]);
					tem1.accumulate("findex", infos[3]);
					FileList.add(tem1);
				} 
	        }
	        send.accumulate("FileList", FileList);
	        send.accumulate("command","107");
	        send.accumulate("token", "");
	        String body=gz.S2Gzip(send.toString());
	        out.write(body);
	        break;
		}
		case 108:{		//单条件查询文档信息索引
			String keywords=j.getString("keywords");
	        FileIndexs fileindex=new FileIndexs();
	        Map<String,String[]> finfo=fileindex.QueryFiles(Paths.filepath,keywords);
	        JSONArray FileList=new JSONArray();
			JSONObject send=new JSONObject();
	        if(!finfo.isEmpty()){
	        	for(Entry<String,String[]> entry: finfo.entrySet()){
					String[] infos=entry.getValue();
					JSONObject tem1=new JSONObject();
					tem1.accumulate("file",entry.getKey());
					tem1.accumulate("author",infos[0]);
					tem1.accumulate("time",infos[1]);
					tem1.accumulate("segments",infos[2]);
					tem1.accumulate("findex", infos[3]);
					FileList.add(tem1);
				} 
	        }
	        send.accumulate("FileList", FileList);
	        send.accumulate("command","108");
	        send.accumulate("token", "");
	        String body=gz.S2Gzip(send.toString());
	        out.write(body);
			break;
		}
		}
	}


}
