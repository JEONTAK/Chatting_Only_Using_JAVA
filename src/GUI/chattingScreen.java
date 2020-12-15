package GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import Action.Protocol;

public class chattingScreen extends JFrame implements Runnable, ActionListener {
	
	//GUI 객체 생성
	public JFrame frame = new JFrame("");
	public JButton exit;
	public JLabel user = new JLabel("USER");
	public JTextArea messageArea = new JTextArea();
	public JTextArea partList = new JTextArea();
	public JTextField textField = new JTextField();
	public chattingScreen(String roomN) {
	///
		
		//frame 설정
		frame.setTitle(roomN);
		frame.setLayout(null);
		frame.setSize(400,600);
		Dimension res = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((res.width / 2) - 200 , (res.height / 2) - 300);
		frame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); //frame에서 X를 눌러도 나가지 않게함.
															 //오로지 EXIT를 눌러야 나갈 수 있음
		setResizable(false);
		//
		
		//user label 설정
		user.setFont(new Font("STXinwei",Font.BOLD,20));
		user.setBounds(280,20,100,30);
		user.setHorizontalAlignment(user.CENTER);
		//
		
		//scroll pane 설정
		JScrollPane p =new JScrollPane(partList);
		p.setBounds(280, 60, 100, 220);
		//
		
		
		//exit 버튼 설정
		exit = new JButton("EXIT");
		exit.setBounds(280,490,100,39);
		//
		
		
		//textField 설정
		textField.setBounds(20,490,250,40);
		textField.setEditable(false);
		textField.setText("");
		//
		
		
		//messageArea 설정
		messageArea.setEditable(false);
		messageArea.setBounds(20,20,250,460);
		messageArea.setText("");
		//
		
		//partList TextArea 설정
		partList.setEditable(false);
		partList.setText("");
		//
		
		
	
		
		//frame에 component추가
		frame.add(user);
		frame.add(messageArea);
		frame.add(textField);
		frame.add(exit);
		frame.add(p);
		//
		
		frame.setVisible(true);
		event();//event 실행
	}
	
	public void event() {
		textField.addActionListener(this);
		exit.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {//이벤트 설정
		if(e.getSource() == textField) {//채팅창에 입력 시
			loginScreen.printW.println(Protocol.CHATTINGSENDMESSAGE + "|" + textField.getText() + "|" + frame.getTitle()); // 메세지를 보냄
			loginScreen.printW.flush();
			textField.setText("");
		}
		else if (e.getSource() == exit) {//나가기 버튼 누를 시 
			frame.setVisible(false);
			loginScreen.printW.println(Protocol.EXITCHATTINGROOM + "|" + "Message" + "|" + frame.getTitle());
			loginScreen.printW.flush();
			partList.setText("");
		} 
		
	}
	
	@Override
	public void run() {
		String line[] = null;
		while(true) {
			try {
				
				
			}catch (Exception io) {
				io.printStackTrace();
			}
		}
	}
}
