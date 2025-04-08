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
            case 4: return 10; // 128 bitów
            case 6: return 12; // 192 bity
            case 8: return 14; // 256 bitów
            default: return 10; // Domyślnie 10 rund
        }
    }

    byte[] keyExpansion(byte[] key){
        int keyLength = key.length;
        int Nk = keyLength / 4; //liczba słów w kluczu głownym
        int Nb = 4; //liczba kolumn macierzy stanu
        int Nr = calculateRounds(Nk); //liczba rund
        int expandedKeySize = Nb * (Nr + 1); //liczba słów w rozszerzonym kluczu

        byte[] expandedKey = new byte[expandedKeySize * 4]; //rozszerzony klucz

        for(int i=0; i<key.length; i++){
            expandedKey[i] = key[i]; //kopiujemy klucz główny do rozszerzonego klucza
        }

        byte[] temp = new byte[4]; //tymczasowa tablica do przechowywania słowa
        for(int i=Nk; i<expandedKeySize; i++){
            for(int j=0; j<4; j++){
                temp[j] = expandedKey[(i - 1) * 4 + j]; //pobieramy ostatnie słowo z rozszerzonego klucza
            }

            if(i % Nk == 0){
                temp = rotWord(temp); // Rotacja słowa
                temp = subWord(temp); // Substytucja bajtów
                if ((i / Nk) - 1 < rcon.length) {
                    temp[0] ^= rcon[(i / Nk) - 1]; // Użyj poprawnego indeksu z Rcon
                }
            } else if (Nk > 6 && i % Nk == 4) {
                // Dodatkowy krok dla 256-bitowego klucza
                temp = subWord(temp);
            }

            for(int j=0; j<4; j++){
                expandedKey[i * 4 + j] = (byte) (expandedKey[(i - Nk) * 4 + j] ^ temp[j]); //rozszerzamy klucz
            }
        }
        return expandedKey;
    }

    byte[] rotWord(byte[] word){
        byte[] temp = new byte[4];
        for(int i=0; i<4; i++){
            temp[i] = word[(i + 1) % 4]; //rotacja słowa
        }
        return temp;
    }

    byte[] subWord(byte[] word){
        for(int i=0; i<4; i++){
            word[i] = (byte) aes.sbox[word[i] & 0xFF]; //substytucja bajtów
        }
        return word;
    }

    byte[] rcon = {
            (byte) 0x01, (byte) 0x02, (byte) 0x04, (byte) 0x08,
            (byte) 0x10, (byte) 0x20, (byte) 0x40, (byte) 0x80,
            (byte) 0x1B, (byte) 0x36
    };
}
