package tasks

import contributors.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

suspend fun loadContributorsChannels(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    coroutineScope {
        val repos = service
            .getOrgRepos(req.org)
            .also { logRepos(req, it) }
            .body() ?: emptyList()

        val allUsers = mutableListOf<User>()
        val channel = Channel<List<User>>()

        repos.forEach {repo ->
            launch {
                log("starting loading for ${repo.name}")
                val users = service.getRepoContributors(req.org, repo.name)
                    .also {logUsers(repo, it)}
                    .bodyList()
                channel.send(users)
            }
        }
        repeat(repos.size){ i ->
            val users = channel.receive()
            allUsers.addAll(users)
            updateResults(allUsers.aggregate(), i == repos.lastIndex)
        }
    }
}
