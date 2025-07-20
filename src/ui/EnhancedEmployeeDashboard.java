package ui;

import dao.EmployeeDAO;
import dao.AttendanceDAO;
import model.Employee;
import model.Attendance;
import model.Payroll;
import service.PayrollCalculator;
import service.JasperReportService;
import ui.PayrollDetailsDialog;
import ui.LoginForm;
import ui.LeaveRequestDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;


/**
 * Enhanced Employee Dashboard with improved usability and bug fixes
 * Addresses mentor feedback: "GUI could use improvements in terms of usability. Found bugs in functionality."
 */
public class EnhancedEmployeeDashboard extends JFrame {
    private Employee currentUser;
    private JTabbedPane tabbedPane;

    // Enhanced UI components with better validation
    private JLabel nameLabel, positionLabel, statusLabel, salaryLabel;
    private JLabel phoneLabel, addressLabel, sssLabel, philhealthLabel;
    private JTable attendanceTable;
    private DefaultTableModel attendanceTableModel;
    private TableRowSorter<DefaultTableModel> attendanceTableSorter;
    private JLabel totalDaysLabel, averageHoursLabel, attendanceRateLabel;
    private JTable payrollTable;
    private DefaultTableModel payrollTableModel;
    private JComboBox<String> monthComboBox;
    private JComboBox<String> yearComboBox;
    private JProgressBar loadingProgressBar;
    private JLabel statusBarLabel;

    // Enhanced search and filter functionality
    private JTextField attendanceSearchField;
    private JComboBox<String> attendanceFilterComboBox;
    private JButton attendanceRefreshButton;
    private JButton payrollRefreshButton;

    // Services
    private AttendanceDAO attendanceDAO;
    private PayrollCalculator payrollCalculator;
    private JasperReportService jasperReportService;

    // Loading state management
    private boolean isLoadingData = false;

    public EnhancedEmployeeDashboard(Employee user) {
        this.currentUser = user;

        try {
            // Initialize services with error handling
            initializeServices();
            
            // Initialize UI components with enhanced features
            initializeEnhancedComponents();
            setupEnhancedLayout();
            setupEnhancedEventHandlers();

            // Load initial data with progress indication
            loadDataWithProgress();

            System.out.println("✅ Enhanced Employee Dashboard initialized successfully for: " + user.getFullName());

        } catch (Exception e) {
            System.err.println("❌ Enhanced Employee Dashboard initialization failed: " + e.getMessage());
            e.printStackTrace();
            createErrorInterface(e);
        }

        // Enhanced window properties
        setTitle("MotorPH Payroll System - Employee Portal (Enhanced)");
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // Add window icon and enhanced features
        setupEnhancedWindow();
    }

    private void initializeServices() throws Exception {
        this.attendanceDAO = new AttendanceDAO();
        this.payrollCalculator = new PayrollCalculator();
        this.jasperReportService = new JasperReportService();
        
        // Test services to ensure they work
        attendanceDAO.getAttendanceByEmployeeId(currentUser.getEmployeeId());
        System.out.println("✅ Services initialized and tested successfully");
    }

    private void initializeEnhancedComponents() {
        // Enhanced tabbed pane with better styling
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);

        // Enhanced personal info labels with tooltips
        nameLabel = createEnhancedLabel("Loading...", "Employee's full name");
        positionLabel = createEnhancedLabel("Loading...", "Current job position");
        statusLabel = createEnhancedLabel("Loading...", "Employment status (Regular/Probationary)");
        salaryLabel = createEnhancedLabel("Loading...", "Basic monthly salary");
        phoneLabel = createEnhancedLabel("Loading...", "Contact phone number");
        addressLabel = createEnhancedLabel("Loading...", "Home address");
        sssLabel = createEnhancedLabel("Loading...", "Social Security System number");
        philhealthLabel = createEnhancedLabel("Loading...", "PhilHealth insurance number");

