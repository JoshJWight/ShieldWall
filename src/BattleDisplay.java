import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JFrame;

public class BattleDisplay extends JFrame {
	public static final int START_WIDTH = 1000;
	public static final int START_HEIGHT = 600;
	
	//World units, not pixels
	public static final int margin = 5;
	
	
	public Vector2 corner;
	public double pixPerUnit;
	
	ArrayList<Guy> guys;
	
	ArrayList<Color> hpColors;
	ArrayList<Color> shieldColors;
 	
	public BattleDisplay(ArrayList<Guy> guys)
	{
		corner = new Vector2(0, 0);
		pixPerUnit = 10;
		
		this.guys = guys;
		
		//Precompute colors for all hp values
		hpColors = new ArrayList<Color>();
		for(int i=0; i<= Guy.maxHp; i++)
		{
			float fraction = ((float)i)/((float)Guy.maxHp);
			hpColors.add(new Color(1.0f, fraction, fraction));
		}
		shieldColors = new ArrayList<Color>();
		for(int i=0; i<= Guy.maxStam; i++)
		{
			float fraction = ((float) i) / ((float)Guy.maxStam);
			shieldColors.add(new Color(1.0f - fraction, fraction, 0.5f));
		}
		
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	System.exit(0);
		    }
		});
		
		this.setTitle("Shield Wall");
		this.setSize(START_WIDTH, START_HEIGHT);
		this.setVisible(true);
	}
	
	//Convert to screen coords. Wanted short names since we'll use these a lot
	//TODO could precompute some of this stuff
	int sx(double worldX)
	{
		return (int)((worldX - corner.x) * pixPerUnit);
	}
	int sy(double worldY)
	{
		return (int)((worldY - corner.y) * pixPerUnit);
	}
	int ss(double worldScalar)
	{
		return (int)(worldScalar * pixPerUnit);
	}
	
	public boolean inbounds(Vector2 v)
	{
		return (v.x >= (corner.x - margin))
			 &&(v.y >= (corner.y - margin))
			 &&(v.x <= (corner.x + (getWidth()/pixPerUnit) + margin))
			 &&(v.y <= (corner.y + (getHeight()/pixPerUnit) + margin));  
	}
	public void paint(Graphics graphics){
		Image image = createImage(getWidth(), getHeight());
		Graphics g = image.getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		for(Guy guy: guys) {
			if(inbounds(guy.p))
			{
				//Body
				Color guyColor = new Color(guy.rgb);
				if(guy.deathTimer > 0)
				{
					guyColor = new Color(guyColor.getRed(), guyColor.getGreen(), guyColor.getBlue(), 
							(255 * guy.deathTimer) / Guy.maxDeathTimer);
				}
				g.setColor(guyColor);
				g.fillOval(
						sx(guy.p.x - Guy.radius), 
						sy(guy.p.y - Guy.radius), 
						ss(Guy.radius*2.0),
						ss(Guy.radius*2.0));
				
				//Health
				g.setColor(hpColors.get(guy.hp));
				g.fillOval(
						sx(guy.p.x - Guy.hpRadius),
						sy(guy.p.y - Guy.hpRadius), 
						ss(Guy.hpRadius*2.0), 
						ss(Guy.hpRadius*2.0));
				
				//Bearing / attack
				double lineTip = Guy.radius * 7.0/6.0;
				if(guy.attackTimer > 0)
				{
					lineTip = guy.attackReach;
				}
				Vector2 wepIn = new Vector2(guy.p).addPolar(Guy.radius * 5.0/6.0, guy.bearingRad);
				Vector2 wepOut = new Vector2(guy.p).addPolar(Guy.radius * lineTip, guy.bearingRad);
				g.drawLine(
						sx(wepIn.x),
						sy(wepIn.y),
						sx(wepOut.x),
						sy(wepOut.y));
				
				//Shield
				g.setColor(shieldColors.get(guy.stam));				
				//This is the hypotenuse of a triangle where the other sides are the radius and half the shield
				double lenToShield = Guy.radius / Math.cos(Guy.halfShieldRad);
				//dunno if this left/right is correct but doesn't really matter
				Vector2 shieldRight = new Vector2(guy.p).addPolar(lenToShield, guy.bearingRad + Guy.halfShieldRad);
				Vector2 shieldLeft = new Vector2(guy.p).addPolar(lenToShield, guy.bearingRad - Guy.halfShieldRad);
				g.drawLine(
						sx(shieldRight.x),
						sy(shieldRight.y),
						sx(shieldLeft.x),
						sy(shieldLeft.y));
			}
		}
		
		graphics.drawImage(image, 0, 0, null);
	}
}
