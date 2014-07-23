package verifier.auditoriumverifierplugins;

import auditorium.IncorrectFormatException;
import auditorium.Message;
import sexpression.ASExpression;
import sexpression.StringExpression;
import sexpression.stream.ASEInputStreamReader;
import sexpression.stream.InvalidVerbatimStreamException;
import verifier.*;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This is a plugin for verifier that will look at the hashes contained in each logged message and recompute the hash chain
 * to ensure that nothing has gone awry.
 *
 * @author Matt Bernhard
 */
public class HashChainVerifier implements IVerifierPlugin {

    private Verifier verifier;

    private ASExpression incrementalHash;

    /**
     * Initialize the plugin.
     *
     * @param verifier the verifier instance that is constructing this plugin
     * @throws PluginException
     */
    @Override
    public void init(Verifier verifier)  {
        incrementalHash = StringExpression.makeString(StringExpression.makeString("0000000000").getSHA1());
        this.verifier = verifier;
    }


    /**
     * This will look through each message in the log and ensure that its hash chain hasn't been compromised.
     *
     * TODO We should probably retool the way verifier handles plugins since they seem to be somewhat backwards
     *
     */
    public void verify() throws HashChainCompromisedException {
        /* Initialize that hash chain with string 0000000000, the known starting value for our hash */
        ASExpression hash = StringExpression.makeString(StringExpression.makeString("0000000000").getSHA1());

        try {
            ASEInputStreamReader in = new ASEInputStreamReader( new FileInputStream(new File(verifier.getArgs().get("log"))));

            ASExpression exp;

            /* Loop until end of file and load into dag to build set */
            while ((exp = in.read()) != null) {
                Message msg = new Message(exp);

                /* Build a new message based on the one found in the log */
                Message compare = new Message(msg.getType(), msg.getFrom(), msg.getSequence(), msg.getDatum());

                /* Recompute its hash, including the value we expect it to have for its hash */
                compare.chain(hash);
                hash = compare.getChainedHash();

                /* Compare the newly built hash with the hash we read in */
                if(!hash.equals(msg.getChainedHash()))
                    throw new HashChainCompromisedException("The hash chain failed to verify!");
            }
        } catch (EOFException ignored) {
        } catch (IOException | IncorrectFormatException | InvalidVerbatimStreamException e) {
            throw new PluginException("auditorium", e);
        }
    }

    /**
     * This will allow our verifier to check to make sure that messages, as they are logged,
     * are properly hash chained.
     *
     * @param entry the new message to check
     */
    public void verifyIncremental(Message entry) throws HashChainCompromisedException {

        /* Build a new message based on the one found in the log */
        Message compare = new Message(entry.getType(), entry.getFrom(), entry.getSequence(), entry.getDatum());

        /* Recompute its hash, including the value we expect it to have for its hash */
        compare.chain(incrementalHash);
        incrementalHash = compare.getHash();

        /* Compare the newly built hash with the hash we read in */
        if(!incrementalHash.equals(entry.getHash()))
            throw new HashChainCompromisedException("The hash chain failed to verify!");
    }
}
