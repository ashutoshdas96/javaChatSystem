import java.net.*;
import java.io.*;
import java.util.*;

public class Server
{
	public static void main(String[] args)
	{
		try
		{
			System.out.println("Server Started");
			ServerSocket ss = new ServerSocket(9014);
			for(;;)
			{
				Socket s = ss.accept();
				System.out.println("New connection established.");
				Thread t = new ChatThread(s);
				t.start();
			}
			//ss.close();
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}

class ChatThread extends Thread
{
	Socket s;
	public ChatThread(Socket s)
	{
		this.s = s;
	}
	public void run()
	{
		try
		{
			DataInputStream in = new DataInputStream(s.getInputStream());
			DataOutputStream out = new DataOutputStream(s.getOutputStream());

			String str1="";

			out.writeUTF("Connected to Server.");
			for(;;)
			{
				str1 = in.readUTF();
				if(str1.equals(":quit"))
					break;
				System.out.println("-->" + str1);

			}
			System.out.println("Client Disconnected");
			in.close();
			out.close();
			s.close();
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}
