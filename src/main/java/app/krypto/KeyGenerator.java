package app.krypto;

import java.security.SecureRandom;

public class KeyGenerator {
    private AES aes = new AES();

    byte[] generateKey(){
        byte[] key = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(key);
        return key;
    }

    byte[] keyExpansion(byte[] key){
        int Nk = 4; //liczba słów w kluczu głownym
        int Nb = 4; //liczba kolumn macierzy stanu
        int Nr = 10; //liczba rund
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
                temp = rotWord(temp); //rotacja słowa
                temp = subWord(temp); //substytucja bajtów
                if ((i / Nk) - 1 < rcon.length) {
                    temp[0] ^= rcon[(i / Nk) - 1]; // Użyj poprawnego indeksu z Rcon
                }
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
