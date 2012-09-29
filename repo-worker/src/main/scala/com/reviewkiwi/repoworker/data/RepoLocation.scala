package com.reviewkiwi.repoworker.data

import java.net.URI

abstract class RepoLocation(uri: URI)

case class GitRepoLocation(uri: URI)
