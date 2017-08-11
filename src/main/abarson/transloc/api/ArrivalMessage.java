package abarson.transloc.api;

/**
 * Encapsulates information returned by an arrival time query to the Transloc API.
 * 
 * @author adambarson
 *
 */
public class ArrivalMessage {
	private String routeId;
	private String stopId;
	private String time;
	
	public ArrivalMessage(String routeId, String stopId, String time){
		this.routeId = routeId;
		this.stopId = stopId;
		this.time = time;
	}
	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getRouteId() {
		return routeId;
	}
	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}
	public String getStopId() {
		return stopId;
	}
	public void setStopId(String stopId) {
		this.stopId = stopId;
	}
}
