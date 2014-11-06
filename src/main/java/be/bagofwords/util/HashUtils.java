package be.bagofwords.util;


import be.bagofwords.text.SimpleString;

public class HashUtils {

    public static final int startHash = 5381;
    public static final int addHash = 65599;

    public static int integerHashCode(String s) {
        int hash = startHash;
        for (int pos = 0; pos < s.length(); pos++)
            hash = hash * addHash + s.charAt(pos);
        return hash;
    }

    public static long hashCode(String s) {
        return appendHashCode(startHash, s);
    }

    public static long hashCode(String... strings) {
        long hash = startHash;
        for (String s : strings) {
            hash = appendHashCode(hash, s);
        }
        return hash;
    }

    public static long appendHashCode(long hash, String s) {
        for (int pos = 0; pos < s.length(); pos++) {
            hash = hash * addHash + s.charAt(pos);
        }
        return hash;
    }

    public static long appendHashCode(long hash, SimpleString word) {
        for (int pos = word.getStart(); pos < word.getEnd(); pos++) {
            hash = hash * addHash + word.getOrigText().charAt(pos);
        }
        return hash;
    }

    public static long hashCode(SimpleString targetWord) {
        return appendHashCode(startHash, targetWord);
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
