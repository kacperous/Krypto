package app.krypto;

import java.security.SecureRandom;

public class KeyGenerator {
    private AES aes = new AES();

    public byte[] generateKey(int keySize){
        byte[] key = new byte[keySize / 8];
        SecureRandom random = new SecureRandom();
        random.nextBytes(key);
        return key;
    }

    byte[] generateKey(){
        return generateKey(128);
    }

    public int calculateRounds(int Nk) {
        switch(Nk) {
            case 4: return 10;
            case 6: return 12;
            case 8: return 14;
            default: return 10;
        }
    }

    byte[] keyExpansion(byte[] key){
        int keyLength = key.length;
        int Nk = keyLength / 4;
        int Nb = 4;
        int Nr = calculateRounds(Nk);
        int expandedKeySize = Nb * (Nr + 1);

        byte[] expandedKey = new byte[expandedKeySize * 4];

        for(int i=0; i<key.length; i++){
            expandedKey[i] = key[i];
        }

        byte[] temp = new byte[4];
        for(int i=Nk; i<expandedKeySize; i++){
            for(int j=0; j<4; j++){
                temp[j] = expandedKey[(i - 1) * 4 + j];
            }

            if(i % Nk == 0){
                temp = rotWord(temp);
                temp = subWord(temp);
                if ((i / Nk) - 1 < rcon.length) {
                    temp[0] ^= rcon[(i / Nk) - 1];
                }
            } else if (Nk > 6 && i % Nk == 4) {
                temp = subWord(temp);
            }

            for(int j=0; j<4; j++){
                expandedKey[i * 4 + j] = (byte) (expandedKey[(i - Nk) * 4 + j] ^ temp[j]);
            }
        }
        return expandedKey;
    }

    byte[] rotWord(byte[] word){
        byte[] temp = new byte[4];
        for(int i=0; i<4; i++){
            temp[i] = word[(i + 1) % 4];
        }
        return temp;
    }

    byte[] subWord(byte[] word){
        for(int i=0; i<4; i++){
            word[i] = (byte) aes.sbox[word[i] & 0xFF];
        }
        return word;
    }

    byte[] rcon = {
            (byte) 0x01, (byte) 0x02, (byte) 0x04, (byte) 0x08,
            (byte) 0x10, (byte) 0x20, (byte) 0x40, (byte) 0x80,
            (byte) 0x1B, (byte) 0x36
    };
}
