package mg.erpnext.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import mg.erpnext.model.AttributionCSV;
import mg.erpnext.model.EmployeCSV;
import mg.erpnext.model.ParseResult;

@Service
public class AttributionCSVService {

    private static final Logger logger = LoggerFactory.getLogger(AttributionCSVService.class);
    // public List<AttributionCSV> parseCSVAttributionSalariale(MultipartFile file, List<EmployeCSV> les_employes) throws Exception {
    //     List<AttributionCSV> attributionList = new ArrayList<>();

    //     try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
    //         String line;
    //         boolean firstLine = true;
            
    //         while ((line = br.readLine()) != null) {
    //             if (line.trim().isEmpty()) continue;
                
    //             if (firstLine) {
    //                 firstLine = false;
    //                 continue; // Skip header
    //             }
                
    //             String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                
    //             if (values.length < 4) {
    //                 throw new RuntimeException("Format invalide. 4 colonnes attendues : Mois Ref_Employe Salaire Base_Salaire");
    //             }
                
    //             AttributionCSV attribution = new AttributionCSV();
    //             attribution.setMois(cleanValue(values[0]));
    //            for (EmployeCSV employe : les_employes) {
    //                 try {
    //                     if (employe.getRef().equalsIgnoreCase(cleanValue(values[1]))) {
    //                         System.out.println("ref_em : " + employe.getRef());
    //                         attribution.setEmploye(employe);
    //                         break; // <-- Sortir de la boucle une fois l'employé trouvé
    //                     }
    //                 } catch (NumberFormatException e) {
    //                     System.err.println("Erreur de format de référence: " + employe.getRef());
    //                 }
    //             }
    //             attribution.setSalaire_base(cleanValue(values[2]));
    //             attribution.setSalaire(cleanValue(values[3]));
    //             attributionList.add(attribution); 
    //         }
    //     }
    //     return attributionList;
    // }


    public ParseResult<AttributionCSV> parseCSVAttributionSalariale(MultipartFile file, List<EmployeCSV> lesEmployes) throws Exception {
        ParseResult<AttributionCSV> result = new ParseResult<>();
        int lineNumber = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean firstLine = true;
            
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;
                
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                
                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                
                if (values.length < 4) {
                    result.addError("Ligne " + lineNumber + " mal formatée (4 colonnes attendues, " + values.length + " trouvées): " + line);
                    continue;
                }
                
                try {
                    AttributionCSV attribution = new AttributionCSV();
                    attribution.setMois(cleanValue(values[0]));
                    
                    String refEmploye = cleanValue(values[1]);
                    EmployeCSV employeTrouve = null;
                    
                    for (EmployeCSV employe : lesEmployes) {
                        if (employe.getRef().equalsIgnoreCase(refEmploye)) {
                            employeTrouve = employe;
                            break;
                        }
                    }
                    
                    if (employeTrouve == null) {
                        result.addError("Ligne " + lineNumber + ": Employé avec référence '" + refEmploye + "' non trouvé");
                        continue;
                    }
                    
                    attribution.setEmploye(employeTrouve);
                    attribution.setSalaire_base(cleanValue(values[2]));
                    attribution.setSalaire(cleanValue(values[3]));
                    
                    result.addItem(attribution);
                } catch (Exception e) {
                    result.addError("Ligne " + lineNumber + " erreur de parsing : " + e.getMessage());
                }
            }
        }
        return result;
    }




    private String cleanValue(String value) {
        if (value == null) return "";
        return value.trim().replaceAll("^\"|\"$", "");
    }

    public void importAttributions(List<AttributionCSV> attributions, HttpSession session) throws Exception {
        String sid = (String) session.getAttribute("frappe_sid");
        logger.info("Vérification du frappe_sid dans importAttributions : {}", sid);
        if (sid == null || sid.isEmpty()) {
            logger.error("Session ERPNext absente ou expirée dans importAttributions.");
            throw new RuntimeException("Session ERPNext absente ou expirée.");
        }
        
        String requestUrl = "http://erpnext.localhost:8000/api/method/erpnext.erpnext_integrations.page.import_extract.import_attributions";
        
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = objectMapper.writeValueAsString(attributions);

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
                        System.out.println("Importation réussie pour " + imported + " attribution salariales.");
                    } else {
                        System.out.println("Aucune attribution importée. Vérifiez les données.");
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


    // creér moi une service ici pour reinicialiser le base 

}