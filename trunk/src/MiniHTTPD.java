import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Locale;
import java.util.TimeZone;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.Date;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;

import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.Socket;

import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;

import java.nio.channels.Channels;

import java.text.SimpleDateFormat;

/**
 * A simple NIO HTTP 1.0 server
 *
 * <p> MiniHTTPD version 1.0,
 * Copyright &copy; 2005 Christopher Ottley (xknight@users.sourceforge.net, http://nariva.sf.net/minihttpd/)
 *
 * <p><b>Features & limitations: </b><ul>
 *
 *    <li> Only one Java file </li>
 *    <li> Released as open source, modified BSD licence </li>
 *    <li> No fixed config files, logging, authorization etc. </li>
 *    <li> Supports parameter parsing of GET and POST methods </li>
 *    <li> Supports dynamic content </li>
 *    <li> Doesn't cache anything </li>
 *    <li> Doesn't limit bandwidth, request time or simultaneous connections </li>
 *
 * </ul>
 *
 * <p><b>Changes from NanoHTTPD: </b><ul>
 *
 *    <li> Uses Java NIO for Socket listening </li>
 *    <li> Removed file server code </li>
 *    <li> Removed mime type list </li>
 *    <li> Strongly typed HTTP Codes as an enumeration of constants </li>
 *    <li> Default response is html with link to subproject on sourceforge </li>
 *    <li> Removed license display ability from command line </li>
 *    <li> Added ability to limit accepted connections by IP or hostname </li>
 *    <li> Can bind webserver to interface based on port, host name and port, InetAddress and port </li>
 *
 * </ul>
 *
 * <p><b>Way to use: </b><ul>
 *
 *    <li> Subclass serve() and embed to your own program </li>
 *
 * </ul>
 *
 * Based on NanoHTTPD by Jarno Elonen (elonen@iki.fi, http://iki.fi/elonen/)
 *
 * Portions of this code:

    Copyright (c) 2001 Jarno Elonen <elonen@iki.fi>

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer. Redistributions in
    binary form must reproduce the above copyright notice, this list of
    conditions and the following disclaimer in the documentation and/or other
    materials provided with the distribution. The name of the author may not
    be used to endorse or promote products derived from this software without
    specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
    OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 *
 */
public class MiniHTTPD {
  // ==================================================
  // API parts
  // ==================================================

  /**
   * Override this to customize the server.
   *
   * @parm uri  Percent-decoded URI without parameters, for example "/index.cgi"
   * @parm method "GET", "POST" etc.
   * @parm parms  Parsed, percent decoded parameters from URI and, in case of POST, data.
   * @parm header Header entries, percent decoded
   * @return HTTP response, see class Response for details
   */
  public Response serve(String uri, String method, Properties header, Properties parms) {
    System.out.println( method + " '" + uri + "' " );

    Enumeration e = header.propertyNames();
    while (e.hasMoreElements()) {
      String value = (String)e.nextElement();
      System.out.println( "  HDR: '" + value + "' = '" + header.getProperty( value ) + "'" );
    }

    e = parms.propertyNames();
    while (e.hasMoreElements()) {
      String value = (String)e.nextElement();
      System.out.println( "  PRM: '" + value + "' = '" + parms.getProperty( value ) + "'" );
    }

    return new Response();
  }

  /**
   * Access method in case someone wants to get to the HTTP codes.
   */
  public HTTPCODE getCodes() {
    return HTTPCODE.OK;
  }

  /**
   * HTTP response.
   * Return one of these from serve().
   */
  public class Response
  {
    /**
     * Default constructor: response = HTTP_OK, data = default page, mime = 'text/html'
     */
    public Response() {
      this(HTTPCODE.OK, "text/html",
           "<html><head><title>MiniHTTPD 1.0</title></head><body><a href='" +
           "http://nariva.sf.net/data/subprojects/minihttpd.html" +
           "'>MiniHTTPD 1.0</a></body></html>");
    }

    /**
     * Basic constructor.
     */
    public Response(HTTPCODE status, String mimeType, InputStream data) {
      this.status = status;
      this.mimeType = mimeType;
      this.data = data;
    }

    /**
     * Convenience method that makes an InputStream out of
     * given text.
     */
    public Response(HTTPCODE status, String mimeType, String txt) {
      this.status = status;
      this.mimeType = mimeType;
      this.data = new ByteArrayInputStream(txt.getBytes());
    }

    /**
     * Adds given line to the header.
     */
    public void addHeader(String name, String value) {
      header.put(name, value);
    }

