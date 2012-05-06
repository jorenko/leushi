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
			c.drawBitmap(b,
					new Rect(0, 0, b.getWidth(), b.getHeight()),
					new Rect(
							(int)(this.leushiView.getWidth() - (b.getWidth() * 0.75)),
							(int)(b.getHeight() * 1.25),
							(int)(this.leushiView.getWidth() - (b.getWidth() * 0.25)),
							(int)(b.getHeight()*1.75)), null);
		}
		if (lastMatch >= 0) {
			c.drawText(String.format("x %d", multiplier), (int)(leushiView.getWidth() * 0.93), (int)(leushiView.getHeight() * 0.235), leushiView.textpaint);
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
	protected int getCupMatchScore(int rows) {
		lastMatch = BOTTOM;
		int val = super.getCupMatchScore(rows) * multiplier;
		multiplier = 1;
		return val;
	}
}
