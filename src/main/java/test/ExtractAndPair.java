package test;

import java.util.*;
import java.util.regex.*;

public class ExtractAndPair {

    public static void main(String[] args) {
        String kakouText = "加工名:0002\n"
                + "定尺  鋼材長:10000.0mm     本数:1      先端部残材長:  659.0mm   切りﾛｽ設定:    3.0mm\n"
                + "                                        尾端部残材長:    0.0mm   切りﾛｽ合計:    6.0mm\n"
                + "       製品№  0003               名称                           5505.0mm x 1本(  1本)(    1/    5)\n"
                + "       製品№  0001               名称                           3830.0mm x 1本(  1本)(   11/   11)";

        // Extracting values using regex
        Pattern pattern = Pattern.compile("鋼材長:(\\d+\\.\\d+)mm.*本数:(\\d+)");
        Matcher matcher = pattern.matcher(kakouText);

        Map<String, String> pairs = new HashMap<>();

        if (matcher.find()) {
            String kousaichou = matcher.group(1);
            String hon_suu = matcher.group(2);

            pairs.put(kousaichou, hon_suu);
        }

        // Extracting values for 名称 and combining them
        pattern = Pattern.compile("名称\\s+(\\d+\\.\\d+)mm\\s+x\\s+(\\d+)本");
        matcher = pattern.matcher(kakouText);

        Map<String, String> na_mei_pairs = new HashMap<>();

        while (matcher.find()) {
            String meisyou_length = matcher.group(1);
            String meisyou_honsuu = matcher.group(2);

            na_mei_pairs.put(meisyou_length, meisyou_honsuu);
        }

        // Printing results
        System.out.println("Pairs from 鋼材長 and 本数:");
        pairs.forEach((key, value) -> System.out.println(key + " : " + value));

        System.out.println("\nPairs from 名称 and 本:");
        na_mei_pairs.forEach((key, value) -> System.out.println(key + " : " + value));
    }
}
