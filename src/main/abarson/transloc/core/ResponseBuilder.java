package abarson.transloc.core;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import abarson.transloc.api.ArrivalMessage;
import abarson.transloc.api.Route;
import abarson.transloc.api.Stop;

/**
 * Produces responses for the different all possible scenarios the user may invoke.
 * @author adambarson
 *
 */
public final class ResponseBuilder {
	
	private ResponseBuilder(){}
	
	/**
	 * Formats the data of a {@code ArrivalMessage} list to be presented to the user.
	 * @param arrivals
	 * 			The {@code ArrivalMessage} list returned by Transloc API
	 * @param stopList
	 * 			The {@code Stop} list returned by the Transloc API.
	 * @param routeList
	 * 			The {@code Route} list returned by the Transloc API.
	 * @param stop
	 * 			The {@code Stop} that the user has requested arrival information for.
	 * @return Information about arrival times at a particular {@code Stop}.
	 */
	public static String getArrivalTimeResponse(List<ArrivalMessage> arrivals, List<Route> routeList, List<Stop> stopList, Stop stop){
		String response = "";
		String stopName = stop.getName();
		if (!arrivals.isEmpty()){
			boolean isNew = false;
			boolean isLast = false;
			String routeName = "";
			for (ArrivalMessage message : arrivals){
				String nextRoute = DataProcessor.getRouteNameFromID(routeList, message.getRouteId());
				
				isNew = nextRoute.equals(routeName) ? false : true;
				isLast = message == arrivals.get(arrivals.size() - 1);
				
				routeName = nextRoute;
				
				String arrivalTime = DataProcessor.formatTime(message.getTime());
				String currentTime = DataProcessor.formatTime(Calendar.getInstance().getTime().toString());
				String differenceTime = DataProcessor.calculateArrivalTime(currentTime, arrivalTime);
				
				if (isNew){
					response += String.format("\nThere is a %s shuttle arriving at %s in %s minutes", routeName, stopName, differenceTime);
					response += isLast ? "." : "";
				} else if (isLast){
					response += String.format(", and %s minutes.", differenceTime);
				} else {
					response += String.format(", %s minutes", differenceTime);
				}
			}
		} else {
			response += String.format("Looks like there are no shuttles coming to %s any time soon.", stopName);
		}
		return response;
	}
	
	/**
	 * Given a route name, this will tell you all stops the route visits.
	 * @param stopList
	 * 			The {@code Stop} list returned by the Transloc API.
	 * @param routeList
	 * 			The {@code Route} list returned by the Transloc API.
	 * @return Information about a particular {@code Route}
	 */
	public static String getRouteInformationResponse(List<Stop> stopList, List<Route> routeList, Route route) {
		String[] stopIDs = route.getStops();
		String[] stopNames = new String[stopIDs.length];
		for (int i = 0; i < stopIDs.length; ++i){
			stopNames[i] = DataProcessor.getStopNameFromID(stopList, stopIDs[i]);
		}
		
		String response = "";
		if (stopNames.length == 0){
			response += route.getLong_name() + " currently is not stopping anywhere.";
		} else {
			response += "The " + route.getLong_name() + " shuttle stops at the following locations:\n";
			for (int i = 0; i < stopNames.length; ++i){
				response += stopNames[i] + "\n";
			}
		}
		return response;
	}
	
	/**
	 * This will tell you what routes (shuttles) arrive at a given stop. Provides no information about estimates.
	 * @param stopList
	 * 			The {@code Stop} list returned by the Transloc API.
	 * @param routeList
	 * 			The {@code Route} list returned by the Transloc API.
	 * @return Information about a particular {@code Stop}
	 */
	public static String getStopInformationResponse(List<Stop>stopList, List<Route>routeList, Stop stop) {
		String[] routeIDs = stop.getRoutes();
		String[] routeNames = new String[routeIDs.length];
		for (int i = 0; i < routeIDs.length; ++i){
			routeNames[i] = DataProcessor.getRouteNameFromID(routeList, routeIDs[i]);
		}
		String response = "";
		if (routeNames.length == 0){
			response = "There are currently no shuttles that stop at " + stop.getName() + ".";
		} else {
			for (int i = 0; i < routeNames.length; ++i){
				if (i == 0){
					response += "The following shuttles stop at " + stop.getName() + ":\n";
				}
				response += routeNames[i] + "\n";
			}
		}
		return response;
	}
	
	public static String getInServiceShuttlesResponse(Map<String, Boolean> activeRoutes){
		String response = "";
		if (activeRoutes.isEmpty()){
			response += "There are currently no shuttles in service.";
		} else {
			for (String routeName : activeRoutes.keySet()){
				if (activeRoutes.get(routeName)){
					response += "The " + routeName + " is currently running.";
				}
			}
		}
		return response;
	}
	
	@Deprecated
	/**
	 * This method will tell you what shuttles are currently in service.
	 * NOTE: There is a delay. It took about 20 minutes for the API to realize the shuttle was not active.
	 * @param routeList
	 * 			The {@code Stop} list returned by the Transloc API.
	 * @return Information about what shuttles are currently in service
	 */
	public static String getInServiceShuttlesResponse(List<Route>routeList){
		String response = "";
		if (routeList.isEmpty()){
			response += "There are currently no shuttles in service.";
		} else {
			for (int i = 0; i < routeList.size(); ++i){
				if (routeList.get(i).getIs_active()){
					response += routeList.get(i).getLong_name() + " shuttle is currently in service.\n";
				} else {
					response += routeList.get(i).getLong_name() + " shuttle is not in service.\n";
				}
			}
		}
		return response;
	}
}
