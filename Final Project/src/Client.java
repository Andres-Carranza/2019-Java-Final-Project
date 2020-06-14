/*
 * Andres Carranza
 * 5/28/2019
 * This class connects with the server and maintains that connection while its running
 */
import java.io.*;
import java.net.*;
import javafx.application.*;
public class Client {

	private Main main;
	private boolean closeConnection;
	private Socket serverConnection;
	private BufferedReader serverIn;
	private PrintStream serverOut;
	private String clientIp;

	//If exception is thrown, client could not connect to server( invalid server ip)
	public Client(String serverIp, Main main) throws IOException{
		this.main = main;

		closeConnection = false;
		clientIp = InetAddress.getLocalHost().getHostAddress();


		serverConnection = new Socket(serverIp, Server.SERVER_PORT_NUMBER);
		serverIn = new BufferedReader(new InputStreamReader(serverConnection.getInputStream()));
		serverOut = new PrintStream(serverConnection.getOutputStream());

		Thread receiveMessage = new Thread(new ReceiveMessage());

		receiveMessage.start();
	}

	//Format !:recipient class:type:message
	public void sendMessageToServer(String message) {
		serverOut.println( message);
	}

	//Communicates with other clients
	//Format recipient username:recipient class:type:message
	public void sendMessageToClient(String username, String recipientClass, String type, String message) {
		serverOut.println(username + ":" + recipientClass + ":" + type + ":" +message);
	}

	//Format sender name:recipient class:type:message
	//or
	// !:recipient class:type:message
	public void receiveMessage(String message) {
		main.distributeMessage(message);
	}

	//Returns this clients ip
	public String getClientIp() {
		return clientIp;
	}

	//Requests server to set name
	public void setName(String name) {
		sendMessageToServer(Server.SET_NAME);
		sendMessageToServer(name);
	}

	//Closes the connection with the server
	public void closeConnection() {
		closeConnection = true;
		sendMessageToServer(Server.CLOSE_CONNECTION);

		try {
			serverConnection.close();
			serverIn.close();
			serverOut.close();
		} catch(IOException e) {
			e.printStackTrace();
		}


	}

	//Class handles messages received from server
	private  class ReceiveMessage implements Runnable{

		//listens for messages
		@Override
		public void run() {
			try {
				while(!closeConnection) {
					String message = serverIn.readLine();
					Platform.runLater(()->{
						receiveMessage(message);
					});
				}
			}catch(IOException e) {
			}
		}

	}


}
