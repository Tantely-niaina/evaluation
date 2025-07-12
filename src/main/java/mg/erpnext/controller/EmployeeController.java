package mg.erpnext.controller;

import jakarta.servlet.http.HttpSession;
import mg.erpnext.model.ApiResponse;
import mg.erpnext.model.Company;
import mg.erpnext.model.Employee;
import mg.erpnext.service.CompanyService;
import mg.erpnext.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private CompanyService companyService;

    @GetMapping("/employee")
    public String listEmployees(HttpSession session, Model model) {
        ApiResponse<List<Employee>> emp_response = employeeService.fetchEmployees(session);
        ApiResponse<List<Company>> comp_response = companyService.fetchCompanies(session);
        model.addAttribute("employees", emp_response.getData());
        model.addAttribute("error", emp_response.getError());
        model.addAttribute("companies", comp_response.getData());
        model.addAttribute("comp_error", comp_response.getError());
        return "employee/list";
    }

    @GetMapping("/employee/search_id")
    public String searchEmployeeById(
            @RequestParam("identifiant") String identifiant,
            HttpSession session,
            Model model) {
        ApiResponse<List<Employee>> response = employeeService.searchEmployeeById(identifiant, session);
        model.addAttribute("employees", response.getData());
        model.addAttribute("error", response.getError());
        return "employee/list";
    }

    @GetMapping("/employee/search_name")
    public String searchEmployeeByName(
            @RequestParam("nom_complet") String nom_complet,
            HttpSession session,
            Model model) {
        ApiResponse<List<Employee>> response = employeeService.searchEmployeeByName(nom_complet, session);
        model.addAttribute("employees", response.getData());
        model.addAttribute("error", response.getError());
        return "employee/list";
    }

    @GetMapping("/employee/search_company")
    public String searchEmployeeByCompany(
            @RequestParam("company") String company,
            HttpSession session,
            Model model) {
        ApiResponse<List<Employee>> response = employeeService.searchEmployeeByCompany(company, session);
        model.addAttribute("employees", response.getData());
        model.addAttribute("error", response.getError());
        return "employee/list";
    }
}