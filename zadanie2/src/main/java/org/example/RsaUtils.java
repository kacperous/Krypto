package org.example;

import java.math.BigInteger;
import java.io.IOException; 
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;

public class RsaUtils {
    public static class KeyPair {
        public final BigInteger n, e, d;
        public KeyPair(BigInteger n, BigInteger e, BigInteger d) {
            this.n = n;
            this.e = e;
            this.d = d;
        }
    }

    private static final String BLOCK_SEPARATOR = " ";

    public static KeyPair generateKeyPair(int bits) {
        SecureRandom random = new SecureRandom();
        BigInteger p = BigInteger.probablePrime(bits/2, random);
        BigInteger q = BigInteger.probablePrime(bits/2, random);
        BigInteger n = p.multiply(q);
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        BigInteger e = BigInteger.valueOf(65537);
        while (!phi.gcd(e).equals(BigInteger.ONE)) { e = e.add(BigInteger.TWO); }
        BigInteger d = e.modInverse(phi);
        return new KeyPair(n, e, d);
    }

    public static String encrypt(byte[] dataBytes, BigInteger n, BigInteger e) throws Exception {
        int blockSize = (n.bitLength() - 1) / 8;
        if (blockSize <= 0) {
            throw new IllegalArgumentException("Klucz RSA jest za mały do zaszyfrowania danych (rozmiar bloku <= 0).");
        }

        StringBuilder encryptedHexBuilder = new StringBuilder();
        int numBlocks = (dataBytes.length == 0) ? 1 : (dataBytes.length + blockSize - 1) / blockSize;

        for (int i = 0; i < numBlocks; i++) {
            byte[] currentBlockPadded = new byte[blockSize]; 
            int dataLengthInBlock;

            if (dataBytes.length == 0 && i == 0) { 
                dataLengthInBlock = 0;
            } else {
                int start = i * blockSize;
                dataLengthInBlock = Math.min(blockSize, dataBytes.length - start);
                System.arraycopy(dataBytes, start, currentBlockPadded, 0, dataLengthInBlock);
            }
            
            if (dataLengthInBlock < blockSize) {
                currentBlockPadded[dataLengthInBlock] = (byte) 0x80;
                for (int j = dataLengthInBlock + 1; j < blockSize; j++) {
                    currentBlockPadded[j] = (byte) 0x00;
                }
            }

            BigInteger messageChunk = new BigInteger(1, currentBlockPadded);
            if (messageChunk.compareTo(n) >= 0) {
                throw new Exception("Błąd wewnętrzny: blok danych jest zbyt duży dla tego klucza RSA.");
            }
            BigInteger encryptedChunk = messageChunk.modPow(e, n);

            if (encryptedHexBuilder.length() > 0) {
                encryptedHexBuilder.append(BLOCK_SEPARATOR);
            }
            encryptedHexBuilder.append(encryptedChunk.toString(16));
        }
        return encryptedHexBuilder.toString();
    }

    public static byte[] decrypt(String cipherHexBlocks, BigInteger n, BigInteger d) throws Exception {
        if (cipherHexBlocks == null || cipherHexBlocks.isEmpty()) {
            return new byte[0];
        }

        String[] cipherHexArray = cipherHexBlocks.split(BLOCK_SEPARATOR);
        ByteArrayOutputStream decryptedBytesStream = new ByteArrayOutputStream();
        
        int blockSize = (n.bitLength() - 1) / 8;
        if (blockSize <= 0) {
            throw new IllegalArgumentException("Klucz RSA jest za mały do odszyfrowania danych (rozmiar bloku <= 0).");
        }

        for (int k = 0; k < cipherHexArray.length; k++) {
            String cipherHex = cipherHexArray[k];
            if (cipherHex.isEmpty()) {
                if (cipherHexArray.length == 1) return new byte[0];
                continue;
            }

            BigInteger encryptedChunk = new BigInteger(cipherHex, 16);
            BigInteger decryptedChunk = encryptedChunk.modPow(d, n);
            
            byte[] currentBlockBytes = new byte[blockSize]; 

            byte[] tempBytes = decryptedChunk.toByteArray();
            int sourceStartOffset = 0;
            int bytesToCopyLength = tempBytes.length;

            if (tempBytes.length > blockSize) {
                if (tempBytes[0] == 0 && tempBytes.length == blockSize + 1) {
                    sourceStartOffset = 1;
                    bytesToCopyLength = blockSize;
                } else {
                    throw new Exception("Zdeszyfrowany blok jest nieoczekiwanie duży.");
                }
            }
            
            int destinationStartOffset = blockSize - bytesToCopyLength;
             if (destinationStartOffset < 0) {
                throw new Exception("Błąd wewnętrzny: ujemny offset docelowy przy rekonstrukcji bloku.");
            }
            System.arraycopy(tempBytes, sourceStartOffset, currentBlockBytes, destinationStartOffset, bytesToCopyLength);

            int lengthToWrite = blockSize; 

            if (k == cipherHexArray.length - 1) {
                int actualDataLengthInBlock = blockSize;
                if (blockSize > 0) {
                    int idx = blockSize - 1;
                    while (idx >= 0 && currentBlockBytes[idx] == (byte)0x00) {
                        idx--;
                    }
                    if (idx >= 0 && currentBlockBytes[idx] == (byte)0x80) {
                        actualDataLengthInBlock = idx; 
                    }
                } else {
                    actualDataLengthInBlock = 0; 
                }
                lengthToWrite = actualDataLengthInBlock;
            }
            
            if (lengthToWrite > 0) {
                decryptedBytesStream.write(currentBlockBytes, 0, lengthToWrite);
            }
        }
        
        return decryptedBytesStream.toByteArray();
    }

    public static void saveKeys(String publicPath, String privatePath, KeyPair keyPair) throws IOException {
        String publicKeyContent = keyPair.n.toString(16) + "\n" + keyPair.e.toString(16);
        Files.write(Paths.get(publicPath), publicKeyContent.getBytes(StandardCharsets.UTF_8));
        
        String privateKeyContent = keyPair.n.toString(16) + "\n" + keyPair.d.toString(16);
        Files.write(Paths.get(privatePath), privateKeyContent.getBytes(StandardCharsets.UTF_8));
    }

    public static BigInteger[] loadPublicKey(String path) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        String[] lines = content.split("\\R"); 
        if (lines.length < 2) throw new IOException("Nieprawidłowy format pliku klucza publicznego.");
        return new BigInteger[]{ new BigInteger(lines[0], 16), new BigInteger(lines[1], 16) };
    }

    public static BigInteger[] loadPrivateKey(String path) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        String[] lines = content.split("\\R");
        if (lines.length < 2) throw new IOException("Nieprawidłowy format pliku klucza prywatnego.");
        return new BigInteger[]{ new BigInteger(lines[0], 16), new BigInteger(lines[1], 16) };
    }
}