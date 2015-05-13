package com.gmail.jackbcousineau;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import sun.security.krb5.Config;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Screen;
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
import com.badlogic.gdx.math.Affine2;
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
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.gmail.jackbcousineau.AmazementMain.Maze.MazePiece;
//import com.gmail.jackbcousineau.desktop.DesktopLauncher;


public class AmazementMain extends Game implements InputProcessor{

	//DesktopLauncher desktopLauncher;

	int height, width, playerBodyScale = 23, playerSpriteScale = 48, playerOffset = 24, textX = 400, textY = 550;

	@Override
	public void dispose (){
		//Gdx.graphics.setTitle("amazement");
		sb.dispose();
		texture.dispose();
		world.dispose();
		collisionSound.dispose();
		print("Disposing amazement");
		//desktopLauncher.gameRunning = false;
	}

	class EdgeBody{

		public Body north, east, south, west, northOuter, eastOuter, westOuter, southOuter;

		public EdgeBody(){
			north = createBody(1);
			east = createBody(2);
			south = createBody(3);
			west = createBody(4);
			northOuter = createBody(5);
			eastOuter = createBody(6);
			westOuter = createBody(7);
			southOuter = createBody(8);
		}

		private Body createBody(int i){
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
			//boxFixtureDef.restitution = 100f;
			//boxFixtureDef.density = 2.0f;
			boxFixtureDef.shape = lineShape;
			lineBody.createFixture(boxFixtureDef);
			lineShape.dispose();
			return lineBody;
		}

	}

	class Maze{

		class MazeLevel {

			Array<Integer> rowDelay = new Array<Integer>(), timerIDs = new Array<Integer>();
			int level, maxLevel = 5, speed = 1, pieceLength = 100, offset, gridSize;
			Color color;

			public MazeLevel(int level, Color color){
				this.level = level;
				this.color = color;
				if(lowRes) pieceLength/=2;
				setParams();
			}

			public void nextLevel(){
				playerBody.setLinearVelocity(0, 0);
				playerBody.setTransform(50, 50, playerBody.getAngle());
				level++;
				//speed++;
				mazeLevel.shrinkWall(10);
				setParams();
				if(color == Color.BLACK) color = Color.WHITE;
				else color = Color.BLACK;
				resetAll();
				displayLevel();
				for(MazePiece p : pieces){
					p.scheduleMovementTask();
				}
			}

			private void setParams(){
				offset = width%pieceLength/2;
				gridSize = (width-(width%pieceLength))/pieceLength;
				if(rowDelay.size == 0) rowDelay.add(gridSize);
				else rowDelay.set(0, gridSize);
				print("Playing level " + level + ", grid size: " + gridSize + "x" + gridSize + ", offset: " + offset);
				setRowDelay();
			}

			public void shrinkWall(int lengthToSubtract){
				pieceLength-=lengthToSubtract;
				offset = width%pieceLength/2;
				print("New length: " + pieceLength + ", offset: " + offset);
			}

			public void setRowDelay(){
				for(int i = 1; i <= rowDelay.get(0); i++){
					Random rand = new Random();
					int randomNum = rand.nextInt((1000 - 1) + 1) + 1;
					if(rowDelay.size > i) rowDelay.set(i, randomNum);
					else rowDelay.add(randomNum);
					//print("Setting delay for row: " + i + ", delay is: " + rowDelay.get(i));
				}
				//print("Set row delays for " + rowDelay.get(0) + " rows");
			}
		}

		MazeLevel mazeLevel = new MazeLevel(1, Color.BLACK);

		Array<MazePiece> pieces = new Array<MazePiece>();
		Timer timer = new Timer();

		public void turnAll(){
			print("TURNING ALL");
			for(MazePiece p : pieces){
				p.rotateLeft();
			}
		}

		public void turnAllLeft(){
			for(MazePiece p : pieces){
				p.rotateLeft();
			}
		}

		public void turnAllRight(){
			for(MazePiece p : pieces){
				p.rotateRight();
			}
		}

		public void resetAll(){
			for(MazePiece p : pieces){
				world.destroyBody(p.piece);
			}
			pieces.clear();
			timer.cancel();
			timer.purge();
			timer = new Timer();
			setPieces(mazeLevel.level);
		}

