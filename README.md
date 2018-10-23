# Transloc-Rider
A Voice-First Application that allows users to make queries to the TransLoc API for University of Vermont shuttle information.

## Code

src/main/abarson/transloc contains all code responsible for connecting to the TransLoc API. To see how specific queries are made to the Transloc API, see TranslocApi.java. The api package itself encapsulates all of the logic surrounding making requests to Transloc API, and mapping the JSON responses returned to Java classes. See Route.java and Stop.java for the structure of these JSON objects. 

RiderSpeechlet.java defines the structure of the conversation, handling the user's intent and the slot parameters spoken by the user. See RouteCorrector.java and StopCorrector.java in the validation package to see the lists of supported synonyms, and often misheard, slot variables for Routes and Stops. After user testing, these lists will increase in size to allow for more loosely structures requests.

## Testing

When UVM routes and stops go out of season, the Transloc API will reflect this by no longer returning them in API calls. Likewise, when routes and stops come into season, they will appear once again in the API calls made to the Transloc API. To help stay on top of when these changes are made, there are two JUnit test files that can be run. In src/main/test/abarson, RouteTest.java and StopTest.java will alert you if either of the Corrector classes have out-of-season/non-existent stops or routes (stops or routes not returned by Transloc API), or if either of the Corrector classes are missing stops or routes returned by the Transloc API. Running these tests occasionally makes maintaining the system very simple, as stops and routes can be added or removed from the Corrector classes as needed.
