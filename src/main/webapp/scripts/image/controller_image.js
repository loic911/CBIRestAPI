'use strict';

retrievalApp.controller('ImageController', function ($location,$scope,$routeParams, Image,ImageByStorage, Storage) {

        $scope.cleanError = function() {
            $scope.image = {error : {create:null,delete:null}};
        };
        $scope.cleanError();

        $scope.list = function(param) {
            if(param) {
                $scope.images = ImageByStorage.query({storage:$routeParams["storage"]});
            } else {
                $scope.images = Image.query();
            }
        };
        $scope.list($routeParams["storage"]);

        $scope.storageChanged = function(selected) {
            console.log(selected);
            if(selected!="ALL STORAGES")
                $location.url("/storage/"+selected+"/image");
            else
                $location.url("/image");
        };

        $scope.storages = Storage.query(
            function(storage) {
                $scope.storages.unshift({id:"ALL STORAGES"});
                console.log(storage);
                $scope.selectedStorage = {selected: ($routeParams["storage"]?$routeParams["storage"]:"ALL STORAGES")};
            }
        );

        $scope.selectedStorage = $routeParams["storage"];

        //$scope.$watch("selectedStorage", function() {
        //
        //    if($scope.selectedStorage!=null) {
        //        $location.('storage', $scope.selectedStorage.id);
        //    }
        //});


        $scope.create = function () {
            Image.save($scope.image,
                function () {
                    $scope.cleanError();
                    $scope.list($routeParams["storage"]);
                    $('#saveImageModal').modal('hide');
                    $scope.clear();
                },
                function (e) {
                    console.log(e);
                    $scope.image.error = e.data.message;
                });
        };

        //$scope.update = function (id) {
        //    $scope.image = Image.get({id: id});
        //    $('#saveImageModal').modal('show');
        //};

        $scope.delete = function (id) {
            Image.delete({id: id},
                function () {
                    $scope.cleanError();
                    $scope.images = Image.query();
                });
        };

        $scope.clear = function () {
            $scope.image = {name: null, size: null, id: null};
        };


    });
