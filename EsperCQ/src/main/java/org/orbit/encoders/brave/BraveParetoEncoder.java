package org.orbit.encoders.brave;

import org.orbit.encoders.MaximalityEncoder.MaximalityClausesBuilder;
import org.orbit.filters.AnswersFilter;

public class BraveParetoEncoder extends BraveEncoder {

	public BraveParetoEncoder(AnswersFilter answersFilter) {
		super(answersFilter);
	}

	protected MaximalityClausesBuilder returnConstructMaxPart() {
		return maximalityEncoder::constructParetoMaximalityPart;
	}
}
