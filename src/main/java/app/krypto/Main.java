package app.krypto;

public class Main {
    public static void main(String[] args) {
        KeyGenerator keyGenerator = new KeyGenerator();
        byte[] key = keyGenerator.generateKey();
        byte[] exkey = keyGenerator.keyExpansion(key);

        keyGenerator.printExpandedKey(exkey);

    }
}


