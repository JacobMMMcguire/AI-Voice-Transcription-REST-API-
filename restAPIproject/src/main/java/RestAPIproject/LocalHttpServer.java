package RestAPIproject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class LocalHttpServer implements Runnable{
	
	private static volatile String lastTranscription = "Waiting for transcription...";
    private final int portNumber;

    public LocalHttpServer(int port) {
        this.portNumber = port;
    }

    public static void updateTranscription(String text) {
        lastTranscription = text;
    }

    @Override
    public void run() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(portNumber), 0);
            

            server.createContext("/", new MyHtmlHandler()); 
            server.createContext("/status", new MyStatusHandler());
             
            server.setExecutor(null);
            server.start();
            System.out.println("HTTP Server started. View output at http://localhost:" + portNumber);
        } catch (IOException e) {
            System.err.println("Could not start HTTP server: " + e.getMessage());
        }
    }
    
    static class MyStatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            // Send the raw text content only
            byte[] responseBytes = lastTranscription.getBytes("UTF-8");
            t.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            t.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = t.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
    }

    // This nested class handles the browser's HTML requests
    static class MyHtmlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
        	String htmlContent = "<html>"
                    + "<head><title>Transcription Output</title>"
                    + "<style>body { font-family: sans-serif; }</style>"
                    + "</head>"
                    + "<body>"
                    + "<h1>AssemblyAI Transcription Result</h1>"
                    + "<p>Latest result received from Java client:</p>"
                    + "<pre id='output' style='background-color:#eee; padding:20px; border:1px solid #ccc; white-space: pre-wrap; font-size: 1.2em;'>Waiting for update from server...</pre>"
                    
                    // --- THE JAVASCRIPT POLLING CODE ---
                    + "<script>"
                    + "function fetchStatus() {"
                    + "    fetch('/status')" 
                    + "        .then(response => response.text())"
                    + "        .then(data => {"
                    + "            document.getElementById('output').innerText = data;"
                    + "        })"
                    + "        .catch(error => console.error('Error fetching status:', error));"
                    + "}"
                    + "fetchStatus();"
                    + "setInterval(fetchStatus, 2000);"
                    + "</script>"
                    
                    + "</body></html>";
                
                // Send HTTP headers and the content
                byte[] responseBytes = htmlContent.getBytes("UTF-8");
                t.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                t.sendResponseHeaders(200, responseBytes.length);
                OutputStream os = t.getResponseBody();
                os.write(responseBytes);
                os.close();
                
        }
    }
}