		public Maze(){
			setPieces(mazeLevel.level);
		}

		private void setPieces(int level){
			switch(level){
			case 1:
				//addMazePiece(0, 0, 0);
				int s = mazeLevel.gridSize;
				for(int r = 0; r <= s; r++){
					for(int c = 0; c <= s; c+=s){
						//if(!(r == s&&c == s))
						addMazePiece(r, c, 0);
					}
				}
				for(int r = 0; r <= s; r+=s){
					for(int c = 0; c <= s; c++){
						//if(c != 10&&r != 10)
						addMazePiece(r, c, 90);
					}
				}
				addMazePiece(5, 5, 90);
				/*for(int l = 0; l < s; l++){
					addMazePiece(l, 10, 0);
					addMazePiece(0, l, 90);
					addMazePiece(l+1, 0, 180);
					addMazePiece(10, l+1, 270);
				}*/
				//addMazePie
				//addMazePiece(2, 5, 0);
				/*addMazePiece(3, 5, 90);
				addMazePiece(4, 5, 180);
				addMazePiece(5, 5, 270);
				addMazePiece(7, 5, 0);
				addMazePiece(7, 5, 90);
				addMazePiece(7, 5, 180);
				addMazePiece(8, 5, 0);
				addMazePiece(10, 5, 180);
				addMazePiece(2, 3, 0);
				addMazePiece(3, 3, 90);
				addMazePiece(4, 3, 180);
				addMazePiece(5, 3, 270);
				addMazePiece(7, 3, 0);
				addMazePiece(7, 3, 90);
				addMazePiece(7, 3, 180);*/
				break;
			case 2:
				addMazePiece(2, 5, 0);
				addMazePiece(3, 5, 90);
				addMazePiece(4, 5, 180);
				addMazePiece(5, 5, 270);
				addMazePiece(7, 5, 0);
				addMazePiece(7, 5, 90);
				addMazePiece(7, 5, 180);
				addMazePiece(8, 5, 0);
				addMazePiece(10, 5, 180);
				break;
			default:
				addMazePiece(2, 5, 0);
				addMazePiece(3, 5, 90);
				addMazePiece(4, 5, 180);
				addMazePiece(5, 5, 270);
				addMazePiece(7, 5, 0);
				addMazePiece(7, 5, 90);
				addMazePiece(7, 5, 180);
				addMazePiece(8, 5, 0);
				addMazePiece(10, 5, 180);
			}
		}

		private Vector2 snapToGrid(int x, int y){
			return new Vector2((x*mazeLevel.pieceLength)+mazeLevel.offset, (y*mazeLevel.pieceLength)+mazeLevel.offset);
		}

		private void addMazePiece(int x, int y, int degrees){
			Vector2 vec = snapToGrid(x, y);
			pieces.add(new MazePiece((int)vec.x, (int)vec.y, degrees, x, y));
		}

		class MazePiece{

			public Body piece;

			int x, y, x2, y2, degrees = 0, row, col;

			boolean isMoving = false, setPos = false, checkValid = false, valid0 = true, valid90 = true, valid180 = true, valid270 = true;

			float desiredAngle;

			public MazePiece(int x, int y, int degrees, int row, int col){
				int size = mazeLevel.pieceLength;
				int gridSize = mazeLevel.gridSize;
				this.x = x;
				this.y = y;
				x2 = x;
				y2 = y;
				this.row = row;
				this.col = col;
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
				//boxFixtureDef.restitution = 0.75f;
				//boxFixtureDef.density = 2.0f;
				lineFixtureDef.shape = lineShape;
				piece.createFixture(lineFixtureDef);
				lineShape.dispose();
				//print("x1: " + x + ", y1: " + y + ", x2: " + x2 + ", y2: " + y2);
			}

