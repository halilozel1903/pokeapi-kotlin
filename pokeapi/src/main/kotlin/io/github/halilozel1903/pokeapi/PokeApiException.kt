package io.github.halilozel1903.pokeapi

/** Every failure of [PokeApiClient]. Catch the subclasses you care about, or all of them. */
public sealed class PokeApiException(
    /** The requested url. */
    public val url: String,
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {

    /** PokeAPI answered 404: there is no resource with that id or name. */
    public class NotFound(url: String) : PokeApiException(url, "Not found: $url")

    /** PokeAPI answered with another unsuccessful [status] (429, 5xx, ...). */
    public class Http(url: String, public val status: Int) : PokeApiException(url, "HTTP $status for $url")

    /** The request failed before a response arrived (offline, timeout, DNS). */
    public class Network(url: String, cause: Throwable) :
        PokeApiException(url, "Request failed for $url: ${cause.message ?: cause::class.simpleName}", cause)

    /** The response was not the JSON this library expects. */
    public class Decoding(url: String, cause: Throwable) :
        PokeApiException(url, "Could not decode the response of $url: ${cause.message}", cause)
}
