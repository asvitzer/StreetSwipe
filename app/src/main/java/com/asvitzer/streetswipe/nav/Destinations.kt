package com.asvitzer.streetswipe.nav

interface Destinations {
    val route: String
}

object Request: Destinations {
    override val route: String = "Request"

}