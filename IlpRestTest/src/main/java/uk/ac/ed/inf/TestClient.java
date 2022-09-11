package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * A very simple client to GET JSON data from a remote server
 */
public class TestClient
{
    public static void main(String[] args )
    {
        if (args.length != 2){
            System.err.println("Testclient Base-URL Echo-Parameter");
            System.err.println("you must supply the base address of the ILP REST Service e.g. http://restservice.somewhere and a string to be echoed");
            System.exit(1);
        }

        try {
            String baseUrl = args[0];
            String echoBasis = args[1];

            if (! baseUrl.endsWith("/")){
                baseUrl += "/";
            }

            // we call the test endpoint and pass in some test data which will be echoed
            URL url = new URL(baseUrl + "test/" + echoBasis);
            var objectMapper = new ObjectMapper();

            /**
             *
             * this part is only necessary if the GET result (JSON) has to be viewed as native data (here as a String)
             *
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
                }

                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                var getResult = br.lines().collect(Collectors.joining(System.lineSeparator()));

                System.out.println("Output from Server as .... \n" + getResult);
                conn.disconnect();
                var response = objectMapper.readValue(getResult, TestResponse.class);
             */


            /**
             * the Jackson JSON library provides helper methods which can directly take a URL,
             * perform the GET request convert the result to the specified class
             */
            var response = objectMapper.readValue(url, TestResponse.class);
            System.out.println("The server responded as JSON-greeting: \n\n" + response.greeting);


            /**
             * this would be the fastest way to load the JSON package
             */
            var mapper = new ObjectMapper();
            var fastResponse = mapper.readValue(new URL(baseUrl + "test/echo"), TestResponse.class);

            /**
             * some error checking - only needed for the sample
             */
            if (! fastResponse.greeting.endsWith("echo")){
                throw new RuntimeException("wrong echo returned");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
