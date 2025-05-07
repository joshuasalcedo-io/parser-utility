package io.joshuasalcedo.client;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;

import java.net.http.HttpClient;
import java.util.concurrent.Future;
import java.io.IOException;

/**
 * A simple example of how to use the AsyncHttpClient library to make asynchronous HTTP requests.
 */
public class HttpClientExample {
    
    /**
     * Example of how to make an asynchronous HTTP GET request.
     * 
     * @param url The URL to send the request to
     * @return The response as a string
     * @throws Exception If an error occurs
     */
    public static String get(String url) throws Exception {
        // Create a new AsyncHttpClient
        try (AsyncHttpClient client = new DefaultAsyncHttpClient()) {
            // Execute the request
            Future<Response> future = client.prepareGet(url).execute();
            
            // Wait for the response
            Response response = future.get();
            
            // Return the response body
            return response.getResponseBody();
        }
    }
    
    /**
     * Example of how to make an asynchronous HTTP POST request.
     * 
     * @param url The URL to send the request to
     * @param body The body of the request
     * @return The response as a string
     * @throws Exception If an error occurs
     */
    public static String post(String url, String body) throws Exception {
        // Create a new AsyncHttpClient
        try (AsyncHttpClient client = new DefaultAsyncHttpClient()) {
            // Execute the request
            Future<Response> future = client.preparePost(url)
                    .setBody(body)
                    .execute();
            
            // Wait for the response
            Response response = future.get();
            
            // Return the response body
            return response.getResponseBody();
        }
    }
    
    /**
     * Main method to demonstrate how to use the AsyncHttpClient.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        try {


            HttpClient httpClient = HttpClient.newBuilder()


                    .build();
            // Make a GET request
            String response = get("https://www.example.com");
            System.out.println("Response: " + response);
            
            // Make a POST request
            String postResponse = post("https://postman-echo.com/post", "Hello, World!");
            System.out.println("POST Response: " + postResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}