    /**
     * HTTP status code after processing, e.g. "200 OK"
     */
    public HTTPCODE status;

    /**
     * MIME type of content, e.g. "text/html"
     */
    public String mimeType;

    /**
     * Data of the response, may be null.
     */
    public InputStream data;

    /**
     * Headers for the HTTP response. Use addHeader()
     * to add lines.
     */
    public Properties header = new Properties();
  }


  /**
   * Common mime types for dynamic content
   */
  public static final String
    MIME_PLAINTEXT = "text/plain",
    MIME_HTML = "text/html",
    MIME_DEFAULT_BINARY = "application/octet-stream";

  // ==================================================
  // Socket & server code
  // ==================================================

  /** Port to listen on */
  public static int listenPort = 80;

  /** Host name listen port bound to */
  public static String listenHostName = "localhost";

  /** Socket that does the listening */
  protected ServerSocketChannel ssChannel;

  /** Used to manage connections accepted */
  protected Selector selector;

  /** True if the webserver is / should run */
  protected boolean shouldRun = true;

  /** True if should limit allowable clients by hostname/ip */
  protected boolean paranoid = false;

  /** The list of clients allowed (either hostname or ip) if paranoid is true */
  protected Hashtable acceptList = new Hashtable();

  /**
   * Starts a HTTP server to given port on any available interface.<p>
   * Throws an IOException if the socket is already in use
   */
  public MiniHTTPD(int port) throws IOException {
    this(new InetSocketAddress(port));
  }

  /**
   * Starts a HTTP server on given host and port.<p>
   * Throws an IOException if the socket is already in use or could not bind to host
   */
  public MiniHTTPD(String hostName, int port) throws IOException {
    this(new InetSocketAddress(hostName, port));
  }

  /**
   * Starts a HTTP server on given host and port.<p>
   * Throws an IOException if the socket is already in use or could not bind to host
   */
  public MiniHTTPD(InetAddress hostAddress, int port) throws IOException {
    this(new InetSocketAddress(hostAddress, port));
  }

  /**
   * Starts a HTTP server on given host and port.<p>
   * Throws an IOException if the socket is already in use or could not bind to host
   */
  public MiniHTTPD(InetSocketAddress sockAddress) throws IOException {
    listenPort = sockAddress.getPort();
    listenHostName = sockAddress.getHostName();
    // Create a non-blocking server socket channel on port specified
    ssChannel = ServerSocketChannel.open();
    ssChannel.configureBlocking(false);
    ssChannel.socket().bind(sockAddress);

    // Create a selector
    selector = Selector.open();
    ssChannel.register(selector, SelectionKey.OP_ACCEPT);
  }

  /**
   * Start the webserver
   */
  public void start() {
    shouldRun = true;

    Thread t = new Thread( new Runnable() {
      public void run() {
        try { acceptConnections(); } catch ( Exception ignored ) { }
      }
    });
    t.setDaemon( true );
    t.start();
  }

  /**
   * Stop the webserver
   */
  public void stop() {
    shouldRun = false;
  }

  /**
   * Access method to set flag stating if to limit client access to server
   */
  public void setParanoid(boolean paranoid) {
    this.paranoid = paranoid;
    if (paranoid) {
      acceptClient("127.0.0.1");
      acceptClient("localhost");
    }
  }

  /**
   * Returns the current state of the flag stating if to limit client access to server
   */
  public boolean getParanoid() {
    return paranoid;
  }

  /**
   * Add an accepted client to the list. List only used if paranoid is true.
   */
  public void acceptClient(String client) {
    acceptList.put(client.trim().toLowerCase(), client);
  }

  /**
   * Remove a previously accepted client from he list. List only used if paranoid is true.
   */
  public void removeClient(String client) {
    acceptList.remove(client.trim().toLowerCase());
  }

