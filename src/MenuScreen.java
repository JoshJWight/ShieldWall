import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class MenuScreen extends JPanel {
	public static final int defaultNGuys = 4000;
	
	private JTextField nGuysField;
	
	public MenuScreen(Runnable startHandler)
	{
		setLayout(new GridLayout(2, 2));
		JLabel nGuysLabel = new JLabel("Number of guys:");
		nGuysField = new JTextField("" + defaultNGuys);
		add(nGuysLabel, 0, 0);
		add(nGuysField, 0, 1);
		
		JButton startButton = new JButton("Fight!");
		startButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				startHandler.run();
			}
		});
		add(startButton, 1, 0);
	}
	
	public int getNGuys()
	{
		try {
			return Integer.valueOf(nGuysField.getText());
		} catch(NumberFormatException e) {
			return defaultNGuys;
		}
		
	}
}
