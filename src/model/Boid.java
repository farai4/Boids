package model;
import java.util.ArrayList;

/**
 * The <code>Boid</code> class has a position, velocity and acceleration.
 */
public class Boid {

	protected Vector pos = new Vector(0,0);	
	protected Vector vel = new Vector(Math.random()*10-5,Math.random()*10-5);
	protected Vector acc = new Vector(0,0);
	protected Brain brain = new Brain();
	private int[] edges;	
	private boolean rewinding = false;
	protected ArrayList<Vector> previousPos = new ArrayList<Vector>();
	protected ArrayList<Vector> previousVel = new ArrayList<Vector>();
	private int edgeType=0;
	
	protected int width;
	protected int length;
	protected int safeSpace = 10;
	
	/**
	 * Constructs <code>Boid</code> with given field dimensions
	 * @param e Field dimensions
	 */
	public Boid(int[] e){															
		edges = e;
	}
	
	/**
	 * Returns <code>Boid<code>'s position
	 * @return <code>Vector</code> position
	 */
	public Vector getPos() {
		//returns position
		return pos;
	}
	
	/**
	 * Returns <code>Boid<code>'s velocity
	 * @return <code>Vector</code> velocity
	 */
	public Vector getVel() { 
		//returns velocity
		return vel;
	}
	
	/**
	 * Sets <code>Boid<code>'s x position
	 * @param x <code>double<code> x position
	 */
	public void setPosX(double x) {
		//set position x value
		this.pos.setX(x);
	}
	
	/**
	 * Sets <code>Boid<code>'s y position
	 * @param y <code>double<code> y position
	 */
	public void setPosY(double y) { 
		//set position y value
		this.pos.setY(y);
	}
	
	/**
	 * Sets <code>Boid<code>'s position
	 * @param p <code>Vector<code> position
	 */
	public void setPos(Vector p) {
		//set position
		pos = p;
	}
	
	/**
	 * Sets <code>Boid<code>'s x velocity
	 * @param x <code>double<code> x velocity
	 */
	public void setVelX(double x) {
		//set velocity x value
		this.vel.setX(x);
	}
	
	/**
	 * Sets <code>Boid<code>'s y velocity
	 * @param y <code>double<code> y velocity
	 */
	public void setVelY(double y) { 
		//set velocity y value
		this.vel.setY(y);
	}
	
	/**
	 * Sets <code>Boid<code>'s velocity
	 * @param v <code>Vector<code> velocity
	 */
	public void setVel(Vector v) {
		//set velocity
		vel = v;
	}
	
	/**
	 * Sets <code>Boid<code>'s acceleration
	 * @param v <code>Vector<code> acceleration
	 */
	public void setAcc(Vector v) {
		//set acceleration
		this.acc = v;
	}
	
	/**
	 * Returns animal's width
	 * @return width
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Returns animal's length
	 * @return length
	 */
	public int getLength() {
		return length;
	}
	
	/**
	 * Stores previous positions and velocities of <code>Boid</code>
	 */
	protected void store() {
		//accessed by child
		if (!rewinding) {					//store position and velocity
			previousPos.add(getPos());				
			previousVel.add(getVel());
		}else {
			if (previousPos.size()==0) {	//rewind until nothing left to rewind
				rewinding = false;
				previousPos.add(getPos());				
				previousVel.add(getVel());
				return;
			}
			
			this.pos = previousPos.get(previousPos.size()-1);	//set position to previous position
			this.vel = previousVel.get(previousVel.size()-1);	//set velocity to previous velocity
			
			previousPos.remove(previousPos.size()-1);			//remove previous position
			previousVel.remove(previousVel.size()-1);			//remove previous velocity
		}
	}
	
	/**
	 * Sets <code>Boid</code>'s rewind state to true
	 */
	public void rewind() {
		//start rewinding
		rewinding = true;
	}
	
	/**
	 * Sets <code>Boid</code>'s rewind state to false
	 */
	public void stopRewind() {
		//stop rewinding
		rewinding = false;
	}
	
	/**
	 * Returns <code>Boid</code>'s rewind state
	 * @return rewind state
	 */
	public boolean getRewinding() {
		//return rewinding status
		return rewinding;
	}
	
