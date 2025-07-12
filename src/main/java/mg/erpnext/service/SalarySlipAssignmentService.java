package mg.erpnext.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import mg.erpnext.model.ApiResponse;
import mg.erpnext.model.Employee;
import mg.erpnext.model.SalarySlipAssignment;

@Service
public class SalarySlipAssignmentService {
    @Value("${erpnext.api.resource-url}")
    private String frappeResourceUrl;

    private static final String salary_structure_assignment = "Salary Structure Assignment?fields=";

    private static final Logger logger = LoggerFactory.getLogger(SalarySlipAssignmentService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ApiResponse<List<SalarySlipAssignment>> fetchSalaryStructureAssignment(HttpSession session) {
        try {
            logger.info("Début de la récupération des Salary Structure Assignment.");
            String fields = "[\"name\",\"employee_name\",\"employee\",\"salary_structure\",\"from_date\",\"base\",\"company\"]";
            String url = frappeResourceUrl + salary_structure_assignment + fields;

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String frappeSid = (String) session.getAttribute("frappe_sid");
            if (frappeSid == null) {
                logger.warn("Session utilisateur non authentifiée lors de la récupération des Salary Structure Assignment.");
                return new ApiResponse<>("Session utilisateur non authentifiée.");
            }
            headers.add(HttpHeaders.COOKIE, "sid=" + frappeSid);

            logger.debug("Appel de l'API Frappe à l'URL: {}", url);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class
            );

            logger.debug("Réponse reçue de l'API Frappe: status={}, body={}", response.getStatusCode(), response.getBody());

            // Supposons que la réponse JSON a une structure { "data": [ ... ] }
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode dataNode = root.get("data");
            SalarySlipAssignment[] ssa = objectMapper.treeToValue(dataNode, SalarySlipAssignment[].class);
            logger.info("Nombre de Salary Structure Assignment récupérés: {}", ssa.length);
            return new ApiResponse<>(Arrays.asList(ssa));
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des Salary Structure Assignment: {}", e.getMessage(), e);
            return new ApiResponse<>("Erreur lors de la récupération des fiches de paie: " + e.getMessage());
        }
    }

