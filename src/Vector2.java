
public class Vector2 {
	public double x;
	public double y;
	
	public Vector2(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	
	public Vector2(Vector2 other)
	{
		this.x = other.x;
		this.y = other.y;
	}
	
	public Vector2 add(Vector2 other)
	{
		this.x += other.x;
		this.y += other.y;
		return this;
	}
	
	public Vector2 sub(Vector2 other)
	{
		this.x -= other.x;
		this.y -= other.y;
		return this;
	}
	
	public Vector2 mul(double val)
	{
		this.x *= val;
		this.y *= val;
		return this;
	}
	
	double magnitude()
	{
		return Math.sqrt((x * x) + (y * y));
	}
	
	public static Vector2 zero()
	{
		return new Vector2(0, 0);
	}
	
	public Vector2 normalize()
	{
		return this.mul(1.0 / this.magnitude());
	}
}
