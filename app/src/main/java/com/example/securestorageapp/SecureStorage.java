package com.example.securestorageapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

public class SecureStorage {
    private static final String PREFS_NAME = "secure_prefs";

    public static void saveEncryptedData(Context context, String key, String value) {
        try {
            byte[] encryptedData = TinkHelper.encrypt(value, null);
            String encodedData = Base64.encodeToString(encryptedData, Base64.DEFAULT);

            SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, encodedData);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String retrieveEncryptedData(Context context, String key) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String encodedData = sharedPreferences.getString(key, null);
            if (encodedData == null) return null;

            byte[] encryptedData = Base64.decode(encodedData, Base64.DEFAULT);
            return TinkHelper.decrypt(encryptedData, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
