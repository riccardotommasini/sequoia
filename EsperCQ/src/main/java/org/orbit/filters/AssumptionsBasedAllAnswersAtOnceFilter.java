package org.orbit.filters;

import org.orbit.Logger;
import org.orbit.Main;
import org.orbit.encoders.SATEncoderFactory;
import org.orbit.encoders.SATEncoding;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.orbit.Main.*;


public class AssumptionsBasedAllAnswersAtOnceFilter extends AllAnswersAtOnceFilter{

	private ISolver satSolver;

	public AssumptionsBasedAllAnswersAtOnceFilter(HashMap<String, ArrayList<String>> confGraph, 
			HashMap<String, ArrayList<ArrayList<String>>> potAnswersAndCauses, 
			SATEncoderFactory satEncoderFactory,
			Satisfiability satValueForCorrectAnswers,
			Logger statLog) {
		super(confGraph, potAnswersAndCauses, satEncoderFactory, satValueForCorrectAnswers, statLog);
		satSolver=SolverFactory.newLight();
	}

	@Override
	protected void filterRemainingAnswers() throws IOException {
		//Encode
		long startEncode = System.currentTimeMillis(); 
		SATEncoding encoding=encodeForFilteringElements(answersToFilter);
		int originalNbClauses=encoding.getHardClauses().size();
		long timeEncode=System.currentTimeMillis()-startEncode;				
		//Solve
		long startSolve = System.currentTimeMillis();
		runSolverWithEachAssumption(encoding);				
		long timeSolving=System.currentTimeMillis()-startSolve;
		statLogger.lognlIfPrintDetailsRequired(
				"encoding time: "+timeEncode+" ms"+
						" - solving time: "+timeSolving+ " ms"
						+ " - nb variables: "+encoding.getNbVars()
						+ " - nb clauses: "+originalNbClauses
				);
	}

	private void runSolverWithEachAssumption(SATEncoding encoding) throws IOException {		
		satSolver.reset();
		satSolver.newVar(encoding.getNbVars());		
		satSolver.setExpectedNumberOfClauses(encoding.getHardClauses().size());
		try {
			satSolver.addAllClauses(encoding.getHardClauses());
			switch(satValueForCorrect) {	
			case UNSAT:
				for(int i=0;i<encoding.getSoftClauses().size();i++) {
					if(!satSolver.isSatisfiable(encoding.getSoftClauses().get(i))){
						outputAnswers.add(encoder.getAnswerDimac(encoding.getSoftClauses().get(i).get(0)));
					}
				}
				break;
			case SAT:
				for(int i=0;i<encoding.getSoftClauses().size();i++) {
					if(satSolver.isSatisfiable(encoding.getSoftClauses().get(i))){
						outputAnswers.add(encoder.getAnswerDimac(encoding.getSoftClauses().get(i).get(0)));
					}
				}
				break;
			}
		}catch (ContradictionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
	}

}

