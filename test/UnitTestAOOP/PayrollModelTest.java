package UnitTestAOOP;

import Models.PayrollModel;
import org.junit.*;
import static org.junit.Assert.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JUnit test for PayrollModel with negative testing
 * @author martin
 */

public class PayrollModelTest {
    
    private PayrollModel payrollModel;
    
    @Before
    public void setUp() {
        payrollModel = new PayrollModel();
    }
    
    @After
    public void tearDown() {
        payrollModel = null;
    }
    
    // ==========================================
    // CONSTRUCTOR TESTS
    // ==========================================
    
    @Test
    public void testDefaultConstructor() {
        PayrollModel model = new PayrollModel();
        assertNotNull("Model should not be null", model);
        assertEquals("Basic salary should be ZERO", BigDecimal.ZERO, model.getBasicSalary());
        assertEquals("Gross income should be ZERO", BigDecimal.ZERO, model.getGrossIncome());
        assertEquals("Total benefit should be ZERO", BigDecimal.ZERO, model.getTotalBenefit());
        assertEquals("Total deduction should be ZERO", BigDecimal.ZERO, model.getTotalDeduction());
        assertEquals("Net salary should be ZERO", BigDecimal.ZERO, model.getNetSalary());
    }
    
    @Test
    public void testParameterizedConstructor_NullSalary() {
        PayrollModel model = new PayrollModel(1001, 5, null);
        assertEquals("Basic salary should default to ZERO when null", 
                    BigDecimal.ZERO, model.getBasicSalary());
    }
    
    // ==========================================
    // POSITIVE SETTER TESTS
    // ==========================================
    
    @Test
    public void testSetBasicSalary_Valid() {
        BigDecimal validSalary = new BigDecimal("35000.50");
        payrollModel.setBasicSalary(validSalary);
        assertEquals("Basic salary should be set correctly", validSalary, payrollModel.getBasicSalary());
    }
    
    // ==========================================
    // NEGATIVE SETTER TESTS (CRITICAL)
    // ==========================================
    
    @Test
    public void testSetBasicSalary_Null() {
        payrollModel.setBasicSalary(new BigDecimal("50000"));
        payrollModel.setBasicSalary(null);
        assertEquals("Basic salary should default to ZERO when set to null", 
                    BigDecimal.ZERO, payrollModel.getBasicSalary());
    }
    
    @Test
    public void testSetGrossIncome_Negative() {
        BigDecimal negativeGross = new BigDecimal("-5000.00");
        payrollModel.setGrossIncome(negativeGross);
        // The model accepts negative values but we test if it's stored
        assertEquals("Negative gross income should be stored", negativeGross, payrollModel.getGrossIncome());
        
        // Business validation would typically prevent this
        assertFalse("Model with negative gross should be invalid", payrollModel.isValid());
    }
    
    @Test
    public void testSetNetSalary_GreaterThanGross() {
        BigDecimal gross = new BigDecimal("30000.00");
        BigDecimal net = new BigDecimal("35000.00"); // Net > Gross (invalid scenario)
        
        payrollModel.setGrossIncome(gross);
        payrollModel.setNetSalary(net);
        
        // The model stores the values but business logic should catch this
        assertTrue("Net salary greater than gross should be detectable", 
                  payrollModel.getNetSalary().compareTo(payrollModel.getGrossIncome()) > 0);
    }
    
    @Test
    public void testSetEmployeeId_Invalid() {
        // Test with null
        payrollModel.setEmployeeId(null);
        assertNull("Employee ID can be null", payrollModel.getEmployeeId());
        assertFalse("Model with null employee ID should be invalid", payrollModel.isValid());
        
        // Test with negative ID
        payrollModel.setEmployeeId(-1);
        assertEquals("Negative employee ID should be stored", Integer.valueOf(-1), payrollModel.getEmployeeId());
    }
    
    // ==========================================
    // BUSINESS LOGIC TESTS
    // ==========================================
    
    @Test
    public void testCalculateGrossIncome_Valid() {
        BigDecimal basicSalary = new BigDecimal("30000.00");
        BigDecimal benefits = new BigDecimal("5000.00");
        
        payrollModel.setBasicSalary(basicSalary);
        payrollModel.setTotalBenefit(benefits);
        payrollModel.calculateGrossIncome();
        
        BigDecimal expectedGross = basicSalary.add(benefits);
        assertEquals("Gross income should equal basic salary + benefits", 
                    expectedGross, payrollModel.getGrossIncome());
    }
    
