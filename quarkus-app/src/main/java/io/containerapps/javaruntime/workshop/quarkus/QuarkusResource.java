// tag::adocHeader[]
package io.containerapps.javaruntime.workshop.quarkus;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.lang.System.Logger;
import java.util.Arrays;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;

import static java.lang.System.Logger.Level.INFO;
import static java.lang.invoke.MethodHandles.lookup;

@Path("/quarkus")
@Produces(MediaType.TEXT_PLAIN)
public class QuarkusResource {

    private static final Logger LOGGER = System.getLogger(lookup().lookupClass().getName());

    private final StatisticsRepository repository;

    public QuarkusResource(StatisticsRepository statisticsRepository) {
        this.repository = statisticsRepository;
    }
// end::adocHeader[]

    /**
     * Says hello.
     * {@code curl 'localhost:8701/quarkus'}
     *
     * @return hello
     */
// tag::adocMethodHello[]
    @GET
    public String hello() {
        LOGGER.log(INFO, "Quarkus: hello");
        return "Quarkus: hello";
    }
// end::adocMethodHello[]

    /**
     * Simulates requests that use a lot of CPU.
     * {@code curl 'localhost:8701/quarkus/cpu'}
     * {@code curl 'localhost:8701/quarkus/cpu?iterations=10'}
     * {@code curl 'localhost:8701/quarkus/cpu?iterations=10&db=true'}
     * {@code curl 'localhost:8701/quarkus/cpu?iterations=10&db=true&desc=java25'}
     *
     * @param iterations the number of iterations to run (times 20,000).
     * @return the result
     */
// tag::adocMethodCPU[]
    @GET
    @Path("/cpu")
    public String cpu(@QueryParam("iterations") @DefaultValue("10") long iterations,
                      @QueryParam("db") @DefaultValue("false") boolean db,
                      @QueryParam("desc") String desc) {
        LOGGER.log(INFO, "Quarkus: cpu: {0} {1} with desc {2}", iterations, db, desc);
        var start = Instant.now();

        // tag::adocAlgoCPU[]
        consumeCpu(iterations * 20_000);
        // end::adocAlgoCPU[]
        var duration = Duration.between(start, Instant.now());

        if (db) {
            repository.persist(statistics(Type.CPU, Long.toString(iterations), duration, desc));
        }

        var msg = "Quarkus: CPU consumption is done with %d iterations in %d nano-seconds."
            .formatted(iterations, duration.toNanos());
        if (db) {
            msg += " The result is persisted in the database.";
        }
        return msg;
    }
// end::adocMethodCPU[]

    /**
     * Simulates requests that use a lot of memory.
     * {@code curl 'localhost:8701/quarkus/memory'}
     * {@code curl 'localhost:8701/quarkus/memory?bites=10'}
     * {@code curl 'localhost:8701/quarkus/memory?bites=10&db=true'}
     * {@code curl 'localhost:8701/quarkus/memory?bites=10&db=true&desc=java25'}
     *
     * @param bites the number of megabytes to eat
     * @return the result.
     */
// tag::adocMethodMemory[]
    @GET
    @Path("/memory")
    public String memory(@QueryParam("bites") @DefaultValue("10") int bites,
                         @QueryParam("db") @DefaultValue("false") boolean db,
                         @QueryParam("desc") String desc) {
        LOGGER.log(INFO, "Quarkus: memory: {0} {1} with desc {2}", bites, db, desc);

        var start = Instant.now();
        // tag::adocAlgoMemory[]
        HashMap<Integer, byte[]> hunger = new HashMap<>();
        for (int i = 0; i < bites * 1024 * 1024; i += 8192) {
            var bytes = new byte[8192];
            Arrays.fill(bytes, (byte) '0');
            hunger.put(i, bytes);
        }
        // end::adocAlgoMemory[]
        var duration = Duration.between(start, Instant.now());

        if (db) {
            repository.persist(statistics(Type.MEMORY, Integer.toString(bites), duration, desc));
        }

        var msg = "Quarkus: Memory consumption is done with %d bites in %d nano-seconds."
            .formatted(bites, duration.toNanos());
        if (db) {
            msg += " The result is persisted in the database.";
        }
        return msg;
    }
// end::adocMethodMemory[]

    /**
     * Returns what's in the database.
     * {@code curl 'localhost:8701/quarkus/stats'}
     *
     * @return the list of Statistics.
     */
// tag::adocMethodStats[]
    @GET
    @Path("/stats")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Statistics> stats() {
        LOGGER.log(INFO, "Quarkus: retrieving statistics");
        return Statistics.findAll().list();
    }
// end::adocMethodStats[]

    private static void consumeCpu(long iterations) {
        for (var remaining = iterations; remaining > 0; remaining--) {
            if (remaining % 20_000 == 0 && !sleepBriefly()) {
                return;
            }
        }
    }

    private static boolean sleepBriefly() {
        try {
            Thread.sleep(Duration.ofMillis(20));
            return true;
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static Statistics statistics(Type type, String parameter, Duration duration, String description) {
        var statistics = new Statistics();
        statistics.type = type;
        statistics.parameter = parameter;
        statistics.duration = duration;
        statistics.description = description;
        return statistics;
    }
}
