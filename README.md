# Httippi

## Samples

	public class Example {
	
		public static void main(String[] args) {
		
			Httippi client = new Httippi();
			try {
				// get
				String response = client.url(new URL("http://localhost:9000/"))
				     			    .get();			
				
				//same url but post http verb			
				String responsePost = client.url().post(new String("sample body"));
							
				// new url and post http verb
				String responsePost2 = client.url(new URL("http://localhost:9000/postForm?param1=test&param2=test"))
									     .post(new String("sample body"));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
	
	}
