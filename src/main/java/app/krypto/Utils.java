package app.krypto;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Utils {
    public static byte[] padBlock(byte[] input) {
        int blockSize = 16; // Rozmiar bloku w AES
        int paddingLength = blockSize - (input.length % blockSize); // Liczba bajtów potrzebnych do wypełnienia
        byte[] paddedBlock = Arrays.copyOf(input, input.length + paddingLength);

        // Wypełnienie paddingiem według PKCS#7
        for (int i = input.length; i < paddedBlock.length; i++) {
            paddedBlock[i] = (byte) paddingLength;
        }
        return paddedBlock;
    }

    public static byte[] removePadding(byte[] input) {
        int paddingLength = input[input.length - 1]; // Ostatni bajt zawiera długość paddingu
        return Arrays.copyOf(input, input.length - paddingLength);
    }

    public static byte[] stringToBytes(String text) {
        return text.getBytes(StandardCharsets.UTF_8); // Zamiana na tablicę bajtów (UTF-8)
    }

    public static byte[] encryptUserInput(String text, AES aes, byte[] expandedKey) {
        // Zamiana tekstu na bytes + padding
        byte[] inputBytes = stringToBytes(text);
        byte[] paddedInput = padBlock(inputBytes);

        // Szyfrowanie za pomocą AES
        return aes.aesEncrypt(paddedInput, expandedKey);
    }

    public static String decryptUserInput(byte[] encryptedData, AES aes, byte[] expandedKey) {
        // Deszyfrowanie za pomocą AES
        byte[] decryptedBytes = aes.aesDecrypt(encryptedData, expandedKey);

        // Usunięcie paddingu
        byte[] unpaddedData = removePadding(decryptedBytes);

        // Zamiana na tekst
        return new String(unpaddedData, StandardCharsets.UTF_8);
    }
}

