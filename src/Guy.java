
public class Guy {
	
	public float x;
	public float y;
	
	public float xv;
	public float yv;
	
	public int hp;
	public static final int maxHp = 4;
	
	public int stam;
	public static final int maxStam = 10;
	
	public static final float radius = 1;
	
	public static final float maxSpeed = 0.2f;
	public static final float acceleration = 0.1f;
	
	public int rgb;
	
	public Guy(float x, float y, int rgb){
		this.x = x;
		this.y = y;
		this.rgb = rgb;
		
		hp = maxHp;
		
		stam = maxStam;
	}
	
	void updateVelocity(float desiredX, float desiredY)
	{
		float dMag = magnitude(desiredX, desiredY);
		if(dMag==0) dMag = 0.0001f;
		
		xv += acceleration * desiredX/dMag;
		yv += acceleration * desiredY/dMag;
		
		float mag = magnitude(xv, yv);
		if(mag==0) mag = 0.0001f;
		
		if(mag > maxSpeed)
		{
			xv = maxSpeed * xv / mag;
			yv = maxSpeed * yv / mag;
		}
	}
	
	float dist(Guy other)
	{
		return magnitude(x - other.x, y - other.y);
	}
	
	//TODO move to vector class or something
	float magnitude(float a, float b)
	{
		return (float)Math.sqrt((a * a) + (b * b));
	}
}
