
public class Guy {
	
	public Vector2 p;
	public Vector2 v;
	
	public int hp;
	public static final int maxHp = 4;
	
	public int stam;
	public static final int maxStam = 10;
	
	public static final double radius = 1;
	public static final double hpRadius = 0.5;
	
	public static final double maxSpeed = 0.2f;
	public static final double acceleration = 0.02f;
	
	public static final double personalSpace = 2.5;
	
	public int rgb;
	
	public Guy(double x, double y, int rgb){
		this.p = new Vector2(x, y);
		this.v = Vector2.zero();
		this.rgb = rgb;
		
		hp = maxHp;
		
		stam = maxStam;
	}
	
	//Note: this alters the argument
	void updateVelocity(Vector2 desired)
	{
		double dMag = desired.magnitude();
		if(dMag==0) dMag = 0.0001f;
		
		desired.mul(acceleration);
		desired.mul(1.0/dMag);
		
		this.v.add(desired);
		
		if(v.magnitude() > maxSpeed)
		{
			this.v.normalize().mul(maxSpeed);
		}
	}
	
	double dist(Guy other)
	{
		return new Vector2(p).sub(other.p).magnitude();
	}
	
	double dist(Vector2 pos)
	{
		return new Vector2(p).sub(pos).magnitude();
	}
	
}
