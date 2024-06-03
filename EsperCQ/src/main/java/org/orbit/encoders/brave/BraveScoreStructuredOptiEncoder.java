package org.orbit.encoders.brave;

import org.orbit.encoders.MaximalityEncoder.MaximalityClausesBuilder;
import org.orbit.filters.AnswersFilter;

public class BraveScoreStructuredOptiEncoder extends BraveEncoder {
	
	public BraveScoreStructuredOptiEncoder(AnswersFilter answersFilter) {
		super(answersFilter);
	}
		
	protected MaximalityClausesBuilder returnConstructMaxPart() {
        return maximalityEncoder::constructScoreStructuredOptiMaximalityPart;
    }
}
