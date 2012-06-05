package com.fish_level.leushi;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class PuzzleBoard extends GameBoard {
	int required[] = null;
	public PuzzleBoard(LeushiView view, int rows, int cols, Bitmap bottom, Bitmap top, Bitmap[] pieces, int[] required) {
		super(view, rows, cols, bottom, top, pieces);
		this.required = required;
	}
	
	@Override
	public void draw(Canvas c) {
		super.draw(c);
		Bitmap b;
		int w = pieces[0].getWidth();
		int h = pieces[0].getHeight();
		Rect pos = new Rect(
				(int)(this.leushiView.getWidth() - (w * 0.75)),
				(int)(h * 1.25),
				(int)(this.leushiView.getWidth() - (w * 0.25)),
				(int)(h * 1.75));
		
		int txtx = (pos.left + pos.right) / 2;//(int)(leushiView.getWidth() * 0.93);
		int txty = pos.bottom + (int)leushiView.textpaint.getTextSize() + 2;//(int)(leushiView.getHeight() * 0.235);
		
		for (int i = 0; i < required.length; i++) {
			b = getBitmap(i);
			c.drawBitmap(b, new Rect(0, 0, b.getWidth(), b.getHeight()), pos, null);
			c.drawText(String.format("x %d", required[i]), txtx, txty, leushiView.textpaint);
			pos.offset(0, h);
			txty += h;
		}
	}
	
	@Override
	protected int getCupMatchScore(int[] pieces) {
		int nonzeroes = pieces.length;
		int val = super.getCupMatchScore(pieces);
		for (int p = 0; p < pieces.length; p++) {
			if (required[p] > 0) {
				required[p] -= pieces[p];
				if (required[p] < 0) {
					required[p] = 0;
				}
				val += 25 * pieces[p];
			}
			if (required[p] == 0) {
				nonzeroes--;
			}
		}
		if (nonzeroes == 0) {
			state = gameState.WIN;
		}
		return val;
	}
}
