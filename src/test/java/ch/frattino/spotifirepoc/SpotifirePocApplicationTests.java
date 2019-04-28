package ch.frattino.spotifirepoc;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.special.SnapshotResult;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import com.wrapper.spotify.requests.data.albums.GetAlbumRequest;
import com.wrapper.spotify.requests.data.playlists.AddTracksToPlaylistRequest;
import com.wrapper.spotify.requests.data.playlists.CreatePlaylistRequest;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SpotifirePocApplicationTests {

	private static Logger LOG = LoggerFactory.getLogger(SpotifirePocApplicationTests.class);
	private static JsonSerializer JSON_SERIALIZER = JsonSerializer.DEFAULT_READABLE;
	private static SpotifyApi SPOTIFY_API;

	@BeforeClass
	public static void setup(){
		SPOTIFY_API = SpotifyWrapper.getApi();

		// Manually fill those up
		SPOTIFY_API.setAccessToken("BQCX65g36hZ-sKBCC9gSVGVbk55YXpRgDiVcnnnfz3reDwjHiKkTP161ri8yHU-fDw0LGJ0t1GeXm1UYttG0h84uddpkermhOC52n2d73FlXTEa8E11MVDkLwTKv8Dt3cBWDUpoKIUFdv0Occ7sCxs23GF6gCfEpSi_C0BQErWBGAzzvMA6w0HYDqmGbMJXgmi9roDxW9P3Gyr0gU3FDJzGwc9jxSNJ_ogRFmy-OJ6csMpbrJi9cvk5JDE-6ekflb2tCfYD3wKHIRfEFuhwFJ7L_B1R_");
		SPOTIFY_API.setRefreshToken("AQC1HUkzdQUBznkfpjcbjeP6LPZznB7S8bKG1iTEUC5ojATgfdGUSfTpr3l6Epm9q6dj66tVGVA9sdOBxt3C3cvygiitiFyKgSnAVAJJQAigoRQo-6sETEsduCktzehqdN0ZiA");

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

		// We find the user ID
		GetCurrentUsersProfileRequest getCurrentUsersProfileRequest = SPOTIFY_API.getCurrentUsersProfile()
				.build();
		String userId;
		try {
			User user = getCurrentUsersProfileRequest.execute();
			userId = user.getId();
		} catch (Exception e) {
			LOG.error("Error", e);
			Assert.fail(e.getMessage());
			return;
		}

		// We create the playlist
		CreatePlaylistRequest createPlaylistRequest = SPOTIFY_API.createPlaylist(userId, "PoC Playlist")
          .collaborative(false)
          .public_(false)
          .description("Created by Spotifire PoC.")
				.build();
		Playlist playlist;
		try {
			playlist = createPlaylistRequest.execute();
			LOG.info(JSON_SERIALIZER.serialize(playlist));
		} catch (Exception e) {
			LOG.error("Error", e);
			Assert.fail(e.getMessage());
			return;
		}

		// We grab a few random tracks
		List<Track> randomTracks = new ArrayList<>();
		addRandomTracks(randomTracks, "Blind Guardian");
		addRandomTracks(randomTracks, "Helloween");
		addRandomTracks(randomTracks, "Gamma Ray");
		addRandomTracks(randomTracks, "Stratovarius");

		// We add all those tracks to the playlist
		AddTracksToPlaylistRequest addTracksToPlaylistRequest = SPOTIFY_API
				.addTracksToPlaylist(playlist.getId(),
						randomTracks.stream().map(Track::getUri).collect(Collectors.toList()).toArray(new String[] {}))
				.build();
		try {
			SnapshotResult result = addTracksToPlaylistRequest.execute();
			LOG.info(JSON_SERIALIZER.serialize(result));
		} catch (Exception e) {
			LOG.error("Error", e);
			Assert.fail(e.getMessage());
		}

	}

	private void addRandomTracks(List<Track> tracks, String query) {
		SearchTracksRequest request = SPOTIFY_API.searchTracks(query).build();
		try {
			Paging<Track> paging = request.execute();
			tracks.addAll(Arrays.asList(paging.getItems()));
		} catch (Exception e) {
			LOG.error("Error", e);
			Assert.fail(e.getMessage());
		}
	}

}
