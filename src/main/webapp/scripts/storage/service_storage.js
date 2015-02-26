'use strict';

retrievalApp.factory('Storage', function ($resource) {
        return $resource('api/storages/:id', {}, {
            'query': { method: 'GET', isArray: true},
            'get': { method: 'GET'}
        });
    });
