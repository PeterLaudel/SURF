package app;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import Features.InterestPoint;
import PicPropertys.PicSurf;

public class SurfXMLFile {
	
	String m_path;

	public SurfXMLFile(String path) {
		// TODO Auto-generated constructor stub
		m_path = path;
	}
	public Map<String, InterestPoint> ReadSurfXMLFile()
	{
		
		
		
		return null;
	}
	
	public void WriteXMLFile(PicSurf []surfPics)
	{
		try {
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		 
				// root elements
				Document doc = docBuilder.newDocument();
				Element rootElement = doc.createElement("surf");
				doc.appendChild(rootElement);
			
				for(int i  = 0; i < surfPics.length; i++)
				{
					
			 
					// staff elements
					Element image = doc.createElement("image");
					rootElement.appendChild(image);
			 
					// set attribute to staff element
					Attr attr = doc.createAttribute("name");
					attr.setValue("" + surfPics[i].pic.name);
					image.setAttributeNode(attr);
			 
					// shorten way
					// staff.setAttribute("id", "1");
			 
					// firstname elements
					//Arrays.toString(surfPics[i].)
					List<InterestPoint> interestPoints = surfPics[i].interestPoints;
					for(int j = 0; j < interestPoints.size(); j++)
					{
						InterestPoint ip = interestPoints.get(j);
						
						Element ipElement = doc.createElement("interestPoint");
						ipElement.setAttribute("x", "" + ip.x);
						ipElement.setAttribute("y", "" + ip.y);
						image.appendChild(ipElement);
						
						Element orientation = doc.createElement("orientation");
						orientation.appendChild(doc.createTextNode("" + ip.orientation));
						ipElement.appendChild(orientation);
						
						Element negative = doc.createElement("negative");
						negative.appendChild(doc.createTextNode("" + ip.negative));
						ipElement.appendChild(negative);
						
						Element scale = doc.createElement("scale");
						scale.appendChild(doc.createTextNode("" + ip.scale));
						ipElement.appendChild(scale);
						
						Element value = doc.createElement("value");
						value.appendChild(doc.createTextNode("" + ip.value));
						ipElement.appendChild(value);
						
						Element descriptor = doc.createElement("descriptor");
						String descString = Arrays.toString(ip.descriptor);
						descriptor.appendChild(doc.createTextNode(descString));
						ipElement.appendChild(descriptor);
						
						
					}
				}
				// write the content into xml file
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new File(m_path + "/surf_disc.xml"));
		 
				// Output to console for testing
				// StreamResult result = new StreamResult(System.out);
		 
				transformer.transform(source, result);
		 
				System.out.println("File saved!");
		 
			  } catch (ParserConfigurationException pce) {
				pce.printStackTrace();
			  } catch (TransformerException tfe) {
				tfe.printStackTrace();
			  }
		
		}
	
	
	

}
