package GUI;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Calendar;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import Action.Protocol;

public class mainScreen extends JFrame{	
	
	JFrame frame = new JFrame("Messenger");
	JPanel myinfo = new JPanel();
	JPanel myname = new JPanel();
	JLabel name = new JLabel("");
	JPanel friend = new JPanel();
	JTextField stateMessage = new JTextField(20);
	JTextField information = new JTextField(20);
	JButton setting = new JButton("Menu");//메뉴
	JButton findFriend = new JButton("Add Friend");//친구추가
	JButton groupChat = new JButton("Group Chat");//그룹채팅생성
	JList<String> fList;
	DefaultListModel<String> list;
	JPopupMenu menu = new JPopupMenu();
    JPopupMenu user = new JPopupMenu();
    JMenuItem exit = new JMenuItem("Exit");//나가기
    JMenuItem addF = new JMenuItem("Add Friend");//친구추가
    JMenuItem groupC = new JMenuItem("Group Chat");//그룸채팅생성
    JMenuItem chat = new JMenuItem("Chat");//채팅
    JMenuItem info = new JMenuItem("Information");//친구정보
    JMenuItem delete=new JMenuItem("Delete");//친구삭제
    JMenuItem changeTL = new JMenuItem("Change Today Line");//오늘의한마디
    JLabel coronadeath = new JLabel();
    JLabel coronadecide = new JLabel();
    JLabel today = new JLabel();
	private BufferedReader br;
	private PrintWriter pw;
	public mainScreen(){
		
		 DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        factory.setNamespaceAware(true);
	        DocumentBuilder builder;
	        Document doc = null;
	        Calendar now=Calendar.getInstance(); //현재 날짜를 리얼타임으로 받아옴
	        int []death={0,0}; //전일, 당일 누적 사망자, 확진자 저장 arr
	        int []decide={0,0};
	        try {
	            //OpenApi호출
	            String urlstr = "http://openapi.data.go.kr/openapi/service/rest/Covid19/getCovid19InfStateJson"//url
	                    + "?serviceKey=2xbO73yAQSwDcoTLuXbVB4DPg6qQX8%2BMWLG4Oosuvhu7cYcMXzfx41spvhcH%2FsY1CIZM0pP7IfYp6LnoiTpd9g%3D%3D"//인증키
	                    +"&startCreateDt="+now.get(Calendar.YEAR)+(now.get(Calendar.MONTH)+1)+(now.get(Calendar.DATE)-1)//어제 날짜
	                  +"&endCreateDt="+now.get(Calendar.YEAR)+(now.get(Calendar.MONTH)+1)+now.get(Calendar.DATE);//현재 날짜
	            URL url = new URL(urlstr);
	            HttpURLConnection urlconnection = (HttpURLConnection) url.openConnection(); //url로 연결
	            br = new BufferedReader(new InputStreamReader(urlconnection.getInputStream(), "UTF-8"));//응답 읽기
	            String result = "";
	            String line;
	            while ((line = br.readLine()) != null) {
	                result = result + line.trim();// result = URL로 XML을 읽은 값
	            }
	            br.close(); //모든 XML을 받아온 후 버퍼리더, url연결 차단
	            urlconnection.disconnect();
	            // xml 파싱하기
	            InputSource is = new InputSource(new StringReader(result));
	            builder = factory.newDocumentBuilder();
	            doc = builder.parse(is);
	            XPathFactory xpathFactory = XPathFactory.newInstance();
	            XPath xpath = xpathFactory.newXPath();
	            XPathExpression expr = xpath.compile("//items/item");
	            NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);//받아온 날짜의 갯수만큼 생성
	            for (int i = 0; i < nodeList.getLength(); i++) { 
	                NodeList child = nodeList.item(i).getChildNodes();//받아온 데이터셋 안의 요소만큼 생성
	                for (int j = 0; j < child.getLength(); j++) {
	                    Node node = child.item(j);
	                    switch(node.getNodeName()) {
	                    case "deathCnt":
	                       death[i]=Integer.parseInt(node.getTextContent()); //누적 사망자를 날짜별로 저장
	                       break;
	                    case "decideCnt":
	                       decide[i]=Integer.parseInt(node.getTextContent()); //누적 확진자를 날짜별로 저장
	                       break;
	                    case "stateDt":
	                       break;
	                    }
	                }
	            }
	            death[0]-=death[1]; //누적 사망자, 확진자 값을 빼서 당일 사망자, 확진자를 구함
	            decide[0]-=decide[1];
	        } catch (Exception e) {
	            System.out.println(e.getMessage());
	        }
	    String date = Integer.toString(now.get(Calendar.YEAR))+ "년 "+
            		Integer.toString((now.get(Calendar.MONTH)+1))+ "월 " +Integer.toString((now.get(Calendar.DATE))) + "일";
		today.setText(date + " 오늘의 코로나 상황"); //코로나 관련 정보 출력
	    coronadeath.setText("사망자 : " + death[0]);
	    coronadecide.setText("확진자 : " + decide[0]);
	    today.setFont(new Font("STXinwei",Font.BOLD,15)); 
	    coronadeath.setFont(new Font("STXinwei",Font.BOLD,15));
	    coronadecide.setFont(new Font("STXinwei",Font.BOLD,15));
		fList = new JList<String>(new DefaultListModel<String>());
        list = (DefaultListModel)fList.getModel();
        frame.setLayout(null);
		information.setEditable(false);
		stateMessage.setEditable(false);
		name.setFont(new Font("STXinwei",Font.BOLD,20));
		name.setHorizontalAlignment(name.CENTER);
		setting.setBounds(0, 0, 100, 50); //각 버튼 위치지정
		findFriend.setBounds(100, 0, 142, 50);
		groupChat.setBounds(242,0,142,50);
		name.setBounds(0, 50, 100, 100);
		information.setBounds(100,75,300,25);
		stateMessage.setBounds(100,100,300,25);
		JScrollPane p =new JScrollPane(fList); //친구목록창
		p.setBounds(0, 150, 385, 600);
		coronadeath.setBounds(10, 810, 170, 30);
		coronadecide.setBounds(200, 810, 170, 30);
		today.setBounds(10, 770, 380, 30);
		menu.add(exit);
		menu.add(addF);
		menu.add(groupC);
		menu.add(changeTL);
		setting.setComponentPopupMenu(menu); //팝업창
	    user.add(chat);
	    user.add(info);
	    user.add(delete);
	    fList.setComponentPopupMenu(user);
	    
	    frame.add(setting);
		frame.add(findFriend);
		frame.add(groupChat);
		frame.add(name);
		frame.add(information);
		frame.add(stateMessage);
		frame.add(p);
		frame.add(coronadeath);
		frame.add(coronadecide);
		frame.add(today);
		frame.setSize(400,900);
	    Dimension res = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((res.width / 2) - 200 , (res.height / 2) - 425);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); //프레임에 있는 X 버튼 눌러도 창 안닫히게 함.
															 //오로지 EXIT 클릭시 나갈 수 있음.
		
	}

}


