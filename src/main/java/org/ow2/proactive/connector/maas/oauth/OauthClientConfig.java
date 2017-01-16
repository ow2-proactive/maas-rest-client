package org.ow2.proactive.connector.maas.oauth;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.web.client.RestTemplate;

public class OauthClientConfig {

	public RestTemplate restTemplate(String[] token, boolean ignoreHttpsCert) {

	    // Bypass self signed HTTPS certificate
        if (ignoreHttpsCert) {
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier());
            HttpsURLConnection.setDefaultSSLSocketFactory(getSSLSocketFactory());
        }

        return ProtectedResourceClientFactory.create(consumerKey, consumerSecret, accessKey, accessSecret);
    }

	private SSLSocketFactory getSSLSocketFactory() {

        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
            public X509Certificate[] getAcceptedIssuers(){return null;}
            public void checkClientTrusted(X509Certificate[] certs, String authType){}
            public void checkServerTrusted(X509Certificate[] certs, String authType){}
        }};

        // Install the all-trusting trust manager
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            return sc.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private HostnameVerifier hostnameVerifier() {
        return new javax.net.ssl.HostnameVerifier() {

            public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
                if (hostname.equals("maas-1.activeeon.com")) {
                    return true;
                }
                return false;
            }
        };
    }
}
