package com.fish_level.leushi;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class SurvivalBoard extends GameBoard {
	protected int lastMatch = EMPTY;
	protected int multiplier = 1;
	
	public SurvivalBoard(LeushiView view, int rows, int cols, Bitmap bottom, Bitmap top, Bitmap[] pieces) {
		super(view, rows, cols, bottom, top, pieces);
	}

	@Override
	public void draw(Canvas c) {
		super.draw(c);
		Bitmap b;
		b = getBitmap(lastMatch);
		if (b != null) {
			Rect pos = new Rect(
					(int)(this.leushiView.getWidth() - (b.getWidth() * 0.75)),
					(int)(b.getHeight() * 1.25),
					(int)(this.leushiView.getWidth() - (b.getWidth() * 0.25)),
					(int)(b.getHeight()*1.75));
			
			c.drawBitmap(b, new Rect(0, 0, b.getWidth(), b.getHeight()), pos, null);
			
			int txtx = (pos.left + pos.right) / 2;//(int)(leushiView.getWidth() * 0.93);
			int txty = pos.bottom + (int)leushiView.textpaint.getTextSize() + 2;//(int)(leushiView.getHeight() * 0.235);
			c.drawText(String.format("x %d", multiplier), txtx, txty, leushiView.textpaint);
		}
	}
	
	@Override
	protected int getMatchScore(int piece) {
		int val;
		if (lastMatch == piece && piece != BOTTOM) {
			if (multiplier < 8) {
				multiplier++;
			}
			val = super.getMatchScore(piece) * multiplier;
		} else {
			val = super.getMatchScore(piece) * multiplier;
			multiplier = 1;
		}
		lastMatch = piece;
		return val;
	}

	@Override
	protected int getCupMatchScore(int[] pieces) {
		lastMatch = BOTTOM;
		int val = super.getCupMatchScore(pieces) * multiplier;
		multiplier = 1;
		return val;
	}
}
