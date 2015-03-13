'use strict';

retrievalApp.controller('SearchController',  function ($location,$scope,Storage,$http,$upload) {

    //$scope.results = [{id:"1",properties:{date:"test"},similarities:"5545455"},{id:"3",properties:{date:"test"},similarities:"5545455"},{id:"4",properties:{date:"test"},similarities:"5545455"}];

    $scope.maxSearch = 30;

    $scope.cleanError = function() {
        $scope.search = {error: null};
        $scope.results = null;
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

    $scope.startSearch = function() {
        $scope.cleanError();
        var storages = "";
        console.log("************************");
        console.log($scope);

        if($scope.storagesSelected.length!=$scope.storages.length) {
            //if all storage are selected, don't set storage parameters

            storages = $scope.storagesSelected.join(";");
        }

        if($scope.filesToSearch && $scope.filesToSearch.length>0) {
            $scope.searchMultipart($scope.filesToSearch,$scope.maxSearch,storages)
        } else if($scope.urlSearch) {
            $scope.searchUrl($scope.urlSearch,$scope.maxSearch,storages)
        } else {
            $scope.cleanError();
            $scope.search.error = "No file selected";
        }
    };


    $scope.searchUrl = function(url,max,storages) {
        console.log($scope);
        $http(
            {url:'/api/searchUrl',
                method:'POST',
                params: {url:url,max:max,storages:storages,saveImage:true}}
        ).success(function(data, status, headers, config) {
                console.log(data);
                $scope.cleanError();
                $scope.results = data;
            }).
            error(function(data, status, headers, config) {
                console.log(data);
                $scope.cleanError();
                $scope.search.error = data.message;
            });
    }

    $scope.searchMultipart = function(files,max,storages) {
            console.log(files);
            var url = '/api/search?max='+max+"&storages="+storages + "&saveImage=true";

            //$files: an array of files selected, each file has name, size, and type.
            for (var i = 0; i < files.length; i++) {
                var file = files[i];
                $scope.upload = $upload.upload({
                    url: url, //upload.php script, node.js route, or servlet url
                    //method: 'POST' or 'PUT',
                    //headers: {'header-key': 'header-value'},
                    //withCredentials: true,
                    data: {},
                    file: file, // or list of files ($files) for html5 only
                    //fileName: 'doc.jpg' or ['1.jpg', '2.jpg', ...] // to modify the name of the file(s)
                    // customize file formData name ('Content-Desposition'), server side file variable name.
                    //fileFormDataName: myFile, //or a list of names for multiple files (html5). Default is 'file'
                    // customize how data is added to formData. See #40#issuecomment-28612000 for sample code
                    //formDataAppender: function(formData, key, val){}
                }).progress(function(evt) {
                    console.log('percent: ' + parseInt(100.0 * evt.loaded / evt.total));
                }).success(function(data, status, headers, config) {
                    // file is uploaded successfully
                    console.log(data);
                    $scope.cleanError();
                    $scope.results = data;
                }).error(function(data, status, headers, config) {
                    // file is uploaded successfully
                    console.log(data);
                    $scope.cleanError();
                    $scope.search.error = data.message;
                });
            }
    }

    $scope.onFileSelect = function($files) {
        $scope.filesToSearch = $files;
    };

});

