package Models;

import java.util.*;
import java.util.regex.Pattern;

/**
 * GovIdModel class that maps to the govid table
 * Fields: govId, sss, philhealth, tin, pagibig, employeeId
 * 
 * Handles multiple government IDs per employee with enhanced validation
 * for Philippine government ID formats used in payroll deductions
 * 
 * @author User
 */
public class GovIdModel {
    
    private Integer govId;
    private String sss;
    private String philhealth;
    private String tin;
    private String pagibig;
    private Integer employeeId;
    
    // Enhanced validation patterns for Philippine government IDs
    // Updated patterns based on actual Philippine government ID formats
    private static final Pattern SSS_PATTERN = Pattern.compile("^\\d{2}-\\d{7}-\\d{1}$");
    private static final Pattern PHILHEALTH_PATTERN = Pattern.compile("^\\d{2}-\\d{9}-\\d{1}$");
    private static final Pattern TIN_PATTERN = Pattern.compile("^\\d{3}-\\d{3}-\\d{3}-\\d{3}$");
    private static final Pattern PAGIBIG_PATTERN = Pattern.compile("^\\d{4}-\\d{4}-\\d{4}$");
    
    // Alternative patterns for flexible input (digits only)
    private static final Pattern SSS_DIGITS_PATTERN = Pattern.compile("^\\d{10}$");
    private static final Pattern PHILHEALTH_DIGITS_PATTERN = Pattern.compile("^\\d{12}$");
    private static final Pattern TIN_DIGITS_PATTERN = Pattern.compile("^\\d{12}$");
    private static final Pattern PAGIBIG_DIGITS_PATTERN = Pattern.compile("^\\d{12}$");
    
    // ID type constants
    public static final String ID_TYPE_SSS = "SSS";
    public static final String ID_TYPE_PHILHEALTH = "PhilHealth";
    public static final String ID_TYPE_TIN = "TIN";
    public static final String ID_TYPE_PAGIBIG = "Pag-IBIG";
    
    // All required government IDs for payroll
    public static final String[] ALL_GOVERNMENT_IDS = {
        ID_TYPE_SSS, ID_TYPE_PHILHEALTH, ID_TYPE_TIN, ID_TYPE_PAGIBIG
    };
    
    // Constructors
    public GovIdModel() {}
    
    public GovIdModel(Integer employeeId) {
        this.employeeId = employeeId;
    }
    
    public GovIdModel(String sss, String philhealth, String tin, String pagibig, Integer employeeId) {
        this.sss = sss;
        this.philhealth = philhealth;
        this.tin = tin;
        this.pagibig = pagibig;
        this.employeeId = employeeId;
    }
    
    public GovIdModel(Integer govId, String sss, String philhealth, String tin, String pagibig, Integer employeeId) {
        this.govId = govId;
        this.sss = sss;
        this.philhealth = philhealth;
        this.tin = tin;
        this.pagibig = pagibig;
        this.employeeId = employeeId;
    }
    
    // Getters and Setters
    public Integer getGovId() { return govId; }
    public void setGovId(Integer govId) { this.govId = govId; }
    
    public String getSss() { return sss; }
    public void setSss(String sss) { this.sss = sss; }
    
    public String getPhilhealth() { return philhealth; }
    public void setPhilhealth(String philhealth) { this.philhealth = philhealth; }
    
    public String getTin() { return tin; }
    public void setTin(String tin) { this.tin = tin; }
    
