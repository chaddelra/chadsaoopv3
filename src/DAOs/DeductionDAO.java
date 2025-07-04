package DAOs;

import Models.DeductionModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeductionDAO {
    
    public boolean addDeduction(DeductionModel deduction) {
        String sql = "INSERT INTO deduction (typeName, deductionAmount, lowerLimit, upperLimit, baseTax, deductionRate, payrollId) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, deduction.getDeductionType().getDisplayName());
            pstmt.setBigDecimal(2, deduction.getAmount());
            pstmt.setBigDecimal(3, deduction.getLowerLimit());
            pstmt.setBigDecimal(4, deduction.getUpperLimit());
            pstmt.setBigDecimal(5, deduction.getBaseTax());
            pstmt.setBigDecimal(6, deduction.getDeductionRate());
            pstmt.setInt(7, deduction.getPayPeriodId()); // This maps to payrollId in database
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        deduction.setDeductionId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error adding deduction: " + e.getMessage());
        }
        return false;
    }
    
    public DeductionModel getDeductionById(int deductionId) {
        String sql = "SELECT * FROM deduction WHERE deductionId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, deductionId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractDeductionFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting deduction by ID: " + e.getMessage());
        }
        return null;
    }
    
    public List<DeductionModel> getDeductionsByPayPeriodId(int payPeriodId) {
        List<DeductionModel> deductions = new ArrayList<>();
        String sql = "SELECT d.* FROM deduction d " +
                    "JOIN payroll p ON d.payrollId = p.payrollId " +
                    "WHERE p.payPeriodId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, payPeriodId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                deductions.add(extractDeductionFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting deductions by pay period ID: " + e.getMessage());
        }
        return deductions;
    }
    
    public List<DeductionModel> getDeductionsByEmployeeId(int employeeId) {
        List<DeductionModel> deductions = new ArrayList<>();
        String sql = "SELECT d.*, p.employeeId FROM deduction d " +
                    "JOIN payroll p ON d.payrollId = p.payrollId " +
                    "WHERE p.employeeId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                deductions.add(extractDeductionFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting deductions by employee ID: " + e.getMessage());
        }
        return deductions;
    }
    
    public List<DeductionModel> getDeductionsByType(DeductionModel.DeductionType deductionType) {
        List<DeductionModel> deductions = new ArrayList<>();
        String sql = "SELECT * FROM deduction WHERE typeName = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, deductionType.getDisplayName());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                deductions.add(extractDeductionFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting deductions by type: " + e.getMessage());
        }
        return deductions;
    }
    
    public List<DeductionModel> getDeductionsByPayrollId(int payrollId) {
        List<DeductionModel> deductions = new ArrayList<>();
        String sql = "SELECT * FROM deduction WHERE payrollId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, payrollId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                deductions.add(extractDeductionFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting deductions by payroll ID: " + e.getMessage());
        }
        return deductions;
    }
    
    public boolean updateDeduction(DeductionModel deduction) {
        String sql = "UPDATE deduction SET typeName = ?, deductionAmount = ?, lowerLimit = ?, upperLimit = ?, baseTax = ?, deductionRate = ?, payrollId = ? WHERE deductionId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, deduction.getDeductionType().getDisplayName());
            pstmt.setBigDecimal(2, deduction.getAmount());
            pstmt.setBigDecimal(3, deduction.getLowerLimit());
            pstmt.setBigDecimal(4, deduction.getUpperLimit());
            pstmt.setBigDecimal(5, deduction.getBaseTax());
            pstmt.setBigDecimal(6, deduction.getDeductionRate());
            pstmt.setInt(7, deduction.getPayPeriodId()); // Maps to payrollId in database
            pstmt.setInt(8, deduction.getDeductionId());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating deduction: " + e.getMessage());
        }
        return false;
    }
    
    public boolean deleteDeduction(int deductionId) {
        String sql = "DELETE FROM deduction WHERE deductionId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, deductionId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting deduction: " + e.getMessage());
        }
        return false;
    }
    
    public List<DeductionModel> getAllDeductions() {
        List<DeductionModel> deductions = new ArrayList<>();
        String sql = "SELECT * FROM deduction ORDER BY deductionId";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                deductions.add(extractDeductionFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all deductions: " + e.getMessage());
        }
        return deductions;
    }
    
    /**
     * Gets deductions for an employee within a specific pay period
     * @param employeeId Employee ID
     * @param payPeriodId Pay period ID
     * @return List of deductions for the employee in the pay period
     */
    public List<DeductionModel> getDeductionsByEmployeeAndPeriod(int employeeId, int payPeriodId) {
        List<DeductionModel> deductions = new ArrayList<>();
        String sql = "SELECT d.*, p.employeeId FROM deduction d " +
                    "JOIN payroll p ON d.payrollId = p.payrollId " +
                    "WHERE p.employeeId = ? AND p.payPeriodId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, employeeId);
            pstmt.setInt(2, payPeriodId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                deductions.add(extractDeductionFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting deductions by employee and period: " + e.getMessage());
        }
        return deductions;
    }
    
    /**
     * Gets master deduction rules (templates) from database
     * These are deductions where payrollId is NULL - they serve as calculation templates
     * @return List of master deduction rules
     */
    public List<DeductionModel> getMasterDeductionRules() {
        List<DeductionModel> rules = new ArrayList<>();
        String sql = "SELECT * FROM deduction WHERE payrollId IS NULL ORDER BY typeName, lowerLimit";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                rules.add(extractDeductionFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting master deduction rules: " + e.getMessage());
        }
        return rules;
    }
    
    /**
     * Gets master deduction rules for a specific type
     * @param deductionType The type of deduction
     * @return List of deduction rules for the specified type
     */
    public List<DeductionModel> getMasterDeductionRulesByType(DeductionModel.DeductionType deductionType) {
        List<DeductionModel> rules = new ArrayList<>();
        String sql = "SELECT * FROM deduction WHERE payrollId IS NULL AND typeName = ? ORDER BY lowerLimit";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, deductionType.getDisplayName());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                rules.add(extractDeductionFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting master deduction rules by type: " + e.getMessage());
        }
        return rules;
    }
    
    /**
     * Creates standard deductions for an employee's payroll using database-driven calculations
     * @param employeeId Employee ID
     * @param payrollId Payroll ID
     * @param grossIncome Employee's gross income for SSS calculation
     * @param monthlySalary Employee's monthly salary for other calculations
     * @return true if all deductions were created successfully
     */
    public boolean createStandardDeductions(int employeeId, int payrollId, java.math.BigDecimal grossIncome, java.math.BigDecimal monthlySalary) {
        try {
            // Get all master deduction rules from database
            List<DeductionModel> allRules = getMasterDeductionRules();
            
            if (allRules.isEmpty()) {
                System.err.println("No master deduction rules found in database");
                return false;
            }
            
            // Filter rules by type for calculations
            List<DeductionModel> sssRules = DeductionModel.filterDeductionsByType(allRules, DeductionModel.DeductionType.SSS);
            List<DeductionModel> philHealthRules = DeductionModel.filterDeductionsByType(allRules, DeductionModel.DeductionType.PHILHEALTH);
            List<DeductionModel> pagIbigRules = DeductionModel.filterDeductionsByType(allRules, DeductionModel.DeductionType.PAG_IBIG);
            List<DeductionModel> taxRules = DeductionModel.filterDeductionsByType(allRules, DeductionModel.DeductionType.WITHHOLDING_TAX);
            
            // Calculate deduction amounts using database rules
            java.math.BigDecimal sssAmount = DeductionModel.calculateSSSDeduction(grossIncome, sssRules);
            java.math.BigDecimal philHealthAmount = DeductionModel.calculatePhilHealthDeduction(monthlySalary, philHealthRules);
            java.math.BigDecimal pagIbigAmount = DeductionModel.calculatePagIbigDeduction(monthlySalary, pagIbigRules);
            
            // Calculate taxable income for withholding tax
            java.math.BigDecimal taxableIncome = monthlySalary.subtract(sssAmount).subtract(philHealthAmount).subtract(pagIbigAmount);
            java.math.BigDecimal taxAmount = DeductionModel.calculateWithholdingTax(taxableIncome, taxRules);
            
            // Create SSS deduction
            DeductionModel sssDeduction = new DeductionModel();
            sssDeduction.setEmployeeId(employeeId);
            sssDeduction.setDeductionType(DeductionModel.DeductionType.SSS);
            sssDeduction.setAmount(sssAmount);
            sssDeduction.setPayPeriodId(payrollId); // Maps to payrollId in database
            
            // Create PhilHealth deduction
            DeductionModel philhealthDeduction = new DeductionModel();
            philhealthDeduction.setEmployeeId(employeeId);
            philhealthDeduction.setDeductionType(DeductionModel.DeductionType.PHILHEALTH);
            philhealthDeduction.setAmount(philHealthAmount);
            philhealthDeduction.setPayPeriodId(payrollId);
            
            // Create Pag-IBIG deduction
            DeductionModel pagibigDeduction = new DeductionModel();
            pagibigDeduction.setEmployeeId(employeeId);
            pagibigDeduction.setDeductionType(DeductionModel.DeductionType.PAG_IBIG);
            pagibigDeduction.setAmount(pagIbigAmount);
            pagibigDeduction.setPayPeriodId(payrollId);
            
            // Create Withholding Tax deduction
            DeductionModel withholdingTaxDeduction = new DeductionModel();
            withholdingTaxDeduction.setEmployeeId(employeeId);
            withholdingTaxDeduction.setDeductionType(DeductionModel.DeductionType.WITHHOLDING_TAX);
            withholdingTaxDeduction.setAmount(taxAmount);
            withholdingTaxDeduction.setPayPeriodId(payrollId);
            
            // Add all deductions
            boolean success = true;
            success &= addDeduction(sssDeduction);
            success &= addDeduction(philhealthDeduction);
            success &= addDeduction(pagibigDeduction);
            success &= addDeduction(withholdingTaxDeduction);
            
            return success;
            
        } catch (Exception e) {
            System.err.println("Error creating standard deductions: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Creates a late deduction for rank-and-file employees
     * @param employeeId Employee ID
     * @param payrollId Payroll ID
     * @param hourlyRate Employee's hourly rate
     * @param lateHours Number of late hours
     * @return true if late deduction was created successfully
     */
    public boolean createLateDeduction(int employeeId, int payrollId, java.math.BigDecimal hourlyRate, java.math.BigDecimal lateHours) {
        try {
            java.math.BigDecimal lateAmount = DeductionModel.calculateLateDeduction(hourlyRate, lateHours);
            
            if (lateAmount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                return true; // No late deduction needed
            }
            
            DeductionModel lateDeduction = new DeductionModel();
            lateDeduction.setEmployeeId(employeeId);
            lateDeduction.setDeductionType(DeductionModel.DeductionType.LATE_DEDUCTION);
            lateDeduction.setAmount(lateAmount);
            lateDeduction.setPayPeriodId(payrollId);
            
            return addDeduction(lateDeduction);
            
        } catch (Exception e) {
            System.err.println("Error creating late deduction: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets total deductions for a specific payroll
     * @param payrollId Payroll ID
     * @return Total deduction amount
     */
    public java.math.BigDecimal getTotalDeductionsForPayroll(int payrollId) {
        String sql = "SELECT SUM(deductionAmount) FROM deduction WHERE payrollId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, payrollId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                java.math.BigDecimal total = rs.getBigDecimal(1);
                return total != null ? total : java.math.BigDecimal.ZERO;
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting total deductions for payroll: " + e.getMessage());
        }
        
        return java.math.BigDecimal.ZERO;
    }
    
    /**
     * Gets total deductions by type for a specific payroll
     * @param payrollId Payroll ID
     * @param deductionType Type of deduction
     * @return Total deduction amount for the specified type
     */
    public java.math.BigDecimal getTotalDeductionsByTypeForPayroll(int payrollId, DeductionModel.DeductionType deductionType) {
        String sql = "SELECT SUM(deductionAmount) FROM deduction WHERE payrollId = ? AND typeName = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, payrollId);
            pstmt.setString(2, deductionType.getDisplayName());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                java.math.BigDecimal total = rs.getBigDecimal(1);
                return total != null ? total : java.math.BigDecimal.ZERO;
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting total deductions by type for payroll: " + e.getMessage());
        }
        
        return java.math.BigDecimal.ZERO;
    }
    
    /**
     * Deletes all deductions for a specific payroll
     * @param payrollId Payroll ID
     * @return true if deletion was successful
     */
    public boolean deleteDeductionsForPayroll(int payrollId) {
        String sql = "DELETE FROM deduction WHERE payrollId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, payrollId);
            pstmt.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error deleting deductions for payroll: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if deduction rules exist in the database
     * @return true if master deduction rules are properly configured
     */
    public boolean areDeductionRulesConfigured() {
        List<DeductionModel> rules = getMasterDeductionRules();
        
        // Check if we have at least one rule for each mandatory deduction type
        boolean hasSSSRules = rules.stream().anyMatch(r -> r.getDeductionType() == DeductionModel.DeductionType.SSS);
        boolean hasPhilHealthRules = rules.stream().anyMatch(r -> r.getDeductionType() == DeductionModel.DeductionType.PHILHEALTH);
        boolean hasPagIbigRules = rules.stream().anyMatch(r -> r.getDeductionType() == DeductionModel.DeductionType.PAG_IBIG);
        boolean hasTaxRules = rules.stream().anyMatch(r -> r.getDeductionType() == DeductionModel.DeductionType.WITHHOLDING_TAX);
        
        return hasSSSRules && hasPhilHealthRules && hasPagIbigRules && hasTaxRules;
    }
    
    private DeductionModel extractDeductionFromResultSet(ResultSet rs) throws SQLException {
        DeductionModel deduction = new DeductionModel();
        deduction.setDeductionId(rs.getInt("deductionId"));
        
        // Convert database typeName to enum
        String typeName = rs.getString("typeName");
        try {
            deduction.setDeductionType(DeductionModel.DeductionType.fromString(typeName));
        } catch (IllegalArgumentException e) {
            System.err.println("Unknown deduction type in database: " + typeName);
            // Set a default or handle as needed
            deduction.setDeductionType(DeductionModel.DeductionType.SSS);
        }
        
        deduction.setAmount(rs.getBigDecimal("deductionAmount"));
        deduction.setLowerLimit(rs.getBigDecimal("lowerLimit"));
        deduction.setUpperLimit(rs.getBigDecimal("upperLimit"));
        deduction.setBaseTax(rs.getBigDecimal("baseTax"));
        deduction.setDeductionRate(rs.getBigDecimal("deductionRate"));
        
        // Map payrollId from database to payPeriodId in model
        int payrollId = rs.getInt("payrollId");
        if (!rs.wasNull()) {
            deduction.setPayPeriodId(payrollId);
        }
        
        // Try to get employeeId if available (from JOIN queries)
        try {
            int employeeId = rs.getInt("employeeId");
            if (!rs.wasNull()) {
                deduction.setEmployeeId(employeeId);
            }
        } catch (SQLException e) {
            // employeeId column not available in this query - that's okay
            deduction.setEmployeeId(null);
        }
        
        return deduction;
    }
}