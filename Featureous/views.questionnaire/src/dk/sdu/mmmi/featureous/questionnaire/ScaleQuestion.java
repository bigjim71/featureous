/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.questionnaire;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.font.TextAttribute;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *
 * @author ao
 */
public class ScaleQuestion extends JPanel implements Question{
    private final ButtonGroup bg;
    private final JRadioButton b0;
    private final JRadioButton b1;
    private final JRadioButton b2;
    private final JRadioButton b3;
    private final JRadioButton b4;

    public ScaleQuestion(String question, String min, String max) {
        super(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        setMaximumSize(new Dimension(600, 20));
        JLabel q = new JLabel(question);
        Map<TextAttribute, Object> attr = (Map<TextAttribute, Object>) q.getFont().getAttributes();
        attr.put(TextAttribute.FAMILY, Font.DIALOG);
        attr.put(TextAttribute.SIZE, 12);
//        attr.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
        q.setFont(Font.getFont(attr));
        add(q, BorderLayout.CENTER);
        
        bg = new ButtonGroup();
        b0 = new JRadioButton();
        b1 = new JRadioButton();
        b2 = new JRadioButton();
        b3 = new JRadioButton();
        b4 = new JRadioButton();
        bg.add(b0);
        bg.add(b1);
        bg.add(b2);
        bg.add(b3);
        bg.add(b4);
        JPanel answ = new JPanel(new GridBagLayout());
        JLabel minLabl = new JLabel(min);
        minLabl.setForeground(new Color(45, 80, 23));
        answ.add(minLabl);
        answ.add(b0);
        answ.add(b1);
        answ.add(b2);
        answ.add(b3);
        answ.add(b4);
        JLabel maxLabl = new JLabel(max);
        maxLabl.setForeground(new Color(45, 80, 23));
        answ.add(maxLabl);
        add(answ, BorderLayout.EAST);
    }

    public String getAnswer(){
        if(b0.isSelected()){
            return ""+0;
        }
        if(b1.isSelected()){
            return ""+1;
        }
        if(b2.isSelected()){
            return ""+2;
        }
        if(b3.isSelected()){
            return ""+3;
        }
        if(b4.isSelected()){
            return ""+4;
        }
        return "";
    }
}
