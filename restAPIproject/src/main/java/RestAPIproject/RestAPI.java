package RestAPIproject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.google.gson.Gson;

public class RestAPI {
	
	public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException{
		
	        int portNumber = 8080; 
	        Transcript transcript = null;
	        
	        LocalHttpServer server = new LocalHttpServer(portNumber);
	        Thread serverThread = new Thread(server);
	        serverThread.start(); 
	        Thread.sleep(2000);

	        try {
	        	
					transcript = new Transcript();
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
					
						System.out.println("Sending GET Request...");
						
						HttpResponse<String> getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
						transcript = gson.fromJson(getResponse.body(), Transcript.class);
						
						if("completed".equals(transcript.getStatus()) || "error".equals(transcript.getStatus())) {
							break;
						}
						
						Thread.sleep(1000);
					
					}
		        
					
					if ("completed".equals(transcript.getStatus())) {
		                System.out.println("API process finished. Updating localhost:" + portNumber + " page.");
		                LocalHttpServer.updateTranscription(transcript.getText());
		                System.out.println("Content updated. Check your browser at http://localhost:" + portNumber);
		                
		                Thread.currentThread().join(); 
		                
		            } else {
		                LocalHttpServer.updateTranscription("Transcription failed.");
		                System.err.println("Transcription failed");
		            }

		        } catch (IOException e) {

		            System.err.println("An I/O or connection error occurred.");
		            
		            if (e.getMessage().contains("Connection refused")) {
		                 System.err.println("ERROR: Local server is not running on localhost:" + portNumber + ". Please start the server process first.");
		            }
		            e.printStackTrace();
				

		       }
	}
		
}
	        
	    


