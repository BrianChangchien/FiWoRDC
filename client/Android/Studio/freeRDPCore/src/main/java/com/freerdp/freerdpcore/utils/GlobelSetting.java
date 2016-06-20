package com.freerdp.freerdpcore.utils;

/**
 * Created by Brian_NB on 2016/6/3.
 */
public class GlobelSetting {
    public static final String sServicePort = "8080";

    public static boolean compareVersionNames(String oldVersionName, String newVersionName) {
        boolean bRes = false;

        String[] oldNumbers = oldVersionName.split("\\.");
        String[] newNumbers = newVersionName.split("\\.");

        // To avoid IndexOutOfBounds
        int maxIndex = Math.min(oldNumbers.length, newNumbers.length);

        for (int i = 0; i < maxIndex; i ++) {
            int oldVersionPart = Integer.valueOf(oldNumbers[i]);
            int newVersionPart = Integer.valueOf(newNumbers[i]);

            if (oldVersionPart < newVersionPart) {
                bRes = true;
                break;
            } else if (oldVersionPart > newVersionPart) {
                bRes = false;
                break;
            }
        }

        // If versions are the same so far, but they have different length...
        if (bRes == false && oldNumbers.length != newNumbers.length) {
            bRes = (oldNumbers.length > newNumbers.length)?true:false;
        }

        return bRes;
    }
}
