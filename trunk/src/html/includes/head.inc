#parse( 'macros.inc' ) 
#set( $searchFieldDefault = 'search ...') 
<html>
<head>
  <title>roosster - personal search engine</title>
  <style type="text/css"> @import url($baseurl/styles.css); </style>
</head>
<body>
  <div id="roosster">
    <div id="headernote">
      <form >
          <input type="text" id="headerQueryStr" name="headerQueryStr" value="$searchFieldDefault" 
                 onfocus="if(this.value=='$searchFieldDefault') this.value='';" />
      </form>
    </div>
    <span class="roossterheading">roosster - personal search engine</span>
  </div>

