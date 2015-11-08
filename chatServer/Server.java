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
	public static final String ANSI_RESET = "\u001B[0m";
	//public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	//public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	//public static final String ANSI_CYAN = "\u001B[36m";
	//public static final String ANSI_WHITE = "\u001B[37m";

	private int i;
	private String user;
	private char[] passwd;
	private char[] passwdDB;
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
				passwdDB = (inDB.readUTF()).toCharArray();
				System.out.println("Auth Detail from DB");
				for(i=2;i>=0;i--)
				{
					System.out.println("waiting for user to enter passwd");
					passwd = (in.readUTF()).toCharArray();
					if(Arrays.equals(passwdDB, passwd))
					{
						out.writeUTF("authSuccess");
						java.util.Arrays.fill(passwd, ' ');
						java.util.Arrays.fill(passwdDB, ' ');
						break;
					}
					else
					{
						out.writeUTF(ANSI_RED + "Wrong password, " + i + " tries left" + ANSI_RESET);
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

			out.writeUTF(ANSI_PURPLE + "[ [ [        Connection established.        ] ] ]\n");
			out.writeUTF("[ [ [             ***WARNING***             ] ] ]");
			out.writeUTF("[ [ [  Unsecured Connection, not encrypted. ] ] ]\n");
			out.writeUTF("[ [ [              ***INFO***               ] ] ]");
			out.writeUTF("[ [ [     Enter ':quit' or ':q' to quit.    ] ] ]\n");
			out.writeUTF("[ [ [Enter ':help' for list of all commands.] ] ]\n" + ANSI_RESET);

			broadcast(ANSI_GREEN + "+++++ " + user + " joined Server. +++++" + ANSI_RESET, sockList);

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
					out.writeUTF(ANSI_PURPLE + "[   Waiting for users to join Private chat.    ]");
					out.writeUTF("[ Enter ':leave' or ':l' to leave private chat.]" + ANSI_RESET);
					//requestPrivate(pList);
					broadcast(ANSI_PURPLE + "*** === " + user + " want to have private chat with " + strOut, pList);
					broadcast("*** === Enter '@connect <user1> <user2> ...' to reply to one or more " + ANSI_RESET, pList);

					pList.add(s);
					for(;;)
					{
						str1 = in.readUTF();
						if(str1.equals(":l") || str1.equals(":leave"))
						{
							out.writeUTF(ANSI_RED + "*** ----- You left private chat, Now the msg will be Broadcasted to all ----- ***" + ANSI_RESET);
							break;
						}
						broadcast(ANSI_BLUE + "***" + user + "\t~ " + ANSI_RESET + str1, pList);
					}
				}
				if(str1.equals(":l") || str1.equals(":leave"))
				{
					continue;
				}

				broadcast(ANSI_BLUE + "-- " + user + "\t--> " + ANSI_RESET + str1, sockList);
				System.out.println("-- " + user + "\t-->" +  str1);
			}
			System.out.println("Client Disconnected");
			//Solve me . . .
			//If arraylist is really a list, then remove the user from that.
			broadcast(ANSI_RED + "----- " + user + " left server. -----" + ANSI_RESET , sockList);
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
