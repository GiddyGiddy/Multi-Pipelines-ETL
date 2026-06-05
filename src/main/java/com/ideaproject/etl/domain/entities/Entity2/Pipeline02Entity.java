package com.ideaproject.etl.domain.entities.Entity2;

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
 * Domain entity for pipeline 02 base items.
 */
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ENTITY_2")
public class Pipeline02Entity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "PRIM_KEY")
    private BigInteger primKey;

    @Column(name = "TWOBJECTVERSION_ID")
    private int twobjectversionId;

    @Column(name = "ID")
    private int id;
}

