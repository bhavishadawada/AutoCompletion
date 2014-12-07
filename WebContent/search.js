/**
 * 
 */

var myApp = angular.module('searchApp', []);

myApp.directive('ngEnter', function () {
  return function (scope, element, attrs) {
      element.bind("keydown keypress", function (event) {
          if(event.which === 13) {
              scope.$apply(function (){
                  scope.$eval(attrs.ngEnter);
              });

              event.preventDefault();
          }
      });
  };
});

myApp.controller('searchCtrl', function($scope, $http) {

	  var xmlhttp;
	  $scope.content = "";
	  $scope.termLs = ["google", "yahoo", "apple", "facebook"];
    $scope.autoComplete = function(){
          console.log("type " + $scope.query);
          // update $scope.termLs here
    };
    $scope.search = function(){
      console.log("search " + $scope.query);
      xmlhttp=new XMLHttpRequest();
      xmlhttp.open("GET","http://localhost:25811/search?query="+$scope.query+"&ranker=favorite&format=text",true);
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
