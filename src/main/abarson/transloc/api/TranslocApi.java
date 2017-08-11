package abarson.transloc.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import abarson.transloc.util.JsonUtils;

/**
 * Manages making requests to the Transloc API. 
 * @author adambarson
 *
 */
public final class TranslocApi {
	public static final String urlRoot = "https://transloc-api-1-2.p.mashape.com/%s.json?agencies=603&callback=call";
	public static final String routeParameter = "&routes=%s";
	public static final String stopParameter = "&stops=%s";
	
	private TranslocApi(){}
	
	/**
	 * Checks to see whether there are any predictions without providing parameters. If there are not, that means there are no shuttles in service.
	 * @return True if there are no shuttles in service.
	 * @throws IOException
	 * @throws JSONException
	 */
	public static boolean noService() throws IOException, JSONException{
		List<ArrivalMessage> arrivals = getArrivalTimes("", "");
		return (arrivals.isEmpty());
	}
	
	
	/**
	 * Given a routeID (like Summer Route) and a stopID (like WDW), gets a list of arrival times.
	 * (e.g. When is the next Summer Route shuttle coming to WDW?)
	 * @param routeID The ID of the route
	 * @param stopID The ID of the stop
	 * @return An {@link ArrivalMessage} list
	 * @throws IOException
	 * @throws JSONException
	 */
	public static List<ArrivalMessage> getArrivalTimes(String routeId, String stopId) throws IOException, JSONException{
		String arrivalUrl = String.format(urlRoot, "arrival-estimates") + String.format(routeParameter + stopParameter, routeId, stopId);
		JSONArray arrivalsJson = JsonUtils.readJsonFromUrl(arrivalUrl).getJSONArray("data");
		List<ArrivalMessage> arrivals = new ArrayList<ArrivalMessage>();
		for (int i = 0; i < arrivalsJson.length(); ++i){
			JSONObject arrivalInfo = arrivalsJson.getJSONObject(i);
			JSONArray arrivalTime = arrivalInfo.getJSONArray("arrivals");
			for (int j = 0; j < arrivalTime.length(); ++j){
				String time = arrivalTime.getJSONObject(j).getString("arrival_at");
				arrivals.add(new ArrivalMessage(routeId, stopId, time));
			}
		}
		return arrivals;
	}
	
	/**
	 * Given only a stopID (like WDW), gets a list of arrival times.
	 * (e.g. When is the next shuttle coming to WDW?)
	 * @param routeID The ID of the route
	 * @param stopID The ID of the stop
	 * @return An {@link ArrivalMessage} list
	 * @throws IOException
	 * @throws JSONException
	 */
	public static List<ArrivalMessage> getArrivalTimes(String stopId) throws IOException, JSONException{
		String arrivalUrl = String.format(urlRoot, "arrival-estimates") + String.format(stopParameter, stopId);
		JSONArray arrivalsJson = JsonUtils.readJsonFromUrl(arrivalUrl).getJSONArray("data");
		List<ArrivalMessage> arrivals = new ArrayList<ArrivalMessage>();
		for (int i = 0; i < arrivalsJson.length(); ++i){
			JSONObject arrivalInfo = arrivalsJson.getJSONObject(i);
			JSONArray arrivalTime = arrivalInfo.getJSONArray("arrivals");
			for (int j = 0; j < arrivalTime.length(); ++j){
				String time = arrivalTime.getJSONObject(j).getString("arrival_at");
				String routeId = arrivalTime.getJSONObject(j).getString("route_id");
				arrivals.add(new ArrivalMessage(routeId, stopId, time));
			}
		}
		return arrivals;
	}
	
	/**
	 * Gets all available stops from the Transloc API.
	 * @return A {@code Stop} list returned by Transloc API
	 * @throws JSONException
	 * @throws IOException
	 */
	public static List<Stop> getStops() throws JSONException, IOException{
		String stopsUrl = String.format(urlRoot, "stops");
		JSONObject stopsJson = JsonUtils.readJsonFromUrl(stopsUrl);
		List<Stop> stopList = new ArrayList<Stop>();
		JSONArray stops = stopsJson.getJSONArray("data");
		for (int i = 0; i < stops.length(); ++i){
			stopList.add(Stop.fromJson(stops.getString(i)));
			
		}
		return stopList;
	}
	
	/**
	 * Gets all available routes (shuttles) from the Transloc API.
	 * @return A {@code Route} list returned by Transloc API
	 * @throws JSONException
	 * @throws IOException
	 */
	public static List<Route> getRoutes() throws JSONException, IOException{
		String routesUrl = String.format(urlRoot, "routes");
		JSONObject routesJson = JsonUtils.readJsonFromUrl(routesUrl);
		List<Route> routeList = new ArrayList<Route>();
		JSONArray routes = routesJson.getJSONObject("data").getJSONArray("603");
		for (int i = 0; i < routes.length(); ++i){
			routeList.add(Route.fromJson(routes.getString(i)));
		}
		return routeList;
	}
	
}
