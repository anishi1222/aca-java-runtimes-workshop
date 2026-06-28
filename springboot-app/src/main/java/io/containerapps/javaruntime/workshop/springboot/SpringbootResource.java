// tag::adocHeader[]
package io.containerapps.javaruntime.workshop.springboot;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

@RestController
@RequestMapping("/springboot")
public class SpringbootResource {

    private static final Logger LOGGER = System.getLogger(lookup().lookupClass().getName());

    private final StatisticsRepository repository;

    public SpringbootResource(StatisticsRepository statisticsRepository) {
        this.repository = statisticsRepository;
    }
// end::adocHeader[]

    /**
     * Says hello.
     * {@code curl 'localhost:8703/springboot'}
     *
     * @return hello
     */
// tag::adocMethodHello[]
    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public String hello() {
        LOGGER.log(INFO, "Spring Boot: hello");
        return "Spring Boot: hello";
    }
// end::adocMethodHello[]

    /**
     * Simulates requests that use a lot of CPU.
     * {@code curl 'localhost:8703/springboot/cpu'}
     * {@code curl 'localhost:8703/springboot/cpu?iterations=10'}
     * {@code curl 'localhost:8703/springboot/cpu?iterations=10&db=true'}
     * {@code curl 'localhost:8703/springboot/cpu?iterations=10&db=true&desc=java25'}
     *
     * @param iterations the number of iterations to run (times 20,000).
     * @return the result
     */
// tag::adocMethodCPU[]
    @GetMapping(path = "/cpu", produces = MediaType.TEXT_PLAIN_VALUE)
    public String cpu(@RequestParam(value = "iterations", defaultValue = "10") long iterations,
                      @RequestParam(value = "db", defaultValue = "false") boolean db,
                      @RequestParam(value = "desc", required = false) String desc) {
        LOGGER.log(INFO, "Spring Boot: cpu: {0} {1} with desc {2}", iterations, db, desc);
        var start = Instant.now();

        consumeCpu(iterations * 20_000);
        var duration = Duration.between(start, Instant.now());

        if (db) {
            repository.save(statistics(Type.CPU, Long.toString(iterations), duration, desc));
        }

        var msg = "Spring Boot: CPU consumption is done with %d iterations in %d nano-seconds."
            .formatted(iterations, duration.toNanos());
        if (db) {
            msg += " The result is persisted in the database.";
        }
        return msg;
    }
// end::adocMethodCPU[]

    /**
     * Simulates requests that use a lot of memory.
     * {@code curl 'localhost:8703/springboot/memory'}
     * {@code curl 'localhost:8703/springboot/memory?bites=10'}
     * {@code curl 'localhost:8703/springboot/memory?bites=10&db=true'}
     * {@code curl 'localhost:8703/springboot/memory?bites=10&db=true&desc=java25'}
     *
     * @param bites the number of megabytes to eat
     * @return the result.
     */
// tag::adocMethodMemory[]
    @GetMapping(path = "/memory", produces = MediaType.TEXT_PLAIN_VALUE)
    public String memory(@RequestParam(value = "bites", defaultValue = "10") int bites,
                         @RequestParam(value = "db", defaultValue = "false") boolean db,
                         @RequestParam(value = "desc", required = false) String desc) {
        LOGGER.log(INFO, "Spring Boot: memory: {0} {1} with desc {2}", bites, db, desc);

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

        var msg = "Spring Boot: Memory consumption is done with %d bites in %d nano-seconds."
            .formatted(bites, duration.toNanos());
        if (db) {
            msg += " The result is persisted in the database.";
        }
        return msg;
    }
// end::adocMethodMemory[]

    /**
     * Returns what's in the database.
     * {@code curl 'localhost:8703/springboot/stats'}
     *
     * @return the list of Statistics.
     */
// tag::adocMethodStats[]
    @GetMapping(path = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Statistics> stats() {
        LOGGER.log(INFO, "Spring Boot: retrieving statistics");
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
