package UnitTestAOOP;

import oop.classes.enums.ApprovalStatus;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.sql.*;

/**
 * JUnit test for ApprovalStatus with negative testing
 * @author martin
 */

public class ApprovalStatusTest {
    
    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/payrollsystem_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "motorph_123";
    
    private Connection connection;
    
    @Before
    public void setUp() {
        // Initialize any test data if needed
        connection = null;
    }
    
    // ==================== POSITIVE TEST CASES ====================
    
    @Test
    public void testEnumValues() {
        // Test that all enum values exist
        ApprovalStatus[] statuses = ApprovalStatus.values();
        assertEquals("Should have exactly 3 approval statuses", 3, statuses.length);
        
        // Verify each enum exists
        assertNotNull("PENDING should exist", ApprovalStatus.PENDING);
        assertNotNull("APPROVED should exist", ApprovalStatus.APPROVED);
        assertNotNull("REJECTED should exist", ApprovalStatus.REJECTED);
    }
    
    @Test
    public void testGetValue() {
        // Test getValue() returns correct database values
        assertEquals("PENDING getValue should return 'Pending'", "Pending", ApprovalStatus.PENDING.getValue());
        assertEquals("APPROVED getValue should return 'Approved'", "Approved", ApprovalStatus.APPROVED.getValue());
        assertEquals("REJECTED getValue should return 'Rejected'", "Rejected", ApprovalStatus.REJECTED.getValue());
    }
    
    @Test
    public void testFromValueWithValidInputs() {
        // Test fromValue() with valid inputs
        assertEquals("fromValue('Pending') should return PENDING", 
                    ApprovalStatus.PENDING, ApprovalStatus.fromValue("Pending"));
        assertEquals("fromValue('Approved') should return APPROVED", 
                    ApprovalStatus.APPROVED, ApprovalStatus.fromValue("Approved"));
        assertEquals("fromValue('Rejected') should return REJECTED", 
                    ApprovalStatus.REJECTED, ApprovalStatus.fromValue("Rejected"));
    }
    
    @Test
    public void testIsApproved() {
        // Test isApproved() method
        assertTrue("APPROVED.isApproved() should return true", ApprovalStatus.APPROVED.isApproved());
        assertFalse("PENDING.isApproved() should return false", ApprovalStatus.PENDING.isApproved());
        assertFalse("REJECTED.isApproved() should return false", ApprovalStatus.REJECTED.isApproved());
    }
    
    @Test
    public void testIsPending() {
        // Test isPending() method
        assertTrue("PENDING.isPending() should return true", ApprovalStatus.PENDING.isPending());
        assertFalse("APPROVED.isPending() should return false", ApprovalStatus.APPROVED.isPending());
        assertFalse("REJECTED.isPending() should return false", ApprovalStatus.REJECTED.isPending());
    }
    
    @Test
    public void testIsRejected() {
        // Test isRejected() method
        assertTrue("REJECTED.isRejected() should return true", ApprovalStatus.REJECTED.isRejected());
        assertFalse("PENDING.isRejected() should return false", ApprovalStatus.PENDING.isRejected());
        assertFalse("APPROVED.isRejected() should return false", ApprovalStatus.APPROVED.isRejected());
    }
    
    @Test
    public void testValueOf() {
        // Test standard enum valueOf() method
        assertEquals("valueOf('PENDING') should return PENDING enum", 
                    ApprovalStatus.PENDING, ApprovalStatus.valueOf("PENDING"));
        assertEquals("valueOf('APPROVED') should return APPROVED enum", 
                    ApprovalStatus.APPROVED, ApprovalStatus.valueOf("APPROVED"));
        assertEquals("valueOf('REJECTED') should return REJECTED enum", 
                    ApprovalStatus.REJECTED, ApprovalStatus.valueOf("REJECTED"));
    }
    
    // ==================== NEGATIVE TEST CASES ====================
    
