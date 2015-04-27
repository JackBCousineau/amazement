package com.gmail.jackbcousineau;

import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
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


public class AmazementMain extends ApplicationAdapter implements InputProcessor{

	@Override
	public void dispose (){
		sb.dispose();
		texture.dispose();
		world.dispose();
		collisionSound.dispose();
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
			int height = Gdx.graphics.getHeight(), width = Gdx.graphics.getWidth();
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

			int level, speed = 1;
			Color color;

			public MazeLevel(int level, Color color){
				this.level = level;
				this.color = color;
				print("Playing level " + level);
			}

			public void nextLevel(){
				playerBody.setLinearVelocity(0, 0);
				playerBody.setTransform(50, 50, playerBody.getAngle());
				level++;
				speed++;
				print("Playing level " + level);
				if(color == Color.BLACK) color = Color.WHITE;
				else color = Color.BLACK;
				resetAll();
				displayLevel();
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
			setPieces();
		}

		public Maze(){
			setPieces();
		}

		private void setPieces(){
			/*addMazePiece(250, 500, 0);
			addMazePiece(350, 500, 90);
			addMazePiece(450, 500, 180);
			addMazePiece(550, 500, 270);
			addMazePiece(750, 500, 0);
			addMazePiece(750, 500, 90);
			addMazePiece(750, 500, 180);*/
			//addMazePiece(750, 500, 270);
			//addMazePiece(1014, 500, 180);
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
		
		private Vector2 snapToGrid(int x, int y){
			return new Vector2((x*100)+14, (y*100)+14);
		}

		private void addMazePiece(int x, int y, int degrees){
			Vector2 vec = snapToGrid(x, y);
			pieces.add(new MazePiece((int)vec.x, (int)vec.y, degrees));
		}

		class MazePiece{

			public Body piece;

			int x, y, x2, y2, degrees = 0;

			boolean isMoving = false, setPos = false;
			
			float desiredAngle;

			public MazePiece(int x, int y, int degrees){
				this.x = x;
				this.y = y;
				x2 = x;
				y2 = y;
				EdgeShape lineShape = new EdgeShape();
				BodyDef lineBodyDef = new BodyDef();
				lineBodyDef.type = BodyType.KinematicBody;
				lineBodyDef.fixedRotation = false;
				lineBodyDef.position.set(x, y);
				piece = world.createBody(lineBodyDef);
				FixtureDef lineFixtureDef = new FixtureDef();
				lineShape.set(0, 0, 100, 0);
				if(degrees == 0) x2+=100;
				else if(degrees == 90){
					modifyDegrees(90, false);
					y2+=100;
				}
				else if(degrees == 180){
					modifyDegrees(180, false);
					x2-=100;
				}
				else if(degrees == 270){
					modifyDegrees(270, false);
					y2-=100;
				}
				//boxFixtureDef.restitution = 0.75f;
				//boxFixtureDef.density = 2.0f;
				lineFixtureDef.shape = lineShape;
				piece.createFixture(lineFixtureDef);
				lineShape.dispose();
				//print("x1: " + x + ", y1: " + y + ", x2: " + x2 + ", y2: " + y2);
			}

			public void modifyDegrees(int mod, boolean rotate){
				degrees+=mod;
				if(degrees < 0) degrees+=360;
				else if(degrees >= 360) degrees%=360;
				float desiredAngle = piece.getAngle() + (MathUtils.degreesToRadians * mod);
				if(rotate){
					if(mod > 0){
						piece.setAngularVelocity(mazeLevel.speed);
						scheduleTask(desiredAngle, true);
					}
					else if(mod < 0){
						piece.setAngularVelocity(-mazeLevel.speed);
						scheduleTask(desiredAngle, false);
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

			private void scheduleTask(float desiredAngle, boolean direction){
				isMoving = true;
				timer.schedule(new AngleTask(desiredAngle, direction), 5);
			}

			class AngleTask extends TimerTask{

				private boolean direction;

				public AngleTask(float newDesiredAngle, boolean direction){
					desiredAngle = newDesiredAngle;
					this.direction = direction;
				}

				@Override
				public void run() {
					float angle = piece.getAngle();
					x2 = (int)(x + 100 * Math.cos(angle));
					y2 = (int)(y + 100 * Math.sin(angle));
					if((angle <= desiredAngle&&direction)||(angle >= desiredAngle&&!direction)) scheduleTask(desiredAngle, direction);
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

	boolean leftPressed = false, rightPressed = false, upPressed = false, downPressed = false, gravity = false, drawCollision = false, resetPosition = false, displayLevel = false;

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
	
	private void displayLevel(){
		displayLevel = true;
		timer.schedule(new TimerTask(){

			@Override
			public void run() {
				displayLevel = false;
			}}, 1500);
	}

	private void createPlayer(){
		texture = new Texture(Gdx.files.internal("pik.png"));
		playerSprite = new Sprite(texture);

		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(20, 20);
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(50, 50);
		bodyDef.type = BodyType.DynamicBody;
		//boxBodyDef.fixedRotation = false;
		playerBody = world.createBody(bodyDef);
		FixtureDef boxFixtureDef = new FixtureDef();
		boxFixtureDef.shape = boxShape;
		boxFixtureDef.restitution = 0.75f;
		boxFixtureDef.density = 0f;
		playerBody.createFixture(boxFixtureDef);
		boxShape.dispose();
		playerBody.setUserData(playerSprite);
	}

	@Override public void create () {
		//
		Box2D.init();
		final float w = Gdx.graphics.getWidth();
		final float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera();
		camera.setToOrtho(false,w,h);
		camera.update();
		tiledMap = new TmxMapLoader().load("map.tmx");
		tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
		Gdx.input.setInputProcessor(this);
		world = new World(new Vector2(0, 0), false); 
		debugRenderer = new Box2DDebugRenderer();
		timer = new Timer();

		collisionSound = Gdx.audio.newSound(Gdx.files.internal("pew.wav"));

		world.setContactListener(new ContactListener() {
			@Override
			public void beginContact(Contact contact) {
				collisionSound.play();
				//print("CONTACT");
				//Body a=contact.getFixtureA().getBody();
				Body b=contact.getFixtureB().getBody();
				if(b.getPosition().x > w||b.getPosition().y > h){
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
        font = new BitmapFont();
        font.setScale(5);
		world.getBodies(bodies);
		displayLevel();
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
		sb.begin();
        if(displayLevel) font.draw(sb, "Level " + maze.mazeLevel.level, 400, 550);
		//sprite.draw(sb);
		// I did not take a look at implementation but you get the idea
		//sprite.setPosition(x, y); = body.localVector.x;
		//sprite.y = body.localVector.y;
		for (Body b : bodies) {
			// Get the body's user data - in this example, our user 
			// data is an instance of the Entity class
			Sprite e = (Sprite) b.getUserData();

			if (e != null) {
				// Update the entities/sprites position and angle
				e.setPosition(b.getPosition().x-30, b.getPosition().y-24);
				// We need to convert our angle from radians to degrees
				e.setRotation(MathUtils.radiansToDegrees * b.getAngle());
				e.draw(sb);
			}
		}
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
		sb.end();
		ShapeRenderer shapeRenderer = new ShapeRenderer();
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(maze.mazeLevel.color);
		for(MazePiece mp : maze.pieces){
			Vector2 pos = mp.piece.getPosition();
			shapeRenderer.line(pos.x, pos.y, mp.x2, mp.y2);
		}
		shapeRenderer.end();
		//debugRenderer.render(world, camera.combined);
		world.step(1/60f, 6, 2);
		//world.step(Gdx.graphics.getDeltaTime(), 8, 3);
	}

	@Override
	public boolean keyDown(int keycode) {
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
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override public boolean keyUp(int keycode) {
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
		return false;
	}

	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		Vector3 clickCoordinates = new Vector3(screenX,screenY,0);
		Vector3 position = camera.unproject(clickCoordinates);
		playerSprite.setPosition(position.x, position.y);
		playerBody.setTransform(position.x, position.y, playerBody.getAngle());
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