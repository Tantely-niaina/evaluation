package mg.erpnext.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import mg.erpnext.model.ApiResponse;
import mg.erpnext.model.SalarySlip;
import mg.erpnext.service.SalarySlipWithComponentsService;

@Controller
public class SalarySlipWithComponentsController {
  @Service
public class SalarySlipWithComponentsService {

    @Value("${erpnext.api.resource-url}")
    private String frappeResourceUrl;

    private static final Logger logger = LoggerFactory.getLogger(SalarySlipWithComponentsService.class);
    private static final String SALARY_SLIP_LIST = "Salary Slip?fields=";
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Récupère la liste des Salary Slip avec leurs gains (earnings) et déductions (deductions).
     */
    public ApiResponse<List<SalarySlip>> fetchSalarySlipsWithComponents(HttpSession session) {
        logger.info("Appel fetchSalarySlipsWithComponents()");
        try {
            // On enlève earnings et deductions ici
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

            // Récupère la liste de base des Salary Slips
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode dataNode = root.get("data");
            SalarySlip[] salarySlips = objectMapper.treeToValue(dataNode, SalarySlip[].class);

            logger.info("Nombre de Salary Slips trouvés : {}", salarySlips.length);

            // Pour chaque Salary Slip, récupérer earnings et deductions
            for (SalarySlip slip : salarySlips) {
                String slipUrl = frappeResourceUrl + "Salary Slip/" + slip.getName();
                ResponseEntity<String> slipResponse = restTemplate.exchange(
                    slipUrl, HttpMethod.GET, entity, String.class
                );

                JsonNode slipRoot = objectMapper.readTree(slipResponse.getBody());
                JsonNode slipData = slipRoot.get("data");

                JsonNode earningsNode = slipData.get("earnings");
                JsonNode deductionsNode = slipData.get("deductions");

                // Mapper earnings et deductions dans SalarySlip
                List<Map<String, Object>> earnings = objectMapper.convertValue(
                    earningsNode, new TypeReference<List<Map<String, Object>>>() {});
                List<Map<String, Object>> deductions = objectMapper.convertValue(
                    deductionsNode, new TypeReference<List<Map<String, Object>>>() {});

                slip.setEarnings(earnings);
                slip.setDeductions(deductions);
            }

            return new ApiResponse<>(Arrays.asList(salarySlips));
        } catch (Exception e) {
            logger.error("Erreur dans fetchSalarySlipsWithComponents(): {}", e.getMessage(), e);
            return new ApiResponse<>("Erreur lors de la récupération des fiches de paie avec composants: " + e.getMessage());
        }
    }
}

}
