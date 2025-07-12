package mg.erpnext.model;

public class MonthReduction {
    private String mois_annee;
    private double reduction_val;
    private char signe;

    public MonthReduction() {
        // Default constructor
    }

    public MonthReduction(String mois, double reduction_val, char signe) {
        this.mois_annee = mois;
        this.reduction_val = reduction_val;
        this.signe = signe;
    }

    public String getMois_annee() {
        return mois_annee;
    }

    public void setMois_annee(String mois) {
        this.mois_annee = mois;
    }

    public double getReduction_val() {
        return reduction_val;
    }

    public void setReduction_val(double reduction_val) {
        this.reduction_val = reduction_val;
    }

    public char getSigne() {
        return signe;
    }

    public void setSigne(char signe) {
        this.signe = signe;
    }

    public double getSignedReduction() {
    if (signe == '-') return -Math.abs(reduction_val);
    return Math.abs(reduction_val);
}
}
