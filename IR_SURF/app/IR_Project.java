package app;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.MouseInputAdapter;

import PicPropertys.Pic;
import Sorter.Sorter_ColorMean;
import Sorter.Sorter_ColorMean2;
import Sorter.Sorter_SURF;
import Sorter.Sorter_SurfDistance;
import Sorter.Sorter_XMLFile;



class IR_Project implements ActionListener{

	// Einstellbare Parameter:

	private final String startDirectory = "./images/";
	private String path;
	

	//////////////////////////////////////////////////////////////////////////////////////
//	private Sorter_ColorMean sorter_ColorMean;
//	private Sorter_ColorMean2 sorter_ColorMean2;

	private Sorter sorter;
	
	private int frameSizeX = 500; 
	private int frameSizeY = 500; 

	private Pic[] pics;
	
	private JFrame frame;
	private ImageDisplay imageDisplay;

	private int xMouseMove;
	private int yMouseMove;
	private int xMouseStartPos;
	private int yMouseStartPos;

	private double zoomFactor = 1; 

	private MyCanvas myCanvas = new MyCanvas(); 
	private TestAlgorithm myTestAlgorithm;
	private JMenu testMenu;

	private String sortMethod = "ColorMean";

	
	static public void main(String[] args) {
		new IR_Project();
	}

	private IR_Project() 	{
		//Fenstertitel festlegen
		String str = "IR Project";
		

		//Menus erzeugen
		testMenu = new JMenu("Testen");

		JMenuItem mI_all = new JMenuItem("Alle");
		mI_all.addActionListener(this);
		testMenu.add(mI_all);

		JMenu methodMenu = new JMenu("Suchverfahren");

		ButtonGroup buttonGroup = new ButtonGroup();

		JRadioButtonMenuItem mI_colorMean = new JRadioButtonMenuItem("ColorMean",true);
		mI_colorMean.addActionListener(this);
		methodMenu.add(mI_colorMean);
		buttonGroup.add(mI_colorMean);

		JRadioButtonMenuItem mI_colorMean2 = new JRadioButtonMenuItem("ColorMean2",true);
		mI_colorMean2.addActionListener(this);
		methodMenu.add(mI_colorMean2);
		buttonGroup.add(mI_colorMean2);
		
		JRadioButtonMenuItem mI_surf = new JRadioButtonMenuItem("SURF", true);
		mI_surf.addActionListener(this);
		methodMenu.add(mI_surf);
		buttonGroup.add(mI_surf);
		
		JRadioButtonMenuItem mI_surfDistance = new JRadioButtonMenuItem("SurfDistance", true);
		mI_surfDistance.addActionListener(this);
		methodMenu.add(mI_surfDistance);
		buttonGroup.add(mI_surfDistance);
		
		JRadioButtonMenuItem mI_surfFile = new JRadioButtonMenuItem("SurfFile", true);
		mI_surfFile.addActionListener(this);
		methodMenu.add(mI_surfFile);
		buttonGroup.add(mI_surfFile);
		
		JRadioButtonMenuItem mI_test = new JRadioButtonMenuItem("test", true);
		mI_test.addActionListener(this);
		methodMenu.add(mI_test);
		buttonGroup.add(mI_test);


		JMenu settingsMenu = new JMenu("Einstellungen");

		JMenu m_parameter = new JMenu("Parameter");
		settingsMenu.add(m_parameter);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(methodMenu);
		menuBar.add(testMenu);
		menuBar.add(settingsMenu);

		frame = new JFrame(str);
		frame.setJMenuBar(menuBar);

		frame.setSize(frameSizeX, frameSizeY);
		frame.add(myCanvas);

		frame.addWindowListener(new WindowAdapter() 
		{
			public void windowClosing(WindowEvent e) { System.exit(0); }
		});

		frame.setVisible(true);

		myCanvas.loadImages();
		myCanvas.requestFocus();

	}

