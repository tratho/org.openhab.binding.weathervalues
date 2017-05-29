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
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.weathervalues.internal.data.OutdoorClimate;
import org.openhab.binding.weathervalues.internal.data.Rain;
import org.openhab.binding.weathervalues.internal.data.Wind;
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

    public OutdoorClimate outdoorClimate;
    public Wind wind;
    public Rain rain;

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

            pullOutdoorClimate();
            pullWind();
            pullRain();

            close();
            for (SQLReaderListener listener : listOfListener) {
                listener.refreshValues();
            }
        } catch (SQLException e) {
            logger.warn("Error during opening/closing database");
        }
    }

    private void pullOutdoorClimate() {
        Double outTemp = null;
        Double outTempExtra = null;
        Double outHumidity = null;
        Double outHumidityExtra = null;
        Double barometer = null;

        Double outTempDayMin = null;
        Double outTempDayMax = null;
        Time outTempDayMinTime = null;
        Time outTempDayMaxTime = null;

        ResultSet resultSet;

        try {
            resultSet = getResultSet(
                    "SELECT barometer, outTemp, outHumidity, ExtraTemp1, extraHumid1 FROM archive ORDER BY dateTime DESC LIMIT 1");
        } catch (SQLException e) {
            resultSet = null;
        }

        if (resultSet != null) {
            String sOutTemp;
            try {
                sOutTemp = resultSet.getString("outTemp");
            } catch (SQLException e1) {
                sOutTemp = null;
            }
            if (sOutTemp != null) {
                outTemp = Converter.fahrenheit_to_Celsius(Double.parseDouble(sOutTemp));
            } else {
                outTemp = null;
            }

            String sOutTempExtra;
            try {
                sOutTempExtra = resultSet.getString("extraTemp1");
            } catch (SQLException e1) {
                sOutTempExtra = null;
            }
            if (sOutTempExtra != null) {
                outTempExtra = Converter.fahrenheit_to_Celsius(Double.parseDouble(sOutTempExtra));
            } else {
                outTempExtra = null;
            }

            String sOutHumidity;
            try {
                sOutHumidity = resultSet.getString("outHumidity");
            } catch (SQLException e1) {
                sOutHumidity = null;
            }
            if (sOutHumidity != null) {
                outHumidity = Double.parseDouble(sOutHumidity);
            } else {
                outHumidity = null;
            }

            String sOutHumidityExtra;
            try {
                sOutHumidityExtra = resultSet.getString("extraHumid1");
            } catch (SQLException e1) {
                sOutHumidityExtra = null;
            }
            if (sOutHumidityExtra != null) {
                outHumidityExtra = Double.parseDouble(sOutHumidityExtra);
            } else {
                outHumidityExtra = null;
            }

            String sBarometer;
            try {
                sBarometer = resultSet.getString("barometer");
            } catch (SQLException e1) {
                sBarometer = null;
            }
            if (sBarometer != null) {
                barometer = Converter.inchOfHG_to_Millibar(Double.parseDouble(sBarometer));
            } else {
                barometer = null;
            }

            try {
                resultSet.close();
            } catch (SQLException e1) {
                resultSet = null;
            }
        }

        try {
            resultSet = getResultSet(
                    "SELECT min, minTime, max, maxTime FROM archive_day_outTemp ORDER BY dateTime DESC LIMIT 1");
        } catch (SQLException e) {
            resultSet = null;
        }

        if (resultSet != null) {
            String sMinOutTemp;
            try {
                sMinOutTemp = resultSet.getString("min");
            } catch (SQLException e1) {
                sMinOutTemp = null;
            }
            try {
                outTempDayMin = Converter.fahrenheit_to_Celsius(Double.parseDouble(sMinOutTemp));
            } catch (NumberFormatException e) {
                outTempDayMin = null;
            }

            String sMaxOutTemp;
            try {
                sMaxOutTemp = resultSet.getString("max");
            } catch (SQLException e1) {
                sMaxOutTemp = null;
            }
            try {
                outTempDayMax = Converter.fahrenheit_to_Celsius(Double.parseDouble(sMaxOutTemp));
            } catch (NumberFormatException e) {
                outTempDayMax = null;
            }

            String sMinOutTempTime;
            try {
                sMinOutTempTime = resultSet.getString("minTime");
            } catch (SQLException e1) {
                sMinOutTempTime = null;
            }
            try {
                outTempDayMinTime = Converter.seconds_to_Time(Long.parseLong(sMinOutTempTime));
            } catch (NumberFormatException e) {
                outTempDayMinTime = null;
            }

            String sMaxOutTempTime;
            try {
                sMaxOutTempTime = resultSet.getString("maxTime");
            } catch (SQLException e1) {
                sMaxOutTempTime = null;
            }
            try {
                outTempDayMaxTime = Converter.seconds_to_Time(Long.parseLong(sMaxOutTempTime));
            } catch (NumberFormatException e) {
                outTempDayMaxTime = null;
            }

            try {
                resultSet.close();
            } catch (SQLException e1) {
                resultSet = null;
            }
        }
        outdoorClimate = new OutdoorClimate(outTemp, outTempExtra, outHumidity, outHumidityExtra, barometer,
                outTempDayMin, outTempDayMax, outTempDayMinTime, outTempDayMaxTime);
    }

    private void pullWind() {
        Integer windDir = null;
        Double windSpeed = null;

        ResultSet resultSet;

        try {
            resultSet = getResultSet("SELECT windSpeed, windDir FROM archive ORDER BY dateTime DESC LIMIT 1");
        } catch (SQLException e) {
            resultSet = null;
        }

        if (resultSet != null) {
            String sWindDir;
            try {
                sWindDir = resultSet.getString("windDir");
            } catch (SQLException e) {
                sWindDir = null;
            }
            if (sWindDir != null) {
                windDir = (int) Double.parseDouble(sWindDir);
            } else {
                windDir = null;
            }

            String sWindSpeed;
            try {
                sWindSpeed = resultSet.getString("windSpeed");
            } catch (SQLException e) {
                sWindSpeed = null;
            }
            if (sWindSpeed != null) {
                windSpeed = Converter.knoten_to_kmh(Double.parseDouble(sWindSpeed));
            } else {
                windSpeed = null;
            }

            try {
                resultSet.close();
            } catch (SQLException e1) {
                resultSet = null;
            }
        }
        wind = new Wind(windDir, windSpeed);
    }

    private void pullRain() {
        Double rainRate = null;
        Double rainCurrentDay = null;
        Double rainCurrentWeek = null;
        List<Double> listOfRainPerMonth = new ArrayList<>();

        ResultSet resultSet;

        try {
            resultSet = getResultSet("SELECT rainRate FROM archive ORDER BY dateTime DESC LIMIT 1");
        } catch (SQLException e) {
            resultSet = null;
        }

        if (resultSet != null) {
            String sRainRate;
            try {
                sRainRate = resultSet.getString("rainRate");
            } catch (SQLException e) {
                sRainRate = null;
            }
            if (sRainRate != null) {
                rainRate = Converter.inchPerHour_to_MillimeterPerHour(Double.parseDouble(sRainRate));
            } else {
                rainRate = null;
            }

            try {
                resultSet.close();
            } catch (SQLException e1) {
                resultSet = null;
            }
        }

        try {
            resultSet = getResultSet("SELECT sum FROM archive_day_rain ORDER BY dateTime DESC LIMIT 1");
        } catch (SQLException e) {
            resultSet = null;
        }
        if (resultSet != null) {
            String sSumRain;
            try {
                sSumRain = resultSet.getString("sum");
            } catch (SQLException e1) {
                sSumRain = null;
            }
            try {
                rainCurrentDay = Converter.inch_to_Millimeter(Double.parseDouble(sSumRain));
            } catch (NumberFormatException e) {
                rainCurrentDay = null;
            }

            try {
                resultSet.close();
            } catch (SQLException e1) {
                resultSet = null;
            }
        }

        try {
            resultSet = getResultSet("SELECT dateTime, sum FROM archive_day_rain ORDER BY dateTime");
        } catch (SQLException e) {
            resultSet = null;
        }

        List<Time> listOfTime = new ArrayList<>();
        List<Double> listOfRain = new ArrayList<>();
        if (resultSet != null) {
            try {
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
            } catch (SQLException e) {
                listOfTime.clear();
                listOfRain.clear();
            }

            try {
                resultSet.close();
            } catch (SQLException e1) {
                resultSet = null;
            }
        }

        if (listOfTime.size() > 0) {
            for (int i = 0; i < listOfTime.get(0).getDayOfWeek(); i++) {
                if (rainCurrentWeek == null) {
                    rainCurrentWeek = listOfRain.get(i);
                } else {
                    rainCurrentWeek += listOfRain.get(i);
                }
            }

            for (int i = 1; i <= 12; i++) {
                Month month = Month.of(i);
                double rainMonthTemp = 0;
                for (int j = 0; j < listOfTime.size(); j++) {
                    if (listOfTime.get(j).getYear() == new Time().getYear()) {
                        if (listOfTime.get(j).getMonth() == month) {
                            rainMonthTemp += listOfRain.get(j);
                        }
                    }
                }
                if (month.getValue() > new Time().getMonth().getValue()) {
                    listOfRainPerMonth.add(null);
                } else {
                    listOfRainPerMonth.add(rainMonthTemp);
                }
            }
        } else {
            for (int i = 1; i <= 12; i++) {
                listOfRainPerMonth.add(null);
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
}
