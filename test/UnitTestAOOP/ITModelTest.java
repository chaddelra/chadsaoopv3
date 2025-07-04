/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package UnitTestAOOP;

import Models.ITModel;
import Models.EmployeeModel;
import Models.UserAuthenticationModel;
import DAOs.DatabaseConnection;
import DAOs.EmployeeDAO;
import DAOs.UserAuthenticationDAO;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author i-Gits <lr.jdelasarmas@mmdc.mcl.edu.ph>
 */
 
public class ITModelTest {
    
    private ITModel itModel;
    private EmployeeModel itEmployee;
    private DatabaseConnection dbConnection;
    private EmployeeDAO employeeDAO;
    private UserAuthenticationDAO userAuthDAO;
    
    // Test data
    private static final int IT_EMPLOYEE_ID = 9999;
    private static final String IT_FIRST_NAME = "Test";
    private static final String IT_LAST_NAME = "ITUser";
    private static final String IT_EMAIL = "testit@motorph.com";
    private static final String IT_ROLE = "IT";
    
    @BeforeClass
    public static void setUpClass() {
        System.out.println("Starting ITModel Test Suite...");
    }
    
    @AfterClass
    public static void tearDownClass() {
        System.out.println("ITModel Test Suite completed.");
    }
    
    @Before
    public void setUp() {
        // Initialize database connection and DAOs
        dbConnection = new DatabaseConnection();
        employeeDAO = new EmployeeDAO(dbConnection);
        userAuthDAO = new UserAuthenticationDAO();
        
        // Create test IT employee
        itEmployee = new EmployeeModel();
        itEmployee.setEmployeeId(IT_EMPLOYEE_ID);
        itEmployee.setFirstName(IT_FIRST_NAME);
        itEmployee.setLastName(IT_LAST_NAME);
        itEmployee.setEmail(IT_EMAIL);
        itEmployee.setUserRole(IT_ROLE);
        itEmployee.setStatus(EmployeeModel.EmployeeStatus.REGULAR);
        itEmployee.setBasicSalary(new BigDecimal("50000"));
        itEmployee.setBirthDate(LocalDate.of(1990, 1, 1));
        itEmployee.setPositionId(1);
        
        // Initialize ITModel
        itModel = new ITModel(itEmployee);
    }
    
    @After
    public void tearDown() {
        // Clean up any test data created during tests
        cleanupTestData();
    }
    
    // =====================================================
    // ROLE-BASED ACCESS CONTROL TESTS
    // =====================================================
    
    // POSITIVE TESTS
    
    @Test
    public void testITCanManageUsers() {
        System.out.println("Testing: IT can manage users");
        
        // IT should have MANAGE_USERS permission
        String[] permissions = itModel.getITPermissions();
        boolean hasPermission = false;
        for (String perm : permissions) {
            if ("MANAGE_USERS".equals(perm)) {
                hasPermission = true;
                break;
            }
        }
        
        assertTrue("IT should have MANAGE_USERS permission", hasPermission);
    }
    
    @Test
    public void testITCanPerformDatabaseOperations() {
        System.out.println("Testing: IT can perform database operations");
        
        ITModel.ITOperationResult result = itModel.checkDatabaseHealth();
        
        assertNotNull("Database health check should return a result", result);
        // Note: Success depends on actual database connection
        System.out.println("Database health check message: " + result.getMessage());
    }
    
    @Test
    public void testITCanGenerateSystemReports() {
        System.out.println("Testing: IT can generate system reports");
        
        ITModel.ITOperationResult result = itModel.generateSystemHealthReport();
        
        assertNotNull("System health report should return a result", result);
        System.out.println("System health report generated: " + result.isSuccess());
    }
    
    // NEGATIVE TESTS - Access Control
    
    
    // =====================================================
    // USER ACCOUNT MANAGEMENT TESTS
    // =====================================================
    
    // POSITIVE TESTS
    
