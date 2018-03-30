package swd;

import java.io.File;
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
import org.apache.lucene.document.TextField;
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
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;


/** 
 * Copyright @ 2017 Beijing Beidouht Co. Ltd. 
 * All right reserved. 
 * @author: wanyan 
 * date: 2017-10-27 
 */

public class HandleLucene {
	
	private IndexWriter writer;
	private FSDirectory dir;
	
	/*
	 * 扫描文件，建立索引文�?
	 *
	 * @params fdir
	 * 				文件�?
	 * 		   indexpath
	 * 				索引文件存储位置
	 * @return Integer	
	 * 				返回�?有文件的索引�?
	 * 
	 * @2017-11-1
	 * 			修改返回参数为Integer,返回�?有文件的索引�?
	 * @2017-11-7
	 * 			增加对自定义文档和正式格式法条的判断  
	 * @2017-11-10
	 *          修改file字段的索引�?�项，由IndexOptions.NONE改为IndexOptions.DOCS,不建立索引改为只对doc建立索引，这样能够根据file字段删除索引
	 * @2017-11-17
	 * 			修复当文件夹中没有法条文档时，创建空索引的bug
	 * 			增加调用GetIndexOflaw或�?�GetIndexOfdocment后，是否为空的判断，如果为空则跳过不建立索引
	 *			增加对path字段，增加NumericDocValuesField字段，用于排�?
	 *          
	 */
/*	
	public Integer CreateIndex(String fdir,String indexpath) throws IOException{
		
		File[] files = new File(fdir).listFiles();
		
		int filesnum=files.length;
		
		int totalofindex=0;
		
		if(filesnum!=0){		//判断文件夹下是否有法条文�?
		
			Path inpath=Paths.get(indexpath);
				
			Analyzer analyzer = new StandardAnalyzer();		//创建标准分词�?
	
			FSDirectory fsdir=FSDirectory.open(inpath);		//创建磁盘索引文件
		
			RAMDirectory ramdir=new RAMDirectory();		//创建内存索引文件
	
			IndexWriterConfig ramconfig = new IndexWriterConfig(analyzer);
		
			IndexWriter ramiwriter = new IndexWriter(ramdir,ramconfig);		//创建内存IndexWriter
		
			IOWord ioword=new IOWord();
		
			for(int i=0;i<files.length;i++){
			
				String fname=files[i].getName().split("\\.")[0];
				String check=fname.substring(fname.length()-2,fname.length());
				Map<Integer,String> law=new HashMap<Integer,String>();
			
				if(check.contains("�?")||check.contains("条例")||check.contains("草案")){
			
					law=ioword.GetIndexOflaw(files[i]);
				}
				else{
					law=ioword.GetIndexOfdocment(files[i]);
				}
				
			
				if(totalofindex==-1)
					totalofindex=0;
				
				totalofindex+=law.size();
				
				if(!law.isEmpty()){
			
					for (Integer key:law.keySet()){ 
				
//						String laws=law.get(302011);
				
//						System.out.println(laws);
					
						Document doc=new Document();		//创建Document,每一个发条新建一�?		
						FieldType fieldtype=new FieldType();
						fieldtype.setIndexOptions(IndexOptions.DOCS);
						fieldtype.setStored(true);		
						fieldtype.setTokenized(false);
						doc.add(new Field("file",files[i].getName(),fieldtype));		//文档名称存储，不分词
						doc.add(new NumericDocValuesField("path",key));
						doc.add(new IntPoint("path",key));		//法条索引以Int类型存储
						doc.add(new StoredField("path",key));
						doc.add(new Field("law",law.get(key),TextField.TYPE_STORED));		//法条内容索引、分词，存储
		    	
						ramiwriter.addDocument(doc);		//将法条索引添加到内存索引�?			  
					}
				
			}
				else if(totalofindex==0)
					totalofindex=-1;
		
			}	
			ramiwriter.close();
		
			IndexWriterConfig fsconfig=new IndexWriterConfig(analyzer); 
			fsconfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
			IndexWriter fsiwriter=new IndexWriter(fsdir,fsconfig);   
			fsiwriter.addIndexes(ramdir); 		//程序结束后，将内存索引写入到磁盘索引�?
			fsiwriter.close();
		}
		else
			totalofindex=-1;
        return totalofindex;
	}
*/
	
