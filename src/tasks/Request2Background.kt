package tasks

import contributors.GitHubService
import contributors.RequestData
import contributors.User
import javax.swing.SwingUtilities
import kotlin.concurrent.thread

fun loadContributorsBackground(service: GitHubService, req: RequestData, updateResults: (List<User>) -> Unit) {
    thread {
        val users = loadContributorsBlocking(service, req)
        SwingUtilities.invokeLater {
            updateResults(users)
        }
    }
}