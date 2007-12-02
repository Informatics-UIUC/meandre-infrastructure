#!/usr/bin/perl -w

#
# This scripts allows to do massive bulk upload of descriptors
# 
# @author Xavier Llora
#
#

if ( $#ARGV!=6 ) {
	print "Wrong syntax\n";
	print "\tbulk-uploader.pl <RDF|TTL|N-TRIPLE> <descriptors_dir> <jars_dir> <host> <port> <user> <password>\n";
} 
else {
	# Vars
	my $format   = $ARGV[0];
	my $descDir  = $ARGV[1];
	my $jarDir   = $ARGV[2];
	my $host     = $ARGV[3];
	my $port     = $ARGV[4];
	my $user     = $ARGV[5];
	my $password = $ARGV[6];
	
	# Preparations
	my $formatExt = "";
	$descDir =~ s/\/$//;
	$jarDir =~ s/\/$//;
	
	# Check format
	if ( !($format eq"RDF" or $format eq "TTL" or $format eq "N-TRIPLE")  ) {
		print "Unknow descriptor format $format\n";
		print "\tAllowed descriptorformats: RDF, TTL, or N-TRIPLE\n";
	}
	elsif ( $format eq "RDF" ) {
		$formatExt = "rdf";
	}
	elsif ( $format eq "TTL" ) {
		$formatExt = "ttl";
	}
	elsif ( $format eq "N-TRIPLE" ) {
		$formatExt = "nt";
	}
	
	# Assemble the upload url
	my $postURL = "http://$user:$password\@$host:$port/services/repository/add.$formatExt";
		
	print "Uploading to: $postURL\n\n";
	
	# List the descriptors to upload
	my @descriptors = `find $descDir | grep $formatExt\$`;
	chomp @descriptors;
	print "Descriptors to upload: ".@descriptors."\n";
	for my $desc ( @descriptors ) {
		print "\t$desc\n";
	}
	print "\n";

	# List the jars to attach
	my $jarContext = "";
	my @jars = `find $jarDir | grep jar\$`;
	chomp @jars;
	print "Context jars: ".@jars."\n";
	for my $jar ( @jars ) {
		print "\t$jar\n";
		$jarContext .= "-F jar=\@$jar ";
	}
	print "\n";
	
	# Upload
	for my $desc ( @descriptors ) {
		my $upload = "curl -F repository=\@$desc $jarContext $postURL";
		my $out = `$upload`;
		print "$out";
	}
	
}

1;