package crypto.adder;

import crypto.EncryptedRaceSelection;
import crypto.AHomomorphicCiphertext;
import crypto.IPublicKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a collection of RaceSelections for a single 'race', where
 * a 'race' is a single contest between multiple candidates in an election.
 */
public class Race<T extends AHomomorphicCiphertext<T>> {

    /** The public key used to tally */
    private IPublicKey PEK;

    /** The List of all race selections cast in this Race */
    private List<EncryptedRaceSelection<T>> raceSelections;

    /** The List of all candidates in this Race */
    private List<String> candidates;

    /**
     * Creates a new election.
     *
     * @param publicKey         the public key
     */
    public Race(IPublicKey publicKey, List<String> candidates) {
        this.PEK = publicKey;
        this.raceSelections = new ArrayList<>();
        this.candidates = candidates;
    }

    /**
     * Gets the raceSelections of this election.
     *
     * @return          the raceSelections
     */
    public List<EncryptedRaceSelection<T>> getRaceSelections() {
        return raceSelections;
    }

    /**
     * Casts the given raceSelection in this election.
     *
     * @param raceSelection      the raceSelection
     */
    public void castRaceSelection(EncryptedRaceSelection<T> raceSelection) {
        raceSelections.add(raceSelection);
    }

    /**
     * Sums the raceSelections cast in this election.
     * This is the product of the raceSelections modulo <tt>p</tt>).
     *
     * @return          a vote representing the total of the given list of raceSelections
     */
    public EncryptedRaceSelection<T> sumRaceSelections() {

        /* Pull out the first vote */
        EncryptedRaceSelection<T> v = raceSelections.get(0);

        /* Create a new multiplicative identity */
        EncryptedRaceSelection<T> total = EncryptedRaceSelection.identity(v, PEK);

        /* Multiply all the raceSelections together and recompute proof */
        for (EncryptedRaceSelection<T> rs : raceSelections)
            total = rs.operate(total, PEK);

        /* ---------------- TESTING ---------------- */

        System.out.println("In Race.sumRaceSelections() -- Testing single race selection summed, Max expected value: " + raceSelections.size());
        System.out.println("In Race.sumRaceSelections() -- [Single vote summed] sum verfied: " + total.verify(0, raceSelections.size(), PEK));
        System.out.println("-----------------");

        /* ------------------------------------------ */

        return total;
    }

}
