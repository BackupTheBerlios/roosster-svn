#parse( 'includes/macros.inc' ) 

#set( $LAST_QUERY = 'session.last_query_attr' ) 
#set( $REQ_OUTPUT_MSG = 'http.request.outputmessages') 
#set( $DEF_SEARCH_FIELD = 'search ...') 
#set( $session = $req.getSession() ) 
#set( $lastQuery = $session.getAttribute($LAST_QUERY) ) 
#set( $requestURL = $req.getRequestURL().toString() )
#set( $queryString = $req.getParameter("query") )
#if ( $queryString )
  #set( $searchField = $queryString )
  $session.setAttribute($LAST_QUERY, $queryString)
#else
  #set( $searchField = $DEF_SEARCH_FIELD)
#end
<html>
<head>
  <title>roosster - personal search engine</title>
  <style type="text/css"> @import url($baseurl/styles.css); </style>
  <script type="text/javascript" src="$baseurl/js/core.js" ></script>
</head>
<body>
  <div id="roosster">
    <div id="headernote">
      <form action="$baseurl/application/search" method="get" enctype="application/x-www-form-urlencoded">
          <input type="text" id="query" name="query" value="$!searchField" 
                 onfocus="if(this.value=='$DEF_SEARCH_FIELD') this.value='';" />
      </form>
    </div>
    <span class="roossterheading">roosster - personal search engine</span>
    <ul id="tablist">
      <li #if($requestURL == "$baseurl/index.html")class="active"#end> 
        <a href="$baseurl/index.html">Home</a> 
      </li>
      <li #if($commandName == 'search')class="active"#end> 
        <a href="$baseurl/application/search?#if ( $lastQuery )query=$lastQuery#end">Search</a> 
      </li>
      <li #if($requestURL == "$baseurl/add.html")class="active"#end> 
        <a href="$baseurl/add.html">Add</a> 
      </li>
      <li #if($commandName == 'tags')class="active"#end> 
        <a href="$baseurl/application/tags">Tags</a> 
      </li>
      <li #if($requestURL == "$baseurl/tools.html")class="active"#end> 
        <a href="$baseurl/tools.html">Tools</a> 
      </li>
    </ul>
  </div>
  <div id="content">
  #parse('includes/output_messages.inc')


