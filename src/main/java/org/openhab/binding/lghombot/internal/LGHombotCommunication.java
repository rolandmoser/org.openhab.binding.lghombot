package org.openhab.binding.lghombot.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;

import org.openhab.binding.lghombot.handler.LGHombotHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LGHombotCommunication {
    private final Logger logger = LoggerFactory.getLogger(LGHombotHandler.class);

    public Properties GetStatus(String address) throws MalformedURLException, IOException {
        URL url = new URL("http://" + address + ":6260/status.txt");
        logger.debug("GetStatus({})", url.toString());
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        Properties prop = new Properties();
        prop.load(request.getInputStream());

        Properties prop2 = new Properties();
        prop.forEach((key, value) -> prop2.put(key, ((String) value).substring(1, ((String) value).length() - 1)));
        return prop2;
    }

    public void SendCommand(String address, String s) throws MalformedURLException, IOException {
        // TODO replace with proper JSON sending and error handling
        URL url = new URL("http://" + address + ":6260/json.cgi?" + URLEncoder.encode(s, "UTF-8"));
        logger.debug("SendCommand({})", url.toString());
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        int responseCode = request.getResponseCode();
        logger.debug("GetStatus()={}\n{}", responseCode, response);
    }

}
