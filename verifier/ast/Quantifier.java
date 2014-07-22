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

package verifier.ast;

import java.util.ArrayList;
import java.util.HashMap;

import sexpression.*;
import verifier.*;
import verifier.value.*;

public abstract class Quantifier extends AST {

	public abstract void forEvalTrue(Box<Boolean> box);

	public abstract void forEvalFalse(Box<Boolean> box);

	public abstract Value result(Box<Boolean> box, boolean sealed,
			ArrayList<Binding<AST, ActivationRecord>> newUnknowns,
			Box<Integer> newIndex);

	public abstract String getPName();

	protected final String name;
	protected final AST set;
	protected final AST body;
	protected final int index;
	protected final ArrayList<Binding<AST, ActivationRecord>> unknowns;

	private ASExpression ase;

	protected Quantifier(String name, AST set, AST body, int index,
			ArrayList<Binding<AST, ActivationRecord>> unknowns) {
		super(null);
		this.name = name;
		this.set = set;
		this.body = body;
		this.index = index;
		this.unknowns = unknowns;
	}

	/**
	 * @see verifier.ast.AST#toASE()
	 */
	@Override
	public ASExpression toASE() {
		if (ase == null) {
			ASExpression[] values = new ASExpression[unknowns.size()];
			for (int lcv = 0; lcv < values.length; lcv++) {
				AST ast = unknowns.get(lcv).var;
				ActivationRecord rec = unknowns.get(lcv).val;
				values[lcv] = new ListExpression(ast.toASE(), rec.toASE());
			}
			ase = new ListExpression(StringExpression.make(getPName()),
			StringExpression.make(name), set.toASE(), body.toASE(),
					StringExpression.make(Integer.toString(index)),
					new ListExpression(values));
		}
		return ase;
	}

	/**
	 * @see verifier.ast.AST#eval(verifier.ActivationRecord)
	 */
	@Override
	public Value eval(final ActivationRecord environment) {
		final Value set = this.set.eval(environment);

		final ArrayList<Binding<AST, ActivationRecord>> newUnknowns = new ArrayList<>();
		final Box<Boolean> box = new Box<>(false);
		final Box<Integer> newIndex = new Box<>(0);

		set.execute(new AValueVisitor() {

			@Override
			public Value forSet(SetValue setvalue) {
				// construct a list for all computation
				ArrayList<Binding<AST, ActivationRecord>> total = new ArrayList<>();
				for (int lcv = index; lcv < setvalue.size(); lcv++) {
					HashMap<String, Value> extension = new HashMap<>();
					extension.put(name, setvalue.get(lcv));
					total.add(new Binding<>(body,
							environment.extend(extension)));
				}
				total.addAll(unknowns);
				newIndex.set(setvalue.size());

				// Evaluate all unknowns
				Value[] values = new Value[total.size()];
				for (int lcv = 0; lcv < total.size(); lcv++) {
					Binding<AST, ActivationRecord> binding = total.get(lcv);
					values[lcv] = binding.var.eval(binding.val);
				}

				// Make the determination
				for (int lcv = 0; lcv < values.length; lcv++) {
					final int flcv = lcv;
					values[lcv].execute(new AValueVisitor() {

						@Override
						public Value forFalse(False f) {
							forEvalFalse(box);
							return null;
						}

						@Override
						public Value forTrue(True t) {
							forEvalTrue(box);
							return null;
						}

						@Override
						public Value forReduction(Reduction r) {
                            newUnknowns.add(unknowns.get(flcv));
							return null;
						}

					});
				}
				return null;
			}
		});

		return result(box, set.isSealed(), newUnknowns, newIndex);
	}
}
