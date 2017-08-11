package abarson.transloc.validation;

import java.util.HashMap;
import java.util.Map;

import abarson.transloc.api.Stop;
import abarson.transloc.exception.InvalidInputException;

/**
 * Defines a catalog of acceptable user {@code Stop} queries and provides support for correcting commonly misinterpreted {@code Stop} utterances.
 * Also provides support for defining synonyms, and allowing the user flexibility in how they structure their request.
 * 
 * <p>
 * 
 * See {@link Validator} for {@code Stop} validation logic.
 * @author adambarson
 *
 */
public final class StopCorrector {

	private StopCorrector(){}
	
	/**
	 * A Map of {@link Stop} names that the user may try and get information about.
	 * <p>
	 * 
	 * Allows multiple ways to ask for the same {@code Stop}, e.g. Cook, Kalkin, and Cook/Kalkin will
	 * will all converge to the same value.
	 */
	private static final Map<String, String> expectedStops = new HashMap<String, String>(){
		
		
		private static final long serialVersionUID = 1L;

		{
			put("WDW", "WDW");
			put("W. T. W", "WDW");
			put("BBW", "WDW");
			put("PFG", "PFG");
			put("HARRIS/MILLIS", "HARRIS/MILLIS");
			put("UNIVERSITY HEIGHTS", "UNIVERSITY HEIGHTS");
			put("LIVING AND LEARNING", "LIVING AND LEARNING");
			put("ROYAL TYLER THEATRE", "ROYAL TYLER THEATRE");
			put("BILLINGS LIBRARY", "BILLINGS LIBRARY");
			put("MCAULEY", "MCAULEY");
			put("MCAULAY", "MCAULEY");
			put("MACAULAY", "MCAULEY");
			put("MACAULEY", "MCAULEY");
			put("MCCULLY", "MCAULEY");
			put("MERCY HALL", "MERCY HALL");
			put("COOK/KALKIN", "COOK/KALKIN"); //out-of-season as of 8/8/2017
			put("BAILEY-HOWE LIBRARY", "BAILEY-HOWE LIBRARY");
			put("THE LIBRARY", "BAILEY-HOWE LIBRARY");
			put("LIBRARY", "BAILEY-HOWE LIBRARY");
			put("CBW", "CBW");
			put("GIVEN/ROWELL", "GIVEN/ROWELL");
			put("DAVIS SOUTH", "DAVIS SOUTH");
			put("COOLIDGE HALL", "COOLIDGE HALL");
			put("REDSTONE APTS.", "REDSTONE APTS.");
			put("REDSTONE", "REDSTONE APTS.");
			put("JEANNE MANCE", "JEANNE MANCE");
			put("PEARL/UNION", "PEARL/UNION");
			put("JEANNE MANCE", "JEANNE MANCE");
			put("SIMON'S DOWNTOWN", "SIMON'S DOWNTOWN");
			put("COOLIDGE CIRCLE", "COOLIDGE CIRCLE");
			put("WATERMAN", "WATERMAN");
			put("QUARRY HILL ROAD", "QUARRY HILL ROAD");
			put("SHERATON", "SHERATON"); //out-of-season as of 8/8/2017
		}
	};
	
	public static Map<String, String> getExpectedStops(){
		return expectedStops;
	}
	
	/**
	 * Attempts to normalize the provided stopName.
	 * @param stopName
	 * 		The provided {@code Stop} name
	 * @return The normalized {@code Stop} name
	 * @throws InvalidInputException
	 */
	public static String correctStop(String stopName) throws InvalidInputException{
		String stop = expectedStops.get(stopName.toUpperCase());	
		if (stop == null){
			throw new InvalidInputException("I did not understand your stop.");
		}
		return stop;
	}
	
}
