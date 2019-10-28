import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;


public class BattleModel {
	static final int attackCycle = 30;
	private static final double sightRadius = 20;
	
	public ArrayList<Guy> guys;
	public ArrayList<Group> groups;
	
	private ArrayList<Guy> xIndex;
	private ArrayList<Guy> yIndex;
	
	private Guy.XSort xSort;
	private Guy.YSort ySort;
	
	private int timer;
	private Random rand;
	private AI ai;
	
	//TODO have parameters governing number of guys, spacing, etc
	public BattleModel(int nGuys, int nFactions)
	{
		
		guys = new ArrayList<Guy>();
		xIndex = new ArrayList<Guy>();
		yIndex = new ArrayList<Guy>();
		xSort = new Guy.XSort();
		ySort = new Guy.YSort();
		
		ai = new AI();
		
		rand = new Random();
		
		double prepHeight = Math.sqrt(nGuys) * 10.0 / nFactions;
		double prepWidth = Math.sqrt(nGuys) * 20.0 / nFactions;
		double noMansLand = 0;
		
		Color colors[] = {Color.RED, Color.BLUE, Color.GREEN, Color.LIGHT_GRAY, Color.YELLOW, Color.PINK, Color.CYAN};
		//////////
		double wedgeAngle = 2.0 * Math.PI/(double)nFactions;
		double distToCorner = 0.5 * prepWidth / Math.sin(Math.PI / (double) nFactions);
		
		for(int i=0; i<nFactions; i++)
		{
			//Add 45 degrees so the two faction case works well with the x and y indices
			double cornerAngle = i * wedgeAngle + Math.PI/4.0;
			
			//Corner that's used as the coordinate base offset from the one that determines the rotation angle
			Vector2 corner = Vector2.zero().addPolar(distToCorner, (cornerAngle + wedgeAngle));
			
			System.out.println(i + ": cornerAngle " + cornerAngle + ", distToCorner " + distToCorner + 
					", corner (" +  corner.x + ", " + corner.y + ")");
			
			corner.addPolar(noMansLand, cornerAngle);
			
			int rgb;
			if(i < colors.length)
			{
				rgb = colors[i].getRGB();
			}
			else {
				rgb = rand.nextInt();
			}
			
			for(int j=0; j<nGuys/nFactions; j++)
			{
				int initiative = rand.nextInt(attackCycle);
				double baseX = rand.nextDouble() * prepWidth;
				double baseY = rand.nextDouble() * prepHeight;
				Vector2 pos = new Vector2(baseX, baseY).rotate(cornerAngle).add(corner);
				
				Guy guy = new Guy(pos.x, pos.y, initiative, rgb);
				guys.add(guy);
				xIndex.add(guy);
				yIndex.add(guy);
				
				guy.rgb = Colors.randomizeColor(guy.rgb); 
			}
		}
		updateIndices();
		
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
				members.add(founder);
				for(Guy guy: guysWithin(founder, Group.formationRadius))
				{
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
			ai.groupAI(group, groups);
			
			//Groups that are too small cease to exist
			boolean remove = (group.guys.size() < Group.minGuys);
			
			//Reserves will pop themselves to join distressed groups
			if(group.isReserves)
			{
				for(Group other: groups)
				{
					if(other.isDistressed && other.factionRgb == group.factionRgb && 
						group.center.dist(other.center) < Group.distressSearchRadius * 1.5)
					{
						System.out.println("Reserves popping for rescue");
						remove = true;
						break;
					}
				}
			}
			
			if(remove)
			{
				groups.remove(group);
				group.removeAll();
				i--;
			}
		}
	}
	
	private boolean isCollision(Guy guy, Vector2 newPos)
	{
		for(Guy other: guysWithin(guy, Guy.personalSpace * 2.0))
		{
			if(other != guy && other.dist(newPos) < Guy.personalSpace && other.dist(guy.p) > Guy.personalSpace)
			{
				return true;
			}
		}
		return false;
	}
	
	private void updateTimers()
	{
		for(Guy guy: guys)
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
				guy.strafeTimer--;
			}
			
