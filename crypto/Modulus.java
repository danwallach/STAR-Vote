/**
  * This file is part of VoteBox.
  * 
  * VoteBox is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3 as published by
  * the Free Software Foundation.
  * 
  * You should have received a copy of the GNU General Public License
  * along with VoteBox, found in the root of any distribution or
  * repository containing all or part of VoteBox.
  * 
  * THIS SOFTWARE IS PROVIDED BY WILLIAM MARSH RICE UNIVERSITY, HOUSTON,
  * TX AND IS PROVIDED 'AS IS' AND WITHOUT ANY EXPRESS, IMPLIED OR
  * STATUTORY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, WARRANTIES OF
  * ACCURACY, COMPLETENESS, AND NONINFRINGEMENT.  THE SOFTWARE USER SHALL
  * INDEMNIFY, DEFEND AND HOLD HARMLESS RICE UNIVERSITY AND ITS FACULTY,
  * STAFF AND STUDENTS FROM ANY AND ALL CLAIMS, ACTIONS, DAMAGES, LOSSES,
  * LIABILITIES, COSTS AND EXPENSES, INCLUDING ATTORNEYS' FEES AND COURT
  * COSTS, DIRECTLY OR INDIRECTLY ARISING OUR OF OR IN CONNECTION WITH
  * ACCESS OR USE OF THE SOFTWARE.
 */

package crypto;

import java.math.BigInteger;
import java.util.Random;

/**
 * This class represents a group and its order,used in exponential ElGamal.
 *
  */
public class Modulus {

    /* half-way between 1024 and 2048*/
    public static final int DEFAULT_PRIME_BITS = 1536;

    /* when testing a prime number, test to confidence 1-1/(2^PrimeConfidence)*/
    public static final int DEFAULT_PRIME_CONFIDENCE = 64;

    private static BigInteger one = BigInteger.ONE;
    private static BigInteger two = one.add(one);

    private Random randomBits = new java.security.SecureRandom();

    private BigInteger generator, modulus;
    private int numPrimeBits, primeConfidence;

    /**
     *  Initializes the modulus by finding a generator for a group that is close to a specified size.
     *
     * @param numPrimeBits          Number of bits in the prime modulus of the group
     * @param primeConfidence       Confidence interval for the primality of the random modulus
     */
    private void init(int numPrimeBits, int primeConfidence) {

        this.numPrimeBits = numPrimeBits;
        this.primeConfidence = primeConfidence;

        /* Iterate until a appropriately sized probable prime is found */
        for (;;) {

            /* p is a random big integer of size numPrimeBits*/
            BigInteger p = new BigInteger(numPrimeBits, primeConfidence,randomBits);

            /*q is a guess of a prime number based on p*/
            BigInteger q = p.multiply(two).add(one);

            /* If p is a probable prime within our confidence interval */
            if (q.isProbablePrime(primeConfidence)) {

               /* Iterate until we find a group of order q with generator g where g^2 < q  */
                for (;;) {

                    /* pick a random generator for our group */
                    BigInteger g = new BigInteger(numPrimeBits / 2, randomBits);

                    /* square it */
                    BigInteger gsq = g.multiply(g);

                    /* g^2 needs to be less than q */
                    if (gsq.compareTo(q) >= 0)
                        continue;

                    /* degenerate case */
                    if (gsq.equals(one))
                        continue;

                    /* if we got here, that means that q is our modulus and gsq is our generator*/

                    generator = gsq;
                    modulus = q;
                    return;
                }
            }
        }
    }

    /**
     * Establish a generator and modulus for Diffie-Hellman or ElGamal
     * encryption that satisfies the "standard" property where you don't leak
     * information if the information is a square or something or not.
     *
     * @param numPrimeBits          Number of bits in the prime modulus of the group
     * @param primeConfidence       Confidence interval for the primality of the random modulus
     *
     */

    public Modulus(int numPrimeBits, int primeConfidence) {
        init(numPrimeBits, primeConfidence);
    }

    /**
     * Use default (cryptographically strong) values for prime bits and confidence
     */
    public Modulus() {
        init(DEFAULT_PRIME_BITS, DEFAULT_PRIME_CONFIDENCE);
    }

    /**
     * Constructor that takes in pre defined generator and modulus in addition to PrimeBits and PrimeConfidence
     *
     * @param numPrimeBits          Number of bits in the prime modulus of the group
     * @param primeConfidence       Confidence interval for the primality of the random modulus
     * @param generator             Generator for our group
     * @param modulus               The order of the group
     */

    Modulus(int numPrimeBits, int primeConfidence, String generator,String modulus) {

        this.numPrimeBits = numPrimeBits;
        this.primeConfidence = primeConfidence;
        this.generator = new BigInteger(generator);
        this.modulus = new BigInteger(modulus);

        /* generator should be smaller than modulus */
        assert this.generator.compareTo(this.modulus) < 0;

        /* modulus should be prime! */
        assert this.modulus.isProbablePrime(this.primeConfidence);
    }

    /**
     * @return the generator
     */
    public BigInteger getGenerator() {
        return generator;
    }

    /**
     * @param generator     the generator to set
     */
    public void setGenerator(BigInteger generator) {
        this.generator = generator;
    }

    /**
     * @return the modulus
     */
    public BigInteger getModulus() {
        return modulus;

    }


    /**
     * @return random value less than the modulus
     */
    public BigInteger getRandomValue() {
        BigInteger returnVal;

        /* Iterate until we find a random number in the group */
        for (;;) {
            returnVal = new BigInteger(numPrimeBits, randomBits);


            /* the random number needs to be less than the modulus, otherwise try again. This isn't exactly optimal, but it works. */

            if (returnVal.compareTo(modulus) < 0)
                return returnVal;
        }
    }

    /**
     * @return      generator and the modulus as strings.
     */
    @Override
    public String toString() {

        return "NumPrimeBits: " + numPrimeBits + "\nPrimeConfidence: "
                + primeConfidence + "\nGenerator: " + generator.toString()
                + "\nModulus: " + modulus.toString();
    }

    /**
     * Command-line utility to generate and print a modulus
     */
    public static void main(String args[]) {
        int numBits = 300;
        int confidence = 30;
        if (args.length == 2) {
            numBits = Integer.parseInt(args[0]);
            confidence = Integer.parseInt(args[1]);
        }
        else if (args.length != 0) {
            System.out.println("Usage: java Modulus [num-bits confidence]");
            System.exit(0);
        }
        Modulus m = new Modulus(numBits, confidence);
        System.out.println(m);
    }

}
