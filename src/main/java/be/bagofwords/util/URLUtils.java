package be.bagofwords.util;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLUtils {

    public static String makeAbsolute(String url, String context) {
        if (url.startsWith("/")) {
            return getDomain(context) + url;
        } else {
            //Is relative link.
            int posSlash = context.length() - 1;
            while (posSlash >= 0 && context.charAt(posSlash) != '/')
                posSlash--;
            if (posSlash >= 0)
                context = context.substring(0, posSlash);
            while (url.startsWith("../") || url.startsWith("./")) {
                if (url.startsWith("./"))
                    url = url.substring(2);
                else {
                    posSlash = context.length() - 2;
                    while (posSlash >= 0 && context.charAt(posSlash) != '/')
                        posSlash--;
                    url = url.substring(3);
                    if (posSlash >= 0) {
                        context = context.substring(0, posSlash);
                    }
                }
            }
            return context + "/" + url;
        }
    }

    public static String getSuperDomain(String url) {
        url = getDomain(url);
        int start = url.length() - 1;
        int numOfDotsPassed = 0;
        while (start >= 0 && (numOfDotsPassed < 1 || url.charAt(start) != '.')) {
            if (url.charAt(start) == '.')
                numOfDotsPassed++;
            start--;
        }
        url = url.substring(start + 1);
        return url;
    }

    public static String getDomain(String url) {
        url = removeProtocol(url.toLowerCase());
        int end = 0;
        while (end < url.length() && url.charAt(end) != '/' && url.charAt(end) != '?')
            end++;
        return url.substring(0, end);
    }

    public static String getPath(String url) {
        url = removeProtocol(url);
        int end = 0;
        while (end < url.length() && url.charAt(end) != '/' && url.charAt(end) != '?')
            end++;
        if (end < url.length()) {
            return url.substring(end);
        } else {
            return "/"; //default path
        }
    }

    static final Pattern linkP = Pattern.compile("(?<=href=)[^>\n ]+");

    public static ArrayList<String> findLinks(CharSequence html, String context) {
        Matcher m = linkP.matcher(html);
        ArrayList<String> result = new ArrayList<>();
        while (m.find()) {
            String link = m.group();
            //Sometimes links contain &qout;
            if (link.startsWith("&qout;"))
                link = link.substring(6);
            if (link.endsWith("&qout;"))
                link = link.substring(0, link.length() - 6);
            link = link.replaceAll("\\\\", "");
            if (!link.isEmpty() && (link.charAt(0) == '\"' || link.charAt(0) == '\''))
                link = link.substring(1);
            if (!link.isEmpty() && (link.charAt(link.length() - 1) == '\"' || link.charAt(link.length() - 1) == '\''))
                link = link.substring(0, link.length() - 1);
            int posOfHash = -1;
            for (int i = 0; i < link.length(); i++)
                if (link.charAt(i) == '#')
                    posOfHash = i;
            if (posOfHash != -1)
                link = link.substring(0, posOfHash);
            Matcher pM = protocolP.matcher(link);
            if (pM.find()) {
                //Has protocol :
                if (pM.group().equalsIgnoreCase("http://"))
                    result.add(link.substring(7));
            } else {
                //No protocol specified:
                String lowCaLink = link.toLowerCase();
                if (!lowCaLink.contains("javascript:") && !lowCaLink.contains("mailto:") && link.length() > 3) {
                    link = makeAbsolute(link, context);
                    result.add(link);
                }
            }
        }
        return result;
    }

    public static final Pattern protocolP = Pattern.compile("^[A-Za-z]{3,10}://");

    public static String removeProtocol(String url) {
        Matcher m = protocolP.matcher(url);
        url = m.replaceFirst("");
        return url;
    }

    public static String getProtocol(String url) {
        Matcher m = protocolP.matcher(url);
        if (m.find()) {
            String result = m.group().toLowerCase();
            return result.substring(0, result.length() - 3);
        } else
            return "http"; //Assume default
    }

    public static boolean isPossibleUrl1(String input) {
        return input.toLowerCase().matches("[a-z]+\\.(com|be|ly|org|net|nl)");
    }

    public static boolean isPossibleUrl2(String input) {
        return input.toLowerCase().matches("http://(\\w+\\.)+\\w{2,3}[^ ]*");
    }

    public static String getHostName() {
        String hostname;
        try {
            hostname = java.net.InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException("Failed to find localhost name!", e);
        }
        return hostname;
    }
}
