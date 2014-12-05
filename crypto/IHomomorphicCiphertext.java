package crypto;

import java.io.Serializable;

/**
 * This is a class of all homomorphic ciphertexts, i.e. ciphertexts that have
 * some arity 2 operation that can be performed on them to produce a new ciphertext
 * that somehow encodes information about the two operands.
 *
 * @author Matt Bernhard
 */
public interface IHomomorphicCiphertext<T extends IHomomorphicCiphertext> extends ICiphertext, Serializable {

    /**
     * Will perform some arity 2 homomorphic operation on the ciphertexts, depending on
     * how the operation is specified in the concrete (e.g. multiplication for exponential
     * ElGamal or addition for standard ElGamal.
     *
     * @param operand The ciphertext to "add" yourself to
     * @return the result of computing  the arity two function between this object and the parameter operand
     */
    public T operate(T operand);
}
