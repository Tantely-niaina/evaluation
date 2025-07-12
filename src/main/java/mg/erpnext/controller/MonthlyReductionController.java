package mg.erpnext.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import mg.erpnext.model.MonthReduction;
import mg.erpnext.service.MonthReductionService;

import jakarta.servlet.http.HttpSession;

@Controller
public class MonthlyReductionController {

    @Autowired
    private MonthReductionService monthReductionService;

    @GetMapping("/reduction")
    public String reduction(HttpSession session, Model model) {
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
        return "reduction/form";
    }

    @PostMapping("/reduction/insert")
    public String insert_reduction(
            @RequestParam("month") String month,
            @RequestParam("reduction_val") String reduction_val,
            HttpSession session,
            Model model) {
        char signe = reduction_val.charAt(0);
        if (signe != '+' && signe != '-') {
            signe = '+';
        }
        if(signe == '+' || signe == '-') {
            reduction_val = reduction_val.substring(1);
        }
        if(month != null && !month.isEmpty()) {
            if(month.length() == 1) {
                month = "2025-0" + month;
            }
        }
        MonthReduction monthReduction = new MonthReduction(month, Double.parseDouble(reduction_val), signe);
        try {
            monthReductionService.insertMonthReduction(monthReduction);
            model.addAttribute("successMessage", "Réduction ajoutée avec succès !");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erreur lors de l'ajout de la réduction : " + e.getMessage());
        }
        return "reduction/form";
    }
}