package swd;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

public class URLFilter implements Filter {  
  
    public URLFilter() {  
    }  
  
    public void destroy() {  
    }  
  
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {    
        
    	//设置编码格式
    	req.setCharacterEncoding("utf-8");
    	res.setCharacterEncoding("utf-8");
    	res.setContentType("text/html;charset=utf-8");
    	
    	//设置跨域请求  
        HttpServletResponse response = (HttpServletResponse) res;           
        response.setHeader("Access-Control-Allow-Origin", "*");  
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, PUT");  
        response.setHeader("Access-Control-Max-Age", "3628800");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type,Access-Token");
  
        System.out.println(req.getRemoteAddr()+":建立请求！");  
        
        chain.doFilter(req, response);    
    }   
  
    public void init(FilterConfig fConfig) throws ServletException {  
    } 
}