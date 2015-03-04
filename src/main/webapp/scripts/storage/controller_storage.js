'use strict';

retrievalApp.controller('StorageController', function ($scope, resolvedStorage, Storage) {

        $scope.cleanError = function() {
            $scope.storage = {error : {create:null,delete:null}};
        };
        $scope.cleanError();


        $scope.storages = resolvedStorage;

        $scope.create = function () {
            Storage.save($scope.storage,
                function () {
                    $scope.cleanError();
                    $scope.storages = Storage.query();
                    $('#saveStorageModal').modal('hide');
                    $scope.clear();
                },
                function (e) {
                    console.log(e);
                    $scope.storage.error = e.data.message;
                });
        };


        //$scope.update = function (id) {
        //    $scope.storage = Storage.get({id: id});
        //    $('#saveStorageModal').modal('show');
        //};

        $scope.delete = function (id) {
            bootbox.confirm("Are you sure?", function(result) {
                if(result) {
                    Storage.delete({id: id},
                        function () {
                            $scope.cleanError();
                            $scope.storages = Storage.query();
                        });
                }

            });
        };

        $scope.clear = function () {
            $scope.storage = {name: null, size: null, id: null};
        };


    });
