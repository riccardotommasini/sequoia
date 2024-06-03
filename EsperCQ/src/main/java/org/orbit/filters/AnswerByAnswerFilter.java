package org.orbit.filters;

import org.orbit.Logger;
import org.orbit.encoders.SATEncoderFactory;
import org.orbit.encoders.SATEncoding;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.orbit.Main.*;

public class AnswerByAnswerFilter extends AnswersFilter {

    protected ISolver satSolver;

    public AnswerByAnswerFilter(HashMap<String, ArrayList<String>> confGraph,
                                HashMap<String, ArrayList<ArrayList<String>>> potAnswersAndCauses,
                                SATEncoderFactory satEncoderFactory,
                                Satisfiability satValueForCorrectAnswers,
                                Logger statLog) {
        super(confGraph, potAnswersAndCauses, satEncoderFactory, satValueForCorrectAnswers, statLog);
        satSolver = SolverFactory.newLight();
    }

    @Override
    protected void filterRemainingAnswers() throws IOException {
        for (String ans : answersToFilter) {
            long startEncode = System.currentTimeMillis();
            encoder.resetEncoderForAnswer(ans);
            encoder.encode();
            long timeEncode = System.currentTimeMillis() - startEncode;
            long startSolve = System.currentTimeMillis();
            runSolverToCheckAnswer(ans);
            long timeSolving = System.currentTimeMillis() - startSolve;

            statLogger.lognlIfPrintDetailsRequired(
                    ans + " - encoding time: " + timeEncode +
                    " ms - solving time: " + timeSolving + " ms - nb variables: " +
                    encoder.getEncoding().getNbVars() + " - nb clauses: " +
                    encoder.getEncoding().getNbClauses()
            );
        }
    }

    protected void runSolverToCheckAnswer(String answer) throws IOException {
        switch (satValueForCorrect) {
            case UNSAT:
                if (isUnsat(encoder.getEncoding())) {
                    outputAnswers.add(answer);
                }
                break;
            case SAT:
                if (!isUnsat(encoder.getEncoding())) {
                    outputAnswers.add(answer);
                }
                break;
        }
    }

    protected boolean isUnsat(SATEncoding encoding) {
        satSolver.reset();
        satSolver.newVar(encoding.getNbVars());
        satSolver.setExpectedNumberOfClauses(encoding.getNbClauses());
        try {
            satSolver.addAllClauses(encoding.getHardClauses());
        } catch (ContradictionException e1) {
            //trivial unsat
            return true;
        }
        try {
            if (satSolver.isSatisfiable()) {
                return false;
            } else {
                return true;
            }
        } catch (TimeoutException e) {
            System.out.print("time out");
            e.printStackTrace();
        }
        return true;
    }


}
