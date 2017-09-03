package com.pm.server.datatype;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Coordinate {

	private Double latitude;
	private Double longitude;

	private final static Logger log =
			LogManager.getLogger(Coordinate.class.getName());

	public Coordinate() {
		log.trace(
				"Creating coordinate with latitude and longitude 0.0."
		);
		reset();
	}

	public Coordinate(Double latitude, Double longitude) {

		log.trace(
				"Creating coordinate with latitude {} and longitude {}",
				latitude, longitude
		);

		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Coordinate(Coordinate coordinate) {

		log.trace(
				"Creating coordinate with latitude {} and longitude {}",
				coordinate.getLatitude(), coordinate.getLongitude()
		);

		this.latitude = coordinate.getLatitude();
		this.longitude = coordinate.getLongitude();
	}

	public void reset() {
		latitude = 0.0;
		longitude = 0.0;
	}

	@Override
	public boolean equals(Object object) {

		if(object == null) {
			return false;
		}

		if(!Coordinate.class.isAssignableFrom(object.getClass())) {
			return false;
		}
		final Coordinate coordinateCompare = (Coordinate) object;
		if(!this.latitude.equals(coordinateCompare.latitude)) {
			return false;
		}
		else if(!this.longitude.equals(coordinateCompare.longitude)) {
			return false;
		}

		return true;

	}

	public void setLatitude(Double latitude) {

		log.trace("Setting latitude to {}", latitude);

		this.latitude = latitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLongitude(Double longitude) {

		log.trace("Setting longitude to {}", longitude);

		this.longitude = longitude;
	}

	public Double getLongitude() {
		return longitude;
	}

}
