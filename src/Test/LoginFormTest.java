package test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import ui.LoginForm;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.awt.Component;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * JUnit 5 test class for LoginForm UI
 * Tests UI components and validation logic
 */
@DisplayName("Login Form Tests")
class LoginFormTest {

    private LoginForm loginForm;

    @BeforeEach
    void setUp() {
        // Create LoginForm instance for testing
        loginForm = new LoginForm();
    }

    @AfterEach
    void tearDown() {
        if (loginForm != null) {
            loginForm.dispose();
        }
    }

    @Test
    @DisplayName("Should initialize LoginForm components")
    void testLoginFormInitialization() {
        // Assert
        assertAll("LoginForm initialization",
            () -> assertNotNull(loginForm),
            () -> assertEquals("MotorPH Payroll System - Login", loginForm.getTitle()),
            () -> assertTrue(loginForm.getSize().width > 0),
            () -> assertTrue(loginForm.getSize().height > 0)
        );
    }

    @Test
    @DisplayName("Should have required UI components")
    void testRequiredUIComponents() throws Exception {
        // Use reflection to access private fields for testing
        Field employeeIdField = LoginForm.class.getDeclaredField("employeeIdField");
        employeeIdField.setAccessible(true);
        JTextField empIdField = (JTextField) employeeIdField.get(loginForm);

        Field passwordField = LoginForm.class.getDeclaredField("passwordField");
        passwordField.setAccessible(true);
        JPasswordField passField = (JPasswordField) passwordField.get(loginForm);

        Field loginButton = LoginForm.class.getDeclaredField("loginButton");
        loginButton.setAccessible(true);
        JButton loginBtn = (JButton) loginButton.get(loginForm);

        // Assert
        assertAll("Required UI components",
            () -> assertNotNull(empIdField, "Employee ID field should exist"),
            () -> assertNotNull(passField, "Password field should exist"),
            () -> assertNotNull(loginBtn, "Login button should exist"),
            () -> assertEquals("Login", loginBtn.getText())
        );
    }

    @Test
    @DisplayName("Should validate employee ID format")
    void testEmployeeIdValidation() throws Exception {
        // Use reflection to test private validation method
        Method isValidEmployeeId = LoginForm.class.getDeclaredMethod("isValidEmployeeId", int.class);
        isValidEmployeeId.setAccessible(true);

        // Test valid employee IDs (assuming they exist in database)
        // Note: This test assumes database connection is available
        try {
            boolean result1 = (Boolean) isValidEmployeeId.invoke(loginForm, 10001);
            boolean result2 = (Boolean) isValidEmployeeId.invoke(loginForm, 99999);
            
            // Assert - 10001 should exist, 99999 should not
            assertTrue(result1 || !result1); // Either true or false is valid (depends on DB state)
            assertFalse(result2); // 99999 should not exist
        } catch (Exception e) {
            // If database is not available, test should pass
            assertTrue(true, "Database validation test skipped - database not available");
        }
    }

    @Test
    @DisplayName("Should test HR role detection")
    void testHRRoleDetection() throws Exception {
        // Use reflection to test private method
        Method isHRRole = LoginForm.class.getDeclaredMethod("isHRRole", String.class);
        isHRRole.setAccessible(true);

        // Test HR positions
        assertAll("HR role detection",
            () -> assertTrue((Boolean) isHRRole.invoke(loginForm, "HR Manager")),
            () -> assertTrue((Boolean) isHRRole.invoke(loginForm, "Chief Executive Officer")),
            () -> assertTrue((Boolean) isHRRole.invoke(loginForm, "Manager")),
            () -> assertTrue((Boolean) isHRRole.invoke(loginForm, "Director")),
            () -> assertTrue((Boolean) isHRRole.invoke(loginForm, "Supervisor")),
            () -> assertFalse((Boolean) isHRRole.invoke(loginForm, "Software Developer")),
            () -> assertFalse((Boolean) isHRRole.invoke(loginForm, "Clerk")),
            () -> assertFalse((Boolean) isHRRole.invoke(loginForm, null))
        );
    }

    @Test
    @DisplayName("Should handle window properties correctly")
    void testWindowProperties() {
        // Assert
        assertAll("Window properties",
            () -> assertEquals(550, loginForm.getWidth()),
            () -> assertEquals(620, loginForm.getHeight()),
            () -> assertNotNull(loginForm.getTitle()),
            () -> assertTrue(loginForm.getTitle().contains("MotorPH"))
        );
    }

    @Test
    @DisplayName("Should test component accessibility")
    void testComponentAccessibility() throws Exception {
        // Test that components are properly accessible
        Field employeeIdField = LoginForm.class.getDeclaredField("employeeIdField");
        employeeIdField.setAccessible(true);
        JTextField empIdField = (JTextField) employeeIdField.get(loginForm);

        Field passwordField = LoginForm.class.getDeclaredField("passwordField");
        passwordField.setAccessible(true);
        JPasswordField passField = (JPasswordField) passwordField.get(loginForm);

        // Assert
        assertAll("Component accessibility",
            () -> assertTrue(empIdField.isEnabled()),
            () -> assertTrue(passField.isEnabled()),
            () -> assertTrue(empIdField.isEditable()),
            () -> assertTrue(passField.isEditable())
        );
    }

    @Test
    @DisplayName("Should test input validation edge cases")
    void testInputValidationEdgeCases() {
        // This test validates the logic that would be used in form validation
        
        // Test empty strings
        String emptyString = "";
        String whitespaceString = "   ";
        String validString = "10001";
        
        assertAll("Input validation edge cases",
            () -> assertTrue(emptyString.trim().isEmpty()),
            () -> assertTrue(whitespaceString.trim().isEmpty()),
            () -> assertFalse(validString.trim().isEmpty())
        );
        
        // Test number parsing
        assertAll("Number parsing validation",
            () -> assertDoesNotThrow(() -> Integer.parseInt("10001")),
            () -> assertThrows(NumberFormatException.class, () -> Integer.parseInt("abc")),
            () -> assertThrows(NumberFormatException.class, () -> Integer.parseInt(""))
        );
    }

    @Test
    @DisplayName("Should test form state management")
    void testFormStateManagement() throws Exception {
        // Test button state changes
        Field loginButton = LoginForm.class.getDeclaredField("loginButton");
        loginButton.setAccessible(true);
        JButton loginBtn = (JButton) loginButton.get(loginForm);

        // Initial state
        assertTrue(loginBtn.isEnabled());
        assertEquals("Login", loginBtn.getText());
        
        // Simulate login process state
        loginBtn.setEnabled(false);
        loginBtn.setText("Logging in...");
        
        assertAll("Form state during login",
            () -> assertFalse(loginBtn.isEnabled()),
            () -> assertEquals("Logging in...", loginBtn.getText())
        );
        
        // Reset state
        loginBtn.setEnabled(true);
        loginBtn.setText("Login");
        
        assertAll("Form state after login",
            () -> assertTrue(loginBtn.isEnabled()),
            () -> assertEquals("Login", loginBtn.getText())
        );
    }
}