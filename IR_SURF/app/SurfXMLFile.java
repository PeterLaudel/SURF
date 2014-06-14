package app;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import Features.InterestPoint;
import PicPropertys.PicSurf;

public class SurfXMLFile {
	
	String m_path;

	public SurfXMLFile(String path) {
		// TODO Auto-generated constructor stub
		m_path = path;
	}
	public Map<String, List<InterestPoint>> ReadSurfXMLFile()
	{
		try {
			 
				File fXmlFile = new File(m_path + "/surfDescription.xml");
				if(!fXmlFile.exists())
					return null;
				
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(fXmlFile);
			 
				//optional, but recommended
				//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
				doc.getDocumentElement().normalize();
			 
				//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			 
				NodeList nList = doc.getElementsByTagName("image");
			 
				System.out.println("----------------------------");
				
				Map<String, List<InterestPoint>> resultMap = new HashMap<String, List<InterestPoint>>(nList.getLength());
			 
				for (int temp = 0; temp < nList.getLength(); temp++) {
			 
					Node nNode = nList.item(temp);
			 
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			 
						Element eElement = (Element) nNode;
			 
						String name = eElement.getAttribute("name");
						
						List<InterestPoint> ipList = new Vector<InterestPoint>();
						
						NodeList ipNodeList = eElement.getElementsByTagName("interestPoint");
						for(int i = 0; i < ipNodeList.getLength(); i++)
						{
							Element ipElement = (Element) ipNodeList.item(i);
							int x = Integer.parseInt(ipElement.getAttribute("x"));
							int y = Integer.parseInt(ipElement.getAttribute("y"));
							
							Boolean negative = Boolean.parseBoolean(ipElement.getElementsByTagName("negative").item(0).getTextContent());
							float scale = Float.parseFloat(ipElement.getElementsByTagName("scale").item(0).getTextContent());
							float value = Float.parseFloat(ipElement.getElementsByTagName("value").item(0).getTextContent());
							
							InterestPoint ip = new InterestPoint(x, y, scale, value, negative);
							ip.orientation = Float.parseFloat(ipElement.getElementsByTagName("orientation").item(0).getTextContent());
							ip.descriptor = fromString(ipElement.getElementsByTagName("descriptor").item(0).getTextContent());
							ipList.add(ip);
						}
						resultMap.put(name, ipList);
					}
				}
				return resultMap;
		    } catch (Exception e) {
		    	e.printStackTrace();
		    }
		
		return null;
		
	}
	
	private float[] fromString(String string)
	{
	    String[] strings = string.replace("[", "").replace("]", "").split(", ");
	    float result[] = new float[strings.length];
	    for (int i = 0; i < result.length; i++) {
	      result[i] = Float.parseFloat(strings[i]);
	    }
	    return result;
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
				StreamResult result = new StreamResult(new File(m_path + "/surfDescription.xml"));
		 
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
