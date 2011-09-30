/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.utils;

import dk.sdu.mmmi.featureous.core.model.TraceListChangeListener;
import dk.sdu.mmmi.featureous.core.model.TraceSet;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import org.openide.windows.TopComponent;

/**
 *
 * @author ao
 */
public abstract class TraceChangedAggregator implements TraceListChangeListener{

    private final TopComponent tc;
    private TraceSet lastEvt;

    public TraceChangedAggregator(final TopComponent tc) {
        this.tc = tc;
        tc.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent e) {
                if(lastEvt!=null){
                    traceListChanged(lastEvt);
                    lastEvt = null;
                }
            }
        });
    }

    final public void traceListChanged(TraceSet tl) {
        if(tc.isVisible()){
            traceListChangedEvent(tl);
            lastEvt = null;
        }else{
            lastEvt = tl;
        }
    }

    abstract public void traceListChangedEvent(TraceSet tl);
}