    @Test
    public void testCreateSystemUser_Valid() {
        System.out.println("Testing: Create system user with valid data");
        
        String testEmail = "testuser" + System.currentTimeMillis() + "@motorph.com";
        ITModel.ITOperationResult result = itModel.createSystemUser(
            testEmail, 
            "ValidPass123!", 
            "Employee",
            "Test", 
            "User", 
            1
        );
        
        // Note: May fail if database is not accessible
        System.out.println("Create user result: " + result.getMessage());
        
        if (result.isSuccess()) {
            // Clean up - try to deactivate the created user
            EmployeeModel employee = employeeDAO.findByEmail(testEmail);
            if (employee != null) {
                itModel.deactivateUserAccount(employee.getEmployeeId(), "Test cleanup");
            }
        }
    }
    
    @Test
    public void testResetUserPassword_Valid() {
        System.out.println("Testing: Reset password with valid temporary password");
        
        // This test requires an existing employee ID
        ITModel.ITOperationResult result = itModel.resetUserPassword(
            1, // Assuming employee ID 1 exists
            "TempPassword123!"
        );
        
        System.out.println("Password reset result: " + result.getMessage());
        // Success depends on whether employee ID 1 exists in database
    }
    
    @Test
    public void testUpdateUserRole_Valid() {
        System.out.println("Testing: Update user role to valid role");
        
        ITModel.ITOperationResult result = itModel.updateUserRole(
            1, // Assuming employee ID 1 exists
            "HR"
        );
        
        System.out.println("Role update result: " + result.getMessage());
        // Success depends on whether employee ID 1 exists in database
    }
    
    // NEGATIVE TESTS - User Management
    
    @Test
    public void testCreateSystemUser_DuplicateEmail() {
        System.out.println("Testing: Create user with duplicate email");
        
        // Try to create user with existing email
        ITModel.ITOperationResult result = itModel.createSystemUser(
            IT_EMAIL, // Using IT user's email which should exist
            "ValidPass123!", 
            "Employee",
            "Duplicate", 
            "User", 
            1
        );
        
        assertFalse("Should not create user with duplicate email", result.isSuccess());
        assertTrue("Should indicate email already exists", 
            result.getMessage().contains("Email already exists"));
    }
    
    @Test
    public void testCreateSystemUser_InvalidPassword() {
        System.out.println("Testing: Create user with invalid password");
        
        String testEmail = "weakpass" + System.currentTimeMillis() + "@motorph.com";
        
        // Test with weak password
        ITModel.ITOperationResult result = itModel.createSystemUser(
            testEmail,
            "weak", // Too short, no special chars, etc.
            "Employee",
            "Weak", 
            "Password", 
            1
        );
        
        assertFalse("Should not create user with weak password", result.isSuccess());
        assertTrue("Should indicate password requirements not met", 
            result.getMessage().contains("Password does not meet requirements"));
    }
    
    @Test
    public void testCreateSystemUser_InvalidRole() {
        System.out.println("Testing: Create user with invalid role");
        
        String testEmail = "invalidrole" + System.currentTimeMillis() + "@motorph.com";
        
        ITModel.ITOperationResult result = itModel.createSystemUser(
            testEmail,
            "ValidPass123!",
            "SuperAdmin", // Invalid role
            "Invalid", 
            "Role", 
            1
        );
        
        assertFalse("Should not create user with invalid role", result.isSuccess());
        assertTrue("Should indicate invalid role", 
            result.getMessage().contains("Invalid user role"));
    }
    
    @Test
    public void testResetPassword_NonExistentEmployee() {
        System.out.println("Testing: Reset password for non-existent employee");
        
        ITModel.ITOperationResult result = itModel.resetUserPassword(
            99999, // Non-existent employee ID
            "TempPassword123!"
        );
        
        assertFalse("Should not reset password for non-existent employee", result.isSuccess());
        assertTrue("Should indicate employee not found", 
            result.getMessage().contains("Employee not found"));
    }
    
