package abarson.transloc.exception;

/**
 * Represents an exception that occurs when the user provides an invalid input,
 * i.e. one that neither {@link RouteCorrector} not {@link StopCorrector} can resolve.
 * @author adambarson
 *
 */
public class InvalidInputException extends Exception{
	private static final long serialVersionUID = 1L;
	
	public InvalidInputException(Exception e){
		super(e);
	}
	public InvalidInputException(String message){
		super(message);
	}
	public InvalidInputException(String message, Exception e){
		super(message, e);
	}
}
