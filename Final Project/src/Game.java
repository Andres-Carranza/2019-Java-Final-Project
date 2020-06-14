/*
 * Andres Carranza
 * 5/28/2019
 * This class represents a game
 * All games extend this class
 */
import java.net.*;
import javafx.beans.value.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.transform.*;
public abstract class Game {	

	enum GameState{
		INVITE, FIRST_TURN, LOCAL_TURN, AWAY_TURN, LOCAL_WIN, AWAY_WIN, TIE;
	}

	public static final String CLASS_ID = "game";
	protected static int numGames;
	public String OBJECT_ID;
	protected Conversation conversation;
	protected GameState gameState;
	protected VBox gameScreen;
	protected HBox header;
	protected boolean lastScreenIsConversation;
	private Button backButton;

	//Constructor
	public Game(Conversation conversation) {
		this.conversation = conversation;
		numGames++;
		gameState = GameState.INVITE;
		OBJECT_ID = "l" +  getObjectId();
		setUpGame();
	}

	//away game constructor
	public Game(Conversation conversation, String gameId) {
		this.conversation = conversation;
		gameState = GameState.FIRST_TURN;
		OBJECT_ID =  gameId;
		setUpGame();
	}

	//Sets up game
	protected void setUpGame() {
		lastScreenIsConversation = true;

		setUpGameScreen();

		setUpHeader();
		gameScreen.getChildren().add(header);

		setUpBackButton();
		header.getChildren().add(backButton);

	}

	//Called to send a message to another client
	//Sends the message via main class
	//Formatted in:
	//		recipient name:recipient class:type:message
	public void sendMessageToClient(String message) {
		if(conversation.isOnline()) {
			conversation.sendMessageToClient(conversation.getRecipientName(), MessageScene.CLASS_ID, OBJECT_ID, message);
			conversation.gameMessageSent(getGameDisplay(), toString());
		}
		else {
			conversation.checkIfConversationIsOffline();
		}
	}

	//handles messages received
	public abstract void messageReceived(String message);

	//Returns game screen
	public VBox getGameScreen() {
		return gameScreen;
	}

	//Sets screen content
	protected void setScreenContent(Node n) {
		conversation.setScreenContent(n);
	}

	//Sets up game screen and handles resizing
	protected void setUpGameScreen() {
		gameScreen = new VBox();
		gameScreen.prefHeightProperty().bind(conversation.getScreenBox().heightProperty());
		gameScreen.prefWidthProperty().bind(conversation.getScreenBox().widthProperty());
		
		gameScreen.widthProperty().addListener(new ChangeListener<Number>() {
			@Override 
			public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
				Scale scale = new Scale();
				scale.setX(conversation.getScreenBox().getScene().getWidth() / 790);
				scale.setY(conversation.getScreenBox().getScene().getHeight() / 590);
				gameScreen.getTransforms().clear();
				gameScreen.getTransforms().add(scale);
			}
		});
		gameScreen.heightProperty().addListener(new ChangeListener<Number>() {
			@Override 
			public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
				Scale scale = new Scale();
				scale.setX(conversation.getScreenBox().getScene().getWidth() / 790);
				scale.setY(conversation.getScreenBox().getScene().getHeight() / 590);
				gameScreen.getTransforms().clear();
				gameScreen.getTransforms().add(scale);		    
			}
		});


	}

	//Sets up header
	protected void setUpHeader() {
		header = new HBox();
		header.prefWidthProperty().bind(conversation.getScreenBox().prefWidthProperty());
	}

	//returns string represenation
	@Override
	public String toString() {
		return "game";
	}

	//Returns game display
	public VBox getGameDisplay() {
		VBox gameDisplay = new VBox();
		gameDisplay.setAlignment(Pos.CENTER);
		gameDisplay.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				conversation.setScreenContent(gameScreen);
			}
		});

		LocalToolkit.addGlowEffect(gameDisplay, 0.5);
		return gameDisplay;
	}

	//Returns this object
	protected Game getObject() {
		return this;
	}

	//Loads the menu display
	protected static VBox loadMenuDisplay(String img, String caption, int width, int height) {
		VBox b = new VBox();
		b.getChildren().add(LocalToolkit.loadImg(img, width, height));
		Label cap = new Label(caption);
		cap.setStyle("-fx-font-size: 20px;");
		b.getChildren().add(cap);
		b.setAlignment(Pos.BOTTOM_CENTER);
		return b;
	}

	//used by 8 ball class
	protected void switchTo8Ball() {}


	/*
	 * PRIVATE METHODS
	 */

	//returns object id
	private static String getObjectId() {
		return getAddress() + numGames;
	}

	//returns the ip address
	private static String getAddress() {
		String address = "unknown";

		try {
			address = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		return address;
	}

	//sets up back button
	private void setUpBackButton() {
		backButton = new Button();
		backButton.setId("back-button");

		ImageView img = LocalToolkit.loadImg("BackButton.png", 50, 50);
		img.setEffect(new ColorAdjust(0,0,-.4,0));
		backButton.setGraphic(img);
		LocalToolkit.addGlowEffect(backButton);

		//Adding action listener
		backButton.addEventHandler(MouseEvent.MOUSE_CLICKED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {

				//Switching screen content
				if(lastScreenIsConversation)
					setScreenContent(conversation.getContent());
				else
					switchTo8Ball();
			}
		});

	}

}
