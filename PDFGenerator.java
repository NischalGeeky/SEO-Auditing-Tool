package PdfFile;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PDFGenerator {
    public static void main(String[] args) {
        String fileName = "output.pdf"; // Output PDF file name

        // Data to be included in the PDF
        String name = "Nischal";
        int age = 18;
        int FavNum=18;
        List<String> lines = new ArrayList<>();
        lines.add("Category: " + name);
        lines.add("Age: " + age);
        lines.add("Favorite Number: " + FavNum);
        // Generate PDF
        generatePDF(fileName,lines);
    }

    private static void generatePDF(String fileName, List<String> lines) {
        try (PDDocument document = new PDDocument()) {
            File myfont1 = new File("C:\\Users\\nispa\\Downloads\\Dancing_Script,Kalam\\Dancing_Script\\DancingScript-VariableFont_wght.ttf");
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                PDFont f1 = PDType0Font.load(document, myfont1);

                contentStream.setFont(f1, 40); // Adjust font size as needed

                float y = 700; // Initial Y-coordinate
                for (String line : lines) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(100, y);
                    contentStream.showText(line);
                    contentStream.endText();
                    y -= 50; // Adjust line spacing as needed
                }
            }

            // Save the PDF with the same file name, overwriting the existing file
            document.save(fileName);
            System.out.println("PDF created successfully!");

            // Open the generated PDF
            openPDF(fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void openPDF(String fileName) {
        try {
            File file = new File(fileName);
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            System.err.println("Error opening PDF: " + e.getMessage());
        }
    }
}

