//noinspection JSUnusedGlobalSymbols
function ReposCtrl($scope, $location, Repo, RepoWatch) {

    $scope.location = $location;

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
            {name: name, url: url, user: $scope.userId},
            function() { $scope.loadRepos() })
    };

    $scope.loadRepos = function() {
        // $location seems to hate me, see https://github.com/angular/angular.js/issues/1521 no idea why

        $scope.repos = Repo.load({u: $scope.userId});
    };

    $scope.userId = $scope.userId = location.search.replace('?u=', '');

    $scope.loadRepos();
}

ReposCtrl.$inject = ['$scope', '$location', 'Repo', 'RepoWatch'];