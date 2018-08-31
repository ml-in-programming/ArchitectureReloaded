package org.jetbrains.research.groups.ml_methods.error_reporting;


import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Provides functionality to encode and decode secret tokens to make them not directly readable. Let me be clear:
 * THIS IS THE OPPOSITE OF SECURITY!
 */
public class GitHubAccessTokenScrambler {
    private static final String myInitVector = "RandomInitVector";
    private static final  String myKey = "GitHubErrorToken";

    public static void main(String[] args) {
        if (args.length != 2) {
            return;
        }
        String horse = args[0];
        String outputFile = args[1];
        try {
            final String e = encrypt(horse);
            final ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(outputFile));
            o.writeObject(e);
            o.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(myInitVector.getBytes("UTF-8"));
            SecretKeySpec keySpec = new SecretKeySpec(myKey.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.encodeBase64String(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    static String decrypt(InputStream inputStream) throws Exception {
        String in;
        final ObjectInputStream o = new ObjectInputStream(inputStream);
        in = (String) o.readObject();
        IvParameterSpec iv = new IvParameterSpec(myInitVector.getBytes("UTF-8"));
        SecretKeySpec keySpec = new SecretKeySpec(myKey.getBytes("UTF-8"), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);

        byte[] original = cipher.doFinal(Base64.decodeBase64(in));
        return new String(original);
    }
}
