package com.github.woshikid.tools.translater;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.github.woshikid.utils.ServletUtils;

public class TranslateFilter implements Filter{
	private String charset;

	public void init(FilterConfig filterConfig){
		charset = filterConfig.getInitParameter("charset");
		String enable = filterConfig.getInitParameter("enable");
		if("true".equals(enable))Translater.enable = true;
	}
	
	public void destroy(){}

	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)servletRequest;
		HttpServletResponse response = (HttpServletResponse)servletResponse;
		
		if(!Translater.enable){
			filterChain.doFilter(request, response);
		}else{
			TranslateResponse translateResponse = new TranslateResponse(response);
			filterChain.doFilter(request, translateResponse);
			
			if(translateResponse.isBinary()){
				translateResponse.sendContent();
			}else{
				String content = translateResponse.getContent();
				String lang = (String)ServletUtils.getSession(request, "lang");
				content = Translater.translate(content, lang);
				translateResponse.sendContent(content);
			}
		}
	}
	
	final class TranslateOutputStream extends ServletOutputStream {
		private ByteArrayOutputStream buffer;
		
		public TranslateOutputStream(ByteArrayOutputStream buffer) {
			this.buffer = buffer;
		}
		
		public void write(int b) {
			buffer.write(b);
		}
		
		public boolean isReady() {
			return false;
		}

		public void setWriteListener(WriteListener writeListener) {}
	}
	
	final class TranslateResponse extends HttpServletResponseWrapper {
		private ByteArrayOutputStream buffer;
		private TranslateOutputStream output;
		private PrintWriter writer;
		private boolean useStream = true;
		
		public TranslateResponse(HttpServletResponse response) throws IOException {
			super(response);
			buffer = new ByteArrayOutputStream();
			output = new TranslateOutputStream(buffer);
			writer = new PrintWriter(new OutputStreamWriter(buffer, charset));
		}
		
		public ServletOutputStream getOutputStream() {
			useStream = true;
			return output;
		}
		
		public PrintWriter getWriter() {
			useStream = false;
			return writer;
		}
		
		public void flushBuffer() {}
		
		public String getContent() throws IOException {
			writer.flush();
			return buffer.toString(charset);
		}
		
		public void sendContent() throws IOException {
			super.getOutputStream().write(buffer.toByteArray());
			super.flushBuffer();
		}
		
		public void sendContent(String content) throws IOException {
			super.setCharacterEncoding(charset);
			if(useStream){
				byte[] data = content.getBytes(charset);
				super.setContentLength(data.length);
				super.getOutputStream().write(data);
			}else{
				super.getWriter().print(content);
			}
			super.flushBuffer();
		}
		
		public boolean isBinary() throws IOException {
			String content = getContent();
			byte[] b1 = content.getBytes(charset);
			byte[] b2 = buffer.toByteArray();
			if(Arrays.equals(b1, b2)){
				return false;
			}else{
				return true;
			}
		}
	}
}