    @Test
    public void testCalculateNetSalary_Valid() {
        BigDecimal grossIncome = new BigDecimal("35000.00");
        BigDecimal deductions = new BigDecimal("7000.00");
        
        payrollModel.setGrossIncome(grossIncome);
        payrollModel.setTotalDeduction(deductions);
        payrollModel.calculateNetSalary();
        
        BigDecimal expectedNet = grossIncome.subtract(deductions);
        assertEquals("Net salary should equal gross income - deductions", 
                    expectedNet, payrollModel.getNetSalary());
    }
    
    @Test
    public void testCalculateNetSalary_DeductionsExceedGross() {
        BigDecimal grossIncome = new BigDecimal("20000.00");
        BigDecimal deductions = new BigDecimal("25000.00"); // Deductions > Gross
        
        payrollModel.setGrossIncome(grossIncome);
        payrollModel.setTotalDeduction(deductions);
        payrollModel.calculateNetSalary();
        
        BigDecimal expectedNet = grossIncome.subtract(deductions);
        assertTrue("Net salary should be negative when deductions exceed gross", 
                  payrollModel.getNetSalary().compareTo(BigDecimal.ZERO) < 0);
    }
    
    // ==========================================
    // VALIDATION TESTS
    // ==========================================
    
    @Test
    public void testIsValid_CompleteModel() {
        payrollModel.setEmployeeId(1001);
        payrollModel.setPayPeriodId(5);
        payrollModel.setBasicSalary(new BigDecimal("30000.00"));
        payrollModel.setGrossIncome(new BigDecimal("35000.00"));
        payrollModel.setTotalDeduction(new BigDecimal("7000.00"));
        payrollModel.setNetSalary(new BigDecimal("28000.00"));
        
        assertTrue("Complete model should be valid", payrollModel.isValid());
    }
    
    // ==========================================
    // EDGE CASE TESTS
    // ==========================================
    
    @Test
    public void testExtremelyLargeSalaryValues() {
        BigDecimal largeSalary = new BigDecimal("999999999.99");
        payrollModel.setBasicSalary(largeSalary);
        payrollModel.setGrossIncome(largeSalary);
        
        assertEquals("Should handle large salary values", largeSalary, payrollModel.getBasicSalary());
        assertEquals("Should handle large gross values", largeSalary, payrollModel.getGrossIncome());
    }
    
    @Test
    public void testPrecisionHandling() {
        BigDecimal preciseValue = new BigDecimal("12345.67");
        payrollModel.setBasicSalary(preciseValue);
        
        assertEquals("Should maintain decimal precision", preciseValue, payrollModel.getBasicSalary());
    }
    
    // ==========================================
    // INTEGRATION SCENARIO TESTS
    // ==========================================
    
    @Test
    public void testCompletePayrollCalculation() {
        // Simulate a complete payroll calculation scenario
        BigDecimal basicSalary = new BigDecimal("50000.00");
        BigDecimal benefits = new BigDecimal("8000.00");
        BigDecimal deductions = new BigDecimal("12000.00");
        
        payrollModel.setEmployeeId(1001);
        payrollModel.setPayPeriodId(5);
        payrollModel.setBasicSalary(basicSalary);
        payrollModel.setTotalBenefit(benefits);
        
        // Calculate gross income
        payrollModel.calculateGrossIncome();
        assertEquals("Gross should be 58000", new BigDecimal("58000.00"), payrollModel.getGrossIncome());
        
        // Set deductions and calculate net
        payrollModel.setTotalDeduction(deductions);
        payrollModel.calculateNetSalary();
        assertEquals("Net should be 46000", new BigDecimal("46000.00"), payrollModel.getNetSalary());
        
        assertTrue("Complete payroll should be valid", payrollModel.isValid());
    }
    
    @Test
    public void testMinimumWageScenario() {
        // Test with minimum wage values
        BigDecimal minimumWage = new BigDecimal("537.00"); // Daily minimum wage * 26 days
        BigDecimal monthlyMinimum = minimumWage.multiply(new BigDecimal("26"));
        
        payrollModel.setEmployeeId(3001);
        payrollModel.setPayPeriodId(15);
        payrollModel.setBasicSalary(monthlyMinimum);
        payrollModel.setTotalBenefit(BigDecimal.ZERO);
        payrollModel.setTotalDeduction(new BigDecimal("1000.00")); // Some deductions
        
        payrollModel.calculateGrossIncome();
        payrollModel.calculateNetSalary();
        
        assertTrue("Minimum wage payroll should be valid", payrollModel.isValid());
        assertTrue("Net salary should be positive for minimum wage", 
                  payrollModel.getNetSalary().compareTo(BigDecimal.ZERO) > 0);
    }
}