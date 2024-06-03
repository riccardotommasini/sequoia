package org.orbit.encoders;

import org.orbit.filters.AnswersFilter;

public abstract class AuxiliaryEncoder {
	
	protected SATEncoder encoder;
	protected AnswersFilter filter;

	public AuxiliaryEncoder(SATEncoder satEncoder, AnswersFilter answersFilter) {
		encoder=satEncoder;
		filter=answersFilter;
	}

}
