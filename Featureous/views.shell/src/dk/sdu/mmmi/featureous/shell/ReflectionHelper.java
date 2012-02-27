package dk.sdu.mmmi.featureous.shell;

import java.lang.reflect.Method;

/**
 *
 * @author andrzejolszak
 */
public class ReflectionHelper {

    public String methods(Class o) {
        StringBuilder sb = new StringBuilder(o.getName());
        sb.append(": \n");
        for (Method m : o.getClass().getMethods()) {
            sb.append(m.getReturnType().getName() + " ");
            sb.append(m.getName());
            sb.append("(");
            boolean first = true;
            for (Class cc : m.getParameterTypes()) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(cc.getName());
                first = false;
            }
            sb.append(")\n");
        }
        return sb.toString();
    }

    public String methods(Class o, String content) {
        StringBuilder sb = new StringBuilder(o.getName());
        sb.append(": \n");
        for (Method m : o.getClass().getMethods()) {
            if (m.getName().contains(content)) {
                sb.append(m.getReturnType().getName() + " ");
                sb.append(m.getName());
                sb.append("(");
                boolean first = true;
                for (Class cc : m.getParameterTypes()) {
                    if (!first) {
                        sb.append(", ");
                    }
                    sb.append(cc.getName());
                    first = false;
                }
                sb.append(")\n");
            }
        }
        return sb.toString();
    }
}
