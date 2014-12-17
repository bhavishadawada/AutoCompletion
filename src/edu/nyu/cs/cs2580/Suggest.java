package edu.nyu.cs.cs2580;

import java.util.Arrays;

public class Suggest implements Comparable{
	double score;
	String str;
	Suggest(double score, String str){
		this.score = score;
		this.str = str;
	}
	Suggest(double score, String[] wordLs){
		this.score = score;
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < wordLs.length; i++){
			if(i > 0){
				sb.append(" ");
			}
			sb.append(wordLs[i]);
		}
		this.str = sb.toString();
	}
	public String toString(){
		return this.score + " " + this.str;
	}
	@Override
	public int compareTo(Object o) {
		return Double.compare(((Suggest)o).score , score);
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
