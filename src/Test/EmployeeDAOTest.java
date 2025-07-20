package test;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoExtensions;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dao.EmployeeDAO;
import model.Employee;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * JUnit 5 test class for EmployeeDAO
 * Tests database operations with proper mocking
 */
@ExtendWith(MockitoExtensions.class)
@DisplayName("Employee DAO Tests")
class EmployeeDAOTest {

    @Mock
    private Connection mockConnection;
    
    @Mock
    private PreparedStatement mockPreparedStatement;
    
    @Mock
    private ResultSet mockResultSet;

    private EmployeeDAO employeeDAO;
    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        employeeDAO = new EmployeeDAO();
        
        // Create test employee
        testEmployee = new Employee();
        testEmployee.setEmployeeId(10001);
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setPosition("Software Developer");
        testEmployee.setStatus("Regular");
        testEmployee.setBasicSalary(50000.0);
        testEmployee.setBirthday(LocalDate.of(1990, 1, 1));
        testEmployee.setAddress("123 Test Street");
        testEmployee.setPhoneNumber("123-456-7890");
        testEmployee.setSssNumber("12-3456789-0");
        testEmployee.setPhilhealthNumber("12-345678901-2");
        testEmployee.setTinNumber("123-456-789-000");
        testEmployee.setPagibigNumber("1234-5678-9012");
        testEmployee.setImmediateSupervisor("Jane Smith");
        testEmployee.setRiceSubsidy(1500.0);
        testEmployee.setPhoneAllowance(1000.0);
        testEmployee.setClothingAllowance(800.0);
    }

    @Test
    @DisplayName("Should validate employee data before insertion")
    void testEmployeeValidationBeforeInsertion() {
        // Test null employee
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.insertEmployee(null));

        // Test invalid employee ID
        Employee invalidEmployee = new Employee();
        invalidEmployee.setEmployeeId(0);
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.insertEmployee(invalidEmployee));

        // Test missing first name
        Employee noFirstName = new Employee();
        noFirstName.setEmployeeId(10001);
        noFirstName.setLastName("Doe");
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.insertEmployee(noFirstName));

        // Test missing last name
        Employee noLastName = new Employee();
        noLastName.setEmployeeId(10001);
        noLastName.setFirstName("John");
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.insertEmployee(noLastName));

        // Test negative salary
        Employee negativeSalary = new Employee();
        negativeSalary.setEmployeeId(10001);
        negativeSalary.setFirstName("John");
        negativeSalary.setLastName("Doe");
        negativeSalary.setBasicSalary(-1000.0);
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.insertEmployee(negativeSalary));
    }

    @Test
    @DisplayName("Should validate field lengths")
    void testFieldLengthValidation() {
        // Test first name too long
        Employee longFirstName = new Employee();
        longFirstName.setEmployeeId(10001);
        longFirstName.setFirstName("A".repeat(51)); // 51 characters
        longFirstName.setLastName("Doe");
        longFirstName.setBasicSalary(50000.0);
        
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.insertEmployee(longFirstName));

        // Test last name too long
        Employee longLastName = new Employee();
        longLastName.setEmployeeId(10001);
        longLastName.setFirstName("John");
        longLastName.setLastName("D".repeat(51)); // 51 characters
        longLastName.setBasicSalary(50000.0);
        
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.insertEmployee(longLastName));

        // Test position too long
        Employee longPosition = new Employee();
        longPosition.setEmployeeId(10001);
        longPosition.setFirstName("John");
        longPosition.setLastName("Doe");
        longPosition.setPosition("P".repeat(101)); // 101 characters
        longPosition.setBasicSalary(50000.0);
        
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.insertEmployee(longPosition));
    }

    @Test
    @DisplayName("Should validate allowances are non-negative")
    void testAllowanceValidation() {
        // Test negative rice subsidy
        Employee negativeRice = new Employee();
        negativeRice.setEmployeeId(10001);
        negativeRice.setFirstName("John");
        negativeRice.setLastName("Doe");
        negativeRice.setBasicSalary(50000.0);
        negativeRice.setRiceSubsidy(-100.0);
        
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.insertEmployee(negativeRice));

        // Test negative phone allowance
        Employee negativePhone = new Employee();
        negativePhone.setEmployeeId(10001);
        negativePhone.setFirstName("John");
        negativePhone.setLastName("Doe");
        negativePhone.setBasicSalary(50000.0);
        negativePhone.setPhoneAllowance(-100.0);
        
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.insertEmployee(negativePhone));

        // Test negative clothing allowance
        Employee negativeClothing = new Employee();
        negativeClothing.setEmployeeId(10001);
        negativeClothing.setFirstName("John");
        negativeClothing.setLastName("Doe");
        negativeClothing.setBasicSalary(50000.0);
        negativeClothing.setClothingAllowance(-100.0);
        
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.insertEmployee(negativeClothing));
    }

    @Test
    @DisplayName("Should validate phone number format")
    void testPhoneNumberValidation() {
        // Test invalid phone number with letters
        Employee invalidPhone = new Employee();
        invalidPhone.setEmployeeId(10001);
        invalidPhone.setFirstName("John");
        invalidPhone.setLastName("Doe");
        invalidPhone.setBasicSalary(50000.0);
        invalidPhone.setPhoneNumber("abc-def-ghij");
        
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.insertEmployee(invalidPhone));

        // Test phone number too long
        Employee longPhone = new Employee();
        longPhone.setEmployeeId(10001);
        longPhone.setFirstName("John");
        longPhone.setLastName("Doe");
        longPhone.setBasicSalary(50000.0);
        longPhone.setPhoneNumber("1".repeat(21)); // 21 characters
        
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.insertEmployee(longPhone));
    }

    @Test
    @DisplayName("Should validate employment status")
    void testEmploymentStatusValidation() {
        // Test invalid status
        Employee invalidStatus = new Employee();
        invalidStatus.setEmployeeId(10001);
        invalidStatus.setFirstName("John");
        invalidStatus.setLastName("Doe");
        invalidStatus.setBasicSalary(50000.0);
        invalidStatus.setStatus("InvalidStatus");
        
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.insertEmployee(invalidStatus));
    }

    @Test
    @DisplayName("Should validate employee exists method")
    void testEmployeeExistsValidation() {
        // Test invalid employee ID
        assertFalse(employeeDAO.employeeExists(0));
        assertFalse(employeeDAO.employeeExists(-1));
    }

    @Test
    @DisplayName("Should validate search parameters")
    void testSearchParametersValidation() {
        // Test null search term returns all employees
        List<Employee> result = employeeDAO.searchEmployees(null);
        assertNotNull(result);

        // Test empty search term returns all employees
        List<Employee> result2 = employeeDAO.searchEmployees("");
        assertNotNull(result2);

        // Test whitespace search term returns all employees
        List<Employee> result3 = employeeDAO.searchEmployees("   ");
        assertNotNull(result3);
    }

    @Test
    @DisplayName("Should validate status filter parameters")
    void testStatusFilterValidation() {
        // Test null status
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.getEmployeesByStatus(null));

        // Test empty status
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.getEmployeesByStatus(""));

        // Test whitespace status
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.getEmployeesByStatus("   "));
    }

    @Test
    @DisplayName("Should validate position filter parameters")
    void testPositionFilterValidation() {
        // Test null position
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.getEmployeesByPosition(null));

        // Test empty position
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.getEmployeesByPosition(""));

        // Test whitespace position
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.getEmployeesByPosition("   "));
    }

    @Test
    @DisplayName("Should validate supervisor filter parameters")
    void testSupervisorFilterValidation() {
        // Test null supervisor
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.getEmployeesBySupervisor(null));

        // Test empty supervisor
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.getEmployeesBySupervisor(""));

        // Test whitespace supervisor
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.getEmployeesBySupervisor("   "));
    }

    @Test
    @DisplayName("Should validate employee count by status parameters")
    void testEmployeeCountByStatusValidation() {
        // Test null status
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.getEmployeeCountByStatus(null));

        // Test empty status
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.getEmployeeCountByStatus(""));

        // Test whitespace status
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.getEmployeeCountByStatus("   "));
    }

    @Test
    @DisplayName("Should validate update employee parameters")
    void testUpdateEmployeeValidation() {
        // Test null employee
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.updateEmployee(null));

        // Test invalid employee ID
        Employee invalidId = new Employee();
        invalidId.setEmployeeId(0);
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.updateEmployee(invalidId));

        // Test missing required fields for update
        Employee missingFields = new Employee();
        missingFields.setEmployeeId(10001);
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.updateEmployee(missingFields));
    }

    @Test
    @DisplayName("Should validate delete employee parameters")
    void testDeleteEmployeeValidation() {
        // Test invalid employee ID
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.deleteEmployee(0));

        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.deleteEmployee(-1));
    }
}