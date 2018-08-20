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
	
	private IndexWriter writer;
	private FSDirectory dir;
	
	/*
	 *
	 * Copyright @ 2017 Beijing Beidouht Co. Ltd. 
	 * All right reserved. 
	 * @author: wanyan 
	 * date: 2017-10-29 
	 * 
	 * 璇ユ柟娉曚笌浣跨敤鍗曚竴鏌ヨ鏂瑰紡
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
	 * 						  
	 * @2017-10-31
	 * 				淇敼楂樹寒鎴彇鐨勯粯璁ゅ瓧绗︽暟锛屼慨鏀逛负鏍规嵁娉曟潯鍐呭鐨勫瓧绗︽暟鏄剧ず
	 */
	
	public Map<String,List<String[]>> GetSearch(String indexpath,String keywords,int top) throws ParseException, IOException, InvalidTokenOffsetsException{
		
		Map<String,List<String[]>> files=new LinkedHashMap<String,List<String[]>>();
		
		Path inpath=Paths.get(indexpath);
		
		try{
		
		FSDirectory fsdir=FSDirectory.open(inpath);		//鍒涘缓纾佺洏绱㈠紩鏂囦欢
		
		IOContext iocontext=new IOContext();

		RAMDirectory ramdir=new RAMDirectory(fsdir,iocontext);		//鍒涘缓鍐呭瓨绱㈠紩鏂囦欢锛屽苟灏嗙鐩樼储寮曟枃浠舵斁鍒板唴瀛樹腑
		
		Analyzer analyzer=new StandardAnalyzer();		//鍒涘缓鏍囧噯鍒嗚瘝锟�?

		IndexReader indexreader=DirectoryReader.open(ramdir);

		IndexSearcher indexsearcher=new IndexSearcher(indexreader);
		
//		Term term=new Term("law",keywords);
		
//		TermQuery termquery=new TermQuery(term);
//		妯＄硦鏌ヨ		
//		FuzzyQuery fuzzquery=new FuzzyQuery(term);
// 		鐭鏌ヨ		
//		 PhraseQuery.Builder builder = new PhraseQuery.Builder();
//		 builder.add(term);
//		 PhraseQuery phrasequery=builder.build();
//		鏌ヨ鍒嗘瀽锟�?	
			
        QueryParser parser=new QueryParser("law", analyzer);
           
        Query query=parser.parse(keywords.toString());
        
        TopDocs topdocs=indexsearcher.search(query,top); 
        
        ScoreDoc[] hits=topdocs.scoreDocs;
        
        int num=hits.length;
        
        if(num==0){
        	return null;
        }
        
        //姝ゅ鍔犲叆鐨勬槸鎼滅储缁撴灉鐨勯珮浜儴锟�?
        SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<b><font color=red>","</font></b>"); //濡傛灉涓嶆寚瀹氬弬鏁扮殑璇濓紝榛樿鏄姞绮楋紝锟�?<b><b/>
        QueryScorer scorer = new QueryScorer(query);//计算得分，会初始化一个查询结果最高的得分
//        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer); //鏍规嵁杩欎釜寰楀垎璁＄畻鍑轰竴涓墖锟�?
        Highlighter highlighter = new Highlighter(simpleHTMLFormatter, scorer);
 //       highlighter.setTextFragmenter(fragmenter); //璁剧疆锟�?涓嬭鏄剧ず鐨勭墖锟�?
  
        for(int i=0;i<num;i++){
        	
        	Document hitdoc=indexsearcher.doc(hits[i].doc);
        	
    		String temp=hitdoc.get("file");
    		String indexlaws[]=new String[2];
    		Integer index=Integer.valueOf(hitdoc.get("path"));
    		if(index/100000==999)		//判断章段落的索引号是否为999,当索引号为999时，表明没有章段落，索引号设置为0
    			indexlaws[0]="第"+0+"章"+"&emsp";
    		else
    			indexlaws[0]="第"+index/100000+"章"+" ";
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
	 * 璇ユ柟娉曚笌GetSearch鍔熻兘锟�?鑷达紝鐩存帴杩斿洖娉曟潯鍐呭锛屽彧鏄彲浠ヤ娇鐢ㄥ鏉′欢銆佸鍩熻繘琛屾煡锟�?
	 *
	 * @params indexpath
	 * 				绱㈠紩鏂囦欢锟�?鍦ㄧ洰锟�?
	 * 			keywords 
	 * 				浠嶫TextField鑾峰彇鐢ㄦ埛杈撳叆鐨勫涓叧閿瓧,浣跨敤String[]鏂瑰紡浼狅拷?锟斤紝[鍏抽敭瀛楋紝鏌ヨ鏉′欢]
	 * 			top
	 * 				浠嶫Radio鑾峰彇鐢ㄦ埛閫夋嫨鐨勬悳绱㈡潯鏁帮紝鍦ㄧ储寮曟枃浠朵腑鏍规嵁鐩稿叧搴︽帓搴忥紝杩斿洖鍓峵op锟�?
	 * 			
	 * @return Map<Stirng,List<String[]>>
	 * 				灏嗘悳绱㈢粨鏋滀互Map<鏂囦欢鍚嶏紝[绔犺妭锛屾硶鏉>鐨勬槧灏勫叧绯伙紝杩斿洖鏌ヨ缁撴灉		   
	 * 						  
	 * @2017-10-31
	 * 			淇敼浣跨敤BooleanQuery鏂瑰紡瀹炵幇澶氭潯浠舵煡锟�?
	 * @2017-11-1
	 * 			淇敼鍚屾椂鏀寔鍗曟潯浠舵煡璇㈠拰澶氭潯浠舵煡锟�?
	 * 			淇敼浣跨敤鏌ヨ鍒嗘瀽鍣≦ueryParser瀹炵幇澶氬煙銆佸瀛楁銆佸鏉′欢鏌ヨ
	 * @2017-11-16
	 * 			淇敼浣跨敤MultiFieldQueryParser绫诲疄鐜板瀛楁銆佸鏉′欢鏌ヨ
	 * 
	 */
	
	public Map<String,List<String[]>> GetMultipleSearch(String indexpath,List<String[]> keywordset,int top) throws IOException, ParseException, InvalidTokenOffsetsException{
		
		Map<String,List<String[]>> files=new LinkedHashMap<String,List<String[]>>();
		
		Path inpath=Paths.get(indexpath);
		
		FSDirectory fsdir=FSDirectory.open(inpath);		//鍒涘缓纾佺洏绱㈠紩鏂囦欢
		
		IOContext iocontext=new IOContext();

		RAMDirectory ramdir=new RAMDirectory(fsdir,iocontext);		//鍒涘缓鍐呭瓨绱㈠紩鏂囦欢锛屽苟灏嗙鐩樼储寮曟枃浠舵斁鍒板唴瀛樹腑
		
		Analyzer analyzer=new StandardAnalyzer();		//鍒涘缓鏍囧噯鍒嗚瘝锟�?

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
        
        //姝ゅ鍔犲叆鐨勬槸鎼滅储缁撴灉鐨勯珮浜儴锟�?
        SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<b><font color=red>","</font></b>"); //濡傛灉涓嶆寚瀹氬弬鏁扮殑璇濓紝榛樿鏄姞绮楋紝锟�?<b><b/>
        QueryScorer scorer = new QueryScorer(query);//璁＄畻寰楀垎锛屼細鍒濆鍖栦竴涓煡璇㈢粨鏋滄渶楂樼殑寰楀垎
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer); //鏍规嵁杩欎釜寰楀垎璁＄畻鍑轰竴涓墖锟�?
        Highlighter highlighter = new Highlighter(simpleHTMLFormatter, scorer);
        highlighter.setTextFragmenter(fragmenter); //璁剧疆锟�?涓嬭鏄剧ず鐨勭墖锟�?
  
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
	 * 璇ユ柟娉曚笌GetSearch鍔熻兘锟�?鑷达紝鐩存帴杩斿洖娉曟潯鍐呭锛屽彧鏄彲浠ヤ娇鐢ㄥ鏉′欢銆佸鍩熻繘琛屾煡锟�?
	 *
	 * @params indexpath
	 * 				绱㈠紩鏂囦欢锟�?鍦ㄧ洰锟�?
	 * 			fields
	 * 				鎸囧畾浠庡摢鍑犱釜瀛楁鏌ヨ锛屼娇鐢⊿tring[]鏂瑰紡浼犲弬锟�?
	 * 			range
	 * 				鎸囧畾浠庡摢浜涙枃妗ｉ噷鎼滅储keywords
	 * 			keywords 
	 * 				浠嶫TextField鑾峰彇鐢ㄦ埛杈撳叆鐨勫涓叧閿瓧,浣跨敤String鏂瑰紡浼狅拷??
	 * 			top
	 * 				浠嶫Radio鑾峰彇鐢ㄦ埛閫夋嫨鐨勬悳绱㈡潯鏁帮紝鍦ㄧ储寮曟枃浠朵腑鏍规嵁鐩稿叧搴︽帓搴忥紝杩斿洖鍓峵op锟�?
	 * 			
	 * @return Map<Stirng,List<String[]>>
	 * 				灏嗘悳绱㈢粨鏋滀互Map<鏂囦欢鍚嶏紝[绔犺妭锛屾硶鏉>鐨勬槧灏勫叧绯伙紝杩斿洖鏌ヨ缁撴灉		   
	 *
	 * @2017-11-18
	 * 			澧炲姞List<String>鍙傛暟锛岀敤浜庢寚瀹氬湪鍝簺鏂囨。鍐呮煡璇㈡硶锟�?
	 * 			淇敼keywords鍙傛暟绫诲瀷涓篠tring						  
	 * 
	 */

	public Map<String,List<String[]>> GetMultipleSearch(String indexpath,String[] fields,List<String> range,String keywords,int top) throws IOException, ParseException, InvalidTokenOffsetsException{
		
		Map<String,List<String[]>> files=new LinkedHashMap<String,List<String[]>>();
		
		Path inpath=Paths.get(indexpath);
		
		FSDirectory fsdir=FSDirectory.open(inpath);		//鍒涘缓纾佺洏绱㈠紩鏂囦欢
		
		IOContext iocontext=new IOContext();

		RAMDirectory ramdir=new RAMDirectory(fsdir,iocontext);		//鍒涘缓鍐呭瓨绱㈠紩鏂囦欢锛屽苟灏嗙鐩樼储寮曟枃浠舵斁鍒板唴瀛樹腑
		
		Analyzer analyzer=new StandardAnalyzer();		//鍒涘缓鏍囧噯鍒嗚瘝锟�?

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
		        
		    //姝ゅ鍔犲叆鐨勬槸鎼滅储缁撴灉鐨勯珮浜儴锟�?
		SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<b><font color=red>","</font></b>"); //濡傛灉涓嶆寚瀹氬弬鏁扮殑璇濓紝榛樿鏄姞绮楋紝锟�?<b><b/>
		    
		QueryScorer scorer = new QueryScorer(query);//璁＄畻寰楀垎锛屼細鍒濆鍖栦竴涓煡璇㈢粨鏋滄渶楂樼殑寰楀垎
		    
//		Fragmenter fragmenter = new SimpleSpanFragmenter(scorer); //鏍规嵁杩欎釜寰楀垎璁＄畻鍑轰竴涓墖锟�?
		    
		Highlighter highlighter = new Highlighter(simpleHTMLFormatter, scorer);

//          highlighter.setTextFragmenter(fragmenter); //璁剧疆锟�?涓嬭鏄剧ず鐨勭墖锟�?
		  
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
	 * 
	 */
	
	public Map<String,List<String[]>> GetTermSearch(String indexpath,String keywords) throws IOException{
		
		Map<String,List<String[]>> files=new LinkedHashMap<String,List<String[]>>();
    	List<String[]> path=new ArrayList<String[]>();
		
		Path inpath=Paths.get(indexpath);
		
		FSDirectory fsdir=FSDirectory.open(inpath);		//鍒涘缓纾佺洏绱㈠紩鏂囦欢
		
		IOContext iocontext=new IOContext();

		RAMDirectory ramdir=new RAMDirectory(fsdir,iocontext);		//鍒涘缓鍐呭瓨绱㈠紩鏂囦欢锛屽苟灏嗙鐩樼储寮曟枃浠舵斁鍒板唴瀛樹腑
		
		//Analyzer analyzer=new StandardAnalyzer();		//鍒涘缓鏍囧噯鍒嗚瘝锟�?

		IndexReader indexreader=DirectoryReader.open(ramdir);

		IndexSearcher indexsearcher=new IndexSearcher(indexreader);
		
		Term term=new Term("file",keywords);
		
		TermQuery termquery=new TermQuery(term);
		
		SortField sortfield=new SortField("path",SortField.Type.INT,false);		//false涓哄崌锟�?
       
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
	
	public Map<String,List<String[]>> GetTermSearch(String indexpath,String keywords,int top) throws IOException{
		
		@SuppressWarnings("unchecked")
		Map<String,List<String[]>> files=new LinkedMap();
		List<String[]> path=new ArrayList<String[]>();
		
		Path inpath=Paths.get(indexpath);
		
		FSDirectory fsdir=FSDirectory.open(inpath);		//锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟侥硷拷
		
		IOContext iocontext=new IOContext();

		RAMDirectory ramdir=new RAMDirectory(fsdir,iocontext);		//锟斤拷锟斤拷锟节达拷锟斤拷锟斤拷锟侥硷拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟侥硷拷锟脚碉拷锟节达拷锟斤拷
		
//		Analyzer analyzer=new StandardAnalyzer();		//锟斤拷锟斤拷锟斤拷准锟街达拷锟斤拷

		IndexReader indexreader=DirectoryReader.open(ramdir);

		IndexSearcher indexsearcher=new IndexSearcher(indexreader);
		
		Term term=new Term("file",keywords);
		
		TermQuery termquery=new TermQuery(term);
		
		SortField sortfield=new SortField("path",SortField.Type.INT,false);		//false为锟斤拷锟斤拷
      
		Sort sort=new Sort(sortfield);
	
       TopDocs topdocs=indexsearcher.search(termquery,top,sort); 
       
       ScoreDoc[] hits=topdocs.scoreDocs;
       
       String temp=null;
       
       int num=hits.length;
       
       if(num!=0){
       
    	   for(int i=0;i<num;i++){
       	
    		   Document hitdoc=indexsearcher.doc(hits[i].doc);
       	
    		   temp=hitdoc.get("file");
   			
    		   String indexlaws[]=new String[4];
   			
    		   Integer index=Integer.valueOf(hitdoc.get("path"));		
	    		if(index/100000==999)		//判断章段落的索引号是否为999,当索引号为999时，表明没有章段落，索引号设置为0
	    			indexlaws[0]="第"+0+"章"+" ";
	    		else
	    			indexlaws[0]="第"+index/100000+"章"+" ";
   		
    		   index=index%100000;
   		
    		   indexlaws[0]+="第"+index/1000+"节";
   		
    		   String laws=hitdoc.get("law");
    		   String author=hitdoc.get("author");	//锟斤拷取锟斤拷锟斤拷锟斤拷锟斤拷
    		   String time=hitdoc.get("time");		//锟斤拷取锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
   		
    		   if(laws!=null){
       		
    			   indexlaws[1]=laws;
    			   indexlaws[2]=author;
    			   indexlaws[3]=time;
           	
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
	 * DeleteIndex鏂规硶鏍规嵁缁欏畾鏂囨。鍚嶏紝鍒犻櫎绱㈠紩
	 *
	 * @params filename
	 * 				鏂囨。鍚嶇О
	 * 			indexpath 
	 * 				绱㈠紩鏂囦欢瀛樻斁鐩綍	
	 * @return void
	 * 
	 * @2017-11-15
	 * 			浣跨敤鏂规硶forceMergeDeletes()锛屽疄鐜扮珛鍗冲垹锟�?
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
	 * GetTermFreq鏂规硶鍦ㄦ牴鎹甪ile瀛楁涓紝鏂囨。鍚嶇О鍑虹幇鐨勬鏁帮紝鑾峰彇璇ユ枃妗ｄ腑绱㈠紩鐨勬硶鏉★拷?锟芥暟
	 *
	 * @params indexpath 
	 * 				绱㈠紩鏂囦欢瀛樻斁鐩綍
	 * 				
	 * @return Map<String,Integer>
	 * 				杩斿洖鏂囨。涓硶鏉★拷?锟芥暟锟�?<鏂囨。鍚嶇О锛屾硶鏉★拷?锟芥暟>
	 * 
	 * @2017-11-15
	 * 			淇敼涓轰娇鐢═ermsEnum绫昏幏鍙栬瘝锟�?
	 * 			淇褰撴病鏈夌储寮曟枃浠舵椂锛宑atch鎶ラ敊寮傚父锛屽苟杩涜寮规鎻愮ず			   				
	 * 
	 */
	
	public Map<String,Integer> GetTermFreq(String indexpath){
		Map<String,Integer> res=new HashMap<String,Integer>();
		Path inpath=Paths.get(indexpath);
//		Analyzer analyzer=new StandardAnalyzer();		//鍒涘缓鏍囧噯鍒嗚瘝锟�?
		try{
			FSDirectory fsdir=FSDirectory.open(inpath);		//鍒涘缓纾佺洏绱㈠紩鏂囦欢
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
        
//			int dd=indexreader.docFreq(new Term("file","鍔冲姩浜轰簨浜夎浠茶鍔炴瑙勫垯锛堟柊锛夋硶.doc"));
			
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
				System.out.println("检索文档名称和文档中法条总数时失败，原因：未检索到索引文件");
			//	e.printStackTrace();
				return res;
			}
			
			else
				e.printStackTrace();
		}
		return res;
	}
	
	/*
	 *
	 * Copyright @ 2018 Beijing Beidouht Co. Ltd. 
	 * All right reserved. 
	 * @author: wanyan 
	 * date: 2018-7-26 
	 * 
	 * GetFileInfo锟斤拷锟节伙拷取锟侥碉拷锟斤拷锟狡ｏ拷锟斤拷锟竭ｏ拷锟斤拷锟斤拷锟斤拷锟节ｏ拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟侥碉拷锟斤拷息锟斤拷锟矫凤拷锟斤拷锟斤拷锟饺碉拷锟斤拷GetTermFreq锟斤拷取锟侥碉拷锟斤拷锟狡和凤拷锟斤拷锟斤拷锟斤拷,然锟斤拷使锟斤拷TermQuery锟斤拷锟斤拷锟斤拷式,锟斤拷询锟侥碉拷锟斤拷锟斤拷锟斤拷,锟侥碉拷锟斤拷锟斤拷锟斤拷锟斤拷
	 *
	 * @params indexpath 
	 * 				锟斤拷锟斤拷锟侥硷拷位锟斤拷
	 * 				
	 * @return Map<String,String[]>
	 * 				锟斤拷锟斤拷锟斤拷锟斤拷锟組ap<锟侥碉拷锟斤拷锟狡ｏ拷[锟斤拷锟竭ｏ拷锟斤拷锟斤拷锟斤拷锟节ｏ拷锟斤拷锟斤拷锟斤拷锟斤拷]>锟斤拷锟斤拷式锟斤拷锟斤拷
	 * 	   				
	 * 
	 */
	
	public Map<String,String[]> GetFileInfo(String indexpath){
		Map<String,String[]> res=new HashMap<String,String[]>();
		Map<String,Integer> f=this.GetTermFreq(indexpath);	//锟芥储锟侥碉拷锟斤拷锟狡★拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷Map<锟侥碉拷锟斤拷锟狡ｏ拷锟斤拷锟斤拷锟斤拷锟斤拷>
		
		Path inpath=Paths.get(indexpath);
		
		try{
			FSDirectory fsdir=FSDirectory.open(inpath);		
			IOContext iocontext=new IOContext();
			RAMDirectory ramdir=new RAMDirectory(fsdir,iocontext);		
			IndexReader indexreader=DirectoryReader.open(ramdir);
			IndexSearcher indexsearcher=new IndexSearcher(indexreader);
			
			for(String file: f.keySet()){
			
				Term term=new Term("file",file);
				TermQuery termquery=new TermQuery(term);				
			    TopDocs topdocs=indexsearcher.search(termquery,1); 			       
			    ScoreDoc[] hits=topdocs.scoreDocs;
	    		Document hitdoc=indexsearcher.doc(hits[0].doc);
	    	    String finfo[]=new String[3];
	    	    finfo[0]=hitdoc.get("author");
	    	    finfo[1]=hitdoc.get("time");
	    	    finfo[2]=String.valueOf(f.get(file));
	    	    res.put(file,finfo);		    
			}
		
       
			ramdir.close();
			indexreader.close();       
			fsdir.close();
		
		}catch (IOException e) {
			// TODO Auto-generated catch block
			if(e.getClass().getSimpleName().equals("IndexNotFoundException")){
				System.out.println("检索文档作者和文档创建日期时失败，原因：未检索到索引文件");
			//	e.printStackTrace();
				return res;
			}
			
			else
				e.printStackTrace();
		}
		
		return res;
	}
	
	/*
	 *
	 * Copyright @ 2017 Beijing Beidouht Co. Ltd. 
	 * All right reserved. 
	 * @author: wanyan 
	 * date: 2017-11-10 
	 * 
	 * AddIndex锟斤拷锟斤拷锟节革拷锟捷达拷锟捷碉拷file锟街段ｏ拷锟斤拷锟斤拷锟斤拷锟侥碉拷追锟接碉拷锟窖达拷锟节碉拷锟斤拷锟斤拷锟侥硷拷锟叫ｏ拷锟斤拷锟矫伙拷锟斤拷锟斤拷锟斤拷募锟斤拷锟斤拷虼唇锟斤拷锟斤拷锟斤拷募锟�
	 *
	 * @params content 
	 * 				锟窖斤拷要追锟接的凤拷锟斤拷锟斤拷锟斤拷锟斤拷Map锟叫ｏ拷锟侥硷拷锟斤拷为Key,锟斤拷锟斤拷锟斤拷锟芥到List<String[]>锟叫ｏ拷锟斤拷为value,锟芥储锟斤拷式为Map<锟侥硷拷锟斤拷锟斤拷锟斤拷锟斤拷<锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷>>
	 * 
	 * 		   author
	 * 				锟斤拷锟斤拷锟矫凤拷锟斤拷锟斤拷锟斤拷锟斤拷
	 * 			
	 * 		   time
	 * 				锟斤拷锟斤拷锟矫凤拷锟斤拷锟斤拷锟斤拷锟斤拷
	 * 				
	 * 		   indexpath
	 * 				锟斤拷锟斤拷锟侥硷拷路锟斤拷
	 * 
	 * @return Integer
	 * 				锟斤拷锟斤拷锟斤拷拥锟斤拷锟斤拷锟斤拷募锟斤拷械姆锟斤拷锟斤拷锟�
	 * 
	 * Modeified Date:2018-7-25
	 * 				锟斤拷锟接诧拷锟斤拷author,锟斤拷锟捷达拷锟斤拷锟矫凤拷锟斤拷锟斤拷锟斤拷锟竭ｏ拷锟斤拷锟皆诧拷锟斤拷锟斤拷-锟芥储-锟斤拷锟街词碉拷锟斤拷式锟芥储锟斤拷Document锟斤拷
	 * 				锟斤拷锟接诧拷锟斤拷time,锟斤拷锟捷达拷锟斤拷锟矫凤拷锟斤拷锟斤拷锟斤拷锟节ｏ拷锟斤拷锟皆诧拷锟斤拷锟斤拷-锟芥储-锟斤拷锟街词碉拷锟斤拷式锟芥储锟斤拷Document锟斤拷	   				
	 * 
	 */

		
	public Integer AddIndex(Map<String,List<String[]>> content,String author,String time,String indexpath) throws IOException{
		
		Path inpath=Paths.get(indexpath);

		Analyzer analyzer = new StandardAnalyzer();		//鍒涘缓鏍囧噯鍒嗚瘝锟�?
	
		FSDirectory fsdir=FSDirectory.open(inpath);		//鍒涘缓纾佺洏绱㈠紩鏂囦欢
		
		RAMDirectory ramdir=new RAMDirectory();		//鍒涘缓鍐呭瓨绱㈠紩鏂囦欢
	
		IndexWriterConfig ramconfig = new IndexWriterConfig(analyzer);
		
		IndexWriter ramiwriter = new IndexWriter(ramdir,ramconfig);		//鍒涘缓鍐呭瓨IndexWriter
		
		
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
					Document doc=new Document();		//鍒涘缓Document,姣忎竴涓彂鏉℃柊寤轰竴锟�?
					doc.add(new Field("file",entry.getKey(),filetype));		//鏂囨。鍚嶇О瀛樺偍锛屼笉鍒嗚瘝
					//doc.add(new IntPoint("path",Integer.valueOf(laws.get(i)[0])));		//娉曟潯绱㈠紩浠nt绫诲瀷瀛樺偍
					//doc.add(new StoredField("path",Integer.valueOf(laws.get(i)[0])));
					doc.add(new Field("author",author,lawtype));		//锟芥储锟矫凤拷锟斤拷锟斤拷锟斤拷锟斤拷	
					doc.add(new Field("time",time,lawtype));			//锟芥储锟矫凤拷锟斤拷锟侥达拷锟斤拷锟斤拷锟斤拷
					doc.add(new NumericDocValuesField("path",Integer.valueOf(laws.get(i)[0])));
					doc.add(new IntPoint("path",Integer.valueOf(laws.get(i)[0])));		//锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷Int锟斤拷锟酵存储
					doc.add(new StoredField("path",Integer.valueOf(laws.get(i)[0])));
					doc.add(new Field("law",laws.get(i)[1],lawtype));		//鍙戞潯鍐呭绱㈠紩銆佸垎璇嶏紝涓嶅瓨锟�?
			    	ramiwriter.addDocument(doc);		//灏嗘硶鏉＄储寮曟坊鍔犲埌鍐呭瓨绱㈠紩锟�?	
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
		ti.setForceMergeDeletesPctAllowed(0);		//锟斤拷锟斤拷删锟斤拷锟斤拷锟斤拷锟侥合诧拷锟斤拷锟斤拷为0锟斤拷锟斤拷删锟斤拷segment时锟斤拷锟斤拷锟斤拷锟斤拷锟叫合诧拷
    	IndexWriterConfig config=new IndexWriterConfig(analyzer); 
    	config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
    	config.setMergePolicy(ti);		//锟斤拷锟矫合诧拷锟斤拷锟斤拷
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
		//Map<String,List<Integer>> files=new HashMap<String,List<Integer>>();
		Map<String,List<String[]>> contentoffiles=new HashMap<String,List<String[]>>();
		//Map<String,Integer> fre=new HashMap<String,Integer>();
		//List<String[]> contentoflaw=new ArrayList<String[]>();
//		int num=handle.CreateIndex("D:\\Lucene\\src\\","D:\\Lucene\\index\\");
//		System.out.println(num);
//		handle.DeleteIndex("鍔冲姩浜轰簨浜夎浠茶鍔炴瑙勫垯锛堟柊锛夋硶.doc","D:\\Lucene\\index\\");
//		fre=handle.GetTermFreq("D:\\Lucene\\index\\");
//		for(String key:fre.keySet()){
//			System.out.println(key+":"+fre.get(key));
//		}
/*
		files=handle.ExecuteSearch("D:\\Lucene\\index\\","褰撲簨锟�?",4);
		
		if(files==null){
			System.out.println("鏈悳绱㈠埌鍏抽敭锟�?");
		}else{
			for(String key:files.keySet()){
				System.out.println("鏂囦欢鍚嶏細"+key);
				List<Integer> path=files.get(key);
				System.out.println(path.size());	
			}
		}


		contentoflaw=handle.GetContentOfLawByIndex(files,"D:\\Lucene\\src\\");
		
		if(contentoflaw==null){
			System.out.println("鏈悳绱㈠埌鍏抽敭锟�?");
		}else{	
			for(int i=0;i<contentoflaw.size();i++)
				System.out.println(contentoflaw.get(i)[0]+" "+contentoflaw.get(i)[1]+" "+contentoflaw.get(i)[2]+" "+contentoflaw.get(i)[3]);
			}
*/	
		
		List<String[]> keywordset=new ArrayList<String[]>();
		keywordset.add(new String[]{"AND","file","鍔冲姩浜轰簨浜夎浠茶鍔炴瑙勫垯锛堟柊锛夋硶.doc"});
//		contentoffiles=handle.GetSearch("D:\\Lucene\\index\\","file:鍔冲姩浜轰簨浜夎浠茶鍔炴瑙勫垯锛堟柊锛夋硶.doc",1000);
//		contentoffiles=handle.GetMultipleSearch("D:\\Lucene\\index\\",keywordset,1000);
		contentoffiles=handle.GetTermSearch("D:\\Lucene\\index\\","鍔冲姩浜轰簨浜夎浠茶鍔炴瑙勫垯锛堟柊锛夋硶.doc",1000);
		StringBuffer text=new StringBuffer();
		for(String key:contentoffiles.keySet()){
			for(int i=0;i<contentoffiles.get(key).size();i++){
				text.append("&emsp&emsp");
				text.append(contentoffiles.get(key).get(i)[1]);
				text.append("&emsp"+"<i>"+"--摘录自");
				text.append(key);;
				text.append("&emsp"+contentoffiles.get(key).get(i)[0]+"</i>");
				text.append("<br/>");
				text.append("<br/>");
				}
			}
	System.out.println(text.toString());
	
	}
	
	

}
