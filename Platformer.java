package com.platformer;


import java.util.Iterator;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;


public class Platformer extends ApplicationAdapter {
  private Texture bulletImage;
  private Texture gunManImage;
  private Texture buttonImage;
  private Texture victimImage;
  private SpriteBatch batch;
  private OrthographicCamera camera;
  private Rectangle gunMan;
  private Array<Rectangle> bullets;
  private Array<Rectangle> victims;
  private long lastShootTime;
  private long lastVictimTime;
  private Rectangle shootButton;



  @Override
  public void create() {

    // load the images for the bullet and the gunMan
    bulletImage = new Texture(Gdx.files.internal("Bullet.png"));
    gunManImage = new Texture(Gdx.files.internal("Guy with the gun.png"));
    buttonImage = new Texture(Gdx.files.internal("Button.png"));
    victimImage = new Texture(Gdx.files.internal("Victim.png"));



    // create the camera and the SpriteBatch
    camera = new OrthographicCamera();
    camera.setToOrtho(false, 800, 480);
    batch = new SpriteBatch();

    // create a Rectangle to logically represent the gunMan
    gunMan = new Rectangle();
    gunMan.x = 64;// makes the gunMan the same distance from the left side of the screen as half its
                  // width
    gunMan.y = 20; // bottom left corner of the gunMan is 20 pixels above the bottom screen edge
    gunMan.width = 64;
    gunMan.height = 64;

    // create a rectangle to logically represent the shoot button
    shootButton = new Rectangle();
    shootButton.width = 150;
    shootButton.height = 80;
    shootButton.x = 576;
    shootButton.y = 96;

    // create the bullets array and spawn the first victim
    bullets = new Array<Rectangle>();
    victims = new Array<Rectangle>();

    spawnVictim();
  }

  private void spawnBullet() {
    Rectangle bullet = new Rectangle();
    bullet.x = gunMan.x + 64;
    bullet.y = gunMan.y + gunMan.height / 2;
    bullet.width = 32;
    bullet.height = 16;
    bullets.add(bullet);
    lastShootTime = TimeUtils.nanoTime();
  }

  private void spawnVictim() {
    Rectangle victim = new Rectangle();
    victim.x = Gdx.graphics.getWidth() - 20;
    victim.y = MathUtils.random() * (Gdx.graphics.getHeight() - 64);
    victim.width = 40;
    victim.height = 64;
    victims.add(victim);
    lastVictimTime = TimeUtils.nanoTime();
  }

  @Override
  public void render() {

    // clears the screen and replaces it with a white screen
    // arguments are red green blue and opacity bounded [0,1]
    ScreenUtils.clear(1, 1, 1, 1);

    // tell the camera to update its matrices.
    camera.update();


    // tell the SpriteBatch to render in the
    // coordinate system specified by the camera.
    batch.setProjectionMatrix(camera.combined);


    // begin a new batch and draw the gunMan and
    // all drops
    batch.begin();
    batch.draw(gunManImage, gunMan.x, gunMan.y, gunMan.width, gunMan.height);
    batch.draw(buttonImage, shootButton.x, shootButton.y, shootButton.width, shootButton.height);
    for (Rectangle bullet : bullets) {
      batch.draw(bulletImage, bullet.x, bullet.y, bullet.width, bullet.height);
    }
    for (Rectangle victim : victims) {
      batch.draw(victimImage, victim.x, victim.y, victim.width, victim.height);
    }
    batch.end();

    // process user input
    if (Gdx.input.isTouched()) {
      Vector3 touchPos = new Vector3();
      touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
      camera.unproject(touchPos);
      Rectangle point = new Rectangle(touchPos.x, touchPos.y, 30, 30);
      if (lastShootTime + 500000000 < TimeUtils.nanoTime() && point.overlaps(shootButton)) {

        spawnBullet();
      } else if (touchPos.x < Gdx.graphics.getWidth() / 2) {
        gunMan.y = touchPos.y - 64 / 2;
      }
    }
    if (Gdx.input.isKeyPressed(Keys.DOWN))
      gunMan.y -= 200 * Gdx.graphics.getDeltaTime();
    if (Gdx.input.isKeyPressed(Keys.UP))
      gunMan.y += 200 * Gdx.graphics.getDeltaTime();
    if (Gdx.input.isKeyPressed(Keys.SPACE) && lastShootTime + 500000000 < TimeUtils.nanoTime()) {
      spawnBullet();
    }

    // make sure the gunMan stays within the screen bounds
    if (gunMan.y < 0)
      gunMan.y = 0;
    if (gunMan.y > 480 - 64)
      gunMan.y = 480 - 64;

    // check if we need to create a new victim
    if (lastVictimTime + 1000000000 < TimeUtils.nanoTime()) {
      spawnVictim();
    }

    // move the bullets, remove any that are beneath the bottom edge of
    // the screen or that hit the victims.
    for (Iterator<Rectangle> iter = bullets.iterator(); iter.hasNext();) {
      Rectangle bullet = iter.next();
      bullet.x += 200 * Gdx.graphics.getDeltaTime();

      for (Iterator<Rectangle> iter2 = victims.iterator(); iter2.hasNext();) {
        Rectangle victim = iter2.next();
        if (bullet.overlaps(victim)) {
          iter.remove();
          iter2.remove();
        }
      }
      if (bullet.x - 64 > 800) {
        iter.remove();
      }

    }
    for (Iterator<Rectangle> iter = victims.iterator(); iter.hasNext();) {
      Rectangle victim = iter.next();
      victim.x -= 200 * Gdx.graphics.getDeltaTime();
      if (victim.x <= 0) {
        iter.remove();
      }

    }



  }

  @Override
  public void dispose() {
    // dispose of all the native resources
    bulletImage.dispose();
    gunManImage.dispose();
    buttonImage.dispose();
    victimImage.dispose();
    batch.dispose();
  }



}
