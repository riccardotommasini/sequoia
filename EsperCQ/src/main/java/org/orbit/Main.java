package org.orbit;


import org.orbit.encoders.ar.AREncoderFactory;
import org.orbit.encoders.brave.BraveEncoderFactory;
import org.orbit.encoders.iar.IAREncoderFactory;
import org.orbit.filters.*;

import java.io.IOException;
import java.util.*;

import static org.orbit.Parameters.*;

public class Main {

    private static HashMap<String, ArrayList<String>> conflictGraph;
    private static HashMap<String, ArrayList<ArrayList<String>>> potentialAnswersAndCauses;
    public static Set<String> outputAnswers;
    private static AnswersFilter filter;

    public enum Satisfiability {SAT, UNSAT;}

    public static void main(String[] args) {
        /*if 11 arguments are given in command line, they correspond to Parameters fields
         * conflictGraphFile: path to conflict graph file,
         * potentialAnswersAndCausesFile: path to file containing maps of answers and their causes,
         * outputFile: path to file where answers true under the given semantics will be written if parameter printAnswers is set to true,
         * statLogFile: path to file where statistic (number of answers and times) will be written,
         * semantics: among AR, IAR and brave,
         * repairType: among standard, pareto_all_reachable_encoding, pareto_conf_of_conf_encoding and completion,
         * algo: choose an algorithm compatible with the chosen semantics and repair type,
         * encodingContradiction: cqapri_encoding or cavsat_encoding if the semantics is AR or IAR,
         * printAnswers: true to print the answers that hold under the required semantics, false to only print statistics (number of answers and run times)
         * printDetails: true to print details about sizes of encodings and times to encode and solve the problems,
         * solver: sat4j by default, possibility of using a standalone maxsat solver instead for maxsat based algorithm*/
        if (args.length == 11) {
            new Parameters(args);
        } else {
            System.out.println("No argument given (or wrong number of arguments), use parameters defined in Parameters.java");
        }
        Logger statLog;
        try {
            statLog = new Logger(statLogFile, printDetails);
            statLog.lognl(conflictGraphFile);
            statLog.lognl(potentialAnswersAndCausesFile);
            statLog.lognl("Semantic selected : " + semantics + " with repairs: " + repairType);

            //Initialize conflict graph and potential answers and their causes from JSON
            long startLoad = System.currentTimeMillis();
            InputOutputManager inputOutputManager = new InputOutputManager(conflictGraphFile, potentialAnswersAndCausesFile);
            inputOutputManager.initializeConflictGraphAndPotAnsCausesFromJSON();

            conflictGraph = inputOutputManager.getConflictGrap();
            potentialAnswersAndCauses = inputOutputManager.getPotentialAnswersAndCauses();

            long loadingTime = System.currentTimeMillis() - startLoad;
            statLog.lognl("Conflict graph and potential answers and causes loaded in: " + loadingTime + " ms.");

            statLog.lognl("Number of potential answers: " + potentialAnswersAndCauses.keySet().size());

            //Filter answers that hold under the required semantics among potential answers
            long startSolve = System.currentTimeMillis();
            if (semantics.equals(Semantics.IAR) && repairType.equals(RepairType.standard)) {
                outputAnswers = computeStandardIARanswers(conflictGraph, potentialAnswersAndCauses);
            } else if (semantics.equals(Semantics.brave) && repairType.equals(RepairType.standard)) {
                outputAnswers = computeStandardBraveanswers(conflictGraph, potentialAnswersAndCauses);
            } else {
                switch (algo) {
                    case generic_sat_based:
                        switch (semantics) {
                            case AR:
                                filter = new AnswerByAnswerFilter(conflictGraph, potentialAnswersAndCauses, new AREncoderFactory(repairType, encodingContradiction), Satisfiability.UNSAT, statLog);
                                break;
                            case IAR:
                                filter = new AnswerByAnswerFilter(conflictGraph, potentialAnswersAndCauses, new IAREncoderFactory(repairType, encodingContradiction), Satisfiability.UNSAT, statLog);
                                break;
                            case brave:
                                filter = new AnswerByAnswerFilter(conflictGraph, potentialAnswersAndCauses, new BraveEncoderFactory(repairType, encodingContradiction), Satisfiability.SAT, statLog);
                                break;
                            default:
                                System.out.println("unsupported semantic");
                                break;
                        }
                        break;
                    case generic_maxsat_based:
                        switch (semantics) {
                            case AR:
                                filter = new MaxSatBasedAllAnswersAtOnceFilter(conflictGraph, potentialAnswersAndCauses, new AREncoderFactory(repairType, encodingContradiction), Satisfiability.UNSAT, statLog);
                                break;
                            case IAR:
                                filter = new MaxSatBasedAllAnswersAtOnceFilter(conflictGraph, potentialAnswersAndCauses, new IAREncoderFactory(repairType, encodingContradiction), Satisfiability.UNSAT, statLog);
                                break;
                            case brave:
                                filter = new MaxSatBasedAllAnswersAtOnceFilter(conflictGraph, potentialAnswersAndCauses, new BraveEncoderFactory(repairType, encodingContradiction), Satisfiability.SAT, statLog);
                                break;
                            default:
                                System.out.println("unsupported semantic");
                                break;
                        }
                        break;
                    case generic_muses_based:
                        switch (semantics) {
                            case AR:
                                filter = new MUSesBasedAllAnswersAtOnceFilter(conflictGraph, potentialAnswersAndCauses, new AREncoderFactory(repairType, encodingContradiction), Satisfiability.UNSAT, statLog);
                                break;
                            case IAR:
                                filter = new MUSesBasedAllAnswersAtOnceFilter(conflictGraph, potentialAnswersAndCauses, new IAREncoderFactory(repairType, encodingContradiction), Satisfiability.UNSAT, statLog);
                                break;
                            case brave:
                                filter = new MUSesBasedAllAnswersAtOnceFilter(conflictGraph, potentialAnswersAndCauses, new BraveEncoderFactory(repairType, encodingContradiction), Satisfiability.SAT, statLog);
                                break;
                            default:
                                System.out.println("unsupported semantic");
                                break;
                        }
                        break;
                    case generic_assumptions_based:
                        switch (semantics) {
                            case AR:
                                filter = new AssumptionsBasedAllAnswersAtOnceFilter(conflictGraph, potentialAnswersAndCauses, new AREncoderFactory(repairType, encodingContradiction), Satisfiability.UNSAT, statLog);
                                break;
                            case IAR:
                                filter = new AssumptionsBasedAllAnswersAtOnceFilter(conflictGraph, potentialAnswersAndCauses, new IAREncoderFactory(repairType, encodingContradiction), Satisfiability.UNSAT, statLog);
                                break;
                            case brave:
                                filter = new AssumptionsBasedAllAnswersAtOnceFilter(conflictGraph, potentialAnswersAndCauses, new BraveEncoderFactory(repairType, encodingContradiction), Satisfiability.SAT, statLog);
                                break;
                            default:
                                System.out.println("unsupported semantic");
                                break;
                        }
                        break;
                    case cause_by_cause:
                        switch (semantics) {
                            case AR:
                                System.out.println("Algorihtm " + Algorithm.cause_by_cause.toString() + " incompatible with semantics " + Semantics.AR);
                                break;
                            case IAR:
                                filter = new CauseByCauseFilter(conflictGraph, potentialAnswersAndCauses, new IAREncoderFactory(repairType, encodingContradiction), Satisfiability.UNSAT, statLog);
                                break;
                            case brave:
                                filter = new CauseByCauseFilter(conflictGraph, potentialAnswersAndCauses, new BraveEncoderFactory(repairType, encodingContradiction), Satisfiability.SAT, statLog);
                                break;
                            default:
                                System.out.println("unsupported semantic");
                                break;
                        }
                        break;
                    case iar_cause_checking_each_assertion:
                        switch (semantics) {
                            case AR:
                                System.out.println("Algorihtm " + Algorithm.iar_cause_checking_each_assertion.toString() + " incompatible with semantics " + Semantics.AR);
                                break;
                            case IAR:
                                filter = new IARCauseFilter(conflictGraph, potentialAnswersAndCauses, new IAREncoderFactory(repairType, encodingContradiction), Satisfiability.UNSAT, statLog);
                                break;
                            case brave:
                                System.out.println("Algorihtm " + Algorithm.iar_cause_checking_each_assertion.toString() + " incompatible with semantics " + Semantics.brave);
                                break;
                            default:
                                System.out.println("unsupported semantic");
                                break;
                        }
                        break;
                    case iar_assertions_maxsat:
                        switch (semantics) {
                            case AR:
                                System.out.println("Algorihtm " + Algorithm.iar_assertions_maxsat.toString() + " incompatible with semantics " + Semantics.AR);
                                break;
                            case IAR:
                                filter = new AllIARAssertionsFilter(conflictGraph, potentialAnswersAndCauses, new IAREncoderFactory(repairType, encodingContradiction), Satisfiability.UNSAT, statLog);
                                break;
                            case brave:
                                System.out.println("Algorihtm " + Algorithm.iar_assertions_maxsat.toString() + " incompatible with semantics " + Semantics.brave);
                                break;
                            default:
                                System.out.println("unsupported semantic");
                                break;
                        }
                        break;
                    default:
                        System.out.println("unsupported algorithm");
                        break;
                }
                outputAnswers = filter.run();
            }
            long totalSolvingTime = System.currentTimeMillis() - startSolve;
            statLog.lognl("Total filtering time: " + totalSolvingTime + " ms.");
            statLog.lognl("Number of correct answers: " + outputAnswers.size());

            //Write output in JSON file
            if (printAnswers) {
                long startWrite = System.currentTimeMillis();
                InputOutputManager.writeOutput(outputAnswers);
                long writingTime = System.currentTimeMillis() - startWrite;
                statLog.lognl("Answers written to JSON in " + writingTime + " ms.");
            }

            statLog.closeLogger();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Set<String> computeStandardIARanswers(HashMap<String, ArrayList<String>> conflictGraph, HashMap<String, ArrayList<ArrayList<String>>> potentialAnswersAndCauses) {
        Set<String> selfInc = new HashSet<String>();
        for (String assertion : conflictGraph.keySet()) {
            if (conflictGraph.get(assertion).contains(assertion)) {
                selfInc.add(assertion);
                conflictGraph.remove(assertion);
            }
        }
        Set<String> notIARAssertions = new HashSet<String>();
        notIARAssertions.addAll(selfInc);
        for (String assertion : conflictGraph.keySet()) {
            conflictGraph.get(assertion).removeAll(selfInc);
            if (!conflictGraph.get(assertion).isEmpty()) {
                notIARAssertions.add(assertion);
                notIARAssertions.addAll(conflictGraph.get(assertion));
            }
        }

        Set<String> iarAnswers = new HashSet<String>();
        for (String ans : potentialAnswersAndCauses.keySet()) {
            for (List<String> cause : potentialAnswersAndCauses.get(ans)) {
                cause.retainAll(notIARAssertions);
                if (cause.isEmpty()) {
                    iarAnswers.add(ans);
                    break;
                }
            }
        }
        return iarAnswers;
    }

    private static Set<String> computeStandardBraveanswers(HashMap<String, ArrayList<String>> conflictGraph, HashMap<String, ArrayList<ArrayList<String>>> potentialAnswersAndCauses) {
        Set<String> braveAnswers = new HashSet<String>();
        for (String ans : potentialAnswersAndCauses.keySet()) {
            for (List<String> cause : potentialAnswersAndCauses.get(ans)) {
                boolean braveCause = true;
                for (String assertion : cause) {
                    if (conflictGraph.containsKey(assertion)) {
                        for (String conflict : conflictGraph.get(assertion)) {
                            if (cause.contains(conflict)) {
                                braveCause = false;
                            }
                        }
                    }
                }
                if (braveCause) {
                    braveAnswers.add(ans);
                    break;
                }
            }
        }
        return braveAnswers;
    }

}
