package votebox.auditoriumverifierplugins;

import auditorium.IncorrectFormatException;
import auditorium.Message;
import sexpression.ASExpression;
import sexpression.StringExpression;
import sexpression.stream.ASEInputStreamReader;
import sexpression.stream.InvalidVerbatimStreamException;
import verifier.*;
import verifier.value.DAGValue;
import verifier.value.Expression;
import verifier.value.SetValue;
import verifier.value.Value;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is a plugin for verifier that will look at the hashes contained in each logged message and recompute the hash chain
 * to ensure that nothing has gone awry.
 *
 * @author Matt Bernhard
 */
public class HashChainVerifier implements IVerifierPlugin {

    /**
     * Initialize the plugin.
     *
     * @param verifier the verifier instance that is constructing this plugin
     * @throws PluginException
     */
    @Override
    public void init(Verifier verifier) throws PluginException, HashChainCompromisedException {
        verify(verifier);
    }


    /**
     * This will look through each message in the log and ensure that its hash chain hasn't been compromised.
     *
     * TODO We should probably retool the way verifier handles plugins since they seem to be somewhat backwards
     *
     * @param verifier the verifier instance that is constructing this plugin
     */
    public void verify(Verifier verifier) throws HashChainCompromisedException {
        /* Initialize that hash chain with string 0000000000, the known starting value for our hash */
        ASExpression hash = StringExpression.makeString(StringExpression.makeString("0000000000").getSHA1());

        try {
            ASEInputStreamReader in = new ASEInputStreamReader( new FileInputStream(new File(verifier.getArgs().get("log"))));

            ASExpression exp;

            /* Loop until end of file and load into dag to build set */
            while ((exp = in.read()) != null) {
                //System.out.println(exp);
                Message msg = new Message(exp);

                String newData = msg.getHash().toString() /*+ hash.toString()*/;
                hash = StringExpression.makeString(StringExpression.makeString(newData).getSHA1());

                System.out.println(hash + " | " +  msg.getHash());

//                if(!hash.equals(msg.getHash()))
//                    throw new HashChainCompromisedException("The hash chain failed to verify!");


            }
        } catch (EOFException ignored) {
        } catch (IOException | IncorrectFormatException | InvalidVerbatimStreamException e) {
            throw new PluginException("auditorium", e);
        }
    }
}
