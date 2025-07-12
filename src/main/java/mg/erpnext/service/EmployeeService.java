package mg.erpnext.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import mg.erpnext.model.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import mg.erpnext.model.Employee;
import java.util.List;
import java.util.Arrays;

@Service
public class EmployeeService {

    @Value("${erpnext.api.resource-url}")
    private String frappeResourceUrl;

    private static final String EMPLOYEE_LIST = "Employee?fields=";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ApiResponse<List<Employee>> fetchEmployees(HttpSession session) {
        try {
            String fields = "[\"name\",\"employee_name\",\"company\",\"status\"]";
            String url = frappeResourceUrl + EMPLOYEE_LIST + fields;

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
            Employee[] employees = objectMapper.treeToValue(dataNode, Employee[].class);
            return new ApiResponse<>(Arrays.asList(employees));
        } catch (Exception e) {
            return new ApiResponse<>("Erreur lors de la récupération des employés: " + e.getMessage());
        }
    }

    public ApiResponse<List<Employee>> searchEmployeeById(String identifiant, HttpSession session) {
        try {
            // Encodage des champs et filtres pour l'URL
            String fields = "[\"name\",\"employee_name\",\"company\",\"status\"]";
            String filters = "[[\"name\",\"like\",\"%" + identifiant + "%\"]]";
            String url = frappeResourceUrl + "Employee?fields=" + fields + "&filters=" + filters;

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

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode dataNode = root.get("data");
            Employee[] employees = objectMapper.treeToValue(dataNode, Employee[].class);
            return new ApiResponse<>(Arrays.asList(employees));
        } catch (Exception e) {
            return new ApiResponse<>("Erreur lors de la recherche de l'employé: " + e.getMessage());
        }
    }

    public Employee searchOneEmployeeById(String identifiant, HttpSession session) {
        try {
            String fields = "[\"name\",\"employee_name\",\"company\",\"status\"]";
            String filters = "[[\"name\",\"=\",\"" + identifiant + "\"]]";
            String url = frappeResourceUrl + "Employee?fields=" + fields + "&filters=" + filters;

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String frappeSid = (String) session.getAttribute("frappe_sid");
            if (frappeSid == null) {
                return null;
            }
            headers.add(HttpHeaders.COOKIE, "sid=" + frappeSid);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode dataNode = root.get("data");
            Employee[] employees = objectMapper.treeToValue(dataNode, Employee[].class);
            if (employees.length > 0) {
                return employees[0];
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public ApiResponse<List<Employee>> searchEmployeeByName(String nom_complet, HttpSession session) {
        try {
            // Encodage des champs et filtres pour l'URL
            String fields = "[\"name\",\"employee_name\",\"company\",\"status\"]";
            String filters = "[[\"employee_name\",\"like\",\"%" + nom_complet + "%\"]]";
            String url = frappeResourceUrl + "Employee?fields=" + fields + "&filters=" + filters;

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

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode dataNode = root.get("data");
            Employee[] employees = objectMapper.treeToValue(dataNode, Employee[].class);
            return new ApiResponse<>(Arrays.asList(employees));
        } catch (Exception e) {
            return new ApiResponse<>("Erreur lors de la recherche de l'employé: " + e.getMessage());
        }
    }

    public ApiResponse<List<Employee>> searchEmployeeByCompany(String company, HttpSession session) {
        try {
            // Encodage des champs et filtres pour l'URL
            String fields = "[\"name\",\"employee_name\",\"company\",\"status\"]";
            String filters = "[[\"company\",\"=\",\"" + company + "\"]]";
            String url = frappeResourceUrl + "Employee?fields=" + fields + "&filters=" + filters;

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

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode dataNode = root.get("data");
            Employee[] employees = objectMapper.treeToValue(dataNode, Employee[].class);
            return new ApiResponse<>(Arrays.asList(employees));
        } catch (Exception e) {
            return new ApiResponse<>("Erreur lors de la recherche de l'employé: " + e.getMessage());
        }
    }
}