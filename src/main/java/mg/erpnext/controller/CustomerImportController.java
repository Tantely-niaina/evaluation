package mg.erpnext.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import mg.erpnext.model.AttributionCSV;
import mg.erpnext.model.EmployeCSV;
import mg.erpnext.model.ParseResult;
import mg.erpnext.model.SalaryStructureCSV;
import mg.erpnext.service.AttributionCSVService;
import mg.erpnext.service.EmployeCSVService;
import mg.erpnext.service.FicheEmployeCSVService;
import mg.erpnext.service.GrilleSalaireCSVService;
import mg.erpnext.service.Reinicialisationbaseservice;

import jakarta.servlet.http.HttpSession;

@Controller
public class CustomerImportController {
    @Autowired
    private AttributionCSVService attributionCSVService;

    @Autowired
    private FicheEmployeCSVService ficheEmployeService;

    @Autowired
    private Reinicialisationbaseservice reinicialisationbaseservice;

    @Autowired
    private EmployeCSVService customerService;

    @Autowired
    private GrilleSalaireCSVService grilleSalaireCSVService;


    @GetMapping("/import-csv")
    public String showImportForm(Model model, HttpSession session) {
        if(session.getAttribute("frappe_sid") != null) {
            return "import/import-customer";    
        } else {
            System.out.println("Erreur: frappe_sid non trouvé dans la session");
            return "error/error";
        }
    }

