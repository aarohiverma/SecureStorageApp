package com.example.securestorageapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import com.google.crypto.tink.integration.android.AndroidKeysetManager;

import javax.crypto.KeyGenerator;
import java.security.KeyStore;

public class TinkHelper {
    private static final String KEYSTORE_NAME = "AndroidKeyStore";
    private static final String KEY_ALIAS = "master_key";
    public static Aead aead;

    // SharedPreferences name
    private static final String PREFS_NAME = "SecurePrefs";
    private static final String ENCRYPTED_DATA_KEY = "encrypted_data";

    // Initialize Tink and generate/load the key from the Android Keystore
    public static void initialize(Context context) {
        try {
            // Register Tink configuration for AEAD
            AeadConfig.register();

            // Generate or load the key from the Android Keystore
            generateKey(context);

            // Build the AEAD primitive using Tink's API
            KeysetHandle keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES256_GCM);

            // Get AEAD primitive (Authenticated Encryption with Associated Data)
            aead = keysetHandle.getPrimitive(Aead.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error initializing Tink", e);
        }
    }

    private static void generateKey(Context context) throws Exception {
        // Get the Android Keystore instance
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_NAME);
        keyStore.load(null);

        // If the key does not exist, generate a new one
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            // Create a new AES key in the Keystore for encryption/decryption
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_NAME);
            keyGenerator.init(
                    new KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .build()
            );
            keyGenerator.generateKey();
        }

        // Now use the AndroidKeysetManager to create a Tink Keyset backed by the Keystore key
        KeysetHandle keysetHandle = new AndroidKeysetManager.Builder()
                .withSharedPref(context, "keyset", "keyset_prefs") // Store the keyset in SharedPreferences
                .withKeyTemplate(AeadKeyTemplates.AES256_GCM) // Use AES256_GCM key template from Tink
                .withMasterKeyUri("android-keystore://" + KEY_ALIAS) // Reference the Keystore key alias
                .build()
                .getKeysetHandle();

        // Use the keysetHandle to get the AEAD primitive for encryption/decryption
        aead = keysetHandle.getPrimitive(Aead.class);
    }

    // Encrypt the plainText using AEAD (Authenticated Encryption with Associated Data)
    public static byte[] encrypt(String plainText, byte[] associatedData) throws Exception {
        if (aead == null) {
            throw new IllegalStateException("TinkHelper is not initialized. Call initialize() first.");
        }

        // Encrypt the data using AEAD
        return aead.encrypt(plainText.getBytes(), associatedData);
    }

    // Decrypt the encrypted data
    public static String decrypt(byte[] cipherText, byte[] associatedData) throws Exception {
        if (aead == null) {
            throw new IllegalStateException("TinkHelper is not initialized. Call initialize() first.");
        }

        // Decrypt the data using AEAD
        byte[] decryptedData = aead.decrypt(cipherText, associatedData);
        return new String(decryptedData);
    }

    // Store encrypted data in SharedPreferences
    public static void encryptAndStoreData(Context context, String plainText, byte[] associatedData) throws Exception {
        byte[] encryptedData = encrypt(plainText, associatedData);

        // Store the encrypted data in SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ENCRYPTED_DATA_KEY, android.util.Base64.encodeToString(encryptedData, android.util.Base64.DEFAULT));
        editor.apply();
    }

    // Retrieve and decrypt the data from SharedPreferences
    public static String retrieveAndDecryptData(Context context, byte[] associatedData) throws Exception {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String encryptedDataString = sharedPreferences.getString(ENCRYPTED_DATA_KEY, null);

        if (encryptedDataString == null) {
            throw new IllegalStateException("No encrypted data found.");
        }

        byte[] encryptedData = android.util.Base64.decode(encryptedDataString, android.util.Base64.DEFAULT);
        return decrypt(encryptedData, associatedData);
    }
}
