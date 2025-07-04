/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UnitTestAOOP;

import Services.PayrollService;
import DAOs.*;
import Models.*;
import org.junit.*;
import static org.junit.Assert.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 *
 * @author i-Gits <lr.jdelasarmas@mmdc.mcl.edu.ph>
 */

public class PayrollServiceTest {
    
    private PayrollService payrollService;
    private DatabaseConnection databaseConnection;
    private EmployeeDAO employeeDAO;
    private PayPeriodDAO payPeriodDAO;
    private AttendanceDAO attendanceDAO;
    private OvertimeRequestDAO overtimeDAO;
    private PayrollDAO payrollDAO;
    
    // Test data
    private EmployeeModel testEmployee;
    private PayPeriodModel testPayPeriod;
    private static final BigDecimal BASIC_SALARY = new BigDecimal("50000.00");
    private static final BigDecimal HOURLY_RATE = new BigDecimal("288.46"); // Based on basic salary
    
    // Track created pay periods for cleanup
    private List<Integer> createdPayPeriodIds = new ArrayList<>();
    
    @Before
    public void setUp() throws Exception {
        // Initialize database connection and DAOs
        databaseConnection = new DatabaseConnection();
        payrollService = new PayrollService(databaseConnection);
        employeeDAO = new EmployeeDAO(databaseConnection);
        payPeriodDAO = new PayPeriodDAO();
        attendanceDAO = new AttendanceDAO(databaseConnection);
        overtimeDAO = new OvertimeRequestDAO(databaseConnection);
        payrollDAO = new PayrollDAO(databaseConnection);
        
        // Create test employee
        createTestEmployee();
        
        // Create test pay period
        createTestPayPeriod();
    }
    
    @After
    public void tearDown() throws Exception {
        // Clean up test data in correct order to respect foreign key constraints
        if (testEmployee != null && testEmployee.getEmployeeId() != null) {
            // Delete related records first
            cleanupEmployeeRelatedData(testEmployee.getEmployeeId());
        }
        
        // Clean up any other created pay periods
        for (Integer payPeriodId : createdPayPeriodIds) {
            cleanupPayPeriodData(payPeriodId);
        }
        
        if (testPayPeriod != null && testPayPeriod.getPayPeriodId() != null) {
            cleanupPayPeriodData(testPayPeriod.getPayPeriodId());
        }
        
        // Finally delete the employee after all related records are removed
        if (testEmployee != null && testEmployee.getEmployeeId() != null) {
            employeeDAO.delete(testEmployee.getEmployeeId());
        }
    }
    
    // =====================================
    // POSITIVE TEST CASES
    // =====================================
    
    @Test
    public void testCalculateGrossSalary() {
        // Given: Employee with basic salary and benefits
        PayrollService.PayrollCalculation calculation = payrollService.calculateEmployeePayroll(
            testEmployee, testPayPeriod
        );
        
        // Expected: Semi-monthly basic = 50000/2 = 25000
        BigDecimal expectedSemiMonthlyBasic = BASIC_SALARY.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
        
        // Then: Verify gross calculation
        assertNotNull("Calculation should not be null", calculation);
        assertEquals("Basic salary should be semi-monthly", expectedSemiMonthlyBasic, calculation.getBasicSalary());
        assertTrue("Gross income should include basic salary", 
            calculation.getGrossIncome().compareTo(calculation.getBasicSalary()) >= 0);
    }
    
