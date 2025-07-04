package UnitTestAOOP;

import Models.UserAuthenticationModel;
import Models.EmployeeModel;
import Models.HRModel;
import Models.ITModel;
import Models.AccountingModel;
import Models.ImmediateSupervisorModel;
import DAOs.DatabaseConnection;
import DAOs.EmployeeDAO;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/**
 * Focused JUnit test for UserAuthenticationModel
 */
public class UserAuthenticationTest {
    
    private UserAuthenticationModel authModel;
    private static DatabaseConnection dbConnection;
    private static EmployeeDAO employeeDAO;
    
    // Test data constants
    private static final String TEST_EMAIL = "test.user@company.com";
    private static final String TEST_PASSWORD = "TestPass123!";
    private static final String WRONG_PASSWORD = "WrongPass123!";
    private static final String HR_EMAIL = "hr.manager@company.com";
    private static final String IT_EMAIL = "it.admin@company.com";
    private static final String SUPERVISOR_EMAIL = "supervisor@company.com";
    private static final String ACCOUNTING_EMAIL = "accounting@company.com";
    private static final String LOCKED_EMAIL = "locked.user@company.com";
    private static final String UNKNOWN_EMAIL = "unknown.user@company.com";
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("=== Setting up test class ===");
        dbConnection = new DatabaseConnection();
        employeeDAO = new EmployeeDAO(dbConnection);
        
        // Clean up any existing test data
        cleanupTestData();
        
