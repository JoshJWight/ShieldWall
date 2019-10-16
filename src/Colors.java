import java.awt.Color;
import java.util.Random;


public class Colors {
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
}
