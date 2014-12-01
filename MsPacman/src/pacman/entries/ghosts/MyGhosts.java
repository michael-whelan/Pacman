package pacman.entries.ghosts;

import java.util.EnumMap;
import java.util.Random;

import javax.swing.text.Document;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.internal.Ghost;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getActions() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., game.entries.ghosts.mypackage).
 */
public class MyGhosts extends Controller<EnumMap<GHOST,MOVE>>
{
	private final static float CONSISTENCY=1.0f;	//carry out intended move with this probability
	private Random rnd=new Random();
	private EnumMap<GHOST,MOVE> myMoves=new EnumMap<GHOST,MOVE>(GHOST.class);
	private MOVE[] moves=MOVE.values();
	private String blinkyState = "blocky";
	private String inkyState = "blocky";
	private String sueState = "blocky";
	private String pinkyState = "blocky";
	private boolean startTime;//dictates how long two ghosts can be in chase at once
	private long chTimer;
	private long scaTimer;
	String myName;
	static org.w3c.dom.Document doc;
	/* (non-Javadoc)
	 * @see pacman.controllers.Controller#getMove(pacman.game.Game, long)
	 */
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
	public static void LoadXML(){
		 try {	 
				File fXmlFile = new File("C:/Users/Michael/Documents/GitHub/Pacman/MsPacman/staff2.xml");
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				doc = dBuilder.parse(fXmlFile);
		 } catch (Exception e) {
		    	e.printStackTrace();
		    }
	}
	
	public String FSM(String currState, String evt){
		// try {	 
			//	File fXmlFile = new File("C:/Users/Michael/Documents/GitHub/Pacman/MsPacman/staff2.xml");
			//	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			//	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			//	org.w3c.dom.Document doc = dBuilder.parse(fXmlFile);
			 
				//optional, but recommended
				//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
				(doc).getDocumentElement().normalize();
			 
				NodeList nList = doc.getElementsByTagName("states");
			 
				for (int temp = 0; temp < nList.getLength(); temp++) {
			 
					Node nNode = nList.item(temp);
					Element eElement = (Element) nNode;
					if (eElement.getElementsByTagName("currState").item(0).getTextContent().equals(currState) 
							&& eElement.getElementsByTagName("evt").item(0).getTextContent().equals(evt) ) {
						String s = eElement.getElementsByTagName("newState").item(0).getTextContent(); 
					//	if((s.equals("frightened"))||(s.equals("scatter"))||(s.equals("chase"))||(s.equals("blocky"))||(s.equals("blockx"))){
							 return s;	
						//}
						// return currState;
					}
				}
			  //  } catch (Exception e) {
			   // 	e.printStackTrace();
			  //  }
			    return currState;
	}
	
	public MyGhosts(){
		LoadXML();
	}
	
	public EnumMap<GHOST,MOVE> getMove(Game game,long timeDue)
	{		
		myMoves.clear();
		for(GHOST ghost : GHOST.values()){//for each ghost
			myName = ghost.name().toString();
			String myState = GetCurrentState(myName);
			
			//myState = FSM(myState,"toomany");
			myState = CheckOtherGhosts(game, ghost,myState,myName);
			myState = CheckOtherGhosts(game, ghost,myState,myName);
			if(game.doesGhostRequireAction(ghost))//if it requires an action
			{	if(myState.equals("chase")){
					myState = UpdateChase(game,ghost,myName,myState);
				}
				else if (myState.equals("blocky")){
					myState = UpdateBlockY(game,ghost,myName,myState);
				}
				else if (myState.equals("scatter")){
					myState = UpdateScatter(game,ghost,myName,myState);
				}
				else if (myState.equals("blockx")){
					myState = UpdateBlockX(game,ghost,myName,myState);
				}
				else {//if (myState.equals("blocky")){
					UpdateFrightened(game,ghost);
				}
			
					/*
					int currentNodeIndex=game.getGhostCurrentNodeIndex(ghost);	
					int[] activePills=game.getActivePillsIndices();
					int[] activePowerPills=game.getActivePowerPillsIndices();
					int[] targetNodeIndices=new int[activePills.length+activePowerPills.length];
					
					for(int i=0;i<activePills.length;i++)
						targetNodeIndices[i]=activePills[i];
					
					for(int i=0;i<activePowerPills.length;i++)
						targetNodeIndices[activePills.length+i]=activePowerPills[i];
					
					int nearest = game.getClosestNodeIndexFromNodeIndex(game.getPacmanCurrentNodeIndex(),targetNodeIndices,DM.PATH);
					myMoves.put(ghost, game.getNextMoveTowardsTarget(currentNodeIndex,nearest,DM.PATH));//
					*/
				
/*				if(rnd.nextFloat()<CONSISTENCY)	//approach/retreat from the current node that Ms Pac-Man is at
					myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
							game.getPacmanCurrentNodeIndex(),game.getGhostLastMoveMade(ghost),DM.PATH));
				else									//else take a random action
					myMoves.put(ghost,moves[rnd.nextInt(moves.length)]);*/
			}
			SetState(myName,myState);
		}
		//System.out.println("blinky "+blinkyState+" inky "+inkyState+" pinky "+pinkyState+" sue "+sueState);
		
		return myMoves;
	}
	
