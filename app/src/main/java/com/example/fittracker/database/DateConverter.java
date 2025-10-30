package com.example.fittracker.database;

import androidx.room.TypeConverter;
import java.util.Date;

public class DateConverter {
    @TypeConverter
    public static Long fromDate(Date value) {
        return value == null ? null : value.getTime();
    }

    @TypeConverter
    public static Date toDate(Long value) {
        return value == null ? null : new Date(value);
    }
}