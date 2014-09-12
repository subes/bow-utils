package be.bow.text;

/**
 * String that exposes the underlying text from where the string originated
 */

public interface SimpleString {

    int getStart();

    int getEnd();

    String getS();

    String getOrigText();

}
