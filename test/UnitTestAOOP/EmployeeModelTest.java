/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UnitTestAOOP;

import Models.EmployeeModel;
import Models.EmployeeModel.EmployeeStatus;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author i-Gits <lr.jdelasarmas@mmdc.mcl.edu.ph>
 */

public class EmployeeModelTest {
    
    private EmployeeModel employee;
    
    @Before
    public void setUp() {
        employee = new EmployeeModel();
    }
    
    @After
    public void tearDown() {
        employee = null;
    }
    
    // ==================== BASIC CREATION TEST ====================
    
    @Test
    public void testCreateNewEmployee() {
        // Test: Can we create a new employee?
        EmployeeModel newEmployee = new EmployeeModel();
        assertNotNull("A new employee should be created successfully", newEmployee);
        assertEquals("New employee should start as Probationary", 
                    EmployeeStatus.PROBATIONARY, newEmployee.getStatus());
    }
    
    // ==================== NAME TESTS ====================
    
    @Test
    public void testSetEmployeeName() {
        // Test: Can we set an employee's name?
        employee.setFirstName("Juan");
        employee.setLastName("Dela Cruz");
        
        assertEquals("First name should be Juan", "Juan", employee.getFirstName());
        assertEquals("Last name should be Dela Cruz", "Dela Cruz", employee.getLastName());
    }
    
    @Test
    public void testGetFullName() {
        // Test: Does full name combine first and last name correctly?
        employee.setFirstName("Maria");
        employee.setLastName("Santos");
        
        assertEquals("Full name should be Maria Santos", 
                    "Maria Santos", employee.getFullName());
    }
    
    // ==================== CONTACT INFORMATION TESTS ====================
    
    @Test
    public void testSetEmailAddress() {
        // Test: Can we set an employee's email?
        employee.setEmail("employee@motorph.com");
        assertEquals("Email should be saved correctly", 
                    "employee@motorph.com", employee.getEmail());
    }
    
    @Test
    public void testSetPhoneNumber() {
        // Test: Can we set a phone number?
        employee.setPhoneNumber("09171234567");
        assertEquals("Phone number should be saved correctly", 
                    "09171234567", employee.getPhoneNumber());
    }
    
    // ==================== BIRTH DATE TEST ====================
    
    @Test
    public void testSetBirthDate() {
        // Test: Can we set an employee's birth date?
        LocalDate birthDate = LocalDate.of(1990, 6, 15);
        employee.setBirthDate(birthDate);
        
        assertEquals("Birth date should be June 15, 1990", 
                    birthDate, employee.getBirthDate());
    }
    
    // ==================== SALARY TESTS ====================
    
    @Test
    public void testSetBasicSalary() {
        // Test: Can we set an employee's monthly salary?
        BigDecimal salary = new BigDecimal("25000.00");
        employee.setBasicSalary(salary);
        
        assertEquals("Basic salary should be 25,000", 
                    salary, employee.getBasicSalary());
    }
    
    @Test
    public void testSetHourlyRate() {
        // Test: Can we set an employee's hourly rate?
        BigDecimal hourlyRate = new BigDecimal("150.00");
        employee.setHourlyRate(hourlyRate);
        
        assertEquals("Hourly rate should be 150", 
                    hourlyRate, employee.getHourlyRate());
    }
    
    // ==================== EMPLOYEE STATUS TESTS ====================
    
    @Test
    public void testEmployeeStartsAsProbationary() {
        // Test: Do new employees start as probationary?
        assertEquals("New employee should be probationary by default", 
                    EmployeeStatus.PROBATIONARY, employee.getStatus());
    }
    
    @Test
    public void testChangeEmployeeToRegular() {
        // Test: Can we promote an employee to regular status?
        employee.setStatus(EmployeeStatus.REGULAR);
        assertEquals("Employee should now be regular", 
                    EmployeeStatus.REGULAR, employee.getStatus());
    }
    
    @Test
    public void testTerminateEmployee() {
        // Test: Can we terminate an employee?
        employee.setStatus(EmployeeStatus.TERMINATED);
        assertEquals("Employee should be terminated", 
                    EmployeeStatus.TERMINATED, employee.getStatus());
    }
    
    @Test
    public void testIsEmployeeActive() {
        // Test: Can we check if an employee is still active?
        employee.setStatus(EmployeeStatus.REGULAR);
        assertTrue("Regular employee should be active", employee.isActive());
        
        employee.setStatus(EmployeeStatus.TERMINATED);
        assertFalse("Terminated employee should NOT be active", employee.isActive());
    }
    
    // ==================== POSITION AND SUPERVISOR TESTS ====================
    
    @Test
    public void testSetEmployeePosition() {
        // Test: Can we assign an employee to a position?
        employee.setPositionId(5);
        assertEquals("Employee should be in position 5", 
                    Integer.valueOf(5), employee.getPositionId());
    }
    
    @Test
    public void testAssignSupervisor() {
        // Test: Can we assign a supervisor to an employee?
        employee.setSupervisorId(100);
        assertEquals("Employee's supervisor should be employee #100", 
                    Integer.valueOf(100), employee.getSupervisorId());
    }
    
    @Test
    public void testEmployeeHasSupervisor() {
        // Test: Can we check if employee has a supervisor?
        assertFalse("Employee should not have supervisor initially", 
                   employee.hasSupervisor());
        
        employee.setSupervisorId(100);
        assertTrue("Employee should now have a supervisor", 
                  employee.hasSupervisor());
    }
    
