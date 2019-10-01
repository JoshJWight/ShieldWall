import java.util.ArrayList;
import java.util.Random;


public class Main {

	public static void main(String[] args) {
		ArrayList<Guy> list = new ArrayList<Guy>();
		
		BattleDisplay display = new BattleDisplay(list, 0, 0, 120, 80);
		
		Random rand = new Random();
		
		for(int i=0; i<100; i++)
		{
			Guy guy = new Guy(rand.nextInt(120), rand.nextInt(120), rand.nextInt());
			guy.xv = rand.nextFloat() - 0.5f;
			guy.yv = rand.nextFloat() - 0.5f;
			list.add(guy);
		}
		
		while(true)
		{
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for(Guy guy: list)
			{
				guy.x += guy.xv;
				guy.y += guy.yv;
			}
			display.repaint();
		}

	}

}
