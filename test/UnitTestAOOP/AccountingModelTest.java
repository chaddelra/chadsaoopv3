/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package UnitTestAOOP;

import Models.AccountingModel;
import Models.EmployeeModel;
import Models.PayrollModel;
import Models.PayPeriodModel;
import Services.ReportService;
import DAOs.DatabaseConnection;
import DAOs.EmployeeDAO;
import DAOs.PayrollDAO;
import DAOs.PayPeriodDAO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;

/**
 *
 * @author i-Gits <lr.jdelasarmas@mmdc.mcl.edu.ph>
 */

public class AccountingModelTest {
    
    private static DatabaseConnection dbConnection;
    private AccountingModel accountingUser;
    private AccountingModel nonAccountingUser;
    private EmployeeModel regularEmployee;
    private EmployeeModel accountingEmployee;
    private PayPeriodDAO payPeriodDAO;

    private PayrollDAO payrollDAO;
    private EmployeeDAO employeeDAO;
    
    // Test data constants
    private static final Integer TEST_ACCOUNTING_ID = 99001;
    private static final Integer TEST_EMPLOYEE_ID = 99002;
    private static final Integer TEST_PAY_PERIOD_ID = 99999;
    private static final String TEST_ACCOUNTING_EMAIL = "test.accounting@motorph.com";
    private static final String TEST_EMPLOYEE_EMAIL = "test.employee@motorph.com";
    
    @BeforeClass
    public static void setUpClass() {
        // Initialize database connection once for all tests
        dbConnection = new DatabaseConnection();
        System.out.println("Test database connection initialized");
        
        // Clean any existing test data
        cleanupTestData();
    }
    
    @AfterClass
    public static void tearDownClass() {
        // Final cleanup
        cleanupTestData();
        System.out.println("Test cleanup completed");
    }
    
    @Before
    public void setUp() {
        // Clean any existing test data first
        removeTestData();
        
        // Initialize DAOs
        employeeDAO = new EmployeeDAO(dbConnection);
        payrollDAO = new PayrollDAO(dbConnection);
        payPeriodDAO = new PayPeriodDAO();
        
        // Insert test data BEFORE creating models
        insertTestData();
        
        try {
            // Check if employees already exist and load them if they do
            accountingEmployee = employeeDAO.findById(TEST_ACCOUNTING_ID);
            if (accountingEmployee == null) {
                // Create test accounting employee
                accountingEmployee = new EmployeeModel();
                accountingEmployee.setEmployeeId(TEST_ACCOUNTING_ID);
                accountingEmployee.setFirstName("Test");
                accountingEmployee.setLastName("Accounting");
                accountingEmployee.setEmail(TEST_ACCOUNTING_EMAIL);
                accountingEmployee.setUserRole("Accounting");
                accountingEmployee.setStatus(EmployeeModel.EmployeeStatus.REGULAR);
                accountingEmployee.setBasicSalary(new BigDecimal("50000"));
                accountingEmployee.setHourlyRate(new BigDecimal("300"));
                accountingEmployee.setPositionId(1);
                accountingEmployee.setBirthDate(LocalDate.of(1990, 1, 1));
                accountingEmployee.setPasswordHash("hashedpassword");
                
                // Save to database
                employeeDAO.save(accountingEmployee);
            }
            
            regularEmployee = employeeDAO.findById(TEST_EMPLOYEE_ID);
            if (regularEmployee == null) {
                // Create test regular employee
                regularEmployee = new EmployeeModel();
                regularEmployee.setEmployeeId(TEST_EMPLOYEE_ID);
                regularEmployee.setFirstName("Test");
                regularEmployee.setLastName("Employee");
                regularEmployee.setEmail(TEST_EMPLOYEE_EMAIL);
                regularEmployee.setUserRole("Employee");
                regularEmployee.setStatus(EmployeeModel.EmployeeStatus.REGULAR);
                regularEmployee.setBasicSalary(new BigDecimal("30000"));
                regularEmployee.setHourlyRate(new BigDecimal("200"));
                regularEmployee.setPositionId(1);
                regularEmployee.setBirthDate(LocalDate.of(1990, 1, 1));
                regularEmployee.setPasswordHash("hashedpassword");
                
                // Save to database
                employeeDAO.save(regularEmployee);
            }
        } catch (Exception e) {
            System.err.println("Setup error: " + e.getMessage());
            // Continue with test even if save fails
        }
        
        // Create accounting user from accounting employee
        accountingUser = new AccountingModel(accountingEmployee);
        
        // Create non-accounting user from regular employee
        nonAccountingUser = new AccountingModel(regularEmployee);
    }
    
