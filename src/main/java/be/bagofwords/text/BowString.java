package be.bagofwords.text;

/**
 * String that exposes the underlying text from where the string originated
 */

public interface BowString {

    int getStart();

    int getEnd();

    String getS();

    Text getText();

    default String getTextS() {
        return getText().getText();
    }

}