			public void modifyDegrees(int mod, boolean rotate){
				boolean stop = false;
				int originalDegrees = degrees;
				degrees+=mod;
				if(degrees < 0) degrees+=360;
				else if(degrees >= 360) degrees%=360;
				if(checkValid){
					if((degrees == 0&&!valid0)||(degrees == 90&&!valid90)||(degrees == 180&&!valid180)||(degrees == 270&&!valid270)){
						degrees = originalDegrees;
						isMoving = false;
						stop = true;
						return;
					}
				}
				if(stop) print("This should not run");
				float desiredAngle = piece.getAngle() + (MathUtils.degreesToRadians * mod);
				if(rotate){
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

			public void rotateLeft(){
				if(!isMoving) modifyDegrees(90, true);
			}

			public void rotateRight(){
				if(!isMoving) modifyDegrees(-90, true);
			}

			private void scheduleAngleTask(float desiredAngle, boolean direction){
				isMoving = true;
				timer.schedule(new AngleTask(desiredAngle, direction), 5);
			}

			public void scheduleMovementTask(){
				//print("Scheduling wall movement task");
				//timer.scheduleAtFixedRate(new MovementTimer(), mazeLevel.rowDelay.get(col), 3000);
				timer.scheduleAtFixedRate(new TimerTask(){
					public void run() {
						if(Math.random() < 0.5){
							rotateLeft();
						}
						else rotateRight();
					}
				}, mazeLevel.rowDelay.get(col), 3000);
				//timer.schedule(new MovementTimer(), mazeLevel.rowDelay.get(col));
			}

			/*class MovementTimer extends TimerTask{

				@Override
				public void run() {
					if(Math.random() < 0.5){
						rotateLeft();
					}
					else rotateRight();
				}
			}*/

			class AngleTask extends TimerTask{

				private boolean direction;

				public AngleTask(float newDesiredAngle, boolean direction){
					desiredAngle = newDesiredAngle;
					this.direction = direction;
				}

				@Override
				public void run() {
					float angle = piece.getAngle();
					int size = mazeLevel.pieceLength;
					x2 = (int)(x + size * Math.cos(angle));
					y2 = (int)(y + size * Math.sin(angle));
					if((angle <= desiredAngle&&direction)||(angle >= desiredAngle&&!direction)) scheduleAngleTask(desiredAngle, direction);
					else{
						piece.setAngularVelocity(0);
						//piece.setTransform(x, y, desiredAngle);
						setPos = true;
						if(degrees == 180||degrees == 0){
							y2 = (int)y;
							if(degrees == 0) x2++;
						}
						else x2 = (int)x;
						if(degrees == 90) y2++;
						//print("x: " + pos.x + ", y: " + pos.y + ", x2: " + x2 + ", y2: " + y2 + ", degrees: " + degrees);
						isMoving = false;
					}
				}
			}
		}
	}

	private void print(String string){
		System.out.println(string);
	}

	Texture img;
	TiledMap tiledMap;
	OrthographicCamera camera;
	TiledMapRenderer tiledMapRenderer;
	SpriteBatch sb;
	Texture texture;
	Sprite playerSprite;

	boolean leftPressed = false, rightPressed = false, upPressed = false, downPressed = false, gravity = false, drawCollision = false, resetPosition = false, displayLevel = false, keepDisplayLevel = false, lowRes = false, paused = false;

	World world;
	Box2DDebugRenderer debugRenderer;
	Body playerBody, objectiveBody;
	EdgeBody edgeBody;
	Array<Body> bodies = new Array<Body>();
	Maze maze;
	Sound collisionSound;

	//ParticleEffect collisionEffect;

	Timer timer;

	BitmapFont font;

	Clip clip;

	public AmazementMain(int height, int width){
		this.height = height;
		this.width = width;
	}

	/*public AmazementMain(int height, int width, DesktopLauncher desktopLauncher){
		this.height = height;
		this.width = width;
		this.desktopLauncher = desktopLauncher;
	}*/

	private void displayLevel(){
		displayLevel = true;
		timer.schedule(new TimerTask(){

			@Override
			public void run() {
				if(paused) keepDisplayLevel = true;
				displayLevel = false;
			}}, 1500);
	}