    // ==================== PASSWORD AND LOGIN TESTS ====================
    
    @Test
    public void testSetPassword() {
        // Test: Can we set an employee's password?
        employee.setPasswordHash("encrypted_password_123");
        assertEquals("Password should be saved", 
                    "encrypted_password_123", employee.getPasswordHash());
    }
    
    @Test
    public void testUpdateLastLogin() {
        // Test: Can we record when employee logs in?
        employee.updateLastLogin();
        assertNotNull("Last login time should be recorded", employee.getLastLogin());
    }
    
    // ==================== YEARS OF SERVICE TEST ====================
    
    @Test
    public void testCalculateYearsOfService() {
        // Test: Can we calculate how long employee has worked?
        employee.setCreatedAt(LocalDateTime.now().minusYears(3));
        assertEquals("Employee should have 3 years of service", 
                    3, employee.getYearsOfService());
    }
    
    
    // ==================== NEGATIVE TEST - INVALID DATA ====================
    
    @Test
    public void testNegativeSalary() {
        // NEGATIVE TEST: What happens with negative salary?
        BigDecimal negativeSalary = new BigDecimal("-5000");
        employee.setBasicSalary(negativeSalary);
        
        // Model accepts it (no validation) - this documents the behavior
        assertEquals("Model accepts negative salary (needs validation in DAO/Service)", 
                    negativeSalary, employee.getBasicSalary());
    }
    
    @Test
    public void testFutureBirthDate() {
        // NEGATIVE TEST: What happens with future birth date?
        LocalDate futureDate = LocalDate.now().plusYears(1);
        employee.setBirthDate(futureDate);
        
        // Model accepts it (no validation) - this documents the behavior
        assertEquals("Model accepts future birth date (needs validation in DAO/Service)", 
                    futureDate, employee.getBirthDate());
    }
    
    @Test
    public void testEmptyName() {
        // NEGATIVE TEST: What happens with empty name?
        employee.setFirstName("");
        employee.setLastName("");
        
        assertEquals("Model accepts empty first name", "", employee.getFirstName());
        assertEquals("Model accepts empty last name", "", employee.getLastName());
    }
    
    // ==================== NULL VALUE TESTS ====================
    
    @Test
    public void testNullEmail() {
        // Test: What happens when email is not set?
        employee.setEmail(null);
        assertNull("Email can be null", employee.getEmail());
    }
    
    @Test
    public void testNullSupervisor() {
        // Test: Can employee have no supervisor?
        employee.setSupervisorId(null);
        assertNull("Supervisor ID can be null", employee.getSupervisorId());
        assertFalse("Employee should not have supervisor", employee.hasSupervisor());
    }
    
    // ==================== ID MANAGEMENT TEST ====================
    
    @Test
    public void testEmployeeIdManagement() {
        // Test: Employee ID (usually set by database)
        assertNull("New employee should not have ID yet", employee.getEmployeeId());
        
        employee.setEmployeeId(12345);
        assertEquals("Employee ID should be 12345", 
                    Integer.valueOf(12345), employee.getEmployeeId());
    }
    
    // ==================== EQUALS TEST ====================
    
    @Test
    public void testTwoEmployeesAreEqual() {
        // Test: Are two employees with same ID considered equal?
        employee.setEmployeeId(100);
        
        EmployeeModel anotherEmployee = new EmployeeModel();
        anotherEmployee.setEmployeeId(100);
        
        assertTrue("Employees with same ID should be equal", 
                  employee.equals(anotherEmployee));
    }
    
    @Test
    public void testTwoEmployeesAreNotEqual() {
        // Test: Are two employees with different IDs not equal?
        employee.setEmployeeId(100);
        
        EmployeeModel anotherEmployee = new EmployeeModel();
        anotherEmployee.setEmployeeId(200);
        
        assertFalse("Employees with different IDs should not be equal", 
                   employee.equals(anotherEmployee));
    }
    
    // ==================== REAL-WORLD SCENARIO TESTS ====================
    
    @Test
    public void testNewHireScenario() {
        // Test: Complete new hire setup
        LocalDate birthDate = LocalDate.of(1995, 3, 20);
        
        // Create new employee
        EmployeeModel newHire = new EmployeeModel(
            "Pedro", "Garcia", birthDate, 
            "pedro.garcia@motorph.com", "password123", 3
        );
        
        // Set additional info
        newHire.setPhoneNumber("09181234567");
        newHire.setBasicSalary(new BigDecimal("20000"));
        newHire.setSupervisorId(50);
        
        // Verify setup
        assertEquals("Name should be Pedro Garcia", "Pedro Garcia", newHire.getFullName());
        assertEquals("Should be probationary", EmployeeStatus.PROBATIONARY, newHire.getStatus());
        assertTrue("Should have supervisor", newHire.hasSupervisor());
        assertTrue("Should be active", newHire.isActive());
    }
 
    @Test
    public void testEmployeeTerminationScenario() {
        // Test: Terminate an employee
        employee.setFirstName("Carlos");
        employee.setLastName("Mendoza");
        employee.setEmployeeId(500);
        employee.setStatus(EmployeeStatus.REGULAR);
        
        // Terminate employee
        employee.setStatus(EmployeeStatus.TERMINATED);
        
        // Verify termination
        assertFalse("Terminated employee should not be active", employee.isActive());
        assertEquals("Should still have employee ID", 
                    Integer.valueOf(500), employee.getEmployeeId());
        assertEquals("Should still have name", "Carlos Mendoza", employee.getFullName());
    }
}