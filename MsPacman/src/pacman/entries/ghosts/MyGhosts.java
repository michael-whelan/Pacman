package pacman.entries.ghosts;

import java.util.EnumMap;
import java.util.Random;

import javax.swing.text.Document;

import pacman.controllers.Controller;
import pacman.game.Constants;
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
	public static final int PILL_PROXIMITY=15;
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
	final int centreX = 50;
	final int centreY = 60;
	//static org.w3c.dom.Document doc;
	static FSM fsm;// = new FSM();
	
	/* (non-Javadoc)
	 * @see pacman.controllers.Controller#getMove(pacman.game.Game, long)
	 */
	/*
	 * FSM States
	 * frightened 
	 * scatter Done
	 * chase Done
	 * blocky Done
	 * blockx Done
	 * 
	 * 
	 * Events:
	 * timecomplete Done
	 * powerpill
	 * taken Done
	 * toomany done
	 */
	
	
	public MyGhosts(){
		fsm = new FSM();
		fsm.LoadXML();
	}
	
	public EnumMap<GHOST,MOVE> getMove(Game game,long timeDue)
	{		
		myMoves.clear();
		for(GHOST ghost : GHOST.values()){//for each ghost
			myName = ghost.name().toString();
			String myState = GetCurrentState(myName);
			
			
			//myState = CheckOtherGhosts(game, ghost,myState,myName);
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
				else{ //frightened
					myState = UpdateFrightened(game, ghost, myState);
				}
			}
			SetState(myName,myState);
		}
		//System.out.println("blinky "+blinkyState+" inky "+inkyState+" pinky "+pinkyState+" sue "+sueState);
		//centre  = 50, 60
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
				myState = fsm.StateControl(myState,"taken");
				return myState;
			}
		}else if(myName.equals("INKY")){
			if(!CheckInky(myState)){
				myState = fsm.StateControl(myState,"taken");
			return myState;
			}
		}else if(myName.equals("PINKY")){
			if(	!CheckPinky(myState)){
				myState = fsm.StateControl(myState,"taken");
				return myState;
			}
			return myState;
		}else{
			if(	!CheckSue(myState)){
				myState = fsm.StateControl(myState,"taken");
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
		if(game.getDistance(game.getGhostCurrentNodeIndex(ghost), game.getPacmanCurrentNodeIndex(),DM.MANHATTAN)<=40){
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
			if(System.currentTimeMillis() -chTimer >9000){
				if(myState.equals("chase")){
					myState = fsm.StateControl(myState, "toomany");
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
			myState = fsm.StateControl(myState,"timecomplete");
			scaTimer = 0;
		}
		return myState;
	}
	
	public String PositionVSPacman(String lookingFor,Game game){
		
		if(lookingFor.equals("x")){
			int pacmanX = game.getNodeXCood(game.getPacmanCurrentNodeIndex());
			
			if(pacmanX < centreX){
				return "less";
			}
			else {
				return "more";
			}
		}
		else{
			int pacmanY = game.getNodeYCood(game.getPacmanCurrentNodeIndex());
			
			if(pacmanY < centreY){
				return "less";
			}
			else {
				return "more";
			}
		}
	}
	
	public String UpdateBlockY(Game game, GHOST ghost,String myName, String myState){

		int pacmanX = game.getNodeXCood(game.getPacmanCurrentNodeIndex());
		int ghostX = game.getNodeXCood(game.getGhostCurrentNodeIndex(ghost));
		
		if(PositionVSPacman("y",game).equals("less")){
			if(game.getNodeYCood(game.getGhostCurrentNodeIndex(ghost))>centreY){
				myMoves.put(ghost,moves[0]);//up
			}
		}
		else if(PositionVSPacman("y",game).equals("more")){
			if(game.getNodeYCood(game.getGhostCurrentNodeIndex(ghost))<centreY){
				myMoves.put(ghost,moves[2]);//down
			}
		}
		
		if(ghostX<pacmanX){
			myMoves.put(ghost,moves[1]);//right
		}else if(ghostX>pacmanX){
			myMoves.put(ghost,moves[3]);//left
		}
		if(CheckProximity(game,ghost)){
			myState = fsm.StateControl(myState, "near");
		}
		return myState;
	}
	
	public String UpdateBlockX(Game game, GHOST ghost,String myName, String myState){
		int pacmanY = game.getNodeYCood(game.getPacmanCurrentNodeIndex());
		int ghostY = game.getNodeYCood(game.getGhostCurrentNodeIndex(ghost));
	
		if(PositionVSPacman("x",game).equals("less")){
			if(game.getNodeYCood(game.getGhostCurrentNodeIndex(ghost))>centreX){
				myMoves.put(ghost,moves[3]);//left
			}
		}
		if(PositionVSPacman("x",game).equals("more")){
			if(game.getNodeYCood(game.getGhostCurrentNodeIndex(ghost))<centreX){
				myMoves.put(ghost,moves[1]);//right
			}
		}
		
		if(ghostY<pacmanY){
			myMoves.put(ghost,moves[2]);//down
		}else if(ghostY>pacmanY){
			myMoves.put(ghost,moves[0]);//up
		}
		if(CheckProximity(game,ghost)){
			myState = fsm.StateControl(myState, "near");
		}
		return myState;
	}
	
	public String UpdateFrightened(Game game, GHOST ghost,String myState){
		myMoves.put(ghost,game.getApproximateNextMoveAwayFromTarget(game.getGhostCurrentNodeIndex(ghost),
				game.getPacmanCurrentNodeIndex(),game.getGhostLastMoveMade(ghost),DM.PATH));
		
		
		return myState;
	}
	
	public Boolean CheckDanger(Game game, GHOST ghost){
		if(game.getGhostEdibleTime(ghost)>0 || closeToPower(game)){
			return true;
		}
		return false;
			//myMoves.put(ghost,game.getApproximateNextMoveAwayFromTarget(currentIndex,pacmanIndex,game.getGhostLastMoveMade(ghost),DM.PATH));
	}
	 
	private boolean closeToPower(Game game)
	    {
	    	int pacmanIndex=game.getPacmanCurrentNodeIndex();
	    	int[] powerPillIndices=game.getActivePowerPillsIndices();
	    	
	    	for(int i=0;i<powerPillIndices.length;i++)
	    		if(game.getShortestPathDistance(powerPillIndices[i],pacmanIndex)<PILL_PROXIMITY)
	    			return true;

	        return false;
	    }
}