	private void createPlayer(){
		if(lowRes) texture = new Texture(Gdx.files.internal("player24.png"));
		else texture = new Texture(Gdx.files.internal("player48.png"));
		//if(width == 512) texture.
		playerSprite = new Sprite(texture);

		//PolygonShape boxShape = new PolygonShape();
		CircleShape circleShape = new CircleShape();
		//boxShape.setAsBox(20, 20);
		circleShape.setRadius(playerBodyScale);
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(50, 50);
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.fixedRotation = false;
		playerBody = world.createBody(bodyDef);
		FixtureDef boxFixtureDef = new FixtureDef();
		//boxFixtureDef.shape = boxShape;
		boxFixtureDef.shape = circleShape;
		boxFixtureDef.restitution = 0.75f;
		boxFixtureDef.density = 0f;
		playerBody.createFixture(boxFixtureDef);
		//boxShape.dispose();
		circleShape.dispose();
		playerBody.setUserData(playerSprite);
		//playerBody.setAngularVelocity(1);
	}

	private void spinPlayer(boolean dir){
		float vel = playerBody.getAngularVelocity();
		double velMod = 0;
		if(dir){
			if(vel > 0) velMod++;
			else{
				//print(vel + "");
				if(vel > -2) velMod+=1.5;//playerBody.setAngularVelocity(vel*-1);
				else velMod=+3;
			}
		}
		else{
			if(vel > 0){
				if(vel < 2) velMod-=1.5;//playerBody.setAngularVelocity(vel*-1);
				else velMod=-3;
			}
			else velMod--;
		}
		if(Math.abs(vel+velMod) <= 7) playerBody.setAngularVelocity(vel+(float)velMod);
		//print(playerBody.getAngularVelocity() + "");
		//playerBody.setAngularVelocity(playerBody.getAngularVelocity()*-1);
	}

	public class PauseScreen implements Screen{

		@Override
		public void show() {
		}

		@Override
		public void render(float delta) {
		}

		@Override
		public void resize(int width, int height) {
		}

		@Override
		public void pause() {
		}

		@Override
		public void resume() {
		}

		@Override
		public void hide() {
		}

		@Override
		public void dispose() {
		}

	}

	@Override public void pause(){
		if(!paused){
			//print("System paused game");
			paused = true;
			clip.stop();
		}
	}

	@Override public void resume(){
		//print("RESUMED FOCUS");
	}

	private void pauseGame(){
		if(!paused){
			//print("User paused game");
			paused = true;
			//if(displayLevel) keepDisplayLevel = true;
			clip.stop();
		}
		else{
			//print("User unpaused game");
			paused = false;
			clip.start();
			if(keepDisplayLevel) keepDisplayLevel = false;
		}
	}

	@Override public void create () {
		//print("Creating amazement");
		//
		Box2D.init();
		//if(!firstRun)
		//if(setSize)
		Gdx.graphics.setDisplayMode(width, height, false);
		Gdx.graphics.setTitle("Amazement");
		//height = Gdx.graphics.getHeight();
		//width = Gdx.graphics.getWidth();

		font = new BitmapFont();

		if(width == 512){
			lowRes = true;
			playerBodyScale = 12;
			//playerSpriteScale = 24;
			playerOffset = 12;
			textX = 190;
			textY = 290;
			font.setScale(3);
			tiledMap = new TmxMapLoader().load("map512.tmx");
		}
		else{
			font.setScale(5);
			tiledMap = new TmxMapLoader().load("map1024.tmx");
		}

		try {
			clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream("/Users/Jack/Satisfaction.wav"))));
			clip.start(); 
			print("Buffer size: " + clip.getBufferSize());
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		camera = new OrthographicCamera();
		camera.setToOrtho(false,width,height);
		camera.update();
		//if(Gdx.graphics.getWidth() == 512) tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1/2);
		tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
		//tiledMapRenderer.setView(camera.combined, width, height, width, height);
		Gdx.input.setInputProcessor(this);
		world = new World(new Vector2(0, 0), false); 
		debugRenderer = new Box2DDebugRenderer();
		timer = new Timer();

		collisionSound = Gdx.audio.newSound(Gdx.files.internal("pew.wav"));

