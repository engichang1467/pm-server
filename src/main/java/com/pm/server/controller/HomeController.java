package com.pm.server.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HomeController implements Controller {

	private final static Logger log =
			LogManager.getLogger(HomeController.class.getName());

	@RequestMapping(
			value="/",
			method=RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	public String home() {

		log.debug("Mapped GET /");

		return "Welcome to the Pac Macro server!";
	}

}
