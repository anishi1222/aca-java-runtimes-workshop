package io.containerapps.javaruntime.workshop.micronaut;

import com.azure.monitor.opentelemetry.autoconfigure.AzureMonitorAutoConfigure;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.env.Environment;
import io.micronaut.tracing.opentelemetry.OpenTelemetryBuilderCustomizer;
import jakarta.inject.Singleton;

import java.util.Map;
import java.util.Optional;

@Factory
class AzureMonitorOpenTelemetryFactory {

    @Singleton
    OpenTelemetryBuilderCustomizer azureMonitorCustomizer(Environment environment) {
        return builder -> connectionString(environment).ifPresent(value -> {
            builder.addPropertiesSupplier(() -> Map.of("applicationinsights.live.metrics.enabled", "false"));
            AzureMonitorAutoConfigure.customize(builder, value);
        });
    }

    private Optional<String> connectionString(Environment environment) {
        return environment.getProperty("applicationinsights.connection.string", String.class)
            .filter(value -> !value.isBlank())
            .or(() -> Optional.ofNullable(System.getenv("APPLICATIONINSIGHTS_CONNECTION_STRING"))
                .filter(value -> !value.isBlank()));
    }
}
