package org.example;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.module.Module;
import com.espertech.esper.common.client.module.ParseException;
import com.espertech.esper.common.client.util.EventTypeBusModifier;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompiler;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.*;
import com.espertech.esper.common.client.configuration.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Main {

    public static void main(String[] args) throws IOException, ParseException, EPCompileException, EPDeployException {

        String query_file = args.length > 0 ? args[0] : Objects.requireNonNull(Main.class.getResource("/basics.epl")).getPath();
        String input_file = args.length > 2 ? args[2] : Objects.requireNonNull(Main.class.getResource("/A.stream")).getPath();

        String eventTypeName = "Event";
        String statementId = "SimpleSelect";

        Configuration config = new Configuration();
        config.getRuntime().getThreading().setInternalTimerEnabled(false);
        config.getCompiler().getByteCode().setAccessModifiersPublic();
        config.getCompiler().getByteCode().setBusModifierEventType(EventTypeBusModifier.BUS);

        EPCompiler compiler = EPCompilerProvider.getCompiler();

        //Reading input data file
        FileReader in = new FileReader(input_file);
        BufferedReader bufferedReader = new BufferedReader(in);


        System.out.println("Event Schema is Registered.");

        EPRuntime esper = EPRuntimeProvider.getDefaultRuntime(config);
        esper.getEventService().clockExternal();
        esper.getEventService().advanceTime(0);

        esper.initialize();
        File queryFile = new File(query_file);

        Module mod = compiler.readModule(queryFile);

        EPCompiled compiled = compiler.compile(mod, new CompilerArguments(config));

        EPDeployment deploy = esper.getDeploymentService().deploy(compiled);

        EPStatement statement = esper.getDeploymentService().getStatement(deploy.getDeploymentId(), statementId);
        statement
                .addListener((newEvents, oldEvents, statement1, runtime) -> {

                    System.out.println("=== Begin Answer at [" + runtime.getEventService().getCurrentTime() + " ] ===");

                    if (newEvents != null) {
                        System.out.println("New Events or Snapshot");
                        for (EventBean newEvent : newEvents) {
                            System.out.println(newEvent.getUnderlying());
                        }
                    }

                    if (oldEvents != null) {
                        System.out.println("Old Events");
                        for (EventBean newEvent : oldEvents) {
                            System.out.println(newEvent.getUnderlying());
                        }
                    }

                    System.out.println("=== End Answer ===");

                });

        System.out.println("Query file created and compiled.");

        long timestamp = 1000;

        // Reading the first line of the file for the event schema
        String s = bufferedReader.readLine();
        System.out.println(s);
        String[] schema = s.trim().split(",");

        System.out.println("Start Streaming");
        String e = bufferedReader.readLine();
        while (e != null) {

            String[] data = e.replace("[", "").replace("]", "").split(",");

            Map<String, Object> event = new HashMap<>();
            for (int i = 0; i < schema.length; i++) {
                String[] attribute = schema[i].split("\\.");
                Object value;
                switch (attribute[1]) {
                    case "integer":
                        value = Integer.parseInt(data[i].trim());
                        break;
                    case "long":
                        value = Long.parseLong(data[i].trim());
                        break;
                    default:
                        value = data[i].trim();
                }
                event.put(attribute[0], value);
            }

            if (esper.getEventService().getCurrentTime() < timestamp)
                esper.getEventService().advanceTime(timestamp);

            esper.getEventService().sendEventMap(event, eventTypeName);
            timestamp += 1000;
            e = bufferedReader.readLine();
        }

        //close file
        bufferedReader.close();
        in.close();
    }


}