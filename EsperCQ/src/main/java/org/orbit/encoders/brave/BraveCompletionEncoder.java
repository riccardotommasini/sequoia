package org.orbit.encoders.brave;

import org.orbit.encoders.MaximalityEncoder.MaximalityClausesBuilder;
import org.orbit.filters.AnswersFilter;

public class BraveCompletionEncoder extends BraveEncoder {
	
	public BraveCompletionEncoder(AnswersFilter answersFilter) {
		super(answersFilter);
	}
	
	protected MaximalityClausesBuilder returnConstructMaxPart() {
        return maximalityEncoder::constructCompletionMaximalityPart;
    }
}
