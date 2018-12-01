package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

import java.util.List;
@Entity
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private String shipType;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;

    public Ship() {}

    @ElementCollection
    @Column(name = "locations")
    private List<String> locations;



    public Ship(String shipType, List<String> locations, GamePlayer gamePlayer) {
        this.shipType = shipType;
        this.locations = locations;
        this.gamePlayer = gamePlayer;
    }


    public long getShipId() {
        return id;
    }

    public String getShipType() {
        return shipType;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    @JsonIgnore
    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

}

