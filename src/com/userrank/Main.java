package com.userrank;

import com.userrank.UserRank;

public class Main {
	public static void main(String[] args)   {
		System.out.println("main");
		UserRank ur = new UserRank();
		try {
			ur.execute();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

}