package be.bagofwords.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NumUtils {

    public static int getBin(double[] borders, double count) {
        int bin = borders.length;
        while (bin > 0 && count < borders[bin - 1])
            bin--;
        return bin;
    }

    /**
     * Carefull, this method sorts the counts list!
     */

    public static double[] getBorders(int numberOfBins, List<Double> counts) {
        Collections.sort(counts);
        if (!counts.isEmpty()) {
            ArrayList<Integer> borderInds = new ArrayList<>();
            boolean valuesLeft = true;
            while (borderInds.size() < numberOfBins - 1 && valuesLeft) {
                //Find next border
                int maxSizeToSplit = 0;
                int bestIndToSplit = -1;
                for (int i = 0; i < borderInds.size() + 1; i++) {
                    int startInd;
                    int endInd;
                    if (i > 0) {
                        startInd = borderInds.get(i - 1);
                    } else {
                        startInd = 0;
                    }
                    if (i < borderInds.size()) {
                        endInd = borderInds.get(i);
                    } else {
                        endInd = counts.size() - 1;
                    }
                    if (startInd < endInd - 1 && (endInd - startInd > maxSizeToSplit)) {
                        int indToSplit = findBestSplit(counts, startInd, endInd);
                        if (indToSplit != -1) {
                            //Split found
                            int sizeOfSplit = Math.min(indToSplit - startInd, endInd - indToSplit);
                            if (sizeOfSplit > maxSizeToSplit) {
                                maxSizeToSplit = sizeOfSplit;
                                bestIndToSplit = indToSplit;
                            }
                        }
                    }
                }
                if (bestIndToSplit == -1) {
                    valuesLeft = false;
                } else {
                    borderInds.add(bestIndToSplit);
                    Collections.sort(borderInds);
                }
            }
            double[] result = new double[borderInds.size()];
            for (int i = 0; i < result.length; i++) {
                result[i] = counts.get(borderInds.get(i));
            }
            return result;
        } else
            return new double[]{0.0};
    }

    private static int findBestSplit(List<Double> counts, int startInd, int endInd) {
        int middle = startInd + (endInd - startInd) / 2;
        int bottomInd = middle;
        while (bottomInd > startInd && counts.get(bottomInd - 1).equals(counts.get(bottomInd))) {
            bottomInd--;
        }
        int topInd = middle;
        while (topInd < endInd && counts.get(topInd - 1).equals(counts.get(topInd))) {
            topInd++;
        }
        if (bottomInd > startInd) {
            if (topInd < endInd) {
                int dist1 = middle - bottomInd;
                int dist2 = topInd - middle;
                if (dist1 < dist2) {
                    return bottomInd;
                } else {
                    return topInd;
                }
            } else {
                return bottomInd;
            }
        } else {
            if (topInd < endInd) {
                return topInd;
            } else {
                return -1;
            }
        }
    }

    public static String fmt(double input) {
        if (Double.isNaN(input)) {
            return "NaN";
        } else {
            return fmt(input, 2);
        }
    }

    public static String makeNicePercent(double input) {
        input = input * 100;
        NumberFormat format = new DecimalFormat("00.00");
        return format.format(input);
    }

    public static String fmt(double input, int digits) {
        NumberFormat formatter = new DecimalFormat("0.###E00");
        return formatter.format(input);
    }

    public static int sum(int[] array) {
        int sum = 0;
        for (int anArray : array) sum += anArray;
        return sum;
    }

    public static double sum(double[] array) {
        double sum = 0;
        for (double anArray : array) sum += anArray;
        return sum;
    }

    public static double sum(ArrayList<Pair<Double, Integer>> results) {
        double sum = 0;
        for (Pair<Double, Integer> result : results) sum += result.getFirst();
        return sum;
    }

    public static int split(int number, int start, int end) {
        return number << start >>> (32 - (end - start));
    }

    public static int join(int... valsAndSizes) {
        if (valsAndSizes.length % 2 == 1)
            throw new IllegalArgumentException();
        int result = 0;
        for (int i = 0; i < valsAndSizes.length; i += 2) {
            int val = valsAndSizes[i];
            int size = valsAndSizes[i + 1];
            result = (result << size) | val;
            if (val > 1 << size)
                throw new IllegalArgumentException("Can't represent value " + val + " with " + size + " bits.");
        }
        return result;
    }

    public static String fixedLength(long value, int numberOfDigits) {
        String result = Long.toString(value);
        while (result.length() < numberOfDigits) {
            result = "0" + result;
        }
        return result;
    }

    public static double sum(List<Double> values) {
        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        return sum;
    }

    public static double average(List<Double> values) {
        if (values.isEmpty()) {
            throw new RuntimeException("Can not compute average of empty list!");
        }
        return sum(values) / values.size();
    }

    public static long sumOfLongValues(List<Long> values) {
        long sum = 0;
        for (Long value : values) {
            sum += value;
        }
        return sum;
    }

    //For more general formula, see http://blog.smola.org/post/987977550/log-probabilities-semirings-and-floating-point-numbers

    public static double sumLogProbs(double probPos, double probNeg) {
        if (probPos > probNeg) {
            return probPos + Math.log(Math.exp(probNeg - probPos) + 1);
        } else {
            return probNeg + Math.log(Math.exp(probPos - probNeg) + 1);
        }
    }

    public static double sumLogProbs(double[] probabilities) {
        double max = max(probabilities);
        double sum = 0;
        for (double probability : probabilities) {
            sum += Math.exp(probability - max);
        }
        return max + Math.log(sum);
    }

    private static double max(double[] probabilities) {
        double max = probabilities[0];
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > max) {
                max = probabilities[i];
            }
        }
        return max;
    }

    public static boolean equal(double first, double second) {
        return first == second || Math.abs(first - second) < 0.000001 * Math.max(Math.abs(first), Math.abs(second));
    }
}
