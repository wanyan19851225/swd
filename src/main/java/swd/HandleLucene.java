package swd;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import org.apache.commons.collections.map.LinkedMap;
//import org.apache.commons.collections4.map.LinkedMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.index.TieredMergePolicy;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;


/** 
 * Copyright @ 2017 Beijing Beidouht Co. Ltd. 
 * All right reserved. 
 * @author: wanyan 
 * date: 2017-10-27 
 */

public class HandleLucene {
	
	private static IndexWriter indexwriter;
	private FSDirectory dir;
	private static IndexReader indexreader;
	private static RAMDirectory ramdir;
	private static FSDirectory fsdir;
	
	public boolean CreateIndexReader(String indexpath) throws IOException{
		boolean f = false;
		try{
			if(indexreader==null){
				Path inpath=Paths.get(indexpath);
				if(fsdir==null)		//判断是否已经创建，如果没有创建则新建，否则复用
					fsdir=FSDirectory.open(inpath);	
				IOContext iocontext=new IOContext();
				if(ramdir!=null)		//如果已经创建，则关闭
					ramdir.close();
				ramdir=new RAMDirectory(fsdir,iocontext);		
				indexreader=DirectoryReader.open(ramdir);
			}
			else{
				if(indexwriter!=null){
					if(!indexwriter.isOpen())		//增加对indexwriter是否关闭的判断，2018-9-5
						this.CreateIndexWriter(indexpath);
					IndexReader tr=DirectoryReader.openIfChanged((DirectoryReader)indexreader,indexwriter);	
					if(tr!=null){	
						indexreader.close();	
						indexreader=tr;	
					}					
				}		
			}
			f=true;
		}catch (IOException e) {
		// TODO Auto-generated catch block
			if(e.getClass().getSimpleName().equals("IndexNotFoundException"))		//当没有找到索引文件时，catch异常，并弹框提示
				//JOptionPane.showMessageDialog(null, "未找到索引文件，请先创建索引文件", "警告", JOptionPane.ERROR_MESSAGE);
				f=false;
			else
				e.printStackTrace();
		}
		return f;
	}
	
	/*
	 *
	 * Copyright @ 2018 Beijing Beidouht Co. Ltd. 
	 * All right reserved. 
	 * @author: wanyan 
	 * date: 2018-8-28 
	 * 
	 * 该方法与使用单一查询方式
	 *
	 * @params indexpath
	 * 				索引文件所在目录
	 * 			keywords 
	 * 				从JTextField获取用户输入的关键字
	 * 			top
	 * 				从JRadio获取用户选择的搜索条数，在索引文件中根据相关度排序，返回前top条
	 * 			
	 * @return Map<Stirng,List<String[]>>
	 * 				将搜索结果以Map<文件名，[章节，法条]>的映射关系，返回查询结果		   
	 * 						 
	 * 
	 */
	
