package restAPIproject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.Request;

public class RestAPI {
	
	public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException{
		
		 String hostName = "localhost";
	        int portNumber = 8081; 

	        try {
	            Socket clientSocket = new Socket(hostName, portNumber);
	            System.out.println("Connected to " + hostName + " on port " + portNumber);

	            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		
				Transcript transcript = new Transcript();
				transcript.setAudio_url("https://bit.ly/4pFjPvM");
				
				Gson gson = new Gson();
				String jsonRequest = gson.toJson(transcript);
				
				System.out.println(jsonRequest);
				
				HttpRequest postRequest = HttpRequest.newBuilder()
						.uri(new URI("https://api.assemblyai.com/v2/transcript"))
						
						/**NOTE: if you are viewing this from the JacobMMMcguire GitHub repo
						 * the Constants file has not been uploaded to protect the API key.
						 * These files will work if you create your own Constants class 
						 * with it's own API key constant.
						 * */
						.header("Authorization", Contstants.apiKey) 
						.POST(BodyPublishers.ofString(jsonRequest))
						.build();
				
				HttpClient httpClient = HttpClient.newHttpClient();
				
				HttpResponse<String> postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
				
				System.out.println(postResponse.body());
				
				transcript = gson.fromJson(postResponse.body(), Transcript.class);
				
				System.out.println(transcript.getId());
				
				HttpRequest getRequest = HttpRequest.newBuilder()
						.uri(new URI("https://api.assemblyai.com/v2/transcript/" +transcript.getId()))
						.header("Authorization", Contstants.apiKey) 
						.GET()
						.build();
				
				while(true) {
				
				out.println("Sending GET Request...");
				
				HttpResponse<String> getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
				transcript = gson.fromJson(getResponse.body(), Transcript.class);
				
				if("completed".equals(transcript.getStatus()) || "error".equals(transcript.getStatus()))
					break;
				
				Thread.sleep(1000);
				
				}
				
				out.println("Transcription complete:");
				out.println(transcript.getText());
			
		        clientSocket.close(); 
		        
		    } catch (IOException e) {
		        System.err.println("Could not connect to " + hostName + " on port " + portNumber);
		        System.err.println(e.getMessage());
		    }
	}
		
}
	        
	    


