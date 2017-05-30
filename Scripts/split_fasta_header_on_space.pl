#! /usr/bin/perl
#Written by Tom de Man

use strict;

my $fasta = shift;

open FA, "$fasta" || die "cannot open $fasta for reading";
open OUT, ">$fasta.spacelessHeader.fasta";

while (<FA>) {
	chomp;
	if ((/^>/)) { 
		chomp;
		my @split_header = split (" ", $_);
		print OUT "$split_header[0]"."_"."$split_header[-2]"."_"."$split_header[-1]\n";
	} else {
		print OUT "$_\n";
	}
}