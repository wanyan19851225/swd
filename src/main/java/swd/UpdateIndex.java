package swd;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class UpdateIndex implements Runnable{
	public void run() {
		// TODO Auto-generated method stub
		try {
	    	boolean f=true;
	    	String[] s=null;
	    	HandleLucene handle=new HandleLucene();
	    	handle.CreateIndexWriter(swd.Paths.repositorypath);
			while(f){
				s=ServletDemo.item.take();
				System.out.println("队列中待处理元素剩余："+ServletDemo.item.size());
				if(s!=null){
					if(s[0].equals(Action.Add)){
						handle.InsertRepoIndex(s);
						System.out.println(s[1]+" "+"写入中央仓库成功！");
					}
					else if(s[0].equals(Action.Delete)){
						handle.DeleteRepoIndex(s);
						System.out.println(s[1]+" "+"移除成功！");
					}
					//ServletDemo.blockingQueue.poll(0,TimeUnit.MILLISECONDS);
				}
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
