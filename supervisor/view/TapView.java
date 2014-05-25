package supervisor.view;

import supervisor.model.TapMachine;

import javax.swing.*;
import java.awt.*;

/**
 * This is a Tap ribbon that is placed in the left panel of the Supervisor's ActiveUI and represents
 * whether or not the Tap is responsive and on the network.
 *
 * @author Matt Bernhard
 */
public class TapView extends AMachineView{

    /** The mini-model for the Tap */
    private TapMachine machine;

    /** the label that will contain the ribbon */
    private JLabel statusLabel;

    /** A color to show that the machine is active */
    private static Color activeColor = new Color(0,170,0);

    /**
     * A constructor for this mini-view
     *
     * @param tapMachine a mini-machine representation of the Tap, from the supervisor's eyes
     */
    public TapView(TapMachine tapMachine){

        /* First set up the mini-machine */
        machine = tapMachine;

        /* Now create the ribbon */
        statusLabel = new JLabel();
        setLayout(new BorderLayout());
        add(statusLabel, BorderLayout.CENTER);

        /* Set the border, for aesthetic appeal */
        setBorder(BorderFactory.createEtchedBorder());

        /* Update so everything gets filled in nicely */
        updateView();
    }

    /**
     * Allows different Tap machines to be swapped in and out.
     *
     * @param m the new machine
     */
    public void setMachine(TapMachine m){
        machine = m;
    }

    /**
     * Update the view depending on whether or not the machine is online or not
     */
    public void updateView() {

        /* If the machine is non-null and online, set the color to green and display the corresponding message */
        if(machine != null && machine.isOnline()){
            setBackground(activeColor);
            statusLabel.setText("             TAP  Active             ");
        }

        /* Otherwise set the color to gray and show the corresponding message */
        else{
            setBackground(Color.GRAY);
            statusLabel.setText("             TAP Offline             ");
        }
    }
}
