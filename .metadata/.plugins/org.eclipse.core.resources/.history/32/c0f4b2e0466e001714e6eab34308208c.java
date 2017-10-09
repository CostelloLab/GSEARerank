package costello.rerank;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public class RerankFileParser {
	private String file_name;
	
	public RerankFileParser(String file_name) {
		this.file_name = file_name;
	}
	
	public Map<String, String> ReadFile() {
		Map<String, String> params = new HashMap<String, String>();
		
		try {
            BufferedReader in = new BufferedReader(new FileReader(this.file_name));
            String str;
            while ((str = in.readLine()) != null) {
            	String[] ar = str.split(",");
            	if(ar.length == 2) {
            		params.put(ar[0], ar[1]);
            	}
            }
            in.close();
        } catch (IOException e) {
            System.out.println("File Read Error");
        }
 
 		return params;
	}
}
