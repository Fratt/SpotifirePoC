package ch.frattino.spotifirepoc;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class SpotifyWrapper {

    private static Logger LOG = LoggerFactory.getLogger(SpotifyWrapper.class);
    private static SpotifyApi api = null;

    public static synchronized SpotifyApi getApi() {
        if (api == null) {
            api = buildApi();
        }
        return api;
    }

    private static SpotifyApi buildApi() {

        String clientId = Helpers.readEnvOrWarn("SPOTIFY_CLIENT_ID", LOG);
        String clientSecret = Helpers.readEnvOrWarn("SPOTIFY_CLIENT_SECRET", LOG);

        // Wet set up the API
        SpotifyApi api = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRedirectUri(uri("http://localhost:13378"))
                .build();

        // We grab a "guest access token"
        ClientCredentialsRequest clientCredentialsRequest = api.clientCredentials().build();
        ClientCredentials clientCredentials = null;
        try {
            clientCredentials = clientCredentialsRequest.execute();
        } catch (IOException | SpotifyWebApiException e) {
            LOG.error("Couldn't get a guest access token", e);
            return null;
        }
        api.setAccessToken(clientCredentials.getAccessToken());
        return api;
    }

    private static URI uri(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public static String getAuthorizationCodeUri() {
        // TODO : We don't need all those authorisations!
        AuthorizationCodeUriRequest authorizationCodeUriRequest = getApi().authorizationCodeUri()
                .scope("user-read-recently-played,user-top-read,user-library-modify,user-library-read,playlist-read-private,playlist-modify-public,playlist-modify-private,playlist-read-collaborative,user-read-email,user-read-birthdate,user-read-private,user-read-playback-state,user-modify-playback-state,user-read-currently-playing,app-remote-control,streaming,user-follow-read,user-follow-modify")
                .build();
        return authorizationCodeUriRequest.execute().toString();
    }

}
