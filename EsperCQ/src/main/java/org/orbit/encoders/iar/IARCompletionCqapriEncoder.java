package org.orbit.encoders.iar;

import org.orbit.encoders.ContradictionEncoder.SingleCauseContradictionClausesBuilder;
import org.orbit.encoders.MaximalityEncoder.MaximalityClausesBuilder;
import org.orbit.filters.AnswersFilter;

public class IARCompletionCqapriEncoder extends IAREncoder {	
	
	public IARCompletionCqapriEncoder(AnswersFilter answersFilter) {
		super(answersFilter);		
	}

	protected MaximalityClausesBuilder returnConstructMaxPart() {
        return maximalityEncoder::constructCompletionMaximalityPart;
    }
	
	protected SingleCauseContradictionClausesBuilder returnConstructCauseContradictionPart() {
		return queryContradictionEncoder::contradictCauseWithConflicts;
	}

}
