package org.orbit.filters;


import org.orbit.Logger;
import org.orbit.Main.Satisfiability;
import org.orbit.encoders.SATEncoder;
import org.orbit.encoders.SATEncoderFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public abstract class AnswersFilter {

    protected HashMap<String, ArrayList<String>> conflictGraph;
    protected HashMap<String, ArrayList<ArrayList<String>>> potentialAnswersAndCauses;
    protected Set<String> answersToFilter;
    protected Set<String> outputAnswers;
    protected Satisfiability satValueForCorrect;
    protected Logger statLogger;
    protected SATEncoderFactory encoderFactory;
    protected SATEncoder encoder;

    public AnswersFilter(HashMap<String, ArrayList<String>> confGraph,
                         HashMap<String, ArrayList<ArrayList<String>>> potAnswersAndCauses,
                         SATEncoderFactory satEncoderFactory,
                         Satisfiability satValueForCorrectAnswers,
                         Logger statLog) {
        conflictGraph = confGraph;
        potentialAnswersAndCauses = potAnswersAndCauses;
        outputAnswers = new HashSet<String>();
        satValueForCorrect = satValueForCorrectAnswers;
        statLogger = statLog;
        encoderFactory = satEncoderFactory;
        encoderFactory.setFilter(this);
        encoder = encoderFactory.createEncoder();
    }

    public HashMap<String, ArrayList<String>> getConflictGraph() {
        return conflictGraph;
    }

    public HashMap<String, ArrayList<ArrayList<String>>> getPotentialAnswersAndCauses() {
        return potentialAnswersAndCauses;
    }

    public Set<String> run() throws IOException {
        long startFilterSelfInc = System.currentTimeMillis();
        removeSelfInconsistentAssertions();
        long timeFilterSelfInc = System.currentTimeMillis() - startFilterSelfInc;
        statLogger.lognl("Self-inconsistent assertions filtered in: " + timeFilterSelfInc + " ms.");
        long startIAR = System.currentTimeMillis();
        filterUnconflictedAnswersAndAssertions();
        long timeIAR = System.currentTimeMillis() - startIAR;
        statLogger.lognl("Answers trivially IAR found in: " + timeIAR + " ms.");
        long startFilterRemaining = System.currentTimeMillis();
        if (answersToFilter.size() != 0) {
            filterRemainingAnswers();
        }
        long timeFilterRemaining = System.currentTimeMillis() - startFilterRemaining;
        statLogger.lognl("Time to filter remaining answers (not trivially IAR): " + timeFilterRemaining + " ms.");
        return outputAnswers;
    }

    //Preprocess: remove self-inconsistent assertions
    private void removeSelfInconsistentAssertions() {
        ArrayList<String> newIARassertionsToRemoveFromConflictGraph = new ArrayList<String>();
        for (String assertion : conflictGraph.keySet()) {
            if (conflictGraph.get(assertion).contains(assertion)) {
                //Remove causes that contain assertion and answer with no remaining causes
                ArrayList<String> inconsistentAnswersToRemove = new ArrayList<String>();
                for (String ans : potentialAnswersAndCauses.keySet()) {
                    ArrayList<ArrayList<String>> inconsistentCausesToRemove = new ArrayList<ArrayList<String>>();
                    for (ArrayList<String> cause : potentialAnswersAndCauses.get(ans)) {
                        if (cause.contains(assertion)) {
                            inconsistentCausesToRemove.add(cause);
                        }
                    }
                    potentialAnswersAndCauses.get(ans).removeAll(inconsistentCausesToRemove);
                    if (potentialAnswersAndCauses.get(ans).isEmpty()) {//All causes of ans were inconsistent
                        inconsistentAnswersToRemove.add(ans);
                    }
                }
                for (String inconsAns : inconsistentAnswersToRemove) {
                    potentialAnswersAndCauses.remove(inconsAns);
                }
                //Remove self-inconsistent assertion from conflict graph
                ArrayList<String> linkedAssertions = new ArrayList<String>();
                for (String ass : conflictGraph.keySet()) {
                    if (conflictGraph.get(ass).contains(assertion)) {
                        linkedAssertions.add(ass);
                    }
                }
                for (String linked : linkedAssertions) {
                    conflictGraph.get(linked).remove(assertion);
                    if (conflictGraph.get(linked).isEmpty()) {
                        newIARassertionsToRemoveFromConflictGraph.add(linked);
                    }
                }
            }
        }
        for (String assertionWithoutConf : newIARassertionsToRemoveFromConflictGraph) {
            conflictGraph.remove(assertionWithoutConf);
        }
    }

    //First filtering of answers with some cause without conflict and remove assertions without conflict from causes
    private void filterUnconflictedAnswersAndAssertions() throws IOException {
        for (String ans : answersToFilter) {
            for (ArrayList<String> cause : potentialAnswersAndCauses.get(ans)) {
                cause.retainAll(conflictGraph.keySet());
                if (cause.isEmpty()) {
                    outputAnswers.add(ans);
                    break;
                }
            }
        }
        answersToFilter.removeAll(outputAnswers);
        statLogger.lognl("Number of answers trivially IAR: " + outputAnswers.size());
    }

    protected abstract void filterRemainingAnswers() throws IOException;

    public Set<String> filterRemainingAnswers(HashMap<String, ArrayList<String>> confGraph,
                                              HashMap<String, ArrayList<ArrayList<String>>> potAnswersAndCauses) throws IOException {
        this.conflictGraph = confGraph;
        this.potentialAnswersAndCauses = potAnswersAndCauses;
        this.outputAnswers = new HashSet<>();
        answersToFilter = potentialAnswersAndCauses.keySet();
        filterRemainingAnswers();
        return outputAnswers;
    }

}
