package org.secuso.privacyfriendlywerwolf.client;

import android.content.Intent;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;

import org.secuso.privacyfriendlywerwolf.R;
import org.secuso.privacyfriendlywerwolf.activity.GameActivity;
import org.secuso.privacyfriendlywerwolf.activity.MainActivity;
import org.secuso.privacyfriendlywerwolf.activity.StartClientActivity;
import org.secuso.privacyfriendlywerwolf.context.GameContext;
import org.secuso.privacyfriendlywerwolf.controller.Controller;
import org.secuso.privacyfriendlywerwolf.model.NetworkPackage;
import org.secuso.privacyfriendlywerwolf.model.Player;
import org.secuso.privacyfriendlywerwolf.server.ServerGameController;
import org.secuso.privacyfriendlywerwolf.util.Constants;
import org.secuso.privacyfriendlywerwolf.util.ContextUtil;
import org.secuso.privacyfriendlywerwolf.util.GameUtil;

import java.util.List;
import java.util.Random;

import static org.secuso.privacyfriendlywerwolf.util.Constants.EMPTY_VOTING_PLAYER;


/**
 * updates the model on the client, aswell as the view on the client and initiates communication to the server
 *
 * @author Tobias Kowalski <tobias.kowalski@stud.tu-darmstadt.de>
 * @author Florian Staubach <florian.staubach@stud.tu-darmstadt.de>
 */
public class ClientGameController extends Controller {

    private static final String TAG = "ClientGameController";
    private static final ClientGameController GAME_CONTROLLER = new ClientGameController();

    ServerGameController serverGameController;

    Player me;
    long myId;

    StartClientActivity startClientActivity;
    GameActivity gameActivity;
    WebsocketClientHandler websocketClientHandler;
    GameContext gameContext;


    private ClientGameController() {
        Log.d(TAG, "GameController singleton created");
        websocketClientHandler = new WebsocketClientHandler();
        websocketClientHandler.setGameController(this);
        gameContext = GameContext.getInstance();
        //serverGameController = ServerGameController.getInstance();
    }

    public static ClientGameController getInstance() {
        return GAME_CONTROLLER;

    }

    public void startGame(GameContext gc) {
        //TODO: extract the roles of the players and give it to the activity
        //TODO: extract every other information which were send by the server

        gameContext.copy(gc);
        startClientActivity.startGame();
        //wait some time before the gameactivity has been created
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
        }