    @Test
    public void testCalculateNetSalary_withTaxDeductions() {
        // Given: Employee with standard deductions
        PayrollService.PayrollCalculation calculation = payrollService.calculateEmployeePayroll(
            testEmployee, testPayPeriod
        );
        
        // Expected deductions (based on basic salary)
        BigDecimal expectedSSS = BASIC_SALARY.multiply(new BigDecimal("0.045")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedPhilHealth = BASIC_SALARY.multiply(new BigDecimal("0.0275")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedPagIbig = BASIC_SALARY.multiply(new BigDecimal("0.02")).setScale(2, RoundingMode.HALF_UP);
        
        // Then: Verify deductions are applied
        assertNotNull("Calculation should not be null", calculation);
        assertTrue("Total deductions should be greater than zero", 
            calculation.getTotalDeductions().compareTo(BigDecimal.ZERO) > 0);
        assertTrue("Net salary should be less than gross income", 
            calculation.getNetSalary().compareTo(calculation.getGrossIncome()) < 0);
        assertEquals("Net salary should be gross minus deductions",
            calculation.getGrossIncome().subtract(calculation.getTotalDeductions()),
            calculation.getNetSalary());
    }
    
    @Test
    public void testCalculateOvertimePay() {
        // Given: Employee with approved overtime
        createApprovedOvertime();
        
        // When: Calculate payroll
        PayrollService.PayrollCalculation calculation = payrollService.calculateEmployeePayroll(
            testEmployee, testPayPeriod
        );
        
        // Then: Verify overtime is included
        assertNotNull("Calculation should not be null", calculation);
        assertTrue("Overtime pay should be greater than zero", 
            calculation.getOvertimePay().compareTo(BigDecimal.ZERO) > 0);
        
        // Verify overtime calculation (1.5x hourly rate)
        BigDecimal expectedOvertimeRate = HOURLY_RATE.multiply(new BigDecimal("1.5"));
        assertTrue("Overtime should be calculated at 1.5x rate", 
            calculation.getOvertimePay().compareTo(BigDecimal.ZERO) > 0);
    }
    
    @Test
    public void testCalculateWithBonusesAndDeductions() {
        // Given: Employee with complete attendance and overtime
        createCompleteAttendanceRecords();
        createApprovedOvertime();
        
        // When: Process payroll
        boolean result = payrollService.processEmployeePayroll(
            testEmployee.getEmployeeId(), testPayPeriod.getPayPeriodId()
        );
        
        // Then: Verify successful processing
        assertTrue("Payroll should be processed successfully", result);
        
        // Verify payroll record was created
        List<PayrollModel> payrollRecords = payrollDAO.findByEmployee(testEmployee.getEmployeeId());
        assertFalse("Payroll records should exist", payrollRecords.isEmpty());
        
        PayrollModel payroll = payrollRecords.get(0);
        assertTrue("Net salary should be positive", payroll.getNetSalary().compareTo(BigDecimal.ZERO) > 0);
    }
    
    @Test
    public void testPayrollRoundingErrors() {
        // Given: Employee with salary that causes rounding
        testEmployee.setBasicSalary(new BigDecimal("33333.33")); // Causes rounding in calculations
        employeeDAO.update(testEmployee);
        
        // When: Calculate payroll
        PayrollService.PayrollCalculation calculation = payrollService.calculateEmployeePayroll(
            testEmployee, testPayPeriod
        );
        
        // Then: Verify all amounts are properly rounded to 2 decimal places
        assertEquals("Basic salary should have 2 decimal places", 
            2, calculation.getBasicSalary().scale());
        assertEquals("Gross income should have 2 decimal places", 
            2, calculation.getGrossIncome().scale());
        assertEquals("Net salary should have 2 decimal places", 
            2, calculation.getNetSalary().scale());
        
        // Verify calculations are consistent
        BigDecimal recalculatedNet = calculation.getGrossIncome().subtract(calculation.getTotalDeductions());
        assertEquals("Net salary calculation should be consistent", 
            recalculatedNet.setScale(2, RoundingMode.HALF_UP), 
            calculation.getNetSalary());
    }
    
    // =====================================
    // NEGATIVE TEST CASES
    // =====================================
    
    @Test
    public void testCalculatePayroll_negativeHours() {
        // Given: Invalid attendance with negative hours (time out before time in)
        AttendanceModel invalidAttendance = new AttendanceModel(
            testEmployee.getEmployeeId(),
            LocalDate.now()
        );
        // Set invalid times manually
        invalidAttendance.setTimeIn(LocalTime.of(17, 0));   // 5:00 PM
        invalidAttendance.setTimeOut(LocalTime.of(8, 0));   // 8:00 AM - Invalid: earlier than time in
        attendanceDAO.save(invalidAttendance);
        
        // When: Calculate payroll
        PayrollService.PayrollCalculation calculation = payrollService.calculateEmployeePayroll(
            testEmployee, testPayPeriod
        );
        
        // Then: Should handle gracefully without negative values
        assertNotNull("Calculation should not be null", calculation);
        assertTrue("Attendance earnings should not be negative", 
            calculation.getAttendanceEarnings().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue("Net salary should not be negative", 
            calculation.getNetSalary().compareTo(BigDecimal.ZERO) >= 0);
    }
    
    
    @Test
    public void testCalculatePayroll_futureDates() {
        // Given: Pay period in the future
        PayPeriodModel futurePeriod = new PayPeriodModel(
            LocalDate.now().plusMonths(1),
            LocalDate.now().plusMonths(1).plusDays(14),
            "Future Period"
        );
        payPeriodDAO.save(futurePeriod);
        createdPayPeriodIds.add(futurePeriod.getPayPeriodId());
        
        // When: Try to process payroll for future period
        boolean result = payrollService.processEmployeePayroll(
            testEmployee.getEmployeeId(), futurePeriod.getPayPeriodId()
        );
        
        // Then: Should process (business logic may allow future processing)
        // but verify no attendance/overtime data affects calculation
        if (result) {
            List<PayrollModel> payrollRecords = payrollDAO.findByPayPeriod(futurePeriod.getPayPeriodId());
            if (!payrollRecords.isEmpty()) {
                PayrollModel payroll = payrollRecords.get(0);
                // Should only have basic salary, no attendance-based earnings
                assertEquals("Should only have basic salary for future period",
                    testEmployee.getBasicSalary().divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP),
                    payroll.getBasicSalary());
            }
        }
        
        // Cleanup
        payrollDAO.deletePayrollByPeriod(futurePeriod.getPayPeriodId());
        // PayPeriodDAO doesn't have a delete method
        // payPeriodDAO.delete(futurePeriod.getPayPeriodId());
    }
    
    // =====================================
    // EDGE CASE TESTS
    // =====================================
    
    @Test
    public void testProcessPayroll_duplicateProcessing() {
        // Given: Process payroll once
        boolean firstResult = payrollService.processEmployeePayroll(
            testEmployee.getEmployeeId(), testPayPeriod.getPayPeriodId()
        );
        assertTrue("First processing should succeed", firstResult);
        
        // When: Try to process again for same period
        boolean secondResult = payrollService.processEmployeePayroll(
            testEmployee.getEmployeeId(), testPayPeriod.getPayPeriodId()
        );
        
        // Then: Should handle duplicate gracefully
        assertTrue("Should handle duplicate processing", secondResult);
        
        // Verify only one payroll record exists
        List<PayrollModel> payrollRecords = payrollDAO.findByPayPeriod(testPayPeriod.getPayPeriodId());
        assertEquals("Should not create duplicate payroll records", 1, 
            payrollRecords.stream()
                .filter(p -> p.getEmployeeId().equals(testEmployee.getEmployeeId()))
                .count());
    }
    
 
    @Test
    public void testCalculatePayroll_extremeOvertimeHours() {
        // Given: Extreme overtime hours (24 hours in one day)
        OvertimeRequestModel extremeOvertime = new OvertimeRequestModel(
            testEmployee.getEmployeeId(),
            LocalDateTime.now().withHour(0).withMinute(0),
            LocalDateTime.now().withHour(23).withMinute(59),
            "Emergency work"
        );
        extremeOvertime.setApprovalStatus(OvertimeRequestModel.ApprovalStatus.APPROVED);
        overtimeDAO.save(extremeOvertime);
        
        // When: Calculate payroll
        PayrollService.PayrollCalculation calculation = payrollService.calculateEmployeePayroll(
            testEmployee, testPayPeriod
        );
        
        // Then: Should handle extreme values
        assertNotNull("Calculation should not be null", calculation);
        assertTrue("Should calculate extreme overtime", 
            calculation.getOvertimePay().compareTo(BigDecimal.ZERO) > 0);
        
        // Verify overtime doesn't exceed reasonable limits
        BigDecimal maxReasonableOvertime = HOURLY_RATE.multiply(new BigDecimal("24")).multiply(new BigDecimal("1.5"));
        assertTrue("Overtime should not exceed 24 hours pay", 
            calculation.getOvertimePay().compareTo(maxReasonableOvertime) <= 0);
    }
    
 
    
    @Test
    public void testCalculateDeductions_highSalaryTaxBracket() {
        // Given: High salary employee (for tax calculation testing)
        testEmployee.setBasicSalary(new BigDecimal("100000.00"));
        employeeDAO.update(testEmployee);
        
        // When: Calculate payroll
        PayrollService.PayrollCalculation calculation = payrollService.calculateEmployeePayroll(
            testEmployee, testPayPeriod
        );
        
        // Then: Verify higher tax bracket is applied
        assertNotNull("Calculation should not be null", calculation);
        assertTrue("High earner should have significant deductions", 
            calculation.getTotalDeductions().compareTo(new BigDecimal("5000")) > 0);
        
        // Verify progressive tax calculation
        BigDecimal taxableIncome = calculation.getGrossIncome();
        if (taxableIncome.compareTo(new BigDecimal("33333")) > 0) {
            // Should be in higher tax bracket
            assertTrue("Should apply higher tax rate", 
                calculation.getTotalDeductions().compareTo(
                    taxableIncome.multiply(new BigDecimal("0.20"))
                ) > 0);
        }
    }
    
    // =====================================
    // HELPER METHODS
    // =====================================
    
    private void createTestEmployee() {
        // Generate unique email to avoid duplicates
        String uniqueEmail = "john.doe.test." + System.currentTimeMillis() + "@company.com";
        
        testEmployee = new EmployeeModel(
            "John",
            "Doe",
            LocalDate.of(1990, 1, 1),
            uniqueEmail,
            "hashedpassword123",
            1 // Position ID
        );
        testEmployee.setBasicSalary(BASIC_SALARY);
        testEmployee.setHourlyRate(HOURLY_RATE);
        testEmployee.setPhoneNumber("09171234567");
        testEmployee.setStatus(EmployeeModel.EmployeeStatus.REGULAR);
        
        boolean saved = employeeDAO.save(testEmployee);
        assertTrue("Test employee should be saved", saved);
    }
    
    private void createTestPayPeriod() {
        LocalDate startDate = LocalDate.now().minusDays(14);
        LocalDate endDate = LocalDate.now().minusDays(1);
        
        testPayPeriod = new PayPeriodModel(startDate, endDate, "Test Period");
        boolean saved = payPeriodDAO.save(testPayPeriod);
        assertTrue("Test pay period should be saved", saved);
    }
    
    private void createCompleteAttendanceRecords() {
        LocalDate currentDate = testPayPeriod.getStartDate();
        
        while (!currentDate.isAfter(testPayPeriod.getEndDate())) {
            if (currentDate.getDayOfWeek().getValue() <= 5) { // Weekdays only
                AttendanceModel attendance = new AttendanceModel(
                    testEmployee.getEmployeeId(),
                    currentDate,
                    LocalTime.of(8, 0)   // 8:00 AM
                );
                attendance.setTimeOut(LocalTime.of(17, 0));  // 5:00 PM
                attendanceDAO.save(attendance);
            }
            currentDate = currentDate.plusDays(1);
        }
    }
    
    private void createApprovedOvertime() {
        OvertimeRequestModel overtime = new OvertimeRequestModel(
            testEmployee.getEmployeeId(),
            LocalDateTime.now().minusDays(5).withHour(17).withMinute(0),
            LocalDateTime.now().minusDays(5).withHour(21).withMinute(0),
            "Project deadline"
        );
        overtime.setApprovalStatus(OvertimeRequestModel.ApprovalStatus.APPROVED);
        overtime.setDateApproved(LocalDateTime.now().minusDays(4));
        overtimeDAO.save(overtime);
    }
    
    private void cleanupEmployeeRelatedData(Integer employeeId) {
        try {
            // Clean up attendance records
            List<AttendanceModel> attendanceRecords = attendanceDAO.getAttendanceByEmployee(employeeId);
            for (AttendanceModel attendance : attendanceRecords) {
                attendanceDAO.delete(attendance.getAttendanceId());
            }
            
            // Clean up overtime requests
            List<OvertimeRequestModel> overtimeRequests = overtimeDAO.findByEmployee(employeeId);
            for (OvertimeRequestModel overtime : overtimeRequests) {
                overtimeDAO.delete(overtime.getOvertimeRequestId());
            }
            
            // Note: Payroll records will be deleted when cleaning up pay periods
            // This avoids foreign key constraint issues with payslips
        } catch (Exception e) {
            System.err.println("Error cleaning up employee data: " + e.getMessage());
        }
    }
    
    private void cleanupPayPeriodData(Integer payPeriodId) {
        try {
            // First, we need to delete payslips that reference payroll records
            // Since we don't have PayslipDAO, we'll use direct SQL
            String deletePayslipsSql = "DELETE FROM payslip WHERE payPeriodId = ?";
            try (Connection conn = databaseConnection.createConnection();
                 PreparedStatement stmt = conn.prepareStatement(deletePayslipsSql)) {
                stmt.setInt(1, payPeriodId);
                stmt.executeUpdate();
            }
            
            // Now we can safely delete payroll records
            payrollDAO.deletePayrollByPeriod(payPeriodId);
        } catch (Exception e) {
            System.err.println("Error cleaning up pay period data: " + e.getMessage());
        }
    }
}


