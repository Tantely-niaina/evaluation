package mg.erpnext.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import mg.erpnext.model.ApiResponse;
import mg.erpnext.model.Employee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import mg.erpnext.model.SalarySlip;
import mg.erpnext.model.MonthReduction;
import mg.erpnext.model.SalarySlipAssignment;

import java.util.List;
import java.util.Set;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

@Service
public class SalarySlipService {

    @Value("${erpnext.api.resource-url}")
    private String frappeResourceUrl;

    @Autowired
    private SalarySlipAssignmentService salarySlipAssignmentService;

    @Autowired
    private MonthReductionService monthReductionService;

    private static final String SALARY_SLIP_LIST = "Salary Slip?fields=";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(SalarySlipService.class);


    public ApiResponse<List<SalarySlip>> fetchSalarySlips(HttpSession session) {
        logger.info("Appel fetchSalarySlips()");
        try {
            String fields = "[\"name\", \"employee\", \"employee_name\", \"start_date\", \"end_date\", \"net_pay\", \"gross_pay\", \"total_deduction\"]";
            String url = frappeResourceUrl + SALARY_SLIP_LIST + fields;

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
            SalarySlip[] salarySlips = objectMapper.treeToValue(dataNode, SalarySlip[].class);
            logger.info("fetchSalarySlips() réussi, nombre de fiches: {}", salarySlips.length);
            return new ApiResponse<>(Arrays.asList(salarySlips));
        } catch (Exception e) {
            logger.error("Erreur dans fetchSalarySlips(): {}", e.getMessage(), e);
            return new ApiResponse<>("Erreur lors de la récupération des fiches de paie: " + e.getMessage());
        }
    }

    // private void enrichWithEarningsAndDeductions(SalarySlip slip, String frappeSid) {
    //     try {
    //         RestTemplate restTemplate = new RestTemplate();
    //         HttpHeaders headers = new HttpHeaders();
    //         headers.setContentType(MediaType.APPLICATION_JSON);
    //         headers.add(HttpHeaders.COOKIE, "sid=" + frappeSid);

    //         // Earnings
    //         String earningsUrl = frappeResourceUrl + "Salary Detail?fields=[\"salary_component\",\"amount\"]&filters=[[\"parent\",\"=\",\"" + slip.getName() + "\"],[\"parentfield\",\"=\",\"earnings\"]]";
    //         HttpEntity<String> entity = new HttpEntity<>(headers);
    //         ResponseEntity<String> earningsResp = restTemplate.exchange(earningsUrl, HttpMethod.GET, entity, String.class);
    //         JsonNode earningsNode = objectMapper.readTree(earningsResp.getBody()).get("data");
    //         slip.setEarnings(earningsNode);

    //         // Deductions
    //         String deductionsUrl = frappeResourceUrl + "Salary Detail?fields=[\"salary_component\",\"amount\"]&filters=[[\"parent\",\"=\",\"" + slip.getName() + "\"],[\"parentfield\",\"=\",\"deductions\"]]";
    //         ResponseEntity<String> deductionsResp = restTemplate.exchange(deductionsUrl, HttpMethod.GET, entity, String.class);
    //         JsonNode deductionsNode = objectMapper.readTree(deductionsResp.getBody()).get("data");
    //         slip.setDeductions(deductionsNode);

    //     } catch (Exception e) {
    //             throw new RuntimeException("Erreur lors de l'enrichissement des données de la fiche de paie: " + e.getMessage());
    //     }
    // }
    
    public ApiResponse<List<SalarySlip>> fetchSalaryElements(HttpSession session) {
        logger.info("Appel fetchSalaryElements()");
        try {
            String fields = "[\"name\",\"employee\",\"employee_name\",\"start_date\",\"end_date\",\"net_pay\",\"gross_pay\",\"total_deduction\"]";
            String url = frappeResourceUrl + SALARY_SLIP_LIST + fields;

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
            logger.info("fetchSalaryElements() réussi, nombre de fiches: {}", salarySlips.length);
            return new ApiResponse<>(Arrays.asList(salarySlips));
        } catch (Exception e) {
            logger.error("Erreur dans fetchSalaryElements(): {}", e.getMessage(), e);
            return new ApiResponse<>("Erreur lors de la récupération des éléments de salaire: " + e.getMessage());
        }
    }

