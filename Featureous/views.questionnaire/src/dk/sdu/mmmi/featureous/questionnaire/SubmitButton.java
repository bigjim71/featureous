/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.questionnaire;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import javax.swing.JButton;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor;

/**
 *
 * @author ao
 */
public class SubmitButton extends JButton {

    private final List<Question> scs;

    public SubmitButton(List<Question> scs) {
        super("Submit");
        this.scs = scs;
        addActionListener(al);
    }
    private final ActionListener al = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Featureous: generating static traces");
                    progressHandle.start();
                    progressHandle.switchToIndeterminate();
                    
                    PostMethod method = null;
                    BufferedReader br = null;

                    try {
                        HttpClient client = new HttpClient();
                        client.getParams().setParameter("http.useragent", "Featureous plugin");

                        method = new PostMethod("http://featureous.org/submit.php");
                        method.addParameter("version", "\"" + Questionnaire.QUESTIONNAIRE_VERSION + "\"");
                        for (Question sc : scs) {
                            method.addParameter("q" + scs.indexOf(sc), "\"" + sc.getAnswer() + "\"");
                        }

                        client.setTimeout(7000);
                        int returnCode = client.executeMethod(method);

                        if (returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
                            // still consume the response body
                            method.getResponseBodyAsString();
                        } else {
                            br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
                            String readLine;
                            while (((readLine = br.readLine()) != null)) {
                            }
                        }
                        String msg = "Your feedback has been sent! Thanks for your time!";
                        NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(nd);
                    } catch (Exception ee) {
                        String msg = "Error sending your feedback... We would be grateful if you took a screenshot of your answers and sent it to ao@mmmi.sdu.dk .";
                        NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(nd);
                    } finally {
                        if(method!=null){
                            method.releaseConnection();
                        }
                        if (br != null) {
                            try {
                                br.close();
                            } catch (Exception fe) {
                            }
                        }
                        progressHandle.finish();
                    }
                }
            });
        }
    };
}
