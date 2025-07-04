package Models;

import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * AddressModel class that maps to the address table
 * Fields: addressId, street, barangay, city, province, zipCode
 * 
 * Updated to support Manila-specific address handling and Philippine address formats
 * 
 * @author User
 */
public class AddressModel {
    
    private Integer addressId;
    private String street;
    private String barangay;
    private String city;
    private String province;
    private String zipCode;
    
    // Philippine address format constants
    public static final String COUNTRY = "Philippines";
    public static final String METRO_MANILA = "Metro Manila";
    public static final String NCR = "National Capital Region";
    
    // Manila-specific cities (for validation)
    public static final List<String> MANILA_CITIES = Arrays.asList(
        "Manila", "Quezon City", "Makati", "Taguig", "Pasig", "Mandaluyong", 
        "San Juan", "Muntinlupa", "Parañaque", "Las Piñas", "Caloocan", 
        "Malabon", "Navotas", "Valenzuela", "Marikina", "Pasay", "Pateros"
    );
    
    // Common Philippine provinces
    public static final List<String> COMMON_PROVINCES = Arrays.asList(
        "Metro Manila", "National Capital Region", "Laguna", "Cavite", "Rizal", 
        "Bulacan", "Pampanga", "Batangas", "Zambales", "Tarlac", "Nueva Ecija",
        "Quezon", "Camarines Norte", "Camarines Sur", "Albay", "Sorsogon", "Masbate",
        "Catanduanes", "Marinduque", "Occidental Mindoro", "Oriental Mindoro", "Palawan",
        "Romblon", "Bataan", "Aurora", "Antique", "Aklan", "Capiz", "Guimaras",
        "Iloilo", "Negros Occidental", "Negros Oriental", "Bohol", "Cebu", "Leyte",
        "Southern Leyte", "Samar", "Eastern Samar", "Northern Samar", "Biliran",
        "Siquijor", "Surigao del Norte", "Surigao del Sur", "Agusan del Norte",
        "Agusan del Sur", "Dinagat Islands", "Bukidnon", "Camiguin", "Lanao del Norte",
        "Misamis Occidental", "Misamis Oriental", "Davao del Norte", "Davao del Sur",
        "Davao Oriental", "Davao de Oro", "Davao Occidental", "Cotabato", "Sarangani",
        "South Cotabato", "Sultan Kudarat", "Maguindanao", "Lanao del Sur", "Sulu",
        "Tawi-Tawi", "Basilan", "Zamboanga del Norte", "Zamboanga del Sur",
        "Zamboanga Sibugay", "Isabela", "Cagayan", "Quirino", "Nueva Vizcaya",
        "Batanes", "Ilocos Norte", "Ilocos Sur", "La Union", "Pangasinan",
        "Abra", "Benguet", "Ifugao", "Kalinga", "Mountain Province", "Apayao"
    );
    
    // Philippine ZIP code pattern (4 digits)
    private static final Pattern ZIP_CODE_PATTERN = Pattern.compile("^\\d{4}$");
    
    // Manila area ZIP codes (1000-1799)
    private static final Pattern MANILA_ZIP_PATTERN = Pattern.compile("^1[0-7]\\d{2}$");
    
