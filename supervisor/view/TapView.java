package supervisor.view;

import supervisor.model.TapMachine;

import java.awt.*;

/**
 * @author Matt Bernhard
 * @version 0.0.1
 *          Date: 7/29/13
 */
public class TapView extends AMachineView {

    private TapMachine machine;

    public TapView(TapMachine tapMachine){

        machine = tapMachine;

        setBackground(Color.GRAY);

    }
    public void updateView() {

        if(machine.isOnline())
            setBackground(Color.GREEN);
        else
            setBackground(Color.GRAY);
    }
}
