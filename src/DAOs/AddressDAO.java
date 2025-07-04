package DAOs;

import Models.AddressModel;
import java.sql.*;
import java.util.*;

/**
 * AddressDAO - Data Access Object for AddressModel
 * Enhanced with Manila-specific address operations and Philippine location support
 * @author User
 */
public class AddressDAO {
    
    public AddressDAO() {
    }
    
    // BASIC CRUD OPERATIONS
    
    /**
     * Create - Insert new address into database
     * @param address
     * @return 
     */
    public boolean save(AddressModel address) {
        String sql = "INSERT INTO address (street, barangay, city, province, zipCode) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, address.getStreet());
            pstmt.setString(2, address.getBarangay());
            pstmt.setString(3, address.getCity());
            pstmt.setString(4, address.getProvince());
            pstmt.setString(5, address.getZipCode());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // Get the generated address ID
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        address.setAddressId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error saving address: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Read - Find address by ID
     * @param addressId
     * @return 
     */
    public AddressModel findById(int addressId) {
        String sql = "SELECT * FROM address WHERE addressId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, addressId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToAddress(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding address: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Read - Get all addresses
     * @return 
     */
    public List<AddressModel> findAll() {
        List<AddressModel> addresses = new ArrayList<>();
        String sql = "SELECT * FROM address ORDER BY city, province";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                addresses.add(mapResultSetToAddress(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving addresses: " + e.getMessage());
        }
        return addresses;
    }
    
    /**
     * Update - Update existing address
     * @param address
     * @return 
     */
    public boolean update(AddressModel address) {
        if (address.getAddressId() == null || address.getAddressId() <= 0) {
            System.err.println("Cannot update address: Invalid address ID");
            return false;
        }
        
        String sql = "UPDATE address SET street = ?, barangay = ?, city = ?, province = ?, zipCode = ? WHERE addressId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, address.getStreet());
            pstmt.setString(2, address.getBarangay());
            pstmt.setString(3, address.getCity());
            pstmt.setString(4, address.getProvince());
            pstmt.setString(5, address.getZipCode());
            pstmt.setInt(6, address.getAddressId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating address: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Delete - Remove address from database
     * @param address
     * @return 
     */
    public boolean delete(AddressModel address) {
        return deleteById(address.getAddressId());
    }
    
    /**
     * Delete - Remove address by ID
     * @param addressId
     * @return 
     */
    public boolean deleteById(int addressId) {
        String sql = "DELETE FROM address WHERE addressId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, addressId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting address: " + e.getMessage());
        }
        return false;
    }
    
    // MANILA-SPECIFIC ADDRESS OPERATIONS
    
    /**
     * Find all addresses in Manila area (Metro Manila)
     * @return List of Manila area addresses
     */
    public List<AddressModel> findManilaAreaAddresses() {
        List<AddressModel> addresses = new ArrayList<>();
        String sql = """
            SELECT * FROM address 
            WHERE LOWER(province) LIKE '%metro manila%' 
               OR LOWER(province) LIKE '%ncr%' 
               OR LOWER(province) LIKE '%national capital region%'
               OR LOWER(city) IN ('manila', 'quezon city', 'makati', 'taguig', 'pasig', 
                                  'mandaluyong', 'san juan', 'muntinlupa', 'paranaque', 
                                  'las pinas', 'caloocan', 'malabon', 'navotas', 
                                  'valenzuela', 'marikina', 'pasay', 'pateros')
            ORDER BY city, street
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                addresses.add(mapResultSetToAddress(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding Manila area addresses: " + e.getMessage());
        }
        return addresses;
    }
    
    /**
     * Find all addresses in Taguig (user's location)
     * @return List of Taguig addresses
     */
    public List<AddressModel> findTaguigAddresses() {
        List<AddressModel> addresses = new ArrayList<>();
        String sql = "SELECT * FROM address WHERE LOWER(city) LIKE '%taguig%' ORDER BY street";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                addresses.add(mapResultSetToAddress(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding Taguig addresses: " + e.getMessage());
        }
        return addresses;
    }
    
    /**
     * Find addresses outside Manila area
     * @return List of non-Manila addresses
     */
    public List<AddressModel> findNonManilaAddresses() {
        List<AddressModel> addresses = new ArrayList<>();
        String sql = """
            SELECT * FROM address 
            WHERE NOT (LOWER(province) LIKE '%metro manila%' 
                      OR LOWER(province) LIKE '%ncr%' 
                      OR LOWER(province) LIKE '%national capital region%'
                      OR LOWER(city) IN ('manila', 'quezon city', 'makati', 'taguig', 'pasig', 
                                         'mandaluyong', 'san juan', 'muntinlupa', 'paranaque', 
                                         'las pinas', 'caloocan', 'malabon', 'navotas', 
                                         'valenzuela', 'marikina', 'pasay', 'pateros'))
            ORDER BY province, city, street
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                addresses.add(mapResultSetToAddress(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding non-Manila addresses: " + e.getMessage());
        }
        return addresses;
    }
    
    /**
     * Find addresses with valid Manila ZIP codes (1000-1799)
     * @return List of addresses with Manila ZIP codes
     */
    public List<AddressModel> findAddressesWithManilaZipCodes() {
        List<AddressModel> addresses = new ArrayList<>();
        String sql = "SELECT * FROM address WHERE zipCode REGEXP '^1[0-7][0-9]{2}$' ORDER BY zipCode, city";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                addresses.add(mapResultSetToAddress(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding addresses with Manila ZIP codes: " + e.getMessage());
        }
        return addresses;
    }
    
    /**
     * Get address statistics by location category
     * @return Map with address counts by location
     */
    public Map<String, Integer> getAddressLocationStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        
        // Count Manila area addresses
        String manilaSQL = """
            SELECT COUNT(*) FROM address 
            WHERE LOWER(province) LIKE '%metro manila%' 
               OR LOWER(province) LIKE '%ncr%' 
               OR LOWER(province) LIKE '%national capital region%'
               OR LOWER(city) IN ('manila', 'quezon city', 'makati', 'taguig', 'pasig', 
                                  'mandaluyong', 'san juan', 'muntinlupa', 'paranaque', 
                                  'las pinas', 'caloocan', 'malabon', 'navotas', 
                                  'valenzuela', 'marikina', 'pasay', 'pateros')
            """;
        
        // Count Taguig addresses specifically
        String taguigSQL = "SELECT COUNT(*) FROM address WHERE LOWER(city) LIKE '%taguig%'";
        
        // Count total addresses
        String totalSQL = "SELECT COUNT(*) FROM address";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Manila area count
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(manilaSQL)) {
                if (rs.next()) {
                    stats.put("manilaArea", rs.getInt(1));
                }
            }
            
            // Taguig count
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(taguigSQL)) {
                if (rs.next()) {
                    stats.put("taguig", rs.getInt(1));
                }
            }
            
            // Total count
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(totalSQL)) {
                if (rs.next()) {
                    int total = rs.getInt(1);
                    stats.put("total", total);
                    stats.put("nonManilaArea", total - stats.getOrDefault("manilaArea", 0));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting address location statistics: " + e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * Auto-correct Manila area addresses (fix province field)
     * @return Number of addresses corrected
     */
    public int autoCorrectManilaProvinces() {
        String sql = """
            UPDATE address 
            SET province = 'Metro Manila' 
            WHERE LOWER(city) IN ('manila', 'quezon city', 'makati', 'taguig', 'pasig', 
                                  'mandaluyong', 'san juan', 'muntinlupa', 'paranaque', 
                                  'las pinas', 'caloocan', 'malabon', 'navotas', 
                                  'valenzuela', 'marikina', 'pasay', 'pateros')
            AND NOT (LOWER(province) LIKE '%metro manila%' 
                    OR LOWER(province) LIKE '%ncr%' 
                    OR LOWER(province) LIKE '%national capital region%')
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            return stmt.executeUpdate(sql);
            
        } catch (SQLException e) {
            System.err.println("Error auto-correcting Manila provinces: " + e.getMessage());
            return 0;
        }
    }
    
    // ENHANCED SEARCH AND FILTER METHODS
    
    /**
     * Find addresses by city with distance category
     * @param city
     * @return 
     */
    public List<AddressModel> findByCity(String city) {
        List<AddressModel> addresses = new ArrayList<>();
        String sql = "SELECT * FROM address WHERE LOWER(city) = LOWER(?) ORDER BY street";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, city);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                addresses.add(mapResultSetToAddress(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding addresses by city: " + e.getMessage());
        }
        return addresses;
    }
    
    /**
     * Find addresses by province with Manila classification
     * @param province
     * @return 
     */
    public List<AddressModel> findByProvince(String province) {
        List<AddressModel> addresses = new ArrayList<>();
        String sql = "SELECT * FROM address WHERE LOWER(province) = LOWER(?) ORDER BY city, street";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, province);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                addresses.add(mapResultSetToAddress(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding addresses by province: " + e.getMessage());
        }
        return addresses;
    }
    
    /**
     * Find addresses by ZIP code range
     * @param startZip Starting ZIP code
     * @param endZip Ending ZIP code
     * @return List of addresses in ZIP code range
     */
    public List<AddressModel> findByZipCodeRange(String startZip, String endZip) {
        List<AddressModel> addresses = new ArrayList<>();
        String sql = "SELECT * FROM address WHERE zipCode BETWEEN ? AND ? ORDER BY zipCode, city";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, startZip);
            pstmt.setString(2, endZip);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                addresses.add(mapResultSetToAddress(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding addresses by ZIP code range: " + e.getMessage());
        }
        return addresses;
    }
    
    /**
     * Search addresses by partial text with location priority
     * Manila area addresses appear first
     * @param searchText
     * @return 
     */
    public List<AddressModel> searchAddressesWithLocationPriority(String searchText) {
        List<AddressModel> addresses = new ArrayList<>();
        String sql = """
            SELECT *, 
                   CASE WHEN (LOWER(province) LIKE '%metro manila%' 
                             OR LOWER(province) LIKE '%ncr%' 
                             OR LOWER(province) LIKE '%national capital region%'
                             OR LOWER(city) IN ('manila', 'quezon city', 'makati', 'taguig', 'pasig', 
                                                'mandaluyong', 'san juan', 'muntinlupa', 'paranaque', 
                                                'las pinas', 'caloocan', 'malabon', 'navotas', 
                                                'valenzuela', 'marikina', 'pasay', 'pateros'))
                        THEN 1 ELSE 2 END as location_priority,
                   CASE WHEN LOWER(city) LIKE '%taguig%' THEN 0 ELSE 1 END as taguig_priority
            FROM address 
            WHERE street LIKE ? OR barangay LIKE ? OR city LIKE ? OR province LIKE ? OR zipCode LIKE ?
            ORDER BY taguig_priority, location_priority, city, street
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + searchText + "%";
            for (int i = 1; i <= 5; i++) {
                pstmt.setString(i, searchPattern);
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                addresses.add(mapResultSetToAddress(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching addresses with location priority: " + e.getMessage());
        }
        return addresses;
    }
    
    /**
     * Standard search addresses method
     * @param searchText
     * @return 
     */
    public List<AddressModel> searchAddresses(String searchText) {
        List<AddressModel> addresses = new ArrayList<>();
        String sql = """
            SELECT * FROM address WHERE 
            street LIKE ? OR barangay LIKE ? OR city LIKE ? OR province LIKE ? OR zipCode LIKE ? 
            ORDER BY city, street
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + searchText + "%";
            for (int i = 1; i <= 5; i++) {
                pstmt.setString(i, searchPattern);
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                addresses.add(mapResultSetToAddress(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching addresses: " + e.getMessage());
        }
        return addresses;
    }
    
    // PHILIPPINE LOCATION REFERENCE DATA
    
    /**
     * Gets unique Manila area cities from database
     * @return List of Manila cities found in database
     */
    public List<String> getManilaAreaCities() {
        List<String> cities = new ArrayList<>();
        String sql = """
            SELECT DISTINCT city FROM address 
            WHERE LOWER(province) LIKE '%metro manila%' 
               OR LOWER(province) LIKE '%ncr%' 
               OR LOWER(province) LIKE '%national capital region%'
               OR LOWER(city) IN ('manila', 'quezon city', 'makati', 'taguig', 'pasig', 
                                  'mandaluyong', 'san juan', 'muntinlupa', 'paranaque', 
                                  'las pinas', 'caloocan', 'malabon', 'navotas', 
                                  'valenzuela', 'marikina', 'pasay', 'pateros')
            AND city IS NOT NULL 
            ORDER BY city
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                cities.add(rs.getString("city"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting Manila area cities: " + e.getMessage());
        }
        return cities;
    }
    
    /**
     * Get unique cities
     * @return 
     */
    public List<String> getUniqueCities() {
        List<String> cities = new ArrayList<>();
        String sql = "SELECT DISTINCT city FROM address WHERE city IS NOT NULL ORDER BY city";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                cities.add(rs.getString("city"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting unique cities: " + e.getMessage());
        }
        return cities;
    }
    
    /**
     * Get unique provinces with Manila area indicator
     * @return List of province maps with Manila area flag
     */
    public List<Map<String, Object>> getUniqueProvincesWithClassification() {
        List<Map<String, Object>> provinces = new ArrayList<>();
        String sql = """
            SELECT DISTINCT province,
                   CASE WHEN (LOWER(province) LIKE '%metro manila%' 
                             OR LOWER(province) LIKE '%ncr%' 
                             OR LOWER(province) LIKE '%national capital region%')
                        THEN true ELSE false END as is_manila_area,
                   COUNT(*) as address_count
            FROM address 
            WHERE province IS NOT NULL 
            GROUP BY province
            ORDER BY is_manila_area DESC, province
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Map<String, Object> provinceInfo = new HashMap<>();
                provinceInfo.put("province", rs.getString("province"));
                provinceInfo.put("is_manila_area", rs.getBoolean("is_manila_area"));
                provinceInfo.put("address_count", rs.getInt("address_count"));
                provinces.add(provinceInfo);
            }
        } catch (SQLException e) {
            System.err.println("Error getting unique provinces with classification: " + e.getMessage());
        }
        return provinces;
    }
    
    /**
     * Get unique provinces (simple list)
     * @return 
     */
    public List<String> getUniqueProvinces() {
        List<String> provinces = new ArrayList<>();
        String sql = "SELECT DISTINCT province FROM address WHERE province IS NOT NULL ORDER BY province";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                provinces.add(rs.getString("province"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting unique provinces: " + e.getMessage());
        }
        return provinces;
    }
    
    /**
     * Get ZIP code ranges used in database
     * @return Map with ZIP code range statistics
     */
    public Map<String, Object> getZipCodeRangeStatistics() {
        Map<String, Object> stats = new HashMap<>();
        String sql = """
            SELECT 
                MIN(zipCode) as min_zip,
                MAX(zipCode) as max_zip,
                COUNT(DISTINCT zipCode) as unique_zip_count,
                COUNT(*) as total_addresses_with_zip,
                SUM(CASE WHEN zipCode REGEXP '^1[0-7][0-9]{2}$' THEN 1 ELSE 0 END) as manila_zip_count
            FROM address 
            WHERE zipCode IS NOT NULL AND zipCode != ''
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                stats.put("min_zip", rs.getString("min_zip"));
                stats.put("max_zip", rs.getString("max_zip"));
                stats.put("unique_zip_count", rs.getInt("unique_zip_count"));
                stats.put("total_addresses_with_zip", rs.getInt("total_addresses_with_zip"));
                stats.put("manila_zip_count", rs.getInt("manila_zip_count"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting ZIP code range statistics: " + e.getMessage());
        }
        return stats;
    }
    
    // UTILITY METHODS
    
    /**
     * Check if address exists
     * @param addressId
     * @return 
     */
    public boolean exists(int addressId) {
        String sql = "SELECT COUNT(*) FROM address WHERE addressId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, addressId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking address existence: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Get count of all addresses
     * @return 
     */
    public int getAddressCount() {
        String sql = "SELECT COUNT(*) FROM address";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting address count: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Validate and clean address data
     * @param address Address to validate and clean
     * @return true if address was modified during cleaning
     */
    public boolean validateAndCleanAddress(AddressModel address) {
        boolean modified = false;
        
        if (address == null) return false;
        
        // Auto-correct Manila province if needed
        if (address.autoCorrectManilaProvince()) {
            modified = true;
        }
        
        // Trim whitespace from all fields
        if (address.getStreet() != null) {
            String trimmed = address.getStreet().trim();
            if (!trimmed.equals(address.getStreet())) {
                address.setStreet(trimmed);
                modified = true;
            }
        }
        
        if (address.getBarangay() != null) {
            String trimmed = address.getBarangay().trim();
            if (!trimmed.equals(address.getBarangay())) {
                address.setBarangay(trimmed);
                modified = true;
            }
        }
        
        if (address.getCity() != null) {
            String trimmed = address.getCity().trim();
            if (!trimmed.equals(address.getCity())) {
                address.setCity(trimmed);
                modified = true;
            }
        }
        
        if (address.getProvince() != null) {
            String trimmed = address.getProvince().trim();
            if (!trimmed.equals(address.getProvince())) {
                address.setProvince(trimmed);
                modified = true;
            }
        }
        
        if (address.getZipCode() != null) {
            String trimmed = address.getZipCode().trim();
            if (!trimmed.equals(address.getZipCode())) {
                address.setZipCode(trimmed);
                modified = true;
            }
        }
        
        return modified;
    }
    
    /**
     * Helper method to map ResultSet to AddressModel
     */
    private AddressModel mapResultSetToAddress(ResultSet rs) throws SQLException {
        return new AddressModel(
            rs.getInt("addressId"),
            rs.getString("street"),
            rs.getString("barangay"),
            rs.getString("city"),
            rs.getString("province"),
            rs.getString("zipCode")
        );
    }
}