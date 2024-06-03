package org.orbit.encoders;

import org.orbit.Logger;
import org.sat4j.core.Vec;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

import java.io.IOException;

public class SATEncoding {

	private int nbVars;
	private IVec<IVecInt> hardclauses;
	private IVec<IVecInt> softclauses;
	private String problemType;
	private String topWeight;

	public SATEncoding(String problemCode) {		
		hardclauses=new Vec<IVecInt>();
		softclauses=new Vec<IVecInt>();
		problemType=problemCode;
	}

	public void setNbVars(int nbV) {
		nbVars=nbV;
	}
	
	public void addSoftClause(IVecInt clause) {
		softclauses.push(clause);
	}
	
	public void addHardClause(IVecInt clause) {
		hardclauses.push(clause);		
	}
	
	public int getNbVars() {
		return nbVars;
	}

	public int getNbClauses() {
		return hardclauses.size()+softclauses.size();
	}

	public IVec<IVecInt> getHardClauses() {
		return hardclauses;
	}
	
	public IVec<IVecInt> getSoftClauses() {
		return softclauses;
	}

	public void logTo(Logger solverInput) throws IOException {
		topWeight="";
		if(softclauses.size()!=0) {
			topWeight=(10*(softclauses.size()+1))+" ";
		}		
		solverInput.lognl(problemType+" "+nbVars+" "+getNbClauses()+" "+topWeight);
		for(int i=0;i<hardclauses.size();i++) {			
			solverInput.lognl(topWeight+hardclauses.get(i).toString().replace(",", " ")+" 0");
		}
		for(int i=0;i<softclauses.size();i++) {
			solverInput.lognl(1+" "+softclauses.get(i).toString().replace(",", " ")+" 0");
		}
		solverInput.flushLogger();
	}
}
