package code.api.github

/**
 * {{{
 *   {
 *     "before": "5aef35982fb2d34e9d9d4502f6ede1072793222d",
 *     "repository": {
 *       "url": "http://github.com/defunkt/github",
 *       "name": "github",
 *       "description": "You're lookin' at it.",
 *       "watchers": 5,
 *       "forks": 2,
 *       "private": 1,
 *       "owner": {
 *         "email": "chris@ozmm.org",
 *         "name": "defunkt"
 *       }
 *     },
 *     "commits": [
 *       {
 *         "id": "41a212ee83ca127e3c8cf465891ab7216a705f59",
 *         "url": "http://github.com/defunkt/github/commit/41a212ee83ca127e3c8cf465891ab7216a705f59",
 *         "author": {
 *           "email": "chris@ozmm.org",
 *           "name": "Chris Wanstrath"
 *         },
 *         "message": "okay i give in",
 *         "timestamp": "2008-02-15T14:57:17-08:00",
 *         "added": ["filepath.rb"]
 *       },
 *       {
 *         "id": "de8251ff97ee194a289832576287d6f8ad74e3d0",
 *         "url": "http://github.com/defunkt/github/commit/de8251ff97ee194a289832576287d6f8ad74e3d0",
 *         "author": {
 *           "email": "chris@ozmm.org",
 *           "name": "Chris Wanstrath"
 *         },
 *         "message": "update pricing a tad",
 *         "timestamp": "2008-02-15T14:36:34-08:00"
 *       }
 *     ],
 *     "after": "de8251ff97ee194a289832576287d6f8ad74e3d0",
 *     "ref": "refs/heads/master"
 *   }
 * }}}
 */
case class GithubPostReceivePayload(
  before: String,
  repository: GithubRepositoryPayload,
  commits: List[GithubCommitPayload],
  after: String,
  ref: String
)

case class GithubRepositoryPayload(
  url: String,
  name: String,
  description: String,
  watchers: Int,
  forks: Int,
  `private`: Int,
  owner: GithubUserPayload
)


case class GithubUserPayload(
  email: String,
  name: String
)

case class GithubCommitPayload(
  id: String,
  url: String,
  author: GithubUserPayload,
  message: String,
  timestamp: String,
  added: List[String]
)