    public ApiResponse<List<SalarySlip>> searchSalaryElementsByMonth(int month, HttpSession session) {
        logger.info("Appel searchSalaryElementsByMonth() pour mois: {}", month);
        try {
            // Calcul des dates de début et de fin pour le mois donné de 2025
            String startDate = String.format("2025-%02d-01", month);
            // Calcul du dernier jour du mois
            int endDay;
            switch (month) {
                case 2: endDay = 28; break; // 2025 n'est pas bissextile
                case 4: case 6: case 9: case 11: endDay = 30; break;
                default: endDay = 31;
            }
            String endDate = String.format("2025-%02d-%02d", month, endDay);

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
            logger.info("searchSalaryElementsByMonth() réussi, nombre de fiches: {}", salarySlips.length);
            return new ApiResponse<>(Arrays.asList(salarySlips));
        } catch (Exception e) {
            logger.error("Erreur dans searchSalaryElementsByMonth(): {}", e.getMessage(), e);
            return new ApiResponse<>("Erreur lors de la récupération des éléments de salaire: " + e.getMessage());
        }
    }

    public ApiResponse<List<SalarySlip>> searchSalarySlipByEmployee(String employee, HttpSession session) {
        logger.info("Appel searchSalarySlipByEmployee() pour employé: {}", employee);
        try {
            // Ajoute start_date et end_date ici
            String fields = "[\"name\",\"employee\",\"employee_name\",\"start_date\",\"end_date\",\"posting_date\",\"salary_structure\",\"status\",\"net_pay\"]";
            String filters = "[[\"employee\",\"=\",\"" + employee + "\"]]";
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
            logger.info("searchSalarySlipByEmployee() réussi, nombre de fiches: {}", salarySlips.length);
            return new ApiResponse<>(Arrays.asList(salarySlips));
        } catch (Exception e) {
            logger.error("Erreur dans searchSalarySlipByEmployee(): {}", e.getMessage(), e);
            return new ApiResponse<>("Erreur lors de la recherche de la fiche de paie: " + e.getMessage());
        }
    }

    public ApiResponse<SalarySlip> getSalarySlipDetails(String salarySlipName, HttpSession session) {
        logger.info("Appel getSalarySlipDetails() pour fiche: {}", salarySlipName);
        try {
            String fields = "["
                + "\"name\",\"employee\",\"employee_name\",\"company\",\"department\",\"designation\",\"branch\","
                + "\"start_date\",\"end_date\",\"posting_date\",\"status\",\"salary_structure\",\"payroll_frequency\",\"currency\","
                + "\"net_pay\",\"gross_pay\",\"total_deduction\",\"rounded_total\",\"total_working_days\",\"leave_without_pay\","
                + "\"payment_days\",\"absent_days\",\"base_gross_pay\",\"base_total_deduction\",\"base_net_pay\",\"base_rounded_total\","
                + "\"gross_year_to_date\",\"base_gross_year_to_date\",\"year_to_date\",\"base_year_to_date\",\"month_to_date\",\"base_month_to_date\","
                + "\"bank_name\",\"bank_account_no\",\"mode_of_payment\",\"ctc\",\"total_earnings\",\"non_taxable_earnings\","
                + "\"deductions_before_tax_calculation\",\"tax_exemption_declaration\",\"standard_tax_exemption_amount\",\"annual_taxable_amount\","
                + "\"income_tax_deducted_till_date\",\"current_month_income_tax\",\"future_income_tax_deductions\",\"total_income_tax\","
                + "\"letter_head\",\"journal_entry\",\"payroll_entry\""
                + "]";
            String filters = "[[\"name\",\"=\",\"" + salarySlipName + "\"]]";
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
            if (dataNode.isArray() && dataNode.size() > 0) {
                logger.info("getSalarySlipDetails() réussi pour {}", salarySlipName);
                SalarySlip salarySlip = objectMapper.treeToValue(dataNode.get(0), SalarySlip.class);
                return new ApiResponse<>(salarySlip);
            } else {
                logger.warn("Aucune fiche de paie trouvée pour {}", salarySlipName);
                return new ApiResponse<>("Aucune fiche de paie trouvée pour ce nom.");
            }
        } catch (Exception e) {
            logger.error("Erreur dans getSalarySlipDetails(): {}", e.getMessage(), e);
            return new ApiResponse<>("Erreur lors de la récupération du détail de la fiche de paie: " + e.getMessage());
        }
    }

