package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.CommitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2019_2.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the buildType with id = 'BuildTrigger'
accordingly, and delete the patch script.
*/
changeBuildType(RelativeId("BuildTrigger")) {
    vcs {
        remove(DslContext.settingsRoot.id!!)
        add(RelativeId("HttpsGithubComGradleNativePlatformGitRefsHeadsMaster1"))
    }

    features {
        val feature1 = find<CommitStatusPublisher> {
            commitStatusPublisher {
                vcsRootExtId = "${DslContext.settingsRoot.id}"
                publisher = github {
                    githubUrl = "https://api.github.com"
                    authType = personalToken {
                        token = "%github.bot-teamcity.token%"
                    }
                }
            }
        }
        feature1.apply {
            vcsRootExtId = ""
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = personalToken {
                    token = "credentialsJSON:5fe7c16b-cef8-4e22-a305-52f6ae768b72"
                }
            }
        }
    }
}
