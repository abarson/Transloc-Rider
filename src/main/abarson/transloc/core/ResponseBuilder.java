package abarson.transloc.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.rowset.Predicate;

import abarson.transloc.CatRiderSpeechlet;
import abarson.transloc.api.ArrivalMessage;
import abarson.transloc.api.Route;
import abarson.transloc.api.Stop;

/**
 * Produces responses for the different all possible scenarios the user may invoke.
 * @author adambarson
 *
 */
public final class ResponseBuilder {
	
	//arrival time response
	public static final String SHUTTLE_ARRIVING_NOW = "There is a %s shuttle arriving at %s %s";
	public static final String SHUTTLE_ARRIVING_SOON = "There is a %s shuttle arriving at %s in %s";
	public static final String LAST_TIME = ", and %s.";
	public static final String NO_SHUTTLES_COMING = "Looks like there are no shuttles coming to %s any time soon.";
	
	//route information response
	public static final String ROUTE_HAS_NO_STOPS = "The %s shuttle currently is not stopping anywhere.";
	public static final String ROUTE_STOP_LIST = "The %s shuttle stops at the following locations:\n";
	
	//stop information response
	public static final String STOP_HAS_NO_ROUTES = "There are currently no shuttles that stop at %s.";
	public static final String STOP_ROUTE_LIST = "The following shuttles stop at %s:\n";
	
	//no service response
	public static final String NO_SERVICE = "There are no shuttles currently running.";
	
	//interrupt response
	public static final String INTERRUPT = "Okay, I'll stop.";
	
	//help response
	public static final String HELP = "If you provide me with a stop, I can tell you what shuttles are coming to that stop within"
			+ " the next 30 minutes. If you provide me a shuttle and a stop, I can tell you when that particular"
			+ " shuttle is arriving at the stop you've provided. For information about which shuttles are active,"
			+ " you can say \"Alexa, ask Rider what shuttles are active.\" What stop would you like "
			+ " shuttle information for?";
	
	//welcome response
	public static final String WELCOME = "Welcome to the University of Vermont Cat Rider Skill! If you provide me with a stop, I can tell you what shuttles are coming to that stop within"
			+ " the next 30 minutes. For a list of all options, you can "
			+ " say, \"Alexa, ask Cat Rider for help.\" What stop would you like shuttle"
			+ " information for?";
	
	//api error response
	public static final String API_ERROR = "I'm having trouble connecting to the Transloc API right now. Please try again later.";
	
	//active shuttles response
	public static final String ACTIVE_ROUTES = "The %s shuttle is currently running.\n";
	
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
	public static ResponseObject getArrivalTimeResponse(List<ArrivalMessage> arrivals, List<Route> routeList, List<Stop> stopList, Stop stop){
		String speech = "";
		String stopName = stop.getName();
		
		arrivals = parseArrivals(arrivals);
		if (!arrivals.isEmpty()){
			boolean isNew = false;
			boolean isLast = false;
			String routeName = "";
			
			for (int i = 0; i < arrivals.size(); ++i){
				ArrivalMessage message = arrivals.get(i);
				
				String nextRoute = DataProcessor.getRouteNameFromID(routeList, message.getRouteId());
				
				isNew = nextRoute.equals(routeName) ? false : true;
				
				if (i < arrivals.size() - 1){
					isLast = !nextRoute.equals(DataProcessor.getRouteNameFromID(routeList, arrivals.get(i + 1).getRouteId()));
				} else {
					isLast = true;
				}
				
				
				routeName = nextRoute;
				
				if (isNew){
					if (message.getTime().equals("now")){
						speech += String.format(SHUTTLE_ARRIVING_NOW, routeName, stopName, message.getTime());
					} else {
						speech += String.format(SHUTTLE_ARRIVING_SOON, routeName, stopName, message.getTime());
					}
					speech += isLast ? ". " : "";
				} else if (isLast){
					speech += String.format(LAST_TIME, message.getTime()) + " ";
				} else {
					speech += String.format(", %s", message.getTime());
				}
			}
		} else {
			speech += String.format(NO_SHUTTLES_COMING, stopName);
		}
		return new ResponseObject(speech, speech, CatRiderSpeechlet.INVOCATION_NAME, speech);
	}
	
	//TODO: This should me moved somewhere else
	public static List<ArrivalMessage> parseArrivals(List<ArrivalMessage> arrivals){
		List<ArrivalMessage> parsedArrivals = new ArrayList<ArrivalMessage>(arrivals.size());
		for (ArrivalMessage message : arrivals){
			String arrivalTime = DataProcessor.formatTime(message.getTime());
			String differenceTime = DataProcessor.calculateArrivalTime(arrivalTime);
			if (!differenceTime.equals("")){
				message.setTime(differenceTime);
				parsedArrivals.add(message);
			}
		}
		Collections.sort(parsedArrivals);
		return parsedArrivals;
	}
	
