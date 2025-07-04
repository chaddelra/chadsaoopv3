package UnitTestAOOP;

import Models.LeaveRequestModel;
import Models.LeaveRequestModel.ApprovalStatus;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * JUnit test class for LeaveRequestModel with negative testing.
 * @author martin
 */

public class LeaveRequestModelTest {
    
    private LeaveRequestModel leaveRequest;
    private static final Integer VALID_EMPLOYEE_ID = 10001;
    private static final Integer VALID_LEAVE_TYPE_ID = 1;
    private static final String VALID_REASON = "Annual vacation with family";
    
    @Before
    public void setUp() {
        // Initialize a fresh LeaveRequestModel before each test
        leaveRequest = new LeaveRequestModel();
    }
    
    @After
    public void tearDown() {
        // Clean up after each test
        leaveRequest = null;
    }
    
    // ==================== POSITIVE TESTS ====================
    
    // --- Date Validation Tests (Positive) ---
    
    @Test
    public void testSetDateRange_validSameDay() {
        LocalDate sameDay = LocalDate.now().plusDays(10);
        leaveRequest.setLeaveStart(sameDay);
        leaveRequest.setLeaveEnd(sameDay);
        
        assertTrue("Same day leave should be valid", leaveRequest.hasValidDates());
        assertEquals("Same day leave should count as 1 day", 1, leaveRequest.getLeaveDays());
    }
    
    @Test
    public void testGetLeaveDays_validMultipleDays() {
        LocalDate startDate = LocalDate.of(2025, 7, 1);
        LocalDate endDate = LocalDate.of(2025, 7, 5);
        
        leaveRequest.setLeaveStart(startDate);
        leaveRequest.setLeaveEnd(endDate);
        
        assertEquals("Leave days calculation should be inclusive", 5, leaveRequest.getLeaveDays());
    }
    
    // --- Enum Validation Tests (Positive) ---
    
    @Test
    public void testValidEnumValues() {
        // Test all valid enum values
        for (ApprovalStatus status : ApprovalStatus.values()) {
            leaveRequest.setApprovalStatus(status);
            assertEquals("Enum value should be set correctly", status, leaveRequest.getApprovalStatus());
            assertNotNull("Enum getValue() should not return null", status.getValue());
        }
    }
    
    @Test
    public void testEnumFromString_validValues() {
        // Test conversion from database string values
        assertEquals(ApprovalStatus.PENDING, ApprovalStatus.fromString("Pending"));
        assertEquals(ApprovalStatus.APPROVED, ApprovalStatus.fromString("Approved"));
        assertEquals(ApprovalStatus.REJECTED, ApprovalStatus.fromString("Rejected"));
        
        // Test case-insensitive conversion
        assertEquals(ApprovalStatus.PENDING, ApprovalStatus.fromString("pending"));
        assertEquals(ApprovalStatus.APPROVED, ApprovalStatus.fromString("APPROVED"));
        assertEquals(ApprovalStatus.REJECTED, ApprovalStatus.fromString("ReJeCtEd"));
    }
    
    // --- Approval Workflow Tests (Positive) ---
    
    @Test
    public void testApproveRequest_withNotes() {
        String supervisorNotes = "Approved for annual leave";
        LocalDateTime beforeApproval = LocalDateTime.now();
        
        leaveRequest.approve(supervisorNotes);
        
        assertEquals("Status should be APPROVED", ApprovalStatus.APPROVED, leaveRequest.getApprovalStatus());
        assertEquals("Supervisor notes should be set", supervisorNotes, leaveRequest.getSupervisorNotes());
        assertNotNull("Date approved should be set", leaveRequest.getDateApproved());
        assertTrue("Date approved should be recent", 
            leaveRequest.getDateApproved().isAfter(beforeApproval.minusSeconds(1)));
        assertTrue("Request should be marked as approved", leaveRequest.isApproved());
        assertTrue("Request should be marked as processed", leaveRequest.isProcessed());
    }
    
    @Test
    public void testRejectRequest_withNotes() {
        String rejectionReason = "Insufficient leave balance";
        LocalDateTime beforeRejection = LocalDateTime.now();
        
        leaveRequest.reject(rejectionReason);
        
        assertEquals("Status should be REJECTED", ApprovalStatus.REJECTED, leaveRequest.getApprovalStatus());
        assertEquals("Supervisor notes should be set", rejectionReason, leaveRequest.getSupervisorNotes());
        assertNotNull("Date approved should be set even for rejection", leaveRequest.getDateApproved());
        assertTrue("Date approved should be recent", 
            leaveRequest.getDateApproved().isAfter(beforeRejection.minusSeconds(1)));
        assertTrue("Request should be marked as rejected", leaveRequest.isRejected());
        assertTrue("Request should be marked as processed", leaveRequest.isProcessed());
    }
    
