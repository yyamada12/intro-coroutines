package tasks

import contributors.*
import kotlinx.coroutines.*

suspend fun loadContributorsConcurrent(service: GitHubService, req: RequestData): List<User> = coroutineScope {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .body() ?: emptyList()

    repos.map {repo ->
        async {
            log("starting loading for ${repo.name}")
            service.getRepoContributors(req.org, repo.name)
                .also {logUsers(repo, it)}
                .bodyList()
        }
    }.awaitAll().flatten().aggregate()
}