/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package UnitTestAOOP;

import Models.*;
import Services.*;
import DAOs.*;
import Models.EmployeeModel.EmployeeStatus;
import Models.LeaveRequestModel.ApprovalStatus;
import Models.ImmediateSupervisorModel.*;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author i-Gits <lr.jdelasarmas@mmdc.mcl.edu.ph>
 */

public class ImmediateSupervisorModelTest {
    
    private DatabaseConnection dbConnection;
    private ImmediateSupervisorModel supervisor;
    private ImmediateSupervisorModel unauthorizedSupervisor;
    private EmployeeModel regularEmployee;
    private EmployeeModel teamMember1;
    private EmployeeModel teamMember2;
    private EmployeeModel nonTeamMember;
    
    // DAOs for test data setup
    private EmployeeDAO employeeDAO;
    private LeaveRequestDAO leaveRequestDAO;  // Changed from LeaveDAO
    private OvertimeRequestDAO overtimeDAO;
    private AttendanceDAO attendanceDAO;
    
    // Test data IDs
    private static Integer testSupervisorId = 99001;
    private static Integer testTeamMember1Id = 99002;
    private static Integer testTeamMember2Id = 99003;
    private static Integer testNonTeamMemberId = 99004;
    private static Integer testRegularEmployeeId = 99005;
    private static Integer testUnauthorizedSupervisorId = 99006;
    
    @BeforeClass
    public static void setUpClass() {
        System.out.println("===== Starting ImmediateSupervisorModel Test Suite =====");
    }
    
    @AfterClass
    public static void tearDownClass() {
        System.out.println("===== Completed ImmediateSupervisorModel Test Suite =====");
    }
    
    @Before
    public void setUp() {
        // Initialize database connection
        dbConnection = new DatabaseConnection();
        
        // Initialize DAOs
        employeeDAO = new EmployeeDAO(dbConnection);
        leaveRequestDAO = new LeaveRequestDAO(dbConnection);  // Changed from leaveDAO
        overtimeDAO = new OvertimeRequestDAO(dbConnection);
        attendanceDAO = new AttendanceDAO(dbConnection);
        
        // Create test supervisor
        supervisor = new ImmediateSupervisorModel(
            testSupervisorId,
            "John",
            "Supervisor",
            "john.supervisor@motorph.com",
            "Immediate Supervisor",
            "Engineering"
        );
        
        // Create unauthorized supervisor (different department)
        unauthorizedSupervisor = new ImmediateSupervisorModel(
            testUnauthorizedSupervisorId,
            "Jane",
            "Unauthorized",
            "jane.unauthorized@motorph.com",
            "Immediate Supervisor",
            "Marketing"
        );
        
        // Create regular employee (not a supervisor)
        regularEmployee = new EmployeeModel();
        regularEmployee.setEmployeeId(testRegularEmployeeId);
        regularEmployee.setFirstName("Regular");
        regularEmployee.setLastName("Employee");
        regularEmployee.setEmail("regular.employee@motorph.com");
        regularEmployee.setUserRole("Employee");
        regularEmployee.setStatus(EmployeeStatus.REGULAR);
        
        // Create team members
        teamMember1 = createTestEmployee(testTeamMember1Id, "Alice", "Engineer", "Engineering", testSupervisorId);
        teamMember2 = createTestEmployee(testTeamMember2Id, "Bob", "Developer", "Engineering", testSupervisorId);
        nonTeamMember = createTestEmployee(testNonTeamMemberId, "Charlie", "Designer", "Marketing", testUnauthorizedSupervisorId);
    }
    
    @After
    public void tearDown() {
        // Clean up test data would go here in a real implementation
        // For now, we'll assume test data is cleaned up separately
        supervisor = null;
        unauthorizedSupervisor = null;
        regularEmployee = null;
        teamMember1 = null;
        teamMember2 = null;
        nonTeamMember = null;
    }
    
    // ========================================
    // POSITIVE TEST CASES
    // ========================================   
    /**
     * Test that supervisor can view team attendance
     */
    @Test
    public void testSupervisorCanViewTeamAttendance() {
        System.out.println("Testing: Supervisor can view team attendance");
        
        LocalDate today = LocalDate.now();
        List<AttendanceService.DailyAttendanceRecord> teamAttendance = 
            supervisor.getTeamAttendance(today);
        
        assertNotNull("Team attendance should not be null", teamAttendance);
        // Note: This might be empty if no attendance records exist for today
        System.out.println("Found " + teamAttendance.size() + " attendance records");
    }
    
