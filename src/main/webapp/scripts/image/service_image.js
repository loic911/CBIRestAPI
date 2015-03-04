'use strict';

retrievalApp.factory('Image', function ($resource) {
        return $resource('api/images/:id', {}, {
            'query': { method: 'GET', isArray: true},
            'get': { method: 'GET'}
        });
    });

retrievalApp.factory('ImageByStorage', function ($resource) {
    return $resource('api/storages/:storage/images', {}, {
        'query': { method: 'GET', isArray: true},
        'get': { method: 'GET'}
    });
});
