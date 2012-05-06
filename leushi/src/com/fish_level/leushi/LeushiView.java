package com.fish_level.leushi;

import org.schroe.leushi.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class LeushiView extends SurfaceView implements SurfaceHolder.Callback {
	private int current_bg = 0;
	private Bitmap[] backgrounds = null;
	private Bitmap gameover = null;
	Bitmap divider = null;
	private Bitmap scoreLabel = null;
	private GameThread thread = null;
	private long lastTime = 0;
	private GameBoard board = null;
	private long elapsed;
	final int COLUMNS = 4;
	private final int ROWS = 7;
	private final double BOARD_WIDTH_RATIO = 0.8;
	private int downCol = -1;
	private int downRow = -1;
	private int hovering = -1;
	private long tick_ms;
	private boolean speedUp = false;
	private final long MIN_TICK = 50;
	private double width_ratio = 1.0;
	private double height_ratio = 1.0;
	public Paint textpaint = null;
	public enum gameType {
		SURVIVAL, PUZZLE
	};

	void onMatch() {
		current_bg += 1;
		current_bg %= backgrounds.length;
	}

	public LeushiView(Context context, AttributeSet attrs, gameType type) {
		super(context, attrs);
		getHolder().addCallback(this);
		
		backgrounds = new Bitmap[] {
				BitmapFactory.decodeResource(getResources(), R.drawable.earth_background),
				BitmapFactory.decodeResource(getResources(), R.drawable.water_background),
		};
		gameover = BitmapFactory.decodeResource(getResources(), R.drawable.gameover);
		divider = BitmapFactory.decodeResource(getResources(), R.drawable.stroke);
		scoreLabel = BitmapFactory.decodeResource(getResources(), R.drawable.score);
		thread = new GameThread();
		switch (type) {
		case SURVIVAL:
			board = new SurvivalBoard(this, ROWS, COLUMNS, BitmapFactory.decodeResource(getResources(), R.drawable.bottom), BitmapFactory.decodeResource(getResources(), R.drawable.top),
											new Bitmap[] {
												BitmapFactory.decodeResource(getResources(), R.drawable.airball),
												BitmapFactory.decodeResource(getResources(), R.drawable.decayball),
												BitmapFactory.decodeResource(getResources(), R.drawable.earthball),
												BitmapFactory.decodeResource(getResources(), R.drawable.fireball),
												BitmapFactory.decodeResource(getResources(), R.drawable.growthball),
												BitmapFactory.decodeResource(getResources(), R.drawable.waterball),
											});
			break;
		case PUZZLE:
			board = new GameBoard(this, ROWS, COLUMNS, BitmapFactory.decodeResource(getResources(), R.drawable.bottom), BitmapFactory.decodeResource(getResources(), R.drawable.top),
										new Bitmap[] {
											BitmapFactory.decodeResource(getResources(), R.drawable.airball),
											BitmapFactory.decodeResource(getResources(), R.drawable.decayball),
											BitmapFactory.decodeResource(getResources(), R.drawable.earthball),
											BitmapFactory.decodeResource(getResources(), R.drawable.fireball),
											BitmapFactory.decodeResource(getResources(), R.drawable.growthball),
											BitmapFactory.decodeResource(getResources(), R.drawable.waterball),
										});
			break;
		}
		tick_ms = 1000;
		textpaint = new Paint();
		textpaint.setARGB(0xff, 0xc0, 0xc0, 0xc0);
		textpaint.setTextAlign(Align.RIGHT);
		textpaint.setTextSize((int)(24 * height_ratio));
		textpaint.setTypeface(Typeface.create("Narkism", Typeface.NORMAL));
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		width_ratio = (float)width/(float)backgrounds[current_bg].getWidth();
		height_ratio = (float)height/(float)backgrounds[current_bg].getHeight();
		gameover = Bitmap.createScaledBitmap(gameover, (int)(gameover.getWidth()*width_ratio), (int)(gameover.getHeight()*height_ratio), true);
		divider = Bitmap.createScaledBitmap(divider, (int)(divider.getWidth()*width_ratio), (int)(divider.getHeight()*height_ratio), true);
		scoreLabel = Bitmap.createScaledBitmap(scoreLabel, (int)(scoreLabel.getWidth()*width_ratio), (int)(scoreLabel.getHeight()*height_ratio), true);
		for (int i = 0; i < backgrounds.length; i++) {
			backgrounds[i] = Bitmap.createScaledBitmap(backgrounds[i], width, height, true);
		}
		board.setSize((int)(getWidth()*BOARD_WIDTH_RATIO), getHeight());
	}

	public void surfaceCreated(SurfaceHolder holder) {
		int w = getWidth();
		int h = getHeight();
		width_ratio = (float)w/(float)backgrounds[current_bg].getWidth();
		height_ratio = (float)h/(float)backgrounds[current_bg].getHeight();
		gameover = Bitmap.createScaledBitmap(gameover, (int)(gameover.getWidth()*width_ratio), (int)(gameover.getHeight()*height_ratio), true);
		divider = Bitmap.createScaledBitmap(divider, (int)(divider.getWidth()*width_ratio), (int)(divider.getHeight()*height_ratio), true);
		scoreLabel = Bitmap.createScaledBitmap(scoreLabel, (int)(scoreLabel.getWidth()*width_ratio), (int)(scoreLabel.getHeight()*height_ratio), true);
		for (int i = 0; i < backgrounds.length; i++) {
			backgrounds[i] = Bitmap.createScaledBitmap(backgrounds[i], w, h, true);
		}
		board.setSize((int)(w*BOARD_WIDTH_RATIO), h);
		thread = new GameThread();
		thread.setRunning(true);
		thread.start();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		thread.setRunning(false);
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
				// try again
			}
		}
	}
	
	/**
	 * Performs a game board tick if the specified milliseconds have elapsed since the last one.
	 * @param ms The time in milliseconds that must have elapsed for a tick to be performed
	 */
	private void tickIfElapsed(long ms) {
		if (elapsed >= ms) {
			elapsed -= ms;
			int before = board.score / 100;
			board.tick();
			int after = board.score / 100;
			if (after > before && tick_ms > MIN_TICK) {
				tick_ms -= MIN_TICK;
			}
		}
	}
	
	/**
	 * Update the game state.
	 * @param ms The current system time in milliseconds
	 */
	public void update(long ms) {
		elapsed += ms - lastTime;
		lastTime = ms;
		if (speedUp) {
			int[] falling = board.falling;
			tickIfElapsed(MIN_TICK);
			if (board.falling != falling) {
				speedUp = false;
			}
		} else {
			tickIfElapsed(tick_ms);
		}
	}
	
	/**
	 * Draws the whole game view.
	 * @param c The canvas to draw onto.
	 */
	public void draw(Canvas c) {
		c.drawBitmap(backgrounds[current_bg], 0, 0, null);
		if (downCol >= 0) {
			Paint p = new Paint();
			p.setARGB(0x80, 0xbb, 0xbb, 0xcc);
			p.setStyle(Style.FILL);
			Rect r = new Rect(downCol*getColumnWidth(), 0, (downCol+1)*getColumnWidth(), getHeight());
			c.drawRect(r, p);
		}
		if (hovering >= 0) {
			Paint p = new Paint();
			p.setARGB(0x80, 0xbb, 0xcc, 0xbb);
			p.setStyle(Style.FILL);
			Rect r = new Rect(hovering*getColumnWidth(), 0, (hovering+1)*getColumnWidth(), getHeight());
			c.drawRect(r, p);
		}
		board.draw(c);
		
		c.drawBitmap(scoreLabel, getWidth()-scoreLabel.getWidth(), 0, null);
		c.drawText(Integer.toString(board.score), (int)(getWidth() * 0.98), (int)(getHeight() * 0.08), textpaint);
		
		if (board.gameOver) {
			c.drawBitmap(gameover, getWidth()/2-gameover.getWidth()/2, getHeight()/2-gameover.getHeight()/2, null);
		}
	}
	
	/**
	 * Pauses the game.
	 */
	public void pause() {
		thread.setRunning(false);
		this.setVisibility(INVISIBLE);
	}
	
	/**
	 * Resumes a paused game.
	 */
	public void resume() {
		thread.setRunning(true);
		this.setVisibility(VISIBLE);
	}
	
	/**
	 * @return The width of each game board column.
	 */
	public int getColumnWidth() {
		return board.bottom.getWidth();
	}
	
	/**
	 * @return The height of each game board row.
	 */
	public int getRowHeight() {
		return board.bottom.getHeight();
	}
	
	/**
	 * The touchscreen handler.
	 * 
	 * Things to do here:
	 *  - If the user drags from one column into the next left or right, swap them.
	 *  - If the user drags down at least two rows, accelerate the drop.
	 * 
	 * @param event The motion event.
	 * @return True if the event was handled, false otherwise. 
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (board.gameOver) {
			downCol = -1;
			hovering = -1;
			return false;
		}
		
		int col = (int)(event.getX() / getColumnWidth());
		int row = (int)(event.getY() / getRowHeight());
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (col < COLUMNS) {
				downCol = col;
			}
			downRow = row;
			return true;
		case MotionEvent.ACTION_UP:
			if (downCol >= 0 && col >= 0) {
				if (downCol > col) {
					board.swap(downCol, downCol - 1);
				} else if (downCol < col) {
					if (downCol + 1 < COLUMNS)
						board.swap(downCol, downCol + 1);
				}
			}
			if ((downCol == col) && ((row - downRow) > 1)) {
				speedUp = true;
			}
			downCol = -1;
			hovering = -1;
			return true;
		case MotionEvent.ACTION_MOVE:
			if (downCol >= 0 && col >= 0 && col < COLUMNS) {
				if (Math.abs(downCol - col) == 1) {
					hovering = col;
				}
				if (downCol == col) {
					hovering = -1;
				}
			}
			return true;
		}
		return false;
	}

	public class GameThread extends Thread {
		private boolean run = false;
		
		public void setRunning(boolean value) {
			lastTime = System.currentTimeMillis();
			elapsed = 0;
			run = value;
		}
		
		@Override
		public void run() {
			Canvas c;
			while (run) {
				c = null;
				update(System.currentTimeMillis());
				
				try {
					c = getHolder().lockCanvas(null);
					synchronized (getHolder()) {
						draw(c);
					}
				} finally {
					if (c != null) {
						getHolder().unlockCanvasAndPost(c);
					}
				}
			}
		}
	}
}