    @Test
    public void testDeactivateAccount_NonExistentEmployee() {
        System.out.println("Testing: Deactivate non-existent employee account");
        
        ITModel.ITOperationResult result = itModel.deactivateUserAccount(
            99999, // Non-existent employee ID
            "Test reason"
        );
        
        assertFalse("Should not deactivate non-existent employee", result.isSuccess());
        assertTrue("Should indicate employee not found", 
            result.getMessage().contains("Employee not found"));
    }
    
    @Test
    public void testUpdateRole_InvalidRole() {
        System.out.println("Testing: Update user to invalid role");
        
        ITModel.ITOperationResult result = itModel.updateUserRole(
            1, // Assuming employee ID 1 exists
            "CEO" // Invalid role
        );
        
        assertFalse("Should not update to invalid role", result.isSuccess());
        assertTrue("Should indicate invalid role", 
            result.getMessage().contains("Invalid user role"));
    }
    
    // =====================================================
    // DATABASE OPERATIONS TESTS
    // =====================================================
    
    @Test
    public void testDatabaseHealthCheck() {
        System.out.println("Testing: Database health check");
        
        ITModel.ITOperationResult result = itModel.checkDatabaseHealth();
        
        assertNotNull("Database health check should return result", result);
        assertNotNull("Should have a message", result.getMessage());
        
        // The actual success depends on database availability
        System.out.println("Database health: " + result.getMessage());
    }
    
    @Test
    public void testGetDatabaseStatistics() {
        System.out.println("Testing: Get database statistics");
        
        ITModel.ITOperationResult result = itModel.getDatabaseStatistics();
        
        assertNotNull("Database statistics should return result", result);
        
        if (result.isSuccess()) {
            assertNotNull("Should have statistics data", result.getDatabaseStats());
            assertTrue("Statistics should contain table info", 
                result.getDatabaseStats().contains("EMPLOYEE"));
        }
    }
    
    @Test
    public void testDatabaseBackup_InvalidPath() {
        System.out.println("Testing: Database backup with potentially invalid path");
        
        ITModel.ITOperationResult result = itModel.performDatabaseBackup("///invalid\\path::file.sql");
        
        // This is a simplified test - in real implementation, 
        // invalid paths should be handled properly
        assertNotNull("Should return a result even with invalid path", result);
    }
    
    // =====================================================
    // SECURITY MANAGEMENT TESTS
    // =====================================================
    
    @Test
    public void testGeneratePasswordResetToken_Valid() {
        System.out.println("Testing: Generate password reset token for valid employee");
        
        ITModel.ITOperationResult result = itModel.generatePasswordResetToken(1);
        
        System.out.println("Reset token generation: " + result.getMessage());
        
        if (result.isSuccess()) {
            assertNotNull("Should have reset token", result.getResetToken());
            assertFalse("Token should not be empty", result.getResetToken().isEmpty());
        }
    }
    
    @Test
    public void testGeneratePasswordResetToken_InvalidEmployee() {
        System.out.println("Testing: Generate reset token for invalid employee");
        
        ITModel.ITOperationResult result = itModel.generatePasswordResetToken(99999);
        
        assertFalse("Should not generate token for non-existent employee", result.isSuccess());
        assertTrue("Should indicate employee not found", 
            result.getMessage().contains("Employee not found"));
    }
    
    // =====================================================
    // SYSTEM MONITORING TESTS
    // =====================================================
    
    @Test
    public void testSystemHealthReport() {
        System.out.println("Testing: Generate system health report");
        
        ITModel.ITOperationResult result = itModel.generateSystemHealthReport();
        
        assertNotNull("System health report should return result", result);
        
        if (result.isSuccess()) {
            assertNotNull("Should have health report data", result.getSystemHealthReport());
            assertTrue("Report should contain system info", 
                result.getSystemHealthReport().contains("SYSTEM HEALTH REPORT"));
        }
    }
    
