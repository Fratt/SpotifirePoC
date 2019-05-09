package ch.frattino.spotifirepoc;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.special.SnapshotResult;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.data.albums.GetAlbumRequest;
import com.wrapper.spotify.requests.data.playlists.AddTracksToPlaylistRequest;
import com.wrapper.spotify.requests.data.playlists.CreatePlaylistRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchAlbumsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchArtistsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import com.wrapper.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SpotifirePocApplicationTests {

    private static Logger LOG = LoggerFactory.getLogger(SpotifirePocApplicationTests.class);
    private static JsonSerializer JSON_SERIALIZER = JsonSerializer.DEFAULT_READABLE;
    private static SpotifyApi SPOTIFY_API;

    @BeforeClass
    public static void setup() {
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
    public void searchForAlbumsCrawl() {
        try {
                int page = 0;
                int pageSize = 50; // This is the maximum allowed by the API
                int totalResults;
                do {
                    int offset = page * pageSize;
                    SearchAlbumsRequest request = SPOTIFY_API
                            .searchAlbums("michael jackson")
                            .limit(pageSize) // This is the maximum allowed by the API
                            .offset(offset)
                            .build();
                    Paging<AlbumSimplified> paging = request.execute();
                    totalResults = paging.getTotal();
                    printPage(paging, page, offset);
                    page++;
                } while (page * pageSize < totalResults);
        } catch (Exception e) {
            LOG.error("Error", e);
        }
    }

    private void printPage(Paging<AlbumSimplified> paging, int page, int offset) {
        for (int i=0; i<paging.getItems().length; i++) {
            AlbumSimplified album = paging.getItems()[i];
            LOG.info("[" + (page + 1) + "][" + (i + offset + 1) + "] " + printArtists(album.getArtists()) + ": " + album.getName() + " (" + album.getAlbumType() + ")");
        }
    }

    private String printArtists(ArtistSimplified[] artists) {
        return Stream.of(artists).map(ArtistSimplified::getName).collect(Collectors.joining(", "));
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
    public void getCurrentUser() throws IOException, SpotifyWebApiException {

        grabAccessToken();

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
    public void createPlaylist() throws IOException, SpotifyWebApiException {

        grabAccessToken();

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
                        randomTracks.stream().map(Track::getUri).collect(Collectors.toList()).toArray(new String[]{}))
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

    private void grabAccessToken() throws IOException, SpotifyWebApiException {
        String refreshToken = Helpers.readEnvOrWarn("SPOTIFY_REFRESH_TOKEN", LOG);
        if (refreshToken == null) {
            Assert.fail("Couldn't continue without a refresh token.");
        }
        SPOTIFY_API.setRefreshToken(refreshToken);

        // We grab an access using the refresh token
        LOG.info("Obtaining authorization code using the refresh token...");
        AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = SPOTIFY_API.authorizationCodeRefresh()
                .build();
        AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();
        SPOTIFY_API.setAccessToken(authorizationCodeCredentials.getAccessToken());
        LOG.info("Success!");
    }

}
