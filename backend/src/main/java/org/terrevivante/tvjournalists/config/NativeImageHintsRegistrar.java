package org.terrevivante.tvjournalists.config;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

/**
 * Enregistre les hints pour la compilation native GraalVM.
 *
 * <p>Permet à Hibernate/JPA d'accéder réflexivement à UUID[] et autres types
 * utilisés par les entités JPA et les domain models.
 */
public class NativeImageHintsRegistrar implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // UUID[] est utilisé par Hibernate pour les paramètres de requête et la réflexion JPA
        hints.reflection().registerType(TypeReference.of("java.util.UUID[]"),
            hint -> hint.onReachableType(TypeReference.of("java.util.UUID"))
        );
    }
}