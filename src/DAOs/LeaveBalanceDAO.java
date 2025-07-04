package DAOs;

import Models.LeaveBalance;
import java.sql.*;
import java.time.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * LeaveBalanceDAO with balance calculation and conflict resolution
 * Enhanced for timezone-aware leave balance management
 */
public class LeaveBalanceDAO {
    private static final ZoneId MANILA_TIMEZONE = ZoneId.of("Asia/Manila");
    private static final String DB_URL = "jdbc:mysql://localhost:3306/payrollsystem_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    // SQL Queries
    private static final String INSERT_LEAVE_BALANCE = 
        "INSERT INTO leavebalance (employeeId, leaveTypeId, totalLeaveDays, usedLeaveDays, " +
        "remainingLeaveDays, carryOverDays, balanceYear, lastUpdated) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_LEAVE_BALANCE = 
        "UPDATE leavebalance SET totalLeaveDays = ?, usedLeaveDays = ?, remainingLeaveDays = ?, " +
        "carryOverDays = ?, lastUpdated = ? WHERE leaveBalanceId = ?";
    
    private static final String SELECT_BY_ID = 
        "SELECT leaveBalanceId, employeeId, leaveTypeId, totalLeaveDays, usedLeaveDays, " +
        "remainingLeaveDays, carryOverDays, balanceYear, lastUpdated FROM leavebalance " +
        "WHERE leaveBalanceId = ?";
    
    private static final String SELECT_BY_EMPLOYEE_TYPE_YEAR = 
        "SELECT leaveBalanceId, employeeId, leaveTypeId, totalLeaveDays, usedLeaveDays, " +
        "remainingLeaveDays, carryOverDays, balanceYear, lastUpdated FROM leavebalance " +
        "WHERE employeeId = ? AND leaveTypeId = ? AND balanceYear = ?";
    
    private static final String SELECT_BY_EMPLOYEE_YEAR = 
        "SELECT leaveBalanceId, employeeId, leaveTypeId, totalLeaveDays, usedLeaveDays, " +
        "remainingLeaveDays, carryOverDays, balanceYear, lastUpdated FROM leavebalance " +
        "WHERE employeeId = ? AND balanceYear = ? ORDER BY leaveTypeId";
    
    private static final String SELECT_BY_EMPLOYEE = 
        "SELECT leaveBalanceId, employeeId, leaveTypeId, totalLeaveDays, usedLeaveDays, " +
        "remainingLeaveDays, carryOverDays, balanceYear, lastUpdated FROM leavebalance " +
        "WHERE employeeId = ? ORDER BY balanceYear DESC, leaveTypeId";
    
    private static final String DELETE_LEAVE_BALANCE = 
        "DELETE FROM leavebalance WHERE leaveBalanceId = ?";
    
    private static final String SELECT_CONFLICTING_BALANCES = 
        "SELECT leaveBalanceId, employeeId, leaveTypeId, totalLeaveDays, usedLeaveDays, " +
        "remainingLeaveDays, carryOverDays, balanceYear, lastUpdated FROM leavebalance " +
        "WHERE employeeId = ? AND leaveTypeId = ? AND balanceYear = ? AND leaveBalanceId != ?";
    
    private static final String SELECT_BALANCE_SUMMARY = 
        "SELECT " +
        "COUNT(*) as totalBalances, " +
        "SUM(totalLeaveDays) as totalAllocated, " +
        "SUM(usedLeaveDays) as totalUsed, " +
        "SUM(remainingLeaveDays) as totalRemaining, " +
        "SUM(carryOverDays) as totalCarryOver " +
        "FROM leavebalance WHERE employeeId = ? AND balanceYear = ?";
    
    private static final String SELECT_EXPIRING_BALANCES = 
        "SELECT leaveBalanceId, employeeId, leaveTypeId, totalLeaveDays, usedLeaveDays, " +
        "remainingLeaveDays, carryOverDays, balanceYear, lastUpdated FROM leavebalance " +
        "WHERE balanceYear = ? AND remainingLeaveDays > 0 ORDER BY employeeId, leaveTypeId";
    
    private static final String SELECT_LOW_BALANCES = 
        "SELECT leaveBalanceId, employeeId, leaveTypeId, totalLeaveDays, usedLeaveDays, " +
        "remainingLeaveDays, carryOverDays, balanceYear, lastUpdated FROM leavebalance " +
        "WHERE balanceYear = ? AND remainingLeaveDays <= ? AND remainingLeaveDays > 0 " +
        "ORDER BY remainingLeaveDays ASC";
    
