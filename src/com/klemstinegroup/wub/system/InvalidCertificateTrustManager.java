package com.klemstinegroup.wub.system;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * ignore invalid Https certificate from OPAM
 * <p>see http://javaskeleton.blogspot.com.br/2011/01/avoiding-sunsecurityvalidatorvalidatore.html
 */
public class InvalidCertificateTrustManager implements X509TrustManager {
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    public void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {

    }

    public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {
    }
}
