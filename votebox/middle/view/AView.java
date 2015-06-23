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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public abstract class AView implements IView, Runnable{

    protected final HashMap<IDrawable, Rectangle> _hitboxMap;
    protected final LinkedList<IDrawable> _currentDrawables;
    protected final HashMap<EventType, IEventHandler> _handlers;

    protected int _yoffset;
    protected IDrawable _background;

    private Thread _eventDispatcher         = new Thread(this);
    private List<Object[]> _pendingEvents   = new ArrayList<>();
    
    protected AView() {
        _hitboxMap = new HashMap<>();
        _currentDrawables = new LinkedList<>();
        _handlers = new HashMap<>();

        _eventDispatcher.start();
    }

    /**
     * @see votebox.middle.view.IView#draw(votebox.middle.view.IDrawable)
     */
    public void draw(IDrawable element) {
        _currentDrawables.add(element);
        _hitboxMap.put(element, makeHitBox(element));
        invalidate(element);
    }

    /**
     * @see votebox.middle.view.IView#setBackground(votebox.middle.view.IDrawable)
     */
    public synchronized void setBackground(IDrawable element) {
        _background = element;
    }

    /**
     * @see votebox.middle.view.IView#register(votebox.middle.view.EventType,
     *      votebox.middle.view.IEventHandler)
     */
    public synchronized void register(EventType eventType, IEventHandler lambda) {
        _handlers.put( eventType, lambda );
    }

    /**
     * Call this method to create a rectangle that appropriately represents the
     * hit box of a given drawable.
     * 
     * @param drawable the given drawable.
     * @return the rectangle that represents the hitbox of drawable.
     */
    protected Rectangle makeHitBox(IDrawable drawable) {
        return new Rectangle(drawable.getX(), drawable.getY() + _yoffset, drawable.getImage().getWidth(), drawable.getImage().getHeight());
    }

    /**
     * Get the focusable which covers a given (x,y) coordinate.
     * 
     * @param x the given x-coordinate.
     * @param y the given y-coordinate.
     * @return  the focusable which contains the given coordinate pair, or null
     *          if there is no focusable associated with the pair.
     */
    protected IDrawable getFocusableFromHitbox(int x, int y) {

        for (IDrawable d : _hitboxMap.keySet())
            if (d instanceof IFocusable && _hitboxMap.get(d).contains(x, y))
                return d;

        return null;
    }

    /**
     * If a given event type has been registered for, execute its handler.  Returns immediately.
     * 
     * @param type  the event type against which event will be checked
     * @param event the event wrapper to be checked
     */
    protected void deliver(final EventType type, final InputEvent event) {

    	synchronized(_pendingEvents){
    	  _pendingEvents.add(new Object[]{type, event});
    	  _pendingEvents.notify();
    	}
    }
    
    /**
     * Dispatches events (in the order of their arrival)
     */
    public void run(){

    	while(true){

    		synchronized(_pendingEvents){

    			while(_pendingEvents.size() == 0)
					try { _pendingEvents.wait(); }
                    catch (InterruptedException e) { e.printStackTrace(); }
    			
    			while (_pendingEvents.size() > 0) {

    				Object[] params     = _pendingEvents.remove(0);
    				EventType type      = (EventType)params[0];
    				InputEvent event    = (InputEvent)params[1];
    				
    				if(_handlers.containsKey(type))
    					_handlers.get(type).handle(event);
    			}
    		}
    	}
    }

    public IDrawable getBackground(){
        return _background;
    }
}
