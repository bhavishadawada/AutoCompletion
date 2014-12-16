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

myApp.directive('ngSpace', function () {
  return function (scope, element, attrs) {
      element.bind("keydown keypress", function (event) {
          if(event.which === 32) {
              scope.$apply(function (){
                  scope.$eval(attrs.ngSpace);
              });
              event.preventDefault();
          }
      });
  };
});

myApp.controller('searchCtrl', function($scope, $http) {

	  var searchXmlhttp;
	  var suggestXmlhttp;
	  var userId;

	  if (typeof(Storage) != "undefined") {
      userId = localStorage.getItem("userId");
      console.log("from localStorage get userId: " + userId);
    }
    if (userId == null) {
    	var xmlhttp = new XMLHttpRequest();
    	xmlhttp.open("GET","http://localhost:25811/getId?query=test", true);
    	xmlhttp.onreadystatechange = function(){
    		if (xmlhttp.readyState==4 && xmlhttp.status==200)
        {
      	userId = xmlhttp.responseText;
     		$scope.$apply();
     		localStorage.setItem("userId", userId);
     		console.log("from server get userId: " + userId)
        }
    	};
    	xmlhttp.send();
    }

	  $scope.suggestList = [];
    $scope.suggest = function(){
    	//$scope.query = $scope.query.replace(/\s/g,"%20");
    	$scope.query = $scope.query.replace("-","%20");
      console.log("type " + $scope.query);
      suggest($scope.query);
    };
    $scope.predict = function(){
    	console.log("type space" + $scope.query);
    	$scope.query = $scope.query + " ";
    	suggest($scope.query);
    }
    $scope.search = function(){
    	//$scope.query = $scope.query.replace(/\s/g,"%20");
      console.log("search " + $scope.query);
      searchXmlhttp=new XMLHttpRequest();
      searchXmlhttp.open("GET","http://localhost:25811/search?query="+$scope.query+"&ranker=favorite&format=json&num=30",true);
      searchXmlhttp.onreadystatechange = searchHandler;
      searchXmlhttp.send();
      expansion();
    };
    $scope.select = function(suggestId){
    	$scope.query = $scope.suggestList[suggestId].term;
    	$scope.search();
    }

    $scope.getExpansion= function(expansionId){
    	$scope.query = $scope.expansionLs[expansionId].term;
    	$scope.search();
    }
    
    function expansion(){
    	var xmlhttp = new XMLHttpRequest();
    	xmlhttp.open("GET","http://localhost:25811/prf?query="+$scope.query+"&ranker=favorite&format=json&num=30&numterms=10",true);
    	console.log("GET","http://localhost:25811/prf?query="+$scope.query+"&ranker=favorite&format=json&num=30&numterms=10");
    	xmlhttp.onreadystatechange = function(){
      	$scope.expansionLs = angular.fromJson(xmlhttp.responseText);
     		$scope.$apply();
     		console.log($scope.expansionLs);
    	}
    	xmlhttp.send()
    }
    
    function suggest(prefix){
    	prefix = prefix.replace(/\s/g,"%20");
      suggestXmlhttp=new XMLHttpRequest();
      suggestXmlhttp.open("GET","http://localhost:25811/suggest?query="+prefix+"&userId="+userId+"&ranker=favorite&format=json&num=10",true);
      console.log("http://localhost:25811/suggest?query="+prefix+"&userId="+userId+"&ranker=favorite&format=json&num=10");
      suggestXmlhttp.onreadystatechange = suggestHandler;
      suggestXmlhttp.send();
    }
    
    function getContent(tag, content) 
    { 
       var x = content.indexOf("<"+tag);
       x = content.indexOf(">", x);    
       var y = content.lastIndexOf("</"+tag+">"); 
       return content.slice(x + 1, y);
    } 
    
    function fetch(url, id){
    	var xmlhttp = new XMLHttpRequest();
    	xmlhttp.open("GET", url);
    	xmlhttp.onreadystatechange = function(){
    		if (xmlhttp.readyState==4 && xmlhttp.status==200)
        {
      	content = xmlhttp.responseText;
      	var body = getContent("body", content);
      	document.getElementById("fetch").innerHTML = body;
      	var temp = document.getElementById("mw-content-text");
      	document.getElementById(id).innerHTML = temp.getElementsByTagName("p")[0].innerHTML;

     		$scope.$apply();
        }
    	};
    	xmlhttp.send();
    	
    }
    
    function searchHandler(){
      if (searchXmlhttp.readyState==4 && searchXmlhttp.status==200)
      {
      	$scope.webList = angular.fromJson(searchXmlhttp.responseText);
     		$scope.$apply();
     		for(var i = 0; i < $scope.webList.length; i++){
     			fetch("/AutoCompletion/wiki/"+$scope.webList[i].title, "web"+$scope.webList[i].docid);
     		}
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
