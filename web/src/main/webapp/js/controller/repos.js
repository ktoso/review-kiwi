function ReposCtrl($scope, Repo, RepoWatch) {

    $scope.user = "ktoso";

    $scope.enablePooling = function(repo) {
        $scope.switchPooling(repo, true)
    };

    $scope.disablePooling = function(repo) {
        $scope.switchPooling(repo, false)
    };

    $scope.switchPooling = function(repo, enabled) {
        var ghRepoId = repo.github_repo_id;
        RepoWatch.switchPooling(
            {
                'github_repo_id': ghRepoId,
                'pooling': enabled
            },
            function() { repo.fetch_using_pooling = enabled }
        );
    };

    $scope.createNewRepository = function(name, url) {
        Repo.create(
            {name: name, url: url, watcher: $scope.user},
            function() { $scope.loadRepos() })
    };

    $scope.loadRepos = function() {
        $scope.repos = Repo.query();
    };

    $scope.loadRepos();
}