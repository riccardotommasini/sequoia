package org.orbit;


public class Parameters {

	//DEFAULT PARAMETERS: to replace with arguments to use to avoid passing them through command line
	public static String conflictGraphFile="testInput/testNoPriority_conflictgraph.json";
	public static String potentialAnswersAndCausesFile="testInput/testNoPriority_answers_causes.json";
	public static Semantics semantics=Semantics.brave;
	public static RepairType repairType=RepairType.completion;
	public static String outputFile="testOutput/testNoPriority_conflictgraph_brave_std.json";
	public static String statLogFile="testOutput/testNoPriority_output_brave_std_log.txt";
	public static Algorithm algo=Algorithm.generic_sat_based;
	public static EncodingTypeContradictionPart encodingContradiction=EncodingTypeContradictionPart.cqapri_encoding;
	public static Solvers solver=Solvers.sat4j;
	public static boolean printAnswers=true;
	public static boolean printDetails=true;
	public static String solverInput="solverFiles/input.wcnf";
	public static String solverOutput="solverFiles/output.txt";

	public enum Semantics
	{
		AR,
		IAR,
		brave;
	}

	public enum RepairType
	{
		standard,
		pareto_all_reachable_encoding,
		pareto_conf_of_conf_encoding,
		completion;
	}

	public enum Algorithm
	{
		generic_sat_based,
		generic_maxsat_based,
		generic_muses_based,
		generic_assumptions_based,
		cause_by_cause, //for IAR or brave		
		iar_cause_checking_each_assertion, //for IAR only
		iar_assertions_maxsat; //for IAR only
	}

	public enum EncodingTypeContradictionPart
	{
		cqapri_encoding,
		cavsat_encoding; //for AR or IAR
	}

	public enum Solvers
	{
		sat4j,
		sat4j_standalone,//only for maxsat
		maxhs;//only for maxsat
	}

	public Parameters(String[] args) {
		conflictGraphFile=args[0];
		potentialAnswersAndCausesFile=args[1];
		outputFile=args[2];
		statLogFile=args[3];
		semantics=Semantics.valueOf(args[4]);
		repairType=RepairType.valueOf(args[5]);
		algo=Algorithm.valueOf(args[6]);
		encodingContradiction=EncodingTypeContradictionPart.valueOf(args[7]);
		printAnswers=Boolean.valueOf(args[8]);
		printDetails=Boolean.valueOf(args[9]);
		solver=Solvers.valueOf(args[10]);
	}
}
