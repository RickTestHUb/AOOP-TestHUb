package test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import model.Attendance;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * JUnit 5 test class for Attendance model
 * Tests all functionality including validation, calculations, and edge cases
 */
@DisplayName("Attendance Model Tests")
class AttendanceModelTest {

    private Attendance attendance;

    @BeforeEach
    void setUp() {
        attendance = new Attendance();
    }

    @Test
    @DisplayName("Should create valid attendance record")
    void testCreateValidAttendance() {
        // Arrange
        int employeeId = 10001;
        Date date = Date.valueOf(LocalDate.now());
        Time logIn = Time.valueOf(LocalTime.of(8, 0));
        Time logOut = Time.valueOf(LocalTime.of(17, 0));

        // Act
        attendance.setEmployeeId(employeeId);
        attendance.setDate(date);
        attendance.setLogIn(logIn);
        attendance.setLogOut(logOut);

        // Assert
        assertAll("Valid attendance creation",
            () -> assertEquals(employeeId, attendance.getEmployeeId()),
            () -> assertEquals(date, attendance.getDate()),
            () -> assertEquals(logIn, attendance.getLogIn()),
            () -> assertEquals(logOut, attendance.getLogOut()),
            () -> assertTrue(attendance.isPresent()),
            () -> assertEquals(9.0, attendance.getWorkHours(), 0.01)
        );
    }

    @Test
    @DisplayName("Should throw exception for invalid employee ID")
    void testInvalidEmployeeIdThrowsException() {
        assertAll("Invalid employee ID tests",
            () -> assertThrows(IllegalArgumentException.class, 
                () -> attendance.setEmployeeId(0)),
            () -> assertThrows(IllegalArgumentException.class, 
                () -> attendance.setEmployeeId(-1))
        );
    }

    @Test
    @DisplayName("Should throw exception for null date")
    void testNullDateThrowsException() {
        assertThrows(IllegalArgumentException.class, 
            () -> attendance.setDate(null));
    }

    @Test
    @DisplayName("Should throw exception for invalid time sequence")
    void testInvalidTimeSequenceThrowsException() {
        // Arrange
        attendance.setEmployeeId(10001);
        attendance.setDate(Date.valueOf(LocalDate.now()));
        attendance.setLogIn(Time.valueOf(LocalTime.of(17, 0))); // Later time

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> attendance.setLogOut(Time.valueOf(LocalTime.of(8, 0)))); // Earlier time
    }

    @Test
    @DisplayName("Should correctly detect late arrival")
    void testLateArrivalDetection() {
        // Arrange
        attendance.setEmployeeId(10001);
        attendance.setDate(Date.valueOf(LocalDate.now()));
        attendance.setLogIn(Time.valueOf(LocalTime.of(8, 30))); // 30 minutes late
        attendance.setLogOut(Time.valueOf(LocalTime.of(17, 0)));

        // Act & Assert
        assertTrue(attendance.isLate());
        assertEquals(30.0, attendance.getLateMinutes(), 0.01);
    }

    @Test
    @DisplayName("Should correctly detect undertime")
    void testUndertimeDetection() {
        // Arrange
        attendance.setEmployeeId(10001);
        attendance.setDate(Date.valueOf(LocalDate.now()));
        attendance.setLogIn(Time.valueOf(LocalTime.of(8, 0)));
        attendance.setLogOut(Time.valueOf(LocalTime.of(16, 30))); // 30 minutes early

        // Act & Assert
        assertTrue(attendance.hasUndertime());
        assertEquals(30.0, attendance.getUndertimeMinutes(), 0.01);
    }

    @Test
    @DisplayName("Should correctly calculate work hours")
    void testWorkHoursCalculation() {
        // Arrange
        attendance.setEmployeeId(10001);
        attendance.setDate(Date.valueOf(LocalDate.now()));
        attendance.setLogIn(Time.valueOf(LocalTime.of(8, 0)));
        attendance.setLogOut(Time.valueOf(LocalTime.of(17, 30))); // 9.5 hours

        // Act
        double workHours = attendance.getWorkHours();

        // Assert
        assertEquals(9.5, workHours, 0.01);
    }

    @Test
    @DisplayName("Should detect full day work")
    void testFullDayDetection() {
        // Arrange
        attendance.setEmployeeId(10001);
        attendance.setDate(Date.valueOf(LocalDate.now()));
        attendance.setLogIn(Time.valueOf(LocalTime.of(8, 0)));
        attendance.setLogOut(Time.valueOf(LocalTime.of(17, 0))); // 9 hours (>= 8)

        // Act & Assert
        assertTrue(attendance.isFullDay());
    }

    @Test
    @DisplayName("Should handle null logout time")
    void testNullLogoutTime() {
        // Arrange
        attendance.setEmployeeId(10001);
        attendance.setDate(Date.valueOf(LocalDate.now()));
        attendance.setLogIn(Time.valueOf(LocalTime.of(8, 0)));
        attendance.setLogOut(null);

        // Act & Assert
        assertAll("Null logout handling",
            () -> assertTrue(attendance.isPresent()),
            () -> assertFalse(attendance.hasUndertime()),
            () -> assertEquals(0.0, attendance.getWorkHours(), 0.01)
        );
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void testEqualsAndHashCode() {
        // Arrange
        Attendance attendance1 = new Attendance();
        attendance1.setId(1);
        attendance1.setEmployeeId(10001);
        attendance1.setDate(Date.valueOf(LocalDate.now()));

        Attendance attendance2 = new Attendance();
        attendance2.setId(1);
        attendance2.setEmployeeId(10001);
        attendance2.setDate(Date.valueOf(LocalDate.now()));

        // Act & Assert
        assertEquals(attendance1, attendance2);
        assertEquals(attendance1.hashCode(), attendance2.hashCode());
    }

    @Test
    @DisplayName("Should test toString method")
    void testToString() {
        // Arrange
        attendance.setId(1);
        attendance.setEmployeeId(10001);
        attendance.setDate(Date.valueOf(LocalDate.now()));
        attendance.setLogIn(Time.valueOf(LocalTime.of(8, 0)));
        attendance.setLogOut(Time.valueOf(LocalTime.of(17, 0)));

        // Act
        String result = attendance.toString();

        // Assert
        assertAll("ToString validation",
            () -> assertNotNull(result),
            () -> assertTrue(result.contains("Attendance")),
            () -> assertTrue(result.contains("10001")),
            () -> assertTrue(result.contains("9.00"))
        );
    }
}