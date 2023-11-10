package model;

import java.util.ArrayList;

/**
 * 
 */
public class Brain {
	double cohesionBias;
	double separationBias;
	double alignmentBias;

	/**
	 * Constructor for <code>Brain</code> with default values
	 */
	Brain(){
		
	}

	/**
	 * Constructor for <code>Brain</code> with given values
	 * @param cB cohesion bias
	 * @param sB separation bias
	 * @param alB alignment bias
	 */
	Brain(double cB, double sB, double alB){
		cohesionBias = cB;
		separationBias = sB;
		alignmentBias = alB;
	}

	/**
	 * Returns <code>Vector</code> summed from cohesion separation and alignment calculations
	 * @param thisBoid <code>Boid</code> to which this <code>Brain</code> belongs
	 * @param boids <code>ArrayList</code> of <code>Animal</code> for calculations
	 * @return
	 */
	public Vector steer(Boid thisBoid, ArrayList<Boid> boids) {
		//apply all forces
		Vector temp = new Vector(0,0);
		temp = temp.add(cohesion(thisBoid, boids));
		temp = temp.add(separation(thisBoid, boids));
		temp = temp.add(alignment(thisBoid, boids));
		//temp = temp.add(avoidance(t, thisBoid));
		return temp;
	}

	/**
	 * Returns <code>Vector</code> from cohesion calculation
	 * @param b <code>Boid</code> to which this <code>Brain</code> belongs
	 * @param boids <code>ArrayList</code> of <code>Boid</code> for calculations
	 * @return
	 */
	private Vector cohesion(Boid b, ArrayList<Boid> boids) {
		//return difference between average position of array boids and b
		Vector temp = new Vector(0,0);

		for (Boid i: boids) {
			temp = temp.add(i.getPos());
		}

		if (boids.size()!=0) {
			temp = temp.div(boids.size());

			temp = temp.sub(b.getPos());
		}
		temp = temp.setMag(cohesionBias);
		return temp;
	}

	/**
	 * Returns <code>Vector</code> from separation calculation
	 * @param b <code>Boid</code> to which this <code>Brain</code> belongs
	 * @param boids <code>ArrayList</code> of <code>Boid</code> for calculations
	 * @return
	 */
	private Vector separation(Boid b, ArrayList<Boid> boids) {
		//return sum of differences between position of b and boid in boids that is too close
		Vector temp = new Vector(0,0);
		for (Boid i: boids) {
			if (b.getPos().dist(i.getPos())<b.safeSpace){
				temp = temp.add(b.getPos().sub(i.getPos()));
			}
		}
		return temp.setMag(separationBias);
	}

	/**
	 * Returns <code>Vector</code> from alignment calculation
	 * @param b <code>Boid</code> to which this <code>Brain</code> belongs
	 * @param boids <code>ArrayList</code> of <code>Boid</code> for calculations
	 * @return
	 */
	private Vector alignment(Boid b, ArrayList<Boid> boids) {
		//return difference between average velocity of array boids and b
		Vector temp = new Vector(0,0);

		for (Boid i: boids) {
			temp = temp.add(i.getVel());
		}

		if (boids.size()!=0) {
			temp = temp.sub(b.getVel());
		}

		temp = temp.setMag(alignmentBias);
		return temp;
	}
	
	/**
	 * Sets cohesion bias
	 * @param c cohesion bias
	 */
	public void setCohesion(double c) {
		cohesionBias = c;
	}

	/**
	 * Returns cohesion bias
	 * @return cohesion bias
	 */
	public double getCohesion() {
		return cohesionBias;
	}

	/**
	 * Sets alignment bias
	 * @param a alignment bias
	 */
	public void setAlignment(double a) {
		alignmentBias = a;
	}

	/**
	 * Returns alignment bias
	 * @return alignment bias
	 */
	public double getAlignment() {
		return alignmentBias;
	}

	/**
	 * Sets separation bias
	 * @param s separation bias
	 */
	public void setSeparation(double s) {
		separationBias = s;
	}

	/**
	 * Returns separation bias
	 * @return separation bias
	 */
	public double getSeparation() {
		return separationBias;
	}
}