  /**
   * Accepts the connections for the HTTP server.
   */
  protected void acceptConnections() throws IOException {
    while (shouldRun) {
      // Wait for an event
      selector.select();

      if (!shouldRun) { return; }

      // Get list of selection keys with pending events
      Iterator it = selector.selectedKeys().iterator();

      // Process each key
      while (it.hasNext()) {
        // Get the selection key
        SelectionKey selKey = (SelectionKey)it.next();

        // Remove it from the list to indicate that it is being processed
        it.remove();

        // Check if it's a connection request
        if (selKey.isAcceptable()) {
          // Get channel with connection request
          ServerSocketChannel serverSocketChannel = (ServerSocketChannel)selKey.channel();

          // Accept the connection request.
          // If serverSocketChannel is blocking, this method blocks.
          // The returned channel is in blocking mode.
          SocketChannel sChannel = serverSocketChannel.accept();

          // If serverSocketChannel is non-blocking, sChannel may be null
          if (sChannel != null) {
            if (paranoid) {
              Socket s = sChannel.socket();
              InetAddress remoteAddress = s.getInetAddress();
              String remoteIp = remoteAddress.getHostAddress();

              // If either the remote ip or remote hostname is in the accept list
              // then we go ahead and process. Split up so that hostname lookup
              // is only done when necessary.
              if (acceptList.containsKey(remoteIp)) {
                new HTTPSession(sChannel);
              } else {
                String remoteName = remoteAddress.getHostName().trim().toLowerCase();
                if (acceptList.containsKey(remoteName)) {
                  new HTTPSession(sChannel);
                } else { // Otherwise close the socket
                  s.close();
                }
              }
            } else {
              new HTTPSession(sChannel);
            }
          }
        }
      }
    }
  }

  /**
   * Handles one session, i.e. parses the HTTP request
   * and returns the response.
   */
  private class HTTPSession implements Runnable {

    private SocketChannel myChannel;
    private BufferedReader myIn;

    public HTTPSession(SocketChannel sChannel) {
      myChannel = sChannel;
      Thread t = new Thread(this);
      t.setDaemon(true);
      t.start();
    }

    public void run() {
      try {
        InputStream is = Channels.newInputStream(myChannel);
        if (is == null) return;
        BufferedReader in = new BufferedReader(new InputStreamReader(is));

        // Read the request line
        StringTokenizer st = new StringTokenizer(in.readLine());
        if (!st.hasMoreTokens())
          sendError(HTTPCODE.BAD_REQUEST, "BAD REQUEST: Syntax error. Usage: GET /example/file.html");

        String method = st.nextToken();

        if (!st.hasMoreTokens())
          sendError(HTTPCODE.BAD_REQUEST, "BAD REQUEST: Missing URI. Usage: GET /example/file.html" );

        String uri = decodePercent(st.nextToken());

        // Decode parameters from the URI
        Properties parms = new Properties();
        int qmi = uri.indexOf('?');
        if (qmi >= 0) {
          decodeParms(uri.substring(qmi + 1), parms);
          uri = decodePercent(uri.substring(0, qmi));
        }

        // If there's another token, it's protocol version,
        // followed by HTTP headers. Ignore version but parse headers.
        Properties header = new Properties();
        if (st.hasMoreTokens()) {
          String line = in.readLine();
          while (line.trim().length() > 0) {
            int p = line.indexOf(':');
            header.put(line.substring(0, p).trim(), line.substring(p + 1).trim());
            line = in.readLine();
          }
        }

        // If the method is POST, there may be parameters
        // in data section, too, read another line:
        if (method.equalsIgnoreCase("POST")) {
          decodeParms(in.readLine(), parms);
        }

        // Ok, now do the serve()
        Response r = serve(uri, method, header, parms);
        if (r == null)
          sendError(HTTPCODE.SERVER_ERROR, "SERVER INTERNAL ERROR: Serve() returned a null response." );
        else
          sendResponse(r.status, r.mimeType, r.header, r.data);

        in.close();
      } catch ( IOException ioe ) {
        try {
          sendError( HTTPCODE.SERVER_ERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
        }
        catch (Throwable t) { }
      } catch (InterruptedException ignored) {
        // Thrown by sendError, ignore and exit the thread.
      } finally {
        try {
          myChannel.close();
        } catch (IOException ignored) { }
      }
    }

    /**
     * Decodes the percent encoding scheme. <br/>
     * For example: "an+example%20string" -> "an example string"
     */
    private String decodePercent(String str) throws InterruptedException {
      try {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
          char c = str.charAt(i);
          switch (c) {
            case '+':
              sb.append(' ');
              break;
            case '%':
              sb.append((char)Integer.parseInt(str.substring(i + 1, i + 3), 16));
              i += 2;
              break;
            default:
              sb.append(c);
              break;
          }
        }
        return new String(sb.toString().getBytes());
      } catch(Exception e) {
        sendError(HTTPCODE.BAD_REQUEST, "BAD REQUEST: Bad percent-encoding.");
        return null;
      }
    }