    @Test
    public void testFromValueWithNull() {
        // Test fromValue() with null input
        assertNull("fromValue(null) should return null", ApprovalStatus.fromValue(null));
    }
    
    
    @Test
    public void testFromValueWithInvalidValues() {
        // Test fromValue() with various invalid inputs
        assertNull("fromValue('pending') should return null (wrong case)", 
                  ApprovalStatus.fromValue("pending"));
        assertNull("fromValue('PENDING') should return null (wrong case)", 
                  ApprovalStatus.fromValue("PENDING"));
        assertNull("fromValue('Approve') should return null (wrong value)", 
                  ApprovalStatus.fromValue("Approve"));
        assertNull("fromValue('Cancelled') should return null (non-existent status)", 
                  ApprovalStatus.fromValue("Cancelled"));
        assertNull("fromValue('123') should return null (numeric string)", 
                  ApprovalStatus.fromValue("123"));
        assertNull("fromValue('Pending ') should return null (trailing space)", 
                  ApprovalStatus.fromValue("Pending "));
        assertNull("fromValue(' Pending') should return null (leading space)", 
                  ApprovalStatus.fromValue(" Pending"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValueOfWithInvalidValue() {
        // Test standard valueOf() with invalid value - should throw exception
        ApprovalStatus.valueOf("Invalid");
    }
    
    // ==================== EDGE CASES ====================
    
    @Test
    public void testFromValueCaseSensitivity() {
        // Test case sensitivity in fromValue()
        assertNull("fromValue should be case sensitive for 'PENDING'", 
                  ApprovalStatus.fromValue("PENDING"));
        assertNull("fromValue should be case sensitive for 'approved'", 
                  ApprovalStatus.fromValue("approved"));
        assertNull("fromValue should be case sensitive for 'ReJeCtEd'", 
                  ApprovalStatus.fromValue("ReJeCtEd"));
    }
    
    // ==================== DATABASE INTEGRATION TESTS ====================
    
    @Test
    public void testDatabaseEnumCompatibility() {
        // Test that enum values match database enum values
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            
            // Query to get enum values from database
            String query = "SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                          "WHERE TABLE_SCHEMA = 'payrollsystem_db' " +
                          "AND TABLE_NAME = 'leaverequest' " +
                          "AND COLUMN_NAME = 'approvalStatus'";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            if (rs.next()) {
                String columnType = rs.getString("COLUMN_TYPE");
                // Extract enum values from column type
                assertTrue("Database should contain 'Pending' value", 
                          columnType.contains("'Pending'"));
                assertTrue("Database should contain 'Approved' value", 
                          columnType.contains("'Approved'"));
                assertTrue("Database should contain 'Rejected' value", 
                          columnType.contains("'Rejected'"));
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            // If database is not available, skip this test
            System.out.println("Database test skipped: " + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    // ==================== REALISTIC USAGE SCENARIOS ====================
    
    @Test
    public void testLeaveRequestApprovalWorkflow() {
        // Simulate a typical leave request approval workflow
        ApprovalStatus status = ApprovalStatus.PENDING;
        
        // Initial state
        assertTrue("New request should be pending", status.isPending());
        assertFalse("New request should not be approved", status.isApproved());
        assertFalse("New request should not be rejected", status.isRejected());
        
        // Approve the request
        status = ApprovalStatus.APPROVED;
        assertFalse("Approved request should not be pending", status.isPending());
        assertTrue("Approved request should be approved", status.isApproved());
        assertFalse("Approved request should not be rejected", status.isRejected());
    }
    
    @Test
    public void testDatabaseValueConversion() {
        // Simulate converting database values to enum
        String[] dbValues = {"Pending", "Approved", "Rejected"};
        ApprovalStatus[] expectedStatuses = {
            ApprovalStatus.PENDING,
            ApprovalStatus.APPROVED,
            ApprovalStatus.REJECTED
        };
        
        for (int i = 0; i < dbValues.length; i++) {
            ApprovalStatus status = ApprovalStatus.fromValue(dbValues[i]);
            assertEquals("Database value '" + dbValues[i] + "' should convert correctly",
                        expectedStatuses[i], status);
        }
    }   
}