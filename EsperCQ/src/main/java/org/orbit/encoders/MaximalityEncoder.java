package org.orbit.encoders;

import org.sat4j.core.VecInt;
import org.sat4j.specs.IVecInt;

import java.util.*;
import org.orbit.filters.AnswersFilter;

public class MaximalityEncoder extends AuxiliaryEncoder {

	public interface PartialMaximalityClausesBuilder {	
		Set<String> constructMaxClausesAndReturnNewAssertions(Set<String> firstAssertionsToConsider);	
	}

	public interface MaximalityClausesBuilder {		
		void constructMaximalityPart(Set<String> firstAssertionsToConsider);	
	}

	public MaximalityEncoder(SATEncoder satEncoder, AnswersFilter answersFilter) {
		super(satEncoder, answersFilter);
	}

	//Construct iteratively the maximality clauses for given assertions and all new assertions introduced during the process
	public Set<String> iterativelyConstructMaximalityPart(Set<String> firstAssertionsToConsider, PartialMaximalityClausesBuilder partialMaxClausesBuilder){				
		Set<String> nextAssertionsToConsider=partialMaxClausesBuilder.constructMaxClausesAndReturnNewAssertions(firstAssertionsToConsider);
		if(!nextAssertionsToConsider.isEmpty()){//some new assertions have been added to the formula, continue building clauses for them
			return iterativelyConstructMaximalityPart(nextAssertionsToConsider, partialMaxClausesBuilder);
		}
		return new HashSet<String>();//no new assertions, all clauses are constructed for this part
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////
	//Score structured maximality
	/////////////////////////////////////////////////////////////////////////////////////////////////
	public void constructScoreStructuredOptiMaximalityPart(Set<String> assertionsToConsider){				
		iterativelyConstructMaximalityPart(assertionsToConsider, returnScoreStructuredOptiMaximalityCriteria());
	}

	PartialMaximalityClausesBuilder returnScoreStructuredOptiMaximalityCriteria() {
		return this::constructScoreStructuredOptiMaximalityPartForAssertionsAndReturnNewAssertions;
	}

	public Set<String> constructScoreStructuredOptiMaximalityPartForAssertionsAndReturnNewAssertions(Set<String> assertionsToConsider){								
		Set<String> nextAssertionsToConsider=new HashSet<String>();//assertions that will be added when constructing clauses	
		for(String x:assertionsToConsider){
			IVecInt clause=new VecInt();
			clause.push(encoder.assertionToDimac.get(x));//a repair should contain x or some y in conflict and of same or greater prio
			ArrayList<String> conflicts=filter.getConflictGraph().get(x);			
			if(conflicts!=null){
				for(String y:conflicts){		
					if(encoder.hasNotDimac(y)) {
						nextAssertionsToConsider.add(y);
						encoder.addDimac(y);
					}										
					clause.push(encoder.assertionToDimac.get(y));					
				}
			}
			encoder.encoding.addHardClause(clause);			
		}
		return nextAssertionsToConsider;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////
	//Pareto maximality
	/////////////////////////////////////////////////////////////////////////////////////////////////
	public void constructParetoMaximalityPart(Set<String> assertionsToConsider) {
		iterativelyConstructMaximalityPart(assertionsToConsider, returnParetoMaximalityCriteria());
	}

	PartialMaximalityClausesBuilder returnParetoMaximalityCriteria() {
		return this::constructParetoMaximalityPartForAssertionsAndReturnNewAssertions;
	}

	protected Set<String> constructParetoMaximalityPartForAssertionsAndReturnNewAssertions(Set<String> assertionsToConsider){								
		Set<String> nextAssertionsToConsider=new HashSet<String>();//assertions that will be added when constructing clauses	
		for(String x:assertionsToConsider){
			if(filter.getConflictGraph().get(x)!=null) {
				for(String conf: filter.getConflictGraph().get(x)) {
					IVecInt clause=new VecInt();
					clause.push(-1*encoder.assertionToDimac.get(x));//a repair should not contain x except if for each greater-equal conflict z it contains y in greater-equal conflict with z
					ArrayList<String> conflictsOfconf=filter.getConflictGraph().get(conf);			
					if(conflictsOfconf!=null){
						for(String y:conflictsOfconf){		
							if(encoder.hasNotDimac(y)) {
								nextAssertionsToConsider.add(y);
								encoder.addDimac(y);
							}										
							clause.push(encoder.assertionToDimac.get(y));					
						}
					}
					encoder.encoding.addHardClause(clause);	
				}				
			}					
		}
		return nextAssertionsToConsider;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////
	//Completion maximality
	/////////////////////////////////////////////////////////////////////////////////////////////////
	public void constructCompletionMaximalityPart(Set<String> assertionsToConsider) {
		iterativelyConstructMaximalityPart(assertionsToConsider, returnCompletionMaximalityCriteria());
		constructCompletionConditions();
	}	

	PartialMaximalityClausesBuilder returnCompletionMaximalityCriteria() {
		return this::constructCompletionMaximalityPartForAssertionsAndReturnNewAssertions;
	}

	protected Set<String> constructCompletionMaximalityPartForAssertionsAndReturnNewAssertions(Set<String> assertionsToConsider){								
		Set<String> nextAssertionsToConsider=new HashSet<String>();//assertions that will be added when constructing clauses		
		for(String x:assertionsToConsider){
			IVecInt clauseBase=new VecInt();
			clauseBase.push(encoder.assertionToDimac.get(x));//a repair should contain x or some y in conflict and of same or greater prio in the completion
			ArrayList<String> conflicts=filter.getConflictGraph().get(x);			
			if(conflicts!=null){
				for(String y:conflicts){		
					if(encoder.hasNotDimac(y)) {
						nextAssertionsToConsider.add(y);
						encoder.addDimac(y);
					}
					IVecInt clausePresence=new VecInt();//aux var implies y
					IVecInt clauseCompletion=new VecInt();//aux var implies y>'x

					Map.Entry<String,String> yx = new AbstractMap.SimpleEntry<>(y,x);
					Map.Entry<String,String> xy = new AbstractMap.SimpleEntry<>(x,y);

					encoder.checkIfAuxVariablesHasDimacAndAddIfNot(yx);
					encoder.checkIfprioRelationHasDimacAndAddIfNot(yx);
					encoder.checkIfprioRelationHasDimacAndAddIfNot(xy);//will be used in constructCompletionConditions()					

					clauseBase.push(encoder.completionAuxVariablesToDimac.get(yx));	

					clausePresence.push(-1*encoder.completionAuxVariablesToDimac.get(yx));
					clausePresence.push(encoder.assertionToDimac.get(y));

					clauseCompletion.push(-1*encoder.completionAuxVariablesToDimac.get(yx));
					clauseCompletion.push(encoder.prioRelationToDimac.get(yx));

					encoder.encoding.addHardClause(clausePresence);	
					encoder.encoding.addHardClause(clauseCompletion);
				}
			}
			encoder.encoding.addHardClause(clauseBase);		
		}
		return nextAssertionsToConsider;
	}

	private void constructCompletionConditions() {
		for(String x:encoder.assertionToDimac.keySet()){//all assertions occurring in the encoding have been added when building the "greedy" clauses with constructCompletionMaximalityPartForAssertionsAndReturnNewAssertions
			ArrayList<String> conflicts=filter.getConflictGraph().get(x);			
			if(conflicts!=null){
				for(String y:conflicts){
					Map.Entry<String,String> xy = new AbstractMap.SimpleEntry<>(x,y);
					Map.Entry<String,String> yx = new AbstractMap.SimpleEntry<>(y,x);

					IVecInt clauseAtLeastOne=new VecInt();					
					clauseAtLeastOne.push(encoder.prioRelationToDimac.get(xy));
					clauseAtLeastOne.push(encoder.prioRelationToDimac.get(yx));
					encoder.encoding.addHardClause(clauseAtLeastOne);	

					IVecInt clauseNotBoth=new VecInt();
					clauseNotBoth.push(-1*encoder.prioRelationToDimac.get(xy));
					clauseNotBoth.push(-1*encoder.prioRelationToDimac.get(yx));
					encoder.encoding.addHardClause(clauseNotBoth);	

					if(filter.getConflictGraph().get(y)==null || !filter.getConflictGraph().get(y).contains(x)) {//y > x
						IVecInt clauseExtension=new VecInt();
						clauseExtension.push(encoder.prioRelationToDimac.get(yx));
						encoder.encoding.addHardClause(clauseExtension);
					}					
				}
			}
		}
		constructTransitiveClosureConditions();
	}


	private void constructTransitiveClosureConditions() {
		for(String alpha:encoder.assertionToDimac.keySet()){
			for(String beta:encoder.assertionToDimac.keySet()){	
				if(alpha!=beta) {
					Map.Entry<String,String> alphabeta = new AbstractMap.SimpleEntry<>(alpha,beta);
					Map.Entry<String,String> betaalpha = new AbstractMap.SimpleEntry<>(beta,alpha);

					encoder.checkIfprioRelationHasDimacAndAddIfNot(alphabeta);
					encoder.checkIfprioClosureHasDimacAndAddIfNot(alphabeta);
					encoder.checkIfprioClosureHasDimacAndAddIfNot(betaalpha);

					IVecInt clauseInitClosure=new VecInt();
					clauseInitClosure.push(-1*encoder.prioRelationToDimac.get(alphabeta));
					clauseInitClosure.push(encoder.prioClosureToDimac.get(alphabeta));
					encoder.encoding.addHardClause(clauseInitClosure);	

					IVecInt clauseNoCycle=new VecInt();
					clauseNoCycle.push(-1*encoder.prioRelationToDimac.get(alphabeta));
					clauseNoCycle.push(-1*encoder.prioClosureToDimac.get(betaalpha));
					encoder.encoding.addHardClause(clauseNoCycle);	
				}
			}
		}
		for(Map.Entry<String,String> alphabeta:encoder.prioClosureToDimac.keySet()) {
			for(Map.Entry<String,String> betagamma:encoder.prioRelationToDimac.keySet()) {//beta and gamma are in conflict
				if(alphabeta.getValue().equals(betagamma.getKey()) && !alphabeta.getKey().equals(betagamma.getValue())) {
					Map.Entry<String,String> alphagamma=new AbstractMap.SimpleEntry<>(alphabeta.getKey(),betagamma.getValue());
					IVecInt clause=new VecInt();
					clause.push(-1*encoder.prioClosureToDimac.get(alphabeta));
					clause.push(-1*encoder.prioRelationToDimac.get(betagamma));
					clause.push(encoder.prioClosureToDimac.get(alphagamma));
					encoder.encoding.addHardClause(clause);
				}				
			}
		}
	}

	/*private void constructAcyclicityConstraintPartByComputingCycles() {//note that this will produce clauses corresponding to "cycle" between each pair conflicting assertions
		Graph<String, DefaultEdge> g = new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		for(String assertion:encoder.assertionToDimac.keySet()) {	     
			g.addVertex(assertion);
		}
		for(Map.Entry<String,String> alphabeta:encoder.prioRelationToDimac.keySet()) {
			g.addEdge(alphabeta.getKey(), alphabeta.getValue());
		}
		TiernanSimpleCycles<String, DefaultEdge> cyclesFinder = new  TiernanSimpleCycles<String, DefaultEdge>(g);
		for(List<String> cycle:cyclesFinder.findSimpleCycles()) {	    	 			
			IVecInt clause=new VecInt();	 		
			for(int i=0;i<cycle.size()-1;i++) {
				Map.Entry<String,String> alphabeta = new AbstractMap.SimpleEntry<>(cycle.get(i),cycle.get(i+1));
				clause.push(-1*encoder.prioRelationToDimac.get(alphabeta));
			}
			Map.Entry<String,String> alphabeta = new AbstractMap.SimpleEntry<>(cycle.get(cycle.size()-1),cycle.get(0));
			clause.push(-1*encoder.prioRelationToDimac.get(alphabeta));
			encoder.encoding.addHardClause(clause);			
		}
	}*/
}
