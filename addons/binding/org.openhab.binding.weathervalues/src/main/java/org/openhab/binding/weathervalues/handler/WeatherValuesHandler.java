/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.weathervalues.handler;

import static org.openhab.binding.weathervalues.WeatherValuesBindingConstants.*;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.weathervalues.internal.SQLReaderListener;
import org.openhab.binding.weathervalues.internal.SQLiteReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WeatherValuesHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Thomas Traunbauer - Initial contribution
 */
public class WeatherValuesHandler extends BaseThingHandler implements SQLReaderListener {

    private Logger logger = LoggerFactory.getLogger(WeatherValuesHandler.class);

    private SQLiteReader sqliteReader;

    public WeatherValuesHandler(Thing thing) {
        super(thing);
    }

    /**
     * Returns the IP Address of this device
     *
     * @return the IP Address of this device
     */
    public String getIPAddress() {
        return (String) getThing().getConfiguration().getProperties().get(DEVICE_PARAMETER_HOST);
    }

    /**
     * Returns the database name of the sql database
     *
     * @return the database name of the sql database
     */
    public String getDBName() {
        return (String) getThing().getConfiguration().getProperties().get(DEVICE_PARAMETER_DB_NAME);
    }

    public long getRefreshInterval() {
        long timeInMintues = ((BigDecimal) thing.getConfiguration().get(DEVICE_PARAMETER_REFRESH)).longValue();
        long timeInSeconds = timeInMintues * 60;
        return timeInSeconds;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        try {
            sqliteReader = new SQLiteReader(getIPAddress(), getDBName());
            sqliteReader.addListener(this);
            scheduler.scheduleWithFixedDelay(sqliteReader, 1, getRefreshInterval(), TimeUnit.SECONDS);
            updateStatus(ThingStatus.ONLINE);
        } catch (ClassNotFoundException e) {
            logger.error("Error during loading drivers for database");
            sqliteReader.removeListener(this);
            sqliteReader = null;
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void refreshValues() {
        updateState(CHANNEL_BAROMETER, sqliteReader.getBarometer());
        updateState(CHANNEL_OUTSIDE_TEMPERATURE, sqliteReader.getOutdoorTemperature());
        updateState(CHANNEL_OUTSIDE_TEMP_DAY_MIN, sqliteReader.getOutdoorTemperatureCurrentDayMin());
        updateState(CHANNEL_OUTSIDE_TEMP_DAY_MIN_TIME, sqliteReader.getOutdoorTemperatureCurrentDayMinTime());
        updateState(CHANNEL_OUTSIDE_TEMP_DAY_MAX, sqliteReader.getOutdoorTemperatureCurrentDayMax());
        updateState(CHANNEL_OUTSIDE_TEMP_DAY_MAX_TIME, sqliteReader.getOutdoorTemperatureCurrentDayMaxTime());
        updateState(CHANNEL_OUTSIDE_HUMIDITY, sqliteReader.getOutdoorHumidity());
        updateState(CHANNEL_WIND_SPEED, sqliteReader.getWindSpeed());
        updateState(CHANNEL_WIND_DIRECTION, sqliteReader.getWindDirection());
        updateState(CHANNEL_RAIN_RATE, sqliteReader.getRainRate());
        updateState(CHANNEL_RAIN_CURRENT_DAY, sqliteReader.getRainCurrentDay());
        updateState(CHANNEL_RAIN_CURRENT_WEEK, sqliteReader.getRainCurrentWeek());
        updateState(CHANNEL_RAIN_CURRENT_MONTH, sqliteReader.getRainCurrentMonth());
        updateState(CHANNEL_RAIN_CURRENT_YEAR, sqliteReader.getRainCurrentYear());
        updateState(CHANNEL_RAIN_MONTH1, sqliteReader.getRainMonth(1));
        updateState(CHANNEL_RAIN_MONTH2, sqliteReader.getRainMonth(2));
        updateState(CHANNEL_RAIN_MONTH3, sqliteReader.getRainMonth(3));
        updateState(CHANNEL_RAIN_MONTH4, sqliteReader.getRainMonth(4));
        updateState(CHANNEL_RAIN_MONTH5, sqliteReader.getRainMonth(5));
        updateState(CHANNEL_RAIN_MONTH6, sqliteReader.getRainMonth(6));
        updateState(CHANNEL_RAIN_MONTH7, sqliteReader.getRainMonth(7));
        updateState(CHANNEL_RAIN_MONTH8, sqliteReader.getRainMonth(8));
        updateState(CHANNEL_RAIN_MONTH9, sqliteReader.getRainMonth(9));
        updateState(CHANNEL_RAIN_MONTH10, sqliteReader.getRainMonth(10));
        updateState(CHANNEL_RAIN_MONTH11, sqliteReader.getRainMonth(11));
        updateState(CHANNEL_RAIN_MONTH12, sqliteReader.getRainMonth(12));
    }

}
