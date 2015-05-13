package crypto.adder;

/**
 * todo fix comments
 */
public class SearchSpaceExhaustedException extends RuntimeException {
    private static final long serialVersionUID = 0L;

    /**
     * Constructs an <code>SearchSpaceExhaustedException</code> with
     * <tt>null</tt> as its error message string.
     */
    public SearchSpaceExhaustedException() {
        super();
    }

    /**
     * Constructs a <code>SearchSpaceExhaustedException</code>, saving a
     * reference to the error message string <tt>s</tt> for later retrieval by
     * the <tt>getMessage</tt> method.
     *
     * @param s the detail message
     */
    public SearchSpaceExhaustedException(String s) {
        super(s);
    }
}
