/**
 * 
 */

var myApp = angular.module('searchApp', []);
myApp.controller('searchCtrl', function($scope) {
    $scope.search = function(){
          console.log("search " + $scope.query);
    };
});
