package org.orbit.encoders.ar;

import org.orbit.encoders.ContradictionEncoder.ContradictionClausesBuilder;
import org.orbit.encoders.MaximalityEncoder.MaximalityClausesBuilder;
import org.orbit.filters.AnswersFilter;

public class ARCompletionCqapriEncoder extends ARPrioEncoder {

	public ARCompletionCqapriEncoder(AnswersFilter answersFilter) {
		super(answersFilter);
	}
	
	protected MaximalityClausesBuilder returnConstructMaxPart() {
        return maximalityEncoder::constructCompletionMaximalityPart;
    }

	protected ContradictionClausesBuilder returnConstructContradictionPart() {
		return queryContradictionEncoder::buildClausesContradictingCausesWithConflicts;
	}
	
}
