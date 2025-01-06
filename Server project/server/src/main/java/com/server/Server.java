package com.server;

import com.sun.net.httpserver.*;

import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import java.io.*;

class Server {

    public static void main(String[] args) throws Exception {
        try {
            UserAuthenticator authchecker = new UserAuthenticator();

            HttpsServer server = HttpsServer.create(new InetSocketAddress(8001), 0);
            final HttpContext finalContext = server.createContext("/info", new Handler());
            final HttpContext pathContext = server.createContext("/paths", new Path());
            server.createContext("/registration", new RegistrationHandler(authchecker));

            finalContext.setAuthenticator(authchecker);
            pathContext.setAuthenticator(authchecker);

            SSLContext sslContext = myServerSSLContext(args[0], args[1]);
            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    InetSocketAddress remote = params.getClientAddress();
                    SSLContext c = getSSLContext();
                    SSLParameters sslparams = c.getDefaultSSLParameters();
                    params.setSSLParameters(sslparams);
                }
            });

            // creates a default executor
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();

            MessageDatabase dataBase = MessageDatabase.getInstance();
            dataBase.open("MessageDB");

        } catch (FileNotFoundException e) {
            // Certificate file not found!
            System.out.println("Certificate not found!");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SSLContext myServerSSLContext(String keystorePath, String passWord) throws Exception {
        char[] passphrase = passWord.toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(keystorePath), passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return ssl;
    }
}
