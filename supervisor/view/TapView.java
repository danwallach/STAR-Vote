package supervisor.view;

import supervisor.model.TapMachine;

import javax.swing.*;
import java.awt.*;

/**
 * This is a Tap ribbon that is placed in the left panel of the Supervisor's ActiveUI and represents
 * whether or not the Tap is responsive and on the network.
 *
 * @author Matt Bernhard
 * @version 0.0.1
 *          Date: 7/29/13
 */
public class TapView extends AMachineView{

    private TapMachine machine;

    private JLabel statusLabel;

    private static Color activeColor = new Color(0,170,0);

    public TapView(TapMachine tapMachine){

        machine = tapMachine;

        statusLabel = new JLabel();
        setLayout(new BorderLayout());
        add(statusLabel, BorderLayout.CENTER);

        setBorder(BorderFactory.createEtchedBorder());

        updateView();
    }

    public void setMachine(TapMachine m){
        machine = m;
    }

    public void updateView() {

        if(machine != null && machine.isOnline()){
            setBackground(activeColor);
            statusLabel.setText("             TAP  Active             ");
        }else{
            setBackground(Color.GRAY);
            statusLabel.setText("             TAP Offline             ");
        }
    }
}
