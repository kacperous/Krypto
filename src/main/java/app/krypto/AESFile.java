package app.krypto;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

public class AESFile {
    public String loadFile(String filePath) {
        try(PDDocument document = PDDocument.load(new File(filePath)))
        {
            if(document.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                return text;
            } else {
                System.out.println("File is not encrypted");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
