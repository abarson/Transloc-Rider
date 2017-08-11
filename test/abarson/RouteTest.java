package abarson;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;

import abarson.transloc.api.Route;
import abarson.transloc.api.TranslocApi;
import abarson.transloc.validation.RouteCorrector;

public class RouteTest {
	
	private static List<Route> translocRoutes;
	
	@BeforeClass
	public static void initialize(){
		try {
			translocRoutes = TranslocApi.getRoutes();
		} catch (JSONException | IOException e){
			fail("Failed to connect to Transloc API: " + e.getMessage());
		}
	}
	
	@Test
	/**
	 * Test to ensure that RouteCorrector encapsulates all Routes returned by Transloc API.
	 * This will help to ensure that the route list is up to date, and help stay on top of
	 * seasonal routes being changed.
	 */
	public void testValidateRoute(){
		List<String> missingRoutes = new ArrayList<String>();
		for (Route route : translocRoutes){
			String routeName = route.getLong_name();
			if (!RouteCorrector.getExpectedRoutes().containsValue(routeName)){
				missingRoutes.add(routeName);
			}
		}
		
		if (!missingRoutes.isEmpty()){
			String missingRoutesAsString = "\n";
			for (String mR : missingRoutes){
				missingRoutesAsString += mR + "\n";
			}
			fail("RouteCorrector is missing the following Routes: " + missingRoutesAsString);
		}
	}
	
	@Test
	/**
	 * Test to see whether or not there are Routes not returned by the Transloc API, but which RouteCorrector
	 * contains. This is not so much a abarson of correctness, but to easily be able see when Routes go out of season.
	 */
	public void testSeasonalRoutes(){
		List<String> translocRouteNames = new ArrayList<String>(translocRoutes.size());
		for (Route route : translocRoutes){
			translocRouteNames.add(route.getLong_name());
		}
		
		List<String> nonExistentRoutes = new ArrayList<String>();
		for (String routeFromCorrector : RouteCorrector.getExpectedRoutes().values()){
			if (!translocRouteNames.contains(routeFromCorrector)){
				nonExistentRoutes.add(routeFromCorrector);
			}
		}
		
		if (!nonExistentRoutes.isEmpty()){
			String nonExistentRoutesAsString = "\n";
			for (String nER : nonExistentRoutes){
				nonExistentRoutesAsString += nER + "\n";
			}
			fail("The following Routes contained by RouteCorrector are either out of season or non-existent: " + nonExistentRoutesAsString);
		}
	}
	
	
}
