package mg.erpnext.model;

public class SalaryDetail {
    private String salary_component;
    private Double amount;

    // Getters & Setters
    public String getSalary_component() { return salary_component; }
    public void setSalary_component(String salary_component) { this.salary_component = salary_component; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}