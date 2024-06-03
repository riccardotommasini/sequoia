package org.orbit.encoders.ar;

import org.orbit.encoders.ContradictionEncoder.ContradictionClausesBuilder;
import org.orbit.encoders.MaximalityEncoder;
import org.orbit.encoders.MaximalityEncoder.MaximalityClausesBuilder;
import org.orbit.filters.AnswersFilter;

import java.util.HashSet;
import java.util.Set;

public abstract class ARPrioEncoder extends AREncoder {
		
	protected MaximalityEncoder maximalityEncoder;

	public ARPrioEncoder(AnswersFilter answersFilter) {
		super(answersFilter);		
		maximalityEncoder= new MaximalityEncoder(this, this.filter);		
	}

	protected void buildClauses() {	
		buildClauses(returnConstructMaxPart(), returnConstructContradictionPart());
	}

	private void buildClauses(MaximalityClausesBuilder maximalityClausesBuilder, ContradictionClausesBuilder contradictionClausesBuilder) {	
		contradictionClausesBuilder.constructContradictionPart();
		Set<String> assertionsToConsider=new HashSet<String>();
		assertionsToConsider.addAll(assertionToDimac.keySet());
		maximalityClausesBuilder.constructMaximalityPart(assertionsToConsider);
		consistencyEncoder.buildConsistencyClauses();	
	}
	
	abstract MaximalityClausesBuilder returnConstructMaxPart();
}
