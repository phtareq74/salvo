package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
public class Salvo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private Integer turn;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;
    //private Set<GamePlayer> gamePlayers;

    public long getId() {
        return id;
    }

    public Salvo() {}

    @ElementCollection
    @Column(name = "locations")
    private List<String> locations;
     public Salvo ( Integer turn, List<String>locations, GamePlayer gamePlayer){
             this.turn = turn;
             this.locations = locations;
         this.gamePlayer = gamePlayer;
     }
    public void setLocations(List<String> locations) {
        this.locations = locations;
    }
    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }
     public long getSalvoId(){return id;}
     public Integer getTurn(){return turn;}
     public List<String>getLocations(){return locations;}
    @JsonIgnore
    public GamePlayer getGamePlayer(){return gamePlayer;}


}
