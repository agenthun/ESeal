package com.pekingopera.versionupdate.download;



import com.pekingopera.versionupdate.ParseData;
import com.pekingopera.versionupdate.listener.OnlineCheckListener;
import com.pekingopera.versionupdate.type.RequestType;
import com.pekingopera.versionupdate.util.HandlerUtil;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 *
 */
public class OnlineCheckWorker implements Runnable {

    protected String url;
    protected OnlineCheckListener checkCB;
    protected ParseData parser;
    private RequestType requestType;
    protected TreeMap<String, Object> checkParams;

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUpdateListener(OnlineCheckListener checkCB) {
        this.checkCB = checkCB;
    }

    public void setParser(ParseData parser) {
        this.parser = parser;
    }

    public void setRequestMethod(RequestType requestType) {
        this.requestType = requestType;
    }

    public void setParams(TreeMap<String, Object> checkParams) {
        this.checkParams = checkParams;
    }

    @Override
    public void run() {
        try {
            String response = null;
            if (requestType == RequestType.post) {
                response = check(requestType, url, checkParams);
            } else {
                response = check(requestType, url);
            }
            String parse = parser.parse(response);
            if (parse == null) {
                throw new IllegalArgumentException("parse response to update failed by " + parser.getClass().getCanonicalName());
            }
            sendHasParams(parse);
        } catch (HttpException he) {
        } catch (Exception e) {
        }
    }

    protected String check(RequestType requestType, String urlStr) throws Exception {
        URL getUrl = new URL(urlStr);
        HttpURLConnection urlConn = (HttpURLConnection) getUrl.openConnection();
        urlConn.setConnectTimeout(10000);
        if (requestType == RequestType.get) {
            urlConn.setRequestMethod("GET");
        } else {
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            urlConn.setRequestMethod("POST");
        }
        urlConn.connect();

        int responseCode = urlConn.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            throw new HttpException(responseCode, urlConn.getResponseMessage());
        }

        BufferedReader bis = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "utf-8"));

        StringBuilder sb = new StringBuilder();
        String lines;
        while ((lines = bis.readLine()) != null) {
            sb.append(lines);
        }
        return sb.toString();
    }

    protected String check(RequestType requestType, String url, Map<String, Object> requestParams) {

        if (requestParams == null) {
            requestParams = new TreeMap();
        }
        String result = "";
        BufferedReader in = null;
        String paramStr = "";
        Iterator realUrl = requestParams.keySet().iterator();

        String paj;
        while (realUrl.hasNext()) {
            paj = (String) realUrl.next();
            if (!paramStr.isEmpty()) {
                paramStr = paramStr + '&';
            }
            try {
                paramStr = paramStr + paj + '=' + URLEncoder.encode(requestParams.get(paj).toString(), "utf-8");
            } catch (UnsupportedEncodingException var28) {
                result = "{\"code\":-2100,\"location\":\"Request:120\",\"message\":\"api sdk throw exception! " + var28.toString() + "\"}";
            }
        }

        try {
            if (requestType == RequestType.get) {
                if (url.indexOf(63) > 0) {
                    url = url + '&' + paramStr;
                } else {
                    url = url + '?' + paramStr;
                }
            }

//            paj = "---------------------------" + MD5.stringToMD5(String.valueOf(System.currentTimeMillis())).substring(0, 15);
            paj = "---------------------------";
            URL realUrl1 = new URL(url);
            Object connection = null;
            if (url.toLowerCase().startsWith("https")) {
                HttpsURLConnection line = (HttpsURLConnection) realUrl1.openConnection();
                line.setHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
                connection = line;
            } else {
                connection = realUrl1.openConnection();
            }

            ((URLConnection) connection).setRequestProperty("accept", "*/*");
            ((URLConnection) connection).setRequestProperty("connection", "Keep-Alive");
            ((URLConnection) connection).setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            ((URLConnection) connection).setConnectTimeout(10000);
            if (requestType == RequestType.post) {
                ((HttpURLConnection) connection).setRequestMethod("POST");
                ((URLConnection) connection).setDoOutput(true);
                ((URLConnection) connection).setDoInput(true);
                ((URLConnection) connection).setRequestProperty("Content-Type", "multipart/form-data; boundary=" + paj);
                DataOutputStream line1 = new DataOutputStream(((URLConnection) connection).getOutputStream());
                StringBuffer strBuf = new StringBuffer();
                Iterator filename = requestParams.keySet().iterator();

                while (filename.hasNext()) {
                    String endData = (String) filename.next();
                    if (!endData.equals("Debug")) {
                        strBuf.append("\r\n").append("--").append(paj).append("\r\n");
                        strBuf.append("Content-Disposition: form-data; name=\"" + endData + "\"\r\n\r\n");
                        strBuf.append(requestParams.get(endData));
                    }
                }
                System.out.println("URL----POST:" + url);
                System.out.println("URL----PARAMS:" + strBuf.toString());


                line1.write(strBuf.toString().getBytes());

                byte[] endData2 = ("\r\n--" + paj + "--\r\n").getBytes();
                line1.write(endData2);
                line1.flush();
                line1.close();
            }

            ((URLConnection) connection).connect();

            String line2;
            for (in = new BufferedReader(new InputStreamReader(((URLConnection) connection).getInputStream())); (line2 = in.readLine()) != null; result = result + line2) {
                ;
            }
        } catch (Exception var29) {
            result = "{\"code\":-2200,\"location\":\"Request:220\",\"message\":\"api sdk throw exception! " + var29.toString() + "\"}";
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception var27) {
                result = "{\"code\":-2300,\"location\":\"Request:219\",\"message\":\"api sdk throw exception! " + var27.toString() + "\"}";
            }

        }
        return result;
    }

    private void sendHasParams(final String params) {
        if (checkCB == null) return;
        HandlerUtil.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                checkCB.hasParams(params);
            }
        });
    }
}
