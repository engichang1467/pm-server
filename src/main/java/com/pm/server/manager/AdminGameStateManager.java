package com.pm.server.manager;

import com.pm.server.PmServerException;
import com.pm.server.datatype.GameState;
import com.pm.server.datatype.Player;
import com.pm.server.registry.GameStateRegistry;
import com.pm.server.registry.PacdotRegistry;
import com.pm.server.registry.PlayerRegistry;
import com.pm.server.registry.TagRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AdminGameStateManager {

    private PlayerRegistry playerRegistry;

    private PacdotRegistry pacdotRegistry;

    private GameStateRegistry gameStateRegistry;

    private TagRegistry tagRegistry;

    @Autowired
    public AdminGameStateManager(
            PlayerRegistry playerRegistry,
            PacdotRegistry pacdotRegistry,
            GameStateRegistry gameStateRegistry,
            TagRegistry tagRegistry) {
        this.playerRegistry = playerRegistry;
        this.pacdotRegistry = pacdotRegistry;
        this.gameStateRegistry = gameStateRegistry;
        this.tagRegistry = tagRegistry;
    }

    public void changeGameState(GameState newState) throws PmServerException {

        try {
            switch(newState) {

                case INITIALIZING:
                    gameStateRegistry.resetGame();
                    playerRegistry.reset();
                    pacdotRegistry.resetPacdots();
                    tagRegistry.clearTags();
                    break;

                case IN_PROGRESS:
                    gameStateRegistry.startGame();
                    playerRegistry.startFromReady();
                    break;

                case PAUSED:
                    gameStateRegistry.pauseGame();
                    break;

                case FINISHED_PACMAN_WIN:
                    gameStateRegistry.setWinnerPacman();
                    break;

                case FINISHED_GHOSTS_WIN:
                    gameStateRegistry.setWinnerGhosts();
                    playerRegistry.setPlayerStateByName(
                            Player.Name.Pacman, Player.State.CAPTURED
                    );
                    break;

            }
        }
        catch(IllegalStateException e) {
            throw new PmServerException(HttpStatus.CONFLICT, e.getMessage());
        }

    }

}
