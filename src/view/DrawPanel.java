package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import model.Vector;

import javax.swing.JPanel;

import model.Animal;
import model.Terrain;

public class DrawPanel extends JPanel implements MouseListener{
	private static final long serialVersionUID = 1L;
	private int width;
	private int height;
	private int mouseX;
	private int mouseY;
	private int brushSize = 30;
	private int brushScale = 10;
	private Terrain terrain;
	private int terrainType = 1;
	private int drawState = 0;
	private ArrayList<Vector> waypoints = new ArrayList<Vector>();
	private int size = 640;
	private double scale;
	
	private ArrayList<Animal> animals = new ArrayList<Animal>();
	
	public DrawPanel(Terrain t, ArrayList<Vector> w) {
		width = t.getWidth();
		height = t.getHeight();
		setBounds(0,0,size,size);
		scale = (double)width/size;
		brushSize = width/10;
		brushScale = width/64;
		
		terrain = new Terrain(t);
		setCursor(getToolkit().createCustomCursor(new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB), new Point(), null));
		waypoints = new ArrayList<Vector>(w);
		
		addMouseListener(this);
		addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {	
				mouseX = (int)(e.getX()*scale);
				mouseY = (int)(e.getY()*scale);
				repaint();
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				if (drawState == 0) {
					paintTerrain(e);
				}else if (drawState == 1) {
					paintAnimal(e);
				}else if (drawState == 2) {
					paintWaypoint(e);
				}
				mouseX = (int)(e.getX()*scale);
				mouseY = (int)(e.getY()*scale);
				repaint();
			}			
		});
		
		this.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (drawState != 2) {
					if (e.getWheelRotation()<0) {
						brushSize+=brushScale;
						brushSize /= brushScale;
						brushSize*=brushScale;
					}else if(e.getWheelRotation()>0) {
						brushSize-=brushScale;
						brushSize = Math.max(brushSize, 2);
					}
					repaint();
				}
			}
			
		});
	}
	
	public void setType(int type) {
		terrainType = type;
	}
	
	public void setDrawState(int s) {
		drawState = s;
		if (s == 2) {
			brushSize = 20;
		}
	}
	
	public BufferedImage getImage() {
		return terrain.getImage();
	}

	public Terrain getTerrain(){
		return terrain;
	}
	
	public void setTerrain(Terrain t) {
		terrain = new Terrain(t);
	}
	
	public ArrayList<Animal> getAnimals() {
		return animals;
	}
	
	public void setAnimals(ArrayList<Animal> a) {
		animals = a;
	}
	
	public ArrayList<Vector> getWaypoints(){
		return waypoints;
	}
	
	public void setWaypoints(ArrayList<Vector> w) {
		waypoints = w;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
		BufferedImage img = terrain.getImage();
		
		AffineTransform af = new AffineTransform();
        af.scale((double)640/width, (double)640/height);
        g2.transform(af);
        
        g2.drawImage(img, 0, 0, this);
        
        Color lime = new Color(50, 205, 50);
        for (int i=0; i<waypoints.size(); i++) {
        	Vector w = waypoints.get(i);
        	g2.setColor(lime);
        	g2.fillOval((int)w.getX()-5, (int)w.getY()-5, 15, 15);
        	g2.setColor(java.awt.Color.BLACK);
        	g2.drawString(String.valueOf(i+1), (int)w.getX(), (int)w.getY()+5);
        }
        
        Color red = new Color(255, 0, 0);
        g2.setColor(red);
        for (int i=0; i<animals.size(); i++) {
        	Vector animalPos = animals.get(i).getPos();
        	g2.drawOval((int)animalPos.getX()-1, (int)animalPos.getY()-1, 2, 2);
        }
        
        
        g2.setColor(java.awt.Color.BLACK);
        g2.drawOval(mouseX-brushSize/2, mouseY-brushSize/2, brushSize, brushSize);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (drawState == 0) {
			paintTerrain(e);
		}else if(drawState == 1) {
			paintAnimal(e);
		}else if(drawState == 2) {
			paintWaypoint(e);
		}
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
	
	private void paintTerrain(MouseEvent e) {
		//mouseX = e.getX();
		//mouseY = e.getY();
		if (e.getButton() == 1 || e.getModifiersEx() == 1024) {
			terrain.setArea(mouseX, mouseY, brushSize/2, terrainType);
			
			for (int i=animals.size()-1; i>=0; i--) {
				Vector mouseV = new Vector(mouseX, mouseY);
				if (mouseV.dist(animals.get(i).getPos())<brushSize/2) {
					animals.remove(i);
				}
			}
		}else if (e.getButton() == 3 || e.getModifiersEx() == 4096) {
			terrain.setArea(mouseX, mouseY, brushSize/2, 0);
		}
	}
	
	private void paintWaypoint(MouseEvent e) {
		if (e.getButton() == 1 ) {
			Vector nW = new Vector(mouseX, mouseY);
			waypoints.add(nW);
		}else if (e.getButton() == 3 || e.getModifiersEx() == 4096) {
			for (int i=waypoints.size()-1; i>=0; i--) {
				Vector mouseV = new Vector(mouseX, mouseY);
				if (mouseV.dist(waypoints.get(i))<20) {
					waypoints.remove(i);
				}
			}
		}
	}
	
	private void paintAnimal(MouseEvent e) {
		if (e.getButton() == 1 || e.getModifiersEx() == 1024) {
			Vector newPos = new Vector(mouseX+Math.random()*brushSize-brushSize/2, mouseY+Math.random()*brushSize-brushSize/2);
			Vector mouseV = new Vector(mouseX, mouseY);
			
			if (mouseV.dist(newPos)>brushSize/2) {
				Vector temp = mouseV.sub(newPos);
				temp = temp.setMag(brushSize/2);
				newPos = mouseV.add(temp);
			}
			
			int[] edges = {0, width, 0, height};
			Animal temp = new Animal(edges);
			
			if (terrain.getPoint((int)newPos.getX(), (int)newPos.getY()) == 2 || terrain.getPoint((int)newPos.getX(), (int)newPos.getY()) ==3) {
				
			}else {
				temp.setPos(newPos);
				animals.add(temp);
			}
			
		}else if (e.getButton() == 3 || e.getModifiersEx() == 4096) {
			for (int i=animals.size()-1; i>=0; i--) {
				Vector mouseV = new Vector(mouseX, mouseY);
				if (mouseV.dist(animals.get(i).getPos())<brushSize/2) {
					animals.remove(i);
				}
			}
		}
	}
}
