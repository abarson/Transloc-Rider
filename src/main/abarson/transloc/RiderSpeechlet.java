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
import com.amazon.speech.ui.PlainTextOutputSpeech;

import abarson.transloc.api.ArrivalMessage;
import abarson.transloc.api.Route;
import abarson.transloc.api.Stop;
import abarson.transloc.api.TranslocApi;
import abarson.transloc.core.DataProcessor;
import abarson.transloc.core.ResponseBuilder;
import abarson.transloc.exception.InvalidInputException;
import abarson.transloc.exception.RouteException;
import abarson.transloc.exception.StopException;
import abarson.transloc.validation.RouteCorrector;
import abarson.transloc.validation.StopCorrector;
import abarson.transloc.validation.Validator;

public class RiderSpeechlet implements SpeechletV2{

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
		
		//initialize route and stop list
		try {
			routeList = TranslocApi.getRoutes();
			stopList = TranslocApi.getStops();
			activeRoutes = DataProcessor.getActiveRouteMap(routeList);
		} catch (JSONException | IOException e) {
			log.error(e.getMessage());
			//return a failure response
		}
		
	}

	@Override
	public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
		Session session = requestEnvelope.getSession();
		LaunchRequest request = requestEnvelope.getRequest();
		log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
			    session.getSessionId());
		
		
		if (routeList == null || stopList == null){
			//return new failure response
		}
		
		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		speech.setText("This is called from on launch.");
		return SpeechletResponse.newTellResponse(speech);
	}

	@Override
	public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
		Session session = requestEnvelope.getSession();
		IntentRequest request = requestEnvelope.getRequest();
		log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
			    session.getSessionId());
		
		if (routeList == null || stopList == null){
			//return new failure response
		}
		
		Intent intent = request.getIntent();
		
		PlainTextOutputSpeech speech;
		String feedback = "";
		try {
		switch (intent.getName()){
		case "GetArrivalsIntent":
			if (activeRoutes.isEmpty()){
				//return new NoShuttles response
			}
			
			String routeName = getValueFromSlot(intent, "Route");
			String stopName = getValueFromSlot(intent, "Stop");
			
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
				} catch (RouteException e){
					feedback = e.getMessage() + " However, ";
					arrivals = TranslocApi.getArrivalTimes(stop.getStop_id());
				} 
			} else { //if the user didn't provide a route, just generate predictions for all routes coming to this stop
				arrivals = TranslocApi.getArrivalTimes(stop.getStop_id());
			}
			
			String output = ResponseBuilder.getArrivalTimeResponse(arrivals, routeList, stopList, stop);
			
			speech = new PlainTextOutputSpeech();
			speech.setText(feedback + output);
			return SpeechletResponse.newTellResponse(speech);
			
		default:
			speech = new PlainTextOutputSpeech();
			speech.setText("I did not understand you.");
			return SpeechletResponse.newTellResponse(speech);
		}
		} catch (StopException | InvalidInputException | IOException | JSONException e){
			speech = new PlainTextOutputSpeech();
			speech.setText("Something went wrong. " + e.getMessage());
			return SpeechletResponse.newTellResponse(speech);
		}
	}

	@Override
	public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
		Session session = requestEnvelope.getSession();
		SessionEndedRequest request = requestEnvelope.getRequest();
		log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
			    session.getSessionId());
		
	}
	
	public String getValueFromSlot(Intent intent, String name){
		Slot slot = intent.getSlot(name);
		return slot != null ? slot.getValue() : null; 
	}

}
