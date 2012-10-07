angular.module('kiwi.resource', ['ngResource']).
    factory('Repo',function ($resource) {
        var Repo = $resource(
            '/api/repos',
            { apiKey:'e0915567a4a4b02d2a1b731997050bc3642a95d5' },
            {
                query:{method:'GET', isArray:true},
                create:{method:'POST'}
            }
        );

        return Repo;
    }).
    factory('RepoWatch', function ($resource) {
        var RepoWatch = $resource(
            '/api/repos/pooling',
            { apiKey:'e0915567a4a4b02d2a1b731997050bc3642a95d5' },
            {
                switchPooling:{method:'POST'}
            }
        );

        return RepoWatch;
    });