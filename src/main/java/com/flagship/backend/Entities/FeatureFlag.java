package com.flagship.backend.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.*;

@Entity
@Table(name = "feature_flags")
@Data
public class FeatureFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String flagName;

    @Column
    private String description;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private int rolloutPercent;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "flag", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<TargetingRule> targetingRules = new ArrayList<>();

    @Column(nullable = false)
    private String seed;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false)
    private long flagConversions;

    @Column(nullable = false)
    private long controlConversions;
}
