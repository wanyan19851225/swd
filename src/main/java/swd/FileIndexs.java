package swd;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import org.apache.commons.collections4.map.LinkedMap;
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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

public class FileIndexs {

	private static IndexReader indexreader;
	private static FSDirectory fsdir;
	private static RAMDirectory ramdir;
	private static IndexWriter indexwriter;
	private static IndexWriter ramwriter;
	
	/*
 	 * Copyright @ 2018 Beijing Beidouht Co. Ltd. 
	 * All right reserved. 
	 * @author: wanyan 
	 * date: 2018-8-9 
	 *
	 * 该方法创建AddIndexWriter，如果已经创建则不在创建，如果未创建，则创建
	 *
	 * @params 
	 * 		   indexpath
	 * 				索引文件存储位置
	 * @return void	
	 *          
	 */
	public void CreateAddIndexWriter(String indexpath) throws IOException{
		
		Path inpath=Paths.get(indexpath);
		Analyzer analyzer = new StandardAnalyzer();		//创建标准分词器
		if(fsdir==null)		//判断磁盘索引是否创建，如果已经创建，则不再重新创建
			fsdir=FSDirectory.open(inpath);		//创建磁盘索引文件
		if(ramdir!=null)		//判断内存索引是否创建，如果已经创建则关闭内存索引，清空占用的内存
			ramdir.close();
		ramdir=new RAMDirectory();		//创建内存索引文件
		IndexWriterConfig ramconfig = new IndexWriterConfig(analyzer);
		ramwriter = new IndexWriter(ramdir,ramconfig);		//创建内存IndexWriter
		
		if(indexwriter==null){
			TieredMergePolicy ti=new TieredMergePolicy();
			ti.setForceMergeDeletesPctAllowed(0);		//设置删除索引的合并策略为0，有删除segment时，立即进行合并
			IndexWriterConfig fsconfig=new IndexWriterConfig(analyzer); 
			fsconfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			fsconfig.setMergePolicy(ti);
			indexwriter=new IndexWriter(fsdir,fsconfig); 
		}	
	}
	
