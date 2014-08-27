package Process;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class SurfValuesDialog extends JPanel{
	
	/**
	 * 
	 */
	ImageValuesPanel ivp1;
	ImageValuesPanel ivp2;
	
	public SurfValuesDialog(String name1, String name2)
	{
		
		JPanel ivpPanel = new JPanel();
		ivpPanel.setLayout(new BorderLayout());
		ivp1 = new ImageValuesPanel(name1);
		ivp2 = new ImageValuesPanel(name2);
		
		ivpPanel.add(ivp1, BorderLayout.WEST);
		ivpPanel.add(ivp2, BorderLayout.EAST);
		
		add(ivpPanel);
		int option = JOptionPane.showConfirmDialog(null, this, "Client Dialog", JOptionPane.DEFAULT_OPTION);
		//this.pack();

	}
	
	int GetOctaveDepthImage1()
	{
		return ivp1.GetOctaveDepth();
	}
	
	int GetOctaveDepthImage2()
	{
		return ivp2.GetOctaveDepth();
	}
	
	int GetLayerDepthImage1()
	{
		return ivp1.GetLayerDepth();
	}
	
	int GetLayerDepthImage2()
	{
		return ivp2.GetLayerDepth();
	}
	
	int GetNumberImage1()
	{
		return ivp1.GetNumber();
	}
	
	int GetNumberImage2()
	{
		return ivp2.GetNumber();
	}
	
	public class ImageValuesPanel extends JPanel
	{
		JComboBox<Integer> octaveComboBox = new JComboBox<Integer>();
		JComboBox<Integer> layerComboBox = new JComboBox<Integer>();
		SpinnerModel sm = new SpinnerNumberModel(200,0,10000,1);
		JSpinner spinner = new JSpinner(sm);
		
		public ImageValuesPanel(String name)
		{
			JPanel controlPanel = new JPanel();
			
			
			for(int i = 3; i < 5; i++)
			{
				octaveComboBox.addItem(i);
				layerComboBox.addItem(i);
			}
			
			
			JLabel octaveLabel = new JLabel("Octave Depth: ");
			JLabel layerLabel = new JLabel("Layer Depth: ");
			JLabel numberLabel = new JLabel("Number IP: ");
			
			controlPanel.setLayout(new GridLayout(0, 2, 6, 3));
			
			GridBagConstraints c = new GridBagConstraints();
	        c.insets = new Insets(0,1,0,0);
	        controlPanel.add(new JLabel(name), c);
	        controlPanel.add(new JLabel(), c);
	        controlPanel.add(octaveLabel, c);
	        controlPanel.add(octaveComboBox, c);
	        controlPanel.add(layerLabel, c);
	        controlPanel.add(layerComboBox, c);
	        controlPanel.add(numberLabel, c);
	        controlPanel.add(spinner, c);
	        add(controlPanel);
		}
		
		public int GetOctaveDepth()
		{
			return (int) octaveComboBox.getSelectedItem(); 
		}
		
		public int GetLayerDepth()
		{
			return (int) layerComboBox.getSelectedItem();
		}
		
		public int GetNumber()
		{
			return (int) spinner.getValue();
		}
		
		
	}

}
