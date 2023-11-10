package view;

import javax.swing.*;

import presenter.*;

public class BoidView {
	private int height = 800;								//frame height
	private int width = 1200;									//frame width
	private JFrame frame = new JFrame("Field");				//instantiate frame
	private SimulationPanel panel = new SimulationPanel((int)(height*0.8), (int)(height*0.8));	//instantiate panel to arbitrary size for now
	
	public void initialize() {
		frame.setLayout(null);
		frame.setBounds(100, 0, width, height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		//set frame parameters
		
		frame.add(panel);
	}

	
	public JFrame getFrame() {								//frame getter
		return frame;
	}
	
	public void setFrame(JFrame f) {						//frame setter
		frame = f;
		frame.setBounds(0,0,width,height);
		frame.setVisible(true);
	}
	
	public SimulationPanel getPanel() {						//panel getter
		return panel;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void setPanel(SimulationPanel p) {				//panel setter
		frame.remove(panel);
		//frame.setVisible(false);
		panel = p;
		frame.add(panel);
		frame.repaint();
	}
}
