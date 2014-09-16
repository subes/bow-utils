package be.bow.util;

import be.bow.text.*;
import it.unimi.dsi.fastutil.chars.CharArrayList;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils extends org.apache.commons.lang3.StringUtils {

    public static final int NUM_OF_PADDED_SPACES = 1;

    static final char[] quotes = {'"', '\'', '‘', '’', '“', '”'};
    private static final CharArrayList characterMapping = new CharArrayList();

    /**
     * Remove slashes from inputString
     */

    private static final Map<Character, Character> escapeFileNameMapping;
    private static final Map<Character, Character> escapeTab;
    private static final Map<Character, Character> escapePath;
    private static final Map<Character, Character> escapeNewLine;

    static {
        escapeFileNameMapping = new HashMap<>();
        escapeFileNameMapping.put('/', '+');
        escapeFileNameMapping.put('\\', '-');
        escapeTab = new HashMap<>();
        escapeTab.put('\t', ' ');
        escapePath = new HashMap<>();
        escapePath.put('/', '-');
        escapePath.put('\r', '+');
        escapePath.put('\n', '$');
        escapePath.put('\t', '§');
        escapePath.put(' ', '.');
        escapeNewLine = new HashMap<>();
        escapeNewLine.put('\r', 'r');
        escapeNewLine.put('\n', 'n');
    }

    private static final String[] topLevelDomains = {"com", "co.uk", "net", "org", "nl", "be", "me", "nu"};
    private static final List<String> certainErrorRegex = Arrays.asList(".*\\d+[A-Za-z]{3,}.*", ".*\\d+\\.[A-Za-z]{3,}.*");
    private static Pattern namePattern = Pattern.compile("([^a-zA-Z]*[A-Z][a-z]*[^a-zA-Z]*)+");

    public static void normalizeQuotationMarks(char[] extracted) {
        for (int pos = 0; pos < extracted.length; pos++) {
            boolean foundQuote = false;
            for (int i = 0; i < quotes.length && !foundQuote; i++) {
                if (quotes[i] == extracted[pos]) {
                    foundQuote = true;
                    extracted[pos] = '\'';
                }
            }
        }
    }

    public static boolean isASCIIVowel(char c) {
        return c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u' || c == 'y';
    }

    public static boolean isASCIIConsonant(char c) {
        return c == 'b' || c == 'c' || c == 'd' || c == 'f' || c == 'g' || c == 'h' || c == 'j' || c == 'k' || c == 'l' || c == 'm' || c == 'n' || c == 'p' || c == 'q' || c == 'r' || c == 's'
                || c == 't' || c == 'v' || c == 'w' || c == 'x' || c == 'z';
    }

    public static String removeAccentsSlow(String str) {
        if (str.contains("Ø") || str.contains("ø")) {
            //Normalizer does not seem to work for this character. Does it fail on other characters also?
            str = str.replaceAll("ø", "o").replaceAll("Ø", "O");
        }
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    public static void removeHTML(MappedText mappedText) {
        StringUtils.replaceScripts(mappedText);
        StringUtils.replaceAll("<style[^<]+</style>", "", mappedText);
        StringUtils.replaceAll("<br[^>]*>", "\n", mappedText);
        StringUtils.replaceAll("</?p[^>]*>", "\n", mappedText);
        StringUtils.replaceAll("</?div[^>]*>", "\n", mappedText);
        StringUtils.replaceAll("</?h[1234][^>]*>", "\n", mappedText);
        //Now remove all remaining tags
        StringUtils.replaceAll("<[^>]*>", "", mappedText);
        replaceHTMLEntities(mappedText);
        //checkMapping(mappingFrom, mappingTo, length);
    }

    private static void replaceScripts(MappedText mappedText) {
        Pattern pattern1 = Pattern.compile("<script[^>]*>");
        Pattern pattern2 = Pattern.compile("</script");
        Matcher matcher1 = pattern1.matcher(mappedText.getText());
        Matcher matcher2 = pattern2.matcher(mappedText.getText());
        ArrayList<Pair<Integer, Integer>> matches1 = new ArrayList<>();
        ArrayList<Pair<Integer, Integer>> matches2 = new ArrayList<>();
        while (matcher1.find()) {
            matches1.add(new Pair<>(matcher1.start(), matcher1.end()));
        }
        while (matcher2.find()) {
            matches2.add(new Pair<>(matcher2.start(), matcher2.end()));
        }
        if (matches1.size() != matches2.size())
            throw new IllegalArgumentException("Could not parse this file!");
        else {
            ArrayList<Pair<Integer, Integer>> matches = new ArrayList<>();
            for (int i = 0; i < matches1.size(); i++)
                matches.add(new Pair<>(matches1.get(i).getSecond(), matches2.get(i).getFirst()));
            replaceMatches(matches, "", mappedText);
        }
    }

    public static void replaceMatches(List<Match> matches, MappedText mappedText) {
        Collections.sort(matches);
        checkForOverlapping(matches);
        int newLength = computeNewLength(mappedText, matches);
        char[] newText = new char[newLength];
        int[] newMapping = new int[newLength];
        int positionInOrig = 0;
        int positionDiff = 0;
        for (Match match : matches) {
            if (positionInOrig > match.getStart()) {
                throw new RuntimeException("Something went wrong while replacing matches in text.");
            }
            String replacement = match.getReplacement();
            //Before match, copy data like it was before:
            while (positionInOrig < match.getStart()) {
                newText[positionInOrig + positionDiff] = mappedText.getTextArray()[positionInOrig];
                newMapping[positionInOrig + positionDiff] = mappedText.getMappingToOrig()[positionInOrig];
                positionInOrig++;
            }
            //In match, replace text by replacement and set mapping
            Pair<Integer, Integer> oldMatch = match.keepWordMapping() ? mappedText.getMappingToOrig(match.getStart(), match.getEnd()) : null;
            for (int i = 0; i < replacement.length(); i++) {
                newText[positionInOrig + positionDiff + i] = replacement.charAt(i);
                int newCharMapping;
                if (oldMatch != null && i == 0) {
                    newCharMapping = oldMatch.getFirst();
                } else if (oldMatch != null && i == replacement.length() - 1) {
                    newCharMapping = oldMatch.getSecond() - 1;
                } else {
                    newCharMapping = -1;
                }
                newMapping[positionInOrig + positionDiff + i] = newCharMapping;
            }
            positionDiff += replacement.length() - (match.getEnd() - match.getStart());
            positionInOrig += match.getEnd() - match.getStart();
        }
        //continue till end of text
        while (positionInOrig < mappedText.getTextArray().length)

        {
            newText[positionInOrig + positionDiff] = mappedText.getTextArray()[positionInOrig];
            newMapping[positionInOrig + positionDiff] = mappedText.getMappingToOrig()[positionInOrig];
            positionInOrig++;
        }

        mappedText.setText(newText);
        mappedText.setMappingToOrig(newMapping);
    }

    private static int computeNewLength(MappedText mappedText, List<Match> matches) {
        int length = mappedText.getText().length();
        for (Match match : matches) {
            length -= match.getEnd() - match.getStart();
            length += match.getReplacement().length();
        }
        return length;
    }

    private static void checkForOverlapping(List<Match> sortedMatches) {
        for (int i = 0; i < sortedMatches.size(); i++) {
            Match first = sortedMatches.get(i);
            for (int j = i + 1; j < sortedMatches.size(); j++) {
                Match second = sortedMatches.get(j);
                if (second.getStart() < first.getEnd()) {
                    throw new RuntimeException("Found overlapping matches " + first + " " + second);
                }
            }
        }
    }

    public static void replaceMatches(List<Pair<Integer, Integer>> matches, String repl, MappedText mappedText) {
        List<Match> replacements = new ArrayList<>();
        for (Pair<Integer, Integer> match : matches) {
            replacements.add(new Match(match.getFirst(), match.getSecond(), repl));
        }
        replaceMatches(replacements, mappedText);
    }

    private static void replaceHTMLEntities(MappedText mappedText) {
        String text = mappedText.getText();
        Matcher m = HTMLEntities.htmlEntityPattern.matcher(text);
        ArrayList<Match> matches = new ArrayList<>();
        while (m.find()) {
            String entity = text.substring(m.start(), m.end());
            if (entity.matches("&#\\d{1,6};")) {
                char to = (char) Integer.parseInt(entity.substring(2, entity.length() - 1));
                matches.add(new Match(m.start(), m.end(), "" + to));
            } else {
                for (String key : HTMLEntities.entityMapping.keySet()) {
                    if (entity.equals(key)) {
                        char to = HTMLEntities.entityMapping.get(key);
                        matches.add(new Match(m.start(), m.end(), "" + to));
                        break;
                    }
                }
            }
        }
        replaceMatches(matches, mappedText);
    }

    public static String replaceHTMLEntities(String line) {
        Matcher m = HTMLEntities.htmlEntityPattern.matcher(line);
        ArrayList<Pair<Integer, Integer>> matches = new ArrayList<>();
        ArrayList<String> replacements = new ArrayList<>();
        while (m.find()) {
            String entity = line.substring(m.start(), m.end());
            if (entity.matches("&#\\d{1,6};")) {
                char to = (char) Integer.parseInt(entity.substring(2, entity.length() - 1));
                matches.add(new Pair<>(m.start(), m.end()));
                replacements.add("" + to);
            } else {
                for (String key : HTMLEntities.entityMapping.keySet()) {
                    if (entity.equals(key)) {
                        char to = HTMLEntities.entityMapping.get(key);
                        matches.add(new Pair<>(m.start(), m.end()));
                        replacements.add("" + to);
                        break;
                    }
                }
            }
        }
        StringBuilder result = new StringBuilder();
        int prevEnd = 0;
        for (int i = 0; i < matches.size(); i++) {
            Pair<Integer, Integer> match = matches.get(i);
            result.append(line.substring(prevEnd, match.getFirst()));
            result.append(replacements.get(i));
            prevEnd = match.getSecond();
        }
        result.append(line.substring(prevEnd));
        return result.toString();
    }

    /**
     * This method is faster (for some unknown reason) then String.toLowerCase();
     *
     * @param inputFile
     */

    public static void convertToLowerCase(char[] inputFile) {
        for (int i = 0; i < inputFile.length; i++) {
            inputFile[i] = Character.toLowerCase(inputFile[i]);
        }
    }

    public static void split(MappedText mappedText, int start, int end) {
        char[] newText = new char[end - start];
        int[] newMapping = new int[end - start];
        for (int i = start; i < end; i++) {
            newText[i - start] = mappedText.getTextArray()[i];
            newMapping[i - start] = mappedText.getMappingToOrig()[i];
        }
        mappedText.setText(newText);
        mappedText.setMappingToOrig(newMapping);
    }

    public static void replaceString(int start, int end, String repl, MappedText mappedText) {
        replaceMatches(Arrays.asList(new Match(start, end, repl)), mappedText);
    }

    public static void replaceAll(String regex, String repl, MappedText mappedText) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(mappedText.getText());
        ArrayList<Pair<Integer, Integer>> matches = new ArrayList<>();
        while (matcher.find()) {
            matches.add(new Pair<>(matcher.start(), matcher.end()));
        }
        replaceMatches(matches, repl, mappedText);
    }

    public static byte[] convert(String word) {
        byte[] result = word.getBytes();
        //Remove trailing zeros. Why are these here anyway?
        int ind = result.length;
        while (ind > 0 && result[ind - 1] == 0)
            ind--;
        if (ind < result.length)
            result = Arrays.copyOf(result, ind);
        return result;
    }

    public static byte[] convert(char[] word) {
        return convert(new String(word));
    }

    public static String convert(byte[] data) {
        return convert(data, 0, data.length);
    }

    public static String convert(byte[] data, int offset, int length) {
        Charset ch = Charset.forName("utf-8");
        ByteBuffer buff = ByteBuffer.wrap(data, offset, length);
        return ch.decode(buff).toString();
    }

    public static String convert(ByteBuffer value) {
        Charset ch = Charset.forName("utf-8");
        return ch.decode(value).toString();
    }

    public static String prepareWordForIndex(String word) {
        word = reduceCharacterDiversityLevel2(word);
        return new String(addSpaces(word));
    }

    private static char[] addSpaces(CharSequence word) {
        char[] newS = new char[word.length() + 2 * NUM_OF_PADDED_SPACES];
        for (int i = 0; i < newS.length; i++) {
            if (i < NUM_OF_PADDED_SPACES || i >= word.length() + NUM_OF_PADDED_SPACES) {
                newS[i] = ' ';
            } else {
                newS[i] = word.charAt(i - NUM_OF_PADDED_SPACES);
            }
        }
        return newS;
    }

    public static String removeAccents(char[] newS) {
        for (int i = 0; i < newS.length; i++) {
            char orig = newS[i];
            newS[i] = removeAccent(orig);
        }
        return new String(newS);
    }

    public static char removeAccent(char orig) {
        boolean nonAscii = (orig < 0 || orig > 0x0080);
        if (nonAscii) {
            return getMappedCharacter(orig);
        } else {
            return orig;
        }
    }

    private static char getMappedCharacter(char orig) {
        char mapping = Character.MAX_VALUE;
        if (orig < characterMapping.size()) {
            mapping = characterMapping.get(orig);
        }
        if (mapping == Character.MAX_VALUE) {
            //We don't know this mapping yet. Let's learn it:
            synchronized (characterMapping) {
                String s = new String(new char[]{orig});
                String mappedS = StringUtils.removeAccentsSlow(s);
                if (mappedS.isEmpty()) {
                    //This is probably part of a two-character letter. Aah unicode...
                    mapping = orig;
                } else {
                    mapping = mappedS.charAt(0);
                }
                if (characterMapping.size() <= orig) {
                    for (int i = characterMapping.size(); i <= orig; i++) {
                        characterMapping.add(Character.MAX_VALUE);
                    }
                }
                characterMapping.set(orig, mapping);
            }
        }
        return mapping;
    }

    public static String removeAccents(String origWord) {
        return removeAccents(origWord.toCharArray());
    }

    public static String removeIncorrectDash(String origWord) {
        return origWord.replace('–', '-');
    }

    public static int getPartition(String word, int numOfPartitions) {
        //We put all 'weird' words in the first partition
        if (word.isEmpty()) {
            return 0;
        }
        return getPartition(word.charAt(0), numOfPartitions);
    }

    public static int getPartition(char firstLetter, int numOfPartitions) {
        char firstLetterLowerCase = Character.toLowerCase(firstLetter);
        if (firstLetterLowerCase < 'a' || firstLetterLowerCase > 'z') {
            return 0;
        } else {
            return (firstLetterLowerCase - 'a') * numOfPartitions / ('z' - 'a' + 1);
        }
    }

    public static boolean crossesParagraphBoundaries(String ngram) {
        for (int i = 0; i < ngram.length(); i++) {
            char c = ngram.charAt(i);
            if (c == '\r' || c == '\n' || c == '\t') {
                return true;
            }
        }
        return false;
    }

    public static String escapeFileName(String inputString) {
        return escapeString(inputString, '_', escapeFileNameMapping);
    }

    public static String unescapeFileName(String inputString) {
        return unescapeString(inputString, '_', escapeFileNameMapping);
    }

    private static String escapeString(String inputString, char escapeChar, Map<Character, Character> mapping) {
        StringBuilder result = new StringBuilder();
        int last = 0;
        for (int pos = 0; pos < inputString.length(); pos++) {
            char curr = inputString.charAt(pos);
            Character replacement;
            if (curr == escapeChar) {
                replacement = escapeChar;
            } else {
                replacement = mapping.get(curr);
            }
            if (replacement != null) {
                result.append(inputString.substring(last, pos));
                result.append(escapeChar);
                result.append(replacement);
                last = pos + 1;
            }
        }
        result.append(inputString.substring(last));
        String finalResult = result.toString();
        //assert (unescapeString(finalResult, escapeChar, mapping).equals(inputString)); //TODO remove for performance
        return finalResult;
    }

    private static String unescapeString(String inputString, char escapeChar, Map<Character, Character> mapping) {
        StringBuilder result = new StringBuilder();
        int last = 0;
        for (int pos = 0; pos < inputString.length(); pos++) {
            char curr = inputString.charAt(pos);
            if (curr == escapeChar) {
                if (pos + 1 >= inputString.length()) {
                    throw new RuntimeException("Incorrect string " + inputString + " for escape char " + escapeChar);
                }
                char next = inputString.charAt(pos + 1);
                Character orig = findOrig(escapeChar, mapping, next);
                if (orig == null) {
                    throw new RuntimeException("Incorrect string " + inputString + " for escape char " + escapeChar);
                }
                result.append(inputString.substring(last, pos));
                result.append(orig);
                pos++;
                last = pos + 1;
            }
        }
        result.append(inputString.substring(last));
        return result.toString();
    }

    private static Character findOrig(char escapeChar, Map<Character, Character> mapping, char next) {
        if (next == escapeChar) {
            return escapeChar;
        }
        for (Map.Entry<Character, Character> entry : mapping.entrySet()) {
            if (entry.getValue() == next) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static String escapeTab(String inputString) {
        return escapeString(inputString, '_', escapeTab);
    }

    public static String unescapeTab(String inputString) {
        return unescapeString(inputString, '_', escapeTab);
    }

    public static String unescapePathName(String inputString) {
        return unescapeString(inputString, '_', escapePath);
    }

    public static String escapeNewLine(String inputString) {
        return escapeString(inputString, '§', escapeNewLine);
    }

    public static String unescapeNewLine(String inputString) {
        return unescapeString(inputString, '§', escapeNewLine);
    }

    public static String escapePathName(String inputString) {
        return escapeString(inputString, '_', escapePath);
    }

    public static String removeRedundantWhiteSpace(String origString) {
        String newString1 = origString;
        String newString2 = newString1.replaceAll("\\s", " ");
        while (!newString2.equals(newString1)) {
            newString1 = newString2;
            newString2 = newString1.replaceAll("  ", " "); //Replace two spaces by one
        }
        return newString2.trim();
    }

    public static boolean isPossibleName(String ngram) {
        return namePattern.matcher(ngram).matches();
    }

    public static String reduceCharacterDiversityLevel1(SimpleString word) {
        return reduceCharacterDiversityLevel1(word.getS());
    }

    public static String reduceCharacterDiversityLevel1(String word) {
        char[] data = word.toCharArray();
        return reduceCharacterDiversityLevel1(data);
    }

    public static String reduceCharacterDiversityLevel1(char[] text) {
        convertToLowerCase(text);
        return reduceCharacterDiversityLevel0(text);
    }

    public static String reduceCharacterDiversityLevel0(String word) {
        return reduceCharacterDiversityLevel0(word.toCharArray());
    }

    public static String reduceCharacterDiversityLevel0(char[] word) {
        //Replace all numbers with 0's:
        replaceDigits(word);
        return new String(word);
    }

    public static String reduceCharacterDiversityLevel2(String word) {
        return removeAccents(reduceCharacterDiversityLevel1(word));
    }

    public static String reduceCharacterDiversityLevel2(SimpleString word) {
        return reduceCharacterDiversityLevel2(word.getS());
    }

    public static String reduceCharacterDiversityLevel3(String word) {
        return toVownNonVowl(reduceCharacterDiversityLevel2(word));
    }

    public static String reduceCharacterDiversityLevel(int level, String text) {
        switch (level) {
            case 0:
                return reduceCharacterDiversityLevel0(text);
            case 1:
                return reduceCharacterDiversityLevel1(text);
            case 2:
                return reduceCharacterDiversityLevel2(text);
            case 3:
                return reduceCharacterDiversityLevel3(text);
        }
        throw new RuntimeException("Unknown level " + level);
    }

    private static String toVownNonVowl(String s) {
        char[] result = new char[s.length()];
        for (int i = 0; i < s.length(); i++) {
            if (isASCIIVowel(result[i])) {
                result[i] = 'v';
            } else if (isASCIIConsonant(result[i])) {
                result[i] = 'c';
            } else if (result[i] == ' ') {
                result[i] = ' ';
            } else {
                result[i] = 'o';
            }
        }
        return new String(result);
    }

    private static void replaceDigits(char[] data) {
        //Replace all numbers with 0's:
        for (int i = 0; i < data.length; i++)
            if (Character.isDigit(data[i]))
                data[i] = '0';
    }

    public static String urlEncode(String input) {
        try {
            return URLEncoder.encode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String urlDecode(String input) {
        try {
            return URLDecoder.decode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean mightBeUrlOrEmailOrHashTag(String fullString) {
        fullString = fullString.toLowerCase();
        if (fullString.contains("@")) {
            return true;
        } else if (fullString.contains("#")) {
            return true; //twitter hashtags
        } else {
            if (!fullString.contains(".")) {
                return false;
            } else if (fullString.contains("/")) {
                return true;
            } else if (fullString.contains("#")) {
                return true;
            } else if (fullString.contains("www")) {
                return true;
            } else {
                for (String topLevelDomain : topLevelDomains) {
                    if (fullString.matches(".*\\." + topLevelDomain + "$") || fullString.matches(".*\\." + topLevelDomain + "\\W.*")) {
                        return true;
                    }
                }

            }
        }
        return false;
    }

    public static List<SimpleString> splitInWords(String text, Set<String> wordsWithPunct) {
        List<SimpleString> result = new ArrayList<>();
        WordIterator wordIterator = new WordIterator(text, wordsWithPunct);
        while (wordIterator.hasNext()) {
            result.add(wordIterator.next());
        }
        return result;
    }

    public static String createStringContext(SimpleString ex, int length) {
        String bf = ex.getOrigText().substring(Math.max(0, ex.getStart() - length), ex.getStart());
        String er = "**" + ex.getOrigText().substring(ex.getStart(), ex.getEnd()) + "**";
        String af = ex.getOrigText().substring(ex.getEnd(), Math.min(ex.getOrigText().length(), ex.getEnd() + length));
        String result = bf + er + af;
        result = result.replaceAll("\n", " ").replaceAll("\t", " ");
        while (result.contains("  ")) {
            result = result.replaceAll("  ", " ");
        }
        return result;
    }

    public static ExtendedString getPrevWord(SimpleString err, Set<String> wordsWithPunctuation) {
        return WordIterator.findWord(err.getOrigText().toCharArray(), err.getStart(), Direction.Left, wordsWithPunctuation);
    }

    public static ExtendedString getNextWord(SimpleString err, Set<String> wordsWithPunctuation) {
        return WordIterator.findWord(err.getOrigText().toCharArray(), err.getEnd(), Direction.Right, wordsWithPunctuation);
    }

    public static String getLastNgram(SimpleString example, int length) {
        String tagS = example.getS();
        while (tagS.length() < length) {
            tagS += " ";
        }
        return tagS.substring(tagS.length() - length);
    }

    public static String getFirstNgram(SimpleString example, int length) {
        String tagS = example.getS();
        while (tagS.length() < length) {
            tagS = " " + tagS;
        }
        return tagS.substring(0, length);
    }

    public static boolean isCapital(char letter) {
        return Character.isUpperCase(removeAccent(letter));
    }

    public static String getSuffix(String word, int length, int reductionLevel) {
        String suffix = StringUtils.reduceCharacterDiversityLevel(reductionLevel, word);
        return suffix.substring(Math.max(0, suffix.length() - length));
    }

    public static String getSuffix(SimpleString word, int length, int reductionLevel) {
        return getSuffix(word.getS(), length, reductionLevel);
    }
}
