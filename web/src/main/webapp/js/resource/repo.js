angular.module('kiwi.resource', ['ngResource']).
    factory('Repo',function ($resource) {
        var Repo = $resource(
            '/api/repos',
            { apiKey:'' },
            {
                query:{method:'GET', isArray:true},
                load:{method:'GET', isArray:true},
                create:{method:'POST'}
            }
        );

        return Repo;
    }).
    factory('RepoWatch', function ($resource) {
        var RepoWatch = $resource(
            '/api/repos/pooling',
            { apiKey:'' },
            {
                switchPooling:{method:'POST'}
            }
        );

        return RepoWatch;
    });