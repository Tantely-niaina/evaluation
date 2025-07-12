package mg.erpnext.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
// import java.text.SimpleDateFormat;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import mg.erpnext.model.EmployeCSV;
import mg.erpnext.model.ParseResult;

@Service
public class EmployeCSVService {

    public ParseResult<EmployeCSV> parseCSV(MultipartFile file) throws Exception {
        ParseResult<EmployeCSV> result = new ParseResult<>();
        // SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean firstLine = true;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                if (values.length < 7) {
                    result.addError("Ligne " + lineNumber + " mal formatée (7 colonnes attendues, " + values.length + " trouvées): " + line);
                    continue;
                }

                try {

                    EmployeCSV customer = new EmployeCSV();
                    // customer.setRef(cleanValue(values[0]));
                    customer.setRef(cleanValue(values[0]) , result.getValidItems());
                    customer.setNom(cleanValue(values[1]));
                    customer.setPrenom(cleanValue(values[2]));
                    customer.setGenre(cleanValue(values[3]));
                    customer.setDateEmbauche(cleanValue(values[4]));
                    customer.setDateNaissance(cleanValue(values[5]));
                    customer.setCompany(cleanValue(values[6]));
                    result.addItem(customer);
                } catch (Exception e) {
                    result.addError("Ligne " + lineNumber + " erreur de parsing : " + e.getMessage());
                }
            }
        }

        return result;
    }


    // Méthode pour nettoyer les valeurs (enlever les guillemets et espaces)
    private String cleanValue(String value) {
        if (value == null) return "";
        return value.trim().replaceAll("^\"|\"$", "");
    }

}




    // public List<EmployeCSV> parseCSV(MultipartFile file) throws Exception {
    //     List<EmployeCSV> customers = new ArrayList<>();
    //     SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    //     try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
    //         String line;
    //         boolean firstLine = true;
            
    //         while ((line = br.readLine()) != null) {
    //             // Sauter les lignes vides
    //             if (line.trim().isEmpty()) {
    //                 continue;
    //             }
                
    //             if (firstLine) {
    //                 firstLine = false;
    //                 continue; // Skip header
    //             }
                
    //             // Utiliser la virgule comme séparateur et gérer les guillemets
    //             String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                
    //             if (values.length < 7) {
    //                 throw new RuntimeException("Ligne mal formatée - 7 colonnes attendues mais " + values.length + " trouvées: " + line);
    //             }
                
    //             try {
    //                 EmployeCSV customer = new EmployeCSV();
    //                 customer.setRef(cleanValue(values[0]));
    //                 customer.setNom(cleanValue(values[1]));
    //                 customer.setPrenom(cleanValue(values[2]));
    //                 customer.setGenre(cleanValue(values[3]));
    //                 customer.setDateEmbauche(cleanValue(values[4]));
    //                 customer.setDateNaissance(cleanValue(values[5]));
    //                 customer.setCompany(cleanValue(values[6]));
                    
    //                 customers.add(customer);
    //             } catch (Exception e) {
    //                 throw new RuntimeException("Format de date invalide dans la ligne: " + line, e);
    //             }
    //         }
    //     }
    //     return customers;
    // }
