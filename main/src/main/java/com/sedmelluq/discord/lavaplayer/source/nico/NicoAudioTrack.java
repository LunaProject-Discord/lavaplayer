package com.sedmelluq.discord.lavaplayer.source.nico;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sedmelluq.discord.lavaplayer.container.mpeg.MpegAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.nico.objects.video.Session;
import com.sedmelluq.discord.lavaplayer.source.nico.objects.video.Watch;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.PersistentHttpStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;

/**
 * Audio track that handles processing NicoNico tracks.
 */
public class NicoAudioTrack extends DelegatedAudioTrack {
    private static final Logger log = LoggerFactory.getLogger(NicoAudioTrack.class);

    private final NicoAudioSourceManager sourceManager;

    /**
     * @param trackInfo     Track info
     * @param sourceManager Source manager which was used to find this track
     */
    public NicoAudioTrack(AudioTrackInfo trackInfo, NicoAudioSourceManager sourceManager) {
        super(trackInfo);

        this.sourceManager = sourceManager;
    }

    @Override
    public void process(LocalAudioTrackExecutor localExecutor) throws Exception {
        sourceManager.checkLoggedIn();

        try (HttpInterface httpInterface = sourceManager.getHttpInterface()) {
            loadVideoMainPage(httpInterface);
            String playbackUrl = loadPlaybackUrl(httpInterface);

            log.debug("URLからニコニコ動画を開始： {}", playbackUrl);

            try (PersistentHttpStream stream = new PersistentHttpStream(httpInterface, new URI(playbackUrl), null)) {
                processDelegate(new MpegAudioTrack(trackInfo, stream), localExecutor);
            }
        }
    }

    private void loadVideoMainPage(HttpInterface httpInterface) throws IOException {
        HttpGet request = new HttpGet("https://www.nicovideo.jp/watch/" + trackInfo.identifier);

        try (CloseableHttpResponse response = httpInterface.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (!HttpClientTools.isSuccessWithContent(statusCode)) {
                throw new IOException("Unexpected status code from video main page: " + statusCode);
            }

            EntityUtils.consume(response.getEntity());
        }
    }

    private String loadPlaybackUrl(HttpInterface httpInterface) throws IOException {
        String pageUrl = "https://www.nicovideo.jp/watch/" + trackInfo.identifier;
        HttpGet request = new HttpGet(pageUrl);
        log.info(pageUrl);

        try (CloseableHttpResponse response = httpInterface.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (!HttpClientTools.isSuccessWithContent(statusCode)) {
                throw new IOException("Unexpected status code from playback parameters page: " + statusCode);
            }
            Document document = Jsoup.parse(EntityUtils.toString(response.getEntity()));
            log.debug(document.toString());
            String json = document.select("div#js-initial-watch-data").attr("data-api-data");

            ObjectMapper om = new ObjectMapper();
            JsonNode jsonNode = om.readTree(URLDecoder.decode(json, "UTF-8"));
            Watch watchObj = om.readValue(URLDecoder.decode(json, "UTF-8"), Watch.class);
            log.debug(watchObj.toString());

            Session sessionObj = watchObj.getMedia().getDelivery().getMovie().getSession();
            log.debug("Niconico Session: {}", sessionObj.toString());

            if (watchObj.getViewer().getIsPremium()) {
                log.debug("プレミアムアカウントでニコニコ動画へリクエストします。(ユーザー名:{}, Transfer Preset:{})", watchObj.getViewer().getNickname(), sessionObj.getTransferPresets().get(0));
            }

            String postJson = String.format("{\"session\":{\"recipe_id\":\"%s\",\"content_id\":\"out1\",\"content_type\":\"movie\",\"content_src_id_sets\":[{\"content_src_ids\":[{\"src_id_to_mux\":{\"video_src_ids\":[\"%s\"],\"audio_src_ids\":[\"%s\"]}}]}],\"timing_constraint\":\"unlimited\",\"keep_method\":{\"heartbeat\":{\"lifetime\":120000}},\"protocol\":{\"name\":\"http\",\"parameters\":{\"http_parameters\":{\"parameters\":{\"http_output_download_parameters\":{\"use_well_known_port\":\"yes\",\"use_ssl\":\"yes\",\"transfer_preset\":\"%s\"}}}}},\"content_uri\":\"\",\"session_operation_auth\":{\"session_operation_auth_by_signature\":{\"token\":%s,\"signature\":\"%s\"}},\"content_auth\":{\"auth_type\":\"ht2\",\"content_key_timeout\":600000,\"service_id\":\"nicovideo\",\"service_user_id\":\"%s\"},\"client_info\":{\"player_id\":\"%s\"},\"priority\":%s}}",
                    sessionObj.getRecipeId(), sessionObj.getVideos().get(0), sessionObj.getAudios().get(0), sessionObj.getTransferPresets().get(0), sessionObj.getToken(), sessionObj.getSignature(), sessionObj.getServiceUserId(), sessionObj.getPlayerId(), sessionObj.getPriority());

            log.debug("PostJson: {} URL: {}", postJson, sessionObj.getUrls().get(0).getUrl());

            HttpPost httpPost = new HttpPost("https://api.dmc.nico/api/sessions?_format=json");
            httpPost.setEntity(new StringEntity(postJson, "UTF-8"));
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Accept-Encoding", "gzip, deflate, br");
            httpPost.addHeader("Accept-Language", "ja,en;q=0.9,en-GB;q=0.8,en-US;q=0.7");
            httpPost.addHeader("Connection", "keep-alive");
            httpPost.addHeader("DNT", "1");
            httpPost.addHeader("Content-type", "application/json");
            httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.63");
            httpPost.addHeader("Host", "api.dmc.nico");
            httpPost.addHeader("Origin", "https://www.nicovideo.jp");
            httpPost.addHeader("Referer", "https://www.nicovideo.jp/");
            httpPost.setEntity(new StringEntity(postJson, "UTF-8"));

            JsonNode postJsonNode;
            try (CloseableHttpResponse postresponse = httpInterface.execute(httpPost)) {
                int statusCodePost = postresponse.getStatusLine().getStatusCode();
                if (statusCodePost != 201) {
                    log.info(EntityUtils.toString(postresponse.getEntity()));
                    throw new IOException("動画の配信リクエスト時にエラー: " + statusCodePost);
                }
                postJsonNode = om.readTree(EntityUtils.toString(postresponse.getEntity()));
            }

            String sessionId = postJsonNode.get("data").get("session").get("id").asText();
            String contentUri = postJsonNode.get("data").get("session").get("content_uri").asText();
            log.debug("Session Json: {}", postJsonNode.get("data").toString());
            log.debug("URL: {}", contentUri);
            return contentUri;
        }
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new NicoAudioTrack(trackInfo, sourceManager);
    }

    @Override
    public AudioSourceManager getSourceManager() {
        return sourceManager;
    }
}
