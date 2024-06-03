package org.orbit.filters;


import org.orbit.Logger;
import org.orbit.Main;
import org.orbit.encoders.SATEncoderFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.orbit.Main.*;

public class IARCauseFilter extends AnswerByAnswerFilter {

	private Set<String> iarAssertions;
	private Set<String> nonIarAssertions;

	public IARCauseFilter(HashMap<String, ArrayList<String>> confGraph, 
			HashMap<String, ArrayList<ArrayList<String>>> potAnswersAndCauses, 
			SATEncoderFactory satEncoderFactory,
			Satisfiability satValueForCorrectAnswers,
			Logger statLog) {
		super(confGraph, potAnswersAndCauses, satEncoderFactory, satValueForCorrectAnswers, statLog);
		iarAssertions=new HashSet<String>();
		nonIarAssertions=new HashSet<String>();
	}

	@Override
	protected void filterRemainingAnswers() throws IOException {		
		for(String ans : answersToFilter) {				
			for(ArrayList<String> cause:potentialAnswersAndCauses.get(ans)) {				
				long startSolve = System.currentTimeMillis(); 
				int initSize=cause.size();
				cause.removeAll(nonIarAssertions);
				if(cause.size()==initSize) {//cause does not contain already known non-IAR assertion
					cause.removeAll(iarAssertions);
					if(cause.isEmpty()) {
						outputAnswers.add(ans);
					}else {
						boolean causeFoundNotIAR=false;
						for(String assertion:cause) {
							if(!causeFoundNotIAR) {
								ArrayList<String> unaryCause=new ArrayList<String>();
								unaryCause.add(assertion);
								encoder.resetEncoderForCause(unaryCause);
								encoder.encode();
								if(isUnsat(encoder.getEncoding())){
									iarAssertions.add(assertion);
								}else {
									nonIarAssertions.add(assertion);
									causeFoundNotIAR=true;
								}
							}	
						}
						if(!causeFoundNotIAR) {
							outputAnswers.add(ans);
						}	
					}
				}	
				long timeSolving=System.currentTimeMillis()-startSolve;

				statLogger.lognlIfPrintDetailsRequired(
						ans+" cause "+cause+" - checking time: "+timeSolving+ " ms");
				if(outputAnswers.contains(ans)) {
					break;
				}
			}
		}
	}



}
