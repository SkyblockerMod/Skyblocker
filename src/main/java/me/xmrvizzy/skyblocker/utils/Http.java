package me.xmrvizzy.skyblocker.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import net.minecraft.SharedConstants;

/**
 * @implNote All http requests are sent using HTTP 2
 */
public class Http {
	private static final String USER_AGENT = "Skyblocker/" + SkyblockerMod.VERSION + " (" + SharedConstants.getGameVersion().getName() + ")";
	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.build();
	
	public static String sendGetRequest(String url) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.GET()
				.header("Accept", "application/json")
				.header("Accept-Encoding", "gzip, deflate")
				.header("User-Agent", USER_AGENT)
				.version(Version.HTTP_2)
				.uri(URI.create(url))
				.build();
		
		HttpResponse<InputStream> response = HTTP_CLIENT.send(request, BodyHandlers.ofInputStream());
		InputStream decodedInputStream = getDecodedInputStream(response);
		String body = new String(decodedInputStream.readAllBytes());
		
		return body;
	}
	
	public static HttpHeaders sendHeadRequest(String url) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.method("HEAD", BodyPublishers.noBody())
				.header("Accept", "application/json")
				.header("Accept-Encoding", "gzip, deflate")
				.header("User-Agent", USER_AGENT)
				.version(Version.HTTP_2)
				.uri(URI.create(url))
				.build();
		
		HttpResponse<Void> response = HTTP_CLIENT.send(request, BodyHandlers.discarding());		
		return response.headers();
	}
	
	private static InputStream getDecodedInputStream(HttpResponse<InputStream> response) {
		String encoding = getContentEncoding(response);
		
		try {
			switch (encoding) {
				case "":
					return response.body();
				case "gzip":
					return new GZIPInputStream(response.body());
				case "deflate":
					return new InflaterInputStream(response.body());
				default:
					throw new UnsupportedOperationException("The server sent content in an unexpected encoding: " + encoding);
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	private static String getContentEncoding(HttpResponse<InputStream> response) {
		return response.headers().firstValue("Content-Encoding").orElse("");
	}
	
	public static String getEtag(HttpHeaders headers) {
		return headers.firstValue("Etag").orElse("");
	}
	
	public static String getLastModified(HttpHeaders headers) {
		return headers.firstValue("Last-Modified").orElse("");
	}
}
