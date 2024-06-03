package org.esper.filters;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.runtime.client.EPEventService;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;
import org.orbit.Logger;
import org.orbit.Main;
import org.orbit.Parameters;
import org.orbit.encoders.ar.AREncoderFactory;
import org.orbit.encoders.brave.BraveEncoderFactory;
import org.orbit.encoders.iar.IAREncoderFactory;
import org.orbit.filters.AnswerByAnswerFilter;

import java.io.IOException;
import java.util.*;

import static org.orbit.Parameters.*;


public class AnswerByAnswerListener implements UpdateListener {


    AnswerByAnswerFilter filter;

    public AnswerByAnswerListener(Logger statLog) {

        switch (semantics) {
            case AR:
                filter = new AnswerByAnswerFilter(new HashMap<>(), new HashMap<>(), new AREncoderFactory(Parameters.repairType, Parameters.encodingContradiction), Main.Satisfiability.SAT, statLog);
                break;
            case IAR:
                filter = new AnswerByAnswerFilter(new HashMap<>(), new HashMap<>(), new IAREncoderFactory(Parameters.repairType, Parameters.encodingContradiction), Main.Satisfiability.SAT, statLog);
                break;
            case brave:
                filter = new AnswerByAnswerFilter(new HashMap<>(), new HashMap<>(), new BraveEncoderFactory(Parameters.repairType, Parameters.encodingContradiction), Main.Satisfiability.SAT, statLog);
            default:
                System.out.println("unsupported semantic");
                break;
        }
    }

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement, EPRuntime runtime) {

        EPEventService eventService = runtime.getEventService();
        synchronized (eventService) {
            System.out.println("=== Answer for [" + statement.getName() + "] at [" + eventService.getCurrentTime() + "] ===");

        }
        if (newEvents != null) {
            System.out.println("Insertion or Result Stream");

            try {
                Set<String> strings = log(newEvents);
                strings.forEach(System.out::println);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        if (oldEvents != null) {
            System.out.println("Remove Stream");
            try {
                log(oldEvents);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("=== End Answer ===");

    }


    private Set<String> log(EventBean[] events) throws IOException {

        HashMap<String, ArrayList<String>> confGraph = new HashMap<>();
        HashMap<String, ArrayList<ArrayList<String>>> potAnswersAndCauses = new HashMap<>();

        for (EventBean e : events) {
            Map<String, Object> meb = (Map) e.getUnderlying();
            ArrayList<ArrayList<String>> explanations = new ArrayList<>();

            String[][] causes = (String[][]) meb.get("causes");

            for (String[] cs : causes) {
                ArrayList<String> ex = new ArrayList<>();
                for (String s : cs) {
                    ex.add(s);
                }
                explanations.add(ex);
            }

            Object key = meb.get("ans");
            potAnswersAndCauses.put(key + "", explanations);

            String[] cs = (String[]) meb.get("conflicts");
            ArrayList<String> conflicts = new ArrayList<>();
            for (String c : cs) {
                conflicts.add(c);
            }
            confGraph.put((String) meb.get("tid"), conflicts);

        }

        return filter.filterRemainingAnswers(confGraph, potAnswersAndCauses);
    }

}
