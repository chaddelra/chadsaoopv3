package Models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.ArrayList;

import Services.PayrollService;
import Services.ReportService;
import Services.AttendanceService;
import DAOs.EmployeeDAO;
import DAOs.PayrollDAO;
import DAOs.PayPeriodDAO;
import DAOs.DatabaseConnection;

public class AccountingModel extends EmployeeModel {
    
    // Service layer dependencies
    private final PayrollService payrollService;
    private final ReportService reportService;
    private final AttendanceService attendanceService;
    
    // DAO dependencies for financial operations
    private final EmployeeDAO employeeDAO;
    private final PayrollDAO payrollDAO;
    private final PayPeriodDAO payPeriodDAO;
    
    // Accounting Role Permissions
    private static final String[] ACCOUNTING_PERMISSIONS = {
        "VERIFY_PAYROLL", "VIEW_PAYROLL_DATA", "GENERATE_FINANCIAL_REPORTS", 
        "AUDIT_FINANCIAL_DATA", "MANAGE_TAX_CALCULATIONS"
    };

    /**
     * Constructor for Accounting role
     */
    public AccountingModel(int employeeId, String firstName, String lastName, String email, String userRole) {
        // Call the parent EmployeeModel constructor with proper parameters
        super(employeeId, firstName, lastName, email, userRole);
        
        // Initialize services with database connection
        DatabaseConnection dbConnection = new DatabaseConnection();
        this.payrollService = new PayrollService();
        this.reportService = new ReportService();
        this.attendanceService = new AttendanceService();
        
        // Initialize DAOs
        this.employeeDAO = new EmployeeDAO();
        this.payrollDAO = new PayrollDAO();
        this.payPeriodDAO = new PayPeriodDAO();
        
        System.out.println("Accounting user initialized: " + getFullName());
    }

    /**
     * Constructor from existing EmployeeModel
     */
    public AccountingModel(EmployeeModel employee) {
        // Call parent constructor with employee data
        super(employee.getEmployeeId(), employee.getFirstName(), employee.getLastName(), 
              employee.getEmail(), employee.getUserRole());
        
        // Copy additional fields from the source employee
        this.copyFromEmployeeModel(employee);
        
        // Initialize Accounting-specific components
        this.payrollService = new PayrollService();
        this.reportService = new ReportService();
        this.attendanceService = new AttendanceService();
        
        this.employeeDAO = new EmployeeDAO();
        this.payrollDAO = new PayrollDAO();
        this.payPeriodDAO = new PayPeriodDAO();
        
        System.out.println("Accounting user initialized from EmployeeModel: " + getFullName());
    }

    /**
     * Helper method to copy data from another EmployeeModel
     */
    private void copyFromEmployeeModel(EmployeeModel source) {
        if (source.getStatus() != null) this.setStatus(source.getStatus());
        if (source.getBasicSalary() != null) this.setBasicSalary(source.getBasicSalary());
        if (source.getBirthDate() != null) this.setBirthDate(source.getBirthDate());
        if (source.getPositionId() != null) this.setPositionId(source.getPositionId());
        // Add more fields as needed based on your EmployeeModel
    }

    // ================================
    // PAYROLL VERIFICATION OPERATIONS
    // ================================

