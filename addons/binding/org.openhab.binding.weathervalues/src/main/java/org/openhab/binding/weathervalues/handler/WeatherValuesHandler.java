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
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingFactory;
import org.eclipse.smarthome.core.thing.type.TypeResolver;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.weathervalues.internal.SQLiteReader;
import org.openhab.binding.weathervalues.internal.SQLiteReaderListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WeatherValuesHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Thomas Traunbauer - Initial contribution
 */
public class WeatherValuesHandler extends BaseThingHandler implements SQLiteReaderListener {

    private Logger logger = LoggerFactory.getLogger(WeatherValuesHandler.class);

    private SQLiteReader sqliteReader;

    public WeatherValuesHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
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
        return ((BigDecimal) thing.getConfiguration().get(DEVICE_PARAMETER_REFRESH)).longValue();
    }

    @Override
    public void initialize() {
        try {
            sqliteReader = new SQLiteReader(getIPAddress(), getDBName());
            sqliteReader.addListener(this);
            try {
                sqliteReader.tryConnect();
                updateStatus(ThingStatus.ONLINE);
                scheduler.scheduleWithFixedDelay(sqliteReader, 300, getRefreshInterval(), TimeUnit.MILLISECONDS);
            } catch (SQLException e) {
                logger.error("Error during opening database");
                updateStatus(ThingStatus.OFFLINE);
                sqliteReader.removeListener(this);
                sqliteReader = null;
            }
        } catch (ClassNotFoundException e) {
            logger.error("Error during loading drivers for database");
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    private ChannelUID getChannelUID(String channelId) {
        Channel channel = thing.getChannel(channelId);
        if (channel == null) {
            // refresh thing...
            Thing newThing = ThingFactory.createThing(TypeResolver.resolve(thing.getThingTypeUID()), thing.getUID(),
                    thing.getConfiguration());
            updateThing(newThing);
            channel = thing.getChannel(channelId);
        }
        return channel.getUID();
    }

    @Override
    protected void updateState(String id, State state) {
        super.updateState(getChannelUID(id), state);
    }

    @Override
    public void getUpdate() {
        if (sqliteReader.getBarometer() != null) {
            updateState(CHANNEL_BAROMETER, new DecimalType(sqliteReader.getBarometer()));
        }
        if (sqliteReader.getOutdoorTemperature() != null) {
            updateState(CHANNEL_OUTSIDE_TEMPERATURE, new DecimalType(sqliteReader.getOutdoorTemperature()));
        }
        if (sqliteReader.getOutdoorTemperatureCurrentDayMin() != null) {
            updateState(CHANNEL_OUTSIDE_TEMPERATURE_DAY_MIN,
                    new DecimalType(sqliteReader.getOutdoorTemperatureCurrentDayMin()));
        }
        if (sqliteReader.getOutdoorTemperatureCurrentDayMinTime() != null) {
            updateState(CHANNEL_OUTSIDE_TEMPERATURE_DAY_MIN_TIME,
                    sqliteReader.getOutdoorTemperatureCurrentDayMinTime());
        }
        if (sqliteReader.getOutdoorTemperatureCurrentDayMax() != null) {
            updateState(CHANNEL_OUTSIDE_TEMPERATURE_DAY_MAX,
                    new DecimalType(sqliteReader.getOutdoorTemperatureCurrentDayMax()));
        }
        if (sqliteReader.getOutdoorTemperatureCurrentDayMaxTime() != null) {
            updateState(CHANNEL_OUTSIDE_TEMPERATURE_DAY_MAX_TIME,
                    sqliteReader.getOutdoorTemperatureCurrentDayMaxTime());
        }
        if (sqliteReader.getOutdoorHumidity() != null) {
            updateState(CHANNEL_OUTSIDE_HUMIDITY, new DecimalType(sqliteReader.getOutdoorHumidity()));
        }
        if (sqliteReader.getWindSpeed() != null) {
            updateState(CHANNEL_WIND_SPEED, new DecimalType(sqliteReader.getWindSpeed()));
        }
        if (sqliteReader.getWindDirection() != null) {
            updateState(CHANNEL_WIND_DIRECTION, new StringType(sqliteReader.getWindDirection().toString()));
        }
        if (sqliteReader.getRainRate() != null) {
            updateState(CHANNEL_RAIN_RATE, new DecimalType(sqliteReader.getRainRate()));
        }
        if (sqliteReader.getRainCurrentDay() != null) {
            updateState(CHANNEL_RAIN_CURRENT_DAY, new DecimalType(sqliteReader.getRainCurrentDay()));
        }
        if (sqliteReader.getRainCurrentWeek() != null) {
            updateState(CHANNEL_RAIN_CURRENT_WEEK, new DecimalType(sqliteReader.getRainCurrentWeek()));
        }
        if (sqliteReader.getRainCurrentMonth() != null) {
            updateState(CHANNEL_RAIN_CURRENT_MONTH, new DecimalType(sqliteReader.getRainCurrentMonth()));
        }
        if (sqliteReader.getRainCurrentYear() != null) {
            updateState(CHANNEL_RAIN_CURRENT_YEAR, new DecimalType(sqliteReader.getRainCurrentYear()));
        }
        if (sqliteReader.getRainJanuary() != null) {
            updateState(CHANNEL_RAIN_MONTH1, new DecimalType(sqliteReader.getRainJanuary()));
        }
        if (sqliteReader.getRainFebruary() != null) {
            updateState(CHANNEL_RAIN_MONTH2, new DecimalType(sqliteReader.getRainFebruary()));
        }
        if (sqliteReader.getRainMarch() != null) {
            updateState(CHANNEL_RAIN_MONTH3, new DecimalType(sqliteReader.getRainMarch()));
        }
        if (sqliteReader.getRainApril() != null) {
            updateState(CHANNEL_RAIN_MONTH4, new DecimalType(sqliteReader.getRainApril()));
        }
        if (sqliteReader.getRainMay() != null) {
            updateState(CHANNEL_RAIN_MONTH5, new DecimalType(sqliteReader.getRainMay()));
        }
        if (sqliteReader.getRainJune() != null) {
            updateState(CHANNEL_RAIN_MONTH6, new DecimalType(sqliteReader.getRainJune()));
        }
        if (sqliteReader.getRainJuly() != null) {
            updateState(CHANNEL_RAIN_MONTH7, new DecimalType(sqliteReader.getRainJuly()));
        }
        if (sqliteReader.getRainAugust() != null) {
            updateState(CHANNEL_RAIN_MONTH8, new DecimalType(sqliteReader.getRainAugust()));
        }
        if (sqliteReader.getRainSeptember() != null) {
            updateState(CHANNEL_RAIN_MONTH9, new DecimalType(sqliteReader.getRainSeptember()));
        }
        if (sqliteReader.getRainOctober() != null) {
            updateState(CHANNEL_RAIN_MONTH10, new DecimalType(sqliteReader.getRainOctober()));
        }
        if (sqliteReader.getRainNovember() != null) {
            updateState(CHANNEL_RAIN_MONTH11, new DecimalType(sqliteReader.getRainNovember()));
        }
        if (sqliteReader.getRainDecember() != null) {
            updateState(CHANNEL_RAIN_MONTH12, new DecimalType(sqliteReader.getRainDecember()));
        }
    }

}
