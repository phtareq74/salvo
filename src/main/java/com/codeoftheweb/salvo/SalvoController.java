
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
        leaderBoardDTO.put("results", CountResultsDTO(player));
        return leaderBoardDTO;
    }

    // building leaderboard - obejct for different game results for that particular player
    private Map<String, Object> CountResultsDTO(Player player) {
        Map<String, Object> countWinsDTO = new LinkedHashMap<String, Object>();
        countWinsDTO.put("won", CountGameResults(1.0, player));
        countWinsDTO.put("tied", CountGameResults(0.5, player));
        countWinsDTO.put("lost", CountGameResults(0.0, player));
        countWinsDTO.put("totalScore", CountSum(player));
        return countWinsDTO;
    }

    // building leaderboard - counting that type of results
    private Long CountGameResults(Double result, Player player) {
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
        mapGamePlayer.put("lastTurn",findTurnNumber(gamePlayer)-1);
        // mapGamePlayer.put("gameState", getGameState(gameplayer));
        mapGamePlayer.put("hitsInfo", setHittedsAndSinked(gamePlayer));
        mapGamePlayer.put("ships", gamePlayer.getShips()
                .stream()
                .map(ship -> makeShipDTO(ship))
                .collect(Collectors.toList()));
        mapGamePlayer.put("user_salvo", makeSalvoDTO(gamePlayer));
        if (opponent != null) {
            mapGamePlayer.put("opponent_salvo", makeSalvoDTO(opponent));
            mapGamePlayer.put("opponent_sunkShips", getSunkShips(opponent));
            mapGamePlayer.put("user_sunkShips", getSunkShips(gamePlayer));
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


    private List<Object> makeSalvoDTO(GamePlayer gamePlayer) {

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

    private Set<Object> setHittedsAndSinked (GamePlayer gamePlayer){

        Integer lastTurn = findTurnNumber(gamePlayer) - 1;
        if (lastTurn == 0){ return null; }

        Set<Object> hitsAndSinksSet = new HashSet<Object>();
        for (int i = 1; i <= lastTurn; i++){
            hitsAndSinksSet.add(MakeHitsAndSinks(i, gamePlayer));
        }
        return hitsAndSinksSet;
    }
    private Map<String, Object> MakeHitsAndSinks(int currentTurn, GamePlayer gamePlayer){

        Map<String, Object> hitsAndSinks = new LinkedHashMap<String, Object>();
        hitsAndSinks.put("turn", currentTurn);
        hitsAndSinks.put("hitsOnUser", MakeHitsOnGivenPlayer(gamePlayer, currentTurn));
        hitsAndSinks.put("hitsOnEnemy", MakeHitsOnGivenPlayer(getOpponent(gamePlayer), currentTurn));
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

private Integer findTurnNumber(GamePlayer gamePlayer) {
    Integer turn = 0;
    for (Salvo salvo : gamePlayer.getSalvos()) {
        Integer turnNo = salvo.getTurn();
        if (turn < turnNo) {
            turn = turnNo;
        }
    }
    return turn + 1;
}
    private long noPlayersSinkedShips(GamePlayer gamePlayer){
        return gamePlayer.getShips().stream()
                .filter(ship -> ship.isSunk())
                .count();
    }

    public List<Object> getSunkShips (GamePlayer gamePlayer) {
        List<Object> sunkShips = new ArrayList<>();
        for (Ship ship : gamePlayer.getShips()) {
            if (ship.isSunk()){
                sunkShips.add(makeShipDTO(ship));
            }
        }
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
        }

        else {
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


        }else {

            for (Salvo salvo : salvos)
            {
                salvo.setGamePlayer(gamePlayer);
                salvoRepository.save(salvo);
            }

            return new ResponseEntity<>(makeMap("succes", "Salvos are created")
                    , HttpStatus.CREATED);

        }
    }
}













