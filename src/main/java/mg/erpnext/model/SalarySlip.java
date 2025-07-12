package mg.erpnext.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SalarySlip {
    private String name;
    private String employee;
    private String employee_name;
    private String start_date;
    private String end_date;
    private String posting_date;
    private String company;
    private String department;
    private String designation;
    private String branch;
    private String status;
    private String salary_structure;
    private String payroll_frequency;
    private String currency;
    private Double net_pay;
    private Double gross_pay;
    private Double total_deduction;
    private Double rounded_total;
    private Double total_working_days;
    private Double leave_without_pay;
    private Double payment_days;
    private Double absent_days;
    private Double base_gross_pay;
    private Double base_total_deduction;
    private Double base_net_pay;
    private Double base_rounded_total;
    private Double gross_year_to_date;
    private Double base_gross_year_to_date;
    private Double year_to_date;
    private Double base_year_to_date;
    private Double month_to_date;
    private Double base_month_to_date;
    private String bank_name;
    private String bank_account_no;
    private String mode_of_payment;
    private Double ctc;
    private Double non_taxable_earnings;
    private Double deductions_before_tax_calculation;
    private Double tax_exemption_declaration;
    private Double standard_tax_exemption_amount;
    private Double annual_taxable_amount;
    private Double income_tax_deducted_till_date;
    private Double current_month_income_tax;
    private Double future_income_tax_deductions;
    private Double total_income_tax;
    private String letter_head;
    private String journal_entry;
    private String payroll_entry;
    private Object earnings;
    private Object deductions;
    private Double total_earnings;
    private Integer docstatus;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public String getEmployee_name() {
        return employee_name;
    }

    public void setEmployee_name(String employeeName) {
        this.employee_name = employeeName;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String startDate) {
        this.start_date = startDate;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String endDate) {
        this.end_date = endDate;
    }

    public String getPosting_date() {
        return posting_date;
    }

    public void setPosting_date(String postingDate) {
        this.posting_date = postingDate;
    }

    public Double getNet_pay() {
        return net_pay;
    }

    public void setNet_pay(Double netPay) {
        this.net_pay = netPay;
    }

    public String getSalary_structure() {
        return salary_structure;
    }

    public void setSalary_structure(String salaryStructure) {
        this.salary_structure = salaryStructure;
    }

    public String getPayroll_frequency() {
        return payroll_frequency;
    }

    public void setPayroll_frequency(String payrollFrequency) {
        this.payroll_frequency = payrollFrequency;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getGross_pay() {
        return gross_pay;
    }

    public void setGross_pay(Double grossPay) {
        this.gross_pay = grossPay;
    }

    public Double getRounded_total() {
        return rounded_total;
    }

    public void setRounded_total(Double roundedTotal) {
        this.rounded_total = roundedTotal;
    }

    public Double getTotal_working_days() {
        return total_working_days;
    }

    public void setTotal_working_days(Double totalWorkingDays) {
        this.total_working_days = totalWorkingDays;
    }

    public Double getLeave_without_pay() {
        return leave_without_pay;
    }

    public void setLeave_without_pay(Double leaveWithoutPay) {
        this.leave_without_pay = leaveWithoutPay;
    }

    public Double getPayment_days() {
        return payment_days;
    }

    public void setPayment_days(Double paymentDays) {
        this.payment_days = paymentDays;
    }

    public Double getAbsent_days() {
        return absent_days;
    }

    public void setAbsent_days(Double absentDays) {
        this.absent_days = absentDays;
    }

    public Double getBase_gross_pay() {
        return base_gross_pay;
    }

    public void setBase_gross_pay(Double baseGrossPay) {
        this.base_gross_pay = baseGrossPay;
    }

    public Double getBase_total_deduction() {
        return base_total_deduction;
    }

    public void setBase_total_deduction(Double baseTotalDeduction) {
        this.base_total_deduction = baseTotalDeduction;
    }

    public Double getBase_net_pay() {
        return base_net_pay;
    }

    public void setBase_net_pay(Double baseNetPay) {
        this.base_net_pay = baseNetPay;
    }

    public Double getBase_rounded_total() {
        return base_rounded_total;
    }

    public void setBase_rounded_total(Double baseRoundedTotal) {
        this.base_rounded_total = baseRoundedTotal;
    }

    public Double getGross_year_to_date() {
        return gross_year_to_date;
    }

    public void setGross_year_to_date(Double grossYearToDate) {
        this.gross_year_to_date = grossYearToDate;
    }

    public Double getBase_gross_year_to_date() {
        return base_gross_year_to_date;
    }

    public void setBase_gross_year_to_date(Double baseGrossYearToDate) {
        this.base_gross_year_to_date = baseGrossYearToDate;
    }

    public Double getYear_to_date() {
        return year_to_date;
    }

    public void setYear_to_date(Double yearToDate) {
        this.year_to_date = yearToDate;
    }

    public Double getBase_year_to_date() {
        return base_year_to_date;
    }

    public void setBase_year_to_date(Double baseYearToDate) {
        this.base_year_to_date = baseYearToDate;
    }

    public Double getMonth_to_date() {
        return month_to_date;
    }

    public void setMonth_to_date(Double monthToDate) {
        this.month_to_date = monthToDate;
    }

    public Double getBase_month_to_date() {
        return base_month_to_date;
    }

    public void setBase_month_to_date(Double baseMonthToDate) {
        this.base_month_to_date = baseMonthToDate;
    }

    public String getBank_name() {
        return bank_name;
    }

    public void setBank_name(String bankName) {
        this.bank_name = bankName;
    }

    public String getBank_account_no() {
        return bank_account_no;
    }

    public void setBank_account_no(String bankAccountNo) {
        this.bank_account_no = bankAccountNo;
    }

    public String getMode_of_payment() {
        return mode_of_payment;
    }

    public void setMode_of_payment(String modeOfPayment) {
        this.mode_of_payment = modeOfPayment;
    }

    public Double getCtc() {
        return ctc;
    }

    public void setCtc(Double ctc) {
        this.ctc = ctc;
    }

    public Double getTotal_earnings() {
        return total_earnings;
    }

    public void setTotal_earnings(Double totalEarnings) {
        this.total_earnings = totalEarnings;
    }

    public Double getNon_taxable_earnings() {
        return non_taxable_earnings;
    }

    public void setNon_taxable_earnings(Double nonTaxableEarnings) {
        this.non_taxable_earnings = nonTaxableEarnings;
    }

    public Double getDeductions_before_tax_calculation() {
        return deductions_before_tax_calculation;
    }

    public void setDeductions_before_tax_calculation(Double deductionsBeforeTaxCalculation) {
        this.deductions_before_tax_calculation = deductionsBeforeTaxCalculation;
    }

    public Double getTax_exemption_declaration() {
        return tax_exemption_declaration;
    }

    public void setTax_exemption_declaration(Double taxExemptionDeclaration) {
        this.tax_exemption_declaration = taxExemptionDeclaration;
    }

    public Double getStandard_tax_exemption_amount() {
        return standard_tax_exemption_amount;
    }

    public void setStandard_tax_exemption_amount(Double standardTaxExemptionAmount) {
        this.standard_tax_exemption_amount = standardTaxExemptionAmount;
    }

    public Double getAnnual_taxable_amount() {
        return annual_taxable_amount;
    }

    public void setAnnual_taxable_amount(Double annualTaxableAmount) {
        this.annual_taxable_amount = annualTaxableAmount;
    }

    public Double getIncome_tax_deducted_till_date() {
        return income_tax_deducted_till_date;
    }

    public void setIncome_tax_deducted_till_date(Double incomeTaxDeductedTillDate) {
        this.income_tax_deducted_till_date = incomeTaxDeductedTillDate;
    }

    public Double getCurrent_month_income_tax() {
        return current_month_income_tax;
    }

    public void setCurrent_month_income_tax(Double currentMonthIncomeTax) {
        this.current_month_income_tax = currentMonthIncomeTax;
    }

    public Double getFuture_income_tax_deductions() {
        return future_income_tax_deductions;
    }

    public void setFuture_income_tax_deductions(Double futureIncomeTaxDeductions) {
        this.future_income_tax_deductions = futureIncomeTaxDeductions;
    }

    public Double getTotal_income_tax() {
        return total_income_tax;
    }

    public void setTotal_income_tax(Double totalIncomeTax) {
        this.total_income_tax = totalIncomeTax;
    }

    public String getLetter_head() {
        return letter_head;
    }

    public void setLetter_head(String letterHead) {
        this.letter_head = letterHead;
    }

    public String getJournal_entry() {
        return journal_entry;
    }

    public void setJournal_entry(String journalEntry) {
        this.journal_entry = journalEntry;
    }

    public String getPayroll_entry() {
        return payroll_entry;
    }

    public void setPayroll_entry(String payrollEntry) {
        this.payroll_entry = payrollEntry;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Object getEarnings() {
        return earnings;
    }

    public void setEarnings(Object earnings) {
        this.earnings = earnings;
    }

    public Object getDeductions() {
        return deductions;
    }

    public void setDeductions(Object deductions) {
        this.deductions = deductions;
    }

    public Double getTotal_deduction() {
        return total_deduction;
    }

    public void setTotal_deduction(Double totalDeduction) {
        this.total_deduction = totalDeduction;
    }

    public Integer getDocstatus() {
        return docstatus;
    }

    public void setDocstatus(Integer docstatus) {
        this.docstatus = docstatus;
    }
}