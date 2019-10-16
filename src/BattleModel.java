import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;


public class BattleModel {
	static final int attackCycle = 30;
	
	public ArrayList<Guy> guys;
	public ArrayList<Group> groups;
	
	private int timer;
	private Random rand;
	private AI ai;
	
	//TODO have parameters governing number of guys, spacing, etc
	public BattleModel(int nGuys)
	{
		guys = new ArrayList<Guy>();
		
		ai = new AI();
		
		rand = new Random();
		
		int prepHeight = 50;
		int prepWidth = 150;
		int noMansLand = 60;
		
		for(int i=0; i<nGuys; i++)
		{
			int rgb = (i%2==0) ? Color.RED.getRGB() : Color.BLUE.getRGB();
			int initiative = rand.nextInt(attackCycle);
			Guy guy = new Guy(rand.nextInt(prepWidth), rand.nextInt(prepHeight) + ((i%2)*(prepHeight + noMansLand)), initiative, rgb);
			guys.add(guy);
			
			guy.rgb = Colors.randomizeColor(guy.rgb);
		}
		
		makeGroups();
	}
	
	private void makeGroups()
	{
		groups = new ArrayList<Group>();
		//HashMap<Guy, Group> guyToGroup = new HashMap<Guy, Group>();
		for(Guy founder: guys)
		{
			if(founder.group == null)
			{
				ArrayList<Guy> members = new ArrayList<Guy>();
				//TODO add magic algorithm
				for(Guy guy: guys)
				{
					//This will also get the founder
					if(guy.group == null&&
					    guy.factionRgb == founder.factionRgb &&
					    guy.dist(founder) < Group.formationRadius)
					{
						members.add(guy);
					}
				}
				Group group = new Group(members, Colors.randomizeColor(founder.rgb));
				groups.add(group);
			}
		}
	}
	
	private void updateMemberships()
	{
		//Put guys into groups they are close enough to
		for(Guy guy: guys)
		{
			if(guy.hp > 0 && guy.group == null)
			{
				for(Group group: groups)
				{
					if(group.factionRgb == guy.factionRgb && guy.dist(group.center) < group.radius)
					{
						group.add(guy);
						break;
					}
				}
			}
		}
	}
	
	private void updateGroups()
	{
		//Update groups
		for(int i=0; i<groups.size(); i++)
		{
			Group group = groups.get(i);
			group.update();
			if(group.guys.size() < Group.minGuys)
			{
				groups.remove(group);
				group.removeAll();
				i--;
			}
		}
	}
	
	private boolean isCollision(Guy guy, Vector2 newPos)
	{
		//TODO update with magic algorithm
		for(Guy other: guys)
		{
			if(other != guy && other.dist(newPos) < Guy.personalSpace && other.dist(guy.p) > Guy.personalSpace)
			{
				return true;
			}
		}
		return false;
	}
	
	private void updateGuy(Guy guy, ArrayList<Guy> allies, ArrayList<Guy> enemies)
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
			ai.guyAI(guy, allies, enemies, groups, timer);
			
			Vector2 newP = new Vector2(guy.p).add(guy.v);
			
			if(!isCollision(guy, newP))
			{
				guy.p = newP;
			}
			else
			{
				guy.v.mul(-1.0);
				guy.strafeTimer = Guy.maxStrafeTimer;
				guy.strafeRad = rand.nextBoolean() ? Guy.strafeMagnitude : - Guy.strafeMagnitude;
			}
		}
	}
	
	private void updateGuys()
	{
		ArrayList<Guy> allies = new ArrayList<Guy>();
		ArrayList<Guy> enemies = new ArrayList<Guy>();
		
		ArrayList<Guy> vanishing = new ArrayList<Guy>();
		
		//Update guys
		//TODO this procedure is getting long, split it into multiple methods
		for(Guy guy: guys)
		{
			allies.clear();
			enemies.clear();
		
			//TODO use magic algorithm to pare this down to only nearest neighbors
			//Preferably in sqrt(n) or log(n) or constant time
			for(Guy other: guys)
			{
				if(other.factionRgb == guy.factionRgb && other != guy)
				{
					allies.add(other);
				}
				else if(other.factionRgb != guy.factionRgb)
				{
					enemies.add(other);
				}
			}
			updateGuy(guy, allies, enemies);
			if(guy.hp<=0 && guy.deathTimer == 0)
			{
				vanishing.add(guy);
			}
		}
		
		for(Guy guy: vanishing)
		{
			guys.remove(guy);
		}
	}
	
	public void update()
	{
		updateMemberships();
		updateGroups();
		updateGuys();
		
		timer = (timer + 1) % attackCycle;
	}
}
