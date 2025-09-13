package com.zs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import java.awt.image.BufferedImage;

import com.google.gson.Gson;
import com.zs.dto.Company;
import com.zs.dto.CompanyList;
import com.zs.dto.ResultFile;
import com.zs.log.MyFormatter;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

/**
 * Hello world!
 *
 */
public class App 
{
    private static Parser parser = new Parser();
    private static Gson gson = new Gson();
    private static PDFTextStripper pdfTextStripper = new PDFTextStripper();
    private static final Logger logger = Logger.getLogger(App.class.getName());


    public static void main( String[] args ) throws SecurityException, FileNotFoundException, IOException
    {   
        Logger.getLogger("org.apache").setLevel(java.util.logging.Level.OFF);

        // Create FileHandler and ConsoleHandler
        FileHandler fileHandler = new FileHandler("C:\\batch\\application.log");
        
        fileHandler.setFormatter(new MyFormatter());
            
        // Add handlers to the logger
        logger.addHandler(fileHandler);            

        // Remove default handlers
        logger.setUseParentHandlers(false);

        String strOperation = "";
        if (args.length > 0) {
            strOperation = args[0];
        }

        if (strOperation.compareTo("-process") == 0) {
            process(args[1], args[2], args[3]);
        } else if (strOperation.compareTo("-read") == 0) {     
            getPDFTextInStr(new File(args[1]));
        } else if (strOperation.compareTo("-readFromImage") == 0) {     
            getPDFTextFromImgInStr(new File(args[1]));
        } else if (strOperation.compareTo("-writeTo") == 0) {     
            writeTextToFile(args[1], args[2]);
        } else {
            printHelp();
        }        
    }

    public static void printHelp() {
        System.out.println("Version 1.0");
        System.out.println("usage: renamepdf [options] [<params>]");
        System.out.println("Options:");
        System.out.println(" -process  <path> <pathOut> <jsonPath>");
        System.out.println("           Der Prozess benennt alle PDF Dateien (*.pdf) um, die sich im <path> befindet und ");
        System.out.println("           speichert in dem Verzeichnis <pathOut> ab. Bestehende Datei aus dem aktuellen Verzeichnis");
        System.out.println("           Verzeichnis in dem <path>/save gesichert. <jsonPath> ist der Name der Datei, ");
        System.out.println("           bei dem alle Regelwerke existieren ");        
        System.out.println(" -read     <fileName> ");
        System.out.println("           zeigt die Texte einer PDF Datei an.");
        System.out.println(" -readFromImage <fileName> ");
        System.out.println("           zeigt die Texte einer PDF Datei aus dem Image an.");
        System.out.println(" -writeTo  <Directory> <FileTo>");
        System.out.println("           zeigt die Texte einer PDF Datei an.");

    }

    public static void process(String path, String pathOut, String pathDic) throws SecurityException, IOException {                   
        CompanyList companyList = parseDictionary(pathDic);
        logger.info("process started...");
        File[] files = getFileLists(path);
        for (File file: files) {
            logger.info("analyse the file: " + file.getName());
            boolean found = false;
            String[] strList = getAllTexts(file);
            if (strList == null || strList.length == 0) continue;
            logger.info("*****************************");

            for (Company company: companyList.getCompanies()) {
                List<String> nameContext = company.getMetrics().getName();
                String invoiceContext = company.getMetrics().getInvoice();
                String creditContext = company.getMetrics().getCredit();
                ResultFile resultFile = searchCompany(strList, nameContext, invoiceContext, creditContext);    
                if (resultFile != null) {

                    logger.info("hit company: " + company.getName());

                    resultFile.setCompanyName(company.getName());
                    resultFile.setYear(2025);
                    Path copied = Paths.get(file.getAbsolutePath());
                    Path copied2 = Paths.get(pathOut + resultFile.getFileName());
                    Path copiedSave = Paths.get(file.getParentFile().getAbsolutePath() + "\\save\\" + file.getName());
                    found = true;
                    try {
                        Files.copy(copied, copied2, StandardCopyOption.REPLACE_EXISTING);
                        logger.info("copied = " + copied);
                        logger.info("copied to = " + copied2);
                        Path copyTargt = Files.copy(copied, copiedSave, StandardCopyOption.REPLACE_EXISTING);
                        
                        if (copyTargt.compareTo(copiedSave) == 0) {
                            Files.delete(copied);
                        }                        
                    } catch (IOException e) {
                        logger.severe("" + e.getMessage());
                    }                        
                }

                if (found) break;
            }
        }

        logger.info("process finished...");
    }
    public static void getPDFTextInStr (File file) {
        String[] strArray = getPDFText(file);
        for (String str: strArray) {
            str = str.trim();
            System.out.println(str);
        }
    }

