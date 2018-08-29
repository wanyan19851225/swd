package swd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TieredMergePolicy;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

public class ItemIndexs {
	
	/*
	 *
	 * Copyright @ 2018 Beijing Beidouht Co. Ltd. 
	 * All right reserved. 
	 * @author: wanyan 
	 * date: 2018-8-23 
	 * 
	 * ItemIndexs为用户发送过来的文档内容，建立缓存索引
	 *
	 * @params content 
	 * 				以Map<文档名称，List<[段落索引号，段落内容]>形式传入参数
	 * 		   author
	 * 				文档作者
	 * 		   time
	 * 				文档创建日期	
	 * 		   indexpath
	 * 				文档缓存索引路径
	 * @return Integer
	 * 				返回建立段落索引总数   				
	 */
	public Integer AddIndexs(Map<String,List<String[]>> content,String indexpath) throws IOException{
		
		Path inpath=Paths.get(indexpath);
		Analyzer analyzer = new StandardAnalyzer();		
		RAMDirectory rdir=new RAMDirectory();	
		IndexWriterConfig rconf = new IndexWriterConfig(analyzer);
		IndexWriter rw = new IndexWriter(rdir,rconf);
		
		int total=0;
		List<String[]> laws=new ArrayList<String[]>();
		
		FieldType filetype=new FieldType();
		filetype.setIndexOptions(IndexOptions.DOCS);
		filetype.setStored(true);		
		filetype.setTokenized(false);
		
		FieldType lawtype=new FieldType();
		lawtype.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
		lawtype.setStored(true);		
		lawtype.setTokenized(true);
		
		for(Entry<String, List<String[]>> entry:content.entrySet()){ 
			laws=entry.getValue();
			int count=laws.size();
			total=count;
			for(int i=0;i<count;i++){
				Document doc=new Document();		
				doc.add(new Field("file",entry.getKey(),filetype));
				doc.add(new NumericDocValuesField("path",Integer.valueOf(laws.get(i)[0])));
				doc.add(new IntPoint("path",Integer.valueOf(laws.get(i)[0])));
				doc.add(new StoredField("path",Integer.valueOf(laws.get(i)[0])));
				doc.add(new Field("law",laws.get(i)[1],lawtype));
			    rw.addDocument(doc);
				}	    		  
			}
		rw.close();
		
		FSDirectory fdir=FSDirectory.open(inpath);	
		TieredMergePolicy ti=new TieredMergePolicy();
		ti.setForceMergeDeletesPctAllowed(0);
		IndexWriterConfig fconf=new IndexWriterConfig(analyzer); 
		fconf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		fconf.setMergePolicy(ti);
		IndexWriter fw=new IndexWriter(fdir,fconf);
		fw.addIndexes(rdir); 		
		fw.close();
		
		return total;
	}
	
	public Boolean DeleteIndexs(String indexpath) {
		Boolean f=true;
		File fpath=new File(indexpath);
		if(!fpath.exists())
			f=false;
		else {
			File[] files=fpath.listFiles();
			for(int i=0;i<files.length;i++) {
				f=files[i].delete();
			}
		}	
		return f;
	}
}