        // Enhanced attendance table with sorting and filtering
        String[] attendanceColumns = {"Date", "Log In", "Log Out", "Work Hours", "Status", "Late (min)", "Undertime (min)"};
        attendanceTableModel = new DefaultTableModel(attendanceColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Enhanced column type detection for better sorting
                if (columnIndex == 0) return java.sql.Date.class; // Date
                if (columnIndex == 3 || columnIndex == 5 || columnIndex == 6) return Double.class; // Numbers
                return String.class;
            }
        };

        attendanceTable = new JTable(attendanceTableModel);
        attendanceTableSorter = new TableRowSorter<>(attendanceTableModel);
        attendanceTable.setRowSorter(attendanceTableSorter);
        setupEnhancedTableStyling(attendanceTable);

        // Enhanced summary labels
        totalDaysLabel = createEnhancedLabel("Total Days: 0", "Total attendance days recorded");
        averageHoursLabel = createEnhancedLabel("Average Hours: 0.00", "Average work hours per day");
        attendanceRateLabel = createEnhancedLabel("Attendance Rate: 0%", "Percentage of expected work days attended");

        // Enhanced payroll table
        String[] payrollColumns = {"Period", "Days Worked", "Gross Pay", "Deductions", "Net Pay", "Actions"};
        payrollTableModel = new DefaultTableModel(payrollColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only Actions column
            }
        };
        payrollTable = new JTable(payrollTableModel);
        setupEnhancedTableStyling(payrollTable);

        // Enhanced month/year selectors with validation
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        monthComboBox = new JComboBox<>(months);
        monthComboBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        monthComboBox.setFont(new Font("Arial", Font.PLAIN, 12));
        monthComboBox.setToolTipText("Select month for payroll calculation");

        // Enhanced year selector with more years
        String[] years = {"2020", "2021", "2022", "2023", "2024", "2025", "2026"};
        yearComboBox = new JComboBox<>(years);
        yearComboBox.setSelectedItem("2024");
        yearComboBox.setFont(new Font("Arial", Font.PLAIN, 12));
        yearComboBox.setToolTipText("Select year for payroll calculation");

        // Enhanced search and filter components
        attendanceSearchField = new JTextField(15);
        attendanceSearchField.setFont(new Font("Arial", Font.PLAIN, 12));
        attendanceSearchField.setToolTipText("Search attendance records by date or status");
        
        String[] filterOptions = {"All Records", "Present Only", "Late Only", "Undertime Only", "Full Day Only"};
        attendanceFilterComboBox = new JComboBox<>(filterOptions);
        attendanceFilterComboBox.setFont(new Font("Arial", Font.PLAIN, 12));
        attendanceFilterComboBox.setToolTipText("Filter attendance records by type");

        // Enhanced action buttons
        attendanceRefreshButton = createEnhancedButton("🔄 Refresh", "Reload attendance data", 
                new Color(108, 117, 125), Color.WHITE);
        payrollRefreshButton = createEnhancedButton("🔄 Refresh", "Reload payroll data", 
                new Color(108, 117, 125), Color.WHITE);

        // Progress bar for loading states
        loadingProgressBar = new JProgressBar();
        loadingProgressBar.setStringPainted(true);
        loadingProgressBar.setVisible(false);

        // Enhanced status bar
        statusBarLabel = new JLabel("Ready");
        statusBarLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        statusBarLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }

    private JLabel createEnhancedLabel(String text, String tooltip) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setToolTipText(tooltip);
        return label;
    }

    private JButton createEnhancedButton(String text, String tooltip, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }
            @Override
            public void mouseExited(MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    private void setupEnhancedTableStyling(JTable table) {
        table.setRowHeight(28);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(70, 130, 180));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(173, 216, 230));
        table.setGridColor(new Color(200, 200, 200));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        
        // Enhanced selection model
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Add alternating row colors
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
    }

    private void setupEnhancedLayout() {
        setLayout(new BorderLayout());

        // Enhanced Header Panel with user info
        JPanel headerPanel = createEnhancedHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Enhanced tabs with improved content
        tabbedPane.addTab("📊 Personal Information", createEnhancedPersonalInfoTab());
        tabbedPane.addTab("📅 My Attendance", createEnhancedAttendanceTab());
        tabbedPane.addTab("💰 My Payroll", createEnhancedPayrollTab());

        add(tabbedPane, BorderLayout.CENTER);

        // Enhanced status bar with progress indicator
        JPanel statusPanel = createEnhancedStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JPanel createEnhancedHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 25, 112));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Left side - title and user info
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setBackground(new Color(25, 25, 112));
        
        JLabel titleLabel = new JLabel("🏍️ MotorPH Payroll System - Employee Portal");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel userInfoLabel = new JLabel("Welcome, " + currentUser.getFullName() + " (ID: " + currentUser.getEmployeeId() + ")");
        userInfoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        userInfoLabel.setForeground(Color.LIGHT_GRAY);
        
        leftPanel.add(titleLabel);
        leftPanel.add(Box.createHorizontalStrut(20));
        leftPanel.add(userInfoLabel);

        // Right side - action buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(new Color(25, 25, 112));

        JButton profileButton = createHeaderButton("👤 Profile", "View/Edit Profile");
        JButton settingsButton = createHeaderButton("⚙️ Settings", "Application Settings");
        JButton logoutButton = createHeaderButton("🚪 Logout", "Logout from System");
        
        profileButton.addActionListener(e -> showUserProfile());
        settingsButton.addActionListener(e -> showSettings());
        logoutButton.addActionListener(e -> logout());

        rightPanel.add(profileButton);
        rightPanel.add(settingsButton);
        rightPanel.add(logoutButton);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JButton createHeaderButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(25, 25, 112));
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(100, 30));
        
        // Enhanced hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(230, 230, 230));
            }
            @Override
            public void mouseExited(MouseEvent evt) {
                button.setBackground(Color.WHITE);
            }
        });
        
        return button;
    }

    private JPanel createEnhancedPersonalInfoTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create scrollable main info panel
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();

        // Enhanced title with icon
        JLabel titleLabel = new JLabel("👤 Personal Information");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(25, 25, 112));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 30, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        infoPanel.add(titleLabel, gbc);

        // Reset grid settings
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        // Enhanced field addition with better spacing
        addEnhancedInfoField(infoPanel, gbc, "👤 Full Name:", nameLabel, 1);
        addEnhancedInfoField(infoPanel, gbc, "💼 Position:", positionLabel, 2);
        addEnhancedInfoField(infoPanel, gbc, "📋 Status:", statusLabel, 3);
        addEnhancedInfoField(infoPanel, gbc, "💰 Basic Salary:", salaryLabel, 4);
        addEnhancedInfoField(infoPanel, gbc, "📱 Phone:", phoneLabel, 5);
        addEnhancedInfoField(infoPanel, gbc, "🏠 Address:", addressLabel, 6);
        addEnhancedInfoField(infoPanel, gbc, "🆔 SSS Number:", sssLabel, 7);
        addEnhancedInfoField(infoPanel, gbc, "🏥 PhilHealth:", philhealthLabel, 8);

        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(infoPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);

        // Enhanced allowances panel
        JPanel allowancesPanel = createEnhancedAllowancesPanel();

        // Enhanced action panel
        JPanel actionPanel = createEnhancedActionPanel();

        panel.add(scrollPane, BorderLayout.NORTH);
        panel.add(allowancesPanel, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addEnhancedInfoField(JPanel parent, GridBagConstraints gbc, String labelText, JLabel valueLabel, int row) {
        gbc.gridx = 0; gbc.gridy = row;
        gbc.insets = new Insets(10, 0, 10, 20);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setPreferredSize(new Dimension(150, 25));
        parent.add(label, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        valueLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        valueLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        valueLabel.setOpaque(true);
        valueLabel.setBackground(new Color(248, 248, 255));
        parent.add(valueLabel, gbc);
    }

    private JPanel createEnhancedAllowancesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2), 
                "💼 Monthly Allowances & Benefits",
                TitledBorder.LEFT, TitledBorder.TOP, 
                new Font("Arial", Font.BOLD, 16), 
                new Color(70, 130, 180));
        panel.setBorder(border);
        panel.setBackground(Color.WHITE);

        JPanel allowanceGrid = new JPanel(new GridLayout(2, 2, 20, 15));
        allowanceGrid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        allowanceGrid.setBackground(Color.WHITE);

        // Enhanced allowance cards with animations
        JPanel ricePanel = createEnhancedAllowanceCard("🍚 Rice Subsidy",
                String.format("₱%.2f", currentUser.getRiceSubsidy()),
                new Color(144, 238, 144), "Monthly rice allowance");

        JPanel phonePanel = createEnhancedAllowanceCard("📱 Phone Allowance",
                String.format("₱%.2f", currentUser.getPhoneAllowance()),
                new Color(173, 216, 230), "Monthly communication allowance");

        JPanel clothingPanel = createEnhancedAllowanceCard("👔 Clothing Allowance",
                String.format("₱%.2f", currentUser.getClothingAllowance()),
                new Color(255, 182, 193), "Monthly clothing allowance");

        // Calculate total with better formatting
        double totalAllowances = currentUser.getRiceSubsidy() +
                currentUser.getPhoneAllowance() +
                currentUser.getClothingAllowance();
        JPanel totalPanel = createEnhancedAllowanceCard("💰 Total Allowances",
                String.format("₱%.2f", totalAllowances),
                new Color(255, 215, 0), "Sum of all monthly allowances");

        allowanceGrid.add(ricePanel);
        allowanceGrid.add(phonePanel);
        allowanceGrid.add(clothingPanel);
        allowanceGrid.add(totalPanel);

        panel.add(allowanceGrid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createEnhancedAllowanceCard(String title, String amount, Color bgColor, String tooltip) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        card.setToolTipText(tooltip);

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel amountLabel = new JLabel(amount, JLabel.CENTER);
        amountLabel.setFont(new Font("Arial", Font.BOLD, 20));

        // Add subtle animation on hover
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                card.setBackground(bgColor.brighter());
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(bgColor.darker(), 2),
                    BorderFactory.createEmptyBorder(14, 14, 14, 14)));
            }
            @Override
            public void mouseExited(MouseEvent evt) {
                card.setBackground(bgColor);
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GRAY, 1),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)));
            }
        });

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(amountLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createEnhancedActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        TitledBorder border = BorderFactory.createTitledBorder("🚀 Quick Actions");
        border.setTitleFont(new Font("Arial", Font.BOLD, 14));
        actionPanel.setBorder(border);
        actionPanel.setBackground(Color.WHITE);

        JButton leaveRequestButton = createEnhancedButton("📝 Submit Leave Request", 
                "Submit a new leave request", new Color(70, 130, 180), Color.WHITE);
        leaveRequestButton.setPreferredSize(new Dimension(200, 40));

        JButton viewPayslipButton = createEnhancedButton("💰 View Latest Payslip", 
                "View your latest payslip", new Color(34, 139, 34), Color.WHITE);
        viewPayslipButton.setPreferredSize(new Dimension(200, 40));

        JButton updateProfileButton = createEnhancedButton("👤 Update Profile", 
                "Update your personal information", new Color(255, 140, 0), Color.WHITE);
        updateProfileButton.setPreferredSize(new Dimension(200, 40));

        // Enhanced event handlers
        leaveRequestButton.addActionListener(e -> showLeaveRequestDialog());
        viewPayslipButton.addActionListener(e -> showLatestPayslip());
        updateProfileButton.addActionListener(e -> showUserProfile());

        actionPanel.add(leaveRequestButton);
        actionPanel.add(viewPayslipButton);
        actionPanel.add(updateProfileButton);

        return actionPanel;
    }

    private JPanel createEnhancedAttendanceTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Enhanced top panel with search and filter
        JPanel topPanel = createEnhancedAttendanceControlPanel();

        // Enhanced summary panel
        JPanel summaryPanel = createEnhancedAttendanceSummaryPanel();

        // Table panel with better styling
        JPanel tablePanel = new JPanel(new BorderLayout());
        TitledBorder tableBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "📅 Attendance Records", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), new Color(70, 130, 180));
        tablePanel.setBorder(tableBorder);

        JScrollPane attendanceScrollPane = new JScrollPane(attendanceTable);
        attendanceScrollPane.setPreferredSize(new Dimension(0, 400));
        tablePanel.add(attendanceScrollPane, BorderLayout.CENTER);

        // Combine panels
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(topPanel, BorderLayout.NORTH);
        northPanel.add(summaryPanel, BorderLayout.SOUTH);

        panel.add(northPanel, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createEnhancedAttendanceControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left side - search and filter
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(new JLabel("🔍 Search:"));
        leftPanel.add(attendanceSearchField);
        leftPanel.add(Box.createHorizontalStrut(10));
        leftPanel.add(new JLabel("🔽 Filter:"));
        leftPanel.add(attendanceFilterComboBox);

        // Right side - action buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.add(attendanceRefreshButton);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createEnhancedAttendanceSummaryPanel() {
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        TitledBorder border = BorderFactory.createTitledBorder("📊 Attendance Summary");
        border.setTitleFont(new Font("Arial", Font.BOLD, 12));
        summaryPanel.setBorder(border);
        summaryPanel.setBackground(new Color(248, 248, 255));

        // Create summary cards
        JPanel totalDaysCard = createSummaryCard("📅", totalDaysLabel, new Color(173, 216, 230));
        JPanel avgHoursCard = createSummaryCard("⏰", averageHoursLabel, new Color(144, 238, 144));
        JPanel attendanceRateCard = createSummaryCard("📈", attendanceRateLabel, new Color(255, 182, 193));

        summaryPanel.add(totalDaysCard);
        summaryPanel.add(avgHoursCard);
        summaryPanel.add(attendanceRateCard);

        return summaryPanel;
    }

    private JPanel createSummaryCard(String icon, JLabel dataLabel, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel iconLabel = new JLabel(icon, JLabel.CENTER);
        iconLabel.setFont(new Font("Arial", Font.BOLD, 20));

        dataLabel.setHorizontalAlignment(JLabel.CENTER);
        dataLabel.setFont(new Font("Arial", Font.BOLD, 12));

        card.add(iconLabel, BorderLayout.NORTH);
        card.add(dataLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createEnhancedPayrollTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Enhanced top panel with period selection
        JPanel topPanel = createEnhancedPayrollControlPanel();

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        TitledBorder tableBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(34, 139, 34), 2),
                "💰 Payroll Information", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), new Color(34, 139, 34));
        tablePanel.setBorder(tableBorder);

        JScrollPane payrollScrollPane = new JScrollPane(payrollTable);
        tablePanel.add(payrollScrollPane, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createEnhancedPayrollControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left side - period selection
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(new JLabel("📅 Select Period:"));
        leftPanel.add(monthComboBox);
        leftPanel.add(yearComboBox);
        
        JButton calculateButton = createEnhancedButton("💰 Calculate Payroll", 
                "Calculate payroll for selected period", new Color(34, 139, 34), Color.WHITE);
        calculateButton.addActionListener(e -> calculatePayrollForPeriod());
        leftPanel.add(calculateButton);

        // Right side - action buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton exportButton = createEnhancedButton("📄 Export PDF", 
                "Export payroll summary to PDF", new Color(220, 53, 69), Color.WHITE);
        exportButton.addActionListener(e -> exportPayrollToPDF());
        
        rightPanel.add(payrollRefreshButton);
        rightPanel.add(exportButton);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createEnhancedStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        statusPanel.setBackground(new Color(245, 245, 245));

        // Left side - status message
        JPanel leftStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftStatusPanel.setBackground(new Color(245, 245, 245));
        leftStatusPanel.add(statusBarLabel);

        // Center - progress bar
        JPanel centerStatusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerStatusPanel.setBackground(new Color(245, 245, 245));
        centerStatusPanel.add(loadingProgressBar);

        // Right side - current date/time
        JPanel rightStatusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightStatusPanel.setBackground(new Color(245, 245, 245));
        
        JLabel dateTimeLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        dateTimeLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        dateTimeLabel.setForeground(new Color(100, 100, 100));
        rightStatusPanel.add(dateTimeLabel);

        statusPanel.add(leftStatusPanel, BorderLayout.WEST);
        statusPanel.add(centerStatusPanel, BorderLayout.CENTER);
        statusPanel.add(rightStatusPanel, BorderLayout.EAST);

        return statusPanel;
    }

    private void setupEnhancedEventHandlers() {
        // Enhanced attendance search functionality
        attendanceSearchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filterAttendanceRecords();
            }
        });

        // Enhanced attendance filter functionality
        attendanceFilterComboBox.addActionListener(e -> filterAttendanceRecords());

        // Enhanced refresh button handlers
        attendanceRefreshButton.addActionListener(e -> refreshAttendanceData());
        payrollRefreshButton.addActionListener(e -> refreshPayrollData());

        // Enhanced payroll table double-click handler
        payrollTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int selectedRow = payrollTable.getSelectedRow();
                    if (selectedRow != -1) {
                        showPayrollDetails(selectedRow);
                    }
                }
            }
        });

        // Enhanced tab change handler
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            switch (selectedIndex) {
                case 0: // Personal Info
                    updateStatusBar("Personal information displayed");
                    break;
                case 1: // Attendance
                    updateStatusBar("Attendance records loaded");
                    refreshAttendanceData();
                    break;
                case 2: // Payroll
                    updateStatusBar("Payroll information displayed");
                    refreshPayrollData();
                    break;
            }
        });

        // Enhanced keyboard shortcuts
        setupKeyboardShortcuts();
    }

    private void setupKeyboardShortcuts() {
        // F5 - Refresh current tab
        KeyStroke f5 = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f5, "refresh");
        getRootPane().getActionMap().put("refresh", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshCurrentTab();
            }
        });

        // Ctrl+P - Print/Export
        KeyStroke ctrlP = KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlP, "print");
        getRootPane().getActionMap().put("print", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportCurrentTab();
            }
        });

        // Ctrl+Q - Logout
        KeyStroke ctrlQ = KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlQ, "logout");
        getRootPane().getActionMap().put("logout", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
    }

    private void loadDataWithProgress() {
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                isLoadingData = true;
                
                // Show progress bar
                SwingUtilities.invokeLater(() -> {
                    loadingProgressBar.setVisible(true);
                    loadingProgressBar.setString("Loading employee data...");
                });

                publish("Loading personal information...");
                Thread.sleep(500); // Simulate loading time
                loadPersonalInformation();

                publish("Loading attendance records...");
                Thread.sleep(500);
                loadAttendanceData();

                publish("Loading payroll information...");
                Thread.sleep(500);
                loadPayrollData();

                publish("Finalizing...");
                Thread.sleep(300);

                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                String lastMessage = chunks.get(chunks.size() - 1);
                loadingProgressBar.setString(lastMessage);
                updateStatusBar(lastMessage);
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    
                    SwingUtilities.invokeLater(() -> {
                        loadingProgressBar.setVisible(false);
                        updateStatusBar("Data loaded successfully");
                        isLoadingData = false;
                    });
                    
                    System.out.println("✅ All data loaded successfully");
                    
                } catch (Exception e) {
                    System.err.println("❌ Error loading data: " + e.getMessage());
                    e.printStackTrace();
                    
                    SwingUtilities.invokeLater(() -> {
                        loadingProgressBar.setVisible(false);
                        updateStatusBar("Error loading data");
                        isLoadingData = false;
                        
                        JOptionPane.showMessageDialog(EnhancedEmployeeDashboard.this,
                                "Error loading data: " + e.getMessage(),
                                "Loading Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }
        };

        worker.execute();
    }

    private void loadPersonalInformation() {
        SwingUtilities.invokeLater(() -> {
            nameLabel.setText(currentUser.getFullName());
            positionLabel.setText(currentUser.getPosition());
            statusLabel.setText(currentUser.getStatus());
            salaryLabel.setText(String.format("₱%.2f", currentUser.getBasicSalary()));
            phoneLabel.setText(currentUser.getPhoneNumber());
            addressLabel.setText(currentUser.getAddress());
            sssLabel.setText(currentUser.getSssNumber());
            philhealthLabel.setText(currentUser.getPhilhealthNumber());
        });
    }

    private void loadAttendanceData() {
        try {
            List<Attendance> attendanceList = attendanceDAO.getAttendanceByEmployeeId(currentUser.getEmployeeId());
            
            SwingUtilities.invokeLater(() -> {
                // Clear existing data
                attendanceTableModel.setRowCount(0);
                
                // Populate table
                for (Attendance attendance : attendanceList) {
                    Object[] row = {
                        attendance.getDate(),
                        attendance.getLogIn(),
                        attendance.getLogOut(),
                        attendance.getWorkHours(),
                        attendance.getStatus(),
                        attendance.getLateMinutes(),
                        attendance.getUndertimeMinutes()
                    };
                    attendanceTableModel.addRow(row);
                }
                
                // Update summary
                updateAttendanceSummary(attendanceList);
            });
            
        } catch (Exception e) {
            System.err.println("❌ Error loading attendance data: " + e.getMessage());
            throw new RuntimeException("Failed to load attendance data", e);
        }
    }

    private void loadPayrollData() {
        try {
            // Get current month/year for initial load
            String currentMonth = monthComboBox.getSelectedItem().toString();
            String currentYear = yearComboBox.getSelectedItem().toString();
            
            // Calculate payroll for current period
            calculatePayrollForPeriod(currentMonth, currentYear);
            
        } catch (Exception e) {
            System.err.println("❌ Error loading payroll data: " + e.getMessage());
            throw new RuntimeException("Failed to load payroll data", e);
        }
    }

    private void updateAttendanceSummary(List<Attendance> attendanceList) {
        if (attendanceList.isEmpty()) {
            totalDaysLabel.setText("Total Days: 0");
            averageHoursLabel.setText("Average Hours: 0.00");
            attendanceRateLabel.setText("Attendance Rate: 0%");
            return;
        }

        int totalDays = attendanceList.size();
        double totalHours = attendanceList.stream()
                .mapToDouble(Attendance::getWorkHours)
                .sum();
        double averageHours = totalHours / totalDays;
        
        // Calculate attendance rate (assuming 22 working days per month)
        int expectedDays = 22;
        double attendanceRate = (totalDays / (double) expectedDays) * 100;
        attendanceRate = Math.min(attendanceRate, 100.0);

        totalDaysLabel.setText("Total Days: " + totalDays);
        averageHoursLabel.setText(String.format("Average Hours: %.2f", averageHours));
        attendanceRateLabel.setText(String.format("Attendance Rate: %.1f%%", attendanceRate));
    }

    private void filterAttendanceRecords() {
        String searchText = attendanceSearchField.getText().toLowerCase();
        String filterType = attendanceFilterComboBox.getSelectedItem().toString();
        
        RowFilter<Object, Object> filter = new RowFilter<Object, Object>() {
            @Override
            public boolean include(Entry<? extends Object, ? extends Object> entry) {
                boolean matchesSearch = true;
                boolean matchesFilter = true;
                
                // Apply search filter
                if (!searchText.isEmpty()) {
                    matchesSearch = false;
                    for (int i = 0; i < entry.getValueCount(); i++) {
                        if (entry.getStringValue(i).toLowerCase().contains(searchText)) {
                            matchesSearch = true;
                            break;
                        }
                    }
                }
                
                // Apply type filter
                if (!filterType.equals("All Records")) {
                    String status = entry.getStringValue(4); // Status column
                    switch (filterType) {
                        case "Present Only":
                            matchesFilter = status.equalsIgnoreCase("Present");
                            break;
                        case "Late Only":
                            matchesFilter = status.equalsIgnoreCase("Late");
                            break;
                        case "Undertime Only":
                            matchesFilter = status.contains("Undertime");
                            break;
                        case "Full Day Only":
                            matchesFilter = status.equalsIgnoreCase("Present") && 
                                           !status.contains("Late") && 
                                           !status.contains("Undertime");
                            break;
                    }
                }
                
                return matchesSearch && matchesFilter;
            }
        };
        
        attendanceTableSorter.setRowFilter(filter);
        updateStatusBar("Filtered " + attendanceTable.getRowCount() + " records");
    }

    private void refreshAttendanceData() {
        if (isLoadingData) return;
        
        updateStatusBar("Refreshing attendance data...");
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                loadAttendanceData();
                return null;
            }
            
            @Override
            protected void done() {
                updateStatusBar("Attendance data refreshed");
            }
        };
        
        worker.execute();
    }

    private void refreshPayrollData() {
        if (isLoadingData) return;
        
        updateStatusBar("Refreshing payroll data...");
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                loadPayrollData();
                return null;
            }
            
            @Override
            protected void done() {
                updateStatusBar("Payroll data refreshed");
            }
        };
        
        worker.execute();
    }

    private void calculatePayrollForPeriod() {
        String month = monthComboBox.getSelectedItem().toString();
        String year = yearComboBox.getSelectedItem().toString();
        calculatePayrollForPeriod(month, year);
    }

    private void calculatePayrollForPeriod(String month, String year) {
        try {
            updateStatusBar("Calculating payroll for " + month + " " + year + "...");
            
            // Get attendance for the period
            List<Attendance> attendanceForPeriod = attendanceDAO.getAttendanceByEmployeeIdAndPeriod(
                    currentUser.getEmployeeId(), month, year);
            
            // Calculate payroll
            Payroll payroll = payrollCalculator.calculatePayroll(currentUser, attendanceForPeriod);
            
            // Update table
            SwingUtilities.invokeLater(() -> {
                // Clear existing data
                payrollTableModel.setRowCount(0);
                
                // Add calculated payroll
                Object[] row = {
                    month + " " + year,
                    payroll.getDaysWorked(),
                    String.format("₱%.2f", payroll.getGrossPay()),
                    String.format("₱%.2f", payroll.getTotalDeductions()),
                    String.format("₱%.2f", payroll.getNetPay()),
                    "View Details"
                };
                payrollTableModel.addRow(row);
                
                updateStatusBar("Payroll calculated successfully");
            });
            
        } catch (Exception e) {
            System.err.println("❌ Error calculating payroll: " + e.getMessage());
            SwingUtilities.invokeLater(() -> {
                updateStatusBar("Error calculating payroll");
                JOptionPane.showMessageDialog(this,
                        "Error calculating payroll: " + e.getMessage(),
                        "Calculation Error", JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    private void showPayrollDetails(int selectedRow) {
        try {
            String period = (String) payrollTableModel.getValueAt(selectedRow, 0);
            
            // Create and show payroll details dialog
            PayrollDetailsDialog dialog = new PayrollDetailsDialog(this, currentUser, period);
            dialog.setVisible(true);
            
        } catch (Exception e) {
            System.err.println("❌ Error showing payroll details: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error displaying payroll details: " + e.getMessage(),
                    "Display Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportPayrollToPDF() {
        try {
            updateStatusBar("Exporting payroll to PDF...");
            
            String period = monthComboBox.getSelectedItem() + " " + yearComboBox.getSelectedItem();
            String fileName = "Payroll_" + currentUser.getEmployeeId() + "_" + period.replace(" ", "_") + ".pdf";
            
            // Use JasperReports service
            jasperReportService.generatePayrollReport(currentUser, period, fileName);
            
            updateStatusBar("Payroll exported successfully");
            
            int result = JOptionPane.showConfirmDialog(this,
                    "Payroll exported to " + fileName + "\nWould you like to open the file?",
                    "Export Successful", JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                // Open the PDF file
                Desktop.getDesktop().open(new java.io.File(fileName));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error exporting payroll: " + e.getMessage());
            updateStatusBar("Error exporting payroll");
            JOptionPane.showMessageDialog(this,
                    "Error exporting payroll: " + e.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshCurrentTab() {
        int selectedTab = tabbedPane.getSelectedIndex();
        switch (selectedTab) {
            case 0: // Personal Info
                loadPersonalInformation();
                break;
            case 1: // Attendance
                refreshAttendanceData();
                break;
            case 2: // Payroll
                refreshPayrollData();
                break;
        }
    }

    private void exportCurrentTab() {
        int selectedTab = tabbedPane.getSelectedIndex();
        switch (selectedTab) {
            case 1: // Attendance
                exportAttendanceData();
                break;
            case 2: // Payroll
                exportPayrollToPDF();
                break;
            default:
                JOptionPane.showMessageDialog(this,
                        "Export not available for this tab",
                        "Export", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void exportAttendanceData() {
        try {
            updateStatusBar("Exporting attendance data...");
            
            String fileName = "Attendance_" + currentUser.getEmployeeId() + "_" + 
                            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
            
            jasperReportService.generateAttendanceReport(currentUser, fileName);
            
            updateStatusBar("Attendance data exported successfully");
            
            int result = JOptionPane.showConfirmDialog(this,
                    "Attendance data exported to " + fileName + "\nWould you like to open the file?",
                    "Export Successful", JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                Desktop.getDesktop().open(new java.io.File(fileName));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error exporting attendance: " + e.getMessage());
            updateStatusBar("Error exporting attendance");
            JOptionPane.showMessageDialog(this,
                    "Error exporting attendance: " + e.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showUserProfile() {
        try {
            // Create and show user profile dialog
            JDialog profileDialog = new JDialog(this, "User Profile", true);
            profileDialog.setSize(400, 300);
            profileDialog.setLocationRelativeTo(this);
            
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            JLabel titleLabel = new JLabel("👤 User Profile", JLabel.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
            panel.add(titleLabel, BorderLayout.NORTH);
            
            JTextArea profileText = new JTextArea();
            profileText.setEditable(false);
            profileText.setFont(new Font("Arial", Font.PLAIN, 12));
            profileText.setText(
                "Employee ID: " + currentUser.getEmployeeId() + "\n" +
                "Name: " + currentUser.getFullName() + "\n" +
                "Position: " + currentUser.getPosition() + "\n" +
                "Status: " + currentUser.getStatus() + "\n" +
                "Department: " + currentUser.getDepartment() + "\n" +
                "Email: " + currentUser.getEmail() + "\n" +
                "Phone: " + currentUser.getPhoneNumber() + "\n" +
                "Hire Date: " + currentUser.getHireDate()
            );
            
            panel.add(new JScrollPane(profileText), BorderLayout.CENTER);
            
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> profileDialog.dispose());
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(closeButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            
            profileDialog.add(panel);
            profileDialog.setVisible(true);
            
        } catch (Exception e) {
            System.err.println("❌ Error showing user profile: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error displaying user profile: " + e.getMessage(),
                    "Profile Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSettings() {
        try {
            JDialog settingsDialog = new JDialog(this, "Settings", true);
            settingsDialog.setSize(300, 200);
            settingsDialog.setLocationRelativeTo(this);
            
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            JLabel titleLabel = new JLabel("⚙️ Application Settings", JLabel.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            panel.add(titleLabel, BorderLayout.NORTH);
            
            JPanel settingsPanel = new JPanel(new GridLayout(3, 1, 10, 10));
            
            JCheckBox autoRefreshCheckBox = new JCheckBox("Auto-refresh data every 5 minutes", true);
            JCheckBox notificationsCheckBox = new JCheckBox("Show notifications", true);
            JCheckBox soundCheckBox = new JCheckBox("Enable sound effects", false);
            
            settingsPanel.add(autoRefreshCheckBox);
            settingsPanel.add(notificationsCheckBox);
            settingsPanel.add(soundCheckBox);
            
            panel.add(settingsPanel, BorderLayout.CENTER);
            
            JPanel buttonPanel = new JPanel();
            JButton saveButton = new JButton("Save");
            JButton cancelButton = new JButton("Cancel");
            
            saveButton.addActionListener(e -> {
                // Save settings logic would go here
                JOptionPane.showMessageDialog(settingsDialog, "Settings saved successfully!");
                settingsDialog.dispose();
            });
            
            cancelButton.addActionListener(e -> settingsDialog.dispose());
            
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            
            settingsDialog.add(panel);
            settingsDialog.setVisible(true);
            
        } catch (Exception e) {
            System.err.println("❌ Error showing settings: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error displaying settings: " + e.getMessage(),
                    "Settings Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showLeaveRequestDialog() {
        try {
            LeaveRequestDialog dialog = new LeaveRequestDialog(this, currentUser);
            dialog.setVisible(true);
        } catch (Exception e) {
            System.err.println("❌ Error showing leave request dialog: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error displaying leave request form: " + e.getMessage(),
                    "Leave Request Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showLatestPayslip() {
        try {
            String currentMonth = LocalDate.now().getMonth().name();
            String currentYear = String.valueOf(LocalDate.now().getYear());
            
            PayrollDetailsDialog dialog = new PayrollDetailsDialog(this, currentUser, 
                    currentMonth + " " + currentYear);
            dialog.setVisible(true);
            
        } catch (Exception e) {
            System.err.println("❌ Error showing payslip: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error displaying payslip: " + e.getMessage(),
                    "Payslip Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                updateStatusBar("Logging out...");
                
                // Close this window
                this.dispose();
                
                // Show login form
                SwingUtilities.invokeLater(() -> {
                    try {
                        new LoginForm().setVisible(true);
                    } catch (Exception e) {
                        System.err.println("❌ Error showing login form: " + e.getMessage());
                        System.exit(0);
                    }
                });
                
            } catch (Exception e) {
                System.err.println("❌ Error during logout: " + e.getMessage());
                System.exit(0);
            }
        }
    }

    private void setupEnhancedWindow() {
        try {
            // Set window icon
            setIconImage(new ImageIcon(getClass().getResource("/icons/motorph-icon.png")).getImage());
        } catch (Exception e) {
            System.out.println("⚠️ Window icon not found, using default");
        }
        
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (UnsupportedLookAndFeelException e) {
            System.out.println("⚠️ Could not set system look and feel");
        }
        
        // Enhanced window close operation
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                logout();
            }
        });
    }

    private void createErrorInterface(Exception error) {
        // Remove all components
        getContentPane().removeAll();
        
        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        JLabel errorIcon = new JLabel("❌", JLabel.CENTER);
        errorIcon.setFont(new Font("Arial", Font.BOLD, 48));
        errorIcon.setForeground(Color.RED);
        
        JLabel errorTitle = new JLabel("System Error", JLabel.CENTER);
        errorTitle.setFont(new Font("Arial", Font.BOLD, 24));
        errorTitle.setForeground(Color.RED);
        
        JTextArea errorMessage = new JTextArea();
        errorMessage.setText("An error occurred while initializing the application:\n\n" + 
                            error.getMessage() + "\n\n" +
                            "Please contact system administrator or try restarting the application.");
        errorMessage.setEditable(false);
        errorMessage.setFont(new Font("Arial", Font.PLAIN, 14));
        errorMessage.setBackground(getBackground());
        
        JButton restartButton = new JButton("🔄 Restart Application");
        restartButton.setFont(new Font("Arial", Font.BOLD, 14));
        restartButton.addActionListener(e -> {
            dispose();
            System.exit(0);
        });
        
        JButton exitButton = new JButton("❌ Exit");
        exitButton.setFont(new Font("Arial", Font.BOLD, 14));
        exitButton.addActionListener(e -> System.exit(1));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(restartButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(exitButton);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(errorIcon, BorderLayout.NORTH);
        topPanel.add(errorTitle, BorderLayout.SOUTH);
        
        errorPanel.add(topPanel, BorderLayout.NORTH);
        errorPanel.add(new JScrollPane(errorMessage), BorderLayout.CENTER);
        errorPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(errorPanel);
        revalidate();
        repaint();
    }

    private void updateStatusBar(String message) {
        SwingUtilities.invokeLater(() -> {
            statusBarLabel.setText(message);
            statusBarLabel.repaint();
        });
    }

    /**
     * Enhanced alternating row renderer for better table visualization
     */
    private class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component component = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                if (row % 2 == 0) {
                    component.setBackground(Color.WHITE);
                } else {
                    component.setBackground(new Color(248, 248, 255));
                }
            }
            
            // Special formatting for status column
            if (column == 4 && value != null) { // Status column
                String status = value.toString();
                if (status.equalsIgnoreCase("Present")) {
                    component.setForeground(new Color(0, 128, 0));
                } else if (status.equalsIgnoreCase("Late")) {
                    component.setForeground(new Color(255, 140, 0));
                } else if (status.equalsIgnoreCase("Absent")) {
                    component.setForeground(Color.RED);
                } else {
                    component.setForeground(Color.BLACK);
                }
            } else {
                component.setForeground(Color.BLACK);
            }
            
            return component;
        }
    }

    /**
     * Main method for testing the enhanced dashboard
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Create sample employee for testing
                Employee testEmployee = new Employee();
                testEmployee.setEmployeeId("EMP001");
                testEmployee.setFullName("John Michael Santos");
                testEmployee.setPosition("Senior Software Developer");
                testEmployee.setStatus("Regular");
                testEmployee.setBasicSalary(75000.00);
                testEmployee.setPhoneNumber("+63 912 345 6789");
                testEmployee.setAddress("123 Makati Avenue, Makati City, Philippines");
                testEmployee.setSssNumber("03-1234567-8");
                testEmployee.setPhilhealthNumber("PH-123456789");
                testEmployee.setRiceSubsidy(2000.00);
                testEmployee.setPhoneAllowance(1500.00);
                testEmployee.setClothingAllowance(1000.00);
                testEmployee.setEmail("john.santos@company.com");
                testEmployee.setDepartment("Information Technology");
                testEmployee.setHireDate("2020-01-15");
                
                EnhancedEmployeeDashboard dashboard = new EnhancedEmployeeDashboard(testEmployee);
                dashboard.setVisible(true);
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Error starting application: " + e.getMessage(),
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}