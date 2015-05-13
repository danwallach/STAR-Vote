package crypto.adder;

/* todo fix this commenting */
public class InvalidPrivateKeyException extends RuntimeException {
    private static final long serialVersionUID = 0L;

    /**
     * Constructs an <code>InvalidPrivateKeyException</code> with
     * <tt>null</tt> as its error message string.
     */
    public InvalidPrivateKeyException() {
        super();
    }

    /**
     * Constructs a <code>InvalidPrivateKeyException</code>, saving a reference
     * to the error message string <tt>s</tt> for later retrieval by the
     * <tt>getMessage</tt> method.
     *
     * @param s the detail message
     */
    public InvalidPrivateKeyException(String s) {
        super(s);
    }
}
