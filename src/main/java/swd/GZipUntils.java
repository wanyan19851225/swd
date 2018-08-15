package swd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipUntils {

	public String S2Gzip(String s) throws UnsupportedEncodingException{
		
		if (s == null || s.length() == 0)
			return s;
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip=null;
		try {
			gzip = new GZIPOutputStream(out);
			gzip.write(s.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(gzip!=null){
				try {
					gzip.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return new String(out.toByteArray(),"ISO-8859-1");
	}
	public String Gzip2S(String s){
		if (s == null || s.length() == 0)
			return s;

		ByteArrayOutputStream out=new ByteArrayOutputStream();
		ByteArrayInputStream in=null;
		GZIPInputStream gzip=null;
		try {
			in=new ByteArrayInputStream(s.getBytes("ISO-8859-1"));
			gzip=new GZIPInputStream(in);
			byte[] buffer = new byte[1024];
			int offset = -1;
			while ((offset = gzip.read(buffer,0,buffer.length)) != -1) {
				out.write(buffer, 0, offset);
				}

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (gzip != null) {
				try {
					gzip.close();
				
				} catch (IOException e) {
					}
				
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					}
				}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					}
				}
		}
		return new String(out.toByteArray());
	}

}
