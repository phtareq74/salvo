$(document).ready(function () {

    var form = $("#login-form");
    var message = document.getElementById("alertMessage");


    $("#loginBtn").click(function () {
        login(form);

    });

    $("#logoutBtn").click(function () {
        logout();
    });

    $("#signUpBtn").click(function () {
        createPlayer();
    });
//    $("#createGameBtn").click(function () {
//        createGame();
//    });

    getGamesData();

    function getGamesData() {

        fetch("/api/games", {
                credentials: 'include',
                method: "GET"
            })
            .then(function (response) {
                return response.json()

                    .then(function (data) {
                        console.log(data);
                        getLeaderboardJSON();
                        checkCurrentPlayer(data);

                    })

            })

            .catch(function (error) {

            })
    }

    function checkCurrentPlayer(data) {

        var currentPlayer = data.currentplayer.userName;
        if (currentPlayer != null) {
            message.innerHTML = "Succesfully logged in";
            console.log(message);
            $("#formlogin").hide();
            $("#logoutBtn").show();
            $("#createGameBtn").show();
            $("#allList").show();
            createLists(data);

        }

    }

    function login(data) {
        var userName = document.getElementById("inputEmail").value;
        console.log(userName)

        var password = document.getElementById("inputPassword").value;

        fetch("/api/login", {
                credentials: 'include',
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: 'userName=' + userName + '&password=' + password


            })

            .then(r => {
                if (r.status == 200) {
                    console.log(r);
                    location.reload();

                } else if (r.status == 401) {
                    message.innerHTML = "User not found";
                    console.log(message);

                }

            })
            .catch(e => console.log(e));

    }

    function logout() {
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

                    console.log(r)
                }
            })
            .catch(e => console.log(e));

    }

    function createPlayer(form) {
        let userName = document.getElementById("inputEmail").value;
        console.log(userName)

        let password = document.getElementById("inputPassword").value;

        fetch("/api/players", {
                credentials: 'include',
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: 'userName=' + userName + '&password=' + password

            })
            .then(r =>
                r.json()
            )
            .then(data => {
                console.log(data)

                if (data.error) {
                    message.innerHTML = data.error
                } else {
                    console.log(userName);
                }
            })
            .catch(e => console.log(e));

    }

    $("#createGameBtn").click(function (data) {

        fetch("/api/games", {
                credentials: 'include',
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
            })
            .then(r => r.json())
            .then(r => {
                    console.log(r);
                if (r.gpCreated != null) {
                    var gpID = r.gpCreated;
                    console.log(gpID);
                    window.location.href = "/web/game.html?gp=" + gpID;

                } else {
                    alert("error")
                }

            })
            .catch(function (r) {
                console.log(r)
            })
    });

    function createLists(data) {
//        console.log(data.games)
        for (var i = 0; i < data.games.length; i++) {
            console.log(i)
            var li = document.createElement("li");

            if (data.games[i].gamePlayers[1] != null) {
                var p1 = data.games[i].gamePlayers[0].player.userName;

                var p2 = data.games[i].gamePlayers[1].player.userName;

            } else {
                console.log(data.games[i])
                var p1 = data.games[i].gamePlayers[0].player.userName;
                var p2 = "Waiting for player to join!";

                if (data.currentplayer != null) {
                    var joinBtn = document.createElement("button");
                    var gmID = data.games[i].gid;
                    joinBtn.innerHTML = data.currentplayer.userName + ": you can join this game!";
                    joinBtn.setAttribute("data-gid", gmID);
                    joinBtn.setAttribute("class", "joinGameBtn btn-warning");
                }

            }

            var date = new Date(data.games[i].create);
            date = date.toLocaleString();

            li.innerHTML = date + ": " + p1 + " --vs-- " + p2;

            if (data.currentplayer != null &&
                p2 == "Waiting for player to join!" &&
                p1 !== data.currentplayer.userName) {
                li.append(joinBtn);
            }

            document.getElementById("listGame").appendChild(li);

            $(".joinGameBtn").click(function () {
                var gameID = $(this).data("gid");
                console.log(gameID);
                fetch("/api/games/" + gameID + "/players", {
                        credentials: 'include',
                        method: 'POST',
                    })
                .then(r => r.json())
                    .then(r => {
                        if (r.gamePlayerID != null) {
                            window.location.href = "/web/game.html?gp=" + r.gamePlayerID
                        } else {
                            alert("error")
                        }

                    })
                    .catch(e => console.log(e))

            });

            if (data.currentplayer != null) {

                if (data.currentplayer.userName == p1 ||
                    data.currentplayer.userName == p2) {
                    var btn = document.createElement("button");
                    btn.innerHTML = "Re-enter game ";
                    li.append(btn);
                }

                for (var j = 0; j < data.games[i].gamePlayers.length; j++) {
                    var playerInGame = data.games[i].gamePlayers[j].player.userName;
                    if (playerInGame == data.currentplayer.userName) {
                        var gpID = data.games[i].gamePlayers[j].gpid;

                        btn.setAttribute("data-gpid", gpID);
                        btn.setAttribute("class", "reEnterBtn btn-info");
                    }

                    $(".reEnterBtn").click(function () {
                        var btnDataId = $(this).data("gpid");
                        window.location.href = "http://localhost:8080/web/game.html?gp=" + btnDataId;
                    });
                }
            }
        }
    }


    function getLeaderboardJSON() {
        $.getJSON("../api/leaderboard", function (leaderboardJSON) {
            leaderBoardTable(leaderboardJSON);
        });
    }

    function leaderBoardTable(leaderBoard) {
        console.log(leaderBoard);
        leaderBoard.sort(function (a, b) {
            return b.results.totalScore - a.results.totalScore
        })

        for (var i = 0; i < leaderBoard.length; i++) {

            var tr = document.createElement("tr");

            var result = leaderBoard[i].results;
            var totalscore = result.totalScore;
            var arrayWon = result.won;
            var arrayTied = result.tied;
            var arrayLost = result.lost;

            var tdName = document.createElement("td");
            tdName.innerHTML = leaderBoard[i].userName;
            tr.appendChild(tdName);

            var tdTotalScore = document.createElement("td");
            tdTotalScore.innerHTML = totalscore;
            tr.appendChild(tdTotalScore);

            var tdWon = document.createElement("td");
            tdWon.innerHTML = arrayWon;
            tr.appendChild(tdWon);

            var tdLost = document.createElement("td");
            tdLost.innerHTML = arrayLost;
            tr.appendChild(tdLost);

            var tdTied = document.createElement("td");
            tdTied.innerHTML = arrayTied;
            tr.appendChild(tdTied);

            var tbody = document.getElementById("LeaderBoard");
            tbody.appendChild(tr);

        }
    }


});

