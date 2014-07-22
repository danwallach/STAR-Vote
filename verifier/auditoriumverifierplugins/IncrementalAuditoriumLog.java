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

package verifier.auditoriumverifierplugins;

import auditorium.IncorrectFormatException;
import auditorium.Message;
import sexpression.ASExpression;
import verifier.*;
import verifier.value.DAGValue;
import verifier.value.Expression;
import verifier.value.SetValue;
import verifier.value.Value;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This verifier plugin maintains all-set and all-dag based on incremental log
 * data given via this API (rather than read from a file). It also registers the
 * signature-verify primitive.
 * 
 * @author kyle
 * 
 */
public class IncrementalAuditoriumLog implements IIncrementalPlugin {

	private Verifier verifier;
	private ArrayList<Expression> allset;
	private SetValue allsetValue;
	private DagBuilder alldag;
	private DAGValue alldagValue;
    private HashChainVerifier hashChainVerifier;

    public IncrementalAuditoriumLog(){}

    public IncrementalAuditoriumLog(HashChainVerifier hashChainVerifier) {
        this.hashChainVerifier = hashChainVerifier;
    }

	/**
	 * @see verifier.IVerifierPlugin#init(verifier.Verifier)
	 */
	public void init(Verifier verifier) {
		allset = new ArrayList<>();
		alldag = new DagBuilder();
		this.verifier = verifier;

        hashChainVerifier.init(verifier);

		registerHandlers();
		registerGlobals();
	}

	/**
	 * Add incremental log data.
	 * 
	 * @param entry Message to append to the log.
	 */
	public void addLogData(Message entry) throws InvalidLogEntryException{

        try {
            hashChainVerifier.verifyIncremental(entry);
        } catch (HashChainCompromisedException e) {
            /* TODO We should probably not throw a runtime exception, but some how note the hash chain was compromised. */
            throw new InvalidLogEntryException(e);
        }
        allset.add(new Expression(entry.toASE()));
		alldag.add(entry);
		registerGlobals();
	}

	/**
	 * Add incremental log data.
	 * 
	 * @param entry S-expression representing a message to append to the log.
	 */
	public void addLogData(ASExpression entry) throws InvalidLogEntryException {
		try {
            hashChainVerifier.verifyIncremental(new Message(entry));
			allset.add(new Expression(entry));
			alldag.add(new Message(entry));
			registerGlobals();
		} catch (IncorrectFormatException | HashChainCompromisedException e) { throw new InvalidLogEntryException(e); }
    }

	/**
	 * Add incremental log data.
	 * 
	 * @param entry Expression value representing a message to append to the log.
	 */
	public void addLogData(Expression entry) throws InvalidLogEntryException {
		try {
			allset.add(entry);
			alldag.add(new Message(entry.getASE()));
			registerGlobals();
		} catch (IncorrectFormatException e) { throw new InvalidLogEntryException(e); }
	}

	/**
	 * Seal the logs -- the verifier will no longer return a reduction now. If
	 * something isn't in the log the verifier will expect that it never will
	 * be.
	 */
	public void closeLog() {
		allsetValue.seal();
		alldagValue.seal();
	}

    /**
     * Initialization of global variables.
     */
	private void registerGlobals() {
		HashMap<String, Value> bindings = new HashMap<>();

		allsetValue = new SetValue(allset.toArray(new Expression[allset.size()]));
		bindings.put("all-set", allsetValue);
		alldagValue = alldag.toDAG();

		/* TODO XXX: hack: this is the only way I could figure to pass along an outer tuning parameter to an interior data structure. I think we
		   TODO should work on standardizing this, that is, figure out which class holds the arguments for everyone. Verifier's as good a choice as
		   TODO any. [10/09/2007 11:37 dsandler] */
        /* If the key is present, toggle based on the value */
		if (verifier.getArgs().containsKey("dagcache")) {
			if (Boolean.parseBoolean(verifier.getArgs().get("dagcache")))
				alldagValue.enableCache();
			else
				alldagValue.disableCache();
		}

		bindings.put("all-dag", alldagValue);

		ActivationRecord.END.setBindings(bindings);
	}

    /**
     * Registers handlers for this IncrementalAuditoriumLog
     */
	private void registerHandlers() {
		verifier.getPrimitiveFactories().put("signature-verify", SignatureVerify.FACTORY);
	}
}
