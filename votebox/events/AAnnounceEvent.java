package votebox.events;

/**
 * An abstract class that will take care of the serial number all events must have
 *
 * @author Matt Bernhard
 */
public abstract class AAnnounceEvent implements IAnnounceEvent {

    /** The serial number of the machine incurring this event */
    private int serial;

    public AAnnounceEvent(int serial){
        this.serial = serial;
    }

    /**
     * @return the serial number of the sender of this message
     */
    public int getSerial() {
        return serial;
    }

}
