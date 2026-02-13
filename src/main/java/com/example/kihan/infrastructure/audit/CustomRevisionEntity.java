package com.example.kihan.infrastructure.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import java.io.Serializable;

@Entity
@Table(name = "revinfo")
@RevisionEntity(CustomRevisionListener.class)
@Getter
@Setter
public class CustomRevisionEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    private Long id;

    @RevisionTimestamp
    private Long timestamp;

    private String username;
}
