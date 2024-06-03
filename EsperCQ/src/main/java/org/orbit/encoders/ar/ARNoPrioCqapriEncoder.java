package org.orbit.encoders.ar;

import org.orbit.encoders.ContradictionEncoder.ContradictionClausesBuilder;
import org.orbit.filters.AnswersFilter;

public class ARNoPrioCqapriEncoder extends ARNoPrioEncoder {
	
	public ARNoPrioCqapriEncoder(AnswersFilter answersFilter) {
		super(answersFilter);
	}

	protected ContradictionClausesBuilder returnConstructContradictionPart() {
		return queryContradictionEncoder::buildClausesContradictingCausesWithConflicts;
	}

}
