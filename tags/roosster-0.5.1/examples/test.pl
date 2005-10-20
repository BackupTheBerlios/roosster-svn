#!/usr/bin/perl

use IO::All;
use LWP::UserAgent;
$ua = LWP::UserAgent->new;

io('search.xml') > $contents;
 
my $req = HTTP::Request->new(POST => 'http://localhost:8282/roosster/api/entry?fetch.content=false');
$req->content($contents);

my $res = $ua->request($req);
print $res->as_string;

