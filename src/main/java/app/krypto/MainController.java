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
import java.util.Arrays;
import java.util.Base64;

public class MainController {
    @FXML
    private TextField keyValueField;
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
    @FXML
    private RadioButton key128Radio;
    @FXML
    private RadioButton key192Radio;
    @FXML
    private RadioButton key256Radio;

    private KeyGenerator keyGenerator = new KeyGenerator();
    private AES aes = new AES();
    private byte[] expandedKey;
    private boolean isFileMode = false;

    // Zmienne do przechowywania danych binarnych
    private byte[] inputFileBytes = null;
    private byte[] outputFileBytes = null;
    private boolean isInputBinary = false;
    private boolean isOutputBinary = false;
    private String lastInputFileName = "";
    private String lastOutputFileName = "";

    @FXML
    public void initialize() {
        windowRadio.setSelected(true);
        key128Radio.setSelected(true);
    }

    @FXML
    void generateKeysAction(ActionEvent event) {
        int keySize = 128;

        if (key192Radio.isSelected()) {
            keySize = 192;
        } else if (key256Radio.isSelected()) {
            keySize = 256;
        }

        byte[] key = keyGenerator.generateKey(keySize);
        expandedKey = keyGenerator.keyExpansion(key);

        int Nk = key.length / 4;
        int rounds = keyGenerator.calculateRounds(Nk);
        aes.setNumberOfRounds(rounds);

        StringBuilder keyString = new StringBuilder();
        for (int i = 0; i < key.length; i++) {
            keyString.append(String.format("%02X ", key[i]));
        }
        keyValueField.setText(keyString.toString().trim());
    }

    @FXML
    void encryptAction(ActionEvent event) {
        if (expandedKey == null) {
            outputTextArea.setText("Najpierw wygeneruj klucz!");
            return;
        }

        byte[] dataToEncrypt;

        // Sprawdź czy pracujemy z danymi binarnymi
        if (isInputBinary && inputFileBytes != null) {
            dataToEncrypt = inputFileBytes;
        } else {
            // Pobierz tekst z pola tekstowego
            String inputText = inputTextArea.getText();
            dataToEncrypt = inputText.getBytes(StandardCharsets.UTF_8);
        }

        // Przygotuj dane do szyfrowania (dodaj padding)
        byte[] paddedData = Utils.padBlock(dataToEncrypt);

        // Zaszyfruj dane
        byte[] encryptedBytes = aes.aesEncrypt(paddedData, expandedKey);

        // Zachowaj zaszyfrowane dane do późniejszego zapisu
        outputFileBytes = encryptedBytes;
        isOutputBinary = true;

        // Zachowaj informację o nazwie pliku
        lastOutputFileName = lastInputFileName;

        StringBuilder hexOutput = new StringBuilder();
        for (byte b : encryptedBytes) {
            hexOutput.append(String.format("%02X ", b & 0xFF));
        }
        outputTextArea.setText(hexOutput.toString());
    }

    @FXML
    void decryptAction(ActionEvent event) {
        if (expandedKey == null) {
            inputTextArea.setText("Najpierw wygeneruj klucz!");
            return;
        }

        byte[] dataToDecrypt;

        // Sprawdź czy mamy dane binarne do odszyfrowania
        if (isOutputBinary && outputFileBytes != null) {
            dataToDecrypt = outputFileBytes;
        } else {
            // Próbuj przekonwertować hex na bajty
            String hexText = outputTextArea.getText().replaceAll("\\s+", "");
            if (hexText.startsWith("[ZASZYFROWANY") || hexText.startsWith("[ZASZYFROWANE")) {
                inputTextArea.setText("Użyj przycisku 'Otwórz' aby wczytać plik zaszyfrowany");
                return;
            }

            try {
                dataToDecrypt = new byte[hexText.length() / 2];
                for (int i = 0; i < dataToDecrypt.length; i++) {
                    dataToDecrypt[i] = (byte) Integer.parseInt(
                            hexText.substring(i * 2, (i * 2) + 2), 16);
                }
            } catch (Exception e) {
                inputTextArea.setText("Błąd podczas parsowania danych hex: " + e.getMessage());
                return;
            }
        }

        // Odszyfruj dane
        byte[] decryptedData = aes.aesDecrypt(dataToDecrypt, expandedKey);

        try {
            // Usuń padding
            byte[] unpaddedData = Utils.removePadding(decryptedData);

            // Sprawdź czy dane są binarne
            boolean looksLikeBinary = isBinaryContent(unpaddedData);

            // Zachowaj odszyfrowane dane
            inputFileBytes = unpaddedData;
            isInputBinary = looksLikeBinary;
            lastInputFileName = lastOutputFileName;

            if (looksLikeBinary) {
                // Pokaż informację o pliku binarnym
                inputTextArea.setText("[ODSZYFROWANY PLIK BINARNY]" +
                        (lastOutputFileName.isEmpty() ? "" : " - " + lastOutputFileName) +
                        "\nRozmiar: " + unpaddedData.length + " bajtów" +
                        "\nUżyj przycisku 'Zapisz' aby zapisać plik.");
            } else {
                // Pokaż jako tekst jeśli nie wygląda na binarne dane
                String decryptedText = new String(unpaddedData, StandardCharsets.UTF_8);
                inputTextArea.setText(decryptedText);
                isInputBinary = false;
            }
        } catch (Exception e) {
            inputTextArea.setText("Błąd podczas deszyfrowania: " + e.getMessage());
        }
    }