        // Create test employees
        createTestEmployees();
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("=== Tearing down test class ===");
        cleanupTestData();
    }
    
    @Before
    public void setUp() {
        System.out.println("\n--- Setting up test ---");
        authModel = new UserAuthenticationModel(dbConnection);
    }
    
    @After
    public void tearDown() {
        System.out.println("--- Tearing down test ---");
        if (authModel != null && authModel.isUserLoggedIn()) {
            authModel.logout();
        }
    }
    
    // Helper method to hash password the same way UserAuthenticationModel does
    private static String hashPasswordLikeAuth(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    // ========================================
    // CORE AUTHENTICATION TESTS
    // ========================================
    
    @Test
    public void testValidLogin() {
        System.out.println("Testing valid login...");
        
        // Act
        boolean loginResult = authModel.login(TEST_EMAIL, TEST_PASSWORD);
        
        // Assert - Testing state
        assertTrue("Valid login should succeed", loginResult);
        assertTrue("User should be logged in", authModel.isUserLoggedIn());
        assertEquals("Email should match", TEST_EMAIL, authModel.getEmail());
        assertNotNull("Session token should be generated", authModel.getSessionToken());
        assertNotNull("User object should be created", authModel.getUserObject());
    }
    
    @Test
    public void testInvalidLogin_incorrectPassword() {
        System.out.println("Testing invalid login - NEGATIVE TEST...");
        
        // Act
        boolean loginResult = authModel.login(TEST_EMAIL, WRONG_PASSWORD);
        
        // Assert - Testing security
        assertFalse("Login with wrong password should fail", loginResult);
        assertFalse("User should not be logged in", authModel.isUserLoggedIn());
        assertNull("Session token should not be generated", authModel.getSessionToken());
        assertTrue("Login attempts should increase", authModel.getLoginAttempts() > 0);
    }
    
    @Test
    public void testInvalidLogin_unknownUser() {
        System.out.println("Testing unknown user login - NEGATIVE TEST...");
        
        // Act
        boolean loginResult = authModel.login(UNKNOWN_EMAIL, TEST_PASSWORD);
        
        // Assert
        assertFalse("Login with unknown email should fail", loginResult);
        assertFalse("User should not be logged in", authModel.isUserLoggedIn());
        assertEquals("Login attempts should increase", 1, authModel.getLoginAttempts());
    }
    
    @Test
    public void testInvalidLogin_nullCredentials() {
        System.out.println("Testing null input handling - NEGATIVE TEST...");
        
        // Test defensive programming and input validation
        boolean loginResult = authModel.login(null, TEST_PASSWORD);
        assertFalse("Login with null email should fail", loginResult);
        
        authModel = new UserAuthenticationModel(dbConnection);
        loginResult = authModel.login(TEST_EMAIL, null);
        assertFalse("Login with null password should fail", loginResult);
    }
    
    @Test
    public void testLockedAccountLogin() {
        System.out.println("Testing account locking mechanism - NEGATIVE TEST...");
        
        // Demonstrate state management and security
        for (int i = 0; i < 5; i++) {
            authModel.login(LOCKED_EMAIL, WRONG_PASSWORD);
        }
        
        assertTrue("Account should be locked after 5 failed attempts", authModel.isAccountLocked());
        
        boolean loginResult = authModel.login(LOCKED_EMAIL, TEST_PASSWORD);
        assertFalse("Login to locked account should fail", loginResult);
    }
    

    @Test
    public void testRoleAssignmentOnLogin() {
        System.out.println("Testing role assignment...");
        
        // Test polymorphic behavior - same login method, different user types
        
        // HR role - child of EmployeeModel
        authModel.login(HR_EMAIL, TEST_PASSWORD);
        assertEquals("HR role should be assigned", "HR", authModel.getUserRole());
        assertTrue("Should create HRModel instance", authModel.getUserObject() instanceof HRModel);
        assertTrue("HRModel is also an EmployeeModel", authModel.getUserObject() instanceof EmployeeModel);
        authModel.logout();
        
        // IT role - child of EmployeeModel
        authModel.login(IT_EMAIL, TEST_PASSWORD);
        assertEquals("IT role should be assigned", "IT", authModel.getUserRole());
        assertTrue("Should create ITModel instance", authModel.getUserObject() instanceof ITModel);
        assertTrue("ITModel is also an EmployeeModel", authModel.getUserObject() instanceof EmployeeModel);
        authModel.logout();
        
        // Supervisor role - child of EmployeeModel
        authModel.login(SUPERVISOR_EMAIL, TEST_PASSWORD);
        assertEquals("Supervisor role should be assigned", "IMMEDIATESUPERVISOR", authModel.getUserRole());
        assertTrue("Should create ImmediateSupervisorModel", authModel.getUserObject() instanceof ImmediateSupervisorModel);
        authModel.logout();
        
        // Base Employee role
        authModel.login(TEST_EMAIL, TEST_PASSWORD);
        assertEquals("Employee role should be assigned", "EMPLOYEE", authModel.getUserRole());
        assertTrue("Should create EmployeeModel", authModel.getUserObject() instanceof EmployeeModel);
    }
    
    @Test
    public void testPolymorphicModelAccess() {
        System.out.println("Testing polymorphic model access...");
        
        // Login as HR
        authModel.login(HR_EMAIL, TEST_PASSWORD);
        
        // Test polymorphic access methods
        HRModel hrModel = authModel.getAsHRModel();
        assertNotNull("Should get HR model through specific cast", hrModel);
        
        EmployeeModel empModel = authModel.getAsEmployeeModel();
        assertNotNull("Should get Employee model (parent class)", empModel);
        
        // Test that same object can be accessed as parent or child type
        assertTrue("Both references point to same object", hrModel == empModel);
        
        // Test wrong cast returns null (type safety)
        ITModel itModel = authModel.getAsITModel();
        assertNull("Should not get IT model when logged in as HR", itModel);
    }
    
    
    @Test
    public void testPermissionAbstraction() {
        System.out.println("Testing permission abstraction...");
        
        // Test abstracted permission system
        
        // HR permissions
        authModel.login(HR_EMAIL, TEST_PASSWORD);
        assertTrue("HR has admin permission", authModel.hasPermission("ADMIN"));
        assertTrue("HR has HR_ACCESS permission", authModel.hasPermission("HR_ACCESS"));
        assertTrue("HR is identified as admin", authModel.isAdmin());
        authModel.logout();
        
        // Employee permissions (negative test)
        authModel.login(TEST_EMAIL, TEST_PASSWORD);
        assertFalse("Employee does NOT have admin permission", authModel.hasPermission("ADMIN"));
        assertTrue("Employee has basic access", authModel.hasPermission("EMPLOYEE_ACCESS"));
        assertFalse("Employee is NOT admin", authModel.isAdmin());
    }
    
    @Test
    public void testSessionManagement() {
        System.out.println("Testing session management...");
        
        // Login
        authModel.login(TEST_EMAIL, TEST_PASSWORD);
        
        // Test encapsulated session state
        assertTrue("Session should be valid", authModel.isSessionValid());
        assertNotNull("Session token exists", authModel.getSessionToken());
        assertTrue("Session has expiry time", authModel.getMinutesUntilExpiry() > 0);
        
        // Test session modification
        authModel.extendSession(30);
        assertTrue("Extended session remains valid", authModel.isSessionValid());
        
        // Logout and verify state change
        authModel.logout();
        assertFalse("Session invalid after logout", authModel.isSessionValid());
        assertNull("Session token cleared", authModel.getSessionToken());
    }
    
    // ========================================
    // COMPOSITION AND AGGREGATION TESTS
    // ========================================
    
    @Test
    public void testUserModelComposition() {
        System.out.println("Testing model composition...");
        
        // Test that UserAuthenticationModel properly composes different model types
        
        authModel.login(IT_EMAIL, TEST_PASSWORD);
        
        // UserAuthenticationModel HAS-A relationship with specific model
        Object userObject = authModel.getUserObject();
        assertNotNull("User object should be composed", userObject);
        assertEquals("Model type should be ITModel", "ITModel", authModel.getUserModelType());
        
        // Test model-specific functionality through composition
        ITModel itModel = authModel.getAsITModel();
        assertNotNull("IT model accessible through composition", itModel);
        assertEquals("Composed model has correct data", IT_EMAIL, itModel.getEmail());
    }
    
    // ========================================
    // SOLID PRINCIPLES DEMONSTRATION
    // ========================================
    
    @Test
    public void testSingleResponsibility() {
        System.out.println("Testing Single Responsibility - Each class has one job...");
        
        // UserAuthenticationModel handles authentication
        boolean loginResult = authModel.login(TEST_EMAIL, TEST_PASSWORD);
        assertTrue("Authentication class handles login", loginResult);
        
        // EmployeeDAO handles database operations (through getUserByEmployeeId)
        EmployeeModel emp = employeeDAO.findByEmail(TEST_EMAIL);
        Object retrieved = authModel.getUserByEmployeeId(emp.getEmployeeId());
        assertNotNull("DAO handles data retrieval", retrieved);
    }
    
    @Test
    public void testOpenClosedPrinciple() {
        System.out.println("Testing Open/Closed - Extension without modification...");
        
        // System is open for extension (new roles) but closed for modification
        // Each role extends EmployeeModel without modifying parent
        
        authModel.login(ACCOUNTING_EMAIL, TEST_PASSWORD);
        assertTrue("New role (Accounting) works without modifying base", 
            authModel.getUserObject() instanceof AccountingModel);
        assertTrue("Still maintains parent relationship", 
            authModel.getUserObject() instanceof EmployeeModel);
    }
    
    // ========================================
    // EXCEPTION HANDLING AND EDGE CASES
    // ========================================
    
    @Test
    public void testExceptionHandling() {
        System.out.println("Testing exception handling and recovery...");
        
        // Test system resilience
        try {
            // Try to get non-existent user
            Object user = authModel.getUserByEmployeeId(Integer.MAX_VALUE);
            assertNull("Should handle non-existent ID gracefully", user);
        } catch (Exception e) {
            fail("Should not throw exception for invalid ID");
        }
        
        // System should still function after error
        boolean loginResult = authModel.login(TEST_EMAIL, TEST_PASSWORD);
        assertTrue("System recovers after error", loginResult);
    }
    
    @Test
    public void testNullSafety() {
        System.out.println("Testing null safety - Defensive programming...");
        
        // Fresh instance with no login
        UserAuthenticationModel freshModel = new UserAuthenticationModel(dbConnection);
        
        // All methods should handle null state gracefully
        assertFalse("isSessionValid handles null", freshModel.isSessionValid());
        assertEquals("getFullName handles null", "", freshModel.getFullName());
        assertEquals("getDisplayRole handles null", "Guest", freshModel.getDisplayRole());
        assertFalse("hasPermission handles null", freshModel.hasPermission("ANY"));
        assertEquals("getUserModelType handles null", "None", freshModel.getUserModelType());
    }
    
    // ========================================
    // DESIGN PATTERN TESTS
    // ========================================
    
    @Test
    public void testFactoryPattern() {
        System.out.println("Testing Factory Pattern - Object creation based on role...");
        
        // UserAuthenticationModel acts as a factory for creating appropriate model objects
        
        // Creates HRModel for HR role
        authModel.login(HR_EMAIL, TEST_PASSWORD);
        assertTrue("Factory creates HRModel", authModel.getUserObject() instanceof HRModel);
        authModel.logout();
        
        // Creates ITModel for IT role
        authModel.login(IT_EMAIL, TEST_PASSWORD);
        assertTrue("Factory creates ITModel", authModel.getUserObject() instanceof ITModel);
        authModel.logout();
        
        // Creates base EmployeeModel for regular employee
        authModel.login(TEST_EMAIL, TEST_PASSWORD);
        assertTrue("Factory creates EmployeeModel", authModel.getUserObject() instanceof EmployeeModel);
        assertFalse("Not a specialized model", authModel.getUserObject() instanceof HRModel);
    }
    
    // ========================================
    // INTEGRATION TEST
    // ========================================
    
    @Test
    public void testCompleteUserLifecycle() {
        System.out.println("Testing complete user lifecycle - Integration test...");
        
        // 1. Failed login attempt
        boolean failedLogin = authModel.login(TEST_EMAIL, WRONG_PASSWORD);
        assertFalse("Failed login recorded", failedLogin);
        assertEquals("Login attempt counted", 1, authModel.getLoginAttempts());
        
        // 2. Successful login
        boolean successLogin = authModel.login(TEST_EMAIL, TEST_PASSWORD);
        assertTrue("Successful login", successLogin);
        assertEquals("Login attempts reset", 0, authModel.getLoginAttempts());
        
        // 3. Active session
        assertTrue("Session active", authModel.isSessionValid());
        assertTrue("User logged in", authModel.isUserLoggedIn());
        
        // 4. Use permissions
        assertTrue("Has employee permissions", authModel.hasPermission("EMPLOYEE_ACCESS"));
        
        // 5. Logout
        authModel.logout();
        assertFalse("Logged out successfully", authModel.isUserLoggedIn());
        assertNull("Session cleared", authModel.getSessionToken());
    }
    
    // ========================================
    // HELPER METHODS
    // ========================================
    
    private static void createTestEmployees() {
        System.out.println("Creating test employees...");
        
        createTestEmployee(TEST_EMAIL, "Test", "User", "Employee", 1);
        createTestEmployee(HR_EMAIL, "HR", "Manager", "HR", 2);
        createTestEmployee(IT_EMAIL, "IT", "Admin", "IT", 3);
        createTestEmployee(SUPERVISOR_EMAIL, "Super", "Visor", "ImmediateSupervisor", 4);
        createTestEmployee(ACCOUNTING_EMAIL, "Account", "Manager", "Accounting", 5);
        createTestEmployee(LOCKED_EMAIL, "Locked", "User", "Employee", 1);
    }
    
    private static void createTestEmployee(String email, String firstName, String lastName, 
                                          String role, int positionId) {
        EmployeeModel employee = new EmployeeModel();
        employee.setEmail(email);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setUserRole(role);
        employee.setPasswordHash(hashPasswordLikeAuth(TEST_PASSWORD));
        employee.setBirthDate(LocalDate.now().minusYears(30));
        employee.setPhoneNumber("1234567890");
        employee.setBasicSalary(new BigDecimal("50000.00"));
        employee.setHourlyRate(new BigDecimal("250.00"));
        employee.setStatus(EmployeeModel.EmployeeStatus.REGULAR);
        employee.setPositionId(positionId);
        
        employeeDAO.save(employee);
        System.out.println("Created: " + email + " (" + role + ")");
    }
    
    private static void cleanupTestData() {
        System.out.println("Cleaning up test data...");
        
        String[] testEmails = {
            TEST_EMAIL, HR_EMAIL, IT_EMAIL, SUPERVISOR_EMAIL, 
            ACCOUNTING_EMAIL, LOCKED_EMAIL
        };
        
        for (String email : testEmails) {
            EmployeeModel employee = employeeDAO.findByEmail(email);
            if (employee != null) {
                employeeDAO.delete(employee.getEmployeeId());
            }
        }
    }
}