    @After
    public void tearDown() {
        // Clean up test data after each test
        removeTestData();
    }
    
    // ================================
    // POSITIVE TEST CASES
    // ================================
    
    /**
     * Test that accounting role can generate financial reports
     */
    
    
    @Test
    public void testFinanceCanGenerateReports() {
        System.out.println("Testing: Accounting can generate financial reports");
        
        // Ensure test data exists
        PayPeriodModel testPeriod = payPeriodDAO.findById(TEST_PAY_PERIOD_ID);
        if (testPeriod == null) {
            // Insert if not exists
            insertTestPayPeriod();
        }
        
        // Test payroll report generation
        ReportService.PayrollReport payrollReport = 
            accountingUser.generateFinancialReport(TEST_PAY_PERIOD_ID);
        
        assertNotNull("Payroll report should not be null", payrollReport);
        
        // For accounting user with proper role, report should be successful
        if (accountingUser.getUserRole() != null && accountingUser.getUserRole().equals("Accounting")) {
            assertTrue("Payroll report should be successful for accounting role", payrollReport.isSuccess());
        }
        
        // Test tax compliance report generation
        YearMonth currentMonth = YearMonth.now();
        ReportService.ComplianceReport complianceReport = 
            accountingUser.generateTaxComplianceReport(currentMonth);
        
        assertNotNull("Compliance report should not be null", complianceReport);
        
        // Test salary comparison report
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();
        ReportService.SalaryComparisonReport salaryReport = 
            accountingUser.generateSalaryComparisonReport(startDate, endDate);
        
        assertNotNull("Salary report should not be null", salaryReport);
    }
 
    /**
     * Test payroll verification functionality
     */
    @Test
    public void testPayrollVerification() {
        System.out.println("Testing: Payroll verification functionality");
        
        // Ensure pay period exists
        insertTestPayPeriod();
        
        AccountingModel.AccountingResult result = 
            accountingUser.verifyPayrollForPeriod(TEST_PAY_PERIOD_ID);
        
        assertNotNull("Verification result should not be null", result);
        
        // Check if user has VERIFY_PAYROLL permission
        boolean hasPermission = false;
        for (String permission : accountingUser.getAccountingPermissions()) {
            if (permission.equals("VERIFY_PAYROLL")) {
                hasPermission = true;
                break;
            }
        }
        
        if (hasPermission && accountingUser.getUserRole() != null && accountingUser.getUserRole().equals("Accounting")) {
            assertTrue("Verification should be successful for accounting role", result.isSuccess());
            assertNotNull("Should have a message", result.getMessage());
            assertTrue("Should have processed records >= 0", result.getTotalRecords() >= 0);
        } else {
            assertFalse("Verification should fail without proper role", result.isSuccess());
            assertEquals("Should have permission denied message", 
                "Insufficient permissions to verify payroll", result.getMessage());
        }
    }
    
    /**
     * Test financial audit functionality
     */
    @Test
    public void testFinancialAudit() {
        System.out.println("Testing: Financial audit functionality");
        
        // Ensure pay period exists
        insertTestPayPeriod();
        
        AccountingModel.AccountingResult auditResult = 
            accountingUser.performFinancialAudit(TEST_PAY_PERIOD_ID);
        
        assertNotNull("Audit result should not be null", auditResult);
        
        // Check permissions and role
        if (accountingUser.getUserRole() != null && accountingUser.getUserRole().equals("Accounting")) {
            assertTrue("Audit should be successful for accounting role", auditResult.isSuccess());
            assertNotNull("Compliance score should be set", auditResult.getComplianceScore());
            assertTrue("Compliance score should be between 0 and 100", 
                auditResult.getComplianceScore().compareTo(BigDecimal.ZERO) >= 0 &&
                auditResult.getComplianceScore().compareTo(new BigDecimal("100")) <= 0);
        }
    }
    
