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
				Thread t = new ChatThread(s, sockList, userList);
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
	ArrayList<String> userList;
	public ChatThread(Socket s, ArrayList<Socket> sockList, ArrayList<String> userList)
	{
		this.s = s;
		this.sockList = sockList;
		this.userList = userList;
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
						out.writeUTF("Wrong password, " + i + " tries left");
					}
				}
				sDB.close();
				if(i<0)
				{
					System.out.println("Client entered wrong passwd");
					s.close();
					return;
				}
				//Client authenticated. Add to list..
				sockList.add(s);
				userList.add(user);
			}

			out.writeUTF("[ [ [        Connection established.        ] ] ]\n");
			out.writeUTF("[ [ [             ***WARNING***             ] ] ]");
			out.writeUTF("[ [ [  Unsecured Connection, not encrypted. ] ] ]\n");
			out.writeUTF("[ [ [              ***INFO***               ] ] ]");
			out.writeUTF("[ [ [     Enter ':quit' or ':q' to quit.    ] ] ]\n");
			out.writeUTF("[ [ [Enter ':help' for list of all commands.] ] ]\n");

			broadcast("+++++ " + user + " joined Server. +++++", sockList);

			String strOut;
			String cmd;

			for(;;)
			{
				str1 = in.readUTF();
				String[] res = str1.split("\\s");
				cmd = res[0];
				if(cmd.equals(":quit") || cmd.equals(":q"))
					break;
				if(cmd.equals(":help") || cmd.equals(":h"))
				{
					out.writeUTF("=== '@showlist' or '@sl'  : Show all online users.");
					out.writeUTF("=== '@connect <user1> <user2> ...': Send Private message to specified users. ");
					out.writeUTF("=== ':help'     or ':h'   : Show this list of all available command");
					out.writeUTF("=== ':quit'     or ':q'   : Disconnect from server");
					continue;
				}
				if(cmd.equals("@showlist") || cmd.equals("@sl"))
				{
					strOut = "=== Available users";
					Iterator litr = userList.listIterator();
					while(litr.hasNext())
					{
						String strU = (String)litr.next();
						strOut = strOut + ", " + strU;
					}
					strOut = strOut + ".";
					System.out.println(strOut);
					out.writeUTF(strOut);
					continue;
				}
				if(cmd.equals("@connect"))
				{
					//Create private broadcast.
					ArrayList<Socket> pList = new ArrayList<Socket>();
					int count = 0;
					strOut = user + " ";
					for(int x=1; x<res.length;x++)
					{
						strOut = strOut + ", " + res[x];
						Iterator litr = userList.listIterator();
						count = 0;
						while(litr.hasNext())
						{
						//	System.out.println(res[x]);
							String strU = (String)litr.next();
							if(strU.equals(res[x]))
								pList.add((Socket)sockList.get(count));
							count++;
						}
					}
					out.writeUTF("[   Waiting for users to join Private chat.    ]");
					out.writeUTF("[ Enter ':leave' or ':l' to leave private chat.]");
					//requestPrivate(pList);
					broadcast("*** === " + user + " want to have private chat with " + strOut, pList);
					broadcast("*** === Enter '@connect <user1> <user2> ...' to reply to one or more ", pList);

					pList.add(s);
					for(;;)
					{
						str1 = in.readUTF();
						if(str1.equals(":l") || str1.equals(":leave"))
						{
							out.writeUTF("*** ----- " + user + " left private chat");
							break;
						}
						broadcast("***" + user + "\t~" + str1, pList);
					}
				}

				broadcast("--" + user + "\t-->"  + str1, sockList);
				System.out.println("--" + user + "\t-->" +  str1);
			}
			System.out.println("Client Disconnected");
			//Solve me . . .
			//If arraylist is really a list, then remove the user from that.
			broadcast("----- " + user + " left server. -----", sockList);
			in.close();
			out.close();
			s.close();
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
	public void broadcast(String str, ArrayList<Socket> sockList)
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
