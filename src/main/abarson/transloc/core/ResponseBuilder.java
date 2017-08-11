package abarson.transloc.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import abarson.transloc.RiderSpeechlet;
import abarson.transloc.api.ArrivalMessage;
import abarson.transloc.api.Route;
import abarson.transloc.api.Stop;
import abarson.transloc.util.JsonUtils;

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
	public static ResponseObject getArrivalTimeResponse(List<ArrivalMessage> arrivals, List<Route> routeList, List<Stop> stopList, Stop stop){
		String speech = "";
		String stopName = stop.getName();
		
		arrivals = parseArrivals(arrivals);
		if (!arrivals.isEmpty()){
			boolean isNew = false;
			boolean isLast = false;
			String routeName = "";
			for (ArrivalMessage message : arrivals){
				String nextRoute = DataProcessor.getRouteNameFromID(routeList, message.getRouteId());
				
				isNew = nextRoute.equals(routeName) ? false : true;
				isLast = message == arrivals.get(arrivals.size() - 1);
				
				routeName = nextRoute;
				
				/*String arrivalTime = DataProcessor.formatTime(message.getTime());
				
				Calendar cal = Calendar.getInstance();
				if (!JsonUtils.RUNNING_LOCALLY){
					cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) - 4); //account for UTC offset because of lambda
				}
				String currentTime = DataProcessor.formatTime(cal.getTime().toString());
				String differenceTime = DataProcessor.calculateArrivalTime(currentTime, arrivalTime);*/
				
				if (isNew){
					if (message.getTime().equals("now")){
						speech += String.format("There is a %s shuttle arriving at %s %s", routeName, stopName, message.getTime());
					} else {
						speech += String.format("There is a %s shuttle arriving at %s in %s", routeName, stopName, message.getTime());
					}
					speech += isLast ? "." : "";
				} else if (isLast){
					speech += String.format(", and %s.", message.getTime());
				} else {
					speech += String.format(", %s", message.getTime());
				}
			}
		} else {
			speech += String.format("Looks like there are no shuttles coming to %s any time soon.", stopName);
		}
		return new ResponseObject(speech, speech, RiderSpeechlet.INVOCATION_NAME, speech);
	}
	
	public static List<ArrivalMessage> parseArrivals(List<ArrivalMessage> arrivals){
		List<ArrivalMessage> parsedArrivals = new ArrayList<ArrivalMessage>(arrivals.size());
		for (ArrivalMessage message : arrivals){
			String arrivalTime = DataProcessor.formatTime(message.getTime());
			
			Calendar cal = Calendar.getInstance();
			if (!JsonUtils.RUNNING_LOCALLY){
				cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) - 4); //account for UTC offset because of lambda
			}
			String currentTime = DataProcessor.formatTime(cal.getTime().toString());
			String differenceTime = DataProcessor.calculateArrivalTime(currentTime, arrivalTime);
			if (!differenceTime.equals("")){
				message.setTime(differenceTime);
				parsedArrivals.add(message);
			}
		}
		return parsedArrivals;
	}
	
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
			speech += route.getLong_name() + " currently is not stopping anywhere.";
		} else {
			speech += "The " + route.getLong_name() + " shuttle stops at the following locations:\n";
			for (int i = 0; i < stopNames.length; ++i){
				speech += stopNames[i] + "\n";
			}
		}
		return new ResponseObject(speech, speech, RiderSpeechlet.INVOCATION_NAME, speech);
	}
	
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
			speech = "There are currently no shuttles that stop at " + stop.getName() + ".";
		} else {
			for (int i = 0; i < routeNames.length; ++i){
				if (i == 0){
					speech += "The following shuttles stop at " + stop.getName() + ":\n";
				}
				speech += routeNames[i] + "\n";
			}
		}
		return new ResponseObject(speech, speech, RiderSpeechlet.INVOCATION_NAME, speech);
	}
	
	public static ResponseObject getNoServiceResponse(){
		String speech = "There are no shuttles currently running.";
		return new ResponseObject(speech, speech, RiderSpeechlet.INVOCATION_NAME, speech);
	}
	
	public static ResponseObject getStopResponse(){
		String speech = "Okay, I'll stop.";
		return new ResponseObject(speech, speech, RiderSpeechlet.INVOCATION_NAME, speech);
	}
	public static ResponseObject getHelpResponse(){
		String speech = "If you provide me with a stop, I can tell you what shuttles are coming to that stop within"
				+ " the next 30 minutes. If you provide me a shuttle and a stop, I can tell you when that particular"
				+ " shuttle is arriving at the stop you've provided. For information about which shuttles are active,"
				+ " you can say \"Alexa, ask Rider what shuttles are active.\" What stop would you like "
				+ " shuttle information for?";
		return new ResponseObject(speech, speech, RiderSpeechlet.INVOCATION_NAME, speech);
	}
	
	public static ResponseObject getWelcomeResponse(){
		String speech = "Welcome to the University of Vermont Rider Skill! If you provide me with a stop, I can tell you what shuttles are coming to that stop within"
				+ " the next 30 minutes. For a list of all options, you can "
				+ " say, \"Alexa, ask Rider for help.\" What stop would you like shuttle"
				+ " information for?";
		return new ResponseObject(speech, speech, RiderSpeechlet.INVOCATION_NAME, speech);
	}
	
	public static ResponseObject getApiErrorResponse(){
		String speech = "I'm having trouble connecting to the Transloc API right now. Please try again later.";
		return new ResponseObject(speech, speech, RiderSpeechlet.INVOCATION_NAME, speech);
	}
	
	public static ResponseObject getInServiceShuttlesResponse(Map<String, Boolean> activeRoutes){
		
		if (activeRoutes.isEmpty()){
			return getNoServiceResponse();
		} 
		
		String speech = "";
		for (String routeName : activeRoutes.keySet()){
			if (activeRoutes.get(routeName)){
				speech += "The " + routeName + " shuttle is currently running.\n";
			}
		}
		
		return new ResponseObject(speech, speech, RiderSpeechlet.INVOCATION_NAME, speech);
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
		return new ResponseObject(speech, speech, RiderSpeechlet.INVOCATION_NAME, speech);
	}
}