    // Street address validation patterns
    private static final Pattern STREET_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s,.-]+$");
    private static final Pattern BARANGAY_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s,.-]+$");
    
    // Constructors
    public AddressModel() {}
    
    public AddressModel(String street, String barangay, String city, String province, String zipCode) {
        this.street = street;
        this.barangay = barangay;
        this.city = city;
        this.province = province;
        this.zipCode = zipCode;
    }
    
    public AddressModel(Integer addressId, String street, String barangay, String city, String province, String zipCode) {
        this.addressId = addressId;
        this.street = street;
        this.barangay = barangay;
        this.city = city;
        this.province = province;
        this.zipCode = zipCode;
    }
    
    // Getters and Setters
    public Integer getAddressId() { return addressId; }
    public void setAddressId(Integer addressId) { this.addressId = addressId; }
    
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    
    public String getBarangay() { return barangay; }
    public void setBarangay(String barangay) { this.barangay = barangay; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    

    // MANILA-SPECIFIC METHODS
    
    /**
     * Checks if this address is in Manila (Metro Manila area)
     * @return true if address is in Manila area
     */
    public boolean isInManilaArea() {
        if (city == null && province == null) {
            return false;
        }
        
        // Check if city is in Manila area
        if (city != null && isValidManilaCity(city)) {
            return true;
        }
        
        // Check if province indicates Manila area
        if (province != null) {
            String lowerProvince = province.toLowerCase();
            return lowerProvince.contains("metro manila") || 
                   lowerProvince.contains("national capital region") || 
                   lowerProvince.contains("ncr");
        }
        
        return false;
    }
    
    /**
     * Checks if the address is in Taguig (user's location)
     * @return true if address is in Taguig
     */
    public boolean isInTaguig() {
        if (city == null) {
            return false;
        }
        
        return city.toLowerCase().contains("taguig");
    }
    
    /**
     * Validates if a city is a valid Manila area city
     * @param cityName City name to validate
     * @return true if city is in Manila area
     */
    public static boolean isValidManilaCity(String cityName) {
        if (cityName == null) {
            return false;
        }
        
        String lowerCity = cityName.toLowerCase();
        return MANILA_CITIES.stream()
                .anyMatch(city -> city.toLowerCase().equals(lowerCity));
    }
    
    /**
     * Auto-corrects province for Manila area addresses
     * @return true if province was corrected
     */
    public boolean autoCorrectManilaProvince() {
        if (city != null && isValidManilaCity(city)) {
            if (province == null || !province.toLowerCase().contains("metro manila")) {
                this.province = METRO_MANILA;
                return true;
            }
        }
        return false;
    }
    
    /**
     * Validates Manila ZIP code format
     * @return true if ZIP code is valid for Manila area
     */
    public boolean hasValidManilaZipCode() {
        if (zipCode == null) {
            return false;
        }
        
        return MANILA_ZIP_PATTERN.matcher(zipCode).matches();
    }
    
    /**
     * Gets the suggested ZIP code for Manila cities
     * @param cityName Manila city name
     * @return Suggested ZIP code range or null if not a Manila city
     */
    public static String getSuggestedZipCodeForManilaCity(String cityName) {
        if (cityName == null) {
            return null;
        }
        
        String lowerCity = cityName.toLowerCase();
        
        // Common Manila area ZIP codes
        return switch (lowerCity) {
            case "manila" -> "1000-1099";
            case "quezon city" -> "1100-1199";
            case "makati" -> "1200-1299";
            case "taguig" -> "1630-1639";
            case "pasig" -> "1600-1609";
            case "mandaluyong" -> "1550-1559";
            case "san juan" -> "1500-1509";
            case "muntinlupa" -> "1770-1779";
            case "parañaque" -> "1700-1709";
            case "las piñas" -> "1740-1749";
            case "caloocan" -> "1400-1429";
            case "malabon" -> "1470-1479";
            case "navotas" -> "1480-1489";
            case "valenzuela" -> "1440-1449";
            case "marikina" -> "1800-1809";
            case "pasay" -> "1300-1309";
            case "pateros" -> "1620-1629";
            default -> "1000-1799 (Manila Area)";
        };
    }
    
    /**
     * Gets the distance category from Manila (rough estimate)
     * @return Distance category string
     */
    public String getDistanceFromManilaCategory() {
        if (isInManilaArea()) {
            return "Within Manila Area";
        }
        
        if (province == null) {
            return "Unknown";
        }
        
        String lowerProvince = province.toLowerCase();
        
        // Adjacent provinces
        if (lowerProvince.contains("rizal") || lowerProvince.contains("cavite") || 
            lowerProvince.contains("laguna") || lowerProvince.contains("bulacan")) {
            return "Adjacent to Manila (30-60 km)";
        }
        
        // Nearby provinces
        if (lowerProvince.contains("batangas") || lowerProvince.contains("pampanga") || 
            lowerProvince.contains("zambales") || lowerProvince.contains("quezon")) {
            return "Nearby Manila (60-150 km)";
        }
        
        // Luzon provinces
        if (lowerProvince.contains("nueva ecija") || lowerProvince.contains("tarlac") || 
            lowerProvince.contains("pangasinan") || lowerProvince.contains("ilocos")) {
            return "Within Luzon (150+ km)";
        }
        
        return "Outside Manila Area";
    }
    

    // PHILIPPINE ADDRESS VALIDATION
 
    /**
     * Validates Philippine ZIP code format
     * @param zipCode ZIP code to validate
     * @return true if valid Philippine ZIP code
     */
    public static boolean isValidPhilippineZipCode(String zipCode) {
        if (zipCode == null) {
            return false;
        }
        
        return ZIP_CODE_PATTERN.matcher(zipCode.trim()).matches();
    }
    
    /**
     * Validates street address format
     * @param streetAddress Street address to validate
     * @return true if valid street format
     */
    public static boolean isValidStreetAddress(String streetAddress) {
        if (streetAddress == null || streetAddress.trim().isEmpty()) {
            return true; // Street is optional
        }
        
        String trimmed = streetAddress.trim();
        return trimmed.length() <= 100 && STREET_PATTERN.matcher(trimmed).matches();
    }
    
    /**
     * Validates barangay name format
     * @param barangayName Barangay name to validate
     * @return true if valid barangay format
     */
    public static boolean isValidBarangayName(String barangayName) {
        if (barangayName == null || barangayName.trim().isEmpty()) {
            return true; // Barangay is optional
        }
        
        String trimmed = barangayName.trim();
        return trimmed.length() <= 50 && BARANGAY_PATTERN.matcher(trimmed).matches();
    }
    
    /**
     * Validates city name
     * @param cityName City name to validate
     * @return true if valid city name
     */
    public static boolean isValidCityName(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            return false; // City is required
        }
        
        String trimmed = cityName.trim();
        return trimmed.length() <= 50 && trimmed.matches("^[a-zA-Z\\s.-]+$");
    }
    
    /**
     * Validates province name
     * @param provinceName Province name to validate
     * @return true if valid province name
     */
    public static boolean isValidProvinceName(String provinceName) {
        if (provinceName == null || provinceName.trim().isEmpty()) {
            return false; // Province is required
        }
        
        String trimmed = provinceName.trim();
        return trimmed.length() <= 50 && trimmed.matches("^[a-zA-Z\\s.-]+$");
    }
    
    /**
     * Checks if province is a known Philippine province
     * @param provinceName Province name to check
     * @return true if province is recognized
     */
    public static boolean isKnownPhilippineProvince(String provinceName) {
        if (provinceName == null) {
            return false;
        }
        
        String lowerProvince = provinceName.toLowerCase();
        return COMMON_PROVINCES.stream()
                .anyMatch(province -> province.toLowerCase().equals(lowerProvince));
    }
    
    /**
     * Validates complete address for Philippine format
     * @return ValidationResult with details
     */
    public ValidationResult validatePhilippineAddress() {
        ValidationResult result = new ValidationResult();
        
        // Validate required fields
        if (!isValidCityName(city)) {
            result.addError("City is required and must be valid");
        }
        
        if (!isValidProvinceName(province)) {
            result.addError("Province is required and must be valid");
        }
        
        // Validate optional fields
        if (!isValidStreetAddress(street)) {
            result.addError("Street address format is invalid");
        }
        
        if (!isValidBarangayName(barangay)) {
            result.addError("Barangay name format is invalid");
        }
        
        if (!isValidPhilippineZipCode(zipCode)) {
            result.addError("ZIP code must be 4 digits");
        }
        
        // Validate Manila-specific rules
        if (isInManilaArea() && !hasValidManilaZipCode()) {
            result.addWarning("ZIP code may not be appropriate for Manila area");
        }
        
        // Check if province is known
        if (!isKnownPhilippineProvince(province)) {
            result.addWarning("Province name may not be recognized");
        }
        
        return result;
    }
    

    // BUSINESS METHODS
    
    /**
     * Get full address as formatted string (Philippine format)
     * @return Formatted address string
     */
    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();
        
        if (street != null && !street.trim().isEmpty()) {
            fullAddress.append(street);
        }
        
        if (barangay != null && !barangay.trim().isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(barangay);
        }
        
        if (city != null && !city.trim().isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(city);
        }
        
        if (province != null && !province.trim().isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(province);
        }
        
        if (zipCode != null && !zipCode.trim().isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(" ");
            fullAddress.append(zipCode);
        }
        
        // Add country for complete address
        if (fullAddress.length() > 0) {
            fullAddress.append(", ").append(COUNTRY);
        }
        
        return fullAddress.toString();
    }
    
    /**
     * Get address formatted for Philippine postal system
     * @return Postal formatted address
     */
    public String getPostalFormattedAddress() {
        StringBuilder address = new StringBuilder();
        
        // Line 1: Street address
        if (street != null && !street.trim().isEmpty()) {
            address.append(street).append("\n");
        }
        
        // Line 2: Barangay, City
        if (barangay != null && !barangay.trim().isEmpty()) {
            address.append(barangay).append(", ");
        }
        
        if (city != null && !city.trim().isEmpty()) {
            address.append(city).append("\n");
        }
        
        // Line 3: Province ZIP
        if (province != null && !province.trim().isEmpty()) {
            address.append(province);
        }
        
        if (zipCode != null && !zipCode.trim().isEmpty()) {
            address.append(" ").append(zipCode);
        }
        
        return address.toString();
    }
    
    /**
     * Check if address is valid (has minimum required fields)
     * @return true if address has required fields
     */
    public boolean isValid() {
        return city != null && !city.trim().isEmpty() && 
               province != null && !province.trim().isEmpty();
    }
    
    /**
     * Check if address is complete (has all fields)
     * @return true if all fields are populated
     */
    public boolean isComplete() {
        return street != null && !street.trim().isEmpty() &&
               barangay != null && !barangay.trim().isEmpty() &&
               city != null && !city.trim().isEmpty() &&
               province != null && !province.trim().isEmpty() &&
               zipCode != null && !zipCode.trim().isEmpty();
    }
    
    /**
     * Get formatted address for display
     * @return Formatted display address
     */
    public String getDisplayAddress() {
        return getFullAddress();
    }
    
    /**
     * Get short address format (City, Province)
     * @return Short address string
     */
    public String getShortAddress() {
        StringBuilder shortAddress = new StringBuilder();
        
        if (city != null && !city.trim().isEmpty()) {
            shortAddress.append(city);
        }
        
        if (province != null && !province.trim().isEmpty()) {
            if (shortAddress.length() > 0) shortAddress.append(", ");
            shortAddress.append(province);
        }
        
        return shortAddress.toString();
    }
    
    /**
     * Get address location category
     * @return Location category string
     */
    public String getLocationCategory() {
        if (isInTaguig()) {
            return "Local (Taguig)";
        } else if (isInManilaArea()) {
            return "Metro Manila";
        } else {
            return getDistanceFromManilaCategory();
        }
    }
    
    /**
     * Check if address needs ZIP code validation
     * @return true if ZIP code should be validated
     */
    public boolean needsZipCodeValidation() {
        return city != null && province != null;
    }
    
    /**
     * Auto-complete address based on ZIP code
     * @return true if address was auto-completed
     */
    public boolean autoCompleteFromZipCode() {
        if (zipCode == null || !isValidPhilippineZipCode(zipCode)) {
            return false;
        }
        
        // Auto-complete Manila area addresses
        if (MANILA_ZIP_PATTERN.matcher(zipCode).matches()) {
            if (province == null || !province.toLowerCase().contains("metro manila")) {
                this.province = METRO_MANILA;
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get address suggestions based on partial input
     * @param partialCity Partial city name
     * @return List of matching cities
     */
    public static List<String> getCitySuggestions(String partialCity) {
        List<String> suggestions = new ArrayList<>();
        
        if (partialCity == null || partialCity.trim().isEmpty()) {
            return suggestions;
        }
        
        String lowerPartial = partialCity.toLowerCase();
        
        // Add Manila cities that match
        MANILA_CITIES.stream()
                .filter(city -> city.toLowerCase().contains(lowerPartial))
                .forEach(suggestions::add);
        
        return suggestions;
    }
    
    /**
     * Get province suggestions based on partial input
     * @param partialProvince Partial province name
     * @return List of matching provinces
     */
    public static List<String> getProvinceSuggestions(String partialProvince) {
        List<String> suggestions = new ArrayList<>();
        
        if (partialProvince == null || partialProvince.trim().isEmpty()) {
            return suggestions;
        }
        
        String lowerPartial = partialProvince.toLowerCase();
        
        // Add provinces that match
        COMMON_PROVINCES.stream()
                .filter(province -> province.toLowerCase().contains(lowerPartial))
                .forEach(suggestions::add);
        
        return suggestions;
    }
    
    @Override
    public String toString() {
        return String.format("AddressModel{addressId=%d, street='%s', barangay='%s', city='%s', province='%s', zipCode='%s', location='%s'}", 
                           addressId, street, barangay, city, province, zipCode, getLocationCategory());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        AddressModel address = (AddressModel) obj;
        return Objects.equals(addressId, address.addressId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(addressId);
    }
    
    // VALIDATION RESULT CLASS
 
    
    /**
     * Validation result class for address validation
     */
    public static class ValidationResult {
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
        
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }
        
        public String getErrorsAsString() {
            return String.join("; ", errors);
        }
        
        public String getWarningsAsString() {
            return String.join("; ", warnings);
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ValidationResult{");
            sb.append("valid=").append(isValid());
            
            if (!errors.isEmpty()) {
                sb.append(", errors=").append(errors);
            }
            
            if (!warnings.isEmpty()) {
                sb.append(", warnings=").append(warnings);
            }
            
            sb.append("}");
            return sb.toString();
        }
    }
}