package io.containerapps.javaruntime.workshop.micronaut;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.runtime.Micronaut;
import jakarta.persistence.Entity;

@Introspected(packages = "io.containerapps.javaruntime.workshop.micronaut", includedAnnotations = Entity.class)
public class Application {

    @SuppressWarnings("null")
    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}