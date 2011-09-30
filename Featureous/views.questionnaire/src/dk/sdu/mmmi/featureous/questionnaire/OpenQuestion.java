/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.questionnaire;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;

/**
 *
 * @author ao
 */
public class OpenQuestion extends JPanel implements Question{
    private final JTextPane edit;

    public OpenQuestion(String question) {
        super(new BorderLayout());
        setMinimumSize(new Dimension(200, 200));
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        add(new JLabel(question), BorderLayout.NORTH);
        edit = new JTextPane();
        edit.setMinimumSize(new Dimension(200, 200));
        edit.setBorder(BorderFactory.createEtchedBorder());
        add(edit, BorderLayout.CENTER);
    }

    public String getAnswer(){
        return edit.getText();
    }
}
