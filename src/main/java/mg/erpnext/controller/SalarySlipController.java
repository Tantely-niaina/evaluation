package mg.erpnext.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import mg.erpnext.model.ApiResponse;
import mg.erpnext.model.Employee;
import mg.erpnext.model.History;
import mg.erpnext.model.SalarySlip;
import mg.erpnext.service.EmployeeService;
import mg.erpnext.service.HistoryService;
import mg.erpnext.service.SalarySlipAssignmentService;
import mg.erpnext.service.SalarySlipPdfService;
import mg.erpnext.service.SalarySlipService;
import mg.erpnext.service.SalarySlipWithComponentsService;

@Controller
public class SalarySlipController {

    @Autowired
    private SalarySlipService salarySlipService;

    @Autowired
    private SalarySlipPdfService salarySlipPdfService;

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private HistoryService HistoryService;
     @Autowired
    private SalarySlipWithComponentsService salarySlipWithComponentsService;

    @Autowired
    private SalarySlipAssignmentService salarySlipAssignmentService;

      private static final Logger logger = LoggerFactory.getLogger(SalarySlipService.class);

    @GetMapping("/salary_slip")
    public String listSalarySlip(HttpSession session, Model model) {
        ApiResponse<List<SalarySlip>> ss_response = salarySlipService.fetchSalarySlips(session);
        model.addAttribute("salary_slips", ss_response.getData());
        model.addAttribute("ss_error", ss_response.getError());
        return "salary_slip/list";
    }

    @GetMapping("/salary_elements")
    public String listSalaryElements(HttpSession session, Model model) {
        ApiResponse<List<SalarySlip>> ss_response = salarySlipService.fetchSalaryElements(session);
        model.addAttribute("salary_elements", ss_response.getData());
        model.addAttribute("ss_error", ss_response.getError());

        Map<Integer, String> mois = new LinkedHashMap<>();
        mois.put(1, "Janvier");
        mois.put(2, "Février");
        mois.put(3, "Mars");
        mois.put(4, "Avril");
        mois.put(5, "Mai");
        mois.put(6, "Juin");
        mois.put(7, "Juillet");
        mois.put(8, "Août");
        mois.put(9, "Septembre");
        mois.put(10, "Octobre");
        mois.put(11, "Novembre");
        mois.put(12, "Décembre");

        model.addAttribute("months", mois);

        return "salary_elements/list";
    }

    @GetMapping("/salary_elements/search_by_month")
    public String listSalaryElementsByMonth(@RequestParam("month") String month, HttpSession session, Model model) {
        ApiResponse<List<SalarySlip>> ss_response = salarySlipService.searchSalaryElementsByMonth(Integer.parseInt(month), session);
        model.addAttribute("salary_elements", ss_response.getData());
        model.addAttribute("ss_error", ss_response.getError());
        return "salary_elements/list";
    }

    @GetMapping("/salary_slip/details")
    public String getSalarySlipDetails(
            @RequestParam("name") String name,
            HttpSession session,
            Model model) {
        ApiResponse<SalarySlip> response = salarySlipService.getSalarySlipDetails(name, session);
        model.addAttribute("salary_slip", response.getData());
        model.addAttribute("error", response.getError());
        return "salary_slip/details";
    }

    @GetMapping("/salary_slip/search_salary_slip")
    public String searchEmployeeByCompany(
            @RequestParam("employee") String employee,
            HttpSession session,
            Model model) {
        ApiResponse<List<SalarySlip>> response = salarySlipService.searchSalarySlipByEmployee(employee, session);
        model.addAttribute("salary_slips", response.getData());
        model.addAttribute("error", response.getError());
        return "salary_slip/list";
    }

