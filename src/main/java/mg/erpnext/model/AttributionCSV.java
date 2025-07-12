package mg.erpnext.model;

import java.util.Objects;

public class AttributionCSV {
    private String mois;
    // private String ref_employe;
    private String salaire_base;
    private String salaire;
    private EmployeCSV employe;

    public AttributionCSV(String mois, String salaire_base, String salaire, EmployeCSV employe) {
        this.mois = mois;
        this.salaire_base = salaire_base;
        this.salaire = salaire;
        this.employe = employe;
    }

    public AttributionCSV() {
    }

    public EmployeCSV getEmploye(){
        return employe;
    }

    public void setEmploye(EmployeCSV employe){
        if(employe == null){
            throw new IllegalArgumentException("L'employe ne peut être null");
        } 
        this.employe = employe;
    }
    
    
    public String getSalaire_base() {
        return salaire_base;
    }

    public void setSalaire_base(String salaire_base) {
        String salaire = validatePositiveNumber(salaire_base, "salaire de base");
        this.salaire_base = salaire;
    }

    public String getSalaire() {
        return salaire;
    }

    public void setSalaire(String salaire) {
        if(salaire == null){
            throw new IllegalArgumentException("Le salaire ne peut être null");
        }
        this.salaire = salaire;
    }

    @Override
    public String toString() {
        return "AttributionCSV [mois=" + mois + ", salaire_base=" + salaire_base + ", salaire=" + salaire + ", employe="
                + employe + "]";
    }

    public String getMois() {
        return mois;
    }

    public void setMois(String mois) {
        this.mois = validateDate(mois);
    }

    // Méthodes de validation helper
    private String validateNonEmpty(String value, String errorMessage) {
        Objects.requireNonNull(value, errorMessage);
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return value;
    }

    private String validatePositiveNumber(String value, String fieldName) {
        validateNonEmpty(value, "Le " + fieldName + " ne peut être nul ou vide");
        try {
            double num = Double.parseDouble(value);
            if (num < 0) {
                throw new IllegalArgumentException("Le " + fieldName + " ne peut être négatif");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Le " + fieldName + " doit être un nombre valide");
        }
    }

    private String validateDate(String dateStr) {
        validateNonEmpty(dateStr, "La date ne peut être nulle ou vide");

        if (!dateStr.matches("^\\d{2}/\\d{2}/\\d{4}$")) {
            throw new IllegalArgumentException("Format de date invalide. Doit être dd/MM/yyyy");
        }

        String[] parts = dateStr.split("/");
        int jour = Integer.parseInt(parts[0]);
        int mois = Integer.parseInt(parts[1]);
        int annee = Integer.parseInt(parts[2]);

        if (mois < 1 || mois > 12) {
            throw new IllegalArgumentException("Mois invalide. Doit être entre 01 et 12");
        }

        if (annee < 1000 || annee > 9999) {
            throw new IllegalArgumentException("Année invalide. Doit avoir 4 chiffres");
        }

        int maxJours;
        if (mois == 2) {
            maxJours = (annee % 4 == 0 && (annee % 100 != 0 || annee % 400 == 0)) ? 29 : 28;
        } else if (mois == 4 || mois == 6 || mois == 9 || mois == 11) {
            maxJours = 30;
        } else {
            maxJours = 31;
        }

        if (jour < 1 || jour > maxJours) {
            throw new IllegalArgumentException("Jour invalide pour ce mois. Maximum: " + maxJours);
        }

        return dateStr;
    }
}