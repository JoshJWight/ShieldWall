import java.util.Arrays;


public class Vector2 {
	public double x;
	public double y;
	
	public Vector2(double x, double y)
	{
		this.x = x;
		this.y = y;
		checkNaN();
	}
	
	public Vector2(Vector2 other)
	{
		this.x = other.x;
		this.y = other.y;
		checkNaN();
	}
	
	public Vector2 add(Vector2 other)
	{
		this.x += other.x;
		this.y += other.y;
		checkNaN();
		return this;
	}
	
	public Vector2 sub(Vector2 other)
	{
		this.x -= other.x;
		this.y -= other.y;
		checkNaN();
		return this;
	}
	
	public Vector2 mul(double val)
	{
		this.x *= val;
		this.y *= val;
		checkNaN();
		return this;
	}
	
	double magnitude()
	{
		checkNaN();
		return Math.sqrt((x * x) + (y * y));
	}
	
	public static Vector2 zero()
	{
		return new Vector2(0, 0);
	}
	
	public static Vector2 unit(double theta)
	{
		return new Vector2(1.0, theta).toCartesian();
	}
	
	public Vector2 normalize()
	{
		double mag = this.magnitude();
		if(mag == 0)
		{
			//Arbitrary unit vector as fail case for zero vector
			y = 1.0;
		}
		else
		{
			mul(1.0 / mag);
		}
		checkNaN();
		return this;
	}
	
	public Vector2 addPolar(double r, double theta)
	{
		x += Math.cos(theta) * r;
		y += Math.sin(theta) * r;
		checkNaN();
		return this;
	}
	//Notation here is that polar coords are (r, theta)
	public Vector2 addPolar(Vector2 polar)
	{
		addPolar(polar.x, polar.y);
		checkNaN();
		return this;
	}
	
	public Vector2 toPolar()
	{
		double r = magnitude();
		double theta = Math.atan2(y, x);
		x = r;
		y = theta;
		return this;
	}
	
	public Vector2 toCartesian()
	{
		double xx = Math.cos(y) * x;
		double yy = Math.sin(y) * x;
		x = xx;
		y = yy;
		return this;
	}
	
	public Vector2 rotate(double angle)
	{
		//TODO optimize?
		toPolar();
		y += angle;
		toCartesian();
		return this;
	}
	
	public double angle()
	{
		return Math.atan2(y, x);
	}
	
	private void checkNaN()
	{
		if(Double.isNaN(x) || Double.isNaN(x))
		{
			System.out.println("Vector2 has NaN value. Printing stack trace and exiting...");
			System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
			System.exit(0);
		}
	}
}
