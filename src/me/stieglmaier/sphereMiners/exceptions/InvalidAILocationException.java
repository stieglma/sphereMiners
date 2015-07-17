package me.stieglmaier.sphereMiners.exceptions;

/**
 * This exception should be thrown if the location of an AI is invalid in case
 * they should be initialized. An invalid location equals an invalid filename.
 */
public final class InvalidAILocationException extends Exception {

    /**
     * id for serializable classes
     */
    private static final long serialVersionUID = 1170066104466868734L;

    /**
     * Constructs the exception without any further text.
     */
    public InvalidAILocationException() {
        super();
    }

    /**
     * Constructs the exception with the given text.
     *
     * @param s the text which should be added to the exception
     */
    public InvalidAILocationException(String s) {
        super(s);
    }
}
