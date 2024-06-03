package org.orbit.encoders.ar;

import org.orbit.encoders.ContradictionEncoder.ContradictionClausesBuilder;
import org.orbit.encoders.MaximalityEncoder.MaximalityClausesBuilder;
import org.orbit.filters.AnswersFilter;

public class ARParetoCavsatEncoder extends ARPrioEncoder {

	public ARParetoCavsatEncoder(AnswersFilter answersFilter) {
		super(answersFilter);
	}
	
	protected MaximalityClausesBuilder returnConstructMaxPart() {
        return maximalityEncoder::constructParetoMaximalityPart;
    }

	protected ContradictionClausesBuilder returnConstructContradictionPart() {
		return queryContradictionEncoder::buildClausesContradictingCausesBySelectingMissingAssertion;
	}
	
}
