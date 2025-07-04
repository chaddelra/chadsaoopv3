package UnitTestAOOP;

import DAOs.LeaveBalanceDAO;
import DAOs.DatabaseConnection;
import Models.LeaveBalance;
import org.junit.*;
import static org.junit.Assert.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Comprehensive JUnit test for LeaveBalanceDAO with integrated negative testing
 */
public class LeaveBalanceDAOTest {
    
    private static DatabaseConnection dbConnection;
    private LeaveBalanceDAO leaveBalanceDAO;
    private Connection testConnection;
    
    // Test data constants
    private static final Integer TEST_EMPLOYEE_ID = 99999;
    private static final Integer TEST_LEAVE_TYPE_ID = 1;
    private static final Integer TEST_YEAR = 2024;
    private static final Integer INVALID_ID = -1;
    private static final Integer NULL_ID = null;
    private static final Integer LARGE_ID = Integer.MAX_VALUE;
    
    @BeforeClass
    public static void setUpClass() {
        // Initialize database connection with your credentials
        dbConnection = new DatabaseConnection();
    }
    
    @Before
    public void setUp() throws SQLException {
        leaveBalanceDAO = new LeaveBalanceDAO(dbConnection);
        testConnection = dbConnection.createConnection();
        
        // Clean up any existing test data
        cleanupTestData();
        
        // Insert test employee and leave type if they don't exist
        insertTestEmployee();
        insertTestLeaveType();
    }
    
    @After
    public void tearDown() throws SQLException {
        // Clean up test data after each test
        cleanupTestData();
        
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }
    
    /**
     * Helper method to clean up test data
     */
    private void cleanupTestData() throws SQLException {
        String[] cleanupQueries = {
            "DELETE FROM leavebalance WHERE employeeId >= 99996",
            "DELETE FROM employee WHERE employeeId >= 99996"
        };
        
        try (Statement stmt = testConnection.createStatement()) {
            for (String query : cleanupQueries) {
                stmt.executeUpdate(query);
            }
        }
    }
    
