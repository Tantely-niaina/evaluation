package mg.erpnext.model;

public class SalaryStructureCSV {
    private String salary_structure;
    private String name;
    private String abbr;
    private String type;
    private String valeur;
    private String company;

  

    // Constructeurs
    public SalaryStructureCSV() {
    }

    public SalaryStructureCSV(String name, String abbr, String type, String valeur) {
        this.name = name;
        this.abbr = abbr;
        this.type = type;
        this.valeur = valeur;
    }


    // public void setFormula(List<SalaryStructure> salaryStructures) {
    //     if (this.getRemarque() == null || this.getRemarque().trim().isEmpty()) {
    //         this.formula = "base";
    //         return;
    //     }

    //     // Créer une map pour retrouver rapidement les abréviations par nom
    //     Map<String, String> nameToAbbr = new HashMap<>();
    //     for (SalaryStructure ss : salaryStructures) {
    //         nameToAbbr.put(ss.getName().toLowerCase(), ss.getAbbr());
    //     }

    //     // Traiter la remarque pour remplacer les noms par les abréviations
    //     String processedRemark = this.getRemarque().toLowerCase();
        
    //     // Remplacer chaque occurrence des noms par leurs abréviations
    //     for (Map.Entry<String, String> entry : nameToAbbr.entrySet()) {
    //         if (processedRemark.contains(entry.getKey())) {
    //             processedRemark = processedRemark.replace(entry.getKey(), entry.getValue());
    //         }
    //     }

    //     // Nettoyer les espaces et formater la formule
    //     processedRemark = processedRemark.replaceAll("\\s+\\+\\s+", "+")
    //                                 .replaceAll("\\s+", "")
    //                                 .trim();

    //     // Gérer le cas où la valeur est un pourcentage
    //     if (this.getValeur().endsWith("%")) {
    //         try {
    //             double percentage = Double.parseDouble(this.getValeur().replace("%", "")) / 100;
    //             this.formula = "(" + processedRemark + ")*" + percentage;
    //         } catch (NumberFormatException e) {
    //             this.formula = processedRemark; // Fallback si le parsing échoue
    //         }
    //     } else {
    //         this.formula = processedRemark;
    //     }
    // }



    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }


    // public String getFromula(){
    //     return this.formula;
    // }


    public String getSalary_structure() {
        return salary_structure;
    }

    public void setSalary_structure(String salary_structure) {
        this.salary_structure = salary_structure;
    }

    // Getters et Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbr() {
        return abbr;
    }

    public void setAbbr(String abbr) {
        this.abbr = abbr;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValeur() {
        return valeur;
    }

    public void setValeur(String valeur) {
        this.valeur = valeur;
    }

    

    @Override
    public String toString() {
        return "SalaryStructure{" +
                "name='" + name + '\'' +
                ", abbr='" + abbr + '\'' +
                ", type='" + type + '\'' +
                ", valeur='" + valeur + '\'' +
                '}';
    }
}
