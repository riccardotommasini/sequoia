package org.orbit;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class InputOutputManager {

	private String conflictGraphFile;
	private String potentialAnswersAndCausesFile;
	private HashMap<String, ArrayList<String>> conflictGraph;
	private HashMap<String, ArrayList<ArrayList<String>>> potentialAnswersAndCauses;

	public InputOutputManager(String confGraphFile, String potAnswersAndCausesFile) {
		conflictGraphFile=confGraphFile;
		potentialAnswersAndCausesFile=potAnswersAndCausesFile;
	}

	public void initializeConflictGraphAndPotAnsCausesFromJSON() {
		ObjectMapper mapper = new ObjectMapper();		
		try {
			conflictGraph = mapper.readValue(new File(conflictGraphFile), HashMap.class);
			potentialAnswersAndCauses =  mapper.readValue(new File(potentialAnswersAndCausesFile), HashMap.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public HashMap<String, ArrayList<String>> getConflictGrap() {
		return conflictGraph;
	}

	public HashMap<String, ArrayList<ArrayList<String>>> getPotentialAnswersAndCauses(){
		return potentialAnswersAndCauses;
	}

	public static void writeOutput(Set<String> outputAnswers) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.writeValue(new File(Parameters.outputFile), outputAnswers);
		} catch (JsonGenerationException e) {
			System.out.println("Exception occurs when writing output:");
			e.printStackTrace();
		} catch (JsonMappingException e) {
			System.out.println("Exception occurs when writing output:");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Exception occurs when writing output:");
			e.printStackTrace();
		}

	}

}
