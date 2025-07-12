package mg.erpnext.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import mg.erpnext.model.SalarySlip;

@Service
public class StatistiqueService {

    @Value("${erpnext.api.resource-url}")
    private String frappeResourceUrl;
    
    @Autowired
    private SalarySlipService salarySlipService;

    private static final String SALARY_SLIP_LIST = "Salary Slip?fields=";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(StatistiqueService.class);

    public ApiResponse<Map<String, Double>> calculerTotalNetParMois(HttpSession session) {
        try {
            ApiResponse<List<SalarySlip>> response = salarySlipService.fetchSalarySlips(session);
            if (!response.isSuccess() || response.getData() == null) {
                return new ApiResponse<>("Erreur lors de la récupération des fiches de paie: " + response.getError());
            }
            List<SalarySlip> salarySlips = response.getData();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            Map<String, Double> totalParMois = salarySlips.stream()
                .collect(Collectors.groupingBy(
                    slip -> {
                        LocalDate date = LocalDate.parse(slip.getStart_date(), formatter);
                        return date.getYear() + "-" + String.format("%02d", date.getMonthValue());
                    },
                    Collectors.summingDouble(SalarySlip::getNet_pay)
                ));
            return new ApiResponse<>(totalParMois);
        } catch (Exception e) {
            return new ApiResponse<>("Erreur lors du calcul du total des salaires nets par mois: " + e.getMessage());
        }
    }

    public ApiResponse<List<SalarySlip>> searchSalaryElementsByMonth(int year, int month, HttpSession session) {
        try {
            // Calcul des dates de début et de fin pour le mois et l'année donnés
            String startDate = String.format("%d-%02d-01", year, month);
            // Calcul du dernier jour du mois
            int endDay;
            switch (month) {
                case 2: 
                    // Vérifie si l'année est bissextile
                    endDay = (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) ? 29 : 28; 
                    break;
                case 4: case 6: case 9: case 11: endDay = 30; break;
                default: endDay = 31;
            }
            String endDate = String.format("%d-%02d-%02d", year, month, endDay);

            String fields = "[\"name\",\"employee\",\"employee_name\",\"start_date\",\"end_date\",\"net_pay\",\"gross_pay\",\"total_deduction\"]";
            String filters = "[[\"start_date\",\">=\",\"" + startDate + "\"],[\"end_date\",\"<=\",\"" + endDate + "\"]]";
            String url = frappeResourceUrl + SALARY_SLIP_LIST + fields + "&filters=" + filters;

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
            SalarySlip[] salarySlips = objectMapper.treeToValue(dataNode, SalarySlip[].class);
            return new ApiResponse<>(Arrays.asList(salarySlips));
        } catch (Exception e) {
            return new ApiResponse<>("Erreur lors de la récupération des éléments de salaire: " + e.getMessage());
        }
    }

    public ApiResponse<Map<String, Map<String, Double>>> getEvolutionSalaireParMois(HttpSession session) {
        try {
            ApiResponse<List<SalarySlip>> response = salarySlipService.fetchSalarySlips(session);
            if (!response.isSuccess() || response.getData() == null) {
                return new ApiResponse<>("Erreur lors de la récupération des fiches de paie: " + response.getError());
            }
            List<SalarySlip> salarySlips = response.getData();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            // Utilise LinkedHashMap pour garder l'ordre chronologique
            Map<String, Map<String, Double>> evolution = new LinkedHashMap<>();
            for (SalarySlip slip : salarySlips) {
                LocalDate date = LocalDate.parse(slip.getStart_date(), formatter);
                String key = date.getYear() + "-" + String.format("%02d", date.getMonthValue());
                evolution.putIfAbsent(key, new LinkedHashMap<>());
                Map<String, Double> mois = evolution.get(key);
                double netPay = slip.getNet_pay() != null ? slip.getNet_pay() : 0.0;
                double grossPay = slip.getGross_pay() != null ? slip.getGross_pay() : 0.0;
                double totalDeduction = slip.getTotal_deduction() != null ? slip.getTotal_deduction() : 0.0;
                mois.put("net_pay", mois.getOrDefault("net_pay", 0.0) + netPay);
                mois.put("gross_pay", mois.getOrDefault("gross_pay", 0.0) + grossPay);
                mois.put("total_deduction", mois.getOrDefault("total_deduction", 0.0) + totalDeduction);

                logger.info("Slip [{}]: net_pay={}, gross_pay={}, total_deduction={}", slip.getName(), netPay, grossPay, totalDeduction);
            }
            logger.info("Evolution salariale par mois : {}", evolution);
            return new ApiResponse<>(evolution);
        } catch (Exception e) {
            return new ApiResponse<>("Erreur lors du calcul de l'évolution des salaires: " + e.getMessage());
        }
    }
}
