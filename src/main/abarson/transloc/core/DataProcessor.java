package abarson.transloc.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import abarson.transloc.api.ArrivalMessage;
import abarson.transloc.api.Route;
import abarson.transloc.api.Stop;
import abarson.transloc.api.TranslocApi;

/**
 * This class is responsible for processing data that has been retrieved from queries made to Transloc API.
 * 
 * @author adambarson
 *
 */
public final class DataProcessor {
	
	private DataProcessor(){}
	
	/**
	 * Given the current time and arrival time as Strings in the format (HH:mm:ss), get the amount of time
	 * until the shuttle arrives.
	 * @param currentTime 
	 * 			The current time of day (EDT for UVM)
	 * @param arrivalTime 
	 * 			The arrival time of the shuttle (EDT for UVM)
	 * @return The amount of time until the shuttle arrives.
	 */
	public static String calculateArrivalTime(String currentTime, String arrivalTime){ 
		//create two arrays of Strings that contain the hour, minutes, and seconds in each index
		String[]currentTimeSplit = currentTime.split(":");
		String[]arrivalTimeSplit = arrivalTime.split(":");
		int[]estimation = new int[currentTimeSplit.length];
		//TODO: add some exception handling somewhere
		for (int i = currentTimeSplit.length - 1; i >= 0; i--){ 
			//convert the Strings into integers and find the difference between them
			int cur = Integer.parseInt(currentTimeSplit[i]);
			int arr = Integer.parseInt(arrivalTimeSplit[i]);
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
			theTime = "Over an hour!";
		} else { //otherwise, format a String from the minutes and seconds
			int minutes = estimation[1];
			int seconds = estimation[2];
			if (minutes < 10){
				theTime += "0" + minutes + ":";
			} else {
				theTime += minutes +":";
			}
			if (seconds < 10){
				theTime += "0" + seconds;
			} else {
				theTime += seconds + "";
			}
		}
		return theTime;
	}

	public static String formatTime(String time){
		return time.substring(11, 19);
	}
	
	public static Map<String, Boolean> getActiveRouteMap(List<Route> routeList) throws IOException, JSONException{
		Map<String, Boolean> activeRoutes = new HashMap<String, Boolean>();
		List<ArrivalMessage> arrivalMessages = TranslocApi.getArrivalTimes("","");
		
		
		for (ArrivalMessage message : arrivalMessages){
			String routeName = getRouteNameFromID(routeList, message.getRouteId()).toUpperCase();
			activeRoutes.put(routeName, true);
		}
		
		if (!activeRoutes.isEmpty()){
			for (Route route : routeList){
				String routeName = route.getLong_name().toUpperCase();
				if (!activeRoutes.containsKey(routeName)){
					activeRoutes.put(route.getLong_name().toUpperCase(), false);
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
