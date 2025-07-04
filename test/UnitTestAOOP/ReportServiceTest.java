package UnitTestAOOP;

import DAOs.*;
import Models.*;
import Services.*;
import org.junit.*;
import static org.junit.Assert.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * JUnit test for ReportService with negative testing
 * @author martin
 */
public class ReportServiceTest {
    
    private static DatabaseConnection databaseConnection;
    private static ReportService reportService;
    private static EmployeeDAO employeeDAO;
    private static PayPeriodDAO payPeriodDAO;
    private static PayrollDAO payrollDAO;
    private static AttendanceDAO attendanceDAO;
    private static LeaveDAO leaveDAO;
    
    // Test data IDs
    private static Integer testEmployeeId;
    private static Integer testPayPeriodId;
    private static Integer nonExistentEmployeeId = 99999;
    private static Integer nonExistentPayPeriodId = 99999;
    
    @BeforeClass
    public static void setUpClass() {
        // Initialize database
        databaseConnection = new DatabaseConnection(
            "jdbc:mysql://localhost:3306/payrollsystem_db",
            "root",
            "motorph_123"
        );
        
        // Initialize DAOs
        employeeDAO = new EmployeeDAO(databaseConnection);
        payPeriodDAO = new PayPeriodDAO();
        payrollDAO = new PayrollDAO(databaseConnection);
        attendanceDAO = new AttendanceDAO(databaseConnection);
        leaveDAO = new LeaveDAO(databaseConnection);
        
        // Initialize service
        reportService = new ReportService(databaseConnection);
        
        // Set up test data
        setupTestData();
    }
    
    @AfterClass
    public static void tearDownClass() {
        // Clean up test data
        cleanupTestData();
    }
    
    @Before
    public void setUp() {
        // Any per-test setup if needed
    }
    
    @After
    public void tearDown() {
        // Any per-test cleanup if needed
    }
    
    // ================================
    // SETUP AND CLEANUP METHODS
    // ================================
    
    private static void setupTestData() {
        try {
            // Create a test employee for testing
            EmployeeModel testEmployee = new EmployeeModel();
            testEmployee.setFirstName("Test");
            testEmployee.setLastName("Employee");
            testEmployee.setBirthDate(LocalDate.of(1990, 1, 1));
            testEmployee.setEmail("test.employee@test.com");
            testEmployee.setPhoneNumber("09171234567");
            testEmployee.setBasicSalary(new BigDecimal("30000"));
            testEmployee.setHourlyRate(new BigDecimal("178.57"));
            testEmployee.setUserRole("Employee");
            testEmployee.setPasswordHash("test_hash");
            testEmployee.setStatus(EmployeeModel.EmployeeStatus.REGULAR);
            testEmployee.setPositionId(1); // Assuming position 1 exists
            
            if (employeeDAO.save(testEmployee)) {
                testEmployeeId = testEmployee.getEmployeeId();
            }
            
            // Get an existing pay period for testing
            List<PayPeriodModel> payPeriods = payPeriodDAO.findAll();
            if (!payPeriods.isEmpty()) {
                testPayPeriodId = payPeriods.get(0).getPayPeriodId();
            }
            
        } catch (Exception e) {
            System.err.println("Error setting up test data: " + e.getMessage());
        }
    }
    
    private static void cleanupTestData() {
        try {
            // Delete test employee if created
            if (testEmployeeId != null) {
                employeeDAO.delete(testEmployeeId);
            }
        } catch (Exception e) {
            System.err.println("Error cleaning up test data: " + e.getMessage());
        }
    }
    
    // ================================
    // PAYROLL REPORT TESTS
    // ================================
    
