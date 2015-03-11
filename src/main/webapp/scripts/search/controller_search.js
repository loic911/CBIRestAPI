'use strict';

retrievalApp.controller('SearchController',  function ($location,$scope,Storage,$http) {

    $scope.maxSearch = 30;

    $scope.cleanError = function() {
        $scope.search = {error: null};
    };
    $scope.cleanError();

    $scope.storages = Storage.query(function(storage) {
        //select all storage by default
        $scope.storagesSelected = _.pluck(
            _.filter(storage, function(x){return x['id']!=undefined}
            ),'id');
    });


    $scope.$watch('storagesSelected', function(storagesSelected){
        console.log(storagesSelected);
    });

    $scope.search = function() {
        console.log($scope);
        $http(
            {url:'/api/searchUrl',
             method:'POST',
             params: {url:$scope.urlSearch,max:$scope.maxSearch}}
        ).success(function(data, status, headers, config) {
            console.log(data);
            $scope.cleanError();
        }).
        error(function(data, status, headers, config) {
            console.log(data);
            $scope.cleanError();
            $scope.search.error = data.message;
        });



    }


});

