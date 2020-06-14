/*
 * Andres Carranza and Ethan Pereira
 * 5/28/2019
 * This code represents a Tic Tac Toe Game, 2 Player, one player plays X, And the other plays O.
 */
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;

public class TicTacToe extends Game{
	public static final String CLASS_ID ="tic-tac-toe";
	private Text caption;
	private ImageView headerImage;
	private VBox playerHeaders;
	private HBox oHeader;
	private HBox xHeader;
	private Text oText;
	private Text xText;
	private GridPane ticTacToeGrid;
	private HBox ticTacToeBox;
	private boolean isX;
	private StackPane[][] ticTacToeSquares;
	private char[][] values;

	//Constructor
	public TicTacToe(Conversation conversation) {
		super(conversation);
	}

	//Constrctor for away game
	public TicTacToe(Conversation conversation, String gameId) {
		super(conversation, gameId);
	}

	//Sets up the game screen
	@Override
	public void setUpGameScreen() {
		super.setUpGameScreen();
		gameScreen.setId(CLASS_ID);
	}

	//Handles a message received
	@Override
	public void messageReceived(String message) {
		if(!message.equals(CLASS_ID) ){
			gameState = GameState.LOCAL_TURN;
			ticTacToeSquares[Integer.parseInt(message.substring(0,1))][Integer.parseInt(message.substring(1))] = getOpponentSquare();
			renderTicTacToeGrid();
			if(isX) {
				xText.setText("'s turn (you)");
				oText.setText("");
				values[Integer.parseInt(message.substring(0,1))][Integer.parseInt(message.substring(1))] = 'O';

			}
			else {
				oText.setText("'s turn  (you)");
				xText.setText("");
				values[Integer.parseInt(message.substring(0,1))][Integer.parseInt(message.substring(1))] = 'X';
			}

			checkForWinner();
		}
	}

	//returns string represantation
	@Override
	public String toString() {
		return "Tic-Tac-Toe";
	}

	//returns menu display
	public static VBox getMenuDisplay() {
		return loadMenuDisplay("TicTacToe.png", "Tic-Tac-Toe",100,100);
	}

	//returns game display
	@Override
	public VBox getGameDisplay() {
		VBox gameDisplay = super.getGameDisplay();

		if(gameState == GameState.FIRST_TURN || gameState == GameState.INVITE) {
			gameDisplay.getChildren().add(LocalToolkit.scale(ticTacToeGrid.snapshot(null, null),200, 200, true));
			caption = new Text("Play me in Tic-Tac-Toe!");
			gameDisplay.getChildren().add(caption);
		}
		else if(gameState == GameState.AWAY_TURN || gameState == GameState.LOCAL_TURN ) {
			gameDisplay.getChildren().add(LocalToolkit.scale(ticTacToeGrid.snapshot(null, null),200, 200, true));
			caption = new Text("Your turn!");
			gameDisplay.getChildren().add(caption);
		}
		else if(gameState == GameState.LOCAL_WIN || gameState == GameState.AWAY_WIN) {
			gameDisplay.getChildren().add(LocalToolkit.scale(ticTacToeGrid.snapshot(null, null),200, 200, true));
			caption = new Text("I won!");
			gameDisplay.getChildren().add(caption);
		}
		else {
			gameDisplay.getChildren().add(LocalToolkit.scale(ticTacToeGrid.snapshot(null, null),200, 200, true));
			caption = new Text("We tied!");
			gameDisplay.getChildren().add(caption);
		}

		return gameDisplay;
	}

	//returns header
	@Override
	public void setUpHeader() {
		super.setUpHeader();
		header.setId(CLASS_ID+"-header");
		header.setAlignment(Pos.CENTER_LEFT);
	}

	//returns object
	@Override
	protected TicTacToe getObject() {
		return this;
	}

	//sets up game
	@Override
	protected void setUpGame() {
		super.setUpGame();

		isX = gameState == GameState.FIRST_TURN;
		ticTacToeSquares = new StackPane[3][3];
		values = new char[3][3];

		setUpHeaderImage();
		header.getChildren().add(headerImage);

		setUpPlayerHeaders();
		header.getChildren().add(playerHeaders);

		setUpXHeader();
		playerHeaders.getChildren().add(xHeader);

		setUpOHeader();
		playerHeaders.getChildren().add(oHeader);

		setUpTicTacToeBox();
		gameScreen.getChildren().add(ticTacToeBox);

		setUpTicTacToeGrid();
		ticTacToeBox.getChildren().add(ticTacToeGrid);

	}

