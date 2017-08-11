package abarson.transloc;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.ui.Card;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;

import abarson.transloc.api.ArrivalMessage;
import abarson.transloc.api.Route;
import abarson.transloc.api.Stop;
import abarson.transloc.api.TranslocApi;
import abarson.transloc.core.DataProcessor;
import abarson.transloc.core.ResponseBuilder;
import abarson.transloc.core.ResponseObject;
import abarson.transloc.exception.InvalidInputException;
import abarson.transloc.exception.RouteException;
import abarson.transloc.exception.StopException;
import abarson.transloc.validation.RouteCorrector;
import abarson.transloc.validation.StopCorrector;
import abarson.transloc.validation.Validator;

public class RiderSpeechlet implements SpeechletV2{

	public static final String INVOCATION_NAME = "Rider";
	
	private static Logger log = LoggerFactory.getLogger(RiderSpeechlet.class);
	
	private List<Route> routeList;
	private List<Stop> stopList;
	private List<ArrivalMessage> arrivals;
	private Map<String, Boolean> activeRoutes;
	
	@Override
	public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
		Session session = requestEnvelope.getSession();
		SessionStartedRequest request = requestEnvelope.getRequest();
		log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
			    session.getSessionId());
		
		
		
	}

	@Override
	public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
		Session session = requestEnvelope.getSession();
		LaunchRequest request = requestEnvelope.getRequest();
		log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
			    session.getSessionId());
		
		
		ResponseObject response = ResponseBuilder.getWelcomeResponse();
		return newAskResponse(response.getSsmlSpeech(), "What stop would you like shuttle information for?");
	}

	@Override
	public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
		Session session = requestEnvelope.getSession();
		IntentRequest request = requestEnvelope.getRequest();
		log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
			    session.getSessionId());
		
		
		ResponseObject output;
		//initialize route and stop list
		try {
			routeList = TranslocApi.getRoutes();
			stopList = TranslocApi.getStops();
			activeRoutes = DataProcessor.getActiveRouteMap(routeList);
		} catch (JSONException | IOException e) {
			log.error(e.getMessage());
			output = ResponseBuilder.getApiErrorResponse();
			return newTellResponse(output.getSsmlSpeech());
		}
		
		Intent intent = request.getIntent();
		
		PlainTextOutputSpeech speech;
		String feedback = "";
		try {
		switch (intent.getName()){
		case "GetArrivalsIntent":
			if (activeRoutes.isEmpty()){
				output = ResponseBuilder.getNoServiceResponse();
				return newTellResponse(output.getSsmlSpeech(), buildCard(output.getCardTitle(), output.getCardText()));
			}
			
			String routeName = getValueFromSlot(intent, "Route");
			String stopName = getValueFromSlot(intent, "Stop");
			
			if (stopName == null){
				stopName = "";
			}
			
			//if the stopName is invalid, this throws an exception
			stopName = StopCorrector.correctStop(stopName);
			//if there are no shuttles coming to the stop in question, this throws an exception
			Stop stop = Validator.validateStop(stopList, stopName);
			
			if (routeName != null){
				
				try {
					//if the routeName is invalid, this throws an exception
					routeName = RouteCorrector.correctRoute(routeName);
					//try to validate the route to see if it is compatible with the stop.
					//if it is not, ignore the route and just get all predictions for the stop.
					Route route = Validator.validateRouteAndStop(routeList, routeName, stop, activeRoutes);
					arrivals = TranslocApi.getArrivalTimes(route.getRoute_id(), stop.getStop_id());
					if (arrivals.isEmpty()){
						arrivals = TranslocApi.getArrivalTimes(stop.getStop_id());
						if (!arrivals.isEmpty()){
							feedback = "There are no " + routeName + " shuttles coming to " + stopName + " any time soon. However, ";
						}
					}
				} catch (InvalidInputException | RouteException e){
					feedback = e.getMessage() + " ";
					arrivals = TranslocApi.getArrivalTimes(stop.getStop_id());
					if (!arrivals.isEmpty()){
						feedback += "However, ";
					}
				} 
			} else { //if the user didn't provide a route, just generate predictions for all routes coming to this stop
				arrivals = TranslocApi.getArrivalTimes(stop.getStop_id());
			}
			output = ResponseBuilder.getArrivalTimeResponse(arrivals, routeList, stopList, stop);
			return newTellResponse(feedback + output.getSsmlSpeech(), buildCard(output.getCardTitle(), output.getCardText()));
		case "ActiveRoutesIntent":
			output = ResponseBuilder.getInServiceShuttlesResponse(activeRoutes);
			return newTellResponse(output.getSsmlSpeech(), buildCard(output.getCardTitle(), output.getCardText()));
		case "AMAZON.StopIntent":
			output = ResponseBuilder.getStopResponse();
			return newTellResponse(output.getSsmlSpeech(), buildCard(output.getCardTitle(), output.getCardText()));
		case "AMAZON.CancelIntent":
			output = ResponseBuilder.getStopResponse();
			return newTellResponse(output.getSsmlSpeech(), buildCard(output.getCardTitle(), output.getCardText()));
		case "AMAZON.HelpIntent":
			output = ResponseBuilder.getHelpResponse();
			return newAskResponse(output.getSsmlSpeech(), "What stop would you like shuttle information for?");
		default:
			speech = new PlainTextOutputSpeech();
			speech.setText("I did not understand you.");
			return SpeechletResponse.newTellResponse(speech);
		}
		} catch (InvalidInputException e){
			return newAskResponse(e.getMessage() + " Please repeat your stop.", "Please repeat your stop.");
		} catch (StopException | IOException | JSONException e){
			speech = new PlainTextOutputSpeech();
			speech.setText(e.getMessage());
			return SpeechletResponse.newTellResponse(speech, buildCard(INVOCATION_NAME, e.getMessage()));
		} 
	}

	@Override
	public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
		Session session = requestEnvelope.getSession();
		SessionEndedRequest request = requestEnvelope.getRequest();
		log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
			    session.getSessionId());
		
	}

	
	/**
	 * Wrapper for creating the Ask response from the input strings.

	 * @param stringOutput
	 *            the output to be spoken
	 * @param repromptText
	 *            the reprompt for if the user doesn't reply or is
	 *            misunderstood.
	 * @return SpeechletResponse the speechlet response
	 */
	private SpeechletResponse newAskResponse(String stringOutput, String repromptText) {
		SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
		outputSpeech.setSsml("<speak> " + stringOutput + " </speak>");

		PlainTextOutputSpeech repromptOutputSpeech = new PlainTextOutputSpeech();
		repromptOutputSpeech.setText(repromptText);
		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(repromptOutputSpeech);
		//SimpleCard card = buildCard("FOR DEBUGGING");
		//return SpeechletResponse.newAskResponse(repromptOutputSpeech, reprompt, card);
		return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
	}
	
	/**
	 * Wrapper for creating the Ask tell from the input string.

	 * @param message
	 *            the output to be spoken
	 * @return SpeechletResponse the speechlet response
	 */
	private SpeechletResponse newTellResponse(String message) {
		SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
		outputSpeech.setSsml("<speak> " + message + " </speak>");
		return SpeechletResponse.newTellResponse(outputSpeech);
	}
	
	/**
	 * Wrapper for creating the Ask tell from the input string and card.

	 * @param message
	 *            the output to be spoken
	 * @return SpeechletResponse the speechlet response
	 */
	private SpeechletResponse newTellResponse(String message, Card card) {
		SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
		outputSpeech.setSsml("<speak> " + message + " </speak>");
		return SpeechletResponse.newTellResponse(outputSpeech, card);
	}
	
	/**
	 * Wrapper for creating a SimpleCard from the input string.
	 * @param s The failure message
	 * @return SimpleCard a failure card
	 */
	private SimpleCard buildCard(String title, String s){
		SimpleCard card=new SimpleCard();
		card.setTitle(title);
		card.setContent(s);
		return card;
	}
	
	public String getValueFromSlot(Intent intent, String name){
		Slot slot = intent.getSlot(name);
		return slot != null ? slot.getValue() : null; 
	}

}
