package swd;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import redis.clients.jedis.Jedis;

/**
 * Servlet implementation class CommitIndex
 */
public class CommitIndex extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CommitIndex() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Servlet#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		JSONObject j=IOHttp.GetJson(request);
        JSONArray objarry=j.getJSONArray("lawslist");
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
        String id=UUID.randomUUID().toString().replace("-","");
        indexpath.append(j.getString("user")+"\\");
        indexpath.append(id+"\\");
        HandleLucene handle=new HandleLucene();
        int tatol=handle.AddIndex(content,indexpath.toString());
        
		Map<String,String> doc=new HashMap<String,String>();
		doc.put("auth",j.getString("user"));
		doc.put("file",j.getString("file"));
		doc.put("count",j.getString("count"));
		Date dt = new Date();      
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		doc.put("createtime",sdf.format(dt));
		try {
			ServletDemo.file.put(doc);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
        JSONObject send=new JSONObject();
        send.put("token","");
        send.accumulate("result",tatol);
		Writer out = response.getWriter();
		out.write(send.toString());
		String[] s={"a",indexpath.toString()};
        try {
			ServletDemo.item.put(s);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
