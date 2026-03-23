package co.escuelaing.edu.login;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyStore;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.springframework.stereotype.Service;

@Service
public class SecureServiceCaller {

    public String callOtherService() throws Exception {
        String trustStorePath = System.getenv("TRUSTSTORE_PATH");
        String trustStorePassword = System.getenv("TRUSTSTORE_PASSWORD") != null
            ? System.getenv("TRUSTSTORE_PASSWORD")
            : "123456";

        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        if (trustStorePath != null && !trustStorePath.isBlank()) {
            try (InputStream fileStream = new FileInputStream(trustStorePath)) {
                trustStore.load(fileStream, trustStorePassword.toCharArray());
            }
        } else {
            try (InputStream classpathStream = getClass().getClassLoader()
                .getResourceAsStream("keystores/loginTrustStore.p12")) {
                if (classpathStream == null) {
                    throw new IllegalStateException("Truststore not found: keystores/loginTrustStore.p12");
                }
                trustStore.load(classpathStream, trustStorePassword.toCharArray());
            }
        }

        TrustManagerFactory tmf = TrustManagerFactory
            .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);
        SSLContext.setDefault(sslContext);

        return readUrl("https://danieltdseother.duckdns.org:8443/data");
    }

    private String readUrl(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setHostnameVerifier((hostname, session) -> true);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