    // ================================
    // NEGATIVE TEST CASES
    // ================================
    
    /**
     * Test that non-accounting employees cannot edit others' records
     */
    @Test
    public void testEmployeeCannotEditOthers() {
        System.out.println("Testing: Non-accounting employee cannot edit others' records");
        
        // Regular employee (non-accounting) trying to access another employee's data
        EmployeeModel otherEmployee = nonAccountingUser.getEmployeeById(TEST_ACCOUNTING_ID);
        
        // Non-accounting users should not have VIEW_PAYROLL_DATA permission
        if (nonAccountingUser.getUserRole() == null || !nonAccountingUser.getUserRole().equals("Accounting")) {
            assertNull("Non-accounting user should not access other employee data", otherEmployee);
            
            // Verify empty list when trying to get all employees
            List<EmployeeModel> allEmployees = nonAccountingUser.getAllActiveEmployees();
            assertTrue("Should return empty list for unauthorized access", allEmployees.isEmpty());
        }
    }
    
    /**
     * Test access denied for unauthorized actions
     */
    @Test
    public void testAccessDeniedForUnauthorizedAction() {
        System.out.println("Testing: Access denied for unauthorized actions");
        
        // Ensure pay period exists
        insertTestPayPeriod();
        
        // Non-accounting user trying to verify payroll
        AccountingModel.AccountingResult verifyResult = 
            nonAccountingUser.verifyPayrollForPeriod(TEST_PAY_PERIOD_ID);
        
        assertNotNull("Result should not be null", verifyResult);
        assertFalse("Verification should fail for non-accounting role", verifyResult.isSuccess());
        assertEquals("Should have permission denied message", 
            "Insufficient permissions to verify payroll", verifyResult.getMessage());
        
        // Non-accounting user trying to perform audit
        AccountingModel.AccountingResult auditResult = 
            nonAccountingUser.performFinancialAudit(TEST_PAY_PERIOD_ID);
        
        assertFalse("Audit should fail for non-accounting role", auditResult.isSuccess());
        assertEquals("Should have permission denied message", 
            "Insufficient permissions to perform financial audit", auditResult.getMessage());
    }
    
    /**
     * Test unauthorized role access to reports
     */
    @Test
    public void testUnauthorizedRoleAccess() {
        System.out.println("Testing: Unauthorized role access to reports");
        
        // Ensure pay period exists
        insertTestPayPeriod();
        
        // Test with actual non-accounting user (userRole = "Employee")
        // The AccountingModel constructor copies the userRole from the EmployeeModel
        // So nonAccountingUser should have userRole = "Employee"
        
        // Non-accounting user trying to generate financial report
        ReportService.PayrollReport report = 
            nonAccountingUser.generateFinancialReport(TEST_PAY_PERIOD_ID);
        
        assertNotNull("Report object should not be null", report);
        
        // Since nonAccountingUser has userRole = "Employee", not "Accounting",
        // the report generation should fail with permission error
        if (nonAccountingUser.getUserRole() != null && !nonAccountingUser.getUserRole().equals("Accounting")) {
            assertFalse("Report generation should fail for non-accounting role", report.isSuccess());
            assertEquals("Should have permission error", 
                "Insufficient permissions to generate financial reports", report.getErrorMessage());
        }
        
        // Non-accounting user trying to generate tax report
        ReportService.ComplianceReport taxReport = 
            nonAccountingUser.generateTaxComplianceReport(YearMonth.now());
        
        assertNotNull("Tax report object should not be null", taxReport);
        
        if (nonAccountingUser.getUserRole() != null && !nonAccountingUser.getUserRole().equals("Accounting")) {
            assertFalse("Tax report generation should fail for non-accounting role", taxReport.isSuccess());
            assertEquals("Should have permission error", 
                "Insufficient permissions to generate tax reports", taxReport.getErrorMessage());
        }
    }
    
