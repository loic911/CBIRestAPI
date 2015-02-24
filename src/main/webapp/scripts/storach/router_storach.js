'use strict';

retrievalApp
    .config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
            $routeProvider
                .when('/storach', {
                    templateUrl: 'views/storachs.html',
                    controller: 'StorachController',
                    resolve:{
                        resolvedStorach: ['Storach', function (Storach) {
                            return Storach.query().$promise;
                        }]
                    },
                    access: {
                        authorizedRoles: [USER_ROLES.all]
                    }
                })
        });
