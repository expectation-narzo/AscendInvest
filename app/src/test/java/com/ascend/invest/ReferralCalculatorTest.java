package com.ascend.invest;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.ascend.invest.utils.ReferralCalculator;

public class ReferralCalculatorTest {

    @Test
    public void testGetDefaultPercentage() {
        // Level 1: 10%
        assertEquals(10.0, ReferralCalculator.getDefaultPercentage(1), 0.001);
        // Level 5: 2%
        assertEquals(2.0, ReferralCalculator.getDefaultPercentage(5), 0.001);
        // Level 15: 1%
        assertEquals(1.0, ReferralCalculator.getDefaultPercentage(15), 0.001);
        // Invalid Level: 0%
        assertEquals(0.0, ReferralCalculator.getDefaultPercentage(25), 0.001);
    }

    @Test
    public void testCalculateCommission() {
        // $1000 at Level 1 (10%) should be $100
        assertEquals(100.0, ReferralCalculator.calculateCommission(1000.0, 1), 0.001);
        // $1000 at Level 2 (7%) should be $70
        assertEquals(70.0, ReferralCalculator.calculateCommission(1000.0, 2), 0.001);
    }
}