	/**
	 * 
	 * @param c
	 */
	public void setCohesion(double c) {
		//set brain's cohesion weight
		brain.setCohesion(c);
	}
	
	/**
	 * 
	 * @return
	 */
	public double getCohesion() {
		//return brain's cohesion weight
		return brain.getCohesion();
	}
	
	/**
	 * 
	 * @param a
	 */
	public void setAlignment(double a) {
		//set brain's alignment weight
		brain.setAlignment(a);
	}
	
	/**
	 * 
	 * @return
	 */
	public double getAlignment() {
		//return brain's alignment weight
		return brain.getAlignment();
	}
	
	/**
	 * 
	 * @param s
	 */
	public void setSeparation(double s) {
		//set brain's separation weight
		brain.setSeparation(s);
	}
	
	/**
	 * 
	 * @return
	 */
	public double getSeparation() {
		//return brain's separation weight
		return brain.getSeparation();
	}
	
	/**
	 * Sets edge handling
	 * @param e edge handling
	 */
	public void setEdgeType(int e) {
		edgeType = e;
	}
	
	/**
	 * Handles edges
	 */
	protected void edge() {
		//call appropriate edge handler
		if (edgeType == 0) {
			edgeAvoid();
		}else if(edgeType==1){
			edgeBounce();
		}else if(edgeType==2) {
			edgeWrap();
		}
	}
	
	/**
	 * Steers away from edge if too close
	 */
	private void edgeAvoid() {
		int edgeDist = 15;
		double edgeWeight = 1000;
		if (this.getPos().getX()<edges[0]+edgeDist) {
			this.acc = acc.add(new Vector(edgeWeight,0));
		}
		if (this.getPos().getX()>edges[1]-edgeDist) {
			this.acc = acc.add(new Vector(-edgeWeight,0));
		}
		if (this.getPos().getY()<edges[2]+edgeDist) {
			this.acc = acc.add(new Vector(0,edgeWeight));
		}
		if (this.getPos().getY()>edges[3]-edgeDist) {
			this.acc = acc.add(new Vector(0,-edgeWeight));
		}
		confine(0);
	}
	
	/**
	 * Inverts velocity when edge touched
	 */
	private void edgeBounce() {
		int edgeDist = 10;
		if (this.getPos().getX()<edges[0]+edgeDist) {
			this.setVelX(Math.abs(getVel().getX()));
		}
		if (this.getPos().getX()>edges[1]-edgeDist) {
			this.setVelX(-Math.abs(getVel().getX()));
		}
		if (this.getPos().getY()<edges[2]+edgeDist) {
			this.setVelY(Math.abs(getVel().getY()));
		}
		if (this.getPos().getY()>edges[3]-edgeDist) {
			this.setVelY(-Math.abs(getVel().getY()));
		}
		confine(edgeDist);
	}
	
	/**
	 * Sets position to opposite side of canvas when edge touched
	 */
	private void edgeWrap() {
		if (this.getPos().getX()<edges[0]) {
			this.setPosX(edges[1]-5);
		}
		if (this.getPos().getX()>edges[1]) {
			this.setPosX(edges[0]+5);
		}
		if (this.getPos().getY()<edges[2]) {
			this.setPosY(edges[3]-5);
		}
		if (this.getPos().getY()>edges[3]) {
			this.setPosY(edges[2]+5);
		}
	}
	
	/**
	 * Confines <code>Boid</code> to field
	 * @param offset
	 */
	private void confine(double offset) {
		//set back in canvas if outside
		if (this.getPos().getX()<edges[0]) {
			this.setPosX(edges[0]+offset);
		}
		if (this.getPos().getX()>edges[1]) {
			this.setPosX(edges[1]-offset);
		}
		if (this.getPos().getY()<edges[2]) {
			this.setPosY(edges[2]+offset);
		}
		if (this.getPos().getY()>edges[3]) {
			this.setPosY(edges[3]-offset);
		}
	}
	
	/**
	 * Returns direction of velocity
	 * @return direction
	 */
	public double getDir() { 
		double direction = Math.atan2(vel.getY(),vel.getX());
		return direction;
	}
	
	public String toString() {
		//return string value of position
		return String.valueOf(pos.getX())+" "+String.valueOf(pos.getY());
	}
}
