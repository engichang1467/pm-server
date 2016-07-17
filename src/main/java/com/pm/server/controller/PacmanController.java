package com.pm.server.controller;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.pm.server.datatype.Coordinate;
import com.pm.server.datatype.CoordinateImpl;
import com.pm.server.datatype.PlayerState;
import com.pm.server.exceptionhttp.BadRequestException;
import com.pm.server.exceptionhttp.ConflictException;
import com.pm.server.exceptionhttp.InternalServerErrorException;
import com.pm.server.exceptionhttp.NotFoundException;
import com.pm.server.player.Pacman;
import com.pm.server.player.PacmanImpl;
import com.pm.server.repository.PacmanRepository;
import com.pm.server.request.PlayerStateRequest;
import com.pm.server.response.LocationResponse;
import com.pm.server.response.PlayerStateResponse;
import com.pm.server.utils.JsonUtils;
import com.pm.server.utils.ValidationUtils;

@RestController
@RequestMapping("/pacman")
public class PacmanController {

	@Autowired
	private PacmanRepository pacmanRepository;

	private final static Logger log =
			LogManager.getLogger(PacmanController.class.getName());

	@RequestMapping(
			value = "",
			method = RequestMethod.POST
	)
	@ResponseStatus(value = HttpStatus.OK)
	public void createPacman(
			@RequestBody CoordinateImpl location)
			throws
				BadRequestException,
				ConflictException,
				InternalServerErrorException {

		log.debug("Mapped POST /pacman");
		log.debug("Request body: {}", JsonUtils.objectToJson(location));

		ValidationUtils.validateRequestBodyWithLocation(location);

		if(pacmanRepository.getPlayer() != null) {
			String errorMessage = "A Pacman already exists.";
			log.warn(errorMessage);
			throw new ConflictException(errorMessage);
		}

		Pacman pacman = new PacmanImpl();
		pacman.setLocation(location);

		try {
			pacmanRepository.addPlayer(pacman);
		}
		catch(Exception e) {
			log.error(e.getMessage());
			throw new InternalServerErrorException(e.getMessage());
		}

	}

	@RequestMapping(
			value="/location",
			method=RequestMethod.GET
	)
	@ResponseStatus(value = HttpStatus.OK)
	public LocationResponse getPacmanLocation(
			HttpServletResponse response)
			throws NotFoundException, InternalServerErrorException {

		log.debug("Mapped GET /pacman/location");

		Pacman pacman = pacmanRepository.getPlayer();
		if(pacman == null) {
			String errorMessage = "No Pacman exists";
			log.warn(errorMessage);
			throw new NotFoundException(errorMessage);
		}

		Coordinate coordinate = pacman.getLocation();
		if(coordinate == null) {
			String errorMessage =
					"The location of the Pacman could not be extracted.";
			log.error(errorMessage);
			throw new InternalServerErrorException(errorMessage);
		}

		LocationResponse locationResponse = new LocationResponse();
		locationResponse.setLatitude(coordinate.getLatitude());
		locationResponse.setLongitude(coordinate.getLongitude());

		return locationResponse;

	}

	@RequestMapping(
			value="/state",
			method=RequestMethod.GET,
			produces={ "application/json" }
	)
	@ResponseStatus(value = HttpStatus.OK)
	public PlayerStateResponse getPacmanState(HttpServletResponse response)
			throws NotFoundException {

		log.debug("Mapped GET /pacman/state");

		Pacman pacman = pacmanRepository.getPlayer();
		if(pacman == null) {
			String errorMessage = "No Pacman exists";
			log.warn(errorMessage);
			throw new NotFoundException(errorMessage);
		}

		PlayerStateResponse playerStateResponse = new PlayerStateResponse();
		playerStateResponse.setState(pacman.getState());

		String objectString = JsonUtils.objectToJson(playerStateResponse);
		if(objectString != null) {
			log.debug("Returning playerStateResponse: {}", objectString);
		}

		return playerStateResponse;
	}

	@RequestMapping(
			value="/location",
			method=RequestMethod.PUT
	)
	@ResponseStatus(value = HttpStatus.OK)
	public void setPacmanLocation(
			@RequestBody CoordinateImpl location)
			throws BadRequestException, NotFoundException {

		log.debug("Mapped PUT /pacman/location");
		log.debug("Request body: {}", JsonUtils.objectToJson(location));

		ValidationUtils.validateRequestBodyWithLocation(location);

		Pacman pacman = pacmanRepository.getPlayer();
		if(pacman == null) {
			String errorMessage = "No Pacman exists.";
			log.warn(errorMessage);
			throw new NotFoundException(errorMessage);
		}

		log.debug(
				"Setting Pacman at location ({}, {}) to location ({}, {})",
				pacman.getLocation().getLatitude(),
				pacman.getLocation().getLongitude(),
				location.getLatitude(), location.getLongitude()
		);
		pacman.setLocation(location);
	}

	@RequestMapping(
			value="/state",
			method=RequestMethod.PUT
	)
	@ResponseStatus(value = HttpStatus.OK)
	public void setPacmanState(
			@RequestBody PlayerStateRequest stateRequest)
			throws BadRequestException, NotFoundException {

		log.debug("Mapped PUT /pacman/state");
		log.debug("Request body: {}", JsonUtils.objectToJson(stateRequest));

		PlayerState state =
				ValidationUtils.validateRequestBodyWithState(stateRequest);

		Pacman pacman = pacmanRepository.getPlayer();
		if(pacman == null) {
			String errorMessage = "No Pacman exists.";
			log.warn(errorMessage);
			throw new NotFoundException(errorMessage);
		}

		log.debug(
				"Changing Pacman from state {} to {}",
				pacman.getState(), state
		);
		pacmanRepository.setPlayerState(state);

	}

	@RequestMapping(
			method=RequestMethod.DELETE
	)
	@ResponseStatus(value = HttpStatus.OK)
	public void deletePacman(
			HttpServletResponse response)
			throws NotFoundException {

		log.debug("Mapped DELETE /pacman");

		if(pacmanRepository.getPlayer() == null) {
			String errorMessage = "No Pacman exists.";
			log.warn(errorMessage);
			throw new NotFoundException(errorMessage);
		}

		pacmanRepository.clearPlayers();
	}

}
