/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.weathervalues.internal.data;

import java.time.Month;
import java.util.List;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.weathervalues.internal.Time;

/**
 * The {@link Rain}
 *
 * @author Thomas Traunbauer - Initial contribution
 */
public class Rain {

    // current
    private Double rainRateCurrent;
    private Double rainCurrentDay;
    private Double rainCurrentWeek;
    private List<Double> listOfRainPerMonth;

    public Rain(Double rainRateCurrent, Double rainCurrentDay, Double rainCurrentWeek,
            List<Double> listOfRainPerMonth) {
        this.rainRateCurrent = rainRateCurrent;
        this.rainCurrentDay = rainCurrentDay;
        this.rainCurrentWeek = rainCurrentWeek;
        this.listOfRainPerMonth = listOfRainPerMonth;
    }

    public State getRainRateCurrent() {
        if (rainRateCurrent != null) {
            double value = Utility.round(rainRateCurrent);
            return new DecimalType(value);
        }
        return UnDefType.NULL;
    }

    public State getRainCurrentDay() {
        if (rainCurrentDay != null) {
            double value = Utility.round(rainCurrentDay);
            return new DecimalType(value);
        }
        return UnDefType.NULL;
    }

    public State getRainCurrentWeek() {
        if (rainCurrentWeek != null) {
            double value = Utility.round(rainCurrentWeek);
            return new DecimalType(value);
        }
        return UnDefType.NULL;
    }

    public State getRainCurrentMonth() {
        return getRainMonth(new Time().getMonth());
    }

    public State getRainCurrentYear() {
        boolean checker = false;

        double rainCurrentYear = 0.0;
        for (int month = 0; month < 12; month++) {
            Double rainMonth = listOfRainPerMonth.get(month);
            if (rainMonth != null) {
                rainCurrentYear += listOfRainPerMonth.get(month);
                checker = true;
            }
        }
        if (checker) {
            double value = Utility.round(rainCurrentYear);
            return new DecimalType(value);
        } else {
            return UnDefType.NULL;
        }
    }

    public State getRainMonth(Month month) {
        int index = month.getValue() - 1;
        if (listOfRainPerMonth.get(index) != null) {
            double value = Utility.round(listOfRainPerMonth.get(index));
            return new DecimalType(value);
        }
        return UnDefType.NULL;
    }
}
