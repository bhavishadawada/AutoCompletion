/**
 * 
 */

var myApp = angular.module('searchApp', []);
myApp.controller('searchCtrl', function($scope, $http) {
	  var xmlhttp;
	  $scope.content = "";
    $scope.autoComplete = function(){
          console.log("type " + $scope.query);
    };
    $scope.search = function(){
      console.log("search " + $scope.query);
      xmlhttp=new XMLHttpRequest();
      xmlhttp.open("GET","http://localhost:25811/search?query="+$scope.query+"&ranker=favorite&format=html",true);
      xmlhttp.onreadystatechange = handler;
      xmlhttp.send();
    };
    
    function handler(){
      if (xmlhttp.readyState==4 && xmlhttp.status==200)
      {
      	$scope.content = xmlhttp.responseText;
     		$scope.$apply();
      }
    }
});
