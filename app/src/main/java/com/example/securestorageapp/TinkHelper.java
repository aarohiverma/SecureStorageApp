package com.example.securestorageapp;


import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;

public class TinkHelper {
    private static Aead aead;

    static {
        try {
            // Initialize Tink
            AeadConfig.register();
            // Generate a new keyset
            KeysetHandle keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES256_GCM);
            // Get the AEAD primitive
            aead = keysetHandle.getPrimitive(Aead.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] encrypt(String plainText, byte[] associatedData) throws Exception {
        return aead.encrypt(plainText.getBytes(), associatedData);
    }

    public static String decrypt(byte[] cipherText, byte[] associatedData) throws Exception {
        return new String(aead.decrypt(cipherText, associatedData));
    }
}