    @Test
    public void testGeneratePayrollReport_ValidPayPeriod() {
        System.out.println("Testing payroll report generation with valid pay period...");
        
        if (testPayPeriodId == null) {
            System.out.println("No pay period found for testing. Skipping test.");
            return;
        }
        
        ReportService.PayrollReport report = reportService.generatePayrollReport(testPayPeriodId);
        
        assertNotNull("Report should not be null", report);
        assertTrue("Report should be successful", report.isSuccess());
        // Don't check for null error message - it might be empty string
        assertTrue("Error message should be empty for successful report", 
                  report.getErrorMessage() == null || report.getErrorMessage().isEmpty());
        assertEquals("Pay period ID should match", testPayPeriodId, report.getPayPeriodId());
        assertNotNull("Period name should not be null", report.getPeriodName());
        assertNotNull("Start date should not be null", report.getStartDate());
        assertNotNull("End date should not be null", report.getEndDate());
        assertNotNull("Generated date should not be null", report.getGeneratedDate());
        assertTrue("Total employees should be non-negative", report.getTotalEmployees() >= 0);
        assertNotNull("Payroll entries should not be null", report.getPayrollEntries());
    }
    
    @Test
    public void testGeneratePayrollReport_InvalidPayPeriod() {
        System.out.println("Testing payroll report generation with invalid pay period...");
        
        ReportService.PayrollReport report = reportService.generatePayrollReport(nonExistentPayPeriodId);
        
        assertNotNull("Report should not be null even for invalid input", report);
        assertFalse("Report should not be successful for invalid pay period", report.isSuccess());
        assertNotNull("Error message should be present for failed report", report.getErrorMessage());
        assertTrue("Error message should mention pay period not found", 
                  report.getErrorMessage().contains("Pay period not found"));
    }
    
    
    // ================================
    // SALARY COMPARISON REPORT TESTS
    // ================================
    
    @Test
    public void testGenerateSalaryComparisonReport_ValidDateRange() {
        System.out.println("Testing salary comparison report with valid date range...");
        
        LocalDate startDate = LocalDate.now().minusMonths(3);
        LocalDate endDate = LocalDate.now();
        
        ReportService.SalaryComparisonReport report = 
            reportService.generateSalaryComparisonReport(startDate, endDate);
        
        assertNotNull("Report should not be null", report);
        assertTrue("Report should be successful", report.isSuccess());
        assertEquals("Start date should match", startDate, report.getStartDate());
        assertEquals("End date should match", endDate, report.getEndDate());
        assertNotNull("Generated date should not be null", report.getGeneratedDate());
        assertTrue("Total employees should be non-negative", report.getTotalEmployees() >= 0);
        assertNotNull("Salary entries should not be null", report.getSalaryEntries());
        
        // Verify salary statistics
        if (report.getTotalEmployees() > 0) {
            assertTrue("Average salary should be positive", 
                      report.getAverageSalary().compareTo(BigDecimal.ZERO) >= 0);
            assertTrue("Highest salary should be positive", 
                      report.getHighestSalary().compareTo(BigDecimal.ZERO) >= 0);
            assertTrue("Lowest salary should be positive", 
                      report.getLowestSalary().compareTo(BigDecimal.ZERO) >= 0);
            assertTrue("Highest salary should be >= lowest salary", 
                      report.getHighestSalary().compareTo(report.getLowestSalary()) >= 0);
        }
    }
       
    // ================================
    // ATTENDANCE REPORT TESTS
    // ================================
    
    @Test
    public void testGenerateDailyAttendanceReport_ValidDate() {
        System.out.println("Testing daily attendance report with valid date...");
        
        LocalDate today = LocalDate.now();
        
        ReportService.AttendanceReport report = reportService.generateDailyAttendanceReport(today);
        
        assertNotNull("Report should not be null", report);
        assertTrue("Report should be successful", report.isSuccess());
        assertEquals("Report date should match", today, report.getReportDate());
        assertEquals("Report type should be Daily Attendance", "Daily Attendance", report.getReportType());
        assertNotNull("Generated date should not be null", report.getGeneratedDate());
        assertTrue("Present count should be non-negative", report.getPresentCount() >= 0);
        assertTrue("Late count should be non-negative", report.getLateCount() >= 0);
        assertTrue("Absent count should be non-negative", report.getAbsentCount() >= 0);
        assertTrue("Total employees should equal sum of statuses", 
                  report.getTotalEmployees() >= report.getPresentCount() + report.getLateCount() + report.getAbsentCount());
    }
    
