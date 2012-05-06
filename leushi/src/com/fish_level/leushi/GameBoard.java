package com.fish_level.leushi;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;

public class GameBoard {
	protected LeushiView leushiView;
	protected final int BOTTOM = -1;
	protected final int TOP = -2;
	protected final int EMPTY = -3;
	
	protected int board[][];
	protected int next[];
	protected int falling[];
	protected int row = 0;
	protected int score = 0;
	protected boolean gameOver = false;
	protected Bitmap bottom;
	protected Bitmap top;
	protected Bitmap pieces[];
	protected Random rand = null;
	
	public GameBoard(LeushiView leushiView, int rows, int cols, Bitmap bottom, Bitmap top, Bitmap[] pieces) {
		this.leushiView = leushiView;
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
					board[col][i] = EMPTY;
				}
				scoreCupMatch(r-row);
				break;
			}
		}
		falling[col] = EMPTY;
	}
	
	/**
	 * Tally up the score for match
	 * @param piece The id of the piece that was matched
	 */
	protected int getMatchScore(int piece) {
		return 5;
	}
	
	private void scoreMatch(int piece) {
		score += getMatchScore(piece);
		this.leushiView.onMatch();
	}
	
	/**
	 * A cup match has occurred! Score it.
	 * @param rows The number of rows in the cup match, including cup pieces
	 */
	protected int getCupMatchScore(int rows) {
		return 10 * rows;
	}
	
	private void scoreCupMatch(int rows) {
		score += getCupMatchScore(rows);
		this.leushiView.onMatch();
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
				scoreMatch(falling[col]);
				falling[col] = EMPTY;
				board[col][row+1] = EMPTY;
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
							scoreMatch(falling[col]);
							falling[col] = EMPTY;
							board[col][0] = EMPTY;
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
		c.drawBitmap(this.leushiView.divider, 0, height - (this.leushiView.divider.getHeight()/2), null);
		Paint tint = new Paint();
		tint.setStyle(Style.FILL);
		tint.setARGB(0x40, 0, 0, 0);
		c.drawRect(new Rect(0, 0, this.leushiView.getWidth(), height), tint);
		c.drawRect(new Rect(width*this.leushiView.COLUMNS, 0, this.leushiView.getWidth(), this.leushiView.getHeight()), tint);
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