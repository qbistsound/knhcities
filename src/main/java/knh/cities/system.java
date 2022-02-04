package knh.cities;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.Reader;
import java.net.URL;
import java.io.StringReader;
import java.util.List;
import java.util.TimeZone;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class system 
{
	/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	private static int count;
	private static String root; 
	private static String dimg;
	/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	private static final int thread_sz = 32;
	/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	private static final String name = "K&N (CITIES)";
	/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	public static final config config = new config();
	public static final console console = new console();
	public static final database database = new database();
	/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	public static void init(String config_file)
	{
		system.config.init(config_file);
		system.database.init();
	}
	/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	public static Boolean isnum(String text) { try { Integer.parseInt(text); return true; } catch (Exception exception) { return false; } }
	/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	public static String cpath(String path, String child) { return String.format("%s%s%s", path, File.separator, child); }
	/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	public static String rpath(String path)
	{
		List<String> blocks = new ArrayList<String>(Arrays.asList(path.split(File.separator)));
		blocks.remove((blocks.size() - 1));
		return String.join(File.separator, blocks);
	}
	/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	static class config
	{
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		private JSONObject doc = new JSONObject();
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		public int integer(String name) { try { return (!this.doc.has(name)) ? -1 : this.doc.getInt(name); } catch (Exception exception) { return -1; } }
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		public String string(String name) { try { return (!this.doc.has(name)) ? "" : this.doc.getString(name); } catch (Exception exception) { return ""; } }
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		public Boolean bool(String name) { try { return (!this.doc.has(name)) ? false : this.doc.getBoolean(name); } catch (Exception exception) { return false; } }
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		public void init(String path)
		{
			try 
			{ 
				this.doc = new JSONObject(new JSONTokener(new String(Files.readAllBytes(Paths.get(path)))));
				system.console.write(String.format("Using config file [%s]", path));
				system.root = system.rpath(path);
				system.console.write(String.format("Using class root [%s]", system.root));
				system.dimg = Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(system.cpath(system.root, "default-image.png"))));
			}
			catch (Exception exception) { exception.printStackTrace(); }
		}
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	};
	/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	private static class console
	{
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		private int len = 0;
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		public void print(String message) { System.out.print(message); }
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		public void write(String message) { System.out.println(String.format("[%s] %s", system.name, message)); }
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		public void clear() { if (this.len > 0) { this.print(String.format("\r%s\r", new String(new char[this.len]).replace("\0", Character.toString((char) 12288)))); } }
		
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		public void percent(long index, long size)
		{
			String[] blocks = new String[100];
			double pointer_t = (100d / size) * (index + 1);
			for (int block = 0; block < blocks.length; block++) { blocks[block] = (block < pointer_t) ? Character.toString((char) 9608) : Character.toString((char) 9617); }
			String text = String.format("(%d - %d) - %s", (index + 1), size, String.join("", blocks));
			this.clear();
			this.len = text.length();
			this.print(text);
		}
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	};
	/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	static class database
	{
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		private List<Connection> ready;
		private List<Connection> active;
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		private long rdx = 0L;
		private long rsz = 0L;
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		private String namespace = "";
		private String table = "cities";
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		private int size = 1;
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		public String ns() { return (this.namespace.isEmpty()) ? this.table : String.format("%s.%s", this.namespace, this.table); }
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		private String[] omit = new String[] { "{", "}", "(", ")", "&", ";", "~", "[", "]" };
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		public void rename(int id, String name)
		{
			Connection connection = this.request();
			try
			{
				String name_t = name.replaceAll("<[^>]*>", "").trim();
				for (int index = 0; index < this.omit.length; index++) { while (name_t.contains(this.omit[index])) { name_t = name_t.replace(this.omit[index], "_"); } }
		        //
		        PreparedStatement command = connection.prepareStatement(String.format("UPDATE %s SET name=? WHERE id=?", this.ns()));
		        command.setString(1, name_t);
				command.setInt(2, id);
				command.execute();
				command.close();
				this.release(connection);
			}
			catch (Exception exception) 
			{
				exception.printStackTrace();
				this.release(connection);
			}
		}
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		public void image(int id, byte[] bytes)
		{
			Connection connection = this.request();
			try
			{
				InputStream stream = new ByteArrayInputStream(bytes);
		        BufferedImage image = ImageIO.read(stream);
		        ByteArrayOutputStream baos = new ByteArrayOutputStream();
		        ImageIO.write(image, "png", baos);
		        String image_t = Base64.getEncoder().encodeToString(baos.toByteArray());
		        //
		        
		        PreparedStatement command = connection.prepareStatement(String.format("UPDATE %s SET data=? WHERE id=?", this.ns()));
		        command.setString(1, image_t);
				command.setInt(2, id);
				command.execute();
				command.close();
				this.release(connection);
			}
			catch (Exception exception) 
			{
				exception.printStackTrace();
				this.release(connection);
			}
		}
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		public JSONObject search(String string, int size, Boolean editable)
		{
			JSONObject document = new JSONObject();
			Connection connection = this.request();
			try
			{
				String limit = (size > 0) ? String.format(" LIMIT %d", size) : "";
				PreparedStatement command = connection.prepareStatement(String.format("SELECT * FROM %s WHERE name LIKE ?%s", this.ns(), limit));
				command.setString(1, ("%" + string + "%"));
				//
				ResultSet records = command.executeQuery();
				JSONArray elements = this.document(records);
				records.close();
				command.close();
				this.release(connection);
				//add counts
				document.put("elements", elements);
				document.put("count", system.count);
				document.put("editable", editable);
				document.put("paginate", false);
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
				this.release(connection);
			}
			return document;
		}
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		public JSONObject records(int bof, int size, Boolean editable)
		{
			JSONObject document = new JSONObject();
			Connection connection = this.request();
			try
			{
				
				String limit = (size > 0) ? String.format(" LIMIT %d", size) : "";
				String query = String.format("SELECT * FROM %s WHERE id >= %d ORDER BY id ASC%s", this.ns(), bof, limit);
				
				PreparedStatement command = connection.prepareStatement(query);
				//
				command.setFetchSize(100);
				ResultSet records = command.executeQuery();
				JSONArray elements = this.document(records);
				records.close();
				command.close();
				this.release(connection);
				//add counts
				document.put("elements", elements);
				document.put("count", system.count);
				document.put("editable", editable);
				document.put("paginate", true);
			}
			catch (Exception exception) 
			{
				this.release(connection);
				exception.printStackTrace(); 
			}
			return document;
		}
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		public void init()
		{
			this.ready = new ArrayList<Connection>();
			this.active = new ArrayList<Connection>();
			this.size = system.config.integer("database_connections");
			this.namespace = system.config.string("database_namespace");
			String connection_url = system.config.string("database_url");
			String connection_user = system.config.string("database_username");
			String connection_pass = system.config.string("database_password");
			try { for (int index = 0; index < this.size; index++) { this.ready.add(DriverManager.getConnection(connection_url, connection_user, connection_pass)); } }
			catch (Exception exception) { exception.printStackTrace(); }
			//
			if (!this.exists("cities")) { this.create(); }
			this.count();
		}
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		public Boolean exists(String table)
		{
			try
			{
				Connection connection = this.request();
				DatabaseMetaData meta_data = connection.getMetaData();
				ResultSet tables = meta_data.getTables(null, null, table, new String[] {"TABLE"}); 
				Boolean is_table = (tables.next()) ? true : false;
				this.release(connection);
				return is_table;
			}
			catch (Exception exception) { exception.printStackTrace(); }
			return false;
		}
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		//PRIVATE
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		private JSONArray document(ResultSet records)
		{
			JSONArray document = new JSONArray();
			try
			{
				while (records.next())
				{
					JSONObject element = new JSONObject();
					String image = (records.getString(4).equals("*")) ? system.dimg : records.getString(4);  
					element.put("id", records.getInt(1));
					element.put("name", records.getString(2));
					element.put("time", records.getLong(3));
					element.put("image", image);
					document.put(element);	
				
				}
			}
			catch (Exception exception) { exception.printStackTrace(); }
			return document;
		}
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		private void count()
		{
			try 
			{
				Connection connection = this.request();
				PreparedStatement command = connection.prepareStatement(String.format("SELECT COUNT(id) FROM %s", this.ns()));
				command.setFetchSize(100);
				ResultSet records = command.executeQuery();
				while (records.next()) { system.count = records.getInt(1); }
				records.close();
				command.close();
				this.release(connection);
			}
			catch (Exception exception) { exception.printStackTrace(); }
		}
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		private void create()
		{
			//create table
			try 
			{
				Connection connection = this.request();
				String query_string = String.format("CREATE TABLE %s (id BIGINT PRIMARY KEY, name VARCHAR(1024), time BIGINT, data TEXT)", this.ns());
				PreparedStatement command = connection.prepareStatement(query_string);
				command.execute();
				command.close();
				this.release(connection);
			}
			catch (Exception exception) { exception.printStackTrace(); }
			//initilize records
			this.rsz = 0L;
			String path = system.cpath(system.root, "cities.csv");
			system.console.write(String.format("Initilizing database with record-set (%s)", path));
			try
			{
				CSVFormat format = CSVFormat.DEFAULT;
				system.console.write(String.format("Reading document blocks (%s)", path));
				Reader reader = new StringReader(new String(Files.readAllBytes(Paths.get(path))));
				Iterable<CSVRecord> records = format.parse(reader);
				for (CSVRecord n : records) { this.rsz++; n.get(0); }
			}
			catch (Exception exception) { exception.printStackTrace(); }
			//import records
			try
			{
				this.rdx = -1L;
				CSVFormat format = CSVFormat.DEFAULT;
				List<Thread> threads = new ArrayList<Thread>();
				system.console.write(String.format("Importing [%d] records from (%s)", this.rsz, path));
				Reader reader = new StringReader(new String(Files.readAllBytes(Paths.get(path))));
				Iterable<CSVRecord> records = format.parse(reader);
				for (CSVRecord n : records) 
				{ 
					String id = n.get(0);
					String name = n.get(1);
					String urld = n.get(2);
					
					if (!system.isnum(id)) { this.next(); continue; }
					threads.add(new Thread() { public void run() { system.database.insert(Integer.valueOf(id).intValue(), name, urld); }});
					threads.get(threads.size() - 1).start();
					if (threads.size() >= system.thread_sz) { this.clrt(threads); }
				}
				system.console.clear();
				if (threads.size() > 0) { this.clrt(threads); }
			}
			catch (Exception exception) { exception.printStackTrace(); }
		}
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		private void clrt(List<Thread> threads)
		{
			try
			{
				for (Thread t : threads) { t.join(); }
				threads.clear();
			}
			catch (Exception exception) { exception.printStackTrace(); }
		}
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		private void insert(int id, String name, String urld)
		{
			try
			{
				String data = "*";
				try
				{
					BufferedImage image = ImageIO.read(new URL(urld));
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ImageIO.write(image, "png", baos);
					data = Base64.getEncoder().encodeToString(baos.toByteArray());
					Thread.sleep(1000);
				}
				catch (IIOException exception) 
				{
//					exception.printStackTrace();
					data = "*"; 
				}
				//record
				Connection connection = this.request();
				PreparedStatement command = connection.prepareStatement(String.format("INSERT INTO %s VALUES(?,?,?,?)", this.ns()));
				command.setInt(1, id);
				command.setString(2, name);
				command.setLong(3, Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
				command.setString(4, data);
				command.execute();
				command.close();
				this.release(connection);
				this.next();
			}
			catch (Exception exception) { exception.printStackTrace(); }
		}
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		private synchronized void next()
		{
			this.rdx++;
			system.console.percent(this.rdx, this.rsz);
		}
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		private void release(Connection connection)
		{
			this.ready.add(connection);
	        this.active.remove(connection);
		}
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		private Connection request()
		{
			if (this.ready.isEmpty())
			{
				try { Thread.sleep(300); } catch (Exception e) { }
				return this.request();
			}
			//
			Connection connection = this.ready.remove((this.ready.size() - 1));
			this.active.add(connection);
			return connection;
		}
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	};
	/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
}
