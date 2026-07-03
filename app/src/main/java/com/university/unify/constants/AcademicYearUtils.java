package com.university.unify.utils;

import java.util.ArrayList;
import java.util.List;

public class AcademicYearUtils {

    private AcademicYearUtils() {}

    public static String[] getAcademicYears() {
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);

        List<String> years = new ArrayList<>();
        years.add((currentYear - 1) + "/" + currentYear);
        years.add(currentYear + "/" + (currentYear + 1));
        years.add((currentYear + 1) + "/" + (currentYear + 2));

        return years.toArray(new String[0]);
    }
}