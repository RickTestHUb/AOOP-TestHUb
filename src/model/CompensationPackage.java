package model;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

/**
 * CompensationPackage class demonstrating COMPOSITION and AGGREGATION
 * This class aggregates different types of allowances and benefits
 * Addresses mentor feedback about implementing proper OOP principles
 */
public class CompensationPackage {
    private int packageId;
    private int employeeId;
    private String packageName;
    private LocalDate effectiveDate;
    private boolean isActive;
    
    // COMPOSITION: CompensationPackage "has-a" collection of allowances
    private List<Allowance> allowances;
    private List<Deduction> deductions;
    
    // AGGREGATION: References to other objects
    private Employee employee;
    private GovernmentContributions governmentContributions;
    
    // Package totals
    private double totalAllowances;
    private double totalDeductions;
    private double netCompensation;
    
    // Constructors
    public CompensationPackage() {
        this.allowances = new ArrayList<>();
        this.deductions = new ArrayList<>();
        this.effectiveDate = LocalDate.now();
        this.isActive = true;
    }
    
    public CompensationPackage(int employeeId, String packageName) {
        this();
        this.employeeId = employeeId;
        this.packageName = packageName;
    }
    
    public CompensationPackage(Employee employee, String packageName) {
        this(employee.getEmployeeId(), packageName);
        this.employee = employee;
        initializeStandardPackage();
    }
    
    /**
     * Initialize standard compensation package based on employee status
     * Demonstrates POLYMORPHISM through different package configurations
     */
    private void initializeStandardPackage() {
        if (employee == null) return;
        
        // Add allowances based on employee eligibility (POLYMORPHISM)
        addAllowance(new RiceAllowance(employeeId));
        
        if (employee.isRegularEmployee()) {
            addAllowance(new PhoneAllowance(employeeId, 1000.0));
            addAllowance(new ClothingAllowance(employeeId));
        } else if ("Probationary".equals(employee.getStatus())) {
            addAllowance(new PhoneAllowance(employeeId, 500.0)); // Reduced phone allowance
        }
        
        // Initialize government contributions
        this.governmentContributions = new GovernmentContributions(
            employeeId, 0.0, 0.0, 0.0, 0.0);
        
        calculateTotals();
    }
    
    /**
     * Add allowance to the package (COMPOSITION)
     */
    public void addAllowance(Allowance allowance) {
        if (allowance != null && allowance.isEligible(employee)) {
            allowances.add(allowance);
            calculateTotals();
        }
    }
    
    /**
     * Remove allowance from the package
     */
    public boolean removeAllowance(Allowance allowance) {
        boolean removed = allowances.remove(allowance);
        if (removed) {
            calculateTotals();
        }
        return removed;
    }
    
    /**
     * Add deduction to the package (COMPOSITION)
     */
    public void addDeduction(Deduction deduction) {
        if (deduction != null) {
            deductions.add(deduction);
            calculateTotals();
        }
    }
    
    /**
     * Remove deduction from the package
     */
    public boolean removeDeduction(Deduction deduction) {
        boolean removed = deductions.remove(deduction);
        if (removed) {
            calculateTotals();
        }
        return removed;
    }
    
    /**
     * Calculate all totals for the compensation package
     * Demonstrates POLYMORPHISM through different calculation methods
     */
    public void calculateTotals() {
        // Calculate total allowances using polymorphic method calls
        totalAllowances = allowances.stream()
            .mapToDouble(Allowance::getCalculatedAmount)
            .sum();
        
        // Calculate total deductions using polymorphic method calls
        totalDeductions = deductions.stream()
            .mapToDouble(Deduction::getAmount)
            .sum();
        
        // Add government contributions if available
        if (governmentContributions != null) {
            totalDeductions += governmentContributions.getTotalContributions();
        }
        
        // Calculate net compensation
        netCompensation = (employee != null ? employee.getBasicSalary() : 0.0) 
            + totalAllowances - totalDeductions;
    }
    
    /**
     * Get allowances by type (demonstrates POLYMORPHISM)
     */
    public List<Allowance> getAllowancesByType(String type) {
        return allowances.stream()
            .filter(allowance -> type.equals(allowance.getType()))
            .toList();
    }
    
    /**
     * Get deductions by type (demonstrates POLYMORPHISM)
     */
    public List<Deduction> getDeductionsByType(String type) {
        return deductions.stream()
            .filter(deduction -> type.equals(deduction.getType()))
            .toList();
    }
    
    /**
     * Check if package is eligible for bonuses
     */
    public boolean isEligibleForBonus() {
        return employee != null && employee.canReceiveBonus() && isActive;
    }
    
    /**
     * Calculate bonus amount based on package configuration
     */
    public double calculateBonusAmount() {
        if (!isEligibleForBonus()) return 0.0;
        
        // Base bonus calculation
        double baseBonus = employee.getBasicSalary() * 0.1; // 10% of basic salary
        
        // Add allowance-based bonus
        double allowanceBonus = totalAllowances * 0.05; // 5% of total allowances
        
        return baseBonus + allowanceBonus;
    }
    
