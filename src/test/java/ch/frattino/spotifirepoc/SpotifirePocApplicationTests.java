package ch.frattino.spotifirepoc;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.albums.GetAlbumRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchAlbumsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import org.apache.juneau.json.JsonSerializer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpotifirePocApplicationTests {

	private static Logger LOG = LoggerFactory.getLogger(SpotifirePocApplicationTests.class);

	private static SpotifyApi spotifyApi;

	@BeforeClass
	public static void setup() throws URISyntaxException {

		String clientId = readEnvOrFail("SPOTIFY_CLIENT_ID");
		String clientSecret = readEnvOrFail("SPOTIFY_CLIENT_SECRET");

		// Wet set up the API
		spotifyApi = new SpotifyApi.Builder()
				.setClientId(clientId)
				.setClientSecret(clientSecret)
				.setAccessToken("tagol")
				.setRedirectUri(new URI("http://localhost"))
				.build();

		// We grab a "guest access token"
		ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
		ClientCredentials clientCredentials = null;
		try {
			clientCredentials = clientCredentialsRequest.execute();
		} catch (IOException | SpotifyWebApiException e) {
			LOG.error("Couldn't get a guest access token", e);
			Assert.fail("Couldn't get a guest access token");
			return;
		}
		spotifyApi.setAccessToken(clientCredentials.getAccessToken());
	}

	private static String readEnvOrFail(String key) {
		String value = System.getenv(key);
		if (value == null) {
			Assert.fail("You forgot to set the " + key + " environment variable!");
		}
		return value;
	}

	@Test
	public void searchForTrack() {
		SearchTracksRequest request = spotifyApi.searchTracks("Lost in the Twilight Hall").build();
		try {
			Paging<Track> paging = request.execute();
			JsonSerializer jsonSerializer = JsonSerializer.DEFAULT_READABLE;
			for (Track track : paging.getItems()) {
				LOG.info(jsonSerializer.serialize(track));
			}
		} catch (Exception e) {
			LOG.error("Error", e);
		}
	}

	@Test
	public void searchForAlbum() {
		SearchAlbumsRequest request = spotifyApi.searchAlbums("blind guardian a night at the opera").build();
		try {
			Paging<AlbumSimplified> paging = request.execute();
			JsonSerializer jsonSerializer = JsonSerializer.DEFAULT_READABLE;
			for (AlbumSimplified item : paging.getItems()) {
				GetAlbumRequest getAlbumRequest = spotifyApi.getAlbum(item.getId()).build();
				Album album = getAlbumRequest.execute();
				LOG.info(jsonSerializer.serialize(album));
			}
		} catch (Exception e) {
			LOG.error("Error", e);
		}
	}

	@Test
	public void searchForArtist() {
	}

	@Test
	public void createPlaylist() {
		// TODO
	}

}
