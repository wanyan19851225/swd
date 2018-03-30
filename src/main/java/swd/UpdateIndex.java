package swd;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class UpdateIndex implements Runnable{
	public void run() {
		// TODO Auto-generated method stub
		try {
	    	boolean f=true;
	    	String[] s=null;
	    	/*
			Path repath=Paths.get("D:\\Lucene\\RemoteRepository\\index");
			FSDirectory redir = FSDirectory.open(repath);
			Analyzer analyzer=new StandardAnalyzer();
			TieredMergePolicy ti=new TieredMergePolicy();
			ti.setForceMergeDeletesPctAllowed(0);		//设置删除索引的合并策略为0，有删除segment时，立即进行合并
	    	IndexWriterConfig reconfig=new IndexWriterConfig(analyzer); 
	    	reconfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
	    	reconfig.setMergePolicy(ti);		//设置合并策略
	    	IndexWriter rewriter=new IndexWriter(redir,reconfig);
	    	*/
	    	HandleLucene handle=new HandleLucene();
	    	handle.CreateIndexWriter(swd.Paths.repositorypath);
			while(f){
				s=ServletDemo.item.take();
				System.out.println("队列中待处理元素剩余："+ServletDemo.item.size());
				if(s!=null){
					
					if(s[0].equals("a")){
						/*
						Path brpath=Paths.get(s[1]);
						FSDirectory brdir=FSDirectory.open(brpath);
						rewriter.addIndexes(brdir);
						rewriter.commit();
						System.out.println(s[1]+" "+"写入中央仓库成功！");
						 */	
						handle.InsertIndex(s[1]);
						System.out.println(s[1]+" "+"写入中央仓库成功！");
					}
					if(s[0].equals("d")){
						/*
						Term t=new Term("file",s[1]);								
						rewriter.deleteDocuments(t);								
						rewriter.forceMergeDeletes();
						rewriter.commit();
						System.out.println(s[1]+" "+"移除成功！");
						*/
						handle.DeleteIndex(s[1]);
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
