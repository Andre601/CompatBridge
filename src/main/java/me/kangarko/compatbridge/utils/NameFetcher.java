package me.kangarko.compatbridge.utils;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import lombok.RequiredArgsConstructor;

/**
 * Utility class for connecting to Mojang servers to get the players name from a given UUID
 */
@RequiredArgsConstructor
public class NameFetcher implements Callable<String> {

	/**
	 * The URL to connect to
	 */
	private static final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

	/**
	 * The JSON parser library
	 */
	private final JSONParser jsonParser = new JSONParser();

	/**
	 * The UUID to convert to name
	 */
	private final UUID uuid;

	/**
	 * Attempts to connect to Mojangs servers to retrieve the current player username from his unique id
	 *
	 * Runs on the main thread
	 */
	@Override
	public String call() throws Exception {

		final HttpURLConnection connection = (HttpURLConnection) new URL(PROFILE_URL + uuid.toString().replace("-", "")).openConnection();
		final JSONObject response = (JSONObject) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
		final String name = (String) response.get("name");

		if (name == null)
			return "";

		final String cause = (String) response.get("cause");
		final String errorMessage = (String) response.get("errorMessage");

		if (cause != null && cause.length() > 0)
			throw new IllegalStateException(errorMessage);

		return name;
	}
}
