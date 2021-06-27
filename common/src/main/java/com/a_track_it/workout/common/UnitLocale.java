package com.a_track_it.workout.common;

import java.util.Locale;

public class UnitLocale {
    public static UnitLocale Imperial = new UnitLocale();
    public static UnitLocale Metric = new UnitLocale();

    public static UnitLocale getDefault() {
        return getFrom(Locale.getDefault());
    }
    public static UnitLocale getFrom(Locale locale) {
        String countryCode = locale.getCountry().toUpperCase();
        switch (countryCode) {
            case "US":
            case "LR":
            case "MM":
                return Imperial;
            default:
                return Metric;
        }
    }
}
