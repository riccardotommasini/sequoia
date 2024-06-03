package org.orbit.encoders.iar;

import org.orbit.encoders.ContradictionEncoder.SingleCauseContradictionClausesBuilder;
import org.orbit.encoders.MaximalityEncoder.MaximalityClausesBuilder;
import org.orbit.filters.AnswersFilter;

public class IARCompletionCavsatEncoder extends IAREncoder {	
	
	public IARCompletionCavsatEncoder(AnswersFilter answersFilter) {
		super(answersFilter);			
	}

	protected MaximalityClausesBuilder returnConstructMaxPart() {
        return maximalityEncoder::constructCompletionMaximalityPart;
    }
	
	protected SingleCauseContradictionClausesBuilder returnConstructCauseContradictionPart() {
		return queryContradictionEncoder::buildClausesContradictingIARCauseBySelectingMissingAssertion;
	}

}
