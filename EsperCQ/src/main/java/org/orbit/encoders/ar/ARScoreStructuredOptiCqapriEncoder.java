package org.orbit.encoders.ar;

import org.orbit.encoders.ContradictionEncoder.ContradictionClausesBuilder;
import org.orbit.encoders.MaximalityEncoder.MaximalityClausesBuilder;
import org.orbit.filters.AnswersFilter;

public class ARScoreStructuredOptiCqapriEncoder extends ARPrioEncoder {

	public ARScoreStructuredOptiCqapriEncoder(AnswersFilter answersFilter) {
		super(answersFilter);
	}

	protected MaximalityClausesBuilder returnConstructMaxPart() {
        return maximalityEncoder::constructScoreStructuredOptiMaximalityPart;
    }

	protected ContradictionClausesBuilder returnConstructContradictionPart() {
		return queryContradictionEncoder::buildClausesContradictingCausesWithConflicts;
	}

}
