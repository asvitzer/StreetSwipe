package com.asvitzer.streetswipe.nav

interface Destinations {
    val route: String
}

object Request: Destinations {
    override val route: String = "Request"

}
object Loading: Destinations {
    override val route: String = "Loading"

}
object Retry: Destinations {
    override val route: String = "Retry"

}