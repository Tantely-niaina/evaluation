package mg.erpnext.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import mg.erpnext.model.AttributionCSV;
import mg.erpnext.model.EmployeCSV;
import mg.erpnext.model.ParseResult;
import mg.erpnext.model.SalaryStructureCSV;
import mg.erpnext.service.AttributionCSVService;
import mg.erpnext.service.EmployeCSVService;
import mg.erpnext.service.FicheEmployeCSVService;
import mg.erpnext.service.GrilleSalaireCSVService;
import mg.erpnext.service.Reinicialisationbaseservice;

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
@PostMapping("/alea1")
public String showPreviewPage(@RequestParam("file1") MultipartFile file1,
                            @RequestParam("file2") MultipartFile file2,
                            @RequestParam("file3") MultipartFile file3,
                            Model model,
                            HttpSession session) {
    try {
        // Parsez les fichiers pour la prévisualisation
        ParseResult<EmployeCSV> employesResult = customerService.parseCSV(file1);
        ParseResult<SalaryStructureCSV> structuresResult = grilleSalaireCSVService.parseCSVsalarystructure(file2);
        ParseResult<AttributionCSV> attributionsResult = attributionCSVService.parseCSVAttributionSalariale(
            file3, 
            employesResult.getValidItems()
        );
        // Calculer le nombre de salaire slip par employé (clé = ref, valeur = nombre)
        Map<String, Integer> salaireSlipCount = new HashMap<>();
        for (AttributionCSV attr : attributionsResult.getValidItems()) {
            String ref = attr.getEmploye().getRef();
            salaireSlipCount.put(ref, salaireSlipCount.getOrDefault(ref, 0) + 1);
        }
        model.addAttribute("salaireSlipCount", salaireSlipCount);
        // Ajoutez les données au modèle - utilisez getValidItems() pour obtenir les listes
        model.addAttribute("employes", employesResult.getValidItems());
        model.addAttribute("structures", structuresResult.getValidItems());
        model.addAttribute("attributions", attributionsResult.getValidItems());
        model.addAttribute("errorsEmployes", employesResult.getErrors());
        model.addAttribute("errorsStructures", structuresResult.getErrors());
        model.addAttribute("errorsAttributions", attributionsResult.getErrors());

        // Stockez les listes d'objets parsés en session pour la confirmation
        session.setAttribute("employes", employesResult.getValidItems());
        session.setAttribute("structures", structuresResult.getValidItems());
        session.setAttribute("attributions", attributionsResult.getValidItems());

        return "import/preview-import";
    } catch (Exception e) {
        model.addAttribute("message", "Erreur lors de la lecture des fichiers: " + e.getMessage());
        return "import/import-customer";
    }
}
@PostMapping("/confirm-import")
public String confirmImport(HttpSession session, RedirectAttributes redirectAttributes) {
    try {
        // Récupérer les listes depuis la session
        @SuppressWarnings("unchecked")
        java.util.List<EmployeCSV> employes = (java.util.List<EmployeCSV>) session.getAttribute("employes");
        @SuppressWarnings("unchecked")
        java.util.List<SalaryStructureCSV> structures = (java.util.List<SalaryStructureCSV>) session.getAttribute("structures");
        @SuppressWarnings("unchecked")
        java.util.List<AttributionCSV> attributions = (java.util.List<AttributionCSV>) session.getAttribute("attributions");

        // Vérifier que les listes existent bien en session
        if (employes == null || structures == null || attributions == null) {
            redirectAttributes.addFlashAttribute("message", "Les données ont expiré. Veuillez réimporter les fichiers.");
            redirectAttributes.addFlashAttribute("error", true);
            return "redirect:/import-csv";
        }

        // Importer les données
        ficheEmployeService.import_trois_fichiers(
            employes,
            structures,
            attributions,
            session
        );

        // Nettoyer la session APRÈS l'import réussi
        session.removeAttribute("employes");
        session.removeAttribute("structures");
        session.removeAttribute("attributions");

        redirectAttributes.addFlashAttribute("message", "Importation réussie !");
        return "redirect:/import-csv";

    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("message", "Échec de l'import: " + e.getMessage());
        redirectAttributes.addFlashAttribute("error", true);
        return "redirect:/import-csv";
    }
}

private String handleFileUpload(HttpSession session, RedirectAttributes redirectAttributes) {
    try {
        // Récupérer les fichiers depuis la session (déjà fait dans confirmImport)
        MultipartFile file1 = (MultipartFile) session.getAttribute("file1");
        MultipartFile file2 = (MultipartFile) session.getAttribute("file2");
        MultipartFile file3 = (MultipartFile) session.getAttribute("file3");

        // Vérification des fichiers
        if (file1 == null || file2 == null || file3 == null || 
            file1.isEmpty() || file2.isEmpty() || file3.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Veuillez sélectionner les trois fichiers.");
            return "redirect:/import-csv";
        }

        // Le reste de votre logique existante...
        ParseResult<EmployeCSV> resultEmployes = customerService.parseCSV(file1);
        ParseResult<SalaryStructureCSV> resultStructures = grilleSalaireCSVService.parseCSVsalarystructure(file2);
        ParseResult<AttributionCSV> resultAttributions = attributionCSVService.parseCSVAttributionSalariale(
            file3, 
            resultEmployes.getValidItems()
        );

        StringBuilder messageBuilder = new StringBuilder();
        boolean hasErrors = false;

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

        if (!resultEmployes.getValidItems().isEmpty() && 
            !resultStructures.getValidItems().isEmpty() && 
            !resultAttributions.getValidItems().isEmpty()) {

            if(!hasErrors) {
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
        redirectAttributes.addFlashAttribute("error", hasErrors);

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
