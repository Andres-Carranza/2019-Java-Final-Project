/*
 * Programmer: Andres Carranza
 * Date: 5/16/2019
 *
 * CLASS DETAILS:
 	- This class holds the graphics and logicfor a conversation
 */

import javafx.scene.text.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.media.*;

public class Conversation {
	public static final String CLASS_ID = "conversation";
	public static final String TEXT_TYPE = "text";//Used to identify the class when distributing messages from the server
	private MessageScene messageScene;//Used to communicate with the message scene object
	private Text recipientName;//Text containing the recipient username
	private String lastMessage;//String containing the last message sent or received
	private VBox conversationBox;//Box containing GUI for conversation
	private ScrollPane textsScroll;//Scroll pane for scrolling texts
	private HBox headerBox;//Box containing back button and recipient username or start conversation input
	private HBox textingInputBox;//Box containing text field for texting and send button
	private Button backButton;//Button that switches the screen to messageScene
	private VBox textingBox;//Box containing all the texts
	private TextField textingField;//Field used for texting
	private Button sendButton;//Button that sends texts
	private Button startConversationButton;//Button that starts the conversation
	private ComboBox<String> onlineUsersMenuInput;//ComboBox containing all available online users
	private boolean typingText;//True if currently typing a text
	private boolean keepBottom;//True if user is scrolled at the bottom
	private boolean firstText;//True if text is the first text
	private HBox display;//Box used by messageScene to show the conversation display
	private boolean recipientIsConnected;//True if recipeint is connected
	private Button newGameButton;
	private Button removeGameMenuButton;
	private HBox gameMenu;
	private HashMap<String, Game> games;
	private int gamesWon = 0;
	private int gamesTied = 0;
	private int gamesLost = 0;
	private Text lost;
	private Text won;
	private Text tied;
	
	//Constructor for creating a new Conversation
	//Sets up all GUI
	public Conversation(MessageScene messageScene) {
		this.messageScene = messageScene;

		//Setting up conversation box
		setUpConversationBox();

		//Adding header box
		setUpHeaderBox();
		conversationBox.getChildren().add(headerBox);

		//Adding back button
		setUpBackButton();
		headerBox.getChildren().add(backButton);

		//Adding onlineUsersMenu
		setUpOnlineUsersMenuInput();
		headerBox.getChildren().add(onlineUsersMenuInput);

		//Adding start conversation button
		setUpStartConversationButton();
		headerBox.getChildren().add(startConversationButton);

		//Adding texts scroll
		setUpTextsScroll();
		conversationBox.getChildren().add(textsScroll);


		//adding texting box
		setUpTextingBox();
		textsScroll.setContent(textingBox);

		//Adding conversation input box
		setUpTextingInputBox();
		conversationBox.getChildren().add(textingInputBox);

		//Adding new game button
		setUpNewGameButton();
		textingInputBox.getChildren().add(newGameButton);

		//Setting up removeGameMenu button
		setUpRemoveGameMenuButton();

		//Adding Texting field
		setUpTextingField();
		textingInputBox.getChildren().add(textingField);

		//Adding send button
		setUpSendButton();
		textingInputBox.getChildren().add(sendButton);

		//Seting up game menu
		setUpGameMenu();

		//Initializing booleans
		typingText = false;
		keepBottom = true;
		firstText = true;
		recipientIsConnected = true;
		games = new HashMap<String, Game>();
	}

	//Constructor used when a message is received from a username for which there is no existing conversation
	//Sets up all GUI
	public Conversation(MessageScene messageScene, String recipientUsername) {
		this.messageScene = messageScene;

		//Setting up conversation box
		setUpConversationBox();

		//Adding header box
		setUpHeaderBox();
		conversationBox.getChildren().add(headerBox);

		//Adding back button
		setUpBackButton();
		headerBox.getChildren().add(backButton);

		//Adding recipient name
		recipientName = new Text(recipientUsername);
		recipientName.setId("name-header");
		headerBox.getChildren().add(recipientName);

		//Adding texts scroll pane
		setUpTextsScroll();
		conversationBox.getChildren().add(textsScroll);


		//adding texting box
		setUpTextingBox();
		textsScroll.setContent(textingBox);

		//Adding conversation input box
		setUpTextingInputBox();
		conversationBox.getChildren().add(textingInputBox);

		//Adding new game button
		setUpNewGameButton();
		textingInputBox.getChildren().add(newGameButton);

		//Setting up removeGameMenu button
		setUpRemoveGameMenuButton();

		//Adding Texting field
		setUpTextingField();
		textingInputBox.getChildren().add(textingField);
		textingField.setEditable(true);

		//Adding send button
		setUpSendButton();
		textingInputBox.getChildren().add(sendButton);

		//Seting up game menu
		setUpGameMenu();

		//Initializing booleans
		typingText = false;
		keepBottom = true;
		firstText = true;
		recipientIsConnected = true;

		games = new HashMap<String, Game>();

	}

