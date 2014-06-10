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

package preptool.model.language;

import java.util.HashMap;

/**
 * A class that contains literal localized strings used by the LayoutManager, so
 * that it can output the correct strings based on the language.
 *
 * @author Corey Shaw
 */
public class LiteralStrings {

    /**
     * Singleton pattern.
     */
    public static final LiteralStrings Singleton = new LiteralStrings();

    /**
     * Singleton pattern - private constructor.<br>
     * Initializes the hash map with all of the LocalizedStrings TODO later will load from files
     */
    private LiteralStrings() {
        map = new HashMap<String, LocalizedString>();

        HashMap<String, Language> langs = new HashMap<String, Language>();
        for (Language lang: Language.getAllLanguages())
            langs.put(lang.getName(), lang);
        Language ENGLISH = langs.get("English");
        Language SPANISH = langs.get("Español");

        LocalizedString ls = new LocalizedString();
        ls.set(ENGLISH, "Instructions \n ");
        ls.set(SPANISH, "Instrucciónes \n ");
        map.put("INSTRUCTIONS_TITLE", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Review Choices \n ");
        ls.set(SPANISH, "Revise las Opciones \n ");
        map.put("REVIEW_TITLE", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Print Ballot \n ");
        ls.set(SPANISH, "Imprimir la Boleta \n ");
        map.put("RECORD_TITLE", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Straight Party");
        ls.set(SPANISH, "Voto Unido");
        map.put("STRAIGHT_PARTY", ls);


        ls = new LocalizedString();
        ls.set(ENGLISH, "Thank you for voting!");
        ls.set(SPANISH, "Gracias por votar!");
        map.put("SUCCESS_TITLE", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "You must make a selection!");
        ls.set(SPANISH, "Usted debe hacer una selección!");
        ls.set(SPANISH, "Usted debe hacer una selección!");
        map.put("NO_SELECTION_TITLE", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "You must make a selection on every page. If you do not want to vote, select 'None of the above'.");
        ls.set(SPANISH, "Usted debe hacer una selección en cada página. Si usted no quiere votar, seleccione 'Ninguna de las Anteriores'.");
        map.put("NO_SELECTION", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Click to go back to the selection screen");
        ls.set(SPANISH, "Haga clic para volver a la página de selección");
        map.put("RETURN_RACE", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "None of the Above");
        ls.set(SPANISH, "Ninguna de las Anteriores");
        map.put("NONE_OF_ABOVE", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Language Selection");
        ls.set(SPANISH, "Selección de Idioma");
        map.put("LANGUAGE_SELECT_TITLE", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Print Ballot");
        ls.set(SPANISH, "Imprimir la Boleta");
        map.put("CAST_BUTTON", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Print Ballot" + '\u2192');
        ls.set(SPANISH, "Imprimir la Boleta" + '\u2192');
        map.put("COMMIT_BUTTON", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Next Page" + '\u2192');
        ls.set(SPANISH, "Página Siguiente" + '\u2192');
        map.put("NEXT_PAGE_BUTTON", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, '\u2190' + "Previous Page");
        ls.set(SPANISH, '\u2190' + "Página Anterior");
        map.put("PREVIOUS_PAGE_BUTTON", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Return");
        ls.set(SPANISH, "Volver");
        map.put("RETURN_BUTTON", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "\n STEP 1 \n Read Instructions");
        ls.set(SPANISH, "\n PASO 1 \n Lea las Instrucciones");
        map.put("SIDEBAR_INSTRUCTIONS", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "\n STEP 2 \n Make your choices");
        ls.set(SPANISH, "\n PASO 2 \n Elija sus opciones");
        map.put("SIDEBAR_MAKE_CHOICES", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "\n STEP 3 \n Review your choices");
        ls.set(SPANISH, "\n PASO 3 \n Revise sus opciones");
        map.put("SIDEBAR_REVIEW_CHOICES", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "\n STEP 4 \n Record your vote");
        ls.set(SPANISH, "\n PASO 4 \n Registre sus voto");
        map.put("SIDEBAR_RECORD_VOTE", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "\n You are now on \n STEP 1 \n Read Instructions");
        ls.set(SPANISH, "\n Ahora se encuentra en \n PASO 1 \n Lea las Instrucciones");
        map.put("SIDEBAR_INSTRUCTIONS_HIGHLIGHTED", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "\n You are now on \n STEP 2 \n Make your choices");
        ls.set(SPANISH, "\n Ahora se encuentra en \n PASO 2 \n Elija sus opciones");
        map.put("SIDEBAR_MAKE_CHOICES_HIGHLIGHTED", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "\n You are now on \n STEP 3 \n Review your choices");
        ls.set(SPANISH, "\n Ahora se encuentra en \n PASO 3 \n Revise sus opciones");
        map.put("SIDEBAR_REVIEW_CHOICES_HIGHLIGHTED", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "\n You are now on \n STEP 4 \n Record your vote");
        ls.set(SPANISH, "\n Ahora se encuentra en \n PASO 4 \n Registre sus voto");
        map.put("SIDEBAR_RECORD_VOTE_HIGHLIGHTED", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Click to go forward to next race");
        ls.set(SPANISH, "Haz clic para avanzar a la página siguiente");
        map.put("FORWARD_NEXT_RACE", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Click to go back to previous race");
        ls.set(SPANISH, "Haga clic para volver a la página anterior");
        map.put("BACK_PREVIOUS_RACE", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Click to see more candidates");
        ls.set(SPANISH, "Haga clic para ver más candidatos");
        map.put("MORE_CANDIDATES", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Click to go back to the review screen");
        ls.set(SPANISH, "Haga clic para volver a la página de revisión");
        map.put("RETURN_REVIEW_SCREEN", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Click to go back to Instructions");
        ls.set(SPANISH, "Haga clic para volver a las Instrucciones");
        map.put("BACK_INSTRUCTIONS", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Click to go to Step 3: Review your choices");
        ls.set(SPANISH, "Haga clic para avanzar al Paso 3: Revise sus opciones");
        map.put("FORWARD_REVIEW", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Click to go back to Step 3: Review your choices");
        ls.set(SPANISH, "Haga clic para volver al Paso 3: Revise sus opciones");
        map.put("BACK_REVIEW", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Click to record your vote");
        ls.set(SPANISH, "Haga clic para registrar sus voto");
        map.put("FORWARD_SUCCESS", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Click to go to Step 2: Make your choices");
        ls.set(SPANISH, "Haga clic para avanzar al Paso 2: Elija sus opciones");
        map.put("FORWARD_FIRST_RACE", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Click to go back to Step 2: Make your choices");
        ls.set(SPANISH, "Haga clic para volver al Paso 2: Elija sus opciones");
        map.put("BACK_LAST_RACE", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Click to go to Step 4: Record your vote");
        ls.set(SPANISH, "Haga clic para avanzar al Paso 4: Registre sus voto");
        map.put("FORWARD_RECORD", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Click to go back to Language Selection");
        ls.set(SPANISH, "Haga clic para volver a la Selección de Idioma");
        map.put("BACK_LANGUAGE_SELECT", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Click to go to Step 1: Instructions");
        ls.set(SPANISH, "Haga clic para avanzar al Paso 1: Instrucciones");
        map.put("FORWARD_INSTRUCTIONS", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "To make your choice, click the candidate's name or the box next to his/her "
                + "\n name. A green checkmark will appear next to your choice. If you want to change "
                + "\n your choice, just click on a different candidate or box.");
        ls.set(SPANISH, "Para seleccionar una opción, haga clic en el nombre del candidato o en la casilla "
                + "\n junto a su nombre. Una marca de verificación verde aparecerá junto a su selección. "
                + "\n Si desea cambiar sus selección, haga clic en un candidato o una casilla diferente.");
        map.put("RACE_INSTRUCTIONS", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "To make your choice, click on the party name or on the box next to the name. "
                + "\n A green checkmark will appear next to your choice. If you want to change "
                + "\n your choice, just click on a different party or box. Selecting a party here will "
                + "\n select candidates from that party on all subsequent screens, where applicable. ");
        ls.set(SPANISH, "Para seleccionar una opción, haga clic en el nombre del partido o en la casilla "
                + "\n que aparece junto al nombre. Una marca de verificación verde aparecerá junto a su "
                + "\n selección. Si desea cambiar su selección, haga clic en un partido o una casilla "
                + "\n diferente. Selección de un partido aquí seleccionará los candidatos de ese partido "
                + "\n en todas las páginas siguientes, donde se da el caso.");
        map.put("PARTY_INSTRUCTIONS", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "To make your choice, click on the response or on the box next to the "
                + "\n response. A green checkmark will appear next to your choice. If you want to change "
                + "\n your choice, just click on a different response or box.");
        ls.set(SPANISH, "Para seleccionar una opción, haga clic en la respuesta o en la casilla que aparece "
                + "\n junto a la respuesta. Una marca de verificación verde aparecerá junto a su selección. "
                + "\n Si desea cambiar su selección, haga clic en una respuesta o una casilla diferente.");
        map.put("PROPOSITION_INSTRUCTIONS", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Below are the choices you have made. If you would like to make changes, "
                + "\n click on the race you would like to change. If you do not want to make changes, "
                + "\n click the 'Next Page' button to go to Step 4. "
                + "\n \n **Your ballot will not be recorded and printed unless you finish step 4.**");
        ls.set(SPANISH, "A continuación se presentan las opciones que ha seleccionado. Si usted desea "
                + "\n cambiarlos haga clic en la carrera que le gustaría cambiar. Si no desea cambios, "
                + "\n haga clic en el botón 'Página siguiente' para ir al paso 4. "
                + "\n \n ** Su boleta no se registrará y se imprimirá menos que usted finaliza el Paso 4. **");
        map.put("REVIEW_INSTRUCTIONS", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "\n Welcome. You are about to begin voting in a mock election at Rice University. "
                + "\n \n There are four steps to voting in this election. "
                + "\n \n Step 1: Finish reading Instructions. "
                + "\n \n Step 2: Make your choice. This is where you will choose your candidates "
                + "\n and vote for or against the propositions. You will make one choice per page. "
                + "\n \n Step 3: Review your choices. This is where you will see all the choices you "
                + "\n have made and be able to make any changes you want. "
                + "\n \n Step 4: Record your vote by clicking on the 'Print Ballot' button. This is the "
                + "\n last step. Once you finish this step, you will not be able to make any changes, "
                + "\n and your vote will be recorded. "
                + "\n \n When you get to each of these four steps, you will see more detailed "
                + "\n instructions. "
                + "\n \n At the bottom of the screen, there will be buttons you can click with your "
                + "\n mouse. Click the 'Next Page' button to go to the next page.");
        ls.set(SPANISH, "\n Bienvenido. Usted está a punto de comenzar la votación en un simulacro de "
                + "\n elecciones en la Universidad Rice. "
                + "\n \n Hay cuatro pasos para votar en esta elección. "
                + "\n \n Paso 1: Finalizar de leer las Instrucciones. "
                + "\n \n Paso 2: Haga su elección. Aquí es donde usted va a elegir a sus candidatos y "
                + "\n votar a favor o en contra de las proposiciones. Va a hacer una opción por página. "
                + "\n \n Paso 3: Revise sus opciones. Aquí es donde podrás ver todas las opciones que haya "
                + "\n hecho y ser capaces de hacer los cambios que desee. "
                + "\n \n Paso 4: Registre su voto haciendo clic en el botón 'Imprimir Boleta'. Este es el "
                + "\n último paso. Una vez que termine este paso, usted no será capaz de hacer cualquier "
                + "\n cambio, y se grabará su voto. "
                + "\n \n Al llegar a cada uno de estos cuatro pasos, podrás ver instrucciones más detalladas. "
                + "\n \n En la parte inferior de la página, habrá botones en los que puede hacer clic con el "
                + "\n ratón. Haga clic en el botón 'Página Siguiente' para ir a la página siguiente. ");
        map.put("INSTRUCTIONS", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "You can not make any changes once you click the 'Next Page' "
                + "\n button. When you click the button, your vote will be officially "
                + "\n recorded and will be printed. "
                + "\n \n If you want to make changes, click the 'Previous Page' button to "
                + "\n go back to the Review Screen. ");
        ls.set(SPANISH, "No se puede hacer ningún cambio tras hacer clic en el botón 'Página Siguiente'. "
                + "\n Al hacer clic en el botón, su voto será registrado oficialmente y será impreso. "
                + "\n \n Si desea hacer cambios, haga clic en el botón 'Página Anterior' para volver a "
                + "\n la página de la revisión. ");
        map.put("RECORD_INSTRUCTIONS", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Please select the language in which you would like to vote.");
        ls.set(SPANISH, "Por favor seleccione el idioma en el que desea votar.");
        map.put("LANGUAGE_SELECT_INSTRUCTIONS", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "\n Your ballot is being printed.  To cast your vote deposit your ballot in the ballot box. \n You may now leave the voting booth. ");
        ls.set(SPANISH, "\n Se imprime su boleta. Para emitir su voto deposita su papeleta en la urna. \n Ahora puede salir de la cabina de votación. ");
        map.put("SUCCESS", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "\n Your provisional ballot is being printed. \n Please see a poll worker to ensure that the proper protocols are being followed for this vote. ");
        ls.set(SPANISH, "\n Se imprime su boleta provisional. \n Por favor, consulte a un trabajador electoral para asegurar que \n los protocolos adecuados se están siguiendo para esta votación.");
        map.put("PROVISIONAL", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Yes");
        ls.set(SPANISH, "Si");
        map.put("YES", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "No");
        ls.set(SPANISH, "No");
        map.put("NO", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "None");
        ls.set(SPANISH, "Nada");
        map.put("NONE", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Page");
        ls.set(SPANISH, "Pagina");
        map.put("PAGE", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "of");
        ls.set(SPANISH, "de");
        map.put("OF", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Override Cancel");
        ls.set(SPANISH, "Anular Cancelación");
        map.put("OVERRIDE_CANCEL_TITLE", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "The election supervisor has requested that this ballot be cancelled. \n Please cancel, or continue voting if this request was sent by mistake.");
        ls.set(SPANISH, "El supervisor electoral ha solicitado que se cancele esta votación. \n Por favor, cancelar, o continuar votando si esta petición se envió por error.");
        map.put("OVERRIDE_CANCEL_INSTRUCTIONS", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Yes, Cancel this Ballot");
        ls.set(SPANISH, "Sí, Cancele la Boleta");
        map.put("OVERRIDE_CANCEL_CONFIRM", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Override Cast");
        ls.set(SPANISH, "Anular Emisión");
        map.put("OVERRIDE_CAST_TITLE", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "The election supervisor has requested that this ballot be recorded. \n Please record this vote, or continue voting if this request was sent by mistake.");
        ls.set(SPANISH, "El supervisor electoral ha solicitado que se registró esta votación. \n Registre esta votación, o continuar votando si esta petición se envió por error.");
        map.put("OVERRIDE_CAST_INSTRUCTIONS", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "Yes, Record and Print this Ballot");
        ls.set(SPANISH, "Sí, Registre e Imprima esta Boleta");
        map.put("OVERRIDE_CAST_CONFIRM", ls);

        ls = new LocalizedString();
        ls.set(ENGLISH, "No, Ignore this Request");
        ls.set(SPANISH, "No, Ignorar este Solicitud");
        map.put("OVERRIDE_DENY", ls);

    }

    /**
     * The map holding all of the strings
     */
    private HashMap<String, LocalizedString> map;

    /**
     * Returns the string for the given key and language
     *
     * @param key the key for the string
     * @param lang the language
     * @return the translated string
     */
    public String get(String key, Language lang) {
        LocalizedString ls = map.get(key);
        if (ls == null){
            System.out.println("Couldn't find " + key);
            throw new IllegalArgumentException("String not found");
        }
        else
            return ls.get(lang);
    }

    /**
     * Returns the LocalizedString for the given key
     *
     * @param key the key for the string
     * @return the LocalizedString
     */
    public LocalizedString get(String key) {
        LocalizedString ls = map.get(key);
        if (ls == null)
            throw new IllegalArgumentException("String not found");
        else
            return ls;
    }

}