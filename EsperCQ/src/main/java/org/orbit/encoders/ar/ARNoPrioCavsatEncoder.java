package org.orbit.encoders.ar;

import org.orbit.encoders.ContradictionEncoder.ContradictionClausesBuilder;
import org.orbit.filters.AnswersFilter;

public class ARNoPrioCavsatEncoder extends ARNoPrioEncoder {

	public ARNoPrioCavsatEncoder(AnswersFilter answersFilter) {
		super(answersFilter);
	}
	
	protected ContradictionClausesBuilder returnConstructContradictionPart() {
		return queryContradictionEncoder::buildClausesContradictingCausesBySelectingMissingAssertion;
	}

}
