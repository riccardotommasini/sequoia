package org.orbit.filters;


import org.orbit.Logger;
import org.orbit.Main;
import org.orbit.encoders.SATEncoderFactory;
import org.orbit.encoders.SATEncoding;
import org.sat4j.core.VecInt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public abstract class AllAnswersAtOnceFilter extends AnswersFilter {

	public AllAnswersAtOnceFilter(HashMap<String, ArrayList<String>> confGraph,
								  HashMap<String, ArrayList<ArrayList<String>>> potAnswersAndCauses, SATEncoderFactory satEncoderFactory,
								  Main.Satisfiability satValueForCorrectAnswers, Logger statLog) {
		super(confGraph, potAnswersAndCauses, satEncoderFactory, satValueForCorrectAnswers, statLog);
	}

	protected SATEncoding encodeForFilteringElements(Set<String> elementsToFilter) {
		encoder.resetEncoderForSetOfAnswers(elementsToFilter);	
		encoder.encode();	
		SATEncoding encoding=encoder.getEncoding();
		//Add soft clauses corresponding to answers ids
		for(String answer:elementsToFilter) {			
			encoding.addSoftClause(new VecInt().push(encoder.getAnswerDimac(answer)));			
		}	
		return encoding;
	}

}