	//erweitert das testen-menu mit dateinamen aus testset
	private void updateMenu() {
		
		Vector<String> testTypes = new Vector<String>();
		testTypes = myTestAlgorithm.getAllTypes();
		String thisType;
		while(testTypes.size() != 0)
		{
			thisType = testTypes.remove(0);
			if (!thisType.equals("x"))
			{
				JMenuItem mi = new JMenuItem(thisType);
				mi.addActionListener(this);
				testMenu.add(mi);
			}
		}
	}


	public void actionPerformed(ActionEvent event) {		
		
		if (event.getActionCommand() == "ColorMean") {
			sortMethod = "ColorMean";
			sorter = new Sorter_ColorMean(pics);
		}
		else if (event.getActionCommand() == "ColorMean2") {
			sortMethod = "ColorMean2";
			sorter = new Sorter_ColorMean2(pics);
		}
		else if(event.getActionCommand() == "SURF")
		{
			sortMethod = "SURF";
			sorter = new Sorter_SURF(pics, path);
		}
		else if(event.getActionCommand() == "SurfDistance")
		{
			sortMethod = "SurfDistance";
			sorter = new Sorter_SurfDistance(pics, path);
			//sorter = new Sorter_XMLFile(pics, path, "test.xml");
		}
		else if(event.getActionCommand() == "SurfFile")
		{
			sortMethod = "SurfFile";
			sorter = new Sorter_XMLFile(pics, path, "test.dat");
		}
		else if (event.getActionCommand() == "Alle") {
			System.out.println("Alle Testen");
			System.out.println("sortmethod: " + sortMethod);

			Vector<Pic> selectedPics = new Vector<Pic>();
			for (int n = 0; n < pics.length; n++) { 	
				if(pics[n] != null &&(!pics[n].type.equals("x")))
					selectedPics.add(pics[n]);			
			}

			if(selectedPics.size() > 0){
				myTestAlgorithm.test(selectedPics,"all");
				sorter.computeDistance(0, 1);
			}
				
			else 
				System.out.println("Keine Markierungen vorhanden");

			myCanvas.markPixsAsUnselected();
		}
		else if(event.getActionCommand() == "test")
		{
			System.out.println("Testen");
			Vector<Pic> selectedPics = new Vector<Pic>();
			for (int n = 0; n < pics.length; n++) { 	
				if(pics[n] != null &&(!pics[n].type.equals("x")))
					selectedPics.add(pics[n]);			
			}
			System.out.println("All Selected");
			sorter = new Sorter_XMLFile(pics, path, "");
			
		}
		else {
			System.out.println("sortmethod: " + sortMethod);
			System.out.println("Testen nach Dateiname: "+event.getActionCommand());

			myCanvas.markPixsAsUnselected();

			String type = event.getActionCommand();
			Vector<Pic> selectedPics = new Vector<Pic>();
			for (int i = 0; i < pics.length; i++) 
				if(pics[i].type.equals(type))
					selectedPics.add(pics[i]);
			if(selectedPics.size() > 0)
				myTestAlgorithm.test(selectedPics,event.getActionCommand());
			else
				System.out.println("Keine Markierungen vorhanden");
			myCanvas.markPixsAsUnselected();			
		}

		myCanvas.doDrawing();
	}

	//	----------------------------------------------------------------------------------------
	class MyCanvas extends JPanel
	{
		private static final long serialVersionUID = 1L;
		
		private File folder;
		protected int xMousePos;
		protected int yMousePos;
		private int numImages;

