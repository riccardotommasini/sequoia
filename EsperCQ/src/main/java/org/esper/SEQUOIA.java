package org.esper;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.EventPropertyDescriptor;
import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.common.client.meta.EventTypeApplicationType;
import com.espertech.esper.common.client.meta.EventTypeMetadata;
import com.espertech.esper.common.client.module.Module;
import com.espertech.esper.common.client.module.ParseException;
import com.espertech.esper.common.client.util.EventTypeBusModifier;
import com.espertech.esper.common.client.util.SafeIterator;
import com.espertech.esper.common.internal.event.arr.ObjectArrayEventType;
import com.espertech.esper.common.internal.type.AnnotationTag;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompiler;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.*;
import org.esper.filters.AnswerByAnswerListener;
import org.orbit.Logger;

import java.io.*;
import java.util.*;

import static org.orbit.Parameters.*;
import static org.orbit.Parameters.repairType;

public class SEQUOIA {

    private static String stream_file;

    public static void main(String[] args) throws IOException, ParseException, EPCompileException, EPDeployException, InterruptedException {

        stream_file = "/sequoia0.stream";

        File queryFile = new File(Objects.requireNonNull(SEQUOIA.class.getResource("/SEQUOIA.epl")).getPath());
        String statementId = "Answers";//BasicProjectionB;BasicSelectionA;BasicSelectionB;BasicWindowedAggregation;BasicGroupedAggregation"; // you can run multiple statement at time separating them using semicolon

//        File queryFile = new File(Objects.requireNonNull(Main.class.getResource("/02_tables.epl")).getPath());
//        String statementId = "PopulateTableA;PopulateTableAgg1;PopulateTableTableAgg2;PullTableAgg"; // you can run multiple statement at time separating them using semicolon

//        File queryFile = new File(Objects.requireNonNull(Main.class.getResource("/03_windows.epl")).getPath());
//        String statementId = "LengthWindow;LengthWindowAggregate;TimeWindowSliding;TimeWindowHopping;TimeWindowTumbling;TimeBatch;KeepAll;KeepAllSnapshot;LastEvent;UniqueWindow;UniqueSnapshot;RankWindow;RankSnapshot";

//        File queryFile = new File(Objects.requireNonNull(Main.class.getResource("/04_joins.epl")).getPath());
//        String statementId = "InnerJoin;LeftJoin;FullOuterJoin;UnidirectionalJoin;StreamTableJoin";//

//        File queryFile = new File(Objects.requireNonNull(Main.class.getResource("/05_r2sop.epl")).getPath());
//        String statementId = "DStreamOutput";//

        // 01_basics.epl includes examples for streaming projection and selection, with variants in EPL syntax (selection) on stream and tables
        // available statements: SimpleProjectionA, SimpleProjectionB, SimpleSelectionA, SimpleSelectionB

        // 04_aggregations.epl includes examples of basics aggregation function (max, min, avg), group by, order by, having
        // 04_joins.epl contains examples of stream-stream and stream-table joins
        // available statements: LeftJoin, UnidirectionalJoin


        Configuration config = new Configuration();

        run(queryFile, statementId, config);

    }


