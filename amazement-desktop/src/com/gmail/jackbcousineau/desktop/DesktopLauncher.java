package com.gmail.jackbcousineau.desktop;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.gmail.jackbcousineau.AmazementMain;

/**
 * The DesktopLauncher class; the PC-specific module of a libgdx game
 * that passes a PC-friendly instance of the ambiguous main game object into
 * a LWJGL (LightWeight Java Game Library) handler.
 */
public class DesktopLauncher{

	/**
	 * The LWJGL application instance, contains the
	 * actual AmazementMain game instance once started.
	 */
	LwjglApplication app;
	
	/**
	 * The LWJGL configuration object, stored to edit resolution data if changed
	 * in the main menu. Gets passed onto the LWJGL engine when the game is started.
	 */
	LwjglApplicationConfiguration config;

	/**
	 * The width and height that will be given to the LWJGL config
	 * when the game is started. These dimensions would be directly-
	 * stored in the config itself, but a bug in the current version
	 * of LWJGL prevents that functionality from consistently working.
	 */
	int width = 1024, height = 1024;

	/**
	 * The path to the .wav file to use for the game,
	 * set when a file is selected in the main menu.
	 */
	String filePath = null;

	/**
	 * Implementation of main Java method, simply creates the initial DesktopLauncher object.
	 */
	public static void main (String[] arg){
		new DesktopLauncher();
	}

	/**
	 * Constructs new DesktopLauncher, the game's LWJGL config
	 * that we modify to give the game's window certain behaviors,
	 * and the main menu GUI built on Swing.
	 */
	public DesktopLauncher(){
		config = new LwjglApplicationConfiguration();
		config.fullscreen = false;
		config.height = height;
		config.width = width;
		config.resizable = false;
		config.forceExit = false;
		new MainWindowFrame();
	}

	/**
	 * The MainWindowFrame class, built on the Swing framework. Extends the default
	 * JFrame, and contains all the elements of the main menu GUI.
	 */
	class MainWindowFrame extends JFrame{

		/**
		 * Recommended inclusion of a serialVersionUID.
		 */
		private static final long serialVersionUID = 4022199660289052400L;

		/**
		 * Constructs a new MainWindowFrame.
		 */
		public MainWindowFrame(){
			/**
			 * Super constructor for the enclosing JFrame,
			 * sets default dimensions and behavior.
			 */
			super("Amazement");
			setSize(300, 250);
			setLocationRelativeTo(null);
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			setResizable(false);

			/**
			 * Sets a new JPanel as the base element of the
			 * JFrame, organized semantically by a SpringLayout.
			 */
			JPanel pane = new JPanel();
			getContentPane().add(pane);
			SpringLayout layout = new SpringLayout();
			pane.setLayout(layout);

			/**
			 * Sets up the resolution-setting combo-box and label.
			 */
			JLabel resolutionLabel = new JLabel("Resolution");
			pane.add(resolutionLabel);
			JComboBox<String> resolutionBox = new JComboBox<String>();
			resolutionBox.addItem("1024 x 1024");
			resolutionBox.addItem("512 x 512");
			resolutionBox.setFocusable(false);
			resolutionBox.addActionListener(new ActionListener(){

				/**
				 * Combo-box listener, changes height and width
				 * based on which of the two boxes is selected.
				 */
				@Override
				public void actionPerformed(ActionEvent event){
					@SuppressWarnings("unchecked")
					JComboBox<String> box = (JComboBox<String>)event.getSource();
					if(box.getSelectedItem() == "1024 x 1024"){
						height = 1024;
						width = 1024;
					}
					else if(box.getSelectedItem() == "512 x 512"){
						height = 512;
						width = 512;
					}
				}
			});
			pane.add(resolutionBox);

			/**
			 * Sets up the Start button.
			 */
			JButton startButton = new JButton("Start");
			startButton.setFocusable(false);
			pane.add(startButton);
			startButton.addActionListener(new ActionListener(){

				/**
				 * Sets the LWJGL config to the values stored in width and height, and passes
				 * those values, along with the chosen filepath, to LWJGL to start the game itself.
				 */
				@Override
				public void actionPerformed(ActionEvent e){
					config.width = width;
					config.height = height;
					app = new LwjglApplication(new AmazementMain(config.height, config.width, filePath), config);
				}
			});
			
			/**
			 * Sets up the Choose File button and label.
			 */
			final JLabel fileLabel = new JLabel("No file chosen");
			pane.add(fileLabel);
			JButton chooseFile = new JButton("Choose file");
			chooseFile.setFocusable(false);
			chooseFile.addActionListener(new ActionListener(){

				/**
				 * Opens a native interface for local file selection, filtered
				 * for .wav files. Passes the path on to filePath successful.
				 */
				@Override
				public void actionPerformed(ActionEvent e){
					JFileChooser chooser = new JFileChooser();
					chooser.setFileFilter(new FileNameExtensionFilter(".wav files", "wav"));
					int returnVal = chooser.showOpenDialog(null);
					if(returnVal == JFileChooser.APPROVE_OPTION){
						fileLabel.setText("File: " + chooser.getSelectedFile().getName());
						filePath = chooser.getSelectedFile().getPath();
					}
				}
			});
			pane.add(chooseFile);

			/**
			 * Manually implements a Command + Q function to quit the
			 * app if needed, gets overwritten by LWJGL otherwise. 
			 */
			addKeyListener(new KeyAdapter(){

				@Override
				public void keyPressed(KeyEvent e){
					if(e.getKeyChar() == 'q'&&e.isMetaDown())
						System.exit(0);
				}
			});

			/**
			 * Defines Swing SpringLayout constraints for each GUI element.
			 */
			layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, startButton, 0, SpringLayout.HORIZONTAL_CENTER, pane);
			layout.putConstraint(SpringLayout.SOUTH, startButton, -15, SpringLayout.SOUTH, pane);

			layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, resolutionBox, 0, SpringLayout.HORIZONTAL_CENTER, pane);
			layout.putConstraint(SpringLayout.NORTH, resolutionBox, 20, SpringLayout.NORTH, pane);

			layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, resolutionLabel, 0, SpringLayout.HORIZONTAL_CENTER, pane);
			layout.putConstraint(SpringLayout.NORTH, resolutionLabel, 0, SpringLayout.SOUTH, resolutionBox);

			layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, chooseFile, 0, SpringLayout.HORIZONTAL_CENTER, pane);
			layout.putConstraint(SpringLayout.NORTH, chooseFile, 35, SpringLayout.SOUTH, resolutionLabel);

			layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, fileLabel, 0, SpringLayout.HORIZONTAL_CENTER, pane);
			layout.putConstraint(SpringLayout.NORTH, fileLabel, 0, SpringLayout.SOUTH, chooseFile);

			/**
			 * Finally enables the entire frame itself to be seen, after setup is complete.
			 */
			setVisible(true);
		}
	}
}
