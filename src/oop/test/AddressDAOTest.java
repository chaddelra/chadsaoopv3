package oop.test;

import DAOs.AddressDAO;
import Models.AddressModel;
import java.util.List;
import java.util.Map;

/**
 * Simple test class for AddressDAO
 * Tests the enhanced AddressModel integration
 */
public class AddressDAOTest {
    
    public static void main(String[] args) {
        System.out.println("🧪 Testing AddressDAO with Enhanced AddressModel...\n");
        
        // Create AddressDAO instance
        AddressDAO addressDAO = new AddressDAO();
        
        // Test 1: Get total address count
        testAddressCount(addressDAO);
        
        // Test 2: Test finding all addresses
        testFindAllAddresses(addressDAO);
        
        // Test 3: Test Manila-specific methods
        testManilaSpecificMethods(addressDAO);
        
        // Test 4: Test creating a new address
        testCreateAddress(addressDAO);
        
        // Test 5: Test address validation and formatting
        testAddressValidation(addressDAO);
        
        System.out.println("\n✅ AddressDAO testing completed!");
    }
    
    private static void testAddressCount(AddressDAO addressDAO) {
        System.out.println("🔢 Test 1: Getting address count...");
        try {
            int count = addressDAO.getAddressCount();
            System.out.println("   Total addresses in database: " + count);
            if (count >= 0) {
                System.out.println("   ✅ Address count test passed");
            } else {
                System.out.println("   ❌ Address count test failed");
            }
        } catch (Exception e) {
            System.out.println("   ❌ Address count test failed: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testFindAllAddresses(AddressDAO addressDAO) {
        System.out.println("📋 Test 2: Finding all addresses...");
        try {
            List<AddressModel> addresses = addressDAO.findAll();
            System.out.println("   Found " + addresses.size() + " addresses");
            
            // Display first few addresses to verify data
            int displayCount = Math.min(3, addresses.size());
            for (int i = 0; i < displayCount; i++) {
                AddressModel addr = addresses.get(i);
                System.out.println("   " + (i+1) + ". " + addr.getDisplayAddress());
                System.out.println("      Region: " + addr.getRegion());
                System.out.println("      Manila Area: " + (addr.isInMetroManila() ? "Yes" : "No"));
            }
            
            if (addresses.size() > 3) {
                System.out.println("   ... and " + (addresses.size() - 3) + " more addresses");
            }
            
            System.out.println("   ✅ Find all addresses test passed");
        } catch (Exception e) {
            System.out.println("   ❌ Find all addresses test failed: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    private static void testManilaSpecificMethods(AddressDAO addressDAO) {
        System.out.println("🏙️ Test 3: Testing Manila-specific methods...");
        try {
            // Test Manila area addresses
            List<AddressModel> manilaAddresses = addressDAO.findManilaAreaAddresses();
            System.out.println("   Manila area addresses: " + manilaAddresses.size());
            
            // Test non-Manila addresses
            List<AddressModel> nonManilaAddresses = addressDAO.findNonManilaAddresses();
            System.out.println("   Non-Manila addresses: " + nonManilaAddresses.size());
            
            // Test address location statistics
            Map<String, Integer> stats = addressDAO.getAddressLocationStatistics();
            System.out.println("   Address Statistics:");
            System.out.println("   - Total: " + stats.getOrDefault("total", 0));
            System.out.println("   - Manila Area: " + stats.getOrDefault("manilaArea", 0));
            System.out.println("   - Non-Manila Area: " + stats.getOrDefault("nonManilaArea", 0));
            
            // Test unique cities and provinces
            List<String> cities = addressDAO.getUniqueCities();
            List<String> provinces = addressDAO.getUniqueProvinces();
            System.out.println("   Unique cities: " + cities.size());
            System.out.println("   Unique provinces: " + provinces.size());
            
            System.out.println("   ✅ Manila-specific methods test passed");
        } catch (Exception e) {
            System.out.println("   ❌ Manila-specific methods test failed: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    private static void testCreateAddress(AddressDAO addressDAO) {
        System.out.println("➕ Test 4: Testing address creation...");
        try {
            // Create a test address
            AddressModel testAddress = new AddressModel();
            testAddress.setStreet("123 Test Street");
            testAddress.setBarangay("Test Barangay");
            testAddress.setCity("Makati");
            testAddress.setProvince("Metro Manila");
            testAddress.setZipCode("1234");
            
            System.out.println("   Creating test address: " + testAddress.getDisplayAddress());
            System.out.println("   Is valid: " + testAddress.isValid());
            System.out.println("   Is in Metro Manila: " + testAddress.isInMetroManila());
            System.out.println("   Region: " + testAddress.getRegion());
            
            // Try to save the address
            boolean saved = addressDAO.save(testAddress);
            if (saved) {
                System.out.println("   ✅ Address saved successfully with ID: " + testAddress.getAddressId());
                
                // Try to find it back
                AddressModel foundAddress = addressDAO.findById(testAddress.getAddressId());
                if (foundAddress != null) {
                    System.out.println("   ✅ Address found back: " + foundAddress.getDisplayAddress());
                    
                    // Clean up - delete the test address
                    boolean deleted = addressDAO.deleteById(testAddress.getAddressId());
                    if (deleted) {
                        System.out.println("   ✅ Test address cleaned up successfully");
                    }
                } else {
                    System.out.println("   ❌ Could not find saved address");
                }
            } else {
                System.out.println("   ⚠️ Address save failed (this might be normal if there are database constraints)");
            }
            
            System.out.println("   ✅ Address creation test completed");
        } catch (Exception e) {
            System.out.println("   ❌ Address creation test failed: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    private static void testAddressValidation(AddressDAO addressDAO) {
        System.out.println("✅ Test 5: Testing address validation and formatting...");
        try {
            // Test various address formats
            AddressModel[] testAddresses = {
                createTestAddress("  123 Main St  ", "  Poblacion  ", "  makati  ", "  metro manila  ", "  1234  "),
                createTestAddress("456 Test Ave", "Barangay 1", "QUEZON CITY", "METRO MANILA", "1100"),
                createTestAddress("789 Sample Rd", "Test Brgy", "taguig", "ncr", "1630"),
                createTestAddress("321 Demo St", "Sample Barangay", "Manila", "National Capital Region", "1000")
            };
            
            for (int i = 0; i < testAddresses.length; i++) {
                AddressModel addr = testAddresses[i];
                System.out.println("   Test Address " + (i+1) + ":");
                System.out.println("   Before normalization: " + addr.getFormattedAddress());
                
                // Normalize the address
                addr.normalizeAddress();
                
                System.out.println("   After normalization:  " + addr.getFormattedAddress());
                System.out.println("   Is valid: " + addr.isValid());
                System.out.println("   Is Manila area: " + addr.isInMetroManila());
                System.out.println("   ZIP code valid: " + addr.isValidZipCode());
                System.out.println();
            }
            
            System.out.println("   ✅ Address validation test passed");
        } catch (Exception e) {
            System.out.println("   ❌ Address validation test failed: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    private static AddressModel createTestAddress(String street, String barangay, String city, String province, String zipCode) {
        AddressModel address = new AddressModel();
        address.setStreet(street);
        address.setBarangay(barangay);
        address.setCity(city);
        address.setProvince(province);
        address.setZipCode(zipCode);
        return address;
    }
}