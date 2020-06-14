/*
 * Programmer: Andres Carranza
 * Date: 5/16/2019
 *
 * CLASS DETAILS:
 	- This class holds the Scene for the graphics of the window shown after connecting to the server
 	- This class controls all the related with the messaging portion of the app(for now only Conversation)
 */

import java.io.*;
import java.util.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;

public class MessageScene {
	public static final String CLASS_ID = "message scene";//Used to identify this class when distributing messages from the server
	private Main main;//Main class object. Used to communicate with it
	private Scene messageScene;//The scene that contains all the graphics of this class and other classes relating to messaging
	private Button newConvoButton;//Button that prompts the user to start a new conversation when clicked
	private VBox screenBox;//This box is the box containing graphics for the screen. Acts as a card layout
	private VBox menuBox;//Box containing all the graphics for only this class. Acts as a menu
	private HBox bodyBox;//Box containing everything except the newConvoButton
	private VBox onlineUsersBox;//Box containing the onlineUsersListBox
	private VBox onlineUsersListBox;//Contains a list of all online users
	private VBox conversationsContainer;//Box containing all current conversation displays
	private LinkedHashMap< String, Conversation> conversations;//List of all conversations
	private Text noConversations;//Used when there are no conversations
	private String username;//The username entered by the user
	private String[] onlineUsers;//List of all online users. Provided by server
	private ScrollPane conversationsScroll;//Scroll pane containing the conversations
	private boolean noConvos;//True if there are no conversationsContainer
	private int numDisconnected;//Holds the number of old conversations that are now disconnected

	//Constructor
	//Sets up all GUI
	public MessageScene(Main main) {

		this.main = main;

		//Creating vertical box layout
		screenBox = new VBox();

		//Adding menuBox
		setUpMenuBox();
		screenBox.getChildren().add(menuBox);

		//adding convoButton to the menu box
		setUpNewConvoButton();
		menuBox.getChildren().add(newConvoButton);

		//Adding bodyBox
		setUpBodyBox();
		menuBox.getChildren().add(bodyBox);

		//Adding onlineUsersBox
		setUpOnlineUsersBox();
		bodyBox.getChildren().add(onlineUsersBox);

		//Adding scroll pane containing conversationsContainer
		setUpConversationsScroll();
		bodyBox.getChildren().add(conversationsScroll);


		//Adding conversationsContainer
		setUpConversationsContainer();
		conversationsScroll.setContent(conversationsContainer);

		//Setting up scene
		messageScene = new Scene(screenBox,800,600);
		//Loading the CSS sheet
		messageScene.getStylesheets().add(getClass().getResource("MessageScene.css").toExternalForm());

		//Initializing list of conversations
		conversations = new LinkedHashMap<String, Conversation>();

		//Initializing boolean for no conversations
		noConvos = true;

	}

	//Returns the Scene object
	public Scene getScene() {
		return messageScene;
	}

	//Returns an array of online users
	public String[] getOnlineUsers() {
		return onlineUsers;
	}

	//Sets the username
	public void setUsername(String username) {
		this.username = username;
	}

	//Returns the screenBox
	public VBox getScreenBox() {
		return screenBox;
	}

	//Returns the menu box
	public VBox getContent() {
		return menuBox;
	}

	//Returns conversation with recipient name username
	public Conversation getConversation(String username) {
		return conversations.get(username);
	}

	//Changes the window's content
	//Acts as a card layout
	public void setScreenContent(Node n) {
		screenBox.getChildren().clear();
		screenBox.getChildren().add(n);
	}

	//Returns the current conversations
	public Set<String> getConversations() {
		return conversations.keySet();
	}

