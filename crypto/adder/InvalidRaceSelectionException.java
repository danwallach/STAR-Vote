package crypto.adder;

/* todo fix comments */
public class InvalidRaceSelectionException extends RuntimeException {
    private static final long serialVersionUID = 0L;

    /**
     * Constructs an <code>InvalidVoteException</code> with
     * <tt>null</tt> as its error message string.
     */
    public InvalidRaceSelectionException() {
        super();
    }

    /**
     * Constructs a <code>InvalidVoteException</code>, saving a reference
     * to the error message string <tt>s</tt> for later retrieval by the
     * <tt>getMessage</tt> method.
     *
     * @param s the detail message
     */
    public InvalidRaceSelectionException(String s) {
        super(s);
    }
}
