package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import java.io.File;
import java.math.BigInteger;

public class Controller {

    @FXML private TextField privateKeyPathField;
    @FXML private TextField publicKeyPathField;
    @FXML private TextArea plainTextArea;
    @FXML private TextArea encryptedTextArea;
    @FXML private TextArea decryptedTextArea;
    @FXML private TextField inputFileField;
    @FXML private TextField encryptedFileField;
    @FXML private Label statusLabel;
    @FXML private ComboBox<Integer> keySizeBox;


    private BigInteger[] privateKey;   // = [n, d]
    private BigInteger[] publicKey;    // = [n, e]
    @FXML
    public void initialize() {
        keySizeBox.getItems().setAll(1024, 2048, 4096);
        keySizeBox.setValue(2048);
    }

    @FXML
    private void onGenKeys(ActionEvent e) {
        try {
            int bits = keySizeBox.getValue();
            RsaUtils.KeyPair kp = RsaUtils.generateKeyPair(bits);
            RsaUtils.saveKeys("public.key", "private.key", kp);
            publicKey = new BigInteger[] {kp.n, kp.e};
            privateKey = new BigInteger[] {kp.n, kp.d};
            privateKeyPathField.setText(new File("private.key").getAbsolutePath());
            publicKeyPathField.setText(new File("public.key").getAbsolutePath());
            statusLabel.setText("Wygenerowano i zapisano nowe klucze!");
            statusLabel.setStyle("-fx-text-fill: green");
        } catch (Exception ex) {
            statusLabel.setText("Błąd generowania kluczy: " + ex.getMessage());
            statusLabel.setStyle("-fx-text-fill: red");
        }
    }

    @FXML
    private void onLoadPrivateKey(ActionEvent e) {
        File file = chooseFile("Wybierz plik klucza prywatnego");
        if (file == null) return;
        try {
            privateKey = RsaUtils.loadPrivateKey(file.getAbsolutePath());
            privateKeyPathField.setText(file.getAbsolutePath());
            statusLabel.setText("Załadowano klucz prywatny.");
            statusLabel.setStyle("-fx-text-fill: green");
        } catch (Exception ex) {
            statusLabel.setText("Błąd ładowania klucza prywatnego: " + ex.getMessage());
            statusLabel.setStyle("-fx-text-fill: red");
        }
    }

    @FXML
    private void onLoadPublicKey(ActionEvent e) {
        File file = chooseFile("Wybierz plik klucza publicznego");
        if (file == null) return;
        try {
            publicKey = RsaUtils.loadPublicKey(file.getAbsolutePath());
            publicKeyPathField.setText(file.getAbsolutePath());
            statusLabel.setText("Załadowano klucz publiczny.");
            statusLabel.setStyle("-fx-text-fill: green");
        } catch (Exception ex) {
            statusLabel.setText("Błąd ładowania klucza publicznego: " + ex.getMessage());
            statusLabel.setStyle("-fx-text-fill: red");
        }
    }

    @FXML
    private void onEncryptText(ActionEvent e) {
        try {
            if (publicKey == null) throw new Exception("Załaduj klucz publiczny!");
            String plain = plainTextArea.getText();
            byte[] plainBytes = plain.getBytes(StandardCharsets.UTF_8);
            String cipherHex = RsaUtils.encrypt(plainBytes, publicKey[0], publicKey[1]);
            encryptedTextArea.setText(cipherHex); 
            statusLabel.setText("Tekst został zaszyfrowany (HEX).");
            statusLabel.setStyle("-fx-text-fill: green");
        } catch (Exception ex) {
            statusLabel.setText("Błąd szyfrowania: " + ex.getMessage());
            statusLabel.setStyle("-fx-text-fill: red");
        }
    }

    @FXML
    private void onDecryptText(ActionEvent e) {
        try {
            if (privateKey == null) throw new Exception("Załaduj klucz prywatny!");
            String cipherHex = encryptedTextArea.getText();
            byte[] decryptedBytes = RsaUtils.decrypt(cipherHex, privateKey[0], privateKey[1]);
            String decrypted = new String(decryptedBytes, StandardCharsets.UTF_8);
            decryptedTextArea.setText(decrypted);
            statusLabel.setText("Tekst został odszyfrowany.");
            statusLabel.setStyle("-fx-text-fill: green");
        } catch (Exception ex) {
            statusLabel.setText("Błąd deszyfrowania: " + ex.getMessage());
            statusLabel.setStyle("-fx-text-fill: red");
        }
    }

