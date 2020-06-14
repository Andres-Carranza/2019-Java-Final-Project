/*
 * Andres Carranza and Brandon Butsch
 * 5/28/2019
 * Class handles graphics and logic for a connect four game
 */

import java.awt.Point;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;

public class ConnectFour extends Game{
	public static final String CLASS_ID = "connectfour";
	private Text caption;
	private ImageView headerImage;
	private VBox playerHeaders;
	private HBox redHeader;
	private HBox yellowHeader;
	private Text redText;
	private Text yellowText;
	private boolean isRed;
	private HBox connectFourBox;
	private GridPane connectFourGrid;
	private StackPane connectFourSlots[][];
	private char values[][];
	private int[] lowestOpen;

	//Constructor
	public ConnectFour(Conversation conversation) {
		super(conversation);
	}

	//Constructor when someone else created the game
	public ConnectFour(Conversation conversation, String gameId) {
		super(conversation, gameId);
	}


	//Sets up game
	@Override
	protected void setUpGame() {
		super.setUpGame();
		isRed = gameState == GameState.FIRST_TURN;
		connectFourSlots = new StackPane[7][6];
		values = new char[7][6];
		lowestOpen = new int[]{5,5,5,5,5,5,5};

		setUpHeaderImage();
		header.getChildren().add(headerImage);

		setUpPlayerHeaders();
		header.getChildren().add(playerHeaders);

		setUpRedHeader();
		playerHeaders.getChildren().add(redHeader);

		setUpYellowHeader();
		playerHeaders.getChildren().add(yellowHeader);

		setUpConnectFourBox();
		gameScreen.getChildren().add(connectFourBox);

		setUpConnectFourGrid();
		connectFourBox.getChildren().add(connectFourGrid);


	}


	//Sets up the game display
	public VBox getGameDisplay() {
		VBox gameDisplay = super.getGameDisplay();
		gameDisplay.getChildren().add(LocalToolkit.scale(connectFourGrid.snapshot(null, null),200, 200, true));

		if(gameState == GameState.FIRST_TURN || gameState == GameState.INVITE) {
			caption = new Text("Play me in Connect Four!");
			gameDisplay.getChildren().add(caption);
		}
		else if(gameState == GameState.AWAY_TURN || gameState == GameState.LOCAL_TURN ) {
			caption = new Text("Your turn!");
			gameDisplay.getChildren().add(caption);
		}
		else if(gameState == GameState.LOCAL_WIN || gameState == GameState.AWAY_WIN) {
			caption = new Text("I won!");
			gameDisplay.getChildren().add(caption);
		}
		else {
			caption = new Text("You tied!");
			gameDisplay.getChildren().add(caption);
		}
		return gameDisplay;
	}

	//Returns the menu display
	public static VBox getMenuDisplay() {
		return loadMenuDisplay("ConnectFour.png", "Connect Four",120, 100);
	}


	//Handles a message received from the server
	@Override
	public void messageReceived(String message) {
		if(!message.equals(CLASS_ID) ){
			gameState = GameState.LOCAL_TURN;
			int column = Integer.parseInt(message.substring(0,1));
			int row = getLowestOpen(column);
			connectFourSlots[column][row] = getOpponentSquare();
			renderConnectFourGrid();
			if(isRed) {
				redText.setText("'s turn (you)");
				yellowText.setText("");
				values[column][row] = 'Y';

			}
			else {
				yellowText.setText("'s turn  (you)");
				redText.setText("");
				values[column][row] = 'R';
			}

			checkForWinner();
		}

	}


	//Sets up the game screen
	@Override
	public void setUpGameScreen() {
		super.setUpGameScreen();
		gameScreen.setId(CLASS_ID);
	}

	//Sets up the header
	@Override
	public void setUpHeader() {
		super.setUpHeader();
		header.setId(CLASS_ID+"-header");
		header.setAlignment(Pos.CENTER_LEFT);
	}

	//Sets up the header image
	private void setUpHeaderImage() {
		headerImage = LocalToolkit.loadImg("ConnectFourHeader.png", 400, 100);
	}

	//Sets up the player headers
	private void setUpPlayerHeaders() {
		playerHeaders = new VBox();
		playerHeaders.setSpacing(10);
	}

