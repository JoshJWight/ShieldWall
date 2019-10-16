import java.util.ArrayList;
import java.util.Random;


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
		public Objective(Vector2 velocity, double bearing)
		{
			this.velocity = velocity;
			this.bearing = bearing;
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
		return new Objective(Vector2.unit(guy.group.bearing));
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
			if(guy.stam <= guy.maxStam / 5 && distToEnemy < safeDist)
			{
				objective = flee(guy, enemies);
			}
			else if(guy.dist(group.center) > group.radius)
			{
				objective = moveToGroup(guy, group);
			}
			else if(!enemies.isEmpty())
			{
				objective = attackClosest(guy, enemies);
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
}
