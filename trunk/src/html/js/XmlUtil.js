/*
 * This file is part of ROOSSTER.
 * Copyright 2004, Benjamin Reitzammer <benjamin@roosster.org>
 * All rights reserved.
 *
 * ROOSSTER is free software; you can redistribute it and/or modify
 * it under the terms of the Artistic License. 
 * See http://www.opensource.org/licenses/artistic-license.php for details
 */


function w3cDate(){
  var date = new Date();
  
  var day = new String(date.getUTCDate());
  day = day.length > 1 ? day : "0"+day;
  
  var month = new String(date.getUTCMonth()+1);
  month = month.length > 1 ? month : "0" + month;
  
  var hours =date.getUTCHours();
  hours = hours.length > 1 ? hours : "0" + hours;
  
  var time = hours + ":" + date.getUTCMinutes() + ":" + date.getUTCSeconds();
  
  return date.getUTCFullYear() + "-" + month + "-" + day + "T" + time;
}
