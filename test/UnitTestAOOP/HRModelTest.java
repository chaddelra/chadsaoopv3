/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UnitTestAOOP;

import Models.*;
import DAOs.*;
import Services.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runners.MethodSorters;
/**
 *
 * @author i-Gits <lr.jdelasarmas@mmdc.mcl.edu.ph>
 */

public class HRModelTest {

    
    // Test database connection
    private static DatabaseConnection testDbConnection;
    
    // DAO instances
    private static EmployeeDAO employeeDAO;
    private static PayPeriodDAO payPeriodDAO;
    private static PayrollDAO payrollDAO;
    private static AttendanceDAO attendanceDAO;
    private static LeaveDAO leaveDAO;
    private static OvertimeRequestDAO overtimeDAO;
    
    // Service instances
    private static PayrollService payrollService;
    private static ReportService reportService;
    private static AttendanceService attendanceService;
    private static LeaveService leaveService;
    private static OvertimeService overtimeService;
    
    // Test data
    private static EmployeeModel testEmployee;
    private static PayPeriodModel testPayPeriod;
    private static Integer testEmployeeId;
    private static Integer testPayPeriodId;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("=== Setting up HRModel Test Suite ===");
        
        // Initialize test database connection
        testDbConnection = new DatabaseConnection(
            "jdbc:mysql://localhost:3306/payrollsystem_db",
            "root",
            "motorph_123"
        );
        
        // Initialize DAOs
        employeeDAO = new EmployeeDAO(testDbConnection);
        payPeriodDAO = new PayPeriodDAO();
        payrollDAO = new PayrollDAO(testDbConnection);
        attendanceDAO = new AttendanceDAO(testDbConnection);
        leaveDAO = new LeaveDAO(testDbConnection);
        overtimeDAO = new OvertimeRequestDAO(testDbConnection);
        
        // Initialize Services
        payrollService = new PayrollService(testDbConnection);
        reportService = new ReportService(testDbConnection);
        attendanceService = new AttendanceService(testDbConnection);
        leaveService = new LeaveService(testDbConnection);
        overtimeService = new OvertimeService(testDbConnection);
        
        // Create test employee
        createTestEmployee();
        
        // Create test pay period
        createTestPayPeriod();
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("=== Cleaning up HRModel Test Suite ===");
        
        // Clean up test data
        if (testEmployeeId != null) {
            employeeDAO.delete(testEmployeeId);
        }
        
