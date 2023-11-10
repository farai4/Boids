package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Stream;

import javax.swing.JOptionPane;

/**
 * Takes in file name and loads terrain from it
 */
public class TerrainLoader {
    private String filePath;
    
    public void setTerrainFile(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Reads elevation file and stores heights in a 2D array
     * @return 2D array with heights
     */
    public Terrain getTerrain() {
        try {
            File myObj = new File(this.filePath);
            Scanner myReader = new Scanner(myObj);
            int y =0;
            String heading = myReader.nextLine();
            String[] dimensions = heading.trim().split(" ");
            int width = Integer.valueOf(dimensions[0]);
            int height = Integer.valueOf(dimensions[1]);
            double[][] heights = new double[width][height];
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] row = data.trim().split(" ");
                double[] values = Stream.of(row).mapToDouble(Double::parseDouble).toArray();
                
                if (values.length == width*height) {
                	int i=0;
                	int j=0;
                	while (i<height) {
                		while (j<width) {
                			heights[i][j] = values[i*width+j];
                			j++;
                		}
                		i++;
                		j=0;
                	}
                }else {
	                heights[y] = values;
	                y++;
                }
            }
            myReader.close();
            Terrain terrain = new Terrain(heights);
            terrain.setHeights(heights);
            return terrain;
        } catch (FileNotFoundException | NumberFormatException e) {
            return null;
        }
    }
}