	//Returns red game piece
	private ImageView getRed() {
		return LocalToolkit.loadImg("Red.png", 50, 50);
	}

	//Return yellow game piece
	private ImageView getYellow() {
		return LocalToolkit.loadImg("Yellow.png", 50, 50);
	}

	//Sets up the red header
	private void setUpRedHeader() {
		redHeader = new HBox();
		redHeader.getChildren().add(getRed());
		redHeader.setAlignment(Pos.CENTER_LEFT);
		redHeader.setSpacing(5);


		if(isRed)
			redText = new Text("'s turn (you)");
		else
			redText = new Text("'s turn");

		redText.setId(CLASS_ID+"-header-text");
		redHeader.getChildren().add(redText);
	}

	//Sets up the yellow header
	private void setUpYellowHeader() {
		yellowHeader = new HBox();
		yellowHeader.getChildren().add(getYellow());
		yellowHeader.setAlignment(Pos.CENTER_LEFT);
		yellowHeader.setSpacing(5);

		if(!isRed)
			yellowText = new Text(" (you)");
		else
			yellowText = new Text();
		yellowText.setId(CLASS_ID+"-header-text");
		yellowHeader.getChildren().add(yellowText);
	}

	//Sets up the connect four box
	private void setUpConnectFourBox() {
		connectFourBox = new HBox();
		connectFourBox.prefHeightProperty().bind(conversation.getScreenBox().heightProperty());
		connectFourBox.prefWidthProperty().bind(conversation.getScreenBox().widthProperty());

		connectFourBox.setStyle("-fx-background-color: #add8e6");

	}

	//Sets up the connect four grid
	private void setUpConnectFourGrid() {
		connectFourGrid = new GridPane();

		connectFourGrid.setTranslateX(140);
		connectFourGrid.setTranslateY(20);

		for(int x = 0; x < connectFourSlots.length; x++) {
			for(int y = 0; y < connectFourSlots[0].length; y++) {
				connectFourSlots[x][y] = getEmptySlot();
				connectFourGrid.add(connectFourSlots[x][y], x, y);
			}

		}
		connectFourGrid.setMaxHeight(0);

	}

	//Updates connect four grid
	private void renderConnectFourGrid() {
		connectFourGrid = new GridPane();

		connectFourGrid.setTranslateX(140);
		connectFourGrid.setTranslateY(20);

		for(int x = 0; x < connectFourSlots.length; x++) {
			for(int y = 0; y < connectFourSlots[0].length; y++) {
				connectFourGrid.add(connectFourSlots[x][y], x, y);
			}

		}
		connectFourGrid.setMaxHeight(0);

		connectFourBox.getChildren().remove(0);
		connectFourBox.getChildren().add(connectFourGrid);

	}

	//Returns the game piece
	private ImageView getGamePiece() {
		if(isRed)
			return LocalToolkit.loadImg("Red.png", 60, 60);
		return LocalToolkit.loadImg("Yellow.png", 60,60);
	}

	//Returns the opponent game piece
	private ImageView getOpponentGamePiece() {
		if(isRed)
			return LocalToolkit.loadImg("Yellow.png", 60,60);
		return LocalToolkit.loadImg("Red.png", 60, 60);
	}

	//Returns an opponent square
	private StackPane getOpponentSquare() {
		StackPane slot = new StackPane();
		slot.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
		slot.setPadding(new Insets(5));

		slot.getChildren().add(getOpponentGamePiece());

		return slot;
	}

