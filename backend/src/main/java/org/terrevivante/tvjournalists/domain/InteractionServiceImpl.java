package org.terrevivante.tvjournalists.domain;

import org.terrevivante.tvjournalists.persistence.InteractionLogRepository;
import org.terrevivante.tvjournalists.persistence.JournalistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class InteractionServiceImpl implements InteractionService {

    private final InteractionLogRepository interactionLogRepository;
    private final JournalistRepository journalistRepository;

    public InteractionServiceImpl(InteractionLogRepository interactionLogRepository, JournalistRepository journalistRepository) {
        this.interactionLogRepository = interactionLogRepository;
        this.journalistRepository = journalistRepository;
    }

    @Override
    public InteractionLog logInteraction(UUID journalistId, InteractionLog log) {
        Journalist journalist = journalistRepository.findById(journalistId)
            .orElseThrow(() -> new IllegalArgumentException("Journalist not found"));
        log.setJournalist(journalist);
        return interactionLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InteractionLog> findByJournalistId(UUID journalistId) {
        return interactionLogRepository.findByJournalistIdOrderByDateDesc(journalistId);
    }
}