        gameActivity.outputMessage(R.string.progressBar_initial);
        //gameActivity.longOutputMessage(R.string.gameStart_start);
        gameActivity.longOutputMessage(R.string.gameStart_hintRoles);


    }


    public void initiateWerewolfPhase() {

        Player ownPlayer = GameContext.getInstance().getPlayerById(myId);

        if (myId == Constants.SERVER_PLAYER_ID && gameActivity.getMediaPlayer() != null) {

            gameActivity.setMediaPlayer(MediaPlayer.create(gameActivity.getApplicationContext(), R.raw.night_falls));
            gameActivity.getMediaPlayer().start();

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
            }

            gameActivity.outputMessage(R.string.message_villagers_sleep);
            if(!ownPlayer.isDead()) {
                gameActivity.longOutputMessage("Close your eyes");
            }
            //gameActivity.getMediaPlayer().stop();
            gameActivity.setMediaPlayer(MediaPlayer.create(gameActivity.getApplicationContext(), R.raw.close_your_eyes));
            gameActivity.getMediaPlayer().start();

            try {
                Thread.sleep(7000);
            } catch (InterruptedException e) {
                Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
            }

        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
            }
            gameActivity.outputMessage(R.string.message_villagers_sleep);
            if(!ownPlayer.isDead()) {
                gameActivity.longOutputMessage("Close your eyes");
            }
            try {
                Thread.sleep(7000);
            } catch (InterruptedException e) {
                Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
            }
        }

        gameActivity.outputMessage(R.string.message_werewolfes_awaken);

            gameContext.setSetting(GameContext.Setting.KILLED_BY_WEREWOLF, null);

        if (myId == Constants.SERVER_PLAYER_ID && gameActivity.getMediaPlayer() != null) {
            //gameActivity.getMediaPlayer().stop();
            gameActivity.setMediaPlayer(MediaPlayer.create(gameActivity.getApplicationContext(), R.raw.wolves_wake));
            gameActivity.getMediaPlayer().start();

            try {
                Thread.sleep(4500);
            } catch (InterruptedException e) {
                Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
            }

            if (ContextUtil.IS_FIRST_ROUND) {
                ContextUtil.IS_FIRST_ROUND = false;


                gameActivity.setMediaPlayer(MediaPlayer.create(gameActivity.getApplicationContext(), R.raw.wolves_meet));
                gameActivity.getMediaPlayer().start();
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
                }
            }

        } else {

            if (ContextUtil.IS_FIRST_ROUND) {
                ContextUtil.IS_FIRST_ROUND = false;
                try {
                    Thread.sleep(10500);
                } catch (InterruptedException e) {
                    Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
                }
            } else {
                try {
                    Thread.sleep(4500);
                } catch (InterruptedException e) {
                    Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
                }
            }
        }


        //TODO: put into string.xml with translation.. everything
        //gameActivity.longOutputMessage("Die Werwölfe erwachen und suchen sich ein Opfer!");
        if (gameContext.getPlayerById(myId).getPlayerRole() == Player.Role.WEREWOLF && !ownPlayer.isDead()) {
            gameActivity.longOutputMessage("Macht euch bereit für die Abstimmung!");

        }


        sendDoneToServer();

    }


    public void initiateWerewolfVotingPhase() {
        final int time = Integer.parseInt(gameContext.getSetting(GameContext.Setting.TIME_WEREWOLF));
        gameActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // TODO: there is an ASyncNetworkSocket exception when called here
                gameActivity.makeTimer(time).start();
            }
        });


        if (myId == Constants.SERVER_PLAYER_ID && gameActivity.getMediaPlayer() != null) {
            //gameActivity.getMediaPlayer().stop();
            gameActivity.setMediaPlayer(MediaPlayer.create(gameActivity.getApplicationContext(), R.raw.wolves_vote));
            gameActivity.getMediaPlayer().start();

        }

        //gameActivity.longOutputMessage("Die Werwölfe erwachen und suchen sich ein Opfer!");

        //gameActivity.outputMessage(R.string.message_werewolfes_vote);

        Player ownPlayer = GameContext.getInstance().getPlayerById(myId);
        if (!ownPlayer.isDead() && ownPlayer.getPlayerRole().equals(Player.Role.WEREWOLF)) {
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
            }
            gameActivity.openVoting();
        } else {
            // noch kein done: client muss je nach entscheidung der Werwoelfe seinen gamecontext noch updaten
            //sendDoneToServer();
        }

    }

    public void endWerewolfPhase() {

        gameActivity.outputMessage(R.string.message_werewolfes_sleep);

        Player ownPlayer = GameContext.getInstance().getPlayerById(myId);

        if (myId == Constants.SERVER_PLAYER_ID && gameActivity.getMediaPlayer() != null) {
            //gameActivity.getMediaPlayer().stop();
            gameActivity.setMediaPlayer(MediaPlayer.create(gameActivity.getApplicationContext(), R.raw.wolves_sleep));
            gameActivity.getMediaPlayer().start();
        }
        Player roundVictim = getPlayerKilledByWerewolfesName();
        if (gameContext.getPlayerById(myId).getPlayerRole() == Player.Role.WEREWOLF
                && (!ownPlayer.isDead()
                || (roundVictim != null && roundVictim.getPlayerId() == myId))) {
            gameActivity.longOutputMessage("Close your eyes!");


        }
        // give Werewolves 5 secs to close eyes
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
        }


        sendDoneToServer();
    }

    public void initiateWitchElixirPhase() {
        Player roundVictim = getPlayerKilledByWerewolfesName();
        if (GameUtil.isWitchAlive() || (roundVictim != null && roundVictim.getPlayerRole() == Player.Role.WITCH)) {
            gameActivity.outputMessage(R.string.message_witch_awaken);
            gameActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int time = Integer.parseInt(gameContext.getSetting(GameContext.Setting.TIME_WITCH));
                    gameActivity.makeTimer(time).start();
                }
            });


            if (myId == Constants.SERVER_PLAYER_ID && gameActivity.getMediaPlayer() != null) {
                //gameActivity.getMediaPlayer().stop();

                gameActivity.setMediaPlayer(MediaPlayer.create(gameActivity.getApplicationContext(), R.raw.witch_wakes));
                gameActivity.getMediaPlayer().start();
                Log.d(TAG, "Thread3: " + Thread.currentThread().getName());

            }

            // Tell the witch who got killed by Werewolves
            if (gameContext.getPlayerById(myId).getPlayerRole().equals(Player.Role.WITCH)) {
                StringBuilder sb = new StringBuilder();
                sb.append(gameActivity.getString(R.string.gamefield_witch_elixir_action_message1));
                Player victim = getPlayerKilledByWerewolfesName();
                if (victim != null) {
                    sb.append(" ");
                    sb.append(victim.getPlayerName());
                    sb.append(System.getProperty("line.separator"));
                } else {
                    sb.append(" Nobody .");
                }
                gameActivity.showWitchTextPopup("Killed by Werewolves", sb.toString());
            }

            // transition from witch_wakes to witch_heal
            try {
                Thread.sleep(5500);
            } catch (InterruptedException e) {
                Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
            }

            if (myId == Constants.SERVER_PLAYER_ID && gameActivity.getMediaPlayer() != null) {

                gameActivity.setMediaPlayer(MediaPlayer.create(gameActivity.getApplicationContext(), R.raw.witch_heal));
                gameActivity.getMediaPlayer().start();


            }


            Log.d(TAG, "Thread4: " + Thread.currentThread().getName());
            if (gameContext.getSetting(GameContext.Setting.WITCH_ELIXIR) == null) {
                if (gameContext.getPlayerById(myId).getPlayerRole().equals(Player.Role.WITCH)) {
                    useElixir();
                } else {
                    // noch kein done: client muss je nach entscheidung der hexe seinen gamecontext noch updaten
                    //sendDoneToServer();
                }
            } else {
                try {
                    Thread.sleep(4500);
                } catch (InterruptedException e) {
                    Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
                }
                sendDoneToServer();
            }

            // GameThread waits for the MusicThread
            /*try {
                Log.d(TAG, "Another Thread1: " + Thread.currentThread().getName());
                Thread.sleep(5000);
                Log.d(TAG, "Another Thread2: " + Thread.currentThread().getName());
            } catch (InterruptedException e) {
                Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
            }*/


            //gameActivity.longOutputMessage("Die Hexe erwacht!");


        } else

        {
            //gameActivity.longOutputMessage("Es ist keine Hexe im Spiel vorhanden.");
            if (myId == Constants.SERVER_PLAYER_ID && gameActivity.getMediaPlayer() != null) {
                //gameActivity.getMediaPlayer().stop();
                gameActivity.setMediaPlayer(MediaPlayer.create(gameActivity.getApplicationContext(), R.raw.witch_down));
                gameActivity.getMediaPlayer().start();
                try {
                    Thread.sleep(3250);
                } catch (InterruptedException e) {
                    Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
                }
            }
            sendDoneToServer();
        }

    }

    public void endWitchElixirPhase() {
        Log.d(TAG, "Entering End of WitchElixirPhase!");
        // transition heal popup -> witch popup
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
        }
        String elixirSetting = gameContext.getSetting(GameContext.Setting.WITCH_ELIXIR);
        if (myId == Constants.SERVER_PLAYER_ID) {
            ServerGameController.HOST_IS_DONE = true;
            if (!TextUtils.isEmpty(elixirSetting)) {
                serverGameController.handleWitchResultElixir(Long.parseLong(elixirSetting));
            } else {
                serverGameController.handleWitchResultElixir(null);
            }
        } else {
            try {
                NetworkPackage<GameContext.Phase> np = new NetworkPackage<>(NetworkPackage.PACKAGE_TYPE.WITCH_RESULT_ELIXIR);
                np.setOption(GameContext.Setting.WITCH_ELIXIR.toString(), elixirSetting);
                websocketClientHandler.send(np);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void initiateWitchPoisonPhase() {

        Player roundVictim = getPlayerKilledByWerewolfesName();
        if (GameUtil.isWitchAlive() || (roundVictim != null && roundVictim.getPlayerRole() == Player.Role.WITCH)) {
            if (myId == Constants.SERVER_PLAYER_ID && gameActivity.getMediaPlayer() != null) {
                // prevent music overlays
                if (gameActivity.getMediaPlayer().isPlaying()) {
                    gameActivity.getMediaPlayer().stop();
                }
                gameActivity.setMediaPlayer(MediaPlayer.create(gameActivity.getApplicationContext(), R.raw.witch_poison));
                gameActivity.getMediaPlayer().start();

            }

            if (gameContext.getSetting(GameContext.Setting.WITCH_POISON) == null) {
                if (gameContext.getPlayerById(myId).getPlayerRole().equals(Player.Role.WITCH)) {
                    usePoison();
                }
            } else {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
                }

                if (myId == Constants.SERVER_PLAYER_ID) {
                    gameActivity.setMediaPlayer(MediaPlayer.create(gameActivity.getApplicationContext(), R.raw.witch_sleeps));
                    gameActivity.getMediaPlayer().start();
                }

                gameActivity.outputMessage(R.string.message_witch_sleep);
                if (gameContext.getPlayerById(myId).getPlayerRole().equals(Player.Role.WITCH)) {
                    gameActivity.longOutputMessage("Close your eyes");
                }

                // give witch 5.5 secs to close eyes
                try {
                    Thread.sleep(5500);
                } catch (InterruptedException e) {
                    Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
                }
                sendDoneToServer();
            }
            //gameActivity.longOutputMessage("Die Hexe hat ihre Entscheidung getroffen und schlaeft wieder ein!");

        } else

        {
            sendDoneToServer();
        }

    }


    public void endWitchPoisonPhase() {
        Log.d(TAG, "Entering End of WitchPoisonPhase!");

        String poisonSetting = gameContext.getSetting(GameContext.Setting.WITCH_POISON);
        if (myId == Constants.SERVER_PLAYER_ID) {
            ServerGameController.HOST_IS_DONE = true;
            if (!TextUtils.isEmpty(poisonSetting)) {
                serverGameController.handleWitchResultPoison(Long.parseLong(poisonSetting));
            } else {
                serverGameController.handleWitchResultPoison(null);
            }
        } else {
            try {
                NetworkPackage<GameContext.Phase> np = new NetworkPackage<>(NetworkPackage.PACKAGE_TYPE.WITCH_RESULT_POISON);
                np.setOption(GameContext.Setting.WITCH_POISON.toString(), poisonSetting);
                websocketClientHandler.send(np);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    public void initiateSeerPhase() {
        Player roundVictim = getPlayerKilledByWerewolfesName();
        Player witchVictim = getPlayerKilledByWitchName();
        if (GameUtil.isSeerAlive()
                || (roundVictim != null && roundVictim.getPlayerRole() == Player.Role.SEER)
                || (witchVictim != null && witchVictim.getPlayerRole() == Player.Role.SEER)) {
            gameActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int time = Integer.parseInt(gameContext.getSetting(GameContext.Setting.TIME_SEER));
                    gameActivity.makeTimer(120).start();
                }
            });
            gameActivity.outputMessage(R.string.message_seer_awaken);
            if (myId == Constants.SERVER_PLAYER_ID && gameActivity.getMediaPlayer() != null) {
                //gameActivity.getMediaPlayer().stop();
                gameActivity.setMediaPlayer(MediaPlayer.create(gameActivity.getApplicationContext(), R.raw.seer_wakes));
                gameActivity.getMediaPlayer().start();
            }

            try {
                Thread.sleep(3750);
            } catch (InterruptedException e) {
                Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
            }


            if (gameContext.getPlayerById(myId).getPlayerRole().equals(Player.Role.SEER)) {
                gameActivity.showTextPopup("SeerPower", "Click on the Card of the Player, whose identity you want to know!");
                gameActivity.outputMessage("Choose a card!");
            } else {
                sendDoneToServer();
            }


            // delay GameThread if player clicks really fast
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
            }

        } else {
            //gameActivity.longOutputMessage("Es ist kein Seher im Spiel vorhanden.");
            if (myId == Constants.SERVER_PLAYER_ID && gameActivity.getMediaPlayer() != null) {
                //gameActivity.getMediaPlayer().stop();
                gameActivity.setMediaPlayer(MediaPlayer.create(gameActivity.getApplicationContext(), R.raw.seer_down));
                gameActivity.getMediaPlayer().start();
            }
            try {
                Thread.sleep(3500);
            } catch (InterruptedException e) {
                Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
            }
            sendDoneToServer();
        }
    }

    public void endSeerPhase() {
        Player roundVictim = getPlayerKilledByWerewolfesName();
        Player witchVictim = getPlayerKilledByWitchName();
        if (GameUtil.isSeerAlive()
                || (roundVictim != null && roundVictim.getPlayerRole() == Player.Role.SEER)
                || (witchVictim != null && witchVictim.getPlayerRole() == Player.Role.SEER)) {
            gameActivity.outputMessage(R.string.message_seer_sleep);
            if (myId == Constants.SERVER_PLAYER_ID && gameActivity.getMediaPlayer() != null) {
                //gameActivity.getMediaPlayer().stop();
                gameActivity.setMediaPlayer(MediaPlayer.create(gameActivity.getApplicationContext(), R.raw.seer_sleeps));
                gameActivity.getMediaPlayer().start();
            }
            if (gameContext.getPlayerById(myId).getPlayerRole().equals(Player.Role.SEER)) {
                gameActivity.longOutputMessage("Close your eyes!");
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
            }
        }
        sendDoneToServer();
    }

    public void initiateDayPhase() {
        final Player killedPlayer = GameContext.getInstance().getPlayerById(ContextUtil.lastKilledPlayerID);
        final Player killedByWitchPlayer = GameContext.getInstance().getPlayerById(ContextUtil.lastKilledPlayerIDByWitch);
        Player ownPlayer = GameContext.getInstance().getPlayerById(myId);

        // reset variables
        ContextUtil.lastKilledPlayerID = -1;
        ContextUtil.lastKilledPlayerIDByWitch = -1;

        gameActivity.outputMessage(R.string.message_villagers_awaken);
        //gameActivity.longOutputMessage("Es wird hell und alle Dorfbewohner erwachen aus ihrem tiefen Schlaf");

        Log.d(TAG, "Before day_wakes: " + Thread.currentThread().getName());
        if (myId == Constants.SERVER_PLAYER_ID && gameActivity.getMediaPlayer() != null) {

            gameActivity.setMediaPlayer(MediaPlayer.create(gameActivity.getApplicationContext(), R.raw.day_wakes));
            gameActivity.getMediaPlayer().start();

            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
            }
            Log.d(TAG, "Before night_claimed: " + Thread.currentThread().getName());
            gameActivity.setMediaPlayer(MediaPlayer.create(gameActivity.getApplicationContext(), R.raw.night_claimed));
            gameActivity.getMediaPlayer().start();

            try {
                Thread.sleep(2600);
            } catch (InterruptedException e) {
                Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
            }

            Log.d(TAG, "Before xy_died: " + Thread.currentThread().getName());
            if (killedPlayer == null && killedByWitchPlayer == null) {
                //gameActivity.longOutputMessage("Und...in dieser Nacht gestorben ist...niemand");
                gameActivity.setMediaPlayer(MediaPlayer.create(gameActivity.getApplicationContext(), R.raw.day_none_died));
                gameActivity.getMediaPlayer().start();
            } else if (killedPlayer != null && killedByWitchPlayer == null) {
                //gameActivity.longOutputMessage("Leider von uns gegangen ist: " + killedPlayer.getPlayerName());
                gameActivity.setMediaPlayer(MediaPlayer.create(gameActivity.getApplicationContext(), R.raw.day_one_died));
                gameActivity.getMediaPlayer().start();
            } else if (killedPlayer == null && killedByWitchPlayer != null) {
                //gameActivity.longOutputMessage("Leider von uns gegangen ist: " + killedByWitchPlayer.getPlayerName());
                gameActivity.setMediaPlayer(MediaPlayer.create(gameActivity.getApplicationContext(), R.raw.day_one_died));
                gameActivity.getMediaPlayer().start();
            } else if (killedPlayer != null && killedByWitchPlayer != null) {
                if (!(killedPlayer.getPlayerName().equals(killedByWitchPlayer.getPlayerName()))) {
                    gameActivity.setMediaPlayer(MediaPlayer.create(gameActivity.getApplicationContext(), R.raw.day_two_died));
                    gameActivity.getMediaPlayer().start();
                } else {
                    gameActivity.setMediaPlayer(MediaPlayer.create(gameActivity.getApplicationContext(), R.raw.day_one_died));
                    gameActivity.getMediaPlayer().start();
                }
            } else {
                Log.d(TAG, "initiateDayPhase(): Something went wrong here");
            }

        } else {
            Log.d(TAG, "NotHostThread1: " + Thread.currentThread().getName());
            try {
                Thread.sleep(9100);
            } catch (InterruptedException e) {
                Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
            }
            Log.d(TAG, "NotHostThread2: " + Thread.currentThread().getName());
        }

        Log.d(TAG, "EveryoneThread1: " + Thread.currentThread().getName());
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
        }
        Log.d(TAG, "EveryoneThread2: " + Thread.currentThread().getName());

        if (killedPlayer == null && killedByWitchPlayer == null) {
            gameActivity.showTextPopup("", "NOONE died this Night!");
        } else if (killedPlayer != null && killedByWitchPlayer == null) {
            gameActivity.showTextPopup("THE VICTIM", killedPlayer.getPlayerName() + " (" + gameActivity.getResources().getString(killedPlayer.getPlayerRole().getRole()) + ") was killed this Night!");
        } else if (killedPlayer == null && killedByWitchPlayer != null) {
            gameActivity.showTextPopup("THE VICTIM", killedByWitchPlayer.getPlayerName() + " (" + gameActivity.getResources().getString(killedByWitchPlayer.getPlayerRole().getRole()) + ") was killed this Night!");
        } else if (killedPlayer != null && killedByWitchPlayer != null) {
            if (!(killedPlayer.getPlayerName().equals(killedByWitchPlayer.getPlayerName()))) {
                Log.d(TAG, "Two died: random generated the num " + ContextUtil.RANDOM_INDEX);
                Player firstVictim = null;
                Player secondVictim = null;
                if (ContextUtil.RANDOM_INDEX == 0) {
                    firstVictim = killedPlayer;
                    secondVictim = killedByWitchPlayer;
                } else if (ContextUtil.RANDOM_INDEX == 1) {
                    firstVictim = killedByWitchPlayer;
                    secondVictim = killedPlayer;
                } else {
                    Log.d(TAG, "Something went wrong with random");
                    firstVictim = new Player("PLAYER_NOT_EXIST");
                    firstVictim.setPlayerRole(Player.Role.CITIZEN);
                    secondVictim = new Player("PLAYER_NOT_EXIST");
                    secondVictim.setPlayerRole(Player.Role.CITIZEN);
                }
                ContextUtil.RANDOM_INDEX = -1;
                gameActivity.showTextPopup("THE VICTIMS", "The players " + firstVictim.getPlayerName() + " (" + gameActivity.getResources().getString(firstVictim.getPlayerRole().getRole()) + ")"
                        + " and " + secondVictim.getPlayerName() + " (" + gameActivity.getResources().getString(secondVictim.getPlayerRole().getRole()) + ") were killed this Night!");
            } else {
                // players killed twice, only show once
                Log.d(TAG, "initiateDayPhase(): Somehow the same player got killed twice");
                gameActivity.showTextPopup("THE VICTIM", killedPlayer.getPlayerName() + " (" + gameActivity.getResources().getString(killedPlayer.getPlayerRole().getRole()) + ") was killed this Night!");
            }
        }

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
        }


        gameActivity.updateGamefield();
        if(!ownPlayer.isDead()) {
            gameActivity.longOutputMessage("Start discussion");
        }
        gameActivity.outputMessage(R.string.message_villagers_discuss);
        gameActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int time = Integer.parseInt(gameContext.getSetting(GameContext.Setting.TIME_VILLAGER));
                gameActivity.makeTimer(time).start();
            }
        });


    }

    public void initiateDayVotingPhase() {
        Player ownPlayer = GameContext.getInstance().getPlayerById(myId);
        if(!ownPlayer.isDead()) {
            gameActivity.longOutputMessage("Prepare to vote");
        }
        gameActivity.outputMessage(R.string.message_villagers_vote);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
        }


        if (!ownPlayer.isDead()) {
            gameActivity.openVoting();
        }
    }

    public void endDayPhase() {
        Player killedPlayer = GameContext.getInstance().getPlayerById(ContextUtil.lastKilledPlayerID);
        //gameActivity.longOutputMessage("Die Abstimmung ist beendet...");
        //gameActivity.longOutputMessage(R.string.votingResult_day_text, killedPlayer.getPlayerName());
        if (killedPlayer != null) {
            gameActivity.showTextPopup(R.string.votingResult_day_title, R.string.votingResult_day_text, killedPlayer.getPlayerName());
        } else {
            Log.d(TAG, "Something went wrong while voting in Day Phase");
        }
        // reset variable
        ContextUtil.lastKilledPlayerID = -1;


        gameActivity.updateGamefield();
        gameActivity.outputMessage(R.string.message_day_over);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
        }


        sendDoneToServer();


    }

    public void useElixir() {
        if (gameContext.getSetting(GameContext.Setting.WITCH_ELIXIR) == null) {
            gameActivity.askWitchForElixir();
        } else {
            sendDoneToServer();
        }
    }

    public void usePoison() {
        if (gameContext.getSetting(GameContext.Setting.WITCH_POISON) == null) {
            gameActivity.askWitchForPoison();
        } else {
            sendDoneToServer();
        }

    }

    public void usedElixir() {

        String id = GameContext.getInstance().getSetting(GameContext.Setting.KILLED_BY_WEREWOLF);
        gameContext.setSetting(GameContext.Setting.WITCH_ELIXIR, id);

    }


    /**
     * Method gets called if the witch presses a player card button
     * If the witch has the power to use one then the setting is set in the game context
     *
     * @param selectedPlayer the Player the potion is used on
     */
    public void selectedPlayerForWitch(Player selectedPlayer) {
        getGameActivity().showTextPopup("WitchPoison", "You poisoned " + selectedPlayer.getPlayerName());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Log.e(TAG, "D/THREAD_Problem: " + e.getMessage());
        }
        gameActivity.longOutputMessage("Close your eyes!");

        // could be redundant
        String id = String.valueOf(selectedPlayer.getPlayerId());
        gameContext.setSetting(GameContext.Setting.WITCH_POISON, id);

        endWitchPoisonPhase();
    }


    public void sendVotingResult(Player player) {

        if (player != null) {
            // host
            if (myId == Constants.SERVER_PLAYER_ID) {
                //ServerGameController.HOST_IS_DONE = true;
                serverGameController.handleVotingResult(player.getPlayerName());
            } else {
                try {
                    NetworkPackage<String> np = new NetworkPackage<>(NetworkPackage.PACKAGE_TYPE.VOTING_RESULT);
                    np.setPayload(player.getPlayerName());
                    websocketClientHandler.send(np);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (myId == Constants.SERVER_PLAYER_ID) {
                //ServerGameController.HOST_IS_DONE = true;
                serverGameController.handleVotingResult(EMPTY_VOTING_PLAYER);
            } else {
                try {
                    NetworkPackage<String> np = new NetworkPackage<String>(NetworkPackage.PACKAGE_TYPE.VOTING_RESULT);
                    np.setPayload("");
                    websocketClientHandler.send(np);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }


    public void handleVotingResult(String playerName) {

        if (!TextUtils.isEmpty(playerName)) {
            Log.d(TAG, "voting_result received. Kill this guy: " + playerName);
            Player playerToKill = GameContext.getInstance().getPlayerByName(playerName);
            Log.d(TAG, "Player " + getMyPlayer() + " successfully deleted " + playerToKill.getPlayerName() + " after Voting");
            playerToKill.setDead(true);
            ContextUtil.lastKilledPlayerID = playerToKill.getPlayerId();
            // TODO: nur für Werwolfvoting, nicht für DayVoting
            gameContext.setSetting(GameContext.Setting.KILLED_BY_WEREWOLF, String.valueOf(playerToKill.getPlayerId()));
        }

        sendDoneToServer();
    }

    public void handleWitchPoisonResult(String playerName) {
        gameActivity.outputMessage(R.string.message_witch_sleep);
        if (!TextUtils.isEmpty(playerName)) {
            Player playerToKill = GameContext.getInstance().getPlayerByName(playerName);
            playerToKill.setDead(true);
            ContextUtil.lastKilledPlayerIDByWitch = playerToKill.getPlayerId();
            gameContext.setSetting(GameContext.Setting.WITCH_POISON, String.valueOf(playerToKill.getPlayerId()));
        }

        sendDoneToServer();
    }

    public void handleWitchElixirResult(String playerName) {
        if (!TextUtils.isEmpty(playerName)) {
            Player playerToSave = GameContext.getInstance().getPlayerByName(playerName);
            playerToSave.setDead(false);
            ContextUtil.lastKilledPlayerID = -1;
            gameContext.setSetting(GameContext.Setting.WITCH_ELIXIR, "used");
        }


        // if not the host
        sendDoneToServer();
    }


    public void connect(String url, String playerName) {
        websocketClientHandler.startClient(url, playerName);
    }

    /**
     * Returns the player who got killed in the current round
     *
     * @return the player object which got killed
     */
    public Player getPlayerKilledByWerewolfesName() {
        //Long id = Long.getLong(gameContext.getSetting(GameContext.Setting.KILLED_BY_WEREWOLF));
        String id = gameContext.getSetting(GameContext.Setting.KILLED_BY_WEREWOLF);
        if (!TextUtils.isEmpty(id)) {
            Log.d(TAG, "Werewolves killed: " + gameContext.getPlayerById(Long.parseLong(id)).getPlayerName());
            return gameContext.getPlayerById(Long.parseLong(id));
        } else {
            Log.d(TAG, "Werewolves killed no one this round");
            return null;
        }
    }

    public Player getPlayerKilledByWitchName() {
        Long id = ContextUtil.lastKilledPlayerIDByWitch;
        if (id != -1) {
            Log.d(TAG, "Witch killed: " + gameContext.getPlayerById(id).getPlayerName());
            return gameContext.getPlayerById(id);
        } else {
            Log.d(TAG, "Witch killed no one this round");
            return null;
        }
    }

    public void sendDoneToServer() {
        // if not the host
        if (myId != 0) {
            try {
                NetworkPackage<GameContext.Phase> np = new NetworkPackage<GameContext.Phase>(NetworkPackage.PACKAGE_TYPE.DONE);
                //np.setPayload(GameContext.Phase.PHASE_WITCH);
                websocketClientHandler.send(np);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (myId == 0) {
            Log.d(TAG, "Host is now done!");
            ServerGameController.HOST_IS_DONE = true;
            // startNextPhase when all Clients are ready as well
            if (ServerGameController.CLIENTS_ARE_DONE) {
                serverGameController.startNextPhase();
            }
        }
    }

    public GameActivity getGameActivity() {
        return gameActivity;
    }

    public void setGameActivity(GameActivity gameActivity) {
        this.gameActivity = gameActivity;
    }

    public StartClientActivity getStartClientActivity() {
        return startClientActivity;
    }

    public void setStartClientActivity(StartClientActivity startClientActivity) {
        this.startClientActivity = startClientActivity;
    }

    public WebsocketClientHandler getWebsocketClientHandler() {
        return websocketClientHandler;
    }

    public void setWebsocketClientHandler(WebsocketClientHandler websocketClientHandler) {
        this.websocketClientHandler = websocketClientHandler;
    }

    public GameContext getGameContext() {
        return gameContext;
    }

    public Player getMyPlayer() {
        return gameContext.getPlayerById(myId);
    }

    public void setMe(Player me) {
        this.me = me;
    }

    public long getMyPlayerId() {
        return myId;
    }

    public void setMyId(long myId) {
        this.myId = myId;
    }

    public void updateMe() {
        this.me = gameContext.getPlayerById(this.myId);
        Log.d(TAG, "Me is now: " + me.getPlayerName() + "  isDead?: " + me.isDead());
    }

    public void setPhase(GameContext.Phase phase) {
        gameContext.setCurrentPhase(phase);
    }

    public void setServerGameController() {
        serverGameController = ServerGameController.getInstance();
    }

    public List<Player> getPlayerList() {
        return gameContext.getPlayersList();
    }

    public void showSuccesfulConnection() {
        if (myId != 0) {
            this.startClientActivity.showConnected();
        }
    }

    public void abortGame() {
        destroy();

        // go back to start screen
        Intent intent = new Intent(gameActivity, MainActivity.class);
        gameActivity.startActivity(intent);
    }

    /**
     * Destroy all game data and reset to 0.
     * After this you are able to start a new game without any old data
     */
    public void destroy() {

        gameContext.destroy();
        websocketClientHandler.destroy();
        if (serverGameController != null) {
            serverGameController.destroy();
        }
        me = new Player();
        System.gc();
    }
}
