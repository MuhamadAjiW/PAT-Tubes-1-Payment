package payment.util;

import org.apache.commons.lang3.RandomStringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

// Naive implementation of signed urls using AES
public class SignatureUtil {
    public static final Duration PaymentExpiry = Duration.ofSeconds(24 * 60 * 60);

    private static final SecretKey secretKey = new SecretKeySpec("signedsignedsignedsignedsignedee".getBytes(), "AES");
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss.SSS").withZone(ZoneId.systemDefault());

    public static String generateSignature(String identifier, Duration expiryFromNow){
        try{
            Instant expiration = Instant.now().plus(expiryFromNow);
            String formattedTimestamp = timeFormatter.format(expiration);
            String salt = RandomStringUtils.randomAlphabetic(20);;
            return encryptSignature(identifier + "|" + formattedTimestamp + "|" + salt);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static boolean verifySignature(String signature) throws Exception{
        String decryptedSignature = decryptSignature(signature);
        String[] parts = decryptedSignature.split("\\|");

        if (parts.length == 3){
            Instant expirationTime = Instant.from(timeFormatter.parse(parts[1]));
            return Instant.now().isBefore(expirationTime);
        } else {
            return false;
        }
    }

    public static String getIdentifier(String signature) throws Exception{
        String decryptedSignature = decryptSignature(signature);
        String[] parts = decryptedSignature.split("\\|");

        if (parts.length == 3){
            return parts[0];
        } else {
            return null;
        }
    }

    private static String encryptSignature(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getUrlEncoder().encodeToString(encryptedBytes);
    }

    private static String decryptSignature(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getUrlDecoder().decode(data));
        return new String(decryptedBytes);
    }
}
