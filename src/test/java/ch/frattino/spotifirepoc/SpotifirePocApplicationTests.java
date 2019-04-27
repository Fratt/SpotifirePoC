package ch.frattino.spotifirepoc;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import com.wrapper.spotify.requests.data.albums.GetAlbumRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchAlbumsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchArtistsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import com.wrapper.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;
import org.apache.juneau.json.JsonSerializer;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SpotifirePocApplicationTests {

	private static Logger LOG = LoggerFactory.getLogger(SpotifirePocApplicationTests.class);
	private static JsonSerializer JSON_SERIALIZER = JsonSerializer.DEFAULT_READABLE;
	private static SpotifyApi SPOTIFY_API;

	@BeforeClass
	public static void setup(){
		SPOTIFY_API = SpotifyWrapper.getApi();
	}

	@Test
	public void searchForTrack() {
		SearchTracksRequest request = SPOTIFY_API.searchTracks("Lost in the Twilight Hall").build();
		try {
			Paging<Track> paging = request.execute();
			for (Track track : paging.getItems()) {
				LOG.info(JSON_SERIALIZER.serialize(track));
			}
		} catch (Exception e) {
			LOG.error("Error", e);
		}
	}

	@Test
	public void searchForAlbum() {
		SearchAlbumsRequest request = SPOTIFY_API.searchAlbums("blind guardian a night at the opera").build();
		try {
			Paging<AlbumSimplified> paging = request.execute();
			for (AlbumSimplified item : paging.getItems()) {
				GetAlbumRequest getAlbumRequest = SPOTIFY_API.getAlbum(item.getId()).build();
				Album album = getAlbumRequest.execute();
				LOG.info(JSON_SERIALIZER.serialize(album));
			}
		} catch (Exception e) {
			LOG.error("Error", e);
		}
	}

	@Test
	public void searchForArtist() {
		SearchArtistsRequest request = SPOTIFY_API.searchArtists("blind guardian").build();
		try {
			Paging<Artist> paging = request.execute();
			for (Artist artist : paging.getItems()) {
				LOG.info(JSON_SERIALIZER.serialize(artist));
			}
		} catch (Exception e) {
			LOG.error("Error", e);
		}
	}

	@Test
	public void getCurrentUser() {

		SPOTIFY_API.setAccessToken("");
		SPOTIFY_API.setRefreshToken("");

		GetCurrentUsersProfileRequest getCurrentUsersProfileRequest = SPOTIFY_API.getCurrentUsersProfile()
				.build();
		try {
			User user = getCurrentUsersProfileRequest.execute();
			LOG.info(JSON_SERIALIZER.serialize(user));
		} catch (Exception e) {
			LOG.error("Error", e);
		}
	}

	@Test
	public void createPlaylist() {

	}

}
