package be.bagofwords.text;

public interface Text {

    public String getId();

    public String getText();

    default int length() {
        return getText().length();
    }

    default char charAt(int pos) {
        return getText().charAt(pos);
    }

    default String substring(int start, int end) {
        return getText().substring(start, end);
    }
}
