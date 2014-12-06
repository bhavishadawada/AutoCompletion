/**
 * 
 */

var myApp = angular.module('searchApp', []);
myApp.controller('searchCtrl', function($scope) {
    $scope.autoComplete = function(){
          console.log("type " + $scope.query);
    };
    $scope.search = function(){
          console.log("search " + $scope.query);
    }
});
