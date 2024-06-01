package test;

import java.util.*;

public class LengthCombination {
    public static void main(String[] args) {
        // Các chiều dài khác nhau và số lượng tương ứng
        int[] lengths = {3830, 5345, 5505, 5523};
        int[] quantities = {11, 9, 5, 5};

        // Các chiều dài cho trước với số lượng không giới hạn
        int[] predefinedLengths = {7000, 8000, 9000, 10000, 11000, 12000};

        // Kết quả lưu các tổ hợp ghép
        List<String> results = new ArrayList<>();

        // Map lưu chiều dài khác nhau và số lượng còn lại
        Map<Integer, Integer> lengthMap = new HashMap<>();
        for (int i = 0; i < lengths.length; i++) {
            lengthMap.put(lengths[i], quantities[i]);
        }

        // Thử tất cả các cách ghép và lưu lại kết quả
        List<List<String>> allResults = new ArrayList<>();
        for (int predefinedLength : predefinedLengths) {
            List<String> currentResults = new ArrayList<>();
            findBestCombination(new HashMap<>(lengthMap), predefinedLength, currentResults, new ArrayList<>());
            allResults.add(currentResults);
        }

        // Lựa chọn cách ghép tối ưu
        while (!lengthMap.isEmpty()) {
            int minRemainder = Integer.MAX_VALUE;
            String bestResult = null;
            for (List<String> currentResults : allResults) {
                for (String result : currentResults) {
                    int remainder = Integer.parseInt(result.split(":")[1].trim());
                    if (remainder < minRemainder) {
                        minRemainder = remainder;
                        bestResult = result;
                    }
                }
            }
            if (bestResult != null) {
                results.add(bestResult.split(":")[0]);
                updateLengthMap(lengthMap, bestResult.split(":")[0]);
                for (List<String> currentResults : allResults) {
                    String finalBestResult = bestResult;
                    currentResults.removeIf(s -> s.equals(finalBestResult));
                }
            }
        }

        // In ra kết quả
        for (String result : results) {
            System.out.println(result);
        }
    }

    private static void findBestCombination(Map<Integer, Integer> lengthMap, int predefinedLength, List<String> currentResults, List<Integer> currentCombination) {
        if (currentCombination.stream().mapToInt(Integer::intValue).sum() > predefinedLength) return;
        int currentSum = currentCombination.stream().mapToInt(Integer::intValue).sum();
        if (!currentCombination.isEmpty()) {
            int remainder = predefinedLength - currentSum;
            StringBuilder result = new StringBuilder();
            for (int length : currentCombination) {
                result.append(length).append(" ");
            }
            result.append("được ghép vào chiều dài cho trước ").append(predefinedLength).append(" có số lượng 1, ");
            currentResults.add(result.toString() + ":" + remainder);
        }
        for (Map.Entry<Integer, Integer> entry : new HashMap<>(lengthMap).entrySet()) {
            int length = entry.getKey();
            int quantity = entry.getValue();
            if (quantity > 0) {
                currentCombination.add(length);
                lengthMap.put(length, quantity - 1);
                findBestCombination(lengthMap, predefinedLength, currentResults, currentCombination);
                currentCombination.remove(currentCombination.size() - 1);
                lengthMap.put(length, quantity);
            }
        }
    }

    private static void updateLengthMap(Map<Integer, Integer> lengthMap, String result) {
        String[] parts = result.split(" ");
//        System.out.println(Arrays.toString(parts));
        for (int i = 0; i < parts.length; i++) {
//            System.out.println(i);
            if (i + 1 < parts.length && parts[i + 1].equals("được")){
                break;
            }
            int length = Integer.parseInt(parts[i].trim());

            if (lengthMap.get(length) == null){
                break;
            }
            System.out.println("\n" + length + " có số lượng còn lại " + lengthMap.get(length) + "\n");
            lengthMap.put(length, lengthMap.get(length) - 1);
            if (lengthMap.get(length) == 0) {
                lengthMap.remove(length);
            }
        }
    }
}
