package app.krypto;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Scanner;

public class Main extends Application {
//    public static void main(String[] args) {
//        Scanner scanner = new Scanner(System.in);
//
//        KeyGenerator keyGenerator = new KeyGenerator();
//        AES aes = new AES();
//
//        // 1. Generowanie klucza głównego i rozszerzonego
//        byte[] masterKey = keyGenerator.generateKey();
//        byte[] expandedKey = keyGenerator.keyExpansion(masterKey);
//
//        System.out.println("Podaj tekst do zaszyfrowania:");
//        String userInput = scanner.nextLine();
//
//        // 2. Szyfrowanie wejścia użytkownika
//        byte[] encryptedData = Utils.encryptUserInput(userInput, aes, expandedKey);
//        System.out.println("\nZaszyfrowane dane (hex):");
//        printHex(encryptedData);
//
//        // 3. Deszyfrowanie danych
//        String decryptedData = Utils.decryptUserInput(encryptedData, aes, expandedKey);
//        System.out.println("\nOdszyfrowane dane:");
//        System.out.println(decryptedData);
//    }
//
//    private static void printHex(byte[] data) {
//        for (byte b : data) {
//            System.out.printf("%02x ", b);
//        }
//        System.out.println();
//    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Main.fxml"));
            primaryStage.setTitle("Program Krypto AES");
            primaryStage.setScene(new Scene(root));
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
