package abarson.transloc.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtils {

	
	
	private static Logger log = LoggerFactory.getLogger(JsonUtils.class);
	
	private static final String TRANSLOC_AUTHENTICATION_KEY;
	
	public static final boolean RUNNING_LOCALLY;
	
	static {
		//when the code is run from lambda, this will return the environment variable key. In order to run locally, also need to include 
		//a hard-coded key
		String key = System.getenv("TRANSLOC_AUTHENTICATION_KEY");
		if (key != null){
			TRANSLOC_AUTHENTICATION_KEY = key;
			RUNNING_LOCALLY = false;
		} else {
			TRANSLOC_AUTHENTICATION_KEY = "blLICP7BJ3msh38jJYgcX1xbchD5p13WjrsjsnaItLLLkF3bKx"; //so that the Driver class can still be used
			RUNNING_LOCALLY = true;
		}
	}
	
    private static String readAll(Reader rd) throws IOException {
    	log.info("readAll: reader={}", rd);
    	
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String urlString) throws IOException, JSONException {
    	log.info("readJsonFromUrl: url={}", urlString);
    	URL url = new URL(urlString);
		HttpURLConnection huc= (HttpURLConnection) url.openConnection();
		huc.setRequestProperty("X-Mashape-Key", TRANSLOC_AUTHENTICATION_KEY);
		huc.setRequestProperty("Accept", "application/json");
        InputStream is = huc.getInputStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }
}
