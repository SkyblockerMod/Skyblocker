package de.hysky.skyblocker.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import org.jetbrains.annotations.NotNull;

import de.hysky.skyblocker.SkyblockerMod;
import net.minecraft.SharedConstants;

/**
 * @implNote All http requests are sent using HTTP 2
 */
public class Http {
	private static final String NAME_2_UUID = "https://api.minecraftservices.com/minecraft/profile/lookup/name/";
	private static final String HYPIXEL_PROXY = "https://hysky.de/api/hypixel/";
	private static final String USER_AGENT = "Skyblocker/" + SkyblockerMod.VERSION + " (" + SharedConstants.getGameVersion().getName() + ")";
	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.followRedirects(Redirect.NORMAL)
			.build();
	
	public static String sendGetRequest(String url) throws IOException, InterruptedException {
		return sendCacheableGetRequest(url).content();
	}
	
	private static ApiResponse sendCacheableGetRequest(String url) throws IOException, InterruptedException {
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
		HttpHeaders headers = response.headers();
		
		return new ApiResponse(body, response.statusCode(), getCacheStatus(headers), getAge(headers));
	}
	
	public static HttpHeaders sendHeadRequest(String url) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.method("HEAD", BodyPublishers.noBody())
				.header("User-Agent", USER_AGENT)
				.version(Version.HTTP_2)
				.uri(URI.create(url))
				.build();
		
		HttpResponse<Void> response = HTTP_CLIENT.send(request, BodyHandlers.discarding());		
		return response.headers();
	}
	
	public static ApiResponse sendName2UuidRequest(String name) throws IOException, InterruptedException {
		return sendCacheableGetRequest(NAME_2_UUID + name);
	}
	
	/**
	 * @param endpoint the endpoint - do not include any leading or trailing slashes
	 * @param query the query string - use empty string if n/a
	 * @return the requested data with zero pre-processing applied
	 */
	public static ApiResponse sendHypixelRequest(String endpoint, @NotNull String query) throws IOException, InterruptedException {
		return sendCacheableGetRequest(HYPIXEL_PROXY + endpoint + query);
	}
	
	private static InputStream getDecodedInputStream(HttpResponse<InputStream> response) {
		String encoding = getContentEncoding(response.headers());
		
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
	
	private static String getContentEncoding(HttpHeaders headers) {
		return headers.firstValue("Content-Encoding").orElse("");
	}
	
	public static String getEtag(HttpHeaders headers) {
		return headers.firstValue("Etag").orElse("");
	}
	
	public static String getLastModified(HttpHeaders headers) {
		return headers.firstValue("Last-Modified").orElse("");
	}
	
	/**
	 * Returns the cache status of the resource
	 * 
	 * @see <a href="https://developers.cloudflare.com/cache/concepts/default-cache-behavior/#cloudflare-cache-responses">Cloudflare Cache Docs</a>
	 */
	private static String getCacheStatus(HttpHeaders headers) {
		return headers.firstValue("CF-Cache-Status").orElse("UNKNOWN");
	}
	
	private static int getAge(HttpHeaders headers) {
		return Integer.parseInt(headers.firstValue("Age").orElse("-1"));
	}
	
	//TODO If ever needed, we could just replace cache status with the response headers and go from there
	public record ApiResponse(String content, int statusCode, String cacheStatus, int age) implements AutoCloseable {
		
		public boolean ok() {
			return statusCode == 200;
		}
		
		public boolean cached() {
			return cacheStatus.equals("HIT");
		}

		@Override
		public void close() {
			//Allows for nice syntax when dealing with api requests in try catch blocks
			//Maybe one day we'll have some resources to free
		}
	}
}
