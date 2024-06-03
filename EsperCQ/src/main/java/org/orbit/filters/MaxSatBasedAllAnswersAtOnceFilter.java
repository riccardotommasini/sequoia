package org.orbit.filters;

import org.orbit.Command;
import org.orbit.Logger;
import org.orbit.Main;
import org.orbit.Parameters;
import org.orbit.encoders.SATEncoderFactory;
import org.orbit.encoders.SATEncoding;
import org.sat4j.core.VecInt;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.maxsat.WeightedMaxSatDecorator;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.OptToSatAdapter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MaxSatBasedAllAnswersAtOnceFilter extends AllAnswersAtOnceFilter{

	private IProblem problem;

	public MaxSatBasedAllAnswersAtOnceFilter(HashMap<String, ArrayList<String>> confGraph,
											 HashMap<String, ArrayList<ArrayList<String>>> potAnswersAndCauses, SATEncoderFactory satEncoderFactory,
											 Main.Satisfiability satValueForCorrectAnswers, Logger statLog) {
		super(confGraph, potAnswersAndCauses, satEncoderFactory, satValueForCorrectAnswers, statLog);
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
		Integer[] iterations= {0};
		Set<String> answersSetToTrue=solve(encoding, answersToFilter, iterations);
		switch(satValueForCorrect) {	
		case UNSAT:
			outputAnswers.addAll(answersToFilter);
			outputAnswers.removeAll(answersSetToTrue);	
			break;
		case SAT:
			outputAnswers.addAll(answersSetToTrue);	//need to keep the IAR answers already in outputAnswers
			break;
		}			
		long timeSolving=System.currentTimeMillis()-startSolve;
		statLogger.lognlIfPrintDetailsRequired(
				"encoding time: "+timeEncode+" ms"+
						" - solving time: "+timeSolving+ " ms"
						+ " - nb variables: "+encoding.getNbVars()
						+ " - nb clauses: "+originalNbClauses+
						" - number of iterations: "+iterations[0]
				);
	}

	protected Set<String> solve(SATEncoding encoding, Set<String> elementsTofilter, Integer[] iterations) throws IOException{
		Set<String> answersSetToTrue=new HashSet<String>();		
		//iterate to filter all non answers		
		boolean moreNonAnswers=true;					
		while(moreNonAnswers && answersSetToTrue.size()!=elementsTofilter.size()){
			moreNonAnswers=false;
			iterations[0]++;
			//compute optimal model
			if(Parameters.solver.equals(Parameters.Solvers.sat4j)) {
				solveWithSat4j(encoding);			
			}
			else{
				solveWithStandalone(encoding);
			}
			//find answers to filter from solver output file and update encoding		
			for(String answer:elementsTofilter) {
				if(isAssignedToTrueInModel(encoder.getAnswerDimac(answer))){
					moreNonAnswers=true;
					answersSetToTrue.add(answer);			
					encoding.addHardClause(new VecInt().push(-1*encoder.getAnswerDimac(answer)));
				}
			}	
			if(Parameters.semantics.equals(Parameters.Semantics.IAR) && ! Parameters.algo.equals(Parameters.Algorithm.iar_assertions_maxsat)) {
				moreNonAnswers=false;//IAR encodings use independent variables: no need for more than one iteration
			}
		}
		return answersSetToTrue;
	}

	private void solveWithStandalone(SATEncoding encoding) throws IOException {		 							
		//create solver input file
		Logger solverInput=new Logger(Parameters.solverInput);	
		encoding.logTo(solverInput);
		solverInput.closeLogger();
		//solve partial max sat
		Logger solverOutput=new Logger(Parameters.solverOutput);			
		switch(Parameters.solver) {	
		case sat4j_standalone:
			Command.executeCommand("java -jar sat4j-maxsat.jar " + Parameters.solverInput, solverOutput);
			break;
		default:
			Command.executeCommand("./"+Parameters.solver+" "+Parameters.solverInput, solverOutput);
			break;
		}		
		solverOutput.closeLogger();
	}

	private void solveWithSat4j(SATEncoding encoding) throws IOException {	
		//create maxsat solver and input problem
		WeightedMaxSatDecorator maxSat=new WeightedMaxSatDecorator(SolverFactory.newDefault());
		maxSat.newVar(encoding.getNbVars());
		maxSat.setExpectedNumberOfClauses(encoding.getNbClauses());
		try {
			for(int i=0;i<encoding.getHardClauses().size();i++) {			
				maxSat.addHardClause(encoding.getHardClauses().get(i));
			}
			for(int i=0;i<encoding.getSoftClauses().size();i++) {
				maxSat.addSoftClause(1, encoding.getSoftClauses().get(i));//weight 1
			}	
		}
		catch (ContradictionException e) {
			e.printStackTrace();
		} 
		problem = new OptToSatAdapter(new PseudoOptDecorator(maxSat));
		//solve partial max sat
		try {	
			problem.isSatisfiable();			
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
	}

	private boolean isAssignedToTrueInModel(Integer answerDimac) throws IOException {
		switch(Parameters.solver) {	
		case sat4j:
			return problem.model(answerDimac);
		default:
			return decodeModelFromMaxSatSolverOutputFile().contains(answerDimac.toString());
		}	
	}

	private List<String> decodeModelFromMaxSatSolverOutputFile() throws IOException{
		FileReader input = new FileReader(Parameters.solverOutput);
		BufferedReader bufRead = new BufferedReader(input);
		String myLine=bufRead.readLine();
		while ( !myLine.startsWith("v")){    
			myLine = bufRead.readLine();
		}
		bufRead.close();
		return Arrays.stream(myLine.split(" ")).map( s -> s.trim()).collect(Collectors.toCollection(ArrayList::new));
	}
}
