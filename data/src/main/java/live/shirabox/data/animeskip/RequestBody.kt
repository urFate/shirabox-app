package live.shirabox.data.animeskip

object RequestBody {
    const val API_CLIENT_CREATION_MUTATION = "mutation {\n" +
            "  createApiClient(client: {\n" +
            "    appName: \"ShiraBox\", description: \"ShiraBox user client key\"\n" +
            "  }) {\n" +
            "    id\n" +
            "  }\n" +
            "}"

    const val API_CLIENT_SEARCH_QUERY = "query Clients {\n" +
            "  myApiClients(search: \"ShiraBox\") {\n" +
            "    id\n" +
            "  }\n" +
            "}"
    fun loginQuery(email: String, hash: String): String {
        return "query Login {\n" +
                "  login(usernameEmail: \"$email\", passwordHash: \"$hash\") {\n" +
                "    authToken\n" +
                "    refreshToken\n" +
                "    account {\n" +
                "      username\n" +
                "      email\n" +
                "    }\n" +
                "  }\n" +
                "}"
    }

    fun searchShowsQuery(query: String): String {
        return "query SearchShows {\n" +
                "\tsearchShows(search:\"$query\", limit:1){\n" +
                "\t\tid\n" +
                "\t}\n" +
                "}"
    }

    fun findEpisodesByShowIdQuery(showId: String): String {
        return "query FindEpisodes {\n" +
                "\tfindEpisodesByShowId(showId:\"$showId\") {\n" +
                "    number\n" +
                "    season\n" +
                "    timestamps {\n" +
                "      at\n" +
                "      type {\n" +
                "        name\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}"
    }
}