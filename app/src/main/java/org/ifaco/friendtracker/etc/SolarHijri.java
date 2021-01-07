package org.ifaco.friendtracker.etc;

import java.util.Calendar;

public class SolarHijri {
    public int Y = 0, M = 0, D = 0;

    public SolarHijri(Calendar cl) {
        int y = cl.get(Calendar.YEAR), m = cl.get(Calendar.MONTH), d = cl.get(Calendar.DAY_OF_MONTH), l = 0, pl = 0;
        if (isLeapYear(y)) l = 1;
        Calendar clPl = Calendar.getInstance();
        clPl.set(y - 1, 0, 1);
        if (isLeapYear(clPl.get(Calendar.YEAR))) pl = 1;

        switch (m) {
            case 0:
                Y = y - 622;
                if (d <= (20 - pl)) {
                    M = m + 9;
                    D = d + (10 + pl);
                } else {
                    M = m + 10;
                    D = d - (20 - pl);
                }
                break;
            case 1:
                Y = y - 622;
                if (d <= (19 - pl)) {
                    M = m + 9;
                    D = d + (11 + pl);
                } else {
                    M = m + 10;
                    D = d - (19 - pl);
                }
                break;
            case 2:
                if (d <= (20 - l)) {
                    Y = y - 622;
                    M = m + 9;
                    D = d + ((9 + l) + pl);
                } else {
                    Y = y - 621;
                    M = m - 2;
                    D = d - (20 - l);
                }
                break;
            case 3:
                Y = y - 621;
                if (d <= (20 - l)) {
                    M = m - 3;
                    D = d + (11 + l);
                } else {
                    M = m - 2;
                    D = d - (20 - l);
                }
                break;
            case 4:
            case 5:
                Y = y - 621;
                if (d <= (21 - l)) {
                    M = m - 3;
                    D = d + (10 + l);
                } else {
                    M = m - 2;
                    D = d - (21 - l);
                }
                break;
            case 6:
            case 7:
            case 8:
                Y = y - 621;
                if (d <= (22 - l)) {
                    M = m - 3;
                    D = d + (9 + l);
                } else {
                    M = m - 2;
                    D = d - (22 - l);
                }
                break;
            case 9:
                Y = y - 621;
                if (d <= (22 - l)) {
                    M = m - 3;
                    D = d + (8 + l);
                } else {
                    M = m - 2;
                    D = d - (22 - l);
                }
                break;
            case 10:
            case 11:
                Y = y - 621;
                if (d <= (21 - l)) {
                    M = m - 3;
                    D = d + (9 + l);
                } else {
                    M = m - 2;
                    D = d - (21 - l);
                }
                break;
        }
    }

    public static boolean isLeapYear(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        return cal.getActualMaximum(Calendar.DAY_OF_YEAR) > 365;
    }
}
