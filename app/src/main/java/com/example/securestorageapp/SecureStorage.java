package com.example.securestorageapp;
//
//import android.content.Context;
//import android.util.Base64;
//
//public class SecureStorage {
//    public static void saveEncryptedData(Context context, String key, String value) throws Exception {
//        byte[] associatedData = "associated_data".getBytes(StandardCharsets.UTF_8); // Define your associated data
//        byte[] encryptedValue = TinkHelper.encrypt(value, associatedData);
//
//        // Store the encrypted data in SharedPreferences
//        String encodedValue = Base64.encodeToString(encryptedValue, Base64.DEFAULT);
//        context.getSharedPreferences("secure_storage", Context.MODE_PRIVATE)
//                .edit()
//                .putString(key, encodedValue)
//                .apply();
//    }
//
//    public static String retrieveEncryptedData(Context context, String key) throws Exception {
//        // Retrieve the encrypted value from SharedPreferences
//        String encodedValue = context.getSharedPreferences("secure_storage", Context.MODE_PRIVATE)
//                .getString(key, null);
//        if (encodedValue == null) return null;
//
//        byte[] encryptedValue = Base64.decode(encodedValue, Base64.DEFAULT);
//        byte[] associatedData = "associated_data".getBytes(StandardCharsets.UTF_8); // Same associated data used during encryption
//        return TinkHelper.decrypt(encryptedValue, associatedData);
//    }
//}
//package com.example.securestorageapp;
//
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import android.util.Log;

public class SecureStorage {
    private static final String PREFS_NAME = "secure_prefs";

    public static void saveEncryptedData(Context context, String key, String value) {
        try {
            // Initialize Tink before using it
            TinkHelper.initialize(context);
            byte[] associatedData = "associated_data".getBytes(StandardCharsets.UTF_8);
            byte[] encryptedData = TinkHelper.encrypt(value, associatedData);
            String encodedData = Base64.encodeToString(encryptedData, Base64.DEFAULT);

            SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, encodedData);
            editor.apply();

            Log.d("SecureStorage", "Encrypted value stored: " + encodedData);  // Log for debugging
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
            byte[] associatedData = "associated_data".getBytes(StandardCharsets.UTF_8);  // Consistent associated data
            return TinkHelper.decrypt(encryptedData, associatedData);
        } catch (Exception e) {
            Log.e("SecureStorage", "Error retrieving encrypted data", e);  // Log error
            return null;
        }
    }
}
