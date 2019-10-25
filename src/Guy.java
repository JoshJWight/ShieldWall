import java.util.Comparator;


public class Guy {
	
	public Vector2 p;
	public Vector2 v;
	
	public double bearingRad;
	
	public int hp;
	public static final int maxHp = 4;
	
	public int stam;
	public static final int maxStam = 10;
	
	public static final double radius = 1;
	public static final double hpRadius = 0.5;
	
	public static final double maxSpeed = 0.2f;
	public static final double acceleration = 0.02f;
	public static final double rotationSpeed = 0.02f;
	
	public static final double personalSpace = 2.5;
	
	public static final double attackReach = 2.5;
	
	public int rgb;
	public int factionRgb;
	
	public int initiative;
	public int attackTimer;
	public static final int maxAttackTimer = 10;
	public int deathTimer;
	public static final int maxDeathTimer = 50;
	public int stamRegenTimer;
	public static final int maxStamRegenTimer = 50;
	public double strafeRad;
	public int strafeTimer;
	public static final int maxStrafeTimer = 10;
	public static final double strafeMagnitude = Math.PI/8;
	
	public static final double halfShieldRad = Math.PI/3;
	
	public int awolTimer;
	public static final int maxAwolTimer = 300;
	
	public Group group;
	
	public int xIndex;
	public int yIndex;
	
	public Guy(double x, double y, int initiative, int rgb){
		this.p = new Vector2(x, y);
		this.v = Vector2.zero();
		this.rgb = rgb;
		this.factionRgb = rgb;
		
		hp = maxHp;
		
		stam = maxStam;
		
		this.initiative = initiative;
	}
	
	//Note: this alters the argument
	public void updateVelocity(Vector2 desired)
	{
		if(strafeTimer > 0)
		{
			desired.rotate(strafeRad);
		}
		//Interpret a zero vector as a request to slow to a stop
		if(desired.x == 0 && desired.y == 0)
		{
			//If already stopped, mission accomplished, do nothing
			if(v.x == 0 && v.y == 0)
			{
				return;
			}
			//Otherwise, set the desired to the reverse of current course
			desired.add(v).mul(-1);
		}
		
		desired.normalize();
		desired.mul(acceleration);
		
		this.v.add(desired);
		
		if(v.magnitude() > maxSpeed)
		{
			this.v.normalize().mul(maxSpeed);
		}
	}
	
	void updateBearing(double desired)
	{
		double diff = Vector2.recenterBearing(desired - bearingRad);
		double update = Math.min(Math.abs(diff), rotationSpeed);
		if(diff > 0)
		{
			bearingRad = Vector2.recenterBearing(bearingRad + update);
		}
		else
		{
			bearingRad = Vector2.recenterBearing(bearingRad - update);
		}
	}
	
	
	public double dist(Guy other)
	{
		return p.dist(other.p);
	}
	
	public double dist(Vector2 pos)
	{
		return p.dist(pos);
	}
	
	public boolean shieldedFrom(Vector2 pos)
	{
		if(stam<=0)
		{
			return false;
		}
		double posBearing = Math.atan2(pos.y - p.y, pos.x - p.x);
		double diff = Vector2.recenterBearing(bearingRad - posBearing);
		return (diff < halfShieldRad) && (diff > -halfShieldRad);
	}
	
	public static class XSort implements Comparator<Guy>
	{
		public int compare(Guy a, Guy b) {
			return (a.p.x == b.p.x ? 0:
				   (a.p.x < b.p.x ? -1: 1));
				
		}
		
	}
	public static class YSort implements Comparator<Guy>
	{
		public int compare(Guy a, Guy b) {
			return (a.p.y == b.p.y ? 0:
				   (a.p.y < b.p.y ? -1: 1));
				
		}
		
	}
}
