<div id="entrylist-pager">
  #set ( $queryString = $req.getParameter("query") )
      
  #if ( $entryList.offset  <= 0)  #set( $offset = 0 )
  #else #set( $offset = $entryList.offset )
  #end
  
  #if ( $entryList.limit  <= 0)  #set( $limit = 0 )
  #else #set( $limit = $entryList.limit )
  #end
  
  #set( $nextOffset = $offset + $limit )
  
  #if ( $offset - $limit  < 0 )  #set( $prevOffset = 0 )
  #else #set( $prevOffset = $offset - $limit )
  #end
  
  #if ( $entryList.size() > 0 )
    <table style="width:100%;">
      <tr>
        <td class="tdalignleft">
          Showing results $offset - 
          #if ( $nextOffset > $entryList.totalSize ) $entryList.totalSize #else $nextOffset #end 
          of $entryList.totalSize
        </td>
        <td class="tdalignright">
    #if ( $entryList.size() < $entryList.totalSize )
        <a #if ( $offset < 1 ) href="#" class="inactive-link" 
           #else href="$baseurl/application/search?query=$queryString&output.offset=$prevOffset&output.limit=$limit" 
           #end>previous</a> |
        <a #if ( $nextOffset > $entryList.totalSize )href="#" class="inactive-link" 
           #else href="$baseurl/application/search?query=$queryString&output.offset=$nextOffset&output.limit=$limit"
           #end>next</a>
    #end
        </td>    
      </tr>
    </table>
  #else
    No results available for this search: <strong>'$!queryString'</strong>
  #end
</div>