    /**
     * Decodes parameters in percent-encoded URI-format
     * ( e.g. "name=Jack%20Daniels&pass=Single%20Malt" ) and
     * adds them to given Properties.
     */
    private void decodeParms(String parms, Properties p) throws InterruptedException {
      if (parms == null)
        return;

      StringTokenizer st = new StringTokenizer(parms, "&");
      while (st.hasMoreTokens()) {
        String e = st.nextToken();
        int sep = e.indexOf('=');
        if (sep >= 0)
          p.put(decodePercent(e.substring(0, sep)).trim(),
                decodePercent(e.substring(sep + 1)));
      }
    }

    /**
     * Returns an error message as a HTTP response and
     * throws InterruptedException to stop further request processing.
     */
    private void sendError(HTTPCODE status, String msg) throws InterruptedException {
      sendResponse(status, MIME_PLAINTEXT, null, new ByteArrayInputStream(msg.getBytes()));
      throw new InterruptedException();
    }

    /**
     * Sends given response to the socket.
     */
    private void sendResponse(HTTPCODE status, String mime, Properties header, InputStream data) {
      try {
        if (status == null) {
          throw new Error("sendResponse(): Status can't be null.");
        }

        OutputStream out = Channels.newOutputStream(myChannel);
        PrintWriter pw = new PrintWriter( out );
        pw.print("HTTP/1.0 " + status + " \r\n");

        if (mime != null) {
          pw.print("Content-Type: " + mime + "\r\n");
        }

        if (header == null || header.getProperty( "Date" ) == null) {
          pw.print( "Date: " + gmtFrmt.format( new Date()) + "\r\n");
        }

        if (header != null) {
          Enumeration e = header.keys();
          while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            String value = header.getProperty(key);
            pw.print(key + ": " + value + "\r\n");
          }
        }

        pw.print("\r\n");
        pw.flush();

        if (data != null) {
          byte[] buff = new byte[2048];
          int read = 2048;
          while (read == 2048) {
            read = data.read(buff, 0, 2048);
            out.write(buff, 0, read);
          }
        }
        out.flush();
        out.close();
        if (data != null) {
          data.close();
        }
      } catch( IOException ioe ) {
        // Couldn't write? No can do.
        try { myChannel.close(); } catch( Throwable t ) { }
      }
    }
  }

  /**
   * Starts as a standalone server and waits for Enter.
   */
  public static void main( String[] args ) {
    System.out.println("MiniHTTPD 1.0 (c) 2005 Christopher Ottley <xknight@users.sourceforge.net>\n" +
                        "based on NanoHTTPD 1.01 Copyright (c) 2001 Jarno Elonen <elonen@iki.fi>\n" +
                        "(Command line options: [port])\n" );

    // Change port if requested
    int port = 80;
    if (args.length > 0) {
      port = Integer.parseInt(args[0]);
    }

    MiniHTTPD mh = null;
    try {
      mh = new MiniHTTPD(port);
      //mh.setParanoid(true);
      //mh.acceptClient("192.168.254.4");
      //mh.acceptClient("it10.company.com");
      mh.start();
    } catch(IOException ioe) {
      System.err.println( "Couldn't start server:\n" + ioe );
      System.exit(-1);
    }

    System.out.println( "Now serving on port " + port + " ");
    System.out.println( "Hit Enter to stop.\n" );

    try { System.in.read(); } catch( Throwable t ) { };
    mh.stop();
  }

  /**
   * GMT date formatter
   */
  private static SimpleDateFormat gmtFrmt;
    static {
      gmtFrmt = new SimpleDateFormat( "E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
      gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
}

/**
 * Type Safe Enumeration of constants.
 */
class TSE {

  protected int id;
  protected String text;

  public final int ord;
  protected TSE prev;
  protected TSE next;

  protected static int upperBound = 0;
  protected static TSE first = null;
  protected static TSE last = null;

  public TSE(int anID, String aText) {
    this.id = anID;
    this.text = aText;
    this.ord = upperBound++;
    if (first == null) first = this;
    if (last != null) {
      this.prev = last;
      last.next = this;
    }
    last = this;
  }


  public static Enumeration elements() {
    return new Enumeration() {
      private TSE curr = first;
      public boolean hasMoreElements() {
        return curr != null;
      }
      public Object nextElement() {
        TSE c = curr;
        curr = curr.next();
        return c;
      }
    };
  }

  public String toString() { return "" + this.id + " " + this.text; }
  public int id() { return id; }
  public String value() { return text; }
  public static int size() { return upperBound; }
  public static TSE first() { return first; }
  public static TSE last()  { return last;  }
  public TSE prev()  { return this.prev; }
  public TSE next()  { return this.next; }
}

/**
 * HTTP response codes as type safe enumeration constants.
 */
final class HTTPCODE extends TSE {

  private HTTPCODE(int id, String text) { super(id, text); }

  public static final HTTPCODE CONTINUE            = new HTTPCODE(100, "Continue");
  public static final HTTPCODE SWITCHING_PROTOCOLS = new HTTPCODE(101, "Switching Protocols");

  public static final HTTPCODE OK                           = new HTTPCODE(200, "OK");
  public static final HTTPCODE CREATED                      = new HTTPCODE(201, "Created");
  public static final HTTPCODE ACCEPTED                     = new HTTPCODE(202, "Accepted");
  public static final HTTPCODE NONAUTHORITATIVE_INFORMATION = new HTTPCODE(202, "Non-Authoritative Information");
  public static final HTTPCODE NO_CONTENT                   = new HTTPCODE(204, "No Content");
  public static final HTTPCODE RESET_CONTENT                = new HTTPCODE(205, "Reset Content");
  public static final HTTPCODE PARTIAL_CONTENT              = new HTTPCODE(206, "Partial Content");

  public static final HTTPCODE MULTIPLE_CHOICES  = new HTTPCODE(300, "Multiple Choices");
  public static final HTTPCODE MOVED_PERMANENTLY = new HTTPCODE(301, "Moved Permanently");
  public static final HTTPCODE MOVED_TEMPORARILY = new HTTPCODE(302, "Moved Temporarily");
  public static final HTTPCODE SEE_OTHER         = new HTTPCODE(303, "See Other");
  public static final HTTPCODE NOT_MODIFIED      = new HTTPCODE(304, "Not Modified");
  public static final HTTPCODE USE_PROXY         = new HTTPCODE(305, "Use Proxy");

  public static final HTTPCODE BAD_REQUEST                   = new HTTPCODE(400, "Bad Request");
  public static final HTTPCODE UNAUTHORIZED                  = new HTTPCODE(401, "Unauthorized");
  public static final HTTPCODE PAYMENT_REQUIRED              = new HTTPCODE(402, "Payment Required");
  public static final HTTPCODE FORBIDDEN                     = new HTTPCODE(403, "Forbidden");
  public static final HTTPCODE NOT_FOUND                     = new HTTPCODE(404, "Not Found");
  public static final HTTPCODE METHOD_NOT_ALLOWED            = new HTTPCODE(405, "Method Not Allowed");
  public static final HTTPCODE NOT_ACCEPTABLE                = new HTTPCODE(406, "Not Acceptable");
  public static final HTTPCODE PROXY_AUTHENTICATION_REQUIRED = new HTTPCODE(407, "Proxy Authentication Required");
  public static final HTTPCODE REQUEST_TIME_OUT              = new HTTPCODE(408, "Request Time-out");
  public static final HTTPCODE CONFLICT                      = new HTTPCODE(409, "Conflict");
  public static final HTTPCODE GONE                          = new HTTPCODE(410, "Gone");
  public static final HTTPCODE LENGTH_REQUIRED               = new HTTPCODE(411, "Length Required");
  public static final HTTPCODE PRECONDITION_FAILED           = new HTTPCODE(412, "Precondition Failed");
  public static final HTTPCODE REQUEST_ENTITY_TOO_LARGE      = new HTTPCODE(413, "Request Entity Too Large");
  public static final HTTPCODE REQUEST_URI_TOO_LARGE         = new HTTPCODE(414, "Request-URI Too Large");
  public static final HTTPCODE UNSUPPORTED_MEDIA_TYPE        = new HTTPCODE(415, "Unsupported Media Type");

  public static final HTTPCODE SERVER_ERROR               = new HTTPCODE(500, "Server Error");
  public static final HTTPCODE NOT_IMPLEMENTED            = new HTTPCODE(501, "Not Implemented");
  public static final HTTPCODE BAD_GATEWAY                = new HTTPCODE(502, "Bad Gateway");
  public static final HTTPCODE SERVICE_UNAVAILABLE        = new HTTPCODE(503, "Service Unavailable");
  public static final HTTPCODE GATEWAY_TIME_OUT           = new HTTPCODE(504, "Gateway Time-out");
  public static final HTTPCODE HTTP_VERSION_NOT_SUPPORTED = new HTTPCODE(505, "HTTP Version not supported");
}
