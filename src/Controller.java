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
		while(true)
		{
			long startTime = System.currentTimeMillis();
			
			battleModel.update();
			
			battleDisplay.requestFocus();
			battleDisplay.repaint();
			
			long endTime = System.currentTimeMillis();
			
			try {
				long sleep = Math.max((int)(frameTimeMs/battleDisplay.playbackSpeed) - (endTime - startTime), 0);
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
