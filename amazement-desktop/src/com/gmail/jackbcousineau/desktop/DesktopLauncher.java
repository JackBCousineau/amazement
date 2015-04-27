package com.gmail.jackbcousineau.desktop;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.gmail.jackbcousineau.AmazementMain;

public class DesktopLauncher{

	/*private void p(String str){
		System.out.println(str);
	}*/
	LwjglApplicationConfiguration config;

	MainWindowFrame mainWindowFrame;

	static DesktopLauncher desktopLauncher;

	LwjglApplication app;

	public static void main (String[] arg) {
		desktopLauncher = new DesktopLauncher();

	}

	public DesktopLauncher(){
		config = new LwjglApplicationConfiguration();
		//default height 480, width 640
		config.height = 1024;
		config.width = 1024;
		System.out.println(config.height + ", " + config.width);
		mainWindowFrame = new MainWindowFrame();
		//GUIThread thread = new GUIThread();
		//thread.run();
		//startGame();
		//app
	}

	private void startGame(){
		app = new LwjglApplication(new AmazementMain(), config);
	    // create an LwjglFrame with your configuration and the listener
	    /*LwjglFrame frame = new LwjglFrame(new AmazementMain(), config);

	    // add a component listener for when the frame gets moved
	    frame.addComponentListener(new ComponentAdapter() {
	        @Override
	        public void componentMoved(ComponentEvent e) {
	            // somehow pause your game here
	        }
	    });

	    // set the frame visible
	    frame.setVisible(true);*/
	}

	/*class GUIThread extends Thread{

		@Override
		public void run(){
			mainWindowFrame = new MainWindowFrame();
		}
	}*/



	class MainWindowFrame extends JFrame{

		private static final long serialVersionUID = 4022199660289052400L;

		JButton startButton, chooseFile;
		JLabel fileLabel, resolutionLabel;
		JComboBox<String> resolutionBox;
		JCheckBox fullscreenCheckBox;

		public MainWindowFrame(){
			super("Amazement");
			setSize(300, 250);
			setLocationRelativeTo(null);
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			setResizable(false);

			JPanel pane = new JPanel();
			getContentPane().add(pane);
			SpringLayout layout = new SpringLayout();
			pane.setLayout(layout);

			resolutionLabel = new JLabel("Resolution");
			pane.add(resolutionLabel);

			fileLabel = new JLabel("No file chosen");
			pane.add(fileLabel);

			resolutionBox = new JComboBox<String>();
			resolutionBox.addItem("1024 x 1024");
			resolutionBox.addItem("512 x 512");
			resolutionBox.setFocusable(false);
			pane.add(resolutionBox);

			resolutionBox.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent event){
					@SuppressWarnings("unchecked")
					JComboBox<String> box = (JComboBox<String>)event.getSource();
					if(box.getSelectedItem() == "1024 x 1024"){
						config.height = 1024;
						config.width = 1024;
					}
					else if(box.getSelectedItem() == "512 x 512"){
						config.height = 512;
						config.width = 512;
					}
				}
			});

			fullscreenCheckBox = new JCheckBox();
			fullscreenCheckBox.setFocusable(false);
			//pane.add(fullscreenCheckBox);

			startButton = new JButton("Start");
			startButton.setFocusable(false);
			pane.add(startButton);

			startButton.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					startGame();
				}});

			chooseFile = new JButton("Choose file");
			chooseFile.setFocusable(false);
			pane.add(chooseFile);
			chooseFile.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser();
					FileNameExtensionFilter filter = new FileNameExtensionFilter(
							".wav files", "wav");
					chooser.setFileFilter(filter);
					int returnVal = chooser.showOpenDialog(null);
					if(returnVal == JFileChooser.APPROVE_OPTION) {
						System.out.println("You chose to open this file: " +
								chooser.getSelectedFile().getName());
						fileLabel.setText("File: " + chooser.getSelectedFile().getName());
					}
				}

			});

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

			setVisible(true);
		}
	}

	public class LwjglFrame extends JFrame{

		private final Canvas canvas;

		public LwjglFrame(final ApplicationListener listener, final LwjglApplicationConfiguration config) {
			canvas = new Canvas(){

				public final void addNotify () {
					super.addNotify();
					app = new LwjglApplication(listener, config, canvas);
				}

				public final void removeNotify () {
					app.stop();
					super.removeNotify();
				}
			};
			canvas.setIgnoreRepaint(true);
			canvas.setFocusable(true);

			setLayout(new BorderLayout());
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			add(canvas, BorderLayout.CENTER);
			setPreferredSize(new Dimension(config.width, config.height));
			pack();
		}
	}

}
