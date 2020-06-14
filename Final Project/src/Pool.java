/*
 * Andres Carranza
 * 5/28/2019
 * Handles everything related to the pool game
 */

import javafx.application.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.scene.transform.*;

public class Pool  extends Game implements Runnable{
	public static final String CLASS_ID ="pool";
	public static final double DELTA =  1/200.0;
	public static final double FRICTION = 0.984;
	private boolean gameIsRunning;
	private Pane poolTableContainer;
	private VBox poolBox;
	private Vector mouse;
	private Stick stick;
	private boolean mouseLeftDown;
	private Ball balls[];
	private Text caption;
	private Label headerText;
	private boolean firstBall;
	private Text ballColor;
	private boolean localHit;
	private Ball.Color targetColor;
	private int numRed;
	private int numYellow;

	//Constructor
	public Pool(Conversation conversation) {
		super(conversation);

		stick.getStick().setVisible(false);
		localHit = false;
		headerText.setText("Waiting for " +conversation.getRecipientName() + " to play...");
	}

	//Constructor for away game
	public Pool(Conversation conversation, String gameId) {
		super(conversation, gameId);

		stick.getStick().setVisible(true);
		localHit = true;
		headerText.setText("You turn!");
	}

	//Sets the game up
	@Override
	public void setUpGame() {
		super.setUpGame();
		gameIsRunning = true;
		mouseLeftDown = false;
		firstBall = true;
		poolTableContainer  = new Pane();
		numRed = 7;
		numYellow = 7;

		mouse = new Vector();
		poolTableContainer.addEventHandler(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				mouse.x =  event.getX();
				mouse.y =  event.getY();
			}

		});
		poolTableContainer.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				mouseLeftDown = event.isPrimaryButtonDown();
			}

		});
		poolTableContainer.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				mouseLeftDown = event.isPrimaryButtonDown();
			}

		});


		Image poolTable = LocalToolkit.loadImg("PoolTable.png");
		poolTableContainer.getChildren().add(new ImageView (poolTable));

		balls = new Ball[]{
				new Ball(new Vector(413,413),Ball.Color.WHITE,this),

				new Ball(new Vector(1022,413),Ball.Color.YELLOW,this),

				new Ball(new Vector(1056,393),Ball.Color.YELLOW,this),

				new Ball(new Vector(1056,433),Ball.Color.RED,this),

				new Ball(new Vector(1090,374),Ball.Color.RED,this),

				new Ball(new Vector(1090,413),Ball.Color.BLACK,this),

				new Ball(new Vector(1090,452),Ball.Color.YELLOW,this),

				new Ball(new Vector(1126,354),Ball.Color.YELLOW,this),

				new Ball(new Vector(1126,393),Ball.Color.RED,this),

				new Ball(new Vector(1126,433),Ball.Color.YELLOW,this),

				new Ball(new Vector(1126,472),Ball.Color.RED,this),

				new Ball(new Vector(1162,335),Ball.Color.RED,this),

				new Ball(new Vector(1162,374),Ball.Color.RED,this),

				new Ball(new Vector(1162,413),Ball.Color.YELLOW,this),

				new Ball(new Vector(1162,452),Ball.Color.RED,this),

				new Ball(new Vector(1162,491),Ball.Color.YELLOW,this)

		};

		for(Ball ball :  balls) {
			poolTableContainer.getChildren().add(ball.getBall());
		}

		stick = new Stick(new Vector(413,413), this);
		poolTableContainer.getChildren().add(stick.getStick());

		ballColor = new Text("Your color: any");
		ballColor.setStyle("-fx-font-size: 30");
		ballColor.setTranslateX(20);
		gameScreen.getChildren().add(ballColor);


		poolBox = new VBox();
		poolBox.setPadding(new Insets(25));


		Scale scale = new Scale();

		scale.setX(.5);
		scale.setY(.5);
		scale.setPivotX(0);
		scale.setPivotY(0);
		poolTableContainer.getTransforms().add(scale);

		poolBox.getChildren().add(poolTableContainer);
		gameScreen.getChildren().add(poolBox);

		Pane header = new Pane();

		headerText = new Label();
		headerText.setStyle("-fx-font-size: 50");
		header.getChildren().add(headerText);


		ImageView rules = LocalToolkit.loadImg("Rules.png",50,50);
		LocalToolkit.addGlowEffect(rules);

		rules.addEventHandler(MouseEvent.MOUSE_CLICKED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				gameScreen.getChildren().clear();
				gameScreen.getChildren().add(getObject().header);

				Label rules = new Label(" 8 Ball rules: ");
				Label exp = new Label(" - To win hit all of the balls that\n match your color in the pocket!\n - If you hit the white ball or the\n black ball in a pocket, you lose!");
				Label warning = new Label("\n\n ***This game does not adhere to the rules of Classic 8 Ball***");
				rules.setStyle("-fx-font-size: 100");
				exp.setStyle("-fx-font-size: 30");
				warning.setStyle("-fx-font-size: 20 ");
				gameScreen.getChildren().add(rules);
				gameScreen.getChildren().add(exp);
				gameScreen.getChildren().add(warning);
				lastScreenIsConversation = false;
			}
		});

		header.getChildren().add(rules);

		header.setStyle("-fx-padding:10" );
		rules.setTranslateX(670);
		rules.setTranslateY(15);

		super.header.getChildren().add(header);

		Thread gameThread = new Thread(this);
		gameThread.start();
	}

	//Returns the menu display
	public static VBox getMenuDisplay() {
		return loadMenuDisplay("Pool.png", "8 Ball Pool", 200, 100);
	}


	//returns the game dislay
	@Override
	public VBox getGameDisplay() {
		VBox gameDisplay = super.getGameDisplay();

		if(gameState == GameState.FIRST_TURN || gameState == GameState.INVITE) {
			poolTableContainer.getChildren().remove(stick.getStick());
			gameDisplay.getChildren().add(LocalToolkit.scale(poolTableContainer.snapshot(null, null),200, 200, true));
			caption = new Text("Play me in 8 Ball!");
			gameDisplay.getChildren().add(caption);
			poolTableContainer.getChildren().add(stick.getStick());
		}
		else if(gameState == GameState.AWAY_TURN || gameState == GameState.LOCAL_TURN ) {
			poolTableContainer.getChildren().remove(stick.getStick());
			gameDisplay.getChildren().add(LocalToolkit.scale(poolTableContainer.snapshot(null, null),200, 200, true));
			caption = new Text("Your turn!");
			gameDisplay.getChildren().add(caption);
			poolTableContainer.getChildren().add(stick.getStick());
		}
		else if(gameState == GameState.LOCAL_WIN || gameState == GameState.AWAY_WIN) {
			poolTableContainer.getChildren().remove(stick.getStick());
			gameDisplay.getChildren().add(LocalToolkit.scale( gameScreen.getChildren().get(2).snapshot(null, null),200, 200, true));
			caption = new Text("I won!");
			gameDisplay.getChildren().add(caption);
			poolTableContainer.getChildren().add(stick.getStick());
		}



		return gameDisplay;
	}

	//Handles rendering and updating
	@Override
	public void run() {

		long currenttime;
		long previoustime = System.nanoTime();
		long passedtime;
		double unprocessedseconds = 0;
		double secondspertick = 1 / 60.0;
		int tickcount = 0;
		while (gameIsRunning) {

			currenttime = System.nanoTime();
			passedtime = currenttime - previoustime;
			previoustime = currenttime;
			unprocessedseconds += passedtime / 1000000000.0;

			while (unprocessedseconds > secondspertick) {
				Platform.runLater(()->{
					tick();
				});
				unprocessedseconds -= secondspertick;
				tickcount++;
				if (tickcount % 60 == 0) {
					previoustime += 1000;
				}
			}
		}
	}


	//Updates everything
	public void tick() {
		handleCollisions();

		stick.update();

		for(Ball ball: balls)
			ball.update(DELTA);

		if(!ballsMoving() && stick.shot()) {
			stick.reposition(balls[0].getPosition());
		}
	}

	//Checks for  collisions
	public void handleCollisions() {
		for(int i =0; i < balls.length; i++) {
			balls[i].handleBallInPocket();
			balls[i].collideWithTable();
			for(int j = i + 1; j < balls.length; j ++ ) {
				balls[i].collideWith(balls[j]);
			}
		}
	}

	//Sets local hit
	public void setLocalHit(boolean localHit) {
		this.localHit = localHit;
	}

	//Called when stick is shot
	public void onShoot(double power, double rotation) {
		balls[0].shoot(power, rotation);
	}

	//Returns the screen
	public Pane getScreen() {
		return poolTableContainer;
	}

	//Checks if balls are moving
	public boolean ballsMoving() {
		for(Ball ball : balls) {
			if(ball.isMoving())
				return true;
		}
		return false;
	}

	//Stops the game
	public void stop() {
		gameIsRunning = false;
	}

	//Returns the mouse
	public Vector getMouse() {
		return mouse;
	}

	//True if mouse is down
	public boolean isMouseLeftDown() {
		return mouseLeftDown;
	}

	//true if is first ball
	public boolean isFirstBall() {
		return firstBall;
	}

	//handes loss
	public void lost() {
		gameState = GameState.AWAY_WIN;
		ImageView board = new ImageView(poolBox.snapshot(null, null));
		headerText.setText("You lost!");

		gameScreen.getChildren().remove(2);
		gameScreen.getChildren().add(2, board);
		conversation.gameLost();
	}

	//handles win
	public void won() {
		gameState = GameState.LOCAL_WIN;
		ImageView board = new ImageView(poolBox.snapshot(null, null));
		headerText.setText("You won!");
		sendMessageToClient(CLASS_ID);

		gameScreen.getChildren().remove(2);
		gameScreen.getChildren().add(2, board);
		conversation.gameWon();
	}

	//handles a ball in a pocket
	public void ballIn(Ball.Color color) {
		if(color == Ball.Color.WHITE || color == Ball.Color.BLACK) {
			if(localHit) 
				lost();
			else
				won();
			return;
		}

		if(firstBall) {
			if(localHit) {
				targetColor = color;
			}
			else {
				if(color == Ball.Color.RED) {
					targetColor = Ball.Color.YELLOW;
				}
				else {
					targetColor = Ball.Color.RED;
				}
			}
		}
		ballColor.setText("Your color: " + targetColor);
		firstBall = false;

		if(color == Ball.Color.RED)
			numRed--;
		else
			numYellow--;

		if(numRed == 0 ) 
			if(targetColor == Ball.Color.RED) 
				won();
			else
				lost();
		else if(numYellow == 0) 
			if(targetColor == Ball.Color.RED) 
				lost();
			else
				won();






	}

	//Sets the header text
	public void setHeaderText(String text) {
		headerText.setText(text);
	}


	// handles a message received
	@Override
	public void messageReceived(String message) {
		if(!message.equals(CLASS_ID)) {
			String[] msgs = message.split(":");
			stick.shoot(Double.parseDouble(msgs[0]), Double.parseDouble(msgs[1]));
			gameState = GameState.LOCAL_TURN;
			localHit = false;
			setHeaderText("Your turn!");

		}

	}

	//Switches to 8 ball screen
	@Override 
	protected void switchTo8Ball() {
		gameScreen.getChildren().clear();

		gameScreen.getChildren().add(header);
		gameScreen.getChildren().add(poolBox);
		lastScreenIsConversation = true;

	}


}