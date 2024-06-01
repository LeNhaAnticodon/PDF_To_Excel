import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReadPDFToExcel {

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
    public static final String CHL_EXCEL_PATH = "C:\\Users\\HuanTech PC\\Desktop\\p23100.pdf";
    private static int rowToriAiNum;

    public static void main(String[] args) {
        getFullToriaiText();

        getHeaderData();

        getToriaiData();
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

    private static void getToriaiData() {
        if (kakuKakou == null) {
            return;
        }

        // duyệt qua từng bộ ghép vật liệu và lấy ra chiều dài bozai + số lượng và các chiều dài sản phẩm + số lượng của nó
        for (int i = 1; i < kakuKakou.length; i++) {

            if (i == 1) {
                kirirosu = extractValue(kakuKakou[i], "切りﾛｽ設定:", "mm");
            }

            String kaKouText = kakuKakou[i];

            Map<Integer, Integer> kouZaiChouPairs = new LinkedHashMap<>();
            Map<Integer, Integer> meiSyouPairs = new LinkedHashMap<>();

            // Extracting values for 鋼材長 and 本数
            String[] kaKouLines = kaKouText.split("\n");

            for (String line : kaKouLines) {
                if (line.contains("鋼材長:") && line.contains("本数:")) {
                    String kouZaiChou = extractValue(line, "鋼材長:", "mm");
                    String honSuu = extractValue(line, "本数:", " ").split(" ")[0];

                    kouZaiChouPairs.put(convertStringToIntAndMul(kouZaiChou, 100), convertStringToIntAndMul(honSuu, 1));
                }

                // Extracting values for 名称
                if (line.contains("名称")) {
                    String meiSyouLength = extractValue(line, "名称", "mm x").trim();
                    String meiSyouHonSuu = extractValue(line, "mm x", "本").trim();

                    meiSyouPairs.put(convertStringToIntAndMul(meiSyouLength, 100), convertStringToIntAndMul(meiSyouHonSuu, 1));
                }
            }

            KA_KOU_PAIRS.put(kouZaiChouPairs, meiSyouPairs);

        }

        // Printing results
        KA_KOU_PAIRS.forEach((kouZaiChouPairs, meiSyouPairs) -> {
            kouZaiChouPairs.forEach((key, value) -> System.out.println("\n" + key + " : " + value));
            meiSyouPairs.forEach((key, value) -> System.out.println(key + " : " + value));
        });

        // duyệt qua map KA_KOU_PAIRS để tính giá trị các dòng tính vật liệu cần nhập trong excel
        for (Map.Entry<Map<Integer, Integer>, Map<Integer, Integer>> e : KA_KOU_PAIRS.entrySet()) {

            Map<Integer, Integer> kouZaiChouPairs = e.getKey();
            Map<Integer, Integer> meiSyouPairs = e.getValue();
            int kouZaiNum = 1;
            // lấy số lượng của bozai đang duyệt trong forEach, thực tế kouZaiChouPairs chỉ có 1 cặp nhưng không có cách
            // nào lấy được nên dùng for
            for (Map.Entry<Integer, Integer> entry : kouZaiChouPairs.entrySet()) {
                kouZaiNum = entry.getValue();
            }

            rowToriAiNum += kouZaiNum * meiSyouPairs.size();
        }

        // tối thiểu cần 4 dòng để nhập các ô thông tin nên nếu rowToriAiNum < 4 thì đặt là 4
        if (rowToriAiNum < 4) {
            rowToriAiNum = 4;
        }

        System.out.println(rowToriAiNum);
        System.out.println("\n" + kirirosu);
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
