package com.codeoftheweb.salvo;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.FetchType;
import static java.util.stream.Collectors.toList;
import javax.persistence.OneToMany;
import java.util.Set;
import java.util.List;
@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private Date date;
    @OneToMany(mappedBy="game", fetch=FetchType.EAGER)
    private Set<GamePlayer> gamePlayers;

    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER)
    private Set<Score> scores;

    public Game() {
        this.date = new Date();
    }



    public void addGamePlayer(GamePlayer gamePlayer) {
                gamePlayer.setGame(this);
                gamePlayers.add(gamePlayer);
    }

    @JsonIgnore
    public List<Player> getPlayers() {
        return gamePlayers.stream().map(sub -> sub.getPlayer()).collect(toList());
    }

    public Date getDate() {
        return date;
        }
    public long getId() {
        return id;
        }

    public void setDate(Date date) {
        this.date = date;
        }
    @JsonIgnore
    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public void setGamePlayers(Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }

    public void addScore(Score score){
        score.setGame(this);
        scores.add(score);
    }

    public Set<Score> getScores() {
        return scores;
    }


    public boolean hasScore () {
        return (!getScores().isEmpty());
    }


}

