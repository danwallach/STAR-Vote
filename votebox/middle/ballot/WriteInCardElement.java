package votebox.middle.ballot;

import votebox.middle.Properties;


/**
 * A WriteInCardElement is a CardElement that can be toggled and focused on.
 * For the purposes of the voting machine runtime, toggling can be thought of as
 * the voter's expression of preference. Focusing is strictly a gui capability.
 * The "focused" element simply is the element which the user is currently
 * looking at. The only reason this state is supported model side is because the
 * model needs to define an image to hand to the view for when the element is
 * focused. This is important, since the voting machine should do no rendering
 * work during runtime. The focused, state, however unlike the selected state,
 * has nothing to do with the model.< para>
 *
 * In order to gain the focusing capability, this class must implement the
 * IFocusable interface. This class also extends CardElement, because the
 * WriteInCardElement is a special kind of ballot element -- one that can be
 * selected and have its corresponding image be rewritten.< para>
 *
 * @author Mircea C. Berechet
 * Date added to project: 08/06/2013
 */

public final class WriteInCardElement extends SelectableCardElement{

    public WriteInCardElement(String uid, Properties properties) {
        super(uid, properties);
    }

}