package app.krypto;

import java.util.Arrays;

public class Utils {
    public static byte[] padBlock(byte[] input) {
        int blockSize = 16;
        int paddingLength = blockSize - (input.length % blockSize);
        byte[] paddedBlock = Arrays.copyOf(input, input.length + paddingLength);

        for (int i = input.length; i < paddedBlock.length; i++) {
            paddedBlock[i] = (byte) paddingLength;
        }
        return paddedBlock;
    }

    public static byte[] removePadding(byte[] input) {
        int paddingLength = input[input.length - 1];
        return Arrays.copyOf(input, input.length - paddingLength);
    }
}