    @GetMapping("/salary_slip/export_pdf")
    public ResponseEntity<byte[]> exportSalarySlipToPdf(
            @RequestParam("name") String name,
            HttpSession session) {
        try {
            ApiResponse<SalarySlip> response = salarySlipService.getSalarySlipDetails(name, session);
            
            if (response.getError() != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            
            if (response.getData() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            byte[] pdfContent = salarySlipPdfService.generateSalarySlipPdf(response.getData());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "fiche_paie_" + name + ".pdf");
            headers.setContentLength(pdfContent.length);

            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/salary_slip/form")
    public String form(HttpSession session, Model model) {
        ApiResponse<List<Employee>> ss_response = employeeService.fetchEmployees(session);
        model.addAttribute("employees", ss_response.getData());
        model.addAttribute("ss_error", ss_response.getError());
        return "salary_slip/form";
    }

    @PostMapping("/salary_slip/generate")
    public String generate(
            @RequestParam("employee") String employee,
            @RequestParam("start_date") String start_date,
            @RequestParam("end_date") String end_date,
            @RequestParam(value = "salaire_base", required = false) String salaire_base,
            @RequestParam(value = "ecraser", required = false) boolean ecraser,
            @RequestParam(value = "moyenne", required = false) String moyenne,
            HttpSession session,
            Model model) {
        boolean useMoyenne = "on".equals(moyenne);
        Employee employeeResponse = employeeService.searchOneEmployeeById(employee, session);
        if (salaire_base != null && !salaire_base.trim().isEmpty() && !useMoyenne) {
            try {
                double base = Double.parseDouble(salaire_base.trim());
                salarySlipAssignmentService.createOneSalarySlipAssignment(employeeResponse, base, session);
            } catch (NumberFormatException e) {
                model.addAttribute("ss_error", "Salaire base invalide.");
                return "salary_slip/form";
            }
        }
        if (useMoyenne && (salaire_base == null || salaire_base.trim().isEmpty())) {
           
            try {
                List<Double> resuList = salarySlipService.getAllBaseValues(session);
                Double moyenneValue = salarySlipService.calculeMoyenne(resuList);
            logger.info("moyenne ajouter avec succes",moyenneValue );
                salarySlipAssignmentService.createOneSalarySlipAssignment(employeeResponse, moyenneValue, session);
            } catch (NumberFormatException e) {
                model.addAttribute("ss_error", "Salaire base invalide.");
                return "salary_slip/form";
            }
        }
        if(useMoyenne && (salaire_base != null && !salaire_base.trim().isEmpty())) {
            model.addAttribute("ss_error", "Veuillez choisir entre un salaire base ou la moyenne.");
            return "salary_slip/form";
        }
        //moyenne par employé
         /*if(useMoyenne && (salaire_base != null && !salaire_base.trim().isEmpty())) {
            try {
                double base = 0;
                ApiResponse<List<SalarySlipAssignment>> assignmentResponse = salarySlipAssignmentService.searchSalaryStructureAssignmentByEmployee(session, employeeResponse.getName());
                base = salarySlipAssignmentService.calculerMoyenne(assignmentResponse);
                salarySlipAssignmentService.createOneSalarySlipAssignment(employeeResponse, base, session);
            } catch (NumberFormatException e) {
                model.addAttribute("ss_error", "Salaire base invalide.");
                return "salary_slip/form";
            }
        } */
        
 ApiResponse<List<SalarySlip>> response = salarySlipService.generateSalarySlipsForUnoccupiedMonths(employeeResponse, start_date, end_date, ecraser, session);
        model.addAttribute("salary_slip", response.getData());
        model.addAttribute("ss_error", response.getError());
        return "salary_slip/form";
    }

    @GetMapping("/salary_slip/update_form")
    public String updateForm(HttpSession session, Model model) {
        ApiResponse<List<Employee>> ss_response = employeeService.fetchEmployees(session);
        model.addAttribute("employees", ss_response.getData());
        model.addAttribute("ss_error", ss_response.getError());
        return "salary_slip/update_form";
    }

    @PostMapping("/salary_slip/update")
    public String update(
            @RequestParam("min_amount") String min_amount,
            @RequestParam("max_amount") String max_amount,
            @RequestParam("base_salary") String base_salary,
            HttpSession session,
            Model model) {

        try {
            double min = Double.parseDouble(min_amount);
            double max = Double.parseDouble(max_amount);
            double percent = Double.parseDouble(base_salary);

            ApiResponse<String> response = salarySlipService.updateSalarySlipsWithNewBase(min, max, percent, session);

            if (response.getError() != null) {
                model.addAttribute("ss_error", response.getError());
            } else {
                model.addAttribute("ss_success", response.getData());
            }
        } catch (Exception e) {
            model.addAttribute("ss_error", "Erreur lors de la mise à jour : " + e.getMessage());
        }

        return "salary_slip/update_form";
    }
    @GetMapping("/salary_slip/with_components")
    public String listSalarySlipWithComponents(HttpSession session, Model model) {
        ApiResponse<List<SalarySlip>> response = salarySlipWithComponentsService.fetchSalarySlipsWithComponents(session);
        model.addAttribute("ss_error", response.getError());
        ApiResponse<List<Map<String, Object>>> valiny = salarySlipWithComponentsService.fetchSalaryComponents(session);
        model.addAttribute("ss_error", valiny.getError());

        if (response.getData() != null && valiny.getData() != null && !valiny.getData().isEmpty()) {
            model.addAttribute("salary_slips", response.getData());
            model.addAttribute("Elements", valiny.getData());
        } else {
            model.addAttribute("salary_slips", null);
        }
        return "salary_slip/list_with_components";
    }
      @GetMapping("/salary_slip/alea2")
    public String filterSalarySlipAlea2(
            @RequestParam("montantmin") double montantMin,
            @RequestParam("montantmax") double montantMax,
            @RequestParam("Elements") String componentName,
            HttpSession session,
            Model model) {
        ApiResponse<List<SalarySlip>> response = salarySlipWithComponentsService.filterSalarySlipsByComponent(session, montantMin, montantMax, componentName);
        model.addAttribute("ss_error", response.getError());
        model.addAttribute("salary_slips", response.getData());
        return "salary_slip/list_with_components";
    }
    @GetMapping("/salary_slip/alea3")
    public String filterSalarySlipAlea3(
            HttpSession session,
            Model model) {
        List<History> historyList = HistoryService.getAllHistorys();
        model.addAttribute("historys", historyList);
        return "salary_slip/list_with_history";
    }
    @GetMapping("/salary_slip/alea4")
    public String showfromalea4(
            @RequestParam("name") String name,
            HttpSession session,
            Model model) {
        ApiResponse<SalarySlip> response = salarySlipService.getSalarySlipDetails(name, session);
        model.addAttribute("salary_slip", response.getData());
        model.addAttribute("error", response.getError());
        return "salary_slip/update_base_salary";
    }

}