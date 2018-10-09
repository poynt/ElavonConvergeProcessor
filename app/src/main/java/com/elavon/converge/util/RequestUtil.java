package com.elavon.converge.util;

public class RequestUtil {

    private static final String HYPHEN = "-";
    private static final String DEFAULT_POYNT_USER = "PoyntUser";

    public static String getDeviceUserValue(String firstName, String lastName, String nickname) {
        StringBuilder userBuilder = new StringBuilder();
        if (nickname != null && nickname.length() > 0) {
            if((firstName != null && firstName.length() > 0) ||
                    (lastName != null && lastName.length() > 0)) {
                userBuilder.append(nickname).append(HYPHEN);
            }
        }
        if (firstName != null && firstName.length() > 0) {
            if(lastName != null && lastName.length() > 0) {
                userBuilder.append(firstName).append(HYPHEN);
            }
        }
        if (lastName != null && lastName.length() > 0) {
            userBuilder.append(lastName);
        }
        if (userBuilder.length() < 1) {
            userBuilder.append(DEFAULT_POYNT_USER);
        }
        return userBuilder.toString();
    }
}
