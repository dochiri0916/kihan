package com.example.kihan.presentation.audit.response;

import com.example.kihan.infrastructure.audit.CustomRevisionEntity;
import org.springframework.data.history.Revision;
import org.springframework.data.history.RevisionMetadata;

import java.time.Instant;

public record RevisionResponse<T>(
        Long revisionNumber,
        Instant timestamp,
        String username,
        RevisionMetadata.RevisionType action,
        T entity
) {
    public static <T> RevisionResponse<T> from(Revision<Long, T> revision) {
        CustomRevisionEntity customRevision = revision.getMetadata().getDelegate();
        return new RevisionResponse<>(
                revision.getRevisionNumber().orElse(null),
                revision.getRevisionInstant().orElse(null),
                customRevision.getUsername(),
                revision.getMetadata().getRevisionType(),
                revision.getEntity()
        );
    }
}