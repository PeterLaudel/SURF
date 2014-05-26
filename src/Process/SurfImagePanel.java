package Process;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import Features.InterestPoint;
import Imageprocess.Image;
import IntegralImage.IntegralImage;
import SURF.SurfFeatureDescriptor;
import SURF.SurfFeatureDetector;



public class SurfImagePanel extends JPanel {
	
	
	private static final int maxWidth = 600;
	private static final int maxHeight = 600;
	
	ImageView m_imageView;
	ImageView m_octaveView;
	JPanel m_controlPanel;
	JSlider m_octaveDepthSlider;
	JSlider m_octaveLayerSlider;
	JSlider m_thresholdSlider;
	
	SurfFeatureDetector m_surfDetector;
	Vector<InterestPoint> m_interestPoints;
	
	private JCheckBox m_pointCheckBox;
	private JCheckBox m_directionCheckBox;
	private JCheckBox m_rectCheckBox;
	
	public SurfImagePanel(Image image)
	{
		m_imageView = new ImageView(maxWidth, maxHeight);
		m_imageView.setPixels(image.GetImagePixels(), image.GetWidth(), image.GetHeight());
		
		m_interestPoints = new Vector<InterestPoint>();
		IntegralImage ii = new IntegralImage(image);
		m_surfDetector = new SurfFeatureDetector(2500, 4);
		m_surfDetector.Detect(ii, image.GetWidth(), image.GetHeight(), m_interestPoints);
		SurfFeatureDescriptor sfd = new SurfFeatureDescriptor();
		sfd.Compute(ii, m_interestPoints);
		
		m_octaveView = new ImageView(maxWidth, maxHeight);
		m_controlPanel = new JPanel();
		m_controlPanel.setLayout(new GridLayout(0, 2, 6, 3));
		
		JLabel octaveDepthText = new JLabel("Octave Depth: ");
		JLabel octaveLayerText = new JLabel("Octave Layer: ");
		JLabel thresholdText = new JLabel("Threshold: ");
		
		m_octaveDepthSlider = new JSlider(0, 3);
		m_octaveLayerSlider = new JSlider(0, 3);
		
		Image octaveImage = m_surfDetector.GetOctaveImage(m_octaveDepthSlider.getValue(), m_octaveLayerSlider.getValue());
		m_octaveView.setPixels(octaveImage.GetImagePixels(), octaveImage.GetWidth(), octaveImage.GetHeight());
		
		ChangeListener cl = new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				Image image = m_surfDetector.GetOctaveImage(m_octaveDepthSlider.getValue(), m_octaveLayerSlider.getValue());
				m_octaveView.setPixels(image.GetImagePixels(), image.GetWidth(), image.GetHeight());
				invalidate();
				repaint();
			}
		};
		
		m_octaveDepthSlider.addChangeListener(cl);
		m_octaveLayerSlider.addChangeListener(cl);
		
		m_thresholdSlider = new JSlider(0, 1000);
        m_thresholdSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				// TODO Auto-generated method stub
				ApplyThreshold(m_interestPoints, (float) m_thresholdSlider.getValue() / (float) m_thresholdSlider.getMaximum());
				invalidate();
				repaint();
			}
		});
        
		m_pointCheckBox = new JCheckBox("InterestPoints");
		m_directionCheckBox = new JCheckBox("Direction");
		m_rectCheckBox = new JCheckBox("Descriptor Rect");
		
		ActionListener al = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				ApplyThreshold(m_interestPoints, (float) m_thresholdSlider.getValue() / (float) m_thresholdSlider.getMaximum());
				invalidate();
				repaint();
			}
		};
		
		m_pointCheckBox.addActionListener(al);
		m_directionCheckBox.addActionListener(al);
		m_rectCheckBox.addActionListener(al);
		
		//m_pointCheckBox.setSelected(true);
		m_pointCheckBox.doClick();
        
        
		GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0,6,0,0);
        m_controlPanel.add(octaveDepthText, c);
        m_controlPanel.add(m_octaveDepthSlider, c);
        m_controlPanel.add(octaveLayerText, c);
        m_controlPanel.add(m_octaveLayerSlider, c);
        m_controlPanel.add(thresholdText, c);
        m_controlPanel.add(m_thresholdSlider, c);
        m_controlPanel.add(m_pointCheckBox, c);
        m_controlPanel.add(m_directionCheckBox, c);
        m_controlPanel.add(m_rectCheckBox, c);
        
        setLayout(new BorderLayout());
        
       
        
   
        
        add(m_controlPanel, BorderLayout.NORTH);
        JPanel tmp = new JPanel();
        tmp.add(m_imageView);
        add(tmp, BorderLayout.CENTER);
        
        tmp = new JPanel();
        tmp.add(m_octaveView);
        add(tmp, BorderLayout.SOUTH);
        
        
        invalidate();
        repaint();
		
		
	}
	
	void ApplyThreshold(Vector<InterestPoint> interestPoints, float threshold)
    {
    	m_imageView.ClearShape();
    	for(int i = 0; i <interestPoints.size(); i++)
    	{
    		InterestPoint ip = interestPoints.get(i);
    		if((1.0f - ip.value / m_surfDetector.GetMax()) >= threshold)
    			continue;
    		
    		float size = (ip.scale * 20.0f) * 0.5f;
    		
    		AffineTransform at = new AffineTransform();
    		at.rotate(ip.orientation, ip.x, ip.y);
    		Shape s;
    		if(m_rectCheckBox.isSelected())
    		{
    			s = at.createTransformedShape(new Rectangle((int) (ip.x - size), (int) (ip.y - size), (int) (size*2), (int) (size*2)));
    			m_imageView.AddShape(s);
    		}
    	
    		
    		if(m_directionCheckBox.isSelected())
    		{
    			s = at.createTransformedShape(new Line2D.Float(ip.x, ip.y, ip.x+size, ip.y));
    			m_imageView.AddShape(s);
    		}
    		
    		if( m_pointCheckBox.isSelected())
    		{
    			s = new Ellipse2D.Float((float) ip.x, (float) ip.y, 2.0f, 2.0f);
    			m_imageView.AddShape(s);
    		}
    	}
    }

}
