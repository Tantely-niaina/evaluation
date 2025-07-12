package mg.erpnext.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import mg.erpnext.model.ParseResult;
import mg.erpnext.model.SalaryStructureCSV;

@Service
public class GrilleSalaireCSVService {


    // public List<SalaryStructure> parseCSVsalarystructure(MultipartFile file) throws Exception {
    //     List<SalaryStructure> structures = new ArrayList<>();

    //     try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
    //         String line;
    //         boolean firstLine = true;
            
    //         while ((line = br.readLine()) != null) {
    //             if (line.trim().isEmpty()) continue;
                
    //             if (firstLine) {
    //                 firstLine = false;
    //                 continue; // Skip header
    //             }
                
    //             String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                
    //             if (values.length < 5) {
    //                 throw new RuntimeException("Format invalide. 5 colonnes attendues : name,abbr,type,valeur,remarque");
    //             }
                
    //             SalaryStructure structure = new SalaryStructure();
    //             structure.setSalary_structure(values[0]);
    //             structure.setName(cleanValue(values[1]));
    //             structure.setAbbr(cleanValue(values[2]));
    //             structure.setType(cleanValue(values[3]));
    //             structure.setValeur(cleanValue(values[4]));
    //             structure.setCompany(cleanValue(values[5]));
    //             structures.add(structure);
    //         }
    //     }
    //     return structures;
    // }


    public ParseResult<SalaryStructureCSV> parseCSVsalarystructure(MultipartFile file) throws Exception {
        ParseResult<SalaryStructureCSV> result = new ParseResult<>();
        int lineNumber = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean firstLine = true;
            
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;
                
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                
                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                
                if (values.length < 6) {
                    result.addError("Ligne " + lineNumber + " mal formatée (6 colonnes attendues, " + values.length + " trouvées): " + line);
                    continue;
                }
                
                try {
                    SalaryStructureCSV structure = new SalaryStructureCSV();
                    structure.setSalary_structure(cleanValue(values[0]));
                    structure.setName(cleanValue(values[1]));
                    structure.setAbbr(cleanValue(values[2]));
                    structure.setType(cleanValue(values[3]));
                    structure.setValeur(cleanValue(values[4]));
                    structure.setCompany(cleanValue(values[5]));
                    result.addItem(structure);
                } catch (Exception e) {
                    result.addError("Ligne " + lineNumber + " erreur de parsing : " + e.getMessage());
                }
            }
        }
        return result;
    }

    private String cleanValue(String value) {
        if (value == null) return "";
        return value.trim().replaceAll("^\"|\"$", "");
    }


}
