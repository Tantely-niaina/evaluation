package mg.erpnext.service;

import java.util.List;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import mg.erpnext.model.LoginRequest;

import org.springframework.beans.factory.annotation.Value;


@Service
public class AuthService {
    @Value("${erpnext.api.method-url}")
    private String frappeBaseUrl;

    private static final String LOGIN_PATH = "login";

    public AuthResult authenticate(LoginRequest loginRequest) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "{\"usr\":\"" + loginRequest.getUsr() + "\",\"pwd\":\"" + loginRequest.getPwd() + "\"}";
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            frappeBaseUrl + LOGIN_PATH,
            HttpMethod.POST,
            entity,
            String.class
        );

        String sid = null;
        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookies != null) {
            for (String cookie : cookies) {
                if (cookie.startsWith("sid=")) {
                    sid = cookie.split(";", 2)[0].substring(4); // "sid=xxxx"
                    break;
                }
            }
        }

        return new AuthResult(response, sid);
    }

    public static class AuthResult {
        private final ResponseEntity<String> response;
        private final String sid;

        public AuthResult(ResponseEntity<String> response, String sid) {
            this.response = response;
            this.sid = sid;
        }

        public ResponseEntity<String> getResponse() {
            return response;
        }

        public String getSid() {
            return sid;
        }
    }
}