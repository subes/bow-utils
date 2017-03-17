package be.bagofwords.text;

/**
 * Throwaway object to hold text
 */

public class TransientText implements Text {

    private static int counter = 0;

    private int thisCounter;
    private String text;

    public TransientText(String text) {
        this.text = text;
        this.thisCounter = counter++;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getId() {
        return "transient_text_" + Integer.toString(thisCounter);
    }

    public int length() {
        return getText().length();
    }

    public char charAt(int pos) {
        return getText().charAt(pos);
    }

    public String substring(int start, int end) {
        return getText().substring(start, end);
    }

    public String substring(int start) {
        return getText().substring(start);
    }

    @Override
    public int size() {
        return length();
    }
}
