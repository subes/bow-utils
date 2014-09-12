package be.bow.text;

import be.bow.util.HashUtils;

import java.util.Arrays;

public class ExtendedString implements CharSequence, Comparable<ExtendedString>, SimpleString {

    public final char[] data;
    public int start;
    public int end;

    public ExtendedString(char[] data, int start, int end) {
        this.data = data;
        this.start = start;
        this.end = end;
    }

    public ExtendedString(char[] data) {
        this(data, 0, data.length);
    }

    public ExtendedString(String s) {
        this(s.toCharArray());
    }

    @Override
    public int hashCode() {
        return HashUtils.integerHashCode(getS());
    }

    public ExtendedString clone() {
        return new ExtendedString(data, start, end);
    }

    @Override
    public boolean equals(Object obj) {
        char[] comp = null;
        int compStart = -1;
        int compEnd = -1;
        if (obj instanceof ExtendedString) {
            ExtendedString s = (ExtendedString) obj;
            comp = s.data;
            compStart = s.start;
            compEnd = s.end;
        } else if (obj instanceof String) {
            String s = (String) obj;
            comp = s.toCharArray();
            compStart = 0;
            compEnd = comp.length;
        }
        if (comp != null) {
            boolean same = (compEnd - compStart) == (end - start);
            for (int i = start; same && i < end; i++)
                same &= data[i] == comp[i - start + compStart];
            return same;
        } else
            return false;
    }

    public String toString() {
        return getS();
    }

    @Override
    public char charAt(int index) {
        return data[start + index];
    }

    @Override
    public int length() {
        return end - start;
    }

    @Override
    public ExtendedString subSequence(int start, int end) {
        return new ExtendedString(data, this.start + start, this.start + end);
    }

    @Override
    public int compareTo(ExtendedString o) {
        int length = Math.min(length(), o.length());
        for (int i = 0; i < length; i++) {
            if (data[start + i] < o.data[o.start + i])
                return -1;
            else if (data[start + i] > o.data[o.start + i])
                return 1;
        }
        if (length() < o.length())
            return -1;
        else if (o.length() < length())
            return 1;
        else
            return 0;
    }

    public ExtendedString toLowerCase() {
        char[] newD = Arrays.copyOfRange(data, start, end);
        for (int i = 0; i < newD.length; i++)
            if (Character.isUpperCase(newD[i]))
                newD[i] = Character.toLowerCase(newD[i]);
        return new ExtendedString(newD);
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String getS() {
        return new String(data, start, end - start);
    }

    @Override
    public String getOrigText() {
        return new String(data);
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }
}
