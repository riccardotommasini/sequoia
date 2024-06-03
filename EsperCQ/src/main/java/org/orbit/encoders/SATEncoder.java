package org.orbit.encoders;
import org.orbit.filters.AllAnswersAtOnceFilter;
import org.orbit.filters.AnswersFilter;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class SATEncoder {

	protected AnswersFilter filter;
	protected boolean encodingForSeveralElem;

	protected String answerToCheck;
	protected Set<String> setOfElementsToCheck;
	protected List<String> causeToCheck;
	
	protected SATEncoding encoding; 
	protected int dimacVar;
	protected Map<String, Integer> assertionToDimac;
	protected Map<Integer,String> dimacToAssertion;
	protected Map<String, Integer> answerToDimac;
	protected Map<Integer,String> dimacToAnswer;
	protected Map<Map.Entry<String,String>, Integer> prioRelationToDimac;
	protected Map<Map.Entry<String,String>, Integer> prioClosureToDimac;
	protected Map<Map.Entry<String,String>, Integer> completionAuxVariablesToDimac;

	public SATEncoder(AnswersFilter answersFilter) {
		filter=answersFilter;
		encodingForSeveralElem=answersFilter instanceof AllAnswersAtOnceFilter;
	}

	public void resetEncoderForAnswer(String answer) {
		answerToCheck=answer;
		initFields();
	}

	public void resetEncoderForSetOfAnswers(Set<String> setOfAnswers) {
		setOfElementsToCheck=setOfAnswers;
		initFields();
		answerToDimac= new HashMap<String, Integer>();
		dimacToAnswer=new HashMap<Integer,String>();
		for(String answer:setOfElementsToCheck) {
			answerToDimac.put(answer,dimacVar);
			dimacToAnswer.put(dimacVar, answer);
			dimacVar++;
		}
	}
	
	public void resetEncoderForCause(List<String> cause) {
		causeToCheck=cause;
		initFields();		
	}

	private void initFields() {
		encoding=initSATEncoding();
		dimacVar=1;
		assertionToDimac=new HashMap<String, Integer>();		
		dimacToAssertion=new HashMap<Integer,String>();
		prioRelationToDimac=new HashMap<Map.Entry<String,String>, Integer>();		
		prioClosureToDimac=new HashMap<Map.Entry<String,String>, Integer>();
		completionAuxVariablesToDimac=new HashMap<Map.Entry<String,String>, Integer>();
	}

	protected SATEncoding initSATEncoding() {
		if(encodingForSeveralElem) {
			return initSATEncodingForMaxSat();
		}
		else {
			return initSATEncodingForSat();
		}
	}

	private SATEncoding initSATEncodingForMaxSat() {
		return new SATEncoding("p wcnf");
	}

	private SATEncoding initSATEncodingForSat() {
		return new SATEncoding("p cnf");
	}

	public SATEncoding getEncoding() {
		return encoding;
	}

	public void encode() {
		buildClauses();
		encoding.setNbVars(dimacVar-1);
	}

	protected abstract void buildClauses();

	protected void checkIfHasDimacAndAddIfNot(String assertion) {
		if(hasNotDimac(assertion)) {
			addDimac(assertion);
		}	
	}

	protected boolean hasNotDimac(String assertion) {
		return !assertionToDimac.keySet().contains(assertion);
	}

	protected void addDimac(String assertion) {
		assertionToDimac.put(assertion,dimacVar);
		dimacToAssertion.put(dimacVar, assertion);
		dimacVar++;
	}

	public Integer getAnswerDimac(String answer) {
		return answerToDimac.get(answer);
	}
	
	public String getAnswerDimac(Integer dimac) {
		return dimacToAnswer.get(dimac);
	}

	protected void checkIfprioRelationHasDimacAndAddIfNot(Map.Entry<String,String> pair) {
		if(!prioRelationToDimac.keySet().contains(pair)) {
			prioRelationToDimac.put(pair, dimacVar);
			dimacVar++;
		}
	}

	protected void checkIfprioClosureHasDimacAndAddIfNot(Map.Entry<String,String> pair) {
		if(!prioClosureToDimac.keySet().contains(pair)) {
			prioClosureToDimac.put(pair, dimacVar);
			dimacVar++;
		}
	}

	protected void checkIfAuxVariablesHasDimacAndAddIfNot(Map.Entry<String,String> pair) {
		if(!completionAuxVariablesToDimac.keySet().contains(pair)) {
			completionAuxVariablesToDimac.put(pair, dimacVar);
			dimacVar++;
		}
	}

	

}