	//Returns the screenBox
	public VBox getScreenBox() {
		return messageScene.getScreenBox();
	}

	//Returns the display of this conversation
	//Used by messageScene
	public HBox getDisplay() {
		return display;
	}

	//Sets the display 
	//Used by message scene
	public void setDisplay(HBox b) {
		display = b;
	}

	//Changes the window's content
	//Acts as a card layout
	public void setScreenContent(Node n) {
		messageScene.setScreenContent(n);
	}

	//Returns the last message received/sent
	public String getLastMessage() {

		return lastMessage;
	}

	//Checks if this cnversation is offline
	public void checkIfConversationIsOffline() {
		messageScene.checkIfConversationIsOffline(this);
	}

	//Called to send a message to another client
	//Sends the message via main class
	//Formatted in:
	//		recipient name:recipient class:typse:message
	public void sendMessageToClient(String username, String recipientClass, String type, String message) {
		messageScene.sendMessageToClient(username, recipientClass, type, message);
	}

	//Sends a text
	public void sendText(String text ) {
		if(firstText) {
			if(messageScene.isConversationWith(getRecipientName())) {
				setScreenContent(messageScene.getConversation(getRecipientName()).getContent());
				messageScene.getConversation(getRecipientName()).sendText(text);
				return;
			}
			lastMessage = "";
			messageScene.addConversation(this);
			firstText = false;
		}
		if(!messageScene.isOnline(getRecipientName())){
			checkIfConversationIsOffline();
		}
		else {
			sendMessageToClient(recipientName.getText(), MessageScene.CLASS_ID, TEXT_TYPE, text);
			setUpTextBubble("text-sent-bubble-container", text);

			try {
				String sentSound = "Sent.mp3";     

				AudioClip sound = new AudioClip(getClass().getResource(sentSound).toURI().toString());
				sound.play();

			} catch (URISyntaxException e) {
				e.printStackTrace();
			}

			lastMessage = "sent: " +  text;

			messageScene.refeshConversationDisplay(this);
		}
	}


	//Returns screen content
	public VBox getContent() {
		return conversationBox;
	}

	//Returns recipientName
	public String getRecipientName() {
		return recipientName.getText();
	}

	//Called when a text is received
	public void textReceived(String text) {
		if(firstText) {
			lastMessage = "";
			messageScene.addConversation(this);
			firstText = false;
		}

		setUpTextBubble("text-received-bubble-container", text);


		try {
			String receivedSound = "Received.mp3";     

			AudioClip sound = new AudioClip(getClass().getResource(receivedSound).toURI().toString());
			sound.play();

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}



	}
	
	
	//Handles a won game
	public void gameWon() {
		gamesWon++;
		won.setText("Wins: " +gamesWon);
	}
	
	//handles a game tied
	public void gameTied() {
		gamesTied++;
		tied.setText("Ties: " +gamesTied);
	}

	//Handles a game lost
	public void gameLost() {
		gamesLost++;
		
		lost.setText("Losses: " +gamesLost);
		
	}
	
	//Called when a recipient is disconnected
	public void recipientDisconnected() {

		recipientIsConnected = false;
		textingField.setEditable(false);
		textingField.setText(recipientName.getText() + " disconnected");
		typingText = false;


		keepBottom = textsScroll.getVvalue() == 1;
		HBox bubble = new HBox();
		HBox bubbleContainer = new HBox();
		bubbleContainer.setId("text-received-bubble-container");

		bubble.setMaxWidth(0);
		bubble.setId("disconnected-bubble");

		Text t = new Text(recipientName.getText() + " disconnected");
		t.setId("disconnected");

		bubble.getChildren().add(t);
		bubbleContainer.getChildren().add(bubble);
		textingBox.getChildren().add(bubbleContainer);

		lastMessage = "disconnected";
		messageScene.refeshConversationDisplay(this);
	}

