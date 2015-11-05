import java.sql.*;
import java.net.*;
import java.io.*;
import java.util.*;


public class DataBase
{
	public static void main(String[] args)
	{
		try
		{
			System.out.println("Database Server Started");
			ServerSocket ss = new ServerSocket(9140);
			for(;;)
			{
				Socket s = ss.accept();
				System.out.println("New Connection");
				Thread t = new AuthThread(s);
				t.start();
			}
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}

class AuthThread extends Thread
{
	Socket s;

	private String user;
	private String passwd;
	private String sql;

	public AuthThread(Socket s)
	{
		this.s = s;
	}

	public void run()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
		}
		catch(Exception e)
		{
			System.out.println("Error: Unable to load driver class.");
			System.exit(1);
		}

		try
		{
			DataInputStream in = new DataInputStream(s.getInputStream());
			DataOutputStream out = new DataOutputStream(s.getOutputStream());

			Connection conn = null;
			Statement stmt = null;

			String URL = "jdbc:mysql://localhost:3306/chatAuth";
			String USER = "root";
			String PASS = "root";
			conn = DriverManager.getConnection(URL, USER, PASS);
			stmt = conn.createStatement();

			user = in.readUTF();

			sql = "SELECT userName, passwd FROM user WHERE userName = '" + user +"' ";
			ResultSet rs = stmt.executeQuery(sql);
			System.out.println("Data Feched");

			if(!rs.isBeforeFirst())
			{
				out.writeUTF("userNA");
				System.out.println("User not available");
				s.close();
				return;
			}
			else
			{
				rs.next();
				passwd = rs.getString("passwd");
				System.out.println("Psswd extracted");
				out.writeUTF("userA");
				out.writeUTF(passwd);
			}


			//if(passwd.equals(pass))
			//{
			//	System.out.println("Correct Passwd.");
			//}

			rs.close();
			stmt.close();
			conn.close();
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}
