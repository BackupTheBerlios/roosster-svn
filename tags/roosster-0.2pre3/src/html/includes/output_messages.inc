#if ( $messages )
  #if ( $req.getAttribute($REQ_OUTPUT_MSG) ) 
    #set ( $tmp = $messages.add($req.getAttribute($REQ_OUTPUT_MSG)) ) 
  #end
#else
  #if ( $req.getAttribute($REQ_OUTPUT_MSG) ) 
    #set ( $messages = [ $req.getAttribute($REQ_OUTPUT_MSG) ] )
  #else
    #set ( $messages = [] )
  #end
#end
#if ( $messages.size() > 0 )
<div id="output-messages">
  <ul>
    #foreach ( $level_n_msg in $messages)
      #if( $level_n_msg.get(0) == 'info')<li class="info">$level_n_msg.get(1)</li>#end
    #end
  </ul>
  <ul>
    #foreach ( $level_n_msg in $messages)
      #if( $level_n_msg.get(0) == 'error')<li class="error">$level_n_msg.get(1)</li>#end
    #end
  </ul>
</div>
#end
