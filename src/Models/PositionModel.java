package Models;

import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * PositionModel class that maps to the position table
 * Fields: positionId, position, positionDescription, department
 * Handles job positions and organizational structure with automatic department assignment
 * 
 * Updated to support rank-and-file detection and payroll business logic
 * 
 * Departments in MPH:
 * - Leadership 
 * - HR (Human Resources) - includes HR Rank and File
 * - IT (Information Technology)
 * - Accounting (Payroll and Accounting) - includes Payroll Rank and File
 * - Accounts (Account Management) - includes Account Rank and File
 * - Sales and Marketing
 * - Supply Chain and Logistics
 * - Customer Service
 * - Other (fallback)
 * 
 * 
 * @author User
 */
public class PositionModel {
    
    private Integer positionId;
    private String position;           // Job title/position name
    private String positionDescription;
    private String department;
    
    // Constants for common departments 
    public static final String DEPT_LEADERSHIP = "Leadership";
    public static final String DEPT_HR = "HR";
    public static final String DEPT_IT = "IT";
    public static final String DEPT_ACCOUNTING = "Accounting";
    public static final String DEPT_ACCOUNTS = "Accounts";
    public static final String DEPT_SALES_MARKETING = "Sales and Marketing";
    public static final String DEPT_SUPPLY_CHAIN = "Supply Chain and Logistics";
    public static final String DEPT_CUSTOMER_SERVICE = "Customer Service";
    public static final String DEPT_OTHER = "Other";
    
    // Constants for common position types
    public static final String TYPE_EXECUTIVE = "Executive";
    public static final String TYPE_MANAGER = "Manager";
    public static final String TYPE_SUPERVISOR = "Supervisor";
    public static final String TYPE_SPECIALIST = "Specialist";
    public static final String TYPE_ASSOCIATE = "Associate";
    public static final String TYPE_ENTRY_LEVEL = "Entry Level";
    public static final String TYPE_RANK_AND_FILE = "Rank and File";
    

    // CONSTRUCTORS
    
    /**
     * Default constructor
     */
    public PositionModel() {}
    
    /**
     * Constructor with essential fields - auto-assigns department
     * @param position Job title/position name
     */
    public PositionModel(String position) {
        this.position = position;
        this.department = getDepartmentForPosition(position); // Auto-assign department
    }
    
    /**
     * Constructor with position and description - auto-assigns department
     * @param position Job title/position name
     * @param positionDescription Description of the position
     */
    public PositionModel(String position, String positionDescription) {
        this.position = position;
        this.positionDescription = positionDescription;
        this.department = getDepartmentForPosition(position);
    }
    
    /**
     * Full constructor with all fields
     * @param positionId Position ID (from database)
     * @param position Job title/position name
     * @param positionDescription Description of the position
     * @param department Department name
     */
    public PositionModel(Integer positionId, String position, String positionDescription, String department) {
        this.positionId = positionId;
        this.position = position;
        this.positionDescription = positionDescription;
        this.department = department;
    }
    

    // STATIC FACTORY METHODS

    
    /**
     * Creates a position with explicit department (doesn't auto-assign)
     * @param position Job title/position name
     * @param department Department name
     * @return PositionModel instance
     */
    public static PositionModel withDepartment(String position, String department) {
        PositionModel model = new PositionModel();
        model.position = position;
        model.department = department;
        return model;
    }
    
    /**
     * Creates a position with description and auto-assigned department
     * @param position Job title/position name
     * @param positionDescription Description of the position
     * @return PositionModel instance
     */
    public static PositionModel withDescription(String position, String positionDescription) {
        PositionModel model = new PositionModel();
        model.position = position;
        model.positionDescription = positionDescription;
        model.department = getDepartmentForPosition(position);
        return model;
    }
    
    /**
     * Creates a rank-and-file position with automatic department assignment
     * @param position Job title/position name (should contain "rank and file")
     * @return PositionModel instance with appropriate department assigned
     */
    public static PositionModel createRankAndFilePosition(String position) {
        PositionModel model = new PositionModel();
        model.position = position;
        model.department = getDepartmentForPosition(position); // Assign to actual department
        return model;
    }
    

