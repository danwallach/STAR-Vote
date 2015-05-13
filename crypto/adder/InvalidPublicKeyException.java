package crypto.adder;

/* TODO fix this commenting */
public class InvalidPublicKeyException extends RuntimeException {
    private static final long serialVersionUID = 0L;

    /**
     * Constructs an <code>InvalidPublicKeyException</code> with
     * <tt>null</tt> as its error message string.
     */
    public InvalidPublicKeyException() {
        super();
    }

    /**
     * Constructs a <code>InvalidPublicKeyException</code>, saving a reference
     * to the error message string <tt>s</tt> for later retrieval by the
     * <tt>getMessage</tt> method.
     *
     * @param s the detail message
     */
    public InvalidPublicKeyException(String s) {
        super(s);
    }
}
