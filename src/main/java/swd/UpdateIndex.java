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
			ti.setForceMergeDeletesPctAllowed(0);		//����ɾ�������ĺϲ�����Ϊ0����ɾ��segmentʱ���������кϲ�
	    	IndexWriterConfig reconfig=new IndexWriterConfig(analyzer); 
	    	reconfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
	    	reconfig.setMergePolicy(ti);		//���úϲ�����
	    	IndexWriter rewriter=new IndexWriter(redir,reconfig);
	    	*/
	    	HandleLucene handle=new HandleLucene();
	    	handle.CreateIndexWriter(swd.Paths.repositorypath);
			while(f){
				s=ServletDemo.item.take();
				System.out.println("�����д�����Ԫ��ʣ�ࣺ"+ServletDemo.item.size());
				if(s!=null){
					
					if(s[0].equals("a")){
						/*
						Path brpath=Paths.get(s[1]);
						FSDirectory brdir=FSDirectory.open(brpath);
						rewriter.addIndexes(brdir);
						rewriter.commit();
						System.out.println(s[1]+" "+"д������ֿ�ɹ���");
						 */	
						handle.InsertIndex(s[1]);
						System.out.println(s[1]+" "+"д������ֿ�ɹ���");
					}
					if(s[0].equals("d")){
						/*
						Term t=new Term("file",s[1]);								
						rewriter.deleteDocuments(t);								
						rewriter.forceMergeDeletes();
						rewriter.commit();
						System.out.println(s[1]+" "+"�Ƴ��ɹ���");
						*/
						handle.DeleteIndex(s[1]);
						System.out.println(s[1]+" "+"�Ƴ��ɹ���");
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
