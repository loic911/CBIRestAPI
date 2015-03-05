'use strict';

retrievalApp.controller('ImageController',  function ($location,$scope,$upload,$routeParams, Image,ImageByStorage, Storage) {

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
            if(selected!="*** STORAGES ***")
                $location.url("/storage/"+selected+"/image");
            else
                $location.url("/image");
        };

        $scope.storages = Storage.query(
            function(storage) {
                $scope.storages.unshift({id:"*** STORAGES ***"});
                console.log(storage);
                $scope.selectedStorage = {selected: ($routeParams["storage"]?$routeParams["storage"]:"*** STORAGES ***")};
            }
        );

        $scope.selectedStorage = {selected: ($routeParams["storage"]?$routeParams["storage"]:"*** STORAGES ***")};
        $scope.selectedStorageCreate = $scope.selectedStorage;

        $scope.delete = function (image) {

            bootbox.confirm("Are you sure?", function(result) {
                if(result) {
                    ImageByStorage.delete({id: image.id,storage:image.storage},
                        function () {
                            $scope.cleanError();
                            $scope.list($routeParams["storage"]);
                            //$scope.images = Image.query();
                        });
                }

            });


        };

        $scope.clear = function () {
            $scope.image = {name: null, size: null, id: null};
        };

        $scope.printProperties = function(image) {
            var output = '';
            for (var property in image) {
                if(image.hasOwnProperty(property))
                    output += property + ': ' + image[property]+'<br>';
            }
            bootbox.alert(output, function() {
            });
        };

        $scope.create = function() {
            var $files = $scope.filesToUpload;
            console.log($scope.id);
            if(!$files) {
                $scope.cleanError();
                $scope.image.error.create = "No file selected";
            }

            var url = '/api/images?';
            if($scope.id) {
                url = url + "&id="+$scope.id
            }
            if($scope.keys) {
                url = url + "&keys="+$scope.keys
            }
            if($scope.values) {
                url = url + "&values="+$scope.values
            }
            if($scope.async) {
                url = url + "&async="+$scope.async
            }
            console.log($scope.selectedStorageCreate );
            if($scope.selectedStorageCreate && $scope.selectedStorageCreate.selected!="*** STORAGES ***") {
                url = url + "&storage="+$scope.selectedStorageCreate.selected
            }

            //$files: an array of files selected, each file has name, size, and type.
            for (var i = 0; i < $files.length; i++) {
                var file = $files[i];
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
                    $('#saveImageModal').modal('hide');
                    //$scope.storageChanged($scope.selectedStorageCreate.selected);
                    $scope.list($routeParams["storage"]);


                }).error(function(data, status, headers, config) {
                    // file is uploaded successfully
                    console.log(data);
                    $scope.cleanError();
                    $scope.image.error.create = data.message;
                });
                //.error(...)
                //.then(success, error, progress);
                // access or attach event listeners to the underlying XMLHttpRequest.
                //.xhr(function(xhr){xhr.upload.addEventListener(...)})
            }
        };

        $scope.onFileSelect = function($files) {
            $scope.filesToUpload = $files;
        };
});

//inject angular file upload directives and service.
//angular.module('myApp', ['angularFileUpload']);

var UploadController = [ '$scope', '$upload', function($scope, $upload) {


}];
