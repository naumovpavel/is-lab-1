package com.wiftwift.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.wiftwift.entity.ImportAttempt;
import com.wiftwift.repository.ImportAttemptsRepository;

@Service
public class ImportAttemptService {
    @Autowired
    private ImportAttemptsRepository importAttemptsRepository;

    public Page<ImportAttempt> getUserAttempts(Long userId, Pageable pageable) {
        return importAttemptsRepository.findByOwner_Id(userId, pageable);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ImportAttempt save(ImportAttempt attempt) {
        return importAttemptsRepository.save(attempt);
    }

    public Page<ImportAttempt> getAllAttempts(Pageable pageable) {
        return importAttemptsRepository.findAll(pageable);
    }
}
