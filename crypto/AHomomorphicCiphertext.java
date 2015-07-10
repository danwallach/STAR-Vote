package crypto;

import java.io.Serializable;
import java.util.List;

/**
 * This is a class of all homomorphic ciphertexts, i.e. ciphertexts that have
 * some arity 2 operation that can be performed on them to produce a new ciphertext
 * that somehow encodes information about the two operands.
 *
 * @author Matt Bernhard
 */
public abstract class AHomomorphicCiphertext<T extends AHomomorphicCiphertext<T>> implements Provable, Serializable {

    protected int size;

    public AHomomorphicCiphertext(int size){
        this.size = size;
    }

    /**
     * Will perform some arity 2 homomorphic operation on the ciphertexts, depending on
     * how the operation is specified in the concrete (e.g. multiplication for exponential
     * ElGamal or addition for standard ElGamal. This treats the list of operands as if their
     * values are dependent (i.e. the sum of all adds up to the size)
     *
     * @param operands  the ciphertexts to "add" yourself to
     * @return          the result of computing  the arity two function between this object and the parameter operand
     */
    public abstract T operateDependent(List<T> operands, IPublicKey PEK);

    /**
     * Will perform some arity 2 homomorphic operation on the ciphertexts, depending on
     * how the operation is specified in the concrete (e.g. multiplication for exponential
     * ElGamal or addition for standard ElGamal. This assumes that the values are independent
     * (i.e. their sum is in the domain [0,size1+size2])
     *
     * @param operand   the ciphertext to "add" yourself to
     * @return          the result of computing  the arity two function between this object and the parameter operand
     */
    public abstract T operateIndependent(T operand, IPublicKey PEK);

    /**
     * @return  the amount of ciphertexts operated into this one
     */
    public abstract int getSize();
}
