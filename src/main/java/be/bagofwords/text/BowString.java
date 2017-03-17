package be.bagofwords.text;

/**
 * String that exposes the underlying text from where the string originated
 */

public interface BowString extends CharSequence {

    int getStart();

    int getEnd();

    String getS();

    Text getText();

    String getTextS();

    String uniqueId();

}
