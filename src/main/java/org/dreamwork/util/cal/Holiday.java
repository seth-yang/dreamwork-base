package org.dreamwork.util.cal;

import java.util.Objects;

/**
 * Created by game on 2017/9/25
 */
public record Holiday(int month, int date, String name) {

    public boolean equals (int month, int date) {
        return this.month == month &&
                this.date == date;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass () != o.getClass ()) return false;
        Holiday holiday = (Holiday) o;
        return month == holiday.month &&
                date == holiday.date;
    }

    @Override
    public int hashCode () {
        return Objects.hash (month, date);
    }
}