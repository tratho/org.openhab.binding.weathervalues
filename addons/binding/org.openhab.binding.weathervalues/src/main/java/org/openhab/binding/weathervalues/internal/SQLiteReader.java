/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.weathervalues.internal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SQLiteReader}
 *
 * @author Thomas Traunbauer - Initial contribution
 */
public class SQLiteReader implements Runnable, SQLReader {

    private Logger logger = LoggerFactory.getLogger(SQLiteReader.class);

    private String host;
    private String dbName;
    private List<SQLReaderListener> listOfListener;

    private Connection connection;

    // current
    private Double barometer;
    private Double outTemp;
    private Double outHumidity;
    private Double windSpeed;
    private WindDirection windDir;
    private Double rainRate;

    // day
    private Double outTempDayMin;
    private Time outTempDayMinTime;
    private Double outTempDayMax;
    private Time outTempDayMaxTime;
    private Double rainCurrentDay;

    // week
    private Double rainWeekInMM;

    // month
    private List<State> listOfRainMonthInMM;

    // Rain Data
    public SQLiteReader(String host, String dbName) throws ClassNotFoundException {
        this.host = host;
        this.dbName = dbName;
        this.listOfListener = new ArrayList<>();
        this.listOfRainMonthInMM = new ArrayList<>();
        this.rainWeekInMM = null;

        Class.forName("org.sqlite.JDBC");
    }

    @Override
    public void run() {
        callAllListener();
    }

    @Override
    public ResultSet getResultSet(String sql) throws SQLException {
        return connection.createStatement().executeQuery(sql);
    }

    @Override
    public void removeListener(SQLReaderListener listener) {
        for (int i = 0; i < listOfListener.size(); i++) {
            if (listOfListener.get(i) == listener) {
                listOfListener.remove(i);
            }
        }
        listOfListener.add(listener);
    }

    @Override
    public void addListener(SQLReaderListener listener) {
        listOfListener.add(listener);
    }

    @Override
    public void callAllListener() {
        logger.debug("callAllListener()");
        try {
            open();
            try {
                pullCurrent();
            } catch (SQLException e) {
                logger.warn("Error during reading values form database");
            }
            try {
                pullDay();
            } catch (SQLException e) {
                logger.warn("Error during reading values form database");
            }
            try {
                pullDayRain();
            } catch (SQLException e) {
                logger.warn("Error during reading values form database");
            }
            try {
                pullRainData();
            } catch (SQLException e) {
                logger.warn("Error during reading values form database");
            }
            close();
            for (SQLReaderListener listener : listOfListener) {
                listener.refreshValues();
            }
        } catch (SQLException e) {
            logger.warn("Error during opening/closing database");
        }
    }

    private void pullCurrent() throws SQLException {
        String sql = "SELECT barometer, outTemp, outHumidity, windSpeed, windDir, rainRate, extraTemp1 FROM archive ORDER BY dateTime DESC LIMIT 1";
        ResultSet resultSet = getResultSet(sql);

        String sBarometer = resultSet.getString("barometer");
        if (sBarometer != null) {
            barometer = Converter.inchOfHG_to_Millibar(Double.parseDouble(sBarometer));
        } else {
            barometer = null;
        }

        String sOutTemp = resultSet.getString("outTemp");
        if (sOutTemp != null) {
            outTemp = Converter.fahrenheit_to_Celsius(Double.parseDouble(sOutTemp));
        } else {
            outTemp = null;
        }

        String sOutHumidity = resultSet.getString("outHumidity");
        if (sOutHumidity != null) {
            outHumidity = Double.parseDouble(sOutHumidity);
        } else {
            outHumidity = null;
        }

        String sWindSpeed = resultSet.getString("windSpeed");
        if (sWindSpeed != null) {
            windSpeed = Converter.knoten_to_kmh(Double.parseDouble(sWindSpeed));
        } else {
            windSpeed = null;
        }

        String sRainRate = resultSet.getString("rainRate");
        if (sRainRate != null) {
            rainRate = Converter.inchPerHour_to_MillimeterPerHour(Double.parseDouble(sRainRate));
        } else {
            rainRate = null;
        }

        String sWindDir = resultSet.getString("windDir");
        if (sWindDir != null) {
            windDir = Converter.grad_to_windDirection((int) Double.parseDouble(sWindDir));
        } else {
            windDir = WindDirection.Unknown;
        }

        resultSet.close();
    }

    private void pullDay() throws SQLException {
        String sql = "SELECT min, minTime, max, maxTime FROM archive_day_outTemp ORDER BY dateTime DESC LIMIT 1";
        ResultSet resultSet = getResultSet(sql);

        String sMinOutTemp = resultSet.getString("min");
        try {
            outTempDayMin = Converter.fahrenheit_to_Celsius(Double.parseDouble(sMinOutTemp));
        } catch (NumberFormatException e) {
            outTempDayMin = null;
        }

        String sMinOutTempTime = resultSet.getString("minTime");
        try {
            outTempDayMinTime = Converter.seconds_to_Time(Long.parseLong(sMinOutTempTime));
        } catch (NumberFormatException e) {
            outTempDayMinTime = null;
        }

        String sMaxOutTemp = resultSet.getString("max");
        try {
            outTempDayMax = Converter.fahrenheit_to_Celsius(Double.parseDouble(sMaxOutTemp));
        } catch (NumberFormatException e) {
            outTempDayMax = null;
        }

        String sMaxOutTempTime = resultSet.getString("maxTime");
        try {
            outTempDayMaxTime = Converter.seconds_to_Time(Long.parseLong(sMaxOutTempTime));
        } catch (NumberFormatException e) {
            outTempDayMaxTime = null;
        }

        resultSet.close();
    }

