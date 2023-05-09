package org.example;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.EventPropertyDescriptor;
import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.internal.event.arr.ObjectArrayEventType;
import com.espertech.esper.common.internal.event.map.MapEventType;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;


public class LogListener implements UpdateListener {
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
                for (String pn : propertyNames) {
                    System.out.println(pn + "=" + oldEvent.get(pn));
                }
            } else
                System.out.println(oldEvent.getUnderlying());
        }
    }

}