    public ApiResponse<List<SalarySlip>> fetchSalarySlipsBetweenDates(String startDate, String endDate, HttpSession session) {
        logger.info("Appel fetchSalarySlipsBetweenDates() de {} à {}", startDate, endDate);
        try {
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
            logger.info("fetchSalarySlipsBetweenDates() réussi, nombre de fiches: {}", salarySlips.length);
            return new ApiResponse<>(Arrays.asList(salarySlips));
        } catch (Exception e) {
            logger.error("Erreur dans fetchSalarySlipsBetweenDates(): {}", e.getMessage(), e);
            return new ApiResponse<>("Erreur lors de la récupération des fiches de paie entre deux dates: " + e.getMessage());
        }
    }
    
    public List<String> getOccupiedMonths(String startDate, String endDate, HttpSession session) {
        logger.info("Appel getOccupiedMonths() de {} à {}", startDate, endDate);
        ApiResponse<List<SalarySlip>> response = fetchSalarySlipsBetweenDates(startDate, endDate, session);
        Set<String> months = new HashSet<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter ymf = DateTimeFormatter.ofPattern("yyyy-MM");

        if (response.getData() != null) {
            for (SalarySlip slip : response.getData()) {
                if (slip.getStart_date() != null && !slip.getStart_date().isEmpty()) {
                    LocalDate sd = LocalDate.parse(slip.getStart_date(), dtf);
                    months.add(sd.format(ymf));
                }
                if (slip.getEnd_date() != null && !slip.getEnd_date().isEmpty()) {
                    LocalDate ed = LocalDate.parse(slip.getEnd_date(), dtf);
                    months.add(ed.format(ymf));
                }
            }
        }
        // Retourne la liste triée
        List<String> result = new ArrayList<>(months);
        result.sort(String::compareTo);
        logger.info("getOccupiedMonths() retourne {} mois occupés", result.size());
        return result;
    }

    /**
     * Appelle l'API Frappe pour créer une fiche de paie.
     */
    private SalarySlip createSalarySlipInFrappe(SalarySlip slip, HttpSession session) throws Exception {
        logger.info("Appel createSalarySlipInFrappe() pour employé: {}", slip.getEmployee());
        String url = frappeResourceUrl + "Salary Slip";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String frappeSid = (String) session.getAttribute("frappe_sid");
        if (frappeSid == null) {
            throw new Exception("Session utilisateur non authentifiée.");
        }
        headers.add(HttpHeaders.COOKIE, "sid=" + frappeSid);

        // Mapper le SalarySlip vers un objet JSON minimal pour la création
        ObjectMapper mapper = new ObjectMapper();
        // On ne transmet que les champs nécessaires à la création
        String json = mapper.writeValueAsString(new java.util.HashMap<String, Object>() {{
            put("employee", slip.getEmployee());
            put("employee_name", slip.getEmployee_name());
            put("company", slip.getCompany());
            put("currency", slip.getCurrency());
            put("payroll_frequency", slip.getPayroll_frequency());
            put("start_date", slip.getStart_date());
            put("end_date", slip.getEnd_date());
            put("posting_date", slip.getPosting_date());
            put("salary_structure", slip.getSalary_structure());
            put("docstatus", 1);
        }});

        HttpEntity<String> entity = new HttpEntity<>(json, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.POST, entity, String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("Fiche de paie créée avec succès pour {}", slip.getEmployee());
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode dataNode = root.get("data");
            return mapper.treeToValue(dataNode, SalarySlip.class);
        } else {
            logger.error("Erreur lors de la création de la fiche de paie: {}", response.getBody());
            throw new Exception("Erreur lors de la création de la fiche de paie : " + response.getBody());
        }
    }