		world.setContactListener(new ContactListener() {
			@Override
			public void beginContact(Contact contact) {
				//print(playerBody.getAngle() + "");
				//playerBody.applyAngularImpulse(50, false);
				//playerBody.applyTorque(50, true);
				//playerBody.setAngularVelocity(1);
				//playerBody.
				collisionSound.play();
				//print("CONTACT");
				Body a=contact.getFixtureA().getBody();
				Body b=contact.getFixtureB().getBody();
				//for (int i = 0; i < contact.getWorldManifold().getNumberOfContactPoints(); i++) {
				float x = contact.getWorldManifold().getPoints()[0].x;
				float y = contact.getWorldManifold().getPoints()[0].y;
				//print("Post: " + playerBody.getLinearVelocity());
				Vector2 vel = playerBody.getLinearVelocity();
				if(vel.x != 0&&vel.y != 0){
					int deg = (int) (MathUtils.radiansToDegrees*a.getAngle());
					if(vel.x > 0){
						if(vel.y > 0){
							if(deg == 0||deg == 180) spinPlayer(true);
							else spinPlayer(false);
						}
						else{
							if(deg == 90||deg == 270) spinPlayer(true);
							else spinPlayer(false);
						}
					}
					else{
						if(vel.y > 0){
							if(deg == 0||deg == 180) spinPlayer(false);
							else spinPlayer(true);
						}
						else{
							if(deg == 90||deg == 270) spinPlayer(false);
							else spinPlayer(true);
						}
					}
				}
				if(b.getPosition().x > width||b.getPosition().y > height){
					//sprite.setPosition(500, 500);
					resetPosition = true;
				}
				/*drawCollision = true;
				timer.schedule(new TimerTask(){

					@Override
					public void run() {
						print("CANCELLING");
						drawCollision = false;
					}}, 100);*/
			}

			@Override
			public void endContact(Contact contact) {
				//crumpit96 or trumpet96
			}

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
				//Vector2 vec = playerBody.getLinearVelocityFromWorldPoint(new Vector2(0, 0));
				//print("Pre: " + playerBody.getLinearVelocity());
				//print(vec + "");
			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
			}
		});

		sb = new SpriteBatch();

		createPlayer();
		edgeBody = new EdgeBody();
		maze = new Maze();

		//ParticleEffectPool effectPool;
		//Array<PooledEffect> effects = new Array<PooledEffect>();

