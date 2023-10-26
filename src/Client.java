import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JFrame;
//import org.apache.commons.codec.binary.Base64;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/** * 양방향 암호화 알고리즘인 AES256 암호화를 지원하는 클래스 */
public class Client extends JFrame{
		
	static String secretKey = "secreatKeysecreatKeysecreatKeyll";
	static String IV = secretKey.substring(0,16);
	static String name = null;
	int print=0;
	int exit=0;
	JPanel tp = new JPanel();
	JPanel np = new JPanel();
	JTextArea ta = new JTextArea();
	JTextArea na = new JTextArea("",10,12);//,TextArea.SCROLLBARS_VERTICAL_ONLY
	JTextField tf = new JTextField();
	Font f =new Font("굴림체",Font.PLAIN,12);
	
	JScrollPane tascroll = new JScrollPane(ta, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	JScrollPane nascroll = new JScrollPane(na, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	
	DataInputStream in;
	DataOutputStream dos;
	
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
	
	public Client() {
		super("Chatting");
		
		setBounds(300,0,300,300);
		
		//namearea
		na.setFont(f);
		na.setLineWrap(true);
		
		//textarea
		ta.setLineWrap(true);
		ta.setBackground(new Color(155,187,212));
		
		
		//namepanel
		np.setLayout(new BorderLayout());
		np.add(tascroll);
		np.add(nascroll,"East");
		
		//textpanel
		tp.setLayout(new BorderLayout());
		tp.add(tf);
		
		//frameadd
		add(np,"Center");
		add(tp, "South");
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		EventHandler handler = new EventHandler();
		ta.addFocusListener(handler);
		tf.addFocusListener(handler);
		tf.addActionListener(handler);
		
		na.setEditable(false);
		ta.setEditable(false);
		setVisible(true);
	}
	
	void startclient(){
		
		try {
			Socket socket = new Socket("192.168.0.1", 7777);//My Ip
			if(print==0) {
				ta.append("서버에 연결되었습니다.\n");
				print++;
			}
			
			String names = "";
			
			while(names.equals("")) {
				
				if(exit >= 3)
					System.exit(0);
				exit++;
				names = JOptionPane.showInputDialog(null, String.format("이름을 입력하세요. \n3번이상 중복 입력시 다시 실행하세요.(%d)",exit), "이름", JOptionPane.OK_CANCEL_OPTION);
				if(names==null) System.exit(0);
			}
			
			this.name = names;
			Thread send = new Thread(new Send(socket));
			Thread receive = new Thread(new Receive(socket));
			send.start();
			receive.start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (Exception e) {}
		
	}

    public static void main(String[] args) {
    		
    	Client client = new Client();
    	client.startclient();
	    
    }
    
    class Send extends Thread{
    	
    	Socket socket;
    	Send(Socket socket){
    		this.socket = socket;
    		try {
    			dos = new DataOutputStream(socket.getOutputStream());
    		}catch(Exception e) {
    			
    		}
    	}
    	public void run() {
    		try {
    			if(dos != null)
    				dos.writeUTF(name);
    		}catch(Exception e) {}
    	}
    }
    
    class Receive extends Thread{
    	
    	Socket socket;
    	Receive(Socket socket){
    		this.socket = socket;
    		try {
    			in = new DataInputStream(socket.getInputStream());
    		}catch(Exception e) {}
    	}
    	public void run() {
    		while(in!=null) {
    			try {
    				while(true) {
    					String msg = in.readUTF();
    					msg = AES_Decode(msg);
	    				if(msg.equals("중복된 아이디 입니다")) {
	    					startclient();
	    				}else if(msg.charAt(0)=='[' && msg.charAt(msg.length()-2)==']') {
	    					na_change(msg);
	    				}else if(msg.substring(0, 3).equals("del")) {
	    					msg = msg.substring(3, msg.length());
	    					na_change(msg);
	    				}else {
	    					ta.append(msg);
	    					break;
	    				}
    				}
        		}catch(Exception e) {
        			e.printStackTrace();
        		}
    		}
    	}
    	
    	public void na_change(String getmsg) {
    		na.setText("접속자 이름\n");
    		getmsg = getmsg.replace("[", "");
    		getmsg = getmsg.replace("]", "");
    		getmsg = getmsg.replace(" ", "");
			String[] names = getmsg.split(",");
			for(String inputname : names)
				na.append(inputname+"\n");
    	}
    }
    
    class EventHandler extends FocusAdapter implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			String msg = tf.getText();
			
			if("".equals(msg)) return;
			else {
				try {
					dos.writeUTF(AES_Encode(msg));
					tf.setText("");
				} catch (IOException e1) {}
				catch (NoSuchAlgorithmException e1) {
				} catch (InvalidKeyException e1) {
				} catch (NoSuchPaddingException e1) {
				} catch (IllegalBlockSizeException e1) {
				} catch (BadPaddingException e1) {
				} catch (InvalidAlgorithmParameterException e1) {
				}
			}
		}
		
		public void focusGained(FocusEvent e) {
			tf.requestFocus();
		}
	}
}