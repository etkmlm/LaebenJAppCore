package com.laeben.core.network;

import com.laeben.core.entity.exception.HttpException;
import com.laeben.core.entity.exception.NoConnectionException;
import com.laeben.core.entity.Path;
import com.laeben.core.entity.RequestParameter;
import com.laeben.core.entity.exception.StopException;
import com.laeben.core.network.entity.NetworkToken;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.*;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Network utils for communication.
 */
public class Network {
    public static final String DOWNLOAD = "download";

    private static final Set<NetworkToken> currentDownloads = new HashSet<>();

    public static boolean hasDownloads(){
        return !currentDownloads.isEmpty();
    }
    public static Set<NetworkToken> getCurrentDownloads(){
        return Collections.unmodifiableSet(currentDownloads);
    }

    /**
     * Convert input stream to string.
     * @param s the input stream
     * @return the content
     */
    public static String inputStreamToString(InputStream s) throws IOException, StopException {
        String read;
        try (BufferedInputStream stream = new BufferedInputStream(s);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()
        ){
            stream.transferTo(buffer);

            read = buffer.toString(StandardCharsets.UTF_8);
        }
        catch (InterruptedIOException | ClosedByInterruptException ignored){
            Thread.currentThread().interrupt();
            throw new StopException();
        }

        return read;
    }

    /**
     * Get the content of the url as a string.
     * @return the content
     */
    public static String urlToString(String url) throws NoConnectionException, HttpException, IOException, StopException {
        return urlToString(url, null);
    }

    /**
     * Get the content of the url as a string with headers.
     * @return the content
     */
    public static String urlToString(String url, List<RequestParameter> headers) throws NoConnectionException, HttpException, IOException, StopException {
        return inputStreamToString(urlToStream(url, headers));
    }

    /**
     * Get the input stream content of the url with headers.
     * @return the content, null if the url was invalid or file was not found
     */
    public static InputStream urlToStream(String url, List<RequestParameter> headers) throws NoConnectionException, IOException, HttpException, StopException {
        if (url == null)
            return null;

        URL u;
        try {
            u = new URL(url);
        } catch (MalformedURLException ignored) {
            return null;
        }

        if (offline)
            throw new NoConnectionException();


        HttpURLConnection conn = null;
        try{
            conn = (HttpURLConnection) u.openConnection();
            if (headers != null){
                for (RequestParameter h : headers){
                    conn.addRequestProperty(h.key(), h.value().toString());
                }
            }
            return conn.getInputStream();
        }
        catch (FileNotFoundException f){
            return null;
        }
        catch (UnknownHostException | NoRouteToHostException ignored){
            throw new NoConnectionException();
        }
        catch (InterruptedIOException | ClosedByInterruptException ignored){
            Thread.currentThread().interrupt();
            throw new StopException();
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
    public static long getContentLength(String url) throws NoConnectionException, IOException, StopException {

        if (offline)
            throw new NoConnectionException();

        try {
            URL uri = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)uri.openConnection();
            return conn.getContentLengthLong();
        }
        catch (InterruptedIOException | ClosedByInterruptException ignored){
            Thread.currentThread().interrupt();
            throw new StopException();
        }
        catch (UnknownHostException | NoRouteToHostException ignored){
            throw new NoConnectionException();
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
     * Stops all continuing download processes.
     */
    public static void stop(){
        currentDownloads.forEach(NetworkToken::stop);
    }

    /**
     * Patches and disables SSL. :))
     */
    public static boolean patchSSL(){
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

            return true;
        } catch (KeyManagementException | NoSuchAlgorithmException ignored) {
            return false;
        }
    }

    /**
     * Download a file from the net.
     * @param token network token
     * @return path of the downloaded file
     */
    public static Path download(NetworkToken token) throws NoConnectionException, StopException, HttpException, IOException {
        if (offline)
            throw new NoConnectionException();
        HttpsURLConnection conn = null;
        String url = token.getUrl();
        Path destination = token.getDestination();
        try{
            currentDownloads.add(token);

            URL oldUri = new URL(url);
            url = url.replace(" ", "%20");
            URL uri = new URL(url);
            if (token.useOriginalName()){
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
                byte[] buffer = new byte[8192];
                int read;
                while ((read = stream.read(buffer)) != -1){
                    if (token.shouldStop())
                        throw new StopException();
                    file.write(buffer, 0, read);
                    progress += buffer.length;
                    token.onReceivedProgress(progress, length);
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
        catch (InterruptedIOException | ClosedByInterruptException ignored){
            Thread.currentThread().interrupt();
            throw new StopException();
        }
        catch (IOException ex){
            handleNetIO(ex, conn, url);
        }
        finally {
            currentDownloads.remove(token);
        }

        return destination;
    }

    private static void handleNetIO(IOException ex, HttpURLConnection conn, String url) throws HttpException, IOException, StopException {
        if (ex.getMessage().startsWith("Server returned")){
            String[] spl = ex.getMessage().split(":");
            if (spl.length != 4 || conn == null)
                throw ex;
            else {
                int code = Integer.parseInt(spl[1].split(" ")[1]);
                throw new HttpException(code, inputStreamToString(conn.getErrorStream()), url);
            }
        }
        else throw ex;
    }

    /**
     * Post to a url.
     * @param url destination url
     * @param body request body
     * @return the response
     */
    public static String post(String url, String body) throws NoConnectionException, StopException, IOException {
        return post(url, body, null);
    }

    /**
     * Post to an url with headers.
     * @param url destination url
     * @param body request body
     * @param headers headers
     * @return the response
     */
    public static String post(String url, String body, List<RequestParameter> headers) throws NoConnectionException, StopException, IOException {
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
                    catch (InterruptedIOException | ClosedByInterruptException ignored){
                        Thread.currentThread().interrupt();
                        throw new StopException();
                    }
                }
            }
        }
        catch (UnknownHostException | NoRouteToHostException ignored){
            throw new NoConnectionException();
        }
        catch (InterruptedIOException | ClosedByInterruptException ignored){
            Thread.currentThread().interrupt();
            throw new StopException();
        }

        return answer;
    }
    private static String streamToString(InputStream str) throws IOException, StopException {
        StringBuilder answer = new StringBuilder();
        try(InputStreamReader reader = new InputStreamReader(str)){
            char[] buffer = new char[4096];
            int read;
            while ((read = reader.read(buffer)) != -1){
                if (Thread.currentThread().isInterrupted())
                    throw new StopException();
                answer.append(buffer, 0, read);
            }
        }
        catch (InterruptedIOException | ClosedByInterruptException ignored){
            Thread.currentThread().interrupt();
            throw new StopException();
        }

        return answer.toString();
    }

    /**
     * Opens a temporary http server.
     * @param port server port
     * @param response response of the server
     * @return the request from the client
     */
    public static String listenServer(int port, String response) throws IOException, StopException {
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
        catch (InterruptedIOException | ClosedByInterruptException ignored){
            Thread.currentThread().interrupt();
            throw new StopException();
        }
    }

    protected static boolean offline;

    public static boolean isOffline(){
        return offline;
    }
    public static void setOffline(boolean offline) {
        Network.offline = offline;
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
