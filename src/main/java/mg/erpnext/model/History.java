package mg.erpnext.model;

import java.time.LocalDateTime;

public class History {
    private int id;
    private String emplouye_name;
    private Double old_salary_slip;
    private Double new_salary_slip;
    private LocalDateTime updated_at;

    public History() {}
    public History(int id, String emplouye_name, Double old_salary_slip, Double new_salary_slip, LocalDateTime updated_at) {
        this.id = id;
        this.emplouye_name = emplouye_name;
        this.old_salary_slip = old_salary_slip;
        this.new_salary_slip = new_salary_slip;
        this.updated_at = updated_at;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getEmplouye_name() {
        return emplouye_name;
    }
    public void setEmplouye_name(String emplouye_name) {
        this.emplouye_name = emplouye_name;
    }
    public Double getOld_salary_slip() {
        return old_salary_slip;
    }
    public void setOld_salary_slip(Double old_salary_slip) {
        this.old_salary_slip = old_salary_slip;
    }
    public Double getNew_salary_slip() {
        return new_salary_slip;
    }
    public void setNew_salary_slip(Double new_salary_slip) {
        this.new_salary_slip = new_salary_slip;
    }
    public LocalDateTime getUpdated_at() {
        return updated_at;
    }
    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }
   

}