package com.laeben.core.util;

import com.laeben.core.LaebenApp;
import com.laeben.core.entity.exception.HttpException;
import com.laeben.core.entity.exception.NoConnectionException;
import com.laeben.core.entity.Path;
import com.laeben.core.entity.RequestParameter;
import com.laeben.core.entity.exception.StopException;
import com.laeben.core.util.events.ValueEvent;
import com.laeben.core.util.events.ProgressEvent;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Network utils for communication.
 */
public class NetUtils {
    public static final String DOWNLOAD = "download";

    private static final EventHandler<ProgressEvent> handler = new EventHandler<>();

    public static EventHandler<ProgressEvent> getHandler(){
        return handler;
    }

    private static boolean stopRequested;
    private static boolean downloading;

    /**
     * Convert input stream to string.
     * @param s the input stream
     * @return the content
     */
    public static String inputStreamToString(InputStream s){
        String read = "{}";
        try (BufferedInputStream stream = new BufferedInputStream(s);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()
        ){
            int r;
            while ((r = stream.read()) != -1){
                buffer.write(r);
            }


            read = buffer.toString(StandardCharsets.UTF_8);
        }
        catch (IOException e){
            LaebenApp.getHandler().execute(new ValueEvent(LaebenApp.EXCEPTION, e));
        }

        return read;
    }

    /**
     * Get the content of the url as a string.
     * @return the content
     */
    public static String urlToString(String url) throws NoConnectionException, HttpException {
        return urlToString(url, null);
    }

    /**
     * Get the content of the url as a string with headers.
     * @return the content
     */
    public static String urlToString(String url, List<RequestParameter> headers) throws NoConnectionException, HttpException {
        if (url == null)
            return null;

        URL u;
        try {
            u = new URL(url);
        } catch (MalformedURLException e) {
            LaebenApp.handleException(e);
            return null;
        }

        if (offline)
            throw new NoConnectionException();


        HttpsURLConnection conn = null;
        try{
            conn = (HttpsURLConnection) u.openConnection();
            if (headers != null){
                for (RequestParameter h : headers){
                    conn.addRequestProperty(h.key(), h.value().toString());
                }
            }
            return inputStreamToString(conn.getInputStream());
        }
        catch (FileNotFoundException f){
            return null;
        }
        catch (UnknownHostException | NoRouteToHostException ignored){
            throw new NoConnectionException();
        }
        catch (IOException e){
            handleNetIO(e, conn, url);
            return null;
        }
    }