    @FXML
    private void onChooseInputFile(ActionEvent e) {
        File file = chooseFile("Wybierz plik do zaszyfrowania");
        if (file != null) inputFileField.setText(file.getAbsolutePath());
    }

    @FXML
    private void onChooseEncryptedFile(ActionEvent e) {
        File file = chooseFile("Wybierz plik do deszyfrowania");
        if (file != null) encryptedFileField.setText(file.getAbsolutePath());
    }

    @FXML
    private void onEncryptFile(ActionEvent e) {
        try {
            if (publicKey == null) throw new Exception("Załaduj klucz publiczny!");
            String filePath = inputFileField.getText();
            if (filePath.isEmpty()) throw new Exception("Wskaż plik do zaszyfrowania!");
            
            byte[] fileBytes = FileUtils.readFileToBytes(filePath); 
            String cipherHex = RsaUtils.encrypt(fileBytes, publicKey[0], publicKey[1]);

            String originalFileName = Paths.get(filePath).getFileName().toString();
            String suggestedFileName = originalFileName + ".encrypted.txt"; 
            File outputFile = chooseSaveFile("Zapisz zaszyfrowany plik", suggestedFileName);

            if (outputFile != null) {
                FileUtils.writeStringToFile(cipherHex, outputFile.getAbsolutePath());
                statusLabel.setText("Zaszyfrowano plik do: " + outputFile.getAbsolutePath());
                statusLabel.setStyle("-fx-text-fill: green");
            } else {
                statusLabel.setText("Anulowano zapisywanie zaszyfrowanego pliku.");
                statusLabel.setStyle("-fx-text-fill: orange");
            }
        } catch (Exception ex) {
            statusLabel.setText("Błąd szyfrowania pliku: " + ex.getMessage());
            statusLabel.setStyle("-fx-text-fill: red");
        }
    }

    @FXML
    private void onDecryptFile(ActionEvent e) {
        try {
            if (privateKey == null) throw new Exception("Załaduj klucz prywatny!");
            String filePath = encryptedFileField.getText();
            if (filePath.isEmpty()) throw new Exception("Wskaż plik do deszyfrowania!");
            
            String cipherHex = FileUtils.readFileToString(filePath); 
            byte[] decryptedBytes = RsaUtils.decrypt(cipherHex, privateKey[0], privateKey[1]); 

            String originalFileName = Paths.get(filePath).getFileName().toString();
            String suggestedFileName = originalFileName;
            if (suggestedFileName.toLowerCase().endsWith(".encrypted.txt")) {
                suggestedFileName = suggestedFileName.substring(0, suggestedFileName.length() - ".encrypted.txt".length());
            } else if (suggestedFileName.toLowerCase().endsWith(".encrypted")) {
                 suggestedFileName = suggestedFileName.substring(0, suggestedFileName.length() - ".encrypted".length());
            }
            int dotIndex = suggestedFileName.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < suggestedFileName.length() -1 ) { 
                 suggestedFileName = suggestedFileName.substring(0, dotIndex) + ".decrypted" + suggestedFileName.substring(dotIndex);
            } else {
                 suggestedFileName = suggestedFileName + ".decrypted";
            }

            File outputFile = chooseSaveFile("Zapisz odszyfrowany plik", suggestedFileName);
            
            if (outputFile != null) {
                FileUtils.writeBytesToFile(decryptedBytes, outputFile.getAbsolutePath());
                statusLabel.setText("Odszyfrowano plik do: " + outputFile.getAbsolutePath());
                statusLabel.setStyle("-fx-text-fill: green");
            } else {
                statusLabel.setText("Anulowano zapisywanie odszyfrowanego pliku.");
                statusLabel.setStyle("-fx-text-fill: orange");
            }
        } catch (Exception ex) {
            statusLabel.setText("Błąd deszyfrowania pliku: " + ex.getMessage());
            statusLabel.setStyle("-fx-text-fill: red");
        }
    }

    private File chooseFile(String title) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        return fc.showOpenDialog(stage);
    }

    private File chooseSaveFile(String title, String initialFileName) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.setInitialFileName(initialFileName);
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        return fc.showSaveDialog(stage);
    }
}
