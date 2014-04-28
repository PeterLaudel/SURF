// Copyright (C) 2009 by Klaus Jung
// angepasst von Kai Barthel

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.List;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Process extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private static final int border = 10;
	private static final int maxWidth = 600;
	private static final int maxHeight = 600;

	
	private static JFrame frame;
	
	private ImageView srcView;			// source image view
	private ImageView dstView;			// scaled image view
	
	private JComboBox methodList;		// the selected scaling method
	private JLabel statusLine;			// to print some status text
	private JTextField parameterInput1;		// to input a scaling factor
	JPanel images = new JPanel(new FlowLayout());
	private double parameter1 = 1;		// initial scaling factor

	public Process() {
        super(new BorderLayout(border, border));
        
        // load the default image
        File input = new File("D:\\HTW Berlin\\4. Semester\\IC\\workspace\\SURF\\test4.png");
        
        if(!input.canRead()) input = openFile(); // file not found, choose another image
        
        srcView = new ImageView(input);
        srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
       
		// create an empty destination image
		dstView = new ImageView(maxWidth, maxHeight);
		
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
         
        // selector for the method
        JLabel methodText = new JLabel("Methode:");
        String[] methodNames = {"Kopie", "Graustufen"};
        
        methodList = new JComboBox(methodNames);
        methodList.setSelectedIndex(0);		// set initial method
        methodList.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
                processImage(false);
        	}
        });
        
        // input for scaling factor
        JLabel scaleText = new JLabel("Parameter:");
         
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
        statusLine = new JLabel(" ");
        
        // arrange all controls
        JPanel controls = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0,border,0,0);
        controls.add(load, c);
        controls.add(methodText, c);
        controls.add(methodList, c);
        controls.add(scaleText, c);
        controls.add(parameterInput1, c);
        controls.add(apply, c);
        
        
        images.add(srcView);
        images.add(dstView);
        
        add(controls, BorderLayout.NORTH);
        add(images, BorderLayout.CENTER);
        add(statusLine, BorderLayout.SOUTH);
               
        setBorder(BorderFactory.createEmptyBorder(border,border,border,border));
        
        // perform the initial scaling
        processImage(true);
	}
	
	void addImageView(Image image)
	{
		ImageView view = new ImageView(image.GetWidth(), image.GetHeight());
		Image tmpImage = ImageProcess.CastToRGB(image);
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
  
        String methodName = (String)methodList.getSelectedItem();
    	
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
    	
    	String message = "\"" + methodName + "\"";

    	statusLine.setText(message);

		long startTime = System.currentTimeMillis();
		
		//int[] tmpPixels = new int[width * height];
    	switch(methodList.getSelectedIndex()) {
    	case 0:	// Kopie
    		doCopy(srcPixels, dstPixels, width, height);
    		break;
    	case 1:	// Graustufen
    		doGray(srcPixels, dstPixels, width, height);
    		
    		Image srcImage = new Image(dstPixels, width, height);
    		dstPixels = doTmp(srcImage);
    		break;
    	}

		long time = System.currentTimeMillis() - startTime;
		   	
        dstView.setPixels(dstPixels, width, height);
        
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
    	IntegralImage integralImage = new IntegralImage(image);
    	Octave octave = new Octave(2);
    	Image[] images = octave.GetOctave(integralImage);
    	ArrayList<Integer> indices = findMaximum(images);
    	int[] dstPixel = new int[image.GetHeight() * image.GetWidth()];
    	System.arraycopy(image.GetImagePixels(), 0, dstPixel, 0, dstPixel.length);
    	
    	for(int i = 0; i <indices.size(); i++)
    		dstPixel[indices.get(i)] =  0xFF000000 | (255<<16) | (0<<8) | 0;
    	
    	for(int i = 0; i < images.length; i++)
    		addImageView(images[i]);
    	
		return dstPixel;
    }
    
    ArrayList<Integer> findMaximum(Image[] octave)
    {
    	ArrayList<Integer> list = new ArrayList<Integer>();
    	int[] octavePixels = octave[1].GetImagePixels();
    	for (int y = 1; y < octave[1].GetHeight() - 1; y++) {
			for (int x = 1; x < octave[1].GetWidth() - 1; x++) {
				int pos	= y * octave[1].GetWidth() + x;
				int[] neighborhood = new int[] {1, 0 ,-1};
				
				boolean found = true;
				outerloop:
				for(int i = 0; i< neighborhood.length; i++)
					for(int j = 0; j <neighborhood.length; j++)
						for(int k = 0; k < neighborhood.length; k++)
						{
							int[] tmpOctavePixels = octave[1-neighborhood[k]].GetImagePixels();
							System.out.println("i: " + i + " j: " + j + " k: " + k);
							int tmpPos = (y - neighborhood[i]) * octave[1].GetWidth() + (x - neighborhood[j]);
							if(tmpOctavePixels[tmpPos] >=  octavePixels[pos] && (tmpPos != pos ))
							{
								found = false;
								break outerloop;
							}
						}
				
				if(found)
					list.add(pos);
					
			}
			
    	}
    	return list;
    }
}
    