	//Adds a new conversation
	public void addConversation(Conversation conversation) {
		if(noConvos) {
			conversationsContainer.getChildren().clear();
			noConvos = false;
		}

		conversations.put(conversation.getRecipientName(), conversation);
		conversation.setDisplay(new HBox());
		conversation.getDisplay().setId("display-container");
		setGlowEffect(conversation.getDisplay());

		conversation.getDisplay().addEventHandler(MouseEvent.MOUSE_CLICKED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {

				//Switching screens
				setScreenContent(conversation.getContent());
			}
		});
	}

	//Refreshes the conversations display
	//Called when a new message is sent or received
	public synchronized void refeshConversationDisplay(Conversation conversation) {
		conversation.getDisplay().getChildren().clear();

		Text recipientName = new Text(conversation.getRecipientName());
		recipientName.setId("recipient-name");

		Label lastMessage = new Label(conversation.getLastMessage());
		lastMessage.setId("last-message");


		conversation.getDisplay().getChildren().add(recipientName);
		conversation.getDisplay().getChildren().add(lastMessage);


		conversationsContainer.getChildren().remove(conversation.getDisplay());
		conversationsContainer.getChildren().add(0, conversation.getDisplay());
	}

	//True if there is an exiting conversation with senderUsername
	public boolean isConversationWith(String senderUsername) {
		return conversations.containsKey(senderUsername);
	}

	//true if user is online
	public boolean isOnline(String username) {
		for(String user : onlineUsers)
			if(user.equals(username))
				return true;
		return false;
	}
	
	//Stops pool threads on close
	public void stopPool() {
		for(Conversation conversation : conversations.values()) {
			conversation.stopPool();
		}
	}

	/*********************
	 * NETWORKING METHODS*
	 *********************
	 */

	//Called to send a message to another client
	//Sends the message via main class
	//Formatted in:
	//		recipient name:recipient class:type:message
	public void sendMessageToClient(String username, String recipientClass, String type, String message) {
		main.sendMessageToClient(username, recipientClass, type, message);
	}

	//Called when a message is received form the server
	public void messageReceivedFromServer(String message) {
		String type = message.substring(0, message.indexOf(':'));
		message = message.substring(message.indexOf(':') + 1);

		if(type.equals(Server.GET_ONLINE_IPS)) {
			setOnlineUsersList(stringToArray(message));
			checkIfConversationsOffline();
		}
	}

	//Called when a message is received from a client
	public void messageReceivedFromClient(String message) {

		int i1 = message.indexOf(":");
		int i2 = message.indexOf(":", i1 + 1);

		String senderUsername = message.substring(0, i1);
		String messageType = message.substring(i1 + 1, i2);
		message = message.substring(i2 + 1);

		if(messageType.equals(Conversation.TEXT_TYPE)){
			if(isConversationWith(senderUsername)) {
				conversations.get(senderUsername).textReceived(message);
			}
			else {								
				Conversation conversation = new Conversation(this, senderUsername);
				String msg = message;
				conversation.textReceived(msg);
			}
		}
		else {//Means its a game

			if(isConversationWith(senderUsername)) {
				conversations.get(senderUsername).gameMessageReceieved(messageType, message);
			}

			else {								
				Conversation conversation = new Conversation(this, senderUsername);
				conversation.gameMessageReceieved(messageType,message);

			}

		}

	}



	/*
	 ******************
	 * PRIVATE METHODS*
	 ******************
	 */

	//Checks if any conversations are now offline
	private void checkIfConversationsOffline() {
		ArrayList<Conversation> offlines = new ArrayList<Conversation>();
		for(Conversation conversation : conversations.values()) {
			boolean recipientIsOnline = false;
			for(String user :  onlineUsers) {
				if(conversation.getRecipientName().equals(user)) {
					recipientIsOnline = true;
					break;
				}
			}
			if(!recipientIsOnline && conversation.isConnected()) {
				offlines.add(conversation);
			}
		}

		for(Conversation conversation : offlines) {
			conversation.recipientDisconnected();
			conversations.remove(conversation.getRecipientName());
			conversations.put(numDisconnected + "", conversation);
			numDisconnected++;

		}
	}

	//Checks if conversation is offline
	public void checkIfConversationIsOffline(Conversation conversation) {
		boolean recipientIsOnline = false;
		for(String user :  onlineUsers) {
			if(conversation.getRecipientName().equals(user)) {
				recipientIsOnline = true;
				break;
			}
		}
		if(!recipientIsOnline && conversation.isConnected()) {
			conversation.recipientDisconnected();
			conversations.remove(conversation.getRecipientName());
			conversations.put(numDisconnected + "", conversation);
			numDisconnected++;
		}



	}

	//Sets the list of online users
	private void setOnlineUsersList(String[] onlineUsers) {
		this.onlineUsers = onlineUsers;
		onlineUsersListBox.getChildren().clear();
		for(String str : onlineUsers)
			setUpOnlineUsersList(str);
	}

	//Converts a set formatted as a String into an Array
	private String[] stringToArray(String str) {
		str = str.replace("[","");
		str = str.replace("]","");
		str = str.replaceAll("\\s","");
		return str.split("[,]");
	}

	//Sets up menuBox
	private void setUpMenuBox() {
		menuBox = new VBox();
		menuBox.setId("menu-box");
		menuBox.prefHeightProperty().bind(screenBox.heightProperty());
		menuBox.prefWidthProperty().bind(screenBox.widthProperty());
	}

	//Sets up bodyBox
	private void setUpBodyBox() {
		bodyBox = new HBox();
		bodyBox.setId("body-box");
		bodyBox.prefHeightProperty().bind(menuBox.heightProperty());
		bodyBox.prefWidthProperty().bind(menuBox.widthProperty());

	}

	//Sets up onlineUsersBox
	private void setUpOnlineUsersBox() {
		//Online users box (online users + header)
		onlineUsersBox = new VBox();
		onlineUsersBox.setId("online-users-box");

		//Adding header
		Text header = new Text("Users online:");
		header.setId("header");
		onlineUsersBox.getChildren().add(header);

		//Adding scroll pane
		ScrollPane sc = new ScrollPane();
		sc.setId("#online-users-scroll-pane");
		sc.setHbarPolicy(ScrollBarPolicy.NEVER);
		sc.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		sc.prefHeightProperty().bind(onlineUsersBox.heightProperty());
		sc.prefWidthProperty().bind(onlineUsersBox.minWidthProperty());
		onlineUsersBox.getChildren().add(sc);

		//Adding box containing all online users text object
		onlineUsersListBox = new VBox();
		onlineUsersListBox.setId("online-users-list-box");
		onlineUsersListBox.prefHeightProperty().bind(sc.heightProperty());
		onlineUsersListBox.prefWidthProperty().bind(sc.widthProperty());
		sc.setContent(onlineUsersListBox);

	}

	//Sets up onlineUsersList
	public void setUpOnlineUsersList(String user) {
		//Adding online users list  to online users list box

		if(user.equals(username))
			user += " (you)";

		Label label = new Label(user);
		label.setId("online-users-list");
		label.setGraphic(loadImg("OnlineCircle.png", 8, 8));

		onlineUsersListBox.getChildren().add(label);
	}

	//Sets up conversationsScroll
	private void setUpConversationsScroll() {
		conversationsScroll = new ScrollPane();
		conversationsScroll.prefWidthProperty().bind(bodyBox.widthProperty());
		conversationsScroll.setHbarPolicy(ScrollBarPolicy.NEVER);
		conversationsScroll.prefHeightProperty().bind(bodyBox.heightProperty());
	}

	//Sets up conversationContainer
	private void setUpConversationsContainer() {
		conversationsContainer = new VBox();
		conversationsContainer.setId("conversations-container");
		conversationsContainer.prefWidthProperty().bind(conversationsScroll.widthProperty());
		conversationsContainer.prefHeightProperty().bind(conversationsScroll.heightProperty().subtract(2));

		HBox box = new HBox();
		box.setId("no-convo-container");

		noConversations = new Text("No Conversations");
		noConversations.setId("no-convo");
		box.getChildren().add(noConversations);
		conversationsContainer.getChildren().add(box);
	}

	//Used to create an ImageView from an image located at filepath scaled to width and height
	private ImageView loadImg(String filepath, int width, int height)
	{
		FileInputStream inputstream = null;
		try {
			inputstream = new FileInputStream(filepath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ImageView img = new ImageView( new Image(inputstream, width, height, false, false)); 

		return img;
	}

	//Creates an effect so that when a mouse hovers above a node it makes the node glow
	//Useful especially for buttons
	private void setGlowEffect(Node n) {
		//Adding an effect so that when a mouse hovers above the button, it glows
		n.addEventHandler(MouseEvent.MOUSE_ENTERED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				Glow glow = new Glow();
				glow.setLevel(0.5); 
				n.setEffect(glow);
			}
		});

		//Reseting button to default once the mouse has exited
		n.addEventHandler(MouseEvent.MOUSE_EXITED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				n.setEffect(null);
			}
		});
	}

	//Returns the messageScene object
	private MessageScene getObject() {
		return this;
	}

	//Sets up newConvoButton
	private void setUpNewConvoButton() {
		//Creating  button for new conversations 
		newConvoButton = new Button("   New Conversation");
		newConvoButton.setId("new-convo");

		//loading img for button
		newConvoButton.setGraphic(loadImg("NewConvoButton.png", 45, 45));

		//Adding glow effect on hover
		setGlowEffect(newConvoButton);

		//Adding action listener
		newConvoButton.addEventHandler(MouseEvent.MOUSE_CLICKED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				Conversation c = new Conversation(getObject());

				//Switching screens
				getObject().setScreenContent(c.getContent());

			}
		});

	}
}