    public String getPagibig() { return pagibig; }
    public void setPagibig(String pagibig) { this.pagibig = pagibig; }
    
    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }
    
 
    // ENHANCED VALIDATION METHODS
    
    /**
     * Validate SSS format (XX-XXXXXXX-X or 10 digits)
     * @param sss SSS number to validate
     * @return true if valid SSS format
     */
    public static boolean isValidSss(String sss) {
        if (sss == null || sss.trim().isEmpty()) {
            return false;
        }
        
        String cleanSss = sss.trim();
        return SSS_PATTERN.matcher(cleanSss).matches() || 
               SSS_DIGITS_PATTERN.matcher(cleanSss).matches();
    }
    
    /**
     * Validate PhilHealth format (XX-XXXXXXXXX-X or 12 digits)
     * @param philhealth PhilHealth number to validate
     * @return true if valid PhilHealth format
     */
    public static boolean isValidPhilhealth(String philhealth) {
        if (philhealth == null || philhealth.trim().isEmpty()) {
            return false;
        }
        
        String cleanPhilhealth = philhealth.trim();
        return PHILHEALTH_PATTERN.matcher(cleanPhilhealth).matches() || 
               PHILHEALTH_DIGITS_PATTERN.matcher(cleanPhilhealth).matches();
    }
    
    /**
     * Validate TIN format (XXX-XXX-XXX-XXX or 12 digits)
     * @param tin TIN number to validate
     * @return true if valid TIN format
     */
    public static boolean isValidTin(String tin) {
        if (tin == null || tin.trim().isEmpty()) {
            return false;
        }
        
        String cleanTin = tin.trim();
        return TIN_PATTERN.matcher(cleanTin).matches() || 
               TIN_DIGITS_PATTERN.matcher(cleanTin).matches();
    }
    
    /**
     * Validate Pag-IBIG format (XXXX-XXXX-XXXX or 12 digits)
     * @param pagibig Pag-IBIG number to validate
     * @return true if valid Pag-IBIG format
     */
    public static boolean isValidPagibig(String pagibig) {
        if (pagibig == null || pagibig.trim().isEmpty()) {
            return false;
        }
        
        String cleanPagibig = pagibig.trim();
        return PAGIBIG_PATTERN.matcher(cleanPagibig).matches() || 
               PAGIBIG_DIGITS_PATTERN.matcher(cleanPagibig).matches();
    }
    
    /**
     * Validate specific government ID by type
     * @param idType Type of ID (SSS, PhilHealth, TIN, Pag-IBIG)
     * @param idNumber ID number to validate
     * @return true if valid for the specified type
     */
    public static boolean isValidIdByType(String idType, String idNumber) {
        if (idType == null || idNumber == null) {
            return false;
        }
        
        return switch (idType.toUpperCase()) {
            case ID_TYPE_SSS -> isValidSss(idNumber);
            case "PHILHEALTH" -> isValidPhilhealth(idNumber);
            case ID_TYPE_TIN -> isValidTin(idNumber);
            case "PAG-IBIG", "PAGIBIG" -> isValidPagibig(idNumber);
            default -> false;
        };
    }
    
    /**
     * Validate all IDs in this record
     * @return ValidationResult with detailed validation information
     */
    public ValidationResult validateAllIds() {
        ValidationResult result = new ValidationResult();
        
        // Validate SSS
        if (sss != null && !sss.trim().isEmpty()) {
            if (!isValidSss(sss)) {
                result.addError("SSS number format is invalid. Expected: XX-XXXXXXX-X or 10 digits");
            }
        }
        
        // Validate PhilHealth
        if (philhealth != null && !philhealth.trim().isEmpty()) {
            if (!isValidPhilhealth(philhealth)) {
                result.addError("PhilHealth number format is invalid. Expected: XX-XXXXXXXXX-X or 12 digits");
            }
        }
        
        // Validate TIN
        if (tin != null && !tin.trim().isEmpty()) {
            if (!isValidTin(tin)) {
                result.addError("TIN number format is invalid. Expected: XXX-XXX-XXX-XXX or 12 digits");
            }
        }
        
        // Validate Pag-IBIG
        if (pagibig != null && !pagibig.trim().isEmpty()) {
            if (!isValidPagibig(pagibig)) {
                result.addError("Pag-IBIG number format is invalid. Expected: XXXX-XXXX-XXXX or 12 digits");
            }
        }
        
        return result;
    }
    
    /**
     * Check if employee record has all mandatory government IDs
     * Required for payroll deductions
     * @return true if all mandatory IDs are present and valid
     */
    public boolean hasAllMandatoryIds() {
        return hasValidSss() && hasValidPhilhealth() && hasValidTin() && hasValidPagibig();
    }
    
    /**
     * Check if SSS is present and valid
     * @return true if SSS is valid
     */
    public boolean hasValidSss() {
        return sss != null && !sss.trim().isEmpty() && isValidSss(sss);
    }
    
    /**
     * Check if PhilHealth is present and valid
     * @return true if PhilHealth is valid
     */
    public boolean hasValidPhilhealth() {
        return philhealth != null && !philhealth.trim().isEmpty() && isValidPhilhealth(philhealth);
    }
    
    /**
     * Check if TIN is present and valid
     * @return true if TIN is valid
     */
    public boolean hasValidTin() {
        return tin != null && !tin.trim().isEmpty() && isValidTin(tin);
    }
    
    /**
     * Check if Pag-IBIG is present and valid
     * @return true if Pag-IBIG is valid
     */
    public boolean hasValidPagibig() {
        return pagibig != null && !pagibig.trim().isEmpty() && isValidPagibig(pagibig);
    }
    
    /**
     * Get missing mandatory IDs for payroll processing
     * @return List of missing ID types
     */
    public List<String> getMissingMandatoryIds() {
        List<String> missing = new ArrayList<>();
        
        if (!hasValidSss()) {
            missing.add(ID_TYPE_SSS);
        }
        if (!hasValidPhilhealth()) {
            missing.add(ID_TYPE_PHILHEALTH);
        }
        if (!hasValidTin()) {
            missing.add(ID_TYPE_TIN);
        }
        if (!hasValidPagibig()) {
            missing.add(ID_TYPE_PAGIBIG);
        }
        
        return missing;
    }
    
    /**
     * Get invalid IDs that need correction
     * @return List of ID types that are present but invalid
     */
    public List<String> getInvalidIds() {
        List<String> invalid = new ArrayList<>();
        
        if (sss != null && !sss.trim().isEmpty() && !isValidSss(sss)) {
            invalid.add(ID_TYPE_SSS);
        }
        if (philhealth != null && !philhealth.trim().isEmpty() && !isValidPhilhealth(philhealth)) {
            invalid.add(ID_TYPE_PHILHEALTH);
        }
        if (tin != null && !tin.trim().isEmpty() && !isValidTin(tin)) {
            invalid.add(ID_TYPE_TIN);
        }
        if (pagibig != null && !pagibig.trim().isEmpty() && !isValidPagibig(pagibig)) {
            invalid.add(ID_TYPE_PAGIBIG);
        }
        
        return invalid;
    }
    
    /**
     * Get complete ID information as map
     * @return Map of ID type to ID number
     */
    public Map<String, String> getCompleteIdInfo() {
        Map<String, String> idInfo = new HashMap<>();
        
        idInfo.put(ID_TYPE_SSS, this.sss != null ? this.sss : "");
        idInfo.put(ID_TYPE_PHILHEALTH, this.philhealth != null ? this.philhealth : "");
        idInfo.put(ID_TYPE_TIN, this.tin != null ? this.tin : "");
        idInfo.put(ID_TYPE_PAGIBIG, this.pagibig != null ? this.pagibig : "");
        
        return idInfo;
    }
    
    /**
     * Get only valid IDs as map
     * @return Map of ID type to valid ID number
     */
    public Map<String, String> getValidIdInfo() {
        Map<String, String> validIds = new HashMap<>();
        
        if (hasValidSss()) {
            validIds.put(ID_TYPE_SSS, this.sss);
        }
        if (hasValidPhilhealth()) {
            validIds.put(ID_TYPE_PHILHEALTH, this.philhealth);
        }
        if (hasValidTin()) {
            validIds.put(ID_TYPE_TIN, this.tin);
        }
        if (hasValidPagibig()) {
            validIds.put(ID_TYPE_PAGIBIG, this.pagibig);
        }
        
        return validIds;
    }
    

    // ID FORMATTING METHODS

    
    /**
     * Format SSS number with proper separators
     * @param rawSss Raw SSS number (with or without separators)
     * @return Formatted SSS number (XX-XXXXXXX-X) or original if invalid
     */
    public static String formatSss(String rawSss) {
        if (rawSss == null) return null;
        
        String clean = rawSss.replaceAll("[^0-9]", "");
        if (clean.length() == 10) {
            return clean.substring(0, 2) + "-" + clean.substring(2, 9) + "-" + clean.substring(9);
        }
        return rawSss; // Return as-is if not valid length
    }
    
    /**
     * Format TIN number with proper separators
     * @param rawTin Raw TIN number (with or without separators)
     * @return Formatted TIN number (XXX-XXX-XXX-XXX) or original if invalid
     */
    public static String formatTin(String rawTin) {
        if (rawTin == null) return null;
        
        String clean = rawTin.replaceAll("[^0-9]", "");
        if (clean.length() == 12) {
            return clean.substring(0, 3) + "-" + clean.substring(3, 6) + 
                   "-" + clean.substring(6, 9) + "-" + clean.substring(9);
        }
        return rawTin; // Return as-is if not valid length
    }
    
    /**
     * Format PhilHealth number with proper separators
     * @param rawPhilhealth Raw PhilHealth number (with or without separators)
     * @return Formatted PhilHealth number (XX-XXXXXXXXX-X) or original if invalid
     */
    public static String formatPhilhealth(String rawPhilhealth) {
        if (rawPhilhealth == null) return null;
        
        String clean = rawPhilhealth.replaceAll("[^0-9]", "");
        if (clean.length() == 12) {
            return clean.substring(0, 2) + "-" + clean.substring(2, 11) + "-" + clean.substring(11);
        }
        return rawPhilhealth; // Return as-is if not valid length
    }
    
    /**
     * Format Pag-IBIG number with proper separators
     * @param rawPagibig Raw Pag-IBIG number (with or without separators)
     * @return Formatted Pag-IBIG number (XXXX-XXXX-XXXX) or original if invalid
     */
    public static String formatPagibig(String rawPagibig) {
        if (rawPagibig == null) return null;
        
        String clean = rawPagibig.replaceAll("[^0-9]", "");
        if (clean.length() == 12) {
            return clean.substring(0, 4) + "-" + clean.substring(4, 8) + "-" + clean.substring(8);
        }
        return rawPagibig; // Return as-is if not valid length
    }
    
    /**
     * Auto-format all IDs in this record
     * @return true if any ID was reformatted
     */
    public boolean autoFormatAllIds() {
        boolean formatted = false;
        
        if (sss != null && !sss.trim().isEmpty()) {
            String formatted_sss = formatSss(sss);
            if (!formatted_sss.equals(sss)) {
                this.sss = formatted_sss;
                formatted = true;
            }
        }
        
        if (philhealth != null && !philhealth.trim().isEmpty()) {
            String formatted_philhealth = formatPhilhealth(philhealth);
            if (!formatted_philhealth.equals(philhealth)) {
                this.philhealth = formatted_philhealth;
                formatted = true;
            }
        }
        
        if (tin != null && !tin.trim().isEmpty()) {
            String formatted_tin = formatTin(tin);
            if (!formatted_tin.equals(tin)) {
                this.tin = formatted_tin;
                formatted = true;
            }
        }
        
        if (pagibig != null && !pagibig.trim().isEmpty()) {
            String formatted_pagibig = formatPagibig(pagibig);
            if (!formatted_pagibig.equals(pagibig)) {
                this.pagibig = formatted_pagibig;
                formatted = true;
            }
        }
        
        return formatted;
    }
    

    // PAYROLL INTEGRATION METHODS
    
    /**
     * Check if employee is ready for payroll processing
     * (has all required government IDs)
     * @return true if ready for payroll
     */
    public boolean isReadyForPayroll() {
        return hasAllMandatoryIds();
    }
    
    /**
     * Get payroll status message
     * @return Status message for payroll processing
     */
    public String getPayrollStatus() {
        if (hasAllMandatoryIds()) {
            return "Ready for payroll processing";
        } else {
            List<String> missing = getMissingMandatoryIds();
            return "Missing government IDs: " + String.join(", ", missing);
        }
    }
    
    /**
     * Get SSS number for payroll deductions
     * @return SSS number or null if not available
     */
    public String getSssForPayroll() {
        return hasValidSss() ? sss : null;
    }
    
    /**
     * Get PhilHealth number for payroll deductions
     * @return PhilHealth number or null if not available
     */
    public String getPhilhealthForPayroll() {
        return hasValidPhilhealth() ? philhealth : null;
    }
    
    /**
     * Get TIN number for payroll deductions
     * @return TIN number or null if not available
     */
    public String getTinForPayroll() {
        return hasValidTin() ? tin : null;
    }
    
    /**
     * Get Pag-IBIG number for payroll deductions
     * @return Pag-IBIG number or null if not available
     */
    public String getPagibigForPayroll() {
        return hasValidPagibig() ? pagibig : null;
    }
    
    /**
     * Get all IDs formatted for payroll display
     * @return Formatted string with all IDs
     */
    public String getFormattedIdDisplay() {
        StringBuilder display = new StringBuilder();
        
        if (hasValidSss()) {
            display.append("SSS: ").append(sss).append("\n");
        }
        
        if (hasValidPhilhealth()) {
            display.append("PhilHealth: ").append(philhealth).append("\n");
        }
        
        if (hasValidTin()) {
            display.append("TIN: ").append(tin).append("\n");
        }
        
        if (hasValidPagibig()) {
            display.append("Pag-IBIG: ").append(pagibig).append("\n");
        }
        
        return display.toString().trim();
    }
    
    /**
     * Get compact display of all IDs
     * @return Compact string with all IDs
     */
    public String getCompactIdDisplay() {
        List<String> ids = new ArrayList<>();
        
        if (hasValidSss()) {
            ids.add("SSS: " + sss);
        }
        if (hasValidPhilhealth()) {
            ids.add("PhilHealth: " + philhealth);
        }
        if (hasValidTin()) {
            ids.add("TIN: " + tin);
        }
        if (hasValidPagibig()) {
            ids.add("Pag-IBIG: " + pagibig);
        }
        
        return String.join(" | ", ids);
    }
    
    /**
     * Check if record has at least one government ID
     * @return true if any ID is present
     */
    public boolean hasAnyId() {
        return (sss != null && !sss.trim().isEmpty()) ||
               (philhealth != null && !philhealth.trim().isEmpty()) ||
               (tin != null && !tin.trim().isEmpty()) ||
               (pagibig != null && !pagibig.trim().isEmpty());
    }
    
    /**
     * Check if record has any valid government ID
     * @return true if any ID is valid
     */
    public boolean hasAnyValidId() {
        return hasValidSss() || hasValidPhilhealth() || hasValidTin() || hasValidPagibig();
    }
    
    /**
     * Get completion percentage for government IDs
     * @return Percentage of required IDs that are present and valid
     */
    public double getCompletionPercentage() {
        int total = ALL_GOVERNMENT_IDS.length;
        int valid = 0;
        
        if (hasValidSss()) valid++;
        if (hasValidPhilhealth()) valid++;
        if (hasValidTin()) valid++;
        if (hasValidPagibig()) valid++;
        
        return (double) valid / total * 100.0;
    }
    
    /**
     * Get summary of government ID status
     * @return Summary string
     */
    public String getIdSummary() {
        int valid = 0;
        int total = ALL_GOVERNMENT_IDS.length;
        
        if (hasValidSss()) valid++;
        if (hasValidPhilhealth()) valid++;
        if (hasValidTin()) valid++;
        if (hasValidPagibig()) valid++;
        
        return String.format("Government IDs: %d/%d complete (%.1f%%)", 
                           valid, total, getCompletionPercentage());
    }
    
    @Override
    public String toString() {
        return String.format("GovIdModel{govId=%d, employeeId=%d, status='%s', completion=%.1f%%}", 
                           govId, employeeId, getPayrollStatus(), getCompletionPercentage());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        GovIdModel govIdObj = (GovIdModel) obj;
        return Objects.equals(govId, govIdObj.govId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(govId);
    }
    

    // VALIDATION RESULT CLASS
    
    /**
     * Validation result class for government ID validation
     */
    public static class ValidationResult {
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
        
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }
        
        public String getErrorsAsString() {
            return String.join("; ", errors);
        }
        
        public String getWarningsAsString() {
            return String.join("; ", warnings);
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ValidationResult{");
            sb.append("valid=").append(isValid());
            
            if (!errors.isEmpty()) {
                sb.append(", errors=").append(errors);
            }
            
            if (!warnings.isEmpty()) {
                sb.append(", warnings=").append(warnings);
            }
            
            sb.append("}");
            return sb.toString();
        }
    }
}