		private void loadImages() {
			JFileChooser fc = new JFileChooser(startDirectory);

			// Nur komplette Ordner koennen ausgewaehlt werden
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fc.showOpenDialog(this);
			

			if (returnVal != JFileChooser.APPROVE_OPTION)
				System.exit(-1);
			
			

			// Liest alle Dateien des Ordners und schreibt sie in ein Array
			folder = fc.getSelectedFile();
			path = folder.getPath();
			File[] files = folder.listFiles();

			int expectedNumberOfImages = 0; 

			// Dateinamen werden in einem separaten Array der gleichen Laenge abgelegt
			String[] filenames = new String[files.length];
			for (int i = 0; i < files.length; i++) {
				filenames[i] = files[i].getName().toLowerCase();
				// Beachten, dass evtl. nicht alles Bilder sind
				if (filenames[i].endsWith("jpg") || filenames[i].endsWith("png") || filenames[i].endsWith("gif"))
					expectedNumberOfImages++;
			}

			//array mit pic-objekten fuer jedes bild
			pics = new Pic[expectedNumberOfImages];

			numImages = 0;  // number of images 
			for (int i = 0; i < filenames.length; i++) {
				String path = folder + "/" + filenames[i];

				if (filenames[i].endsWith("jpg") || filenames[i].endsWith("png") || filenames[i].endsWith("gif")) {
					try {
						File file = new File(path); 
						Image image = null;
						try {
							image = ImageIO.read(file);
						} 
						catch (RuntimeException e) {
							e.printStackTrace();
						} 

						if (image != null) {
							int iw = image.getWidth(null);
							int ih = image.getHeight(null);

							int maxOrigImgSize = Math.max(iw,ih);

							float thumbSize = 128;

							//skalierungsfaktor bestimmen:
							float scale = (maxOrigImgSize > thumbSize) ? thumbSize/maxOrigImgSize : 1;

							int nw = (int)(iw*scale);
							int nh = (int)(ih*scale);

							BufferedImage currBi = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_ARGB);
							Graphics2D big = currBi.createGraphics();
							big.drawImage(image,0,0,nw,nh,null);

							Pic currPic = pics[numImages] = new Pic();
							currPic.name = filenames[i];
							currPic.type = filenames[i].split("[_]")[0];
							currPic.bImage=currBi;
							currPic.id = numImages;
							currPic.rank = numImages;
							currPic.origWidth=iw;
							currPic.origHeight=ih;

							//bei x-ten jedem Bild anzeige aktualisieren
							if (numImages % 10 == numImages/10-1)
								doDrawing();

							numImages++;
						}
						else 
							pics[numImages] = null;
					} 
					catch (IOException e) {}
				}
			}

			doDrawing();

			myTestAlgorithm = new TestAlgorithm(myCanvas, myCanvas.folder.getPath(), pics);	

			updateMenu();
			//sorter = new Sorter_ColorMean(pics);

			if (numImages == 0) {
				System.out.println("No images found! Exiting ...");
				System.exit(-1);
			}	
		}

		private void markPixsAsUnselected() {
			for (int i = 0; i < pics.length; i++) {
				if(pics[i] != null)
					pics[i].isSelected = false;
			}
		}

		protected void sortBySimilarity(int id) 
		{
			pics[id].isSelected = true;
			sorter.sortBySimilarity();
			pics[id].isSelected = false;
		}


