package org.orbit.filters;


import org.orbit.Logger;
import org.orbit.Main;
import org.orbit.Main.Satisfiability;
import org.orbit.encoders.SATEncoderFactory;
import org.orbit.encoders.SATEncoding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class AllIARAssertionsFilter extends MaxSatBasedAllAnswersAtOnceFilter {

	private Set<String> iarAssertions;
	private Set<String> nonIarAssertions;

	public AllIARAssertionsFilter(HashMap<String, ArrayList<String>> confGraph, 
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
			//Encode
			long startEncode = System.currentTimeMillis();
			Set<String> relevantAssertions=new HashSet<String>();
			for(ArrayList<String> cause:potentialAnswersAndCauses.get(ans)) {
				relevantAssertions.addAll(cause);				
			}
			relevantAssertions.removeAll(iarAssertions);
			relevantAssertions.removeAll(nonIarAssertions);
			SATEncoding encoding=encodeForFilteringElements(relevantAssertions);
			int originalNbClauses=encoding.getNbClauses();
			long timeEncode=System.currentTimeMillis()-startEncode;				
			//Solve
			long startSolve = System.currentTimeMillis();
			Integer[] iterations= {0};
			Set<String> newNonIarAssertions=solve(encoding,relevantAssertions, iterations);
			nonIarAssertions.addAll(newNonIarAssertions);
			iarAssertions.addAll(relevantAssertions);
			iarAssertions.removeAll(newNonIarAssertions);		
			long timeSolving=System.currentTimeMillis()-startSolve;
			//Filter IAR causes
			for(ArrayList<String> cause:potentialAnswersAndCauses.get(ans)) {				
				cause.removeAll(iarAssertions);
				if(cause.isEmpty()) {
					outputAnswers.add(ans);
					break;
				}													
			}			
			statLogger.lognlIfPrintDetailsRequired(
					ans+" encoding time: "+timeEncode+" ms"+
							" - solving+filtering time: "+timeSolving+ " ms"
							+ " - nb variables: "+encoding.getNbVars()
							+ " - nb clauses: "+originalNbClauses+
							" - number of iterations: "+iterations[0]
					);
		}
	}



}
