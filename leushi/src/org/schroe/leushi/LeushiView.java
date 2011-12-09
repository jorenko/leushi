package org.schroe.leushi;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class LeushiView extends SurfaceView implements SurfaceHolder.Callback {
	Bitmap background = null;
	GameThread thread = null;
	double lastTime = 0;
	GameBoard board = null;
	double elapsed;
	
	public class Sprite {
		Bitmap image;
		double x, y;
		public Sprite(int resource, double x, double y) {
			image = BitmapFactory.decodeResource(getResources(), resource);
			this.x = x;
			this.y = y;
		}
		
		public void move(double x, double y) {
			this.x += x;
			this.y += y;
		}
		
		public void wrap(double left, double top, double right, double bottom) {
			top -= image.getHeight()/2;
			bottom -= image.getHeight()/2;
			left -= image.getWidth()/2;
			right -= image.getWidth()/2;
			if (x < left) {
				x += (right-left); 
			}
			if (x > right) {
				x -= (right-left); 
			}
			if (y < top) {
				y += (bottom-top); 
			}
			if (y > bottom) {
				y -= (bottom-top); 
			}
		}
		
		public void draw(Canvas c) {
			c.drawBitmap(image, (float)x, (float)y, null);
		}
	}

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
		
		public void scalePieces(int w, int h) {
			bottom = Bitmap.createScaledBitmap(bottom, w, h, true);
			top = Bitmap.createScaledBitmap(top, w, h, true);
			for (int i = 0; i < pieces.length; i++) {
				pieces[i] = Bitmap.createScaledBitmap(pieces[i], w, h, true);
			}
		}
		
		public void setSize(int w, int h) {
			w /= board.length;
			h /= (board[0].length+1);
			int dim = w < h ? w : h;
			
			scalePieces(dim, dim);
		}
		
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
		
		private void tickTop(int col, int row) {
			for (int r = row+1; r < board[col].length; r++) {
				if (board[col][r] == BOTTOM) {
					for (int i = r; i > row; i--) {
						score += 10;
						board[col][i] = EMPTY;
					}
				}
			}
			falling[col] = EMPTY;
		}
		
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
					if (falling[col] != TOP) {
						board[col][row] = falling[col];
					}
					falling[col] = EMPTY;
					continue;
				}
				if (falling[col] == board[col][row+1]) {
					score += 5;
					falling[col] = EMPTY;
					board[col][row+1] = EMPTY;
					continue;
				}
				if (falling[col] == TOP && board[col][row+1] != EMPTY) {
					tickTop(col, row);
					continue;
				}
				if (board[col][row+1] != EMPTY) {
					board[col][row] = falling[col];
					falling[col] = EMPTY;
					continue;
				}
			}
			if (empty) {
				falling = next;
				for (int col = 0; col < falling.length; col++) {
					switch(falling[col]) {
					case EMPTY:
						break;
					case TOP:
						if (board[col][0] != EMPTY) {
							tickTop(col, -1);
						}
						break;
					default:
						if (board[col][0] != EMPTY) {
							falling = new int[falling.length];
							for (int i = 0; i < falling.length; i++) {
								falling[i] = EMPTY;
							}
							gameOver = true;
							return;
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
		
		public void draw(Canvas c) {
			Bitmap b;
			int width = bottom.getWidth();
			int height = bottom.getHeight();
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

	public LeushiView(Context context, AttributeSet attrs) {
		super(context, attrs);
		getHolder().addCallback(this);
		
		background = BitmapFactory.decodeResource(getResources(), R.drawable.menu_background);
		thread = new GameThread();
		board = new GameBoard(7, 4, BitmapFactory.decodeResource(getResources(), R.drawable.bottom), BitmapFactory.decodeResource(getResources(), R.drawable.top),
										new Bitmap[] {
											BitmapFactory.decodeResource(getResources(), R.drawable.airball),
											BitmapFactory.decodeResource(getResources(), R.drawable.decayball),
											BitmapFactory.decodeResource(getResources(), R.drawable.earthball),
											BitmapFactory.decodeResource(getResources(), R.drawable.fireball),
											BitmapFactory.decodeResource(getResources(), R.drawable.growthball),
											BitmapFactory.decodeResource(getResources(), R.drawable.waterball),
										});
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		background = Bitmap.createScaledBitmap(background, width, height, true);
		board.setSize((int)(getWidth()*0.78), getHeight());
	}

	public void surfaceCreated(SurfaceHolder holder) {
		int w = getWidth();
		int h = getHeight();
		background = Bitmap.createScaledBitmap(background, w, h, true);
		board.setSize((int)(w*0.8), h);
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
	
	public void update(double ms) {
		elapsed += ms - lastTime;
		lastTime = ms;
		
		if (elapsed >= 250) {
			elapsed -= 250;
			board.tick();
		}
	}
	
	public void draw(Canvas c) {
		c.drawBitmap(background, 0, 0, null);
		board.draw(c);
	}
	
	public void pause() {
		thread.setRunning(false);
		this.setVisibility(INVISIBLE);
	}
	
	public void resume() {
		thread.setRunning(true);
		this.setVisibility(VISIBLE);
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
