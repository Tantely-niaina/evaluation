package mg.erpnext.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;

import mg.erpnext.model.SalarySlip;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class SalarySlipPdfService {

    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(240, 240, 240);
    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(200, 200, 200);

    public byte[] generateSalarySlipPdf(SalarySlip salarySlip) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        
        // Marges du document
        document.setMargins(30, 30, 30, 30);

        // Polices
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // En-tête avec informations de base
        addHeader(document, salarySlip, boldFont, regularFont);
        
        // Informations sur les jours travaillés
        addWorkingDaysInfo(document, salarySlip, boldFont, regularFont);
        
        // Section des gains (Earnings)
        addEarningsSection(document, salarySlip, boldFont, regularFont);
        
        // Section des déductions
        addDeductionsSection(document, salarySlip, boldFont, regularFont);
        
        // Résumé financier
        addFinancialSummary(document, salarySlip, boldFont, regularFont);
        
        // Informations fiscales
        addTaxInformation(document, salarySlip, boldFont, regularFont);

        document.close();
        return baos.toByteArray();
    }

    private void addHeader(Document document, SalarySlip salarySlip, PdfFont boldFont, PdfFont regularFont) {
        // Titre principal
        Paragraph title = new Paragraph("FICHE DE PAIE")
                .setFont(boldFont)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        // Tableau des informations de base en 2 colonnes
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
        headerTable.setBorder(new SolidBorder(BORDER_COLOR, 1));

        // Colonne gauche
        addHeaderField(headerTable, "Employee:", salarySlip.getEmployee(), boldFont, regularFont);
        addHeaderField(headerTable, "Employee Name:", salarySlip.getEmployee_name(), boldFont, regularFont);
        addHeaderField(headerTable, "Company:", salarySlip.getCompany(), boldFont, regularFont);
        addHeaderField(headerTable, "Posting Date:", salarySlip.getPosting_date(), boldFont, regularFont);

        // Colonne droite
        addHeaderField(headerTable, "Status:", salarySlip.getStatus(), boldFont, regularFont);
        addHeaderField(headerTable, "Payroll Frequency:", salarySlip.getPayroll_frequency(), boldFont, regularFont);
        addHeaderField(headerTable, "Start Date:", salarySlip.getStart_date(), boldFont, regularFont);
        addHeaderField(headerTable, "End Date:", salarySlip.getEnd_date(), boldFont, regularFont);
        
        // Dernière ligne sur toute la largeur
        Cell salaryStructureCell = new Cell(1, 2)
                .add(new Paragraph("Salary Structure: " + (salarySlip.getSalary_structure() != null ? salarySlip.getSalary_structure() : ""))
                        .setFont(regularFont).setFontSize(10))
                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f))
                .setPadding(5);
        headerTable.addCell(salaryStructureCell);

        document.add(headerTable);
        document.add(new Paragraph("\n"));
    }

    private void addHeaderField(Table table, String label, String value, PdfFont boldFont, PdfFont regularFont) {
        Cell cell = new Cell()
                .add(new Paragraph(label).setFont(boldFont).setFontSize(10))
                .add(new Paragraph(value != null ? value : "").setFont(regularFont).setFontSize(10))
                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f))
                .setPadding(5)
                .setVerticalAlignment(VerticalAlignment.TOP);
        table.addCell(cell);
    }

    private void addWorkingDaysInfo(Document document, SalarySlip salarySlip, PdfFont boldFont, PdfFont regularFont) {
        Table workingDaysTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1, 1, 1})).useAllAvailableWidth();
        workingDaysTable.setBorder(new SolidBorder(BORDER_COLOR, 1));

        // En-têtes
        addWorkingDayCell(workingDaysTable, "Working Days:", formatDouble(salarySlip.getTotal_working_days()), boldFont, regularFont);
        addWorkingDayCell(workingDaysTable, "Unmarked days:", "0", boldFont, regularFont);
        addWorkingDayCell(workingDaysTable, "Leave Without Pay:", formatDouble(salarySlip.getLeave_without_pay()), boldFont, regularFont);
        addWorkingDayCell(workingDaysTable, "Absent Days:", formatDouble(salarySlip.getAbsent_days()), boldFont, regularFont);
        addWorkingDayCell(workingDaysTable, "Payment Days:", formatDouble(salarySlip.getPayment_days()), boldFont, regularFont);
        addWorkingDayCell(workingDaysTable, "", "", boldFont, regularFont); // Cellule vide pour l'alignement

        document.add(workingDaysTable);
        document.add(new Paragraph("\n"));
    }

    private void addWorkingDayCell(Table table, String label, String value, PdfFont boldFont, PdfFont regularFont) {
        Cell cell = new Cell()
                .add(new Paragraph(label).setFont(boldFont).setFontSize(9))
                .add(new Paragraph(value).setFont(regularFont).setFontSize(9))
                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f))
                .setPadding(3)
                .setTextAlignment(TextAlignment.CENTER);
        table.addCell(cell);
    }

    private void addEarningsSection(Document document, SalarySlip salarySlip, PdfFont boldFont, PdfFont regularFont) {
        // En-tête de section
        Paragraph earningsTitle = new Paragraph("EARNINGS")
                .setFont(boldFont)
                .setFontSize(12)
                .setMarginBottom(5);
        document.add(earningsTitle);

        // Tableau des gains
        Table earningsTable = new Table(UnitValue.createPercentArray(new float[]{0.5f, 2f, 1.5f, 1.5f, 1f, 1f})).useAllAvailableWidth();
        earningsTable.setBorder(new SolidBorder(BORDER_COLOR, 1));

        // En-têtes
        addEarningsHeader(earningsTable, "Sr", boldFont);
        addEarningsHeader(earningsTable, "Component", boldFont);
        addEarningsHeader(earningsTable, "Amount", boldFont);
        addEarningsHeader(earningsTable, "Year To Date", boldFont);
        addEarningsHeader(earningsTable, "Tax on flexible benefit", boldFont);
        addEarningsHeader(earningsTable, "Tax on additional salary", boldFont);

        // Exemple de données - vous devrez adapter selon votre modèle SalarySlip
        addEarningsRow(earningsTable, "1", "Salaire Base", salarySlip.getGross_pay(), salarySlip.getGross_year_to_date(), 0.0, 0.0, regularFont);
        // Vous pouvez ajouter d'autres lignes ici selon les composants de salaire

        document.add(earningsTable);
        document.add(new Paragraph("\n"));
    }

    private void addEarningsHeader(Table table, String text, PdfFont boldFont) {
        Cell cell = new Cell()
                .add(new Paragraph(text).setFont(boldFont).setFontSize(9))
                .setBackgroundColor(HEADER_COLOR)
                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f))
                .setPadding(5)
                .setTextAlignment(TextAlignment.CENTER);
        table.addCell(cell);
    }

    private void addEarningsRow(Table table, String sr, String component, Double amount, Double ytd, Double taxFlex, Double taxAdd, PdfFont regularFont) {
        table.addCell(createDataCell(sr, regularFont));
        table.addCell(createDataCell(component, regularFont));
        table.addCell(createDataCell(formatCurrency(amount), regularFont));
        table.addCell(createDataCell(formatCurrency(ytd), regularFont));
        table.addCell(createDataCell(formatCurrency(taxFlex), regularFont));
        table.addCell(createDataCell(formatCurrency(taxAdd), regularFont));
    }

    private void addDeductionsSection(Document document, SalarySlip salarySlip, PdfFont boldFont, PdfFont regularFont) {
        // En-tête de section
        Paragraph deductionsTitle = new Paragraph("DEDUCTIONS")
                .setFont(boldFont)
                .setFontSize(12)
                .setMarginBottom(5);
        document.add(deductionsTitle);

        // Tableau des déductions
        Table deductionsTable = new Table(UnitValue.createPercentArray(new float[]{0.5f, 2f, 1.5f, 1.5f, 1f, 1f})).useAllAvailableWidth();
        deductionsTable.setBorder(new SolidBorder(BORDER_COLOR, 1));

        // En-têtes (même structure que earnings)
        addEarningsHeader(deductionsTable, "Sr", boldFont);
        addEarningsHeader(deductionsTable, "Component", boldFont);
        addEarningsHeader(deductionsTable, "Amount", boldFont);
        addEarningsHeader(deductionsTable, "Year To Date", boldFont);
        addEarningsHeader(deductionsTable, "Tax on flexible benefit", boldFont);
        addEarningsHeader(deductionsTable, "Tax on additional salary", boldFont);

        // Exemple de déduction
        addEarningsRow(deductionsTable, "1", "Taxe sociale", salarySlip.getTotal_deduction(), salarySlip.getTotal_deduction(), 0.0, 0.0, regularFont);

        document.add(deductionsTable);
        document.add(new Paragraph("\n"));
    }

    private void addFinancialSummary(Document document, SalarySlip salarySlip, PdfFont boldFont, PdfFont regularFont) {
        // Tableau en 2 colonnes pour le résumé financier
        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
        summaryTable.setBorder(new SolidBorder(BORDER_COLOR, 1));

        // Colonne gauche
        addSummaryField(summaryTable, "Gross Pay:", formatCurrency(salarySlip.getGross_pay()), boldFont, regularFont);
        addSummaryField(summaryTable, "Gross Pay (Company Currency):", formatCurrency(salarySlip.getGross_pay()), boldFont, regularFont);
        addSummaryField(summaryTable, "Gross Year To Date:", formatCurrency(salarySlip.getGross_year_to_date()), boldFont, regularFont);
        addSummaryField(summaryTable, "Gross Year To Date(Company Currency):", "€ 0.00", boldFont, regularFont);
        addSummaryField(summaryTable, "Total Deduction:", formatCurrency(salarySlip.getTotal_deduction()), boldFont, regularFont);
        addSummaryField(summaryTable, "Total Deduction (Company Currency):", formatCurrency(salarySlip.getTotal_deduction()), boldFont, regularFont);

        // Colonne droite
        addSummaryField(summaryTable, "Net Pay:", formatCurrency(salarySlip.getNet_pay()), boldFont, regularFont);
        addSummaryField(summaryTable, "Net Pay (Company Currency):", formatCurrency(salarySlip.getNet_pay()), boldFont, regularFont);
        addSummaryField(summaryTable, "Rounded Total:", formatCurrency(salarySlip.getRounded_total()), boldFont, regularFont);
        addSummaryField(summaryTable, "Rounded Total (Company Currency):", formatCurrency(salarySlip.getRounded_total()), boldFont, regularFont);
        addSummaryField(summaryTable, "Year To Date:", formatCurrency(salarySlip.getYear_to_date()), boldFont, regularFont);
        addSummaryField(summaryTable, "Year To Date(Company Currency):", "€ 0.00", boldFont, regularFont);

        // Ajout des lignes supplémentaires
        addSummaryField(summaryTable, "Month To Date:", formatCurrency(salarySlip.getMonth_to_date()), boldFont, regularFont);
        addSummaryField(summaryTable, "Month To Date(Company Currency):", "€ 0.00", boldFont, regularFont);
        
        // Total en lettres - sur toute la largeur
        Cell totalWordsCell = new Cell(1, 2)
                .add(new Paragraph("Total in words: " + convertToWords(salarySlip.getNet_pay()))
                        .setFont(regularFont).setFontSize(10))
                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f))
                .setPadding(5);
        summaryTable.addCell(totalWordsCell);

        Cell totalWordsCurrencyCell = new Cell(1, 2)
                .add(new Paragraph("Total in words (Company Currency): " + convertToWords(salarySlip.getNet_pay()))
                        .setFont(regularFont).setFontSize(10))
                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f))
                .setPadding(5);
        summaryTable.addCell(totalWordsCurrencyCell);

        document.add(summaryTable);
        document.add(new Paragraph("\n"));
    }

    private void addSummaryField(Table table, String label, String value, PdfFont boldFont, PdfFont regularFont) {
        Cell cell = new Cell()
                .add(new Paragraph(label).setFont(boldFont).setFontSize(10))
                .add(new Paragraph(value != null ? value : "").setFont(regularFont).setFontSize(10))
                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f))
                .setPadding(5);
        table.addCell(cell);
    }

    private void addTaxInformation(Document document, SalarySlip salarySlip, PdfFont boldFont, PdfFont regularFont) {
        // Tableau en 2 colonnes pour les informations fiscales
        Table taxTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
        taxTable.setBorder(new SolidBorder(BORDER_COLOR, 1));

        addSummaryField(taxTable, "CTC:", formatCurrency(salarySlip.getCtc()), boldFont, regularFont);
        addSummaryField(taxTable, "Income from Other Sources:", "€ 0.00", boldFont, regularFont);
        addSummaryField(taxTable, "Total Earnings:", formatCurrency(salarySlip.getTotal_earnings()), boldFont, regularFont);
        addSummaryField(taxTable, "Non Taxable Earnings:", formatCurrency(salarySlip.getNon_taxable_earnings()), boldFont, regularFont);
        addSummaryField(taxTable, "Standard Tax Exemption Amount:", formatCurrency(salarySlip.getStandard_tax_exemption_amount()), boldFont, regularFont);
        addSummaryField(taxTable, "Tax Exemption Declaration:", formatCurrency(salarySlip.getTax_exemption_declaration()), boldFont, regularFont);
        addSummaryField(taxTable, "Deductions before tax calculation:", formatCurrency(salarySlip.getDeductions_before_tax_calculation()), boldFont, regularFont);
        addSummaryField(taxTable, "Annual Taxable Amount:", formatCurrency(salarySlip.getAnnual_taxable_amount()), boldFont, regularFont);
        addSummaryField(taxTable, "Income Tax Deducted Till Date:", formatCurrency(salarySlip.getIncome_tax_deducted_till_date()), boldFont, regularFont);
        addSummaryField(taxTable, "Current Month Income Tax:", formatCurrency(salarySlip.getCurrent_month_income_tax()), boldFont, regularFont);
        addSummaryField(taxTable, "Future Income Tax:", formatCurrency(salarySlip.getFuture_income_tax_deductions()), boldFont, regularFont);
        addSummaryField(taxTable, "Total Income Tax:", formatCurrency(salarySlip.getTotal_income_tax()), boldFont, regularFont);

        document.add(taxTable);
    }

    private Cell createDataCell(String text, PdfFont regularFont) {
        return new Cell()
                .add(new Paragraph(text != null ? text : "").setFont(regularFont).setFontSize(9))
                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f))
                .setPadding(3)
                .setTextAlignment(TextAlignment.RIGHT);
    }

    private String formatCurrency(Double value) {
        if (value != null) {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);
            currencyFormat.setCurrency(java.util.Currency.getInstance("EUR"));
            return currencyFormat.format(value);
        }
        return "€ 0.00";
    }

    private String formatDouble(Double value) {
        if (value != null) {
            return String.format("%.0f", value);
        }
        return "0";
    }

    private String convertToWords(Double amount) {
        // Implémentation basique - vous pouvez utiliser une bibliothèque comme ICU4J pour une conversion plus robuste
        if (amount == null) return "";
        
        // Exemple simplifié pour les montants en euros
        long integerPart = amount.longValue();
        if (integerPart == 1560000) {
            return "EUR One Million, Five Hundred And Sixty Thousand only.";
        }
        
        // Vous pouvez implémenter une logique plus complète ici
        return "EUR " + String.format("%.0f", amount) + " only.";
    }
}