package com.gmail.jackbcousineau;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.gmail.jackbcousineau.AmazementMain.Maze.MazePiece;

public class AmazementMain extends Game{

	/**
	 * Custom printing function -- greatly simplifies console output
	 * @param toPrint Object to print
	 */
	private void print(Object toPrint){
		System.out.println(toPrint);
	}

	int height, width;
	int playerBodyScale = 23, playerOffset = 24, textX = 400, textY = 550;

	OrthographicCamera camera = new OrthographicCamera();
	TiledMap tiledMap;
	TiledMapRenderer tiledMapRenderer;
	SpriteBatch sb;
	Texture texture;
	Sprite playerSprite;
	Clip clip;
	BitmapFont font;

	World world = new World(new Vector2(0, 0), false); ;
	Box2DDebugRenderer debugRenderer;
	Body playerBody;
	Maze maze;
	Sound collisionSound;

	boolean leftPressed = false, rightPressed = false, upPressed = false, downPressed = false,
			gravity = false, drawCollision = false, resetPosition = false,
			displayLevel = false, keepDisplayLevel = false, gameOver = false, soManyFail = false,
			lowRes = false, paused = false, debug = false;

	String songPath;

	double songDuration;

	Timer timer = new Timer();

	/**
	 * Constructs new AmazementGame object -- base game constructor.<br>
	 * As a subclass of com.badlogic.gdx.Game, calls inherited creation<br>
	 * and initialization method as soon as this constructor is finished.
	 * @param height The game window height.
	 * @param width The game window width.
	 * @param songPath The filepath to the song to use. If nothing is selected,<br>
	 * defaults to the included file "Satisfaction.wav", by the Rolling Stones;<br>
	 * should be placed in your /Users/YourAccountName/Amazement Songs/ folder.
	 */
	public AmazementMain(int height, int width, String songPath){
		this.height = height;
		this.width = width;
		if(songPath == null)
			this.songPath = System.getProperty("user.home")+"/Amazement Songs/Satisfaction.wav";
		else
			this.songPath = songPath;
	}

