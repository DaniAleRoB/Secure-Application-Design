package co.escuelaing.edu.login;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import org.springframework.stereotype.Service;

@Service
public class SecureServiceCaller {

    public String callOtherService() throws Exception {
        return readUrl("https://danieltdseother.duckdns.org/data");
    }

    private String readUrl(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
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