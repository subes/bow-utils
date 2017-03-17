package be.bagofwords.text;

public interface Text extends SequentialData {

    String getText();

    int length();

    char charAt(int pos);

    String substring(int start, int end);

    String substring(int start);

}
