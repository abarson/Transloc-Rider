package abarson.transloc;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONException;

import abarson.transloc.api.ArrivalMessage;
import abarson.transloc.api.Route;
import abarson.transloc.api.Stop;
import abarson.transloc.api.TranslocApi;
import abarson.transloc.core.DataProcessor;
import abarson.transloc.core.ResponseBuilder;
import abarson.transloc.exception.InvalidInputException;
import abarson.transloc.exception.RouteException;
import abarson.transloc.exception.StopException;
import abarson.transloc.validation.RouteCorrector;
import abarson.transloc.validation.StopCorrector;
import abarson.transloc.validation.Validator;

/**
 * Driver that let's you use the Transloc App as if it were a command line tool.
 * @author adambarson
 *
 */
public class Driver {
	public static void main(String [] args) {
		List<Stop> stopList;
		List<Route> routeList;
		Map<String, Boolean> activeRoutes;
		
		try {
			stopList = TranslocApi.getStops();
			routeList = TranslocApi.getRoutes();
			activeRoutes = DataProcessor.getActiveRouteMap(routeList);
			if (activeRoutes.isEmpty()){
				System.out.println("There are no shuttles currently running. You can, however, still use options 3-5.");
			} 
			
			
			
		} catch (JSONException | IOException e) {
			System.out.println("Unable to connect to Transloc API at this time. " + e.getMessage());
			return;
		} 
		
		menu();
		Scanner reader = new Scanner(System.in);
		String response = reader.nextLine();
		while (!response.equals("6")){
			String stopName, routeName, output = "";
			List<ArrivalMessage> arrivals;
			Stop stop;
			Route route;
			try {
				switch(response){
				case("1"):
					if (activeRoutes.isEmpty()){
						output = "There are no shuttles currently active.";
						break;
					}
				
					System.out.println("What is your stop?");
					stopName = reader.nextLine();
					stopName = StopCorrector.correctStop(stopName);
					
					//if there are no shuttles coming to the stop in question, this throws an exception
					stop = Validator.validateStop(stopList, stopName);
					
					arrivals = TranslocApi.getArrivalTimes(stop.getStop_id());
					output = ResponseBuilder.getArrivalTimeResponse(arrivals, routeList, stopList, stop).getTextSpeech();
					break;
				case("2"):
					if (activeRoutes.isEmpty()){
						output = "There are no shuttles currently active.";
						break;
					}
					
					
					System.out.println("What is your stop?");
					stopName = reader.nextLine();
					stopName = StopCorrector.correctStop(stopName);
					
					//if there are no shuttles coming to the stop in question, this throws an exception
					stop = Validator.validateStop(stopList, stopName);
					
					System.out.println("What is your route?");
					routeName = reader.nextLine();
					routeName = RouteCorrector.correctRoute(routeName);
					
					String feedback = "";
					//try to validate the route to see if it is compatible with the stop.
					//if it is not, ignore the route and just get all predictions for the stop.
					try {
						route = Validator.validateRouteAndStop(routeList, routeName, stop, activeRoutes);
						
						arrivals = TranslocApi.getArrivalTimes(route.getRoute_id(), stop.getStop_id());
						if (arrivals.isEmpty() && !routeName.equals("")){
							arrivals = TranslocApi.getArrivalTimes(stop.getStop_id());
							if (!arrivals.isEmpty()){
								feedback = "There are no " + routeName + " shuttles coming to " + stopName + " any time soon. However, ";
							}
						}
					} catch (RouteException e){
						feedback = e.getMessage() + " But, ";
						arrivals = TranslocApi.getArrivalTimes(stop.getStop_id());
					} 
					
					
			
					output = feedback + "\n" + ResponseBuilder.getArrivalTimeResponse(arrivals, routeList, stopList, stop).getTextSpeech();
					break;
				case("3"):
					System.out.println("What is your route?");
					routeName = reader.nextLine();
					routeName = RouteCorrector.correctRoute(routeName);
					
					route = Validator.fetchRoute(routeList, routeName);
					
					output = ResponseBuilder.getRouteInformationResponse(stopList, routeList, route).getTextSpeech();
					break;
				case("4"):
					System.out.println("What is your stop?");
					stopName = reader.nextLine();
					stopName = StopCorrector.correctStop(stopName);
					
					//if there are no shuttles coming to the stop in question, this throws an exception
					stop = Validator.validateStop(stopList, stopName);
					
					output = ResponseBuilder.getStopInformationResponse(stopList, routeList, stop).getTextSpeech();
					break;
				case("5"):
					output = ResponseBuilder.getInServiceShuttlesResponse(activeRoutes).getTextSpeech();
					break;
				} 
				
			} catch (JSONException | IOException | RouteException | StopException | InvalidInputException e){
				output = e.getMessage();
			} 
			System.out.println(output);
			menu();
			response = reader.nextLine();
		}
		reader.close();
	}
	
	public static void menu(){
		System.out.println("\n1: Request arrival information for a particular stop.");
		System.out.println("2: Request arrival information for a particular stop and shuttle.");
		System.out.println("3: Request information about where a particular shuttle stops.");
		System.out.println("4: Request information for which shuttles visit a particular stop.");
		System.out.println("5: Request information for which shuttles are currently in service.");
		System.out.println("6: Quit the app.\n");
	}
	
	
	
	
}
