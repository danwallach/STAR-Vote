/**
 * This file is part of VoteBox.
 * 
 * VoteBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 * 
 * You should have received a copy of the GNU General Public License
 * along with VoteBox, found in the root of any distribution or
 * repository containing all or part of VoteBox.
 * 
 * THIS SOFTWARE IS PROVIDED BY WILLIAM MARSH RICE UNIVERSITY, HOUSTON,
 * TX AND IS PROVIDED 'AS IS' AND WITHOUT ANY EXPRESS, IMPLIED OR
 * STATUTORY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, WARRANTIES OF
 * ACCURACY, COMPLETENESS, AND NONINFRINGEMENT.  THE SOFTWARE USER SHALL
 * INDEMNIFY, DEFEND AND HOLD HARMLESS RICE UNIVERSITY AND ITS FACULTY,
 * STAFF AND STUDENTS FROM ANY AND ALL CLAIMS, ACTIONS, DAMAGES, LOSSES,
 * LIABILITIES, COSTS AND EXPENSES, INCLUDING ATTORNEYS' FEES AND COURT
 * COSTS, DIRECTLY OR INDIRECTLY ARISING OUR OF OR IN CONNECTION WITH
 * ACCESS OR USE OF THE SOFTWARE.
 */

package votebox.middle.view;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;

public class AWTView extends AView {

    public static final int WINDOW_WIDTH        = 1600;
    public static final int WINDOW_HEIGHT       = 900;
    
	public static final int CAST_BALLOT_BUTTON  = KeyEvent.VK_C;
	public static final int KILL_BUTTON         = KeyEvent.VK_X;
	public static final int NEXT_PAGE_BUTTON    = KeyEvent.VK_PERIOD;
	public static final int PREV_PAGE_BUTTON    = KeyEvent.VK_COMMA;
	public static final int SELECT_BUTTON       = KeyEvent.VK_ENTER;
	public static final int LEFT_BUTTON         = KeyEvent.VK_LEFT;
	public static final int RIGHT_BUTTON        = KeyEvent.VK_RIGHT;
	public static final int UP_BUTTON           = KeyEvent.VK_UP;
	public static final int DOWN_BUTTON         = KeyEvent.VK_DOWN;
	public static final int NEXT_BUTTON         = KeyEvent.VK_N;
	public static final int PREVIOUS_BUTTON     = KeyEvent.VK_P;

	private Rectangle   _bounds;
	private DisplayMode _returnMode;
	private boolean _isWindowed;

	private volatile Frame _frame               = new Frame();
	private volatile BufferedImage _bufferImg   = new BufferedImage(WINDOW_WIDTH, WINDOW_HEIGHT, BufferedImage.TYPE_INT_ARGB);

	private double _scaleX = -1;
	private double _scaleY = -1;

    /**
     * A variable that will only allow focusing with orange backgrounds
     * if and only if a key on the keyboard/other input device is pressed,
     * not including the mouse
     */
    private boolean focusEnabled = false;

    public boolean isWindowed() { return _isWindowed; }

    /**
     * Constructor for the view
     *
     * @param isWindowed    whether or not the view is windowed
     * @param allowScaling  whether or not to allow scaling
     * @param baseWidth     the base width setting
     * @param baseHeight    the base height setting
     */
    public AWTView(boolean isWindowed, boolean allowScaling, int baseWidth, int baseHeight){

		super();

		_isWindowed = isWindowed;

		if (isWindowed){
			_yoffset = 10;
			_scaleX = 1.0;
			_scaleY = 1.0;
		}
        else {
			_scaleX = ((double)baseWidth)  / (double)WINDOW_WIDTH;
			_scaleY = ((double)baseHeight) / (double)WINDOW_HEIGHT;
		}

		if(!allowScaling) {
            _scaleX = 1.0;
            _scaleY = 1.0;
        }

	}

    /**
     * Constructor for the view using default settings for base height and width
     *
     * @param windowed      whether or not the view is windowed
     * @param allowScaling  whether or not to allow scaling
     */
	public AWTView(boolean windowed, boolean allowScaling) {
		this(windowed, allowScaling, WINDOW_WIDTH, WINDOW_HEIGHT);
	}

	/**
	 * Finds the complement of the scaling factor.
	 * 
	 * This is abstract out in case we ever need other-than-linear scaling.
	 * 
	 * @param scale     the scaling factor
	 * @return          the complement of the scaling factor
	 */
	protected double reverseScale(double scale)
	{
		return 1.0/scale;  
	}

