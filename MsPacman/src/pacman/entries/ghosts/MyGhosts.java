package pacman.entries.ghosts;

import java.util.EnumMap;
import java.util.Random;

import javax.swing.text.Document;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

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
		
	/* (non-Javadoc)
	 * @see pacman.controllers.Controller#getMove(pacman.game.Game, long)
	 */
	public EnumMap<GHOST,MOVE> getMove(Game game,long timeDue)
	{		
		myMoves.clear();
		
		for(GHOST ghost : GHOST.values())				//for each ghost
			if(game.doesGhostRequireAction(ghost))		//if it requires an action
			{
				
				if(game.getManhattanDistance(game.getGhostCurrentNodeIndex(ghost), game.getPacmanCurrentNodeIndex())< 50){
					myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
							game.getPacmanCurrentNodeIndex(),game.getGhostLastMoveMade(ghost),DM.PATH));
					System.out.println("hit");		
				}
				else{
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
				}
				
				
/*				if(rnd.nextFloat()<CONSISTENCY)	//approach/retreat from the current node that Ms Pac-Man is at
					myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
							game.getPacmanCurrentNodeIndex(),game.getGhostLastMoveMade(ghost),DM.PATH));
				else									//else take a random action
					myMoves.put(ghost,moves[rnd.nextInt(moves.length)]);*/
			}

		return myMoves;
	}
}