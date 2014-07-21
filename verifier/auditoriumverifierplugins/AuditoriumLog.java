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
import sexpression.stream.ASEInputStreamReader;
import sexpression.stream.InvalidVerbatimStreamException;
import verifier.ActivationRecord;
import verifier.IVerifierPlugin;
import verifier.PluginException;
import verifier.Verifier;
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
 * Read log data in from the argument "log" and construct all-set and all-dag
 * based on this.
 * 
 * @author kyle
 * 
 */
public class AuditoriumLog implements IVerifierPlugin {

	/**
	 * @see verifier.IVerifierPlugin#init(verifier.Verifier)
	 */
	public void init(Verifier verifier) throws PluginException {
		registerFactories(verifier);
		registerData(verifier);
	}

	/**
	 * @param verifier This method registers the signature-verify primitive on this
	 *                 verifier.
	 */
	private void registerFactories(Verifier verifier) {
		verifier.getPrimitiveFactories().put("signature-verify", SignatureVerify.FACTORY);
	}

	/**
	 * Load log data from a file and register all-set and all-dag to represent
	 * this data in the given verifier.
	 * 
	 * @param verifier Load the log data from the location specified in the "log"
	 *                 argument given to this verifier and register all-dat and
	 *                 all-set to represent the log data in this verifier.
	 */
	private void registerData(Verifier verifier) {
		DagBuilder dag = new /*FastDAGBuilder(); //*/ DagBuilder();
		ArrayList<Expression> set = new ArrayList<>();

		try {
			ASEInputStreamReader in = new ASEInputStreamReader( new FileInputStream(new File(verifier.getArgs().get("log"))));

            ASExpression exp;
            /* Loop until end of file and load into dag to build set TODO EOF stuff */
			while ((exp = in.read()) != null) {
				Message msg = new Message(exp);
//                System.out.println(msg);
                dag.add(msg);
				set.add(new Expression(msg.toASE()));
			}
		} catch (EOFException ignored) {
		} catch (IOException | IncorrectFormatException | InvalidVerbatimStreamException e) {
			throw new PluginException("auditorium", e);
		}

        /* Seals logs to avoid tampering */
		HashMap<String, Value> bindings = new HashMap<>();
		SetValue sv = new SetValue(set.toArray(new Expression[set.size()]));
		DAGValue dv = dag.toDAG();
		sv.seal();
		dv.seal();
		bindings.put("all-set", sv);
		bindings.put("all-dag", dv);
		ActivationRecord.END.setBindings(bindings);
	}
}
