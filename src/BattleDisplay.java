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
	public float minX;
	public float maxX;
	public float minY;
	public float maxY;
	
	ArrayList<Guy> guys;
	
	public BattleDisplay(ArrayList<Guy> guys, int minX, int minY, int maxX, int maxY)
	{
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
		this.guys = guys;
		
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	System.exit(0);
		    }
		});
		
		this.setTitle("Shield Wall");
		this.setSize(WIDTH, HEIGHT);
		this.setVisible(true);
	}
	public boolean inbounds(float x, float y)
	{
		return (x >= (minX - margin))
			 &&(y >= (minY - margin))
			 &&(x <= (maxX + margin))
			 &&(y <= (maxY + margin));  
	}
	public void paint(Graphics graphics){
		Image image = createImage(WIDTH, HEIGHT);
		Graphics g = image.getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
		int radius = (int)(Guy.radius * WIDTH / (maxX - minX));
		for(Guy guy: guys) {
			if(inbounds(guy.x, guy.y))
			{
				g.setColor(new Color(guy.rgb));
				//Convert world units to pixels
				int x = (int)((guy.x - minX) * WIDTH / (maxX - minX));
				int y = (int)((guy.y - minY) * HEIGHT / (maxY - minY));
				g.fillOval(x - radius, y - radius, radius*2, radius*2);
			}
		}
		
		graphics.drawImage(image, 0, 0, null);
	}
}