			if(guy.group != null && guy.dist(guy.group.center) > guy.group.radius)
			{
				guy.awolTimer++;
			}
			else
			{
				guy.awolTimer = 0;
			}
		}
		for(Group group: groups)
		{
			if(group.strafeTimer > 0)
			{
				group.strafeTimer--;
			}
			if(group.idleness > 0)
			{
				group.idleness --;
			}
			
			if(group.flashTimer > 0)
			{
				group.flashTimer --;
			}
			else if(group.isDistressed || group.isReserves)
			{
				group.flashTimer = Group.maxFlashTimer;
			}
		}
	}
	
	private void updateGuyPos(Guy guy)
	{
		if(guy.hp > 0)
		{
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
		
		for(Guy guy: guys)
		{
			if(guy.hp<=0 && guy.deathTimer == 0)
			{
				vanishing.add(guy);
			}
		}
		for(Guy guy: guys)
		{
			if(guy.hp > 0)
			{
				allies.clear();
				enemies.clear();
			
				for(Guy other: guysWithin(guy, sightRadius))
				{
					if(other.factionRgb == guy.factionRgb)
					{
						allies.add(other);
					}
					else if(other.factionRgb != guy.factionRgb)
					{
						enemies.add(other);
					}
				}
				ai.guyAI(guy, allies, enemies, groups, timer);
			}
			
		}
		for(Guy guy: guys)
		{
			if(guy.hp > 0)
			{
				updateGuyPos(guy);
			}
		}
		
		for(Guy guy: vanishing)
		{
			guys.remove(guy);
			xIndex.remove(guy);
			yIndex.remove(guy);
		}
		updateIndices();
	}
	
	public void update()
	{
		updateTimers();
		updateMemberships();
		updateGroups();
		updateGuys();
		
		timer = (timer + 1) % attackCycle;
	}
	
	private void updateIndices()
	{
		Collections.sort(xIndex, xSort);
		Collections.sort(yIndex, ySort);
		for(int i=0; i< guys.size(); i++)
		{
			xIndex.get(i).xIndex = i;
			yIndex.get(i).yIndex = i;
		}
	}
	
	ArrayList<Guy> guysWithin(Guy guy, double radius)
	{
		//Note: worst case performance here is if all guys are in a narrow horizontal or vertical line.
		//Therefore the optimal battle line is diagonal
		//TODO any way to compensate?
		
		//Also wow this is so un-DRY, any way to improve that?
		ArrayList<Guy> neighbors = new ArrayList<Guy>();
		for(int i=guy.xIndex+1; i<guys.size(); i++)
		{
			Guy other = xIndex.get(i);
			double xDist = Math.abs(other.p.x - guy.p.x);
			double yDist = Math.abs(other.p.y - guy.p.y);
			//First part to avoid double-counting guys
			if(xDist > yDist && guy.dist(other) < radius)
			{
				neighbors.add(other);
			}
			else if(xDist > radius)
			{
				break;
			}
		}
		for(int i=guy.xIndex-1; i>0; i--)
		{
			Guy other = xIndex.get(i);
			double xDist = Math.abs(other.p.x - guy.p.x);
			double yDist = Math.abs(other.p.y - guy.p.y);
			if(xDist > yDist && guy.dist(other) < radius)
			{
				neighbors.add(other);
			}
			else if(xDist > radius)
			{
				break;
			}
		}
		for(int i=guy.yIndex+1; i<guys.size(); i++)
		{
			Guy other = yIndex.get(i);
			double xDist = Math.abs(other.p.x - guy.p.x);
			double yDist = Math.abs(other.p.y - guy.p.y);
			//First part to avoid double-counting guys
			if(yDist > xDist && guy.dist(other) < radius)
			{
				neighbors.add(other);
			}
			else if(yDist > radius)
			{
				break;
			}
		}
		for(int i=guy.yIndex-1; i>0; i--)
		{
			Guy other = yIndex.get(i);
			double xDist = Math.abs(other.p.x - guy.p.x);
			double yDist = Math.abs(other.p.y - guy.p.y);
			if(yDist > xDist && guy.dist(other) < radius)
			{
				neighbors.add(other);
			}
			else if(yDist > radius)
			{
				break;
			}
		}
		
		return neighbors;
	}
}