    public ApiResponse<List<SalarySlip>> generateSalarySlipsForUnoccupiedMonths(
        Employee employee, String startDate, String endDate, HttpSession session) {
        logger.info("Appel generateSalarySlipsForUnoccupiedMonths() pour employé: {}, période: {} à {}", employee.getName(), startDate, endDate);
        ApiResponse<SalarySlipAssignment> cancelled_ssa = new ApiResponse<>();
        ApiResponse<SalarySlipAssignment> updated_ssa = new ApiResponse<>();
        ApiResponse<SalarySlipAssignment> created_ssa = new ApiResponse<>();
        ApiResponse<SalarySlipAssignment> restored_ssa = new ApiResponse<>();
        try {

            List<MonthReduction> monthReductions = monthReductionService.getAllMonthReductions();

            // 1. Mois déjà occupés
            List<String> occupiedMonths = getOccupiedMonths(startDate, endDate, session);

            // 2. Récupérer Salary Structure Assignment de l'employé
            ApiResponse<List<SalarySlipAssignment>> ssaResp = salarySlipAssignmentService.fetchSalaryStructureAssignment(session);
            if (ssaResp.getData() == null || ssaResp.getData().isEmpty()) {
                return new ApiResponse<>("Aucun Salary Structure Assignment trouvé.");
            }
            SalarySlipAssignment ssa = ssaResp.getData().stream()
                .filter(a -> a.getEmployee().equals(employee.getName()))
                .findFirst()
                .orElse(null);
            if (ssa == null) {
                return new ApiResponse<>("Aucun Salary Structure Assignment trouvé pour cet employé.");
            }

            // 3. Boucle sur chaque mois entre startDate et endDate
            List<SalarySlip> createdSlips = new ArrayList<>();
            YearMonth start = YearMonth.parse(startDate.substring(0, 7));
            YearMonth end = YearMonth.parse(endDate.substring(0, 7));
            for (YearMonth ym = start; !ym.isAfter(end); ym = ym.plusMonths(1)) {
            // Réinitialisation à chaque itération
            cancelled_ssa = new ApiResponse<>();
            updated_ssa = new ApiResponse<>();
            created_ssa = new ApiResponse<>();
            restored_ssa = new ApiResponse<>();


            String ymStr = ym.toString(); // format yyyy-MM
            if (occupiedMonths.contains(ymStr)) continue;

            // Début et fin du mois
            String slipStart = ym.atDay(1).toString();
            String slipEnd = ym.atEndOfMonth().toString();

            boolean reductionApplied = false;

            if(monthReductions != null && !monthReductions.isEmpty()) {
                // Vérifier si le mois est réduit
                for (MonthReduction mr : monthReductions) {
                    if (mr.getMois_annee().equals(ymStr)) {
                        // Correction ici : conversion yyyy-MM -> MM-yy
                        String mois_annee_MM_yy = ym.format(DateTimeFormatter.ofPattern("MM-yy"));
                        Double reduction = mr.getSignedReduction();
                        cancelled_ssa = salarySlipAssignmentService.cancelSalaryStructureAssignment(session, mois_annee_MM_yy, employee.getName());
                        updated_ssa = salarySlipAssignmentService.updateBaseByPercent(session, cancelled_ssa, reduction);
                        created_ssa = salarySlipAssignmentService.createOneSalarySlipAssignment(updated_ssa, session);
                        reductionApplied = true;
                        break;
                    }
                }
            }

                SalarySlip slip = new SalarySlip();
                slip.setEmployee(employee.getName());
                slip.setEmployee_name(employee.getEmployee_name());
                slip.setCompany(employee.getCompany());
                slip.setCurrency("EUR"); // ou autre valeur par défaut
                slip.setPayroll_frequency("Monthly");
                slip.setStart_date(slipStart);
                slip.setEnd_date(slipEnd);
                slip.setPosting_date(slipStart);
                slip.setSalary_structure(ssa.getSalary_structure());

                // Appel API pour créer la fiche dans Frappe
                SalarySlip created = createSalarySlipInFrappe(slip, session);
                createdSlips.add(created);
                if(reductionApplied && created_ssa.getData() != null && cancelled_ssa.getData() != null) {
                    restored_ssa = salarySlipAssignmentService.restoreSalaryStructureAssignment(session, created_ssa, cancelled_ssa, employee.getEmployee_name());
                }
            }
            logger.info("Nombre de fiches de paie créées: {}", createdSlips.size());
            return new ApiResponse<>(createdSlips);
        } catch (Exception e) {
            logger.error("Erreur dans generateSalarySlipsForUnoccupiedMonths(): {}", e.getMessage(), e);
            return new ApiResponse<>("Erreur lors de la génération des fiches de paie: " + e.getMessage());
        }
    }