	/*
	 *
	 * Copyright @ 2017 Beijing Beidouht Co. Ltd. 
	 * All right reserved. 
	 * @author: wanyan 
	 * date: 2017-10-29 
	 * 
	 * 该方法与使用单一查询方式
	 *
	 * @params indexpath
	 * 				索引文件�?在目�?
	 * 			keywords 
	 * 				从JTextField获取用户输入的关键字
	 * 			top
	 * 				从JRadio获取用户选择的搜索条数，在索引文件中根据相关度排序，返回前top�?
	 * 			
	 * @return Map<Stirng,List<String[]>>
	 * 				将搜索结果以Map<文件名，[章节，法条]>的映射关系，返回查询结果		   
	 * 						  
	 * @2017-10-31
	 * 				修改高亮截取的默认字符数，修改为根据法条内容的字符数显示
	 */
	
	public Map<String,List<String[]>> GetSearch(String indexpath,String keywords,int top) throws ParseException, IOException, InvalidTokenOffsetsException{
		
		Map<String,List<String[]>> files=new LinkedHashMap<String,List<String[]>>();
		
		Path inpath=Paths.get(indexpath);
		
		try{
		
		FSDirectory fsdir=FSDirectory.open(inpath);		//创建磁盘索引文件
		
		IOContext iocontext=new IOContext();

		RAMDirectory ramdir=new RAMDirectory(fsdir,iocontext);		//创建内存索引文件，并将磁盘索引文件放到内存中
		
		Analyzer analyzer=new StandardAnalyzer();		//创建标准分词�?

		IndexReader indexreader=DirectoryReader.open(ramdir);

		IndexSearcher indexsearcher=new IndexSearcher(indexreader);
		
//		Term term=new Term("law",keywords);
		
//		TermQuery termquery=new TermQuery(term);
//		模糊查询		
//		FuzzyQuery fuzzquery=new FuzzyQuery(term);
// 		短语查询		
//		 PhraseQuery.Builder builder = new PhraseQuery.Builder();
//		 builder.add(term);
//		 PhraseQuery phrasequery=builder.build();
//		查询分析�?	
			
        QueryParser parser=new QueryParser("law", analyzer);
           
        Query query=parser.parse(keywords.toString());
        
        TopDocs topdocs=indexsearcher.search(query,top); 
        
        ScoreDoc[] hits=topdocs.scoreDocs;
        
        int num=hits.length;
        
        if(num==0){
        	return null;
        }
        
        //此处加入的是搜索结果的高亮部�?
        SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<b><font color=red>","</font></b>"); //如果不指定参数的话，默认是加粗，�?<b><b/>
        QueryScorer scorer = new QueryScorer(query);//计算得分，会初始化一个查询结果最高的得分
//        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer); //根据这个得分计算出一个片�?
        Highlighter highlighter = new Highlighter(simpleHTMLFormatter, scorer);
 //       highlighter.setTextFragmenter(fragmenter); //设置�?下要显示的片�?
  
        for(int i=0;i<num;i++){
        	
        	Document hitdoc=indexsearcher.doc(hits[i].doc);
        	
    		String temp=hitdoc.get("file");
    		String indexlaws[]=new String[2];
    		Integer index=Integer.valueOf(hitdoc.get("path"));		
    		indexlaws[0]="��"+index/100000+"��"+" ";
    		index=index%100000;
    		indexlaws[0]+="��"+index/1000+"��";
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
        ramdir.close();
        indexreader.close();
        fsdir.close();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			if(e.getClass().getSimpleName().equals("IndexNotFoundException"))		//当没有找到索引文件时，catch异常，并弹框提示
				JOptionPane.showMessageDialog(null, "未找到索引文件，请先创建索引文件", "警告", JOptionPane.ERROR_MESSAGE);
			else
				e.printStackTrace();
		}
        return files;
		
	}

	/*
	 *
	 * Copyright @ 2017 Beijing Beidouht Co. Ltd. 
	 * All right reserved. 
	 * @author: wanyan 
	 * date: 2017-10-31 
	 * 
	 * 该方法与GetSearch功能�?致，直接返回法条内容，只是可以使用多条件、多域进行查�?
	 *
	 * @params indexpath
	 * 				索引文件�?在目�?
	 * 			keywords 
	 * 				从JTextField获取用户输入的多个关键字,使用String[]方式传�?�，[关键字，查询条件]
	 * 			top
	 * 				从JRadio获取用户选择的搜索条数，在索引文件中根据相关度排序，返回前top�?
	 * 			
	 * @return Map<Stirng,List<String[]>>
	 * 				将搜索结果以Map<文件名，[章节，法条]>的映射关系，返回查询结果		   
	 * 						  
	 * @2017-10-31
	 * 			修改使用BooleanQuery方式实现多条件查�?
	 * @2017-11-1
	 * 			修改同时支持单条件查询和多条件查�?
	 * 			修改使用查询分析器QueryParser实现多域、多字段、多条件查询
	 * @2017-11-16
	 * 			修改使用MultiFieldQueryParser类实现多字段、多条件查询
	 * 
	 */
	
	@SuppressWarnings("resource")
	public Map<String,List<String[]>> GetMultipleSearch(String indexpath,List<String[]> keywordset,int top) throws IOException, ParseException, InvalidTokenOffsetsException{
		
		Map<String,List<String[]>> files=new LinkedHashMap<String,List<String[]>>();
		
		Path inpath=Paths.get(indexpath);
		
		FSDirectory fsdir=FSDirectory.open(inpath);		//创建磁盘索引文件
		
		IOContext iocontext=new IOContext();

		RAMDirectory ramdir=new RAMDirectory(fsdir,iocontext);		//创建内存索引文件，并将磁盘索引文件放到内存中
		
		Analyzer analyzer=new StandardAnalyzer();		//创建标准分词�?

		IndexReader indexreader=DirectoryReader.open(ramdir);

		IndexSearcher indexsearcher=new IndexSearcher(indexreader);
		
		int total=keywordset.size();
		
//		if(total==0)
//			return null;
		
		String[] keywords=new String[total];
		String[] fields=new String[total];
		BooleanClause.Occur[] flags=new BooleanClause.Occur[total];
	
		for(int i=0;i<total;i++){
			keywords[i]=keywordset.get(i)[2];
			fields[i]=keywordset.get(i)[1];
			if(keywordset.get(i)[0].equals("AND")){
				flags[i]=BooleanClause.Occur.MUST;	
			}else if(keywordset.get(i)[0].equals("OR")){
				flags[i]=BooleanClause.Occur.SHOULD;
			}else if(keywordset.get(i)[0].equals("NOT")){
				flags[i]=BooleanClause.Occur.MUST_NOT;
			}
		}
		
		Query query=MultiFieldQueryParser.parse(keywords,fields,flags,analyzer);

/*
		BooleanClause.Occur[] flags=new BooleanClause.Occur[total];
		Term[] term=null;
		TermQuery[] termquery=null;
		BooleanClause[] flags;
		BooleanQuery.Builder builder=new BooleanQuery.Builder();
		
*/  
        
       
/*		
		for(int i=0;i<total;i++){
			term[i]=new Term("law",keywordset.get(i)[0]);
			termquery[i]=new TermQuery(term[i]);
			if(keywordset.get(i)[1].equals("AND")||keywordset.get(i)[1].equals("")){
				builder.add(termquery[i],BooleanClause.Occur.MUST);
			}else if(keywordset.get(i)[i].equals("OR")){
				builder.add(termquery[i],BooleanClause.Occur.SHOULD);
			}else if(keywordset.get(i)[i].equals("NOT")){
				builder.add(termquery[i],BooleanClause.Occur.MUST_NOT);
			}	
		}

		BooleanQuery  booleanquery=builder.build();
*/        
        TopDocs topdocs=indexsearcher.search(query,top); 
        
        ScoreDoc[] hits=topdocs.scoreDocs;
        
        int num=hits.length;
        
        if(num==0){
        	return null;
        }
        
        //此处加入的是搜索结果的高亮部�?
        SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<b><font color=red>","</font></b>"); //如果不指定参数的话，默认是加粗，�?<b><b/>
        QueryScorer scorer = new QueryScorer(query);//计算得分，会初始化一个查询结果最高的得分
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer); //根据这个得分计算出一个片�?
        Highlighter highlighter = new Highlighter(simpleHTMLFormatter, scorer);
        highlighter.setTextFragmenter(fragmenter); //设置�?下要显示的片�?
  
        for(int i=0;i<num;i++){
        	
        	Document hitdoc=indexsearcher.doc(hits[i].doc);
        	
    		String temp=hitdoc.get("file");
    		String indexlaws[]=new String[2];
    		Integer index=Integer.valueOf(hitdoc.get("path"));		
    		indexlaws[0]="�?"+index/100000+"�?"+"&emsp";
    		index=index%100000;
    		indexlaws[0]+="�?"+index/1000+"�?";
    		String laws=hitdoc.get("law");
    		if(laws!=null){
    			TokenStream tokenStream=analyzer.tokenStream("laws",new StringReader(laws));
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
        indexreader.close();
        fsdir.close();
        return files;
		
	}

	/*
	 *
	 * Copyright @ 2017 Beijing Beidouht Co. Ltd. 
	 * All right reserved. 
	 * @author: wanyan 
	 * date: 2017-11-18 
	 * 
	 * 该方法与GetSearch功能�?致，直接返回法条内容，只是可以使用多条件、多域进行查�?
	 *
	 * @params indexpath
	 * 				索引文件�?在目�?
	 * 			fields
	 * 				指定从哪几个字段查询，使用String[]方式传参�?
	 * 			range
	 * 				指定从哪些文档里搜索keywords
	 * 			keywords 
	 * 				从JTextField获取用户输入的多个关键字,使用String方式传�??
	 * 			top
	 * 				从JRadio获取用户选择的搜索条数，在索引文件中根据相关度排序，返回前top�?
	 * 			
	 * @return Map<Stirng,List<String[]>>
	 * 				将搜索结果以Map<文件名，[章节，法条]>的映射关系，返回查询结果		   
	 *
	 * @2017-11-18
	 * 			增加List<String>参数，用于指定在哪些文档内查询法�?
	 * 			修改keywords参数类型为String						  
	 * 
	 */

	public Map<String,List<String[]>> GetMultipleSearch(String indexpath,String[] fields,List<String> range,String keywords,int top) throws IOException, ParseException, InvalidTokenOffsetsException{
		
		Map<String,List<String[]>> files=new LinkedHashMap<String,List<String[]>>();
		
		Path inpath=Paths.get(indexpath);
		
		FSDirectory fsdir=FSDirectory.open(inpath);		//创建磁盘索引文件
		
		IOContext iocontext=new IOContext();

		RAMDirectory ramdir=new RAMDirectory(fsdir,iocontext);		//创建内存索引文件，并将磁盘索引文件放到内存中
		
		Analyzer analyzer=new StandardAnalyzer();		//创建标准分词�?

		IndexReader indexreader=DirectoryReader.open(ramdir);

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
			
		TopDocs topdocs=indexsearcher.search(booleanquery,top); 
		        
		ScoreDoc[] hits=topdocs.scoreDocs;
		        
		int num=hits.length;
		        
		if(num==0){
		        	
			return null;
		        
		}
		        
		    //此处加入的是搜索结果的高亮部�?
		SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<b><font color=red>","</font></b>"); //如果不指定参数的话，默认是加粗，�?<b><b/>
		    
		QueryScorer scorer = new QueryScorer(query);//计算得分，会初始化一个查询结果最高的得分
		    
//		Fragmenter fragmenter = new SimpleSpanFragmenter(scorer); //根据这个得分计算出一个片�?
		    
		Highlighter highlighter = new Highlighter(simpleHTMLFormatter, scorer);

//          highlighter.setTextFragmenter(fragmenter); //设置�?下要显示的片�?
		  
		for(int i=0;i<num;i++){
		        	
			Document hitdoc=indexsearcher.doc(hits[i].doc);
		        	
		    String temp=hitdoc.get("file");
		    String indexlaws[]=new String[2];
		    Integer index=Integer.valueOf(hitdoc.get("path"));		
		    indexlaws[0]="�?"+index/100000+"�?"+"&emsp";
		    index=index%100000;
		    indexlaws[0]+="�?"+index/1000+"�?";
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
	        ramdir.close();
	        indexreader.close();
	        fsdir.close();
		
		return files;
		
	}
		
	/*
	 *
	 * Copyright @ 2017 Beijing Beidouht Co. Ltd. 
	 * All right reserved. 
	 * @author: wanyan 
	 * date: 2017-11-16 
	 * 
	 * 该方法与GetMultipleSearch功能�?致，直接返回法条内容，只能查询单条件、单字段，针对不分词字段进行查询
	 *
	 * @params indexpath
	 * 				索引文件�?在目�?
	 * 			keywords 
	 * 				从JTextField获取用户输入的关键字
	 * 			top
	 * 				从JRadio获取用户选择的搜索条数，在索引文件中根据相关度排序，返回前top�?
	 * 			
	 * @return Map<Stirng,List<String[]>>
	 * 				将搜索结果以Map<文件名，[章节，法条]>的映射关系，返回查询结果		   
	 * @2017-11-17
	 * 			使用SortField和Sort调用IndexSearch方法,对搜索结果用path字段升序排序						  
	 * 
	 */
	
	public Map<String,List<String[]>> GetTermSearch(String indexpath,String keywords) throws IOException{
		
		Map<String,List<String[]>> files=new LinkedHashMap<String,List<String[]>>();
    	List<String[]> path=new ArrayList<String[]>();
		
		Path inpath=Paths.get(indexpath);
		
		FSDirectory fsdir=FSDirectory.open(inpath);		//创建磁盘索引文件
		
		IOContext iocontext=new IOContext();

		RAMDirectory ramdir=new RAMDirectory(fsdir,iocontext);		//创建内存索引文件，并将磁盘索引文件放到内存中
		
		//Analyzer analyzer=new StandardAnalyzer();		//创建标准分词�?

		IndexReader indexreader=DirectoryReader.open(ramdir);

		IndexSearcher indexsearcher=new IndexSearcher(indexreader);
		
		Term term=new Term("file",keywords);
		
		TermQuery termquery=new TermQuery(term);
		
		SortField sortfield=new SortField("path",SortField.Type.INT,false);		//false为升�?
       
		Sort sort=new Sort(sortfield);
		
		int top=indexreader.numDocs();
	
        TopDocs topdocs=indexsearcher.search(termquery,top,sort); 
        
        ScoreDoc[] hits=topdocs.scoreDocs;
          
        int num=hits.length;
        
        if(num==0){
        	files=null;
        }
		else{
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
		
        ramdir.close();
        indexreader.close();
        fsdir.close();
        return files;
		
	}
	
	/*
	 *
	 * Copyright @ 2018 Beijing Beidouht Co. Ltd. 
	 * All right reserved. 
	 * @author: wanyan 
	 * date: 2018-1-23 
	 * 
	 * �÷�����GetMultipleSearch����һ�£�ֱ�ӷ��ط������ݣ�ֻ�ܲ�ѯ�����������ֶΣ���Բ��ִ��ֶν��в�ѯ
	 *
	 * @params indexpath
	 * 				�����ļ�����Ŀ¼
	 * 			keywords 
	 * 				��JTextField��ȡ�û�����Ĺؼ���
	 * 			top
	 * 				��JRadio��ȡ�û�ѡ��������������������ļ��и�����ض����򣬷���ǰtop��
	 * 			
	 * @return Map<Stirng,List<String[]>>
	 * 				�����������Map<�ļ�����[�½ڣ�����]>��ӳ���ϵ�����ز�ѯ���		   					  
	 * 
	 */
	
	public Map<String,List<String[]>> GetTermSearch(String indexpath,String keywords,int top) throws IOException{
		
		Map<String,List<String[]>> files=new LinkedMap();
		List<String[]> path=new ArrayList<String[]>();
		
		Path inpath=Paths.get(indexpath);
		
		FSDirectory fsdir=FSDirectory.open(inpath);		//�������������ļ�
		
		IOContext iocontext=new IOContext();

		RAMDirectory ramdir=new RAMDirectory(fsdir,iocontext);		//�����ڴ������ļ����������������ļ��ŵ��ڴ���
		
//		Analyzer analyzer=new StandardAnalyzer();		//������׼�ִ���

		IndexReader indexreader=DirectoryReader.open(ramdir);

		IndexSearcher indexsearcher=new IndexSearcher(indexreader);
		
		Term term=new Term("file",keywords);
		
		TermQuery termquery=new TermQuery(term);
		
		SortField sortfield=new SortField("path",SortField.Type.INT,false);		//falseΪ����
      
		Sort sort=new Sort(sortfield);
	
       TopDocs topdocs=indexsearcher.search(termquery,top,sort); 
       
       ScoreDoc[] hits=topdocs.scoreDocs;
       
       String temp=null;
       
       int num=hits.length;
       
       if(num!=0){
       
    	   for(int i=0;i<num;i++){
       	
    		   Document hitdoc=indexsearcher.doc(hits[i].doc);
       	
    		   temp=hitdoc.get("file");
   			
    		   String indexlaws[]=new String[2];
   			
    		   Integer index=Integer.valueOf(hitdoc.get("path"));		
   		
    		   indexlaws[0]="��"+index/100000+"��"+" ";
   		
    		   index=index%100000;
   		
    		   indexlaws[0]+="��"+index/1000+"��";
   		
    		   String laws=hitdoc.get("law");
   		
    		   if(laws!=null){
       		
    			   indexlaws[1]=laws;
           	
    			   path.add(indexlaws);
    		   
    		   }
      
    	   }
   	
    	   files.put(temp,path);
       
       }
		
//       ramdir.close();
//       indexreader.close();
//       fsdir.close();
       return files;
		
	}
	
	
	/*
	 *
	 * Copyright @ 2017 Beijing Beidouht Co. Ltd. 
	 * All right reserved. 
	 * @author: wanyan 
	 * date: 2017-11-10 
	 * 
	 * DeleteIndex方法根据给定文档名，删除索引
	 *
	 * @params filename
	 * 				文档名称
	 * 			indexpath 
	 * 				索引文件存放目录	
	 * @return void
	 * 
	 * @2017-11-15
	 * 			使用方法forceMergeDeletes()，实现立即删�?
	 * 				   				
	 * 
	 */

	public void DeleteIndex(String s) throws IOException{
		if(writer!=null){
			Term t=new Term("file",s);								
			writer.deleteDocuments(t);								
			writer.forceMergeDeletes();
			writer.commit();
		}
		/*
		String[] s={"d",filename};
		
		ServletDemo.blockingQueue.put(s);
		*/	        	
	}

	/*
	 *
	 * Copyright @ 2017 Beijing Beidouht Co. Ltd. 
	 * All right reserved. 
	 * @author: wanyan 
	 * date: 2017-11-10 
	 * 
	 * GetTermFreq方法在根据file字段中，文档名称出现的次数，获取该文档中索引的法条�?�数
	 *
	 * @params indexpath 
	 * 				索引文件存放目录
	 * 				
	 * @return Map<String,Integer>
	 * 				返回文档中法条�?�数�?<文档名称，法条�?�数>
	 * 
	 * @2017-11-15
	 * 			修改为使用TermsEnum类获取词�?
	 * 			修复当没有索引文件时，catch报错异常，并进行弹框提示			   				
	 * 
	 */
	
	public Map<String,Integer> GetTermFreq(String indexpath){
		Map<String,Integer> res=new HashMap<String,Integer>();
		Path inpath=Paths.get(indexpath);
//		Analyzer analyzer=new StandardAnalyzer();		//创建标准分词�?
		try{
			FSDirectory fsdir=FSDirectory.open(inpath);		//创建磁盘索引文件
			IOContext iocontext=new IOContext();
			RAMDirectory ramdir=new RAMDirectory(fsdir,iocontext);		
			IndexReader indexreader=DirectoryReader.open(ramdir);
//			IndexSearcher indexsearcher=new IndexSearcher(indexreader);
		
		Terms terms = MultiFields.getTerms(indexreader,"file");
		if(terms!=null){
			TermsEnum termsEnums = terms.iterator();
			while(termsEnums.next()!=null){
				int num=termsEnums.docFreq();
//			 	System.out.println();
				BytesRef byteRef=termsEnums.term();
				String term = new String(byteRef.bytes, byteRef.offset, byteRef.length,"UTF-8");
//			 	System.out.println(term);
				res.put(term,num);			 
			}
		}
/*		
		int top=indexreader.numDocs();
		if(top!=0){
			QueryParser parser=new QueryParser("file", analyzer); 
			parser.setAllowLeadingWildcard(true);
			Query query=parser.parse("*.*");
			TopDocs topdocs=indexsearcher.search(query,top);     
			ScoreDoc[] hits=topdocs.scoreDocs;
			int num=hits.length;
			for(int i=0;i<num;i++){	
				Document hitdoc=indexsearcher.doc(hits[i].doc);
				String temp=hitdoc.get("file");
				if(!res.containsKey(temp)) {
					res.put(temp,0);
				}
			}
        
//			int dd=indexreader.docFreq(new Term("file","劳动人事争议仲裁办案规则（新）法.doc"));
			
//			System.out.println(dd);
			for(String key:res.keySet()){
				res.put(key,indexreader.docFreq(new Term("file",key)));
			}
		} 
*/		
        ramdir.close();
        indexreader.close();
        fsdir.close();
		
		}catch (IOException e) {
			// TODO Auto-generated catch block
			if(e.getClass().getSimpleName().equals("IndexNotFoundException")){		//当没有找到索引文件时，catch异常，并弹框提示
				System.out.println("û�д��������ļ�");
			//	e.printStackTrace();
				return res;
			}
			
			else
				e.printStackTrace();
		}
		return res;
	}
	
	public List<Map<String,String>> GetField(String indexpath,String field){
		List<Map<String,String>> res=new ArrayList<Map<String,String>>();
		Path path=Paths.get(indexpath);
		try{
			FSDirectory fsdir=FSDirectory.open(path);
			Analyzer analyzer=new StandardAnalyzer();
			IOContext iocontext=new IOContext();
			RAMDirectory ramdir=new RAMDirectory(fsdir,iocontext);		
			IndexReader indexreader=DirectoryReader.open(ramdir);
			IndexSearcher indexsearcher=new IndexSearcher(indexreader);
			int top=indexreader.numDocs();
			if(top!=0){
				QueryParser parser=new QueryParser(field, analyzer); 
				parser.setAllowLeadingWildcard(true);
				Query query=parser.parse("*");
				TopDocs topdocs=indexsearcher.search(query,top);     
				ScoreDoc[] hits=topdocs.scoreDocs;
				int num=hits.length;
				for(int i=0;i<num;i++){	
					Document hitdoc=indexsearcher.doc(hits[i].doc);
					Map<String,String> m=new HashMap<String,String>();
					m.put("file",hitdoc.get("file"));
					m.put("auth",hitdoc.get("auth"));
					m.put("createtime",hitdoc.get("createtime"));
					m.put("count",hitdoc.get("count"));
					res.add(m);
				}      
			} 
        ramdir.close();
        indexreader.close();
        fsdir.close();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			if(e.getClass().getSimpleName().equals("IndexNotFoundException")){
				System.out.println("û�д��������ļ�");
				return res;
			}
			else
				e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
		
	}

		
	public Integer AddIndex(Map<String,List<String[]>> content,String indexpath) throws IOException{
		
		Path inpath=Paths.get(indexpath);

		Analyzer analyzer = new StandardAnalyzer();		//创建标准分词�?
	
		FSDirectory fsdir=FSDirectory.open(inpath);		//创建磁盘索引文件
		
		RAMDirectory ramdir=new RAMDirectory();		//创建内存索引文件
	
		IndexWriterConfig ramconfig = new IndexWriterConfig(analyzer);
		
		IndexWriter ramiwriter = new IndexWriter(ramdir,ramconfig);		//创建内存IndexWriter
		
		
		int totalofindex=0;
		List<String[]> laws=new ArrayList<String[]>();
		
		for(Entry<String, List<String[]>> entry:content.entrySet()){ 
				
				FieldType filetype=new FieldType();
				filetype.setIndexOptions(IndexOptions.DOCS);
				filetype.setStored(true);		
				filetype.setTokenized(false);
				FieldType lawtype=new FieldType();
				lawtype.setIndexOptions(IndexOptions.NONE);
				lawtype.setStored(true);		
				lawtype.setTokenized(false);
				laws=entry.getValue();
				int count=laws.size();
				totalofindex=count;
				for(int i=0;i<count;i++){
					Document doc=new Document();		//创建Document,每一个发条新建一�?
					doc.add(new Field("file",entry.getKey(),filetype));		//文档名称存储，不分词
					//doc.add(new IntPoint("path",Integer.valueOf(laws.get(i)[0])));		//法条索引以Int类型存储
					//doc.add(new StoredField("path",Integer.valueOf(laws.get(i)[0])));
					doc.add(new NumericDocValuesField("path",Integer.valueOf(laws.get(i)[0])));
					doc.add(new IntPoint("path",Integer.valueOf(laws.get(i)[0])));		//����������Int���ʹ洢
					doc.add(new StoredField("path",Integer.valueOf(laws.get(i)[0])));
					doc.add(new Field("law",laws.get(i)[1],lawtype));		//发条内容索引、分词，不存�?
			    	ramiwriter.addDocument(doc);		//将法条索引添加到内存索引�?	
				}	    		  
			}
	
		ramiwriter.close();
	
    	IndexWriterConfig fsconfig=new IndexWriterConfig(analyzer); 
    	fsconfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    	IndexWriter fsiwriter=new IndexWriter(fsdir,fsconfig);
        fsiwriter.addIndexes(ramdir); 		
        fsiwriter.close();
        return totalofindex;
	}
	
	public void AddIndex(Map<String,String> file) throws IOException{
		if(writer!=null){
			Document doc=new Document();
			for(Entry<String,String> entry:file.entrySet()){
					FieldType filetype=new FieldType();
					filetype.setIndexOptions(IndexOptions.DOCS);
					filetype.setStored(true);		
					filetype.setTokenized(true);
					FieldType type=new FieldType();
					type.setIndexOptions(IndexOptions.NONE);
					type.setStored(true);		
					type.setTokenized(false);
					if(entry.getKey().equals("file"))
						doc.add(new Field(entry.getKey(),entry.getValue(),filetype));
					else
						doc.add(new Field(entry.getKey(),entry.getValue(),type));
				}
			writer.addDocument(doc);
			writer.commit();
		}
		
	}

	public void CreateIndexWriter(String indexpath) throws IOException{
		try{
		Path inpath=Paths.get(indexpath);
		FSDirectory dir = FSDirectory.open(inpath);
		Analyzer analyzer=new StandardAnalyzer();
		TieredMergePolicy ti=new TieredMergePolicy();
		ti.setForceMergeDeletesPctAllowed(0);		//����ɾ�������ĺϲ�����Ϊ0����ɾ��segmentʱ���������кϲ�
    	IndexWriterConfig config=new IndexWriterConfig(analyzer); 
    	config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
    	config.setMergePolicy(ti);		//���úϲ�����
    	writer=new IndexWriter(dir,config);
		
		}catch (IOException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void InsertIndex(String s) throws IOException{
		if(writer!=null){
			Path brpath=Paths.get(s);
			FSDirectory brdir=FSDirectory.open(brpath);
			writer.addIndexes(brdir);
			writer.commit();
		}

	}
	
	public void CloseIndexWriter() throws IOException{
		if(dir!=null)
			dir.close();
		if(writer!=null)
			writer.close();
	}
	
	public static void main(String[] args) throws Exception{
		HandleLucene handle=new HandleLucene();
		Map<String,List<Integer>> files=new HashMap<String,List<Integer>>();
		Map<String,List<String[]>> contentoffiles=new HashMap<String,List<String[]>>();
		Map<String,Integer> fre=new HashMap<String,Integer>();
		List<String[]> contentoflaw=new ArrayList<String[]>();
//		int num=handle.CreateIndex("D:\\Lucene\\src\\","D:\\Lucene\\index\\");
//		System.out.println(num);
//		handle.DeleteIndex("劳动人事争议仲裁办案规则（新）法.doc","D:\\Lucene\\index\\");
//		fre=handle.GetTermFreq("D:\\Lucene\\index\\");
//		for(String key:fre.keySet()){
//			System.out.println(key+":"+fre.get(key));
//		}
/*
		files=handle.ExecuteSearch("D:\\Lucene\\index\\","当事�?",4);
		
		if(files==null){
			System.out.println("未搜索到关键�?");
		}else{
			for(String key:files.keySet()){
				System.out.println("文件名："+key);
				List<Integer> path=files.get(key);
				System.out.println(path.size());	
			}
		}


		contentoflaw=handle.GetContentOfLawByIndex(files,"D:\\Lucene\\src\\");
		
		if(contentoflaw==null){
			System.out.println("未搜索到关键�?");
		}else{	
			for(int i=0;i<contentoflaw.size();i++)
				System.out.println(contentoflaw.get(i)[0]+" "+contentoflaw.get(i)[1]+" "+contentoflaw.get(i)[2]+" "+contentoflaw.get(i)[3]);
			}
*/	
		
		List<String[]> keywordset=new ArrayList<String[]>();
		keywordset.add(new String[]{"AND","file","劳动人事争议仲裁办案规则（新）法.doc"});
//		contentoffiles=handle.GetSearch("D:\\Lucene\\index\\","file:劳动人事争议仲裁办案规则（新）法.doc",1000);
//		contentoffiles=handle.GetMultipleSearch("D:\\Lucene\\index\\",keywordset,1000);
		contentoffiles=handle.GetTermSearch("D:\\Lucene\\index\\","劳动人事争议仲裁办案规则（新）法.doc",1000);
		StringBuffer text=new StringBuffer();
		for(String key:contentoffiles.keySet()){
			for(int i=0;i<contentoffiles.get(key).size();i++){
				text.append("&emsp&emsp");
				text.append(contentoffiles.get(key).get(i)[1]);
				text.append("&emsp"+"<i>"+"--摘录�?");
				text.append(key);;
				text.append("&emsp"+contentoffiles.get(key).get(i)[0]+"</i>");
				text.append("<br/>");
				text.append("<br/>");
				}
			}
	System.out.println(text.toString());
	
	}
	
	

}