        if (testPayPeriodId != null) {
            payPeriodDAO.deleteById(testPayPeriodId);
        }
    }
    
    @Before
    public void setUp() {
        // Setup before each test if needed
    }
    
    @After
    public void tearDown() {
        // Cleanup after each test if needed
    }
    
    // ================================
    // HELPER METHODS
    // ================================
    
    private static void createTestEmployee() {
        testEmployee = new EmployeeModel();
        testEmployee.setFirstName("Test");
        testEmployee.setLastName("Employee");
        testEmployee.setBirthDate(LocalDate.of(1990, 1, 1));
        testEmployee.setEmail("test.employee" + System.currentTimeMillis() + "@test.com");
        testEmployee.setPhoneNumber("09171234567");
        testEmployee.setBasicSalary(new BigDecimal("30000"));
        testEmployee.setHourlyRate(new BigDecimal("178.57"));
        testEmployee.setUserRole("Employee");
        testEmployee.setPasswordHash("hashed_password");
        testEmployee.setStatus(EmployeeModel.EmployeeStatus.REGULAR);
        testEmployee.setPositionId(1);
        
        if (employeeDAO.save(testEmployee)) {
            testEmployeeId = testEmployee.getEmployeeId();
            System.out.println("Created test employee with ID: " + testEmployeeId);
        }
    }
    
    private static void createTestPayPeriod() {
        LocalDate startDate = LocalDate.now().minusDays(15);
        LocalDate endDate = LocalDate.now().minusDays(1);
        
        testPayPeriod = new PayPeriodModel(
            null,
            startDate,
            endDate,
            "Test Period " + System.currentTimeMillis()
        );
        
        if (payPeriodDAO.save(testPayPeriod)) {
            testPayPeriodId = testPayPeriod.getPayPeriodId();
            System.out.println("Created test pay period with ID: " + testPayPeriodId);
        }
    }
    
    // ================================
    // ENTITY VALIDATION - POSITIVE TESTS
    // ================================
    
    @Test
    public void testSetStartDate_valid() {
        System.out.println("Testing valid start date setting...");

        PayPeriodModel period = new PayPeriodModel();
        LocalDate validStartDate = LocalDate.now();
        LocalDate validEndDate = validStartDate.plusDays(14);

        period.setStartDate(validStartDate);
        period.setEndDate(validEndDate);
        period.setPeriodName("Valid Test Period");

        assertEquals("Start date should be set correctly", validStartDate, period.getStartDate());
        assertTrue("Pay period should be valid", period.isValid());
    }
    
    @Test
    public void testSetEndDate_valid() {
        System.out.println("Testing valid end date setting...");
        
        PayPeriodModel period = new PayPeriodModel();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(14);
        
        period.setStartDate(startDate);
        period.setEndDate(endDate);
        period.setPeriodName("Valid Period");
        
        assertEquals("End date should be set correctly", endDate, period.getEndDate());
        assertTrue("Pay period should be valid", period.isValid());
    }
    
    @Test
    public void testSetGrossPay_valid() {
        System.out.println("Testing valid gross pay setting...");
        
        PayrollModel payroll = new PayrollModel();
        BigDecimal validGrossPay = new BigDecimal("35000.00");
        payroll.setGrossIncome(validGrossPay);
        
        assertEquals("Gross pay should be set correctly", validGrossPay, payroll.getGrossIncome());
    }
    
    @Test
    public void testValidEnumValues() {
        System.out.println("Testing valid enum values...");
        
        // Test EmployeeStatus enum
        EmployeeModel.EmployeeStatus probationary = EmployeeModel.EmployeeStatus.PROBATIONARY;
        assertEquals("Probationary status value", "Probationary", probationary.getValue());
        
        EmployeeModel.EmployeeStatus regular = EmployeeModel.EmployeeStatus.REGULAR;
        assertEquals("Regular status value", "Regular", regular.getValue());
        
        EmployeeModel.EmployeeStatus terminated = EmployeeModel.EmployeeStatus.TERMINATED;
        assertEquals("Terminated status value", "Terminated", terminated.getValue());
        
        // Test LeaveRequest ApprovalStatus enum
        LeaveRequestModel.ApprovalStatus pending = LeaveRequestModel.ApprovalStatus.PENDING;
        assertEquals("Pending approval status", "Pending", pending.getValue());
        
        LeaveRequestModel.ApprovalStatus approved = LeaveRequestModel.ApprovalStatus.APPROVED;
        assertEquals("Approved status", "Approved", approved.getValue());
    }
    
    // ================================
    // ENTITY VALIDATION - NEGATIVE TESTS
    // ================================
    
    @Test
    public void testSetDateRange_endBeforeStart() {
        System.out.println("Testing invalid date range (end before start)...");
        
        PayPeriodModel period = new PayPeriodModel();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.minusDays(5); // End before start
        
        period.setStartDate(startDate);
        period.setEndDate(endDate);
        period.setPeriodName("Invalid Period");
        
        assertFalse("Pay period should be invalid when end date is before start date", 
                   period.isValid());
        
        // Test saving invalid period
        boolean saved = payPeriodDAO.save(period);
        assertFalse("Should not save invalid pay period", saved);
    }
    
    @Test
    public void testSetDateRange_pastDates() {
        System.out.println("Testing past dates handling...");
        
        LeaveRequestModel leaveRequest = new LeaveRequestModel();
        LocalDate pastStart = LocalDate.now().minusDays(30);
        LocalDate pastEnd = LocalDate.now().minusDays(25);
        
        leaveRequest.setEmployeeId(testEmployeeId);
        leaveRequest.setLeaveTypeId(1);
        leaveRequest.setLeaveStart(pastStart);
        leaveRequest.setLeaveEnd(pastEnd);
        leaveRequest.setLeaveReason("Past leave request");
        
        // Try to submit past leave request
        LeaveService.LeaveRequestResult result = leaveService.submitLeaveRequest(
            testEmployeeId, 1, pastStart, pastEnd, "Past leave"
        );
        
        assertFalse("Should not allow leave request for past dates", result.isSuccess());
        assertTrue("Error message should mention past dates", 
                  result.getMessage().toLowerCase().contains("past"));
    }
    
    @Test
    public void testSetReason_null() {
        System.out.println("Testing null reason handling...");
        
        LeaveRequestModel leaveRequest = new LeaveRequestModel();
        leaveRequest.setLeaveReason(null);
        
        assertNull("Reason should be null", leaveRequest.getLeaveReason());
        
        // Test with empty string
        leaveRequest.setLeaveReason("");
        assertEquals("Reason should be empty string", "", leaveRequest.getLeaveReason());
        
        // Test overtime request with null reason
        OvertimeRequestModel overtimeRequest = new OvertimeRequestModel();
        overtimeRequest.setOvertimeReason(null);
        assertNull("Overtime reason should be null", overtimeRequest.getOvertimeReason());
    }
    
    @Test
    public void testSetEmployeeId_invalid() {
        System.out.println("Testing invalid employee ID...");
        
        // Test with negative employee ID
        AttendanceModel attendance = new AttendanceModel();
        attendance.setEmployeeId(-1);
        assertEquals("Should set negative employee ID", Integer.valueOf(-1), attendance.getEmployeeId());
        
        // Test with non-existent employee ID
        Integer nonExistentId = 999999;
        EmployeeModel employee = employeeDAO.findById(nonExistentId);
        assertNull("Should return null for non-existent employee", employee);
        
        // Test payroll processing with invalid employee
        boolean result = payrollService.processEmployeePayroll(nonExistentId, testPayPeriodId);
        assertFalse("Should fail to process payroll for non-existent employee", result);
    }
    
    @Test
    public void testSetGrossPay_negative() {
        System.out.println("Testing negative gross pay...");
        
        PayrollModel payroll = new PayrollModel();
        BigDecimal negativeGross = new BigDecimal("-1000.00");
        
        payroll.setGrossIncome(negativeGross);
        assertEquals("Should allow setting negative gross income", negativeGross, payroll.getGrossIncome());
        
        // Calculate net salary with negative gross
        payroll.setTotalDeduction(new BigDecimal("500.00"));
        BigDecimal netSalary = payroll.getGrossIncome().subtract(payroll.getTotalDeduction());
        assertTrue("Net salary should be negative", netSalary.compareTo(BigDecimal.ZERO) < 0);
    }
    
    @Test
    public void testSetNetPay_greaterThanGross() {
        System.out.println("Testing net pay greater than gross pay...");
        
        PayrollModel payroll = new PayrollModel();
        BigDecimal grossIncome = new BigDecimal("30000.00");
        BigDecimal netSalary = new BigDecimal("35000.00"); // Greater than gross
        
        payroll.setGrossIncome(grossIncome);
        payroll.setNetSalary(netSalary);
        
        assertTrue("Net salary is greater than gross income", 
                 payroll.getNetSalary().compareTo(payroll.getGrossIncome()) > 0);
        
        // This would indicate negative deductions
        BigDecimal impliedDeductions = grossIncome.subtract(netSalary);
        assertTrue("Implied deductions would be negative", 
                 impliedDeductions.compareTo(BigDecimal.ZERO) < 0);
    }
    
    @Test
    public void testSetDeductions_negative() {
        System.out.println("Testing negative deductions...");
        
        PayrollModel payroll = new PayrollModel();
        BigDecimal negativeDeduction = new BigDecimal("-500.00");
        
        payroll.setTotalDeduction(negativeDeduction);
        assertEquals("Should allow setting negative deductions", 
                    negativeDeduction, payroll.getTotalDeduction());
        
        // Test with zero deductions
        payroll.setTotalDeduction(BigDecimal.ZERO);
        assertEquals("Should allow zero deductions", BigDecimal.ZERO, payroll.getTotalDeduction());
    }
    
    @Test
    public void testInvalidEnumConversion() {
        System.out.println("Testing invalid enum conversion...");
        
        // Test invalid EmployeeStatus conversion
        try {
            EmployeeModel.EmployeeStatus status = EmployeeModel.EmployeeStatus.fromString("InvalidStatus");
            fail("Should throw exception for invalid status");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception message should mention unknown status", 
                      e.getMessage().contains("Unknown employee status"));
        }
        
        // Test invalid ApprovalStatus conversion
        try {
            LeaveRequestModel.ApprovalStatus status = LeaveRequestModel.ApprovalStatus.fromString("InvalidApproval");
            fail("Should throw exception for invalid approval status");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception message should mention unknown approval status", 
                      e.getMessage().contains("Unknown approval status"));
        }
    }
    
    @Test
    public void testNullEnumHandling() {
        System.out.println("Testing null enum handling...");
        
        // Test null status conversion
        EmployeeModel.EmployeeStatus nullStatus = EmployeeModel.EmployeeStatus.fromString(null);
        assertEquals("Should return default PROBATIONARY for null", 
                    EmployeeModel.EmployeeStatus.PROBATIONARY, nullStatus);
        
        // Test null approval status
        LeaveRequestModel.ApprovalStatus nullApproval = LeaveRequestModel.ApprovalStatus.fromString(null);
        assertEquals("Should return default PENDING for null", 
                    LeaveRequestModel.ApprovalStatus.PENDING, nullApproval);
    }
    
    // ================================
    // REPORT GENERATION TESTS
    // ================================
    
    @Test
    public void testGeneratePayrollReport_validPeriod() {
        System.out.println("Testing payroll report generation for valid period...");
        
        // Process payroll first
        PayrollService.PayrollProcessingResult processingResult = 
            payrollService.processPayrollForPeriod(testPayPeriodId);
        
        // Generate report
        ReportService.PayrollReport report = reportService.generatePayrollReport(testPayPeriodId);
        
        assertNotNull("Report should not be null", report);
        assertTrue("Report should be successful", report.isSuccess());
        assertEquals("Pay period ID should match", testPayPeriodId, report.getPayPeriodId());
        assertNotNull("Report entries should not be null", report.getPayrollEntries());
    }
    
    @Test
    public void testGeneratePayrollReport_invalidPeriod() {
        System.out.println("Testing payroll report generation for invalid period...");
        
        Integer invalidPeriodId = 999999;
        ReportService.PayrollReport report = reportService.generatePayrollReport(invalidPeriodId);
        
        assertNotNull("Report should not be null", report);
        assertFalse("Report should fail for invalid period", report.isSuccess());
        assertTrue("Error message should mention pay period not found", 
                  report.getErrorMessage().contains("Pay period not found"));
    }
    
    @Test
    public void testGenerateAttendanceReport_validDate() {
        System.out.println("Testing attendance report generation for valid date...");
        
        LocalDate reportDate = LocalDate.now();
        ReportService.AttendanceReport report = reportService.generateDailyAttendanceReport(reportDate);
        
        assertNotNull("Report should not be null", report);
        assertTrue("Report should be successful", report.isSuccess());
        assertEquals("Report date should match", reportDate, report.getReportDate());
        assertNotNull("Attendance records should not be null", report.getAttendanceRecords());
    }
    
    @Test
    public void testGenerateMonthlyAttendanceReport() {
        System.out.println("Testing monthly attendance report generation...");
        
        YearMonth currentMonth = YearMonth.now();
        ReportService.MonthlyAttendanceReport report = 
            reportService.generateMonthlyAttendanceReport(currentMonth);
        
        assertNotNull("Report should not be null", report);
        assertTrue("Report should be successful", report.isSuccess());
        assertEquals("Year month should match", currentMonth, report.getYearMonth());
        assertNotNull("Employee summaries should not be null", report.getEmployeeSummaries());
    }
    
    // ================================
    // BOUNDARY VALUE TESTS
    // ================================
    
    @Test
    public void testMaximumOvertimeHours() {
        System.out.println("Testing maximum overtime hours validation...");
        
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(5); // 5 hours overtime (exceeds 4 hour max)
        
        OvertimeService.OvertimeRequestResult result = overtimeService.submitOvertimeRequest(
            testEmployeeId, start, end, "Extended overtime"
        );
        
        assertFalse("Should reject overtime exceeding maximum hours", result.isSuccess());
        assertTrue("Error message should mention maximum hours", 
                  result.getMessage().toLowerCase().contains("maximum"));
    }
    
    @Test
    public void testMinimumOvertimeMinutes() {
        System.out.println("Testing minimum overtime minutes validation...");
        
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusMinutes(15); // Only 15 minutes (less than 30 min minimum)
        
        OvertimeService.OvertimeRequestResult result = overtimeService.submitOvertimeRequest(
            testEmployeeId, start, end, "Short overtime"
        );
        
        assertFalse("Should reject overtime less than minimum duration", result.isSuccess());
        assertTrue("Error message should mention minimum duration", 
                  result.getMessage().toLowerCase().contains("minimum"));
    }
    
    @Test
    public void testZeroSalaryCalculation() {
        System.out.println("Testing zero salary calculation...");

        EmployeeModel zeroSalaryEmployee = new EmployeeModel();
        zeroSalaryEmployee.setEmployeeId(999); // Set a dummy ID
        zeroSalaryEmployee.setBasicSalary(BigDecimal.ZERO);
        zeroSalaryEmployee.setHourlyRate(BigDecimal.ZERO);

        PayrollService.PayrollCalculation calc = payrollService.calculateEmployeePayroll(
            zeroSalaryEmployee, testPayPeriod
        );

        assertNotNull("Calculation should not be null", calc);
        // The basic salary is divided by 2 for semi-monthly, so 0/2 = 0
        assertEquals("Basic salary should be zero", 
             0, 
             calc.getBasicSalary().compareTo(BigDecimal.ZERO));
        // Net salary could be negative due to fixed deductions
        assertTrue("Net salary should be zero or negative due to deductions", 
                  calc.getNetSalary().compareTo(BigDecimal.ZERO) <= 0);
    }
    
    // ================================
    // CONCURRENT ACCESS TESTS
    // ================================
    
    @Test
    public void testDuplicateAttendanceTimeIn() {
        System.out.println("Testing duplicate attendance time in...");
        
        // First time in
        AttendanceService.AttendanceResult result1 = 
            attendanceService.recordTimeIn(testEmployeeId, null);
        
        // Attempt duplicate time in
        AttendanceService.AttendanceResult result2 = 
            attendanceService.recordTimeIn(testEmployeeId, null);
        
        assertTrue("First time in should succeed", result1.isSuccess());
        assertFalse("Second time in should fail", result2.isSuccess());
        assertTrue("Error message should mention already timed in", 
                  result2.getMessage().contains("already timed in"));
    }
    
    @Test
    public void testOverlappingLeaveRequests() {
        System.out.println("Testing overlapping leave requests...");
        
        LocalDate start1 = LocalDate.now().plusDays(10);
        LocalDate end1 = start1.plusDays(3);
        
        // First leave request
        LeaveService.LeaveRequestResult result1 = leaveService.submitLeaveRequest(
            testEmployeeId, 1, start1, end1, "First leave"
        );
        
        // Overlapping leave request
        LocalDate start2 = start1.plusDays(2); // Overlaps with first request
        LocalDate end2 = start2.plusDays(2);
        
        LeaveService.LeaveRequestResult result2 = leaveService.submitLeaveRequest(
            testEmployeeId, 1, start2, end2, "Overlapping leave"
        );
        
        assertTrue("First leave request should succeed", result1.isSuccess());
        assertFalse("Overlapping leave request should fail", result2.isSuccess());
        assertTrue("Error message should mention overlap", 
                  result2.getMessage().toLowerCase().contains("overlap"));
    }
    
    // ================================
    // DATA INTEGRITY TESTS
    // ================================
    
    @Test
    public void testEmployeeDataIntegrity() {
        System.out.println("Testing employee data integrity...");
        
        // Test required fields
        EmployeeModel incompleteEmployee = new EmployeeModel();
        // Missing required fields: firstName, lastName, birthDate, email, etc.
        
        boolean saved = employeeDAO.save(incompleteEmployee);
        assertFalse("Should not save employee with missing required fields", saved);
        
        // Test unique email constraint
        EmployeeModel duplicateEmailEmployee = new EmployeeModel();
        duplicateEmailEmployee.setFirstName("Duplicate");
        duplicateEmailEmployee.setLastName("Email");
        duplicateEmailEmployee.setBirthDate(LocalDate.of(1990, 1, 1));
        duplicateEmailEmployee.setEmail(testEmployee.getEmail()); // Use existing email
        duplicateEmailEmployee.setBasicSalary(new BigDecimal("25000"));
        duplicateEmailEmployee.setHourlyRate(new BigDecimal("150"));
        duplicateEmailEmployee.setPasswordHash("password");
        duplicateEmailEmployee.setPositionId(1);
        
        boolean duplicateSaved = employeeDAO.save(duplicateEmailEmployee);
        assertFalse("Should not save employee with duplicate email", duplicateSaved);
    }
    
    @Test
    public void testPayrollUniqueConstraint() {
        System.out.println("Testing payroll unique constraint...");
        
        // Process payroll for test employee
        boolean firstProcess = payrollService.processEmployeePayroll(testEmployeeId, testPayPeriodId);
        
        // Try to process again (should handle gracefully)
        boolean secondProcess = payrollService.processEmployeePayroll(testEmployeeId, testPayPeriodId);
        
        assertTrue("First payroll process should succeed", firstProcess);
        assertTrue("Second payroll process should handle existing record gracefully", secondProcess);
    }
    
    // ================================
    // CALCULATION ACCURACY TESTS
    // ================================
    
    @Test
    public void testOvertimePayCalculation() {
        System.out.println("Testing overtime pay calculation accuracy...");
        
        BigDecimal hourlyRate = new BigDecimal("200.00");
        BigDecimal expectedMultiplier = new BigDecimal("1.5");
        
        OvertimeRequestModel overtimeRequest = new OvertimeRequestModel();
        overtimeRequest.setOvertimeStart(LocalDateTime.now().minusHours(2));
        overtimeRequest.setOvertimeEnd(LocalDateTime.now());
        
        BigDecimal overtimeHours = overtimeRequest.getOvertimeHours();
        BigDecimal calculatedPay = overtimeService.calculateOvertimePay(overtimeRequest, hourlyRate);
        
        BigDecimal expectedPay = overtimeHours.multiply(hourlyRate).multiply(expectedMultiplier)
                                            .setScale(2, BigDecimal.ROUND_HALF_UP);
        
        assertEquals("Overtime pay calculation should be accurate", expectedPay, calculatedPay);
    }
    
    @Test
    public void testTaxCalculationBoundaries() {
        System.out.println("Testing tax calculation at bracket boundaries...");
        
        // Test at tax bracket boundary (₱20,833 monthly)
        EmployeeModel boundaryEmployee = new EmployeeModel();
        boundaryEmployee.setBasicSalary(new BigDecimal("20833"));
        boundaryEmployee.setHourlyRate(new BigDecimal("125"));
        
        PayPeriodModel period = new PayPeriodModel();
        period.setStartDate(LocalDate.now().minusDays(15));
        period.setEndDate(LocalDate.now());
        
        PayrollService.PayrollCalculation calc = 
            payrollService.calculateEmployeePayroll(boundaryEmployee, period);
        
        assertNotNull("Calculation should not be null", calc);
        // At boundary, tax should be zero or minimal
        assertTrue("Tax deduction should be reasonable at boundary", 
                  calc.getTotalDeductions().compareTo(new BigDecimal("5000")) < 0);
    }
    
    // ================================
    // ERROR RECOVERY TESTS
    // ================================
    
    @Test
    public void testInvalidDateFormatHandling() {
        System.out.println("Testing invalid date format handling...");
        
        // Test with null dates
        PayPeriodModel nullDatePeriod = new PayPeriodModel();
        nullDatePeriod.setStartDate(null);
        nullDatePeriod.setEndDate(null);
        nullDatePeriod.setPeriodName("Null Date Period");
        
        assertFalse("Period with null dates should be invalid", nullDatePeriod.isValid());
        
        boolean saved = payPeriodDAO.save(nullDatePeriod);
        assertFalse("Should not save period with null dates", saved);
    }
    
    @Test
    public void testDivisionByZeroProtection() {
        System.out.println("Testing division by zero protection...");
        
        // Test average calculation with zero divisor
        AttendanceService.AttendanceSummary summary = 
            attendanceService.getMonthlyAttendanceSummary(testEmployeeId, YearMonth.now());
        
        assertNotNull("Summary should not be null", summary);
        // Even with no attendance, should handle division gracefully
        assertTrue("Average hours should be zero or valid", 
                  summary.getAverageHoursPerDay().compareTo(BigDecimal.ZERO) >= 0);
    }
    
    // ================================
    // PERFORMANCE BOUNDARY TESTS
    // ================================
    
    @Test(timeout = 5000) // 5 second timeout
    public void testLargePayrollProcessing() {
        System.out.println("Testing large payroll processing performance...");
        
        // This test ensures payroll processing completes in reasonable time
        PayrollService.PayrollProcessingResult result = 
            payrollService.processPayrollForPeriod(testPayPeriodId);
        
        assertNotNull("Result should not be null", result);
        assertTrue("Should complete within timeout", true);
    }
    
    @Test
    public void testReportGenerationWithNoData() {
        System.out.println("Testing report generation with no data...");
        
        // Create a new pay period with no payroll data
        LocalDate futureStart = LocalDate.now().plusMonths(1);
        LocalDate futureEnd = futureStart.plusDays(14);
        
        PayPeriodModel emptyPeriod = new PayPeriodModel(
            null, futureStart, futureEnd, "Empty Period"
        );
        
        if (payPeriodDAO.save(emptyPeriod)) {
            ReportService.PayrollReport report = 
                reportService.generatePayrollReport(emptyPeriod.getPayPeriodId());
            
            assertNotNull("Report should not be null", report);
            assertTrue("Report should succeed even with no data", report.isSuccess());
            assertEquals("Total employees should be zero", 0, report.getTotalEmployees());
            assertEquals("Total gross income should be zero", 
                        BigDecimal.ZERO, report.getTotalGrossIncome());
            
            // Cleanup
            payPeriodDAO.deleteById(emptyPeriod.getPayPeriodId());
        }
    }
}


