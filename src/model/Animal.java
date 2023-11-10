package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Animal extends Boid{
	private double viewDist;
	private double maxSpeed;
	private double speed;
	private double control;
	
	private boolean[] passableTerrain;
	private double[] terrainSpeedInfluence;
	private Terrain t;
	private ArrayList<Vector> waypoints;
	private int currentWaypoint = 0;
	
	private double maxSlope = Math.PI*70/180;
	
	/**
	 * Constructs <code>Animal</code> with given field dimensions
	 * @param e Field dimensions
	 */
	public Animal(int[] e) {
		super(e);
		setAnimalType("Elephant");
	}
	
	/**
	 * Sets animal type and sets porperties to animal's default
	 * @param animalType
	 */
	public void setAnimalType(String animalType) {
		switch(animalType) {
		case "Elephant":
			elephant();
			break;
		case "Penguin":
			penguin();
			break;
		case "Deer":
			deer();
			break;
		}
	}
	
	/**
	 * Sets properties to default for elephant
	 */
	private void elephant() {	
		viewDist = 27;
		maxSpeed = 4;
		speed = maxSpeed/2;
		control = 1;
		width=4;
		length = 6;
		super.brain = new Brain(20, 40, 20);
		
		boolean[] tempPass = {true,true,true,false};
		passableTerrain = tempPass;
		double[] tempSpeed = {1,0.75,0.25,0};
		terrainSpeedInfluence = tempSpeed;
	}
	
	/**
	 * Sets properties to default for penguin
	 */
	private void penguin() {
		viewDist = 15;
		maxSpeed = 2;
		speed = maxSpeed;
		control = 0.5;
		width=2;
		length = 3;
		super.brain = new Brain(20, 40, 20);
		
		boolean[] tempPass = {true,true,true,false};
		passableTerrain = tempPass;
		double[] tempSpeed = {1,0.5,1.5,0};
		terrainSpeedInfluence = tempSpeed;
	}
	
	/**
	 * Sets properties to default for deer
	 */
	private void deer() {
		viewDist = 20;
		maxSpeed = 6;
		speed = maxSpeed;
		control = 2;
		width=4;
		length = 6;
		super.brain = new Brain(20, 40, 20);
		
		boolean[] tempPass = {true,true,false,false};
		passableTerrain = tempPass;
		double[] tempSpeed = {1,0.75,0,0};
		terrainSpeedInfluence = tempSpeed;
	}
	
	/**
	 * Sets animal's waypoints
	 * @param w waypoints
	 */
	public void setWaypoints(ArrayList<Vector> w) {
		waypoints = w;
		currentWaypoint = 0;
	}
	
	/**
	 * Returns all <code>Animal</code> within view distance of this <code>Animal</code>
	 * @param others <code>ArrayList</code> of other <code>Animal</code>
	 * @return <code>ArrayList</code> of <code>Animal</code> in radius
	 */
	private ArrayList<Boid> getAdjacent(ArrayList<Animal> others) {	//returns all animals in view
		ArrayList<Boid> spotted = new ArrayList<Boid>();

		for (Animal other: others) {
			if (inView(other.getPos())){
				spotted.add(other);
			}
		}

		return spotted;
	}

	/**
	 * Returns whether <code>Vector</code> is in view arc
	 * @param other given <code>Vector</code>
	 * @return <code>boolean</code> whether <code>Vector</code> is in view
	 */
	private boolean inView(Vector other) {
		Vector pos = super.getPos();

		if (pos.dist(other)>viewDist) {
			return false;
		}
		return true;
	}

	/**
	 * Updates <code>Animal</code>'s acceleration, velocity and movement
	 * @param others <code>ArrayList</code> of other <code>Animal</code>
	 */
	public void update(ArrayList<Animal> others) {
		super.store();
		
		if (!super.getRewinding()) {
			currentPosSpeed();
			ArrayList<Boid> temp = getAdjacent(others);			//creates list of visible animals
			accelerate(temp);
			move();
			super.edge();
		}
	}

	/**
	 * Adds acceleration to velocity, velocity to position then resets acceleration
	 */
	private void move() {
		this.vel = this.vel.add(acc);
		this.vel = this.vel.limit(speed);
		
		this.pos = this.pos.add(vel);
		this.acc = this.acc.mult(0);
	}

	/**
	 * Returns <code>Vector</code> pointing away from any obstacles in terrain
	 * @return <code>Vector</code> pointing away from obstacles
	 */
	private Vector avoidance() {
		Vector temp = new Vector(0,0);
		if (t==null) {
			return temp;
		}
		int [][] views;
		Vector v =new Vector(viewDist,viewDist);
		int x = (int)this.pos.getX();
		int y = (int)this.pos.getY();
		
		views= t.getArea(x,y,(int)viewDist);
		for (int i= 0; i< views.length;i++){
			for (int j=0; j<views[i].length;j++){
				int type = views[i][j];
				Vector result = new Vector(j, i);
				
				if (!passableTerrain[type] && result.dist(v)<viewDist) {
					//temp = temp.add(v.sub(result));
					double obstacleVelAngle = getVel().angleBetween(v.sub(result));
					double otherAngle = Math.asin(1/v.dist(result));
					if(obstacleVelAngle>otherAngle) {
						temp = temp.add(getVel().add(v.sub(result)));
					}
				}
			}
		}
		temp.mult(100);
		return temp;
	}
	
	/**
	 * Returns <code>Vector</code> pointing away from steep slopes in terrain
	 * @return <code>Vector</code> pointing away from steep gradient
	 */
	private Vector gradientAvoidance() {
		Vector temp = new Vector(0,0);
		if (t==null) {
			return temp;
		}
		double [][] views;
		Vector v =new Vector(viewDist,viewDist);
		int x = (int)this.pos.getX();
		int y = (int)this.pos.getY();
		
		views= t.getGradientArea(x,y,(int)viewDist);
		for (int i= 0; i< views.length;i++){
			for (int j=0; j<views[i].length;j++){
				double slope = views[i][j];
				Vector result = new Vector(j, i);
				if (slope>maxSlope) {
					//System.out.println(slope);
					double obstacleVelAngle = getVel().angleBetween(v.sub(result));
					double otherAngle = Math.asin(1/v.dist(result));
					if(obstacleVelAngle>otherAngle) {
						temp = temp.add(getVel().add(v.sub(result)));
					}
				}
			}
		}
		return temp;
	}
	
	/**
	 * Returns <code>Vector</code> pointing to next waypoint and increments waypoint <code>Animal</code> gets close enough
	 * @return <code>Vector</code> pointing to next waypoint
	 */
	private Vector waypointSteer(){
		Vector temp= new Vector(0,0);
		
		if (waypoints != null&&waypoints.size()!=0) {
			Vector tempWaypoint = waypoints.get(currentWaypoint);
			double dist = tempWaypoint.dist(getPos());
			
			temp = tempWaypoint.sub(getPos());
			if (dist < 20) {
				currentWaypoint++;
				currentWaypoint = currentWaypoint%waypoints.size();
			}
		}
 		return temp;
	}
	
	/**
	 * Sets terrain
	 * @param t given <code>Terrain</code>
	 */
	public void setTerrain(Terrain t){
		this.t=t;
	}
	
	/**
	 * Gets acceleration from brain
	 * @param others <code>ArrayList</code> of other <code>Boid</code>
	 */
	private void accelerate(ArrayList<Boid> others) {
		Vector temp;
		temp = super.brain.steer(this, others);
		temp = temp.add(avoidance());
		temp = temp.add(gradientAvoidance());
		temp = temp.add(waypointSteer());
		this.acc = acc.add(temp);
		this.acc = this.acc.limit(control);
	}
	
	/**
	 * Limits speed based on terrain currently standing on
	 */
	private void currentPosSpeed() {
		int tempX = Math.max(Math.min(t.getWidth()-1, (int)this.getPos().getX()),0);
		int tempY = Math.max(Math.min(t.getHeight()-1, (int)this.getPos().getY()),0);
		
		int terrainType = t.getPoint(tempX, tempY);
		double speedInfluence = 1;
		switch (terrainType){
			case 0:
				speedInfluence *= 1;
				break;
			case 1:
				speedInfluence *= terrainSpeedInfluence[1];
				break;
			case 2:
				speedInfluence *= terrainSpeedInfluence[2];
				break;
			case 3:
				speedInfluence *= terrainSpeedInfluence[3];
				break;
		}
		
		speed = speedInfluence*maxSpeed;
	}
	
	/**
	 * Sets view distance of <code>Animal</code>
	 * @param vd view distance
	 */
	public void setViewDist(double vd) {
		viewDist = vd;
	}
	
	/**
	 * Returns view distance of <code>Animal</code>
	 * @return
	 */
	public double getViewDist() {
		return viewDist;
	}
	
	public void setMaxSlope(double a) {
		maxSlope = a;
	}
	
	public double getMaxSlope() {
		return maxSlope;
	}

	/**
	 * Returns all previous positions of <code>Animal</code>
	 * @return previous positions
	 */
	public List<Vector> getPreviousPos() {
		return previousPos;
	}

	/**
	 * Returns all previous velocities of <code>Animal</code>
	 * @return previous velocities
	 */
	public List<Vector> getPreviousVel() {
		return previousVel;
	}
}
