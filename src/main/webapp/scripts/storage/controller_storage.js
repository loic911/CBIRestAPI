'use strict';

retrievalApp.controller('StorageController', function ($scope, resolvedStorage, Storage) {

        $scope.storages = resolvedStorage;

        $scope.create = function () {
            Storage.save($scope.storage,
                function () {
                    $scope.storages = Storage.query();
                    $('#saveStorageModal').modal('hide');
                    $scope.clear();
                });
        };

        //$scope.update = function (id) {
        //    $scope.storage = Storage.get({id: id});
        //    $('#saveStorageModal').modal('show');
        //};

        $scope.delete = function (id) {
            Storage.delete({id: id},
                function () {
                    $scope.storages = Storage.query();
                });
        };

        $scope.clear = function () {
            $scope.storage = {name: null, size: null, id: null};
        };
    });
