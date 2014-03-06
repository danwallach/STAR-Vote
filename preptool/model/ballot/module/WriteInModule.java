package preptool.model.ballot.module;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import preptool.model.language.Language;
import preptool.view.AModuleView;
import preptool.view.View;
import preptool.view.WriteInModuleView;

/**
 * Created by matt on 2/24/14.
 */
public class WriteInModule extends AModule {


    /**
     * Creates a new Module with the given unique name
     */
    public WriteInModule() {
        super("WriteInModule");
    }

    @Override
    public AModuleView generateView(View view) {
        return new WriteInModuleView(view.getX(), view.getY());
    }

    @Override
    public boolean needsTranslation(Language lang) {
        return false;
    }

    @Override
    public Element toSaveXML(Document doc) {
        Element moduleElt = doc.createElement( "Module" );
        moduleElt.setAttribute( "type", "WriteInModule" );
        moduleElt.setAttribute( "name", getName() );



        return moduleElt;
    }
}