	//Returns if this  conversation is online
	public boolean isOnline() {
		return messageScene.isOnline(getRecipientName());
	}

	//Returns if this conversatin is connected
	public boolean isConnected() {
		return recipientIsConnected;
	}
	
	//Returns all the games this class has	
	public HashMap<String, Game> getGames() {
		return games;
	}

	//Returns the game ids
	public Set<String> getGameIds(){
		return games.keySet();
	}

	//Handles a game message sent
	public void gameMessageSent(VBox display, String game) {
		setUpGameBubble("text-sent-bubble-container", display);

		try {
			String sentSound = "Sent.mp3";     

			AudioClip sound = new AudioClip(getClass().getResource(sentSound).toURI().toString());
			sound.play();

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		lastMessage = "sent: " + game;

		messageScene.refeshConversationDisplay(this);

	}

	//Handles a game message received
	public void gameMessageReceieved(String gameId, String message) {
		if(firstText) {
			lastMessage = "";
			messageScene.addConversation(this);
			firstText = false;
		}
		if(gameId.charAt(0) == 'l') {
			gameId = "a" + gameId.substring(1);
		}
		else {
			gameId = "l" + gameId.substring(1);
		}

		if(!games.containsKey(gameId)) {
			Game game = null;

			/*
			 * Adding games
			 */
			if(message.equals(TicTacToe.CLASS_ID)) {
				game = new TicTacToe(getObject(), gameId);
			}
			else if(message.equals(ConnectFour.CLASS_ID)) {
				game = new ConnectFour(getObject(), gameId);
			}
			else if(message.equals(Pool.CLASS_ID)) {
				game = new Pool(getObject(), gameId);
			}
			games.put(game.OBJECT_ID, game);
			if(conversationBox.getChildren().contains(removeGameMenuButton)) {
				conversationBox.getChildren().remove(gameMenu);
				textingInputBox.getChildren().remove(removeGameMenuButton);
				textingInputBox.getChildren().add(0, newGameButton);
			}
		}
		games.get(gameId).messageReceived(message);
		setUpGameBubble("text-received-bubble-container", games.get(gameId).getGameDisplay());

		lastMessage = "received: " + games.get(gameId);
		messageScene.refeshConversationDisplay(this);

		try {
			String receivedSound = "Received.mp3";     

			AudioClip sound = new AudioClip(getClass().getResource(receivedSound).toURI().toString());
			sound.play();

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	//Stops the pool game threads when screen is closed
	public void stopPool() {
		for(Game game :  games.values()) {
			if(game.getClass() == Pool.class) {
				((Pool) game).stop();
			}
		}
	}

	/*
	 *******************
	 * PRIVATE METHODS *
	 *******************
	 */

	//Sets up text bubble
	private  void setUpGameBubble(String id, VBox display) {		

		keepBottom = textsScroll.getVvalue() ==1 ;
		HBox bubble = new HBox();
		HBox bubbleContainer = new HBox();
		bubbleContainer.setId(id);

		bubble.setId(id.substring(0, id.lastIndexOf("-")));

		display.getChildren().get(1).setId(bubble.getId().substring(0, bubble.getId().lastIndexOf("-")));

		bubble.getChildren().add(display);
		bubbleContainer.getChildren().add(bubble);

		textingBox.getChildren().add(bubbleContainer);



	}

	//Sets uo the remove game button
	private void setUpRemoveGameMenuButton() {
		removeGameMenuButton = new Button();
		removeGameMenuButton.setGraphic(loadImg("RemoveGameMenuButton.png",65,65));
		setGlowEffect(removeGameMenuButton);
		removeGameMenuButton.setId("game-buttons");

		removeGameMenuButton.addEventHandler(MouseEvent.MOUSE_CLICKED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				if(textingField.isEditable() ) {

					if(textingField.getText().isEmpty())
						textingField.setText("Enter message");
					conversationBox.getChildren().remove(gameMenu);

					textingInputBox.getChildren().remove(removeGameMenuButton);
					textingInputBox.getChildren().add(0, newGameButton);
				}
			}
		});

	}

	//return this object
	private Conversation getObject() {
		return this;
	}

	//Sts up the game menu
	private void setUpGameMenu() {
		gameMenu = new HBox();
		gameMenu.setId("game-menu");

		/*
		 * ADDING GAME DISPLAYS
		 */

		//Adding tic tac toe
		VBox ticTacToeDisplay = TicTacToe.getMenuDisplay();
		gameMenu.getChildren().add(ticTacToeDisplay);
		ticTacToeDisplay.addEventHandler(MouseEvent.MOUSE_CLICKED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				if(firstText) {
					if(messageScene.isConversationWith(getRecipientName())) {//means other person already started a conversation
						setScreenContent(messageScene.getConversation(getRecipientName()).getContent());
						Game ticTacToe = new TicTacToe(messageScene.getConversation(getRecipientName()));
						games.put(ticTacToe.OBJECT_ID, ticTacToe);
						messageScene.getConversation(getRecipientName()).getGames().put(ticTacToe.OBJECT_ID, ticTacToe);
						ticTacToe.sendMessageToClient(TicTacToe.CLASS_ID);
						return;
					}
					lastMessage = "";
					messageScene.addConversation(getObject());
					firstText = false;
				}
				Game ticTacToe = new TicTacToe(getObject());
				games.put(ticTacToe.OBJECT_ID, ticTacToe);
				ticTacToe.sendMessageToClient(TicTacToe.CLASS_ID);
				conversationBox.getChildren().remove(gameMenu);

				textingInputBox.getChildren().remove(removeGameMenuButton);
				textingInputBox.getChildren().add(0, newGameButton);

			}
		});
		setGlowEffect(ticTacToeDisplay);


