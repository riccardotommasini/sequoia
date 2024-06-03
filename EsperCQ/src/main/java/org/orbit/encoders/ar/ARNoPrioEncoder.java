package org.orbit.encoders.ar;

import org.orbit.encoders.ContradictionEncoder.ContradictionClausesBuilder;
import org.orbit.filters.AnswersFilter;

public abstract class ARNoPrioEncoder extends AREncoder {

	public ARNoPrioEncoder(AnswersFilter answersFilter) {
		super(answersFilter);
	}

	protected void buildClauses() {	
		buildClauses(returnConstructContradictionPart());
	}

	protected void buildClauses(ContradictionClausesBuilder contradictionClausesBuilder) {	
		contradictionClausesBuilder.constructContradictionPart();
		consistencyEncoder.buildConsistencyClauses();	
	}	
}
