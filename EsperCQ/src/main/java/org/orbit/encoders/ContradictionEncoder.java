package org.orbit.encoders;

import org.orbit.filters.AllIARAssertionsFilter;
import org.sat4j.core.VecInt;
import org.sat4j.specs.IVecInt;
import org.orbit.filters.AnswersFilter;
import org.orbit.filters.AnswersFilter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContradictionEncoder extends AuxiliaryEncoder {

	private boolean maxSat;

	public interface ContradictionClausesBuilder {		
		void constructContradictionPart();		
	}

	public interface SingleCauseContradictionClausesBuilder {		
		void constructSingleCauseContradictionPart(List<String> causeToCheck, String answer);	
	}

	public ContradictionEncoder(SATEncoder satEncoder, AnswersFilter answersFilter, boolean forMaxSat) {
		super(satEncoder, answersFilter);
		maxSat=forMaxSat;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////
	//CQAPri style: contradict query by selecting a conflicting assertion for each cause	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	public void buildClausesContradictingCausesWithConflicts() {
		if(!maxSat) {
			buildClausesContradictingCausesWithConflictsForAnswer(encoder.answerToCheck);
		}else {
			for(String answer:encoder.setOfElementsToCheck) {
				buildClausesContradictingCausesWithConflictsForAnswer(answer);
			}
		}
	}

	private void buildClausesContradictingCausesWithConflictsForAnswer(String ans) {
		for(List<String> cause:filter.getPotentialAnswersAndCauses().get(ans))  {
			contradictCauseWithConflicts(cause, ans);
		}
	}

	public void contradictCauseWithConflicts(List<String> cause, String answer) {
		Set<String> conflicts=new HashSet<String>();
		for(String causeAssertion:cause) {
			conflicts.addAll(filter.getConflictGraph().get(causeAssertion));
		}		
		IVecInt clause=new VecInt();		
		if(maxSat) {
			clause.push(-1*encoder.answerToDimac.get(answer));
		}
		for(String conflictingAssertion:conflicts){
			encoder.checkIfHasDimacAndAddIfNot(conflictingAssertion);			
			clause.push(encoder.assertionToDimac.get(conflictingAssertion));							
		}
		encoder.encoding.addHardClause(clause);	
	}


	/////////////////////////////////////////////////////////////////////////////////////////////////
	//CAvSAT style: contradict query by enforcing not selecting at least one assertion of each cause
	/////////////////////////////////////////////////////////////////////////////////////////////////
	public void buildClausesContradictingCausesBySelectingMissingAssertion() {
		if(!maxSat) {
			buildClausesContradictingCausesBySelectingMissingAssertionForAnswer(encoder.answerToCheck);
		}else {
			for(String answer:encoder.setOfElementsToCheck) {
				buildClausesContradictingCausesBySelectingMissingAssertionForAnswer(answer);
			}
		}
		encodeRepairSubsetMaximality();
	}


	private void buildClausesContradictingCausesBySelectingMissingAssertionForAnswer(String ans) {
		for(List<String> cause:filter.getPotentialAnswersAndCauses().get(ans)) {
			contradictCauseBySelectingMissingAssertion(cause, ans);
		}
	}

	private void contradictCauseBySelectingMissingAssertion(List<String> cause, String answer) {
		IVecInt clause=new VecInt();
		if(maxSat) {
			clause.push(-1*encoder.answerToDimac.get(answer));
		}
		for(String assertion:cause) {
			encoder.checkIfHasDimacAndAddIfNot(assertion);
			clause.push(-1*encoder.assertionToDimac.get(assertion));
		}
		encoder.encoding.addHardClause(clause);
	}

	public void encodeRepairSubsetMaximality() {
		Set<String> assertionsToConsider=new HashSet<String>();
		assertionsToConsider.addAll(encoder.assertionToDimac.keySet());
		for(String assertion:assertionsToConsider) {
			IVecInt clause=new VecInt();
			clause.push(encoder.assertionToDimac.get(assertion));
			for(String conflict:filter.getConflictGraph().get(assertion)) {
				encoder.checkIfHasDimacAndAddIfNot(conflict);
				clause.push(encoder.assertionToDimac.get(conflict));
			}
			encoder.encoding.addHardClause(clause);
		}
	}

	//IAR case: variables may be independent for each cause
	public void buildClausesContradictingIARCauseBySelectingMissingAssertion(List<String> cause, String answer) {
		if(filter instanceof AllIARAssertionsFilter) {//factorize clauses that encode subset maximality (done in IAR encoder)
			contradictCauseBySelectingMissingAssertion(cause, answer);
		}else {
			contradictCauseBySelectingMissingAssertion(cause, answer);
			encodeRepairSubsetMaximality();
		}
	}

}
