package pacman.entries.ghosts;

import java.util.EnumMap;
import java.util.Random;

import javax.swing.text.Document;

import pacman.controllers.Controller;
import pacman.controllers.KeyBoardInput;
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
	private String blinkyState = "chase";//initial setting of the states.
	private String inkyState = "blockx";
	private String sueState = "blocky";
	private String pinkyState = "chase";
	private boolean startTime;//dictates how long two ghosts can be in chase at once
	private long chTimer;
	private long scaTimer;
	String myName;
	final int centreX = 50;
	final int centreY = 60;
	final int chaseTime = 5000;
	final int scatterTime = 1000;
	public static FSM fsm;// = new FSM();
	static KeyBoardInput input;
	
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
	
	
	public MyGhosts(KeyBoardInput input){
		fsm = new FSM();
		fsm.LoadXML();//load the fsm at startup
		MyGhosts.input = input;
	}
	
    public static KeyBoardInput getKeyboardInput()//for reload of the FSM during play
    {
    	return input;
    }

	public EnumMap<GHOST,MOVE> getMove(Game game,long timeDue)
	{		
		myMoves.clear();
		for(GHOST ghost : GHOST.values()){//for each ghost
			myName = ghost.name().toString();//get current ghosts name
			String myState = GetCurrentState(myName);//get currents ghost state depending on name.
			
			myState = CheckOtherGhosts(game, ghost,myState,myName);//check if your state is already taken
		
			if(game.doesGhostRequireAction(ghost))//if it requires an action
			{//checks the current state of the ghost and calls a different function depending on the state
				if(myState.equals("chase")){
					myState = UpdateChase(game,ghost,myName,myState);
				}
				else if (myState.equals("blocky")){
					myState = UpdateBlockY(game,ghost,myName,myState);
				}
				else if (myState.equals("scatter")){
					myState = UpdateScatter(game,ghost,myName,myState);
				}
				else {//if (myState.equals("blockx")){
					myState = UpdateBlockX(game,ghost,myName,myState);
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
				myState = fsm.StateControl(myState,"taken");//state changes dictated by the fsm and current event
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
	//check group of ghosts for seeing if they have the same states>>start
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
	//>>end check group
	//check the distance between current ghost and pacman
	public Boolean CheckProximity(Game game, GHOST ghost){
		if(game.getDistance(game.getGhostCurrentNodeIndex(ghost), game.getPacmanCurrentNodeIndex(),DM.EUCLID)<=40){
			return true;
		}
		return false;
	}
	//The different updates depending on states >>start
	public String UpdateChase(Game game, GHOST ghost,String myName, String myState){
		if(myName.equals("BLINKY")){
			myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
				game.getPacmanCurrentNodeIndex(),game.getGhostLastMoveMade(ghost),DM.MANHATTAN));
		}
		else{
			myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
					game.getPacmanCurrentNodeIndex(),game.getGhostLastMoveMade(ghost),DM.EUCLID));
		}
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
			if(System.currentTimeMillis() -chTimer >chaseTime){
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
		if(System.currentTimeMillis()- scaTimer>scatterTime){
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
	//these block functions are used to match the different ghosts either X or Y
	public String UpdateBlockY(Game game, GHOST ghost,String myName, String myState){

		int pacmanX = game.getNodeXCood(game.getPacmanCurrentNodeIndex());
		int ghostX = game.getNodeXCood(game.getGhostCurrentNodeIndex(ghost));
		
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
    //>>end update group
}