	//sets up header image
	private void setUpHeaderImage() {		
		headerImage = LocalToolkit.loadImg("TicTacToeHeader.png", 400,100);
	}

	//sets up playe headers
	private void setUpPlayerHeaders() {
		playerHeaders = new VBox();
		playerHeaders.setSpacing(10);
	}

	//returns x image
	private ImageView getX() {
		return LocalToolkit.loadImg("TicTacToeX.png", 45, 45);
	}

	//returns o image
	private ImageView getO() {
		return LocalToolkit.loadImg("TicTacToeO.png", 45, 45);
	}

	//sets up o header
	private void setUpOHeader() {
		oHeader = new HBox();
		oHeader.getChildren().add(getO());
		oHeader.setAlignment(Pos.CENTER_LEFT);
		oHeader.setSpacing(5);

		if(!isX)
			oText = new Text(" (you)");
		else
			oText = new Text();
		oText.setId(CLASS_ID+"-header-text");
		oHeader.getChildren().add(oText);
	}

	//sets up x header
	private void setUpXHeader() {
		xHeader = new HBox();
		xHeader.getChildren().add(getX());
		xHeader.setAlignment(Pos.CENTER_LEFT);
		xHeader.setSpacing(5);

		if(isX)
			xText = new Text("'s turn (you)");
		else
			xText = new Text("'s turn");
		xText.setId(CLASS_ID+"-header-text");
		xHeader.getChildren().add(xText);
	}

	//sets up tic tac toe box
	private void setUpTicTacToeBox() {
		ticTacToeBox = new HBox();
		ticTacToeBox.prefHeightProperty().bind(conversation.getScreenBox().heightProperty());
		ticTacToeBox.prefWidthProperty().bind(conversation.getScreenBox().widthProperty());
	}

	//sets up tic tac toe grid
	private void setUpTicTacToeGrid() {

		ticTacToeGrid = new GridPane();
		ticTacToeGrid.setTranslateX(175);
		ticTacToeGrid.setTranslateY(15);

		ticTacToeSquares[0][0] = getEmptySquare();
		ticTacToeGrid.add(ticTacToeSquares[0][0], 0, 0);

		ticTacToeSquares[0][1] = getEmptySquare();
		ticTacToeGrid.add(ticTacToeSquares[0][1], 0, 1);

		ticTacToeSquares[0][2] = getEmptySquare();
		ticTacToeGrid.add(ticTacToeSquares[0][2], 0, 2);

		ticTacToeSquares[1][0] = getEmptySquare();
		ticTacToeGrid.add(ticTacToeSquares[1][0], 1, 0);

		ticTacToeSquares[1][1] = getEmptySquare();
		ticTacToeGrid.add(ticTacToeSquares[1][1], 1, 1);

		ticTacToeSquares[1][2] = getEmptySquare();
		ticTacToeGrid.add(ticTacToeSquares[1][2], 1, 2);

		ticTacToeSquares[2][0] = getEmptySquare();
		ticTacToeGrid.add(ticTacToeSquares[2][0], 2, 0);

		ticTacToeSquares[2][1] = getEmptySquare();
		ticTacToeGrid.add(ticTacToeSquares[2][1], 2, 1);

		ticTacToeSquares[2][2] = getEmptySquare();
		ticTacToeGrid.add(ticTacToeSquares[2][2], 2, 2);

		ticTacToeGrid.setGridLinesVisible(true);
		ticTacToeGrid.setMaxHeight(0);



	}

	//updates tic tac toe grid
	private void renderTicTacToeGrid() {

		ticTacToeGrid = new GridPane();
		ticTacToeGrid.setTranslateX(175);
		ticTacToeGrid.setTranslateY(15);
		
		ticTacToeGrid.add(ticTacToeSquares[0][0], 0, 0);

		ticTacToeGrid.add(ticTacToeSquares[0][1], 0, 1);

		ticTacToeGrid.add(ticTacToeSquares[0][2], 0, 2);

		ticTacToeGrid.add(ticTacToeSquares[1][0], 1, 0);

		ticTacToeGrid.add(ticTacToeSquares[1][1], 1, 1);

		ticTacToeGrid.add(ticTacToeSquares[1][2], 1, 2);

		ticTacToeGrid.add(ticTacToeSquares[2][0], 2, 0);

		ticTacToeGrid.add(ticTacToeSquares[2][1], 2, 1);

		ticTacToeGrid.add(ticTacToeSquares[2][2], 2, 2);

		ticTacToeGrid.setGridLinesVisible(true);
		ticTacToeGrid.setMaxHeight(0);

		ticTacToeBox.getChildren().remove(0);
		ticTacToeBox.getChildren().add(ticTacToeGrid);


	}
	
