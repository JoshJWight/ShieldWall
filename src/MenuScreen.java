import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class MenuScreen extends JPanel {
	public static final int defaultNGuys = 4000;
	public static final int defaultNFactions = 2;
	
	private JTextField nGuysField;
	private JTextField nFactionsField;
	
	public MenuScreen(Runnable startHandler)
	{
		setLayout(new GridLayout(3, 2));
		
		JLabel nGuysLabel = new JLabel("Number of guys:");
		nGuysField = new JTextField("" + defaultNGuys);
		add(nGuysLabel);
		add(nGuysField);
		
		JLabel nFactionsLabel = new JLabel("Number of factions:");
		nFactionsField = new JTextField("" + defaultNFactions);
		add(nFactionsLabel);
		add(nFactionsField);
		
		JButton startButton = new JButton("Fight!");
		startButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				startHandler.run();
			}
		});
		add(startButton);
	}
	
	public int getNGuys()
	{
		try {
			return Integer.valueOf(nGuysField.getText());
		} catch(NumberFormatException e) {
			return defaultNGuys;
		}
		
	}
	
	public int getNFactions()
	{
		try {
			return Integer.valueOf(nFactionsField.getText());
		} catch(NumberFormatException e) {
			return defaultNFactions;
		}
	}
}
