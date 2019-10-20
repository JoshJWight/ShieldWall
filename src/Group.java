import java.util.ArrayList;


public class Group {
	public static final int minGuys = 10;
	public static final double formationRadius = 20;
	public static final double radiusMultiplier = 4.0;
	
	public ArrayList<Guy> guys;
	
	public int rgb;
	public int factionRgb;
	
	//AI
	public Vector2 target;
	public int strafeTimer;
	public double strafeBearing;
	
	public int idleness;
	public static final int idleThreshold = 500;
	boolean isReserves;
	
	public static final int maxStrafeTimer = 40;
	
	
	//These values are computed on update
	Vector2 center;
	double radius;
	double disengageRadius;
	
	public Group(ArrayList<Guy> initialGuys, int rgb)
	{
		this.guys = new ArrayList<Guy>();
		this.rgb = rgb;
		this.factionRgb = initialGuys.get(0).factionRgb;
		for(Guy guy: initialGuys)
		{
			add(guy);
		}
		
		updateCenter();
		updateRadii();
	}
	
	private void updateCenter()
	{
		Vector2 newCenter = Vector2.zero();
		for(Guy guy: guys)
		{
			newCenter.add(guy.p);
		}
		newCenter.mul(1.0/(double)guys.size());
		center = newCenter;
	}
	
	private void updateRadii()
	{
		radius = Math.sqrt(guys.size()) * radiusMultiplier;
		disengageRadius = radius * 2.0;
	}
	
	
	public void update()
	{
		//Note: these calculated values include the guys we're about to remove.
		//But that shouldn't matter in the long run
		updateCenter();
		updateRadii();
		for(int i=0; i<guys.size(); i++)
		{
			Guy guy = guys.get(i);
			if(guy.hp <= 0 || guy.dist(center) > disengageRadius)
			{
				remove(guy);
				i--;
			}
		}
	}
	
	public void add(Guy guy)
	{
		guys.add(guy);
		guy.rgb = rgb;
		guy.group = this;
	}
	
	public void remove(Guy guy)
	{
		guys.remove(guy);
		guy.rgb = guy.factionRgb;
		guy.group = null;
	}
	
	public void removeAll()
	{
		while(guys.size() > 0)
		{
			remove(guys.get(guys.size() - 1));
		}
	}
}
