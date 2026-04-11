package org.terrevivante.tvjournalists.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JournalistService {
    Journalist save(Journalist journalist);
    Optional<Journalist> findById(UUID id);
    Page<Journalist> search(String name, List<String> media, List<String> themes, Pageable pageable);
}