		//Adding connectfourdisplay display
		VBox connectFourDisplay = ConnectFour.getMenuDisplay();//change
		gameMenu.getChildren().add(connectFourDisplay);
		connectFourDisplay.addEventHandler(MouseEvent.MOUSE_CLICKED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				if(firstText) {
					if(messageScene.isConversationWith(getRecipientName())) {//means other person already started a conversation
						setScreenContent(messageScene.getConversation(getRecipientName()).getContent());
						Game connectFour = new ConnectFour(messageScene.getConversation(getRecipientName()));
						games.put(connectFour.OBJECT_ID, connectFour);
						messageScene.getConversation(getRecipientName()).getGames().put(connectFour.OBJECT_ID, connectFour);
						connectFour.sendMessageToClient(ConnectFour.CLASS_ID);
						return;
					}
					lastMessage = "";
					messageScene.addConversation(getObject());
					firstText = false;
				}
				Game connectFour = new ConnectFour(getObject());//change
				games.put(connectFour.OBJECT_ID, connectFour);
				connectFour.sendMessageToClient(ConnectFour.CLASS_ID);
				conversationBox.getChildren().remove(gameMenu);

				textingInputBox.getChildren().remove(removeGameMenuButton);
				textingInputBox.getChildren().add(0, newGameButton);

			}
		});
		setGlowEffect(connectFourDisplay);

		VBox poolDisplay = Pool.getMenuDisplay();//change
		gameMenu.getChildren().add(poolDisplay);
		poolDisplay.addEventHandler(MouseEvent.MOUSE_CLICKED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				if(firstText) {
					if(messageScene.isConversationWith(getRecipientName())) {//means other person already started a conversation
						setScreenContent(messageScene.getConversation(getRecipientName()).getContent());
						Game pool = new Pool(messageScene.getConversation(getRecipientName()));
						games.put(pool.OBJECT_ID, pool);
						messageScene.getConversation(getRecipientName()).getGames().put(pool.OBJECT_ID, pool);
						pool.sendMessageToClient(Pool.CLASS_ID);
						return;
					}
					lastMessage = "";
					messageScene.addConversation(getObject());
					firstText = false;
				}
				Game pool = new Pool(getObject());//change
				games.put(pool.OBJECT_ID, pool);
				pool.sendMessageToClient(Pool.CLASS_ID);
				conversationBox.getChildren().remove(gameMenu);

				textingInputBox.getChildren().remove(removeGameMenuButton);
				textingInputBox.getChildren().add(0, newGameButton);

			}
		});
		setGlowEffect(poolDisplay);

		won = new Text("Wins: " + gamesWon);
		lost = new Text("Losses: " + gamesLost);
		tied = new Text("Ties: " + gamesTied);
		
		won.setStyle("-fx-font-size: 30px" );
		lost.setStyle("-fx-font-size: 30px" );
		tied.setStyle("-fx-font-size: 30px" );

		VBox scores = new VBox();

		scores.getChildren().add(won);
		scores.getChildren().add(lost);
		scores.getChildren().add(tied);

		
		gameMenu.getChildren().add(scores);

	}

	//Sets up the new game button
	private void setUpNewGameButton() {
		newGameButton = new Button();
		newGameButton.setGraphic(loadImg("NewGameButton.png",65,65));
		setGlowEffect(newGameButton);
		newGameButton.setId("game-buttons");
		newGameButton.addEventHandler(MouseEvent.MOUSE_CLICKED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				if(textingField.isEditable() ) {

					if(textingField.getText().isEmpty())
						textingField.setText("Enter message");

					setUpGameMenu();
					conversationBox.getChildren().add(gameMenu);

					textingInputBox.getChildren().remove(newGameButton);
					textingInputBox.getChildren().add(0, removeGameMenuButton);
				}
			}
		});
	}


	//Sets up text scroll
	private void setUpTextsScroll() {

		textsScroll = new ScrollPane();
		textsScroll.setId("scroll-pane");
		textsScroll.setHbarPolicy(ScrollBarPolicy.NEVER);
		textsScroll.prefHeightProperty().bind(conversationBox.heightProperty());
		textsScroll.prefWidthProperty().bind(conversationBox.widthProperty());
		
		textsScroll.setVvalue(1);
		textsScroll.vvalueProperty().addListener((observable, oldValue, newValue) -> {
			
			if ((Double) oldValue == 1.0 && keepBottom  ) {
				textsScroll.setVvalue(1);
				keepBottom = false;
			}
		});

	}

	//Sets up texting box
	private void setUpTextingBox() {
		textingBox = new VBox();
		textingBox.prefHeightProperty().bind(textsScroll.heightProperty().subtract(2));
		textingBox.prefWidthProperty().bind(textsScroll.widthProperty());
		textingBox.setId("texting-box");

	}

	//Returns width of a Text
	private double getTextWidth( String text, int size) {
		FontWidth fw = new FontWidth(new Font(size));
		return fw.computeStringWidth(text);
	}

	//Sets up text bubble
	private void setUpTextBubble(String id, String text) {
		keepBottom = textsScroll.getVvalue() == 1;
		HBox bubble = new HBox();
		HBox bubbleContainer = new HBox();
		bubbleContainer.setId(id);

		bubble.setId(id.substring(0, id.lastIndexOf("-")));

		Text t = new Text(text);

		double boxWidth = textingBox.getWidth();
		if(boxWidth == 0)
			boxWidth = 770;
		if(getTextWidth(text, 20) > boxWidth / 2 - 40)
			t.setWrappingWidth(boxWidth / 2 - 40);

		t.setId(bubble.getId().substring(0, bubble.getId().lastIndexOf("-")));

		bubble.getChildren().add(t);
		bubbleContainer.getChildren().add(bubble);

		textingBox.getChildren().add(bubbleContainer);

		if(id.equals("text-received-bubble-container")) {
			lastMessage = "received: " + text;
			messageScene.refeshConversationDisplay(this);
		}

	}

	//Sets up start conversation button
	private void setUpStartConversationButton() {
		startConversationButton = new Button("Start Conversation");
		startConversationButton.setId("start-convo-btn");
		startConversationButton.setMinWidth(230);
		setGlowEffect(startConversationButton);

		//Adding Action listener	
		startConversationButton.addEventHandler(MouseEvent.MOUSE_CLICKED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				String recipientUsername = onlineUsersMenuInput.getValue();
				if(recipientUsername != null) {
					recipientName = new Text(recipientUsername);
					recipientName.setId("name-header");

					textingField.setEditable(true);

					headerBox.getChildren().clear();
					headerBox.getChildren().add(backButton);
					headerBox.getChildren().add(recipientName);
				}

			}
		});
	}

	//Sets up onlineUsersMenuInput
	private void setUpOnlineUsersMenuInput() {
		onlineUsersMenuInput = new ComboBox<String>();
		onlineUsersMenuInput.getItems().addAll(messageScene.getOnlineUsers());
		onlineUsersMenuInput.getItems().removeAll(messageScene.getConversations());
		onlineUsersMenuInput.prefWidthProperty().bind(headerBox.widthProperty());
		onlineUsersMenuInput.setId("online-users-menu-input");

		//Adding action listener
		onlineUsersMenuInput.getEditor().addEventHandler(MouseEvent.MOUSE_CLICKED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				onlineUsersMenuInput.show();
			}
		});
	}

	//Sets up send button
	private void setUpSendButton() {
		sendButton = new Button();
		sendButton.setGraphic(loadImg("SendButton.png",75 , 60));
		setGlowEffect(sendButton);
		sendButton.setId("send-button");

		sendButton.addEventHandler(MouseEvent.MOUSE_CLICKED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				if(typingText == true && !textingField.getText().equals("")  ) {
					sendText(textingField.getText());
					textingField.setText("Enter message");
					typingText = false;

				}
				else {
					if(recipientIsConnected)
						textingField.setText("Enter message");
					typingText = false;
				}
			}
		});
	}

	//Sets up texingInputBox
	private void setUpTextingInputBox(){
		textingInputBox = new HBox();
		textingInputBox.prefWidthProperty().bind(conversationBox.widthProperty());
	}

	//Sets up conversationBox
	private void setUpConversationBox() {
		//Creating VBox container
		conversationBox = new VBox();
		conversationBox.setId("conversation-box");
		conversationBox.prefHeightProperty().bind(getScreenBox().heightProperty());
		conversationBox.prefWidthProperty().bind(getScreenBox().widthProperty());

		conversationBox.addEventHandler(MouseEvent.MOUSE_CLICKED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				if(textingField.getText().equals("")) {
					textingField.setText("Enter message");
					conversationBox.requestFocus();
					typingText = false;
				}
				else {
					conversationBox.requestFocus();					
				}
			}
		});


	}

	//Sets up headerBox
	private void setUpHeaderBox() {
		//Creating header box
		headerBox = new HBox();
		headerBox.prefWidthProperty().bind(conversationBox.widthProperty());
		headerBox.setId("header-box");

	}

	//Sets up back button
	private void setUpBackButton() {
		backButton = new Button();
		backButton.setId("back-button");

		ImageView img = loadImg("BackButton.png", 40, 40);
		img.setEffect(new ColorAdjust(0,0,-.4,0));
		backButton.setGraphic(img);
		setGlowEffect(backButton);

		//Adding action listener
		backButton.addEventHandler(MouseEvent.MOUSE_CLICKED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {

				//Switching screen content
				setScreenContent(messageScene.getContent());
				textingInputBox.getChildren().remove(0);
				textingInputBox.getChildren().add(0, newGameButton);
				conversationBox.getChildren().remove(gameMenu);
			}
		});
	}



	//Sets up texting field
	private void setUpTextingField() {
		textingField = new TextField();
		textingField.setId("texting-field");
		textingField.prefWidthProperty().bind(textingInputBox.widthProperty());
		textingField.setText("Enter message");
		textingField.setEditable(false);

		//Adding action listener
		textingField.addEventHandler(MouseEvent.MOUSE_CLICKED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				if(textingField.isEditable() == true && textingField.getText().equals("Enter message")) {
					textingField.setText("");
					typingText = true;
				}
			}
		});
		textingField.setOnKeyPressed(
				new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent e) {
						if(e.getCode() == KeyCode.ENTER) {
							if(typingText == true && !textingField.getText().isEmpty()) {
								sendText(textingField.getText());
								textingField.setText("");

							}
							else {
								if(textingField.getText().equals("")) {
									textingField.setText("Enter message");
									conversationBox.requestFocus();
									typingText = false;
								}
							}
						}
					}
				});

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

}
