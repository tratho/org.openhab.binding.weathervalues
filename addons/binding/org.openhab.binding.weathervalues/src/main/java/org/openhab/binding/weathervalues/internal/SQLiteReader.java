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

import org.eclipse.smarthome.core.library.types.DateTimeType;
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

    // month
    private List<Double> listOfRainMonth;

    // Rain Data
    private List<Time> listOfTime;
    private List<Double> listOfRain;

    public SQLiteReader(String host, String dbName) throws ClassNotFoundException {
        this.host = host;
        this.dbName = dbName;
        this.listOfListener = new ArrayList<>();
        this.listOfTime = new ArrayList<>();
        this.listOfRain = new ArrayList<>();
        this.listOfRainMonth = new ArrayList<>();

        Class.forName("org.sqlite.JDBC");
    }

    @Override
    public void run() {
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
            pullMonthRain();
        } catch (SQLException e) {
            logger.warn("Error during reading values form database");
        }
        callAllListener();
    }

    @Override
    public void open() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::resource:http://" + host + "/" + dbName);
    }

    @Override
    public void close() throws SQLException {
        connection.close();
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
        for (SQLReaderListener listener : listOfListener) {
            listener.refreshValues();
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

    private void pullMonthRain() throws SQLException {
        if (listOfTime.size() == 0) {
            pullRainData();
        }

        listOfRainMonth.clear();

        for (int month = 1; month <= 12; month++) {
            double rainMonthTemp = 0;
            for (int i = 0; i < listOfTime.size(); i++) {
                if (listOfTime.get(i).getYear() == new Time().getYear()) {
                    if (listOfTime.get(i).getMonth() == month) {
                        rainMonthTemp = rainMonthTemp + listOfRain.get(i);
                    }
                }
            }
            listOfRainMonth.add(Converter.inch_to_Millimeter(rainMonthTemp));
        }
    }

    private void pullRainData() throws SQLException {
        listOfTime.clear();
        listOfRain.clear();

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
                listOfRain.add(0, Double.parseDouble(sRain));
            } catch (NumberFormatException e) {
                if (listOfTime.size() != listOfRain.size()) {
                    listOfTime.remove(0);
                }
            }
        }

        resultSet.close();
    }

    public Double getBarometer() {
        return barometer;
    }

    public Double getOutdoorHumidity() {
        return outHumidity;
    }

    public Double getOutdoorTemperature() {
        return outTemp;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public WindDirection getWindDirection() {
        return windDir;
    }

    public Double getRainRate() {
        return rainRate;
    }

    public Double getOutdoorTemperatureCurrentDayMin() {
        return outTempDayMin;
    }

    public DateTimeType getOutdoorTemperatureCurrentDayMinTime() {
        return outTempDayMinTime.getDateTimeType();
    }

    public Double getOutdoorTemperatureCurrentDayMax() {
        return outTempDayMax;
    }

    public DateTimeType getOutdoorTemperatureCurrentDayMaxTime() {
        return outTempDayMaxTime.getDateTimeType();
    }

    public Double getRainCurrentDay() {
        return rainCurrentDay;
    }

    public Double getRainCurrentWeek() {
        if (listOfRainMonth.size() == 0) {
            try {
                pullMonthRain();
            } catch (SQLException e) {
                return null;
            }
        }

        int currentDay = listOfTime.get(listOfTime.size() - 1).getDayOfWeek();

        double rainWeekTemp = 0;
        for (int i = listOfTime.size() - currentDay; i < listOfTime.size(); i++) {
            rainWeekTemp += listOfRain.get(i);
        }

        return Converter.inch_to_Millimeter(rainWeekTemp);
    }

    public Double getRainCurrentMonth() {
        return listOfRainMonth.get(new Time().getMonth() - 1);
    }

    public Double getRainCurrentYear() {
        if (listOfRainMonth.size() == 0) {
            try {
                pullMonthRain();
            } catch (SQLException e) {
                return null;
            }
        }

        double rainCurrentYear = 0.0;
        for (int month = 0; month < 12; month++) {
            rainCurrentYear += listOfRainMonth.get(month);
        }
        return rainCurrentYear;
    }

    public Double getRainJanuary() {
        return listOfRainMonth.get(0);
    }

    public Double getRainFebruary() {
        return listOfRainMonth.get(1);
    }

    public Double getRainMarch() {
        return listOfRainMonth.get(2);
    }

    public Double getRainApril() {
        return listOfRainMonth.get(3);
    }

    public Double getRainMay() {
        return listOfRainMonth.get(4);
    }

    public Double getRainJune() {
        return listOfRainMonth.get(5);
    }

    public Double getRainJuly() {
        return listOfRainMonth.get(6);
    }

    public Double getRainAugust() {
        return listOfRainMonth.get(7);
    }

    public Double getRainSeptember() {
        return listOfRainMonth.get(8);
    }

    public Double getRainOctober() {
        return listOfRainMonth.get(9);
    }

    public Double getRainNovember() {
        return listOfRainMonth.get(10);
    }

    public Double getRainDecember() {
        return listOfRainMonth.get(11);
    }
}
