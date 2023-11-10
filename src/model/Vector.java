package model;

/**
 * The <code>Vector</code> class holds an x and a y value
 */
public class Vector{
	private double x = 0;
	private double y = 0;
	
	/**
	 * Creates vector with given x and y values
	 * @param x x value
	 * @param y y value
	 */
	public Vector(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Returns <code>Vector</code> x value
	 * @return x value
	 */
	public double getX() {
		return this.x;
	}
	
	/**
	 * Returns <code>Vector</code> y value
	 * @return y value
	 */
	public double getY() {
		return this.y;
	}
	
	/**
	 * Sets <code>Vector</code> x value
	 * @param x x value
	 */
	public void setX(double x) {
		this.x = x;
	}
	
	/**
	 * Sets <code>Vector</code> y value
	 * @param y y value
	 */
	public void setY(double y) {
		this.y = y;
	}
	
	/**
	 * Add another <code>Vector</code> to this <code>Vector</code>
	 * @param other <code>Vector</code> to be added
	 * @return sum of Vectors
	 */
	public Vector add(Vector other) {
		Vector temp = new Vector(this.getX()+other.getX(), this.getY()+other.getY());
		return temp;
	}
	
	/**
	 * Add another <code>Vector</code> from this <code>Vector</code>
	 * @param other <code>Vector</code> to be subtracted
	 * @return difference between Vectors
	 */
	public Vector sub(Vector other) {
		Vector temp = new Vector(this.getX()-other.getX(),this.getY()-other.getY());
		return temp;
	}
	
	/**
	 * Scale <code>Vector</code> up by some <code>double</code>
	 * @param n number to scale up by
	 * @return scaled <code>Vector</code>
	 */
	public Vector mult(double n) {
		Vector temp = new Vector(this.getX()*n,this.getY()*n);
		return temp;
	}
	
	/**
	 * Scale <code>Vector</code> down by some <code>double</code>
	 * @param n number to scale down by
	 * @return scaled <code>Vector</code>
	 */
	public Vector div(double n) {
		Vector temp = new Vector((double)(this.getX()/n),((double)this.getY()/n));
		return temp;
	}
	
	/**
	 * Limit <code>Vector</code> magnitude to some <code>double</code>
	 * @param n number to limit magnitude by
	 * @return limited <code>Vector</code>
	 */
	public Vector limit(double n) {
		Vector temp = new Vector(this.getX(), this.getY());
		double magnitude = getMag();
		
		if (magnitude != 0 && magnitude>n) {
			temp = temp.setMag(n);
		}
		
		return temp;
	}
	
	/**
	 * Set <code>Vector</code> magnitude to some <code>double</code>
	 * @param n number to set magnitude to
	 * @return updated <code>Vector</code>
	 */
	public Vector setMag(double n) {
		Vector temp = new Vector(this.getX(), this.getY());
		double magnitude = getMag();
		
		if (magnitude != 0) {
			temp = temp.div(magnitude);
			temp = temp.mult(n);
		}
		
		return temp;
	}
	
	/**
	 * Returns <code>Vector</code>'s magnitude
	 * @return magnitude
	 */
	public double getMag() {
		Vector origin = new Vector(0,0);
		Vector temp = new Vector(this.getX(), this.getY());
		double magnitude = temp.dist(origin);
		return magnitude;
	}
	
	/**
	 * Change x value's sign
	 * @return updated <code>Vector</code>
	 */
	public Vector mirrorX() {
		Vector temp = new Vector(this.getX()*-1,this.getY());
		return temp;
	}
	
	/**
	 * Change y value's sign
	 * @return updated <code>Vector</code>
	 */
	public Vector mirrorY() {
		Vector temp = new Vector(this.getX(),this.getY()*-1);
		return temp;
	}
	
	/**
	 * Returns distance between this <code>Vector</code> and given <code>Vector</code>
	 * @param other given <code>Vector</code>
	 * @return distance
	 */
	public double dist(Vector other) {
		double dx = this.getX()-other.getX();
		double dy = this.getY()-other.getY();
		double temp = Math.sqrt(Math.pow(dx, 2)+Math.pow(dy,2));
		return temp;
	}
	
	public double dotProduct(Vector other) {
		return getX()*other.getX() + getY()*other.getY();
	}
	
	public double angleBetween(Vector other) {
		if(getMag()*other.getMag()!=0) {
			return Math.acos(dotProduct(other)/(getMag()*other.getMag()));
		}
		return 0;
	}
	
	/**
     *  Scales a number between given input range to given output range
     * @param number number to be scaled
     * @param inMin input range minimum
     * @param inMax input range maximum
     * @param outMin output range minimum
     * @param outMax output range maximum
     * @return number within specified range
     */
    private double scale (double number, double inMin, double inMax, double outMin, double outMax) {
        return ((number - inMin) * (outMax - outMin) / (inMax - inMin) + outMin);
    }
	
	public boolean equals(Vector other) {
		return getX()==other.getX()&&getY()==other.getY();
	}
	
	public String toString() {
		return x + " " + y;
	}
}
