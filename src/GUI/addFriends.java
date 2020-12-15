package GUI;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class addFriends{
	JFrame frame = new JFrame("Add Friend");
	//유저검색창
	JTextField input = new JTextField();
	JLabel userList = new JLabel("UserList");
	
	//검색된 유저 리스트
	JList<String> wList;
	DefaultListModel<String> list;
	
	//확인버튼
	JButton summit = new JButton("Summit");
	//친구추가 팝업메뉴
	JPopupMenu add = new JPopupMenu();
	JMenuItem addF = new JMenuItem("Add Friend");
	
	addFriends(){
		wList = new JList<String>(new DefaultListModel<String>());
        list = (DefaultListModel)wList.getModel();
        //GUI 위치 설정
		frame.setLayout(null);
		input.setText("Search Box");
		userList.setFont(new Font("STXinwei",Font.BOLD,20));
		input.setBounds(0,20,385, 30);
		userList.setBounds(0,50,385,30);
		JScrollPane p =new JScrollPane(wList);
		p.setBounds(0,100,385,400);
		summit.setBounds(285,530,100,30);
		//GUI 위치설정 끝
		
		//GUI frame에 추가
		add.add(addF);
		wList.setComponentPopupMenu(add);
		frame.add(input);
		frame.add(userList);
		frame.add(p);
		frame.add(summit);
		frame.setSize(400,600);
		//GUI frame에 추가 끝
		Dimension res = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((res.width / 2) - 200 , (res.height / 2) - 300);
		frame.setResizable(false);
	}

}