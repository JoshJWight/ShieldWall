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
			guy.rgb = (rand.nextDouble() > 0.5f) ? Color.RED.getRGB() : Color.BLUE.getRGB();
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
				
				Vector2 newP = new Vector2(guy.p).add(guy.v);
				boolean collide = false;
				for(Guy other: list)
				{
					if(other != guy && other.dist(newP) < Guy.personalSpace)
					{
						collide = true;
					}
				}
				if(!collide)
				{
					guy.p = newP;
				}
				else
				{
					guy.v.mul(-1.0);
				}
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
		Vector2 desired;
		double desiredBearing;
		
		//TODO use a class member here
		Random rand = new Random();
		
		//Move at random if no one is visible (does this happen much?)
		if(allies.isEmpty() && enemies.isEmpty())
		{
			desired = new Vector2(rand.nextDouble(), rand.nextDouble());
			desiredBearing = rand.nextDouble() * Math.PI;
		}
		//Run away from enemies if no allies are visible
		else if(allies.isEmpty())
		{
			Vector2 threat = Vector2.zero();
			for(Guy enemy: enemies)
			{
				Vector2 diff = new Vector2(enemy.p).sub(guy.p);
				
				//TODO make closer enemies more threatening instead of vice versa
				//Without dividing by zero
				threat.add(diff);
			}
			//face back to the enemies while retreating
			desiredBearing = Math.atan2(threat.y, threat.x);
			desired = threat.mul(-1.0);
		}
		else
		{
			ArrayList<Guy> nearbyAllies = new ArrayList<Guy>();
			double maxDistance = 3;
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
				Vector2 charm = Vector2.zero();
				for(Guy ally: allies)
				{
					//TODO prefer closer allies to farther ones
					charm.add(ally.p).sub(guy.p);
				}
				desiredBearing = Math.atan2(charm.y, charm.x);
				desired = charm;
			}
			//Match velocities of nearby allies if no enemies are visible
			else if(enemies.isEmpty())
			{
				desired = Vector2.zero();
				for(Guy ally: nearbyAllies)
				{
					desired.add(ally.v);
				}
				desiredBearing = Math.atan2(desired.y, desired.x);
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
				desired = new Vector2(closest.p).sub(guy.p);
				desiredBearing = Math.atan2(desired.y, desired.x);
			}
		}
		guy.updateBearing(desiredBearing);
		guy.updateVelocity(desired);
	}

}
