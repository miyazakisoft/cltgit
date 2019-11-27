package com.github.pony;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;

public class Badge {
	String ownerName;
	String repositoryName;
	String branchName;
	String setToBadgePath;

	Badge(String ownerName, String repositoryName, String branchName) {
		this.ownerName = ownerName;
		this.repositoryName = repositoryName;
		this.branchName = branchName;
		this.setToBadgePath = Paths.get(new Web(ownerName, repositoryName).targetPath(), branchName + "-badge.svg")
				.toString();
	}

	Badge(String ownerName, String repositoryName) {
		this(ownerName, repositoryName, "master");
	}

	public void perform() throws IOException {

		Double score = Double.parseDouble(DataBase.getRecentContent(ownerName, repositoryName, branchName, "score"));

		String color = "red";

		if (score == 100.0) {
			color = "brightgreen";

		} else if (score >= 98.0) {
			color = "green";

		} else if (score >= 95.0) {
			color = "yellowgreen";

		} else if (score >= 90.0) {
			color = "yellow";

		} else if (score >= 85.0) {
			color = "orange";

		}

		System.out.println("遵守率: " + String.format("%.1f", score));

		URL url = new URL("https://img.shields.io/badge/compliance-" + score + "%25-" + color
				+ ".svg?logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB4AAAAfCAMAAADHso01AAAABGdBTUEAALGPC/"
				+ "xhBQAAAAFzUkdCAK7OHOkAAAFBUExURUdwTDWbvx+WvyaZwCCXvx2WviGYvx2Wvh+WviyawCGXvyubwCqZvyeawCCWvy"
				+ "KYwB+Wvh6WviGYvx+WvzudwCOYwB6WviKYwB6WviaZwCubwTSdwiKYwCOXvyGYvySZwCSZwSGYwCmawSCXvyCXvyGXvy"
				+ "mZwB+XvyKYvyGXvySYwCCXvx+WviGXv////xqUvRyVvR2VvSCXvjiixv7+//z+/vD4+ySZwCmbwYzI3RmTvPT6/Pn9/u"
				+ "n0+NLq8e32+YPE20yry8Hi7R2Wvk+szDOfxNru9DCew7bd6qXU5W661T+lxx2Vvlyx0JjO4Viwzsrm78/o8He+2KDS5C"
				+ "2cw5LL3/f7/ZrP4mG00X7C2sfl793v9Ge303S81qzX50ipyrDZ6JDK31OuzWO10uXy98Xj7t3v9eDw9UanyZXN4ESnyG"
				+ "v9MTYAAAAudFJOUwAJ5Di/+ZP89Q1bFxE/xHLi1a/nA0/sn/JFLgaDL4uIY5wi2Xq1H7ZJpCeiz7c88OLGAAACFElEQV"
				+ "Qoz22T13riQAyFTbDpnVCTTdveLM0YYzAl9N57SzZt6/s/wBqMMbB7riz9ljTf6AzD6DLcRGy2QNTB/EenUbfPG+S4sI"
				+ "e9iFiPadTkKSKALAMgCb8NGPah4ep1CyDx53kwaJSqRSBh0zudOi4siMK8kJmIsXQ8Oy1RKLKvNGo1cSjdZUVeU6YjAH"
				+ "Ft+ekbDkfTFL+v73kk79X+H/w4qov8ofp5MF+eKdT+ldBkmj9WQUC/U8E2C3QzaipeS+aGFfVbfKQkZGCsPkw8qJnlSp"
				+ "Jl0nxQB/38Bh4jY/TAk1ocXwFWBQpCbxPGkkXunHFzJBlTpyWgtFjOKczV8kUVWeYapZrae0pph+ezZbkd38QvSneGxX"
				+ "JfxUNKkusaeabi1AzCjBeE7Vn7AgjDWptowyYNMO/h9G9JblHEcm8br7Fr15yf3M/yAsG7iRqmnsHChDBR213VpPALmt"
				+ "rfL3nlaG6u+LjDP7ogDWPboFdGF+P0QPdWW+QK6Xy3ujE1XzIOn969kGg1Mrze+0TZyZUF2tvyeGe8o2KHko+KJe0skc"
				+ "biPwtdCBiMrO0QCGL1/phXStAybex6ds5BuT45NMsTEvZma9RrMyTmSx3G600gX4yaVe2KVUkz2btVRoipSr0rAXEZda"
				+ "Nb3R4COCo1crlBW6BALJ8/HbwwY8hPEGAtJBbWdvzKDE6TlyOKzCc+m13L/gUgBJNSwlgnewAAAABJRU5ErkJggg=="); // ダウンロードする
																													// URL
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("User-agent", "Mozilla/5.0");
		InputStream in = conn.getInputStream();

		File file = new File(setToBadgePath); // 保存先
		FileOutputStream out = new FileOutputStream(file, false);
		int b;
		while ((b = in.read()) != -1) {
			out.write(b);
		}

		out.close();
		in.close();

	}

}
