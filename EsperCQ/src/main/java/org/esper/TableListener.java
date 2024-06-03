package org.esper;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.internal.event.arr.ObjectArrayEventType;
import com.espertech.esper.common.internal.event.map.MapEventType;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;

import java.util.Arrays;
import java.util.HashMap;


public class TableListener implements UpdateListener {
    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement, EPRuntime runtime) {
        System.out.println("=== Answer for [" + statement.getName() + "] at [" + runtime.getEventService().getCurrentTime() + "] ===");

        if (newEvents != null) {
            System.out.println("Insertion or Result Stream");
            log(newEvents);
        }

        if (oldEvents != null) {
            System.out.println("Remove Stream");
            log(oldEvents);
        }

        System.out.println("=== End Answer ===");

    }


    private void log(EventBean[] oldEvents) {
        for (EventBean oldEvent : oldEvents) {
            EventType eventType = oldEvent.getEventType();
            if (eventType instanceof ObjectArrayEventType) {
                String[] propertyNames = eventType.getPropertyNames();
                String header = "|";
                for (String pn : propertyNames) {

                    header = header + pn + "|";

                }

                System.out.println(header);

                for (String pn : propertyNames) {
                    String out;
                    if (oldEvent.get(pn) instanceof String[]) {
                        out = Arrays.deepToString((String[]) oldEvent.get(pn));
                    } else
                        out = oldEvent.get(pn).toString();
                    System.out.println(pn + "=" + out);
                }
            } else if (eventType instanceof MapEventType) {
                String[] propertyNames = eventType.getPropertyNames();

                String header = "|";
                for (String pn : propertyNames) {

                    header = header + pn + "|";

                }

                System.out.println(header);

                for (String pn : propertyNames) {
                    String out = "|";
                    if (oldEvent.get(pn) instanceof String[]) {
                        out = out + Arrays.deepToString((String[]) oldEvent.get(pn)).trim() + "|";
                    } else {
                        Object o = oldEvent.get(pn);
                        if (o instanceof HashMap) {
                            String[] propertyNames2 = eventType.getPropertyNames();
                            for (String pn2 : propertyNames2) {
                                if (oldEvent.get(pn2) instanceof String[]) {
                                    out = Arrays.deepToString((String[]) o).trim() + "|";
                                } else
                                    out = o + "|";
                                System.out.println(out);
                            }
                        } else if (o != null)
                            out = o + "|";
                        ;
                    }
                    System.out.println(out);
                }
            } else
                System.out.println(oldEvent.getUnderlying());
        }
    }

}
