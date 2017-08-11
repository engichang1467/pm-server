package com.pm.server.controller;

import com.pm.server.PmServerException;
import com.pm.server.datatype.Coordinate;
import com.pm.server.datatype.Player;
import com.pm.server.registry.PlayerRegistry;
import com.pm.server.request.LocationRequest;
import com.pm.server.request.StateRequest;
import com.pm.server.response.*;
import com.pm.server.utils.JsonUtils;
import com.pm.server.utils.ValidationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/player")
public class PlayerController {

	@Autowired
	private PlayerRegistry playerRegistry;

	private final static Logger log =
			LogManager.getLogger(PlayerController.class.getName());

	@RequestMapping(
			value = "/{playerName}",
			method=RequestMethod.POST,
			produces={ "application/json" }
	)
	@SuppressWarnings("rawtypes")
	public ResponseEntity selectPlayer(
			@PathVariable String playerName,
			@RequestBody(required = false) LocationRequest requestBody
			) throws PmServerException {

		log.info("Mapped POST /player/{}", playerName);
		log.info("Request body: {}", JsonUtils.objectToJson(requestBody));

		Player.Name name = ValidationUtils.validateRequestWithName(playerName);

		Coordinate location = ValidationUtils
				.validateRequestBodyWithLocation(requestBody);

		log.info("Attempting to select Player {} at ({}, {}).",
				name,
				location.getLatitude(),
				location.getLongitude()
		);

		Player player = playerRegistry.getPlayerByName(name);
		if(player == null) {
			String errorMessage = name + " is not a valid Player name.";
			log.warn(errorMessage);
			throw new PmServerException(HttpStatus.BAD_REQUEST, errorMessage);
		}
		else if(player.getState() != Player.State.UNINITIALIZED) {
			String errorMessage =
					"Player "+
					name +
					" has already been selected.";
			log.warn(errorMessage);
			throw new PmServerException(HttpStatus.CONFLICT, errorMessage);
		}

		playerRegistry.setPlayerLocationByName(name, location);
		playerRegistry.setPlayerStateByName(name, Player.State.READY);

		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@RequestMapping(
			value="/{playerName}",
			method=RequestMethod.DELETE
	)
	@SuppressWarnings("rawtypes")
	public ResponseEntity deselectPlayer(
			@PathVariable String playerName,
			HttpServletResponse response)
			throws PmServerException {

		log.info("Mapped DELETE /player/{}", playerName);

		Player.Name name = ValidationUtils.validateRequestWithName(playerName);

		Player player = playerRegistry.getPlayerByName(name);
		if(player == null) {
			String errorMessage =
					"Player " +
					name +
					" was not found.";
			log.warn(errorMessage);
			throw new PmServerException(HttpStatus.NOT_FOUND, errorMessage);
		}
		else if(player.getState() == Player.State.UNINITIALIZED) {
			String errorMessage =
					"Player "+
					name +
					" has not yet been selected.";
			log.warn(errorMessage);
			throw new PmServerException(HttpStatus.BAD_REQUEST, errorMessage);
		}

		try {
			playerRegistry.setPlayerStateByName(
					name, Player.State.UNINITIALIZED
			);
		}
		catch(Exception e) {
			String errorMessage =
					"Player " +
					name +
					" was found but could not be deselected.";
			log.warn(errorMessage);
			throw new PmServerException(
					HttpStatus.INTERNAL_SERVER_ERROR, errorMessage
			);
		}
		log.info("Player {} was succesfully deselected", name);

		log.debug("Setting Player {} to default location", name);
		player.resetLocation();

		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@RequestMapping(
			value="/{playerName}/location",
			method=RequestMethod.GET,
			produces={ "application/json" }
	)
	public ResponseEntity<LocationResponse> getPlayerLocation(
			@PathVariable String playerName,
			HttpServletResponse response)
			throws PmServerException {

		log.info("Mapped GET /player/{}/location", playerName);

		Player.Name name = ValidationUtils.validateRequestWithName(playerName);

		Player player = playerRegistry.getPlayerByName(name);
		if(player == null) {
			String errorMessage =
					"No Player named " +
					name +
					" was found in the registry.";
			log.debug(errorMessage);
			throw new PmServerException(HttpStatus.NOT_FOUND, errorMessage);
		}

		LocationResponse locationResponse = new LocationResponse();
		locationResponse.setLatitude(player.getLocation().getLatitude());
		locationResponse.setLongitude(player.getLocation().getLongitude());

		String objectString = JsonUtils.objectToJson(locationResponse);
		if(objectString != null) {
			log.debug("Returning locationResponse: {}", objectString);
		}

		return ResponseEntity.status(HttpStatus.OK).body(locationResponse);
	}

	@RequestMapping(
			value="/locations",
			method=RequestMethod.GET,
			produces={ "application/json" }
	)
	public ResponseEntity<List<PlayerNameAndLocationResponse>>
			getAllPlayerLocations() {

		log.info("Mapped GET /player/locations");

		List<PlayerNameAndLocationResponse> playerResponseList = new ArrayList<PlayerNameAndLocationResponse>();

		List<Player> playerList = playerRegistry.getAllPlayers();

		if(playerList != null) {
			for(Player player : playerList) {

				String objectString = JsonUtils.objectToJson(player);
				if(objectString != null) {
					log.trace("Processing Player: {}", objectString);
				}

				PlayerNameAndLocationResponse playerResponse =
						new PlayerNameAndLocationResponse();
				playerResponse.setName(player.getName());
				playerResponse.setLocation(player.getLocation());

				playerResponseList.add(playerResponse);
			}
		}

		String objectString = JsonUtils.objectToJson(playerResponseList);
		if(objectString != null) {
			log.debug("Returning Player response list: {}", objectString);
		}

		return ResponseEntity.status(HttpStatus.OK).body(playerResponseList);
	}

	@RequestMapping(
			value="/{playerName}/state",
			method=RequestMethod.GET,
			produces={ "application/json" }
	)
	public ResponseEntity<PlayerStateResponse> getPlayerState(
			@PathVariable String playerName,
			HttpServletResponse response)
			throws PmServerException {

		log.info("Mapped GET /player/{}/state", playerName);

		Player.Name name = ValidationUtils.validateRequestWithName(playerName);

		Player player = playerRegistry.getPlayerByName(name);
		if(player == null) {
			String errorMessage =
					"No Player named " +
					name +
					" was found in the registry.";
			log.debug(errorMessage);
			throw new PmServerException(HttpStatus.NOT_FOUND, errorMessage);
		}

		PlayerStateResponse playerStateResponse = new PlayerStateResponse();
		playerStateResponse.setState(player.getState());

		String objectString = JsonUtils.objectToJson(playerStateResponse);
		if(objectString != null) {
			log.info(
					"Returning Player " + playerName +
					" with state {}", objectString
			);
		}

		return ResponseEntity.status(HttpStatus.OK).body(playerStateResponse);
	}

	@RequestMapping(
			value="/states",
			method=RequestMethod.GET,
			produces={ "application/json" }
	)
	public ResponseEntity<List<PlayerNameAndPlayerStateResponse>>
			getAllPlayerStates() {

		log.info("Mapped GET /player/states");

		List<PlayerNameAndPlayerStateResponse> playerResponseList =
				new ArrayList<PlayerNameAndPlayerStateResponse>();

		List<Player> players = playerRegistry.getAllPlayers();

		if(players != null) {
			for(Player player : players) {

				String objectString = JsonUtils.objectToJson(player);
				if(objectString != null) {
					log.trace("Processing Player: {}", objectString);
				}

				PlayerNameAndPlayerStateResponse playerResponse = new PlayerNameAndPlayerStateResponse();
				playerResponse.name = player.getName();
				playerResponse.state = player.getState();

				playerResponseList.add(playerResponse);
			}
		}

		String objectString = JsonUtils.objectToJson(playerResponseList);
		if(objectString != null) {
			log.debug("Returning Player states: {}", objectString);
		}

		return ResponseEntity.status(HttpStatus.OK).body(playerResponseList);
	}

	@RequestMapping(
			value="/details",
			method=RequestMethod.GET,
			produces={ "application/json" }
	)
	public ResponseEntity<List<PlayerDetailsResponse>>
			getAllPlayerDetails() {

		log.info("Mapped GET /player/details");

		List<PlayerDetailsResponse> playerResponseList =
				new ArrayList<PlayerDetailsResponse>();

		List<Player> players = playerRegistry.getAllPlayers();

		if(players != null) {
			for(Player player : players) {

				String objectString = JsonUtils.objectToJson(player);
				if(objectString != null) {
					log.trace("Processing Player: {}", objectString);
				}

				PlayerDetailsResponse playerResponse = new PlayerDetailsResponse();
				playerResponse.setName(player.getName());
				playerResponse.setState(player.getState());
				playerResponse.setLocation(player.getLocation());

				playerResponseList.add(playerResponse);
			}
		}

		String objectString = JsonUtils.objectToJson(playerResponseList);
		if(objectString != null) {
			log.debug("Returning player details: {}", objectString);
		}

		return ResponseEntity.status(HttpStatus.OK).body(playerResponseList);
	}

	@RequestMapping(
			value="/{playerName}/location",
			method=RequestMethod.PUT
	)
	@SuppressWarnings("rawtypes")
	public ResponseEntity setPlayerLocation(
			@PathVariable String playerName,
			@RequestBody LocationRequest locationRequest)
			throws PmServerException {

		log.info("Mapped PUT /player/{}/location", playerName);
		log.info("Request body: {}", JsonUtils.objectToJson(locationRequest));

		Player.Name name = ValidationUtils.validateRequestWithName(playerName);

		Coordinate location = ValidationUtils
				.validateRequestBodyWithLocation(locationRequest);

		Player player = playerRegistry.getPlayerByName(name);
		if(player == null) {
			String errorMessage =
					"Player " +
					name +
					" was not found.";
			log.debug(errorMessage);
			throw new PmServerException(HttpStatus.NOT_FOUND, errorMessage);
		}
		else if(player.getState() == Player.State.UNINITIALIZED) {
			String errorMessage =
					"Player " +
			        name +
			        " has not been selected yet, so a location cannot be set.";
			log.warn(errorMessage);
			throw new PmServerException(HttpStatus.CONFLICT, errorMessage);
		}

		log.info(
				"Setting Player {} to ({}, {})",
				name, location.getLatitude(), location.getLongitude()
		);
		playerRegistry.setPlayerLocationByName(name, location);

		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@RequestMapping(
			value="/{playerName}/state",
			method=RequestMethod.PUT
	)
	@SuppressWarnings("rawtypes")
	public ResponseEntity setPlayerState(
			@PathVariable String playerName,
			@RequestBody StateRequest stateRequest)
			throws PmServerException {

		log.info("Mapped PUT /player/{}/state", playerName);
		log.info("Request body: {}", JsonUtils.objectToJson(stateRequest));

		Player.Name name = ValidationUtils.validateRequestWithName(playerName);

		Player.State state =
				ValidationUtils.validateRequestBodyWithState(stateRequest);

		Player player = playerRegistry.getPlayerByName(name);
		if(player == null) {
			String errorMessage =
					"Player " +
					name +
					" was not found.";
			log.warn(errorMessage);
			throw new PmServerException(HttpStatus.NOT_FOUND, errorMessage);
		}

		// Illegal state changes
		if(player.getState() == Player.State.UNINITIALIZED &&
				player.getState() != state) {
			String errorMessage =
					"This operation cannot change the state of an unselected/" +
					"uninitialized player; use POST /player/{playerName} " +
					"instead.";
			log.warn(errorMessage);
			throw new PmServerException(HttpStatus.CONFLICT, errorMessage);
		}
		else if(state == Player.State.UNINITIALIZED &&
				state != player.getState()) {
			String errorMessage =
					"This operation cannot change the state of a selected/" +
					"initialized player to uninitialized; use " +
					"DELETE /player/{playerName} instead.";
			log.warn(errorMessage);
			throw new PmServerException(HttpStatus.CONFLICT, errorMessage);
		}

		// Illegal player states
		if(player.getName() != Player.Name.Pacman &&
				state == Player.State.POWERUP) {
			String errorMessage = "The POWERUP state is not valid for a Ghost.";
			log.warn(errorMessage);
			throw new PmServerException(HttpStatus.CONFLICT, errorMessage);
		}

		log.info(
				"Changing Player {} from state {} to {}",
				name, player.getState(), state
		);
		playerRegistry.setPlayerStateByName(name, state);

		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

}