	/**
	 * @see votebox.middle.view.IView#clearDisplay()
	 */
	public synchronized  void clearDisplay() {

		if (_frame.getGraphics() == null)
			return;

		Graphics graphics = _frame.getGraphics();

		graphics.setClip(_bounds);
		graphics.clearRect(_bounds.x, _bounds.y, _bounds.width, _bounds.height);

		_hitboxMap.clear();
		_currentDrawables.clear();
	}

	/**
	 * @see votebox.middle.view.IView#dispose()
	 */
	public synchronized void dispose() {

		GraphicsDevice dev = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

		try {
			if (!_isWindowed)
				dev.setDisplayMode(_returnMode);

			_frame.setVisible(false);
			_frame.dispose();
		}
		catch (Exception e) { e.printStackTrace(); }

		dev.setFullScreenWindow( null );
	}

	/**
	 * @see votebox.middle.view.IView#invalidate(votebox.middle.view.IDrawable)
	 */
	public synchronized void invalidate(IDrawable drawable) {

		deliver(EventType.BEGIN_PAGE_REDRAW, InputEvent.NONE);

		try {

			if (_frame.getGraphics() == null)
				return;

			boolean buffering  = false;

			Graphics graphics;

			if (_scaleX != 1.0 || _scaleY != 1.0) {
				graphics = _bufferImg.getGraphics();
				buffering = true;
			}
            else {
				graphics = _frame.getGraphics();
			}

			/* Set the clipping region to the bounds of the drawable. */
			graphics.setClip( drawable.getX(), drawable.getY() + _yoffset,
					((BufferedImage) drawable.getImage().getImage()).getWidth(),
					((BufferedImage) drawable.getImage().getImage()).getHeight() );

			/* Find the current elements which are in this new drawing area. */
			HashSet<IDrawable> redrawset = new HashSet<>();

            /* Check the current drawables */
			for (IDrawable d : _currentDrawables) {

                Rectangle r = graphics.getClipBounds();

                if (_hitboxMap.keySet().contains(d)) {

                    Rectangle drawableRectangle = _hitboxMap.get(d);

                    if (r != null) {
                        if (r.contains(drawableRectangle)) redrawset.add(d);
                    }
                    else System.err.println(">>> ERROR: The bounding rectangle does not exist! Drawables have not been created properly!");

                }
                else System.err.println(">>> ERROR: The IDrawable " + d.getUniqueID() + " is not in the list of keys: " + _hitboxMap.keySet().toString());

            }

			/* Draw the background image */
			if (_background != null)
				graphics.drawImage((Image)_background.getImage().getImage(), _background.getX(), _background.getY() + _yoffset, null);

			/* Draw the set, but in their original order, not the invalidated order. */
			for (IDrawable id : _currentDrawables)
				if (redrawset.contains(id))
					graphics.drawImage((Image)id.getImage().getImage(), id.getX(), id.getY() + _yoffset, null);

			if (buffering)
				_frame.getGraphics().drawImage(_bufferImg.getScaledInstance((int)(WINDOW_WIDTH * _scaleX), (int)(WINDOW_HEIGHT * _scaleY), Image.SCALE_SMOOTH), 0, 0, null);
		}
		catch (ClassCastException e) { throw new BallotBoxViewException("Problem while invalidating. ", e); }

		deliver(EventType.END_PAGE_REDRAW, InputEvent.NONE);
	}

