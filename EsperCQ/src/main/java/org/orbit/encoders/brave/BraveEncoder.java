package org.orbit.encoders.brave;

import org.orbit.encoders.ConsistencyEncoder;
import org.orbit.encoders.MaximalityEncoder;
import org.orbit.encoders.MaximalityEncoder.MaximalityClausesBuilder;
import org.orbit.encoders.SATEncoder;
import org.orbit.encoders.SomeCauseEncoder;
import org.orbit.filters.AnswersFilter;
import org.orbit.filters.CauseByCauseFilter;

import java.util.HashSet;
import java.util.Set;

public abstract class BraveEncoder extends SATEncoder{
	private ConsistencyEncoder consistencyEncoder;
	protected SomeCauseEncoder someCauseEncoder;
	protected MaximalityEncoder maximalityEncoder;
	
	public BraveEncoder(AnswersFilter answersFilter) {
		super(answersFilter);
		consistencyEncoder= new ConsistencyEncoder(this, this.filter);
		maximalityEncoder= new MaximalityEncoder(this, this.filter);
		someCauseEncoder = new SomeCauseEncoder(this, this.filter, 
				encodingForSeveralElem, answersFilter instanceof CauseByCauseFilter);
	}
	
	protected void buildClauses() {	
		buildClauses(returnConstructMaxPart());
	}
	
	private void buildClauses(MaximalityClausesBuilder maximalityClausesBuilder) {
		someCauseEncoder.buildSomeCauseClauses();
		Set<String> assertionsToConsider=new HashSet<String>();		
		assertionsToConsider.addAll(assertionToDimac.keySet());
		maximalityClausesBuilder.constructMaximalityPart(assertionsToConsider);
		consistencyEncoder.buildConsistencyClauses();	
	}
	
	abstract MaximalityClausesBuilder returnConstructMaxPart();
	
}