	public Map<String,List<String[]>> QuerySegments(String indexpath,String keywords) throws ParseException, IOException, InvalidTokenOffsetsException{
		
		Map<String,List<String[]>> files=new LinkedHashMap<String,List<String[]>>();
		try{
			this.CreateIndexReader(indexpath);
			if(indexreader!=null){	
				Analyzer analyzer=new StandardAnalyzer();		//创建标准分词器
				IndexSearcher indexsearcher=new IndexSearcher(indexreader);
				QueryParser parser=new QueryParser("law", analyzer);
				Query query=parser.parse(keywords.toString());
				int top=indexreader.numDocs();		//获取索引文件中有效文档总数
				if(top==0)	//判断索引文件中的有效文档总数是否为0，如果为零则退出该方法，返回null
					return files;
				TopDocs topdocs=indexsearcher.search(query,top); 
				ScoreDoc[] hits=topdocs.scoreDocs;
				int num=hits.length;  
				if(num==0)
					return files;	
				SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<b><font color=red>","</font></b>"); //如果不指定参数的话，默认是加粗，即<b><b/>
				QueryScorer scorer = new QueryScorer(query);//计算得分，会初始化一个查询结果最高的得分
				Highlighter highlighter = new Highlighter(simpleHTMLFormatter, scorer);		
				for(int i=0;i<num;i++){	
					Document hitdoc=indexsearcher.doc(hits[i].doc);
					String temp=hitdoc.get("file");
					String indexlaws[]=new String[2];
					Integer index=Integer.valueOf(hitdoc.get("path"));		
				    if(index/100000==999)		//判断章段落的索引号是否为999,当索引号为999时，表明没有章段落，索引号设置为0
				    	indexlaws[0]="第"+0+"章"+"&emsp";
				    else
				    	indexlaws[0]="第"+index/100000+"章"+"&emsp";
					index=index%100000;
					indexlaws[0]+="第"+index/1000+"节";
					String laws=hitdoc.get("law");
					if(laws!=null){
						TokenStream tokenStream = analyzer.tokenStream("laws",new StringReader(laws));
						Fragmenter displaysize= new SimpleFragmenter(laws.length());
						highlighter.setTextFragmenter(displaysize);
						String highlaws=highlighter.getBestFragment(tokenStream,laws);
						indexlaws[1]=highlaws;
						if(i==0){
							List<String[]> path=new ArrayList<String[]>();
							path.add(indexlaws);
							files.put(temp,path);
						}else{
							if(files.containsKey(temp)){
								files.get(temp).add(indexlaws);
							}else{
								List<String[]> path=new ArrayList<String[]>();
								path.add(indexlaws);
								files.put(temp,path);
							}     		           	 			
						}
					}
				}
			}	
		}catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
		}
       return files;	
	}
		
	/*
	 *
	 * Copyright @ 2017 Beijing Beidouht Co. Ltd. 
	 * All right reserved. 
	 * @author: wanyan 
	 * date: 2017-11-16 
	 * 
	 * 璇ユ柟娉曚笌GetMultipleSearch鍔熻兘锟�?鑷达紝鐩存帴杩斿洖娉曟潯鍐呭锛屽彧鑳芥煡璇㈠崟鏉′欢銆佸崟瀛楁锛岄拡瀵逛笉鍒嗚瘝瀛楁杩涜鏌ヨ
	 *
	 * @params indexpath
	 * 				绱㈠紩鏂囦欢锟�?鍦ㄧ洰锟�?
	 * 			keywords 
	 * 				浠嶫TextField鑾峰彇鐢ㄦ埛杈撳叆鐨勫叧閿瓧
	 * 			top
	 * 				浠嶫Radio鑾峰彇鐢ㄦ埛閫夋嫨鐨勬悳绱㈡潯鏁帮紝鍦ㄧ储寮曟枃浠朵腑鏍规嵁鐩稿叧搴︽帓搴忥紝杩斿洖鍓峵op锟�?
	 * 			
	 * @return Map<Stirng,List<String[]>>
	 * 				灏嗘悳绱㈢粨鏋滀互Map<鏂囦欢鍚嶏紝[绔犺妭锛屾硶鏉>鐨勬槧灏勫叧绯伙紝杩斿洖鏌ヨ缁撴灉		   
	 * @2017-11-17
	 * 			浣跨敤SortField鍜孲ort璋冪敤IndexSearch鏂规硶,瀵规悳绱㈢粨鏋滅敤path瀛楁鍗囧簭鎺掑簭						  
	 * @2018-8-24
	 * 			修复当未查询到索引时，由原来的返回null，改为返回files
	 */
	
	public Map<String,List<String[]>> GetAllSegments(String indexpath,String keywords) throws IOException{
		
		Map<String,List<String[]>> files=new LinkedHashMap<String,List<String[]>>();
    	List<String[]> path=new ArrayList<String[]>();
		
       	this.CreateIndexReader(indexpath); 
       	
       	if(indexreader!=null){
    		IndexSearcher indexsearcher=new IndexSearcher(indexreader);
    		int top=indexreader.numDocs();
			if(top==0)
				return files;
			Term term=new Term("file",keywords);
			TermQuery termquery=new TermQuery(term);
			SortField sortfield=new SortField("path",SortField.Type.INT,false);		//false为升序
			Sort sort=new Sort(sortfield);
			TopDocs topdocs=indexsearcher.search(termquery,top,sort); 
			ScoreDoc[] hits=topdocs.scoreDocs;
			int num=hits.length;
			if(num==0)		//查询结果为空
				return files;
			String temp=null;
			String laws;
			for(int i=0;i<num;i++){
				String indexlaws[]=new String[2];
				Document hitdoc=indexsearcher.doc(hits[i].doc);
				temp=hitdoc.get("file");	
				indexlaws[0]=hitdoc.get("path");
				laws=hitdoc.get("law");
				if(laws!=null){
					indexlaws[1]=laws;
					path.add(indexlaws);						
				}    
			}  	
			files.put(temp,path);
       	}
        return files;		
	}
	
	/*
	 *
	 * Copyright @ 2018 Beijing Beidouht Co. Ltd. 
	 * All right reserved. 
	 * @author: wanyan 
	 * date: 2018-1-23 
	 * 
	 * 锟矫凤拷锟斤拷锟斤拷GetMultipleSearch锟斤拷锟斤拷一锟铰ｏ拷直锟接凤拷锟截凤拷锟斤拷锟斤拷锟捷ｏ拷只锟杰诧拷询锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟街段ｏ拷锟斤拷圆锟斤拷执锟斤拷侄谓锟斤拷胁锟窖�
	 *
	 * @params indexpath
	 * 				锟斤拷锟斤拷锟侥硷拷锟斤拷锟斤拷目录
	 * 			keywords 
	 * 				锟斤拷JTextField锟斤拷取锟矫伙拷锟斤拷锟斤拷墓丶锟斤拷锟�
	 * 			top
	 * 				锟斤拷JRadio锟斤拷取锟矫伙拷选锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷募锟斤拷懈锟斤拷锟斤拷锟截讹拷锟斤拷锟津，凤拷锟斤拷前top锟斤拷
	 * 			
	 * @return Map<Stirng,List<String[]>>
	 * 				锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟組ap<锟侥硷拷锟斤拷锟斤拷[锟铰节ｏ拷锟斤拷锟斤拷]>锟斤拷映锟斤拷锟较碉拷锟斤拷锟斤拷夭锟窖拷锟斤拷
	 * 
	 * Modeified Date:2018-7-26
	 * 				锟斤拷锟接伙拷取锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷吆痛锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷侄锟街碉拷锟斤拷锟斤拷锟組ap<锟侥硷拷锟斤拷,[锟铰斤拷,锟斤拷锟斤拷,锟斤拷锟斤拷,锟斤拷锟斤拷]>锟斤拷映锟斤拷锟较�,锟斤拷锟截诧拷询锟斤拷锟�
	 * 		   					  
	 * 
	 */
	
	public Map<String,List<String[]>> GetAllSegments(String indexpath,String keywords,int top) throws IOException{
		
		String s=keywords.replaceAll("<[^>]+>","");
		Map<String,List<String[]>> files=new LinkedHashMap<String,List<String[]>>();
		List<String[]> path=new ArrayList<String[]>();
		
		this.CreateIndexReader(indexpath);
		
		if(indexreader!=null){
    		IndexSearcher indexsearcher=new IndexSearcher(indexreader);		
    		Term term=new Term("file",s);		
    		TermQuery termquery=new TermQuery(term);		
    		SortField sortfield=new SortField("path",SortField.Type.INT,false);		//false为升序      
    		Sort sort=new Sort(sortfield);	
    		TopDocs topdocs=indexsearcher.search(termquery,top,sort);         
    		ScoreDoc[] hits=topdocs.scoreDocs;        
    		String temp=null;        
    		int num=hits.length;        
    		if(num==0)
    			return files;        
    		for(int i=0;i<num;i++){        	
    			Document hitdoc=indexsearcher.doc(hits[i].doc);       	
    			temp=hitdoc.get("file");
    			String indexlaws[]=new String[4];
    			Integer index=Integer.valueOf(hitdoc.get("path"));
    			if(index/100000==999)		//判断章段落的索引号是否为999,当索引号为999时，标明没有章段落
    				indexlaws[0]="第"+0+"章"+" ";
    			else
    				indexlaws[0]="第"+index/100000+"章"+" ";
    			index=index%100000;
    			indexlaws[0]+="第"+index/1000+"节";
    			String laws=hitdoc.get("law");
    			String author=hitdoc.get("author");
    			String time=hitdoc.get("time");
    			if(laws!=null){
    				indexlaws[1]=laws;
    				indexlaws[2]=author;
    				indexlaws[3]=time;
    				path.add(indexlaws);
    			}
    		}
    		files.put(temp,path);
		}
       return files;	
	}
	
	
	/*
	 *
	 * Copyright @ 2017 Beijing Beidouht Co. Ltd. 
	 * All right reserved. 
	 * @author: wanyan 
	 * date: 2017-11-10 
	 * 
	 * DeleteIndex删除仓库中的段落索引成功后，删除文档信息索引
	 *
	 * @params String[]
	 * 				以String[操作类型，文档名称，文档信息索引路径]形式传入参数
	 * @return Boolean
	 * 
	 * @2017-11-15
	 * 			浣跨敤鏂规硶forceMergeDeletes()锛屽疄鐜扮珛鍗冲垹锟�?
	 * @2018-8-23
	 * 			修改传入参数为String[]
	 * 			删除仓库中的段落索引成功后，删除文档信息索引				   				
	 * @2018-9-5
	 * 			增加indexwriter是否关闭的判断，如果关闭则调用createindexwriter方法重新创建indexwriter
	 * 			当删除最后一个文档时，才执行提交操作
	 */

	public Boolean DeleteRepoIndex(String[] s){
		Boolean f=true;
		if(indexwriter!=null){
			if(!indexwriter.isOpen())
				this.CreateIndexWriter(s[3]);
			try {
				Term t=new Term("file",s[1]);
				indexwriter.deleteDocuments(t);
				if(Integer.valueOf(s[4])==Integer.valueOf(s[5])){
					indexwriter.forceMergeDeletes();
					indexwriter.commit();
				}
				FileIndexs fileindex=new FileIndexs();
				f=fileindex.DeleteFile(s[1],s[2],Integer.valueOf(s[4]),Integer.valueOf(s[5]));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				f=false;
				e.printStackTrace();
			}								
		}
		else
			f=false;
		return f;
	}


	public void CreateIndexWriter(String indexpath){
		try{
			if(indexwriter==null||!indexwriter.isOpen()){
				Path inpath=Paths.get(indexpath);
				FSDirectory dir = FSDirectory.open(inpath);
				Analyzer analyzer=new StandardAnalyzer();
				TieredMergePolicy ti=new TieredMergePolicy();
				ti.setForceMergeDeletesPctAllowed(0);		
				IndexWriterConfig config=new IndexWriterConfig(analyzer); 
				config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
				config.setMergePolicy(ti);		
				indexwriter=new IndexWriter(dir,config);
			}
		}catch (IOException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	/*
	 *
	 * Copyright @ 2017 Beijing Beidouht Co. Ltd. 
	 * All right reserved. 
	 * @author: wanyan 
	 * date: 2017-11-10 
	 * 
	 * InsertRepoIndex在仓库中的创建段落索引成功后，创建文档信息索引
	 *
	 * @params String[]
	 * 				以String[操作类型，缓存索引路径，文档信息索引路径，文档名称，文档作者，文档创建日期，段落总数，文档路径，文档类型]形式传入参数
	 * @return Boolean
	 * 
	 * @2018-8-23
	 * 			修改传入参数为String[]
	 * 			增加在仓库中的创建段落索引成功后，创建文档信息索引
	 * @2018-9-5
	 * 			增加indexwriter是否关闭的判断，如果关闭则调用createindexwriter方法重新创建indexwriter
	 */
	public Boolean InsertRepoIndex(String[] s){
		Boolean f=true;
		if(indexwriter!=null){
			if(!indexwriter.isOpen())
				this.CreateIndexWriter(s[9]);
			try {
				Path brpath=Paths.get(s[1]);
				FSDirectory brdir = FSDirectory.open(brpath);
				indexwriter.addIndexes(brdir);
				if(s[10].equals(s[11]))
					indexwriter.commit();
				
				Map<String,String[]> finfo=new HashMap<String,String[]>();
				String file=s[3];
				String[] infos=new String[5];
				System.arraycopy(s,4,infos,0,5);
				finfo.put(file, infos);
				FileIndexs fileindex=new FileIndexs();
				f=fileindex.AddFiles(finfo,s[2],Integer.valueOf(s[10]),Integer.valueOf(s[11]));
				
				ItemIndexs itemindex=new ItemIndexs();
				itemindex.DeleteIndexs(s[1]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				f=false;
				e.printStackTrace();
			}
		}
		else
			f=false;
		return f;
	}
	
	public void CloseIndexWriter() throws IOException{
		if(dir!=null)
			dir.close();
		if(indexwriter!=null)
			indexwriter.close();
	}
	
	public static void main(String[] args) throws Exception{
	}
}
