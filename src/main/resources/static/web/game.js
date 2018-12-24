$(document).ready(function () {
    var players_ships = [];
    var enemy_ships = [];
    var User_cell = "";
    var opponent_loc = "";
    var opponent_cell = "";
    var jsonURL = "";
    var urlParam = "";
    var turnNumber = 0;
    var count = 0;
    var shipToSave = [];
    var shipLocation = [];
    var shipName;
    var userShots = [];
    var userSalvos = [];
    var grid1 = document.getElementById("table-rows");
    var grid2 = document.getElementById("opponent-table-rows");
    $.urlParam = function (gp) {
        var results = new RegExp('(gp=\\d*)').exec(window.location.href);
        if (results == null) {
            return null;
        } else {
            results = results[1];

            results = results.toString().split("=");
            urlParam = results[1];

            jsonURL = 'http://localhost:8080/api/game_view/' + urlParam;

        }
    };
    $.urlParam();
    getGameData();
    $("#leaderBTN").click(function () {
        window.location.href = "/web/games.html";
    });

    $("#logOutBtn").click(function () {
        fetch("/api/logout", {
                credentials: 'include',
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
            })
            .then(r => {
                if (r.status == 200) {
                    window.location.href = "/web/games.html"
                    console.log(r)
                }
            })
            .catch(e => console.log(e));

    });
       var allShips = {

        Battleship: {
            shipType: "Battleship",
            length: 4,
            locations: [],
            onGrid: false,
        },
        Destroyer: {
            shipType: "Destroyer",
            length: 3,
            locations: [],
            onGrid: false,
        },
        Submarine: {
            shipType: "Submarine",
            length: 3,
            locations: [],
            onGrid: false,
        },

        PatrolBoat: {
            shipType: "PatrolBoat",
            length: 2,
            locations: [],
            onGrid: false,
        },
        Carrier: {
            shipType: "Carrier",
            length: 5,
            locations: [],
            onGrid: false,
        },

    }

    function getGameData() {
        fetch(jsonURL, {
                credentials: 'include',
                method: "GET"
            })
            .then(function (response) {
                return response.json()
                    .then(function (data) {
                        turnNumber = data.lastTurn;
                        console.log(data);
                        showHide(data);
                        getHeadersHtml();
                        getRowsHtml();
                        getShipLocations(data);
                        getEnemyShipLocations(data);
                        getOpponentRowsHtml();
                        createPlayerInfo(data);
                        createOpponentInfo(data);
                        getPlayerName(data);
                        $("#placing").click(function () {
                            listenToEvents();
                        });
                        postShips(data, allShips);
                        postSalvos(data);
                        shootSalvos();
                        getSunkShips(data);
                        // timeOut(data,urlParam)
                        getGameHistory(data);
                        getSalvos("Enemy_", data.user_salvo, enemy_ships, data);
                        getSalvos("User_", data.opponent_salvo, data.ships, data);
                        getHisAndSinks(data);
                    })
            })

            .catch(function (error) {

            })
    }

    function showHide(data) {
        if (data.ships.length >= 0 && data.ships.length < 5) {
            $("#placing").show();

        } else {
            $("#placing").hide();
        }
    }

    function getHeadersHtml() {
        var colheaders = "";
        for (var i = 0; i < 10; i++) {
            var col = 1 + i;
            var colheader = "<td> " + col + " </td>";
            colheaders += colheader;
        }
        return "<tr><td class='table-light'></td>" + colheaders + "</tr>"
    }

    function getRowsHtml() {
        var rows = "";
        var letters = ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J"];
        $.each(letters, function (index, letters) {
            var row_cell = "";
            var row = "<tr><td class='table-primary'> " + letters + " </td>";
            for (var i = 0; i < 10; i++) {
                User_cell = "User_" + letters + (1 + i);
                row_cell = "<td id=" + User_cell + "> </td>";
                row += row_cell;

            }
            row += "</tr>";
            rows += row;

        });

        return rows
    }
    $("#table-headers").html(getHeadersHtml());
    $("#table-rows").html(getRowsHtml());

    function getShipLocations(data) {
        var ship = data.ships;
        $.each(ship, function (index, ship) {
            $.each(ship.locations, function (i, location) {
                $("#User_" + location).addClass('ship-placed')
                players_ships.push(location);

            })
        })
    }

    function getEnemyShipLocations(data) {
        var ship = [];
        $.each(ship, function (index, ship) {
            $.each(ship.locations, function (i, location) {
                opponent_loc = "Enemy_" + location;
                enemy_ships.push(opponent_loc);
            })
        })
    }

    function createPlayerInfo(data) {
        var info = "";
        var viewer_id = urlParam;
        $(data.gamePlayers).each(function (i, gamePlayer) {

            if (gamePlayer.id == viewer_id) {
                info = gamePlayer.player.userName;
            }

        });

        return info;
    }

    function createOpponentInfo(data) {
        var info = "";
        var viewer_id = urlParam;
        $(data.gamePlayers).each(function (i, gamePlayer) {
            if (gamePlayer.id != viewer_id) {
                info = gamePlayer.player.userName;
            }
        });

        return info;
    }

    function getPlayerName(data) {
        $("#player").text(createPlayerInfo(data) + " (You)");
        $("#opponent").text(createOpponentInfo(data) + " (Enemy)");
    }

    function getOpponentRowsHtml() {
        var opponent_rows = "";
        var letters = ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J"];
        $.each(letters, function (index, letters) {
            var opponent_row_cell = "";
            var opponent_row = "<tr><td class='table-primary'> " + letters + " </td>";
            for (var i = 0; i < 10; i++) {
                opponent_cell = "Enemy_" + letters + (1 + i);
                opponent_row_cell = "<td id=" + opponent_cell + "> </td>";
                opponent_row += opponent_row_cell;
            }
            opponent_row = opponent_row + "</tr>";
            opponent_rows += opponent_row;
        });
        return opponent_rows
    }
    $("#opponent-table-headers").html(getHeadersHtml());
    $("#opponent-table-rows").html(getOpponentRowsHtml());

    function getSalvos(grid, salvos, ships, data) {
        var salvo = salvos;
        $.each(salvo, function (index, salvo) {
            $.each(salvo.locations, function (i, cell) {
                opponent_cell = cell;
                // $("#" + grid + opponent_cell).addClass("opcell");
                $("#" + grid + opponent_cell).text(salvo.turn);
                if ($("#" + grid + opponent_cell).hasClass('ship-placed')) {
                    $("#" + grid + opponent_cell).addClass("hitcell");
                } else {
                    $("#" + grid + opponent_cell).addClass("missedcell");
                }

            })
        })
        var hitsData;
        var hitsEnemy;
        var hitsloc;
        var output;
        for (var i = 0; i < data.hitsInfo.length; i++) {
            hitsData = data.hitsInfo[i];
            for (var j in hitsData.hitsOnEnemy) {
                hitsEnemy = hitsData.hitsOnEnemy[j];
                for (var h = 0; h < hitsEnemy.hits.length; h++) {
                    hitsloc = hitsEnemy.hits[h];
                    $("#" + "Enemy_" + hitsloc).addClass("hitcell");
                }
            }
        }

    }

    function getHisAndSinks(data) {
        var oppenetSunkShip = [];
        var userSunkShip = [];
        for (var i = 0; i < data.opponent_sunkShips.length; i++) {
            oppenetSunkShip = data.opponent_sunkShips[i].locations;
            $.each(oppenetSunkShip, function (j, el) {
                $("#" + "Enemy_" + el).addClass("sunkShip");
            })
        }
        for (var u = 0; u < data.user_sunkShips.length; u++) {
            userSunkShip = data.user_sunkShips[u].locations;
            $.each(userSunkShip, function (j, el) {
                $("#" + "User_" + el).addClass("sunkShip");

            })
        }

    }

    function listenToEvents() {
        switch (count) {

            case 0:
                $("#CarrierContainer").show();
                break;

            case 1:
                $("#BattleshipContainer").show();
                $("#CarrierContainer").hide();
                break;
            case 2:
                $("#DestroyerContainer").show();
                $("#BattleshipContainer").hide();
                break;
            case 3:
                $("#SubmarineContainer").show();
                $("#DestroyerContainer").hide();;
                break;
            case 4:
                $("#PatrolBoatContainer").show();
                $("#SubmarineContainer").hide();
                break;
            case 5:
                $("#PatrolBoatContainer").hide();
                $("#placing").hide();
                $("#clearBoard").show();
                $("#saveShips").show();

        }
        $("#Carrier,#Battleship,#Destroyer,#Submarine,#PatrolBoat").click(function () {
            shipID = this.id;
            placeShips(shipID, grid1);
        })
    }

    function placeShips(shipID, grid1) {
        var loc;
        var grid1Array = grid1.getElementsByTagName('td');
        for (var j = 0; j < grid1Array.length; j++) {
            loc = grid1Array[j];
            $(loc).addClass('gridCell');
        }
        var ship = allShips[shipID];
        printEachShip(ship, shipID)
    }

    function printEachShip(ship, shipID) {
        var shipLength;
        shipName = ship.shipType;
        shipLocation = ship.locations;
        var shipStart = shipLocation[0];
        var rowRight = [];
        var rowLeft = [];
        var colDown = [];
        var colUp = [];
        var shipEnd;
        var optionsArray = [];
        if (shipID == shipName) {

            $("#table-rows td").click(function () {

                if (!ship.onGrid) {

                    shipLength = ship.length;

                    if ($(this).hasClass('gridCell') & !$(this).hasClass('table-primary')) {
                        var tdID = $(this).attr('id');
                        var cellStart = tdID.split("_");
                        var str1 = cellStart[0];
                        var str2 = cellStart[1];
                        var cell = str2.split(/([0-9]+)/);
                        var letter = cell[0];
                        var number = cell[1];
                        number = Number(number);
                        var letterIndex = 0;
                        var allLetters = ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J"];
                        $.each(allLetters, function (i, e) {
                            if (e == letter) {
                                letterIndex = i;
                            };
                        });
                        if (!$(this).hasClass('possibleArea')) {
                            rowRight = [];
                            rowLeft = [];
                            colDown = [];
                            colUp = [];

                            if (number + shipLength - 1 < 11 & !$(this).hasClass('possibleArea')) {

                                for (var i = 0; i < shipLength - 1; i++) {

                                    number++
                                    var oright = (document.getElementById('User_' + letter + number));

                                    if ($(oright).hasClass('ship-location')) {
                                        rowRight = [];
                                        break
                                    } else {
                                        rowRight.push(oright);

                                    }

                                }
                                optionsArray = optionsArray.concat(rowRight)

                            }

                            number = cell[1];
                            if (number - (shipLength - 1) > 0 & !$(this).hasClass('possibleArea')) {
                                for (var i = 0; i < shipLength - 1; i++) {
                                    number--
                                    var oleft = (document.getElementById('User_' + letter + number));
                                    if ($(oleft).hasClass('ship-location')) {
                                        rowLeft = [];
                                        break
                                    } else {
                                        rowLeft.push(oleft);
                                    }
                                }
                                optionsArray = optionsArray.concat(rowLeft);
                            };

                            number = cell[1];
                            if (letterIndex + shipLength - 1 < 10 & !$(this).hasClass('possibleArea')) {
                                for (var i = 0; i < shipLength - 1; i++) {

                                    letterIndex++
                                    var odown = (document.getElementById('User_' + allLetters[letterIndex] + number));
                                    if ($(odown).hasClass('ship-location')) {
                                        colDown = [];
                                        break
                                    } else {
                                        colDown.push(odown);
                                    }
                                }
                                optionsArray = optionsArray.concat(colDown);


                            };

                            letterIndex = allLetters.indexOf(cell[0]);
                            if (letterIndex - (shipLength - 1) >= 0 & !$(this).hasClass('possibleArea')) {
                                for (var i = 0; i < shipLength - 1; i++) {

                                    letterIndex--
                                    var oup = (document.getElementById('User_' + allLetters[letterIndex] + number));
                                    if ($(oup).hasClass('ship-location')) {
                                        colUp = [];
                                        break
                                    } else {
                                        colUp.push(oup);
                                    }
                                }
                                optionsArray = optionsArray.concat(colUp);


                            };


                            if (shipLocation.length == 0) {
                                shipStart = this;
                                shipToSave.push(shipStart);
                                $(this).addClass('ship-location');

                                $(optionsArray).addClass('possibleArea');
                            }



                        }

                        if ($(this).hasClass('possibleArea')) {
                            rowRight.forEach(rowRightCell => {

                                if (this == rowRightCell) {
                                    $(rowRight).removeClass('possibleArea');
                                    $(rowRight).addClass('ship-location');
                                    $(rowLeft).removeClass('possibleArea');
                                    $(colDown).removeClass('possibleArea');
                                    $(colUp).removeClass('possibleArea');
                                    shipToSave = shipToSave.concat(rowRight);

                                }
                            });
                            rowLeft.forEach(rowLeftCell => {
                                if (this == rowLeftCell) {
                                    $(rowLeft).removeClass('possibleArea');
                                    $(rowLeft).addClass('ship-location');
                                    $(rowRight).removeClass('possibleArea');
                                    $(colDown).removeClass('possibleArea');
                                    $(colUp).removeClass('possibleArea');
                                    shipToSave = shipToSave.concat(rowLeft);

                                }
                            });
                            colDown.forEach(colDownCell => {

                                if (this == colDownCell) {
                                    $(colDown).removeClass('possibleArea');
                                    $(colDown).addClass('ship-location');
                                    $(rowRight).removeClass('possibleArea');
                                    $(rowLeft).removeClass('possibleArea');
                                    $(colUp).removeClass('possibleArea');
                                    shipToSave = shipToSave.concat(colDown);


                                }
                            });
                            colUp.forEach(colUpCell => {
                                if (this == colUpCell) {
                                    $(colUp).removeClass('possibleArea');
                                    $(colUp).addClass('ship-location');
                                    $(rowRight).removeClass('possibleArea');
                                    $(rowLeft).removeClass('possibleArea');
                                    $(colDown).removeClass('possibleArea');
                                    shipToSave = shipToSave.concat(colUp);
                                }

                            });

                            ship.onGrid = true;
                            count += 1;
                            listenToEvents();
                            shipToSave.forEach(ID => {
                                var locID = $(ID).attr('id').split("_")[1];
                                if (!shipLocation.includes(locID)) {
                                    shipLocation.push(locID);

                                }
                            });
                            shipToSave = [];
                        }
                    }
                }
            });

        }

        $("#clearBoard").click(function () {
            $(shipLocation).removeClass('ship-location');
            shipLocation = [];
            $("#placing").show();
            $("#clearBoard").hide();
            $("#saveShips").hide();
        });

    }

    function postShips(data, allShips) {
        $("#saveShips").click(function () {
            var gpid = urlParam;
            fetch("/api/games/players/" + gpid + "/ships", {
                    credentials: 'include',
                    method: 'POST',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },

                    body: JSON.stringify([{
                            shipType: "Carrier",
                            locations: allShips.Carrier.locations
                    },
                        {
                            shipType: "Battleship",
                            locations: allShips.Battleship.locations
                    },
                        {
                            shipType: "Destroyer",
                            locations: allShips.Destroyer.locations
                    },
                        {
                            shipType: "Submarine",
                            locations: allShips.Submarine.locations
                    },
                        {
                            shipType: "PatrolBoat",
                            locations: allShips.PatrolBoat.locations
                    }
                           ]),
                })
                .then(r => {
                    console.log(r)
                    window.location.href = "/web/game.html?gp=" + gpid;

                })
                .catch(e => console.log(e))
        });

    }

    function shootSalvos() {
        $("#opponent-table-rows td").click(function () {
            if (userSalvos.length < 5) {
                if (!$(this).hasClass('table-primary')) {
                    if ($(this).hasClass('preSalvo')) {
                        $(this).removeClass('preSalvo');
                        var index = userShots.indexOf(this);
                        userShots.splice(index, 1);
                        userSalvos.splice(index, 1)
                    } else if (!$(this).hasClass('preSalvo') && !$(this).hasClass('missedcell')) {
                        userShots.push(this);
                        $(userShots).addClass('preSalvo');
                        userShots.forEach(locID => {
                            var salvoLoc = $(locID).attr('id').split("_")[1];
                            if (!userSalvos.includes(salvoLoc)) {
                                userSalvos.push(salvoLoc);
                                if (userSalvos.length == 5) {
                                    turnNumber += 1
                                    $("#saveSalvos").show();

                                }
                            }
                        })

                    }

                } else {
                    alert("shoot another cell");

                }
            } else {
                alert("You can't shoot more than 5");

            }

        })
    }

    function postSalvos(data) {
        $("#saveSalvos").click(function () {
            var gpid = urlParam;
            fetch("/api/games/players/" + gpid + "/salvos", {
                    credentials: 'include',
                    method: 'POST',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },

                    body: JSON.stringify([{
                        turn: turnNumber,
                        locations: userSalvos

                }]),
                })
                .then(r => {
                    console.log(r)

                    window.location.href = "/web/game.html?gp=" + gpid;

                    console.log(locations);
                })
                .catch(e => console.log(e))
        });

    }

    function getSunkShips(data) {
        var sunkETable = '';
        var sunkUTable = '';
        sunkUTable += '<table class="table table-bordered">' +
            '<tr>' +
            '<th>User sunk ships</th>' +
            '</tr>'
        for (var i = 0; i < data.user_sunkShips.length; i++) {
            sunkUTable += '<tr>' +
                '<td>' + data.user_sunkShips[i].shipType + '</td>' +
                '</tr>'
        }
        sunkUTable += '</table>'
        $("#sunkUShipsTable").append(sunkUTable);


        sunkETable += '<table class="table table-bordered">' +
            '<tr>' +
            '<th>Enemy sunk ships</th>' +
            '</tr>'
        for (var j = 0; j < data.opponent_sunkShips.length; j++) {
            sunkETable += '<tr>' +
                '<td>' + data.opponent_sunkShips[j].shipType + '</td>' +
                '</tr>'
        }
        sunkETable += '</table>'
        $("#sunkEShipsTable").append(sunkETable);


    }

    function timeOut(data, urlParam) {
        if (data.state.state === "WaitingForSecondPlayer" || data.state.state === 'WaitingForEnemyShips' || data.state.state === 'WaitingForEnemySalvo') {
            setTimeout(function () {
                window.location = window.location.href = "/web/game.html?gp=" + urlParam;
            }, 10000);

        }
    }

    function getGameHistory(data) {
        var shipIsHit;
        var uHits;
        var hitsUser;
        for (var i = 0; i < data.hitsInfo.length; i++) {
            var hitsData = data.hitsInfo[i];
        }
        for (var j in hitsData.hitsOnEnemy) {
            var hitsEnemy = hitsData.hitsOnEnemy[j];
        }
        for (var u in hitsData.hitsOnUser) {
            hitsUser = hitsData.hitsOnUser[u];
            if (hitsUser.hitsTillNow > 0) {
                shipIsHit = u;
                uHits = hitsUser.hitsTillNow;
                var historyInfo = '';
                historyInfo += '<table class="table table-bordered">' +
                    '<tr><h3>Game History</h3></tr>' +
                    '<tr>' +
                    '<th>Turn</th>' +
                    '<th>Hits on user</th>' +
                    '<th>Hits on enemy</th>' +
                    '</tr>' +
                    '<tr>' +
                    '<td></td>' +
                    '<td>' +
                    '<table class="table table-bordered">' +
                    '<tr>' +
                    '<th>Ship type</th>' +
                    '<th>All hits</th>' +
                    '<th>Locations</th>' +
                    '<th>Ship status</th>' +
                    '<th>Ships left</th>' +
                    '</tr>' +
                    '<tr>' +
                    '<td>' +

                    '</td>' +
                    '<td></td>' +
                    '<td></td>' +
                    '<td></td>' +
                    '<td></td>' +
                    '</tr>' +
                    '</table></td>' +
                    '<td><table class="table table-bordered">' +
                    '<tr>' +
                    '<th>Ship type</th>' +
                    '<th>All hits</th>' +
                    '<th>Locations</th>' +
                    '<th>Ship status</th>' +
                    '<th>Ships left</th>' +
                    '</tr>' +
                    '<tr>' +
                    '<td></td>' +
                    '<td></td>' +
                    '<td></td>' +
                    '<td></td>' +
                    '<td></td>' +
                    '</tr>' +
                    '</table></td>' +
                    '</tr>' +
                    '</table>'

                $("#hitoryTable").html(historyInfo);

            }
        }

    }
});