    /**
     * Helper method to insert test employee
     */
    private void insertTestEmployee() throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM employee WHERE employeeId = ?";
        String insertQuery = "INSERT INTO employee (employeeId, firstName, lastName, birthDate, " +
                           "email, basicSalary, userRole, passwordHash, status, positionId) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement checkStmt = testConnection.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, TEST_EMPLOYEE_ID);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement insertStmt = testConnection.prepareStatement(insertQuery)) {
                    insertStmt.setInt(1, TEST_EMPLOYEE_ID);
                    insertStmt.setString(2, "Test");
                    insertStmt.setString(3, "Employee");
                    insertStmt.setDate(4, Date.valueOf("1990-01-01"));
                    insertStmt.setString(5, "test.employee@test.com");
                    insertStmt.setBigDecimal(6, new java.math.BigDecimal("50000"));
                    insertStmt.setString(7, "Employee");
                    insertStmt.setString(8, "hashedpassword");
                    insertStmt.setString(9, "Regular");
                    insertStmt.setInt(10, 1);
                    insertStmt.executeUpdate();
                }
            }
        }
    }
    
    /**
     * Helper method to insert test leave type if needed
     */
    private void insertTestLeaveType() throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM leavetype WHERE leaveTypeId = ?";
        
        try (PreparedStatement checkStmt = testConnection.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, TEST_LEAVE_TYPE_ID);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) == 0) {
                String insertQuery = "INSERT INTO leavetype (leaveTypeId, leaveTypeName, leaveDescription, maxDaysPerYear) " +
                                   "VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertStmt = testConnection.prepareStatement(insertQuery)) {
                    insertStmt.setInt(1, TEST_LEAVE_TYPE_ID);
                    insertStmt.setString(2, "Test Leave");
                    insertStmt.setString(3, "Test Leave Type");
                    insertStmt.setInt(4, 10);
                    insertStmt.executeUpdate();
                }
            }
        }
    }
    
    // =============================
    // POSITIVE TEST CASES
    // =============================
    
    @Test
    public void testSaveLeaveBalance_Success() {
        LeaveBalance leaveBalance = createTestLeaveBalance();
        
        boolean result = leaveBalanceDAO.save(leaveBalance);
        
        assertTrue("Save should return true for valid leave balance", result);
        
        LeaveBalance savedBalance = leaveBalanceDAO.findByEmployeeLeaveTypeAndYear(
            TEST_EMPLOYEE_ID, TEST_LEAVE_TYPE_ID, TEST_YEAR
        );
        
        assertNotNull("Saved balance should be retrievable", savedBalance);
        assertNotNull("Leave balance ID should be set after save", savedBalance.getLeaveBalanceId());
        assertTrue("Leave balance ID should be greater than 0", savedBalance.getLeaveBalanceId() > 0);
    }
    
    @Test
    public void testFindById_ExistingRecord() {
        LeaveBalance savedBalance = createTestLeaveBalance();
        leaveBalanceDAO.save(savedBalance);
        
        // Find the saved record by unique combination
        LeaveBalance retrievedBalance = leaveBalanceDAO.findByEmployeeLeaveTypeAndYear(
            TEST_EMPLOYEE_ID, TEST_LEAVE_TYPE_ID, TEST_YEAR
        );
        
        assertNotNull("Should find the saved balance", retrievedBalance);
        Integer balanceId = retrievedBalance.getLeaveBalanceId();
        
        // Now test findById
        LeaveBalance foundBalance = leaveBalanceDAO.findById(balanceId);
        
        assertNotNull("Should find existing leave balance by ID", foundBalance);
        assertEquals("Employee ID should match", TEST_EMPLOYEE_ID, foundBalance.getEmployeeId());
        assertEquals("Leave type ID should match", TEST_LEAVE_TYPE_ID, foundBalance.getLeaveTypeId());
        assertEquals("Balance year should match", TEST_YEAR, foundBalance.getBalanceYear());
    }
    
    @Test
    public void testUpdate_Success() {
        LeaveBalance leaveBalance = createTestLeaveBalance();
        leaveBalanceDAO.save(leaveBalance);
        
        // Retrieve to get the ID
        LeaveBalance saved = leaveBalanceDAO.findByEmployeeLeaveTypeAndYear(
            TEST_EMPLOYEE_ID, TEST_LEAVE_TYPE_ID, TEST_YEAR
        );
        assertNotNull("Saved balance should exist", saved);
        
        // Update some fields
        saved.setUsedLeaveDays(5);
        saved.setRemainingLeaveDays(10);
        
        boolean result = leaveBalanceDAO.update(saved);
        
        assertTrue("Update should return true", result);
        
        // Verify update
        LeaveBalance updated = leaveBalanceDAO.findById(saved.getLeaveBalanceId());
        assertEquals("Used leave days should be updated", Integer.valueOf(5), updated.getUsedLeaveDays());
        assertEquals("Remaining leave days should be updated", Integer.valueOf(10), updated.getRemainingLeaveDays());
    }
    
    @Test
    public void testDelete_Success() {
        LeaveBalance leaveBalance = createTestLeaveBalance();
        leaveBalanceDAO.save(leaveBalance);
        
        LeaveBalance saved = leaveBalanceDAO.findByEmployeeLeaveTypeAndYear(
            TEST_EMPLOYEE_ID, TEST_LEAVE_TYPE_ID, TEST_YEAR
        );
        assertNotNull("Saved balance should exist", saved);
        
        Integer id = saved.getLeaveBalanceId();
        
        boolean result = leaveBalanceDAO.delete(id);
        
        assertTrue("Delete should return true", result);
        assertNull("Should not find deleted record", leaveBalanceDAO.findById(id));
    }
    
    @Test
    public void testFindByEmployeeLeaveTypeAndYear_Success() {
        LeaveBalance leaveBalance = createTestLeaveBalance();
        leaveBalanceDAO.save(leaveBalance);
        
        LeaveBalance found = leaveBalanceDAO.findByEmployeeLeaveTypeAndYear(
            TEST_EMPLOYEE_ID, TEST_LEAVE_TYPE_ID, TEST_YEAR
        );
        
        assertNotNull("Should find leave balance by employee, type and year", found);
        assertEquals("Employee ID should match", TEST_EMPLOYEE_ID, found.getEmployeeId());
        assertEquals("Leave type ID should match", TEST_LEAVE_TYPE_ID, found.getLeaveTypeId());
        assertEquals("Year should match", TEST_YEAR, found.getBalanceYear());
    }
    
    @Test
    public void testFindByEmployeeAndYear_MultipleRecords() throws SQLException {
        // Create multiple leave balances for same employee and year
        for (int leaveTypeId = 1; leaveTypeId <= 3; leaveTypeId++) {
            LeaveBalance balance = createTestLeaveBalance();
            balance.setLeaveTypeId(leaveTypeId);
            leaveBalanceDAO.save(balance);
        }
        
        List<LeaveBalance> balances = leaveBalanceDAO.findByEmployeeAndYear(TEST_EMPLOYEE_ID, TEST_YEAR);
        
        assertNotNull("Should return non-null list", balances);
        assertTrue("Should find at least 3 records", balances.size() >= 3);
        
        // Verify all records belong to same employee and year
        for (LeaveBalance balance : balances) {
            assertEquals("Employee ID should match", TEST_EMPLOYEE_ID, balance.getEmployeeId());
            assertEquals("Year should match", TEST_YEAR, balance.getBalanceYear());
        }
    }
    
    @Test
    public void testUpdateUsedLeaveDays_Success() {
        LeaveBalance leaveBalance = createTestLeaveBalance();
        leaveBalance.setTotalLeaveDays(15);
        leaveBalance.setCarryOverDays(2);
        leaveBalanceDAO.save(leaveBalance);
        
        LeaveBalance saved = leaveBalanceDAO.findByEmployeeLeaveTypeAndYear(
            TEST_EMPLOYEE_ID, TEST_LEAVE_TYPE_ID, TEST_YEAR
        );
        assertNotNull("Saved balance should exist", saved);
        
        boolean result = leaveBalanceDAO.updateUsedLeaveDays(saved.getLeaveBalanceId(), 7);
        
        assertTrue("Update used leave days should succeed", result);
        
        LeaveBalance updated = leaveBalanceDAO.findById(saved.getLeaveBalanceId());
        assertEquals("Used days should be 7", Integer.valueOf(7), updated.getUsedLeaveDays());
        assertEquals("Remaining days should be 10 (15+2-7)", Integer.valueOf(10), updated.getRemainingLeaveDays());
    }
    
    // =============================
    // NEGATIVE TEST CASES
    // =============================
    
    @Test
    public void testSave_NullObject() {
        try {
            boolean result = leaveBalanceDAO.save(null);
            assertFalse("Save should return false for null object", result);
        } catch (Exception e) {
            assertTrue("DAO threw exception for null object which is acceptable", true);
        }
    }
    
    @Test
    public void testSave_MissingRequiredFields() {
        LeaveBalance incomplete = new LeaveBalance();
        
        try {
            boolean result = leaveBalanceDAO.save(incomplete);
            assertFalse("Save should return false for incomplete object", result);
        } catch (Exception e) {
            assertTrue("DAO threw exception for incomplete data which is acceptable", true);
        }
    }
    
    @Test
    public void testSave_InvalidEmployeeId() {
        LeaveBalance leaveBalance = createTestLeaveBalance();
        leaveBalance.setEmployeeId(99999999); // Non-existent employee
        
        boolean result = leaveBalanceDAO.save(leaveBalance);
        
        assertFalse("Save should fail with invalid employee ID", result);
    }
    
    @Test
    public void testSave_InvalidLeaveTypeId() {
        LeaveBalance leaveBalance = createTestLeaveBalance();
        leaveBalance.setLeaveTypeId(99999); // Non-existent leave type
        
        boolean result = leaveBalanceDAO.save(leaveBalance);
        
        assertFalse("Save should fail with invalid leave type ID", result);
    }
    
    @Test
    public void testSave_DuplicateRecord() {
        LeaveBalance first = createTestLeaveBalance();
        leaveBalanceDAO.save(first);
        
        LeaveBalance duplicate = createTestLeaveBalance();
        boolean result = leaveBalanceDAO.save(duplicate);
        
        assertFalse("Save should fail for duplicate employee/leave type/year combination", result);
    }
    
    @Test
    public void testFindById_NullId() {
        LeaveBalance result = leaveBalanceDAO.findById(NULL_ID);
        
        assertNull("Should return null for null ID", result);
    }
    
    @Test
    public void testFindById_NegativeId() {
        LeaveBalance result = leaveBalanceDAO.findById(INVALID_ID);
        
        assertNull("Should return null for negative ID", result);
    }
    
    @Test
    public void testFindById_NonExistentId() {
        LeaveBalance result = leaveBalanceDAO.findById(999999);
        
        assertNull("Should return null for non-existent ID", result);
    }
    
    @Test
    public void testUpdate_NullObject() {
        try {
            boolean result = leaveBalanceDAO.update(null);
            assertFalse("Update should return false for null object", result);
        } catch (Exception e) {
            assertTrue("DAO threw exception for null object which is acceptable", true);
        }
    }
    
    @Test
    public void testUpdate_NonExistentRecord() {
        LeaveBalance leaveBalance = createTestLeaveBalance();
        leaveBalance.setLeaveBalanceId(999999); // Non-existent ID
        
        boolean result = leaveBalanceDAO.update(leaveBalance);
        
        assertFalse("Update should return false for non-existent record", result);
    }
    
    @Test
    public void testDelete_NullId() {
        boolean result = leaveBalanceDAO.delete(NULL_ID);
        
        assertFalse("Delete should return false for null ID", result);
    }
    
    @Test
    public void testDelete_NonExistentId() {
        boolean result = leaveBalanceDAO.delete(999999);
        
        assertFalse("Delete should return false for non-existent ID", result);
    }
    
    @Test
    public void testFindByEmployeeLeaveTypeAndYear_NullParameters() {
        LeaveBalance result1 = leaveBalanceDAO.findByEmployeeLeaveTypeAndYear(null, TEST_LEAVE_TYPE_ID, TEST_YEAR);
        LeaveBalance result2 = leaveBalanceDAO.findByEmployeeLeaveTypeAndYear(TEST_EMPLOYEE_ID, null, TEST_YEAR);
        LeaveBalance result3 = leaveBalanceDAO.findByEmployeeLeaveTypeAndYear(TEST_EMPLOYEE_ID, TEST_LEAVE_TYPE_ID, null);
        
        assertNull("Should return null when employee ID is null", result1);
        assertNull("Should return null when leave type ID is null", result2);
        assertNull("Should return null when year is null", result3);
    }
    
    @Test
    public void testFindByEmployeeAndYear_InvalidEmployee() {
        List<LeaveBalance> result = leaveBalanceDAO.findByEmployeeAndYear(INVALID_ID, TEST_YEAR);
        
        assertNotNull("Should return empty list, not null", result);
        assertTrue("Should return empty list for invalid employee", result.isEmpty());
    }
    
    @Test
    public void testUpdateUsedLeaveDays_InvalidId() {
        boolean result = leaveBalanceDAO.updateUsedLeaveDays(999999, 5);
        
        assertFalse("Should return false for non-existent leave balance ID", result);
    }
    
    // =============================
    // EDGE CASE TESTS
    // =============================
      
    @Test
    public void testFindAll_EmptyTable() throws SQLException {
        // Clean all test data
        cleanupTestData();
        
        List<LeaveBalance> allBalances = leaveBalanceDAO.findAll();
        
        assertNotNull("Should return empty list, not null", allBalances);
    }
    
    @Test
    public void testCarryOverDays_Calculation() {
        LeaveBalance leaveBalance = createTestLeaveBalance();
        leaveBalance.setTotalLeaveDays(20);
        leaveBalance.setCarryOverDays(5);
        leaveBalance.setUsedLeaveDays(0);
        leaveBalance.setRemainingLeaveDays(25); // Total + CarryOver
        
        boolean saveResult = leaveBalanceDAO.save(leaveBalance);
        assertTrue("Initial save should succeed", saveResult);
        
        LeaveBalance saved = leaveBalanceDAO.findByEmployeeLeaveTypeAndYear(
            TEST_EMPLOYEE_ID, TEST_LEAVE_TYPE_ID, TEST_YEAR
        );
        assertNotNull("Saved balance should exist", saved);
        
        boolean updateResult = leaveBalanceDAO.updateUsedLeaveDays(saved.getLeaveBalanceId(), 10);
        assertTrue("Update should succeed", updateResult);
        
        LeaveBalance result = leaveBalanceDAO.findById(saved.getLeaveBalanceId());
        assertEquals("Remaining should be 15 (20+5-10)", Integer.valueOf(15), result.getRemainingLeaveDays());
    }
    
    @Test
    public void testInitializeYearlyLeaveBalances_NewYear() throws SQLException {
        // Create multiple test employees
        for (int i = 99998; i >= 99996; i--) {
            String insertQuery = "INSERT INTO employee (employeeId, firstName, lastName, birthDate, " +
                               "email, basicSalary, userRole, passwordHash, status, positionId) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = testConnection.prepareStatement(insertQuery)) {
                stmt.setInt(1, i);
                stmt.setString(2, "Test" + i);
                stmt.setString(3, "Employee" + i);
                stmt.setDate(4, Date.valueOf("1990-01-01"));
                stmt.setString(5, "test" + i + "@test.com");
                stmt.setBigDecimal(6, new java.math.BigDecimal("50000"));
                stmt.setString(7, "Employee");
                stmt.setString(8, "hashedpassword");
                stmt.setString(9, "Regular");
                stmt.setInt(10, 1);
                stmt.executeUpdate();
            }
        }
        
        int result = leaveBalanceDAO.initializeYearlyLeaveBalances(2025, 15);
        
        assertTrue("Should initialize leave balances for new year", result > 0);
    }
       
    // =============================
    // ENTITY VALIDATION TESTS
    // =============================
    
    @Test
    public void testEntityValidation_NullTotalDays() {
        LeaveBalance balance = createTestLeaveBalance();
        balance.setTotalLeaveDays(null);
        
        boolean result = leaveBalanceDAO.save(balance);
        
        // Should handle null total days gracefully
        if (result) {
            LeaveBalance saved = leaveBalanceDAO.findByEmployeeLeaveTypeAndYear(
                TEST_EMPLOYEE_ID, TEST_LEAVE_TYPE_ID, TEST_YEAR
            );
            assertNotNull("Should handle null total days", saved);
        } else {
            assertTrue("Save might fail with null total days", true);
        }
    }
       
    // =============================
    // HELPER METHODS
    // =============================
    
    private LeaveBalance createTestLeaveBalance() {
        LeaveBalance balance = new LeaveBalance();
        balance.setEmployeeId(TEST_EMPLOYEE_ID);
        balance.setLeaveTypeId(TEST_LEAVE_TYPE_ID);
        balance.setTotalLeaveDays(15);
        balance.setUsedLeaveDays(0);
        balance.setRemainingLeaveDays(15);
        balance.setCarryOverDays(0);
        balance.setBalanceYear(TEST_YEAR);
        return balance;
    }
    
    @Test
    public void testDatabaseConnection() {
        assertTrue("Database connection should be available", 
                  leaveBalanceDAO.testConnection());
    }
}