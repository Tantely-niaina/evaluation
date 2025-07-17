package mg.erpnext.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import mg.erpnext.model.AttributionCSV;
import mg.erpnext.model.EmployeCSV;
import mg.erpnext.model.SalaryStructureCSV;

@Service
public class FicheEmployeCSVService {
    private static final Logger logger = LoggerFactory.getLogger(FicheEmployeCSVService.class);


    public void importCustomer(Object listCustomers, HttpSession session) throws Exception {
        String sid = (String) session.getAttribute("frappe_sid");
        logger.info("Vérification du frappe_sid dans importCustomer : {}", sid);
        if (sid == null || sid.isEmpty()) {
            logger.error("Session ERPNext absente ou expirée dans importCustomer.");
            throw new RuntimeException("Session ERPNext absente ou expirée.");
        }

        String requestUrl = "http://erpnext.localhost:8000/api/method/erpnext.erpnext_integrations.page.import_extract.import_employes";

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = objectMapper.writeValueAsString(listCustomers);

        // HttpHeaders headers = new HttpHeaders();
        // headers.set("Cookie", "sid=" + sid);
        // headers.setContentType(MediaType.APPLICATION_JSON);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "sid=" + (String) session.getAttribute("frappe_sid"));
        // headers.set("Cookie", "sid=" + sid);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        
        HttpEntity<String> request = new HttpEntity<>(jsonData, headers);
        RestTemplate restTemplate = new RestTemplate();
        
        ResponseEntity<String> response = restTemplate.exchange(
            requestUrl, 
            HttpMethod.POST, 
            request, 
            String.class
        );

        
        
        if (response.getStatusCode().is2xxSuccessful()) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode message = root.path("message");
            
            if (!message.isMissingNode() && message.isObject()) {
                String status = message.path("status").asText();
                int imported = message.path("imported").asInt();
                
                if ("success".equals(status) || "partial".equals(status)) {
                    if (imported > 0) {
                        System.out.println("Importation réussie pour " + imported + " employés.");
                    } else {
                        System.out.println("Aucun employé importé. Vérifiez les données.");
                    }
                    
                    if (message.has("errors")) {
                        System.out.println("Erreurs partielles: " + message.get("errors").toString());
                        throw new RuntimeException("Erreurs partielles: " + message.get("errors").toString());
                    }
                } else {
                    throw new RuntimeException("Erreur d'import: " + message.path("message").asText());
                }
            } else {
                throw new RuntimeException("Réponse inattendue du serveur.");
            }
        } else {
            throw new RuntimeException("Échec de la requête: " + response.getStatusCode());
        }
    }




    public void importStructures(List<SalaryStructureCSV> structures, HttpSession session) throws Exception {
        String sid = (String) session.getAttribute("frappe_sid");
        logger.info("Vérification du frappe_sid dans importStructures : {}", sid);
        if (sid == null || sid.isEmpty()) {
            logger.error("Session ERPNext absente ou expirée dans importStructures.");
            throw new RuntimeException("Session ERPNext absente ou expirée.");
        }
        
        String requestUrl = "http://erpnext.localhost:8000/api/method/erpnext.erpnext_integrations.page.import_extract.import_structures";
        
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = objectMapper.writeValueAsString(structures);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "sid=" + (String) session.getAttribute("frappe_sid"));
        // headers.set("Cookie", "sid=" + sid);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        
        HttpEntity<String> request = new HttpEntity<>(jsonData, headers);
        RestTemplate restTemplate = new RestTemplate();
        
        ResponseEntity<String> response = restTemplate.exchange(
            requestUrl, 
            HttpMethod.POST, 
            request, 
            String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode message = root.path("message");
            
            if (!message.isMissingNode() && message.isObject()) {
                String status = message.path("status").asText();
                int imported = message.path("imported").asInt();
                
                if ("success".equals(status) || "partial".equals(status)) {
                    if (imported > 0) {
                        System.out.println("Importation réussie pour " + imported + " structures salariales.");
                    } else {
                        System.out.println("Aucune structure importée. Vérifiez les données.");
                    }
                    
                    if (message.has("errors")) {
                        System.out.println("Erreurs partielles: " + message.get("errors").toString());
                        throw new RuntimeException("Erreurs partielles: " + message.get("errors").toString());
                    }
                } else {
                    throw new RuntimeException("Erreur d'import: " + message.path("message").asText());
                }
            } else {
                throw new RuntimeException("Réponse inattendue du serveur.");
            }
        } else {
            throw new RuntimeException("Échec de la requête: " + response.getStatusCode());
        }
    }


    public void import_trois_fichiers(List<EmployeCSV> employees, List<SalaryStructureCSV> structures, 
                                List<AttributionCSV> attributions, HttpSession session) throws Exception {
        String sid = (String) session.getAttribute("frappe_sid");
        logger.info("Vérification du frappe_sid dans import_trois_fichiers : {}", sid);
        if (sid == null || sid.isEmpty()) {
            logger.error("Session ERPNext absente ou expirée dans import_trois_fichiers.");
            throw new RuntimeException("Session ERPNext absente ou expirée.");
        }
        
        // Create a wrapper object for all three lists
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> allData = new HashMap<>();
        allData.put("employees", employees);
        allData.put("structures", structures);
        allData.put("attributions", attributions);
        String jsonData = objectMapper.writeValueAsString(allData);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "sid=" + (String) session.getAttribute("frappe_sid"));
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        
        HttpEntity<String> request = new HttpEntity<>(jsonData, headers);
        RestTemplate restTemplate = new RestTemplate();
        
        String requestUrl = "http://erpnext.localhost:8000/api/method/erpnext.erpnext_integrations.page.import_extract.import_trois_fichiers";
        
        ResponseEntity<String> response = restTemplate.exchange(
            requestUrl, 
            HttpMethod.POST, 
            request, 
            String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode message = root.path("message");
            
            if (!message.isMissingNode() && message.isObject()) {
                String status = message.path("status").asText();
                int imported = message.path("imported").asInt();
                
                if ("success".equals(status) || "partial".equals(status)) {
                    if (imported > 0) {
                        System.out.println("Importation réussie pour " + imported + " éléments.");
                    } else {
                        System.out.println("Aucun élément importé. Vérifiez les données.");
                    }
                    
                    if (message.has("errors")) {
                        System.out.println("Erreurs partielles: " + message.get("errors").toString());
                        throw new RuntimeException("Erreurs partielles: " + message.get("errors").toString());
                    }
                } else {
                    throw new RuntimeException("Erreur d'import: " + message.path("message").asText() );
                }
            } else {
                throw new RuntimeException("Réponse inattendue du serveur.");
            }
        } else {
            throw new RuntimeException("Échec de la requête: " + response.getStatusCode());
        }
    }
}