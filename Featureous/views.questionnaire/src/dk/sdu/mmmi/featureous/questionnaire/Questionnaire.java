/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.questionnaire;

import dk.sdu.mmmi.featureous.explorer.api.AbstractTraceView;
import dk.sdu.mmmi.featureous.explorer.spi.FeatureTraceView;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 *
 * @author ao
 */
@ServiceProvider(service=FeatureTraceView.class)
public class Questionnaire extends AbstractTraceView{

    public static final int QUESTIONNAIRE_VERSION = 20110822;

    public Questionnaire() {
        setupAttribs("Feedback questionnaire", "Feedback questionnaire", "opensourceicons/png/blue/chat.png");
    }

    @Override
    public TopComponent createInstance() {
        return new Questionnaire();
    }

    @Override
    public void createView() {
        setLayout(new BorderLayout());
        JLabel title = new JLabel("<html><br><b>Please help us improve Featureous by providing your feedback below:</b><br>"
                + "<br><i> (Please answer as many questions as possible/relevant. Your feedback is anonymous.)</i><br><br> </html>");
        Map<TextAttribute, Object> attr = (Map<TextAttribute, Object>) title.getFont().getAttributes();
        attr.put(TextAttribute.SIZE, 14);
//        attr.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        
        title.setFont(Font.getFont(attr));
        
        add(title, BorderLayout.NORTH);

        List<Question> sqs = createQuestions();

        add(formQuestionPanel(sqs), BorderLayout.CENTER);

        add(new SubmitButton(sqs), BorderLayout.SOUTH);
        this.revalidate();
    }

    private List<Question> createQuestions() {
        List<Question> sqs = new ArrayList<Question>();
        sqs.add(new ScaleQuestion("<html>A1: In the previous project I worked on, I needed to understand source code in a feature-centric manner</html>",
                "Rarely", "Often"));
        sqs.add(new ScaleQuestion("<html>A2: In the previous project I worked on, we had some sort of traceability links from requirements to code</html>",
                "Fully disagree", "Fully agree"));
        sqs.add(new ScaleQuestion("<html>A3: In the previous project I worked on, the implementations of features were explicitly represented and separated from each other (e.g. in separate packages, classes, etc.) </html>",
                "Fully disagree", "Fully agree"));
        
        sqs.add(new ScaleQuestion("<html>B1: In the previous project I worked on, most of the code modifications I have done in the initial development phase were feature-centric</html>",
                "Fully disagree", "Fully agree"));
        sqs.add(new ScaleQuestion("<html>B2: In the previous project I worked on, most of the code modifications I have done in the evolution and maintenance phase were feature-centric</html>",
                "Fully disagree", "Fully agree"));
        
        sqs.add(new ScaleQuestion("<html>C1: My programming experience level is best described as </html>",
                "Novice", "Expert"));
        sqs.add(new ScaleQuestion("<html>C2: Featureous improves my overall understanding of how features are implemented in a program's architecture </html>",
                "Fully disagree", "Fully agree"));
        sqs.add(new ScaleQuestion("<html>C3: Featureous helps me to identify relevant units of source code during feature-wise changes </html>",
                "Fully disagree", "Fully agree"));
        sqs.add(new ScaleQuestion("<html>C4: Featureous helps me to find classes that I can reuse from existing features to implement new features </html>",
                "Fully disagree", "Fully agree"));
        sqs.add(new ScaleQuestion("<html>C5: Featureous helps me to anticipate possible impact of source-code changes on the correcntess of features </html>",
                "Fully disagree", "Fully agree"));
        
        return sqs;
    }

    private JPanel formQuestionPanel(List<Question> sqs){
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        for(Question sq : sqs){
            p.add((Component)sq, gbc);
        }
        gbc.weighty=1;
        p.add(new JPanel(), gbc);
        return p;
    }

    @Override
    public void closeView() {
    }
}
