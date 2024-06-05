package org.example.layout;

import org.springframework.boot.loader.tools.Layouts.Jar;

public class CustmLayout extends Jar {

	@Override
	public String getLauncherClassName() {
		return "org.springframework.boot.loader.ExtLibStarter";
	}

}
