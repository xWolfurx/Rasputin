package net.wolfur.rasputin.web;

import com.sun.net.httpserver.*;
import net.wolfur.rasputin.util.Logger;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

public class WebServer {

    private HttpsServer server;

    public WebServer(int port) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
            String keystoreFilename = "/home/worker/keystore.jks";
            char[] storepass = "STOREPASS".toCharArray();
            char[] keypass = "STOREPASS".toCharArray();

            FileInputStream fileInputStream = new FileInputStream(keystoreFilename);
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(fileInputStream, storepass);

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, keypass);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            this.server = HttpsServer.create(new InetSocketAddress(port), 0);

            this.server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    try {
                        SSLContext c = SSLContext.getDefault();
                        SSLEngine engine = c.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());

                        SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
                        params.setSSLParameters(defaultSSLParameters);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            this.server.createContext("/auth/", new CodeHandler());

            this.server.setExecutor(null);
            this.server.start();
    }

    public void stop() {
        this.server.stop(0);
        this.server = null;
        Logger.warning("Shut down Web-Server.", true);
    }

    public static void writeResponse(HttpExchange httpExchange, String response) throws IOException {
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    public static Map<String, String> queryToMap(String query){
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1) {
                result.put(pair[0], pair[1]);
            }else{
                result.put(pair[0], "");
            }
        }
        return result;
    }
}
