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

import org.eclipse.smarthome.core.types.State;
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

    private OutdoorClimate outdoorClimate;
    private Wind wind;
    private Rain rain;

    // Rain Data
    public SQLiteReader(String host, String dbName) throws ClassNotFoundException {
        this.host = host;
        this.dbName = dbName;
        this.listOfListener = new ArrayList<>();

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
                pullOutdoorClimate();
            } catch (SQLException e) {
                logger.warn("Error during reading Climate values form database");
            }
            try {
                pullWind();
            } catch (SQLException e) {
                logger.warn("Error during reading Wind values form database");
            }
            try {
                pullRain();
            } catch (SQLException e) {
                logger.warn("Error during reading Rain values form database");
            }
            close();
            for (SQLReaderListener listener : listOfListener) {
                listener.refreshValues();
            }
        } catch (SQLException e) {
            logger.warn("Error during opening/closing database");
        }
    }

    private void pullOutdoorClimate() throws SQLException {
        ResultSet resultSet;
        resultSet = getResultSet(
                "SELECT barometer, outTemp, outHumidity, ExtraTemp1, extraHumid1 FROM archive ORDER BY dateTime DESC LIMIT 1");

        Double outTemp;
        Double outTempExtra;
        Double outHumidity;
        Double outHumidityExtra;
        Double barometer;

        String sOutTemp = resultSet.getString("outTemp");
        if (sOutTemp != null) {
            outTemp = Converter.fahrenheit_to_Celsius(Double.parseDouble(sOutTemp));
        } else {
            outTemp = null;
        }

        String sOutTempExtra = resultSet.getString("extraTemp1");
        if (sOutTempExtra != null) {
            outTempExtra = Converter.fahrenheit_to_Celsius(Double.parseDouble(sOutTempExtra));
        } else {
            outTempExtra = null;
        }

        String sOutHumidity = resultSet.getString("outHumidity");
        if (sOutHumidity != null) {
            outHumidity = Double.parseDouble(sOutHumidity);
        } else {
            outHumidity = null;
        }

        String sOutHumidityExtra = resultSet.getString("extraHumid1");
        if (sOutHumidityExtra != null) {
            outHumidityExtra = Double.parseDouble(sOutHumidityExtra);
        } else {
            outHumidityExtra = null;
        }

        String sBarometer = resultSet.getString("barometer");
        if (sBarometer != null) {
            barometer = Converter.inchOfHG_to_Millibar(Double.parseDouble(sBarometer));
        } else {
            barometer = null;
        }

        resultSet.close();

        resultSet = getResultSet(
                "SELECT min, minTime, max, maxTime FROM archive_day_outTemp ORDER BY dateTime DESC LIMIT 1");

        Double outTempDayMin;
        Double outTempDayMax;
        Time outTempDayMinTime;
        Time outTempDayMaxTime;

        String sMinOutTemp = resultSet.getString("min");
        try {
            outTempDayMin = Converter.fahrenheit_to_Celsius(Double.parseDouble(sMinOutTemp));
        } catch (NumberFormatException e) {
            outTempDayMin = null;
        }

        String sMaxOutTemp = resultSet.getString("max");
        try {
            outTempDayMax = Converter.fahrenheit_to_Celsius(Double.parseDouble(sMaxOutTemp));
        } catch (NumberFormatException e) {
            outTempDayMax = null;
        }

        String sMinOutTempTime = resultSet.getString("minTime");
        try {
            outTempDayMinTime = Converter.seconds_to_Time(Long.parseLong(sMinOutTempTime));
        } catch (NumberFormatException e) {
            outTempDayMinTime = null;
        }

        String sMaxOutTempTime = resultSet.getString("maxTime");
        try {
            outTempDayMaxTime = Converter.seconds_to_Time(Long.parseLong(sMaxOutTempTime));
        } catch (NumberFormatException e) {
            outTempDayMaxTime = null;
        }

        resultSet.close();

        outdoorClimate = new OutdoorClimate(outTemp, outTempExtra, outHumidity, outHumidityExtra, barometer,
                outTempDayMin, outTempDayMax, outTempDayMinTime, outTempDayMaxTime);
    }

    private void pullWind() throws SQLException {
        ResultSet resultSet;
        resultSet = getResultSet("SELECT windSpeed, windDir FROM archive ORDER BY dateTime DESC LIMIT 1");

        Integer windDir;
        Double windSpeed;
        String sWindDir = resultSet.getString("windDir");
        if (sWindDir != null) {
            windDir = (int) Double.parseDouble(sWindDir);
        } else {
            windDir = null;
        }

        String sWindSpeed = resultSet.getString("windSpeed");
        if (sWindSpeed != null) {
            windSpeed = Converter.knoten_to_kmh(Double.parseDouble(sWindSpeed));
        } else {
            windSpeed = null;
        }

        wind = new Wind(windDir, windSpeed);

        resultSet.close();
    }

    private void pullRain() throws SQLException {
        ResultSet resultSet;
        resultSet = getResultSet("SELECT rainRate FROM archive ORDER BY dateTime DESC LIMIT 1");

        Double rainRate;

        String sRainRate = resultSet.getString("rainRate");
        if (sRainRate != null) {
            rainRate = Converter.inchPerHour_to_MillimeterPerHour(Double.parseDouble(sRainRate));
        } else {
            rainRate = null;
        }

        resultSet.close();

        resultSet = getResultSet("SELECT sum FROM archive_day_rain ORDER BY dateTime DESC LIMIT 1");

        Double rainCurrentDay;

        String sSumRain = resultSet.getString("sum");
        try {
            rainCurrentDay = Converter.inch_to_Millimeter(Double.parseDouble(sSumRain));
        } catch (NumberFormatException e) {
            rainCurrentDay = null;
        }

        resultSet.close();

        List<Time> listOfTime = new ArrayList<>();
        List<Double> listOfRain = new ArrayList<>();

        resultSet = getResultSet("SELECT dateTime, sum FROM archive_day_rain ORDER BY dateTime");

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

        Double rainCurrentWeek = 0.0;
        for (int i = 0; i < listOfTime.get(0).getDayOfWeek(); i++) {
            rainCurrentWeek += listOfRain.get(i);
        }

        List<Double> listOfRainPerMonth = new ArrayList<>();
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
                listOfRainPerMonth.add(null);
            } else {
                listOfRainPerMonth.add(rainMonthTemp);
            }
        }

        rain = new Rain(rainRate, rainCurrentDay, rainCurrentWeek, listOfRainPerMonth);
    }

    private void open() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::resource:http://" + host + "/" + dbName);
    }

    private void close() throws SQLException {
        connection.close();
    }

    public State getBarometer() {
        return outdoorClimate.getBarometer();
    }

    public State getOutdoorHumidity() {
        return outdoorClimate.getHumidity();
    }

    public State getOutdoorTemperature() {
        return outdoorClimate.getTemperature();
    }

    public State getOutdoorTemperatureCurrentDayMin() {
        return outdoorClimate.getTemperatureMinimum();
    }

    public State getOutdoorTemperatureCurrentDayMinTime() {
        return outdoorClimate.getTemperatureMinimumTime();
    }

    public State getOutdoorTemperatureCurrentDayMax() {
        return outdoorClimate.getTemperatureMaximum();
    }

    public State getOutdoorTemperatureCurrentDayMaxTime() {
        return outdoorClimate.getTemperatureMaximumTime();
    }

    public State getWindSpeed() {
        return wind.getSpeed();
    }

    public State getWindDirection() {
        return wind.getDirection();
    }

    public State getRainRate() {
        return rain.getRainRateCurrent();
    }

    public State getRainCurrentDay() {
        return rain.getRainCurrentDay();
    }

    public State getRainCurrentWeek() {
        return rain.getRainCurrentWeek();
    }

    public State getRainCurrentMonth() {
        return rain.getRainCurrentMonth();
    }

    public State getRainCurrentYear() {
        return rain.getRainCurrentYear();
    }

    public State getRainMonth(int month) {
        return rain.getRainMonth(month);
    }
}