    /**
     * Verifies payroll calculations for a specific pay period
     */
    public AccountingResult verifyPayrollForPeriod(Integer payPeriodId) {
        AccountingResult result = new AccountingResult();
        
        try {
            if (!hasPermission("VERIFY_PAYROLL")) {
                result.setSuccess(false);
                result.setMessage("Insufficient permissions to verify payroll");
                return result;
            }

            PayPeriodModel payPeriod = payPeriodDAO.findById(payPeriodId);
            if (payPeriod == null) {
                result.setSuccess(false);
                result.setMessage("Pay period not found: " + payPeriodId);
                return result;
            }

            List<PayrollModel> payrollRecords = payrollDAO.findByPayPeriod(payPeriodId);
            int verifiedCount = 0;
            int discrepancyCount = 0;
            BigDecimal totalGross = BigDecimal.ZERO;
            BigDecimal totalNet = BigDecimal.ZERO;
            BigDecimal totalDeductions = BigDecimal.ZERO;

            for (PayrollModel payroll : payrollRecords) {
                if (verifyIndividualPayroll(payroll)) {
                    verifiedCount++;
                } else {
                    discrepancyCount++;
                }
                totalGross = totalGross.add(payroll.getGrossIncome());
                totalNet = totalNet.add(payroll.getNetSalary());
                totalDeductions = totalDeductions.add(payroll.getTotalDeduction());
            }

            result.setSuccess(true);
            result.setMessage("Payroll verification completed for period: " + payPeriod.getPeriodName());
            result.setTotalRecords(payrollRecords.size());
            result.setVerifiedRecords(verifiedCount);
            result.setDiscrepancyRecords(discrepancyCount);
            result.setTotalGross(totalGross);
            result.setTotalNet(totalNet);
            result.setTotalDeductions(totalDeductions);
            
            logAccountingActivity("PAYROLL_VERIFIED", 
                "Verified payroll for period: " + payPeriodId + 
                " - Records: " + payrollRecords.size() + 
                ", Discrepancies: " + discrepancyCount);

        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Error verifying payroll: " + e.getMessage());
            System.err.println("Accounting error verifying payroll: " + e.getMessage());
        }

        return result;
    }

