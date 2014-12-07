/**
 * 
 */

var myApp = angular.module('searchApp', []);
myApp.directive('autoComplete', function($timeout) {
  return function(scope, iElement, iAttrs) {
          iElement.autocomplete({
              source: scope[iAttrs.uiItems],
              select: function() {
                  $timeout(function() {
                    iElement.trigger('input');
                  }, 0);
              }
          });
  };
});
myApp.controller('searchCtrl', function($scope, $http) {

  $scope.names = ["john", "bill", "charlie", "robert", "alban", "oscar", "marie", "celine", "brad", "drew", "rebecca", "michel", "francis", "jean", "paul", "pierre", "nicolas", "alfred", "gerard", "louis", "albert", "edouard", "benoit", "guillaume", "nicolas", "joseph"];

	  var xmlhttp;
	  $scope.content = "";
    $scope.autoComplete = function(){
          console.log("type " + $scope.query);
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
