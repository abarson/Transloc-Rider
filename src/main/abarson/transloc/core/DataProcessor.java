package abarson.transloc.core;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import abarson.transloc.api.ArrivalMessage;
import abarson.transloc.api.Route;
import abarson.transloc.api.Stop;
import abarson.transloc.api.TranslocApi;
import abarson.transloc.util.JsonUtils;

/**
 * This class is responsible for processing data that has been retrieved from queries made to Transloc API.
 * 
 * @author adambarson
 *
 */
public final class DataProcessor {
	
	private DataProcessor(){}
	
	private static Logger log = LoggerFactory.getLogger(DataProcessor.class);
	
	private static String CURRENT_TIME;
	
	//Create one instance of the current time because constructing Calendar instances is expensive
	public static void setTime(){
		Calendar cal = Calendar.getInstance();
		if (!JsonUtils.RUNNING_LOCALLY){
			cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) - 4); //account for UTC offset because of lambda
		}
		CURRENT_TIME = formatTime(cal.getTime().toString());
	}
	
	public static String getCurrentTime(){
		return CURRENT_TIME;
	}
	
	/**
	 * Given the current time and arrival time as Strings in the format (HH:mm:ss), get the amount of time
	 * until the shuttle arrives.
	 * @param currentTime 
	 * 			The current time of day (EDT for UVM)
	 * @param arrivalTime 
	 * 			The arrival time of the shuttle (EDT for UVM)
	 * @return The amount of time until the shuttle arrives.
	 */
	public static String calculateArrivalTime(String arrivalTime){ 
		log.info("currentTime: {}, arrivalTime: {}", CURRENT_TIME, arrivalTime);
		
		//create two arrays of Strings that contain the hour, minutes, and seconds in each index
		String[]currentTimeSplit = CURRENT_TIME.split(":");
		String[]arrivalTimeSplit = arrivalTime.split(":");
	
		int[]estimation = new int[currentTimeSplit.length];
		//TODO: add some exception handling somewhere
		for (int i = currentTimeSplit.length - 1; i >= 0; i--){ 
			//convert the Strings into integers and find the difference between them
			int cur = Integer.parseInt(currentTimeSplit[i]);
			int arr = Integer.parseInt(arrivalTimeSplit[i]);
			
			if (i == 0 && cur > arr){
				arr += 24;
			}
			int dif = arr - cur;
			//if this results in a negative number, we need to "borrow" from the next index
			if (dif < 0){
				int next = Integer.parseInt(arrivalTimeSplit[i - 1]) - 1;
				arrivalTimeSplit[i - 1] = next+"";
				dif = 60 + dif;
			}
			estimation[i] = dif;
		}
		
		String theTime = "";
		if (estimation[0] > 0){ //if arrival time is over an hour
			theTime = "";
		} else { //otherwise, format a String from the minutes and seconds
			int minutes = estimation[1];
			if (minutes > 30){
				theTime = ""; //ignore anything above 30 minutes
			} else if (minutes < 1){
				theTime = "now";
			} else if (minutes < 2){
				theTime = "under 2 minutes";
			}
			else {
				theTime += minutes + " minutes";
			}
		}
		return theTime;
	}

	public static String formatTime(String time){
		return time.substring(11, 19);
	}
	
	/* This method is EXTREMELY inefficient and needs to be optimized.
	 * Currently iterates through every arrival prediction for every stop at UVM.
	 * There is definitely a more optimal way to do this.
	 */
	//TODO: optimize
	public static Map<String, Boolean> getActiveRouteMap(List<Route> routeList) throws IOException, JSONException{
		Map<String, Boolean> activeRoutes = new HashMap<String, Boolean>();
		List<ArrivalMessage> arrivalMessages = TranslocApi.getArrivalTimes("");
		
		
		for (ArrivalMessage message : arrivalMessages){
			String routeName = getRouteNameFromID(routeList, message.getRouteId());
			activeRoutes.put(routeName, true);
			if (activeRoutes.size() == routeList.size()){ //this means all shuttles are currently active and have been accounted for, so we do not need to continue.
				break;
			}
		}
		
		if (!activeRoutes.isEmpty()){
			for (Route route : routeList){
				String routeName = route.getLong_name();
				if (!activeRoutes.containsKey(routeName)){
					activeRoutes.put(route.getLong_name(), false);
				}
			}
		}
		return activeRoutes;
	}
	
	/**
	 * Helper method to convert the ID of a {@code Stop} to its name.
	 * @param stopList 
	 * 			The {@code Stop} list returned by Transloc API
	 * @param ID 
	 * 			The ID of the {@code Stop}.
	 * @return The name of the {@code Stop}.
	 */
	public static String getStopNameFromID(List<Stop> stopList, String ID){
		for (Stop s : stopList){
			if (s.getStop_id().equals(ID)){
				return s.getName();
			}
		}
		return "";
	}
	
	/**
	 * Helper method to convert the ID of a {@code Route} to its name.
	 * @param routeList 
	 * 			The {@code Route} list returned by Transloc API
	 * @param ID 
	 * 			The ID of the {@code Route}.
	 * @return The name of the {@code Route}.
	 */
	public static String getRouteNameFromID(List<Route> routeList, String ID){
		for (Route r : routeList){
			if (r.getRoute_id().equals(ID)){
				return r.getLong_name();
			}
		}
		return "";
	}
	
		
}
