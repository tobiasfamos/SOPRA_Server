package ch.uzh.ifi.seal.soprafs19.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.LinkedHashSet;

@Entity
public class Player implements Serializable {

    @Id
    @GeneratedValue
    private Long playerId;

    @Column(nullable = false)
    private int availablemoves;

    @Column
    private int availablebuilds;

}