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
}

