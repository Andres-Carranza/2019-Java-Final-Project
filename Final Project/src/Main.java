/*
 * Programmer: Andres Carranza
 * Date: 5/16/2019
 *
 * CLASS DETAILS:
 	- This class holds the Stage for the JavaFX graphics
 	- This class controls all the other classes
 	- This class is used to distribute the messages received from the server by the client class
 */

import java.io.*;
import javafx.application.*;
import javafx.stage.*; 
import javafx.scene.*;

public class Main extends Application {
	public static final String CLASS_ID= "main"; //Used to identify the class when distributing messages from the server
	//Scenes are equivalent to JPanels
	private ConnectScene connectScene;//This class contains the graphics and logic for the window shown on starting the app. This prompts the user to connect to the server
	private MessageScene messageScene;//This class contains the graphics and logic for everything after connecting to the server
	private Client client;//This class is used to receive and send messages to the server
	private Stage stage; //Stage is the equivalent of a JFrame
	
	//This method is called by the JavaFX thread on initialization
	//Acts as constructor
	//Method to set up GUI and instantiate fields
	@Override 
	public void start(Stage stage) { 
		this.stage = stage;

		messageScene = new MessageScene(this);
		connectScene = new ConnectScene(this);

		
		//Displaying stage
		stage.setResizable(false);
		stage.setTitle("Final Project"); 
		stage.setScene(connectScene.getScene());
		stage.show();


	}      

	//Method is called by the JavaFX thread when the window is closed
	//Methods notifies the server that the client is connecting
	@Override
	public void stop() {
		if(client != null)
			closeConnection();
		messageScene.stopPool();
	}

	//Method closes the connection with the server via the client class 
	public void closeConnection() {
		client.closeConnection();

	}

	//Returns the messaceScene field
	public Scene getMessageScene() {
		return messageScene.getScene();
	}
	
	//Called by connectScene to set the username of the messageScene
	public void setUsername(String username) {
		messageScene.setUsername(username);
	}

	//This is called by connectScene to change the scene once the user has succesfully connected to the server
	public void setStageToMessageScene() {
		stage.setScene(messageScene.getScene());
		stage.setResizable(true);
	}

	/*
	 ********************
	 *NETWORKING METHODS*
	 ********************
	 */
	
	//Called by connecScene to request to connect to the server
	public void connect(String connectCode, String username) throws IOException {
		client = new Client(connectCode, this);
		client.sendMessageToServer(Server.SET_NAME);
		client.sendMessageToServer(username);

	}

	//This method is called when the client has received a message 
	//This method relays the message to distributeMessageFromClient() or distributeMessageFromServer() depending on where the message came from
	public void distributeMessage(String message) {
		if(message.charAt(0) == '!') {
			distributeMessageFromServer(message.substring(1));
		}
		else {
			distributeMessageFromClient(message);
		}
	}

	//This method handles the distribution of messages from the server 
	public void distributeMessageFromServer(String message) {
		String classId = message.substring(0, message.indexOf(':'));
		message = message.substring(message.indexOf(':') + 1);

		if(classId.equals(CLASS_ID)) {
			messageReceived(message);
		}
		else if(classId.equals(MessageScene.CLASS_ID)) {
			messageScene.messageReceivedFromServer(message);
		}
	}

	//This method handles the distribution of messages from clients
	public void distributeMessageFromClient(String message) {
		int i1 = message.indexOf(":");
		int i2 = message.indexOf(":", i1 + 1);
		
		String classId = message.substring(i1 + 1, i2);
		message = message.substring(0, i1 ) + message.substring(i2);
		
		if(classId.equals(CLASS_ID)) {
			messageReceived(message);
		}
		else if(classId.equals(MessageScene.CLASS_ID) || classId.equals(Conversation.CLASS_ID)) {
			messageScene.messageReceivedFromClient(message);
		}
	}

	//This method is used to send messages to clients
	//Sends the messages via the client class
	public void sendMessageToClient(String username, String recipientClass, String type,  String message) {
		client.sendMessageToClient(username, recipientClass, type, message);
	}

	//This method is used to send messages to the server
	//Sends the messages via the client class
	public void sendMessageToServer(String message) {
		client.sendMessageToServer(message);
	}

	//This method is called when a message is received directed to the main(this) class
	public void messageReceived(String message) {
		if(message.equals(Server.USERNAME_VALID)) {
			connectScene.usernameIsValid();
		}
		else if(message.equals(Server.USERNAME_INVALID)) {
			connectScene.usernameIsInvalid();
		}

	} 

	//Method calls the launch method of Application
	public static void main(String args[]){ 
		launch(args); 
	}

}