    public List<SalarySlip> filterSalarySlipsByAssignments(
        List<SalarySlipAssignment> ssa1, HttpSession session) {
        logger.info("Appel filterSalarySlipsByAssignments() pour {} assignments", ssa1.size());
        List<SalarySlip> result = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (SalarySlipAssignment ssaRef : ssa1) {
            // 1. Récupérer tous les SSA de l'employé
            ApiResponse<List<SalarySlipAssignment>> allSsaResp =
                salarySlipAssignmentService.searchSalaryStructureAssignmentByEmployee(session, ssaRef.getEmployee());
            List<SalarySlipAssignment> allSsa = allSsaResp.getData() != null ? allSsaResp.getData() : new ArrayList<>();

            // 2. Récupérer tous les SalarySlip de l'employé
            ApiResponse<List<SalarySlip>> slipsResp = searchSalarySlipByEmployee(ssaRef.getEmployee(), session);
            List<SalarySlip> allSlips = slipsResp.getData() != null ? slipsResp.getData() : new ArrayList<>();

            // 3. from_date du SSA de référence
            LocalDate refFromDate;
            try {
                refFromDate = LocalDate.parse(ssaRef.getFrom_date(), dtf);
            } catch (Exception e) {
                continue; // skip if date is invalid
            }

            // 4. Chercher le from_date du SSA suivant (ssa2) > refFromDate
            LocalDate nextSsaFromDate = null;
            for (SalarySlipAssignment ssa : allSsa) {
                if (ssa.getFrom_date() == null) continue;
                try {
                    LocalDate fd = LocalDate.parse(ssa.getFrom_date(), dtf);
                    if (fd.isAfter(refFromDate)) {
                        if (nextSsaFromDate == null || fd.isBefore(nextSsaFromDate)) {
                            nextSsaFromDate = fd;
                        }
                    }
                } catch (Exception ignore) {}
            }

            // 5. Filtrer les SalarySlip
            for (SalarySlip slip : allSlips) {
                if (slip.getStart_date() == null) continue;
                try {
                    LocalDate slipStart = LocalDate.parse(slip.getStart_date(), dtf);
                    logger.info("SSA refFromDate: {}, nextSsaFromDate: {}, slipStart: {}", refFromDate, nextSsaFromDate, slipStart);
                    boolean afterOrEqualRef = !slipStart.isBefore(refFromDate);
                    boolean beforeNext = (nextSsaFromDate == null) || slipStart.isBefore(nextSsaFromDate);
                    logger.info("afterOrEqualRef: {}, beforeNext: {}", afterOrEqualRef, beforeNext);
                    if (afterOrEqualRef && beforeNext) {
                        result.add(slip);
                    }
                } catch (Exception e) {
                    logger.warn("Erreur parsing date pour slip {}: {}", slip.getName(), e.getMessage());
                }
            }
        }
        logger.info("filterSalarySlipsByAssignments() retourne {} fiches", result.size());
        return result;
    }

