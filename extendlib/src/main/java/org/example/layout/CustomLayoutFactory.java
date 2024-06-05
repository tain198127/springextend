package org.example.layout;

import org.springframework.boot.loader.tools.Layout;
import org.springframework.boot.loader.tools.LayoutFactory;

import java.io.File;
import java.util.Locale;

public class CustomLayoutFactory implements LayoutFactory {

	@Override
	public Layout getLayout(File file) {
		if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }
		String lowerCaseFileName = file.getName().toLowerCase(Locale.ENGLISH);
		return new CustmLayout();
	}

}