    // Pomocnicza metoda do wykrywania zawartości binarnej
    private boolean isBinaryContent(byte[] data) {
        // Zakładamy, że pliki PDF zawsze zaczynają się od %PDF
        if (data.length >= 4 &&
                data[0] == '%' && data[1] == 'P' && data[2] == 'D' && data[3] == 'F') {
            return true;
        }

        // Sprawdzamy czy dane wyglądają na binarne
        int binaryCount = 0;
        int sampleSize = Math.min(100, data.length);

        for (int i = 0; i < sampleSize; i++) {
            byte b = data[i];
            if (b == 0 || (b > 0 && b < 9) || (b > 14 && b < 32 && b != 10 && b != 13)) {
                binaryCount++;
            }
        }

        return binaryCount > (sampleSize * 0.1);
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

                // Sprawdź czy zawartość wygląda na binarną
                isInputBinary = isBinaryContent(content);
                inputFileBytes = content;
                lastInputFileName = file.getName();

                if (isInputBinary) {
                    // Pokaż informację o pliku binarnym
                    inputTextArea.setText("[PLIK BINARNY] " + file.getName() +
                            "\nRozmiar: " + content.length + " bajtów" +
                            "\nPlik zostanie zaszyfrowany jako dane binarne.");
                } else {
                    // Pokaż jako tekst, jeśli nie jest binarny
                    String text = new String(content, StandardCharsets.UTF_8);
                    inputTextArea.setText(text);
                }
            } catch (IOException e) {
                inputTextArea.setText("Błąd podczas wczytywania pliku: " + e.getMessage());
            }
        }
    }

    @FXML
    void saveFileAction(ActionEvent event) {
        File file = saveFile("Zapisz plik");
        if (file != null) {
            try {
                byte[] dataToSave;

                if (isInputBinary && inputFileBytes != null) {
                    // Zapisz dane binarne bez modyfikacji
                    dataToSave = inputFileBytes;
                } else {
                    // Zapisz tekst
                    dataToSave = inputTextArea.getText().getBytes(StandardCharsets.UTF_8);
                }

                Files.write(file.toPath(), dataToSave);
                inputTextArea.setText("Plik został zapisany: " + file.getAbsolutePath());
            } catch (IOException e) {
                inputTextArea.setText("Błąd podczas zapisywania pliku: " + e.getMessage());
            }
        }
    }

    @FXML
    void openFileEncryptedAction(ActionEvent event) {
        File file = chooseFile("Otwórz plik zaszyfrowany");
        if (file != null) {
            try {
                byte[] content = Files.readAllBytes(file.toPath());
                outputFileBytes = content;
                isOutputBinary = true;
                lastOutputFileName = file.getName();

                outputTextArea.setText("[ZASZYFROWANY PLIK] " + file.getName() +
                        "\nRozmiar: " + content.length + " bajtów" +
                        "\nPróbka: " +
                        Base64.getEncoder().encodeToString(
                                Arrays.copyOf(content, Math.min(50, content.length))) + "...");
            } catch (IOException e) {
                outputTextArea.setText("Błąd podczas wczytywania pliku: " + e.getMessage());
            }
        }
    }

    @FXML
    void saveFileEncryptedAction(ActionEvent event) {
        File file = saveFile("Zapisz plik zaszyfrowany");
        if (file != null) {
            try {
                if (outputFileBytes != null) {
                    Files.write(file.toPath(), outputFileBytes);
                    outputTextArea.setText("Plik zaszyfrowany został zapisany: " + file.getAbsolutePath());
                } else {
                    outputTextArea.setText("Brak danych do zapisania.");
                }
            } catch (IOException e) {
                outputTextArea.setText("Błąd podczas zapisywania pliku: " + e.getMessage());
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
        return (Stage) keyValueField.getScene().getWindow();
    }
}