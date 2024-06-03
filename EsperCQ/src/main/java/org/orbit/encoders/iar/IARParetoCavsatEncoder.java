package org.orbit.encoders.iar;

import org.orbit.encoders.ContradictionEncoder.SingleCauseContradictionClausesBuilder;
import org.orbit.encoders.MaximalityEncoder.MaximalityClausesBuilder;
import org.orbit.filters.AnswersFilter;

public class IARParetoCavsatEncoder extends IAREncoder {	
	
	public IARParetoCavsatEncoder(AnswersFilter answersFilter) {
		super(answersFilter);		
	}

	protected MaximalityClausesBuilder returnConstructMaxPart() {
        return maximalityEncoder::constructParetoMaximalityPart;
    }
	
	protected SingleCauseContradictionClausesBuilder returnConstructCauseContradictionPart() {
		return queryContradictionEncoder::buildClausesContradictingIARCauseBySelectingMissingAssertion;
	}

}
