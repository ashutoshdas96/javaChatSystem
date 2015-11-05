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
	private int i;
	private String user;
	private String passwd;
	private String passwdDB;
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

			Socket sDB = new Socket("localhost",9140);
			DataInputStream in = new DataInputStream(s.getInputStream());
			DataOutputStream out = new DataOutputStream(s.getOutputStream());
			DataInputStream inDB = new DataInputStream(sDB.getInputStream());
			DataOutputStream outDB = new DataOutputStream(sDB.getOutputStream());

			//Authenticating

			String str1="";

			out.writeUTF("User: ");
			user = in.readUTF();

			outDB.writeUTF(user);
			str1 = inDB.readUTF();
			System.out.println("Status from DB received.");
			if(str1.equals("userNA"))
			{
				out.writeUTF(str1);
				System.out.println("Status passed to Client. NA");
				s.close();
				sDB.close();
				System.out.println("Terminated Client");
				return;
			}
			else if(str1.equals("userA"))
			{
				out.writeUTF(str1);
				passwdDB = inDB.readUTF();
				System.out.println("Auth Detail from DB");
				for(i=2;i>=0;i--)
				{
					out.writeUTF("Password: ");
					System.out.println("waiting for user to enter passwd");
					passwd = in.readUTF();
					if(passwdDB.equals(passwd))
					{
						out.writeUTF("authSuccess");
						break;
					}
					else
					{
						out.writeUTF("Wrong password, " + i + "tries left");
					}
				}
				sDB.close();
				if(i<0)
				{
					System.out.println("Client entered wrong passwd");
					s.close();
					return;
				}
				sockList.add(s);
			}

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
