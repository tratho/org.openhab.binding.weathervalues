/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.weathervalues.internal;

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

    public static WindDirection grad_to_windDirection(double value) {
        WindDirection windDir;

        if (value == 360) {
            value = 0;
        }
        if (value >= 0 && value < 45) {
            windDir = WindDirection.North;
        } else if (value >= 45 && value < 90) {
            windDir = WindDirection.NorthEast;
        } else if (value >= 90 && value < 135) {
            windDir = WindDirection.East;
        } else if (value >= 135 && value < 180) {
            windDir = WindDirection.SouthEast;
        } else if (value >= 180 && value < 225) {
            windDir = WindDirection.South;
        } else if (value >= 225 && value < 270) {
            windDir = WindDirection.SouthWest;
        } else if (value >= 270 && value < 315) {
            windDir = WindDirection.West;
        } else if (value >= 315 && value < 360) {
            windDir = WindDirection.NorthWest;
        } else {
            windDir = WindDirection.Unknown;
        }
        return windDir;
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

    public static void main(String[] args) {
        System.out.println(seconds_to_Time(1481037311));
        System.out.println(seconds_to_Time(1474495200));
    }

}
