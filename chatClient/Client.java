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

			Console cons;
			char[] passwd;

			DataInputStream in = new DataInputStream(s.getInputStream());
			DataOutputStream out = new DataOutputStream(s.getOutputStream());
			//BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			Scanner c = new Scanner(System.in);

			String str1="", str2="", user="";
			int i=0;
			System.out.print("User: ");
			user = c.nextLine();
			out.writeUTF(user);
			//System.out.println(" ");

			str2 = in.readUTF();
			//System.out.println("Test " + str2);
			if(str2.equals("userNA"))
			{
				System.out.println("Invalid user");
				s.close();
				return;
			}
			else
			{
				for(i=2;i>=0;i--)
				{
					//Password.
					//Don't echo password, zero array to minimize lifetime in memory
					if((cons = System.console()) != null &&
						(passwd = cons.readPassword("%s", "Password: ")) != null)
					{
						out.writeUTF(String.valueOf(passwd));
						java.util.Arrays.fill(passwd, ' ');
					}
					System.out.println(" ");
					str1 = in.readUTF();
					if(str1.equals("authSuccess"))
					{
						System.out.println("Authenticated.");
						break;
					}
					else
					{
						System.out.println(str1);
					}
				}
				if(i<0)
				{
					System.out.println("3 Unsuccessful attempts.");
					System.out.println("Disconnecting...");
					s.close();
					return;
				}
			}

			Thread r = new recvMsg(s);
			r.start();
			str1 = "";
			while(!(str1.equals(":quit") || str1.equals(":q")))
			{
				//str1 = br.readLine();
				str1 = c.nextLine();
				/*
				 *erase whole line: \33[2K
				 * and for move cursor
				 * to previous line: \33[1A
				 *
				*/
				System.out.print("\33[1A\33[2K");
				if(!(str1.isEmpty()))
				{
					out.writeUTF(str1);
					out.flush();
				}
			}
			r.interrupt();
			out.close();
			s.close();
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}

class recvMsg extends Thread
{
	Socket s;
	public recvMsg(Socket s)
	{
		this.s = s;
	}
	public void run()
	{
		try
		{
			DataInputStream inp = new DataInputStream(s.getInputStream());

			while(!isInterrupted())
			{
				String str1 = inp.readUTF();
				System.out.println(str1);
			}
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}
