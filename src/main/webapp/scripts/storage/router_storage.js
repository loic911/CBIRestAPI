'use strict';

retrievalApp
    .config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
            $routeProvider
                .when('/storage', {
                    templateUrl: 'views/storages.html',
                    controller: 'StorageController',
                    resolve:{
                        resolvedStorage: ['Storage', function (Storage) {
                            return Storage.query().$promise;
                        }]
                    },
                    access: {
                        authorizedRoles: [USER_ROLES.all]
                    }
                })
        });
