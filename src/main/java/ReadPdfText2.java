import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReadPdfText2 {

    private static String shortNouKi = "";
    private static String kouJiMe = "";
    private static String kyakuSakiMei = "";
    private static int size1;
    private static int size2;
    private static int size3 = 0;
    private static String koSyuName = "";
    private static String koSyuNumMark = "3";
    private static String kirirosu = "";

    private static final Map<Map<Integer, Integer>, Map<Integer, Integer>> KA_KOU_PAIRS = new LinkedHashMap<>();
    private static String toriaiText = "";
    private static String[] kakuKakou;

    public static final String FILE_PATH = "C:\\Users\\HuanTech PC\\Desktop\\p23100.pdf";
    public static final String CHL_EXCEL_PATH = "C:\\Users\\HuanTech PC\\Desktop\\chl excel.xlsx";
    private static int rowToriAiNum;

    public static void main(String[] args) {
        getFullToriaiText();
        getHeaderData();
        getToriaiData();
        writeDataToExcel();
    }

    private static void getFullToriaiText() {
        try {
            PDDocument document = PDDocument.load(new File(FILE_PATH));
            if (!document.isEncrypted()) {
                PDFTextStripper pdfStripper = new PDFTextStripper();
                toriaiText = pdfStripper.getText(document);
                kakuKakou = toriaiText.split("加工No:");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void getHeaderData() {
        String header = kakuKakou[0];
        String[] linesHeader = header.split("\n");

        String nouKi = extractValue(header, "期[", "]");
        String[] nouKiArr = nouKi.split("/");
        shortNouKi = nouKiArr[1] + "/" + nouKiArr[2];

        kouJiMe = extractValue(header, "考[", "]");
        kyakuSakiMei = extractValue(header, "客先名[", "]");

        String kouSyu = extractValue(linesHeader[4], "鋼材寸法:", "梱包");
        String[] kouSyuNameAndSize = kouSyu.split("-");
        koSyuName = kouSyuNameAndSize[0].trim();

        switch (koSyuName) {
            case "K":
                koSyuNumMark = "3";
                break;
            case "L":
                koSyuNumMark = "4";
                break;
            case "FB":
                koSyuNumMark = "5";
                break;
            case "[":
                koSyuNumMark = "6";
                break;
            case "C":
                koSyuNumMark = "7";
                break;
            case "H":
                koSyuNumMark = "8";
                break;
            case "CA":
                koSyuNumMark = "9";
                break;
        }

        String[] koSyuSizeArr = kouSyuNameAndSize[1].split("x");

        if (koSyuSizeArr.length == 3) {
            size1 = convertStringToIntAndMul(koSyuSizeArr[1], 10);
            size2 = convertStringToIntAndMul(koSyuSizeArr[0], 10);
            size3 = convertStringToIntAndMul(koSyuSizeArr[2], 10);
        } else if (koSyuSizeArr.length == 4) {
            size1 = convertStringToIntAndMul(koSyuSizeArr[1], 10);
            size2 = convertStringToIntAndMul(koSyuSizeArr[0], 10);
            size3 = convertStringToIntAndMul(koSyuSizeArr[3], 10);
        } else {
            size1 = convertStringToIntAndMul(koSyuSizeArr[1], 10);
            size2 = convertStringToIntAndMul(koSyuSizeArr[0], 10);
        }

        System.out.println(shortNouKi + " : " + kouJiMe + " : " + kyakuSakiMei + " : " + koSyuName + koSyuNumMark + " : " + size1 + " : " + size2 + " : " + size3);
    }

    private static void getToriaiData() {
        if (kakuKakou == null) {
            return;
        }

        for (int i = 1; i < kakuKakou.length; i++) {

            if (i == 1) {
                kirirosu = extractValue(kakuKakou[i], "切りﾛｽ設定:", "mm");
            }

            String kaKouText = kakuKakou[i];

            Map<Integer, Integer> kouZaiChouPairs = new LinkedHashMap<>();
            Map<Integer, Integer> meiSyouPairs = new LinkedHashMap<>();

            String[] kaKouLines = kaKouText.split("\n");

            for (String line : kaKouLines) {
                if (line.contains("鋼材長:") && line.contains("本数:")) {
                    String kouZaiChou = extractValue(line, "鋼材長:", "mm");
                    String honSuu = extractValue(line, "本数:", " ").split(" ")[0];

                    kouZaiChouPairs.put(convertStringToIntAndMul(kouZaiChou, 1), convertStringToIntAndMul(honSuu, 1));
                }

                if (line.contains("名称")) {
                    String meiSyouLength = extractValue(line, "名称", "mm x").trim();
                    String meiSyouHonSuu = extractValue(line, "mm x", "本").trim();

                    meiSyouPairs.put(convertStringToIntAndMul(meiSyouLength, 100), convertStringToIntAndMul(meiSyouHonSuu, 1));
                }
            }

            KA_KOU_PAIRS.put(kouZaiChouPairs, meiSyouPairs);
        }

        KA_KOU_PAIRS.forEach((kouZaiChouPairs, meiSyouPairs) -> {
            kouZaiChouPairs.forEach((key, value) -> System.out.println("\n" + key + " : " + value));
            meiSyouPairs.forEach((key, value) -> System.out.println(key + " : " + value));
        });

        for (Map.Entry<Map<Integer, Integer>, Map<Integer, Integer>> e : KA_KOU_PAIRS.entrySet()) {

            Map<Integer, Integer> kouZaiChouPairs = e.getKey();
            Map<Integer, Integer> meiSyouPairs = e.getValue();
            int kouZaiNum = 1;
            for (Map.Entry<Integer, Integer> entry : kouZaiChouPairs.entrySet()) {
                kouZaiNum = entry.getValue();
            }

            rowToriAiNum += kouZaiNum * meiSyouPairs.size();
        }

        if (rowToriAiNum < 4) {
            rowToriAiNum = 4;
        }

        System.out.println(rowToriAiNum);
        System.out.println("\n" + kirirosu);
    }

    private static void writeDataToExcel() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        // Ghi thời gian hiện tại vào ô A1
        Row row1 = sheet.createRow(0);
        Cell cellA1 = row1.createCell(0);
        String currentTime = new SimpleDateFormat("yyMMddHHmm").format(new Date());
        cellA1.setCellValue(currentTime);

        // Ghi size1, size2, size3, 1 vào ô A2, B2, C2, D2
        Row row2 = sheet.createRow(1);
        row2.createCell(0).setCellValue(size1);
        row2.createCell(1).setCellValue(size2);
        row2.createCell(2).setCellValue(size3);
        row2.createCell(3).setCellValue(1);

        // Ghi koSyuNumMark, 1, rowToriAiNum, 1 vào ô A3, B3, C3, D3
        Row row3 = sheet.createRow(2);
        row3.createCell(0).setCellValue(koSyuNumMark);
        row3.createCell(1).setCellValue(1);
        row3.createCell(2).setCellValue(rowToriAiNum);
        row3.createCell(3).setCellValue(1);

        // Ghi kouJiMe, kyakuSakiMei, shortNouKi, kirirosu vào ô D4, D5, D6, D7
        sheet.createRow(3).createCell(3).setCellValue(kouJiMe);
        sheet.createRow(4).createCell(3).setCellValue(kyakuSakiMei);
        sheet.createRow(5).createCell(3).setCellValue(shortNouKi);
        sheet.createRow(6).createCell(3).setCellValue(kirirosu);

        int rowIndex = 3;

        // Ghi dữ liệu từ KA_KOU_PAIRS vào các ô
        for (Map.Entry<Map<Integer, Integer>, Map<Integer, Integer>> entry : KA_KOU_PAIRS.entrySet()) {
            if (rowIndex >= 98) break;

            Map<Integer, Integer> kouZaiChouPairs = entry.getKey();
            Map<Integer, Integer> meiSyouPairs = entry.getValue();

            int keyTemp = 0;
            int valueTemp = 0;

            // Ghi dữ liệu từ mapkey vào ô C4
            for (Map.Entry<Integer, Integer> kouZaiEntry : kouZaiChouPairs.entrySet()) {

                keyTemp = kouZaiEntry.getKey();
                valueTemp = kouZaiEntry.getValue();
            }

            // Ghi dữ liệu từ mapvalue vào ô A4, B4 và các hàng tiếp theo
            for (int i = 0; i < valueTemp; i++) {
                for (Map.Entry<Integer, Integer> meiSyouEntry : meiSyouPairs.entrySet()) {
                    if (rowIndex >= 98) break;

                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(meiSyouEntry.getKey());
                    row.createCell(1).setCellValue(meiSyouEntry.getValue());
                }
                sheet.getRow(rowIndex - meiSyouPairs.size()).createCell(2).setCellValue(keyTemp);
            }
        }

        // nếu không có hàng sản phẩm nào thì sẽ chưa tạo hàng 4, 5, 6, 7 và rowIndex vẫn là 3
        // cần tạo thêm 4 hàng này để ghi các thông tin kouJiMe, kyakuSakiMei, shortNouKi, kirirosu bên dưới
        for (int i = 0; i < 4; i++) {
            if (rowIndex <= i + 3) {
                sheet.createRow(i + 3);
            }
        }

        // Ghi kouJiMe, kyakuSakiMei, shortNouKi, kirirosu vào ô D4, D5, D6, D7
        sheet.getRow(3).createCell(3).setCellValue(kouJiMe);
        sheet.getRow(4).createCell(3).setCellValue(kyakuSakiMei);
        sheet.getRow(5).createCell(3).setCellValue(shortNouKi);
        sheet.getRow(6).createCell(3).setCellValue(kirirosu);

        // Ghi giá trị 0 vào các ô A99, B99, C99, D99
        Row lastRow = sheet.createRow(rowIndex);
        lastRow.createCell(0).setCellValue(0);
        lastRow.createCell(1).setCellValue(0);
        lastRow.createCell(2).setCellValue(0);
        lastRow.createCell(3).setCellValue(0);

        try (FileOutputStream fileOut = new FileOutputStream(CHL_EXCEL_PATH)) {
            workbook.write(fileOut);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String extractValue(String text, String startDelimiter, String endDelimiter) {
        int startIndex = text.indexOf(startDelimiter) + startDelimiter.length();
        int endIndex = text.indexOf(endDelimiter, startIndex);
        return text.substring(startIndex, endIndex).trim();
    }

    private static int convertStringToIntAndMul(String textNum, int multiplier) {
        Double num = null;
        try {
            num = Double.parseDouble(textNum);
        } catch (NumberFormatException e) {
            System.out.println("Lỗi chuyển đổi chuỗi không phải số thực sang số");
        }
        if (num != null) {
            return (int) (num * multiplier);
        }
        return 0;
    }
}