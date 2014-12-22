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

    default String uniqueId() {
        return getText().getId() + "_" + getStart() + "_" + getEnd();
    }

    default boolean equals(BowString other) {
        return getText().getId().equals(other.getText().getId()) && getStart() == other.getStart() && getEnd() == other.getEnd();
    }


}
