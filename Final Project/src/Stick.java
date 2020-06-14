/*
 * Andres Carranza
 * 8/25/2019
 * Class represents a pool stick
 */
import javafx.scene.image.*;
import javafx.scene.transform.*;

public class Stick {
	public static Vector STICK_ORIGIN = new Vector(970, 11);
	public static Vector STICK_SHOT_ORIGIN = new Vector(950,11);
	public static int MAX_POWER = 8000;
	private ImageView stick;
	private Pool engine;
	private Rotate rotation;
	private Vector position;
	private double power;
	private Vector origin;
	private boolean shot;

	//Constructor
	public Stick(Vector position, Pool engine) {
		this.engine = engine;
		this.position = position;

		shot = false;
		power = 0;

		rotation = new Rotate();

		rotation.setPivotX(position.x);
		rotation.setPivotY(position.y);
		this.origin = STICK_ORIGIN.copy();


		stick = new ImageView(LocalToolkit.loadImg("Stick.png"));

		//Setting origin
		stick.setX(position.x - origin.x);
		stick.setY(position.y -origin.y);
	}

	//updayes the stick
	public void update() {
		if(Game.GameState.LOCAL_TURN  == engine.gameState|| Game.GameState.FIRST_TURN == engine.gameState) {

			if(engine.isMouseLeftDown() && !shot)
				increasePower();
			else if( power> 0) {
				shoot();
			}
			updateRotation();	
		}
	}

	//increases stick power
	public void increasePower() {
		if(power == MAX_POWER)
			return;
		power+=200;

		origin.x += 5;
		originChanged();
	}

	//updates rotation
	public void updateRotation() {

		double opposite = engine.getMouse().y - position.y;
		double adjacent = engine.getMouse().x - position.x;

		double angle = Math.atan2(opposite, adjacent);
		rotation.setAngle(Math.toDegrees(angle));

		stick.getTransforms().clear();
		stick.getTransforms().add(rotation);

	}

	//returrns stick
	public ImageView getStick() {
		return stick;
	}

	//updates origin
	public void originChanged() {
		stick.setX(position.x - origin.x);
		stick.setY(position.y - origin.y);
		rotation.setPivotX(position.x);
		rotation.setPivotY(position.y);

	}

	//repositions to position of ball
	public void reposition(Vector position) {
		origin = STICK_ORIGIN.copy();
		this.position = position.copy();
		originChanged();
		shot = false;
	}

	//shoots the ball
	public void shoot() {

		engine.onShoot(power, rotation.getAngle());
		engine.gameState = Game.GameState.AWAY_TURN;
		engine.sendMessageToClient(power+":"+  rotation.getAngle());
		engine.setLocalHit(true);
		stick.setVisible(false);
		engine.setHeaderText("Waiting for " +engine.conversation.getRecipientName() + " to play...");

		power = 0;

		origin = STICK_SHOT_ORIGIN.copy();
		originChanged();

		shot = true;
	}

	//shoots opponent ball
	public void shoot(double power, double rotation) {
		this.rotation.setAngle(rotation);
		stick.setVisible(true);
		engine.onShoot(power, rotation);
		power = 0;

		origin = STICK_SHOT_ORIGIN.copy();
		originChanged();
		shot = true;

	}

	//true if shot
	public boolean shot() {
		return shot;
	}

}
