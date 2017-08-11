package abarson.transloc.api;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Encapsulates information about a {@code Stop} as it is returned by the Transloc API, 
 * with only the relevant information included. Mimics the JSON structure of of a Transloc API {@code Stop}.
 * @author adambarson
 *
 */
public class Stop {
	
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
    
	private String name;
	private String[] routes;
	private String stop_id;
	
	private Stop(){}
	
	public static Stop fromJson(String json) throws IOException{
		Stop stop = OBJECT_MAPPER.readValue(json, Stop.class);
		stop.name = stop.name.toUpperCase();
		return stop;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String[] getRoutes() {
		return routes;
	}
	public void setRoutes(String[] routes) {
		this.routes = routes;
	}
	public String getStop_id() {
		return stop_id;
	}
	public void setStop_id(String stop_id) {
		this.stop_id = stop_id;
	}
}
