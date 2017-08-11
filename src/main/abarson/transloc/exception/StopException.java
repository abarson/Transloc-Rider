package abarson.transloc.exception;

/**
 * Represents an exception that occurs when validating a {@code Stop}.
 * Validation exceptions occur when the {@code Stop} exists, but is out of season, or has no shuttles actually stopping at it.
 * @author adambarson
 *
 */
public class StopException extends Exception{
	
	private static final long serialVersionUID = 1L;
	
	public StopException(Exception e){
		super(e);
	}
	public StopException(String message){
		super(message);
	}
	public StopException(String message, Exception e){
		super(message, e);
	}
}