    // --- Constructor Tests (Positive) ---
    
    @Test
    public void testDefaultConstructor() {
        LeaveRequestModel defaultLeave = new LeaveRequestModel();
        
        assertNotNull("Date created should be set", defaultLeave.getDateCreated());
        assertEquals("Default status should be PENDING", ApprovalStatus.PENDING, defaultLeave.getApprovalStatus());
        assertTrue("New request should be pending", defaultLeave.isPending());
        assertFalse("New request should not be processed", defaultLeave.isProcessed());
    }
    
    @Test
    public void testParameterizedConstructor() {
        LocalDate startDate = LocalDate.now().plusDays(14);
        LocalDate endDate = startDate.plusDays(3);
        
        LeaveRequestModel paramLeave = new LeaveRequestModel(
            VALID_EMPLOYEE_ID, VALID_LEAVE_TYPE_ID, startDate, endDate, VALID_REASON
        );
        
        assertEquals("Employee ID should match", VALID_EMPLOYEE_ID, paramLeave.getEmployeeId());
        assertEquals("Leave type ID should match", VALID_LEAVE_TYPE_ID, paramLeave.getLeaveTypeId());
        assertEquals("Start date should match", startDate, paramLeave.getLeaveStart());
        assertEquals("End date should match", endDate, paramLeave.getLeaveEnd());
        assertEquals("Reason should match", VALID_REASON, paramLeave.getLeaveReason());
        assertEquals("Status should be PENDING", ApprovalStatus.PENDING, paramLeave.getApprovalStatus());
        assertNotNull("Date created should be set", paramLeave.getDateCreated());
    }
    
    // ==================== NEGATIVE TESTS ====================
    
    // --- Date Validation Tests (Negative) ---
    
    @Test
    public void testSetDateRange_endBeforeStart() {
        LocalDate startDate = LocalDate.now().plusDays(10);
        LocalDate endDate = startDate.minusDays(5); // End before start
        
        leaveRequest.setLeaveStart(startDate);
        leaveRequest.setLeaveEnd(endDate);
        
        assertFalse("Invalid date range should be detected", leaveRequest.hasValidDates());
        // The getLeaveDays() method will still calculate, but the result will be negative + 1
        assertTrue("Leave days calculation for invalid range should be <= 0", 
            leaveRequest.getLeaveDays() <= 0);
    }
    
    @Test
    public void testSetDateRange_nullDates() {
        // Test null start date
        leaveRequest.setLeaveStart(null);
        leaveRequest.setLeaveEnd(LocalDate.now());
        
        assertFalse("Null start date should make dates invalid", leaveRequest.hasValidDates());
        assertEquals("Leave days with null start should be 0", 0, leaveRequest.getLeaveDays());
        
        // Test null end date
        leaveRequest.setLeaveStart(LocalDate.now());
        leaveRequest.setLeaveEnd(null);
        
        assertFalse("Null end date should make dates invalid", leaveRequest.hasValidDates());
        assertEquals("Leave days with null end should be 0", 0, leaveRequest.getLeaveDays());
        
        // Test both null
        leaveRequest.setLeaveStart(null);
        leaveRequest.setLeaveEnd(null);
        
        assertFalse("Both null dates should be invalid", leaveRequest.hasValidDates());
        assertEquals("Leave days with both null should be 0", 0, leaveRequest.getLeaveDays());
    }
    
    // --- Field Validation Tests (Negative) ---
    
    @Test
    public void testSetReason_veryLong() {
        // Test with a string longer than typical database varchar(255)
        StringBuilder longReason = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            longReason.append("a");
        }
        
