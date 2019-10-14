import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.awt.Color;



public class Main {
	static final int attackCycle = 30;
	static final int frameTimeMs = 20;
	//TODO move most of this stuff out of Main
	public static void main(String[] args) {
		ArrayList<Guy> list = new ArrayList<Guy>();
		
		BattleDisplay display = new BattleDisplay(list);
		AI ai = new AI();
		
		Random rand = new Random();
		
		int nGuys = 200;
		
		int prepHeight = 50;
		int prepWidth = 100;
		int noMansLand = 60;
		
		for(int i=0; i<nGuys; i++)
		{
			int rgb = (i%2==0) ? Color.RED.getRGB() : Color.BLUE.getRGB();
			int initiative = rand.nextInt(attackCycle);
			Guy guy = new Guy(rand.nextInt(prepWidth), rand.nextInt(prepHeight) + ((i%2)*(prepHeight + noMansLand)), initiative, rgb);
			list.add(guy);
			
			guy.rgb = randomizeColor(guy.rgb);
		}
		
		ArrayList<Group> groups = makeGroups(list);
		
		int timer = 0;
		
		while(true)
		{
			long startTime = System.currentTimeMillis();
			ArrayList<Guy> allies = new ArrayList<Guy>();
			ArrayList<Guy> enemies = new ArrayList<Guy>();
			
			ArrayList<Guy> vanishing = new ArrayList<Guy>();
			
			//Put guys into groups they are close enough to
			for(Guy guy: list)
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
			//Update guys
			//TODO this procedure is getting long, split it into multiple methods
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
						if(other.factionRgb == guy.factionRgb && other != guy)
						{
							allies.add(other);
						}
						else if(other.factionRgb != guy.factionRgb)
						{
							enemies.add(other);
						}
					}
					ai.guyAI(guy, allies, enemies, groups, timer);
					
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
	
	
	public static int randomizeColor(int rgb)
	{
		Color c = new Color(rgb);
		
		int r = randomizeColorSegment(c.getRed());
		int g = randomizeColorSegment(c.getGreen());
		int b = randomizeColorSegment(c.getBlue());
		
		return new Color(r, g, b).getRGB();
	}
	
	public static int randomizeColorSegment(int color)
	{
		//Todo replace with class member
		Random rand = new Random();
		double maxAdjust = 0.4;
		int maxColor = 255;
		
		double adjust = rand.nextDouble() * maxAdjust;
		
		//Increase
		if(rand.nextBoolean())
		{
			color = (int)(color + (maxColor * adjust));
			color = Math.min(color, maxColor);
		}
		//Decrease
		else
		{
			color = (int)(color - (maxColor * adjust));
			color = Math.max(color, 0);
		}
		
		return color;
	}
	
	public static ArrayList<Group> makeGroups(ArrayList<Guy> guys)
	{
		ArrayList<Group> groups = new ArrayList<Group>();
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
				Group group = new Group(members, randomizeColor(founder.rgb));
				groups.add(group);
			}
		}
		return groups;
	}
}
