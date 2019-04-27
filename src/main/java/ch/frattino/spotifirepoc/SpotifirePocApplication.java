package ch.frattino.spotifirepoc;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SpotifirePocApplication implements CommandLineRunner {

	private static Logger LOG = LoggerFactory.getLogger(SpotifirePocApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpotifirePocApplication.class, args);
	}

	@Override
	public void run(String... args) {
		String uri = SpotifyWrapper.getAuthorizationCodeUri();
		LOG.info("Visit that URI: {}", uri);
	}

	@RequestMapping(value = "/")
	public String code(@RequestParam("code") String authorizationCode) {
		LOG.info("Authorization code: {}", authorizationCode);

		// We grab the access token and refresh tokens
		SpotifyApi api = SpotifyWrapper.getApi();
		AuthorizationCodeRequest authorizationCodeRequest = api.authorizationCode(authorizationCode).build();
		AuthorizationCodeCredentials authorizationCodeCredentials = null;
		try {
			authorizationCodeCredentials = authorizationCodeRequest.execute();
		} catch (Exception e) {
			LOG.error("Error while grabbing access and refresh tokens", e);
			return e.getMessage();
		}
		String accessToken = authorizationCodeCredentials.getAccessToken();
		String refreshToken = authorizationCodeCredentials.getRefreshToken();
		api.setAccessToken(accessToken);
		api.setRefreshToken(refreshToken);
		LOG.info("Access token: {}", accessToken);
		LOG.info("Refresh token: {}", refreshToken);
		return "Authorization code: " + authorizationCode + "<br/>Access token: " + accessToken + "<br/>Refresh token: " + refreshToken;
	}
}
