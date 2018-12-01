package com.codeoftheweb.salvo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
public class SalvoController {


    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private ShipRepository shipRepository;
    @Autowired
    private SalvoRepository salvoRepository;
    @Autowired
    private ScoreRepository scoreRepository;

    @RequestMapping(path = "/games", method = RequestMethod.GET)
    public Map<String, Object> getGamesInfo(Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<>();

        if (authentication != null) {
            dto.put("currentplayer", makeCurrentPlayerDTO(authentication));
        } else {
            dto.put("currentplayer", "No logged in player");
        }
        dto.put("games", gameRepository
                .findAll()
                .stream()
                .map(game -> makeGamesDTO(game))
                .collect(toList()));

        return dto;
    }

    private Map<String, Object> makeGamesDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        Set<GamePlayer> gamePlayers = game.getGamePlayers();
        dto.put("gid", game.getId());
        dto.put("create", game.getDate());
        dto.put("gamePlayers", gamePlayers
                .stream()
                .map(gamePlayer -> makeGamePlayerDTO(gamePlayer))
                .collect(toList()));

        if (game.hasScore()) {
            dto.put("scores", MakeScoresDTO(game.getScores()));
        }

        return dto;
    }

    private Map<String, Object> makeGamePlayerDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("gpid", gamePlayer.getId());
        dto.put("player", makePlayerDTO(gamePlayer.getPlayer()));


        return dto;
    }

    private Map<String, Object> makePlayerDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        dto.put("id", player.getId());
        dto.put("userName", player.getUserName());

        return dto;
    }

    private Map<String, Object> makeCurrentPlayerDTO(Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        dto.put("id", currentPlayer(authentication).getId());
        dto.put("userName", currentPlayer(authentication).getUserName());

        return dto;
    }

    private Player currentPlayer(Authentication authentication) {
        return playerRepository.findByUserName(authentication.getName());
    }

    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    private Set<Object> MakeScoresDTO(Set<Score> scores) {
        return scores
                .stream()
                .map(oneScore -> MakeScoreDTO(oneScore))
                .collect(Collectors.toSet());
    }

    private Map<String, Number> MakeScoreDTO(Score score) {
        Map<String, Number> scoreDTO = new LinkedHashMap<String, Number>();
        scoreDTO.put("playerId", score.getPlayer().getId());
        scoreDTO.put("score", score.getScore());
        return scoreDTO;
    }

    @RequestMapping("/leaderboard")
    public List<Object> Leaderboard() {
        return playerRepository
                .findAll()
                .stream()
                .map(onePlayer -> MakeLbDTO(onePlayer))
                .collect(Collectors.toList());
    }

    // building leaderboard - object for one player
    private Map<String, Object> MakeLbDTO(Player player) {
        Map<String, Object> leaderBoardDTO = new LinkedHashMap<String, Object>();
        leaderBoardDTO.put("playerId", player.getId());
        leaderBoardDTO.put("userName", player.getUserName());
        leaderBoardDTO.put("results", CountDifferentResultsDTO(player));
        return leaderBoardDTO;
    }

    // building leaderboard - obejct for different game results for that particular player
    private Map<String, Object> CountDifferentResultsDTO(Player player) {
        Map<String, Object> countWinsDTO = new LinkedHashMap<String, Object>();
        countWinsDTO.put("won", CountCertainResults(1.0, player));
        countWinsDTO.put("tied", CountCertainResults(0.5, player));
        countWinsDTO.put("lost", CountCertainResults(0.0, player));
        countWinsDTO.put("totalScore", CountSum(player));
        return countWinsDTO;
    }

    // building leaderboard - counting that type of results
    private Long CountCertainResults(Double result, Player player) {
        return player.getScores()
                .stream()
                .filter(oneScore -> oneScore.getScore().equals(result))
                .count();
    }

    // building leaderboard - when have all different results counting total sum
    private Double CountSum(Player player) {
        return player.getScores()
                .stream()
                .mapToDouble(oS -> oS.getScore())
                .sum();
    }


    @RequestMapping("/game_view/{gpID}")
    public Map<String, Object> getGameView(@PathVariable Long gpID, Authentication authentication) {

        GamePlayer gamePlayer = gamePlayerRepository.findOne(gpID);
        GamePlayer opponent = getOpponent(gamePlayer);


        Map<String, Object> mapGamePlayer = new LinkedHashMap<>();
        mapGamePlayer.put("gameID", gamePlayer.getGame().getId());
        mapGamePlayer.put("created", gamePlayer.getGame().getDate());
        mapGamePlayer.put("gamePlayers", MakeGamePlayerSetDTO(gamePlayer.getGame().getGamePlayers()));
       // mapGamePlayer.put("gameState", getGameState(gameplayer));
        mapGamePlayer.put("ships", gamePlayer.getShips()
                .stream()
                .map(ship -> makeShipDTO(ship))
                .collect(Collectors.toList()));
        mapGamePlayer.put("user_salvo", makeSalvoDTO2(gamePlayer));
        if (opponent != null) {
            mapGamePlayer.put("opponent_salvo", makeSalvoDTO2(opponent));
        }


        return mapGamePlayer;
    }

    private Set<Object> MakeGamePlayerSetDTO(Set<GamePlayer> gamePlayerSet) {
        return gamePlayerSet
                .stream()
                .map(oneGamePlayer -> MakeGamePlayerDTO(oneGamePlayer))
                .collect(Collectors.toSet());
    }

    private Map<String, Object> MakeGamePlayerDTO(GamePlayer gamePlayer) {
        Map<String, Object> gamePlayerDTO = new LinkedHashMap<String, Object>();
        gamePlayerDTO.put("id", gamePlayer.getId());
        gamePlayerDTO.put("player", MakePlayerDTO(gamePlayer.getPlayer()));
        return gamePlayerDTO;
    }

    private Map<String, Object> MakePlayerDTO(Player player) {
        Map<String, Object> playerDTO = new LinkedHashMap<String, Object>();
        playerDTO.put("id", player.getId());
        playerDTO.put("userName", player.getUserName());
        return playerDTO;
    }


    private Map<String, Object> makeShipDTO(Ship ship) {
        Map<String, Object> shipDTO = new LinkedHashMap<String, Object>();
        shipDTO.put("id", ship.getShipId());
        shipDTO.put("shipType", ship.getShipType());
        shipDTO.put("locations", ship.getLocations());
        return shipDTO;
    }

    private Map<String, Object> makeSalvoDTO(Game game) {

        Map<String, Object> playerMap = new LinkedHashMap<>();
        Set<GamePlayer> gameplayers = game.getGamePlayers();

        gameplayers.forEach(gameplayer -> {
            String playerID = Long.toString(gameplayer.getPlayer().getId());
            Set<Salvo> salvos = gameplayer.getSalvos();
            Map<String, Object> salvoDTO = new LinkedHashMap<>();

            salvos.forEach(salvo -> {
                salvoDTO.put("turn", salvo.getTurn());
                salvoDTO.put("locations", salvo.getLocations());
                playerMap.put(playerID, salvoDTO);
            });
        });
        return playerMap;
    }

    private List<Object> makeSalvoDTO2(GamePlayer gamePlayer) {

        List<Object> list = new ArrayList<>();

        gamePlayer.getSalvos().forEach(salvo -> {
            Map<String, Object> salvoDTO = new LinkedHashMap<>();
            salvoDTO.put("turn", salvo.getTurn());
            salvoDTO.put("locations", salvo.getLocations());
            list.add(salvoDTO);
        });
        return list;
    }

    private GamePlayer getOpponent(GamePlayer gamePlayer) {

        return gamePlayer.getGame().getGamePlayers()
                .stream()
                .filter(gp -> gp.getId() != gamePlayer.getId())
                .findFirst()
                .orElse(null);

    }

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    private ResponseEntity<Map<String, Object>> register(

            @RequestParam String userName, @RequestParam String password) {

        if (userName.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>(makeMap("error", "Missing data"), HttpStatus.FORBIDDEN);
        }

        if (playerRepository.findByUserName(userName) != null) {
            return new ResponseEntity<>(makeMap("error", "Name already in use"), HttpStatus.FORBIDDEN);
        }

        playerRepository.save(new Player(userName, password));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping(path = "/games", method = RequestMethod.POST)

    private ResponseEntity<Map<String, Object>> createNewGame(Authentication authentication) {
        if (authentication == null) {
            return new ResponseEntity<>(makeMap("error", "No logged in player to create game")
                    , HttpStatus.UNAUTHORIZED);
        } else {

            Game newGame = new Game();
            gameRepository.save(newGame);
            GamePlayer gamePlayer = new GamePlayer(currentPlayer(authentication), newGame);
            gamePlayerRepository.save(gamePlayer);
            return new ResponseEntity<>(makeMap("gpCreated", gamePlayer.getId())
                    , HttpStatus.CREATED);
        }
    }

    @RequestMapping(path = "/games/{gid}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getGameJoin(@PathVariable Long gid, Authentication authentication) {
        Game game = gameRepository.findOne(gid);
        Player player = currentPlayer(authentication);

        if (authentication == null) {
            return new ResponseEntity<>(makeMap("error", "Need to be logged in to join a game")
                    , HttpStatus.UNAUTHORIZED);
        } else if (game == null) {
            return new ResponseEntity<>(makeMap("error", "No existing game")
                    , HttpStatus.FORBIDDEN);
        } else {
            GamePlayer gamePlayer = gamePlayerRepository.save(new GamePlayer(player, game));
            return new ResponseEntity<>(makeMap("gamePlayerID", gamePlayer.getId())
                    , HttpStatus.CREATED);
        }
    }


    @RequestMapping(path = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> placeShips(@PathVariable Long gamePlayerId,
                                                          @RequestBody Set<Ship> ships,
                                                          Authentication authentication) {
        GamePlayer gamePlayer = gamePlayerRepository.findOne(gamePlayerId);
        if (authentication == null) {
            return new ResponseEntity<>(makeMap("error", "log in to place ships"), HttpStatus.UNAUTHORIZED);
        } else if (gamePlayer == null) {
            return new ResponseEntity<>(makeMap("error", "gamePlayer does not exist"), HttpStatus.UNAUTHORIZED);
        } else if (gamePlayer.getPlayer() != currentPlayer(authentication)) {
            return new ResponseEntity<>(makeMap("error", "Not your game"), HttpStatus.UNAUTHORIZED);
        } else if (gamePlayer.getShips().size() != 0) {
            return new ResponseEntity<>(makeMap("error", "Ships are already placed")
                    , HttpStatus.FORBIDDEN);
        } else if (gamePlayer.getShips().size() > 5) {
            return new ResponseEntity<>(makeMap("error", "Not allowed to place ships ")
                    , HttpStatus.FORBIDDEN);
        } else {
            for (Ship ship : ships) {
                ship.setGamePlayer(gamePlayer);
                shipRepository.save(ship);
            }
            return new ResponseEntity<>(makeMap("succes", "Ships are created")
                    , HttpStatus.CREATED);
        }
    }

    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    @RequestMapping(path = "/games/players/{gamePlayerId}/salvos", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> placingSalvos(@PathVariable long gamePlayerId,
                                                             @RequestBody List<Salvo> salvos,
                                                             Authentication authentication) {

        GamePlayer gamePlayer = gamePlayerRepository.findOne(gamePlayerId);

        if (authentication == null) {
            return new ResponseEntity<>(makeMap("error", "Need to be logged in")
                    , HttpStatus.UNAUTHORIZED);


        } else if (gamePlayer == null) {
            return new ResponseEntity<>(makeMap("error", "No gamePlayer ")
                    , HttpStatus.UNAUTHORIZED);


        } else if (gamePlayer.getPlayer() != currentPlayer(authentication)) {
            return new ResponseEntity<>(makeMap("error", "Not your game ")
                    , HttpStatus.FORBIDDEN);

        }
        else if (gamePlayer.getSalvos().size() > 5) {
            return new ResponseEntity<>(makeMap("error", "Not your turn")
                    , HttpStatus.FORBIDDEN);

       }
        else {
            for
                (Salvo salvo : salvos){
                salvo.setGamePlayer(gamePlayer);
                salvoRepository.save(salvo);
            }
            return new ResponseEntity<>(makeMap("succes", "Salvos are created")
                    , HttpStatus.CREATED);
        }
//        private String getGameState (GamePlayer gamePlayer) {
//            GamePlayer opponent = gamePlayer.getOpponent();
//            String state = "";
//
//            if (gamePlayer.getShips().size() != 5){
//                state = "placeShips";
//                return state;
//            }
//
//            if (opponent != null) {
//
//                if (gamePlayer.getGame().isGameOver()){
//                    state = "gameOver";
//                    return state;
//                }
//
//                if (opponent.getShips().size() != 5){
//                    state = "opponentPlaceShips";
//                    return state;
//                }
//
//                List<Integer> myTurns = new ArrayList<>();
//                List<Integer> opponentTurns = new ArrayList<>();
//
//                gamePlayer.getSalvos().forEach(salvo -> {
//                    myTurns.add(salvo.getTurn());
//                });
//
//                opponent.getSalvos().forEach(salvo -> {
//                    opponentTurns.add(salvo.getTurn());
//                });
//
//                boolean first = gamePlayer.isFirst();
//
//                if (myTurns.size() < opponentTurns.size()) {
//                    state = "myTurn";
//                    return state;
//                } else if (myTurns.size() > opponentTurns.size()){
//                    state = "opponentTurn";
//                    return state;
//                } else {
//                    if (first){
//                        state = "myTurn";
//                        return state;
//                    } else {
//                        state = "opponentTurn";
//                        return state;
//                    }
//                }
//            }
//
//            state = "noOpponent";
//            return state;
//        }


//
////            if (salvo.getTurn() == gamePlayer.getSalvos().size() + 1 || salvo.getTurn() == null) {
////                salvoRepository.save(salvo);
////                gamePlayer.addSalvos(salvo);
////
////                Player player = gamePlayer.getPlayer();
////                Game game = gamePlayer.getGame();
////
////                GamePlayer oponent = oponentGamePlayer(gamePlayer);
////                Player oponentPlayer = oponent.getPlayer();
////
////                if (getGameState(gamePlayer) == GameState.GameOver_Won) {
////                    Score myScoreWin = new Score(game, player, 1.0);
////                    Score oponScoreLost = new Score(game, oponentPlayer, 0.0);
////                    scoreRepository.save(myScoreWin);
////                    scoreRepository.save(oponScoreLost);
////
////                } else if (getGameState(gamePlayer) == GameState.GameOver_Lost) {
////                    Score myScoreLost = new Score(game, player, 0.0);
////                    Score oponScoreWin = new Score(game, oponentPlayer, 1.0);
////                    scoreRepository.save(myScoreLost);
////                    scoreRepository.save(oponScoreWin);
////
////                } else if (getGameState(gamePlayer) == GameState.GameOver_Tied) {
////                    Score myScoreTied = new Score(game, player, 0.5);
////                    Score oponScoreTied = new Score(game, oponentPlayer, 0.5);
////                    scoreRepository.save(myScoreTied);
////                    scoreRepository.save(oponScoreTied);
////
////
////                }
////
////                return new ResponseEntity<>(makeMap("success", "Created and saved salvos")
////                        , HttpStatus.CREATED);
////
////            } else {
////                return new ResponseEntity<>(makeMap("error", "You already shot this turn"), HttpStatus.FORBIDDEN);
////
////            }
////        }
//
//            //  }
//
//
   }
}