    /**
     * Annule chaque fiche de paie de la liste (docstatus=2) via l'API Frappe.
     */
    public ApiResponse<List<SalarySlip>> cancelSalarySlips(List<SalarySlip> slips, HttpSession session) {
        logger.info("Appel cancelSalarySlips() pour {} fiches", slips.size());
        List<SalarySlip> cancelled = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            String frappeSid = (String) session.getAttribute("frappe_sid");
            if (frappeSid == null) {
                return new ApiResponse<>("Session utilisateur non authentifiée.");
            }

            for (SalarySlip slip : slips) {
                String url = frappeResourceUrl + "Salary Slip/" + slip.getName();
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add(HttpHeaders.COOKIE, "sid=" + frappeSid);

                // On ne transmet que docstatus=2 pour annuler
                String json = mapper.writeValueAsString(new java.util.HashMap<String, Object>() {{
                    put("docstatus", 2);
                }});

                HttpEntity<String> entity = new HttpEntity<>(json, headers);
                ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.PUT, entity, String.class
                );

                if (response.getStatusCode().is2xxSuccessful()) {
                    JsonNode root = mapper.readTree(response.getBody());
                    JsonNode dataNode = root.get("data");
                    SalarySlip cancelledSlip = mapper.treeToValue(dataNode, SalarySlip.class);
                    cancelled.add(cancelledSlip);
                } else {
                    // Si une annulation échoue, on continue avec les autres
                    continue;
                }
            }
            logger.info("Nombre de fiches annulées: {}", cancelled.size());
            return new ApiResponse<>(cancelled);
        } catch (Exception e) {
            logger.error("Erreur dans cancelSalarySlips(): {}", e.getMessage(), e);
            return new ApiResponse<>("Erreur lors de l'annulation des fiches de paie: " + e.getMessage());
        }
    }

    /**
     * Met à jour les SalarySlip pour une plage de base donnée en suivant la séquence demandée.
     */
   public ApiResponse<String> updateSalarySlipsWithNewBase(
            double minAmount, double maxAmount, double percent, HttpSession session) {
        logger.info("Appel updateSalarySlipsWithNewBase() min: {}, max: {}, percent: {}", minAmount, maxAmount, percent);
        try {
            // 1. Récupérer les SalarySlipAssignment originaux (ssa1)
            ApiResponse<List<SalarySlipAssignment>> ssa1Resp =
                salarySlipAssignmentService.fetchSalaryStructureAssignmentByBase(session, minAmount, maxAmount);
            List<SalarySlipAssignment> ssa1 = ssa1Resp.getData();
            if (ssa1 == null || ssa1.isEmpty()) {
                logger.warn("Aucun SSA trouvé dans la plage {} - {}", minAmount, maxAmount);
                return new ApiResponse<>("Aucun Salary Structure Assignment trouvé dans la plage.");
            }

            // 2. Récupérer les SalarySlip associés (ss1)
            List<SalarySlip> ss1 = filterSalarySlipsByAssignments(ssa1, session);

            // 3. Annuler les SalarySlip (ss1)
            ApiResponse<List<SalarySlip>> cancelledSlipsResp = cancelSalarySlips(ss1, session);
            if (cancelledSlipsResp.getError() != null) {
                logger.error("Erreur lors de l'annulation des SalarySlip: {}", cancelledSlipsResp.getError());
                return new ApiResponse<>("Erreur lors de l'annulation des SalarySlip: " + cancelledSlipsResp.getError());
            }

            // 4. Mettre à jour les SalarySlipAssignment (ssa1) avec le nouveau pourcentage (ssa2)
            ApiResponse<List<SalarySlipAssignment>> ssa2Resp =
                salarySlipAssignmentService.updateBaseByPercent(session, minAmount, maxAmount, percent);
            List<SalarySlipAssignment> ssa2 = ssa2Resp.getData();
            if (ssa2 == null || ssa2.isEmpty()) {
                logger.error("Erreur lors de la génération des nouveaux SSA");
                return new ApiResponse<>("Erreur lors de la génération des nouveaux Salary Structure Assignment.");
            }

            // 5. Annuler les SalarySlipAssignment originaux (ssa1)
            ApiResponse<List<SalarySlipAssignment>> cancelledSsa1Resp =
                salarySlipAssignmentService.cancelSalaryStructureAssignmentsByBase(session, minAmount, maxAmount);
            if (cancelledSsa1Resp.getError() != null) {
                logger.error("Erreur lors de l'annulation des SSA originaux: {}", cancelledSsa1Resp.getError());
                return new ApiResponse<>("Erreur lors de l'annulation des SSA originaux: " + cancelledSsa1Resp.getError());
            }

            // 6. Créer les nouveaux SalarySlipAssignment (ssa2)
            ApiResponse<List<SalarySlipAssignment>> createdSsa2Resp =
                salarySlipAssignmentService.createSalarySlipAssignments(ssa2, session);
            if (createdSsa2Resp.getError() != null) {
                logger.error("Erreur lors de la création des nouveaux SSA: {}", createdSsa2Resp.getError());
                return new ApiResponse<>("Erreur lors de la création des nouveaux SSA: " + createdSsa2Resp.getError());
            }

            // 7. Recréer les SalarySlip annulés (ss2)
            List<SalarySlip> ss2 = new ArrayList<>();
            for (SalarySlip slip : ss1) {
                try {
                    SalarySlip recreated = createSalarySlipInFrappe(slip, session);
                    ss2.add(recreated);
                } catch (Exception e) {
                    logger.warn("Erreur lors de la recréation d'une fiche: {}", e.getMessage());
                }
            }

            // 8. Annuler les nouveaux SalarySlipAssignment (ssa2)
            for (SalarySlipAssignment ssa : ssa2) {
                try {
                    salarySlipAssignmentService.cancelSalaryStructureAssignmentsByBase(
                        session, ssa.getBase(), ssa.getBase());
                } catch (Exception ignore) {
                    logger.warn("Erreur lors de l'annulation d'un SSA2: {}", ignore.getMessage());
                }
            }

            // 9. Recréer les SalarySlipAssignment originaux (ssa1)
            ApiResponse<List<SalarySlipAssignment>> recreatedSsa1Resp =
                salarySlipAssignmentService.createSalarySlipAssignments(ssa1, session);
            if (recreatedSsa1Resp.getError() != null) {
                logger.error("Erreur lors de la recréation des SSA originaux: {}", recreatedSsa1Resp.getError());
                return new ApiResponse<>("Erreur lors de la recréation des SSA originaux: " + recreatedSsa1Resp.getError());
            }

            logger.info("Mise à jour des SalarySlip terminée avec succès.");
            return new ApiResponse<>("Mise à jour des SalarySlip terminée avec succès.");
        } catch (Exception e) {
            logger.error("Erreur globale dans updateSalarySlipsWithNewBase(): {}", e.getMessage(), e);
            return new ApiResponse<>("Erreur globale lors de la mise à jour des SalarySlip: " + e.getMessage());
        } 
}
    public List<Double> getAllBaseValues(HttpSession session) {
        logger.info("Appel getAllBaseValues() pour toutes les fiches de paie");
        List<Double> bases = new ArrayList<>();
        try {
            ApiResponse<List<SalarySlipAssignment>> response = salarySlipAssignmentService.fetchSalaryStructureAssignment(session);
            if (response.getData() != null) {
                for (SalarySlipAssignment slip : response.getData()) {
                    // On suppose que SalarySlip a un getter getBase() qui retourne le salaire de base
                    Double base = null;
                    try {
                        base = slip.getBase();
                    } catch (Exception ignore) {}
                    if (base != null) {
                        bases.add(base);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Erreur dans getAllBaseValues(): {}", e.getMessage(), e);
        }
        logger.info("getAllBaseValues() retourne {} valeurs de base", bases.size());
        return bases;
    }

public Double calculeMoyenne(List<Double> base) {
    if (base == null || base.isEmpty()) {
        return 0.0; // ou null si tu veux signaler qu’il n’y a rien
    }

    Double total = 0.0;
    for (Double valeur : base) {
        total += valeur;
    }
    return total / base.size();
}
}
