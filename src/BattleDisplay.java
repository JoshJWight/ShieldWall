import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JFrame;

public class BattleDisplay extends JFrame {
	public static final int WIDTH = 1200;
	public static final int HEIGHT = 800;
	
	//World units, not pixels
	public static final int margin = 5;
	
	//Area of the battlefield covered by the screen
	public double minX;
	public double maxX;
	public double minY;
	public double maxY;
	
	ArrayList<Guy> guys;
	
	ArrayList<Color> hpColors;
	
	public BattleDisplay(ArrayList<Guy> guys, int minX, int minY, int maxX, int maxY)
	{
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
		this.guys = guys;
		
		//Precompute colors for all hp values
		hpColors = new ArrayList<Color>();
		for(int i=0; i<= Guy.maxHp; i++)
		{
			float fraction = ((float)i)/Guy.maxHp;
			hpColors.add(new Color(1.0f, fraction, fraction));
		}
		
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	System.exit(0);
		    }
		});
		
		this.setTitle("Shield Wall");
		this.setSize(WIDTH, HEIGHT);
		this.setVisible(true);
	}
	public boolean inbounds(Vector2 v)
	{
		return (v.x >= (minX - margin))
			 &&(v.y >= (minY - margin))
			 &&(v.x <= (maxX + margin))
			 &&(v.y <= (maxY + margin));  
	}
	public void paint(Graphics graphics){
		Image image = createImage(WIDTH, HEIGHT);
		Graphics g = image.getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
		int radius = (int)(Guy.radius * WIDTH / (maxX - minX));
		int hpRadius = (int)(Guy.hpRadius * WIDTH / (maxX - minX));
		for(Guy guy: guys) {
			if(inbounds(guy.p))
			{
				g.setColor(new Color(guy.rgb));
				//Convert world units to pixels
				int x = (int)((guy.p.x - minX) * WIDTH / (maxX - minX));
				int y = (int)((guy.p.y - minY) * HEIGHT / (maxY - minY));
				g.fillOval(x - radius, y - radius, radius*2, radius*2);
				g.setColor(hpColors.get(guy.hp));
				g.fillOval(x - hpRadius, y - hpRadius, hpRadius*2, hpRadius*2);
			}
		}
		
		graphics.drawImage(image, 0, 0, null);
	}
}