    /**
     * Test supervisor permissions array
     */
    @Test
    public void testSupervisorHasCorrectPermissions() {
        System.out.println("Testing: Supervisor has correct permissions");
        
        String[] permissions = supervisor.getSupervisorPermissions();
        
        assertNotNull("Permissions should not be null", permissions);
        assertTrue("Should have permissions", permissions.length > 0);
        
        // Check for specific required permissions
        boolean hasApproveLeave = false;
        boolean hasApproveOvertime = false;
        boolean hasViewAttendance = false;
        
        for (String permission : permissions) {
            if (permission.equals("APPROVE_TEAM_LEAVE")) hasApproveLeave = true;
            if (permission.equals("APPROVE_TEAM_OVERTIME")) hasApproveOvertime = true;
            if (permission.equals("VIEW_TEAM_ATTENDANCE")) hasViewAttendance = true;
        }
        
        assertTrue("Should have APPROVE_TEAM_LEAVE permission", hasApproveLeave);
        assertTrue("Should have APPROVE_TEAM_OVERTIME permission", hasApproveOvertime);
        assertTrue("Should have VIEW_TEAM_ATTENDANCE permission", hasViewAttendance);
    }
    
    // ========================================
    // NEGATIVE TEST CASES
    // ========================================
    
    
    /**
     * Test that supervisor cannot approve leave for non-team members
     */
    @Test
    public void testSupervisorCannotApproveNonTeamMemberLeave() {
        System.out.println("Testing: Supervisor cannot approve non-team member leave");
        
        // Create leave request for non-team member
        LeaveRequestModel leaveRequest = new LeaveRequestModel(
            nonTeamMember.getEmployeeId(),
            1, // Sick leave
            LocalDate.now().plusDays(5),
            LocalDate.now().plusDays(7),
            "Personal leave"
        );
        
        // Save the request
        leaveRequestDAO.save(leaveRequest);
        
        // Try to approve - should fail
        SupervisorResult result = supervisor.approveTeamLeaveRequest(
            leaveRequest.getLeaveRequestId(),
            "Trying to approve"
        );
        
        assertFalse("Should not be able to approve non-team member leave", result.isSuccess());
        assertEquals("Error message should indicate not team member",
            "Leave request does not belong to your team", result.getMessage());
    }
    
    /**
     * Test that supervisor cannot approve overtime for non-team members
     */
    @Test
    public void testSupervisorCannotApproveNonTeamMemberOvertime() {
        System.out.println("Testing: Supervisor cannot approve non-team member overtime");
        
        // Create overtime request for non-team member
        OvertimeRequestModel overtimeRequest = new OvertimeRequestModel(
            nonTeamMember.getEmployeeId(),
            LocalDateTime.now().plusDays(2).withHour(18).withMinute(0),
            LocalDateTime.now().plusDays(2).withHour(20).withMinute(0),
            "Extra work"
        );
        
        // Save the request
        overtimeDAO.save(overtimeRequest);
        
        // Try to approve - should fail
        SupervisorResult result = supervisor.approveTeamOvertimeRequest(
            overtimeRequest.getOvertimeRequestId(),
            "Trying to approve"
        );
        
        assertFalse("Should not be able to approve non-team member overtime", result.isSuccess());
        assertEquals("Error message should indicate not team member",
            "Overtime request does not belong to your team", result.getMessage());
    }
    
    /**
     * Test rejection without required supervisor notes
     */
    @Test
    public void testRejectLeaveWithoutNotesFailure() {
        System.out.println("Testing: Reject leave without notes should fail");
        
        // Create a valid team member leave request
        LeaveRequestModel leaveRequest = new LeaveRequestModel(
            teamMember1.getEmployeeId(),
            1,
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(12),
            "Vacation"
        );
        
        leaveRequestDAO.save(leaveRequest);
        
        // Try to reject without notes
        SupervisorResult result = supervisor.rejectTeamLeaveRequest(
            leaveRequest.getLeaveRequestId(),
            null // No notes provided
        );
        
        assertFalse("Rejection without notes should fail", result.isSuccess());
        assertEquals("Should require supervisor notes",
            "Supervisor notes are required when rejecting leave requests", 
            result.getMessage());
        
        // Try with empty notes
        result = supervisor.rejectTeamLeaveRequest(
            leaveRequest.getLeaveRequestId(),
            "   " // Empty/whitespace notes
        );
        
        assertFalse("Rejection with empty notes should fail", result.isSuccess());
    }
    
