/*
 * Programmer: Andres Carranza
 * Date: 5/16/2019
 *
 * CLASS DETAILS:
 	- This class connects to the server
 */

import java.io.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.scene.effect.*;
import javafx.scene.paint.*;

public class ConnectScene {
	public static final String CLASS_ID = "connect scene";//Used to identify this class when distributing messages from the server
	private Scene connectScene;//Scene for window prompting user to connect to the server
	private Main main;//Used to communicate with the main object
	private TextField connectCodeField;//Field for entering server connect code
	private TextField usernameField;//Field for entering username
	private Text connectErrorMsg;//Error messaged displayed when error occurred connecting to the server
	private Text usernameErrorMsg;//Error messaged displayed when error occurred setting a username
	private String username;//String containing username
	private BorderPane pane;//Pane containing the graphics
	private GridPane grid;//Grid containing form GUI
	private Text connectHeader;//Form header
	private Text connectInstructions;//Text with instructions to connect

	//Constructor
	//Sets up all GUI
	public ConnectScene(Main main) {
		this.main = main;

		//Setting up border pane
		setUpBorderPane();

		//Adding the grid to the pane
		setUpGridPane();
		pane.setCenter(grid);

		//Adding header to the grid
		setUpConnectHeader();
		grid.add(connectHeader, 0, 0, 2, 1);

		//Adding connectInstructions
		setUpConnectInstructions();
		grid.add(connectInstructions, 0, 1,2,1);

		//Creating a field for the user to enter the text, adding it to the grid
		connectCodeField = new TextField();
		grid.add(connectCodeField, 0, 2,2 ,1);


		//Creating an error message and adding it to the grid
		connectErrorMsg = new Text();
		connectErrorMsg.setFont(new Font("Arial", 15));
		connectErrorMsg.setFill(Color.FIREBRICK);
		grid.add(connectErrorMsg, 0, 3, 2, 1);

		//Adding the name instructions to the grid
		Text nameInstructions = new Text("Enter username");
		nameInstructions.setId("instructions");
		grid.add(nameInstructions,0,4, 2, 1);

		//Adding user name input
		usernameField = new TextField();
		grid.add(usernameField, 0, 5,2 ,1);

		//Adding username error msg to the grid
		usernameErrorMsg = new Text();
		usernameErrorMsg.setFont(new Font("Arial", 15));
		usernameErrorMsg.setFill(Color.FIREBRICK);
		grid.add(usernameErrorMsg, 0, 6, 2, 1);

		//Creating the connect button with an image as a background
		Button connectButton = new Button();
		FileInputStream inputstream = null;
		try {
			inputstream = new FileInputStream("ConnectButton.png");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ImageView connectBtnImg = new ImageView( new Image(inputstream, 40, 40, false, false)); 
		connectButton.setGraphic(connectBtnImg);

		//Adding an effect so that when a mouse hovers above the button, it glows
		connectButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				Glow glow = new Glow();
				glow.setLevel(0.5); 
				connectButton.setEffect(glow);
			}
		});

		//Reseting button to default once the mouse has exited
		connectButton.addEventHandler(MouseEvent.MOUSE_EXITED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				connectButton.setEffect(null);
			}
		});

		//Getting the text from the text field once user clicks the connect button
		connectButton.addEventHandler(MouseEvent.MOUSE_CLICKED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				connect();
			}
		});

		//Adding the button to the grid
		grid.add(connectButton, 2, 5);



		//Creating a scene object 
		connectScene = new Scene(pane); 

		//Adding css sheet to the scene
		connectScene.getStylesheets().add(getClass().getResource("ConnectScene.css").toExternalForm());



	}

	//Returns scene object
	public Scene getScene() {
		return connectScene;
	}

	//Called if username is valid
	public void usernameIsValid() {
		main.setStageToMessageScene();
		main.setUsername(username);

	}

	//Called if username is invalid
	public void usernameIsInvalid() {	
		main.closeConnection();
		usernameField.setText("");
		usernameErrorMsg.setText("Invalid Username");
		connectErrorMsg.setText("");
	}

	/*****************
	 *PRIVATE METHODS*
	 *****************
	 */

	//Sets up connect instructions
	private void setUpConnectInstructions() {
		//Creating the instructions text and adding it to the grid
		connectInstructions = new Text("Enter the connect-code displayed\non the server to connect");
		connectInstructions.setId("instructions");
	}

	//Sets up header
	private void setUpConnectHeader(){
		//Creating the header text and adding it to the grid
		connectHeader = new Text("Connect");
		connectHeader.setId("header");
	}
	//Sets up grid pane
	private void setUpGridPane() {
		//Creating a grid pane layout
		grid = new GridPane();
		grid.setId("grid");
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
	}

	//Setting up border pane
	private void setUpBorderPane() {
		//Creating a border pane layout
		pane = new BorderPane();

		//Adding an action listener to check if user has pressed enter
		pane.setOnKeyPressed(
				new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent e) {
						if(e.getCode() == KeyCode.ENTER) 
							connect();
					}
				});
	}

	//Called when connect button or enter is pressed
	//Attempts to connect to server and displays necessary error messages
	private void connect() {
		if(!usernameField.getText().isEmpty() && !connectCodeField.getText().isEmpty()) {
			try {			
				username = usernameField.getText();
				main.connect(connectCodeField.getText(), username);
			} catch (IOException e) {
				connectCodeField.setText("");
				connectErrorMsg.setText("Unable to connect to server. Try again");
				usernameErrorMsg.setText("");
			}	
		}
		else if(!usernameField.getText().isEmpty()) {
			connectErrorMsg.setText("Enter server connect code");
			usernameErrorMsg.setText("");
		}
		else if(!connectCodeField.getText().isEmpty()) {
			usernameErrorMsg.setText("Enter a username");
			connectErrorMsg.setText("");
		}
		else {
			connectErrorMsg.setText("Enter server connect code");
			usernameErrorMsg.setText("Enter a username");
		}
	}

}