	/**
	 * Implementation of libgdx engine initialization. Loads and initializes most assets,<br>
	 * and all listeners, that are used in the game itself.
	 */
	@Override public void create (){
		Box2D.init();
		Gdx.graphics.setDisplayMode(width, height, false);
		Gdx.graphics.setTitle("Amazement");
		font = new BitmapFont();

		if(width == 512){
			lowRes = true;
			playerBodyScale = 12;
			playerOffset/=2;
			textX = 190;
			textY = 290;
			font.setScale(3);
			tiledMap = new TmxMapLoader().load("map512.tmx");
		}
		else{
			font.setScale(5);
			tiledMap = new TmxMapLoader().load("map1024.tmx");
		}
		tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
		camera.setToOrtho(false,width,height);
		camera.update();
		sb = new SpriteBatch();

		try{
			clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(songPath))));
		} catch (Exception e){
			System.err.println("Error loading .wav file");
			System.err.println(e.getMessage());
		}
		clip.start();
		songDuration = clip.getFrameLength()/clip.getFormat().getFrameRate();
		clip.addLineListener(new LineListener(){

			public void update(LineEvent event){
				if (event.getType() == LineEvent.Type.STOP){
					if((int)clip.getMicrosecondPosition()/1000000 == (int)songDuration){
						maze.resetAll(true);
						soManyFail = true;
					}
				}
			}});

		Gdx.input.setInputProcessor(new InputAdapter(){

			public boolean keyDown(int keycode){
				if(playerBody != null){
					if(!paused){
						if(keycode == Input.Keys.LEFT)
							leftPressed = true;
						if(keycode == Input.Keys.RIGHT)
							rightPressed = true;
						if(keycode == Input.Keys.UP)
							upPressed = true;
						if(keycode == Input.Keys.DOWN)
							downPressed = true;
					}
					if(keycode == Input.Keys.ESCAPE)
						pauseGame();
				}
				return false;
			}

			public boolean keyUp(int keycode){
				if(!paused&&playerBody != null){
					if(keycode == Input.Keys.LEFT)
						leftPressed = false;
					if(keycode == Input.Keys.RIGHT)
						rightPressed = false;
					if(keycode == Input.Keys.UP)
						upPressed = false;
					if(keycode == Input.Keys.DOWN)
						downPressed = false;
					if(!debug){
						if(keycode == Input.Keys.G){
							if(gravity){
								world.setGravity(new Vector2(0, 0));
								gravity = false;
							}
							else{
								world.setGravity(new Vector2(0, -100));
								gravity = true;
							}
						}
						if(keycode == Input.Keys.B)
							playerBody.setLinearVelocity(0, 0);
						if(keycode == Input.Keys.R)
							maze.turnAllRight();
						if(keycode == Input.Keys.L)
							maze.turnAllLeft();
					}
				}
				return false;
			}

			public boolean touchDown(int screenX, int screenY, int pointer, int button){
				if(!debug&&!paused&&playerBody != null){
					Vector3 position = camera.unproject(new Vector3(screenX,screenY,0));
					playerBody.setTransform(position.x, position.y, playerBody.getAngle());
				}
				return true;
			}
		});

		collisionSound = Gdx.audio.newSound(Gdx.files.internal("pew.wav"));

		world.setContactListener(new ContactListener(){

			public void beginContact(Contact contact){
				collisionSound.play((float) .2);
				Body bodyA = contact.getFixtureA().getBody();
				Body bodyB = contact.getFixtureB().getBody();
				Vector2 vel = playerBody.getLinearVelocity();
				if(vel.x != 0&&vel.y != 0){
					int deg = (int)(MathUtils.radiansToDegrees*bodyA.getAngle());
					if(vel.x > 0){
						if(vel.y > 0){
							if(deg == 0||deg == 180)
								spinPlayer(true);
							else
								spinPlayer(false);
						}
						else{
							if(deg == 90||deg == 270)
								spinPlayer(true);
							else
								spinPlayer(false);
						}
					}
					else{
						if(vel.y > 0){
							if(deg == 0||deg == 180)
								spinPlayer(false);
							else
								spinPlayer(true);
						}
						else{
							if(deg == 90||deg == 270)
								spinPlayer(false);
							else
								spinPlayer(true);
						}
					}
				}
				if(bodyB.getPosition().x > width||bodyB.getPosition().y > height)
					resetPosition = true;
			}

			/**
			 * In the absence of a native Adapter for the libgdx ContactListener interface,
			 * the following three methods are stubs for the default ContactListener implementation.
			 */
			public void endContact(Contact contact){
			}

			public void preSolve(Contact contact, Manifold oldManifold){
			}

			public void postSolve(Contact contact, ContactImpulse impulse){
			}
		});

		if(debug) debugRenderer = new Box2DDebugRenderer();

		createPlayer();
		maze = new Maze();

		displayLevel();
		maze.startWallMovement();
	}

	/**
	 * The libgdx rendering function, run every 60th of a second. Anything seen on<br>
	 * the screen is drawn here, according to the corresponding states of the physics<br>
	 * bodies being simulated in the corresponding simulation in the box2d world.<p>
	 * 
	 * The endpoints of the MazePiece walls had to be manually recalculated every 5<br>
	 * milliseconds during rotation so that they could simply be rendered here at all.<p>
	 * 
	 * Any modifications to body movement must be done while rendering. Elements in<br>
	 * the box2d simulation cannot be made asynchronously from the inherited render<br>
	 * calls that are constantly being run by both the box2d engine and its libgdx<br>
	 * rendition counterpart.
	 */
	public void render (){
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		tiledMapRenderer.setView(camera);
		tiledMapRenderer.render();
		sb.setProjectionMatrix(camera.combined);
		if(leftPressed)
			playerBody.applyLinearImpulse(-5, 0, playerBody.getPosition().x, playerBody.getPosition().y, true);
		if(rightPressed)
			playerBody.applyLinearImpulse(5, 0, playerBody.getPosition().x, playerBody.getPosition().y, true);
		if(upPressed)
			playerBody.applyLinearImpulse(0, 5, playerBody.getPosition().x, playerBody.getPosition().y, true);
		if(downPressed)
			playerBody.applyLinearImpulse(0, -5, playerBody.getPosition().x, playerBody.getPosition().y, true);
		ShapeRenderer shapeRenderer = new ShapeRenderer();
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(maze.mazeLevel.color);
		for(MazePiece mp : maze.pieces){
			shapeRenderer.line(mp.x, mp.y, mp.x2, mp.y2);
		}
		shapeRenderer.end();
		if(playerBody != null){
			Vector2 pPos = playerBody.getPosition();
			shapeRenderer.setColor(Color.WHITE);
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.circle(pPos.x, pPos.y, playerBodyScale);
			shapeRenderer.end();
		}
		sb.begin();
		if(displayLevel||keepDisplayLevel)
			font.draw(sb, "Level " + maze.mazeLevel.level, textX, textY);
		if(gameOver)
			font.draw(sb, "You beat the game!", textX, textY);
		if(soManyFail)
			font.draw(sb, "You failed, lel", textX-100, textY);
		for(MazePiece p : maze.pieces){
			if(p.setPos){
				p.setPos = false;
				p.piece.setTransform(p.x, p.y, p.desiredAngle);
			}
		}
		if(resetPosition){
			resetPosition = false;
			maze.mazeLevel.nextLevel();
		}
		if(playerBody != null){
			Sprite e = (Sprite)playerBody.getUserData();
			Vector2 pPos = playerBody.getPosition();
			e.setRotation(MathUtils.radiansToDegrees*playerBody.getAngle());
			e.setPosition(pPos.x-playerOffset, pPos.y-playerOffset);
			e.draw(sb);
		}
		sb.end();
		if(paused){
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(new Color(0, 0, 0, 0.5f));
			shapeRenderer.rect(0, 0, width, height);
			shapeRenderer.end();
			Gdx.gl.glDisable(GL20.GL_BLEND);
		}
		if(debug) debugRenderer.render(world, camera.combined);
		if(!paused) world.step(1/60f, 6, 2);
	}

	/**
	 * libgdx function called when the LWJGL window is closed,
	 * safely disposes certain resources being utilized by the game
	 */
	@Override
	public void dispose (){
		sb.dispose();
		texture.dispose();
		world.dispose();
		collisionSound.dispose();
		print("Disposing amazement");
	}

	/**
	 * libgdx method that is called when the system loses focus on the game
	 * window, calls our custom pause function if the game isn't already paused
	 */
	public void pause(){
		if(!paused) pauseGame();
	}

	/**
	 * Pause-toggle function. Starts and stops music playback automatically.
	 */
	private void pauseGame(){
		if(!paused){
			paused = true;
			clip.stop();
		}
		else{
			paused = false;
			clip.start();
			if(keepDisplayLevel)
				keepDisplayLevel = false;
		}
	}

	/**
	 * Creates the player entity/body, along with the resolution-appropriate texture.
	 */
	private void createPlayer(){
		if(lowRes)
			texture = new Texture(Gdx.files.internal("player24.png"));
		else
			texture = new Texture(Gdx.files.internal("player48.png"));
		playerSprite = new Sprite(texture);

		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(playerBodyScale);
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(50, 50);
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.fixedRotation = false;
		playerBody = world.createBody(bodyDef);
		FixtureDef boxFixtureDef = new FixtureDef();
		boxFixtureDef.shape = circleShape;
		boxFixtureDef.restitution = 0.75f;
		boxFixtureDef.density = 0f;
		playerBody.createFixture(boxFixtureDef);
		circleShape.dispose();
		playerBody.setUserData(playerSprite);
	}

	/**
	 * Displays the current-level label on the screen for 1.5 seconds --
	 * enables a flag for the renderer to display the level label,
	 * and schedules a task to disable the flag in 1500 milliseconds.
	 */
	private void displayLevel(){
		displayLevel = true;
		timer.schedule(new TimerTask(){

			@Override
			public void run(){
				if(paused)
					keepDisplayLevel = true;
				displayLevel = false;
			}}, 1500);
	}

	/**
	 * Spins the player in a given direction, with varying modifications, depending
	 * on the player's current angular velocity. Stays within a preset velocity range.
	 * @param dir true for counter-clockwise, false for clockwise.
	 */
	private void spinPlayer(boolean dir){
		float vel = playerBody.getAngularVelocity();
		double velMod = 0;
		if(Math.abs(vel) > 0)
			velMod++;
		else{
			if(Math.abs(vel) < 2)
				velMod+=1.5;
			else
				velMod+=3;
		}
		if(!dir)
			velMod*=-1;
		if(Math.abs(vel+velMod) <= 7)
			playerBody.setAngularVelocity(vel+(float)velMod);
	}

	/**
	 * The Maze class, contains all elements of the maze itself
	 */
	class Maze{

		MazeLevel mazeLevel = new MazeLevel(1);
		Array<MazePiece> pieces = new Array<MazePiece>();
		Timer timer = new Timer();

		/**
		 * Maze constructor; places the initial maze pieces and the static maze boundaries
		 */
		private Maze(){
			setPieces();
			for(int i = 1; i < 9; i++){
				createBody(i);
			}
		}

		/**
		 * Creates the in-game frame boundaries, and the outlying area
		 * in the upper-right corner that serves as the player's objective
		 * @param i The type of wall to create, distinguished by integers
		 */
		private void createBody(int i){
			EdgeShape lineShape = new EdgeShape();
			BodyDef lineBodyDef = new BodyDef();
			lineBodyDef.type = BodyType.KinematicBody;
			lineBodyDef.fixedRotation = false;
			Body lineBody = world.createBody(lineBodyDef);
			FixtureDef boxFixtureDef = new FixtureDef();
			if(i == 1) lineShape.set(0, height-1, width-100, height);
			else if(i == 2) lineShape.set(width, 0, width-1, height-100);
			else if(i == 3) lineShape.set(0, 0, width, 0);
			else if(i == 4) lineShape.set(0, 0, 0, height);
			else if(i == 5) lineShape.set(width-100, height+50, width+50, height+50);
			else if(i == 6) lineShape.set(width+50, height-100, width+50, height+50);
			else if(i == 7) lineShape.set(width-100, height, width-100, height+50);
			else if(i == 8) lineShape.set(width, height-100, width+50, height-100);
			boxFixtureDef.restitution = 0.75f;
			boxFixtureDef.shape = lineShape;
			lineBody.createFixture(boxFixtureDef);
			lineShape.dispose();
		}

		/**
		 * Turns all loaded maze pieces 90 degrees
		 * to the left, used for debugging
		 */
		private void turnAllLeft(){
			for(MazePiece p : pieces){
				p.rotateLeft();
			}
		}

		/**
		 * Turns all loaded maze pieces 90 degrees
		 * to the right, used for debugging
		 */
		private void turnAllRight(){
			for(MazePiece p : pieces){
				p.rotateRight();
			}
		}

		/**
		 * Resets -- literally destroys and recreates -- all maze pieces
		 * to the current specifications defined in the MazeLevel instance,
		 * and resets the timer object for a new iteration of tasks
		 * @param destroy Completely destroy the wall pieces, the timer,
		 * and remove the player body from the game
		 */
		private void resetAll(boolean destroy){
			for(MazePiece p : pieces){
				world.destroyBody(p.piece);
			}
			pieces.clear();
			timer.cancel();
			timer.purge();
			if(!destroy){
				timer = new Timer();
				setPieces();
			}
			else{
				timer = null;
				world.destroyBody(playerBody);
				playerBody = null;
				leftPressed = false;
				rightPressed = false;
				upPressed = false;
				downPressed = false;
			}
		}

		/**
		 * Finds the next angle of a given counter-clockwise turn of a unit circle
		 * @param deg Degrees to find subsequent angle of
		 * @return Degrees of subsequent angle that was found, or 0 if given invalid input
		 */
		private int findNext(int deg){
			switch(deg){
			case 0:
				return 90;
			case 90:
				return 180;
			case 180:
				return 270;
			}
			return 0;
		}

		/**
		 * Places the pieces of the maze. Support for specific layouts for any given level
		 * is in place, but not utilized -- see the commented blocks for example use. 
		 */
		private void setPieces(){
			int s = mazeLevel.gridSize;
			switch(mazeLevel.level){
			/*	// Able to use case queries to detect and set individual level layouts
			 *  case 1:
			 *		addMazePiece(5, 5, 90);
			 *		addMazePiece(5, 7, 270);
			 *		break;
			 *	case 2:
			 *		for(int r = 0; r <= s; r++){
			 * 			for(int c = 0; c <= s; c+=s){
			 *				if(r != s) addMazePiece(c, r, 90);
			 *			}
			 *		}
			 *		for(int r = 0; r <= s; r+=s){
			 *			for(int c = 0; c <= s; c++){
			 *				if(c != s) addMazePiece(c, r, 0);
			 *			}
			 *		}
			 *		break;
			 */
			/**
			 *  Use default case for any level that's not explicitly defined, or that doesn't break at the end.
			 */
			default:
				int last = 0;
				for(int r = 0; r <= s; r+=2){
					for(int c = 0; c <= s; c++){
						int next = findNext(last);
						addMazePiece(c, r, next);
						last = next;
					}
				}
			}
		}

		/**
		 * Uses current level and wall dimensions to calculate
		 * given "simple" grid coordinates into an actual
		 * location on the game screen, in raw coordinates.
		 * @param x Simplified X coordinate
		 * @param y Simplified Y coordinate
		 * @return A Vector2 of the raw gamespace coordinates
		 */
		private Vector2 snapToGrid(int x, int y){
			return new Vector2((x*mazeLevel.pieceLength)+mazeLevel.offset, (y*mazeLevel.pieceLength)+mazeLevel.offset);
		}

		/**
		 * Adds a MazePiece to the current level of the game.
		 * @param x Simplified X coordinate
		 * @param y Simplified Y coordinate
		 * @param degrees Orientation to start the wall at, in degrees.<br>
		 * Must be a positive, quarter-interval of a unit circle. 
		 */
		private void addMazePiece(int x, int y, int degrees){
			Vector2 vec = snapToGrid(x, y);
			pieces.add(new MazePiece((int)vec.x, (int)vec.y, degrees, x, y));
		}

		/**
		 * Tells each loaded wall piece to start its pseudo-randomized movement.
		 */
		private void startWallMovement(){
			for(MazePiece p : pieces)
				p.scheduleMovementTask();
		}

		/**
		 * The MazeLevel class, stores all of the information about the current
		 * level and the measurement specifications of the walls being used
		 */
		class MazeLevel{

			Array<Integer> rowDelay = new Array<Integer>(), timerIDs = new Array<Integer>();
			int level, offset, gridSize;
			int maxLevel = 6, speed = 1, pieceLength = 100, wallDecrement = 10;
			long rotationDelay;
			Color color = Color.BLACK;

			/**
			 * MazeLevel constructor, starts at given level and changes
			 * select attributes depending on the resolution being used,
			 * then sets the general level parameters
			 * @param level Initial level
			 */
			private MazeLevel(int level){
				this.level = level;
				if(lowRes){
					pieceLength/=2;
					wallDecrement/=2;
				}
				setParams();
			}

			/**
			 * Brings the game to the next level. Specifically, resetting the player's position,
			 * velocity, and rotation; incrementing the level and the wall speed; shrinking the wall sizes;
			 * updating the level parameters; alternating color between black and white; resetting the maze
			 * walls and their respective task handlers; and displaying the level label.<p>
			 * 
			 * If the max level has already been reached, and subsequently beaten,
			 * which it has been if this function is being called, ends the game
			 * and displays a message to the victorious player, whom at this point
			 * will be delirious with glee from a sudden, overwhelming release of dopamine<p>
			 * 
			 * ( ͡° ͜ʖ ͡°)
			 */
			private void nextLevel(){
				if(level != maxLevel){
					playerBody.setLinearVelocity(0, 0);
					playerBody.setTransform(50, 50, 0);
					playerBody.setAngularVelocity(0);

					level++;
					speed++;

					pieceLength-=wallDecrement;
					offset = width%pieceLength/2;
					print("New wall length: " + pieceLength + " with offset: " + offset);

					setParams();

					if(color == Color.BLACK) color = Color.WHITE;
					else color = Color.BLACK;

					resetAll(false);

					displayLevel();

					startWallMovement();
				}
				else{
					resetAll(true);
					gameOver = true;
					font.setScale(2);
				}
			}

			/**
			 * Calculates the wall-movement delay, based on the max level,
			 * current level, and the duration of the song being played
			 * @return The movement delay, in seconds
			 */
			private double findMovementDelay(){
				return (((((maxLevel+1)-level)/songDuration)*1000)/3);
			}

			/**
			 *  Updates/sets the wall-grid base offsets, the grid-size itself,
			 *  prints information about the level being created, and creates
			 *  the random delays between spinning-cycles for each row of walls
			 */
			private void setParams(){
				rotationDelay = (long)(findMovementDelay()*1000);
				offset = width%pieceLength/2;
				gridSize = (width-(width%pieceLength))/pieceLength;
				print("Playing level " + level + ", grid size: " + gridSize + "x" + gridSize + ", offset: " + offset + ", movement delay: " + findMovementDelay());

				int roundedDelay = (int)(Math.round(rotationDelay));
				for(int i = 0; i <= gridSize; i++){
					Random rand = new Random();
					int randomNum = rand.nextInt(roundedDelay) + 1;
					if(rowDelay.size > i) rowDelay.set(i, randomNum);
					else rowDelay.add(randomNum);
				}
			}
		}

		/**
		 * The MazePiece class. Handles an individual wall segment and its corresponding timer.
		 */
		class MazePiece{

			/**
			 * The box2d body used to simulate the wall itself
			 */
			private Body piece;

			int x, y, x2, y2, row, col;
			int degrees = 0;

			boolean isMoving = false, setPos = false, checkValid = false,
					valid0 = true, valid90 = true, valid180 = true, valid270 = true;

			float desiredAngle;

			/**
			 * Constructs a new MazePiece at the given coordinates, with the given orientation.<br>
			 * Takes in row and column data out of convenience; more efficient than recalculating.
			 * @param x Raw X location in-game
			 * @param y Raw Y location in-game
			 * @param degrees Initial orientation, in degrees
			 * @param row The given row location
			 * @param col The given column location
			 */
			private MazePiece(int x, int y, int degrees, int row, int col){
				int size = mazeLevel.pieceLength;
				int gridSize = mazeLevel.gridSize;
				this.x = x;
				this.y = y;
				x2 = x;
				y2 = y;
				this.row = row;
				this.col = col;
				/**
				 * Prevents walls that are adjacent to the edge of the screen
				 * from being able to turn in a direction that would end
				 * up with the piece off-screen.
				 */
				if(row == 0||row == gridSize||col == 0||col == gridSize){
					if(row == 0){
						valid180 = false;
					}
					if(row == gridSize){
						valid0 = false;
					}
					if(col == 0){
						valid270 = false;
					}
					if(col == gridSize){
						valid90 = false;
					}
					checkValid = true;
				}
				EdgeShape lineShape = new EdgeShape();
				BodyDef lineBodyDef = new BodyDef();
				lineBodyDef.type = BodyType.KinematicBody;
				lineBodyDef.fixedRotation = false;
				lineBodyDef.position.set(x, y);
				piece = world.createBody(lineBodyDef);
				FixtureDef lineFixtureDef = new FixtureDef();
				lineShape.set(0, 0, size, 0);
				if(degrees == 0) x2+=size;
				else if(degrees == 90){
					modifyDegrees(90, false);
					y2+=size;
				}
				else if(degrees == 180){
					modifyDegrees(180, false);
					x2-=size;
				}
				else if(degrees == 270){
					modifyDegrees(270, false);
					y2-=size;
				}
				lineFixtureDef.shape = lineShape;
				piece.createFixture(lineFixtureDef);
				lineShape.dispose();
			}

			/**
			 * Performs rotation for a given wall, provided that the action is valid
			 * @param mod Amount of degrees to turn wall. Must be a quarter-interval<br>
			 * of a unit circle, between 360 and -360, inclusive.
			 * @param animate Whether to animate the rotation or not.
			 */
			private void modifyDegrees(int mod, boolean animate){
				int originalDegrees = degrees;
				degrees+=mod;
				if(degrees < 0)
					degrees+=360;
				else if(degrees >= 360)
					degrees%=360;
				if(checkValid){
					if((degrees == 0&&!valid0)||(degrees == 90&&!valid90)||(degrees == 180&&!valid180)||(degrees == 270&&!valid270)){
						degrees = originalDegrees;
						isMoving = false;
						return;
					}
				}
				float desiredAngle = piece.getAngle() + (MathUtils.degreesToRadians * mod);
				if(animate){
					if(mod > 0){
						piece.setAngularVelocity(mazeLevel.speed);
						scheduleAngleTask(desiredAngle, true);
					}
					else if(mod < 0){
						piece.setAngularVelocity(-mazeLevel.speed);
						scheduleAngleTask(desiredAngle, false);
					}
				}
				else piece.setTransform(piece.getPosition(), desiredAngle);
			}

			/**
			 * Rotates a maze piece to the left, if it's not already in motion.
			 */
			private void rotateLeft(){
				if(!isMoving) modifyDegrees(90, true);
			}

			/**
			 * Rotates the maze piece to the right, if it's not already in motion.
			 */
			private void rotateRight(){
				if(!isMoving) modifyDegrees(-90, true);
			}

			/**
			 * Creates the JRE-handled timer that chooses a random direction
			 * for the maze piece to move in, at a row-dependent offset delay
			 * and at a song/level dependent frequency. This function schedules,
			 * a recurring task, and only needs to be called once per level.
			 */
			private void scheduleMovementTask(){
				timer.scheduleAtFixedRate(new TimerTask(){
					public void run(){
						if(Math.random() < 0.5)
							rotateLeft();
						else
							rotateRight();
					}
				}, mazeLevel.rowDelay.get(col), mazeLevel.rotationDelay);
			}

			/**
			 * Schedules an AngleTask to run in 5 milliseconds.
			 * @param desiredAngle Angle that is trying to be reached.
			 * @param direction The direction that the piece is turning in.<br>
			 * true for counter-clockwise, false for clockwise.
			 */
			private void scheduleAngleTask(float desiredAngle, boolean direction){
				isMoving = true;
				timer.schedule(new AngleTask(desiredAngle, direction), 5);
			}

			/**
			 * The custom TimerTask that checks if the piece in motion has
			 * reached its desired angle yet. If it has, it adjusts the
			 * final location of the wall piece in case any JRE calculations
			 * are slightly off. If it hasn't, it schedules itself to run
			 * again in 5 milliseconds.
			 */
			class AngleTask extends TimerTask{

				private boolean direction;

				public AngleTask(float newDesiredAngle, boolean direction){
					desiredAngle = newDesiredAngle;
					this.direction = direction;
				}

				/**
				 * Manually calculates the exact position and rotation of
				 * both points of the wall segment.
				 */
				@Override
				public void run(){
					float angle = piece.getAngle();
					int size = mazeLevel.pieceLength;
					x2 = (int)(x + size * Math.cos(angle));
					y2 = (int)(y + size * Math.sin(angle));
					if((angle <= desiredAngle&&direction)||(angle >= desiredAngle&&!direction)) scheduleAngleTask(desiredAngle, direction);
					else{
						piece.setAngularVelocity(0);
						setPos = true;
						if(degrees == 180||degrees == 0){
							y2 = (int)y;
							if(degrees == 0)
								x2++;
						}
						else
							x2 = (int)x;
						if(degrees == 90)
							y2++;
						isMoving = false;
					}
				}
			}
		}
	}
}