    /**
     * Test handling of invalid/non-existent leave request ID
     */
    @Test
    public void testApproveNonExistentLeaveRequest() {
        System.out.println("Testing: Approve non-existent leave request");
        
        Integer invalidRequestId = 99999; // Non-existent ID
        
        SupervisorResult result = supervisor.approveTeamLeaveRequest(
            invalidRequestId,
            "Approving non-existent request"
        );
        
        assertFalse("Should fail for non-existent request", result.isSuccess());
        assertTrue("Should indicate request not found",
            result.getMessage().contains("not found") || 
            result.getMessage().contains("does not belong to your team"));
    }
    
    /**
     * Test handling of null parameters
     */
    @Test
    public void testNullParameterHandling() {
        System.out.println("Testing: Null parameter handling");
        
        // Test null leave request ID
        SupervisorResult result = supervisor.approveTeamLeaveRequest(null, "Notes");
        assertFalse("Should fail with null request ID", result.isSuccess());
        
        // Test null overtime request ID
        result = supervisor.approveTeamOvertimeRequest(null, "Notes");
        assertFalse("Should fail with null overtime ID", result.isSuccess());
    }
    
    /**
     * Test unauthorized department access
     */
    @Test
    public void testUnauthorizedDepartmentAccess() {
        System.out.println("Testing: Unauthorized department access");
        
        // Engineering supervisor trying to manage Marketing team
        List<EmployeeModel> wrongDeptMembers = supervisor.getTeamMembers();
        
        // Verify no Marketing employees are returned
        for (EmployeeModel member : wrongDeptMembers) {
            assertFalse("Should not access Marketing department members",
                testNonTeamMemberId.equals(member.getEmployeeId()));
        }
    }
    
    /**
     * Test invalid role assignment
     */
    @Test
    public void testInvalidRoleAssignment() {
        System.out.println("Testing: Invalid role assignment");
        
        // Create employee with invalid role
        EmployeeModel invalidRole = new EmployeeModel();
        invalidRole.setEmployeeId(99007);
        invalidRole.setFirstName("Invalid");
        invalidRole.setLastName("Role");
        invalidRole.setUserRole("InvalidRole"); // Not a valid supervisor role
        
        ImmediateSupervisorModel invalidSupervisor = 
            new ImmediateSupervisorModel(invalidRole);
        
        // Should still work but with limited/no permissions
        List<EmployeeModel> team = invalidSupervisor.getTeamMembers();
        assertNotNull("Should return empty list, not null", team);
        assertTrue("Invalid role should have no team members", team.isEmpty());
    }
    
    /**
     * Test attendance issues detection with edge cases
     */
    @Test
    public void testAttendanceIssuesDetectionEdgeCases() {
        System.out.println("Testing: Attendance issues detection edge cases");
        
        // Test with past month (should still work)
        YearMonth pastMonth = YearMonth.now().minusMonths(3);
        SupervisorResult result = supervisor.getTeamAttendanceIssues(pastMonth);
        
        assertNotNull("Result should not be null for past month", result);
        assertTrue("Should handle past month gracefully", result.isSuccess());
        
        // Test with future month (should return empty)
        YearMonth futureMonth = YearMonth.now().plusMonths(3);
        result = supervisor.getTeamAttendanceIssues(futureMonth);
        
        assertNotNull("Result should not be null for future month", result);
        assertTrue("Should handle future month gracefully", result.isSuccess());
        assertEquals("Should have no issues for future month", 0, 
            result.getAttendanceIssues().size());
    }
    
    // ========================================
    // HELPER METHODS
    // ========================================
    
    private EmployeeModel createTestEmployee(Integer id, String firstName, 
                                           String lastName, String department, 
                                           Integer supervisorId) {
        EmployeeModel employee = new EmployeeModel();
        employee.setEmployeeId(id);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@motorph.com");
        employee.setUserRole("Employee");
        employee.setStatus(EmployeeStatus.REGULAR);
        employee.setSupervisorId(supervisorId);
        employee.setPositionId(1); // Default position
        employee.setBirthDate(LocalDate.of(1990, 1, 1));
        employee.setBasicSalary(new BigDecimal("30000"));
        employee.setHourlyRate(new BigDecimal("180"));
        
        return employee;
    }
}