    private static final String SELECT_DEPARTMENT_UTILIZATION = 
        "SELECT " +
        "p.department, " +
        "COUNT(DISTINCT lb.employeeId) as employeeCount, " +
        "SUM(lb.totalLeaveDays) as totalAllocated, " +
        "SUM(lb.usedLeaveDays) as totalUsed, " +
        "AVG(lb.usedLeaveDays * 100.0 / NULLIF(lb.totalLeaveDays, 0)) as utilizationRate " +
        "FROM leavebalance lb " +
        "INNER JOIN employee e ON lb.employeeId = e.employeeId " +
        "INNER JOIN position p ON e.positionId = p.positionId " +
        "WHERE lb.balanceYear = ? " +
        "GROUP BY p.department " +
        "ORDER BY utilizationRate DESC";
    
    /**
     * Get database connection with Manila timezone
     */
    private Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        
        // Set connection timezone to Manila
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET time_zone = '+08:00'");
        }
        
        return conn;
    }
    
    /**
     * Create new leave balance
     * @param leaveBalance
     * @return 
     */
    public boolean createLeaveBalance(LeaveBalance leaveBalance) {
        if (leaveBalance == null || !leaveBalance.isValidBalance()) {
            return false;
        }
        
        // Check for existing balance to avoid conflicts
        LeaveBalance existing = getLeaveBalance(
            leaveBalance.getEmployeeId(), 
            leaveBalance.getLeaveTypeId(), 
            leaveBalance.getBalanceYear()
        );
        
        if (existing != null) {
            // Resolve conflict by merging
            return resolveAndUpdateBalance(existing, leaveBalance);
        }
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_LEAVE_BALANCE, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, leaveBalance.getEmployeeId());
            stmt.setInt(2, leaveBalance.getLeaveTypeId());
            stmt.setInt(3, leaveBalance.getTotalLeaveDays());
            stmt.setInt(4, leaveBalance.getUsedLeaveDays());
            stmt.setInt(5, leaveBalance.getRemainingLeaveDays());
            stmt.setInt(6, leaveBalance.getCarryOverDays());
            stmt.setInt(7, leaveBalance.getBalanceYear().getValue());
            stmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now(MANILA_TIMEZONE)));
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        leaveBalance.setLeaveBalanceId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating leave balance: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Update leave balance
     * @param leaveBalance
     * @return 
     */
    public boolean updateLeaveBalance(LeaveBalance leaveBalance) {
        if (leaveBalance == null || leaveBalance.getLeaveBalanceId() <= 0) {
            return false;
        }
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_LEAVE_BALANCE)) {
            
            stmt.setInt(1, leaveBalance.getTotalLeaveDays());
            stmt.setInt(2, leaveBalance.getUsedLeaveDays());
            stmt.setInt(3, leaveBalance.getRemainingLeaveDays());
            stmt.setInt(4, leaveBalance.getCarryOverDays());
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now(MANILA_TIMEZONE)));
            stmt.setInt(6, leaveBalance.getLeaveBalanceId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating leave balance: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get leave balance by ID
     * @param leaveBalanceId
     * @return 
     */
    public LeaveBalance getLeaveBalanceById(int leaveBalanceId) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            
            stmt.setInt(1, leaveBalanceId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLeaveBalance(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting leave balance by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get leave balance by employee, type, and year
     * @param employeeId
     * @param leaveTypeId
     * @param balanceYear
     * @return 
     */
    public LeaveBalance getLeaveBalance(int employeeId, int leaveTypeId, Year balanceYear) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_EMPLOYEE_TYPE_YEAR)) {
            
            stmt.setInt(1, employeeId);
            stmt.setInt(2, leaveTypeId);
            stmt.setInt(3, balanceYear.getValue());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLeaveBalance(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting leave balance: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get all leave balances for employee by year
     * @param employeeId
     * @param balanceYear
     * @return 
     */
    public List<LeaveBalance> getLeaveBalancesByEmployee(int employeeId, Year balanceYear) {
        List<LeaveBalance> balances = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_EMPLOYEE_YEAR)) {
            
            stmt.setInt(1, employeeId);
            stmt.setInt(2, balanceYear.getValue());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    balances.add(mapResultSetToLeaveBalance(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting leave balances by employee: " + e.getMessage());
        }
        
        return balances;
    }
    
    /**
     * Get all leave balances for employee (all years)
     * @param employeeId
     * @return 
     */
    public List<LeaveBalance> getAllLeaveBalancesByEmployee(int employeeId) {
        List<LeaveBalance> balances = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_EMPLOYEE)) {
            
            stmt.setInt(1, employeeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    balances.add(mapResultSetToLeaveBalance(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all leave balances by employee: " + e.getMessage());
        }
        
        return balances;
    }
    
    /**
     * Delete leave balance
     * @param leaveBalanceId
     * @return 
     */
    public boolean deleteLeaveBalance(int leaveBalanceId) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_LEAVE_BALANCE)) {
            
            stmt.setInt(1, leaveBalanceId);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting leave balance: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Deduct leave from balance
     * @param employeeId
     * @param leaveTypeId
     * @param daysToDeduct
     * @param balanceYear
     * @return 
     */
    public boolean deductLeaveFromBalance(int employeeId, int leaveTypeId, Year balanceYear, int daysToDeduct) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                LeaveBalance balance = getLeaveBalance(employeeId, leaveTypeId, balanceYear);
                if (balance == null || !balance.canTakeLeave(daysToDeduct)) {
                    conn.rollback();
                    return false;
                }
                
                // Deduct the leave
                boolean success = balance.deductLeave(daysToDeduct);
                if (!success) {
                    conn.rollback();
                    return false;
                }
                
                // Update in database
                boolean updated = updateLeaveBalance(balance);
                if (!updated) {
                    conn.rollback();
                    return false;
                }
                
                conn.commit();
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("Error deducting leave from balance: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Add leave back to balance (for cancelled requests)
     * @param employeeId
     * @param leaveTypeId
     * @param balanceYear
     * @param daysToAdd
     * @return 
     */
    public boolean addLeaveToBalance(int employeeId, int leaveTypeId, Year balanceYear, int daysToAdd) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                LeaveBalance balance = getLeaveBalance(employeeId, leaveTypeId, balanceYear);
                if (balance == null) {
                    conn.rollback();
                    return false;
                }
                
                // Add the leave back
                balance.addLeave(daysToAdd);
                
                // Update in database
                boolean updated = updateLeaveBalance(balance);
                if (!updated) {
                    conn.rollback();
                    return false;
                }
                
                conn.commit();
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("Error adding leave to balance: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Resolve conflicts between leave balances
     * @param existing
     * @param newBalance
     * @return 
     */
    public boolean resolveAndUpdateBalance(LeaveBalance existing, LeaveBalance newBalance) {
        if (existing == null || newBalance == null) {
            return false;
        }
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Check for other conflicting balances
                List<LeaveBalance> conflicts = getConflictingBalances(newBalance);
                
                if (!conflicts.isEmpty()) {
                    // Merge with existing balances
                    LeaveBalance merged = existing;
                    for (LeaveBalance conflict : conflicts) {
                        merged = merged.mergeWith(conflict);
                        deleteLeaveBalance(conflict.getLeaveBalanceId());
                    }
                    
                    // Update the existing balance with merged data
                    merged.resolveConflict(newBalance);
                    boolean updated = updateLeaveBalance(merged);
                    
                    if (updated) {
                        conn.commit();
                        return true;
                    } else {
                        conn.rollback();
                        return false;
                    }
                } else {
                    // Simple conflict resolution
                    existing.resolveConflict(newBalance);
                    boolean updated = updateLeaveBalance(existing);
                    
                    if (updated) {
                        conn.commit();
                        return true;
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("Error resolving balance conflict: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get conflicting leave balances
     */
    private List<LeaveBalance> getConflictingBalances(LeaveBalance balance) {
        List<LeaveBalance> conflicts = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_CONFLICTING_BALANCES)) {
            
            stmt.setInt(1, balance.getEmployeeId());
            stmt.setInt(2, balance.getLeaveTypeId());
            stmt.setInt(3, balance.getBalanceYear().getValue());
            stmt.setInt(4, balance.getLeaveBalanceId());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    conflicts.add(mapResultSetToLeaveBalance(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting conflicting balances: " + e.getMessage());
        }
        
        return conflicts;
    }
    
    /**
     * Get leave balance summary for employee
     * @param employeeId
     * @param balanceYear
     * @return 
     */
    public Map<String, Object> getLeaveBalanceSummary(int employeeId, Year balanceYear) {
        Map<String, Object> summary = new HashMap<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BALANCE_SUMMARY)) {
            
            stmt.setInt(1, employeeId);
            stmt.setInt(2, balanceYear.getValue());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    summary.put("employeeId", employeeId);
                    summary.put("balanceYear", balanceYear.getValue());
                    summary.put("totalBalances", rs.getInt("totalBalances"));
                    summary.put("totalAllocated", rs.getInt("totalAllocated"));
                    summary.put("totalUsed", rs.getInt("totalUsed"));
                    summary.put("totalRemaining", rs.getInt("totalRemaining"));
                    summary.put("totalCarryOver", rs.getInt("totalCarryOver"));
                    
                    int allocated = rs.getInt("totalAllocated");
                    int used = rs.getInt("totalUsed");
                    summary.put("utilizationRate", allocated > 0 ? (double) used / allocated * 100 : 0);
                    summary.put("availableDays", rs.getInt("totalRemaining") + rs.getInt("totalCarryOver"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting leave balance summary: " + e.getMessage());
        }
        
        return summary;
    }
    
    /**
     * Get expiring leave balances for year-end processing
     * @param expiringYear
     * @return 
     */
    public List<LeaveBalance> getExpiringLeaveBalances(Year expiringYear) {
        List<LeaveBalance> expiring = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_EXPIRING_BALANCES)) {
            
            stmt.setInt(1, expiringYear.getValue());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    expiring.add(mapResultSetToLeaveBalance(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting expiring leave balances: " + e.getMessage());
        }
        
        return expiring;
    }
    
    /**
     * Get employees with low leave balances
     * @param balanceYear
     * @param threshold
     * @return 
     */
    public List<LeaveBalance> getLowLeaveBalances(Year balanceYear, int threshold) {
        List<LeaveBalance> lowBalances = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_LOW_BALANCES)) {
            
            stmt.setInt(1, balanceYear.getValue());
            stmt.setInt(2, threshold);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lowBalances.add(mapResultSetToLeaveBalance(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting low leave balances: " + e.getMessage());
        }
        
        return lowBalances;
    }
    
    /**
     * Initialize leave balances for new year
     * @param newYear
     * @param employeeLeaveAllocation
     * @return 
     */
    public boolean initializeNewYearBalances(Year newYear, Map<Integer, Map<Integer, Integer>> employeeLeaveAllocation) {
        if (employeeLeaveAllocation == null || employeeLeaveAllocation.isEmpty()) {
            return false;
        }
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                int successCount = 0;
                int totalCount = 0;
                
                for (Map.Entry<Integer, Map<Integer, Integer>> employeeEntry : employeeLeaveAllocation.entrySet()) {
                    int employeeId = employeeEntry.getKey();
                    Map<Integer, Integer> leaveAllocations = employeeEntry.getValue();
                    
                    for (Map.Entry<Integer, Integer> leaveEntry : leaveAllocations.entrySet()) {
                        int leaveTypeId = leaveEntry.getKey();
                        int allocation = leaveEntry.getValue();
                        
                        // Check if balance already exists
                        LeaveBalance existing = getLeaveBalance(employeeId, leaveTypeId, newYear);
                        if (existing == null) {
                            LeaveBalance newBalance = new LeaveBalance(employeeId, leaveTypeId, allocation, newYear);
                            if (createLeaveBalance(newBalance)) {
                                successCount++;
                            }
                        } else {
                            successCount++; // Already exists, count as success
                        }
                        totalCount++;
                    }
                }
                
                if (successCount == totalCount) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("Error initializing new year balances: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Process year-end carry-over
     * @param fromYear
     * @param toYear
     * @param maxCarryOverDays
     * @return 
     */
    public boolean processYearEndCarryOver(Year fromYear, Year toYear, int maxCarryOverDays) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                List<LeaveBalance> expiringBalances = getExpiringLeaveBalances(fromYear);
                int successCount = 0;
                
                for (LeaveBalance expiring : expiringBalances) {
                    if (expiring.getRemainingLeaveDays() > 0) {
                        // Create next year balance with carry-over
                        LeaveBalance nextYear = expiring.createNextYearBalance(maxCarryOverDays);
                        
                        // Check if next year balance already exists
                        LeaveBalance existing = getLeaveBalance(
                            nextYear.getEmployeeId(), 
                            nextYear.getLeaveTypeId(), 
                            toYear
                        );
                        
                        if (existing != null) {
                            // Update existing balance with carry-over
                            existing.setCarryOverDays(nextYear.getCarryOverDays());
                            existing.calculateRemainingDays();
                            if (updateLeaveBalance(existing)) {
                                successCount++;
                            }
                        } else {
                            // Create new balance
                            if (createLeaveBalance(nextYear)) {
                                successCount++;
                            }
                        }
                    } else {
                        successCount++; // No carry-over needed
                    }
                }
                
                if (successCount == expiringBalances.size()) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("Error processing year-end carry-over: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get department leave utilization report
     * @param balanceYear
     * @return 
     */
    public List<Map<String, Object>> getDepartmentLeaveUtilization(Year balanceYear) {
        List<Map<String, Object>> utilization = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_DEPARTMENT_UTILIZATION)) {
            
            stmt.setInt(1, balanceYear.getValue());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> dept = new HashMap<>();
                    dept.put("department", rs.getString("department"));
                    dept.put("employeeCount", rs.getInt("employeeCount"));
                    dept.put("totalAllocated", rs.getInt("totalAllocated"));
                    dept.put("totalUsed", rs.getInt("totalUsed"));
                    dept.put("utilizationRate", rs.getDouble("utilizationRate"));
                    utilization.add(dept);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting department leave utilization: " + e.getMessage());
        }
        
        return utilization;
    }
    
    /**
     * Bulk update leave balances
     * @param balances
     * @return 
     */
    public boolean bulkUpdateLeaveBalances(List<LeaveBalance> balances) {
        if (balances == null || balances.isEmpty()) {
            return false;
        }
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(UPDATE_LEAVE_BALANCE)) {
                Timestamp updateTime = Timestamp.valueOf(LocalDateTime.now(MANILA_TIMEZONE));
                
                for (LeaveBalance balance : balances) {
                    if (balance.getLeaveBalanceId() <= 0) continue;
                    
                    stmt.setInt(1, balance.getTotalLeaveDays());
                    stmt.setInt(2, balance.getUsedLeaveDays());
                    stmt.setInt(3, balance.getRemainingLeaveDays());
                    stmt.setInt(4, balance.getCarryOverDays());
                    stmt.setTimestamp(5, updateTime);
                    stmt.setInt(6, balance.getLeaveBalanceId());
                    
                    stmt.addBatch();
                }
                
                int[] results = stmt.executeBatch();
                conn.commit();
                
                // Check if all updates were successful
                for (int result : results) {
                    if (result <= 0) {
                        return false;
                    }
                }
                
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("Error bulk updating leave balances: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Reset all leave balances for new year
     * @param newYear
     * @return 
     */
    public boolean resetLeaveBalancesForNewYear(Year newYear) {
        String resetQuery = "UPDATE leavebalance SET usedLeaveDays = 0, remainingLeaveDays = totalLeaveDays + carryOverDays, " +
                           "lastUpdated = ? WHERE balanceYear = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(resetQuery)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now(MANILA_TIMEZONE)));
            stmt.setInt(2, newYear.getValue());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error resetting leave balances for new year: " + e.getMessage());
        }
        
        return false;
    }
    
    // Helper methods
    
    /**
     * Map ResultSet to LeaveBalance
     */
    private LeaveBalance mapResultSetToLeaveBalance(ResultSet rs) throws SQLException {
        LeaveBalance balance = new LeaveBalance();
        
        balance.setLeaveBalanceId(rs.getInt("leaveBalanceId"));
        balance.setEmployeeId(rs.getInt("employeeId"));
        balance.setLeaveTypeId(rs.getInt("leaveTypeId"));
        balance.setTotalLeaveDays(rs.getInt("totalLeaveDays"));
        balance.setUsedLeaveDays(rs.getInt("usedLeaveDays"));
        balance.setCarryOverDays(rs.getInt("carryOverDays"));
        balance.setBalanceYear(Year.of(rs.getInt("balanceYear")));
        balance.setLastUpdated(rs.getTimestamp("lastUpdated"));
        
        // Calculate remaining days
        balance.calculateRemainingDays();
        
        return balance;
    }
    
    /**
     * Get current Manila time
     * @return 
     */
    public LocalDateTime getCurrentManilaTime() {
        return LocalDateTime.now(MANILA_TIMEZONE);
    }
    
    /**
     * Check if employee has sufficient leave balance
     * @param employeeId
     * @param leaveTypeId
     * @param balanceYear
     * @param requestedDays
     * @return 
     */
    public boolean hasSufficientLeaveBalance(int employeeId, int leaveTypeId, Year balanceYear, int requestedDays) {
        LeaveBalance balance = getLeaveBalance(employeeId, leaveTypeId, balanceYear);
        return balance != null && balance.canTakeLeave(requestedDays);
    }
    
    /**
     * Get leave balance utilization rate
     * @param employeeId
     * @param leaveTypeId
     * @param balanceYear
     * @return 
     */
    public double getLeaveBalanceUtilizationRate(int employeeId, int leaveTypeId, Year balanceYear) {
        LeaveBalance balance = getLeaveBalance(employeeId, leaveTypeId, balanceYear);
        return balance != null ? balance.getUtilizationRate() : 0.0;
    }
}