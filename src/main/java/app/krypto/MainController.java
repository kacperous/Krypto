package app.krypto;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class MainController {
    @FXML
    private TextField keyValue1;
    @FXML
    private TextField keyValue2;
    @FXML
    private TextField keyValue3;
    @FXML
    private Button generateKeys;
    @FXML
    private TextArea inputTextArea;
    @FXML
    private TextArea outputTextArea;
    @FXML
    private Button openFile;
    @FXML
    private Button saveFile;
    @FXML
    private Button encrypt;
    @FXML
    private Button decrypt;
    @FXML
    private Button openFileEncrypted;
    @FXML
    private Button saveFileEncrypted;
    @FXML
    private RadioButton windowRadio;
    @FXML
    private RadioButton fileRadio;

    private KeyGenerator keyGenerator = new KeyGenerator();
    private AES aes = new AES();
    private byte[] expandedKey;
    private boolean isFileMode = false;

    @FXML
    public void initialize() {
        windowRadio.setSelected(true);
        // Dodatkowa inicjalizacja jeśli potrzebna
    }

    @FXML
    void generateKeysAction(ActionEvent event) {
        byte[] key = keyGenerator.generateKey();
        expandedKey = keyGenerator.keyExpansion(key);

        // Wypełnianie pól z wartościami klucza w formie hex
        StringBuilder key1 = new StringBuilder();
        StringBuilder key2 = new StringBuilder();
        StringBuilder key3 = new StringBuilder();

        for (int i = 0; i < 5; i++) {
            key1.append(String.format("%02X ", key[i]));
        }
        for (int i = 5; i < 10; i++) {
            key2.append(String.format("%02X ", key[i]));
        }
        for (int i = 10; i < 16; i++) {
            key3.append(String.format("%02X ", key[i]));
        }

        keyValue1.setText(key1.toString().trim());
        keyValue2.setText(key2.toString().trim());
        keyValue3.setText(key3.toString().trim());
    }

    @FXML
    void encryptAction(ActionEvent event) {
        if (expandedKey == null) {
            outputTextArea.setText("Najpierw wygeneruj klucz!");
            return;
        }

        String input = inputTextArea.getText();
        if (input.isEmpty()) {
            outputTextArea.setText("Wprowadź tekst do zaszyfrowania");
            return;
        }

        byte[] encryptedBytes = Utils.encryptUserInput(input, aes, expandedKey);

        // Wyświetl wynik jako tekst hex
        StringBuilder hexOutput = new StringBuilder();
        for (byte b : encryptedBytes) {
            hexOutput.append(String.format("%02X ", b));
        }
        outputTextArea.setText(hexOutput.toString());
    }

    @FXML
    void decryptAction(ActionEvent event) {
        if (expandedKey == null) {
            inputTextArea.setText("Najpierw wygeneruj klucz!");
            return;
        }

        String hexText = outputTextArea.getText().replaceAll("\\s+", "");
        if (hexText.isEmpty()) {
            inputTextArea.setText("Wprowadź zaszyfrowany tekst");
            return;
        }

        // Konwersja hex na bajty
        byte[] encryptedBytes = new byte[hexText.length() / 2];
        for (int i = 0; i < encryptedBytes.length; i++) {
            int index = i * 2;
            encryptedBytes[i] = (byte) Integer.parseInt(hexText.substring(index, index + 2), 16);
        }

        String decryptedData = Utils.decryptUserInput(encryptedBytes, aes, expandedKey);
        inputTextArea.setText(decryptedData);
    }

    @FXML
    void radioModeAction(ActionEvent event) {
        isFileMode = fileRadio.isSelected();
    }

    @FXML
    void openFileAction(ActionEvent event) {
        File file = chooseFile("Otwórz plik");
        if (file != null) {
            try {
                byte[] content = Files.readAllBytes(file.toPath());
                inputTextArea.setText(new String(content, StandardCharsets.UTF_8));
            } catch (IOException e) {
                inputTextArea.setText("Błąd odczytu pliku: " + e.getMessage());
            }
        }
    }

    @FXML
    void saveFileAction(ActionEvent event) {
        File file = saveFile("Zapisz plik");
        if (file != null) {
            try {
                Files.write(file.toPath(), inputTextArea.getText().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                inputTextArea.setText("Błąd zapisu pliku: " + e.getMessage());
            }
        }
    }

    @FXML
    void openFileEncryptedAction(ActionEvent event) {
        File file = chooseFile("Otwórz plik zaszyfrowany");
        if (file != null) {
            try {
                byte[] content = Files.readAllBytes(file.toPath());
                StringBuilder hexOutput = new StringBuilder();
                for (byte b : content) {
                    hexOutput.append(String.format("%02X ", b));
                }
                outputTextArea.setText(hexOutput.toString());
            } catch (IOException e) {
                outputTextArea.setText("Błąd odczytu pliku: " + e.getMessage());
            }
        }
    }

    @FXML
    void saveFileEncryptedAction(ActionEvent event) {
        File file = saveFile("Zapisz plik zaszyfrowany");
        if (file != null) {
            try {
                String hexText = outputTextArea.getText().replaceAll("\\s+", "");
                byte[] bytes = new byte[hexText.length() / 2];
                for (int i = 0; i < bytes.length; i++) {
                    int index = i * 2;
                    bytes[i] = (byte) Integer.parseInt(hexText.substring(index, index + 2), 16);
                }
                Files.write(file.toPath(), bytes);
            } catch (IOException e) {
                outputTextArea.setText("Błąd zapisu pliku: " + e.getMessage());
            }
        }
    }

    private File chooseFile(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        return fileChooser.showOpenDialog(getStage());
    }

    private File saveFile(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        return fileChooser.showSaveDialog(getStage());
    }

    private Stage getStage() {
        return (Stage) keyValue1.getScene().getWindow();
    }
}