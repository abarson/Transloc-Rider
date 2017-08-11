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
		huc.setRequestProperty("X-Mashape-Key", "blLICP7BJ3msh38jJYgcX1xbchD5p13WjrsjsnaItLLLkF3bKx");
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
