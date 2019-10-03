import java.util.ArrayList;
import java.util.Random;

import java.awt.Color;


public class Main {

	//TODO move most of this stuff out of Main
	public static void main(String[] args) {
		ArrayList<Guy> list = new ArrayList<Guy>();
		
		BattleDisplay display = new BattleDisplay(list, -50, -50, 250, 150);
		
		Random rand = new Random();
		
		for(int i=0; i<100; i++)
		{
			Guy guy = new Guy(rand.nextInt(120), rand.nextInt(120), rand.nextInt());
			guy.rgb = (rand.nextFloat() > 0.5f) ? Color.RED.getRGB() : Color.BLUE.getRGB();
			list.add(guy);
		}
		
		while(true)
		{
			
			ArrayList<Guy> allies = new ArrayList<Guy>();
			ArrayList<Guy> enemies = new ArrayList<Guy>();
			
			for(Guy guy: list)
			{
				allies.clear();
				enemies.clear();
				
				//TODO use magic algorithm to pare this down to only nearest neighbors
				//Preferably in sqrt(n) or log(n) or constant time
				for(Guy other: list)
				{
					if(other.rgb == guy.rgb && other != guy)
					{
						allies.add(other);
					}
					else if(other.rgb != guy.rgb)
					{
						enemies.add(other);
					}
				}
				guyAI(guy, allies, enemies);
				
				guy.x += guy.xv;
				guy.y += guy.yv;
				System.out.println("guy at " + guy.x + ", " + guy.y + " with speed " + guy.xv + ", " + guy.yv);
				//TODO collision detection
			}
			display.repaint();
			
			try {
				//TODO only sleep remainder of frame time
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	//TODO move out into separate class
	public static void guyAI(Guy guy, ArrayList<Guy> allies, ArrayList<Guy> enemies)
	{
		//TODO Have guys respect personal space?
		
		float desiredX = 0;
		float desiredY = 0;
		//TODO use a class member here
		Random rand = new Random();
		
		//Move at random if no one is visible (does this happen much?)
		if(allies.isEmpty() && enemies.isEmpty())
		{
			desiredX = rand.nextFloat();
			desiredY = rand.nextFloat();
		}
		//Run away from enemies if no allies are visible
		else if(allies.isEmpty())
		{
			float xThreat = 0;
			float yThreat = 0;
			for(Guy enemy: enemies)
			{
				float xDiff = enemy.x - guy.x;
				float yDiff = enemy.y - guy.y;
				
				//TODO make closer enemies more threatening instead of vice versa
				//Without dividing by zero
				xThreat += (xDiff);
				yThreat += (yDiff);
			}
			desiredX = - xThreat;
			desiredY = - yThreat;
		}
		else
		{
			ArrayList<Guy> nearbyAllies = new ArrayList<Guy>();
			float maxDistance = 3;
			for(Guy ally: allies)
			{
				if(guy.dist(ally) < maxDistance)
				{
					nearbyAllies.add(ally);
				}
			}
			int minNearby = 3;
			//Move toward allies if not enough of them are close
			
			if(nearbyAllies.size() < minNearby)
			{
				//TODO not DRY - make a function that does this
				float xThreat = 0;
				float yThreat = 0;
				for(Guy ally: allies)
				{
					float xDiff = ally.x - guy.x;
					float yDiff = ally.y - guy.y;
					
					//TODO prefer closer allies to farther ones
					
					xThreat += (xDiff);
					yThreat += (yDiff);
				}
				desiredX = xThreat;
				desiredY = yThreat;
			}
			//Match velocities of nearby allies if no enemies are visible
			else if(enemies.isEmpty())
			{
				for(Guy ally: nearbyAllies)
				{
					desiredX += ally.xv;
					desiredY += ally.yv;
				}
			}
			//Move toward closest enemy if enough allies are close
			else
			{
				Guy closest = enemies.get(0);
				for(Guy enemy: enemies)
				{
					if(guy.dist(enemy) < guy.dist(closest))
					{
						closest = enemy;
					}
				}
				desiredX = closest.x - guy.x;
				desiredY = closest.y - guy.y;
			}
		}
		
		guy.updateVelocity(desiredX, desiredY);
	}

}