    @PostMapping("/import-customer")
    public String handleFileUpload(@RequestParam("file1") MultipartFile file1,
                                @RequestParam("file2") MultipartFile file2,
                                @RequestParam("file3") MultipartFile file3,
                                RedirectAttributes redirectAttributes,
                                HttpSession session) {

        // Vérification des fichiers
        if (file1.isEmpty() || file2.isEmpty() || file3.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Veuillez sélectionner les trois fichiers.");
            return "redirect:/import-csv";
        }

        try {
            // Parsing des fichiers avec gestion des erreurs
            ParseResult<EmployeCSV> resultEmployes = customerService.parseCSV(file1);
            ParseResult<SalaryStructureCSV> resultStructures = grilleSalaireCSVService.parseCSVsalarystructure(file2);
            ParseResult<AttributionCSV> resultAttributions = attributionCSVService.parseCSVAttributionSalariale(file3, resultEmployes.getValidItems());

            // Construction du message de résultat
            StringBuilder messageBuilder = new StringBuilder();
            boolean hasErrors = false;

            // Vérification des erreurs pour chaque fichier
            if (resultEmployes.hasErrors() || resultStructures.hasErrors() || resultAttributions.hasErrors()) {
                hasErrors = true;
                
                messageBuilder.append("Importation partielle réussie, mais avec des erreurs :<br/><br/>");
                
                if (resultEmployes.hasErrors()) {
                    messageBuilder.append("Erreurs dans le fichier Employés :<br/>")
                                .append(String.join("<br/>", resultEmployes.getErrors()))
                                .append("<br/><br/>");
                }
                
                if (resultStructures.hasErrors()) {
                    messageBuilder.append("Erreurs dans le fichier Grilles salariales :<br/>")
                                .append(String.join("<br/>", resultStructures.getErrors()))
                                .append("<br/><br/>");
                }
                
                if (resultAttributions.hasErrors()) {
                    messageBuilder.append("Erreurs dans le fichier Attributions :<br/>")
                                .append(String.join("<br/>", resultAttributions.getErrors()))
                                .append("<br/>");
                }
            } else {
                messageBuilder.append("Importation réussie !<br/>")
                            .append("- Employés : ").append(resultEmployes.getValidItems().size()).append("<br/>")
                            .append("- Grilles salariales : ").append(resultStructures.getValidItems().size()).append("<br/>")
                            .append("- Attributions : ").append(resultAttributions.getValidItems().size());
            }

            // Importation des données valides seulement si au moins un élément valide par fichier
            if (!resultEmployes.getValidItems().isEmpty() && 
                !resultStructures.getValidItems().isEmpty() && 
                !resultAttributions.getValidItems().isEmpty()) {

                if(!hasErrors){
                    ficheEmployeService.import_trois_fichiers(
                    resultEmployes.getValidItems(),
                    resultStructures.getValidItems(),
                    resultAttributions.getValidItems(),
                    session
                );
                }
                
            } else {
                hasErrors = true;
                messageBuilder.append("<br/><br/>Importation annulée : un ou plusieurs fichiers ne contiennent aucune donnée valide.");
            }

            redirectAttributes.addFlashAttribute("message", messageBuilder.toString());

            if (hasErrors) {
                redirectAttributes.addFlashAttribute("error", true);
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", 
                "Échec critique de l'importation : " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", true);
        }

        return "redirect:/import-csv";
    }
    
    // Dans votre contrôleur
    @PostMapping("/reinitialiser-base")
    public String reinitialiserBase(HttpSession session , RedirectAttributes redirectAttributes) {
        try {
            reinicialisationbaseservice.reinitialiserBaseDonnees(session);
            redirectAttributes.addFlashAttribute("message", "Réinitialisation des données réussie !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Échec de la réinitialisation : " + e.getMessage());
        }
        return "redirect:/import-csv";
    }

    // // version Lalatiana
    // @PostMapping("/import-customer")
    // public String handleFileUpload(@RequestParam("file1") MultipartFile file1,
    //                                @RequestParam("file2") MultipartFile file2, 
    //                                @RequestParam("file3") MultipartFile file3,
    //                              RedirectAttributes redirectAttributes,
    //                              HttpSession session) {
    //     if (file1.isEmpty()) {
    //         redirectAttributes.addFlashAttribute("message", "Veuillez sélectionner un fichier à importer.");
    //         return "redirect:/app/import-csv";
    //     }

    //     if(file2.isEmpty()) {
    //         redirectAttributes.addFlashAttribute("message", "Veuillez sélectionner un fichier à importer.");
    //         return "redirect:/app/import-csv";
    //     } 

    //     if(file3.isEmpty()) {
    //         redirectAttributes.addFlashAttribute("message", "Veuillez sélectionner un fichier à importer.");
    //         return "redirect:/app/import-csv";
    //     }

    //     try {

    //         List<EmployeCSV> customers = customerService.parseCSV(file1);
    //         List<SalaryStructure> salarystructures = grilleSalaireCSVService.parseCSVsalarystructure(file2);  
    //         List<AttributionCSV> attributions = attributionCSVService.parseCSVAttributionSalariale(file3, customers);
           


    //         ficheEmployeService.import_trois_fichiers(customers , salarystructures , attributions,  session);

    //         redirectAttributes.addFlashAttribute("message", "Importation réussie pour " + attributions.size() + " attributions.");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("message", "Échec de l'importation: " + e.getMessage());
    //     }

    //     return "redirect:/app/import-csv";
    // }


    // @PostMapping("/import-customer")
    // public String handleFileUpload(@RequestParam("file1") MultipartFile file1,
    //                             @RequestParam("file2") MultipartFile file2,
    //                             @RequestParam("file3") MultipartFile file3,
    //                             RedirectAttributes redirectAttributes,
    //                             HttpSession session) {


    //     int error_donner = 0;

    //     if (file1.isEmpty() || file2.isEmpty() || file3.isEmpty()) {
    //         redirectAttributes.addFlashAttribute("message", "Veuillez sélectionner les trois fichiers.");
    //         return "redirect:/app/import-csv";
    //     }

    //     try {
    //         // Étape 1 : parsing avec collecte des erreurs
    //         ParseResult<EmployeCSV> resultEmployes = customerService.parseCSV(file1);
    //         List<EmployeCSV> customers = resultEmployes.getValidItems();

    //         List<SalaryStructure> salarystructures = grilleSalaireCSVService.parseCSVsalarystructure(file2);  
    //         List<AttributionCSV> attributions = attributionCSVService.parseCSVAttributionSalariale(file3, customers);

    //         String successMessage = "Importation réussie pour " + attributions.size() + " attributions.";
    //         if (resultEmployes.hasErrors()) {
    //             successMessage += "<br/>Mais quelques erreurs ont été trouvées dans le fichier des employés :<br/>" + String.join("<br/>", resultEmployes.getErrors());
    //             error_donner = 1;
    //         }
    //         redirectAttributes.addFlashAttribute("message", successMessage);

    //         if(error_donner == 1){
    //              return "redirect:/app/import-csv";
    //         } else{
    //             ficheEmployeService.import_trois_fichiers(customers , salarystructures , attributions, session);
    //         } 

    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("message", "Échec de l'importation: " + e.getMessage());
    //     }

    //     return "redirect:/app/import-csv";
    // }
}