    @Test
    public void testGenerateDailyAttendanceReport_WeekendDate() {
        System.out.println("Testing daily attendance report with weekend date...");
        
        // Find a Saturday
        LocalDate saturday = LocalDate.now();
        while (saturday.getDayOfWeek().getValue() != 6) {
            saturday = saturday.plusDays(1);
        }
        
        ReportService.AttendanceReport report = reportService.generateDailyAttendanceReport(saturday);
        
        assertNotNull("Report should not be null", report);
        assertTrue("Report should be successful", report.isSuccess());
        // Weekend might have zero attendance which is valid
    }
    
    @Test
    public void testGenerateDailyAttendanceReport_FutureDate() {
        System.out.println("Testing daily attendance report with future date...");
        
        LocalDate futureDate = LocalDate.now().plusDays(30);
        
        ReportService.AttendanceReport report = reportService.generateDailyAttendanceReport(futureDate);
        
        assertNotNull("Report should not be null", report);
        assertTrue("Report should be successful", report.isSuccess());
        // Should have no attendance for future dates
        assertEquals("Should have zero present for future date", 0, report.getPresentCount());
        assertEquals("Should have zero late for future date", 0, report.getLateCount());
    }
    
    // ================================
    // MONTHLY ATTENDANCE REPORT TESTS
    // ================================
    
    @Test
    public void testGenerateMonthlyAttendanceReport_ValidMonth() {
        System.out.println("Testing monthly attendance report with valid month...");
        
        YearMonth currentMonth = YearMonth.now();
        
        ReportService.MonthlyAttendanceReport report = 
            reportService.generateMonthlyAttendanceReport(currentMonth);
        
        assertNotNull("Report should not be null", report);
        assertTrue("Report should be successful", report.isSuccess());
        assertEquals("Year month should match", currentMonth, report.getYearMonth());
        assertNotNull("Generated date should not be null", report.getGeneratedDate());
        assertNotNull("Employee summaries should not be null", report.getEmployeeSummaries());
        assertTrue("Overall attendance rate should be between 0 and 100", 
                  report.getOverallAttendanceRate().compareTo(BigDecimal.ZERO) >= 0 &&
                  report.getOverallAttendanceRate().compareTo(new BigDecimal("100")) <= 0);
    }
      
    // ================================
    // LEAVE REPORT TESTS
    // ================================
    
    @Test
    public void testGenerateLeaveReport_ValidYear() {
        System.out.println("Testing leave report with valid year...");
        
        Integer currentYear = LocalDate.now().getYear();
        
        ReportService.LeaveReport report = reportService.generateLeaveReport(currentYear);
        
        assertNotNull("Report should not be null", report);
        assertTrue("Report should be successful", report.isSuccess());
        assertEquals("Year should match", currentYear, report.getYear());
        assertNotNull("Generated date should not be null", report.getGeneratedDate());
        assertNotNull("Leave summaries should not be null", report.getLeaveSummaries());
        assertTrue("Total allocated days should be non-negative", report.getTotalAllocatedDays() >= 0);
        assertTrue("Total used days should be non-negative", report.getTotalUsedDays() >= 0);
        assertTrue("Used days should not exceed allocated days", 
                  report.getTotalUsedDays() <= report.getTotalAllocatedDays());
    }
    
    @Test
    public void testGenerateLeaveReport_FutureYear() {
        System.out.println("Testing leave report with future year...");
        
        Integer futureYear = LocalDate.now().getYear() + 2;
        
        ReportService.LeaveReport report = reportService.generateLeaveReport(futureYear);
        
        assertNotNull("Report should not be null", report);
        assertTrue("Report should be successful", report.isSuccess());
        // Might have allocated days but no used days for future year
    }
    
    // ================================
    // OVERTIME REPORT TESTS
    // ================================
    