	//returns game piece
	private ImageView getGamePiece() {
		if(isX)
			return LocalToolkit.loadImg("TicTacToeX.png", 120, 120);
		return LocalToolkit.loadImg("TicTacToeO.png", 120, 120);
	}

	//returns opponent game piece
	private ImageView getOpponentGamePiece() {
		if(isX)
			return LocalToolkit.loadImg("TicTacToeO.png", 120, 120);
		return LocalToolkit.loadImg("TicTacToeX.png", 120, 120);
	}

	//returns empty square
	private StackPane getEmptySquare() {
		StackPane square = new StackPane();
		Rectangle r = new Rectangle(140,140);
		r.setFill(Color.ORANGE);
		LocalToolkit.addGlowEffect(square, 1);


		square.addEventHandler(MouseEvent.MOUSE_CLICKED,new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				if(gameState == GameState.LOCAL_TURN || gameState == GameState.FIRST_TURN) {
					square.getChildren().add(getGamePiece());
					square.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);
					gameState = GameState.AWAY_TURN;
					if(isX) {
						oText.setText("'s turn");
						xText.setText(" (you)");
						values[GridPane.getColumnIndex(square)][GridPane.getRowIndex(square)] = 'X';
					}
					else {
						xText.setText("'s turn");
						oText.setText(" (you)");
						values[GridPane.getColumnIndex(square)][GridPane.getRowIndex(square)] = 'O';
					}
					checkForWinner();
					sendMessageToClient(GridPane.getColumnIndex(square) + "" + GridPane.getRowIndex(square));
				}
			}
		});

		square.getChildren().add(r);
		return square;
	}

	//returns winnner square
	private Rectangle getWinnerSquare() {
		Rectangle r = new Rectangle(140,140);
		r.setFill(Color.GREEN);
		r.setEffect(new Glow(1.0));
		return r;
	}

	//returns opponent square
	private StackPane getOpponentSquare() {
		StackPane square = new StackPane();
		Rectangle r = new Rectangle(140,140);
		r.setFill(Color.ORANGE);
		LocalToolkit.addGlowEffect(square, 1);

		square.getChildren().add(r);
		square.getChildren().add(getOpponentGamePiece());

		return square;
	}



	//checks for a winner
	private void checkForWinner() {
		boolean winner = false;
		boolean xWin = false;
		if(checkRows().charAt(0) == 'X') {
			winner = true;
			xWin = true;
			if(checkRows().charAt(1) == '1') {
				ticTacToeSquares[0][0].getChildren().remove(0);
				ticTacToeSquares[0][0].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[1][0].getChildren().remove(0);
				ticTacToeSquares[1][0].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[2][0].getChildren().remove(0);
				ticTacToeSquares[2][0].getChildren().add( 0, getWinnerSquare());
			}
			else if(checkRows().charAt(1) == '2') {
				ticTacToeSquares[0][1].getChildren().remove(0);
				ticTacToeSquares[0][1].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[1][1].getChildren().remove(0);
				ticTacToeSquares[1][1].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[2][1].getChildren().remove(0);
				ticTacToeSquares[2][1].getChildren().add( 0, getWinnerSquare());
			}
			else {
				ticTacToeSquares[0][2].getChildren().remove(0);
				ticTacToeSquares[0][2].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[1][2].getChildren().remove(0);
				ticTacToeSquares[1][2].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[2][2].getChildren().remove(0);
				ticTacToeSquares[2][2].getChildren().add( 0, getWinnerSquare());
			}
			renderTicTacToeGrid();
		}
		else if(checkRows().charAt(0) == 'O') {
			winner = true;
			xWin = false;
			if(checkRows().charAt(1) == '1') {
				ticTacToeSquares[0][0].getChildren().remove(0);
				ticTacToeSquares[0][0].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[1][0].getChildren().remove(0);
				ticTacToeSquares[1][0].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[2][0].getChildren().remove(0);
				ticTacToeSquares[2][0].getChildren().add( 0, getWinnerSquare());
			}
			else if(checkRows().charAt(1) == '2') {
				ticTacToeSquares[0][1].getChildren().remove(0);
				ticTacToeSquares[0][1].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[1][1].getChildren().remove(0);
				ticTacToeSquares[1][1].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[2][1].getChildren().remove(0);
				ticTacToeSquares[2][1].getChildren().add( 0, getWinnerSquare());
			}
			else {
				ticTacToeSquares[0][2].getChildren().remove(0);
				ticTacToeSquares[0][2].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[1][2].getChildren().remove(0);
				ticTacToeSquares[1][2].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[2][2].getChildren().remove(0);
				ticTacToeSquares[2][2].getChildren().add( 0, getWinnerSquare());
			}
			renderTicTacToeGrid();
		}
		else if(checkColumns().charAt(0) == 'X') {
			winner = true;
			xWin = true;
			if(checkColumns().charAt(1) == '1') {
				ticTacToeSquares[0][0].getChildren().remove(0);
				ticTacToeSquares[0][0].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[0][1].getChildren().remove(0);
				ticTacToeSquares[0][1].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[0][2].getChildren().remove(0);
				ticTacToeSquares[0][2].getChildren().add( 0, getWinnerSquare());
			}
			else if(checkColumns().charAt(1) == '2') {
				ticTacToeSquares[1][0].getChildren().remove(0);
				ticTacToeSquares[1][0].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[1][1].getChildren().remove(0);
				ticTacToeSquares[1][1].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[1][2].getChildren().remove(0);
				ticTacToeSquares[1][2].getChildren().add( 0, getWinnerSquare());
			}
			else {
				ticTacToeSquares[2][0].getChildren().remove(0);
				ticTacToeSquares[2][0].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[2][1].getChildren().remove(0);
				ticTacToeSquares[2][1].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[2][2].getChildren().remove(0);
				ticTacToeSquares[2][2].getChildren().add( 0, getWinnerSquare());
			}
			renderTicTacToeGrid();
		}
		else if(checkColumns().charAt(0) == 'O'){
			winner = true;
			xWin = false;
			if(checkColumns().charAt(1) == '1') {
				ticTacToeSquares[0][0].getChildren().remove(0);
				ticTacToeSquares[0][0].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[0][1].getChildren().remove(0);
				ticTacToeSquares[0][1].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[0][2].getChildren().remove(0);
				ticTacToeSquares[0][2].getChildren().add( 0, getWinnerSquare());
			}
			else if(checkColumns().charAt(1) == '2') {
				ticTacToeSquares[1][0].getChildren().remove(0);
				ticTacToeSquares[1][0].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[1][1].getChildren().remove(0);
				ticTacToeSquares[1][1].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[1][2].getChildren().remove(0);
				ticTacToeSquares[1][2].getChildren().add( 0, getWinnerSquare());
			}
			else {
				ticTacToeSquares[2][0].getChildren().remove(0);
				ticTacToeSquares[2][0].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[2][1].getChildren().remove(0);
				ticTacToeSquares[2][1].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[2][2].getChildren().remove(0);
				ticTacToeSquares[2][2].getChildren().add( 0, getWinnerSquare());
			}
			renderTicTacToeGrid();
		}
		else if(checkDiagonals().charAt(0) == 'X') {
			winner = true;
			xWin = true;
			if(checkDiagonals().charAt(1) == '1') {
				ticTacToeSquares[0][0].getChildren().remove(0);
				ticTacToeSquares[0][0].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[1][1].getChildren().remove(0);
				ticTacToeSquares[1][1].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[2][2].getChildren().remove(0);
				ticTacToeSquares[2][2].getChildren().add( 0, getWinnerSquare());
			}
			else {
				ticTacToeSquares[2][0].getChildren().remove(0);
				ticTacToeSquares[2][0].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[1][1].getChildren().remove(0);
				ticTacToeSquares[1][1].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[0][2].getChildren().remove(0);
				ticTacToeSquares[0][2].getChildren().add( 0, getWinnerSquare());
			}
			renderTicTacToeGrid();
		}
		else if(checkDiagonals().charAt(0) == 'O'){
			winner = true;
			xWin = false;
			if(checkDiagonals().charAt(1) == '1') {
				ticTacToeSquares[0][0].getChildren().remove(0);
				ticTacToeSquares[0][0].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[1][1].getChildren().remove(0);
				ticTacToeSquares[1][1].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[2][2].getChildren().remove(0);
				ticTacToeSquares[2][2].getChildren().add( 0, getWinnerSquare());
			}
			else {
				ticTacToeSquares[2][0].getChildren().remove(0);
				ticTacToeSquares[2][0].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[1][1].getChildren().remove(0);
				ticTacToeSquares[1][1].getChildren().add( 0, getWinnerSquare());
				ticTacToeSquares[0][2].getChildren().remove(0);
				ticTacToeSquares[0][2].getChildren().add( 0, getWinnerSquare());
			}
			renderTicTacToeGrid();
		}

		if(winner) {
			ImageView board = new ImageView(ticTacToeGrid.snapshot(null, null));
			ticTacToeBox.getChildren().remove(ticTacToeGrid);
			ticTacToeBox.getChildren().add(board);
			board.setTranslateX(175);
			board.setTranslateY(15);


			if(xWin) {
				if(isX) {
					xText.setText(" won (you)");
					oText.setText(" lost");
					conversation.gameWon();

					gameState = GameState.LOCAL_WIN;
				}
				else {
					xText.setText(" won");
					oText.setText(" lost (you)");
					gameState = GameState.AWAY_WIN;
					conversation.gameLost();

				}
			}
			else {
				if(isX) {
					oText.setText(" won");
					xText.setText(" lost (you)");
					gameState = GameState.AWAY_WIN;
					conversation.gameLost();

				}
				else {
					oText.setText(" won (you)");
					xText.setText(" lost");
					gameState = GameState.LOCAL_WIN;
					conversation.gameWon();
				}

			}
		}
		else {
			if(isFull()) {
				ImageView board = new ImageView(ticTacToeGrid.snapshot(null, null));
				ticTacToeBox.getChildren().remove(ticTacToeGrid);
				ticTacToeBox.getChildren().add(board);
				conversation.gameTied();
				board.setTranslateX(175);
				board.setTranslateY(15);
				if(isX) {
					xText.setText(" tied (you)");
					oText.setText(" tied");
				}
				else {
					oText.setText(" tied (you)");
					xText.setText(" tied");
				}
				gameState = GameState.TIE;
			}
		}
	}

	//checks if columns contain a winner
	private String checkColumns() {
		int i = 1;
		for(char column[] : values) {
			char winner = checkColumn(column);
			if(winner == 'X')
				return winner + "" +i;
			else if(winner == 'O') 
				return winner + "" + i;
			i++;
		}

		return " ";
	}

	//checks if rows contain a winner
	private String checkRows() {
		char row1[] = {values[0][0], values[1][0],values[2][0]};
		char row2[] = {values[0][1], values[1][1],values[2][1]};
		char row3[] = {values[0][2], values[1][2],values[2][2]};

		if(checkColumn(row1) == 'X')
			return "X1";
		if(checkColumn(row1) == 'O')
			return "O1";
		if(checkColumn(row2) == 'X')
			return "X2";
		if(checkColumn(row2) == 'O')
			return "O2";
		if(checkColumn(row3) == 'X')
			return "X3";
		if(checkColumn(row3) == 'O')
			return "O3";
		return " ";
	}
	
	//checks if diagonal contain a winner
	private String checkDiagonals() {
		char diagonal1[] = {values[0][0], values[1][1],values[2][2]};
		char diagonal2[] = {values[0][2], values[1][1],values[2][0]};

		if(checkColumn(diagonal1) == 'X')
			return "X1";
		if(checkColumn(diagonal1) == 'O')
			return "O1";
		if(checkColumn(diagonal2) == 'X')
			return "X2";
		if(checkColumn(diagonal2) == 'O')
			return "O2";


		return " ";
	}

	//checks an array of game squares for a winner
	private char checkColumn(char[] column) {
		if('X' == column[0] && 'X' == column[1] && 'X' == column[2])
			return 'X';
		else if( 'O' == column[0] && 'O' == column[1] && 'O' == column[2])
			return 'O';
		return ' ';
	}

	//checks if board is full
	private boolean isFull() {

		for(char rows[] : values)
			for(char value : rows)
				if(value != 'X' && value != 'O')
					return false;

		return true;
	}
}


