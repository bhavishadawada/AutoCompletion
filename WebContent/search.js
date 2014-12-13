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

	  var searchXmlhttp;
	  var suggestXmlhttp;
	  $scope.suggestList = [];
    $scope.suggest = function(){
      console.log("type " + $scope.query);
      suggestXmlhttp=new XMLHttpRequest();
      suggestXmlhttp.open("GET","http://localhost:25811/suggest?query="+$scope.query+"&ranker=favorite&format=json&num=10",true);
      suggestXmlhttp.onreadystatechange = suggestHandler;
      suggestXmlhttp.send();
    };
    $scope.search = function(){
      console.log("search " + $scope.query);
      searchXmlhttp=new XMLHttpRequest();
      searchXmlhttp.open("GET","http://localhost:25811/search?query="+$scope.query+"&ranker=favorite&format=json&num=30",true);
      searchXmlhttp.onreadystatechange = searchHandler;
      searchXmlhttp.send();
    };
    
    function searchHandler(){
      if (searchXmlhttp.readyState==4 && searchXmlhttp.status==200)
      {
      	$scope.webList = angular.fromJson(searchXmlhttp.responseText);
     		$scope.$apply();
      }
    }

    function suggestHandler(){
      if (suggestXmlhttp.readyState==4 && suggestXmlhttp.status==200)
      {
      	$scope.suggestList = angular.fromJson(suggestXmlhttp.responseText);
     		$scope.$apply();
     		console.log($scope.suggestList);
      }
    }
});
