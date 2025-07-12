package mg.erpnext.service;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import mg.erpnext.model.ApiResponse;
import mg.erpnext.model.SalarySlip;
@Service
public class SalarySlipWithComponentsService {

    @Value("${erpnext.api.resource-url}")
    private String frappeResourceUrl;

    private static final Logger logger = LoggerFactory.getLogger(SalarySlipWithComponentsService.class);
    private static final String SALARY_SLIP_LIST = "Salary Slip?fields=";
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Récupère la liste des Salary Slip avec leurs gains (earnings) et déductions.
     */
    public ApiResponse<List<SalarySlip>> fetchSalarySlipsWithComponents(HttpSession session) {
        logger.info("Appel fetchSalarySlipsWithComponents()");
        return fetchAndFilterSalarySlips(session, null, 0, 0, false);
    }

    /**
     * Filtre les Salary Slips contenant un Salary Component spécifique avec montant min et max.
     */
    public ApiResponse<List<SalarySlip>> filterSalarySlipsByComponent(HttpSession session, double montantMin, double montantMax, String componentName) {
        logger.info("Appel filterSalarySlipsByComponent() - component: {}, montantMin: {}, montantMax: {}", componentName, montantMin, montantMax);
        return fetchAndFilterSalarySlips(session, componentName, montantMin, montantMax, true);
    }

    /**
     * Méthode interne pour récupérer et éventuellement filtrer les Salary Slips.
     */
    private ApiResponse<List<SalarySlip>> fetchAndFilterSalarySlips(HttpSession session, String componentName, double montantMin, double montantMax, boolean applyFilter) {
        try {
            // Champs sans earnings/deductions pour éviter l'erreur
            String fields = "[\"name\",\"employee\",\"employee_name\",\"start_date\",\"end_date\",\"net_pay\",\"gross_pay\",\"total_deduction\"]";
            String url = frappeResourceUrl + SALARY_SLIP_LIST + fields;

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String frappeSid = (String) session.getAttribute("frappe_sid");
            if (frappeSid == null) {
                logger.warn("Session utilisateur non authentifiée.");
                return new ApiResponse<>("Session utilisateur non authentifiée.");
            }
            headers.add(HttpHeaders.COOKIE, "sid=" + frappeSid);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode dataNode = root.get("data");
            SalarySlip[] allSalarySlips = objectMapper.treeToValue(dataNode, SalarySlip[].class);

            logger.info("Nombre total de Salary Slips : {}", allSalarySlips.length);

            List<SalarySlip> resultSlips = new ArrayList<>();

            for (SalarySlip slip : allSalarySlips) {
                String slipUrl = frappeResourceUrl + "Salary Slip/" + slip.getName();
                ResponseEntity<String> slipResponse = restTemplate.exchange(
                    slipUrl, HttpMethod.GET, entity, String.class
                );

                JsonNode slipRoot = objectMapper.readTree(slipResponse.getBody());
                JsonNode slipData = slipRoot.get("data");

                JsonNode earningsNode = slipData.get("earnings");
                JsonNode deductionsNode = slipData.get("deductions");

                List<Map<String, Object>> earnings = objectMapper.convertValue(
                    earningsNode, new TypeReference<List<Map<String, Object>>>() {});
                List<Map<String, Object>> deductions = objectMapper.convertValue(
                    deductionsNode, new TypeReference<List<Map<String, Object>>>() {});

                slip.setEarnings(earnings);
                slip.setDeductions(deductions);

                // ✅ Si filtre activé, ne garder que les Salary Slips qui contiennent le component et montant dans la plage
                if (applyFilter) {
                    boolean hasComponentInRange = earnings.stream().anyMatch(comp ->
                        componentName.equals(comp.get("salary_component")) &&
                        comp.get("amount") instanceof Number &&
                        isAmountInRange(((Number) comp.get("amount")).doubleValue(), montantMin, montantMax)
                    ) || deductions.stream().anyMatch(comp ->
                        componentName.equals(comp.get("salary_component")) &&
                        comp.get("amount") instanceof Number &&
                        isAmountInRange(((Number) comp.get("amount")).doubleValue(), montantMin, montantMax)
                    );

                    if (hasComponentInRange) {
                        resultSlips.add(slip);
                    }
                } else {
                    resultSlips.add(slip);
                }
            }

            logger.info("Nombre de Salary Slips après filtre : {}", resultSlips.size());
            return new ApiResponse<>(resultSlips);

        } catch (Exception e) {
            logger.error("Erreur dans fetchAndFilterSalarySlips(): {}", e.getMessage(), e);
            return new ApiResponse<>("Erreur lors de la récupération des fiches de paie : " + e.getMessage());
        }
    }

    private boolean isAmountInRange(double amount, double min, double max) {
        return amount >= min && amount <= max;
    }
    public ApiResponse<List<Map<String, Object>>> fetchSalaryComponents(HttpSession session) {
        logger.info("Appel fetchSalaryComponents()");
        try {
            // URL vers Salary Component
            String url = frappeResourceUrl + "Salary Component?fields=[\"name\",\"salary_component\",\"type\"]";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String frappeSid = (String) session.getAttribute("frappe_sid");
            if (frappeSid == null) {
                logger.warn("Session utilisateur non authentifiée.");
                return new ApiResponse<>("Session utilisateur non authentifiée.");
            }
            headers.add(HttpHeaders.COOKIE, "sid=" + frappeSid);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode dataNode = root.get("data");

            List<Map<String, Object>> components = objectMapper.convertValue(
                dataNode, new TypeReference<List<Map<String, Object>>>() {});

            logger.info("Nombre de Salary Components récupérés : {}", components.size());
            return new ApiResponse<>(components);

        } catch (Exception e) {
            logger.error("Erreur dans fetchSalaryComponents(): {}", e.getMessage(), e);
            return new ApiResponse<>("Erreur lors de la récupération des composants salariaux : " + e.getMessage());
        }
    }
}
