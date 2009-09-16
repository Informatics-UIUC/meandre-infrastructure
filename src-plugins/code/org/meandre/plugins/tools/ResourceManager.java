package org.meandre.plugins.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.support.io.IOUtils;


public class ResourceManager {

    /**
     * The core root logger
     */
    protected Logger log = KernelLoggerFactory.getCoreLogger();

    private final String BASE_URL;

    public ResourceManager(CoreConfiguration cnf) {
        BASE_URL = "http://localhost:" + cnf.getBasePort() + cnf.getAppContext() + JarToolServlet.SERVLET_PATH;
        log.fine("baseURL=" + BASE_URL);
    }

    public String getResourceForMD5(String sMD5) {
        // sanitize the string
        sMD5 = sMD5.replaceAll("\\r|\\n", "");

        try {
            URL req = new URL(BASE_URL + sMD5 + ".md5/name");
            String resp = IOUtils.getTextFromReader(IOUtils.getReaderForResource(req.toURI()));

            return new JSONObject(resp).getString("name");
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        catch (FileNotFoundException e) {
            return null;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void addResource(String sFile, String sMD5) {
        // sanitize the string
        sMD5 = sMD5.replaceAll("\\r|\\n", "");

        try {
            URL req = new URL(BASE_URL + "add");

            String data = "name=" + URLEncoder.encode(sFile, "UTF-8");
            data += "&" + "md5=" + URLEncoder.encode(sMD5, "UTF-8");

            URLConnection conn = req.openConnection();
            conn.setDoOutput(true);

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            // consume the response - we don't care about it for now
            IOUtils.getTextFromReader(new InputStreamReader(conn.getInputStream()));
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
