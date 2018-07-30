package swd;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UpdateFile implements Runnable{
	public void run() {
		// TODO Auto-generated method stub
		try {
	    	boolean f=true;
	    	Map<String,String> s=new HashMap<String,String>();
	    	HandleLucene handle=new HandleLucene();
	    	handle.CreateIndexWriter(swd.Paths.filepath);
			while(f){
				s=ServletDemo.file.take();
				if(s!=null){
					handle.AddIndex(s);
					System.out.println(s.get("file")+" "+"：写入文件列表成功！");
				}
				System.out.println("文件队列中待处理元素剩余："+ServletDemo.file.size());
			}
			handle.CloseIndexWriter();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
