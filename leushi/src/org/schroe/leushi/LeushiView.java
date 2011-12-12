package org.schroe.leushi;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
	private Bitmap divider = null;
	private Bitmap scoreLabel = null;
	private GameThread thread = null;
	private long lastTime = 0;
	private GameBoard board = null;
	private long elapsed;
	private final int COLUMNS = 4;
	private final int ROWS = 7;
	private final double BOARD_WIDTH_RATIO = 0.8;
	private int downCol = -1;
	private int downRow = -1;
	private int hovering = -1;
	private long tick_ms;
	private boolean speedUp = false;
	private final long MIN_TICK = 50;

	public class GameBoard {
		private int board[][];
		private int next[];
		private int falling[];
		private int row = 0;
		private int score = 0;
		private boolean gameOver = false;
		private Bitmap bottom, top, pieces[];
		private Random rand = null;
		private final int BOTTOM = -1;
		private final int TOP = -2;
		private final int EMPTY = -3;
		
		public GameBoard(int rows, int cols, Bitmap bottom, Bitmap top, Bitmap[] pieces) {
			rand = new Random(System.currentTimeMillis());
			board = new int[cols][rows];
			falling = new int[cols];
			this.bottom = bottom;
			this.top = top;
			this.pieces = pieces;
			scalePieces(bottom.getWidth(), bottom.getHeight());
			populateNext(2);
			for (int c = 0; c < cols; c++) {
				for (int r = 0; r < rows; r++) {
					board[c][r] = EMPTY;
				}
				falling[c] = EMPTY;
			}
		}
		
		/**
		 * Scales all the game pieces to the specified size.
		 * @param w The width to scale to
		 * @param h The height to scale to
		 */
		public void scalePieces(int w, int h) {
			bottom = Bitmap.createScaledBitmap(bottom, w, h, true);
			top = Bitmap.createScaledBitmap(top, w, h, true);
			for (int i = 0; i < pieces.length; i++) {
				pieces[i] = Bitmap.createScaledBitmap(pieces[i], w, h, true);
			}
		}

		/**
		 * Sets the total size of the game board.
		 * @param w The new width.
		 * @param h The new height.
		 */
		public void setSize(int w, int h) {
			w /= board.length;
			h /= (board[0].length+1);
			int dim = w < h ? w : h;
			
			scalePieces(dim, dim);
		}
		
		/**
		 * Swaps the contents of the two specified columns
		 * @param a a column to swap
		 * @param b a column to swap
		 */
		public void swap(int a, int b) {
			if (falling[a] != EMPTY && falling[b] == EMPTY && board[b][row] != EMPTY) {
				falling[b] = falling[a];
				falling[a] = EMPTY;
			}

			if (falling[b] != EMPTY && falling[a] == EMPTY && board[a][row] != EMPTY) {
				falling[a] = falling[b];
				falling[b] = EMPTY;
			}
			
			int[] temp = board[a];
			board[a] = board[b];
			board[b] = temp;
		}
		
		/**
		 * Generate the next 'on deck' row
		 * 
		 * @param numcols The number of columns to put new pieces in
		 */
		public void populateNext(int numcols) {
			List<Integer> cols = new ArrayList<Integer>();
			List<Integer> addto = new ArrayList<Integer>();
			next = new int[falling.length];
			for (int c = 0; c < next.length; c++) {
				next[c] = EMPTY;
				cols.add(c);
			}
			for (int c = 0; c < numcols; c++) {
				addto.add(cols.remove(rand.nextInt(cols.size())));
			}
			for (int c : addto) {
				next[c] = rand.nextInt(pieces.length+2)-2;
			}
		}
		
		/**
		 * Do the tick processing for a top-half container piece at the specified position.
		 * @param col The column the top piece is in
		 * @param row The row the top piece is in
		 */
		private void tickTop(int col, int row) {
			for (int r = row+1; r < board[col].length; r++) {
				if (board[col][r] == BOTTOM) {
					for (int i = r; i > row; i--) {
						score += 10;
						board[col][i] = EMPTY;
					}
					onMatch();
				}
			}
			falling[col] = EMPTY;
		}
		
		/**
		 * Performs all necessary processing for one update to the game board.
		 * 
		 * This includes shifting the falling row down by one, clearing any
		 * matches, updating the score, and generating a new next row if all
		 * falling pieces have come to rest.
		 */
		public void tick() {
			if (gameOver) {
				return;
			}
			boolean empty = true;
			for (int col = 0; col < falling.length; col++) {
				if (falling[col] == EMPTY) {
					continue;
				}
				empty = false;
				if (row == board[col].length-1) {
					// we've reached the bottom of the board
					if (falling[col] != TOP) {
						board[col][row] = falling[col];
					}
					falling[col] = EMPTY;
					continue;
				}
				if (falling[col] == board[col][row+1]) {
					// Match!
					score += 5;
					falling[col] = EMPTY;
					board[col][row+1] = EMPTY;
					onMatch();
					continue;
				}
				if (falling[col] == TOP && board[col][row+1] != EMPTY) {
					// A top half just landed. Check for bottoms in the stack...
					tickTop(col, row);
					continue;
				}
				if (board[col][row+1] != EMPTY) {
					// A piece has landed and is not a match
					board[col][row] = falling[col];
					falling[col] = EMPTY;
					continue;
				}
			}
			if (empty) {
				// We've dropped all our falling pieces into the actual board. Time to start the next row falling.
				falling = next;
				// First we need to check whether the new falling row will cause a game over by dropping onto an occupied space.
				for (int col = 0; col < falling.length; col++) {
					switch(falling[col]) {
					case EMPTY:
						break;
					case TOP:
						if (board[col][0] != EMPTY) {
							// A top has landed... on the top.
							tickTop(col, -1);
						}
						break;
					default:
						if (board[col][0] != EMPTY) {
							if (board[col][0] == falling[col]) {
								// The match is allowed to clear and averts near disaster.
								score += 5;
								falling[col] = EMPTY;
								board[col][0] = EMPTY;
								onMatch();
							} else {
								// Game over, man! Clear out the falling row so everything draws nicely
								falling = new int[falling.length];
								for (int i = 0; i < falling.length; i++) {
									falling[i] = EMPTY;
								}
								gameOver = true;
								return;
							}
						}
						break;
					}
				}
				populateNext(2);
				row = 0;
			} else {
				row++;
			}
		}
		
		public int getScore() {
			return score;
		}
		
		/**
		 * Gets the bitmap associated with the given game-piece index.
		 * @param i The index to retrieve the bitmap for
		 * @return The bitmap, or null if the index indicates any empty square
		 */
		public Bitmap getBitmap(int i) {
			switch (i) {
			case BOTTOM:
				return bottom;
			case TOP:
				return top;
			case EMPTY:
				return null;
			default:
				if (i >= 0 && i < pieces.length) {
					return pieces[i];
				}
				return null;
			}
		}
		
		/**
		 * Draws the game board into the supplied canvas.
		 * @param c The canvas to draw onto.
		 */
		public void draw(Canvas c) {
			Bitmap b;
			int width = bottom.getWidth();
			int height = bottom.getHeight();
			c.drawBitmap(divider, 0, height - (divider.getHeight()/2), null);
			Paint tint = new Paint();
			for (int col = 0; col < board.length; col++) {
				b = getBitmap(next[col]);
				if (b != null) {
					c.drawBitmap(b, col*width, 0, null);
				}
				b = getBitmap(falling[col]);
				if (b != null) {
					c.drawBitmap(b, col*width, (this.row+1)*height, null);
				}
				for (int row = 0; row < board[col].length; row++) {
					b = getBitmap(board[col][row]);
					if (b != null) {
						c.drawBitmap(b, col*width, (row+1)*height, null);
					}
				}
			}
		}
	}
	
	private void onMatch() {
		current_bg += 1;
		current_bg %= backgrounds.length;
	}

	public LeushiView(Context context, AttributeSet attrs) {
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
		board = new GameBoard(ROWS, COLUMNS, BitmapFactory.decodeResource(getResources(), R.drawable.bottom), BitmapFactory.decodeResource(getResources(), R.drawable.top),
										new Bitmap[] {
											BitmapFactory.decodeResource(getResources(), R.drawable.airball),
											BitmapFactory.decodeResource(getResources(), R.drawable.decayball),
											BitmapFactory.decodeResource(getResources(), R.drawable.earthball),
											BitmapFactory.decodeResource(getResources(), R.drawable.fireball),
											BitmapFactory.decodeResource(getResources(), R.drawable.growthball),
											BitmapFactory.decodeResource(getResources(), R.drawable.waterball),
										});
		tick_ms = 1000;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		gameover = Bitmap.createScaledBitmap(gameover, (int)(gameover.getWidth()*((float)width/(float)backgrounds[current_bg].getWidth())), (int)(gameover.getHeight()*((float)height/(float)backgrounds[current_bg].getHeight())), true);
		for (int i = 0; i < backgrounds.length; i++) {
			backgrounds[i] = Bitmap.createScaledBitmap(backgrounds[i], width, height, true);
		}
		board.setSize((int)(getWidth()*BOARD_WIDTH_RATIO), getHeight());
	}

	public void surfaceCreated(SurfaceHolder holder) {
		int w = getWidth();
		int h = getHeight();
		gameover = Bitmap.createScaledBitmap(gameover, (int)(gameover.getWidth()*((float)w/(float)backgrounds[current_bg].getWidth())), (int)(gameover.getHeight()*((float)h/(float)backgrounds[current_bg].getHeight())), true);
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
		
		Paint textp = new Paint();
		textp.setARGB(0xff, 0xc0, 0xc0, 0xc0);
		textp.setTextAlign(Align.RIGHT);
		textp.setTextSize((int)(getResources().getDisplayMetrics().density * 24 + 0.5));
		textp.setTypeface(Typeface.create("Narkism", Typeface.NORMAL));
		//c.drawText("Score", getWidth(), 30, textp);
		c.drawBitmap(scoreLabel, getWidth()-scoreLabel.getWidth(), 0, null);
		c.drawText(Integer.toString(board.score), (int)(getWidth() * 0.96), (int)(getHeight() * 0.08), textp);
		
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
