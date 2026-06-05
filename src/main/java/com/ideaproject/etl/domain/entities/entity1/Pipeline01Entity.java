package com.ideaproject.etl.domain.entities.entity1;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigInteger;

/**
 * Domain entity for pipeline 01 base items.
 */
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ENTITY_1")
public class Pipeline01Entity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "PRIM_KEY")
    private BigInteger primKey;

    @Column(name = "TWOBJECTVERSION_ID")
    private int twobjectversionId;

    @Column(name = "ID")
    private int id;
}

