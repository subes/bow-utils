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
        long hash = startHash;
        for (int pos = 0; pos < s.length(); pos++)
            hash = hash * addHash + s.charAt(pos);
        return hash;
    }

    public static long hashCode(String... strings) {
        long hash = startHash;
        for (String s : strings) {
            for (int pos = 0; pos < s.length(); pos++)
                hash = hash * addHash + s.charAt(pos);
        }
        return hash;
    }

    public static long hashCode(SimpleString targetWord) {
        return hashCode(targetWord.getS());
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