        leaveRequest.setLeaveReason(longReason.toString());
        assertEquals("Very long reason should be stored", longReason.toString(), leaveRequest.getLeaveReason());
        // Note: Database constraint would handle truncation, not the model
    }
    
    @Test
    public void testSetEmployeeId_invalid() {
        // Test negative employee ID
        Integer negativeId = -1;
        leaveRequest.setEmployeeId(negativeId);
        assertEquals("Negative employee ID should be stored (database will validate)", 
            negativeId, leaveRequest.getEmployeeId());
        
        // Test null employee ID
        leaveRequest.setEmployeeId(null);
        assertNull("Null employee ID should be allowed at model level", leaveRequest.getEmployeeId());
        
        // Test zero employee ID
        Integer zeroId = 0;
        leaveRequest.setEmployeeId(zeroId);
        assertEquals("Zero employee ID should be stored", zeroId, leaveRequest.getEmployeeId());
    }
    
    @Test
    public void testSetLeaveTypeId_invalid() {
        // Test negative leave type ID
        Integer negativeId = -99;
        leaveRequest.setLeaveTypeId(negativeId);
        assertEquals("Negative leave type ID should be stored", negativeId, leaveRequest.getLeaveTypeId());
        
        // Test null leave type ID
        leaveRequest.setLeaveTypeId(null);
        assertNull("Null leave type ID should be allowed at model level", leaveRequest.getLeaveTypeId());
    }
    
    // --- Enum Tests (Negative) ---
    
    @Test
    public void testInvalidEnumConversion() {
        try {
            ApprovalStatus.fromString("InvalidStatus");
            fail("Should throw IllegalArgumentException for invalid status");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception message should mention unknown status", 
                e.getMessage().contains("Unknown approval status"));
        }
    }
    
    @Test
    public void testNullEnumHandling() {
        // Test fromString with null
        ApprovalStatus status = ApprovalStatus.fromString(null);
        assertEquals("Null string should default to PENDING", ApprovalStatus.PENDING, status);
        
        // Test setting null status
        leaveRequest.setApprovalStatus(null);
        assertNull("Null approval status should be allowed", leaveRequest.getApprovalStatus());
        
        // Test status checks with null
        leaveRequest.setApprovalStatus(null);
        assertFalse("Null status should not be pending", leaveRequest.isPending());
        assertFalse("Null status should not be approved", leaveRequest.isApproved());
        assertFalse("Null status should not be rejected", leaveRequest.isRejected());
        assertTrue("Null status should be considered processed", leaveRequest.isProcessed());
    }
    
    // --- Supervisor Notes Tests (Negative) ---
    
    @Test
    public void testReject_withNullNotes() {
        // Rejection typically requires notes, but model allows null
        leaveRequest.reject(null);
        assertNull("Model should allow reject with null notes", leaveRequest.getSupervisorNotes());
        assertEquals("Status should still be rejected", ApprovalStatus.REJECTED, leaveRequest.getApprovalStatus());
    }
    
    // --- Equals and HashCode Tests ---
    
    @Test
    public void testEquals_null() {
        assertFalse("Should not equal null", leaveRequest.equals(null));
    }
    
    @Test
    public void testEquals_sameId() {
        LeaveRequestModel leave1 = new LeaveRequestModel();
        LeaveRequestModel leave2 = new LeaveRequestModel();
        
        leave1.setLeaveRequestId(100);
        leave2.setLeaveRequestId(100);
        
        assertTrue("Objects with same ID should be equal", leave1.equals(leave2));
        assertEquals("Objects with same ID should have same hashcode", leave1.hashCode(), leave2.hashCode());
    }
    
    // --- Complex Scenario Tests ---
    
    @Test
    public void testLeaveRequestLifecycle() {
        // 1. Create new request
        LocalDate startDate = LocalDate.now().plusDays(30);
        LocalDate endDate = startDate.plusDays(4);
        
        LeaveRequestModel request = new LeaveRequestModel(
            VALID_EMPLOYEE_ID, VALID_LEAVE_TYPE_ID, startDate, endDate, 
            "Planned vacation to Europe"
        );
        
        // 2. Verify initial state
        assertTrue("New request should be pending", request.isPending());
        assertFalse("New request should not be processed", request.isProcessed());
        assertEquals("Should be 5 days leave", 5, request.getLeaveDays());
        assertTrue("Should be future leave", request.isFutureLeave());
        
        // 3. Simulate approval
        request.approve("Enjoy your vacation!");
        
        // 4. Verify approved state
        assertTrue("Should be approved", request.isApproved());
        assertTrue("Should be processed", request.isProcessed());
        assertFalse("Should not be pending", request.isPending());
        assertNotNull("Should have approval date", request.getDateApproved());
        
        // 5. Try to change to rejected (unusual but possible)
        request.setApprovalStatus(ApprovalStatus.REJECTED);
        assertTrue("Should now be rejected", request.isRejected());
        // Note: dateApproved and supervisorNotes remain from approval
    }
    
    @Test
    public void testEnumGetAllValues() {
        String[] allValues = ApprovalStatus.getAllValues();
        
        assertEquals("Should have 3 status values", 3, allValues.length);
        assertEquals("First value should be Pending", "Pending", allValues[0]);
        assertEquals("Second value should be Approved", "Approved", allValues[1]);
        assertEquals("Third value should be Rejected", "Rejected", allValues[2]);
    }
    
    @Test
    public void testLeaveOverYearBoundary() {
        // Test leave request spanning across years
        LocalDate endOfYear = LocalDate.of(2025, 12, 28);
        LocalDate startOfNextYear = LocalDate.of(2026, 1, 3);
        
        leaveRequest.setLeaveStart(endOfYear);
        leaveRequest.setLeaveEnd(startOfNextYear);
        
        assertTrue("Cross-year leave should have valid dates", leaveRequest.hasValidDates());
        assertEquals("Should calculate correct days across years", 7, leaveRequest.getLeaveDays());
    }
}