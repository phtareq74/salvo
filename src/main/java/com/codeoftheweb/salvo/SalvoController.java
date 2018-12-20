
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

    private Map<String, Object> MakeLbDTO(Player player) {
        Map<String, Object> leaderBoardDTO = new LinkedHashMap<String, Object>();
        leaderBoardDTO.put("playerId", player.getId());
        leaderBoardDTO.put("userName", player.getUserName());
        leaderBoardDTO.put("results", CountResultsDTO(player));
        return leaderBoardDTO;
    }

    private Map<String, Object> CountResultsDTO(Player player) {
        Map<String, Object> countWinsDTO = new LinkedHashMap<String, Object>();
        countWinsDTO.put("won", CountGameResults(1.0, player));
        countWinsDTO.put("tied", CountGameResults(0.5, player));
        countWinsDTO.put("lost", CountGameResults(0.0, player));
        countWinsDTO.put("totalScore", CountSum(player));
        return countWinsDTO;
    }

    private Long CountGameResults(Double result, Player player) {
        return player.getScores()
                .stream()
                .filter(oneScore -> oneScore.getScore().equals(result))
                .count();
    }

    private Double CountSum(Player player) {
        return player.getScores()
                .stream()
                .mapToDouble(oS -> oS.getScore())
                .sum();
    }

    @RequestMapping("/game_view/{gpID}")
    public Map<String, Object> getGameView(@PathVariable Long gpID, Authentication authentication) {

        GamePlayer currentPlayer = gamePlayerRepository.findOne(gpID);

        Map<String, Object> mapGamePlayer = new LinkedHashMap<>();
        mapGamePlayer.put("gameID", currentPlayer.getGame().getId());
        mapGamePlayer.put("created", currentPlayer.getGame().getDate());
        mapGamePlayer.put("gamePlayers", MakeGamePlayerSetDTO(currentPlayer.getGame().getGamePlayers()));
        mapGamePlayer.put("lastTurn",findTurnNumber(currentPlayer)-1);
        mapGamePlayer.put("ships", currentPlayer.getShips()
                .stream()
                .map(ship -> makeShipDTO(ship))
                .collect(Collectors.toList()));
        mapGamePlayer.put("user_salvo", makeSalvoDTO(currentPlayer));
        mapGamePlayer.put("state", ChangeGameState(currentPlayer));

        if (currentPlayer.getGame().isFull()) {
            mapGamePlayer.put("hitsInfo", setHittedsAndSinked(currentPlayer));
            GamePlayer opponent = getOpponent(currentPlayer);
            mapGamePlayer.put("opponent_salvo", makeSalvoDTO(opponent));
            mapGamePlayer.put("opponent_sunkShips", getSunkShips(opponent));
            mapGamePlayer.put("user_sunkShips", getSunkShips(currentPlayer));
            mapGamePlayer.put("gameState", getGameState(currentPlayer));
        }


        return mapGamePlayer;
    }

    private Set<Object> MakeGamePlayerSetDTO(Set<GamePlayer> gamePlayerSet) {
        return gamePlayerSet
                .stream()
                .map(oneGamePlayer -> MakeGamePlayerDTO(oneGamePlayer))
                .collect(Collectors.toSet());
    }

    private Map<String, Object> MakeGamePlayerDTO(GamePlayer currentPlayer) {
        Map<String, Object> gamePlayerDTO = new LinkedHashMap<String, Object>();
        gamePlayerDTO.put("id", currentPlayer.getId());
        gamePlayerDTO.put("player", MakePlayerDTO(currentPlayer.getPlayer()));
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

    private List<Object> makeSalvoDTO(GamePlayer currentPlayer) {

        List<Object> list = new ArrayList<>();

        currentPlayer.getSalvos().forEach(salvo -> {
            Map<String, Object> salvoDTO = new LinkedHashMap<>();
            salvoDTO.put("turn", salvo.getTurn());
            salvoDTO.put("locations", salvo.getLocations());
            list.add(salvoDTO);
        });
        return list;
    }

    private GamePlayer getOpponent(GamePlayer currentPlayer) {

        return currentPlayer.getGame().getGamePlayers()
                .stream()
                .filter(gp -> gp.getId() != currentPlayer.getId())
                .findFirst()
                .orElse(null);

    }

    private Set<Object> setHittedsAndSinked (GamePlayer currentPlayer){

        Integer lastTurn = findTurnNumber(currentPlayer) - 1;
        if (lastTurn == 0){ return null; }

        Set<Object> hitsAndSinksSet = new HashSet<Object>();
        for (int i = 1; i <= lastTurn; i++){
            hitsAndSinksSet.add(MakeHitsAndSinks(i, currentPlayer));
        }
        return hitsAndSinksSet;
    }
    private Map<String, Object> MakeHitsAndSinks(int currentTurn, GamePlayer currentPlayer){

        Map<String, Object> hitsAndSinks = new LinkedHashMap<String, Object>();
        hitsAndSinks.put("turn", currentTurn);
        hitsAndSinks.put("hitsOnUser", MakeHitsOnGivenPlayer(currentPlayer, currentTurn));
        hitsAndSinks.put("hitsOnEnemy", MakeHitsOnGivenPlayer(getOpponent(currentPlayer), currentTurn));
        return hitsAndSinks;
    }
    private Map<Object, Object> MakeHitsOnGivenPlayer(GamePlayer givenGP, int currentTurn){

        Set<Ship> givenPlayerShips = givenGP.getShips();
        Set<Salvo> enemySalvosFromCurrentTurn = getOpponent(givenGP).getSalvos()
                .stream()
                .filter(salvo -> salvo.getTurn() == currentTurn)
                .collect(Collectors.toSet());

        Map<Object, Object> hitsOnGivenPlayer = new LinkedHashMap<Object, Object>();

        givenPlayerShips.stream().forEach((ship) -> {
            hitsOnGivenPlayer.put(ship.getShipType(), MakeShipInfoForHits(ship, enemySalvosFromCurrentTurn));
        });
        return hitsOnGivenPlayer;
    }
    private Map<String, Object> MakeShipInfoForHits(Ship currentShip, Set<Salvo> currentSalvosFromCurrentTurn){

        List<String> currentShipLocations = currentShip.getLocations();
        ArrayList<String> hits = new ArrayList<>();

        currentSalvosFromCurrentTurn.forEach(salvo -> {
            salvo.getLocations().forEach(singleShot -> {
                if (currentShipLocations.contains(singleShot)){
                    hits.add(singleShot);
                    currentShip.addHits(singleShot);
                }
            });
        });

        Map<String, Object> currentShipInfo = new LinkedHashMap<String, Object>();
        currentShipInfo.put("size", currentShipLocations.size());
        currentShipInfo.put("hits", hits);
        currentShipInfo.put("hitsTillNow", currentShip.getHits().size());
        currentShipInfo.put("isSunk", currentShip.isSunk());
        return currentShipInfo;
    }

    private Integer findTurnNumber(GamePlayer currentPlayer) {
    Integer turn = 0;
    for (Salvo salvo : currentPlayer.getSalvos()) {
        Integer turnNo = salvo.getTurn();
        if (turn < turnNo) {
            turn = turnNo;
        }
    }
    return turn + 1;
}

    private boolean isMyTurn (GamePlayer gamePlayer) {
        boolean myTurn = false;
        if (gamePlayer.getId() < getOpponent(gamePlayer).getId()){
            if (findTurnNumber(gamePlayer) <= findTurnNumber(getOpponent(gamePlayer))) {
                myTurn = true;
            }
        } else {
            if (findTurnNumber(gamePlayer) < findTurnNumber(getOpponent(gamePlayer))) {
                myTurn = true;
            }
        }
        return myTurn;
}

    private long numberSunkShips(GamePlayer currentPlayer){

        return currentPlayer.getShips().stream()
                .filter(ship -> ship.isSunk())
                .count();

    }

    public List<Object> getSunkShips (GamePlayer currentPlayer) {
        List<Object> sunkShips = new ArrayList<>();
        for (Ship ship : currentPlayer.getShips()) {
            if (ship.isSunk()){
                sunkShips.add(makeShipDTO(ship));
            }
        }
        System.out.println (sunkShips.size() + "fun");
        return sunkShips;
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
            GamePlayer currentPlayer = new GamePlayer(currentPlayer(authentication), newGame);
            gamePlayerRepository.save(currentPlayer);
            return new ResponseEntity<>(makeMap("gpCreated", currentPlayer.getId())
                    , HttpStatus.CREATED);
        }
    }

    @RequestMapping(path = "/games/{gid}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getGameJoin(@PathVariable Long gid, Authentication authentication) {
        Game game = gameRepository.findOne(gid);


        if(game.isFull()) {
            return new ResponseEntity<>(makeMap("error", "Game is full"), HttpStatus.FORBIDDEN);

        }
        Player player = currentPlayer(authentication);

        if (authentication == null) {
            return new ResponseEntity<>(makeMap("error", "Need to be logged in to join a game")
                    , HttpStatus.UNAUTHORIZED);
        } else if (game == null) {
            return new ResponseEntity<>(makeMap("error", "No existing game")
                    , HttpStatus.FORBIDDEN);
        } else {

            GamePlayer currentPlayer = new GamePlayer(player, game);
            gamePlayerRepository.save(currentPlayer);
            return new ResponseEntity<>(makeMap("gamePlayerID", currentPlayer.getId())
                    , HttpStatus.CREATED);
        }
    }


    @RequestMapping(path = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> placeShips(@PathVariable Long gamePlayerId,
                                                          @RequestBody Set<Ship> ships,
                                                          Authentication authentication) {

        GamePlayer currentPlayer = gamePlayerRepository.findOne(gamePlayerId);
        Game game = currentPlayer.getGame();
        if (authentication == null) {
            return new ResponseEntity<>(makeMap("error", "log in to place ships"), HttpStatus.UNAUTHORIZED);
        } else if (currentPlayer == null) {
            return new ResponseEntity<>(makeMap("error", "gamePlayer does not exist"), HttpStatus.UNAUTHORIZED);
        } else if (currentPlayer.getPlayer() != currentPlayer(authentication)) {
            return new ResponseEntity<>(makeMap("error", "Not your game"), HttpStatus.UNAUTHORIZED);
        } else if (currentPlayer.getShips().size() > 5) {
            return new ResponseEntity<>(makeMap("error", "Ships are already placed")
                    , HttpStatus.FORBIDDEN);
        }

        else {
            for (Ship ship : ships) {
                ship.setGamePlayer(currentPlayer);

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

        GamePlayer currentPlayer = gamePlayerRepository.findOne(gamePlayerId);
        Integer currentTurnNo = findTurnNumber(currentPlayer);
        GamePlayer opponent = getOpponent(currentPlayer);

        if (authentication == null) {
            return new ResponseEntity<>(makeMap("error", "Need to be logged in")
                    , HttpStatus.UNAUTHORIZED);
        } else if (currentPlayer == null) {
            return new ResponseEntity<>(makeMap("error", "No gamePlayer ")
                    , HttpStatus.UNAUTHORIZED);
        } else if (currentPlayer.getPlayer() != currentPlayer(authentication)) {
            return new ResponseEntity<>(makeMap("error", "Not your game ")
                    , HttpStatus.FORBIDDEN);

       }

        else {

            for (Salvo salvo : salvos) {
                salvo.setGamePlayer(currentPlayer);
               if(isMyTurn(currentPlayer)) {
                   salvoRepository.save(salvo);
               }

            }
                return new ResponseEntity<>(makeMap("succes", "Salvos are created")
                        , HttpStatus.CREATED);
        }
    }


    private Map<String, Object>  ChangeGameState(GamePlayer currentPlayer) {
             Map<String, Object> stateDTO = new LinkedHashMap<String, Object>();

        Game game = currentPlayer.getGame();
        if (!game.isFull()) {
            if (currentPlayer.getShips().size() == 0) {
                currentPlayer.setState(GamePlayer.GameState.WaitingForShips);
            } else {
                currentPlayer.setState(GamePlayer.GameState.WaitingForSecondPlayer);
            }
        } else if (game.isFull()) {
            GamePlayer opponent = getOpponent(currentPlayer);
            if (currentPlayer.getShips().size() == 0) {
                currentPlayer.setState(GamePlayer.GameState.WaitingForShips);
            } else if (opponent.getShips().size() == 0) {
                currentPlayer.setState(GamePlayer.GameState.WaitingForEnemyShips);
            } else if (!isMyTurn(currentPlayer)) {
                currentPlayer.setState(GamePlayer.GameState.WaitingForEnemySalvo);
            } else {
                currentPlayer.setState(GamePlayer.GameState.MyTurn);
            }
        }else if(isGameOver(currentPlayer)){
            currentPlayer.setState(GamePlayer.GameState.GameOver);
            getOpponent(currentPlayer).setState(GamePlayer.GameState.GameOver);
            gamePlayerRepository.save(currentPlayer);
            gamePlayerRepository.save(getOpponent(currentPlayer));
        }
        stateDTO.put("state", currentPlayer.getState());
        return stateDTO ;

    }



    private boolean isGameOver(GamePlayer currentPlayer) {
        GamePlayer opponent = getOpponent(currentPlayer);
         boolean gameOver = false;
        System.out.println(findTurnNumber(currentPlayer) +"ut");
        System.out.println(findTurnNumber(getOpponent(currentPlayer))+"ot");
       if (findTurnNumber(currentPlayer) > 0 && findTurnNumber(currentPlayer) == findTurnNumber(getOpponent(currentPlayer))) {
           System.out.println(getSunkShips(currentPlayer));
                 if (getSunkShips(currentPlayer).size()== 5 || getSunkShips(getOpponent(currentPlayer)).size() == 5){
                    gameOver = true;
            }
       }

        return gameOver;
    }
    private Map<String, Object> getGameState(GamePlayer currentPlayer){
        Map<String, Object> gameStateDTO = new LinkedHashMap<String, Object>();
        gameStateDTO.put("isGameOver", isGameOver(currentPlayer));
        ChangeGameState(currentPlayer);
        if(isGameOver(currentPlayer)){
            gameStateDTO.put("whoWon", whoWon(currentPlayer));
        }
//        if(isGameOver(currentPlayer)){
//            currentPlayer.setState(GamePlayer.GameState.GameOver);
//            getOpponent(currentPlayer).setState(GamePlayer.GameState.GameOver);
//
//            gamePlayerRepository.save(currentPlayer);
//            gamePlayerRepository.save(getOpponent(currentPlayer));
//        }
        return gameStateDTO;
    }

    private String whoWon(GamePlayer currentPlayer){
        if (numberSunkShips(currentPlayer) < numberSunkShips(getOpponent(currentPlayer))){
            changeScores(currentPlayer, "userWon");
            return currentPlayer.getPlayer().getUserName();
        } else if (numberSunkShips(currentPlayer) > numberSunkShips(getOpponent(currentPlayer))){
            changeScores(getOpponent(currentPlayer), "opponetWon");
            return getOpponent(currentPlayer).getPlayer().getUserName();
        } else {
            changeScores(currentPlayer,"tie");
            return "tie";
        }
    }

    private void changeScores(GamePlayer currentPlayer, String tie){
        if(!currentPlayer.getGame().hasScore()){
            if(tie == "tie"){
                Score newScore1 = new Score(currentPlayer.getPlayer(), currentPlayer.getGame(), 0.5);
                Score newScore2 = new Score(getOpponent(currentPlayer).getPlayer(), currentPlayer.getGame(), 0.5);
                scoreRepository.save(newScore1);
                scoreRepository.save(newScore2);
            } else {
                Score newScore1 = new Score(currentPlayer.getPlayer(), currentPlayer.getGame(), 1.0);
                Score newScore2 = new Score(getOpponent(currentPlayer).getPlayer(), currentPlayer.getGame(), 0.0);
                scoreRepository.save(newScore1);
                scoreRepository.save(newScore2);
            }

        }
    }

    private Integer checkForSunkShips (GamePlayer currentPlayer){

        GamePlayer opponent = getOpponent(currentPlayer);

        if (opponent != null ) {
            int sunkShips = 5;
            for (Ship ship : currentPlayer.getShips()) {
                if (ship.isSunk()) {
                    sunkShips--;
                }
            }
            return sunkShips;
        }
        return null;
    }
}

