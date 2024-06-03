package org.orbit.encoders;

import org.sat4j.core.VecInt;
import org.sat4j.specs.IVecInt;
import org.orbit.filters.AnswersFilter;

public class ConsistencyEncoder extends AuxiliaryEncoder {

	public ConsistencyEncoder(SATEncoder satEncoder, AnswersFilter answersFilter) {
		super(satEncoder, answersFilter);
	}

	public void buildConsistencyClauses() {
		for(String assertion:encoder.assertionToDimac.keySet()) {
			if(filter.getConflictGraph().keySet().contains(assertion)) {
				for(String conflict:filter.getConflictGraph().get(assertion)) {
					if(encoder.assertionToDimac.keySet().contains(conflict)) {
						IVecInt clause=new VecInt();
						clause.push(-1*encoder.assertionToDimac.get(assertion));
						clause.push(-1*encoder.assertionToDimac.get(conflict));
						encoder.encoding.addHardClause(clause);	
					}					
				}
			}

		}
	}
}
