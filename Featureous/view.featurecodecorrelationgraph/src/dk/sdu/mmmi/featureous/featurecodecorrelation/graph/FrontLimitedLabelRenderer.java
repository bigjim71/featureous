/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.featurecodecorrelation.graph;

import prefuse.render.LabelRenderer;
import prefuse.visual.VisualItem;

/**
 */
public class FrontLimitedLabelRenderer extends LabelRenderer {

    private int m_maxLength;
    private String m_textField;

    public FrontLimitedLabelRenderer(String textField, int maxLength) {
        super.setTextField(textField);
        m_textField = textField;
        m_maxLength = maxLength;
    }

    public String getText(VisualItem vi) {
        String type = vi.getString(Keys.NODE_TYPE.getName());
        String text = vi.getString(m_textField);
        if(type.equals(Keys.TYPE_CLASS)){
            int lastDot = text.lastIndexOf(".");
            if(lastDot!=-1 && lastDot<text.length()-1){
                text = text.substring(lastDot+1);
            }
            if (text.length() > m_maxLength) {
                return "..." + text.substring(text.length() - m_maxLength);
            }
        }

        return text;
    }
}
