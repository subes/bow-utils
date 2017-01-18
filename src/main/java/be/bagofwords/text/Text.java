package be.bagofwords.text;

public interface Text extends SequentialData {

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

    default String substring(int start) {
        return getText().substring(start);
    }

    @Override
    default int size() {
        return length();
    }

}
