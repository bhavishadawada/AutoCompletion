package edu.nyu.cs.cs2580;

import java.util.Arrays;

public class Suggest implements Comparable{
	double score;
	String str;
	Suggest(double score, String str){
		this.score = score;
		this.str = str;
	}
	public String toString(){
		return this.score + " " + this.str;
	}
	@Override
	public int compareTo(Object o) {
		if(score >= ((Suggest)o).score){
			return -1;
		}
		else{
			return 1;
		}
	}
	static public void main(String[] args){
		Suggest[] sgLs = new Suggest[3];
		sgLs[0] = new Suggest(1, "abc");
		sgLs[1] = new Suggest(3, "cde");
		sgLs[2] = new Suggest(2, "cde");
		Arrays.sort(sgLs);
		for(Suggest sg : sgLs){
			System.out.println(sg);
		}
	}
}