    public ApiResponse<List<SalarySlipAssignment>> searchSalaryStructureAssignmentByEmployee(HttpSession session, String employeeName) {
        logger.info("Début de searchSalaryStructureAssignmentByEmployee pour employeeName='{}'", employeeName);
        try {
            String fields = "[\"name\",\"employee_name\",\"employee\",\"salary_structure\",\"from_date\",\"base\",\"company\"]";
            String filters = "[[\"employee\",\"=\",\"" + employeeName + "\"]]";
            String url = frappeResourceUrl + salary_structure_assignment + fields + "&filters=" + filters;

            logger.debug("URL d'appel Frappe: {}", url);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String frappeSid = (String) session.getAttribute("frappe_sid");
            if (frappeSid == null) {
                logger.warn("Session utilisateur non authentifiée lors de la recherche SSA pour '{}'", employeeName);
                return new ApiResponse<>("Session utilisateur non authentifiée.");
            }
            headers.add(HttpHeaders.COOKIE, "sid=" + frappeSid);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class
            );

            logger.debug("Réponse reçue de Frappe: status={}, body={}", response.getStatusCode(), response.getBody());

            // Supposons que la réponse JSON a une structure { "data": [ ... ] }
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode dataNode = root.get("data");
            SalarySlipAssignment[] ssa = objectMapper.treeToValue(dataNode, SalarySlipAssignment[].class);
            logger.info("Nombre de Salary Structure Assignment trouvés pour '{}': {}", employeeName, ssa.length);
            return new ApiResponse<>(Arrays.asList(ssa));
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche SSA pour '{}': {}", employeeName, e.getMessage(), e);
            return new ApiResponse<>("Erreur lors de la récupération des fiches de paie: " + e.getMessage());
        }
    }

    public ApiResponse<SalarySlipAssignment> fetchSalaryStructureAssignmentByDate(
        HttpSession session, String mois_annee, String employeeName) {
        logger.info("Début fetchSalaryStructureAssignmentByDate pour employeeName='{}', mois_annee='{}'", employeeName, mois_annee);
        try {
            // Convertir "MM-yy" en dernier jour du mois
            YearMonth ym = YearMonth.parse(mois_annee, DateTimeFormatter.ofPattern("MM-yy"));
            LocalDate lastDay = ym.atEndOfMonth();
            logger.debug("Dernier jour du mois pour '{}': {}", mois_annee, lastDay);

            // Récupérer tous les SalaryStructureAssignment de l'employé
            ApiResponse<List<SalarySlipAssignment>> resp = searchSalaryStructureAssignmentByEmployee(session, employeeName);
            if (resp.getData() == null || resp.getData().isEmpty()) {
                logger.warn("Aucun Salary Structure Assignment trouvé pour l'employé '{}'", employeeName);
                return new ApiResponse<>("Aucun Salary Structure Assignment trouvé pour cet employé.");
            }
            logger.debug("Nombre de Salary Structure Assignment récupérés pour '{}': {}", employeeName, resp.getData().size());

            // Filtrer ceux dont from_date <= lastDay, et prendre le plus récent (from_date max)
            SalarySlipAssignment result = resp.getData().stream()
                .filter(ssa -> {
                    try {
                        LocalDate from = LocalDate.parse(ssa.getFrom_date());
                        boolean valid = !from.isAfter(lastDay);
                        logger.trace("Vérification SSA '{}': from_date={}, valid={}", ssa.getName(), ssa.getFrom_date(), valid);
                        return valid;
                    } catch (Exception e) {
                        logger.error("Erreur de parsing de date pour SSA '{}': {}", ssa.getName(), ssa.getFrom_date());
                        return false;
                    }
                })
                .max((a, b) -> {
                    LocalDate da = LocalDate.parse(a.getFrom_date());
                    LocalDate db = LocalDate.parse(b.getFrom_date());
                    return da.compareTo(db);
                })
                .orElse(null);

            if (result == null) {
                logger.warn("Aucun Salary Structure Assignment valide trouvé pour la période '{}' (dernier jour: {})", mois_annee, lastDay);
                return new ApiResponse<>("Aucun Salary Structure Assignment valide trouvé pour la période.");
            }
            logger.info("Salary Structure Assignment sélectionné: name='{}', from_date='{}'", result.getName(), result.getFrom_date());
            return new ApiResponse<>(result);
        } catch (DateTimeParseException e) {
            logger.error("Format de date invalide (attendu MM-yy): '{}'", mois_annee);
            return new ApiResponse<>("Format de date invalide (attendu MM-yy): " + mois_annee);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération du Salary Structure Assignment pour '{}', '{}': {}", employeeName, mois_annee, e.getMessage(), e);
            return new ApiResponse<>("Erreur lors de la récupération: " + e.getMessage());
        }
    }

    public ApiResponse<List<SalarySlipAssignment>> fetchSalaryStructureAssignmentByBase(
            HttpSession session, double minBase, double maxBase) {
        ApiResponse<List<SalarySlipAssignment>> response = fetchSalaryStructureAssignment(session);
        if (response.getData() == null) {
            logger.warn("Aucun SalarySlipAssignment trouvé pour le filtre base.");
            return response;
        }
        List<SalarySlipAssignment> filtered = response.getData().stream()
            .filter(ssa -> ssa.getBase() != null && ssa.getBase() >= minBase && ssa.getBase() <= maxBase)
            .collect(Collectors.toList());
        logger.info("Nombre de SalarySlipAssignment filtrés par base ({} - {}): {}", minBase, maxBase, filtered.size());
        return new ApiResponse<>(filtered);
    }

    public ApiResponse<SalarySlipAssignment> updateBaseByPercent(HttpSession session, ApiResponse<SalarySlipAssignment> cancelledSsa,  double percent) {
        ApiResponse<SalarySlipAssignment> response = cancelledSsa;
        
        double factor = 1 + (percent / 100.0);
        SalarySlipAssignment ssa = response.getData();
        SalarySlipAssignment copy = new SalarySlipAssignment();
        copy.setName(null);
        copy.setEmployee(ssa.getEmployee());
        copy.setEmployee_name(ssa.getEmployee_name());
        copy.setSalary_structure(ssa.getSalary_structure());
        copy.setFrom_date(ssa.getFrom_date());
        copy.setCompany(ssa.getCompany());
        if (ssa.getBase() != null) {
            double newBase = ssa.getBase() * factor;
            // On ne garde que les bases strictement positives
            if (newBase > 0) {
                copy.setBase(newBase);
            } else {
                // Si la base devient négative ou nulle, on ignore cette entrée
                return null;
            }
        } else {
            copy.setBase(null);
        }
        return new ApiResponse<>(copy);
    }

    public ApiResponse<List<SalarySlipAssignment>> updateBaseByPercent(HttpSession session, double minBase, double maxBase, double percent) {
        ApiResponse<List<SalarySlipAssignment>> response = fetchSalaryStructureAssignmentByBase(session, minBase, maxBase);
        if (response.getData() == null) {
            logger.warn("Aucun SalarySlipAssignment à mettre à jour.");
            return response;
        }
        double factor = 1 + (percent / 100.0);
        List<SalarySlipAssignment> updated = response.getData().stream()
            .map(ssa -> {
                SalarySlipAssignment copy = new SalarySlipAssignment();
                copy.setName(ssa.getName() + "-copie");
                copy.setEmployee(ssa.getEmployee());
                copy.setEmployee_name(ssa.getEmployee_name());
                copy.setSalary_structure(ssa.getSalary_structure());
                copy.setFrom_date(ssa.getFrom_date());
                copy.setCompany(ssa.getCompany());
                if (ssa.getBase() != null) {
                    double newBase = ssa.getBase() * factor;
                    // On ne garde que les bases strictement positives
                    if (newBase > 0) {
                        copy.setBase(newBase);
                    } else {
                        // Si la base devient négative ou nulle, on ignore cette entrée
                        return null;
                    }
                } else {
                    copy.setBase(null);
                }
                return copy;
            })
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toList());
        logger.info("Nombre de SalarySlipAssignment mis à jour: {}", updated.size());
        return new ApiResponse<>(updated);
    }

    public ApiResponse<SalarySlipAssignment> cancelSalaryStructureAssignment(HttpSession session, String mois_annee, String employeeName) {
        ApiResponse<SalarySlipAssignment> response = fetchSalaryStructureAssignmentByDate(session, mois_annee, employeeName);
        if (response.getData() == null) {
            logger.warn("Aucun Salary Structure Assignment à annuler pour l'employé: {} et le from_date <= {}", employeeName, mois_annee);
            return new ApiResponse<>("Aucun Salary Structure Assignment à annuler.");
        }

        SalarySlipAssignment cancelledSsa = new SalarySlipAssignment();
        try {
            String frappeSid = (String) session.getAttribute("frappe_sid");
            if (frappeSid == null) {
                logger.error("Session utilisateur non authentifiée lors de l'annulation.");
                return new ApiResponse<>("Session utilisateur non authentifiée.");
            }

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add(HttpHeaders.COOKIE, "sid=" + frappeSid);

            SalarySlipAssignment ssa = response.getData();
            String url = frappeResourceUrl + "Salary Structure Assignment/" + ssa.getName();
            String json = "{\"docstatus\":2}";
            logger.info("Annulation du Salary Structure Assignment: {}", ssa.getName());
            HttpEntity<String> entity = new HttpEntity<>(json, headers);
            ResponseEntity<String> resp = restTemplate.exchange(
                    url, HttpMethod.PUT, entity, String.class
            );
            if (resp.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(resp.getBody());
                JsonNode dataNode = root.get("data");
                cancelledSsa = objectMapper.treeToValue(dataNode, SalarySlipAssignment.class);
                logger.info("Salary Structure Assignment annulé avec succès: {}", ssa.getName());
            } else {
                logger.warn("Échec de l'annulation pour: {} - Status: {}", ssa.getName(), resp.getStatusCode());
            }
            logger.info("Annulation du Salary Structure Assignment réussie pour l'employé: {}", employeeName);
            return new ApiResponse<>(cancelledSsa);
        } catch (Exception e) {
            logger.error("Erreur lors de l'annulation du Salary Structure Assignment: {}", e.getMessage(), e);
            return new ApiResponse<>("Erreur lors de l'annulation du Salary Structure Assignment: " + e.getMessage());
        }
    }

    /**
     * Crée un SalarySlipAssignment via l'API Frappe.
     */
    public ApiResponse<SalarySlipAssignment> createOneSalarySlipAssignment(ApiResponse<SalarySlipAssignment> updated_ssa, HttpSession session) {
        try {
            String url = frappeResourceUrl + "Salary Structure Assignment";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String frappeSid = (String) session.getAttribute("frappe_sid");
            if (frappeSid == null) {
                logger.error("Session utilisateur non authentifiée lors de la création.");
                return new ApiResponse<>("Session utilisateur non authentifiée.");
            }
            headers.add(HttpHeaders.COOKIE, "sid=" + frappeSid);

            // Création de l'objet SalarySlipAssignment à partir de l'employé et du salaire de base
            SalarySlipAssignment assignment = updated_ssa.getData();
            assignment.setDocstatus(1); // Valider lors de la création

            String json = objectMapper.writeValueAsString(assignment);
            HttpEntity<String> entity = new HttpEntity<>(json, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode dataNode = root.get("data");
                SalarySlipAssignment createdAssignment = objectMapper.treeToValue(dataNode, SalarySlipAssignment.class);
                return new ApiResponse<>(createdAssignment);
            } else {
                return new ApiResponse<>("Erreur lors de la création du Salary Structure Assignment: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la création du Salary Structure Assignment: {}", e.getMessage(), e);
            return new ApiResponse<>("Erreur lors de la création du Salary Structure Assignment: " + e.getMessage());
        }
    }

    /**
     * Restaure un SalaryStructureAssignment : annule le SSA créé (created_ssa) et recrée le SSA annulé (cancelled_ssa).
     */
    public ApiResponse<SalarySlipAssignment> restoreSalaryStructureAssignment(
            HttpSession session,
            ApiResponse<SalarySlipAssignment> created_ssa,
            ApiResponse<SalarySlipAssignment> cancelled_ssa,
            String employeeName) {
        try {
            // 1. Annuler le SSA créé (created_ssa)
            SalarySlipAssignment created = created_ssa != null ? created_ssa.getData() : null;
            if (created != null && created.getName() != null && created.getDocstatus() != null && created.getDocstatus() != 2) {
                String frappeSid = (String) session.getAttribute("frappe_sid");
                if (frappeSid == null) {
                    logger.error("Session utilisateur non authentifiée lors de la restauration.");
                    return new ApiResponse<>("Session utilisateur non authentifiée.");
                }
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add(HttpHeaders.COOKIE, "sid=" + frappeSid);

                String url = frappeResourceUrl + "Salary Structure Assignment/" + created.getName();
                String json = "{\"docstatus\":2}";
                HttpEntity<String> entity = new HttpEntity<>(json, headers);
                ResponseEntity<String> resp = restTemplate.exchange(
                        url, HttpMethod.PUT, entity, String.class
                );
                if (!resp.getStatusCode().is2xxSuccessful()) {
                    logger.warn("Échec de l'annulation du SSA créé: {} - Status: {}", created.getName(), resp.getStatusCode());
                }
            }

            // 2. Recréer le SSA annulé (cancelled_ssa)
            SalarySlipAssignment cancelled = cancelled_ssa != null ? cancelled_ssa.getData() : null;
            if (cancelled != null) {
                cancelled.setDocstatus(1); // Valider lors de la recréation
                String frappeSid = (String) session.getAttribute("frappe_sid");
                if (frappeSid == null) {
                    logger.error("Session utilisateur non authentifiée lors de la restauration.");
                    return new ApiResponse<>("Session utilisateur non authentifiée.");
                }
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add(HttpHeaders.COOKIE, "sid=" + frappeSid);

                // On retire le nom pour forcer la création d'un nouveau document
                cancelled.setName(null);
                String url = frappeResourceUrl + "Salary Structure Assignment";
                String json = objectMapper.writeValueAsString(cancelled);
                HttpEntity<String> entity = new HttpEntity<>(json, headers);
                ResponseEntity<String> resp = restTemplate.exchange(
                        url, HttpMethod.POST, entity, String.class
                );
                if (resp.getStatusCode().is2xxSuccessful()) {
                    JsonNode root = objectMapper.readTree(resp.getBody());
                    JsonNode dataNode = root.get("data");
                    SalarySlipAssignment recreated = objectMapper.treeToValue(dataNode, SalarySlipAssignment.class);
                    logger.info("SSA restauré pour l'employé: {}", employeeName);
                    return new ApiResponse<>(recreated);
                } else {
                    logger.warn("Échec de la recréation du SSA annulé pour l'employé: {} - Status: {}", employeeName, resp.getStatusCode());
                    return new ApiResponse<>("Erreur lors de la recréation du Salary Structure Assignment: " + resp.getStatusCode());
                }
            }
            return new ApiResponse<>("Aucun SSA à restaurer.");
        } catch (Exception e) {
            logger.error("Erreur lors de la restauration du Salary Structure Assignment: {}", e.getMessage(), e);
            return new ApiResponse<>("Erreur lors de la restauration du Salary Structure Assignment: " + e.getMessage());
        }
    }

    public ApiResponse<List<SalarySlipAssignment>> cancelSalaryStructureAssignmentsByBase(
        HttpSession session, double minBase, double maxBase) {
        ApiResponse<List<SalarySlipAssignment>> response = fetchSalaryStructureAssignmentByBase(session, minBase, maxBase);
        if (response.getData() == null || response.getData().isEmpty()) {
            logger.warn("Aucun Salary Structure Assignment à annuler pour la base comprise entre {} et {}", minBase, maxBase);
            return new ApiResponse<>("Aucun Salary Structure Assignment à annuler.");
        }

        List<SalarySlipAssignment> cancelled = new ArrayList<>();
        try {
            String frappeSid = (String) session.getAttribute("frappe_sid");
            if (frappeSid == null) {
                logger.error("Session utilisateur non authentifiée lors de l'annulation.");
                return new ApiResponse<>("Session utilisateur non authentifiée.");
            }

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add(HttpHeaders.COOKIE, "sid=" + frappeSid);

            for (SalarySlipAssignment ssa : response.getData()) {
                String url = frappeResourceUrl + "Salary Structure Assignment/" + ssa.getName();
                String json = "{\"docstatus\":2}";
                logger.info("Annulation du Salary Structure Assignment: {}", ssa.getName());
                HttpEntity<String> entity = new HttpEntity<>(json, headers);
                ResponseEntity<String> resp = restTemplate.exchange(
                        url, HttpMethod.PUT, entity, String.class
                );
                if (resp.getStatusCode().is2xxSuccessful()) {
                    JsonNode root = objectMapper.readTree(resp.getBody());
                    JsonNode dataNode = root.get("data");
                    SalarySlipAssignment cancelledSsa = objectMapper.treeToValue(dataNode, SalarySlipAssignment.class);
                    cancelled.add(cancelledSsa);
                    logger.info("Salary Structure Assignment annulé avec succès: {}", ssa.getName());
                } else {
                    logger.warn("Échec de l'annulation pour: {} - Status: {}", ssa.getName(), resp.getStatusCode());
                }
            }
            logger.info("Nombre total de Salary Structure Assignment annulés: {}", cancelled.size());
            return new ApiResponse<>(cancelled);
        } catch (Exception e) {
            logger.error("Erreur lors de l'annulation des Salary Structure Assignment: {}", e.getMessage(), e);
            return new ApiResponse<>("Erreur lors de l'annulation des Salary Structure Assignment: " + e.getMessage());
        }
    }

    /**
     * Crée un SalarySlipAssignment via l'API Frappe.
     */
    public ApiResponse<SalarySlipAssignment> createOneSalarySlipAssignment(Employee employee, Double salaire_base, HttpSession session) {
        try {
            String url = frappeResourceUrl + "Salary Structure Assignment";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String frappeSid = (String) session.getAttribute("frappe_sid");
            if (frappeSid == null) {
                logger.error("Session utilisateur non authentifiée lors de la création.");
                return new ApiResponse<>("Session utilisateur non authentifiée.");
            }
            headers.add(HttpHeaders.COOKIE, "sid=" + frappeSid);

            // Création de l'objet SalarySlipAssignment à partir de l'employé et du salaire de base
            SalarySlipAssignment assignment = new SalarySlipAssignment();
            assignment.setEmployee(employee.getName());
            assignment.setEmployee_name(employee.getEmployee_name());
            assignment.setSalary_structure("gasy1");
            String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            assignment.setFrom_date(today);
            assignment.setCompany(employee.getCompany());
            assignment.setBase(salaire_base);
            assignment.setDocstatus(1); // Valider lors de la création

            String json = objectMapper.writeValueAsString(assignment);
            logger.info("Création du SalarySlipAssignment pour l'employé: {}, structure: {}", employee.getName());
            HttpEntity<String> entity = new HttpEntity<>(json, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode dataNode = root.get("data");
                SalarySlipAssignment createdAssignment = objectMapper.treeToValue(dataNode, SalarySlipAssignment.class);
                logger.info("SalarySlipAssignment créé avec succès pour l'employé: {}", employee.getName());
                return new ApiResponse<>(createdAssignment);
            } else {
                logger.warn("Échec de la création du SalarySlipAssignment pour l'employé: {} - Status: {}", employee.getName(), response.getStatusCode());
                return new ApiResponse<>("Erreur lors de la création du Salary Structure Assignment: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la création du Salary Structure Assignment: {}", e.getMessage(), e);
            return new ApiResponse<>("Erreur lors de la création du Salary Structure Assignment: " + e.getMessage());
        }
    }

    /**
     * Crée en masse les SalarySlipAssignment via l'API Frappe.
     */
    public ApiResponse<List<SalarySlipAssignment>> createSalarySlipAssignments(
        List<SalarySlipAssignment> assignments, HttpSession session) {
        List<SalarySlipAssignment> created = new ArrayList<>();
        try {
            String url = frappeResourceUrl + "Salary Structure Assignment";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String frappeSid = (String) session.getAttribute("frappe_sid");
            if (frappeSid == null) {
                logger.error("Session utilisateur non authentifiée lors de la création.");
                return new ApiResponse<>("Session utilisateur non authentifiée.");
            }
            headers.add(HttpHeaders.COOKIE, "sid=" + frappeSid);

            logger.info("Début de la création en masse de {} SalarySlipAssignment(s)", assignments.size());
            for (SalarySlipAssignment assignment : assignments) {
                SalarySlipAssignment assignmentToCreate = new SalarySlipAssignment();
                assignmentToCreate.setEmployee(assignment.getEmployee());
                assignmentToCreate.setEmployee_name(assignment.getEmployee_name());
                assignmentToCreate.setSalary_structure(assignment.getSalary_structure());
                assignmentToCreate.setFrom_date(assignment.getFrom_date());
                assignmentToCreate.setCompany(assignment.getCompany());
                assignmentToCreate.setBase(assignment.getBase());
                assignmentToCreate.setDocstatus(1); // Valider lors de la création

                String json = objectMapper.writeValueAsString(assignmentToCreate);
                logger.debug("Création du SalarySlipAssignment pour l'employé: {}, structure: {}", assignment.getEmployee(), assignment.getSalary_structure());
                HttpEntity<String> entity = new HttpEntity<>(json, headers);
                ResponseEntity<String> response = restTemplate.exchange(
                        url, HttpMethod.POST, entity, String.class
                );
                if (response.getStatusCode().is2xxSuccessful()) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    JsonNode dataNode = root.get("data");
                    SalarySlipAssignment createdAssignment = objectMapper.treeToValue(dataNode, SalarySlipAssignment.class);
                    created.add(createdAssignment);
                    logger.info("SalarySlipAssignment créé avec succès pour l'employé: {}", assignment.getEmployee());
                } else {
                    logger.warn("Échec de la création du SalarySlipAssignment pour l'employé: {} - Status: {}", assignment.getEmployee(), response.getStatusCode());
                }
            }
            logger.info("Nombre total de SalarySlipAssignment créés: {}", created.size());
            return new ApiResponse<>(created);
        } catch (Exception e) {
            logger.error("Erreur lors de la création des Salary Structure Assignment: {}", e.getMessage(), e);
            return new ApiResponse<>("Erreur lors de la création des Salary Structure Assignment: " + e.getMessage());
        }
    }
}