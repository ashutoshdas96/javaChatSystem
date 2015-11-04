import java.io.*;
import java.net.*;
import java.util.*;

public class Client
{
	public static void main(String[] args)
	{
		try
		{
			Socket s = new Socket("localhost",9014);

			DataInputStream in = new DataInputStream(s.getInputStream());
			DataOutputStream out = new DataOutputStream(s.getOutputStream());
			//BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			Scanner c = new Scanner(System.in);

			String str1="", str2="";
			str2 = in.readUTF();
			System.out.println("-->" + str2);
			while(!str1.equals(":quit"))
			{
				//str1 = br.readLine();
				str1 = c.nextLine();
				out.writeUTF(str1);
				out.flush();
			}
			out.close();
			s.close();
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}
