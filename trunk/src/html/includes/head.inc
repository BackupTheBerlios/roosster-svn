#parse( 'includes/macros.inc' ) 
#set( $searchFieldDefault = 'search ...') 
#set( $queryString = $req.getParameter("query") )
#if ( $queryString )
  #set( $searchField = $queryString) 
#else
  #set( $searchField = $searchFieldDefault)
#end
<html>
<head>
  <title>roosster - personal search engine</title>
  <style type="text/css"> @import url($baseurl/styles.css); </style>
</head>
<body>
  <div id="roosster">
    <div id="headernote">
      <form action="$baseurl/application/search" method="get">
          <input type="text" id="query" name="query" value="$searchField" 
                 onfocus="if(this.value=='$searchFieldDefault') this.value='';" />
      </form>
    </div>
    <span class="roossterheading">roosster - personal search engine</span>
  </div>