	//Returns an empty slot
	private StackPane getEmptySlot() {
		StackPane slot = new StackPane();
		slot.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
		slot.setPadding(new Insets(5));

		Circle c = new Circle(0, 0, 30);
		c.setFill(Color.LIGHTGREY);
		LocalToolkit.addGlowEffect(c, 1);
		slot.getChildren().add(c);

		slot.addEventHandler(MouseEvent.MOUSE_CLICKED,new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				if(gameState == GameState.LOCAL_TURN || gameState == GameState.FIRST_TURN) {
					int column = GridPane.getColumnIndex(slot);
					int row = getLowestOpen(column);

					connectFourSlots[column][row].getChildren().clear();
					connectFourSlots[column][row].getChildren().add(getGamePiece());
					connectFourSlots[column][row].removeEventHandler(MouseEvent.MOUSE_CLICKED, this);


					gameState = GameState.AWAY_TURN;
					if(isRed) {
						yellowText.setText("'s turn");
						redText.setText(" (you)");
						values[column][row] = 'R';
					}
					else {
						redText.setText("'s turn");
						yellowText.setText(" (you)");
						values[column][row] = 'Y';
					}
					checkForWinner();
					sendMessageToClient(column +"");
				}
			}
		});

		return slot;
	}


	//Returns lowest possible row for column coumn
	private int getLowestOpen(int column) {
		int lowest = lowestOpen[column];
		lowestOpen[column]--;
		return lowest;
	}

	//checks if board is full
	private boolean isFull() {

		for(char rows[] : values)
			for(char value : rows)
				if(value != 'R' && value != 'Y')
					return false;

		return true;
	}

	//Checks if someone has won
	private void checkForWinner() {

		if(checkColumns())
			return;
		else if(checkRows())
			return;
		else if(checkDiagonals())
			return;


		if(isFull()) {
			ImageView board = new ImageView(connectFourGrid.snapshot(null, null));
			connectFourBox.getChildren().remove(connectFourGrid);
			connectFourBox.getChildren().add(board);
			board.setTranslateX(140);
			board.setTranslateY(20);
			conversation.gameTied();
			if(isRed) {
				redText.setText(" tied (you)");
				yellowText.setText(" tied");
			}
			else {
				yellowText.setText(" tied (you)");
				redText.setText(" tied");
			}
			gameState = GameState.TIE;
		}
	}

	//Checks if any columns contain a winner
	private boolean checkColumns() {
		int columnIndex = 0;
		Point positions[] = new Point[7];
		for(char column[] : values) {
			for(int i = 0; i< column.length; i++) {
				positions[i] = new Point(columnIndex, i);//x is column y is row
			}
			columnIndex++;
			if(checkArray(column,positions))
				return true;
		}
		return false;
	}

	//Checks if any rows contain a winner
	private boolean checkRows() {
		char[] row = new char[7];
		Point positions[] = new Point[7];

		for(int rowIndex = 0; rowIndex < 6; rowIndex++) {
			for(int columnIndex = 0; columnIndex < 7; columnIndex++ ) {
				row[columnIndex] = values[columnIndex][rowIndex];
				positions[columnIndex] = new Point(columnIndex, rowIndex);//x is column y is row
			}

			if(checkArray(row,positions))
				return true;
		}



		return false;
	}

	//Checks if any diagonals cotain a winner
	private boolean checkDiagonals() {

		for(int i = 0; i< 12; i++) {
			if(checkDiagonal(i))
				return true;
		}
		return false;
	}

	//Checks if a diagonal contains a winner
	private boolean checkDiagonal(int i) {
		char[] diagonal;
		Point positions[];
		switch(i) {
		case 0: 
			diagonal = new char[] {values[0][2],values[1][3],values[2][4],values[3][5]};
			positions = new Point[]{new Point(0,2),new Point(1,3),new Point(2,4),new Point(3,5)};
			return checkArray(diagonal,positions);
		case 1: 
			diagonal = new char[] {values[0][1],values[1][2],values[2][3],values[3][4],values[4][5]};
			positions = new Point[]{new Point(0,1),new Point(1,2),new Point(2,3),new Point(3,4), new Point(4,5)};
			return checkArray(diagonal,positions);
		case 2: 
			diagonal = new char[] {values[0][0],values[1][1],values[2][2],values[3][3],values[4][4], values[5][5]};
			positions = new Point[]{new Point(0,0),new Point(1,1),new Point(2,2),new Point(3,3), new Point(4,4), new Point(5,5)};
			return checkArray(diagonal,positions);
		case 3: 
			diagonal = new char[] {values[1][0],values[2][1],values[3][2],values[4][3],values[5][4], values[6][5]};
			positions = new Point[]{new Point(1,0),new Point(2,1),new Point(3,2),new Point(4,3), new Point(5,4), new Point(6,5)};
			return checkArray(diagonal,positions);
		case 4: 
			diagonal = new char[] {values[2][0],values[3][1],values[4][2],values[5][3],values[6][4]};
			positions = new Point[]{new Point(2,0),new Point(3,1),new Point(4,2),new Point(5,3), new Point(6,4)};
			return checkArray(diagonal,positions);
		case 5: 
			diagonal = new char[] {values[3][0],values[4][1],values[5][2],values[6][3]};
			positions = new Point[]{new Point(3,0),new Point(4,1),new Point(5,2),new Point(6,3)};
			return checkArray(diagonal,positions);
		case 6: 
			diagonal = new char[] {values[6][2],values[5][3],values[4][4],values[3][5]};
			positions = new Point[]{new Point(6,2),new Point(5,3),new Point(4,4),new Point(3,5)};
			return checkArray(diagonal,positions);
		case 7: 
			diagonal = new char[] {values[6][1],values[5][2],values[4][3],values[3][4], values[2][5]};
			positions = new Point[]{new Point(6,1), new Point(5,2),new Point(4,3), new Point(3,4), new Point(2,5)};
			return checkArray(diagonal,positions);
		case 8: 
			diagonal = new char[] {values[6][0],values[5][1],values[4][2],values[3][3], values[2][4], values[1][5]};
			positions = new Point[]{new Point(6,0), new Point(5,1),new Point(4,2), new Point(3,3), new Point(2,4), new Point(1,5)};
			return checkArray(diagonal,positions);
		case 9: 
			diagonal = new char[] {values[5][0],values[4][1],values[3][2],values[2][3], values[1][4], values[0][5]};
			positions = new Point[]{new Point(5,0), new Point(4,1),new Point(3,2), new Point(2,3), new Point(1,4), new Point(0,5)};
			return checkArray(diagonal,positions);
		case 10: 
			diagonal = new char[] {values[4][0],values[3][1],values[2][2],values[1][3], values[0][4]};
			positions = new Point[]{new Point(4,0), new Point(3,1),new Point(2,2), new Point(1,3), new Point(0,4)};
			return checkArray(diagonal,positions);
		case 11: 
			diagonal = new char[] {values[3][0],values[2][1],values[1][2],values[0][3]};
			positions = new Point[]{new Point(3,0), new Point(2,1),new Point(1,2), new Point(0,3)};
			return checkArray(diagonal,positions);
		}
		return false;
	}

	//Checks if an array of squares contains a winner
	private boolean checkArray(char array[], Point[] positions) {
		int numInARow = 1;
		Point[] fourInARow = new Point[4]; 
		fourInARow[0] = positions[0];
		for(int i = 1; i < array.length; i++) {
			if(array[i] == array[i-1] && (array[i] == 'R' || array[i] == 'Y') ) {
				fourInARow[numInARow ] = positions[i];
				numInARow++;
			}
			else {
				numInARow = 1;
				fourInARow[0] = positions[i];
			}
			if(numInARow == 4) {
				won(array[i], fourInARow);
				return true;
			}
		}
		return false;
	}

	//Handles a win
	private void won(char winner, Point[] fourInARow) {
		highlightArray(fourInARow);
		ImageView board = new ImageView(connectFourGrid.snapshot(null, null));

		connectFourBox.getChildren().remove(connectFourGrid);
		connectFourBox.getChildren().add(board);

		board.setTranslateX(140);
		board.setTranslateY(20);

		if(winner == 'R') {
			if(isRed) {
				redText.setText(" won (you)");
				yellowText.setText(" lost");
				conversation.gameWon();
				gameState = GameState.LOCAL_WIN;
			}
			else {
				redText.setText(" won");
				yellowText.setText(" lost (you)");
				gameState = GameState.AWAY_WIN;
				conversation.gameLost();

			}
		}
		else {
			if(isRed) {
				yellowText.setText(" won");
				redText.setText(" lost (you)");
				gameState = GameState.AWAY_WIN;
				conversation.gameLost();
			}
			else {
				yellowText.setText(" won (you)");
				redText.setText(" lost");
				conversation.gameWon();

				gameState = GameState.LOCAL_WIN;
			}

		}




	}

	//Highlights the winning array
	private void highlightArray(Point[] fourInARow) {
		for(Point p : fourInARow) {
			connectFourSlots[p.x][p.y].setBackground(new Background(new BackgroundFill(Color.rgb(57,255,20), CornerRadii.EMPTY, Insets.EMPTY)));
		}
	}
}
