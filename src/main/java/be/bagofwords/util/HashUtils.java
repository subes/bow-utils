package be.bagofwords.util;

import be.bagofwords.text.BowString;

public class HashUtils {

    public static final int startHash = 5381;
    public static final int addHash = 65599;

    public static int integerHashCode(String s) {
        int hash = startHash;
        for (int pos = 0; pos < s.length(); pos++)
            hash = hash * addHash + s.charAt(pos);
        return hash;
    }

    public static long hashCode(CharSequence s) {
        return append(startHash, s);
    }

    public static long hashCode(String... strings) {
        long hash = startHash;
        for (String s : strings) {
            hash = append(hash, s);
        }
        return hash;
    }

    public static long append(long hash, CharSequence s) {
        for (int pos = 0; pos < s.length(); pos++) {
            hash = hash * addHash + s.charAt(pos);
        }
        return hash;
    }

    public static long append(long hash, BowString word) {
        for (int pos = word.getStart(); pos < word.getEnd(); pos++) {
            hash = hash * addHash + word.getTextS().charAt(pos);
        }
        return hash;
    }

    public static long append(long hash, long number) {
        return hash * addHash + number;
    }

    public static long append(long hash, double number) {
        return hash * addHash + Double.doubleToLongBits(number);
    }

    public static long randomDistributeHash(long hash) {
        long result = startHash;
        for (int i = 0; i < 8; i++) {
            result = result * addHash + (byte) hash;
            hash = hash >> 8;
        }
        return result;
    }

}
