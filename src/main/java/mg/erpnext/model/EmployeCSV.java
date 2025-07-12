package mg.erpnext.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EmployeCSV {
    private String ref;
    private String nom;
    private String prenom;
    private String genre;
    private Date dateEmbauche;
    private Date dateNaissance;
    private String company;

    // Getters et Setters standards
    public String getRef() { return ref; }
    public void setRef(String ref) { this.ref = ref; }
    public void setRef(String ref , List<EmployeCSV> liste_emploier) { 

        for(EmployeCSV empoiCsv : liste_emploier){
            if(empoiCsv.getRef().equals(ref)){
                throw new IllegalArgumentException("ref "+ ref + "est déja un référence à un autre employé");
            }
        }
        this.ref = ref; 
    }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    // Getters pour les dates (retournent des Date)
    public Date getDateEmbauche() { return dateEmbauche; }
    public Date getDateNaissance() { return dateNaissance; }

    // Setters avec validation pour les dates (acceptent String)
    public void setDateEmbauche(String dateStr) {
        this.dateEmbauche = parseAndValidateDate(dateStr, "d'embauche");
    }

    public void setDateNaissance(String dateStr) {
        this.dateNaissance = parseAndValidateDate(dateStr, "de naissance");
    }

    // Méthode privée pour parser et valider une date
    private Date parseAndValidateDate(String dateStr, String fieldName) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("La date " + fieldName + " ne peut pas être vide.");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false); // Désactive les dates invalides (ex: 31/02/2023)

        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException(
                "Format de date " + fieldName + " invalide. Utilisez JJ/MM/AAAA. Ex: 15/05/1990. " +
                "Erreur détectée: " + e.getMessage()
            );
        }
    }
}