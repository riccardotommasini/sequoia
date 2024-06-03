package org.orbit.encoders.iar;

import org.orbit.encoders.ContradictionEncoder.SingleCauseContradictionClausesBuilder;
import org.orbit.encoders.MaximalityEncoder.MaximalityClausesBuilder;
import org.orbit.filters.AnswersFilter;

public class IARScoreStructuredOptiCavsatEncoder extends IAREncoder {	
	
	public IARScoreStructuredOptiCavsatEncoder(AnswersFilter answersFilter) {
		super(answersFilter);		
	}

	protected MaximalityClausesBuilder returnConstructMaxPart() {
        return maximalityEncoder::constructScoreStructuredOptiMaximalityPart;
    }

	protected SingleCauseContradictionClausesBuilder returnConstructCauseContradictionPart() {
		return queryContradictionEncoder::buildClausesContradictingIARCauseBySelectingMissingAssertion;
	}
}
