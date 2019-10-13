import java.util.ArrayList;


public class Group {
	public static final double formationRadius = 20;
	
	public ArrayList<Guy> guys;
	
	public int rgb;
	
	//These values are computed on update
	Vector2 center;
	double radius;
	
	public Group(ArrayList<Guy> initialGuys, int rgb)
	{
		this.guys = initialGuys;
		this.rgb = rgb;
		for(Guy guy: guys)
		{
			guy.rgb = rgb;
		}
	}
}
