public class Main {
	
	static final int frameTimeMs = 20;
	//TODO move most of this stuff out of Main
	public static void main(String[] args) {
		int nGuys = 400;
		BattleModel model = new BattleModel(nGuys);
		BattleDisplay display = new BattleDisplay(model.guys, model.groups);
		
		while(true)
		{
			long startTime = System.currentTimeMillis();
			
			model.update();
			
			display.repaint();
			
			long endTime = System.currentTimeMillis();
			
			//System.out.println("Frame processing time: " + (endTime - startTime));
			
			try {
				long sleep = Math.max((frameTimeMs - (endTime - startTime)), 0);
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
