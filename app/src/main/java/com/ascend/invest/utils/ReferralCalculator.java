package com.ascend.invest.utils;

/**
 * Utility class to handle referral and commission logic.
 * Decoupled from Android/Firebase for unit testing purposes.
 */
public class ReferralCalculator {

    /**
     * Returns the default commission percentage for a given level.
     * 
     * @param level The referral depth level (1 to 20).
     * @return The commission percentage (0.0 to 100.0).
     */
    public static double getDefaultPercentage(int level) {
        if (level == 1) return 10.0;
        if (level == 2) return 7.0;
        if (level == 3) return 5.0;
        if (level == 4) return 3.0;
        if (level >= 5 && level <= 10) return 2.0;
        if (level >= 11 && level <= 20) return 1.0;
        return 0.0;
    }

    /**
     * Calculates the commission amount based on investment and level.
     */
    public static double calculateCommission(double investmentAmount, int level) {
        double percentage = getDefaultPercentage(level);
        return (investmentAmount * percentage) / 100.0;
    }
}