	/**
	 * @see votebox.middle.view.IView#run(java.lang.Runnable)
	 */
	public void run(Runnable lambda) {

		final AWTView outer = this;

		GraphicsDevice dev = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

		_bounds = dev.getDefaultConfiguration().getBounds();
		_returnMode = dev.getDisplayMode();

		if (_isWindowed) {

            _frame = new Frame(dev.getDefaultConfiguration()) {

                private static final long serialVersionUID = 1L;

				@Override
				public void paint(Graphics g) {

					for (IDrawable d : _currentDrawables)
						try { outer.invalidate(d); }
					    catch (BallotBoxViewException e) { e.printStackTrace(); }
				}
			};

			_frame.setSize(new java.awt.Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

		}
		else {

			_frame = new Frame(dev.getDefaultConfiguration()){

				private static final long serialVersionUID = 1L;

				@Override
				public void paint(Graphics g) {

					for (IDrawable d : _currentDrawables)
						try { outer.invalidate(d); }
					    catch (BallotBoxViewException e) { e.printStackTrace(); }

				}

			};

			_frame.setUndecorated(true);
			_frame.setIgnoreRepaint(false);

			try {
                //dev.getBestConfiguration()
				dev.setFullScreenWindow(_frame);

				DisplayMode[] modes = dev.getDisplayModes();

				DisplayMode bestMatch = modes[0];

                /* Look for the best display mode */
				for (DisplayMode mode : modes) {

                    /* If a perfect match is found, use it */
					if (mode.getWidth() == WINDOW_WIDTH && mode.getHeight() == WINDOW_HEIGHT) {
						bestMatch = mode;
						break;
					}

                    /* Otherwise look for a mode that is better than the best match so far TODO check that this makes sense*/
					if((mode.getWidth()  == WINDOW_WIDTH  && mode.getHeight() > bestMatch.getHeight()) ||
                       (mode.getHeight() == WINDOW_HEIGHT && mode.getWidth()  > bestMatch.getWidth())) bestMatch = mode;

				}

				dev.setDisplayMode(bestMatch);

				if(bestMatch.getWidth() == WINDOW_WIDTH && bestMatch.getHeight() == WINDOW_HEIGHT){
					_scaleX = 1.0;
					_scaleY = 1.0;
				}
			} /* finally? */
			catch (Exception e) { System.err.println("Failed attempt to fullscreen AWTView @ 1024x768 ["+e.getMessage()+"]"); }
		}

		_frame.addKeyListener( new KeyAdapter() {

			public void keyReleased(KeyEvent e) {

                focusEnabled = true;

				switch (e.getKeyCode()) {

				    case CAST_BALLOT_BUTTON:
					    deliver(EventType.CAST_BALLOT, InputEvent.NONE);
					    break;

				    case NEXT_PAGE_BUTTON:
					    deliver(EventType.NEXT_PAGE, InputEvent.NONE);
					    break;

				    case PREV_PAGE_BUTTON:
					    deliver(EventType.PREV_PAGE, InputEvent.NONE);
					    break;

                    case SELECT_BUTTON:
					    deliver(EventType.SELECT, InputEvent.NONE);
					    break;

                    case LEFT_BUTTON:
					    deliver(EventType.LEFT, InputEvent.NONE);
					    break;

                    case RIGHT_BUTTON:
					    deliver(EventType.RIGHT, InputEvent.NONE);
					    break;

                    case UP_BUTTON:
					    deliver(EventType.UP, InputEvent.NONE);
					    break;

                    case DOWN_BUTTON:
					    deliver(EventType.DOWN, InputEvent.NONE);
					    break;

                    case NEXT_BUTTON:
					    deliver(EventType.NEXT, InputEvent.NONE);
					    break;

                    case PREVIOUS_BUTTON:
					    deliver(EventType.PREVIOUS, InputEvent.NONE);
					    break;
				}
			}
		} );

		_frame.addMouseMotionListener( new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				IDrawable d = getFocusableFromHitbox((int)(e.getX() * reverseScale(_scaleX)), (int)(e.getY() * reverseScale(_scaleY)));

				if (d != null)
					deliver(EventType.MOUSE_MOVE, new InputEvent(d));
			}

		} );

		_frame.addMouseListener(new MouseAdapter() {

			private IDrawable __drawable = null;

			public void mousePressed(MouseEvent e) {
				__drawable = getFocusableFromHitbox((int)(e.getX() * reverseScale(_scaleX)), (int)(e.getY() * reverseScale(_scaleY)));
			}

			public void mouseReleased(MouseEvent e) {

				IDrawable d = getFocusableFromHitbox((int)(e.getX() * reverseScale(_scaleX)), (int)(e.getY() * reverseScale(_scaleY)));

                if (d != null && d == __drawable)
					deliver(EventType.MOUSE_DOWN, new InputEvent(d));
			}

		} );

		_frame.setLayout(null);
		_frame.setVisible(true);
		lambda.run();
	}

    public boolean focusEnabled(){
        return focusEnabled;
    }

    /**
     * Gets the frame for use by the write-in GUI
     *
     * @return      the frame
     */
    public Frame getFrame(){
        return _frame;
    }

    @Override
    public IDrawable getBackground() {
        return _background;
    }
}
