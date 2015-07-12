package be.bagofwords.http;

import be.bagofwords.ui.UI;
import be.bagofwords.util.URLUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * @author jan &and; koen
 * @version 1.1
 */
public class URLDownloader {

    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:28.0) Gecko/20100101 Firefox/28.0";

    // accessible fields
    private String method;
    private String url;
    private String userAgent;
    private final ArrayList<String> redirectedUrls;
    private final long timeOut;
    private final long maxSize;
    private int status;
    private String postData;
    private String content;
    private long totalBytesDownloaded;

    // internal settings
    private final String defaultEncoding = "UTF-8";
    private final int maxNbRedirects = 10;

    private byte[] buffer;
    private byte[] rawcontent, raw;
    private List<String> extraRequestHeaders;
    private Map<String, String> savedCookies;
    private String[] responseHeaders;
    private int endOfHeader;

	/*
     * statusses:
	 *  -10 not a valid url
	 *  -11 infinite redirect
	 *  -12 length of extracted header contains 0 lines
	 *  -14 can't decode raw content as defaultEncoding
	 *  -15 can't decode rawcontent with found encoding
	 */

    /**
     * URLDownloader
     *
     * @param url the url to be downloaded
     */

    public URLDownloader(String url) {
        this("GET", url, null, 5000, -1);
    }

    public URLDownloader(String url, String userAgent) {
        this("GET", url, userAgent, 5000, 1024 * 1024);
    }

    public URLDownloader(String method, String url, String userAgent) {
        this(method, url, userAgent, 5000, 1024 * 1024);
    }

    public void setPostData(String postData) {
        this.postData = postData.trim() + "\r\n";
    }

    public URLDownloader(String method, String url, String userAgent, long timeout, long maxSize) {
        String protocol = URLUtils.getProtocol(url);
        if (!protocol.equals("http") && !protocol.equals("https")) {
            throw new RuntimeException("Unsupported protocol in url " + url);
        }
        this.url = url;
        if (!StringUtils.isEmpty(userAgent)) {
            this.userAgent = userAgent;
        } else {
            this.userAgent = DEFAULT_USER_AGENT;
        }
        this.method = method;
        this.timeOut = timeout;
        this.maxSize = maxSize;
        this.totalBytesDownloaded = 0;
        this.redirectedUrls = new ArrayList<>();
        this.extraRequestHeaders = new ArrayList<>();
        this.savedCookies = new HashMap<>();
    }

    /**
     * Makes connection with the webserver, and download the content. The status can be retrieved by calling getStatus()
     */
    public DownloadResult download() {
        DownloadResult result = null;
        while (result == null) {
            result = downloadRaw();
            if (result.isSuccess()) {
                extractHeaderAndRawContent();
                extractAndSaveCookies();
                if (hasChunkedEncoding())
                    fixChunkedEncoding();
                switch (status) {
                    case 200:
                        decodeBuffer();
                        break;
                    case 301:
                    case 302:
                        if (redirectedUrls.size() < maxNbRedirects) {
                            method = "GET";
                            postData = null;
                            String newurl = extractRedirect();
                            if (StringUtils.isEmpty(newurl)) {
                                result = new DownloadResult(false, "could not find redirect");
                            } else {
                                Matcher pM = URLUtils.protocolP.matcher(newurl);
                                if (!pM.find()) {
                                    newurl = URLUtils.makeAbsolute(newurl, url);
                                }
                                redirectedUrls.add(newurl);
                                url = newurl;
                                result = null; //will download redirected page
                            }
                        } else {
                            status = -11;
                            result = new DownloadResult(false, "too many redirects");
                        }
                        break;
                    case 404:
                        result = new DownloadResult(false, "404 not found");
                        break;
                    default:
                        result = new DownloadResult(false, "unknown status " + status);
                }
            }
        }
        return result;
    }

    private void extractAndSaveCookies() {
        for (String header : getResponseHeaders()) {
            int indOfColon = header.indexOf(':');
            if (indOfColon > -1) {
                String headerName = header.substring(0, indOfColon);
                if ("Set-Cookie".equals(headerName)) {
                    String cookieAndAttributes = header.substring(indOfColon + 2);
                    String cookie;
                    if (cookieAndAttributes.contains(";")) {
                        cookie = cookieAndAttributes.split(";")[0];
                    } else {
                        cookie = cookieAndAttributes;
                    }
                    cookie = cookie.split(";")[0];
                    int indOfEquals = cookie.indexOf('=');
                    if (indOfEquals != -1) {
                        String cookieName = cookie.substring(0, indOfEquals);
                        String cookieValue = cookie.substring(indOfEquals + 1);
                        savedCookies.put(cookieName, cookieValue);
                    }
                }
            }
        }
    }

    public int getStatus() {
        return status;
    }

    /**
     * get the html content of the webpage
     *
     * @return the html content
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns the url passed in the constructor, without the http://
     *
     * @return the url
     */
    public String getURL() {
        return url;
    }

    public ArrayList<String> getRedirectedURLs() {
        return redirectedUrls;
    }


    private UrlParts splitUrl(String url) {
        return new UrlParts(URLUtils.getProtocol(url), URLUtils.getDomain(url), URLUtils.getPath(url));
    }

    /**
     * Decode the raw bytes from the webserver with the hopfully correct encoding
     *
     * @post if all is ok, <code>content</code> is set
     * @post if all is ok, <code>getSuccesfull()</code> returns true
     */

    private void decodeBuffer() {
        String encoding = extractEncodingFromHeader();
        if (StringUtils.isEmpty(encoding))
            encoding = extractEncodingFromContent();
        if (StringUtils.isEmpty(encoding))
            encoding = extractEncodingFromGuess();
        if (StringUtils.isEmpty(encoding))
            encoding = defaultEncoding;
        encoding = encoding.replaceAll("\"", "").replaceAll(";", "");
        try {
            content = new String(rawcontent, encoding);
            content = content.replace("\r", "");
        } catch (UnsupportedEncodingException e) {
            UI.writeError("Problem while reading url " + getURL() + " with encoding " + encoding, e);
            status = -15;
        }
    }

    /**
     * Guess the encoding by trying several encodings, and seeing if there are unknown bytes (65533, 0xfffd)
     *
     * @return the encoding, or null if none works
     */
    private String extractEncodingFromGuess() {
        String[] candidateEncodings = new String[]{"UTF-8", "ISO-8859-1", "ISO-8859-15", "US-ASCII", "UTF-16BE", "UTF-16LE", "UTF-16"};
        for (int e = 0; e < Math.min(4, candidateEncodings.length); e++) {
            try {
                String content = new String(rawcontent, candidateEncodings[e]);
                int nbErrs = 0;
                for (int i = 0; i < content.length(); i++) {
                    if ((int) content.charAt(i) == 65533)
                        nbErrs++;
                }
                if (nbErrs == 0) {
                    return candidateEncodings[e];
                }
            } catch (UnsupportedEncodingException ue) {
                status = -14;
            }
        }
        return null;
    }

    /**
     * Look at html to find an encoding. Decoded with <code>defaultEncoding</code>
     */
    private String extractEncodingFromContent() {
        try {
            String content = new String(rawcontent, defaultEncoding);
            int positionOfEncoding = content.indexOf("charset=");
            if (positionOfEncoding > -1) {
                int start = positionOfEncoding + 8;
                int possibleEnd1 = content.indexOf("\"", start);
                int possibleEnd2 = content.indexOf("'", start);
                int possibleEnd3 = content.indexOf(" ", start);
                if (possibleEnd1 == -1) {
                    possibleEnd1 = Integer.MAX_VALUE;
                }
                if (possibleEnd2 == -1) {
                    possibleEnd2 = Integer.MAX_VALUE;
                }
                if (possibleEnd3 == -1) {
                    possibleEnd3 = Integer.MAX_VALUE;
                }
                int end = Math.min(Math.min(possibleEnd1, possibleEnd2), possibleEnd3);
                if (end < Integer.MAX_VALUE) {
                    String candCharset = content.substring(start, end);
                    candCharset = candCharset.trim();
                    return candCharset;
                } else {
                    return null;
                }
            }
            // <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
        } catch (UnsupportedEncodingException e) {
            status = -14;
        }
        return null;
    }

    private String extractEncodingFromHeader() {
        String res = null;
        for (String aHeader : responseHeaders) {
            if (aHeader.contains("charset=")) {
                res = aHeader.substring(aHeader.indexOf("charset=") + 8);
                break;
            }
        }
        return res;
    }

    private String extractRedirect() {
        for (String aHeader : responseHeaders) {
            if (aHeader.startsWith("Location: ") || aHeader.startsWith("location: ")) {
                return aHeader.substring(aHeader.indexOf(" ") + 1);
            }
        }
        return null;
    }

    private void extractHeaderAndRawContent() {
        endOfHeader = 0;
        for (int i = 3; i < buffer.length; i++) {
            if (buffer[i - 3] == 13 && buffer[i - 2] == 10 && buffer[i - 1] == 13 && buffer[i] == 10) {
                endOfHeader = i;
                break;
            }
        }
        // extraction of header
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < endOfHeader; i++)
            sb.append((char) buffer[i]);
        responseHeaders = sb.toString().trim().split("\n");
        for (int i = 0; i < responseHeaders.length; i++)
            responseHeaders[i] = responseHeaders[i].trim();
        if (responseHeaders.length > 1) {
            status = Integer.parseInt(responseHeaders[0].split(" ")[1]);
            // TODO: [1] could throw indexoutofboundsexception?
        } else {
            status = -12;
            System.err.println("unexpected error: found no header");
        }
        //moving endOfHeader forward until we clear the newlines (not 100% accurate, since the newlines can be considered part of the html, but there are advantages).
        while (endOfHeader < buffer.length && (buffer[endOfHeader] == '\n' || buffer[endOfHeader] == '\r'))
            endOfHeader++;
        // extraction of content
        rawcontent = new byte[buffer.length - endOfHeader];
        System.arraycopy(buffer, endOfHeader + 0, rawcontent, 0, rawcontent.length);
        raw = new byte[buffer.length];
        System.arraycopy(buffer, 0, raw, 0, buffer.length);
    }

    private boolean hasChunkedEncoding() {
        for (String aHeader : responseHeaders) {
            if (aHeader.toLowerCase().contains("transfer-encoding: chunked"))
                return true;
        }
        return false;
    }

    private void fixChunkedEncoding() {
        //		if(Math.random()>-1)return;
        int lastlength = -1, chunkLenTot = 0;
        int p = 0;
        while (lastlength != 0) {
            int endOfLenSpec = p;
            while (!(rawcontent[endOfLenSpec] == '\r' && rawcontent[endOfLenSpec + 1] == '\n'))
                endOfLenSpec++;
            int chunkLen = 0;
            for (int i = p; i < endOfLenSpec; i++) {
                if ((char) rawcontent[i] == ' ')//FIXME, occurred on CNN. bytes= cf <A HREF="/cgi-bin/m?ci=us-204044h&cg=0&cc=1&si=http%3A//edition.cnn.com/2010/WORLD/europe/12/23/italy.embassy.blast/index.html&rp=http%3A//edition.cnn.com/EUROPE/&ts=compact&rnd=1293190300730">Click Here</A>0
                    break;
                chunkLen = chunkLen * 16 + Integer.valueOf("" + (char) rawcontent[i], 16);
            }
            //UI.write("chunklen: "+chunkLen);
            chunkLenTot += chunkLen;
            endOfLenSpec += 2; //0x10 Ox13
            lastlength = chunkLen;
            p = endOfLenSpec + chunkLen + 2;//+2 for 0x10 0x13. Assert it really is.
        }
        //UI.write("tot: "+chunkLenTot);
        byte[] newrawcontent = new byte[chunkLenTot];
        lastlength = -1;
        p = 0;
        int nrwi = 0;
        while (lastlength != 0) {
            int endOfLenSpec = p;
            while (!(rawcontent[endOfLenSpec] == '\r' && rawcontent[endOfLenSpec + 1] == '\n'))
                endOfLenSpec++;
            int chunkLen = 0;
            for (int i = p; i < endOfLenSpec; i++) {
                if ((char) rawcontent[i] == ' ')//FIXME, occurred on CNN. bytes= cf <A HREF="/cgi-bin/m?ci=us-204044h&cg=0&cc=1&si=http%3A//edition.cnn.com/2010/WORLD/europe/12/23/italy.embassy.blast/index.html&rp=http%3A//edition.cnn.com/EUROPE/&ts=compact&rnd=1293190300730">Click Here</A>0
                    break;
                chunkLen = chunkLen * 16 + Integer.valueOf("" + (char) rawcontent[i], 16);
            }
            //UI.write("chunklen: "+chunkLen);
            endOfLenSpec += 2; //0x10 Ox13
            for (int i = 0; i < chunkLen; i++)
                newrawcontent[nrwi++] = rawcontent[endOfLenSpec + i];
            lastlength = chunkLen;
            p = endOfLenSpec + chunkLen + 2;//+2 for 0x10 0x13. Assert it really is.
        }
        rawcontent = newrawcontent;

    }

    private DownloadResult downloadRaw() {
        UrlParts urlParts = splitUrl(url);
        if (!urlParts.isValid()) {
            return new DownloadResult(false, "invalid url");
        }
        PrintStream ps = null;
        InputStream is = null;

        Socket s = null;
        try {
            InetAddress address = ExtraDNSCache.getAddress(urlParts.getHost());
            if (urlParts.isHttps()) {
                SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
                s = ssf.createSocket(address, 443);
                s.setSoTimeout((int) timeOut);
            } else {
                s = new Socket();
                s.setSoTimeout((int) timeOut);
                s.connect(new InetSocketAddress(address, 80), (int) timeOut); //Allow for a timeout even during connect.
            }
            ps = new PrintStream(s.getOutputStream());
            ps.print(method + " " + urlParts.getPath().trim() + " HTTP/1.0\r\n");
            printRequestHeaders(urlParts, ps);
            if (!StringUtils.isEmpty(postData)) {
                ps.print(postData);
            }
            is = s.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copy(is, bos);
            buffer = bos.toByteArray();
            totalBytesDownloaded += buffer.length;
            return new DownloadResult(true, "");
        } catch (UnknownHostException exp) {
            return new DownloadResult(false, "unknown host");
        } catch (java.net.SocketTimeoutException e) {
            return new DownloadResult(false, "socket timeout");
        } catch (SSLException e) {
            return new DownloadResult(false, "ssl problem");
        } catch (Exception e) {
            return new DownloadResult(false, e.getMessage());
        } finally {
            IOUtils.closeQuietly(ps);
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(s);
        }
    }

    private void printRequestHeaders(UrlParts urlParts, PrintStream ps) {
        ps.print("Host: " + urlParts.getHost().trim() + "\r\n");
        ps.print("User-Agent: " + userAgent + "\r\n");
        ps.print("Accept-Charset: utf-8");

        for (String header : extraRequestHeaders) {
            ps.print(header + "\r\n");
        }
        if (!savedCookies.isEmpty()) {
            ps.print("Cookie: " + createCookieHeader() + "\r\n");
        }
        if (!StringUtils.isEmpty(postData)) {
            ps.print("Content-Length: " + postData.getBytes().length);
        }
        ps.print("Connection: close\r\n");
        ps.print("\r\n");
    }

    private String createCookieHeader() {
        String result = "";
        for (String key : savedCookies.keySet()) {
            result += key + "=" + savedCookies.get(key) + ";";
        }
        if (!result.isEmpty()) {
            //Trim trailing ';'
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public String getContentType() {
        for (String field : responseHeaders)
            if (field.startsWith("Content-Type:")) {
                String result = field.replaceFirst("Content-Type:", "");
                result = result.trim();
                result = result.split("[ ;]")[0];
                return result;
            }
        return "text/html"; //Return default ...
    }

    public long getTotalBytesDownloaded() {
        return totalBytesDownloaded;
    }

    public byte[] getRawContent() {
        return rawcontent;
    }

    public String[] getResponseHeaders() {
        return responseHeaders;
    }

    public String getResponseHeader(String name) {
        for (String header : getResponseHeaders()) {
            int indOfColon = header.indexOf(':');
            if (indOfColon > -1) {
                String headerName = header.substring(0, indOfColon);
                if (name.equals(headerName)) {
                    String headerValue = header.substring(indOfColon + 2);
                    return headerValue;
                }
            }
        }
        return null;
    }

    public Map<String, String> getSavedCookies() {
        return savedCookies;
    }

    public List<String> getExtraRequestHeaders() {
        return extraRequestHeaders;
    }

    public void setForwardingIP(String ip) {
        getExtraRequestHeaders().add("X-Forwarded-For: " + ip);
    }

    private class UrlParts {
        private String protocol;
        private String host;
        private String path;

        private UrlParts(String protocol, String host, String path) {
            this.protocol = protocol;
            this.host = host;
            this.path = path;
        }

        public String getProtocol() {
            return protocol;
        }

        public String getHost() {
            return host;
        }

        public String getPath() {
            return path;
        }

        public boolean isHttps() {
            return protocol.equals("https");
        }

        public boolean isValid() {
            return !StringUtils.isEmpty(protocol) && !StringUtils.isEmpty(host) && !StringUtils.isEmpty(path);
        }
    }

}
