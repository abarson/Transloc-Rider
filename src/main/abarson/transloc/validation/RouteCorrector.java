package abarson.transloc.validation;

import java.util.HashMap;
import java.util.Map;

import abarson.transloc.api.Route;
import abarson.transloc.exception.InvalidInputException;

/**
 * Defines a catalog of acceptable user {@code Route} queries and provides support for correcting commonly misinterpreted {@code Route} utterances.
 * Also provides support for defining synonyms, and allowing the user flexibility in how they structure their request.
 * 
 * <p>
 * 
 * See {@link Validator} for {@code Route} validation logic.
 * @author adambarson
 *
 */
public final class RouteCorrector {

	private RouteCorrector(){}
	
	/**
	 * A Map of {@link Route} names that the user may try and get information about.
	 * <p>
	 * 
	 * Allows multiple ways to ask for the same {@code Route}, e.g. On-Campus, On-Campus Bus, and On-Campus Shuttle
	 * will all converge to the same value.
	 */
	private static final Map<String, String> expectedRoutes = new HashMap<String, String>(){
		
		private static final long serialVersionUID = 1L;

		{
			put("SUMMER ROUTE", "SUMMER ROUTE");
			put("SUMMER SHUTTLE", "SUMMER ROUTE");
			put("SUMMER BUS", "SUMMER ROUTE");
			put("ON-CAMPUS", "ON-CAMPUS");
			put("ON CAMPUS", "ON-CAMPUS");
			put("OFF CAMPUS LATE NIGHT", "OFF CAMPUS LATE NIGHT");
			put("LATE NIGHT", "OFF CAMPUS LATE NIGHT");
			put("OFF-CAMPUS", "OFF-CAMPUS");
			put("OFF CAMPUS", "OFF-CAMPUS");
			put("REDSTONE EXPRESS", "REDSTONE EXPRESS");
			
		}
	};
	
	public static Map<String, String> getExpectedRoutes(){
		return expectedRoutes;
	}
	
	/**
	 * Attempts to normalize the provided routeName.
	 * @param routeName
	 * 		The provided {@code Route} name
	 * @return The normalized {@code Route} name
	 * @throws InvalidInputException
	 */
	public static String correctRoute(String routeName) throws InvalidInputException{
		String route = expectedRoutes.get(routeName.toUpperCase());	
		if (route == null){
			throw new InvalidInputException("I did not understand your route.");
		}
		return route;
	}
	
}