	public String GetCurrentState(String myName){
		if(myName.equals("BLINKY")){
			return blinkyState;
		}
		else if(myName.equals("INKY")){
			return inkyState;
		}
		else if(myName.equals("PINKY")){
			return pinkyState;
		}
			return sueState;	
	}
	
	public void SetState(String myName, String myState){
		if(myName.equals("BLINKY")){
			blinkyState = myState;
		}
		else if(myName.equals("INKY")){
			inkyState = myState;
		}
		else if(myName.equals("PINKY")){
			pinkyState = myState;
		}
			sueState = myState;
		
	}
	
	public String CheckOtherGhosts(Game game, GHOST ghost, String myState,String myName){
		if(myName.equals("BLINKY")){//if my name is blinky
			if(!CheckBlinky(myState)){//if someone else has the same state
				if(myState.equals("blocky")){//if theis is my state
					String temp= "blockx";
					return temp;
					//CheckOtherGhosts(game,ghost,temp,myName);//recheck and see if another state is free
				}
				else if(myState.equals("blockx")){
					String temp = "scatter";
					return temp;
				}
				return myState;
			}
			return myState;
		}else if(myName.equals("INKY")){
			if(!CheckInky(myState)){
				if(myState.equals("blocky")){
					String temp= "blockx";
					return temp;
				}
				else if(myState.equals("blockx")){
					String temp= "scatter";
					return temp;
				}
				return myState;
			}
			return myState;
			
		}else if(myName.equals("PINKY")){
			if(	!CheckPinky(myState)){
				if(myState.equals("blocky")){
					String temp = "blockx";
					return temp;
				}
				else if(myState.equals("blockx")){
					String temp = "scatter";
					return temp;
				}
				return myState;
			}
			return myState;
		}else{
			if(	!CheckSue(myState)){
				if(myState.equals("blocky")){
					String temp = "blockx";
					return temp;
				}
				else if(myState.equals("blockx")){
					String temp = "scatter";
					return temp;
				}
				return myState;
			}
			
		}
		return myState;
	}
	
	public Boolean CheckInky(String myState){
		if(blinkyState.equals( myState)||pinkyState.equals( myState)||sueState.equals( myState)){
			return false;
		}
		return true;
		}
	
	public Boolean CheckBlinky(String myState){
		if(inkyState.equals( myState)||pinkyState.equals( myState)||sueState.equals( myState)){
			return false;
		}
		return true;
	}
	
	public Boolean CheckPinky(String myState){
		if(blinkyState.equals( myState)||inkyState.equals( myState)||sueState.equals( myState)){
			return false;
		}
		return true;
	}
	
	public Boolean CheckSue(String myState){
		if(blinkyState.equals( myState)||inkyState.equals( myState)||pinkyState.equals( myState)){
			return false;
		}
		return true;
	}
	
	public Boolean CheckProximity(Game game, GHOST ghost){
		if(game.getApproximateShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), game.getPacmanCurrentNodeIndex(),
				game.getGhostLastMoveMade(ghost))<=40){
			return true;
		}
		return false;
	}
	
	public String UpdateChase(Game game, GHOST ghost,String myName, String myState){
		myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
				game.getPacmanCurrentNodeIndex(),game.getGhostLastMoveMade(ghost),DM.PATH));
		int counter= 0;
		
		if(blinkyState.equals("chase")){
			counter++;
		}
		if(inkyState.equals("chase")){
			counter++;
		}
		if(pinkyState.equals("chase")){
			counter++;
		}
		if(sueState.equals("chase")){
			counter++;
		}
		if(counter>1 && !startTime){
			startTime= true;
			chTimer = System.currentTimeMillis();
		}
		if(startTime){
			if(System.currentTimeMillis() -chTimer >8000){
				if(myState.equals("chase")){
					myState = FSM(myState, "toomany");
				}
				chTimer = 0;
				startTime =false;
			}
		}
		return myState;
	}
	
	public String UpdateScatter(Game game, GHOST ghost,String myName, String myState){
		myMoves.put(ghost,moves[rnd.nextInt(moves.length)]);
		scaTimer = System.currentTimeMillis();
		if(System.currentTimeMillis()- scaTimer>5000){
			myState = FSM(myState,"timecomplete");
			scaTimer = 0;
		}
		return myState;
	}
	
	public String UpdateBlockY(Game game, GHOST ghost,String myName, String myState){
		int pacmanY = game.getNodeYCood(game.getPacmanCurrentNodeIndex());
		int ghostY = game.getNodeYCood(game.getGhostCurrentNodeIndex(ghost));
		if(ghostY<pacmanY){
			myMoves.put(ghost,moves[2]);
		}else if(ghostY>pacmanY){
			myMoves.put(ghost,moves[0]);
		}
		if(CheckProximity(game,ghost)){
			myState = FSM(myState, "near");
		}
		return myState;
	}
	
	public String UpdateBlockX(Game game, GHOST ghost,String myName, String myState){
		int pacmanX = game.getNodeXCood(game.getPacmanCurrentNodeIndex());
		int ghostX = game.getNodeXCood(game.getGhostCurrentNodeIndex(ghost));
		if(ghostX<pacmanX){
			myMoves.put(ghost,moves[1]);
		}else if(ghostX>pacmanX){
			myMoves.put(ghost,moves[3]);
		}
		if(CheckProximity(game,ghost)){
			myState = FSM(myState, "near");
		}
		return myState;
	}
	
	public void UpdateFrightened(Game game, GHOST ghost){
		
	}
}