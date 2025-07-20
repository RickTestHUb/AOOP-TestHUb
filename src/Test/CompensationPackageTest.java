package test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import model.*;

/**
 * JUnit 5 test class for Compensation Package functionality
 * Tests inheritance, polymorphism, and abstraction in allowance classes
 */
@DisplayName("Compensation Package Tests")
class CompensationPackageTest {

    private Employee testEmployee;
    private CompensationDetails compensationDetails;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee();
        testEmployee.setEmployeeId(10001);
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setStatus("Regular");
        testEmployee.setBasicSalary(50000.0);

        compensationDetails = new CompensationDetails(10001);
    }

    @Test
    @DisplayName("Should create rice allowance with inheritance")
    void testRiceAllowanceInheritance() {
        // Arrange & Act
        RiceAllowance riceAllowance = new RiceAllowance(10001);

        // Assert - Testing inheritance from Allowance
        assertAll("Rice allowance inheritance",
            () -> assertNotNull(riceAllowance),
            () -> assertEquals(10001, riceAllowance.getEmployeeId()),
            () -> assertEquals("Rice Subsidy", riceAllowance.getType()),
            () -> assertEquals(1500.0, riceAllowance.getAmount(), 0.01),
            () -> assertFalse(riceAllowance.isTaxable()),
            () -> assertTrue(riceAllowance.isEligible(testEmployee))
        );
    }

    @Test
    @DisplayName("Should create phone allowance with inheritance")
    void testPhoneAllowanceInheritance() {
        // Arrange & Act
        PhoneAllowance phoneAllowance = new PhoneAllowance(10001, 800.0);

        // Assert - Testing inheritance from Allowance
        assertAll("Phone allowance inheritance",
            () -> assertNotNull(phoneAllowance),
            () -> assertEquals(10001, phoneAllowance.getEmployeeId()),
            () -> assertEquals("Phone Allowance", phoneAllowance.getType()),
            () -> assertEquals(800.0, phoneAllowance.getAmount(), 0.01),
            () -> assertTrue(phoneAllowance.isTaxable()),
            () -> assertTrue(phoneAllowance.isEligible(testEmployee))
        );
    }

    @Test
    @DisplayName("Should create clothing allowance with inheritance")
    void testClothingAllowanceInheritance() {
        // Arrange & Act
        ClothingAllowance clothingAllowance = new ClothingAllowance(10001);

        // Assert - Testing inheritance from Allowance
        assertAll("Clothing allowance inheritance",
            () -> assertNotNull(clothingAllowance),
            () -> assertEquals(10001, clothingAllowance.getEmployeeId()),
            () -> assertEquals("Clothing Allowance", clothingAllowance.getType()),
            () -> assertEquals(1000.0, clothingAllowance.getAmount(), 0.01),
            () -> assertTrue(clothingAllowance.isTaxable()),
            () -> assertTrue(clothingAllowance.isEligible(testEmployee))
        );
    }

    @Test
    @DisplayName("Should demonstrate polymorphism with allowance calculation")
    void testAllowancePolymorphism() {
        // Arrange - Create different allowance types
        Allowance riceAllowance = new RiceAllowance(10001);
        Allowance phoneAllowance = new PhoneAllowance(10001, 800.0);
        Allowance clothingAllowance = new ClothingAllowance(10001);

        // Act - Call polymorphic method
        double riceAmount = riceAllowance.getCalculatedAmount();
        double phoneAmount = phoneAllowance.getCalculatedAmount();
        double clothingAmount = clothingAllowance.getCalculatedAmount();

        // Assert - Testing polymorphism
        assertAll("Allowance polymorphism",
            () -> assertEquals(1500.0, riceAmount, 0.01),
            () -> assertEquals(800.0, phoneAmount, 0.01),
            () -> assertEquals(1000.0, clothingAmount, 0.01)
        );
    }

    @Test
    @DisplayName("Should test allowance eligibility polymorphism")
    void testAllowanceEligibilityPolymorphism() {
        // Arrange
        Employee regularEmployee = new Employee();
        regularEmployee.setStatus("Regular");

        Employee probationaryEmployee = new Employee();
        probationaryEmployee.setStatus("Probationary");

        Employee contractualEmployee = new Employee();
        contractualEmployee.setStatus("Contractual");

        Allowance riceAllowance = new RiceAllowance(10001);
        Allowance phoneAllowance = new PhoneAllowance(10001, 800.0);
        Allowance clothingAllowance = new ClothingAllowance(10001);

        // Act & Assert - Testing polymorphic eligibility
        assertAll("Eligibility polymorphism",
            // Rice allowance - eligible for Regular and Probationary
            () -> assertTrue(riceAllowance.isEligible(regularEmployee)),
            () -> assertTrue(riceAllowance.isEligible(probationaryEmployee)),
            () -> assertFalse(riceAllowance.isEligible(contractualEmployee)),

            // Phone allowance - eligible for Regular only
            () -> assertTrue(phoneAllowance.isEligible(regularEmployee)),
            () -> assertFalse(phoneAllowance.isEligible(probationaryEmployee)),
            () -> assertFalse(phoneAllowance.isEligible(contractualEmployee)),

            // Clothing allowance - eligible for Regular only
            () -> assertTrue(clothingAllowance.isEligible(regularEmployee)),
            () -> assertFalse(clothingAllowance.isEligible(probationaryEmployee)),
            () -> assertFalse(clothingAllowance.isEligible(contractualEmployee))
        );
    }

    @Test
    @DisplayName("Should test compensation details composition")
    void testCompensationDetailsComposition() {
        // Act
        compensationDetails.calculateAllowancesForEmployee(testEmployee);
        double totalAllowances = compensationDetails.getTotalAllowances();

        // Assert - Testing composition
        assertAll("Compensation details composition",
            () -> assertTrue(compensationDetails.hasAllowances()),
            () -> assertTrue(totalAllowances > 0),
            () -> assertNotNull(compensationDetails.getAllowancesSummary()),
            () -> assertTrue(compensationDetails.getAllowancesSummary().contains("Rice")),
            () -> assertTrue(compensationDetails.getAllowancesSummary().contains("Phone")),
            () -> assertTrue(compensationDetails.getAllowancesSummary().contains("Clothing"))
        );
    }

    @Test
    @DisplayName("Should test allowance validation")
    void testAllowanceValidation() {
        // Test invalid employee ID
        assertThrows(IllegalArgumentException.class, 
            () -> new RiceAllowance(0));

        assertThrows(IllegalArgumentException.class, 
            () -> new RiceAllowance(-1));

        // Test invalid allowance type
        Allowance allowance = new RiceAllowance(10001);
        assertThrows(IllegalArgumentException.class, 
            () -> allowance.setType(null));

        assertThrows(IllegalArgumentException.class, 
            () -> allowance.setType(""));

        // Test invalid amount
        assertThrows(IllegalArgumentException.class, 
            () -> allowance.setAmount(-100.0));
    }

    @Test
    @DisplayName("Should test allowance abstraction")
    void testAllowanceAbstraction() {
        // Arrange
        Allowance riceAllowance = new RiceAllowance(10001);

        // Act & Assert - Testing abstract class functionality
        assertAll("Allowance abstraction",
            () -> assertNotNull(riceAllowance.getType()),
            () -> assertTrue(riceAllowance.getAmount() >= 0),
            () -> assertNotNull(riceAllowance.getEffectiveDate()),
            () -> assertEquals(riceAllowance.getAmount(), riceAllowance.getCalculatedAmount(), 0.01)
        );
    }

    @Test
    @DisplayName("Should test compensation details default constructor")
    void testCompensationDetailsDefaultConstructor() {
        // Arrange & Act
        CompensationDetails defaultCompensation = new CompensationDetails();

        // Assert
        assertAll("Default compensation details",
            () -> assertNotNull(defaultCompensation),
            () -> assertEquals(0.0, defaultCompensation.getRiceSubsidy(), 0.01),
            () -> assertEquals(0.0, defaultCompensation.getPhoneAllowance(), 0.01),
            () -> assertEquals(0.0, defaultCompensation.getClothingAllowance(), 0.01),
            () -> assertEquals(0.0, defaultCompensation.getTotalAllowances(), 0.01),
            () -> assertFalse(defaultCompensation.hasAllowances())
        );
    }

    @Test
    @DisplayName("Should test allowance effective date")
    void testAllowanceEffectiveDate() {
        // Arrange & Act
        RiceAllowance riceAllowance = new RiceAllowance(10001);

        // Assert
        assertAll("Allowance effective date",
            () -> assertNotNull(riceAllowance.getEffectiveDate()),
            () -> assertTrue(riceAllowance.getEffectiveDate().isBefore(
                java.time.LocalDate.now().plusDays(1)))
        );
    }
}