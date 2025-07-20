package test;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoExtensions;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dao.*;
import model.*;
import service.PayrollCalculator;
import java.time.LocalDate;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

/**
 * JUnit 5 test class for PayrollCalculator
 * Tests payroll calculation logic with mocked dependencies
 */
@ExtendWith(MockitoExtensions.class)
@DisplayName("Payroll Calculator Tests")
class PayrollCalculatorTest {

    @Mock
    private EmployeeDAO mockEmployeeDAO;
    
    @Mock
    private AttendanceDAO mockAttendanceDAO;
    
    @Mock
    private LeaveRequestDAO mockLeaveRequestDAO;
    
    @Mock
    private OvertimeDAO mockOvertimeDAO;

    private PayrollCalculator payrollCalculator;
    private Employee testEmployee;
    private LocalDate periodStart;
    private LocalDate periodEnd;

    @BeforeEach
    void setUp() {
        payrollCalculator = new PayrollCalculator();
        
        // Create test employee
        testEmployee = new Employee();
        testEmployee.setEmployeeId(10001);
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setBasicSalary(50000.0);
        testEmployee.setRiceSubsidy(1500.0);
        testEmployee.setPhoneAllowance(1000.0);
        testEmployee.setClothingAllowance(800.0);
        
        // Set test period
        periodStart = LocalDate.of(2024, 6, 1);
        periodEnd = LocalDate.of(2024, 6, 30);
    }

    @Test
    @DisplayName("Should calculate basic payroll correctly")
    void testBasicPayrollCalculation() throws Exception {
        // Arrange
        List<Attendance> attendanceList = createMockAttendanceList(22); // 22 working days
        
        when(mockEmployeeDAO.getEmployeeById(10001)).thenReturn(testEmployee);
        when(mockAttendanceDAO.getAttendanceByEmployeeIdBetweenDates(10001, periodStart, periodEnd))
            .thenReturn(attendanceList);
        when(mockOvertimeDAO.getOvertimeByEmployeeIdAndDateRange(10001, periodStart, periodEnd))
            .thenReturn(new ArrayList<>());
        when(mockLeaveRequestDAO.getApprovedLeavesByEmployeeIdAndDateRange(10001, periodStart, periodEnd))
            .thenReturn(new ArrayList<>());

        // Act
        Payroll result = payrollCalculator.calculatePayroll(10001, periodStart, periodEnd);

        // Assert
        assertAll("Basic payroll calculation",
            () -> assertNotNull(result),
            () -> assertEquals(10001, result.getEmployeeId()),
            () -> assertEquals(22, result.getDaysWorked()),
            () -> assertEquals(50000.0, result.getMonthlyRate(), 0.01),
            () -> assertTrue(result.getGrossPay() > 0),
            () -> assertTrue(result.getNetPay() > 0),
            () -> assertEquals(1500.0, result.getRiceSubsidy(), 0.01),
            () -> assertEquals(1000.0, result.getPhoneAllowance(), 0.01),
            () -> assertEquals(800.0, result.getClothingAllowance(), 0.01)
        );
    }

    @Test
    @DisplayName("Should throw exception for invalid employee ID")
    void testInvalidEmployeeIdThrowsException() {
        // Act & Assert
        assertThrows(PayrollCalculator.PayrollCalculationException.class,
            () -> payrollCalculator.calculatePayroll(0, periodStart, periodEnd));
    }

    @Test
    @DisplayName("Should throw exception for null dates")
    void testNullDatesThrowException() {
        // Act & Assert
        assertAll("Null date validation",
            () -> assertThrows(PayrollCalculator.PayrollCalculationException.class,
                () -> payrollCalculator.calculatePayroll(10001, null, periodEnd)),
            () -> assertThrows(PayrollCalculator.PayrollCalculationException.class,
                () -> payrollCalculator.calculatePayroll(10001, periodStart, null))
        );
    }

    @Test
    @DisplayName("Should throw exception for invalid date range")
    void testInvalidDateRangeThrowsException() {
        // Arrange
        LocalDate invalidEnd = periodStart.minusDays(1);

        // Act & Assert
        assertThrows(PayrollCalculator.PayrollCalculationException.class,
            () -> payrollCalculator.calculatePayroll(10001, periodStart, invalidEnd));
    }

