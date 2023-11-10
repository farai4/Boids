package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JPanel;

import model.*;

public class SimulationPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	private ArrayList<Animal> boids;						//instantiate empty array of animals
	private int fieldWidth;											//panel width
	private int fieldHeight;										//panel height
	private BufferedImage background;
	private boolean showWaypoints = false;
	private ArrayList<Vector> waypoints;
	private int size = 640;
	private double scale;
	
	private double zoomFactor;
	private Point start;
	private double xOffset=0;
	private double yOffset=0;
	
	public SimulationPanel(int w, int h) {					//panel constructor
		fieldWidth = w;
		fieldHeight = h;
		this.setBounds(0,0,size, size);
		scale = (double)size/fieldWidth;
		zoomFactor = (double)size/fieldWidth;
		
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				start = new Point(e.getX(), e.getY());
			}

			public void mouseReleased(MouseEvent e) {
				start = null;
			}
		});
		
		addMouseMotionListener(new MouseAdapter() {

			public void mouseDragged(MouseEvent e) {
				if (start!=null) {
					int dx = e.getX() - start.x;
					int dy = e.getY() - start.y;

					start = new Point(e.getX(), e.getY());
					
					xOffset += dx;
					yOffset += dy;
					
					//yOffset =  Math.min(yOffset,getHeight()) ;
					repaint();
				}
			}
		});

		addMouseWheelListener(new MouseAdapter() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				double zoomedLeft = -xOffset/zoomFactor;
				double zoomedRight = zoomedLeft + getWidth()/(zoomFactor);
				double mouseX = (e.getX()/(double)getWidth()*(getWidth()/(zoomFactor))-xOffset/(zoomFactor));
				
				double zoomedTop = -yOffset/zoomFactor;
				double zoomedBottom = zoomedTop + getHeight()/(zoomFactor);
				double mouseY = (e.getY()/(double)getHeight()*(getHeight()/(zoomFactor))-yOffset/(zoomFactor));
				
				//Zoom in
				if (e.getWheelRotation() < 0) {
					xOffset *= 1.1;
					xOffset -= (getWidth()/zoomFactor-getWidth()/(zoomFactor*1.1))*(mouseX-zoomedLeft)/(zoomedRight-zoomedLeft);
					
					yOffset*=1.1;
					yOffset -= (getHeight()/zoomFactor-getHeight()/(zoomFactor*1.1))*(mouseY-zoomedTop)/(zoomedBottom-zoomedTop);
					
					zoomFactor *= 1.1;
				}
				//Zoom out
				if (e.getWheelRotation() > 0) {
					xOffset /= 1.1;
					xOffset -= (getWidth()/zoomFactor-getWidth()/(zoomFactor/1.1))*(mouseX-zoomedLeft)/(zoomedRight-zoomedLeft);
					
					yOffset /= 1.1;
					yOffset -= (getHeight()/zoomFactor-getHeight()/(zoomFactor/1.1))*(mouseY-zoomedTop)/(zoomedBottom-zoomedTop);
					
					zoomFactor /= 1.1;
				}
				zoomFactor = Math.max(scale, zoomFactor);
				repaint();
			}
		});
	}
	
	public void setBoids(ArrayList <Animal>b) {				//boids setter method
		this.boids = b;
	}
	
	public void setBackground(BufferedImage bg) {
		background = bg;
	}
	
	public int[] getFieldDimensions() {						//returns dimensions of panel
		int[] temp = {0,fieldWidth,0,fieldHeight};
		return temp;
	}
	
	public void setWaypoints(ArrayList<Vector> w) {
		waypoints = w;
	}
	
	public void setShowWaypoints(boolean sw) {
		showWaypoints = sw;
	}
	
	@Override
	public void paintComponent(Graphics g) {				//override JPanel paint method
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        AffineTransform trans = new AffineTransform();
        
        xOffset = Math.min(xOffset,0) ;
		yOffset =  Math.min(yOffset,0) ;
        xOffset = Math.max(xOffset,(getWidth()-(zoomFactor)*fieldWidth));
        yOffset = Math.max(yOffset,(getHeight()-(zoomFactor)*fieldHeight));
		trans.translate(xOffset,yOffset);
		trans.scale(zoomFactor, zoomFactor);
		g2.transform(trans);
        
        if (background != null) {
        	g2.drawImage(background, 0, 0, this);
        }
        
        g2.setColor(java.awt.Color.RED);					//instantiate g2 component
        for (Animal boid : boids) {							//for each boid
        	if (boid != null) {
        		AffineTransform temp = g2.getTransform();	//store panel before transformation
	        	int posX = (int)boid.getPos().getX();
	        	int posY = (int)boid.getPos().getY();
	        	
	        	double dir = boid.getDir();
	        	g2.translate(posX,posY);					//translate to boid's position
	        	g2.rotate(dir+Math.PI/2);					//rotate by boid's direction
	        	
	        	int animalWidth = boid.getWidth();
	        	int animalLength = boid.getLength();
	        	int[] posXs = {0, -animalWidth/2, animalWidth/2};
	        	int[] posYs = {-animalLength/2, animalLength/2, animalLength/2};
	        	g2.drawPolygon(posXs, posYs, 3);			//draw triangle
	        	g2.setTransform(temp);						//reset transformation
        	}
        }
        
        if (showWaypoints) {
        	Color lime = new Color(50, 205, 50);
        	if (waypoints!=null) {
	            for (int i=0; i<waypoints.size(); i++) {
	            	Vector w = waypoints.get(i);
	            	g2.setColor(lime);
	            	g2.fillOval((int)w.getX()-5, (int)w.getY()-5, 15, 15);
	            	g2.setColor(java.awt.Color.BLACK);
	            	g2.drawString(String.valueOf(i+1), (int)w.getX(), (int)w.getY()+5);
	            }
        	}
        }
    	
        g2.setColor(java.awt.Color.BLACK);
        //g2.drawRect(1, 1, fieldWidth-1, fieldHeight-1);		//draw field
        g2.dispose();
    }
}
