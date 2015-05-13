package com.gmail.jackbcousineau.desktop;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglGraphics;
import com.badlogic.gdx.backends.lwjgl.LwjglGraphics.SetDisplayModeCallback;
import com.gmail.jackbcousineau.AmazementMain;
import com.gmail.jackbcousineau.AudioHandler;

public class DesktopLauncher{
	
	private AudioThread audioThread;
	
	private class AudioThread extends Thread{
		
		AudioHandler audioHandler;
		String path;
		
		public AudioThread(String path){
			this.path = path;
		}
		
		public void run(){
			System.out.println("Running audio thread");
			try {
				audioHandler = new AudioHandler(path);
			} catch (Exception e){
			}
		}
	}

	private void p(String str){
		System.out.println(str);
	}
	LwjglApplicationConfiguration config;

	MainWindowFrame mainWindowFrame;

	static DesktopLauncher desktopLauncher;

	LwjglApplication app;
	
	
	int width = 1024, height = 1024;

	//public boolean gameRunning = false;

	//QuitHandler quitHandler;

	//DisplayMode dm;

	//LwjglFrame gameFrame;

	//LwjglGraphics graphics;

	public static void main (String[] arg) {
		desktopLauncher = new DesktopLauncher();

	}
	
	private void createConfig(){
		config = new LwjglApplicationConfiguration();
		//default height 480, width 640
		config.fullscreen = false;
		config.height = height;
		config.width = width;
		config.resizable = false;
		config.forceExit = false;
		p("Created config");
	}

	public DesktopLauncher(){
		createConfig();
		/*LwjglGraphics.SetDisplayModeCallback j = new LwjglGraphics.SetDisplayModeCallback() {
			@Override
			public LwjglApplicationConfiguration onFailure(LwjglApplicationConfiguration initialConfig) {
				p("CONFIG FAILURE");
				return null;
			}
		};
		config.setDisplayModeCallback = j;*/
		//dm = new DisplayMode(config.width, config.height, 0, 32);
		//System.out.println(config.height + ", " + config.width);
		mainWindowFrame = new MainWindowFrame();
		//GUIThread thread = new GUIThread();
		//thread.run();
		//startGame();
		//app
	}

	/*private void readDisplayMode(){
		dm = graphics.getDesktopDisplayMode();
		int h = dm.height;
		int w = dm.width;
		int b = dm.bitsPerPixel;
		int r = dm.refreshRate;
		p(h + ", " + w + ", " + b + ", " + r);
	}*/

	private void startGame(){
		config.width = width;
		config.height = height;
		p("Starting with " + config.width + ", " + config.height);
		if(app != null){
		//Gdx.app.postRunnable();
		//app.stop();
		//p("G: " + config.width + ", " + config.height);
		//Gdx.graphics.setDisplayMode(config.width, config.height, false);
		//app = new LwjglApplication(new AmazementMain(height, width, false), config, graphics);
			//p("Not null");
		}
		//LwjglGraphics lwjglGraphics = new LwjglGraphics(config);
		//else{
		app = new LwjglApplication(new AmazementMain(config.height, config.width), config);
		//graphics = app.getGraphics();
		//p(graphics.setDisplayMode(width, height, false) + "");
		//}
		//readDisplayMode();
		//app = new LwjglApplication(new AmazementMain(height, width), config);
		//Canvas canvas = new Canvas();
		//app = new LwjglApplication(new AmazementMain(height, width), config, canvas);
		//gameFrame = new LwjglFrame(new AmazementMain(height, width), config);
		//gameFrame.setVisible(true);
		//for(DisplayMode dm : app.getGraphics().getDisplayModes()){
		//p(dm.toString());
		//	}
		//readDisplayMode();
		//app.getGraphics().setDisplayMode(width, height, false);
		//readDisplayMode();

		//p("Density " + app.getGraphics().);
		//System.out.println(config.height + ", " + config.width);
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
		app.addLifecycleListener(new LifecycleListener(){

			@Override
			public void pause() {
			}

			@Override
			public void resume() {
			}

			@Override
			public void dispose() {
				//p("CLOSED");
				//gameRunning = false;
				mainWindowFrame.requestFocus();
			}});
		//gameRunning = true;
	}

	/*class GUIThread extends Thread{

		@Override
		public void run(){
			mainWindowFrame = new MainWindowFrame();
		}
	}*/



	class MainWindowFrame extends JFrame{

		private static final long serialVersionUID = 4022199660289052400L;

		//public boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));

		JButton startButton, chooseFile;
		JLabel fileLabel, resolutionLabel;
		JComboBox<String> resolutionBox;
		JCheckBox fullscreenCheckBox;

		//boolean inRestoringState = true, cmdPressed = false, qPressed = false;

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
						//config.
						height = 1024;
						//config.
						width = 1024;
					}
					else if(box.getSelectedItem() == "512 x 512"){
						//config.
						height = 512;
						//config.
						width = 512;
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
						try {
							//audioThread = new AudioThread(chooser.getSelectedFile().getPath());
							//audioThread.start();
							//audioThread.run();
							//playSound(chooser.getSelectedFile().getPath());
							new AudioHandler(chooser.getSelectedFile().getPath());
						} catch (Exception ex) {
						}
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

			/*if(MAC_OS_X){
				//OSXAdapter.
				//this.
				quitHandler = new QuitHandler(){

					@Override
					public void handleQuitRequestWith(QuitEvent qe, QuitResponse qr) {
						p("APPLE QUITTING");
						qr.performQuit();
					}

				};
				//Application.getApplication().setQuitHandler(quitHandler);
			}

			/*addFocusListener(new FocusListener(){

				@Override
				public void focusGained(FocusEvent e) {
					p("GAINED FOCUS");
					//Application.getApplication().setQuitHandler(quitHandler);
				}

				@Override
				public void focusLost(FocusEvent e) {
					p("LOST FOCUS");
				}

			});*/

			addKeyListener(new KeyListener(){

				@Override
				public void keyTyped(KeyEvent e) {
				}

				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyChar() == 'q'&&e.isMetaDown()) p("Should quit");
					if(e.getKeyChar() == 'p'){
						p("Pause pressed");
						//audioHandler.pause();
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
				}});

			/*if(!inRestoringState) addWindowFocusListener(new WindowFocusListener() {

				@Override
				public void windowGainedFocus(WindowEvent arg0) {
					p("window gained focus");
					if(!gameRunning) SwingUtilities.invokeLater( new Runnable() {
						public void run() {
							if(!inRestoringState){
								inRestoringState = true;
								setVisible(false);
								setVisible(true);
								requestFocus();
							} else {
								inRestoringState = false;
							}
						}
					} );
					p("refreshed");
				}

				@Override
				public void windowLostFocus(WindowEvent arg0) {
					p("LOST FOCUS");
				}

			});*/
			setFocusCycleRoot(true);
		}
	}
	
	/*
	 * 	
	public static synchronized void playSound(final String url) {
		  new Thread(new Runnable() {
		  // The wrapper thread is unnecessary, unless it blocks on the
		  // Clip finishing; see comments.
		    public void run() {
		      try {
		        Clip clip = AudioSystem.getClip();
		        clip.open(AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(url))));
		        clip.start(); 
		      } catch (Exception e) {
		        System.err.println(e.getMessage());
		      }
		    }
		  }).start();
		}
	 */

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
