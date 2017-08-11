package abarson.transloc.api;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Encapsulates information about a {@code Route} as it is returned by the Transloc API, 
 * with only the relevant information included. Mimics the JSON structure of of a Transloc API {@code Route}.
 * @author adambarson
 *
 */
public class Route {
	
	/**
     * A Jackson {@code ObjectMapper} configured for our deserialization use case.
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
    static {
        /*
         * This flag is set to allow unknown fields to be skipped, instead of throwing an exception.
         */
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    }
    
	private String route_id;
	private String long_name;
	private String[] stops;
	private boolean is_active;
	
	private Route(){}
	
	public static Route fromJson(String json) throws IOException{
		Route route = OBJECT_MAPPER.readValue(json, Route.class);
		route.long_name = route.long_name.toUpperCase();
		return route;
	}
	
	public String getRoute_id() {
		return route_id;
	}
	public void setRoute_id(String route_id) {
		this.route_id = route_id;
	}
	public String getLong_name() {
		return long_name;
	}
	public void setLong_name(String long_name) {
		this.long_name = long_name;
	}
	public String[] getStops() {
		return stops;
	}
	public void setStops(String[] stops) {
		this.stops = stops;
	}
	public boolean getIs_active() {
		return is_active;
	}
	public void setIs_active(boolean is_active) {
		this.is_active = is_active;
	}
}
