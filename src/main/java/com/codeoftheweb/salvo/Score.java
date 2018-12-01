package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;

    private Double score;
    private Date finishDate;


    public Score() { }

    public Score(Player player,Game game,Double score){
        this.player= player;
        this.game = game;
        this.score = score;
        this.finishDate = new Date();
    }
    public Player getPlayer(){
        return player;
    }
    public void setPlayer(Player player){
        this.player = player;
    }
    @JsonIgnore
    public Game getGame(){
        return game;
    }
    public void setGame(Game game){
        this.game = game;
    }
    public Double getScore(){
        return score;
    }
    public void setScore(Double score){
        this.score = score;
    }

    @JsonIgnore
    public Date getFinishDate() {
        return finishDate;
    }
    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }



}