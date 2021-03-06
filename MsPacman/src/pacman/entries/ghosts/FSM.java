package pacman.entries.ghosts;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
/*
 * FSM States
 * frightened Done
 * scatter Done
 * chase Done
 * blocky
 * blockx
 * 
 * 
 * Events:
 * timecomplete
 * powerpill
 * taken
 * toomany done
 */




public class FSM {
	static org.w3c.dom.Document doc;
	
	
	public void LoadXML(){
		System.out.println("FSM.LoadXML()");
		 try {	 //get the file
				File fXmlFile = new File("myData/pacStates.xml");
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				doc = dBuilder.parse(fXmlFile);
		 } catch (Exception e) {
		    	e.printStackTrace();
		    }
	}
	
	public String StateControl(String currState, String evt){

				(doc).getDocumentElement().normalize();
			 
				NodeList nList = doc.getElementsByTagName("states");
			 
				for (int temp = 0; temp < nList.getLength(); temp++) {
			 //run through the nodes, each with a tag states, and run the following checks
					Node nNode = nList.item(temp);
					Element eElement = (Element) nNode;
					if (eElement.getElementsByTagName("currState").item(0).getTextContent().equals(currState) 
							&& eElement.getElementsByTagName("evt").item(0).getTextContent().equals(evt) ) {
						String s = eElement.getElementsByTagName("newState").item(0).getTextContent(); 
						if((s.equals("scatter"))||(s.equals("chase"))||(s.equals("blocky"))||(s.equals("blockx"))){
						//only return if its a valid return, otherwise keep to the same state
							return s;	
						}
						 return currState;
					}
				}
			    return currState;
	}
}
