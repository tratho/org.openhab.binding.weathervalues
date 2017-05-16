/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.weathervalues.internal;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * The {@link OutdoorClimate}
 *
 * @author Thomas Traunbauer - Initial contribution
 */
public class OutdoorClimate {

    private Double temperature;
    private Double temperatureExtra;
    private Double humidity;
    private Double humidityExtra;
    private Double barometer;

    private Double temperatureDayMin;
    private Double temperatureDayMax;
    private Time temperatureDayMinTime;
    private Time temperatureDayMaxTime;

    public OutdoorClimate(Double temperature, Double temperatureExtra, Double humidity, Double humidityExtra,
            Double barometer, Double temperatureDayMin, Double temperatureDayMax, Time temperatureDayMinTime,
            Time temperatureDayMaxTime) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.temperatureExtra = temperatureExtra;
        this.humidityExtra = humidityExtra;
        this.barometer = barometer;
        this.temperatureDayMin = temperatureDayMin;
        this.temperatureDayMax = temperatureDayMax;
        this.temperatureDayMinTime = temperatureDayMinTime;
        this.temperatureDayMaxTime = temperatureDayMaxTime;
    }

    @Override
    public int hashCode() {
        Double hashValue = 0.0;
        if (temperature != null) {
            hashValue += temperature;
        }
        if (temperatureExtra != null) {
            hashValue += temperatureExtra;
        }
        if (humidity != null) {
            hashValue += humidity;
        }
        if (humidityExtra != null) {
            hashValue += humidityExtra;
        }
        if (barometer != null) {
            hashValue += barometer;
        }
        if (temperatureDayMin != null) {
            hashValue += temperatureDayMin;
        }
        if (temperatureDayMax != null) {
            hashValue += temperatureDayMax;
        }
        return hashValue.hashCode();
    }

    public State getTemperature() {
        if (temperature != null) {
            double value = Utility.round(temperature);
            return new DecimalType(value);
        } else {
            if (temperatureExtra != null) {
                double value = Utility.round(temperatureExtra);
                return new DecimalType(value);
            }
        }
        return UnDefType.NULL;
    }

    public State getHumidity() {
        if (humidity != null) {
            double value = Utility.round(humidity);
            return new DecimalType(value);
        } else {
            if (humidityExtra != null) {
                double value = Utility.round(humidityExtra);
                return new DecimalType(value);
            }
        }
        return UnDefType.NULL;
    }

    public State getBarometer() {
        if (barometer != null) {
            double value = Utility.round(barometer);
            return new DecimalType(value);
        }
        return UnDefType.NULL;
    }

    public State getTemperatureMinimum() {
        if (temperatureDayMin != null) {
            double value = Utility.round(temperatureDayMin);
            return new DecimalType(value);
        }
        return UnDefType.NULL;
    }

    public State getTemperatureMaximum() {
        if (temperatureDayMax != null) {
            double value = Utility.round(temperatureDayMax);
            return new DecimalType(value);
        }
        return UnDefType.NULL;
    }

    public State getTemperatureMinimumTime() {
        if (temperatureDayMinTime != null) {
            return temperatureDayMinTime.getDateTimeType();
        }
        return UnDefType.NULL;
    }

    public State getTemperatureMaximumTime() {
        if (temperatureDayMaxTime != null) {
            return temperatureDayMaxTime.getDateTimeType();
        }
        return UnDefType.NULL;
    }
}
