/**
 * 
 */

var myApp = angular.module('searchApp', []);
myApp.controller('searchCtrl', function($scope, $http) {
	  var xmlhttp;
    $scope.autoComplete = function(){
          console.log("type " + $scope.query);
    };
    $scope.search = function(){
      console.log("search " + $scope.query);
      xmlhttp=new XMLHttpRequest();
      xmlhttp.open("GET","http://localhost:25811/search?query="+$scope.query+"&ranker=favorite",true);
      xmlhttp.onreadystatechange = handler;
      xmlhttp.send();
    };
    
    var handler = function(){
    	console.log("handler");
    	console.log(xmlhttp.responseText);
    }
});