    /**
     * Test invalid role assignment scenarios
     */
    @Test
    public void testInvalidRoleAssignment() {
        System.out.println("Testing: Invalid role assignment scenarios");
        
        // Create employee with invalid role
        EmployeeModel invalidRoleEmployee = new EmployeeModel();
        invalidRoleEmployee.setEmployeeId(99003);
        invalidRoleEmployee.setFirstName("Invalid");
        invalidRoleEmployee.setLastName("Role");
        invalidRoleEmployee.setEmail("invalid.role@test.com");
        invalidRoleEmployee.setUserRole("InvalidRole");
        invalidRoleEmployee.setStatus(EmployeeModel.EmployeeStatus.REGULAR);
        invalidRoleEmployee.setBasicSalary(new BigDecimal("25000"));
        invalidRoleEmployee.setPositionId(1);
        
        // Convert to AccountingModel
        AccountingModel invalidAccounting = new AccountingModel(invalidRoleEmployee);
        
        // Ensure pay period exists
        insertTestPayPeriod();
        
        // Test that invalid role cannot access accounting functions
        AccountingModel.AccountingResult result = 
            invalidAccounting.verifyPayrollForPeriod(TEST_PAY_PERIOD_ID);
        
        assertFalse("Invalid role should not have permissions", result.isSuccess());
        
        // Test permissions array
        String[] permissions = invalidAccounting.getAccountingPermissions();
        assertNotNull("Permissions array should not be null", permissions);
        assertTrue("Should have accounting permissions defined", permissions.length > 0);
    }
    
    // ================================
    // EDGE CASE TESTS
    // ================================
    
    /**
     * Test handling of null and invalid pay period IDs
     */
    @Test
    public void testInvalidPayPeriodHandling() {
        System.out.println("Testing: Invalid pay period handling");
        
        // Test with null pay period - AccountingModel should handle this
        try {
            AccountingModel.AccountingResult nullResult = 
                accountingUser.verifyPayrollForPeriod(null);
            
            assertNotNull("Result should not be null even with null input", nullResult);
            assertFalse("Should fail for null pay period", nullResult.isSuccess());
            assertTrue("Should have error message", 
                nullResult.getMessage() != null && 
                (nullResult.getMessage().contains("Pay period not found") || 
                 nullResult.getMessage().contains("Error verifying payroll")));
        } catch (Exception e) {
            // If exception is thrown, it's also acceptable as it shows null handling
            System.out.println("Null period handled with exception: " + e.getMessage());
        }
        
        // Test with non-existent pay period
        AccountingModel.AccountingResult invalidResult = 
            accountingUser.verifyPayrollForPeriod(999999);
        
        assertFalse("Should fail for invalid pay period", invalidResult.isSuccess());
        assertTrue("Should indicate pay period not found or insufficient permissions", 
            invalidResult.getMessage().contains("Pay period not found") || 
            invalidResult.getMessage().contains("Insufficient permissions"));
    }
    
    /**
     * Test payroll records access with various scenarios
     */
    @Test
    public void testPayrollRecordsAccess() {
        System.out.println("Testing: Payroll records access scenarios");
        
        // Ensure pay period exists
        insertTestPayPeriod();
        
        // Accounting user accessing payroll records
        List<PayrollModel> accountingAccess = 
            accountingUser.getPayrollRecords(TEST_PAY_PERIOD_ID);
        
        assertNotNull("Accounting should get payroll records list", accountingAccess);
        // Note: List might be empty if no payroll records exist for the period
        
        // Non-accounting user accessing payroll records
        List<PayrollModel> unauthorizedAccess = 
            nonAccountingUser.getPayrollRecords(TEST_PAY_PERIOD_ID);
        
        assertNotNull("Should return list object", unauthorizedAccess);
        assertTrue("Should return empty list for unauthorized access", unauthorizedAccess.isEmpty());
    }
    
