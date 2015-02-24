'use strict';

retrievalApp.controller('StorachController', function ($scope, resolvedStorach, Storach) {

        $scope.storachs = resolvedStorach;

        $scope.create = function () {
            Storach.save($scope.storach,
                function () {
                    $scope.storachs = Storach.query();
                    $('#saveStorachModal').modal('hide');
                    $scope.clear();
                });
        };

        $scope.update = function (id) {
            $scope.storach = Storach.get({id: id});
            $('#saveStorachModal').modal('show');
        };

        $scope.delete = function (id) {
            Storach.delete({id: id},
                function () {
                    $scope.storachs = Storach.query();
                });
        };

        $scope.clear = function () {
            $scope.storach = {name: null, size: null, id: null};
        };
    });
