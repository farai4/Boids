package presenter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import model.*;
import view.*;

public class BoidPresenter implements ChangeListener, ActionListener, Runnable{	
	//simulation states
	private volatile boolean running;
	private volatile boolean rewinding;
	private volatile boolean recording;
	
	private int edgeType;
	
	private ArrayList<Animal> boids = new ArrayList<Animal>();
	static BoidView bv = new BoidView();

	private Terrain terrain;
	private ArrayList<Vector> waypoints = new ArrayList<Vector>();
	
	private int btnWidth = 60;								
	private int btnHeight = 30;	
	private int posButtonTop;
	
	private Timer timer;
	private int runsPerFrame = 1;	
	private int normalSpeed = 1;
	private int fastSpeed = 2;
	private int ticks = 0;
	
	private int recordIndex = -1;
	
	private JCheckBox cbxWaypoints;
	
	//Buttons on main view
	private JButton btnDraw;
	private JButton btnRewind;
	private JButton btnStart;
	private JButton btnStop;
	private JButton btnReset;
	private JButton btnFastForward;
	private JButton btnEdit;
	private JButton btnUpload;
	private JButton btnExport;
	private JButton btnImport;
	private JButton btnRecord;
	private JCheckBox chkSteep = new JCheckBox("Show steep areas", false);
	
	//edit frame elements
	//cohesion
	private JLabel lblCohesion = new JLabel("Cohesion value:");
	private JSlider sldrCohesion = new JSlider();
	private JTextField txtCohesion = new JTextField();
	private double cohesion;
	
	//alignment
	private JLabel lblAlignment = new JLabel("Alignment value:");
	private JSlider sldrAlignment = new JSlider();
	private JTextField txtAlignment = new JTextField();
	private volatile double alignment;
	
	//separation
	private JLabel lblSeparation = new JLabel("Separation value:");
	private JSlider sldrSeparation = new JSlider();
	private JTextField txtSeparation = new JTextField();
	private volatile double separation;
	
	//View Distance
	private JLabel lblViewDist = new JLabel("View Distance:");
	private JSlider sldrViewDist = new JSlider();
	private JTextField txtViewDist = new JTextField();
	private volatile double viewDist;
	
	//Max slope
	private JLabel lblMaxSlope = new JLabel("Max Slope:");
	private JSlider sldrMaxSlope = new JSlider();
	private JTextField txtMaxSlope = new JTextField();
	private volatile double maxSlope;
	
	//previous values of edit frame
	private volatile double prevSeparation;
	private volatile double prevAlignment;
	private volatile double prevCohesion;
	private volatile double prevViewDist;
	private volatile String prevAnimal;
	private volatile double prevMaxSlope;
	
	JFrame editF = new JFrame("Edit");
	
	private volatile String animalType = "Elephant";
	
	//draw elements
	private JFrame drawF;
	private DrawPanel drawP;
	private JLabel lblDraw;
	private JRadioButton rbnWoods;
	private JRadioButton rbnRocks;
	private JRadioButton rbnWater;
	
	private String heightsMapName;
	
	public static void main(String[] args) {
		bv.initialize();
		BoidPresenter bp = new BoidPresenter();
		Thread t = new Thread(bp);										//create and start BoidPresenter thread
		t.start();
	}
	
	public BoidPresenter() {
		running = false;												//initialize simulation running state to false
		rewinding = false;												//initialize simulation rewinding state to false
		updateAnimalType(animalType);
		
		prevSeparation = separation;
		prevCohesion = cohesion;
		prevAlignment = alignment;
		prevViewDist = viewDist;
		prevAnimal = animalType;
		prevMaxSlope = maxSlope;
		
		bv.getPanel().setBoids(boids);
		terrain = new Terrain(bv.getPanel().getWidth(), bv.getPanel().getHeight());
		bv.getPanel().setBackground(terrain.getImage());
		bv.setFrame(addElements(bv.getFrame()));						//add all gui elements to gui
		
		timer = new Timer(1, this);							//instantiate timer with normal speed
	    timer.start();
	}
	
	/**
	 * Resets simulation
	 */
	public void reset() {
		boids = new ArrayList<Animal>();
		
		runsPerFrame = normalSpeed;									//set simulation to normal speed
		stopRun();														//stop simulation
		SimulationPanel p = bv.getPanel();								
		terrain.resetTerrainType();
		p.setBackground(terrain.getImage());
		p.setBoids(boids);												//add boids to panel
		p.repaint();													
		stopRewind();													//ensure not rewinding
	}
	
