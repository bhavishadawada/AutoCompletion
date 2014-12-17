AutoCompletion
===============

Web Search Engines Final Project

 == build code ==
javac -cp lib/jsoup-1.8.1.jar:lib/org.apache.commons.io.jar:lib/apache-commons-lang.jar:lib/lucene-analyzers-3.6.2.jar:lib/lucene-core-3.6.2.jar:json-simple-1.1.1.jar src/edu/nyu/cs/cs2580/*.java

 == mining ==
java -cp src:lib/jsoup-1.8.1.jar:lib/org.apache.commons.io.jar:lib/apache-commons-lang.jar:lib/lucene-analyzers-3.6.2.jar:lib/lucene-core-3.6.2.jar:json-simple-1.1.1.jar edu.nyu.cs.cs2580.SearchEngine --mode=mining --options=conf/engine.conf

 == index ==
java -cp src:lib/jsoup-1.8.1.jar:lib/org.apache.commons.io.jar:lib/apache-commons-lang.jar:lib/lucene-analyzers-3.6.2.jar:lib/lucene-core-3.6.2.jar:json-simple-1.1.1.jar edu.nyu.cs.cs2580.SearchEngine --mode=index --options=conf/engine.conf

 == serve ==
java -cp src:lib/jsoup-1.8.1.jar:lib/org.apache.commons.io.jar:lib/apache-commons-lang.jar:lib/lucene-analyzers-3.6.2.jar:lib/lucene-core-3.6.2.jar:json-simple-1.1.1.jar -Xmx512m edu.nyu.cs.cs2580.SearchEngine --options=conf/engine.conf --mode=serve --port=25811

== FrontEnd ==
To run front end properly, put "WebContent" and "wiki" under localhost path,
then goto WebContent/search.js and modify
$scope.wiki_url_base to the base of wiki article 

