package Process;
// Copyright (C) 2009 by Klaus Jung
// angepasst von Kai Barthel

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.io.File;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import FeatureMatching.FeatureMatchFilter;
import FeatureMatching.KDTree;
import FeatureMatching.Matches;
import FeatureMatching.Matching;
import Features.InterestPoint;
import Imageprocess.Image;
import Imageprocess.ImageProcess;
import IntegralImage.IntegralImage;
import SURF.SurfFeatureDescriptor;
import SURF.SurfFeatureDetector;

public class Process extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private static final int border = 10;
	private static final int maxWidth = 600;
	private static final int maxHeight = 600;

	
	private static JFrame frame;
	
	private ImageView srcView;			// source image view
	private ImageView octaveView;
	private ImageView dstView;			// scaled image view
	
	private JLabel statusLine;			// to print some status text
	private JTextField parameterInput1;		// to input a scaling factor
	JPanel images = new JPanel(new FlowLayout());
	private double parameter1 = 1;		// initial scaling factor
	
	private JSlider m_octaveDepthSlider;
	private JSlider m_octaveLayerSlider;
	private JSlider m_thresholdSlider;

	private JCheckBox m_pointCheckBox;
	private JCheckBox m_directionCheckBox;
	private JCheckBox m_rectCheckBox;
	
	private SurfFeatureDetector m_surfDetector;
	private SurfFeatureDescriptor m_surfDescriptor;
	private Vector<InterestPoint> m_interestPoints;
	

	public Process() {
        super(new BorderLayout(border, border));
        
        JPanel tmp = new JPanel(new BorderLayout());
        
        // load the default image
        File input = new File("D:\\HTW Berlin\\4. Semester\\IC\\workspace\\SURF\\image003big.jpg");
        
        if(!input.canRead()) input = openFile(); // file not found, choose another image
        
        
        
        srcView = new ImageView(input);
        srcView.setMaxSize(new Dimension(maxWidth, maxHeight));

     // get image dimensions
    	int width = srcView.getImgWidth();
    	int height = srcView.getImgHeight();


    	// get pixels arrays
    	int srcPixels[] = srcView.getPixels();
    	int dstPixels[] = new int[width * height];
    	
		
		doGray(srcPixels, dstPixels, width, height);
		
		Image srcImage = new Image(dstPixels, width, height);
		
		input = new File("D:\\HTW Berlin\\4. Semester\\IC\\workspace\\SURF\\image003_180.jpg");
		srcView = new ImageView(input);
		// get pixels arrays
		
		 width = srcView.getImgWidth();
    	 height = srcView.getImgHeight();
    	srcPixels = srcView.getPixels();
    	dstPixels = new int[width * height];
    	
		
		doGray(srcPixels, dstPixels, width, height);
		Image srcImage2 = new Image(dstPixels, width, height);
		//doTmp(srcImage);
		
        
        SurfImagePanel sip = new SurfImagePanel(srcImage);
        tmp.add(sip, BorderLayout.WEST);
        SurfImagePanel sip2 = new SurfImagePanel(srcImage2);
        tmp.add(sip2, BorderLayout.CENTER);
        
        /*
        KDTree kdTree = new KDTree();

        Vector<Vector<Matches>> knnMatches = kdTree.KnnMatching(sip.GetInterestPoints(), sip2.GetInterestPoints(), 2);
        Vector<Matches> matches = FeatureMatchFilter.DoRatioTest(knnMatches);
        JComponent component = kdTree.DrawMatches(srcImage, sip.GetInterestPoints(), srcImage2, sip2.GetInterestPoints(), matches);
        */
        
        
        //JPanel tmp2 = new JPanel();
        //tmp2.add(component);
        
        MatchImagePanel mip = new MatchImagePanel(sip.GetInterestPoints(), srcImage, sip2.GetInterestPoints(), srcImage2);
        tmp.add(mip, BorderLayout.SOUTH);
        
        
        
        add(tmp);


        
        frame.pack();
        
        /*
        octaveView = new ImageView(input);
        octaveView.setMaxSize(new Dimension(maxWidth, maxHeight));
       
		// create an empty destination image
		dstView = new ImageView(maxWidth, maxHeight);
		
		m_pointCheckBox = new JCheckBox("InterestPoints");
		m_directionCheckBox = new JCheckBox("Direction");
		m_rectCheckBox = new JCheckBox("Descriptor Rect");
		m_interestPoints = new Vector<InterestPoint>();
		
		ActionListener al = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				ApplyThreshold(m_interestPoints, (float) m_thresholdSlider.getValue() / (float) m_thresholdSlider.getMaximum());
				frame.pack();
			}
		};
		
		m_pointCheckBox.addActionListener(al);
		m_directionCheckBox.addActionListener(al);
		m_rectCheckBox.addActionListener(al);
		
		JLabel octaveDepthText = new JLabel("Octave Depth: ");
		JLabel octaveLayerText = new JLabel("Octave Layer: ");
		
		m_octaveDepthSlider = new JSlider(0, 3);
		m_octaveLayerSlider = new JSlider(0, 3);
		
		ChangeListener cl = new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				Image image = m_surfDetector.GetOctaveImage(m_octaveDepthSlider.getValue(), m_octaveLayerSlider.getValue());
				octaveView.setPixels(image.GetImagePixels(), image.GetWidth(), image.GetHeight());
				frame.pack();
			}
		};
		
		m_octaveDepthSlider.addChangeListener(cl);
		m_octaveLayerSlider.addChangeListener(cl);
		
		// load image button
        JButton load = new JButton("Bild Oeffnen");
        load.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		File input = openFile();
        		if(input != null) {
	        		srcView.loadImage(input);
	        		srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
	                processImage(true);
        		}
        	}        	
        });
        
        m_thresholdSlider = new JSlider(0, 1000);
        m_thresholdSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				// TODO Auto-generated method stub
				ApplyThreshold(m_interestPoints, (float) m_thresholdSlider.getValue() / (float) m_thresholdSlider.getMaximum());
				frame.pack();
			}
		});
        
         
        
        // input for scaling factor
        JLabel scaleText = new JLabel("Threshold:");
         
        parameterInput1 = new JTextField(8);
        parameterInput1.setText(String.valueOf(parameter1));
        parameterInput1.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
                processImage(false);
        	}        	
        });
        
        // apply button
        JButton apply = new JButton("Ausfuehren");
        apply.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
                processImage(false);
        	}        	
        });
        
        // some status text
        
        // arrange all controls
        JPanel controls = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0,border,0,0);
        controls.add(load, c);
        controls.add(octaveDepthText);
        controls.add(m_octaveDepthSlider, c);
        controls.add(octaveLayerText, c);
        controls.add(m_octaveLayerSlider, c);
        controls.add(scaleText, c);
        controls.add(m_thresholdSlider, c);
        controls.add(apply, c);
        controls.add(m_pointCheckBox, c);
        controls.add(m_directionCheckBox, c);
        controls.add(m_rectCheckBox, c);
        
        
        images.add(srcView);
        images.add(octaveView);
        images.add(dstView);
        
        add(controls, BorderLayout.NORTH);
        add(images, BorderLayout.CENTER);
        add(statusLine, BorderLayout.SOUTH);
               
        setBorder(BorderFactory.createEmptyBorder(border,border,border,border));
        
        // perform the initial scaling
        processImage(true);
        */
	}
	
	void addImageView(Image image)
	{
		ImageView view = new ImageView(image.GetWidth(), image.GetHeight());
		Image tmpImage = ImageProcess.CastToRGBCopy(ImageProcess.NormalizeImageCopy(image));
		view.setPixels(tmpImage.GetImagePixels());
		images.add(view);
		frame.pack();
	}
	
	private File openFile() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (*.jpg, *.png, *.gif)", "jpg", "png", "gif");
        chooser.setFileFilter(filter);
        int ret = chooser.showOpenDialog(this);
        if(ret == JFileChooser.APPROVE_OPTION) return chooser.getSelectedFile();
        return null;		
	}
    
	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("Computer Vision 1");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JComponent newContentPane = new Process();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        // display the window.
        frame.pack();
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);
	}

	public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}
	
	
    protected void processImage(boolean silent) {

    	
    	try  {
    		parameter1 = Double.parseDouble(parameterInput1.getText());
    	} catch(Exception e) {
    		 JOptionPane.showMessageDialog(this, "Bitte geben Sie eine Zahl ein.", "Eingabefehler", JOptionPane.ERROR_MESSAGE);
    		 return;
    	}
    	
        
    	// get image dimensions
    	int width = srcView.getImgWidth();
    	int height = srcView.getImgHeight();


    	// get pixels arrays
    	int srcPixels[] = srcView.getPixels();
    	int dstPixels[] = new int[width * height];
    	
    	String message = "\"" + "SURF" + "\"";

    	statusLine.setText(message);

		long startTime = System.currentTimeMillis();
		
		doGray(srcPixels, dstPixels, width, height);
		
		Image srcImage = new Image(dstPixels, width, height);
		doTmp(srcImage);
		
    	

		long time = System.currentTimeMillis() - startTime;
		   	
        
        
        frame.pack();
        
    	statusLine.setText(message + " in " + time + " ms");
    }
    
    void doCopy(int srcPixels[], int dstPixels[], int width, int height) {
    	// loop over all pixels of the destination image

    	for (int y = 0; y < height; y++) {

    		for (int x = 0; x < width; x++) {

    			int pos	= y * width + x;

    			dstPixels[pos] = srcPixels[pos];

    		}
    	}
    }
    
    void doGray(int srcPixels[], int dstPixels[], int width, int height) {
		// loop over all pixels of the destination image
		
		for (int y = 0; y < height; y++) {
			
			for (int x = 0; x < width; x++) {
				
					int pos	= y * width + x;
				
					int c = srcPixels[pos];
					int r = (c>>16)&0xFF;
					int g = (c>> 8)&0xFF;
					int b = (c    )&0xFF;
					
					int lum = (int) (0.299*r + 0.587*g + 0.114*b + parameter1);
					lum = Math.min(lum,255);
					dstPixels[pos] = 0xFF000000 | (lum<<16) | (lum<<8) | lum;
				
			}
		}
    }
    
    int[] doTmp(Image image)
    {
    	/*
    	int[] dstPixel = new int[image.GetHeight() * image.GetWidth()];
    	System.arraycopy(image.GetImagePixels(), 0, dstPixel, 0, dstPixel.length);
		IntegralImage integralImage = new IntegralImage(image);
		BoxFilter yyFilter = BoxFilter.GetSURFyyFilter(9);
		BoxFilter xxFilter = BoxFilter.GetSURFxxFilter(9);
		BoxFilter xyFilter = BoxFilter.GetSURFxyFilter(9);
		
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		
		for (int y = 0; y < image.GetHeight(); y++) {
			
			for (int x = 0; x < image.GetWidth(); x++) {
				int pos	= y * image.GetWidth() + x;
				
				int Dxx = (int) (integralImage.ApplyBoxFilter(xxFilter, x, y));
				int Dyy = (int) (integralImage.ApplyBoxFilter(yyFilter, x, y));
				int Dxy = (int) (integralImage.ApplyBoxFilter(xyFilter, x, y));
				
				int det =(int) (Dxx*Dyy - Math.pow(Dxy*0.9, 2));
				det = Math.abs(det);
				max = Math.max(det, max);
				min = Math.min(det, min);
				dstPixel[pos] = det;
			}
		}
		max = Math.abs(max);
		for (int y = 0; y < image.GetHeight(); y++) {
			
			for (int x = 0; x < image.GetWidth(); x++) {
				int pos	= y * image.GetWidth() + x;
				int value = (int) ((dstPixel[pos] -min) / (float) max * 255.0);
					
				dstPixel[pos] = 0xFF000000 | (value<<16) | (value<<8) | value;
			}
		}
		*/
    	
    	
    	
    	dstView.setPixels(image.GetImagePixels(), image.GetWidth(), image.GetHeight());
    	m_surfDetector = new SurfFeatureDetector(2500, 4);
    	m_surfDescriptor = new SurfFeatureDescriptor();
    	
    	IntegralImage ii = new IntegralImage(image);
    	
    	
    	File input = new File("D:\\HTW Berlin\\4. Semester\\IC\\workspace\\SURF\\image003rotate.jpg");
        if(!input.canRead()) input = openFile(); // file not found, choose another image
        ImageView tmpView= new ImageView(input);
        
        Image tmpImage = new Image(tmpView.getPixels(), tmpView.getImgWidth(), tmpView.getImgHeight());
    	IntegralImage tmpIi = new IntegralImage(tmpImage);
    	
    	Vector<InterestPoint> tmpIp = new Vector<InterestPoint>();
    	m_surfDetector.Detect(tmpIi, tmpImage.GetWidth(), tmpImage.GetHeight(), tmpIp);
    	m_surfDescriptor.Compute(tmpIi, tmpIp);
    	
    	m_surfDetector.Detect(ii, image.GetWidth(), image.GetHeight(), m_interestPoints);
    	m_surfDescriptor.Compute(ii, m_interestPoints);
    	
    	Matching matching = new Matching();
    	//Vector<Matches> matches = new Vector<Matches>();
    	//matching.Match(tmpIp, m_interestPoints, matches);
    	
    	//images.add(matching.DrawMatches(tmpImage, tmpIp, image, m_interestPoints, matches));
    	
    	KDTree kdtree = new KDTree();
    	
    	Vector<Vector<Matches>> knnMatches= kdtree.KnnMatching(tmpIp, m_interestPoints, 2);
    	Vector<Matches> matches = FeatureMatchFilter.DoRatioTest(knnMatches);
    	images.add(kdtree.DrawMatches(tmpImage, tmpIp, image, m_interestPoints, matches));
    	
    	//dstView.setPixels(result.GetImagePixels(), result.GetWidth(), result.GetHeight());
    	
    	//ApplyThreshold(m_surf.GetInterestPoints(), 1.0f);
    	/*
    	BufferedImage bi2 = new BufferedImage(image.GetWidth(), image.GetHeight(), BufferedImage.TYPE_INT_ARGB);
		bi2.setRGB(0, 0, image.GetWidth(), image.GetHeight(), image.GetImagePixels(), 0, image.GetWidth());
		Graphics2D g2d = bi2.createGraphics();
		AffineTransform at = g2d.getTransform();
		at.setToRotation(Math.toRadians(45), 50, 50);
		AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		Rectangle2D rect2D = op.getBounds2D(bi2);
		
		BufferedImage bi = new BufferedImage((int) Math.floor(rect2D.getWidth()), (int) Math.floor(rect2D.getHeight()), bi2.getType());
		op.filter(bi2, bi);
		bi = bi.getSubimage(-5, -5, 60, 60);
		int[] pixels = new int[bi.getWidth() *  bi.getHeight()];
		bi.getRGB(0, 0, bi.getWidth(),  bi.getHeight(), pixels, 0, bi.getWidth());
		dstView.setPixels( pixels, bi.getWidth(), bi.getHeight());
		*/
    	//graphics.dispose();
    	
    	//SymmetrizationImage simage = new SymmetrizationImage(image.GetImagePixels(), image.GetWidth(), image.GetHeight(), 230);
    	//dstView.setPixels(simage.GetImagePixels(), simage.GetWidth(), simage.GetHeight());
    	
    	//addImageView(surf.GetOctaveImage(2, 2));
    	return image.GetImagePixels();
    }
    /*
    void ApplyThreshold(Vector<InterestPoint> interestPoints, float threshold)
    {
    	dstView.ClearShape();
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
    			dstView.AddShape(s);
    		}
    	
    		
    		if(m_directionCheckBox.isSelected())
    		{
    			s = at.createTransformedShape(new Line2D.Float(ip.x, ip.y, ip.x+size, ip.y));
    			dstView.AddShape(s);
    		}
    		
    		if( m_pointCheckBox.isSelected())
    		{
    			s = new Ellipse2D.Float((float) ip.x, (float) ip.y, 2.0f, 2.0f);
    			dstView.AddShape(s);
    		}
    	}
    }
    */
    

}
    
