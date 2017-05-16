/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.weathervalues.internal;

/**
 * The {@link Converter}
 *
 * @author Thomas Traunbauer - Initial contribution
 */
public class Converter {

    public static double inchOfHG_to_Millibar(double value) {
        return (1000.0 / 29.5299830714) * value;
    }

    public static double inch_to_Millimeter(double value) {
        return value * 25.4;
    }

    public static double inchPerHour_to_MillimeterPerHour(double value) {
        return inch_to_Millimeter(value);
    }

    public static double fahrenheit_to_Celsius(double value) {
        return (5.0 / 9.0) * (value - 32);
    }

    public static double knoten_to_kmh(double value) {
        return knoten_to_ms(value) * 3.6;
    }

    public static double knoten_to_ms(double value) {
        return value * 0.514444;
    }

    public static Time seconds_to_Time(long value) {
        return new Time(value * 1000);
    }

    public static long min_to_Milliseconds(int value) {
        return value * 60 * 1000;
    }

    public static long hour_to_Milliseconds(int value) {
        return value * 60 * 60 * 1000;
    }
}
