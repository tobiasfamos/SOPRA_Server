package ch.uzh.ifi.seal.soprafs19.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.LinkedHashSet;
import java.util.LinkedList;

@Entity
public class Game implements Serializable {

    @Id
    @GeneratedValue
    private Long gameId;

    @Column
    private Array[][] state;

    //Getters and Setters

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Array[][] getState() {
        return state;
    }

    public void setState(Array[][] state) {
        this.state = state;
    }

}