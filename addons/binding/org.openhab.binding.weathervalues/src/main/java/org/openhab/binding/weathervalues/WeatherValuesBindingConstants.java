/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.weathervalues;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link WeatherValuesBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Thomas Traunbauer - Initial contribution
 */
public class WeatherValuesBindingConstants {

    public static final String BINDING_ID = "weathervalues";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");

    public static final String DEVICE_PARAMETER_HOST = "DEVICE_HOST";
    public static final String DEVICE_PARAMETER_DB_NAME = "DEVICE_DB_NAME";
    public static final String DEVICE_PARAMETER_REFRESH = "REFRESH_INTERVAL";

    // List of all Channel ids
    public final static String CHANNEL_BAROMETER = "barometer";
    public final static String CHANNEL_OUTSIDE_TEMPERATURE = "outsideTemperature";
    public final static String CHANNEL_OUTSIDE_TEMPERATURE_DAY_MIN = "outsideTemperatureDayMin";
    public final static String CHANNEL_OUTSIDE_TEMPERATURE_DAY_MIN_TIME = "outsideTemperatureDayMinTime";
    public final static String CHANNEL_OUTSIDE_TEMPERATURE_DAY_MAX = "outsideTemperatureDayMax";
    public final static String CHANNEL_OUTSIDE_TEMPERATURE_DAY_MAX_TIME = "outsideTemperatureDayMaxTime";
    public final static String CHANNEL_OUTSIDE_HUMIDITY = "outsideHumidity";
    public final static String CHANNEL_WIND_SPEED = "windSpeed";
    public final static String CHANNEL_WIND_DIRECTION = "windDirection";
    public final static String CHANNEL_RAIN_RATE = "rainRate";
    public final static String CHANNEL_RAIN_CURRENT_DAY = "rainCurrentDay";
    public final static String CHANNEL_RAIN_CURRENT_WEEK = "rainCurrentWeek";
    public final static String CHANNEL_RAIN_CURRENT_MONTH = "rainCurrentMonth";
    public final static String CHANNEL_RAIN_CURRENT_YEAR = "rainCurrentYear";
    public final static String CHANNEL_RAIN_MONTH1 = "rainJanuary";
    public final static String CHANNEL_RAIN_MONTH2 = "rainFebruary";
    public final static String CHANNEL_RAIN_MONTH3 = "rainMarch";
    public final static String CHANNEL_RAIN_MONTH4 = "rainApril";
    public final static String CHANNEL_RAIN_MONTH5 = "rainMay";
    public final static String CHANNEL_RAIN_MONTH6 = "rainJune";
    public final static String CHANNEL_RAIN_MONTH7 = "rainJuly";
    public final static String CHANNEL_RAIN_MONTH8 = "rainAugust";
    public final static String CHANNEL_RAIN_MONTH9 = "rainSeptember";
    public final static String CHANNEL_RAIN_MONTH10 = "rainOctober";
    public final static String CHANNEL_RAIN_MONTH11 = "rainNovember";
    public final static String CHANNEL_RAIN_MONTH12 = "rainDecember";

}
