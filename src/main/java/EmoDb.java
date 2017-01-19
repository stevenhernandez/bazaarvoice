import com.bazaarvoice.emodb.databus.api.Databus;
import com.bazaarvoice.emodb.databus.api.Event;
import com.bazaarvoice.emodb.databus.api.PollResult;
import com.bazaarvoice.emodb.databus.client.DatabusClientFactory;
import com.bazaarvoice.emodb.databus.client.DatabusFixedHostDiscoverySource;
import com.bazaarvoice.emodb.sor.api.*;
import com.bazaarvoice.emodb.sor.client.DataStoreClientFactory;
import com.bazaarvoice.emodb.sor.client.DataStoreFixedHostDiscoverySource;
import com.bazaarvoice.emodb.sor.condition.Conditions;
import com.bazaarvoice.emodb.sor.delta.Delta;
import com.bazaarvoice.emodb.sor.delta.Deltas;
import com.bazaarvoice.emodb.sor.uuid.TimeUUIDs;
import com.bazaarvoice.ostrich.pool.ServicePoolBuilder;
import com.bazaarvoice.ostrich.pool.ServicePoolProxies;
import com.bazaarvoice.ostrich.retry.ExponentialBackoffRetry;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.joda.time.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Steven on 1/15/2017.
 */
public class EmoDb {

    private DataStore dataStore;
    private Databus databus;
    private static final String TABLE = "color_table";

    public void start() throws IOException {
        EventContainer container = loadEvents("src\\main\\resources\\milestone0.txt");
        initializeEmoDb();
        updateTable(container);
        Iterator<Map<String, Object>> result = dataStore.scan(TABLE, null, Long.MAX_VALUE, ReadConsistency.STRONG);
        ColorIndex colorIndex = new ColorIndex();
        while (result.hasNext()) {
            ColorItem colorItem = ColorItem.mapColorItem(result.next());
            colorIndex.put(colorItem.getId(), colorItem);
        }
        colorIndex.listenForQueries();
        databus.subscribe("colorsubscription", Conditions.intrinsic(Intrinsic.TABLE, TABLE), Duration.standardDays(1), Duration.standardDays(1));
        container = loadEvents("src\\main\\resources\\milestone2.txt");
        updateTable(container);
        PollResult pollResult = databus.poll("colorsubscription", Duration.standardMinutes(30), 100);
        List<String> completedEvents = new ArrayList<>();
        for (Event event : pollResult.getEvents()) {
            ColorItem colorItem = ColorItem.mapColorItem(event.getContent());
            colorIndex.put(colorItem.getId(), colorItem);
            completedEvents.add(event.getEventKey());
        }
        databus.acknowledge("colorsubscription", completedEvents);

        ServicePoolProxies.close(dataStore);
        ServicePoolProxies.close(databus);

        colorIndex.listenForQueries();

    }


    private void updateTable(EventContainer container) {
        for (ColorEvent colorEvent : container.getColorEventList()) {
            Audit audit = new AuditBuilder()
                    .setProgram("example-app")
                    .setComment("initial submission")
                    .setLocalHost()
                    .build();
            Delta delta = Deltas.mapBuilder()
                    .put("color", colorEvent.getColorItem().getColor())
                    .put("text", colorEvent.getColorItem().getText()).build();
            dataStore.update(TABLE, colorEvent.getEventId(), TimeUUIDs.newUUID(), delta, audit);
        }
    }

    private void initializeEmoDb() {
        String emodbHost = "localhost:8080";  // Adjust to point to the EmoDB server.
        String apiKey = "";  // Use the API key provided by EmoDB
        MetricRegistry metricRegistry = new MetricRegistry(); // This is usually a singleton passed
        dataStore = ServicePoolBuilder.create(DataStore.class)
                .withHostDiscoverySource(new DataStoreFixedHostDiscoverySource(emodbHost))
                .withServiceFactory(DataStoreClientFactory.forCluster("local_default", new MetricRegistry()).usingCredentials(apiKey))
                .withMetricRegistry(metricRegistry)
                .buildProxy(new ExponentialBackoffRetry(5, 50, 1000, TimeUnit.MILLISECONDS));

        Map<String, String> template = ImmutableMap.of("type", "colorText");
        TableOptions options = new TableOptionsBuilder().setPlacement("ugc_global:ugc").build();
        Audit audit = new AuditBuilder().setProgram("example-app").setLocalHost().build();
        //dataStore.dropTable(TABLE, audit);
        dataStore.createTable(TABLE, options, template, audit);

        databus = ServicePoolBuilder.create(Databus.class)
                .withHostDiscoverySource(new DatabusFixedHostDiscoverySource(emodbHost))
                .withServiceFactory(DatabusClientFactory.forCluster("local_default", metricRegistry).usingCredentials(apiKey))
                .withMetricRegistry(metricRegistry)
                .buildProxy(new ExponentialBackoffRetry(5, 50, 1000, TimeUnit.MILLISECONDS));


    }

    public EventContainer loadEvents (String path) throws IOException {
        Gson gson = new GsonBuilder().registerTypeAdapter(EventContainer.class, new ColorEventDeserializer()).registerTypeAdapter(ColorEvent.class, new ColorEventSerializer()).create();
            return gson.fromJson(new String(Files.readAllBytes(Paths.get(path))), EventContainer.class);

    }
}
