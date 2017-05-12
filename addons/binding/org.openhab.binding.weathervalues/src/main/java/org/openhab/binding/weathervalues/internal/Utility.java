package org.openhab.binding.weathervalues.internal;

import java.text.NumberFormat;

public class Utility {
    public static double round(double d) {
        return round(d, 2);
    }

    public static double round(double d, int numberOfFractionDigits) {
        NumberFormat n = NumberFormat.getInstance();
        n.setMaximumFractionDigits(numberOfFractionDigits);
        String roundedString = n.format(d);
        if (roundedString.contains(",")) {
            roundedString = roundedString.replaceAll(",", ".");
        }
        return Double.parseDouble(roundedString);
    }
}
