package org.orbit.encoders;

import org.sat4j.core.VecInt;
import org.sat4j.specs.IVecInt;
import org.orbit.filters.AnswersFilter;

import java.util.List;

public class SomeCauseEncoder extends AuxiliaryEncoder {

	private boolean maxSat;
	private boolean causeByCause;

	public SomeCauseEncoder(SATEncoder satEncoder, AnswersFilter answersFilter, boolean forMaxSat, boolean causeByCause) {
		super(satEncoder, answersFilter);
		maxSat=forMaxSat;
		this.causeByCause=causeByCause;
	}

	public void buildSomeCauseClauses() {
		if(maxSat) {
			for(String answer:encoder.setOfElementsToCheck) {
				buildSomeCauseClausesForAnswer(answer);
			}
		}
		else if (causeByCause) {
			buildClausesForPresenceOfCause(encoder.causeToCheck);
		}
		else {
			buildSomeCauseClausesForAnswer(encoder.answerToCheck);
		}
	}

	private void buildSomeCauseClausesForAnswer(String answer) {
		IVecInt clauseSomeCause=new VecInt();
		if(maxSat) {
			clauseSomeCause.push(-1*encoder.answerToDimac.get(answer));
		}
		for(List<String> cause: filter.getPotentialAnswersAndCauses().get(answer))  {	
			int dimacCause=encoder.dimacVar;
			encoder.dimacVar++;
			clauseSomeCause.push(dimacCause);			
			for(String assertion:cause) {
				IVecInt noCauseOrAssertionClause=new VecInt();
				noCauseOrAssertionClause.push(-1*dimacCause);
				encoder.checkIfHasDimacAndAddIfNot(assertion);
				noCauseOrAssertionClause.push(encoder.assertionToDimac.get(assertion));
				encoder.encoding.addHardClause(noCauseOrAssertionClause);
			}						
		}
		encoder.encoding.addHardClause(clauseSomeCause);		
	}

	private void buildClausesForPresenceOfCause(List<String> causeToCheck) {
		for(String assertion:causeToCheck) {
			IVecInt assertionPresence=new VecInt();			
			encoder.checkIfHasDimacAndAddIfNot(assertion);
			assertionPresence.push(encoder.assertionToDimac.get(assertion));
			encoder.encoding.addHardClause(assertionPresence);
		}
	}
}
