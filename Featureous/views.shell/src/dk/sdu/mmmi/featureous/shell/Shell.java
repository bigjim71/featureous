/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.shell;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.util.JConsole;
import dk.sdu.mmmi.featureous.core.controller.Controller;
import dk.sdu.mmmi.featureous.core.model.ClassModel;
import dk.sdu.mmmi.featureous.core.model.OrderedBinaryRelation;
import dk.sdu.mmmi.featureous.core.model.TraceModel;
import dk.sdu.mmmi.featureous.explorer.api.AbstractTraceView;
import dk.sdu.mmmi.featureous.explorer.spi.FeatureTraceView;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 *
 * @author ao
 */
@ServiceProvider(service = FeatureTraceView.class)
public class Shell extends AbstractTraceView {

    public Shell() {
        setupAttribs("Featureous shell", "Featureous shell", "opensourceicons/png/orangeyellow/edit.png");
    }

    @Override
    public void createView() {
        final JConsole c = new JConsole();
        this.add(c, BorderLayout.CENTER);
        JPanel p = new JPanel(new FlowLayout());
        this.add(p, BorderLayout.SOUTH);

        final Interpreter i = new Interpreter(c);
        new Thread(i).start(); // start a thread to call the run() method

        try {
            i.set("controller", Controller.getInstance());
            i.set("traceSet", Controller.getInstance().getTraceSet().getFirstLevelTraces());
            i.set("ref", new ReflectionHelper());
            i.eval("import dk.sdu.mmmi.featureous.core.model.*;");
        } catch (EvalError ex) {
            Exceptions.printStackTrace(ex);
        }

        JButton b1 = new JButton("Data extraction");
        b1.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                JTextPane tp = (JTextPane) c.getViewport().getView();
                tp.setText(tp.getText() + "for(TraceModel tm : traceSet){\n"
                        + "  String name = tm.getName();\n"
                        + "  print(name + \": \\n\");\n"
                        + "  //Set<OrderedBinaryRelation<String, Integer>> invs = tm.getMethodInvocations();\n"
                        + "  for(ClassModel cm : tm.getClassSet()){\n"
                        + "    String pkgName = cm.getPackageName();\n"
                        + "    String className = cm.getName();\n"
                        + "    print(\" - \" + className + \"\\n\");\n"
                        + "    Set<String> methods = cm.getAllMethods();\n"
                        + "    //Set<String> objsCreatedByFeat = cm.getInstancesCreated();\n"
                        + "    //Set<String> objsUsedByFeat = cm.getInstancesUsed();\n"
                        + "  }\n"
                        + "};");
            }
        });
        p.add(b1);

        JButton b2 = new JButton("Reflection");
        b2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                JTextPane tp = (JTextPane) c.getViewport().getView();
                tp.setText(tp.getText() + "print(ref.methods(controller.getClass()));\n"
                        + "print(ref.methods(controller.getClass(), \"get\"));");
            }
        });
        p.add(b2);
    }

    @Override
    public void closeView() {
        removeAll();
        revalidate();
    }

    @Override
    public TopComponent createInstance() {
        return new Shell();
    }

    @Override
    public String getGuiMode() {
        return "output";
    }
}
