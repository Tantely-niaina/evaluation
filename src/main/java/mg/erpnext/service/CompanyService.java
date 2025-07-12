package mg.erpnext.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import mg.erpnext.model.ApiResponse;
import mg.erpnext.model.Company;

@Service
public class CompanyService {
    @Value("${erpnext.api.resource-url}")
    private String frappeResourceUrl;

    private static final String COMPANY_LIST = "Company?fields=";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ApiResponse<List<Company>> fetchCompanies(HttpSession session) {
        try {
            String fields = "[\"name\"]";
            String url = frappeResourceUrl + COMPANY_LIST + fields;

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String frappeSid = (String) session.getAttribute("frappe_sid");
            if (frappeSid == null) {
                return new ApiResponse<>("Session utilisateur non authentifiée.");
            }
            headers.add(HttpHeaders.COOKIE, "sid=" + frappeSid);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class
            );

            // Supposons que la réponse JSON a une structure { "data": [ ... ] }
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode dataNode = root.get("data");
            Company[] companies = objectMapper.treeToValue(dataNode, Company[].class);
            return new ApiResponse<>(Arrays.asList(companies));
        } catch (Exception e) {
            return new ApiResponse<>("Erreur lors de la récupération des employés: " + e.getMessage());
        }
    }
}