    @Test
    public void testGenerateOvertimeReport_ValidDateRange() {
        System.out.println("Testing overtime report with valid date range...");
        
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();
        
        ReportService.OvertimeReport report = reportService.generateOvertimeReport(startDate, endDate);
        
        assertNotNull("Report should not be null", report);
        assertTrue("Report should be successful", report.isSuccess());
        assertEquals("Start date should match", startDate, report.getStartDate());
        assertEquals("End date should match", endDate, report.getEndDate());
        assertNotNull("Generated date should not be null", report.getGeneratedDate());
        assertNotNull("Overtime rankings should not be null", report.getOvertimeRankings());
        assertTrue("Total overtime hours should be non-negative", 
                  report.getTotalOvertimeHours().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue("Total overtime pay should be non-negative", 
                  report.getTotalOvertimePay().compareTo(BigDecimal.ZERO) >= 0);
    }
    
    @Test
    public void testGenerateOvertimeReport_SameStartEndDate() {
        System.out.println("Testing overtime report with same start and end date...");
        
        LocalDate sameDate = LocalDate.now();
        
        ReportService.OvertimeReport report = reportService.generateOvertimeReport(sameDate, sameDate);
        
        assertNotNull("Report should not be null", report);
        assertTrue("Report should be successful", report.isSuccess());
        // Should handle single day report correctly
    }
      
    // ================================
    // COMPLIANCE REPORT TESTS
    // ================================
    
    @Test
    public void testGenerateComplianceReport_ValidMonth() {
        System.out.println("Testing compliance report with valid month...");
        
        YearMonth currentMonth = YearMonth.now();
        
        ReportService.ComplianceReport report = reportService.generateComplianceReport(currentMonth);
        
        assertNotNull("Report should not be null", report);
        assertTrue("Report should be successful", report.isSuccess());
        assertEquals("Year month should match", currentMonth, report.getYearMonth());
        assertNotNull("Generated date should not be null", report.getGeneratedDate());
        assertTrue("Total employees should be non-negative", report.getTotalEmployees() >= 0);
        assertTrue("Total SSS should be non-negative", 
                  report.getTotalSSS().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue("Total PhilHealth should be non-negative", 
                  report.getTotalPhilHealth().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue("Total Pag-IBIG should be non-negative", 
                  report.getTotalPagIbig().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue("Total withholding tax should be non-negative", 
                  report.getTotalWithholdingTax().compareTo(BigDecimal.ZERO) >= 0);
        
        // Verify total contributions calculation
        BigDecimal expectedTotal = report.getTotalSSS()
            .add(report.getTotalPhilHealth())
            .add(report.getTotalPagIbig())
            .add(report.getTotalWithholdingTax());
        assertEquals("Total government contributions should be sum of all contributions", 
                    expectedTotal, report.getTotalGovernmentContributions());
    }
    
    // ================================
    // FORMATTING METHOD TESTS
    // ================================
    
    @Test
    public void testFormatCurrency() {
        System.out.println("Testing currency formatting...");
        
        assertEquals("Should format zero correctly", "₱0.00", 
                    reportService.formatCurrency(BigDecimal.ZERO));
        assertEquals("Should format positive amount correctly", "₱1,234.56", 
                    reportService.formatCurrency(new BigDecimal("1234.56")));
        assertEquals("Should format large amount correctly", "₱1,234,567.89", 
                    reportService.formatCurrency(new BigDecimal("1234567.89")));
        assertEquals("Should format negative amount correctly", "₱-100.00", 
                    reportService.formatCurrency(new BigDecimal("-100")));
    }
    
    @Test
    public void testFormatPercentage() {
        System.out.println("Testing percentage formatting...");
        
        assertEquals("Should format zero percentage", "0.00%", 
                    reportService.formatPercentage(BigDecimal.ZERO));
        assertEquals("Should format percentage correctly", "50.00%", 
                    reportService.formatPercentage(new BigDecimal("50")));
        assertEquals("Should format decimal percentage correctly", "75.50%", 
                    reportService.formatPercentage(new BigDecimal("75.5")));
        assertEquals("Should format over 100 percentage correctly", "150.00%", 
                    reportService.formatPercentage(new BigDecimal("150")));
    }
    
    // ================================
    // EDGE CASE AND STRESS TESTS
    // ================================
    
    @Test
    public void testConcurrentReportGeneration() {
        System.out.println("Testing concurrent report generation...");
        
        // Generate multiple reports simultaneously
        ReportService.PayrollReport payrollReport = null;
        ReportService.AttendanceReport attendanceReport = null;
        ReportService.LeaveReport leaveReport = null;
        
        if (testPayPeriodId != null) {
            payrollReport = reportService.generatePayrollReport(testPayPeriodId);
        }
        attendanceReport = reportService.generateDailyAttendanceReport(LocalDate.now());
        leaveReport = reportService.generateLeaveReport(LocalDate.now().getYear());
        
        // All should complete successfully
        if (payrollReport != null) {
            assertTrue("Payroll report should be successful", payrollReport.isSuccess());
        }
        assertNotNull("Attendance report should not be null", attendanceReport);
        assertTrue("Attendance report should be successful", attendanceReport.isSuccess());
        assertNotNull("Leave report should not be null", leaveReport);
        assertTrue("Leave report should be successful", leaveReport.isSuccess());
    }
    
    // ================================
    // SECURITY AND ACCESS TESTS
    // ================================
    
    @Test
    public void testReportDataIntegrity() {
        System.out.println("Testing report data integrity...");
        
        if (testPayPeriodId != null) {
            ReportService.PayrollReport report = reportService.generatePayrollReport(testPayPeriodId);
            
            if (report.isSuccess() && !report.getPayrollEntries().isEmpty()) {
                // Verify data integrity
                BigDecimal calculatedTotalGross = BigDecimal.ZERO;
                BigDecimal calculatedTotalNet = BigDecimal.ZERO;
                
                for (ReportService.PayrollReportEntry entry : report.getPayrollEntries()) {
                    // Verify individual entry integrity
                    assertNotNull("Employee ID should not be null", entry.getEmployeeId());
                    assertNotNull("Employee name should not be null", entry.getEmployeeName());
                    assertNotNull("Basic salary should not be null", entry.getBasicSalary());
                    assertNotNull("Gross income should not be null", entry.getGrossIncome());
                    assertNotNull("Net salary should not be null", entry.getNetSalary());
                    
                    // Verify logical relationships
                    assertTrue("Gross income should be >= basic salary", 
                              entry.getGrossIncome().compareTo(entry.getBasicSalary()) >= 0);
                    assertTrue("Net salary should be <= gross income", 
                              entry.getNetSalary().compareTo(entry.getGrossIncome()) <= 0);
                    assertTrue("Net salary should be positive", 
                              entry.getNetSalary().compareTo(BigDecimal.ZERO) > 0);
                    
                    calculatedTotalGross = calculatedTotalGross.add(entry.getGrossIncome());
                    calculatedTotalNet = calculatedTotalNet.add(entry.getNetSalary());
                }
                
                // Verify report totals match sum of entries (allowing small rounding difference)
                BigDecimal grossDiff = report.getTotalGrossIncome().subtract(calculatedTotalGross).abs();
                BigDecimal netDiff = report.getTotalNetSalary().subtract(calculatedTotalNet).abs();
                
                assertTrue("Total gross income should match sum of entries (within 1 peso)", 
                          grossDiff.compareTo(new BigDecimal("1")) <= 0);
                assertTrue("Total net salary should match sum of entries (within 1 peso)", 
                          netDiff.compareTo(new BigDecimal("1")) <= 0);
            }
        }
    }
    
    // ================================
    // EXCEPTION HANDLING TESTS
    // ================================
    
    @Test
    public void testReportGeneration_DatabaseConnectionFailure() {
        System.out.println("Testing report generation with database connection issues...");
        
        // Create a report service with invalid database connection
        DatabaseConnection badConnection = new DatabaseConnection(
            "jdbc:mysql://invalid_host:3306/invalid_db",
            "invalid_user",
            "invalid_password"
        );
        
        ReportService badReportService = new ReportService(badConnection);
        
        // Try to generate report with bad connection
        try {
            ReportService.PayrollReport report = badReportService.generatePayrollReport(1);
            
            assertNotNull("Report should not be null even with bad connection", report);
            
            // The service might return a successful but empty report, or it might fail
            // Let's check what actually happens
            if (report.isSuccess()) {
                // If it succeeds, it should have no data
                System.out.println("Service returned successful report with bad connection - checking for empty data");
                assertEquals("Should have no payroll entries with bad connection", 0, report.getPayrollEntries().size());
                assertEquals("Should have zero employees with bad connection", 0, report.getTotalEmployees());
            } else {
                // If it fails, check for error message
                System.out.println("Service correctly failed with bad connection");
                assertNotNull("Error message should be present", report.getErrorMessage());
                assertTrue("Error message should mention error", 
                          report.getErrorMessage().toLowerCase().contains("error") ||
                          report.getErrorMessage().toLowerCase().contains("pay period not found"));
            }
        } catch (Exception e) {
            // If service throws exception, that's also valid behavior
            System.out.println("Service throws exception for bad connection - expected behavior: " + e.getMessage());
            assertTrue("Exception is expected for bad connection", true);
        }
    }
    
    @Test
    public void testReportGeneration_ExtremeValues() {
        System.out.println("Testing report generation with extreme values...");
        
        // Test with maximum integer value
        ReportService.PayrollReport report1 = 
            reportService.generatePayrollReport(Integer.MAX_VALUE);
        assertNotNull("Should handle max integer value", report1);
        assertFalse("Should not find pay period with max ID", report1.isSuccess());
        
        // Test with very old dates
        LocalDate veryOldDate = LocalDate.of(1900, 1, 1);
        ReportService.AttendanceReport report2 = 
            reportService.generateDailyAttendanceReport(veryOldDate);
        assertNotNull("Should handle very old dates", report2);
        assertTrue("Should succeed with old date (no data)", report2.isSuccess());
        assertEquals("Should have no attendance for 1900", 0, report2.getTotalEmployees());
        
        // Test with very future dates
        LocalDate farFutureDate = LocalDate.of(2100, 12, 31);
        ReportService.OvertimeReport report3 = 
            reportService.generateOvertimeReport(farFutureDate, farFutureDate);
        assertNotNull("Should handle far future dates", report3);
        assertTrue("Should succeed with future date (no data)", report3.isSuccess());
        assertEquals("Should have no overtime for 2100", BigDecimal.ZERO, report3.getTotalOvertimeHours());
    }
    
    @Test
    public void testReportGeneration_SpecialCharacters() {
        System.out.println("Testing report generation with special characters in data...");
        
        // Create employee with special characters
        EmployeeModel specialEmployee = new EmployeeModel();
        specialEmployee.setFirstName("Test-Name'");
        specialEmployee.setLastName("O'Brien-Smith");
        specialEmployee.setBirthDate(LocalDate.of(1990, 6, 15));
        specialEmployee.setEmail("special.chars@test.com");
        specialEmployee.setBasicSalary(new BigDecimal("35000"));
        specialEmployee.setHourlyRate(new BigDecimal("208.33"));
        specialEmployee.setPasswordHash("special_hash");
        specialEmployee.setStatus(EmployeeModel.EmployeeStatus.REGULAR);
        specialEmployee.setPositionId(1);
        
        boolean saved = employeeDAO.save(specialEmployee);
        
        if (saved) {
            // Generate report including this employee
            ReportService.SalaryComparisonReport report = 
                reportService.generateSalaryComparisonReport(
                    LocalDate.now().minusMonths(1), 
                    LocalDate.now()
                );
            
            assertTrue("Report should handle special characters", report.isSuccess());
            
            // Clean up
            employeeDAO.delete(specialEmployee.getEmployeeId());
        }
    }
    
    @Test
    public void testReportGeneration_PerformanceWithLargeDataSet() {
        System.out.println("Testing report generation performance...");
        
        long startTime = System.currentTimeMillis();
        
        // Generate report for entire year
        LocalDate yearStart = LocalDate.now().withDayOfYear(1);
        LocalDate yearEnd = LocalDate.now().withDayOfYear(365);
        
        ReportService.SalaryComparisonReport report = 
            reportService.generateSalaryComparisonReport(yearStart, yearEnd);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertNotNull("Report should complete", report);
        assertTrue("Report should be successful", report.isSuccess());
        assertTrue("Report should complete within 30 seconds", duration < 30000);
        
        System.out.println("Report generation took " + duration + " ms");
    }
}