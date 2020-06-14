/*
 * Andres Carranza
 * 5/28/2019
 * Class represents a pool bal
 */

import javafx.scene.image.*;

public class Ball {
	enum Color{
		WHITE, BLACK, RED, YELLOW
	}

	public static final int POCKET_RADIUS= 46;

	public static final Vector[] POCKETS = new Vector[] {

			new Vector(750, 32),

			new Vector(750,794),

			new Vector(62,62),

			new Vector(1435,62),

			new Vector(62,762),

			new Vector(1435,762)};
	public static final double COLLISION_ENERGY_LOSS = 0.02;
	public static final int BALL_DIAMETER = 38;	
	public static final int BALL_RADIUS= BALL_DIAMETER / 2;
	public static final Vector BALL_ORIGIN = new Vector(25,25);
	private ImageView ball;
	private Vector velocity;
	private Vector origin;
	private Vector position;
	private boolean moving;
	private boolean visible;
	private Color color;
	private Pool engine;

	//Constructor
	public Ball(Vector position, Color color, Pool engine) {
		this.engine = engine;
		this.color = color;
		if(color == Color.WHITE)
			ball = new ImageView(LocalToolkit.loadImg("WhiteBall.png"));
		else if(color == Color.YELLOW)
			ball = new ImageView(LocalToolkit.loadImg("YellowBall.png"));
		else if(color == Color.RED)
			ball = new ImageView(LocalToolkit.loadImg("RedBall.png"));
		else
			ball = new ImageView(LocalToolkit.loadImg("BlackBall.png"));

		moving = false;
		velocity = new Vector();
		visible = true;

		this.position = position;
		this.origin = BALL_ORIGIN;

		ball.setX(position.x - origin.x);
		ball.setY(position.y -origin.y);

	}

	//Called to update position of ball
	public void originChanged() {
		ball.setX(position.x - origin.x);
		ball.setY(position.y - origin.y);
	}

	//Called when screen is rendered
	//Handles all neccesary tasks to update ball
	public void update(double delta) {
		if(!this.visible){
			return;
		}

		position.addTo(velocity.mult(delta));
		originChanged();
		velocity = velocity.mult(Pool.FRICTION);

		if(velocity.length() < 5) {
			velocity = new Vector();
			moving = false;
		}
	}

	//Returns image of ball
	public ImageView getBall() {
		return ball;
	}

	//return position of ball
	public Vector getPosition() {
		return position;
	}

	//Called when ball is shot by stick
	public void shoot(double power, double rotation) {
		velocity = new Vector(power * Math.cos(Math.toRadians(rotation)), power * Math.sin(Math.toRadians(rotation)));
		moving = true;
	}

	//True if ball is moving
	public boolean isMoving() {
		return moving;
	}

	//True of ball is visible
	public boolean isVisible() {
		return visible;
	}

	//checks if balls collided
	public void collideWith(Ball ball) {

		if(!ball.isVisible() || !this.visible){

			return;

		}

		//Find normal vector
		Vector n = position.subtract(ball.position);

		//Find distance
		double distance = n.length();

		if(distance > BALL_DIAMETER)
			return;

		Vector mtd = n.mult((BALL_DIAMETER - distance)/distance);

		position = position.add(mtd.mult(.5));

		ball.position = ball.position.subtract(mtd.mult(.5));


		//finding unit normal vector

		Vector un = n.mult(1/n.length());

		// Find unit tangent vector

		Vector ut = new Vector(-un.y, un.x);

		// Project velocities onto the unit normal and unit tangent vectors
		double v1n = un.dot(this.velocity);

		double v1t = ut.dot(this.velocity);

		double v2n = un.dot(ball.velocity);

		double v2t = ut.dot(ball.velocity);
		// Find new normal velocities

		double v1nTag = v2n;

		double  v2nTag = v1n;
		// Convert the scalar normal and tangential velocities into vectors

		Vector v1nTagv = un.mult(v1nTag);

		Vector v1tTag = ut.mult(v1t);

		Vector v2nTagv = un.mult(v2nTag);

		Vector v2tTag = ut.mult(v2t);

		// Update velocities

		velocity = v1nTagv.add(v1tTag);

		ball.velocity = v2nTagv.add(v2tTag);

		this.moving = true;

		ball.moving = true;
	}

	//Checks if ball collided with table
	public void collideWithTable(){

		if(!this.moving || !this.visible){

			return;

		}

		boolean collided = false;
		if(this.position.y <= Table.TOP_Y + BALL_RADIUS){

			this.position.y = Table.TOP_Y + BALL_RADIUS;

			this.velocity = new Vector(this.velocity.x, -this.velocity.y);

			collided = true;

		}

		if(this.position.x >= Table.RIGHT_X - BALL_RADIUS){

			this.position.x = Table.RIGHT_X - BALL_RADIUS;

			this.velocity = new Vector(-this.velocity.x, this.velocity.y);

			collided = true;

		}



		if(this.position.y >= Table.BOTTOM_Y -BALL_RADIUS){

			this.position.y = Table.BOTTOM_Y - BALL_RADIUS;

			this.velocity = new Vector(this.velocity.x, -this.velocity.y);

			collided = true;

		}

		if(this.position.x <= Table.LEFT_X + BALL_RADIUS){

			this.position.x = Table.LEFT_X + BALL_RADIUS;

			this.velocity = new Vector(-this.velocity.x, this.velocity.y);

			collided = true;

		}

		if(collided){

			this.velocity = this.velocity.mult(1 - COLLISION_ENERGY_LOSS);

		}
	}

	//Checks if ball is in a pocket
	public void handleBallInPocket(){
		if(!this.visible){
			return;

		}
		boolean inPocket = false;
		for(Vector pocket :  POCKETS) {
			if(ballIsInPocket(pocket)) {
				inPocket = true;
				break;
			}
		}

		if(!inPocket){
			return;

		}
		engine.ballIn(color);

		this.visible = false;
		this.moving = false;

		ball.setVisible(false);
	}

	//Checks if ball is in pocket pocket
	public boolean ballIsInPocket(Vector pocket) {
		return this.position.distFrom(pocket) < POCKET_RADIUS;
	}



}
