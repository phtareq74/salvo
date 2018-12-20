package com.codeoftheweb.salvo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


@Entity
public class GamePlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private Date date;
    private GameState state;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;
    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER)
    Set<Ship> ships = new HashSet<>();
    public GamePlayer(Player player,Game game){
        this.date = new Date();
        this.game = game;
        this.player = player;
    }
    public void setDate(Date date){
      this.date = date;
    }
    public GamePlayer(){ }


    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setGame(Game game) {
        this.game = game;
    }
    public long getId(){
        return id;
    }
    public Date getDate(){
        return date;
    }

    public Player getPlayer() {
        return player;
    }
    @JsonIgnore
    public Game getGame() {
        return game;
    }

    public Set<Ship> getShips() {
        return ships;
    }
    public void addShip(Ship ship) {

        ships.add(ship);
    }
    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER)
    Set<Salvo>salvos = new HashSet<>();

    public Set<Salvo>getSalvos(){return salvos;}

    public  void  addSalvo(Salvo salvo){
        this.salvos.add(salvo);
        salvo.setGamePlayer(this);
    }
    public enum GameState{
        WaitingForShips,
        WaitingForSecondPlayer,
        WaitingForSalvos,
        WaitingForEnemySalvo,
        WaitingForEnemyShips,
        MyTurn,
        GameOver
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

}
