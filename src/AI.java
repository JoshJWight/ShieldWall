import java.util.ArrayList;
import java.util.Random;
import java.util.function.BiPredicate;


public class AI {
	private static class Objective{
		public Vector2 velocity;
		public double bearing;
		public Objective()
		{
			velocity = Vector2.zero();
		}
		//with no bearing arg, bearing is set to point in direction of velocity
		public Objective(Vector2 velocity)
		{
			this.velocity = velocity;
			bearing = velocity.angle();
		}
	}
	
	private Random rand;
	
	public AI()
	{
		rand = new Random();
	}
	
	private Guy closestGuy(Guy guy, ArrayList<Guy> others)
	{
		Guy closest = null;
		for(Guy other: others)
		{
			if(other != guy &&
			  (closest == null || guy.dist(other) < guy.dist(closest)))
			{
				closest = other;
			}
		}
		return closest;
	}
	
	private Group closestGroup(Group group, ArrayList<Group> groups, BiPredicate<Group, Group> predicate)
	{
		Group closest = null;
		for(Group other: groups)
		{
			if(other != group && predicate.test(group, other) &&
			  (closest == null || group.center.dist(other.center) < group.center.dist(closest.center)))
			{
				closest = other;
			}
		}
		return closest;
	}
	
	private Group closestAllyGroup(Guy guy, ArrayList<Group> groups)
	{
		Group closest = null;
		for(Group group: groups)
		{
			if(group.factionRgb==guy.factionRgb &&
			  (closest==null || guy.dist(group.center) < guy.dist(closest.center)))
			{
				closest = group;
			}
		}
		return closest;
	}
	
	private Objective random()
	{
		Vector2 desiredPoint = new Vector2(rand.nextDouble(), rand.nextDouble());
		return new Objective(desiredPoint);
	}
	
	private Objective flee(Guy guy, ArrayList<Guy> enemies)
	{
		if(enemies.isEmpty())
		{
			return random();
		}
		
		Objective obj = new Objective();
		for(Guy enemy: enemies)
		{
			Vector2 diff = new Vector2(enemy.p).sub(guy.p);
			
			//Avoid dividing by zero
			double mag = diff.magnitude() + 0.0001;
			obj.velocity.add(diff.mul(1.0/(mag * mag)));
		}
		//face back to the enemies while retreating
		Guy closestEnemy = closestGuy(guy, enemies);
		obj.bearing = new Vector2(closestEnemy.p).sub(guy.p).angle();
		obj.velocity.mul(-1.0);
		return obj;
	}
	
	private Objective attackClosest(Guy guy, ArrayList<Guy> enemies)
	{
		Guy closest = closestGuy(guy, enemies);
		return new Objective(new Vector2(closest.p).sub(guy.p));
	}
	
	private Objective moveToGroup(Guy guy, Group group)
	{
		return new Objective(new Vector2(group.center).sub(guy.p));
	}
	
	private Objective followGroup(Guy guy)
	{
		//TODO this might be where we put repositioning within the group
		return new Objective(new Vector2(guy.group.target).sub(guy.p));
	}
	
	//TODO move the side effects on stamina and HP out of AI
	private void doAttack(Guy guy, ArrayList<Guy> enemies)
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
				
