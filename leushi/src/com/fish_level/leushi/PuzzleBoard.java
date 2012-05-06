package com.fish_level.leushi;

import android.graphics.Bitmap;

public class PuzzleBoard extends GameBoard {
	public PuzzleBoard(LeushiView view, int rows, int cols, Bitmap bottom, Bitmap top, Bitmap[] pieces) {
		super(view, rows, cols, bottom, top, pieces);
	}
}
