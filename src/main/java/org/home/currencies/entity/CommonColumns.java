package org.home.currencies.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@MappedSuperclass
public abstract class CommonColumns {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "ID")
    private Long id;

    @CreationTimestamp
    @Column(comment = "Date created")
    private Instant createDate;

    @UpdateTimestamp
    @Column(comment = "Date updated", nullable = true, insertable = false)
    private Instant modifiedDate;

    public Long getId() {
        return id;
    }

    public Instant getCreateDate() {
        return createDate;
    }

    public Instant getModifiedDate() {
        return modifiedDate;
    }

}