		//collisionEffect = new ParticleEffect();
		//collisionEffect.load(Gdx.files.internal("collision_particle"), Gdx.files.internal(""));
		//collisionEffect.setPosition(950, 950);
		world.getBodies(bodies);
		displayLevel();
		for(MazePiece p : maze.pieces){
			//p.scheduleMovementTask();
		}
		//Display.
	}

	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		tiledMapRenderer.setView(camera);
		tiledMapRenderer.render();
		sb.setProjectionMatrix(camera.combined);
		if(leftPressed) playerBody.applyLinearImpulse(-5, 0, playerBody.getPosition().x, playerBody.getPosition().y, true);
		if(rightPressed) playerBody.applyLinearImpulse(5, 0, playerBody.getPosition().x, playerBody.getPosition().y, true);
		if(upPressed) playerBody.applyLinearImpulse(0, 5, playerBody.getPosition().x, playerBody.getPosition().y, true);
		if(downPressed) playerBody.applyLinearImpulse(0, -5, playerBody.getPosition().x, playerBody.getPosition().y, true);
		Vector2 pPos = playerBody.getPosition();
		Sprite e = (Sprite)playerBody.getUserData();
		ShapeRenderer shapeRenderer = new ShapeRenderer();
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(maze.mazeLevel.color);
		for(MazePiece mp : maze.pieces){
			shapeRenderer.line(mp.x, mp.y, mp.x2, mp.y2);
		}
		shapeRenderer.end();
		shapeRenderer.setColor(Color.WHITE);
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.circle(pPos.x, pPos.y, playerBodyScale);
		shapeRenderer.end();
		sb.begin();
		if(displayLevel||keepDisplayLevel) font.draw(sb, "Level " + maze.mazeLevel.level, textX, textY);
		//sprite.draw(sb);
		// I did not take a look at implementation but you get the idea
		//sprite.setPosition(x, y); = body.localVector.x;
		//sprite.y = body.localVector.y;
		/*for (Body b : bodies) {
			// Get the body's user data - in this example, our user 
			// data is an instance of the Entity class
			Sprite e = (Sprite) b.getUserData();

			if (e != null) {
				// Update the entities/sprites position and angle
				e.setPosition(b.getPosition().x-30, b.getPosition().y-24);
				// We need to convert our angle from radians to degrees
				e.setRotation(MathUtils.radiansToDegrees * b.getAngle());
				//e.draw(sb);
			}
		}*/
		//if(drawCollision){
		//collisionEffect.setPosition(body.getPosition().x, body.getPosition().y);
		//collisionEffect.draw(sb, Gdx.graphics.getDeltaTime());
		//}
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
		e.setRotation(MathUtils.radiansToDegrees*playerBody.getAngle());
		e.setPosition(pPos.x-playerOffset, pPos.y-playerOffset);
		e.draw(sb);
		sb.end();
		if(paused){
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			//shapeRenderer.setProjectionMatrix(camera.combined);
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(new Color(0, 0, 0, 0.5f));
			//shapeRenderer.setColor(Color.GRAY);
			shapeRenderer.rect(0, 0, width, height);
			shapeRenderer.end();
			Gdx.gl.glDisable(GL20.GL_BLEND);
			//font.draw(sb, "Paused", textX, textY);
		}
		//debugRenderer.render(world, camera.combined);
		if(!paused) world.step(1/60f, 6, 2);
		//world.step(Gdx.graphics.getDeltaTime(), 8, 3);
	}

	@Override
	public boolean keyDown(int keycode) {
		if(!paused){
			if(keycode == Input.Keys.LEFT)
				leftPressed = true;
			//body.applyLinearImpulse(-5, 0, body.getPosition().x, body.getPosition().y, true);
			if(keycode == Input.Keys.RIGHT)
				rightPressed = true;
			//body.applyLinearImpulse(5, 0, body.getPosition().x, body.getPosition().y, true);
			if(keycode == Input.Keys.UP)
				upPressed = true;
			//body.applyLinearImpulse(0, 5, body.getPosition().x, body.getPosition().y, true);
			if(keycode == Input.Keys.DOWN)
				downPressed = true;
			//body.applyLinearImpulse(0, -5, body.getPosition().x, body.getPosition().y, true);
		}
		if(keycode == Input.Keys.ESCAPE)
			pauseGame();
		if(keycode == Input.Keys.S){
			//Thread t = new Thread(new AudioHandler("/Users/Jack/Satisfaction.wav"));
			//t.run();
			if(clip.isActive()){
				print("Music playing, stopping now");
				clip.stop();
			}
			else{
				print("Music not playing, playing now");
				clip.start();
			}
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override public boolean keyUp(int keycode) {
		if(!paused){
			if(keycode == Input.Keys.LEFT) leftPressed = false;
			if(keycode == Input.Keys.RIGHT) rightPressed = false;
			if(keycode == Input.Keys.UP) upPressed = false;
			if(keycode == Input.Keys.DOWN) downPressed = false;
			if(keycode == Input.Keys.NUM_1)
				tiledMap.getLayers().get(0).setVisible(!tiledMap.getLayers().get(0).isVisible());
			if(keycode == Input.Keys.NUM_2)
				tiledMap.getLayers().get(1).setVisible(!tiledMap.getLayers().get(1).isVisible());
			if(keycode == Input.Keys.B){
				playerBody.setLinearVelocity(0, 0);
			}
			if(keycode == Input.Keys.G)
				if(gravity){
					world.setGravity(new Vector2(0, 0));
					gravity = false;
				}
				else{
					world.setGravity(new Vector2(0, -100));
					gravity = true;
				}
			if(keycode == Input.Keys.R)
				maze.turnAllRight();
			if(keycode == Input.Keys.L)
				maze.turnAllLeft();
		}
		return false;
	}

	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if(!paused){
			Vector3 clickCoordinates = new Vector3(screenX,screenY,0);
			Vector3 position = camera.unproject(clickCoordinates);
			playerSprite.setPosition(position.x, position.y);
			playerBody.setTransform(position.x, position.y, playerBody.getAngle());
		}
		return true;
	}

	@Override public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override public boolean scrolled(int amount) {
		return false;
	}

}
/*SpriteBatch batch;
	Texture img;
	Sprite sprite;

	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
		sprite = new Sprite(img, 0, 0, 200, 200);
		sprite.setPosition(10, 10);
		sprite.setRotation(45);
		sprite.set
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		//batch.draw(img, 0, 0);
		sprite.draw(batch);
		batch.end();
	}
}
 */