	/*
 	 * Copyright @ 2018 Beijing Beidouht Co. Ltd. 
	 * All right reserved. 
	 * @author: wanyan 
	 * date: 2018-8-23 
	 *
	 * 该方法创建DeleteIndexWriter，如果已经创建则不在创建，如果未创建，则创建
	 *
	 * @params 
	 * 		   indexpath
	 * 				索引文件存储位置
	 * @return void	
	 * 				
	 */
	public void CreateDeleteIndexWriter(String indexpath) throws IOException{
		
		Path inpath=Paths.get(indexpath);
		if(fsdir==null)		//判断磁盘索引是否创建，如果已经创建，则不再重新创建
			fsdir=FSDirectory.open(inpath);		//创建磁盘索引文件
		
		if(indexwriter==null){
			Analyzer analyzer=new StandardAnalyzer();		//创建标准分词器
			TieredMergePolicy ti=new TieredMergePolicy();
			ti.setForceMergeDeletesPctAllowed(0);		//设置删除索引时的默认合并策略值为0
			IndexWriterConfig fsconfig=new IndexWriterConfig(analyzer);
			fsconfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			fsconfig.setMergePolicy(ti);		//设置合并策略
			indexwriter=new IndexWriter(fsdir,fsconfig);
		}	
	}
	
	
	public boolean CreateIndexReader(String indexpath) throws IOException{
		boolean f = false;
		try{
			if(indexreader==null){
				Path inpath=Paths.get(indexpath);
				if(fsdir==null)		//判断是否已经创建，如果没有创建则新建，否则复用
					fsdir=FSDirectory.open(inpath);		//创建磁盘索引文件
				IOContext iocontext=new IOContext();
				if(ramdir!=null)		//如果已经创建，则关闭
					ramdir.close();
				ramdir=new RAMDirectory(fsdir,iocontext);		//创建内存索引文件，并将磁盘索引文件放到内存中
				indexreader=DirectoryReader.open(ramdir);
			}
			else{
			
				if(indexwriter!=null){		//判断indexwriter是否实例化
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
	 * date: 2018-8-21 
	 * 
	 * AddFiles方法在为文档名称、文档作者、创建日期、文档段落总数建立索引文件
	 *
	 * @param file 
	 * 				以Map<文档名称,[文档作者，创建日期，段落总数]>形式传入参数
	 * 				
	 * @param indexpath
	 * 				索引文件路径
	 * 
	 * @return Boolean
	 * 				返回添加索引是否成功
	 * 
	 */	
	public Boolean AddFiles(Map<String,String[]> file,String indexpath){
		Boolean f=true;
		try {
			this.CreateAddIndexWriter(indexpath);
			FieldType type=new FieldType();
			type.setIndexOptions(IndexOptions.DOCS);
			type.setStored(true);		
			type.setTokenized(false);
			
			FieldType itype=new FieldType();
			itype.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
			itype.setStored(true);		
			itype.setTokenized(true);
			
			for(Entry<String, String[]> entry:file.entrySet()){
				String[] finfo=entry.getValue();
				Document doc=new Document();		
				doc.add(new Field("file",entry.getKey(),type));		//文档名称字段
				doc.add(new Field("findex",entry.getKey(),itype));		//文档名称检索字段
				doc.add(new Field("author",finfo[0],type));		//文档作者字段
				doc.add(new Field("time",finfo[1],type));		//创建日期字段
				doc.add(new NumericDocValuesField("segments",Integer.valueOf(finfo[2])));
				doc.add(new IntPoint("segments",Integer.valueOf(finfo[2])));		//段落总数以Int类型存储
				doc.add(new StoredField("segments",Integer.valueOf(finfo[2])));
				ramwriter.addDocument(doc);			
			}
			ramwriter.close();	
			indexwriter.addIndexes(ramdir); 		//程序结束后，将内存索引写入到磁盘索引中
	        indexwriter.commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			f=false;
		}		
		return f;	
	}
	
	/*
	 *
	 * Copyright @ 2018 Beijing Beidouht Co. Ltd. 
	 * All right reserved. 
	 * @author: wanyan 
	 * date: 2018-8-23 
	 * 
	 * GetAllFiles方法在文档信息索引中，查询所有的文件名称
	 *
	 * @params indexpath 
	 * 				索引文件存放目录
	 * 				
	 * @return List<String>
	 * 				返回文档名称		   				
	 */	
	public List<String> GetAllFiles(String indexpath){
		List<String> fname=new ArrayList<String>();
		try{
			this.CreateIndexReader(indexpath);
			if(indexreader!=null){
				Terms terms = MultiFields.getTerms(indexreader,"file");
				if(terms!=null){
					TermsEnum termsEnums = terms.iterator();
					while(termsEnums.next()!=null){
//						int num=termsEnums.docFreq();		//获取该Term出现的次数，即词频
						BytesRef byteRef=termsEnums.term();
						String term = new String(byteRef.bytes, byteRef.offset, byteRef.length,"UTF-8");
						fname.add(term);			 
					}
				}
			}
		}catch (IOException e) {
			// TODO Auto-generated catch block
			if(e.getClass().getSimpleName().equals("IndexNotFoundException"))		//当没有找到索引文件时，catch异常，并弹框提示
				System.out.println("未找到索引文件！");
			else
				e.printStackTrace();
		}
		return fname;
	}
	
	/*
	 *
	 * Copyright @ 2018 Beijing Beidouht Co. Ltd. 
	 * All right reserved. 
	 * @author: wanyan 
	 * date: 2018-8-23 
	 * 
	 * GetFileInfo方法在文档信息索引中，检索文件信息
	 *
	 * @params indexpath 
	 * 				索引文件存放目录
	 * 				
	 * @return Map<String,String[]>
	 * 				返回Map<文档名称,[文档作者，创建日期，段落总数]>		   				
	 */	
	public Map<String,String[]> GetFileInfo(String indexpath){
		Map<String,String[]> finfo=new HashMap<String,String[]>();
		List<String> fname=this.GetAllFiles(indexpath);
		try{
			this.CreateIndexReader(indexpath);
			if(indexreader!=null){
				IndexSearcher indexsearcher=new IndexSearcher(indexreader);
				for(int i=0;i<fname.size();i++){
					Term term=new Term("file",fname.get(i));
					TermQuery termquery=new TermQuery(term);
				    TopDocs topdocs=indexsearcher.search(termquery,1);
				    ScoreDoc[] hits=topdocs.scoreDocs;
		    		Document hitdoc=indexsearcher.doc(hits[0].doc);
		    	    String infos[]=new String[3];
		    	    infos[0]=hitdoc.get("author");
		    	    infos[1]=hitdoc.get("time");
		    	    infos[2]=String.valueOf(hitdoc.get("segments"));
		    	    finfo.put(fname.get(i),infos);
				}
			}
		}catch (IOException e) {
			// TODO Auto-generated catch block
			if(e.getClass().getSimpleName().equals("IndexNotFoundException")){
				System.out.println("检索文档作者和文档创建日期时失败，原因：未检索到索引文件");
				e.printStackTrace();
				return finfo;
			}
			else
				e.printStackTrace();
		}
		return finfo;
	}

	/*
	 *
	 * Copyright @ 2018 Beijing Beidouht Co. Ltd. 
	 * All right reserved. 
	 * @author: wanyan 
	 * date: 2018-8-23 
	 * 
	 * DeleteIndex方法根据给定文档名，删除文档信息索引中的文档信息
	 *
	 * @params filename
	 * 				文档名称
	 * 			indexpath 
	 * 				文档信息索引文件存放目录	
	 * @return Boolean	   				
	 * 
	 */
	public Boolean DeleteFile(String filename,String indexpath){
		Boolean f=true;
		try {
			this.CreateDeleteIndexWriter(indexpath);
			Term t=new Term("file",filename);
			indexwriter.deleteDocuments(t);
			indexwriter.forceMergeDeletes();		//删除索引时并不是立即从磁盘删除，而是放入回收站，可回滚操作，调用该方法后，是立即删除
			indexwriter.commit();  	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			f=false;
			e.printStackTrace();
		}
		return f;
	}

	/*
	 *
	 * Copyright @ 2018 Beijing Beidouht Co. Ltd. 
	 * All right reserved. 
	 * @author: wanyan 
	 * date: 2018-8-23 
	 * 
	 * 使用多条件组合查询
	 *
	 * @params indexpath
	 * 				索引文件所在目录
	 * 			fields
	 * 				指定从哪几个字段查询，使用String[]方式传参数
	 * 			range
	 * 				指定从哪些文档里搜索keywords
	 * 			keywords 
	 * 				从JTextField获取用户输入的多个关键字,使用String方式传递
	 * 			
	 * @return Map<Stirng,List<String[]>>
	 * 				将搜索结果以Map<文档名称，[文档作者，创建日期，段落总数，文档名称检索]>的映射关系，返回查询结果		   
	 *
	 */
	public Map<String,String[]> QueryFiles(String indexpath,String[] fields,List<String> range,String keywords){
		Map<String,String[]> finfo=new LinkedMap<String,String[]>();
		try {
			this.CreateIndexReader(indexpath);
			if(indexreader!=null){
				Analyzer analyzer=new StandardAnalyzer();
				IndexSearcher indexsearcher=new IndexSearcher(indexreader);
				BooleanQuery.Builder fbuilder=new BooleanQuery.Builder();
				int rnum=range.size();
				for(int i=0;i<rnum;i++){
					Term term=new Term(fields[0],range.get(i));
					TermQuery termquery=new TermQuery(term);
					fbuilder.add(termquery,BooleanClause.Occur.SHOULD);
				}
				BooleanQuery  fbooleanquery=fbuilder.build();
				BooleanQuery.Builder lbuilder=new BooleanQuery.Builder();
				QueryParser parser=new QueryParser(fields[1], analyzer);  
				Query query=parser.parse(keywords);
				lbuilder.add(query,BooleanClause.Occur.MUST);
				BooleanQuery  lbooleanquery=lbuilder.build();
				BooleanQuery.Builder builder=new BooleanQuery.Builder();
				builder.add(fbooleanquery,BooleanClause.Occur.MUST);
				builder.add(lbooleanquery,BooleanClause.Occur.MUST);
				BooleanQuery  booleanquery=builder.build();
				int top=indexreader.numDocs();		//获取有效索引文档总数
				if(top==0)	//判断有效索引文档数是否为0，如果为零则退出该方法，返回null
					return finfo;
				TopDocs topdocs=indexsearcher.search(booleanquery,top); 
				ScoreDoc[] hits=topdocs.scoreDocs;
			    int num=hits.length;
			    if(num==0)
			        return finfo;
			    SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<font color=red>","</font>"); //如果不指定参数的话，默认是加粗，即<b><b/>
				QueryScorer scorer = new QueryScorer(query);//计算得分，会初始化一个查询结果最高的得分
				Highlighter highlighter = new Highlighter(simpleHTMLFormatter, scorer);
				for(int i=0;i<num;i++){	
					Document hitdoc=indexsearcher.doc(hits[i].doc);
					String file=hitdoc.get("file");
					String infos[]=new String[4];
					infos[0]=hitdoc.get("author");
					infos[1]=hitdoc.get("time");
					infos[2]=String.valueOf(hitdoc.get("segments"));
					String fname=hitdoc.get("findex");
					if(fname!=null){
						TokenStream tokenStream = analyzer.tokenStream("fname",new StringReader(fname));
						String highlaws=highlighter.getBestFragment(tokenStream,fname);
						infos[3]=highlaws;
					}
					finfo.put(file, infos);	
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidTokenOffsetsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return finfo;	
	}
}
                                                  