package org.schroe.leushi;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class LeushiActivity extends Activity {
	private MainMenuView menu = null;
	private LeushiView game = null;
	private MainMenuView.MenuItem newGameButton = null;
	private MainMenuView.MenuItem resumeButton = null;
	private MainMenuView.MenuItem quitButton = null;
	private MainMenuView.MenuItem title;
	
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
            menu.addItem(getNewGameButton());
            menu.addItem(getQuitButton());
    	}
    	return menu;
    }
    
    private LeushiView newGame() {
    	game = new LeushiView(this, null);
    	return game;
    }
    
    private LeushiView getGame() {
    	if (game == null) {
    		return newGame();
    	} else {
    		return game;
    	}
    }
    
    private MainMenuView.MenuItem getTitleButton() {
    	if (title == null) {
    		title = new MainMenuView.MenuItem(BitmapFactory.decodeResource(getResources(), R.drawable.title)) {
    			@Override
    			public void onClick() {
    				// The blackest of the blackest black... NNNNNNNNNOOOOOOOOOOOOOOOOOOOOOOOOOOOTHIIIIIIINNNNNNNNGGGGGGGGGGGGG
    			}
    		};
    	}
    	return title;
    }
    
    private MainMenuView.MenuItem getNewGameButton() {
    	if (newGameButton == null) {
    		newGameButton = new MainMenuView.MenuItem(BitmapFactory.decodeResource(getResources(), R.drawable.new_game)) {
				@Override
	        	public void onClick() {
	        		setContentView(newGame());
	        		getMenu().addItem(getResumeButton());
	        	}
	        };
    	}
    	return newGameButton;
    }
    
    private MainMenuView.MenuItem getResumeButton() {
    	if (resumeButton == null) {
    		resumeButton = new MainMenuView.MenuItem(BitmapFactory.decodeResource(getResources(), R.drawable.resume)) {
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
    		quitButton = new MainMenuView.MenuItem(BitmapFactory.decodeResource(getResources(), R.drawable.quit)) {
				@Override
	        	public void onClick() {
	        		finish();
	        	}
	        };
    	}
    	return quitButton;
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