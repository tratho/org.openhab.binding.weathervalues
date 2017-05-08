/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.weathervalues.internal;

import java.util.Calendar;

import org.eclipse.smarthome.core.library.types.DateTimeType;

/**
 * The {@link Time}
 *
 * @author Thomas Traunbauer - Initial contribution
 */
public class Time {
    private long timeInMilliseconds;

    public Time() {
        this.timeInMilliseconds = Calendar.getInstance().getTimeInMillis();
    }

    public Time(long timeInMilliseconds) {
        this.timeInMilliseconds = timeInMilliseconds;
    }

    public int getYear() {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timeInMilliseconds);
        return date.get(Calendar.YEAR);
    }

    public int getMonth() {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timeInMilliseconds);
        return date.get(Calendar.MONTH) + 1;
    }

    public int getDay() {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timeInMilliseconds);
        return date.get(Calendar.DAY_OF_MONTH);
    }

    public int getHour() {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timeInMilliseconds);
        return date.get(Calendar.HOUR_OF_DAY);
    }

    public int getMinute() {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timeInMilliseconds);
        return date.get(Calendar.MINUTE);
    }

    public int getSecond() {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timeInMilliseconds);
        return date.get(Calendar.SECOND);
    }

    public int getDayOfWeek() {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timeInMilliseconds);
        int day = date.get(Calendar.DAY_OF_WEEK) - 1;
        if (day == 0) {
            day = 7;
        }
        return day;
    }

    public Calendar getCalendar() {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timeInMilliseconds);
        return date;
    }

    public DateTimeType getDateTimeType() {
        return new DateTimeType(getCalendar());
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("\"");

        buffer.append(getYear());
        buffer.append("-");

        if (getMonth() < 10) {
            buffer.append("0");
        }
        buffer.append(getMonth());

        buffer.append("-");

        if (getDay() < 10) {
            buffer.append("0");
        }
        buffer.append(getDay());

        buffer.append(" ");

        if (getHour() < 10) {
            buffer.append("0");
        }
        buffer.append(getHour());

        buffer.append(":");

        if (getMinute() < 10) {
            buffer.append("0");
        }
        buffer.append(getMinute());

        buffer.append(":");

        if (getSecond() < 10) {
            buffer.append("0");
        }
        buffer.append(getSecond());

        buffer.append("\"");

        return buffer.toString();
    }
}
