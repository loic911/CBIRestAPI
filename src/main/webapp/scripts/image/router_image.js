'use strict';

retrievalApp
    .config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
            $routeProvider
                .when('/image', {
                    templateUrl: 'views/images.html',
                    controller: 'ImageController',
                    resolve:{
                        resolvedImage: ['Image', function (Image) {
                            return Image.query().$promise;
                        }]
                    },
                    access: {
                        authorizedRoles: [USER_ROLES.all]
                    }
                })
        });