    // GETTERS AND SETTERS

    
    public Integer getPositionId() {
        return positionId;
    }
    
    public void setPositionId(Integer positionId) {
        this.positionId = positionId;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
        // Auto-assign department when position changes
        this.assignDepartmentByPosition();
    }
    
    public String getPositionDescription() {
        return positionDescription;
    }
    
    public void setPositionDescription(String positionDescription) {
        this.positionDescription = positionDescription;
    }
    
    /**
     * Gets the assigned department (auto-assigns if null)
     * @return Department name
     */
    public String getDepartment() {
        if (department == null) {
            assignDepartmentByPosition();
        }
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    

    // RANK-AND-FILE DETECTION METHODS

    
    /**
     * Determines if this position is rank-and-file based on business rules
     * Rule: position contains 'rank' and 'file' (case insensitive)
     * Rank-and-file employees can be in any department (HR, Accounting, Accounts, etc.)
     * @return true if position is rank-and-file
     */
    public boolean isRankAndFile() {
        String pos = getPosition();
        
        // Rule: position LIKE '%rank%file%' (case insensitive)
        return pos != null && pos.toLowerCase().contains("rank") && pos.toLowerCase().contains("file");
    }
    
    /**
     * Determines if this position is non rank-and-file (management/supervisors)
     * @return true if position is non rank-and-file
     */
    public boolean isNonRankAndFile() {
        return !isRankAndFile();
    }
    
    /**
     * Checks if a position title indicates rank-and-file status
     * @param positionTitle Position title to check
     * @return true if position title indicates rank-and-file
     */
    public static boolean isRankAndFilePosition(String positionTitle) {
        if (positionTitle == null) return false;
        
        String lowerTitle = positionTitle.toLowerCase();
        return lowerTitle.contains("rank") && lowerTitle.contains("file");
    }
    
    /**
     * Checks if a department name indicates rank-and-file status
     * NOTE: Rank-and-file is not a department, it's a position level
     * @param departmentName Department name to check
     * @return always false since rank-and-file is not a department
     */
    public static boolean isRankAndFileDepartment(String departmentName) {
        // Rank-and-file is not a department, it's a position level
        return false;
    }
    
    /**
     * Gets the payroll category for this position
     * @return Payroll category string
     */
    public String getPayrollCategory() {
        return isRankAndFile() ? "Rank-and-File" : "Non Rank-and-File";
    }
    
    /**
     * Gets salary calculation method for this position
     * @return Salary calculation method description
     */
    public String getSalaryCalculationMethod() {
        if (isRankAndFile()) {
            return "Daily Rate × Days Worked (with late deductions) + Overtime Pay";
        } else {
            return "Basic Salary - (Absent Days × Daily Rate)";
        }
    }
    
    /**
     * Checks if position is eligible for overtime pay
     * @return true if eligible for overtime
     */
    public boolean isEligibleForOvertime() {
        return isRankAndFile();
    }
    
    /**
     * Checks if position is subject to late deductions
     * @return true if subject to late deductions
     */
    public boolean isSubjectToLateDeductions() {
        return isRankAndFile();
    }
    

    // BUSINESS METHODS

    
    /**
     * Validates the position data
     * @return true if position data is valid
     */
    public boolean isValid() {
        if (position == null || position.trim().isEmpty()) {
            return false;
        }
        
        if (position.length() > 50) {
            return false;
        }
        
        if (positionDescription != null && positionDescription.length() > 255) {
            return false;
        }
        
        return !(department != null && department.length() > 50);
    }
    
    /**
     * Determines the position level based on actual job titles
     * @return Position level (Executive, Manager, Supervisor, etc.)
     */
    public String getPositionLevel() {
        if (position == null) {
            return TYPE_ENTRY_LEVEL;
        }
        
        // Check if rank-and-file first
        if (isRankAndFile()) {
            return TYPE_RANK_AND_FILE;
        }
        
        // Check exact matches for actual positions
        switch (position.trim()) {
            case "Chief Executive Officer", "Chief Operating Officer", "Chief Finance Officer", "Chief Marketing Officer" -> {
                return TYPE_EXECUTIVE;
            }
            case "HR Manager", "Payroll Manager", "Account Manager", "Accounting Head" -> {
                return TYPE_MANAGER;
            }
            case "HR Team Leader", "Payroll Team Leader", "Account Team Leader" -> {
                return TYPE_SUPERVISOR;
            }
            case "IT Operations and Systems" -> {
                return TYPE_SPECIALIST;
            }
            
            case "Sales & Marketing", "Supply Chain and Logistics", "Customer Service and Relations" -> {
                return TYPE_ASSOCIATE;
            }
            default -> {
                // Fallback logic
                String lowerPosition = position.toLowerCase();
                if (lowerPosition.contains("chief")) {
                    return TYPE_EXECUTIVE;
                } else if (lowerPosition.contains("manager") || lowerPosition.contains("head")) {
                    return TYPE_MANAGER;
                } else if (lowerPosition.contains("leader") || lowerPosition.contains("supervisor")) {
                    return TYPE_SUPERVISOR;
                } else if (lowerPosition.contains("specialist") || lowerPosition.contains("operations")) {
                    return TYPE_SPECIALIST;
                } else {
                    return TYPE_ASSOCIATE;
                }
            }
        }
    }
    
    /**
     * Determines user role based on position (matches UserAuthenticationModel logic exactly)
     * @return User role for authentication system
     */
    public String determineUserRole() {
        if (position == null) {
            throw new IllegalArgumentException("Position cannot be null");
        }
        
        // Normalize the position by trimming whitespace  
        String normalizedPosition = position.trim();
        
        // Check for numeric values which would indicate an error
        if (normalizedPosition.matches("\\d+")) {
            throw new IllegalArgumentException("Position appears to be a numeric ID instead of a job title: " + normalizedPosition);
        }
        
        // Check for executive positions (immediate supervisors)
        switch (normalizedPosition) {
            case "Chief Executive Officer", "Chief Operating Officer", "Chief Finance Officer", "Chief Marketing Officer", "Account Manager", "Account Team Leader" -> {
                return "IMMEDIATESUPERVISOR";
            }
            case "IT Operations and Systems" -> {
                return "IT";
            }
            case "HR Manager", "HR Team Leader", "HR Rank and File" -> {
                return "HR";
            }
            case "Accounting Head", "Payroll Manager", "Payroll Team Leader", "Payroll Rank and File" -> {
                return "ACCOUNTING";
            }
            case "Account Rank and File", "Sales & Marketing", "Supply Chain and Logistics", "Customer Service and Relations" -> {
                return "EMPLOYEE";
            }
            default -> {
                System.out.println("Position not directly matched: '" + normalizedPosition + "'. Attempting to infer role.");
                
                // Try to infer the role from the position name
                String lowerPosition = normalizedPosition.toLowerCase();
                
                if (lowerPosition.contains("hr") || lowerPosition.contains("human resource")) {
                    return "HR";
                }
                else if (lowerPosition.contains("it") || lowerPosition.contains("information tech") || lowerPosition.contains("system")) {
                    return "IT";
                }
                else if (lowerPosition.contains("account") || lowerPosition.contains("payroll") || lowerPosition.contains("finance")) {
                    return "ACCOUNTING";
                }
                else if (lowerPosition.contains("manager") || lowerPosition.contains("supervisor") || lowerPosition.contains("lead")) {
                    return "IMMEDIATESUPERVISOR";
                }
                else {
                    System.out.println("Unknown position detected: '" + normalizedPosition + "'. Defaulting to EMPLOYEE role.");
                    return "EMPLOYEE"; // Default to employee
                }
            }
        }
    }
    
    /**
     * Checks if this is a management position
     * @return true if position involves managing others
     */
    public boolean isManagementPosition() {
        String level = getPositionLevel();
        return TYPE_EXECUTIVE.equals(level) || TYPE_MANAGER.equals(level) || TYPE_SUPERVISOR.equals(level);
    }
    
    /**
     * Checks if this is an executive position
     * @return true if position is executive level
     */
    public boolean isExecutivePosition() {
        return TYPE_EXECUTIVE.equals(getPositionLevel());
    }
    
    /**
     * Gets the position display name with department
     * @return Formatted position name with department
     */
    public String getDisplayName() {
        StringBuilder display = new StringBuilder();
        
        if (position != null) {
            display.append(position);
        }
        
        if (department != null && !department.trim().isEmpty()) {
            display.append(" (").append(department).append(")");
        }
        
        return display.toString();
    }
    
    /**
     * Gets the full position information including payroll details
     * @return Full position details
     */
    public String getFullPositionInfo() {
        StringBuilder info = new StringBuilder();
        
        if (position != null) {
            info.append("Position: ").append(position).append("\n");
        }
        
        if (department != null) {
            info.append("Department: ").append(department).append("\n");
        }
        
        info.append("Level: ").append(getPositionLevel()).append("\n");
        info.append("Payroll Category: ").append(getPayrollCategory()).append("\n");
        info.append("Salary Calculation: ").append(getSalaryCalculationMethod()).append("\n");
        info.append("Overtime Eligible: ").append(isEligibleForOvertime() ? "Yes" : "No").append("\n");
        info.append("Late Deductions: ").append(isSubjectToLateDeductions() ? "Yes" : "No").append("\n");
        
        if (positionDescription != null && !positionDescription.trim().isEmpty()) {
            info.append("Description: ").append(positionDescription);
        }
        
        return info.toString();
    }
    
    /**
     * Checks if this position belongs to a specific department
     * @param departmentName Department to check
     * @return true if position belongs to the department
     */
    public boolean belongsToDepartment(String departmentName) {
        if (department == null || departmentName == null) {
            return false;
        }
        
        return department.equalsIgnoreCase(departmentName);
    }
    
    /**
     * Automatically assigns department based on position title
     * Call this method when creating or updating position
     */
    public void assignDepartmentByPosition() {
        this.department = getDepartmentForPosition(this.position);
    }
    
    /**
     * Gets the correct department for a specific position title
     * Rank-and-file positions are assigned to their actual departments
     * @param positionTitle The position title
     * @return Department name for the position
     */
    public static String getDepartmentForPosition(String positionTitle) {
        if (positionTitle == null) {
            return DEPT_OTHER;
        }
        
        String position = positionTitle.trim();
        
        // Leadership positions
        if (position.equals("Chief Executive Officer") || 
            position.equals("Chief Operating Officer") || 
            position.equals("Chief Finance Officer") || 
            position.equals("Chief Marketing Officer")) {
            return DEPT_LEADERSHIP;
        }
        // HR Department (including HR rank-and-file)
        else if (position.equals("HR Manager") || 
                 position.equals("HR Team Leader") || 
                 position.equals("HR Rank and File")) {
            return DEPT_HR;
        }
        // IT Department
        else if (position.equals("IT Operations and Systems") ||
                 position.toLowerCase().contains("it ")) {
            return DEPT_IT;
        }
        // Accounting Department (including Payroll rank-and-file)
        else if (position.equals("Accounting Head") || 
                 position.equals("Payroll Manager") || 
                 position.equals("Payroll Team Leader") || 
                 position.equals("Payroll Rank and File")) {
            return DEPT_ACCOUNTING;
        }
        // Accounts Department (including Account rank-and-file)
        else if (position.equals("Account Manager") || 
                 position.equals("Account Team Leader") || 
                 position.equals("Account Rank and File")) {
            return DEPT_ACCOUNTS;
        }
        // Sales and Marketing Department
        else if (position.equals("Sales & Marketing")) {
            return DEPT_SALES_MARKETING;
        }
        // Supply Chain and Logistics Department
        else if (position.equals("Supply Chain and Logistics")) {
            return DEPT_SUPPLY_CHAIN;
        }
        // Customer Service Department
        else if (position.equals("Customer Service and Relations")) {
            return DEPT_CUSTOMER_SERVICE;
        }
        // If no specific match is found
        return DEPT_OTHER;
    }
    
    /**
     * Gets actual positions available in MPH by department
     * @param departmentName Department name
     * @return List of actual positions for the department
     */
    public static List<String> getActualPositionsForDepartment(String departmentName) {
        List<String> positions = new ArrayList<>();
        
        if (departmentName == null) {
            return positions;
        }
        
        switch (departmentName.toUpperCase()) {
            case "HUMAN RESOURCES", "HR" -> {
                positions.add("HR Manager");
                positions.add("HR Team Leader");
                positions.add("HR Rank and File");
            }
            case "INFORMATION TECHNOLOGY", "IT" -> positions.add("IT Operations and Systems");
                
            case "ACCOUNTING", "FINANCE" -> {
                positions.add("Accounting Head");
                positions.add("Payroll Manager");
                positions.add("Payroll Team Leader");
                positions.add("Payroll Rank and File");
            }
            case "ACCOUNT MANAGEMENT", "ACCOUNTS" -> {
                positions.add("Account Manager");
                positions.add("Account Team Leader");
                positions.add("Account Rank and File");
            }
                
            case "SALES & MARKETING", "SALES" -> positions.add("Sales & Marketing");
            case "OPERATIONS" -> positions.add("Supply Chain and Logistics");
                
            case "CUSTOMER SERVICE" -> positions.add("Customer Service and Relations");
            case "EXECUTIVE" -> {
                positions.add("Chief Executive Officer");
                positions.add("Chief Operating Officer");
                positions.add("Chief Finance Officer");
                positions.add("Chief Marketing Officer");
            }
                
            default -> // Return all positions if department not specified
                positions.addAll(getAllActualPositions());
        }
        
        return positions;
    }
    
    /**
     * Gets all rank-and-file positions
     * @return List of rank-and-file positions
     */
    public static List<String> getRankAndFilePositions() {
        List<String> positions = new ArrayList<>();
        
        // Add specific rank-and-file positions
        positions.add("HR Rank and File");
        positions.add("Payroll Rank and File");
        positions.add("Account Rank and File");
        
        return positions;
    }
    
    /**
     * Gets all non rank-and-file positions
     * @return List of non rank-and-file positions
     */
    public static List<String> getNonRankAndFilePositions() {
        List<String> allPositions = getAllActualPositions();
        List<String> rankAndFilePositions = getRankAndFilePositions();
        
        // Remove rank-and-file positions from all positions
        allPositions.removeAll(rankAndFilePositions);
        
        return allPositions;
    }
    
    /**
     * Creates a position hierarchy key for sorting
     * @return Hierarchy key for organizational sorting
     */
    public String getHierarchyKey() {
        String level = getPositionLevel();
        String dept = getDepartment(); // Use getDepartment() which auto-assigns if needed
        
        // Create sorting key: Level priority + Department + Position
        String levelPriority;
        levelPriority = switch (level) {
            case TYPE_EXECUTIVE -> "1";
            case TYPE_MANAGER -> "2";
            case TYPE_SUPERVISOR -> "3";
            case TYPE_SPECIALIST -> "4";
            case TYPE_ASSOCIATE -> "5";
            case TYPE_RANK_AND_FILE -> "6";
            default -> "7";
        };
        
        return levelPriority + "_" + dept + "_" + (position != null ? position : "");
    }
    
    /**
     * Gets all actual positions available in MPH
     * @return Complete list of all position titles
     */
    public static List<String> getAllActualPositions() {
        List<String> allPositions = new ArrayList<>();
        
        // Executive positions
        allPositions.add("Chief Executive Officer");
        allPositions.add("Chief Operating Officer");
        allPositions.add("Chief Finance Officer");
        allPositions.add("Chief Marketing Officer");
        
        // IT position
        allPositions.add("IT Operations and Systems");
        
        // HR positions
        allPositions.add("HR Manager");
        allPositions.add("HR Team Leader");
        allPositions.add("HR Rank and File");
        
        // Accounting positions
        allPositions.add("Accounting Head");
        allPositions.add("Payroll Manager");
        allPositions.add("Payroll Team Leader");
        allPositions.add("Payroll Rank and File");
        
        // Account management positions
        allPositions.add("Account Manager");
        allPositions.add("Account Team Leader");
        allPositions.add("Account Rank and File");
        
        // Other department positions
        allPositions.add("Sales & Marketing");
        allPositions.add("Supply Chain and Logistics");
        allPositions.add("Customer Service and Relations");
        
        return allPositions;
    }
    
    /**
     * Gets all actual departments in MPH based on positions
     * @return List of department names
     */
    public static List<String> getAllDepartments() {
        List<String> departments = new ArrayList<>();
        departments.add(DEPT_LEADERSHIP);
        departments.add(DEPT_HR);
        departments.add(DEPT_IT);
        departments.add(DEPT_ACCOUNTING);
        departments.add(DEPT_ACCOUNTS);
        departments.add(DEPT_SALES_MARKETING);
        departments.add(DEPT_SUPPLY_CHAIN);
        departments.add(DEPT_CUSTOMER_SERVICE);
        departments.add(DEPT_OTHER);
        return departments;
    }
    
    /**
     * Gets all positions for a specific department
     * @param departmentName Department name
     * @return List of positions in that department
     */
    public static List<String> getPositionsInDepartment(String departmentName) {
        List<String> positions = new ArrayList<>();
        
        if (departmentName == null) {
            return positions;
        }
        
        // Get all actual positions and filter by department
        List<String> allPositions = getAllActualPositions();
        for (String position : allPositions) {
            if (departmentName.equals(getDepartmentForPosition(position))) {
                positions.add(position);
            }
        }
        
        return positions;
    }
    
    /**
     * Validates if department name exists in MPH
     * @param departmentName Department name to validate
     * @return true if department exists
     */
    public static boolean isValidDepartment(String departmentName) {
        return departmentName != null && getAllDepartments().contains(departmentName);
    }
    
    /**
     * Gets complete position-department mapping for reference
     * @return Map of position title to department
     */
    public static Map<String, String> getCompletePositionDepartmentMapping() {
        Map<String, String> mapping = new HashMap<>();
        
        for (String position : getAllActualPositions()) {
            mapping.put(position, getDepartmentForPosition(position));
        }
        
        return mapping;
    }
    
    /**
     * Gets positions by payroll category
     * @param isRankAndFile true for rank-and-file positions, false for non rank-and-file
     * @return List of positions in the category
     */
    public static List<String> getPositionsByPayrollCategory(boolean isRankAndFile) {
        List<String> positions = new ArrayList<>();
        
        for (String position : getAllActualPositions()) {
            if (isRankAndFilePosition(position) == isRankAndFile) {
                positions.add(position);
            }
        }
        
        return positions;
    }
    
    /**
     * Gets payroll statistics - count of positions by category
     * @return Map with "Rank-and-File" and "Non Rank-and-File" counts
     */
    public static Map<String, Integer> getPayrollCategoryStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        
        List<String> rankAndFilePositions = getPositionsByPayrollCategory(true);
        List<String> nonRankAndFilePositions = getPositionsByPayrollCategory(false);
        
        stats.put("Rank-and-File", rankAndFilePositions.size());
        stats.put("Non Rank-and-File", nonRankAndFilePositions.size());
        
        return stats;
    }
    
