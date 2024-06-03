package org.orbit.filters;

import org.orbit.Logger;
import org.orbit.Main;
import org.orbit.encoders.SATEncoderFactory;
import org.orbit.encoders.SATEncoding;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.AllMUSes;
import org.sat4j.tools.FullClauseSelectorSolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
public class MUSesBasedAllAnswersAtOnceFilter extends AllAnswersAtOnceFilter{

	private ISolver satSolver;
	private AllMUSes allMUSes;

	public MUSesBasedAllAnswersAtOnceFilter(HashMap<String, ArrayList<String>> confGraph, 
			HashMap<String, ArrayList<ArrayList<String>>> potAnswersAndCauses, 
			SATEncoderFactory satEncoderFactory,
			Main.Satisfiability satValueForCorrectAnswers,
			Logger statLog) {
		super(confGraph, potAnswersAndCauses, satEncoderFactory, satValueForCorrectAnswers, statLog);
		allMUSes=new AllMUSes(SolverFactory.instance());			
		satSolver=allMUSes.getSolverInstance();
	}

	@Override
	protected void filterRemainingAnswers() throws IOException {
		//Encode
		long startEncode = System.currentTimeMillis(); 
		SATEncoding encoding=encodeForFilteringElements(answersToFilter);
		int originalNbClauses=encoding.getNbClauses();
		long timeEncode=System.currentTimeMillis()-startEncode;				
		//Solve
		long startSolve = System.currentTimeMillis();
		int nbMUSes=runSolverToFindMUSes(encoding);				
		long timeSolving=System.currentTimeMillis()-startSolve;
		statLogger.lognlIfPrintDetailsRequired(
				"encoding time: "+timeEncode+" ms"+
						" - solving time: "+timeSolving+ " ms"
						+ " - nb variables: "+encoding.getNbVars()
						+ " - nb clauses: "+originalNbClauses+
						" - number of MUSes: "+nbMUSes
				);
	}

	private int runSolverToFindMUSes(SATEncoding encoding) throws IOException {		
		satSolver.reset();
		satSolver.newVar(encoding.getNbVars());		
		satSolver.setExpectedNumberOfClauses(encoding.getNbClauses());
		List<IVecInt> muses=new ArrayList<IVecInt>();
		try {
			for(int i=0;i<encoding.getHardClauses().size();i++){
				((FullClauseSelectorSolver<?>) satSolver).addNonControlableClause(encoding.getHardClauses().get(i));
			}
			for(int i=0;i<encoding.getSoftClauses().size();i++){
				((FullClauseSelectorSolver<?>) satSolver).addClause(encoding.getSoftClauses().get(i));
			}
			if(!satSolver.isSatisfiable()){								
				muses = allMUSes.computeAllMUSes();
			}
		} catch (ContradictionException e1) {
			muses = allMUSes.computeAllMUSes();
		} catch (TimeoutException e) {
			System.out.print("time out");
			e.printStackTrace();
		}	
		switch(satValueForCorrect) {	
		case UNSAT:
			for(IVecInt mus:muses) {
				if(mus.size()==1) {
					outputAnswers.add(encoder.getAnswerDimac(mus.get(0)));
				}
			}
			break;
		case SAT:
			outputAnswers.addAll(answersToFilter);
			for(IVecInt mus:muses) {
				if(mus.size()==1) {
					outputAnswers.remove(encoder.getAnswerDimac(mus.get(0)));
				}
			}
			break;
		}
		return muses.size();
	}
}
