/*
 * Andres Carranza
 * 5/28/2019
 * Represents a vector
 */
public class Vector {
	public double x;
	public double y;

	//constructor creates a vector of <0,0>
	public Vector(){
		this.x = 0;

		this.y = 0;

	}
	
	//constructor creates a vector of <x, y>
	public Vector(double x, double y){
		this.x = x;

		this.y = y;

	}

	//returns a copy of thsi vector
	public Vector copy(){

		return new Vector(this.x, this.y);

	}

	//add two vectors together
	public Vector add(Vector vector){

		return new Vector(this.x + vector.x, this.y + vector.y);

	}

	//adds vector to this vector
	public void addTo(Vector vector){

		this.x += vector.x;

		this.y += vector.y;

	}

	//subtracts vector from this vector
	public void subtractFrom(Vector vector){

		this.x -= vector.x;

		this.y -= vector.y;

	}


	//subtracts both ectors
	public Vector subtract(Vector vector){

		return new Vector(this.x - vector.x, this.y - vector.y);

	}

	//multiplies vector by scalar value
	public Vector mult(double scalar){
		return new Vector(this.x * scalar, this.y * scalar);

	}

	//returns the dot product of two vectors
	public double dot(Vector vector){

		return this.x * vector.x + this.y * vector.y;

	}

	//returns the legnth/magnitude of vector
	public double length(){

		return Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2));

	}

	//returns the distance between two vectors
	public double distFrom(Vector vector){

		return this.subtract(vector).length();

	}
	
	//returns the string representation of the vector
	public String toString() {
		return "<" + x + ", " + y + ">";
	}
}