    private void pullDayRain() throws SQLException {
        String sql = "SELECT sum FROM archive_day_rain ORDER BY dateTime DESC LIMIT 1";
        ResultSet resultSet = getResultSet(sql);

        String sRain = resultSet.getString("sum");
        try {
            rainCurrentDay = Converter.inch_to_Millimeter(Double.parseDouble(sRain));
        } catch (NumberFormatException e) {
            rainCurrentDay = null;
        }

        resultSet.close();
    }

    private void pullRainData() throws SQLException {
        List<Time> listOfTime = new ArrayList<>();
        List<Double> listOfRain = new ArrayList<>();

        String sql = "SELECT dateTime, sum FROM archive_day_rain ORDER BY dateTime";
        ResultSet resultSet = getResultSet(sql);

        while (resultSet.next()) {
            String sTimeInSeconds = resultSet.getString("dateTime");
            String sRain = resultSet.getString("sum");

            try {
                listOfTime.add(0, Converter.seconds_to_Time(Long.parseLong(sTimeInSeconds)));
            } catch (NumberFormatException e) {
            }
            try {
                listOfRain.add(0, Converter.inch_to_Millimeter(Double.parseDouble(sRain)));
            } catch (NumberFormatException e) {
                if (listOfTime.size() != listOfRain.size()) {
                    listOfTime.remove(0);
                }
            }
        }

        resultSet.close();

        int currentDay = listOfTime.get(0).getDayOfWeek();

        rainWeekInMM = 0.0;
        for (int i = 0; i < currentDay; i++) {
            rainWeekInMM += listOfRain.get(i);
        }

        listOfRainMonthInMM.clear();

        for (int month = 1; month <= 12; month++) {
            double rainMonthTemp = 0;
            for (int i = 0; i < listOfTime.size(); i++) {
                if (listOfTime.get(i).getYear() == new Time().getYear()) {
                    if (listOfTime.get(i).getMonth() == month) {
                        rainMonthTemp += listOfRain.get(i);
                    }
                }
            }
            if (month > new Time().getMonth()) {
                listOfRainMonthInMM.add(UnDefType.NULL);
            } else {
                listOfRainMonthInMM.add(new DecimalType(rainMonthTemp));
            }
        }
    }

    private void open() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::resource:http://" + host + "/" + dbName);
    }

    private void close() throws SQLException {
        connection.close();
    }

    public State getBarometer() {
        if (barometer != null) {
            double value = Utility.round(barometer);
            return new DecimalType(value);
        }
        return UnDefType.NULL;
    }

    public State getOutdoorHumidity() {
        if (outHumidity != null) {
            double value = Utility.round(outHumidity);
            return new DecimalType(value);
        }
        return UnDefType.NULL;
    }

    public State getOutdoorTemperature() {
        if (outTemp != null) {
            double value = Utility.round(outTemp);
            return new DecimalType(value);
        }
        return UnDefType.NULL;
    }

    public State getWindSpeed() {
        if (windSpeed != null) {
            double value = Utility.round(windSpeed);
            return new DecimalType(value);
        }
        return UnDefType.NULL;
    }

    public State getWindDirection() {
        switch (windDir) {
            case Unknown:
                return UnDefType.NULL;
            default:
                return new StringType(windDir.toString());
        }
    }

    public State getRainRate() {
        if (rainRate != null) {
            double value = Utility.round(rainRate);
            return new DecimalType(value);
        }
        return UnDefType.NULL;
    }

    public State getOutdoorTemperatureCurrentDayMin() {
        if (outTempDayMin != null) {
            double value = Utility.round(outTempDayMin);
            return new DecimalType(value);
        }
        return UnDefType.NULL;
    }

    public State getOutdoorTemperatureCurrentDayMinTime() {
        if (outTempDayMinTime != null) {
            return outTempDayMinTime.getDateTimeType();
        }
        return UnDefType.NULL;
    }

    public State getOutdoorTemperatureCurrentDayMax() {
        if (outTempDayMax != null) {
            double value = Utility.round(outTempDayMax);
            return new DecimalType(value);
        }
        return UnDefType.NULL;
    }

    public State getOutdoorTemperatureCurrentDayMaxTime() {
        if (outTempDayMaxTime != null) {
            return outTempDayMaxTime.getDateTimeType();
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
        if (rainWeekInMM != null) {
            double value = Utility.round(rainCurrentDay);
            return new DecimalType(value);
        }
        return UnDefType.NULL;
    }

    public State getRainCurrentMonth() {
        return getRainMonth(new Time().getMonth() - 1);
    }

    public State getRainCurrentYear() {
        if (listOfRainMonthInMM.size() == 0) {
            try {
                pullRainData();
            } catch (SQLException e) {
                return UnDefType.NULL;
            }
        }

        boolean checker = false;

        double rainCurrentYear = 0.0;
        for (int month = 0; month < 12; month++) {
            State currentMonth = listOfRainMonthInMM.get(month);
            if (currentMonth instanceof DecimalType) {
                rainCurrentYear += ((DecimalType) listOfRainMonthInMM.get(month)).doubleValue();
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

    public State getRainMonth(int month) {
        int index = month - 1;
        if (listOfRainMonthInMM.get(index) instanceof DecimalType) {
            double value = Utility.round(((DecimalType) listOfRainMonthInMM.get(index)).doubleValue());
            return new DecimalType(value);
        }
        return UnDefType.NULL;
    }
}
