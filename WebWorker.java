/**
* Web worker: an object of this class executes in its own new thread
* to receive and respond to a single HTTP request. After the constructor
* the object executes on its "run" method, and leaves when it is done.
*
* One WebWorker object is only responsible for one client connection. 
* This code uses Java threads to parallelize the handling of clients:
* each WebWorker runs in its own thread. This means that you can essentially
* just think about what is happening on one client at a time, ignoring 
* the fact that the entirety of the webserver execution might be handling
* other clients, too. 
*
* This WebWorker class (i.e., an object of this class) is where all the
* client interaction is done. The "run()" method is the beginning -- think
* of it as the "main()" for a client interaction. It does three things in
* a row, invoking three methods in this class: it reads the incoming HTTP
* request; it writes out an HTTP header to begin its response, and then it
* writes out some HTML content for the response content. HTTP requests and
* responses are just lines of text (in a very particular format). 
*
**/

import java.net.Socket;
import java.lang.Runnable;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.util.TimeZone;

public class WebWorker implements Runnable
{

String s;
String path;
int code;
int count = 0;

private Socket socket;

/**
* Constructor: must have a valid open socket
**/
public WebWorker(Socket s)
{
   socket = s;
}

/**
* Worker thread starting point. Each worker handles just one HTTP 
* request and then returns, which destroys the thread. This method
* assumes that whoever created the worker created it with a valid
* open socket object.
**/
// This function has been modified to be able to serve HTML files
// It defaults to the webserver works page if no path is included 
// to localhost.
public void run()
{
   System.err.println("Handling connection...");
   try {
      InputStream  is = socket.getInputStream();
      OutputStream os = socket.getOutputStream();
      readHTTPRequest(is);
      writeHTTPHeader(os,"text/html");
      if(path.length() == 0)
      writeContent(os);
      else
      writeContent2(os);
      os.flush();
      socket.close();
   } catch (Exception e) {
      System.err.println("Output error: "+e);
   }
   System.err.println("Done handling connection.");
   return;
}

/**
* Read the HTTP request header.
**/

//This function is modified to extract the address
//of file. Takes in first line and splits string.
//Then file content is read line by line and stored
// in a string. Lines are checked
// for certain tags and the tags are replaced with
// with appropriate content. If file cannot be 
// opened then a 404 error is sent and a not found
// message is written.
private void readHTTPRequest(InputStream is)
{
   String line;
   String line2;
   BufferedReader r = new BufferedReader(new InputStreamReader(is));
   BufferedReader k;
   while (true) {
      try {
	count++;
         while (!r.ready()) Thread.sleep(1);
         line = r.readLine();
	 String lineArray[] = line.split(" ");
	 if(lineArray.length > 1 && count == 1){
	 path = lineArray[1];
	path = path.substring(1);
	System.err.println(path);
	}
         System.err.println("Request line: ("+line+")");
         if (line.length()==0) break;
      } catch (Exception e) {
         System.err.println("Request error: "+e);
         break;
      }
   }

try{
k = new BufferedReader(new FileReader(path));
Date d1 = new Date();
DateFormat df1 = DateFormat.getDateTimeInstance();
df1.setTimeZone(TimeZone.getTimeZone("GMT"));
//System.err.println(df1.format(d1));
code = 200;
	s = "";
	while((line2 = k.readLine()) != null){
	line2 = line2.replaceAll("<cs371date>", df1.format(d1));
	line2 = line2.replaceAll("<cs371server>", "This is server");
	 s += line2;
	//System.err.println(s);
         if (line2.length()==0) break;
	}
}
       catch (Exception e) {
	System.err.println("no file");
         code = 404;
        
      }

//System.err.println(s);
		
   return;
}


/**
* Write the HTTP header lines to the client network connection.
* @param os is the OutputStream object to write to
* @param contentType is the string MIME content type (e.g. "text/html")
**/

//This function was modified to sent a 404 error if a file could not be found
private void writeHTTPHeader(OutputStream os, String contentType) throws Exception
{
   Date d = new Date();
   DateFormat df = DateFormat.getDateTimeInstance();
   df.setTimeZone(TimeZone.getTimeZone("GMT"));
   if(code == 200)
   os.write("HTTP/1.1 200 OK\n".getBytes());
   else
   os.write("HTTP/1.1 404 Not Found\n".getBytes());
   os.write("Date: ".getBytes());
   os.write((df.format(d)).getBytes());
   os.write("\n".getBytes());
   os.write("Server: Jon's very own server\n".getBytes());
   //os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
   //os.write("Content-Length: 438\n".getBytes()); 
   os.write("Connection: close\n".getBytes());
   os.write("Content-Type: ".getBytes());
   os.write(contentType.getBytes());
   os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
   return;
}

/**
* Write the data content to the client network connection. This MUST
* be done after the HTTP header has been written out.
* @param os is the OutputStream object to write to
**/

//This function is maintained as the write output if no file path is specified
private void writeContent(OutputStream os) throws Exception
{
   os.write("<html><head></head><body>\n".getBytes());
   os.write("<h2>HOME PAGE</h2>\n".getBytes());
   os.write("<h3>My web server works!</h3>\n".getBytes());
   os.write("<h4>Navigate to localhost:8080/res/acc/test.html to see more</h4>\n".getBytes());
   os.write("</body></html>\n".getBytes());
}
// This function either writes the string with
// file content or writes out an not found error to
// to the output stream.
private void writeContent2(OutputStream os) throws Exception
{
if(s != null)
   os.write(s.getBytes());
if(code == 404){
   os.write("<html><head></head><body>\n".getBytes());
   os.write("<h3>404 Error Page not Found!</h3>\n".getBytes());
   os.write("</body></html>\n".getBytes());
}

}

} // end class
