package be.bagofwords.text;

import be.bagofwords.util.HashUtils;

public class BowStringImpl implements CharSequence, Comparable<BowStringImpl>, BowString {

    public final Text text;
    public int start;
    public int end;

    public BowStringImpl(String text) {
        this(text, 0, text.length());
    }

    public BowStringImpl(String text, int start, int end) {
        this(new TransientText(text), start, end);
    }

    public BowStringImpl(Text text, int start, int end) {
        this.text = text;
        this.start = start;
        this.end = end;
    }

    @Override
    public int hashCode() {
        return HashUtils.integerHashCode(getS());
    }

    public BowStringImpl clone() {
        return new BowStringImpl(text, start, end);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BowString) {
            BowString other = (BowString) obj;
            return getText().getId().equals(other.getText().getId()) && getStart() == other.getStart() && getEnd() == other.getEnd();
        } else {
            return obj instanceof String && getS().equals(obj);
        }
    }

    public String toString() {
        return getS();
    }

    @Override
    public char charAt(int index) {
        return text.getText().charAt(start + index);
    }

    @Override
    public int length() {
        return end - start;
    }

    @Override
    public BowStringImpl subSequence(int start, int end) {
        return new BowStringImpl(text, this.start + start, this.start + end);
    }

    @Override
    public int compareTo(BowStringImpl o) {
        return getS().compareTo(o.getS());
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String getS() {
        return text.getText().substring(start, end);
    }

    @Override
    public Text getText() {
        return text;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }
}
