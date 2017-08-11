package abarson.transloc.exception;

/**
 * Represents an exception that occurs when validating a {@code Route}.
 * Validation exceptions occur when the {@code Route} exists, but is out of season.
 * @author adambarson
 *
 */
public class RouteException extends Exception{
	
	private static final long serialVersionUID = 1L;
	
	public RouteException(Exception e){
		super(e);
	}
	public RouteException(String message){
		super(message);
	}
	public RouteException(String message, Exception e){
		super(message, e);
	}
}
