package model;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The <code>Terrain</code> class holds an <code>Array[][]</code> of terrain types and terrain heights
 */
public class Terrain {
	private int[][] terrainType;
	
	private double[][] heights;
    private double[][] gradientMap;
	
    private int width;
    private int height;
    private double min;
    private double max;
    private double angle = Math.PI/2;
    private boolean showSteep = false;
	
    /**
     * Constructs empty <code>Terrain</code> with given width and height
     * @param w width
     * @param h height
     */
	public Terrain(int w, int h) {
		width = w;
		height = h;
		terrainType = new int[height][width];
		heights = new double[height][width];
		
		for (int i=0; i<h; i++) {
			for (int j=0; j<w; j++) {
				terrainType[i][j] = 0;
				heights[i][j] = 0;
			}
		}
		createGradientMap();
		setMinMax();
	}
	
	/**
	 * Constructs new <code>Terrain</code> with values of given <code>Terrain</code>
	 * @param t given <code>Terrain</code>
	 */
	public Terrain(Terrain t) {
		width = t.getWidth();
		height = t.getHeight();
		terrainType = new int[height][width];
		heights = t.getHeights();
		gradientMap = t.getGradientMap();
		
		for (int i=0; i<height; i++) {
			for (int j=0; j<width; j++) {
				setPoint(j, i, t.getPoint(j, i));
			}
		}
		setMinMax();
	}
	
	/**
	 * Constructs new <code>Terrain</code> with values of heights
	 * @param h heights
	 */
	public Terrain(double[][] h) {
		width = h.length;
		height = h[0].length;
		terrainType = new int[height][width];
		heights = new double[height][width];
		
		for (int i=0; i<height; i++) {
			for (int j=0; j<width; j++) {
				heights[i][j] = h[i][j];
			}
		}
		createGradientMap();
		setMinMax();
	}
	
	/**
	 * Returns <code>Terrain</code>'s width
	 * @return width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Returns <code>Terrain</code>'s height
	 * @return height
	 */
    public int getHeight() {
        return height;
    }

    /**
     * Returns terrain value of given coordinate
     * @param x x position
     * @param y y position
     * @return terrain value
     */
	public int getPoint(int x, int y) {
		if (x<width&&y<height) {
			return terrainType[y][x];
		}
		return -1;
	}
	
	/**
	 * Sets terrain value of given coordinate
	 * @param x x position
	 * @param y y position
	 * @param type terrain type
	 */
	public void setPoint(int x, int y, int type) {
		if (x<width&&y<height) {
			terrainType[y][x] = type;
		}
	}
	
	/**
	 * Sets terrain values around given coordinate
	 * @param x x position
	 * @param y y position
	 * @param r radius
	 * @param type terrain type
	 */
	public void setArea(int x, int y, int r, int type) {
		int top = Math.max(y-r, 0);
		int left = Math.max(x-r, 0);
		int bottom = Math.min(y+r, height);
		int right = Math.min(x+r, width);
		
		for (int i=top; i<bottom; i++) {
			for (int j=left; j<right; j++) {
				if (Math.sqrt((i-y)*(i-y)+(j-x)*(j-x))<r) {
					setPoint(j,i,type);
				}
			}
		}
	}
	
	/**
	 * Returns terrain values around given coordinate
	 * @param x x position
	 * @param y y position
	 * @param r radius
	 * @return
	 */
	public int[][] getArea(int x, int y, int r){
		int top = y-r;
		int left = x-r;
		int h = r*2;
		int w = r*2;
		int [][] temp = new int[h][w];
		
		for (int i=Math.max(top,0); i<Math.min(h+Math.max(top,0)-1,this.height-1); i++) {
			for (int j=Math.max(left,0); j<Math.min(w+Math.max(left,0)-1,this.width-1); j++) {
				//if (Math.sqrt((i-y)*(i-y)+(j-x)*(j-x))< r) {
				int tempx = j-x+r;
				int tempy = i-y+r;
				if (tempx < r*2 && tempy<r*2) {
					temp[tempy][tempx] = getPoint(j,i);
				}
				//}
			}
		}
		
		return temp;
	}
	
