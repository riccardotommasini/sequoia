package org.orbit.encoders.ar;

import org.orbit.encoders.ContradictionEncoder.ContradictionClausesBuilder;
import org.orbit.encoders.MaximalityEncoder.MaximalityClausesBuilder;
import org.orbit.filters.AnswersFilter;

public class ARScoreStructuredOptiCavsatEncoder extends ARPrioEncoder {

	public ARScoreStructuredOptiCavsatEncoder(AnswersFilter answersFilter) {
		super(answersFilter);
	}

	protected MaximalityClausesBuilder returnConstructMaxPart() {
        return maximalityEncoder::constructScoreStructuredOptiMaximalityPart;
    }

	protected ContradictionClausesBuilder returnConstructContradictionPart() {
		return queryContradictionEncoder::buildClausesContradictingCausesBySelectingMissingAssertion;
	}

}
