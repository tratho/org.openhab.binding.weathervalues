package org.openhab.binding.weathervalues.internal;

public class Utility {
    public static double round(double value) {
        int numberOfFractionDigits = 2;
        long factor = (long) Math.pow(10, numberOfFractionDigits);
        value *= factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