	/**
	 * Sets simulation state to rewinding and speeds up simulation
	 */
	public void rewind() {
		for(int i = 0; i < boids.size(); i++) {								//set each boid to rewind state
			boids.get(i).rewind();
		}
		runsPerFrame = fastSpeed;									//speed up simulation
		rewinding = true;
		running = true;
	}
	
	/**
	 * Sets simulation state to nor rewinding
	 */
	public void stopRewind() {
		for(int i = 0; i < boids.size(); i++) {								//set each boid to not rewinding state
			boids.get(i).stopRewind();
		}
		rewinding = false;
		running = false;
		runsPerFrame = normalSpeed;									//slow down simulation
	}
	
	/**
	 * Sets simulation state to running and simulation speed to normal
	 */
	public void startRun() {											//starts simulation
		running = true;	
		runsPerFrame = normalSpeed;	
		//stopRewind();
	}
	
	/**
	 * Sets simulation state to not running and simulation speed to normal
	 */
	public void stopRun() {												//stops simulation
		running = false;
		runsPerFrame = normalSpeed;	
		stopRewind();
	}
	
	/**
	 * Returns simulation running state
	 * @return running state of simulation
	 */
	public boolean getRunning() {										//return status of simulation
		return running;
	}
	
	/**
	 * Takes in <code>Frame</code>, adds all relevant elements and returns <code>Frame</code>
	 * @param f <code>Frame</code> to add elements to
	 * @return <code>Frame</code> with added elements
	 */
	public JFrame addElements(JFrame f) {
		//initialize buttons
		int[] field = bv.getPanel().getFieldDimensions();
		posButtonTop = field[3]+10;
		
		btnRewind = button("◁◁", 10, posButtonTop+btnHeight+5);
		btnRewind.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){
				rewind();
			}
		});
		
		btnReset = button("Reset simulation", 10, posButtonTop+btnHeight*2+10, (int)(btnWidth*2.5), btnHeight);
		btnReset.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){
				reset();
			}
		});
		
		btnStop = button("▯▯", 20+btnWidth, posButtonTop+btnHeight+5);
		btnStop.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){
				stopRun();
			}
		});
		
		btnStart = button("▷", 30+btnWidth*2, posButtonTop+btnHeight+5);
		btnStart.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){
				startRun();
			}
		});
		
		btnFastForward = button("▷▷", 40+btnWidth*3, posButtonTop+btnHeight+5);
		btnFastForward.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){
				startRun();
				runsPerFrame = fastSpeed;	
			}
		});
		
		btnEdit= button("Edit animal", bv.getPanel().getWidth()+10, 350, 130, 30);
		btnEdit.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){
				edit();
			}
		});
		
		btnDraw = button("Draw", btnEdit.getX(), btnEdit.getY()+btnEdit.getHeight()+10, 100, 30);
		btnDraw.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){
				draw();
			}
		});
		
		//EDGE RADIO BUTTONS
		JLabel l = new JLabel("Edge avoidance");
		l.setBounds(field[1]+20, 20, 100, 30);
		
		JRadioButton edgeAvoid = new JRadioButton("Avoid", true);
		edgeAvoid.setBounds(field[1]+20, 50, 100, 20);
		edgeAvoid.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (Animal b: boids) {
					b.setEdgeType(0);
					edgeType = 0;
				}
			}
		});
		
		JRadioButton edgeWrap = new JRadioButton("Wraparound");
		edgeWrap.setBounds(field[1]+20, 80, 100, 20);
		edgeWrap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (Animal b: boids) {
					b.setEdgeType(2);
					edgeType = 2;
				}
			}
		});
		
		JRadioButton edgeBounce = new JRadioButton("Bounce");
		edgeBounce.setBounds(field[1]+20, 110, 100, 20);
		edgeBounce.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (Animal b: boids) {
					b.setEdgeType(1);
					edgeType = 1;
				}
			}
		});
		
		//Waypoints checkbox
		cbxWaypoints = new JCheckBox("Show waypoints", false);
		cbxWaypoints.setBounds(bv.getPanel().getWidth()+10, 300, 140, 30);
		cbxWaypoints.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				bv.getPanel().setShowWaypoints(cbxWaypoints.isSelected());
				bv.getPanel().repaint();
			}
			
		});
		
		//Steep checkbox
		JSlider sldrSteep = new JSlider();
		JLabel lblSteep = new JLabel();
		sldrSteep.setVisible(false);
		lblSteep.setVisible(false);
		lblSteep.setBounds(bv.getPanel().getWidth()+120, 260, 150, 20);
		
		chkSteep.setBounds(bv.getPanel().getWidth()+10, 230, 140, 30);
		chkSteep.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				terrain.setShowSteep(chkSteep.isSelected());
				sldrSteep.setVisible(chkSteep.isSelected());
				lblSteep.setVisible(chkSteep.isSelected());
				
				if (boids != null && boids.size()!=0) {
					sldrSteep.setValue((int)(maxSlope*100));
				}
				bv.getPanel().setBackground(terrain.getImage());
				bv.getPanel().repaint();
			}
		
		});
		
		
		sldrSteep.setBounds(bv.getPanel().getWidth()+10, 260, 100, 20);
		sldrSteep.setMaximum((int)(Math.PI/2*100+1));
		sldrSteep.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent ce) {
				double val = sldrSteep.getValue()*0.01;
				val = (double) Math.round(val * 100) / 100;
				terrain.setAngle(val);
				lblSteep.setText(String.valueOf(Math.round(val/Math.PI*180*100)/100));
				
				bv.getPanel().setBackground(terrain.getImage());
				bv.getPanel().repaint();
			}
		});
		
		
		btnUpload = button("Upload terrain", 110+btnWidth*5, posButtonTop+btnHeight*2+10, btnWidth*2, btnHeight);
		btnUpload.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				setEnabled(false);
				JFileChooser fileChooser = new JFileChooser("data/elevation");
				int result = fileChooser.showOpenDialog(f);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					heightsMapName = selectedFile.getAbsolutePath();
					Terrain temp = loadTerrain(selectedFile.getAbsolutePath());
					if (temp == null) {
						JOptionPane.showMessageDialog(null,"Failed to load file");
						setEnabled(true);
						return;
					}
					terrain = new Terrain(temp);
					SimulationPanel newP = new SimulationPanel(terrain.getWidth(), terrain.getHeight());
					bv.setPanel(newP);
					
					bv.getPanel().setWaypoints(waypoints);
					bv.getPanel().setBackground(terrain.getImage());
					
					boids = new ArrayList<Animal>();
					waypoints = new ArrayList<Vector>();
					bv.getPanel().setBoids(boids);
					bv.getPanel().setWaypoints(waypoints);
					
					bv.getPanel().repaint();
				}
				setEnabled(true);
			}
		});
		
		btnExport = button("Save state", 110+btnWidth*5, posButtonTop+btnHeight+5, 100, 30);
        btnExport.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                setEnabled(false);
                JFileChooser jfc = new JFileChooser("data/exports");
                String defaultName = java.time.LocalDate.now().toString()+java.time.LocalTime.now().getHour()+java.time.LocalTime.now().getMinute();
                jfc.setSelectedFile(new File(defaultName+".txt"));
                int result = jfc.showSaveDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                	File f = jfc.getSelectedFile();
                	export(f.getAbsolutePath());
                }
                setEnabled(true);
            }
        });
        
		btnImport = button("Load state", 200+btnWidth*5, posButtonTop+btnHeight+5, 100, 30);
		btnImport.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
                setEnabled(false);
                
                JFileChooser jfc = new JFileChooser("data/exports");
                int result = jfc.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                	File f = jfc.getSelectedFile();
                	load(f.getAbsolutePath());
                }
                setEnabled(true);
				//load("export.txt");
			}
		});
		
		btnRecord = button("⏺", 50+btnWidth*4, posButtonTop+btnHeight+5);
		btnRecord.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    if(!recording) {
			    	if (boids == null||boids.size()==0) {
			    		return;
			    	}
			    	setEnabled(false);
			    	btnRecord.setEnabled(true);
			    	startRun();
			    	
			        btnRecord.setText("⏹");
			        recordIndex = boids.get(0).getPreviousPos().size();
			        recording = true;
                } else {
                	setEnabled(true);
                	stopRun();
                	
                    btnRecord.setText("⏺");
                    record();
                    recording = false;
                }
			}
		});
		
		f.add(cbxWaypoints);
		f.add(chkSteep);
		f.add(sldrSteep);
		f.add(lblSteep);
		
		//add radiobuttons to buttongroup
		ButtonGroup edgeBg = new ButtonGroup();
		edgeBg.add(edgeAvoid);
		edgeBg.add(edgeWrap);
		edgeBg.add(edgeBounce);
				
		//add buttons to panel
		f.add(btnRewind);
		f.add(btnReset);										
		f.add(btnStart);
		f.add(btnStop);
		f.add(btnFastForward);
		f.add(btnDraw);
		f.add(btnUpload);
		f.add(btnEdit);
		f.add(btnImport);
		f.add(btnExport);
		f.add(btnRecord);
		
		//add radiobuttons with label
		f.add(l);
		f.add(edgeAvoid);
		f.add(edgeWrap);
		f.add(edgeBounce);
	
		return f;
	}
	
	/**
	 * Returns a <code>Button</code> with given name, x coordinate, y coordinate and default button size
	 * @param name <code>Button</code> text
	 * @param x <code>Button</code> x position
	 * @param y <code>Button</code> y position
	 * @return <code>Button</code>
	 */
	public JButton button(String name, int x, int y) {	
		JButton btnTemp = new JButton(name);
		btnTemp.setBounds(x, y, btnWidth , btnHeight);
		return btnTemp;
	}
	
	/**
	 * Returns a <code>Button</code> with given name, x coordinate, y coordinate, width and height
	 * @param name <code>Button</code> text
	 * @param x <code>Button</code> x position
	 * @param y <code>Button</code> y position
	 * @param w <code>Button</code> width
	 * @param h <code>Button</code> height
	 * @return
	 */
	public JButton button(String name, int x, int y, int w, int h) {	
		//returns button with given name, position and size parameters
		JButton btnTemp = new JButton(name);
		btnTemp.setBounds(x, y, w, h);
		return btnTemp;
	}
	
	/**
	 * Sets cohesion bias of all animals
	 * @param c cohesion bias
	 */
	public void setCohesion(double c) {
		for(int i = 0; i < boids.size(); i++) {
			boids.get(i).setCohesion(c);
		}
		cohesion = c;
		sldrCohesion.setValue((int)(c*100));
		txtCohesion.setText(String.valueOf(c));
	}
	
	/**
	 * Sets alignment bias of all animals
	 * @param a alignment bias
	 */
	public void setAlignment(double a) {
		for(int i = 0; i < boids.size(); i++) {
			boids.get(i).setAlignment(a);
		}
		alignment = a;
		sldrAlignment.setValue((int)(a*100));
		txtAlignment.setText(String.valueOf(a));
	}
	
	/**
	 * Sets separation bias of all animals
	 * @param s separation bias
	 */
	public void setSeparation(double s) {
		for(int i = 0; i < boids.size(); i++) {
			boids.get(i).setSeparation(s);
		}
		separation = s;
		sldrSeparation.setValue((int)(s*100));
		txtSeparation.setText(String.valueOf(s));
	}
	
	/**
	 * Sets view distance of all animals
	 * @param vd
	 */
	public void setViewDist(double vd) {
		for(int i = 0; i < boids.size(); i++) {
			boids.get(i).setViewDist(vd);
		}
		viewDist = vd;
		sldrViewDist.setValue((int)(vd*100));
		txtViewDist.setText(String.valueOf(vd));
	}
	
	public void setMaxSlope(double ms) {
		for(int i = 0; i < boids.size(); i++) {
			boids.get(i).setMaxSlope(ms);
		}
		maxSlope = ms;
		//sldrMaxSlope.setValue((int)(ms*100));
		//txtMaxSlope.setText(String.valueOf(String.valueOf(Math.round(ms*18000/Math.PI)/100)));
	}
	
	/**
	 * Creates draw frame and initializes elements
	 */
	public void draw() {	
		setEnabled(false);
		
		//instantiate draw frame
		drawF = new JFrame("Draw");
		
		//instantiate draw panel
		drawP = new DrawPanel(terrain, waypoints);
		drawP.setAnimals((ArrayList<Animal>) boids.clone());
		
		//Drawing combobox
		String[] drawType = {"Terrain", "Animals", "Waypoints"};
		JComboBox  cbxDraw = new JComboBox (drawType);
		cbxDraw.setSelectedIndex(0);
		cbxDraw.setBounds(drawP.getWidth()+10, 10, 100, 20);
		cbxDraw.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int selected = cbxDraw.getSelectedIndex();
				
				drawState(selected);
			}
		});
		
		//instantiate terrain type label
		lblDraw = new JLabel("Terrain type");
		lblDraw.setBounds(drawP.getWidth()+10, cbxDraw.getY()+cbxDraw.getHeight(), 100, 20);
		
		//instantiate add terrain button
		JButton btnAddTerrain = new JButton("Add");
		btnAddTerrain.setBounds(drawP.getWidth()+10, 150, btnWidth, btnHeight);
		btnAddTerrain.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				terrain = new Terrain(drawP.getTerrain());
				waypoints = drawP.getWaypoints();
				bv.getPanel().setWaypoints(waypoints);
				bv.getPanel().setBackground(terrain.getImage());
				
				boids = drawP.getAnimals();
				for(int i = 0; i < boids.size(); i++) {
					Animal b;
					b = boids.get(i);
					b.setTerrain(terrain);
					b.setWaypoints(waypoints);
					b.setAnimalType(animalType);
					boids.set(i,b);
				}
				setCohesion(cohesion);
				setAlignment(alignment);
				setSeparation(separation);
				
				SimulationPanel p = bv.getPanel();
				p.setBoids(boids);
				p.repaint();
				drawF.setVisible(false);
				drawF.dispatchEvent(new WindowEvent(drawF, WindowEvent.WINDOW_CLOSING));
				setEnabled(true);
			}
			
		});
		
		//instantiate cancel button
		JButton btnCancelTerrain = new JButton("Cancel");
		btnCancelTerrain.setBounds(drawP.getWidth()+10, 190, btnWidth*2, btnHeight);
		btnCancelTerrain.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				drawP.setTerrain(terrain);
				drawF.setVisible(false);
				drawF.dispatchEvent(new WindowEvent(drawF, WindowEvent.WINDOW_CLOSING));
				setEnabled(true);
			}
			
		});
		
		//instantiate woods radio button and listener
		rbnWoods = new JRadioButton("Woods", true);
		rbnWoods.setBounds(drawP.getWidth()+10, lblDraw.getY()+lblDraw.getHeight(), 100, 20);
		rbnWoods.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				drawP.setType(1);
			}
		});
		
		//instantiate water radio button and listener
		rbnWater = new JRadioButton("Water", false);
		rbnWater.setBounds(drawP.getWidth()+10, rbnWoods.getY()+rbnWoods.getHeight(), 100, 20);
		rbnWater.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				drawP.setType(2);
			}
		});
		
		//instantiate rocks radio button and listener
		rbnRocks = new JRadioButton("Rocks", false);
		rbnRocks.setBounds(drawP.getWidth()+10, rbnWater.getY()+rbnWater.getHeight(), 100, 20);
		rbnRocks.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				drawP.setType(3);
			}
		});
		
		//add radio buttons to group
		ButtonGroup typeBg = new ButtonGroup();
		typeBg.add(rbnWoods);
		typeBg.add(rbnWater);
		typeBg.add(rbnRocks);
		
		//initialize frame values
		drawF.setBounds(0, 0, bv.getPanel().getWidth()+300, bv.getPanel().getHeight()+35);
		drawF.setLayout(null);
		//drawF.setAlwaysOnTop(true);
		drawF.setDefaultCloseOperation(drawF.DO_NOTHING_ON_CLOSE);;
		
		//add elements to frame
		drawF.add(drawP);
		drawF.add(cbxDraw);
		drawF.add(lblDraw);
		drawF.add(rbnWoods);
		drawF.add(rbnWater);
		drawF.add(rbnRocks);
		drawF.add(btnAddTerrain);
		drawF.add(btnCancelTerrain);
		
		//show frame
		drawF.setVisible(true);
	}
	
	/**
	 * Sets draw state
	 * @param state
	 */
	private void drawState(int state) {
		drawP.setDrawState(state);
		switch (state) {
			case 0:
				lblDraw.setVisible(true);
				lblDraw.setText("Terrain type");
				rbnWoods.setVisible(true);
				rbnRocks.setVisible(true);
				rbnWater.setVisible(true);
				break;
			case 1:
				lblDraw.setVisible(true);
				lblDraw.setText(animalType);
				rbnWoods.setVisible(false);
				rbnRocks.setVisible(false);
				rbnWater.setVisible(false);
				break;
			case 2:
				lblDraw.setVisible(false);
				rbnWoods.setVisible(false);
				rbnRocks.setVisible(false);
				rbnWater.setVisible(false);
				break;
		}
	}
	
	/**
	 * Takes in path of <code>File</code> to be loaded and returns <code>Terrain</code>
	 * @param filePath path to file
	 * @return <code>Terrain</code>
	 */
	private Terrain loadTerrain(String filePath) {
		TerrainLoader terrainLoader = new TerrainLoader();
		terrainLoader.setTerrainFile(filePath);
		return terrainLoader.getTerrain();
	}
	
	/**
	 * Initializes edit frame with appropriate elements
	 */
	private void edit() {
		setEnabled(false);
		editF.setLayout(null);
		editF.setDefaultCloseOperation(editF.DO_NOTHING_ON_CLOSE);
		editF.setBounds(btnEdit.getX()/2,100,400,400);
		
		String[] animals = {"Elephant", "Penguin", "Deer"};
		JComboBox cbxAnimal = new JComboBox(animals);
		cbxAnimal.setBounds(10,10,100,30);
		cbxAnimal.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateAnimalType((String)cbxAnimal.getSelectedItem());
			}
		});
		
		JButton btnSaveEdit = new JButton("Save");
		btnSaveEdit.setBounds(editF.getWidth()-btnWidth*2-20, 10, btnWidth*2, btnHeight);
		btnSaveEdit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				prevSeparation = separation;
				prevCohesion = cohesion;
				prevAlignment = alignment;
				prevViewDist = viewDist;
				prevAnimal = animalType;
				prevMaxSlope = maxSlope;
				
				editF.setVisible(false);
				editF.dispatchEvent(new WindowEvent(editF, WindowEvent.WINDOW_CLOSING));
				setEnabled(true);
			}
			
		});
		
		JButton btnCancelEdit = new JButton("Cancel");
		btnCancelEdit.setBounds(btnSaveEdit.getX(), btnSaveEdit.getY()+btnSaveEdit.getHeight()+10, btnWidth*2, btnHeight);
		btnCancelEdit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setSeparation(prevSeparation);
				setAlignment(prevAlignment);
				setCohesion(prevCohesion);
				setViewDist(prevViewDist);
				setMaxSlope(prevMaxSlope);
				animalType = prevAnimal;
				
				editF.setVisible(false);
				editF.dispatchEvent(new WindowEvent(editF, WindowEvent.WINDOW_CLOSING));
				setEnabled(true);
			}
			
		});
		
		int labelXVal = 250;
		int sliderXVal = 250-20;
		int textXVal = 250+80;
		//COHESION
		lblCohesion.setBounds(labelXVal, 180, 100, 20);
		
		sldrCohesion.setBounds(sliderXVal, 200, 100, 20);
		sldrCohesion.setMaximum(10000);
		sldrCohesion.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent ce) {
				double val = sldrCohesion.getValue()*0.01;
				val = (double) Math.round(val * 100) / 100;
				txtCohesion.setText(String.valueOf(val));
				setCohesion(val);
			}
		});
				
		txtCohesion.setBounds(textXVal,200,40,20);
		txtCohesion.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				double val = Double.valueOf(txtCohesion.getText());
				sldrCohesion.setValue((int)(val*100));
				setCohesion(val);
			}
		});
		sldrCohesion.setValue((int)(cohesion*100));
		
		//ALIGNMENT		
		lblAlignment.setBounds(labelXVal, 220, 100, 20);
		
		sldrAlignment.setBounds(sliderXVal, 240, 100, 20);
		sldrAlignment.setMaximum(10000);
		sldrAlignment.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent ce) {
				double val = sldrAlignment.getValue()*0.01;
				val = (double) Math.round(val * 100) / 100;
				txtAlignment.setText(String.valueOf(val));
				setAlignment(val);
			}
		});
		
		txtAlignment.setBounds(textXVal,240,40,20);
		txtAlignment.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				double val = Double.valueOf(txtAlignment.getText());
				sldrAlignment.setValue((int)(val*100));
				setAlignment(val);
			}
		});
		sldrAlignment.setValue((int)alignment*100);
		
		//SEPARATION
		lblSeparation.setBounds(labelXVal, 260, 100, 20);
		
		sldrSeparation.setBounds(sliderXVal, 280, 100, 20);
		sldrSeparation.setMaximum(10000);
		sldrSeparation.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent ce) {
				double val = sldrSeparation.getValue()*0.01;
				val = (double) Math.round(val * 100) / 100;
				txtSeparation.setText(String.valueOf(val));
				setSeparation(val);
			}
		});
		
		txtSeparation.setBounds(textXVal,280,40,20);
		txtSeparation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				double val = Double.valueOf(txtSeparation.getText());
				sldrSeparation.setValue((int)(val*100));
				setSeparation(val);
			}
		});
		sldrSeparation.setValue((int)separation*100);
		
		//View distance
		lblViewDist.setBounds(40, 180, 100, 20);
		
		sldrViewDist.setBounds(20, 200, 100, 20);
		sldrViewDist.setMaximum(10000);
		sldrViewDist.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent ce) {
				double val = sldrViewDist.getValue()*0.01;
				val = (double) Math.round(val * 100) / 100;
				txtViewDist.setText(String.valueOf(val));
				setViewDist(val);
			}
		});
		
		txtViewDist.setBounds(130,200,40,20);
		txtViewDist.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				double val = Double.valueOf(txtViewDist.getText());
				sldrViewDist.setValue((int)(val*100));
				setViewDist(val);
			}
		});
		sldrViewDist.setValue((int)viewDist*100);
		
		//Max slope
		lblMaxSlope.setBounds(40, 220, 100, 20);
				
		sldrMaxSlope.setBounds(20, 240, 100, 20);
		sldrMaxSlope.setMaximum((int)(Math.PI/2*1000+1));
		sldrMaxSlope.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent ce) {
				double val = sldrMaxSlope.getValue();
				val = (double) (val/Math.PI*180)/1000;
				val = (double) Math.round(val * 100) / 100;
				txtMaxSlope.setText(String.valueOf(val));
				setMaxSlope(sldrMaxSlope.getValue()*0.001);
			}
		});
		
		txtMaxSlope.setBounds(130,240,40,20);
		txtMaxSlope.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				double val = Double.valueOf(txtMaxSlope.getText());
				sldrMaxSlope.setValue((int)(val/180*Math.PI*1000));
				setMaxSlope(val);
			}
		});
		sldrMaxSlope.setValue((int)(maxSlope*1000));
		
		editF.add(cbxAnimal);
		editF.add(btnSaveEdit);
		editF.add(btnCancelEdit);
		
		editF.add(lblCohesion);
		editF.add(txtCohesion);
		editF.add(sldrCohesion);
		
		editF.add(lblAlignment);
		editF.add(txtAlignment);
		editF.add(sldrAlignment);
		
		editF.add(lblSeparation);
		editF.add(txtSeparation);
		editF.add(sldrSeparation);
		
		editF.add(lblViewDist);
		editF.add(txtViewDist);
		editF.add(sldrViewDist);
		
		editF.add(lblMaxSlope);
		editF.add(txtMaxSlope);
		editF.add(sldrMaxSlope);
		
		editF.setVisible(true);
	}
	
	/**
	 * Sets each animal's type to given animal type
	 * @param at animal type
	 */
	public void updateAnimalType(String at) {
		animalType = at;
		
		Animal tempA = new Animal(bv.getPanel().getFieldDimensions());
		tempA.setAnimalType(animalType);
		setAlignment(tempA.getAlignment());
		setCohesion(tempA.getCohesion());
		setSeparation(tempA.getSeparation());
		setViewDist(tempA.getViewDist());
		setMaxSlope(tempA.getMaxSlope());
		
		if (boids!=null) {
			for (Animal b: boids) {
				b.setAnimalType(at);
			}
		}
	}
	
	/**
	 * Writes terrain, waypoints and boid positions to text file
	 * @param fileName text file's name
	 */
	public void export(String fileName) {
		Terrain t=new Terrain(terrain);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			int[][] array= t.getTerrain();
			writer.write("Terrain "+t.getWidth()+" "+t.getHeight()+" "+heightsMapName+"\n");
			for (int[] row : array) {
				for (int num : row) {
					writer.write(num+" ");
				}
				writer.newLine();
			}
			writer.write("Waypoints\n");
			for (Vector w: waypoints) {
				double x = w.getX();
                double y = w.getY();
                writer.write(x+" "+y+"\n");
			}
			writer.write("Animals\n");
			for (Animal animal : boids) {
                double x = animal.getPos().getX();
                double y = animal.getPos().getY();
                writer.write(x+" "+y+"\n");
			}
			writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

	/**
	 * Loads terrain, waypoints and boid positions from text file
	 * @param fileName text file's name
	 */
	public void load(String fileName){
		try {
			Scanner reader = new Scanner(new FileReader(fileName));
			
			String heading = reader.nextLine();
			String[] dimensions = heading.trim().split(" ");
			int width = Integer.valueOf(dimensions[1]);
            int height = Integer.valueOf(dimensions[2]);
            Terrain temp= new Terrain(width,height);
            
            if (dimensions.length==4) {
            	heightsMapName = dimensions[3];
            	TerrainLoader tl = new TerrainLoader();
            	tl.setTerrainFile(heightsMapName);
            	temp = tl.getTerrain();
            }
			
			
            waypoints= new ArrayList<Vector>();
            boids = new ArrayList<Animal>();
            
            int[][] loadedTerrain = new int[height][width];
			
			int i = 0;
			String line = reader.nextLine();
			while (!line.equals("Waypoints")) {
				String[] elements = line.trim().split(" ");
				for (int j = 0; j < elements.length; j++) {
					loadedTerrain[i][j] = Integer.parseInt(elements[j]);
				}
				i++;
				line = reader.nextLine();
			}
			line = reader.nextLine();
			while (!line.equals("Animals")) {
				String[] elements = line.trim().split(" ");
				Vector w = new Vector(Double.valueOf(elements[0]), Double.valueOf(elements[1]));
				waypoints.add(w);
				line = reader.nextLine();
			}
			while (reader.hasNextLine()) {
				line = reader.nextLine();
				String[] elements = line.trim().split(" ");
				Vector newPos = new Vector(Double.valueOf(elements[0]), Double.valueOf(elements[1]));
				Animal b = new Animal(new int[] {0,width,0,height});
				b.setPos(newPos);
				boids.add(b);
			}
			reader.close();
			
			temp.setTerrain(loadedTerrain);
			terrain = new Terrain(temp);
			for(Animal b: boids) {
				b.setAnimalType(animalType);
				b.setTerrain(terrain);
				b.setWaypoints(waypoints);
			}
			SimulationPanel p = new SimulationPanel(width, height);
			
			p.setBackground(terrain.getImage());
			p.setWaypoints(waypoints);
			p.setBoids(boids);
			p.setShowWaypoints(cbxWaypoints.isSelected());
			bv.setPanel(p);
			bv.getPanel().repaint();
		} catch (IOException | NumberFormatException e) {
			JOptionPane.showMessageDialog(bv.getFrame(), "Failed to load file");
		}
	}
	
	/**
	 * Requests file name from user then writes boids previous positions and velocities as well as terrain to text file
	 */
	public void record() {
		 JFileChooser jfc = new JFileChooser("data/recordings");
		 String defaultName = "recording";
		 File f = new File(jfc.getCurrentDirectory().toString()+"\\"+defaultName+".txt");
		 int n = 1;
		 
		 while (f.exists()) {
			 f = new File(jfc.getCurrentDirectory().toString()+"\\"+defaultName+"("+String.valueOf(n)+")"+".txt");
			 n++;
		 }
		 
		 jfc.setSelectedFile(f);
		 
		 int result = jfc.showSaveDialog(null);

         if (result == JFileChooser.APPROVE_OPTION) {
         	f = jfc.getSelectedFile();
         }else {
        	 return;
         }
		
		try {
			FileWriter myWriter = new FileWriter(f);

			myWriter.write(animalType+"\n");
			myWriter.write(boids.size() + "\n");
			for(int i = 0; i < boids.size(); i++) {
				List<Vector> previousPositions = boids.get(i).getPreviousPos();
				List<Vector> preViousVelocity = boids.get(i).getPreviousVel();
				writeToFile(previousPositions, preViousVelocity, myWriter);
			}
			myWriter.write(terrain.toString());
			myWriter.close();

		} catch (IOException e) {
			JOptionPane.showMessageDialog(bv.getFrame(), "Failed to save file");
			e.printStackTrace();
		}
	}
	
	/**
	 * Write all the boid positions to file line by line
	 * @param positions
	 * @param myWriter
	 */
	public void writeToFile(List<Vector> positions, List<Vector> velocity, FileWriter myWriter) {
		try {
			for(int i = recordIndex; i < positions.size(); i++) {
				myWriter.append(positions.get(i).toString() + " " + velocity.get(i).toString() + "|");
			}
			myWriter.append("\n");
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Stops simulation and either enables or disables all elements on main view
	 * @param state state of elements
	 */
	private void setEnabled(boolean state) {
		stopRun();
		
		btnDraw.setEnabled(state);
		btnReset.setEnabled(state);
		btnRewind.setEnabled(state);
		btnStart.setEnabled(state);
		btnStop.setEnabled(state);
		btnFastForward.setEnabled(state);
		btnEdit.setEnabled(state);
		btnUpload.setEnabled(state);
		btnImport.setEnabled(state);
		btnExport.setEnabled(state);
		btnRecord.setEnabled(state);
		chkSteep.setSelected(false);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof Timer) {						//update each boid for each timer tick if simulation is running
			if (running) {
				ticks++;
			}
		}
	}
	
	/**
	 * Updates animal positions
	 */
	private void updateSimulation() {
		//if (running) {
			for(int i = 0; i < boids.size(); i++) {
				if (!boids.get(i).getRewinding()&&rewinding) {
					stopRewind();
				}
				boids.get(i).update(boids);
			}
			SimulationPanel p = bv.getPanel();
			p.setBoids(boids);	
		//}
	}
	
	/**
	 * Repaints GUI
	 */
	private void showSimulation() {
		SimulationPanel p = bv.getPanel();
		p.repaint();
	}

	@Override
	public void run() {
		int frameRate = 500;
	    int skipFrames = 1000 / frameRate;
	    int maxFrameSkips = 5;

	    int nextTick = ticks;
	    int loops;
	    
	    while(true) {
		    while (running) {
		        loops = 0;
		        while(ticks > nextTick && loops < maxFrameSkips) {
		        	int n=0;
		        	while (n<runsPerFrame) {
		        		updateSimulation();
		        		n++;
		        	}
		            
		        	nextTick += skipFrames;
		            loops++;
		        }
	
		        showSimulation();
		    }
		    while(!running) {
		    	loops = 0;
		    }
	    }
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		
	}
}
