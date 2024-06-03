package org.orbit.filters;


import org.orbit.Logger;
import org.orbit.Main;
import org.orbit.encoders.SATEncoderFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CauseByCauseFilter extends AnswerByAnswerFilter {

	public CauseByCauseFilter(HashMap<String, ArrayList<String>> confGraph, 
			HashMap<String, ArrayList<ArrayList<String>>> potAnswersAndCauses, 
			SATEncoderFactory satEncoderFactory,
			Main.Satisfiability satValueForCorrectAnswers,
			Logger statLog) {
		super(confGraph, potAnswersAndCauses, satEncoderFactory, satValueForCorrectAnswers, statLog);
	}

	@Override
	protected void filterRemainingAnswers() throws IOException {		
		for(String ans : answersToFilter) {		
			for(ArrayList<String> cause:potentialAnswersAndCauses.get(ans)) {
				if(!outputAnswers.contains(ans)) {
					long startEncode = System.currentTimeMillis(); 
					encoder.resetEncoderForCause(cause);
					encoder.encode();
					long timeEncode=System.currentTimeMillis()-startEncode;
					long startSolve = System.currentTimeMillis(); 
					runSolverToCheckAnswer(ans);
					long timeSolving=System.currentTimeMillis()-startSolve;

					statLogger.lognlIfPrintDetailsRequired(
							ans+" cause "+cause+" - encoding time: "+timeEncode+
							" ms - solving time: "+timeSolving+ " ms - nb variables: "+
							encoder.getEncoding().getNbVars()+ " - nb clauses: "+
							encoder.getEncoding().getNbClauses());
				}
			}
		}
	}

}
