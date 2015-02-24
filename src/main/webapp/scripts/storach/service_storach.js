'use strict';

retrievalApp.factory('Storach', function ($resource) {
        return $resource('app/rest/storachs/:id', {}, {
            'query': { method: 'GET', isArray: true},
            'get': { method: 'GET'}
        });
    });
