package com.gmail.jackbcousineau;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
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
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.gmail.jackbcousineau.AmazementMain.Maze.MazePiece;

public class AmazementMain extends ApplicationAdapter implements InputProcessor{

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
			boxFixtureDef.density = 2.0f;
			boxFixtureDef.shape = lineShape;
			lineBody.createFixture(boxFixtureDef);
			lineShape.dispose();
			return lineBody;
		}

	}

	class Maze{

		Array<MazePiece> pieces = new Array<MazePiece>();
		Timer timer = new Timer();

		public void turnAll(){
			print("TURNING ALL");
			for(MazePiece p : pieces){
				p.rotateLeft();
			}
		}

		public void turnAllLeft(){
			//print("TURNING ALL LEFT");
			for(MazePiece p : pieces){
				p.rotateLeft();
			}
		}

		public void turnAllRight(){
			//print("TURNING ALL RIGHT");
			for(MazePiece p : pieces){
				p.rotateRight();
			}
		}

		public Maze(){
			addMazePiece(250, 500, 0);
			addMazePiece(350, 500, 90);
			addMazePiece(450, 500, 180);
			addMazePiece(550, 500, 270);
		}

		private void addMazePiece(int x, int y, int degrees){
			pieces.add(new MazePiece(x, y, degrees));
		}

		class MazePiece{

			public Body piece;

			int x, y, x2, y2, degrees = 0;

			boolean isMoving = false;

			//orientation: 1 = left-right horizontal, 2 = right-left horizontal, 3 = bottom-up vertical, 4 = top-down vertical
			//orientation: true = horizontal, false = vertical
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
						piece.setAngularVelocity(1);
						scheduleTask(desiredAngle, true);
					}
					else if(mod < 0){
						piece.setAngularVelocity(-1);
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

				private float desiredAngle;
				private boolean direction;

				public AngleTask(float desiredAngle, boolean direction){
					this.desiredAngle = desiredAngle;
					this.direction = direction;
				}

				/*private float getCorrectAngle(){
					//float angle = piece.getAngle() + (MathUtils.degreesToRadians * degrees);
					//print("Angle in degrees: " + Math.ceil((MathUtils.radiansToDegrees * angle)%360));
					//return MathUtils.degreesToRadians*(Math.ceil((MathUtils.radiansToDegrees * rad)%360));
					float angle = piece.getAngle();
					//print("Raw angle: " + angle  + ", " + MathUtils.radiansToDegrees*angle);
					return angle;
				}*/

				@Override
				public void run() {
					float angle = piece.getAngle();
					x2 = (int)(x + 100 * Math.cos(angle));
					y2 = (int)(y + 100 * Math.sin(angle));
					if((angle <= desiredAngle&&direction)||(angle >= desiredAngle&&!direction)) scheduleTask(desiredAngle, direction);
					else{
						piece.setAngularVelocity(0);
						piece.setTransform(x, y, desiredAngle);
						//double rad = MathUtils.degreesToRadians*degrees;
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

	boolean leftPressed = false, rightPressed = false, upPressed = false, downPressed = false, gravity = false, drawCollision = false, resetPosition = false;

	World world;
	Box2DDebugRenderer debugRenderer;
	Body playerBody, wallBody, edgeBody2, lineBody, objectiveBody;
	EdgeBody edgeBody;
	Array<Body> bodies = new Array<Body>();
	Maze maze;

	ParticleEffect collisionEffect;

	Timer timer;

	private void createPlayer(){
		/*
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(20, 20);
		BodyDef boxBodyDef = new BodyDef();
		boxBodyDef.position.set(500, 500);
		boxBodyDef.angle = MathUtils.PI / 32;
		boxBodyDef.type = BodyType.DynamicBody;
		boxBodyDef.fixedRotation = false;
		Body boxBody = world.createBody(boxBodyDef);
		FixtureDef boxFixtureDef = new FixtureDef();
		boxFixtureDef.shape = boxShape;
		boxFixtureDef.restitution = 0.75f;
		boxFixtureDef.density = 2.0f;
		boxBody.createFixture(boxFixtureDef);
		boxShape.dispose();
		 */
		texture = new Texture(Gdx.files.internal("pik.png"));
		playerSprite = new Sprite(texture);

		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(20, 20);
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(50, 50);
		//boxBodyDef.angle = MathUtils.PI / 32;
		bodyDef.type = BodyType.DynamicBody;
		//boxBodyDef.fixedRotation = false;
		playerBody = world.createBody(bodyDef);
		FixtureDef boxFixtureDef = new FixtureDef();
		boxFixtureDef.shape = boxShape;
		boxFixtureDef.restitution = 0.75f;
		boxFixtureDef.density = 0f;
		playerBody.createFixture(boxFixtureDef);
		boxShape.dispose();


		//sprite.translate(512, 512);
		//BodyDef bodyDef = new BodyDef();
		//bodyDef.type = BodyType.DynamicBody;
		//bodyDef.bullet = true;
		//bodyDef.position.set(100, 300);
		//body = world.createBody(bodyDef);
		playerBody.setUserData(playerSprite);
	}

	@Override public void create () {
		//
		Box2D.init();
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera();
		camera.setToOrtho(false,w,h);
		camera.update();
		tiledMap = new TmxMapLoader().load("map.tmx");
		tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
		Gdx.input.setInputProcessor(this);
		world = new World(new Vector2(0, 0), false); 
		debugRenderer = new Box2DDebugRenderer();
		timer = new Timer();

		world.setContactListener(new ContactListener() {
			@Override
			public void beginContact(Contact contact) {
				//print("CONTACT");
				//Body a=contact.getFixtureA().getBody();
				Body b=contact.getFixtureB().getBody();
				/*if(b.getPosition().x > 1024){
			    	print("BODY B: x> 1024");
			    }
			    if(b.getPosition().y > 1024){
			    	print("BODY B: y> 1024");
			    }*/
				if(b.getPosition().x > 1024||b.getPosition().y > 1024){
					//sprite.setPosition(500, 500);
					resetPosition = true;
					print("Next level");
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
				//print("ENDCONTACT");
				//crumpit96 or trumpet96
			}

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
			}
		});

		//camera.lookAt(0, 0, 0);

		//camera.translate(0, 534);

		sb = new SpriteBatch();

		createPlayer();
		edgeBody = new EdgeBody();
		maze = new Maze();

		ParticleEffectPool effectPool;
		Array<PooledEffect> effects = new Array<PooledEffect>();

		collisionEffect = new ParticleEffect();
		collisionEffect.load(Gdx.files.internal("collision_particle"), Gdx.files.internal(""));
		//collisionEffect.setPosition(950, 950);
		world.getBodies(bodies);
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
		if(resetPosition){
			resetPosition = false;
			playerBody.setTransform(100, 100, playerBody.getAngle());
			//print(body.getLinearVelocity() + "");
			playerBody.setLinearVelocity(100, 100);
			//body.apLinearImpulse(new Vector2(5, 5), body.getPosition(), false);
		}
		sb.end();
		ShapeRenderer shapeRenderer = new ShapeRenderer();
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.BLACK);
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
		//body.getPosition().set(position.x, position.y);
		//((Sprite)body.getUserData()).setPosition(position.x, position.y);
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