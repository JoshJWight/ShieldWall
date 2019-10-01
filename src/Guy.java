
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
	
	public int rgb;
	
	public Guy(float x, float y, int rgb){
		this.x = x;
		this.y = y;
		this.rgb = rgb;
		
		hp = maxHp;
		
		stam = maxStam;
	}
}