    /**
     * Checks if this position's current department matches the expected department
     * @return true if department is correctly assigned
     */
    public boolean hasDepartmentCorrectlyAssigned() {
        if (position == null) {
            return true; // No position to validate
        }
        
        String expectedDepartment = getDepartmentForPosition(position);
        return expectedDepartment.equals(this.department);
    }
    
    /**
     * Fixes department assignment if it's incorrect
     * @return true if department was corrected, false if it was already correct
     */
    public boolean correctDepartmentAssignment() {
        String expectedDepartment = getDepartmentForPosition(position);
        if (!expectedDepartment.equals(this.department)) {
            this.department = expectedDepartment;
            return true;
        }
        return false;
    }
    
    /**
     * Gets department statistics - count of positions per department
     * @return Map of department name to position count
     */
    public static Map<String, Integer> getDepartmentStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        
        // Initialize all departments with 0
        for (String dept : getAllDepartments()) {
            stats.put(dept, 0);
        }
        
        // Count positions per department
        for (String position : getAllActualPositions()) {
            String department = getDepartmentForPosition(position);
            stats.put(department, stats.getOrDefault(department, 0) + 1);
        }
        
        return stats;
    }
    
    /**
     * Validates if position title exists in MPH
     * @param positionTitle Position title to validate
     * @return true if position exists in MPH
     */
    public static boolean isValidPositionTitle(String positionTitle) {
        if (positionTitle == null) {
            return false;
        }
        
        return getAllActualPositions().contains(positionTitle.trim());
    }
    
    /**
     * Gets positions by user role for testing purposes
     * @param userRole User role (HR, IT, ACCOUNTING, etc.)
     * @return List of positions that map to this user role
     */
    public static List<String> getPositionsByUserRole(String userRole) {
        List<String> positions = new ArrayList<>();
        
        if (userRole == null) {
            return positions;
        }
        
        switch (userRole.toUpperCase()) {
            case "IMMEDIATESUPERVISOR" -> {
                positions.add("Chief Executive Officer");
                positions.add("Chief Operating Officer");
                positions.add("Chief Finance Officer");
                positions.add("Chief Marketing Officer");
                positions.add("Account Manager");
                positions.add("Account Team Leader");
            }
                
            case "IT" -> positions.add("IT Operations and Systems");
                
            case "HR" -> {
                positions.add("HR Manager");
                positions.add("HR Team Leader");
                positions.add("HR Rank and File");
            }
                
            case "ACCOUNTING" -> {
                positions.add("Accounting Head");
                positions.add("Payroll Manager");
                positions.add("Payroll Team Leader");
                positions.add("Payroll Rank and File");
            }
                
            case "EMPLOYEE" -> {
                positions.add("Account Rank and File");
                positions.add("Sales & Marketing");
                positions.add("Supply Chain and Logistics");
                positions.add("Customer Service and Relations");
            }
        }
        
        return positions;
    }
    

    // UTILITY METHODS

    
    @Override
    public String toString() {
        return "PositionModel{" +
                "positionId=" + positionId +
                ", position='" + position + '\'' +
                ", department='" + getDepartment() + '\'' +
                ", level='" + getPositionLevel() + '\'' +
                ", payrollCategory='" + getPayrollCategory() + '\'' +
                ", userRole='" + (position != null ? determineUserRole() : "N/A") + '\'' +
                ", overtimeEligible=" + isEligibleForOvertime() +
                ", lateDeductions=" + isSubjectToLateDeductions() +
                ", departmentCorrect=" + hasDepartmentCorrectlyAssigned() +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        PositionModel that = (PositionModel) obj;
        return Objects.equals(positionId, that.positionId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(positionId);
    }
    
    /**
     * Returns a formatted display string for this position
     * @return Human-readable string with position details
     */
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        
        if (position != null) {
            sb.append(position);
        }
        
        if (getDepartment() != null && !getDepartment().trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(getDepartment());
        }
        
        sb.append(" [").append(getPositionLevel()).append("]");
        sb.append(" (").append(getPayrollCategory()).append(")");
        
        if (positionDescription != null && !positionDescription.trim().isEmpty()) {
            sb.append("\nDescription: ").append(positionDescription);
        }
        
        return sb.toString();
    }
    
}