    private static void run(File queryFile, String statementId, Configuration config) throws IOException, ParseException, EPCompileException, EPDeployException, InterruptedException {
        config.getRuntime().getThreading().setInternalTimerEnabled(false);
        config.getCompiler().getByteCode().setAccessModifiersPublic();
        config.getCompiler().getByteCode().setBusModifierEventType(EventTypeBusModifier.BUS);

        EPCompiler compiler = EPCompilerProvider.getCompiler();

        EPRuntime esper = EPRuntimeProvider.getDefaultRuntime(config);
        esper.initialize();

        EPEventService eventService = esper.getEventService();
        System.out.println(eventService.getCurrentTime());
        eventService.advanceTime(0);


        Module mod = compiler.readModule(queryFile);

        CompilerArguments compilerArguments = new CompilerArguments(config);
        EPCompiled compiled = compiler.compile(mod, compilerArguments);

        EPDeployment deploy = esper.getDeploymentService().deploy(compiled);

        List<Thread> threadList = new ArrayList<>();

        Map<String, EventPropertyDescriptor[]> eventSchemas = new HashMap<>();


        Logger statLog = new Logger(statLogFile, printDetails);
        statLog.lognl(conflictGraphFile);
        statLog.lognl(potentialAnswersAndCausesFile);
        statLog.lognl("Semantic selected : " + semantics + " with repairs: " + repairType);


        for (EPStatement statement : deploy.getStatements()) {
            AnnotationTag annotation = (AnnotationTag) statement.getAnnotations()[0];
            if (annotation.value().equals("DML")) {
                if (statementId.contains(statement.getName())) {
                    if (statement.getName().contains("Pull")) {
                        threadList.add(new Thread(() -> {
                            pullTable(statement);
                        }));
                    } else {
                        statement.addListener(new AnswerByAnswerListener(statLog));
                        statement.addListener(new LogListener());
                    }
                }
            } else {
                //Reading input data file
                EventType eventType = statement.getEventType();
                EventPropertyDescriptor[] propertyDescriptors = eventType.getPropertyDescriptors();
                EventTypeMetadata metadata = eventType.getMetadata();
                EventTypeApplicationType applicationType = metadata.getApplicationType();
                if (applicationType.equals(EventTypeApplicationType.MAP)) {
                    eventSchemas.put(eventType.getName(), propertyDescriptors);
                }
            }
        }


        threadList.add(new Thread(() -> {

            String input_file = SEQUOIA.class.getResource(stream_file).getPath();
            int tuple_id = 0;
            try {

                FileReader in = new FileReader(input_file);
                ;
                BufferedReader bufferedReader = new BufferedReader(in);
                // Reading the first line of the file for the event schema

                String e = bufferedReader.readLine();

                while (e != null) {
                    String[] data = e.replace("[", "").replace("]", "").split(",");
                    long nextTime = Long.parseLong(data[data.length - 1].trim());
                    long currentTime = eventService.getCurrentTime();


                    EventPropertyDescriptor[] propertyDescriptors = eventSchemas.get("StreamA");

                    Map<String, Object> event = new HashMap<>();


                    for (int i = 0; i < propertyDescriptors.length; i++) {

                        Object value = data[i].trim();
                        if (Long.class.equals(propertyDescriptors[i].getPropertyType())) {
                            value = Long.parseLong(data[i].trim());
                        } else if (Integer.class.equals(propertyDescriptors[i].getPropertyType())) {
                            value = Integer.parseInt(data[i].trim());
                        } else if (Double.class.equals(propertyDescriptors[i].getPropertyType())) {
                            value = Double.parseDouble(data[i].trim());
                        }

                        event.put(propertyDescriptors[i].getPropertyName(), value);
                    }


                    if (currentTime > nextTime) continue; //out of order
                    else if (currentTime < nextTime) {
//                        System.err.println("Got an [" + data[0].trim() + "] at [" + nextTime + "]");
                        eventService.advanceTime(nextTime);
                    }

                    eventService.sendEventMap(event, "StreamA");

//                    pullTable(esper.getDeploymentService().getStatement(deploy.getDeploymentId(), "PWindowScope"));

                    e = bufferedReader.readLine();

//                    Create a realistic pace for the execution
//                    Thread.sleep(1000);

                }


            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
//            } catch (InterruptedException ex) {
//                throw new RuntimeException(ex);
            }
        }));

        threadList.forEach(Thread::start);

    }

    private static void pullTable(EPStatement statement) {
        SafeIterator<EventBean> eventBeanSafeIterator = statement.safeIterator();
        while (eventBeanSafeIterator.hasNext()) {
            EventBean next = eventBeanSafeIterator.next();
            if (next.getEventType() instanceof ObjectArrayEventType) {
                String[] propertyNames = next.getEventType().getPropertyNames();
                for (String pn : propertyNames) {
                    System.out.println(pn + "=" + next.get(pn));
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


}