    public static void writeTextToFile(String path, String fileTo) throws IOException {
        try (FileWriter fileWriter = new FileWriter(fileTo)) {
            fileWriter.write("Hello Folks!");

            File[] files = getFileLists(path);
            for (File file: files) {
                String[] str = getPDFText(file);
                fileWriter.write("********************************");
                fileWriter.write("\n");
                fileWriter.write("fileName: " + file.getName());
                fileWriter.write("\n");
                fileWriter.write(Arrays.toString(str));
                fileWriter.write("\n");
            }
        }
    }
    public static void getPDFTextFromImgInStr (File file) {
        try (PDDocument doc = Loader.loadPDF(file)) {
            String[] strArray = extractTextFromPDFWithImage(doc);
            for (String str: strArray) {
                str = str.trim();
                System.out.println(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }

     /*
      return all the files from given path 
    */
    public static String[] getAllTexts (File file) {
    	try (PDDocument doc = Loader.loadPDF(file)) {
        	//- get Texts
            String str = pdfTextStripper.getText(doc);
            String[] strArray = str.split("\r\n");
            if (strArray != null && strArray.length > 0) {
                for (int i = 0; i < strArray.length; i++) strArray[i] = strArray[i].trim(); 
                return strArray;
            }
            String[] strArray2 = extractTextFromPDFWithImage(doc);
            if (strArray2 != null && strArray2.length > 0) {
                for (int i = 0; i < strArray2.length; i++) strArray2[i] = strArray2[i].trim(); 
                return strArray2;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null; 
    }

    /*
      return all the files from given path 
    */
    public static String[] getPDFText (File file) {
    	try (PDDocument doc = Loader.loadPDF(file)) {
        	String str = pdfTextStripper.getText(doc);
            return str.split("\r\n");    
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } 
    }


    /*
      return all the files from given path 
    */
    private static File[] getFileLists (String path) {
        return new File(path).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".pdf");
            }
        });
    }


     /*
      return all the files from given path 
    */
    public static CompanyList parseDictionary (String fileName) {
         try (Reader reader = new FileReader(fileName)) {
            return gson.fromJson(reader, CompanyList.class);
        } catch (IOException e) {
            throw new RuntimeException(e);         
        }        
    }

    private static ResultFile searchCompany (String[] textList, List<String> companyNames, String invoice, String credit) {
        ResultFile result = null;
        boolean foundCompanyName = true;
        boolean foundInvoice = false;
        boolean foundCredit = false;
        String invoiceStr = ""; 
        String creditStr = ""; 

        /* find text for company */
        for (String comp: companyNames) {
            boolean foundName = false;
            for (String str: textList) {
               if (str.contains(comp)) {
                    foundName = true;
                    break;
                } 
            }

            foundCompanyName = foundCompanyName & foundName;
            if (!foundName) break;
        }

        if (!foundCompanyName) return null;

        /* find invoice or credit */
        for (String str: textList) {
            str = str.trim();        
            String strRes;
            if (invoice != null) {
                strRes = parser.find(str, invoice);
                if (strRes != null) {
                    invoiceStr = strRes;
                    foundInvoice = true;
                }
            }

            if (credit != null) {
                strRes = parser.find(str, credit);
                if (strRes != null) {
                    creditStr = strRes;
                    foundCredit = true;
                }
            }
            
            if (foundInvoice || foundCredit) break;
        }   
        
        if (foundInvoice || foundCredit) {
            result = new ResultFile();
            result.setInvoice(invoiceStr);
            result.setCredit(creditStr);
        }
        return result;    
    }

    public static String[] extractTextFromPDFWithImage(PDDocument doc) throws IOException {
        PDPageTree list = doc.getPages();
        for (PDPage page : list) {
            PDResources pdResources = page.getResources();
            for (COSName c : pdResources.getXObjectNames()) {
                PDXObject o = pdResources.getXObject(c);
                if (o instanceof org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject) {
                    BufferedImage image = ((org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject)o).getImage();
                    String strArry[] = extractTextFromImage(image).split("\n");
                    return strArry;
                }
            }
        }
        return null;
    }

    public static String extractTextFromImage(BufferedImage image) {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Users\\o.kilic\\AppData\\Local\\Tesseract-OCR\\tessdata"); // Pfad zu den Tesseract-Trainingsdaten
        try {
            return tesseract.doOCR(image);
        } catch (TesseractException e) {
            System.err.println("Fehler bei der Texterkennung: " + e.getMessage());
            return null;
        }
    }
}
