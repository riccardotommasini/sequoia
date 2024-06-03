package org.orbit.encoders.iar;

import org.orbit.encoders.ConsistencyEncoder;
import org.orbit.encoders.ContradictionEncoder;
import org.orbit.encoders.ContradictionEncoder.SingleCauseContradictionClausesBuilder;
import org.orbit.encoders.MaximalityEncoder;
import org.orbit.encoders.MaximalityEncoder.MaximalityClausesBuilder;
import org.orbit.encoders.SATEncoder;
import org.orbit.filters.AllIARAssertionsFilter;
import org.orbit.filters.CauseByCauseFilter;
import org.orbit.filters.IARCauseFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.orbit.filters.AnswersFilter;

public abstract class IAREncoder extends SATEncoder {

	private ConsistencyEncoder consistencyEncoder;
	protected ContradictionEncoder queryContradictionEncoder;
	protected MaximalityEncoder maximalityEncoder;
	private boolean checkSingleCause;
	private boolean checkAssertions;

	public IAREncoder(AnswersFilter answersFilter) {
		super(answersFilter);
		checkSingleCause= answersFilter instanceof CauseByCauseFilter || answersFilter instanceof IARCauseFilter;
		checkAssertions= answersFilter instanceof AllIARAssertionsFilter;
		consistencyEncoder= new ConsistencyEncoder(this, this.filter);		
		maximalityEncoder= new MaximalityEncoder(this, this.filter);	
		queryContradictionEncoder= new ContradictionEncoder(this, this.filter, encodingForSeveralElem);	
	}

	protected void buildClauses() {	
		buildClauses(returnConstructMaxPart(), returnConstructCauseContradictionPart());
	}

	protected void buildClauses(MaximalityClausesBuilder maximalityClausesBuilder, SingleCauseContradictionClausesBuilder causeContradictionClausesBuilder) {
		if(encodingForSeveralElem && !checkAssertions) {
			for(String answer:setOfElementsToCheck) {
				buildIARClausesForAnswer(answer, maximalityClausesBuilder, causeContradictionClausesBuilder);
			}
		}
		else if(checkAssertions) {			
				buildIARClausesForAssertions(setOfElementsToCheck, maximalityClausesBuilder, causeContradictionClausesBuilder);			
		}
		else if(checkSingleCause) {
			buildIARClausesForCause(answerToCheck, causeToCheck, maximalityClausesBuilder, causeContradictionClausesBuilder);
		}
		else {
			buildIARClausesForAnswer(answerToCheck, maximalityClausesBuilder, causeContradictionClausesBuilder);
		}	
	}
	
	private void buildIARClausesForAnswer(String answer, MaximalityClausesBuilder maximalityClausesBuilder, SingleCauseContradictionClausesBuilder contradictionClausesBuilder) {
		for(List<String> cause:filter.getPotentialAnswersAndCauses().get(answer)) {
			assertionToDimac.clear();//independent variables for each causes
			completionAuxVariablesToDimac.clear();
			prioClosureToDimac.clear();
			prioRelationToDimac.clear();
			buildIARClausesForCause(answer, cause, maximalityClausesBuilder, contradictionClausesBuilder);
		}
	}
		
	private void buildIARClausesForCause(String answer, List<String> cause, MaximalityClausesBuilder maximalityClausesBuilder, SingleCauseContradictionClausesBuilder contradictionClausesBuilder) {
		contradictionClausesBuilder.constructSingleCauseContradictionPart(cause, answer);
		Set<String> assertionsToConsider=new HashSet<String>();
		assertionsToConsider.addAll(assertionToDimac.keySet());
		maximalityClausesBuilder.constructMaximalityPart(assertionsToConsider);
		consistencyEncoder.buildConsistencyClauses();
	}
	
	private void buildIARClausesForAssertions(Set<String> assertions, MaximalityClausesBuilder maximalityClausesBuilder,
			SingleCauseContradictionClausesBuilder contradictionClausesBuilder) {
		for(String assertion:assertions) {
			ArrayList<String> unaryCause=new ArrayList<String>();
			unaryCause.add(assertion);
			contradictionClausesBuilder.constructSingleCauseContradictionPart(unaryCause, assertion);
		}		
		if(this instanceof IARCompletionCavsatEncoder || this instanceof IARParetoCavsatEncoder || this instanceof IARScoreStructuredOptiCavsatEncoder) {
			queryContradictionEncoder.encodeRepairSubsetMaximality();
		}
		Set<String> assertionsToConsider=new HashSet<String>();
		assertionsToConsider.addAll(assertionToDimac.keySet());
		maximalityClausesBuilder.constructMaximalityPart(assertionsToConsider);
		consistencyEncoder.buildConsistencyClauses();
	}
	
	abstract SingleCauseContradictionClausesBuilder returnConstructCauseContradictionPart();

	abstract MaximalityClausesBuilder returnConstructMaxPart();
}