    @Test
    @DisplayName("Should calculate overtime pay correctly")
    void testOvertimePayCalculation() throws Exception {
        // Arrange
        List<Attendance> attendanceList = createMockAttendanceList(20);
        List<Overtime> overtimeList = createMockOvertimeList(10.0); // 10 hours overtime
        
        when(mockEmployeeDAO.getEmployeeById(10001)).thenReturn(testEmployee);
        when(mockAttendanceDAO.getAttendanceByEmployeeIdBetweenDates(10001, periodStart, periodEnd))
            .thenReturn(attendanceList);
        when(mockOvertimeDAO.getOvertimeByEmployeeIdAndDateRange(10001, periodStart, periodEnd))
            .thenReturn(overtimeList);
        when(mockLeaveRequestDAO.getApprovedLeavesByEmployeeIdAndDateRange(10001, periodStart, periodEnd))
            .thenReturn(new ArrayList<>());

        // Act
        Payroll result = payrollCalculator.calculatePayroll(10001, periodStart, periodEnd);

        // Assert
        assertAll("Overtime calculation",
            () -> assertEquals(10.0, result.getTotalOvertimeHours(), 0.01),
            () -> assertTrue(result.getOvertimePay() > 0),
            () -> assertTrue(result.getGrossPay() > result.getGrossEarnings()) // Gross pay includes overtime
        );
    }

    @Test
    @DisplayName("Should handle employee not found")
    void testEmployeeNotFound() {
        // Arrange
        when(mockEmployeeDAO.getEmployeeById(99999)).thenReturn(null);

        // Act & Assert
        assertThrows(PayrollCalculator.PayrollCalculationException.class,
            () -> payrollCalculator.calculatePayroll(99999, periodStart, periodEnd));
    }

    @Test
    @DisplayName("Should calculate government contributions correctly")
    void testGovernmentContributionsCalculation() throws Exception {
        // Arrange
        List<Attendance> attendanceList = createMockAttendanceList(22);
        
        when(mockEmployeeDAO.getEmployeeById(10001)).thenReturn(testEmployee);
        when(mockAttendanceDAO.getAttendanceByEmployeeIdBetweenDates(10001, periodStart, periodEnd))
            .thenReturn(attendanceList);
        when(mockOvertimeDAO.getOvertimeByEmployeeIdAndDateRange(10001, periodStart, periodEnd))
            .thenReturn(new ArrayList<>());
        when(mockLeaveRequestDAO.getApprovedLeavesByEmployeeIdAndDateRange(10001, periodStart, periodEnd))
            .thenReturn(new ArrayList<>());

        // Act
        Payroll result = payrollCalculator.calculatePayroll(10001, periodStart, periodEnd);

        // Assert
        assertAll("Government contributions",
            () -> assertTrue(result.getSss() > 0, "SSS contribution should be calculated"),
            () -> assertTrue(result.getPhilhealth() > 0, "PhilHealth contribution should be calculated"),
            () -> assertTrue(result.getPagibig() > 0, "Pag-IBIG contribution should be calculated"),
            () -> assertTrue(result.getTax() >= 0, "Tax should be calculated"),
            () -> assertTrue(result.getTotalDeductions() > 0, "Total deductions should include government contributions")
        );
    }

    // Helper methods
    private List<Attendance> createMockAttendanceList(int days) {
        List<Attendance> attendanceList = new ArrayList<>();
        LocalDate currentDate = periodStart;
        
        for (int i = 0; i < days && !currentDate.isAfter(periodEnd); i++) {
            Attendance attendance = new Attendance();
            attendance.setEmployeeId(10001);
            attendance.setDate(Date.valueOf(currentDate));
            attendance.setLogIn(Time.valueOf(LocalTime.of(8, 0)));
            attendance.setLogOut(Time.valueOf(LocalTime.of(17, 0)));
            attendanceList.add(attendance);
            
            currentDate = currentDate.plusDays(1);
            // Skip weekends
            if (currentDate.getDayOfWeek().getValue() > 5) {
                currentDate = currentDate.plusDays(2);
            }
        }
        
        return attendanceList;
    }

    private List<Overtime> createMockOvertimeList(double totalHours) {
        List<Overtime> overtimeList = new ArrayList<>();
        Overtime overtime = new Overtime();
        overtime.setEmployeeId(10001);
        overtime.setDate(Date.valueOf(periodStart));
        overtime.setHours(totalHours);
        overtime.setApproved(true);
        overtimeList.add(overtime);
        return overtimeList;
    }
}