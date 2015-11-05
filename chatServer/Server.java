import java.net.*;
import java.io.*;
import java.util.*;

public class Server
{
	public static void main(String[] args)
	{
		ArrayList<Socket> sockList = new ArrayList<Socket>();
		ArrayList<String> userList = new ArrayList<String>();

		try
		{
			System.out.println("Server Started");
			ServerSocket ss = new ServerSocket(9014);
			for(;;)
			{
				Socket s = ss.accept();
				sockList.add(s);
				System.out.println("New connection established.");
				Thread t = new ChatThread(s, sockList);
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
	ArrayList<Socket> sockList;
	public ChatThread(Socket s, ArrayList<Socket> sockList)
	{
		this.s = s;
		this.sockList = sockList;
	}
	public void run()
	{
		try
		{
			DataInputStream in = new DataInputStream(s.getInputStream());
			DataOutputStream out = new DataOutputStream(s.getOutputStream());

			String str1="";

			out.writeUTF("[ [ [   Connected to Server.   ] ] ]");
			for(;;)
			{
				str1 = in.readUTF();
				if(str1.equals(":quit"))
					break;
				broadcast("-->" + str1);
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
	public void broadcast(String str)
	{
		Iterator itr = sockList.listIterator();
		while(itr.hasNext())
		{
			try
			{
				Socket sock = (Socket)itr.next();
				DataOutputStream oBC = new DataOutputStream(sock.getOutputStream());
				oBC.writeUTF(str);
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
		}
	}
}
