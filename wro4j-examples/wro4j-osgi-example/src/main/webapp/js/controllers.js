'use strict';

/* Controllers */

angular.module('myApp.controllers', []).

    controller('homeCtrl', ['$scope', '$http', '$log',
    function($scope, $http, $log) {

      $scope.loadFile = function(file) {

        $log.debug("Enter loadFile");
        $scope.fileTextArea = "Loading ...\n\n(If this is a combination of several static files, " +
            "it might take a second to process)";

        if($scope.currFile != null){
          $scope.currFile.class = "inactive";
        }

        file.class="active";
        $scope.currFile = file;

        $http({method: 'GET', url: file.path}).
            success(function(data, status, headers, config) {

              $log.debug("Retrieved file contents");
              $scope.fileTextArea = data;

            }).
            error(function(data, status, headers, config) {

              $log.error("Unable to retrieve file contents");
              $scope.fileTextArea = "Uh oh! Problem occurred trying to load "+file.path;

            });

      };

      $scope.files = [
        {
          name: 'Jquery',
          path: '/wro4j-osgi/wro/jquery.js'
        },
        {
          name: 'Angular',
          path: '/wro4j-osgi/wro/angular.js'
        },
        {
          name: 'Bootstrap Javascript',
          path: '/wro4j-osgi/wro/bootstrap.css'
        },
        {
          name: 'Bootstrap Styles',
          path: '/wro4j-osgi/wro/bootstrap.css'
        },
        {
          name: 'All thirdparty Javascript',
          path: '/wro4j-osgi/wro/thirdparty.js'
        },
        {
          name: 'All thirdparty Css',
          path: '/wro4j-osgi/wro/thirdparty.css'
        },
        {
          name: 'Custom Javascript',
          path: '/wro4j-osgi/wro/application.js'
        }];

      $scope.fileTextArea = "Please click a link";

      /*$scope.$watch('fileTextArea', function (newVal, olVal) {
        if (!angular.isUndefined(newVal)) {
          window.console.log(newVal);
        }
      });*/

      $scope.loadFile($scope.files[0]);

    }])

  .controller('statusCtrl', ['$scope', 'StatusService',
    function($scope, StatusService){
      $scope.lastError = StatusService;
    }]);