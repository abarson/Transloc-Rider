# Transloc-Rider
A Voice-First Application that allows users to make queries to the TransLoc API for University of Vermont shuttle information.

## Code

src/main/abarson/transloc contains all code responsible for connecting to the TransLoc API. To see how specific queries are made to the Transloc API, see TranslocApi.java. The api package itself encapsulates all of the logic surrounding making requests to Transloc API, and mapping the JSON responses returned to Java classes. See Route.java and Stop.java for the structure of these JSON objects. 

RiderSpeechlet.java defines the structure of the converstaion, handling the user's intent and the slot parameters spoken by the user. See RouteCorrector.java and StopCorrector.java in the validation package to see the lists of supported synonyms, and often misheard, slot variables for Routes and Stops. After user testing, these lists will increase in size to allow for more loosely structures requests.
