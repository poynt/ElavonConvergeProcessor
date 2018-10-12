package com.elavon.converge.util;

public class RequestUtil {

    private static final String HYPHEN = "-";
    private static final String DEFAULT_POYNT_USER = "PoyntUser";

    public static String getDeviceUserValue(String firstName, String lastName, String nickname) {
        StringBuilder userBuilder = new StringBuilder();
        if (nickname != null && nickname.length() > 0) {
            userBuilder.append(nickname);
        }
        if (firstName != null && firstName.length() > 0) {
            if (userBuilder.length() > 0) {
                userBuilder.append(HYPHEN);
            }
            userBuilder.append(firstName);
        }
        if (lastName != null && lastName.length() > 0) {
            if (firstName != null && firstName.length() > 0){
                userBuilder.append(HYPHEN);
            }
            userBuilder.append(lastName);
        }
        if (userBuilder.length() < 1) {
            userBuilder.append(DEFAULT_POYNT_USER);
        }
        return userBuilder.toString();
    }
}
