// tag::adocHeader[]
package io.containerapps.javaruntime.workshop.micronaut;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;

import java.lang.System.Logger;
import java.util.Arrays;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import static java.lang.System.Logger.Level.INFO;
import static java.lang.invoke.MethodHandles.lookup;

@Controller("/micronaut")
public class MicronautResource {

    private static final Logger LOGGER = System.getLogger(lookup().lookupClass().getName());

    private final StatisticsRepository repository;

    public MicronautResource(StatisticsRepository statisticsRepository) {
        this.repository = statisticsRepository;
    }
// end::adocHeader[]

    /**
     * Says hello.
     * {@code curl 'localhost:8702/micronaut'}
     *
     * @return hello
     */
// tag::adocMethodHello[]
    @Get(produces = MediaType.TEXT_PLAIN)
    public String hello() {
        LOGGER.log(INFO, "Micronaut: hello");
        return "Micronaut: hello";
    }
// end::adocMethodHello[]

    /**
     * Simulates requests that use a lot of CPU.
     * {@code curl 'localhost:8702/micronaut/cpu'}
     * {@code curl 'localhost:8702/micronaut/cpu?iterations=10'}
     * {@code curl 'localhost:8702/micronaut/cpu?iterations=10&db=true'}
     * {@code curl 'localhost:8702/micronaut/cpu?iterations=10&db=true&desc=java25'}
     *
     * @param iterations the number of iterations to run (times 20,000).
     * @return the result
     */
// tag::adocMethodCPU[]
    @Get(uri = "/cpu", produces = MediaType.TEXT_PLAIN)
    public String cpu(@QueryValue(value = "iterations", defaultValue = "10") long iterations,
                      @QueryValue(value = "db", defaultValue = "false") boolean db,
                      @QueryValue(value = "desc", defaultValue = "") String desc) {
        LOGGER.log(INFO, "Micronaut: cpu: {0} {1} with desc {2}", iterations, db, desc);
        var start = Instant.now();

        consumeCpu(iterations * 20_000);
        var duration = Duration.between(start, Instant.now());

        if (db) {
            repository.save(statistics(Type.CPU, Long.toString(iterations), duration, desc));
        }

        var msg = "Micronaut: CPU consumption is done with %d iterations in %d nano-seconds."
            .formatted(iterations, duration.toNanos());
        if (db) {
            msg += " The result is persisted in the database.";
        }
        return msg;
    }
// end::adocMethodCPU[]

    /**
     * Simulates requests that use a lot of memory.
     * {@code curl 'localhost:8702/micronaut/memory'}
     * {@code curl 'localhost:8702/micronaut/memory?bites=10'}
     * {@code curl 'localhost:8702/micronaut/memory?bites=10&db=true'}
     * {@code curl 'localhost:8702/micronaut/memory?bites=10&db=true&desc=java25'}
     *
     * @param bites the number of megabytes to eat
     * @return the result.
     */
// tag::adocMethodMemory[]
    @Get(uri = "/memory", produces = MediaType.TEXT_PLAIN)
    public String memory(@QueryValue(value = "bites", defaultValue = "10") int bites,
                         @QueryValue(value = "db", defaultValue = "false") boolean db,
                         @QueryValue(value = "desc", defaultValue = "") String desc) {
        LOGGER.log(INFO, "Micronaut: memory: {0} {1} with desc {2}", bites, db, desc);

        var start = Instant.now();
        Map<Integer, byte[]> hunger = new HashMap<>();
        for (int i = 0; i < bites * 1024 * 1024; i += 8192) {
            var bytes = new byte[8192];
            Arrays.fill(bytes, (byte) '0');
            hunger.put(i, bytes);
        }
        var duration = Duration.between(start, Instant.now());

        if (db) {
            repository.save(statistics(Type.MEMORY, Integer.toString(bites), duration, desc));
        }

        var msg = "Micronaut: Memory consumption is done with %d bites in %d nano-seconds."
            .formatted(bites, duration.toNanos());
        if (db) {
            msg += " The result is persisted in the database.";
        }
        return msg;
    }
// end::adocMethodMemory[]

    /**
     * Returns what's in the database.
     * {@code curl 'localhost:8702/micronaut/stats'}
     *
     * @return the list of Statistics.
     */
// tag::adocMethodStats[]
    @Get(uri = "/stats", produces = MediaType.APPLICATION_JSON)
    public List<Statistics> stats() {
        LOGGER.log(INFO, "Micronaut: retrieving statistics");
        return StreamSupport.stream(repository.findAll().spliterator(), false).toList();
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