	/**
	 * Returns int array of terrain types
	 * @return
	 */
	public int[][] getTerrain(){
		return terrainType;
	}
	
	/**
	 * Sets terrain type to given <code>Array</code>
	 * @param t <code>Array</code> of terrain types
	 */
	public void setTerrain(int[][] t) {
		if (t != null) {
			terrainType = t;
			height = t.length;
			if (t.length != 0) {
				width = t[0].length;
			}else {
				width = 0;
			}
			heights = new double[height][width];
		}
	}
	
	/**
	 * Returns <code>BufferedImage</code> derived from <code>Terrain</code>
	 * @return image
	 */
	public BufferedImage getImage() {
		BufferedImage img = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB);
		//Color white = new Color(255,255,255);
		Color green = new Color(0,255,0);
		Color blue = new Color(0,0,255);
		Color brown = new Color(116,71,48);
		Color red = new Color(255,0,0);
		
		for (int i=0; i<height; i++) {
			for (int j=0; j<width; j++) {
				if (terrainType[i][j] == 0) {
					int scaleVal = (int)scale(heights[i][j],min,max,0,255);
					Color temp = new Color(scaleVal, scaleVal, scaleVal);
					img.setRGB(j,i,temp.getRGB());
				}
				else if (terrainType[i][j] == 1) {
					Color temp = new Color(0, (int)scale(heights[i][j],min,max,50,green.getGreen()), 0);
					img.setRGB(j,i,temp.getRGB());
				}
				else if (terrainType[i][j] == 2) {
					Color temp = new Color(0, 0, (int)scale(heights[i][j],min,max,50,blue.getBlue()));
					img.setRGB(j,i,temp.getRGB());
				}
				else if (terrainType[i][j] == 3) {
					Color temp = new Color((int)scale(heights[i][j],min,max,50,brown.getRed()), (int)scale(heights[i][j],min,max,50,brown.getGreen()), (int)scale(heights[i][j],min,max,50,brown.getBlue()));
					img.setRGB(j,i,temp.getRGB());
				}
			}
		}
		if (showSteep) {
			for (int i=1; i<height-1; i++) {
				for (int j=1; j<width-1; j++) {
					if (gradientMap[i-1][j-1]>angle) {
						img.setRGB(j, i, red.getRGB());
					}
				}
			}
		}
		return img;
	}
	
	/**
	 * Returns area around given coordinate as a <code>String</code>
	 * @param x x position
	 * @param y y position
	 * @param r radius
	 * @return String
	 */
	public String areaToString(int x, int y, int r) {
		int[][] view = getArea(x,y,r);
		String val = "";
		for (int i=0; i<view.length; i++) {
			for (int j = 0; j<view[i].length; j++) {
				if (i == view.length/2 && j == view.length/2 ) {
					val += "X";
				}else {
					val += view[i][j];
				}
			}
			val += "\n";
		}
		val += "\n";
		return val;
	}
	
	/**
	 * Returns heights
	 * @return heights
	 */
	public double[][] getHeights() {
        return heights;
    }

	/**
	 * Sets heights
	 * @param heights heights
	 */
    public void setHeights(double[][] heights) {
        this.heights = heights;
    }

    /**
     * Returns gradient map of <code>Terrain</code>
     * @return gradient map
     */
    public double[][] getGradientMap() {
        return gradientMap;
    }

    /**
     * Sets gradient map of <code>Terrain</code>
     * @param gm gradient map
     */
    public void setGradientMap(double[][] gm) {
        this.gradientMap = gm;
    }
    
    /**
     * Creates gradient map for <code>Terrain</code>
     */
    private void createGradientMap() {
    	if (height<2||width<2) {
    		return;
    	}
    	gradientMap = new double[height-2][width-2];
    	for (int i=0; i<gradientMap.length; i++) {
    		for (int j=0; j<gradientMap[i].length; j++) {
    			gradientMap[i][j] = computeAverageGradient(i,j);
    		}
    	}
    }
    
    /**
     * Returns average slope between given x y coordinate and the coordinates to the right, bottom and bottom right of the given coordinate
     * @param y y coordinate
     * @param x x coordinate
     * @return average slope
     */
    private double computeAverageGradient(int y, int x) {
    	double sum = 0;
    	sum += getGradient(new Vector(x,y), new Vector(x+1,y));
    	sum += getGradient(new Vector(x,y), new Vector(x,y+1));
    	sum += getGradient(new Vector(x,y), new Vector(x+1,y+1));
    	
    	sum += getGradient(new Vector(x+1,y), new Vector(x,y+1));
    	sum += getGradient(new Vector(x+1,y), new Vector(x+1,y+1));
    	
    	sum += getGradient(new Vector(x,y+1), new Vector(x+1,y+1));
    	
        double average = sum/6;
        return average;
    }
    
    /**
     * Returns slope between two given points
     * @param p1 First point
     * @param p2 Second point
     * @return slope
     */
    private double getGradient(Vector p1, Vector p2) {
    	double height1 = heights[(int)p1.getY()][(int)p1.getX()];
    	double height2 = heights[(int)p2.getY()][(int)p2.getX()];
    	
    	double dx = p1.getX()-p2.getX();
    	double dy = p1.getX()-p2.getX();
    	double dz = height1-height2;
    	double xyDist = Math.sqrt(dx*dx+dy*dy);
    	
    	double dp = Math.sqrt(dz*dz+xyDist*xyDist);
    	double slope = 0;
    	if(dp!=0) {
    		slope = Math.acos((double)(xyDist/dp));
    	}
    	//System.out.println(slope);
    	return Math.abs(slope);
    }
    
    /**
     * Returns slopes around given point
     * @param x x coordinate
     * @param y y coordinate
     * @param r radius
     * @return <code>Array</code> of slopes
     */
    public double[][] getGradientArea(int x, int y, int r){
		int top = y-r;
		int left = x-r;
		int h = r*2;
		int w = r*2;
		double [][] temp = new double[h][w];
		
		for (int i=Math.max(top,0); i<Math.min(h+Math.max(top,0)-1,this.height-2); i++) {
			for (int j=Math.max(left,0); j<Math.min(w+Math.max(left,0)-1,this.width-2); j++) {
				int tempx = j-x+r;
				int tempy = i-y+r;
				if (tempx < r*2 && tempy<r*2) {
					temp[tempy][tempx] = gradientMap[i][j];
				}
			}
		}
		
		return temp;
	}
    
    /**
     * Sets minimum and maximum height of <code>Terrain</code>
     */
    private	void setMinMax() {
		max = 0;
		min = 100000;
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				max = Double.max(max,heights[y][x]);
				min = Double.min(min,heights[y][x]);
			}
		}
		if (max == 0) {
			min = -1;
		}
	}
    
    /**
     * Resets terrain type <code>Array</code>
     */
    public void resetTerrainType() {
    	for (int i=0; i<height; i++) {
			for (int j=0; j<width; j++) {
				terrainType[i][j] = 0;
			}
		}
    }
    
    /**
     * Sets whether <code>getImage()</code> will show steep slopes
     * @param s show steep slopes <code>boolean</code>
     */
    public void setShowSteep(boolean s) {
    	showSteep = s;
    }
    
    /**
     * Sets angle steepness needed to show
     * @param a slope angle
     */
    public void setAngle(double a) {
    	angle = a;
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
    
    public String terrainTypeString() {
    	String s = "";
    	for (int[] row: terrainType) {
			s += Arrays.toString(row);
			s += "\n";
		}
    	return s;
    }
    
    @Override
    public String toString() {
		String s = String.valueOf(width)+" "+String.valueOf(height)+"\n";
		for (double[] row: heights) {
			s += Arrays.toString(row);
			s += "\n";
		}
		s += "\n";
		for (int[] row: terrainType) {
			s += Arrays.toString(row);
			s += "\n";
		}
		return s;
    }
}