		////////////////////////////////////////////////////////////////////
		private MyCanvas() {
			imageDisplay = new ImageDisplay(frameSizeX, frameSizeY);	

			addFocusListener(new FocusListener() {

				public void focusGained(FocusEvent e) {
					doDrawing();
				}

				public void focusLost(FocusEvent e) {}

			});

			// Add a listener for resize events
			addComponentListener(new ComponentAdapter() {

				public void componentResized(ComponentEvent evt) {
					JComponent c = (JComponent)evt.getSource();
					// Get new size
					Dimension newSize = c.getSize();
					// Regenerate the image

					frameSizeX = newSize.width;
					frameSizeY = newSize.height;		
					xMouseMove = 0;
					yMouseMove = 0;

					imageDisplay.resize(frameSizeX, frameSizeY);

					doDrawing();
				}
			}
			);


			addMouseListener(new MouseInputAdapter() {
				public void mousePressed (MouseEvent me){
					xMousePos = xMouseStartPos = me.getX();
					yMousePos = yMouseStartPos = me.getY();
				}

				public void mouseClicked(MouseEvent me) {
					xMousePos = me.getX();
					yMousePos = me.getY(); 
					int id = imageDisplay.getImageId(xMousePos, yMousePos);
					if (id != -1 && pics[id] != null) {

						if (me.getButton()==MouseEvent.BUTTON1) { //linke Maustaste
							if (me.getClickCount() == 2) { //Doppelklick
								if(pics[id] != null && (!pics[id].type.equals("x"))) {	
									System.out.println("Testen Bild "+id);
									System.out.println("sortmethod:"+sortMethod);

									Vector<Pic> selectedPics = new Vector<Pic>();

									selectedPics.add(pics[id]);
									myTestAlgorithm.test(selectedPics,"");
									myCanvas.markPixsAsUnselected();
									doDrawing();
								}
							}
							else if (me.getClickCount() == 1) {
								if (id >= 0) {
									if (pics[id] != null) 
										pics[id].isSelected = !pics[id].isSelected;
									doDrawing();
								}
							}
						}
					}

					xMouseMove = 0; 
					yMouseMove = 0;
				}

				public void mouseReleased (MouseEvent me) {
					xMouseMove = 0; 
					yMouseMove = 0;
				}
			});

			addMouseMotionListener(new MouseInputAdapter() {
				public void mouseDragged (MouseEvent me) {
					xMousePos = me.getX();
					yMousePos = me.getY();
					xMouseMove = xMousePos - xMouseStartPos; 
					yMouseMove = yMousePos - yMouseStartPos;
					xMouseStartPos = xMousePos;
					yMouseStartPos = yMousePos;
					doDrawing();
				}
				public void mouseMoved (MouseEvent me) {
					xMousePos = me.getX();
					yMousePos = me.getY();
				}
			});

			MouseWheelListener listener = new MouseWheelListener() {
				public void mouseWheelMoved(MouseWheelEvent e) {
					int count = e.getWheelRotation();

					if (count < 0) {
						zoomFactor = zoomFactor*1.1;
						if (zoomFactor > 50) 
							zoomFactor = 50;
					}
					else {
						zoomFactor = zoomFactor/1.1;	
						if (zoomFactor < 1) zoomFactor = 1;
					}
					doDrawing();
				}
			};

			this.addMouseWheelListener(listener);

			this.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if (e.getKeyChar() == '+' ) 
						zoomFactor *= 1.1;
				
					if (e.getKeyChar() == '-' ) 
						zoomFactor /= 1.1;
				
					// Features anzeigen
					if (e.getKeyChar() == 'f' ) 
						imageDisplay.drawFeature(true);
				
					// Bilder anzeigen
					if (e.getKeyChar() == 'b' ) 
						imageDisplay.drawFeature(false);
				
					if (e.getKeyChar() == 'r' ) {
						for (int i = 0; i < pics.length; i++) {
							if(pics[i] != null)
								pics[i].rank = pics[i].id;
						}
					}

					doDrawing();					
				}
				public void keyReleased(KeyEvent e) {}
			}); 

			this.addFocusListener(new FocusListener() 
			{
				public void focusGained(FocusEvent e) {
					doDrawing();
				}

				public void focusLost(FocusEvent arg0) {}
			});

		}

		@Override
		protected void paintComponent(final Graphics g) {
			super.paintComponent(g);
			imageDisplay.draw(g);
		}

		public void doDrawing() {	
			if (imageDisplay == null)
				return;

			imageDisplay.setPics(pics);
			imageDisplay.calculateDrawingPositions(xMousePos, yMousePos, xMouseMove, yMouseMove, zoomFactor);

			repaint();
		}

		public String getSortMethod() {
			return sortMethod;
		}
	}
}