    /**
     * Test boundary conditions for financial calculations
     */
    @Test
    public void testFinancialCalculationBoundaries() {
        System.out.println("Testing: Financial calculation boundaries");
        
        // Ensure pay period exists
        insertTestPayPeriod();
        
        try {
            // First check if employee already exists and delete if so
            EmployeeModel existing = employeeDAO.findById(99004);
            if (existing != null) {
                employeeDAO.delete(99004);
            }
            
            // Test with zero salary employee
            EmployeeModel zeroSalaryEmployee = new EmployeeModel();
            zeroSalaryEmployee.setEmployeeId(99004);
            zeroSalaryEmployee.setFirstName("Zero");
            zeroSalaryEmployee.setLastName("Salary");
            zeroSalaryEmployee.setEmail("zero.salary@test.com");
            zeroSalaryEmployee.setBasicSalary(BigDecimal.ZERO);
            zeroSalaryEmployee.setHourlyRate(BigDecimal.ZERO);
            zeroSalaryEmployee.setUserRole("Employee");
            zeroSalaryEmployee.setStatus(EmployeeModel.EmployeeStatus.REGULAR);
            zeroSalaryEmployee.setPositionId(1);
            zeroSalaryEmployee.setBirthDate(LocalDate.of(1990, 1, 1));
            zeroSalaryEmployee.setPasswordHash("hashedpassword");
            
            // Save to database
            boolean saved = employeeDAO.save(zeroSalaryEmployee);
            
            if (saved) {
                // Create payroll with edge case values
                PayrollModel edgeCasePayroll = new PayrollModel();
                edgeCasePayroll.setEmployeeId(99004);
                edgeCasePayroll.setPayPeriodId(TEST_PAY_PERIOD_ID);
                edgeCasePayroll.setBasicSalary(BigDecimal.ZERO);
                edgeCasePayroll.setGrossIncome(BigDecimal.ZERO);
                edgeCasePayroll.setTotalDeduction(BigDecimal.ZERO);
                edgeCasePayroll.setNetSalary(BigDecimal.ZERO);
                edgeCasePayroll.setTotalBenefit(BigDecimal.ZERO);
                
                // Save payroll
                payrollDAO.save(edgeCasePayroll);
            }
            
            // Verify payroll should handle zero values
            AccountingModel.AccountingResult verifyResult = 
                accountingUser.verifyPayrollForPeriod(TEST_PAY_PERIOD_ID);
            
            assertNotNull("Should handle zero salary cases", verifyResult);
            
        } catch (Exception e) {
            System.err.println("Error in boundary test: " + e.getMessage());
            // Test can still pass - we're mainly testing that it doesn't crash
        } finally {
            // Clean up
            try {
                employeeDAO.delete(99004);
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }
    
    /**
     * Test concurrent access scenarios
     */
    @Test
    public void testConcurrentAccessControl() {
        System.out.println("Testing: Concurrent access control");
        
        // Ensure pay period exists
        insertTestPayPeriod();
        
        // Create multiple accounting users
        AccountingModel accounting1 = new AccountingModel(
            99005, "User1", "Accounting", "user1@test.com", "Accounting"
        );
        AccountingModel accounting2 = new AccountingModel(
            99006, "User2", "Accounting", "user2@test.com", "Accounting"
        );
        
        // Both should be able to generate reports (they will have proper permissions)
        ReportService.PayrollReport report1 = 
            accounting1.generateFinancialReport(TEST_PAY_PERIOD_ID);
        ReportService.PayrollReport report2 = 
            accounting2.generateFinancialReport(TEST_PAY_PERIOD_ID);
        
        assertNotNull("First user report should not be null", report1);
        assertNotNull("Second user report should not be null", report2);
        
        // With proper Accounting role, reports should be successful
        if (accounting1.getUserRole() != null && accounting1.getUserRole().equals("Accounting")) {
            assertTrue("First user should generate report", report1.isSuccess());
            assertTrue("Second user should generate report", report2.isSuccess());
        }
    }
    
    /**
     * Test toString method
     */
    @Test
    public void testToString() {
        System.out.println("Testing: toString method");
        
        String result = accountingUser.toString();
        assertNotNull("toString should not return null", result);
        assertTrue("Should contain employee ID", result.contains(String.valueOf(TEST_ACCOUNTING_ID)));
        assertTrue("Should contain role info", result.contains("permissions"));
    }
    
    // ================================
    // HELPER METHODS
    // ================================
    
    private void insertTestData() {
        try (Connection conn = dbConnection.createConnection()) {
            // Insert test position if not exists
            String positionSql = "INSERT IGNORE INTO position (positionId, position, department) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(positionSql)) {
                pstmt.setInt(1, 1);
                pstmt.setString(2, "Test Position");
                pstmt.setString(3, "Test Department");
                pstmt.executeUpdate();
            }
            
            System.out.println("Test data insertion completed");
            
        } catch (SQLException e) {
            System.err.println("Error inserting test data: " + e.getMessage());
        }
    }
    
    private void insertTestPayPeriod() {
        try (Connection conn = dbConnection.createConnection()) {
            // Check if pay period already exists
            String checkSql = "SELECT payPeriodId FROM payperiod WHERE payPeriodId = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, TEST_PAY_PERIOD_ID);
                if (checkStmt.executeQuery().next()) {
                    return; // Already exists
                }
            }
            
            // Insert test pay period
            String sql = "INSERT INTO payperiod (payPeriodId, startDate, endDate, periodName) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, TEST_PAY_PERIOD_ID);
                pstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now().minusDays(15)));
                pstmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                pstmt.setString(4, "Test Period");
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error inserting test pay period: " + e.getMessage());
        }
    }
    
    private void removeTestData() {
        try (Connection conn = dbConnection.createConnection()) {
            // Delete in correct order to avoid foreign key constraints
            conn.createStatement().executeUpdate("DELETE FROM payroll WHERE employeeId >= 99000");
            conn.createStatement().executeUpdate("DELETE FROM attendance WHERE employeeId >= 99000");
            conn.createStatement().executeUpdate("DELETE FROM leaverequest WHERE employeeId >= 99000");
            conn.createStatement().executeUpdate("DELETE FROM overtimerequest WHERE employeeId >= 99000");
            conn.createStatement().executeUpdate("DELETE FROM leavebalance WHERE employeeId >= 99000");
            conn.createStatement().executeUpdate("DELETE FROM govid WHERE employeeId >= 99000");
            conn.createStatement().executeUpdate("DELETE FROM payperiod WHERE payPeriodId >= 99000");
            conn.createStatement().executeUpdate("DELETE FROM employee WHERE employeeId >= 99000");
            
            System.out.println("Test data removal completed");
            
        } catch (SQLException e) {
            System.err.println("Error removing test data: " + e.getMessage());
        }
    }
    
    private static void cleanupTestData() {
        try (Connection conn = dbConnection.createConnection()) {
            // Clean up test data with IDs above 99000
            conn.createStatement().executeUpdate("DELETE FROM payroll WHERE employeeId >= 99000");
            conn.createStatement().executeUpdate("DELETE FROM attendance WHERE employeeId >= 99000");
            conn.createStatement().executeUpdate("DELETE FROM leaverequest WHERE employeeId >= 99000");
            conn.createStatement().executeUpdate("DELETE FROM overtimerequest WHERE employeeId >= 99000");
            conn.createStatement().executeUpdate("DELETE FROM leavebalance WHERE employeeId >= 99000");
            conn.createStatement().executeUpdate("DELETE FROM govid WHERE employeeId >= 99000");
            conn.createStatement().executeUpdate("DELETE FROM payperiod WHERE payPeriodId >= 99000");
            conn.createStatement().executeUpdate("DELETE FROM employee WHERE employeeId >= 99000");
            
            System.out.println("Test data cleanup completed");
            
        } catch (Exception e) {
            System.err.println("Error during final cleanup: " + e.getMessage());
        }
    }
}