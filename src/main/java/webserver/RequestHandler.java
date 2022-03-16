package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.User;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect!! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (	InputStream in = connection.getInputStream();
        		OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
        	BufferedReader br = new BufferedReader(new InputStreamReader(in));
        	
        	String line = br.readLine();
        	
        	if (line == null) {
        		return;
        	}
        	
        	String[] tokens = line.split(" ");
        	
        	while (!line.equals("")) {
        		line = br.readLine();
        	}
        	String url = "";
        	if (tokens[1].startsWith("/user/create?")) {
        		
        		url = tokens[1].substring(0, tokens[1].indexOf("?"));
        		Map<String, String> requestMap = HttpRequestUtils.parseQueryString(tokens[1].substring(tokens[1].indexOf("?")+1));
        		
        		User user = new User(requestMap.get("userId"), requestMap.get("password"), requestMap.get("name"), requestMap.get("email"));
        		System.out.println(user.toString());
        		DataOutputStream dos = new DataOutputStream(out);
        		response302Header(dos, "/index.html");
        	} else {
        		url = tokens[1];
        	}
        	
        	String fileName = "./webapp";
        	byte[] bytes = Files.readAllBytes(new File(fileName + url).toPath());  
        	
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
    
    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + url);
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
