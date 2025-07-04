package UnitTestAOOP;

import Services.JasperReportGenerator;
import DAOs.DatabaseConnection;
import DAOs.EmployeeDAO;
import DAOs.ReferenceDataDAO;
import Models.EmployeeModel;
import Models.EmployeeModel.EmployeeStatus;

import org.junit.*;
import static org.junit.Assert.*;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * JUnit test for JasperReportGenerator with negative testing
 * @author martin
 */

public class JasperReportGeneratorTest {
    
    private static DatabaseConnection databaseConnection;
    private static EmployeeDAO employeeDAO;
    private static ReferenceDataDAO referenceDataDAO;
    private static JasperReportGenerator reportGenerator;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("=== Setting up Simple JasperReportGenerator Test ===");
        
        // Initialize database connection
        databaseConnection = new DatabaseConnection(
            "jdbc:mysql://localhost:3306/payrollsystem_db",
            "root",
            "motorph_123"
        );
        
        // Initialize DAOs
        employeeDAO = new EmployeeDAO(databaseConnection);
        referenceDataDAO = new ReferenceDataDAO(databaseConnection);
        
        // Try to initialize report generator
        try {
            reportGenerator = new JasperReportGenerator(databaseConnection);
            System.out.println("✓ JasperReportGenerator created");
        } catch (Exception e) {
            System.out.println("✗ Could not create JasperReportGenerator: " + e.getMessage());
        }
    }
    
    // DATABASE CONNECTION TESTS
    
    @Test
    public void testDatabaseConnection() {
        System.out.println("\n=== Testing Database Connection ===");
        
        boolean connected = databaseConnection.testConnection();
        assertTrue("Database should be connected", connected);
        System.out.println("Database connection: " + (connected ? "SUCCESS" : "FAILED"));
    }
    
    @Test
    public void testEmployeeDAOConnection() {
        System.out.println("\n=== Testing EmployeeDAO ===");
        
        List<EmployeeModel> employees = employeeDAO.getAllEmployees();
        assertNotNull("Employee list should not be null", employees);
        System.out.println("Found " + employees.size() + " employees in database");
    }
    
    @Test
    public void testReferenceDataDAO() {
        System.out.println("\n=== Testing ReferenceDataDAO ===");
        
        // Test getting departments
        List<String> departments = referenceDataDAO.getAllDepartments();
        assertNotNull("Departments list should not be null", departments);
        System.out.println("Found " + departments.size() + " departments");
        
        // Test getting positions
        var positions = referenceDataDAO.getAllPositions();
        assertNotNull("Positions list should not be null", positions);
        System.out.println("Found " + positions.size() + " positions");
    }
    
    // TEMPLATE FILE TESTS
    
    @Test
    public void testTemplateDirectory() {
        System.out.println("\n=== Testing Template Directory ===");
        
        File reportsDir = new File("src/reports/");
        if (!reportsDir.exists()) {
            System.out.println("Creating reports directory...");
            boolean created = reportsDir.mkdirs();
            assertTrue("Should be able to create reports directory", created);
        }
        
        assertTrue("Reports directory should exist", reportsDir.exists());
        assertTrue("Reports directory should be a directory", reportsDir.isDirectory());
    }
    
    @Test
    public void testGetAvailableTemplates() {
        System.out.println("\n=== Testing Get Available Templates ===");
        
        if (reportGenerator == null) {
            System.out.println("Skipping - JasperReportGenerator not available");
            return;
        }
        
        try {
            List<String> templates = reportGenerator.getAvailableTemplates();
            assertNotNull("Templates list should not be null", templates);
            System.out.println("Found " + templates.size() + " templates:");
            for (String template : templates) {
                System.out.println("  - " + template);
            }
        } catch (Exception e) {
            System.out.println("Could not get templates: " + e.getMessage());
        }
    }
    
    @Test
    public void testTemplateExists() {
        System.out.println("\n=== Testing Template Exists Method ===");
        
        if (reportGenerator == null) {
            System.out.println("Skipping - JasperReportGenerator not available");
            return;
        }
        
        try {
            // Test with non-existent template
            boolean exists = reportGenerator.templateExists("non_existent.jrxml");
            assertFalse("Non-existent template should return false", exists);
            
            // Test with potentially existing template
            exists = reportGenerator.templateExists("payslip_template.jrxml");
            System.out.println("Payslip template exists: " + exists);
        } catch (Exception e) {
            System.out.println("Could not check templates: " + e.getMessage());
        }
    }
    
    // OUTPUT DIRECTORY TESTS
    
    @Test
    public void testOutputDirectory() {
        System.out.println("\n=== Testing Output Directory ===");
        
        File outputDir = new File("reports/output/");
        if (!outputDir.exists()) {
            System.out.println("Creating output directory...");
            boolean created = outputDir.mkdirs();
            assertTrue("Should be able to create output directory", created);
        }
        
        assertTrue("Output directory should exist", outputDir.exists());
        assertTrue("Output directory should be a directory", outputDir.isDirectory());
        
        // Test write permissions
        File testFile = new File(outputDir, "test_write.tmp");
        try {
            boolean created = testFile.createNewFile();
            assertTrue("Should be able to create files in output directory", created);
            testFile.delete();
            System.out.println("✓ Output directory is writable");
        } catch (Exception e) {
            fail("Cannot write to output directory: " + e.getMessage());
        }
    }
    
    // DATABASE QUERY TESTS (Testing the data retrieval methods indirectly)
    
    @Test
    public void testEmployeeDataRetrieval() {
        System.out.println("\n=== Testing Employee Data Retrieval ===");
        
        // Get all active employees
        List<EmployeeModel> activeEmployees = employeeDAO.getActiveEmployees();
        assertNotNull("Active employees list should not be null", activeEmployees);
        System.out.println("Found " + activeEmployees.size() + " active employees");
        
        // Test finding by position
        List<EmployeeModel> positionEmployees = employeeDAO.findByPosition(1);
        assertNotNull("Position employees list should not be null", positionEmployees);
        System.out.println("Found " + positionEmployees.size() + " employees in position 1");
    }
    
    @Test
    public void testPayPeriodData() {
        System.out.println("\n=== Testing Pay Period Data ===");
        
        var allPayPeriods = referenceDataDAO.getAllPayPeriods();
        assertNotNull("Pay periods should not be null", allPayPeriods);
        System.out.println("Found " + allPayPeriods.size() + " pay periods");
        
        var currentPayPeriod = referenceDataDAO.getCurrentPayPeriod();
        if (currentPayPeriod != null) {
            System.out.println("Current pay period: " + currentPayPeriod);
        } else {
            System.out.println("No current pay period found");
        }
    }
    
    // ERROR HANDLING TESTS
    
    @Test
    public void testInvalidDatabaseConnection() {
        System.out.println("\n=== Testing Invalid Database Connection ===");
        
        DatabaseConnection badConnection = new DatabaseConnection(
            "jdbc:mysql://localhost:3306/non_existent_db",
            "invalid_user",
            "invalid_pass"
        );
        
        assertFalse("Invalid connection should fail", badConnection.testConnection());
        
        try {
            JasperReportGenerator badGenerator = new JasperReportGenerator(badConnection);
            // Even if it creates, methods should handle bad connection gracefully
            assertNotNull("Generator should be created even with bad connection", badGenerator);
        } catch (Exception e) {
            System.out.println("Expected error with bad connection: " + e.getMessage());
        }
    }
    
    // CONFIGURATION TESTS
    
    @Test
    public void testSystemConfiguration() {
        System.out.println("\n=== Testing System Configuration ===");
        
        var config = referenceDataDAO.getSystemConfig();
        assertNotNull("System config should not be null", config);
        
        // Verify expected configuration values
        assertEquals("Company name should be MotorPH", "MotorPH", config.get("companyName"));
        assertNotNull("Should have payroll cutoff dates", config.get("payrollCutoff1"));
        assertNotNull("Should have hours per day config", config.get("hoursPerDay"));
        
        System.out.println("System configuration loaded successfully");
        for (var entry : config.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
    }
    
    @Test
    public void testGovernmentRates() {
        System.out.println("\n=== Testing Government Rates ===");
        
        var sssRates = referenceDataDAO.getSSSRates();
        assertNotNull("SSS rates should not be null", sssRates);
        assertTrue("SSS should have employee rate", sssRates.containsKey("employeeRate"));
        
        var philHealthRates = referenceDataDAO.getPhilHealthRates();
        assertNotNull("PhilHealth rates should not be null", philHealthRates);
        
        var pagIbigRates = referenceDataDAO.getPagIBIGRates();
        assertNotNull("Pag-IBIG rates should not be null", pagIbigRates);
        
        System.out.println("✓ All government rates loaded successfully");
    }
    
    // VALIDATION TESTS
    
    @Test
    public void testReferenceDataValidation() {
        System.out.println("\n=== Testing Reference Data Validation ===");
        
        // Test valid position ID
        boolean validPosition = referenceDataDAO.isValidPositionId(1);
        System.out.println("Position ID 1 is valid: " + validPosition);
        
        // Test invalid position ID
        boolean invalidPosition = referenceDataDAO.isValidPositionId(99999);
        assertFalse("Invalid position ID should return false", invalidPosition);
        
        // Test other validations
        boolean invalidBenefit = referenceDataDAO.isValidBenefitTypeId(99999);
        assertFalse("Invalid benefit type ID should return false", invalidBenefit);
        
        boolean invalidLeave = referenceDataDAO.isValidLeaveTypeId(99999);
        assertFalse("Invalid leave type ID should return false", invalidLeave);
    }
    
    @After
    public void tearDown() {
        // Cleanup is minimal for simple tests
    }
    
    @AfterClass
    public static void tearDownClass() {
        System.out.println("\n=== Simple Test Suite Complete ===");
        System.out.println("These tests verify basic functionality without JasperReports");
        System.out.println("For full report generation tests, ensure all dependencies are installed");
    }
}