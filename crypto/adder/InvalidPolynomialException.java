package crypto.adder;

/* TODO fix this commenting */
public class InvalidPolynomialException extends RuntimeException {
    private static final long serialVersionUID = 0L;

    /**
     * Constructs an <code>InvalidPolynomialException</code> with
     * <tt>null</tt> as its error message string.
     */
    public InvalidPolynomialException() {
        super();
    }

    /**
     * Constructs a <code>InvalidPolynomialException</code>, saving a reference
     * to the error message string <tt>s</tt> for later retrieval by the
     * <tt>getMessage</tt> method.
     *
     * @param s the detail message
     */
    public InvalidPolynomialException(String s) {
        super(s);
    }
}
