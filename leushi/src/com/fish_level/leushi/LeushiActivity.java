package com.fish_level.leushi;

import org.schroe.leushi.R;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class LeushiActivity extends Activity {
	private MainMenuView menu = null;
	private LeushiView game = null;
	private MainMenuView.MenuItem survivalButton = null;
	private MainMenuView.MenuItem puzzleButton = null;
	private MainMenuView.MenuItem resumeButton = null;
	private MainMenuView.MenuItem quitButton = null;
	private MainMenuView.MenuItem title = null;
	private MainMenuView.MenuItem company = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(getMenu());
    }
    
    private MainMenuView getMenu() {
    	if (menu == null) {
            menu = new MainMenuView(this, null, BitmapFactory.decodeResource(getResources(), R.drawable.menu_background));
            menu.addItem(getTitleButton());
            menu.addItem(getSurvivalButton());
            menu.addItem(getPuzzleButton());
            menu.addItem(getQuitButton());
            menu.addItem(getCompanyButton());
    	}
    	return menu;
    }
    
    private LeushiView newSurvival() {
    	game = new LeushiView(this, null, LeushiView.gameType.SURVIVAL);
    	return game;
    }
    
    private LeushiView newPuzzle() {
    	game = new LeushiView(this, null, LeushiView.gameType.PUZZLE);
    	return game;
    }
    
    private LeushiView getGame() {
    	if (game == null) {
    		return newSurvival();
    	} else {
    		return game;
    	}
    }
    
    private MainMenuView.MenuItem getTitleButton() {
    	if (title == null) {
    		title = new MainMenuView.MenuItem(BitmapFactory.decodeResource(getResources(), R.drawable.title), 0, 0.0) {
    			@Override
    			public void onClick() {
    				// The blackest of the blackest black... NNNNNNNNNOOOOOOOOOOOOOOOOOOOOOOOOOOOTHIIIIIIINNNNNNNNGGGGGGGGGGGGG
    			}
    		};
    	}
    	return title;
    }
    
    private MainMenuView.MenuItem getSurvivalButton() {
    	if (survivalButton == null) {
    		survivalButton = new MainMenuView.MenuItem(BitmapFactory.decodeResource(getResources(), R.drawable.new_survival), 0, 0.54) {
				@Override
	        	public void onClick() {
	        		setContentView(newSurvival());
	        		getMenu().addItem(getResumeButton());
	        	}
	        };
    	}
    	return survivalButton;
    }
    
    private MainMenuView.MenuItem getPuzzleButton() {
    	if (puzzleButton == null) {
    		puzzleButton = new MainMenuView.MenuItem(BitmapFactory.decodeResource(getResources(), R.drawable.new_puzzle), 0, 0.42) {
				@Override
	        	public void onClick() {
	        		setContentView(newPuzzle());
	        		getMenu().addItem(getResumeButton());
	        	}
	        };
    	}
    	return puzzleButton;
    }
    
    private MainMenuView.MenuItem getResumeButton() {
    	if (resumeButton == null) {
    		resumeButton = new MainMenuView.MenuItem(BitmapFactory.decodeResource(getResources(), R.drawable.resume), 0, 0.30) {
				@Override
	        	public void onClick() {
					getGame().resume();
	        		setContentView(getGame());
	        	}
	        };
    	}
    	return resumeButton;
    }
    
    private MainMenuView.MenuItem getQuitButton() {
    	if (quitButton == null) {
    		quitButton = new MainMenuView.MenuItem(BitmapFactory.decodeResource(getResources(), R.drawable.quit), 0, 0.66) {
				@Override
	        	public void onClick() {
	        		finish();
	        	}
	        };
    	}
    	return quitButton;
    }
    
    private MainMenuView.MenuItem getCompanyButton() {
    	if (company == null) {
    		company = new MainMenuView.MenuItem(BitmapFactory.decodeResource(getResources(), R.drawable.fishlevelgames), 0, 0.90) {
    			@Override
    			public void onClick() {
    				// The blackest of the blackest black... NNNNNNNNNOOOOOOOOOOOOOOOOOOOOOOOOOOOTHIIIIIIINNNNNNNNGGGGGGGGGGGGG
    			}
    		};
    	}
    	return company;
    }
	
	@Override
	public void onBackPressed() {
		if (game != null && game.getVisibility() == View.VISIBLE) {
			getGame().pause();
			setContentView(getMenu());
		} else {
			super.onBackPressed();
		}
	}
}