package library.model;

/**
 * Global configurable borrowing and business rules.
 */
public class LibraryConfiguration {
    private int maxActiveLoans = 5;
    private int defaultLoanDurationDays = 14;
    private int maxRenewals = 2;
    private int renewalPeriodDays = 14;
    private int maxActiveHolds = 3;
    private int holdExpiryDays = 3;
    private String systemName = "City Central Library";

    public LibraryConfiguration() {
    }

    public LibraryConfiguration(int maxActiveLoans, int defaultLoanDurationDays, 
                                int maxRenewals, int renewalPeriodDays, 
                                int maxActiveHolds, int holdExpiryDays, 
                                String systemName) {
        this.maxActiveLoans = maxActiveLoans > 0 ? maxActiveLoans : 5;
        this.defaultLoanDurationDays = defaultLoanDurationDays > 0 ? defaultLoanDurationDays : 14;
        this.maxRenewals = maxRenewals >= 0 ? maxRenewals : 2;
        this.renewalPeriodDays = renewalPeriodDays > 0 ? renewalPeriodDays : 14;
        this.maxActiveHolds = maxActiveHolds > 0 ? maxActiveHolds : 3;
        this.holdExpiryDays = holdExpiryDays > 0 ? holdExpiryDays : 3;
        this.systemName = systemName != null ? systemName : "City Central Library";
    }

    public int getMaxActiveLoans() {
        return maxActiveLoans;
    }

    public void setMaxActiveLoans(int maxActiveLoans) {
        if (maxActiveLoans > 0) this.maxActiveLoans = maxActiveLoans;
    }

    public int getDefaultLoanDurationDays() {
        return defaultLoanDurationDays;
    }

    public void setDefaultLoanDurationDays(int defaultLoanDurationDays) {
        if (defaultLoanDurationDays > 0) this.defaultLoanDurationDays = defaultLoanDurationDays;
    }

    public int getMaxRenewals() {
        return maxRenewals;
    }

    public void setMaxRenewals(int maxRenewals) {
        if (maxRenewals >= 0) this.maxRenewals = maxRenewals;
    }

    public int getRenewalPeriodDays() {
        return renewalPeriodDays;
    }

    public void setRenewalPeriodDays(int renewalPeriodDays) {
        if (renewalPeriodDays > 0) this.renewalPeriodDays = renewalPeriodDays;
    }

    public int getMaxActiveHolds() {
        return maxActiveHolds;
    }

    public void setMaxActiveHolds(int maxActiveHolds) {
        if (maxActiveHolds > 0) this.maxActiveHolds = maxActiveHolds;
    }

    public int getHoldExpiryDays() {
        return holdExpiryDays;
    }

    public void setHoldExpiryDays(int holdExpiryDays) {
        if (holdExpiryDays > 0) this.holdExpiryDays = holdExpiryDays;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        if (systemName != null && !systemName.isBlank()) this.systemName = systemName;
    }
}
