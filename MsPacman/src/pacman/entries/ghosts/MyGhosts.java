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
	private String blinkyState = "chase";
	private String inkyState = "chase";
	private String sueState = "chase";
	private String pinkyState = "chase";
	String myName;
	/* (non-Javadoc)
	 * @see pacman.controllers.Controller#getMove(pacman.game.Game, long)
	 */
	/*
	 * FSM States
	 * frightened
	 * scatter
	 * chase
	 * blocky
	 * blockx
	 * 
	 * 
	 * Events:
	 * timecomplete
	 * powerpill
	 * taken
	 * toomany
	 */
	public String FSM(String currState, String evt){
		 try {	 
				File fXmlFile = new File("C:/Users/Michael/Documents/GitHub/Pacman/MsPacman/staff2.xml");
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				org.w3c.dom.Document doc = dBuilder.parse(fXmlFile);
			 
				//optional, but recommended
				//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
				(doc).getDocumentElement().normalize();
			 
				//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			 
				NodeList nList = doc.getElementsByTagName("states");
			 
				for (int temp = 0; temp < nList.getLength(); temp++) {
			 
					Node nNode = nList.item(temp);
					Element eElement = (Element) nNode;
					//System.out.println("\nCurrent Element :" + nNode.getNodeName());
					if (eElement.getElementsByTagName("currState").item(0).getTextContent().equals(currState) 
							&& eElement.getElementsByTagName("evt").item(0).getTextContent().equals(evt) ) {
						String s = eElement.getElementsByTagName("newState").item(0).getTextContent(); 
					//	System.out.println(s);
						if((s.equals("frightened"))||(s.equals("scatter"))||(s.equals("chase"))||(s.equals("blocky"))||(s.equals("blockx"))){
							 return s;	
						}
						 return currState;
					}
				}
			    } catch (Exception e) {
			    	e.printStackTrace();
			    }
			    return currState;
	}
	
	
	public EnumMap<GHOST,MOVE> getMove(Game game,long timeDue)
	{		
		myMoves.clear();
		
		for(GHOST ghost : GHOST.values()){//for each ghost
			myName = ghost.name().toString();
			String myState = GetCurrentState(myName);
			myState = FSM("chase","toomany");
			System.out.println(myState);
			CheckOtherGhosts(game, ghost,myState,myName);
			if(game.doesGhostRequireAction(ghost))//if it requires an action
			{	if(myState.equals("chase")){
					UpdateChase(game,ghost);
				}
				else if (myState.equals("frightened")){
					UpdateFrightened(game,ghost);
				}
				else if (myState.equals("scatter")){
					UpdateFrightened(game,ghost);
				}
				else if (myState.equals("blockx")){
					UpdateFrightened(game,ghost);
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
		}
		//counter++;
		//System.out.println(myName);
		return myMoves;
	}
	
	public String GetCurrentState(String myName){
		if(myName == "BLINKY"){
			return blinkyState;
		}
		else if(myName == "INKY"){
			return inkyState;
		}
		else if(myName == "PINKY"){
			return pinkyState;
		}
			return sueState;
		
	}
	
	public void CheckOtherGhosts(Game game, GHOST ghost, String myState,String myName){
		if(ghost.name()!=myName){
			//System.out.println("hit");
		}else{
			//System.out.println(myName);
		}
	}
	
	public void UpdateChase(Game game, GHOST ghost){
		
		myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
				game.getPacmanCurrentNodeIndex(),game.getGhostLastMoveMade(ghost),DM.PATH));	
	}
	
	public void UpdateFrightened(Game game, GHOST ghost){
				
		}
	public void UpdateScatter(Game game, GHOST ghost){
		
	}
	public void UpdateBlockY(Game game, GHOST ghost){
		
	}
	public void UpdateBlockX(Game game, GHOST ghost){
		
	}
}