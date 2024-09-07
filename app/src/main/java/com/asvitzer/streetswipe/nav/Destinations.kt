package com.asvitzer.streetswipe.nav

interface Destinations {
    val route: String
}
object Overview: Destinations {
    override val route: String = "Overview"
}
object Request: Destinations {
    override val route: String = "Request"

}