import java.util.ArrayList;
import java.util.Random;

import java.awt.Color;



public class Main {
	static final int attackCycle = 30;
	static final int frameTimeMs = 20;
	//TODO move most of this stuff out of Main
	public static void main(String[] args) {
		ArrayList<Guy> list = new ArrayList<Guy>();
		
		BattleDisplay display = new BattleDisplay(list);
		
		Random rand = new Random();
		
		int nGuys = 1000;
		
		int prepHeight = 200;
		int prepWidth = 200;
		int noMansLand = 60;
		
		for(int i=0; i<nGuys; i++)
		{
			int rgb = (i%2==0) ? Color.RED.getRGB() : Color.BLUE.getRGB();
			int initiative = rand.nextInt(attackCycle);
			Guy guy = new Guy(rand.nextInt(prepWidth), rand.nextInt(prepHeight) + ((i%2)*(prepHeight + noMansLand)), initiative, rgb);
			list.add(guy);
		}
		
		int timer = 0;
		
		while(true)
		{
			long startTime = System.currentTimeMillis();
			ArrayList<Guy> allies = new ArrayList<Guy>();
			ArrayList<Guy> enemies = new ArrayList<Guy>();
			
			ArrayList<Guy> vanishing = new ArrayList<Guy>();
			
			for(Guy guy: list)
			{
				if(guy.attackTimer > 0)
				{
					guy.attackTimer--;
				}
				if(guy.deathTimer > 0)
				{
					guy.deathTimer--;
				}
				if(guy.stamRegenTimer > 0)
				{
					guy.stamRegenTimer--;
					if(guy.stamRegenTimer ==0)
					{
						guy.stam++;
						if(guy.stam < Guy.maxStam)
						{
							guy.stamRegenTimer = Guy.maxStamRegenTimer;
						}
					}
				}
				if(guy.strafeTimer > 0)
				{
					guy.strafeTimer --;
				}
				
				if(guy.hp > 0)
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
					guyAI(guy, allies, enemies, timer);
					
					Vector2 newP = new Vector2(guy.p).add(guy.v);
					boolean collide = false;
					for(Guy other: list)
					{
						if(other != guy && other.dist(newP) < Guy.personalSpace && other.dist(guy.p) > Guy.personalSpace)
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
						guy.strafeTimer = Guy.maxStrafeTimer;
						guy.strafeRad = rand.nextBoolean() ? Guy.strafeMagnitude : - guy.strafeMagnitude;
					}
				}
				else if(guy.hp<=0 && guy.deathTimer == 0)
				{
					vanishing.add(guy);
				}
			}
			
			for(Guy guy: vanishing)
			{
				list.remove(guy);
			}
			
			display.repaint();
			
			long endTime = System.currentTimeMillis();
			
			//System.out.println("Frame processing time: " + (endTime - startTime));
			
			try {
				Thread.sleep(Math.max((20 - (endTime - startTime)), 0));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			timer = (timer + 1) % attackCycle;
		}
	}
	//TODO move out into separate class
	public static void guyAI(Guy guy, ArrayList<Guy> allies, ArrayList<Guy> enemies, int timer)
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
			double maxDistance = 10;
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
		
		//Attack
		if(timer == guy.initiative && !enemies.isEmpty())
		{
			Vector2 targetPoint = new Vector2(guy.p).addPolar(Guy.attackReach, guy.bearingRad);
			for(Guy enemy: enemies)
			{
				if(enemy.hp > 0 && enemy.dist(targetPoint) < Guy.radius)
				{
					guy.attackTimer = Guy.maxAttackTimer;
					if(enemy.shieldedFrom(guy.p))
					{
						enemy.stam --;
					}
					else
					{
						enemy.hp --;
						if(enemy.hp == 0)
						{
							enemy.deathTimer = Guy.maxDeathTimer;
						}
					}
					if(enemy.stam < Guy.maxStam)
					{
						enemy.stamRegenTimer = Guy.maxStamRegenTimer;
					}
				}
			}
		}
	}

}
