package org.orbit.encoders.ar;

import org.orbit.encoders.ConsistencyEncoder;
import org.orbit.encoders.ContradictionEncoder;
import org.orbit.encoders.ContradictionEncoder.ContradictionClausesBuilder;
import org.orbit.encoders.SATEncoder;
import org.orbit.filters.AnswersFilter;

public abstract class AREncoder extends SATEncoder {
	
	protected ConsistencyEncoder consistencyEncoder;
	protected ContradictionEncoder queryContradictionEncoder;

	public AREncoder(AnswersFilter answersFilter) {
		super(answersFilter);
		consistencyEncoder= new ConsistencyEncoder(this, this.filter);		
		queryContradictionEncoder= new ContradictionEncoder(this, this.filter, encodingForSeveralElem);	
	}
	
	abstract ContradictionClausesBuilder returnConstructContradictionPart();	
	
}
