package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect!! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
        	BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        	String st = "";
        	String url = "";
        	boolean isUrl = false;
        	while ((st = br.readLine()) != null) {
        		if (st.equals("")) {
        			break;
        		}
        		url = st.split(" ")[1];
        		
        		System.out.println(url);
        		if (url.equals("/index.html")) {
        			isUrl = true;
        			break;
        		}
        	}
        	String fileName = "./webapp";
        	byte[] bytes = "Hello World".getBytes();
        	if (isUrl) {
        		bytes = Files.readAllBytes(Paths.get(fileName + url));  
        	}
        	
            DataOutputStream dos = new DataOutputStream(out);
            
            response200Header(dos, bytes.length);
            responseBody(dos, bytes);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