	//TODO: Use a StringBuilder
	//TODO: Not yet used in conversation
	/**
	 * Given a route name, this will tell you all stops the route visits.
	 * @param stopList
	 * 			The {@code Stop} list returned by the Transloc API.
	 * @param routeList
	 * 			The {@code Route} list returned by the Transloc API.
	 * @return Information about a particular {@code Route}
	 */
	public static ResponseObject getRouteInformationResponse(List<Stop> stopList, List<Route> routeList, Route route) {
		String[] stopIDs = route.getStops();
		String[] stopNames = new String[stopIDs.length];
		for (int i = 0; i < stopIDs.length; ++i){
			stopNames[i] = DataProcessor.getStopNameFromID(stopList, stopIDs[i]);
		}
		
		String speech = "";
		if (stopNames.length == 0){
			speech += String.format(ROUTE_HAS_NO_STOPS, route.getLong_name());
		} else {
			speech += String.format(ROUTE_STOP_LIST, route.getLong_name());
			for (int i = 0; i < stopNames.length; ++i){
				speech += stopNames[i] + "\n";
			}
		}
		return new ResponseObject(speech, speech, CatRiderSpeechlet.INVOCATION_NAME, speech);
	}
	
	//TODO: Not yet used in conversation
	/**
	 * This will tell you what routes (shuttles) arrive at a given stop. Provides no information about estimates.
	 * @param stopList
	 * 			The {@code Stop} list returned by the Transloc API.
	 * @param routeList
	 * 			The {@code Route} list returned by the Transloc API.
	 * @return Information about a particular {@code Stop}
	 */
	public static ResponseObject getStopInformationResponse(List<Stop>stopList, List<Route>routeList, Stop stop) {
		String[] routeIDs = stop.getRoutes();
		String[] routeNames = new String[routeIDs.length];
		for (int i = 0; i < routeIDs.length; ++i){
			routeNames[i] = DataProcessor.getRouteNameFromID(routeList, routeIDs[i]);
		}
		String speech = "";
		if (routeNames.length == 0){
			speech = String.format(STOP_HAS_NO_ROUTES, stop.getName());
		} else {
			for (int i = 0; i < routeNames.length; ++i){
				if (i == 0){
					speech += String.format(STOP_ROUTE_LIST, stop.getName());
				}
				speech += routeNames[i] + "\n";
			}
		}
		return new ResponseObject(speech, speech, CatRiderSpeechlet.INVOCATION_NAME, speech);
	}
	
	public static ResponseObject getNoServiceResponse(){
		String speech = NO_SERVICE;
		return new ResponseObject(speech, speech, CatRiderSpeechlet.INVOCATION_NAME, speech);
	}
	
	public static ResponseObject getInterruptResponse(){
		String speech = INTERRUPT;
		return new ResponseObject(speech, speech, CatRiderSpeechlet.INVOCATION_NAME, speech);
	}
	public static ResponseObject getHelpResponse(){
		String speech = HELP;
		return new ResponseObject(speech, speech, CatRiderSpeechlet.INVOCATION_NAME, speech);
	}
	
	public static ResponseObject getWelcomeResponse(){
		String speech = WELCOME;
		return new ResponseObject(speech, speech, CatRiderSpeechlet.INVOCATION_NAME, speech);
	}
	
	public static ResponseObject getApiErrorResponse(){
		String speech = API_ERROR;
		return new ResponseObject(speech, speech, CatRiderSpeechlet.INVOCATION_NAME, speech);
	}
	
	//TODO: Make this sound better
	public static ResponseObject getInServiceShuttlesResponse(Map<String, Boolean> activeRoutes){
		
		if (activeRoutes.isEmpty()){
			return getNoServiceResponse();
		} 
		
		//filter out any inactive routes from the route map and put them into a list
		List<String> active = activeRoutes.entrySet()
											.stream()
											.filter(x -> x.getValue() == true)
											.map(x -> x.getKey())
											.collect(Collectors.toList());
		
		StringBuilder speech = new StringBuilder();
		
		speech.append("The ");
		speech.append(active.get(0));
		for (int i = 1; i < active.size(); ++i){
			if (i != active.size() - 1){
				speech.append(", ");
			} else {
				speech.append(", and ");
			}
			speech.append(active.get(i));
		}
		
		if (active.size() > 1){
			speech.append(" shuttles are currently in service.");
		} else {
			speech.append(" shuttle is currently in service.");
		}
		
		return new ResponseObject(speech.toString(), speech.toString(), CatRiderSpeechlet.INVOCATION_NAME, speech.toString());
	}
	
	
	@Deprecated
	/**
	 * This method will tell you what shuttles are currently in service.
	 * NOTE: There is a delay. It took about 20 minutes for the API to realize the shuttle was not active.
	 * @param routeList
	 * 			The {@code Stop} list returned by the Transloc API.
	 * @return Information about what shuttles are currently in service
	 */
	public static ResponseObject getInServiceShuttlesResponse(List<Route>routeList){
		String speech = "";
		if (routeList.isEmpty()){
			speech += "There are currently no shuttles in service.";
		} else {
			for (int i = 0; i < routeList.size(); ++i){
				if (routeList.get(i).getIs_active()){
					speech += routeList.get(i).getLong_name() + " shuttle is currently in service.\n";
				} else {
					speech += routeList.get(i).getLong_name() + " shuttle is not in service.\n";
				}
			}
		}
		return new ResponseObject(speech, speech, CatRiderSpeechlet.INVOCATION_NAME, speech);
	}
	
	/*
	public static void main(String [] args){
		Map<String, Boolean> activeRoutes = new HashMap<String, Boolean>();
		activeRoutes.put("ON CAMPUS", true);
		activeRoutes.put("REDSTONE", false);
		activeRoutes.put("OFF CAMPUS", true);
		activeRoutes.put("LATE NIGHT", false);
		
		List<String> active = activeRoutes.entrySet()
				.stream()
				.filter(x -> x.getValue() == true)
				.map(x -> x.getKey())
				.collect(Collectors.toList());
	}*/
}
