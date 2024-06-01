import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadPdfText {
    public static void main(String[] args) {
        String filePath = "C:\\Users\\HuanTech PC\\Desktop\\p23100.pdf";

        try {
            PDDocument document = PDDocument.load(new File(filePath));
            if (!document.isEncrypted()) {
                PDFTextStripper pdfStripper = new PDFTextStripper();
                String toriaiText = pdfStripper.getText(document);
//                System.out.println(toriaiText);

                String[] kakuKakou = toriaiText.split("加工No:");
//                System.out.println(kakuKakou[1]);

                for (int i = 1; i < kakuKakou.length; i++) {
                    String kakouText = kakuKakou[i];

                    // Extracting values using regex
                    Pattern pattern = Pattern.compile("鋼材長:\\s*(\\d+\\.\\d+)mm.*本数:\\s*(\\d+)");
                    Matcher matcher = pattern.matcher(kakouText);

                    Map<String, String> pairs = new HashMap<>();

                    if (matcher.find()) {
                        String kousaichou = matcher.group(1).trim();
                        String hon_suu = matcher.group(2).trim();

                        pairs.put(kousaichou, hon_suu);
                    }

                    // Extracting values for 名称 and combining them
                    pattern = Pattern.compile("名称\\s+(\\d+\\.\\d+)mm\\s*x\\s*(\\d+)本");
                    matcher = pattern.matcher(kakouText);

                    Map<String, String> na_mei_pairs = new HashMap<>();

                    while (matcher.find()) {
                        String meisyou_length = matcher.group(1).trim();
                        String meisyou_honsuu = matcher.group(2).trim();

                        na_mei_pairs.put(meisyou_length, meisyou_honsuu);
                    }

                    // Printing results
                    System.out.println("\nPairs from 鋼材長 and 本数:");
                    pairs.forEach((key, value) -> System.out.println(key + " : " + value));

                    System.out.println("Pairs from 名称 and 本:");
                    na_mei_pairs.forEach((key, value) -> System.out.println(key + " : " + value));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
