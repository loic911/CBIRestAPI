'use strict';

retrievalApp.controller('ImageController', function ($scope,$routeParams, Image,ImageByStorage) {

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
        }
        $scope.list($routeParams["storage"]);

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
