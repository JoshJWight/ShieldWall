import javax.swing.JFrame;

public class Controller {
	static final int frameTimeMs = 20;
	
	private JFrame gui;
	private MenuScreen menu;
	
	private Thread battleThread;
	
	private BattleModel battleModel;
	private BattleDisplay battleDisplay;
	
	
	public Controller()
	{
		Runnable startHandler = new Runnable() {
			public void run(){
				menuStartHandler();
			}
		};
		menu = new MenuScreen(startHandler);
		
		gui = new JFrame();
		gui.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	System.exit(0);
		    }
		});
		
		gui.setTitle("Shield Wall");
		gui.setSize(BattleDisplay.START_WIDTH, BattleDisplay.START_HEIGHT);
		gui.add(menu);
		gui.setVisible(true);
	}
	
	public void menuStartHandler()
	{
		int nGuys = menu.getNGuys();
		int nFactions = menu.getNFactions();
		
		battleModel = new BattleModel(nGuys, nFactions);
		battleDisplay = new BattleDisplay(battleModel.guys, battleModel.groups);
		gui.remove(menu);
		gui.add(battleDisplay);
		gui.revalidate();
		
		battleThread = new Thread() {
			public void run() {
				runBattle(nGuys);
			}
		};
		battleThread.start();
	}
	
	public void runBattle(int nGuys)
	{
		long lastFrameCheck = System.currentTimeMillis();
		int framesInInterval = 0;
		
		long endTimer = 0;
		final long maxEndTimer = 5000;
		while(endTimer < maxEndTimer)
		{
			long startTime = System.currentTimeMillis();
			
			battleModel.update();
			
			battleDisplay.requestFocus();
			battleDisplay.repaint();
			
			long endTime = System.currentTimeMillis();
			
			long sleepTime = Math.max((long)(frameTimeMs/battleDisplay.playbackSpeed) - (endTime - startTime), 0);
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(endTime - lastFrameCheck >= 1000)
			{
				battleDisplay.framesPerSecond = (int)Math.round(framesInInterval * 1000.0 / (double)(endTime - lastFrameCheck));
				lastFrameCheck = endTime;
				framesInInterval = 0;
			}
			
			if(battleModel.winner() != null)
			{
				battleDisplay.winnerRgb = battleModel.winner().factionRgb;
				battleDisplay.winnerName = battleModel.factionName(battleDisplay.winnerRgb);
				
				long timeElapsed = (endTime - startTime) + sleepTime;
				endTimer += timeElapsed;
			}	
			
			framesInInterval ++;
		}
		gui.remove(battleDisplay);
		gui.add(menu);
		gui.revalidate();
		menu.repaint();
	}
}