				//TODO feels awkward to have this logic here
				if(guy.group != null && guy.group.isReserves)
				{
					guy.group.isReserves = false;
					guy.group.idleness = 0;
				}
				break;
			}
		}
	}
	
	public void guyAI(Guy guy, ArrayList<Guy> allies, ArrayList<Guy> enemies, ArrayList<Group> groups, int timer)
	{
		//TODO Have guys respect personal space?
		Objective objective;
		
		double safeDist = 5;
		double distToEnemy = safeDist+1;
		if(enemies.size() > 0)
		{
			Guy closestEnemy = closestGuy(guy, enemies);
			distToEnemy = guy.dist(closestEnemy);
		}
		
		if(guy.group != null)
		{
			Group group = guy.group;
			if(guy.stam <= Guy.stamRetreatThreshold && distToEnemy < safeDist)
			{
				objective = flee(guy, enemies);
			}
			else if(guy.dist(group.center) > group.radius)
			{
				objective = moveToGroup(guy, group);
			}
			else if(!enemies.isEmpty())
			{
				Guy closestEnemy = closestGuy(guy, enemies);
				if(guy.dist(closestEnemy) > 5 && closestEnemy.dist(guy.group.center) > guy.group.radius)
				{
					objective = followGroup(guy);
				}
				else
				{
					objective = attackClosest(guy, enemies);
				}
			}
			else
			{
				objective = followGroup(guy);
			}
		}
		else
		{
			if(distToEnemy < safeDist)
			{
				objective = flee(guy, enemies);
			}
			else
			{
				Group closestGroup = closestAllyGroup(guy, groups);
				if(closestGroup == null)
				{
					objective = flee(guy, enemies);
				}
				else
				{
					objective = moveToGroup(guy, closestGroup);
				}
			}
		}
		
		guy.updateBearing(objective.bearing);
		guy.updateVelocity(objective.velocity);
		
		if(timer == guy.initiative && !enemies.isEmpty())
		{
			doAttack(guy, enemies);
		}
	}
	
	public void strafeAI(Guy guy, Guy obstacle)
	{
		guy.v.mul(-1.0);
		
		double magnitude;
		if(obstacle.factionRgb == guy.factionRgb)
		{
			if(obstacle.stam <= Guy.stamRetreatThreshold)
			{
				magnitude = Guy.baseStrafeMagnitude * 3.0;
			}
			else
			{
				magnitude = Guy.baseStrafeMagnitude;
			}
		}
		else
		{
			magnitude = Guy.baseStrafeMagnitude / 2.0;
		}
		
		guy.strafeTimer = Guy.maxStrafeTimer;
		guy.strafeRad = rand.nextBoolean() ? magnitude : - magnitude;
	}
	
	private boolean groupIsOutnumbered(Group group, ArrayList<Group> groups)
	{
		final double enemyToAllyThreshold = 2;
		int allyTotal = 0;
		int enemyTotal = 0;
		//This will hit the group itself
		for(Group other: groups)
		{
			if(group.center.dist(other.center) < Group.distressSearchRadius)
			{
				if(other.factionRgb == group.factionRgb)
				{
					allyTotal += other.guys.size();
				}
				else
				{
					enemyTotal += other.guys.size();
				}
			}
		}
		
		return (double)enemyTotal / (double)allyTotal > enemyToAllyThreshold;
	}
	
	private Group groupAtPoint(Vector2 p, ArrayList<Group> groups)
	{
		for(Group group: groups)
		{
			if(group.center.dist(p) < group.radius)
			{
				return group;
			}
		}
		return null;
	}
	
	public void groupAI(Group group, ArrayList<Group> groups)
	{
		if(group.isReserves)
		{
			group.isDistressed = false;
			
			Group nearestDistressedAlly = closestGroup(group, groups, 
					(Group me, Group other)->(other.isDistressed && other.factionRgb == me.factionRgb));
			if(nearestDistressedAlly != null)
			{
				double bearing = new Vector2(nearestDistressedAlly.center).sub(group.center).angle();
				
				double checkDist = group.disengageRadius + 5;
				double sideAngle = Math.PI/4;
				Vector2 center = new Vector2(group.center).addPolar(checkDist, bearing);
				Vector2 right = new Vector2(group.center).addPolar(checkDist, bearing + sideAngle);
				Vector2 left = new Vector2(group.center).addPolar(checkDist, bearing - sideAngle);
				
				Group centerGroup = groupAtPoint(center, groups);
				Group rightGroup = groupAtPoint(right, groups);
				Group leftGroup = groupAtPoint(left, groups);
				
				if(centerGroup == null || centerGroup == nearestDistressedAlly)
				{
					group.target = new Vector2(nearestDistressedAlly.center);
				}
				else if(rightGroup == null)
				{
					group.target = right;
				}
				else if(leftGroup == null)
				{
					group.target = left;
				}
				//If every path is blocked, just try to barge on through
				else 
				{
					group.target = new Vector2(nearestDistressedAlly.center);
				}
			}
			else
			{
				Group nearestAlly = closestGroup(group, groups,
					(Group me, Group other)->(other.factionRgb == me.factionRgb));
				if(nearestAlly != null && group.center.dist(nearestAlly.center) < group.radius + nearestAlly.radius)
				{
					Vector2 diff = new Vector2(nearestAlly.center).sub(group.center);
					if(!diff.equals(Vector2.zero()))
					{
						//Head in the opposite direction of diff
						double targetBearing = Vector2.recenterBearing(diff.angle() + Math.PI);
						group.target = new Vector2(group.center).addPolar(group.disengageRadius, targetBearing);
					}
				}
				else
				{
					Group nearestActiveAlly = closestGroup(group, groups,
							(Group me, Group other)->(other.factionRgb == me.factionRgb && !other.isReserves));
					if(group.center.dist(nearestActiveAlly.center) > Group.reserveFollowDistance)
					{
						group.target = nearestActiveAlly.center;
					}
					else
					{
						group.target = new Vector2(group.center);
					}
				}
			}
			return;
		}
		else
		{
			group.isDistressed = groupIsOutnumbered(group, groups);
		}
		
		if(group.strafeTimer > 0)
		{
			group.target = new Vector2(group.center).add(Vector2.unit(group.strafeBearing).mul(group.disengageRadius));
			return;
		}
		
		Group closestEnemy = closestGroup(group, groups,
				(Group me, Group other)->(other.factionRgb != me.factionRgb));
		
		if(closestEnemy == null)
		{
			group.target = new Vector2(group.center);
		}
		else
		{
			group.target = new Vector2(closestEnemy.center);
			
			final double maxAngle = Math.PI/8.0;
			final double strafeAmount = Math.PI/2;
			for(Group other: groups)
			{
				if(other!=group && other!=closestEnemy &&
				   group.center.dist(other.center) < group.center.dist(closestEnemy.center)&&
				   group.center.dist(other.center) < group.radius + other.radius &&
				   other.radius > group.radius/2.0) 
				{
					double enemyBearing = group.center.bearingTo(closestEnemy.center);
					double bearingDiff = Vector2.recenterBearing(
							group.center.bearingTo(other.center) - enemyBearing);
					if(Math.abs(bearingDiff) < maxAngle)
					{
						group.strafeTimer = Group.maxStrafeTimer;
						
						//if other is to the +bearing side of closestEnemy, turn to -bearing, and vice versa
						if(bearingDiff > 0)
						{
							group.strafeBearing = Vector2.recenterBearing(enemyBearing - strafeAmount);
						}
						else
						{
							group.strafeBearing = Vector2.recenterBearing(enemyBearing + strafeAmount);
						}
						
						//If the group is spending too much time strafing, make it do reserve duty
						//TODO distinguish between strafing that actually covers difference and strafing that just goes back and forth
						group.idleness += Group.maxStrafeTimer * 2;
						if(group.idleness > Group.idleThreshold)
						{
							group.isReserves = true;
						}
						
						break;
					}
				}
			}
		}
	}
}
