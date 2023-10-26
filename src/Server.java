import java.awt.BorderLayout;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JFrame;

public class Server extends JFrame {
	
	final static String secretKey = "secreatKeysecreatKeysecreatKeyll";
	static String IV = secretKey.substring(0,16);
	Panel p = new Panel();
	TextArea ta = new TextArea();
	TextField tf = new TextField();
	HashMap client;	
	ArrayList<String> list = new ArrayList<String>();
	
	public String AES_Decode(String str) throws IllegalBlockSizeException, BadPaddingException,
	UnsupportedEncodingException, InvalidKeyException, InvalidAlgorithmParameterException,
	NoSuchAlgorithmException, NoSuchPaddingException {
		
		byte[] keyData = secretKey.getBytes();
		
		SecretKey AES_Key = new SecretKeySpec(keyData, "AES");
		
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		c.init(Cipher.DECRYPT_MODE, AES_Key, new IvParameterSpec(IV.getBytes("UTF-8")));
		
		byte[] bytestr = Base64.getDecoder().decode(str.getBytes("UTF-8"));
		
		return new String(c.doFinal(bytestr), "UTF-8");
	}
	
	public String AES_Encode(String str) throws NoSuchAlgorithmException, NoSuchPaddingException,
	InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException,
	BadPaddingException, UnsupportedEncodingException {
		byte[] keydata = secretKey.getBytes();
		
		SecretKey AES_Key = new  SecretKeySpec(keydata, "AES");
		
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		c.init(Cipher.ENCRYPT_MODE, AES_Key, new IvParameterSpec(IV.getBytes()));
		
		byte[] encrypt = c.doFinal(str.getBytes("UTF-8"));
		
		return new String(Base64.getEncoder().encode(encrypt));
	}
	
	public Server() {
		super("Chatting");
		
		client = new HashMap();
		Collections.synchronizedMap(client);
		 
		setSize(300,300);
		
		add(ta, "Center");
		
		p.setLayout(new BorderLayout());
		add(p, "South");
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});		
		p.add(tf);
		ta.setEditable(false);
		setVisible(true);
	}
	
	public void start() {
		ServerSocket serversocket;
		Socket socket;
		try {
			 serversocket = new ServerSocket(7777);
			 ta.append("서버가 준비되었습니다\n");
			 
			 while(true) {
				 socket = serversocket.accept();
				 Reception reception = new Reception(socket);
				 reception.start();				 
			 }
		 }catch(Exception e) {
			 e.printStackTrace();
		 }
	}
	public static void main(String[] args) {
		 
		Server server = new Server();
		 server.start();
		
	}
	
	class Reception extends Thread{
		DataInputStream in;
		DataOutputStream dos;
		Socket socket;
		
		Reception(Socket socket){
			this.socket = socket;
			try {
				in = new DataInputStream(socket.getInputStream());
				dos = new DataOutputStream(socket.getOutputStream());
			} catch (IOException e) {}
		}
		
		public void run(){
			String name = "";
			
			int value = 0;
			try {
				name = in.readUTF();
				if(client.containsKey(name)==true) {
					value=1;
					dos.writeUTF(AES_Encode("중복된 아이디 입니다"));
				}else if(value!= 1) {
					
					try {
							client.put(name, dos);
							Iterator iterator = client.entrySet().iterator();
							list.clear();
							while(iterator.hasNext()){

							  Map.Entry entry = (Map.Entry)iterator.next();

							  String keys = (String)entry.getKey();

							  list.add(keys);
							}
							send(AES_Encode(list + "\n"));
							send(AES_Encode(name + ">님이 입장하셨습니다\n"));
							
							while(in != null) {
								send(AES_Encode(name + ">"+AES_Decode(in.readUTF())+"\n")); 
							}
					} catch (IOException e) {
						//e.printStackTrace();
					} catch (Exception e) {
						//e.printStackTrace();
					} finally {
						try {
							list.remove(name);
							send(AES_Encode("del"+list));
							send(AES_Encode(name+">님이 퇴장하셨습니다.\n"));
						} catch (IOException e) {}
						client.remove(name);
					}
				}
			}catch(Exception e) {}
		}
	}
	
	void send(String msg) throws IOException {//, int value
		Iterator it = client.keySet().iterator();
		ta.append(msg+"\n");
		while(it.hasNext()) {
			try {
				DataOutputStream out = (DataOutputStream)client.get(it.next());
				out.writeUTF(msg);
				}catch (IOException e) {}
		}
	}
}