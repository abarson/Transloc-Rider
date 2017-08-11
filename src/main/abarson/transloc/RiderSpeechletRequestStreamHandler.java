package abarson.transloc;

import java.util.HashSet;
import java.util.Set;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

public class RiderSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler{
	private static final Set<String> supportedApplicationIds = new HashSet<String>();
    static {
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
        supportedApplicationIds.add("amzn1.ask.skill.84f7a0bc-c066-44cf-93fe-890daa7d0408"); //Rider
    }

    public RiderSpeechletRequestStreamHandler() {
        super(new RiderSpeechlet(), supportedApplicationIds);
    }
}
