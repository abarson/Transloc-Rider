package abarson;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;

import abarson.transloc.api.Stop;
import abarson.transloc.api.TranslocApi;
import abarson.transloc.validation.StopCorrector;

public class StopTest {
	
	private static List<Stop> translocStops;
	
	@BeforeClass
	public static void initialize(){
		try {
			translocStops = TranslocApi.getStops();
		} catch (JSONException | IOException e){
			fail("Failed to connect to Transloc API: " + e.getMessage());
		}
	}
	
	@Test
	/**
	 * Test to ensure that StopCorrector encapsulates all Stops returned by Transloc API.
	 * This will help to ensure that the stop list is up to date, and help stay on top of
	 * seasonal stops being changed.
	 */
	public void testValidateStop() {
		List<String> missingStops = new ArrayList<String>();
		
		for (Stop stop : translocStops){
			String stopName = stop.getName();
			if (!StopCorrector.getExpectedStops().containsValue(stopName)){
				if (!missingStops.contains(stopName)){
					missingStops.add(stopName);
				}
			}
		}
		
		if (!missingStops.isEmpty()){
			String missingStopsAsString = "\n";
			for (String mS : missingStops){
				missingStopsAsString += mS + "\n";
			}
			fail("StopCorrector is missing the following Stops: " + missingStopsAsString);
		}
		
	}
	
	@Test
	/**
	 * Test to see whether or not there are Stops not returned by the Transloc API, but which StopCorrector
	 * contains. This is not so much a abarson of correctness, but to easily be able see when Stops go out of season.
	 */
	public void testSeasonalStops(){
		List<String> translocStopNames = new ArrayList<String>(translocStops.size());
		for (Stop stop : translocStops){
			translocStopNames.add(stop.getName());
		}
		
		List<String> nonExistentStops = new ArrayList<String>();
		for (String stopFromCorrector : StopCorrector.getExpectedStops().values()){
			if (!translocStopNames.contains(stopFromCorrector)){
				if (!nonExistentStops.contains(stopFromCorrector)){
					nonExistentStops.add(stopFromCorrector);
				}
			}
		}
		
		if (!nonExistentStops.isEmpty()){
			String nonExistentStopsAsString = "\n";
			for (String nES : nonExistentStops){
				nonExistentStopsAsString += nES + "\n";
			}
			fail("The following Stops contained by StopCorrector are either out of season or non-existent: " + nonExistentStopsAsString);
		}
	}

}