    // =====================================================
    // EDGE CASES AND BOUNDARY TESTS
    // =====================================================
    
 
    @Test
    public void testCreateUser_EmptyStrings() {
        System.out.println("Testing: Create user with empty strings");
        
        ITModel.ITOperationResult result = itModel.createSystemUser(
            "", // empty email
            "ValidPass123!",
            "Employee",
            "", // empty first name
            "", // empty last name
            1
        );
        
        assertFalse("Should not create user with empty required fields", result.isSuccess());
    }
    
 
    @Test
    public void testGetUsersByRole() {
        System.out.println("Testing: Get users by specific role");
        
        List<EmployeeModel> itUsers = itModel.getUsersByRole("IT");
        
        assertNotNull("Should return a list (even if empty)", itUsers);
        System.out.println("Found " + itUsers.size() + " IT users");
    }
    
    @Test
    public void testGetAllSystemUsers() {
        System.out.println("Testing: Get all system users");
        
        List<UserAuthenticationModel> users = itModel.getAllSystemUsers();
        
        assertNotNull("Should return a list (even if empty)", users);
        System.out.println("Total system users: " + users.size());
    }
    
    // =====================================================
    // SPECIAL CHARACTER AND INJECTION TESTS
    // =====================================================
    
    @Test
    public void testCreateUser_SpecialCharactersInName() {
        System.out.println("Testing: Create user with special characters in name");
        
        String testEmail = "special" + System.currentTimeMillis() + "@motorph.com";
        
        ITModel.ITOperationResult result = itModel.createSystemUser(
            testEmail,
            "ValidPass123!",
            "Employee",
            "Test'; DROP TABLE employee;--", // SQL injection attempt
            "User<script>alert('xss')</script>", // XSS attempt
            1
        );
        
        // The system should either handle these safely or reject them
        System.out.println("Special character test result: " + result.getMessage());
        
        if (result.isSuccess()) {
            // Clean up
            EmployeeModel employee = employeeDAO.findByEmail(testEmail);
            if (employee != null) {
                itModel.deactivateUserAccount(employee.getEmployeeId(), "Test cleanup");
            }
        }
    }
    
    @Test
    public void testInvalidEmailFormats() {
        System.out.println("Testing: Various invalid email formats");
        
        String[] invalidEmails = {
            "notanemail",
            "@motorph.com",
            "user@",
            "user@@motorph.com",
            "user @motorph.com",
            "user@motorph",
            ".user@motorph.com",
            "user.@motorph.com"
        };
        
        for (String invalidEmail : invalidEmails) {
            ITModel.ITOperationResult result = itModel.createSystemUser(
                invalidEmail,
                "ValidPass123!",
                "Employee",
                "Test",
                "User",
                1
            );
            
            // Should either fail or handle gracefully
            System.out.println("Email '" + invalidEmail + "' result: " + result.isSuccess());
        }
    }
    
    // =====================================================
    // HELPER METHODS
    // =====================================================
    
    private void cleanupTestData() {
        // Clean up any test data created during tests
        try (Connection conn = dbConnection.createConnection()) {
            // Be careful with cleanup - only remove test data
            System.out.println("Cleaning up test data...");
        } catch (SQLException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }
    
    @Test
    public void testITModelToString() {
        System.out.println("Testing: ITModel toString method");
        
        String itModelString = itModel.toString();
        
        assertNotNull("toString should not return null", itModelString);
        assertTrue("Should contain employee ID", itModelString.contains("employeeId=" + IT_EMPLOYEE_ID));
        assertTrue("Should contain name", itModelString.contains(IT_FIRST_NAME + " " + IT_LAST_NAME));
        assertTrue("Should contain email", itModelString.contains(IT_EMAIL));
    }
    
    @Test
    public void testITOperationResultToString() {
        System.out.println("Testing: ITOperationResult toString method");
        
        ITModel.ITOperationResult result = new ITModel.ITOperationResult();
        result.setSuccess(true);
        result.setMessage("Test message");
        
        String resultString = result.toString();
        
        assertNotNull("toString should not return null", resultString);
        assertTrue("Should contain success status", resultString.contains("success=true"));
        assertTrue("Should contain message", resultString.contains("Test message"));
    }
}