    /**
     * Generate compensation summary report
     */
    public String generateSummaryReport() {
        StringBuilder report = new StringBuilder();
        
        report.append("=== COMPENSATION PACKAGE SUMMARY ===\n");
        report.append("Employee: ").append(employee != null ? employee.getFullName() : "Unknown").append("\n");
        report.append("Package: ").append(packageName).append("\n");
        report.append("Effective Date: ").append(effectiveDate).append("\n");
        report.append("Status: ").append(isActive ? "Active" : "Inactive").append("\n\n");
        
        report.append("ALLOWANCES:\n");
        for (Allowance allowance : allowances) {
            report.append(String.format("- %s: ₱%.2f%s\n", 
                allowance.getType(), 
                allowance.getAmount(),
                allowance.isTaxable() ? " (Taxable)" : ""));
        }
        report.append(String.format("Total Allowances: ₱%.2f\n\n", totalAllowances));
        
        report.append("DEDUCTIONS:\n");
        for (Deduction deduction : deductions) {
            report.append(String.format("- %s: ₱%.2f\n", 
                deduction.getType(), 
                deduction.getAmount()));
        }
        
        if (governmentContributions != null) {
            report.append(String.format("- SSS: ₱%.2f\n", governmentContributions.getSss()));
            report.append(String.format("- PhilHealth: ₱%.2f\n", governmentContributions.getPhilhealth()));
            report.append(String.format("- Pag-IBIG: ₱%.2f\n", governmentContributions.getPagibig()));
            report.append(String.format("- Tax: ₱%.2f\n", governmentContributions.getTax()));
        }
        
        report.append(String.format("Total Deductions: ₱%.2f\n\n", totalDeductions));
        
        report.append("SUMMARY:\n");
        report.append(String.format("Basic Salary: ₱%.2f\n", 
            employee != null ? employee.getBasicSalary() : 0.0));
        report.append(String.format("Total Allowances: ₱%.2f\n", totalAllowances));
        report.append(String.format("Total Deductions: ₱%.2f\n", totalDeductions));
        report.append(String.format("Net Compensation: ₱%.2f\n", netCompensation));
        
        if (isEligibleForBonus()) {
            report.append(String.format("Potential Bonus: ₱%.2f\n", calculateBonusAmount()));
        }
        
        return report.toString();
    }
    
    /**
     * Validate the compensation package
     */
    public boolean isValid() {
        return employeeId > 0 && 
               packageName != null && !packageName.trim().isEmpty() &&
               effectiveDate != null &&
               totalAllowances >= 0 &&
               totalDeductions >= 0;
    }
    
    /**
     * Clone the compensation package for a different employee
     */
    public CompensationPackage cloneForEmployee(Employee newEmployee) {
        CompensationPackage clonedPackage = new CompensationPackage(newEmployee, this.packageName);
        
        // Clone allowances if eligible
        for (Allowance allowance : this.allowances) {
            if (allowance.isEligible(newEmployee)) {
                // Create new allowance instance based on type
                if (allowance instanceof RiceAllowance) {
                    clonedPackage.addAllowance(new RiceAllowance(newEmployee.getEmployeeId()));
                } else if (allowance instanceof PhoneAllowance) {
                    clonedPackage.addAllowance(new PhoneAllowance(newEmployee.getEmployeeId(), allowance.getAmount()));
                } else if (allowance instanceof ClothingAllowance) {
                    clonedPackage.addAllowance(new ClothingAllowance(newEmployee.getEmployeeId()));
                }
            }
        }
        
        return clonedPackage;
    }
    
    // Getters and Setters
    public int getPackageId() { return packageId; }
    public void setPackageId(int packageId) { this.packageId = packageId; }
    
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { 
        this.employee = employee;
        if (employee != null) {
            this.employeeId = employee.getEmployeeId();
        }
    }
    
    public GovernmentContributions getGovernmentContributions() { return governmentContributions; }
    public void setGovernmentContributions(GovernmentContributions governmentContributions) { 
        this.governmentContributions = governmentContributions;
        calculateTotals();
    }
    
    public List<Allowance> getAllowances() { return new ArrayList<>(allowances); }
    public List<Deduction> getDeductions() { return new ArrayList<>(deductions); }
    
    public double getTotalAllowances() { return totalAllowances; }
    public double getTotalDeductions() { return totalDeductions; }
    public double getNetCompensation() { return netCompensation; }
    
    public int getAllowanceCount() { return allowances.size(); }
    public int getDeductionCount() { return deductions.size(); }
    
    @Override
    public String toString() {
        return String.format("CompensationPackage{id=%d, employee=%s, package='%s', net=₱%.2f}", 
            packageId, 
            employee != null ? employee.getFullName() : "Unknown",
            packageName, 
            netCompensation);
    }
}