package com.zs.dto;

public class ResultFile {
    int year;
    String companyName;
    String invoice;
    String credit;
    boolean isCorrection;

    public void setYear(int year) {
        this.year = year;
    }
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    public void setInvoice(String invoice) {
        if (invoice != null && invoice.trim().length() > 0) {
            invoice = invoice.trim();
            invoice = invoice.replaceAll(" ", "_");
        }
        this.invoice = invoice;
    } 
    public void setCredit(String credit) {
        if (credit != null && credit.trim().length() > 0) {
            credit = credit.trim();
            credit = credit.replaceAll(" ", "_");
        }
        this.credit = credit;
    }
    public String getFileName () {
        String str = "";
        if (this.credit != null && this.credit.trim().length() > 0) {
            str = this.credit + "_Gutschrift"; 
        } else {
            str = this.invoice;
        }
        return new StringBuilder()            
            .append(this.year)
            .append("_")
            .append(this.companyName)
            .append("_#")
            .append(str)
            .append(".pdf")
            .toString();
    }
}
