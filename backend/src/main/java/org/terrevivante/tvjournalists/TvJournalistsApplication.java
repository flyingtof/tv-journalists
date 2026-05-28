package org.terrevivante.tvjournalists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.terrevivante.tvjournalists.config.NativeImageHintsRegistrar;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@ImportRuntimeHints(NativeImageHintsRegistrar.class)
public class TvJournalistsApplication {
    private static final Logger log = LoggerFactory.getLogger(TvJournalistsApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(TvJournalistsApplication.class, args);
    }
}