    /**
     * Get the content length of the url.
     * @return the content
     */
    public static long getContentLength(String url) throws NoConnectionException {

        if (offline)
            throw new NoConnectionException();

        try {
            URL uri = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)uri.openConnection();
            return conn.getContentLengthLong();
        }
        catch (UnknownHostException | NoRouteToHostException ignored){
            throw new NoConnectionException();
        }
        catch (IOException e){
            LaebenApp.handleException(e);
            return 0;
        }
    }

    /**
     * Get the file name from the url;
     * @return the file name
     */
    public static String getFileNameFromUrl(URL url){
        String[] p = url.getFile().split("/");
        return p[p.length - 1];
    }

    /**
     * Get the url object from the url.
     * @return the url object
     */
    public static URL getUrl(String url){
        try{
            return new URL(url);
        }
        catch (MalformedURLException ignored){
            throw new RuntimeException();
        }
    }

    /**
     * Stop the continuing download process.
     */
    public static void stop(){
        if (downloading)
            stopRequested = true;
    }


    /**
     * Patches and disables SSL. :))
     */
    public static void patchSSL(){
        try {
            var ssl = SSLContext.getInstance("SSL");

            ssl.init(null, new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {

                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {

                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            }, null);

            HttpsURLConnection.setDefaultSSLSocketFactory(ssl.getSocketFactory());

            HttpsURLConnection.setDefaultHostnameVerifier((a, b) -> true);
        }
        catch (Exception e) {
            LaebenApp.handleException(e);
        }
    }

    /**
     * Download a file from the net.
     * @param url destination url
     * @param destination file or directory path relative to useOriginalName
     * @param useOriginalName use destination as a file or base dir
     * @param handle progress handling
     * @return path of the downloaded file
     */
    public static Path download(String url, Path destination, boolean useOriginalName, boolean handle) throws NoConnectionException, StopException, HttpException, FileNotFoundException {

        if (offline)
            throw new NoConnectionException();
        HttpsURLConnection conn = null;
        downloading = true;
        try{
            URL oldUri = new URL(url);
            url = url.replace(" ", "%20");
            URL uri = new URL(url);
            if (useOriginalName){
                String fileName = getFileNameFromUrl(oldUri);
                destination = destination.to(fileName);
            }
            destination.prepare();

            conn = (HttpsURLConnection) uri.openConnection();

            long length = conn.getContentLengthLong();
            long progress = 0;

            try(InputStream stream = conn.getInputStream();
                FileOutputStream file = new FileOutputStream(destination.toFile())
            ){
                byte[] buffer = new byte[4096];
                int read;
                while ((read = stream.read(buffer)) != -1){
                    if (stopRequested)
                        throw new StopException();
                    file.write(buffer, 0, read);
                    progress += buffer.length;
                    if (handle)
                        handler.execute(new ProgressEvent(DOWNLOAD, progress, length));
                }
            }
            destination.toFile().setLastModified(conn.getLastModified());
        }
        catch (UnknownHostException | NoRouteToHostException ignored){
            throw new NoConnectionException();
        }
        catch (FileNotFoundException fo){
            throw fo;
        }
        catch (StopException e){
            stopRequested = false;
            throw e;
        }
        catch (IOException ex){
            handleNetIO(ex, conn, url);
        }
        finally {
            downloading = false;
        }

        return destination;
    }

    private static void handleNetIO(IOException ex, HttpsURLConnection conn, String url) throws HttpException {
        if (ex.getMessage().startsWith("Server returned")){
            String[] spl = ex.getMessage().split(":");
            if (spl.length != 4 || conn == null)
                LaebenApp.handleException(ex);
            else {
                int code = Integer.parseInt(spl[1].split(" ")[1]);
                throw new HttpException(code, inputStreamToString(conn.getErrorStream()), url);
            }
        }
        else
            LaebenApp.handleException(ex);
    }

    /**
     * Post to a url.
     * @param url destination url
     * @param body request body
     * @return the response
     */
    public static String post(String url, String body) throws NoConnectionException {
        return post(url, body, null);
    }

    /**
     * Post to an url with headers.
     * @param url destination url
     * @param body request body
     * @param headers headers
     * @return the response
     */
    public static String post(String url, String body, List<RequestParameter> headers) throws NoConnectionException {
        String answer = null;

        if (offline)
            throw new NoConnectionException();

        try{
            URL uri = new URL(url);

            HttpURLConnection connection = (HttpURLConnection)uri.openConnection();
            if (headers != null){
                for (RequestParameter h : headers)
                    connection.addRequestProperty(h.key(), h.value().toString());
            }

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            try (OutputStream stream = connection.getOutputStream();
                 OutputStreamWriter writer = new OutputStreamWriter(stream)){

                writer.write(body);
            }

            try(InputStream stream = connection.getInputStream()){
                answer = streamToString(stream);
            }
            catch (IOException e){
                if (connection.getErrorStream() != null){
                    try(InputStream stream = connection.getErrorStream()){
                        answer = streamToString(stream);
                    }
                    catch (IOException ex){
                        LaebenApp.handleException(ex);
                    }
                }
            }
        }
        catch (UnknownHostException | NoRouteToHostException ignored){
            throw new NoConnectionException();
        }
        catch (IOException e){
            LaebenApp.handleException(e);
        }

        return answer;
    }
    private static String streamToString(InputStream str){
        StringBuilder answer = new StringBuilder();
        try(InputStreamReader reader = new InputStreamReader(str)){
            char[] buffer = new char[4096];
            int read;
            while ((read = reader.read(buffer)) != -1){
                answer.append(buffer, 0, read);
            }
        }
        catch (IOException e){
            LaebenApp.handleException(e);
        }

        return answer.toString();
    }

    /**
     * Opens a temporary http server.
     * @param port server port
     * @param response response of the server
     * @return the request from the client
     */
    public static String listenServer(int port, String response){
        try(ServerSocket socket = new ServerSocket(port);
            Socket a = socket.accept();
            BufferedReader input = new BufferedReader(new InputStreamReader(a.getInputStream()));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(a.getOutputStream()))){

            StringBuilder content = new StringBuilder();
            String read;
            do {
                content.append(read = input.readLine()).append("\n");
            }while (read != null && !read.isEmpty() && !read.trim().isEmpty());

            output.write("HTTP/1.1 200 OK");

            if (response != null){
                output.write("\nContent-Type: text/html");
                output.write("\nContent-Length: " + response.length());
                output.write("\n\n" + response);
            }

            return content.toString();
        }
        catch (Exception e){
            LaebenApp.handleException(e);
            return null;
        }
    }

    protected static boolean offline;

    public static boolean isOffline(){
        return offline;
    }
    public static void setOffline(boolean offline) {
        NetUtils.offline = offline;
    }

    /**
     * Check network state.
     * @return state
     */
    public static boolean check(){
        try{
            URL url = new URL("https://google.com");
            URLConnection c = url.openConnection();
            c.getInputStream().read();
            c.getInputStream().close();


            return true;
        }
        catch (UnknownHostException | NoRouteToHostException ignored){
            return false;
        }
        catch (Exception e){
            return true;
        }
    }
}
