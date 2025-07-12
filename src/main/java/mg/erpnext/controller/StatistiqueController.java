package mg.erpnext.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import mg.erpnext.model.ApiResponse;
import mg.erpnext.model.SalarySlip;
import mg.erpnext.service.StatistiqueService;

@Controller
public class StatistiqueController {

    @Autowired
    private StatistiqueService statistiqueService;

    @GetMapping("/statistics")
    public String listTotal(HttpSession session, Model model) {
        ApiResponse<Map<String, Double>> ss_response = statistiqueService.calculerTotalNetParMois(session);
        model.addAttribute("salary_slips", ss_response.getData());
        model.addAttribute("ss_error", ss_response.getError());

        // Ajout pour le graphe
        ApiResponse<Map<String, Map<String, Double>>> evolution = statistiqueService.getEvolutionSalaireParMois(session);
        model.addAttribute("evolution_data", evolution.getData());
        model.addAttribute("evolution_error", evolution.getError());

        return "statistique/list";
    }

    @GetMapping("/statistics/details")
    public String listTotalDetails(@RequestParam("year_month") String year_month, HttpSession session, Model model) {
        String[] parts = year_month.split("-");
        int year = Integer.parseInt(parts[0]);
        int monthValue = Integer.parseInt(parts[1]);
        ApiResponse<List<SalarySlip>> ss_response = statistiqueService.searchSalaryElementsByMonth(year, monthValue, session);
        model.addAttribute("salary_elements", ss_response.getData());
        model.addAttribute("ss_error", ss_response.getError());
        return "statistique/details";
    }
}