    /**
     * Verifies individual payroll calculation
     */
    private boolean verifyIndividualPayroll(PayrollModel payroll) {
        try {
            EmployeeModel employee = employeeDAO.findById(payroll.getEmployeeId());
            if (employee == null) {
                System.out.println("Employee not found for payroll: " + payroll.getEmployeeId());
                return false;
            }

            // Verify basic salary calculation (semi-monthly)
            BigDecimal expectedBasicSalary = employee.getBasicSalary().divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
            if (!payroll.getBasicSalary().equals(expectedBasicSalary)) {
                System.out.println("Basic salary mismatch for employee: " + payroll.getEmployeeId() + 
                    " - Expected: " + expectedBasicSalary + ", Found: " + payroll.getBasicSalary());
                return false;
            }

            // Verify net salary calculation
            BigDecimal expectedNet = payroll.getGrossIncome().subtract(payroll.getTotalDeduction());
            if (!payroll.getNetSalary().equals(expectedNet)) {
                System.out.println("Net salary calculation mismatch for employee: " + payroll.getEmployeeId() + 
                    " - Expected: " + expectedNet + ", Found: " + payroll.getNetSalary());
                return false;
            }

            // Verify deduction calculations (basic government deductions)
            BigDecimal expectedDeductions = calculateExpectedDeductions(employee.getBasicSalary());
            BigDecimal deductionDifference = payroll.getTotalDeduction().subtract(expectedDeductions).abs();
            
            // Allow small tolerance for rounding differences
            if (deductionDifference.compareTo(new BigDecimal("1.00")) > 0) {
                System.out.println("Deduction calculation mismatch for employee: " + payroll.getEmployeeId() + 
                    " - Expected: " + expectedDeductions + ", Found: " + payroll.getTotalDeduction());
                return false;
            }

            return true;

        } catch (Exception e) {
            System.err.println("Error verifying individual payroll for employee " + payroll.getEmployeeId() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Calculates expected government deductions for verification
     */
    private BigDecimal calculateExpectedDeductions(BigDecimal basicSalary) {
        // Philippine government mandated deductions (simplified)
        BigDecimal sss = basicSalary.multiply(new BigDecimal("0.045")).setScale(2, RoundingMode.HALF_UP); // 4.5% SSS
        BigDecimal philHealth = basicSalary.multiply(new BigDecimal("0.0275")).setScale(2, RoundingMode.HALF_UP); // 2.75% PhilHealth
        BigDecimal pagIbig = basicSalary.multiply(new BigDecimal("0.02")).setScale(2, RoundingMode.HALF_UP); // 2% Pag-IBIG
        
        return sss.add(philHealth).add(pagIbig);
    }

    // ================================
    // FINANCIAL REPORTING OPERATIONS
    // ================================

    /**
     * Generates comprehensive financial report using ReportService
     */
    public ReportResult generateFinancialReport(Integer payPeriodId) {
        if (!hasPermission("GENERATE_FINANCIAL_REPORTS")) {
            ReportResult report = new ReportResult();
            report.setSuccess(false);
            report.setErrorMessage("Insufficient permissions to generate financial reports");
            return report;
        }
        
        ReportResult report = reportService.generatePayrollReport(payPeriodId);
        
        if (report.isSuccess()) {
            logAccountingActivity("FINANCIAL_REPORT_GENERATED", 
                "Generated financial report for period: " + payPeriodId);
        }
        
        return report;
    }

    /**
     * Generates tax compliance report using ReportService
     */
    public ReportResult generateTaxComplianceReport(YearMonth yearMonth) {
        if (!hasPermission("GENERATE_FINANCIAL_REPORTS")) {
            ReportResult report = new ReportResult();
            report.setSuccess(false);
            report.setErrorMessage("Insufficient permissions to generate tax reports");
            return report;
        }
        
        ReportResult report = reportService.generateComplianceReport(yearMonth);
        
        if (report.isSuccess()) {
            logAccountingActivity("TAX_REPORT_GENERATED", 
                "Generated tax compliance report for: " + yearMonth);
        }
        
        return report;
    }

    /**
     * Generates salary comparison report using ReportService
     */
    public ReportResult generateSalaryComparisonReport(LocalDate startDate, LocalDate endDate) {
        if (!hasPermission("GENERATE_FINANCIAL_REPORTS")) {
            ReportResult report = new ReportResult();
            report.setSuccess(false);
            report.setErrorMessage("Insufficient permissions to generate salary reports");
            return report;
        }
        
        return reportService.generateSalaryComparisonReport(startDate, endDate);
    }

    // ================================
    // AUDIT OPERATIONS
    // ================================

    /**
     * Performs financial audit for a pay period
     */
    public AccountingResult performFinancialAudit(Integer payPeriodId) {
        AccountingResult result = new AccountingResult();
        
        try {
            if (!hasPermission("AUDIT_FINANCIAL_DATA")) {
                result.setSuccess(false);
                result.setMessage("Insufficient permissions to perform financial audit");
                return result;
            }

            // Get payroll verification results
            AccountingResult verificationResult = verifyPayrollForPeriod(payPeriodId);
            
            if (!verificationResult.isSuccess()) {
                result.setSuccess(false);
                result.setMessage("Failed to verify payroll during audit");
                return result;
            }

            // Calculate compliance score
            double complianceScore = verificationResult.getTotalRecords() > 0 ? 
                (double)(verificationResult.getVerifiedRecords()) / verificationResult.getTotalRecords() * 100 : 100;

            result.setSuccess(true);
            result.setMessage("Financial audit completed for period: " + payPeriodId);
            result.setTotalRecords(verificationResult.getTotalRecords());
            result.setVerifiedRecords(verificationResult.getVerifiedRecords());
            result.setDiscrepancyRecords(verificationResult.getDiscrepancyRecords());
            result.setTotalGross(verificationResult.getTotalGross());
            result.setTotalNet(verificationResult.getTotalNet());
            result.setTotalDeductions(verificationResult.getTotalDeductions());
            result.setComplianceScore(BigDecimal.valueOf(complianceScore));
            
            logAccountingActivity("FINANCIAL_AUDIT_PERFORMED", 
                "Completed financial audit for period: " + payPeriodId + 
                " - Compliance: " + String.format("%.2f%%", complianceScore));

        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Error performing financial audit: " + e.getMessage());
        }

        return result;
    }

    // ================================
    // DATA ACCESS METHODS
    // ================================

    /**
     * Gets all payroll records for a period (with permission check)
     */
    public List<PayrollModel> getPayrollRecords(Integer payPeriodId) {
        if (!hasPermission("VIEW_PAYROLL_DATA")) {
            System.err.println("Accounting: Insufficient permissions to view payroll records");
            return new ArrayList<>();
        }
        
        return payrollDAO.findByPayPeriod(payPeriodId);
    }

    /**
     * Gets employee information (with permission check)
     */
    public EmployeeModel getEmployeeById(Integer employeeId) {
        if (!hasPermission("VIEW_PAYROLL_DATA")) {
            System.err.println("Accounting: Insufficient permissions to view employee data");
            return null;
        }
        
        return employeeDAO.findById(employeeId);
    }

    /**
     * Gets all active employees (with permission check)
     */
    public List<EmployeeModel> getAllActiveEmployees() {
        if (!hasPermission("VIEW_PAYROLL_DATA")) {
            System.err.println("Accounting: Insufficient permissions to view employee data");
            return new ArrayList<>();
        }
        
        return employeeDAO.getActiveEmployees();
    }

    // ================================
    // UTILITY METHODS
    // ================================

    /**
     * Checks if Accounting user has specific permission
     */
    private boolean hasPermission(String permission) {
        // First check if the user has the Accounting role
        String userRole = getUserRole();
        if (userRole == null || !userRole.equalsIgnoreCase("Accounting")) {
            return false; // Non-accounting users have no accounting permissions
        }
        
        // If user has Accounting role, check if the permission is in the list
        for (String accountingPermission : ACCOUNTING_PERMISSIONS) {
            if (accountingPermission.equals(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets all Accounting permissions
     */
    public String[] getAccountingPermissions() {
        return ACCOUNTING_PERMISSIONS.clone();
    }

    /**
     * Logs Accounting activities for audit purposes
     */
    private void logAccountingActivity(String action, String details) {
        try {
            String logMessage = String.format("[ACCOUNTING AUDIT] %s - %s: %s (Performed by: %s - ID: %d)",
                LocalDate.now(), action, details, getFullName(), getEmployeeId());
            System.out.println(logMessage);
            
        } catch (Exception e) {
            System.err.println("Error logging Accounting activity: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "AccountingModel{" +
                "employeeId=" + getEmployeeId() +
                ", name='" + getFullName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", permissions=" + java.util.Arrays.toString(ACCOUNTING_PERMISSIONS) +
                '}';
    }

    // ================================
    // INNER CLASSES - RESULT OBJECTS
    // ================================

    /**
     * Result class for Accounting operations
     */
    public static class AccountingResult {
        private boolean success = false;
        private String message = "";
        private int totalRecords = 0;
        private int verifiedRecords = 0;
        private int discrepancyRecords = 0;
        private BigDecimal totalGross = BigDecimal.ZERO;
        private BigDecimal totalNet = BigDecimal.ZERO;
        private BigDecimal totalDeductions = BigDecimal.ZERO;
        private BigDecimal complianceScore = BigDecimal.ZERO;

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public int getTotalRecords() { return totalRecords; }
        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
        
        public int getVerifiedRecords() { return verifiedRecords; }
        public void setVerifiedRecords(int verifiedRecords) { this.verifiedRecords = verifiedRecords; }
        
        public int getDiscrepancyRecords() { return discrepancyRecords; }
        public void setDiscrepancyRecords(int discrepancyRecords) { this.discrepancyRecords = discrepancyRecords; }
        
        public BigDecimal getTotalGross() { return totalGross; }
        public void setTotalGross(BigDecimal totalGross) { this.totalGross = totalGross; }
        
        public BigDecimal getTotalNet() { return totalNet; }
        public void setTotalNet(BigDecimal totalNet) { this.totalNet = totalNet; }
        
        public BigDecimal getTotalDeductions() { return totalDeductions; }
        public void setTotalDeductions(BigDecimal totalDeductions) { this.totalDeductions = totalDeductions; }
        
        public BigDecimal getComplianceScore() { return complianceScore; }
        public void setComplianceScore(BigDecimal complianceScore) { this.complianceScore = complianceScore; }

        @Override
        public String toString() {
            return "AccountingResult{" +
                   "success=" + success + 
                   ", message='" + message + '\'' +
                   ", totalRecords=" + totalRecords + 
                   ", verifiedRecords=" + verifiedRecords + 
                   ", discrepancyRecords=" + discrepancyRecords + 
                   ", complianceScore=" + complianceScore + "%" +
                   '}';
        }
    }

    /**
     * Generic result class for reports
     */
    public static class ReportResult {
        private boolean success = false;
        private String errorMessage = "";
        private String reportContent = "";
        private String filePath = "";

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public String getReportContent() { return reportContent; }
        public void setReportContent(String reportContent) { this.reportContent = reportContent; }
        
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
    }
}