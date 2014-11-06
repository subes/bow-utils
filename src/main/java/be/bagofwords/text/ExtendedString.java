package be.bagofwords.text;

import be.bagofwords.util.HashUtils;

public class ExtendedString implements CharSequence, Comparable<ExtendedString>, SimpleString {

    public final String origText;
    public int start;
    public int end;

    public ExtendedString(String origText, int start, int end) {
        this.origText = origText;
        this.start = start;
        this.end = end;
    }

    public ExtendedString(String origText) {
        this(origText, 0, origText.length());
    }

    @Override
    public int hashCode() {
        return HashUtils.integerHashCode(getS());
    }

    public ExtendedString clone() {
        return new ExtendedString(origText, start, end);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExtendedString) {
            return getS().equals(((ExtendedString) obj).getS());
        } else {
            return obj instanceof String && getS().equals(obj);
        }
    }

    public String toString() {
        return getS();
    }

    @Override
    public char charAt(int index) {
        return origText.charAt(start + index);
    }

    @Override
    public int length() {
        return end - start;
    }

    @Override
    public ExtendedString subSequence(int start, int end) {
        return new ExtendedString(origText, this.start + start, this.start + end);
    }

    @Override
    public int compareTo(ExtendedString o) {
        return getS().compareTo(o.getS());
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String getS() {
        return origText.substring(start, end);
    }

    @Override
    public String getOrigText() {
        return origText;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }
}
