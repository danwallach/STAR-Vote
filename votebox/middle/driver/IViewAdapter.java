package votebox.middle.driver;


import votebox.middle.view.IView;

/**
 * This class represents an adapter specifically for a view
 *
 * @author matt
 */
public interface IViewAdapter extends IAdapter {

    /**
     * @return the IView object for use by the write-in application
     */
   public IView getView();



}
