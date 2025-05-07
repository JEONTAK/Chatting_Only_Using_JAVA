package GUI;

import Action.Protocol;
import Action.RSA;
import FunctionTest.Email.SendMail;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class loginScreen extends JFrame implements ActionListener, Runnable, ListSelectionListener, PopupMenuListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    //화면 구성
    private JFrame frame = new JFrame("Login");
    private JLabel name = new JLabel("Messenger");
    private JLabel id = new JLabel("ID");
    private JLabel pw = new JLabel("PW");
    private JTextField idField = new JTextField(20);
    private JPasswordField pwField = new JPasswordField(20);
    private JButton create = new JButton("CREATE");
    private JButton summit = new JButton("LOGIN");
    //화면 구성

    private String publicKey;//공개키 정의

    //GUI 정의
    newAccount createAccount;
    mainScreen mainS;
    addFriends aF;
    makeGroupChat mGC;

    Socket socket;
    private BufferedReader br;
    private BufferedReader br2;
    static PrintWriter printW;

    private String sNumber = "><^^"; // default 시크릿 넘버
    private boolean condition_S = false; // 이메일 인증 확인
    private boolean condition_Id = false; // ID 중복 체크
    private static Map<Integer, chattingScreen> chat_room = new HashMap<>(); //채팅방 관리

    public loginScreen() throws IOException {
        //소켓 생성
        network();

        mainS = new mainScreen();
        createAccount = new newAccount();
        aF = new addFriends();
        mGC = new makeGroupChat();


        //RSA부분
        String filepath = "./key.txt";//파일 경로
        br2 = new BufferedReader(new InputStreamReader(new FileInputStream(filepath)));
        publicKey = br2.readLine();

        //로그인창 GUI 설정 및 위치, 크기, 글자 폰트 설정
        frame.setLayout(null);
        name.setFont(new Font("STXinwei", Font.BOLD, 50));
        id.setFont(new Font("STXinwei", Font.BOLD, 20));
        pw.setFont(new Font("STXinwei", Font.BOLD, 20));
        create.setFont(new Font("STXinwei", Font.BOLD, 15));
        summit.setFont(new Font("STXinwei", Font.BOLD, 15));
        name.setBounds(55, 0, 300, 75);
        id.setBounds(60, 100, 50, 50);
        pw.setBounds(60, 160, 50, 50);
        idField.setBounds(140, 100, 175, 50);
        pwField.setBounds(140, 160, 175, 50);
        create.setBounds(25, 260, 100, 50);
        summit.setBounds(275, 260, 100, 50);
        pwField.setEchoChar('*');
        frame.add(name);
        frame.add(id);
        frame.add(pw);
        frame.add(idField);
        frame.add(pwField);
        frame.add(create);
        frame.add(summit);
        frame.setSize(400, 350);
        Dimension res = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((res.width / 2) - 200, (res.height / 2) - 175);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

        //액션 리스너 실행
        event();
    }

    public void event() {
        //회원가입 버튼 리스너
        create.addActionListener(this);
        //로그인 확인 버튼 리스너
        summit.addActionListener(this);
        //비밀번호 입력창 엔터 리스너
        pwField.addActionListener(this);
        //회원가입 확인 버튼 리스너
        createAccount.createOK.addActionListener(this);
        //회원가입 아이디 중복 유무 체크 버튼 리스너
        createAccount.isDuplicate.addActionListener(this);
        //회원가입 이메일 인증 번호 전송 버튼 리스너
        createAccount.checkEmail.addActionListener(this);
        //회원가입 이메일 인증 번호 확인 버튼 리스너
        createAccount.emailokB.addActionListener(this);
        //메인스크린 그룹 채팅 버튼 리스너
        mainS.groupChat.addActionListener(this);
        //메인스크린 설정 버튼 리스너
        mainS.setting.addActionListener(this);
        //메인스크린 친구추가 버튼 리스너
        mainS.findFriend.addActionListener(this);
        //메인스크린 EXIT 팝업메뉴 리스너
        mainS.exit.addActionListener(this);
        //메인스크린 친구추가 팝업메뉴 리스너
        mainS.addF.addActionListener(this);
        //메인스크린 그룹채팅 팝업메뉴 리스너
        mainS.groupC.addActionListener(this);
        //메인스크린 오늘의 한마디 변경 팝업메뉴 리스너
        mainS.changeTL.addActionListener(this);
        //메인스크린 친구 클릭시 나오는 정보 팝업메뉴 리스너
        mainS.info.addActionListener(this);
        //메인스크린 친구 클릭시 나오는 1대1 채팅 팝업메뉴 리스너
        mainS.chat.addActionListener(this);
        //메인스크린 친구 클릭시 나오는 친구 제거 리스너
        mainS.delete.addActionListener(this);
        //친구추가창 유저검색창 리스너
        aF.input.addActionListener(this);
        //친구추가창 친구추가 팝업메뉴 리스너
        aF.addF.addActionListener(this);
        //친구추가창 확인 버튼 리스너
        aF.summit.addActionListener(this);
        //그룹채팅방 생성창 생성 버튼 리스너
        mGC.create.addActionListener(this);
        //메인스크린 친구클릭시 나타나는 팝업메뉴 설정
        initPopupListener(mainS.fList, mainS.user);
        //메인스크린 설정 클릭시 나타나는 팝업메뉴 설정
        initPopupListener(mainS.setting, mainS.menu);
        //친구추가 창에서 유저클릭시 나타나는 팝업메뉴 설정
        initPopupListener(aF.wList, aF.add);
    }

    public void network() throws IOException {

        // 소켓 생성
        try {
            String serverInfo[] = readServerInfo();
            socket = new Socket(serverInfo[0], Integer.parseInt(serverInfo[1]));
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printW = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

        } catch (UnknownHostException e) {
            System.out.println("서버를 찾을 수 없습니다");
            e.printStackTrace();
            System.exit(0);
        } catch (IOException e) {
            System.out.println("서버와 연결이 안되었습니다");
            e.printStackTrace();
            System.exit(0);
        }

        // 이벤트

        // 스레드 생성
        Thread t = new Thread(this);
        t.start();
    }

    //서버의 IP주소, Port Number를 불러옴
    public static String[] readServerInfo() throws IOException {
        //파일이름은 serverinfo.dat으로 지정한다.
        String fileName = "serverinfo.dat";
        Scanner inputStream = null;
        String[] serverInfo = new String[2];
        String input;
        try {//서버의 정보를 serverinfo.dat에서 받아온다.
            inputStream = new Scanner(new File(fileName));
            input = inputStream.nextLine();
            serverInfo = input.split(" ");
        }//만약 파일이 존재하지 않는다면, 서버주소를 127.0.0.1, 포트번호를  9500 기본 값으로서 설정해준다.
        catch(FileNotFoundException e) {
            serverInfo[0] = "127.0.0.1";
            serverInfo[1] = "9500";
        }
        return serverInfo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == create) { //회원가입 버튼 누르면
            createAccount.frame.setVisible(true);
        }
        else if(e.getSource() == summit) { //로그인 버튼 누르면
            String id = idField.getText();
            String pwss = pwField.getText();
            //비밀번호 RSA이용 암호화
            pwss=RSA.encode(pwss, publicKey);
            if (id.length() == 0 || pwss.length() == 0) {
                JOptionPane.showMessageDialog(this, "빈칸을 입력해주세요");
            } else {
                String line = id + "%" + pwss;
                //서버에게 로그인 요청
                printW.println(Protocol.ENTERLOGIN + "|" + line);
                printW.flush();
            }
            idField.setText("");
            pwField.setText("");
        }
        else if(e.getSource() == pwField) { //비밀번호 치고 엔터 누르면
            String id = idField.getText();
            String pwss = pwField.getText();
            //비밀번호 RSA이용 암호화
            pwss=RSA.encode(pwss, publicKey);
            if (id.length() == 0 || pwss.length() == 0) {
                JOptionPane.showMessageDialog(this, "빈칸을 입력해주세요");
            } else {
                String line = id + "%" + pwss;
                //서버에게 로그인 요청
                printW.println(Protocol.ENTERLOGIN + "|" + line);
                printW.flush();
            }
            idField.setText("");
            pwField.setText("");
        }
        else if(e.getSource() == createAccount.createOK) { //회원가입 완료 버튼 클릭
            //유저가 입력한 값과 초기 기본값으로 설정
            String name = createAccount.nameField.getText();
            String id = createAccount.idField.getText();
            String pw1 = createAccount.pwField.getText();
            //비밀번호 암호화
            pw1=RSA.encode(pw1, publicKey);
            String ageYear = (String) createAccount.ageYearC.getSelectedItem();
            String ageMonth = (String) createAccount.ageMonthC.getSelectedItem();
            String ageDay = (String)createAccount.ageDayC.getSelectedItem();
            String email = createAccount.emailField.getText();
            String nickname = createAccount.nnField.getText();
            String today_line = "Enter Today Message";
            int state = 0;
            //회원가입시 입력 안한 곳 있다면
            if(name.length() == 0 || id.length() == 0 || pw1.length() == 0 || email.length() == 0 || nickname.length() == 0) {
                JOptionPane.showMessageDialog(this, "Please enter a blank space.");
            }//회원가입시 중복체크 하고 이메일 인증번호도 확인 했으면
            else if(condition_S && condition_Id) {
                String line = "";
                line = (createAccount.idField.getText() + "%" + pw1 +
                        "%" + createAccount.nnField.getText() + "%" + createAccount.nameField.getText() +
                        "%" + createAccount.emailField.getText() + "@" + createAccount.emailC.getSelectedItem() +
                        "%" + createAccount.ageYearC.getSelectedItem() + createAccount.ageMonthC.getSelectedItem()
                        + createAccount.ageDayC.getSelectedItem() + "%" + today_line + "%" + state);
                System.out.println(line);
                //서버에게 등록요청 함
                printW.println(Protocol.REGISTER + "|" + line);
                printW.flush();

                JOptionPane.showMessageDialog(this, "Create Account Success.");
                createAccount.idField.setText("");
                createAccount.pwField.setText("");
                createAccount.nnField.setText("");
                createAccount.nameField.setText("");
                createAccount.emailField.setText("");
                createAccount.emailC.setSelectedIndex(0);
                createAccount.ageYearC.setSelectedIndex(0);
                createAccount.ageMonthC.setSelectedIndex(0);
                createAccount.ageDayC.setSelectedIndex(0);
                condition_S = false;
                condition_Id = false;
                sNumber = "><^^";
                createAccount.frame.setVisible(false);
                this.frame.setVisible(true);
            }//만약 ID중복체크 안했다면
            else if (!condition_Id && condition_S) {
                JOptionPane.showMessageDialog(this, "Check ID Duplicated");
            }//만약 이메일 중복체크 안했다면
            else if (!condition_S && condition_Id) {
                JOptionPane.showMessageDialog(this, "Check Email");
            }//둘다 안했다면
            else if (!condition_Id && !condition_S) {
                JOptionPane.showMessageDialog(this, "Check ID Duplicated , Check Email");
            }

        }
        else if(e.getSource() == createAccount.isDuplicate) {//ID 중복 체크
            if (createAccount.idField.getText().length() == 0) {
                JOptionPane.showMessageDialog(this, "Input ID");
            } else {
                printW.println(Protocol.IDSEARCHCHECK + "|" + createAccount.idField.getText());
                printW.flush();
            }
        }
        else if (e.getSource() == createAccount.checkEmail) // 회원가입 페이지 -----------> 인증번호 전송
        {
            if (createAccount.emailField.getText().length() == 0) {
                JOptionPane.showMessageDialog(this, "Input Email");
            } else {
                JOptionPane.showMessageDialog(this, "Send the Check number");
                String emailString = createAccount.emailField.getText() + "@"
                        + (String) createAccount.emailC.getSelectedItem();
                System.out.println(emailString);
                sNumber = String.valueOf(SendMail.SendMail(emailString));
            }
        }else if (e.getSource() == createAccount.emailokB) { // 회원가입 페이지 -----------> 인증번호확인
            if (sNumber.compareTo(createAccount.emailadductionT.getText()) == 0) {
                JOptionPane.showMessageDialog(this, "Confirm");
                condition_S = true;
            } else {
                JOptionPane.showMessageDialog(this, "Wrong Number");
            }
        }//메인스크린 설정 클릭시
        else if(e.getSource() == mainS.setting) {
            mainS.menu.show(mainS.setting, 0, 50);
        }//메인스크린 친구추가 버튼 클릭시
        else if(e.getSource() == mainS.findFriend) {
            aF.input.setText("");
            //서버에게 전체유저목록 요청
            printW.println(Protocol.REQUEST_FRIEND_LIST + "|" + "ALL!@#");
            printW.flush();
            aF.frame.setVisible(true);
        }//메인스크린 그룹채팅방 버튼 클릭시
        else if(e.getSource() == mainS.groupChat) {//방만들기 페이지 오픈
            mGC.frame.setVisible(true);
            //그룹채팅창 열기 위해 친구 목록 요청
            printW.println(Protocol.REQUEST_GROUPCHAT_LIST);
            printW.flush();
        }//메인스크린에서 나가기 팝업메뉴 클릭시
        else if(e.getSource() == mainS.exit) {
            mainS.frame.setVisible(false);
            this.frame.setVisible(true);
            //서버에게 유저 로그아웃 요청
            printW.println(Protocol.EXITMAINROOM + "|" + "message");
            printW.flush();
        }//메인스크린에서 친구추가 팝업메뉴 클릭시
        else if(e.getSource() == mainS.addF) {
            aF.input.setText("");
            //그룹채팅창 열기 위해 친구 목록 요청
            printW.println(Protocol.REQUEST_FRIEND_LIST + "|" + "ALL!@#");
            printW.flush();
            aF.frame.setVisible(true);
        }//메인스크린 그룹채팅방 팝업메뉴 클릭시
        else if(e.getSource() == mainS.groupC) {
            mGC.frame.setVisible(true);
            //그룹채팅창 열기 위해 친구 목록 요청
            printW.println(Protocol.REQUEST_GROUPCHAT_LIST);
            printW.flush();
        }//메인스크린 오늘의 한마디 변경 팝업메뉴 클릭시
        else if(e.getSource() == mainS.changeTL) {
            //변경된 문자열 받아옴
            String cTL = JOptionPane.showInputDialog("Enter Today Line");
            //서버에게 오늘의 한마디 변경 요청
            printW.println(Protocol.CHANGE_TODAY_LINE + "|" + cTL);
            printW.flush();
        }//메인스크린에서 친구 클릭시 나오는 chat 팝업메뉴 클릭시
        else if(e.getSource() == mainS.chat) {
            List line = mainS.fList.getSelectedValuesList();
            //서버에게 채팅방 열기 요청
            printW.println(Protocol.REQUEST_MAKE_GROUPCHAT + "|" + line);
            printW.flush();
        }//메인스크린에서 친구 클릭시 나오는 정보 팝업메뉴 클릭시
        else if(e.getSource() == mainS.info) {
            //서버에게 친구 정보 요청
            printW.println(Protocol.CHECK_FRIEND_INFO + "|" + mainS.fList.getSelectedValue());
            printW.flush();
        }//친구 삭제 기능
        else if(e.getSource()==mainS.delete) {
            //서버에게 친구 삭제 요청 보냄
            printW.println(Protocol.REQUEST_FRIEND_DELETE+"|"+ mainS.fList.getSelectedValue());
            printW.flush();
        }//친구 추가 창에서 확인 버튼 클릭시
        else if (e.getSource() == aF.summit) {
            aF.frame.setVisible(false);
        }//친구 추가창에서 유저 검색시
        else if (e.getSource() == aF.input) {
            //만약 공백이면 전체 유저 요청
            if(aF.input.getText().equalsIgnoreCase("")) {
                printW.println(Protocol.REQUEST_FRIEND_LIST + "|" + "ALL!@#");
                printW.flush();
            }
            //아니면 서버에게 해당 문자열포함한 유저목록 요청
            else {
                printW.println(Protocol.REQUEST_FRIEND_LIST + "|" + aF.input.getText());
                printW.flush();
            }
        }//친구 추가창에서 유저 클릭시 나오는 친구추가 팝업메뉴 클릭시
        else if(e.getSource() == aF.addF) {
            //서버에게 친구추가 요청
            printW.println(Protocol.REQUEST_FRIEND_ADD + "|" + aF.wList.getSelectedValue());
            printW.flush();
        }//그룹채팅방 설정 창에서 만들기 버튼 클릭시
        else if(e.getSource() == mGC.create) {
            //선택한 친구 목록 값 받아서 서버에게 채팅창 Open 요청
            List line = mGC.friendList.getSelectedValuesList();
            printW.println(Protocol.REQUEST_MAKE_GROUPCHAT + "|" + line);
            printW.flush();
            mGC.frame.setVisible(false);
        }
    }

    private void initPopupListener(JButton setting, JPopupMenu menu) {
        menu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                //팝업메뉴 나오는 위치 설정
                Object obj = e.getSource();
                String l = obj.toString();
                int x = l.indexOf("desiredLocationX");
                int y = l.indexOf("desiredLocationY");
                int label = l.indexOf("label");
                int rX = Integer.parseInt(l.substring(x + 17, y - 1));
                int rY = Integer.parseInt(l.substring(y + 17 ,label - 1));
                Point p = new Point(rX,rY);
                SwingUtilities.convertPointToScreen(p, setting);
                menu.setLocation(rX,rY);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {

            }
        });
    }

    private void initPopupListener(JList<String> list, JPopupMenu m) {
        m.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                //팝업메뉴 나오는 위치 설정
                Object obj = e.getSource();
                String l = obj.toString();
                int x = l.indexOf("desiredLocationX");
                int y = l.indexOf("desiredLocationY");
                int label = l.indexOf("label");
                int rX = Integer.parseInt(l.substring(x + 17, y - 1));
                int rY = Integer.parseInt(l.substring(y + 17 ,label - 1));
                Point p = new Point(rX,rY);
                SwingUtilities.convertPointToScreen(p, list);
                m.setLocation(rX,rY);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {

            }
        });

    }

    @Override
    public void run() {
        // 받는쪽
        String line[] = null;
        while (true) {
            try {
                line = br.readLine().split("\\|");
                if (line == null) {
                    br.close();
                    printW.close();
                    socket.close();

                    System.exit(0);
                } else if (line[0].compareTo(Protocol.IDSEARCHCHECK_OK) == 0) { // 회원가입 ID 중복 안됨
                    JOptionPane.showMessageDialog(this, "Available");
                    condition_Id = true;
                } else if (line[0].compareTo(Protocol.IDSEARCHCHECK_NO) == 0) { // 회원가입 ID 중복 됨
                    JOptionPane.showMessageDialog(this, "Unavailable");
                    condition_Id = false;
                } else if (line[0].compareTo(Protocol.ENTERLOGIN_OK) == 0) // 로그인 성공
                {
                    this.frame.setVisible(false);
                    mainS.frame.setVisible(true);
                    System.out.println(line);
                    mainS.name.setText(line[1]);
                    //자신의 정보설정
                    mainS.information.setText(line[2] + " / " + line[4]);
                    mainS.stateMessage.setText(line[3]);
                    mainS.list.removeAllElements();
                    //친구목록 설정
                    String text[] = line[5].split(":");
                    for (int i = 0; i < text.length; i++) {
                        String t[] = text[i].split("&");
                        if(t[0].equalsIgnoreCase("null") && t[1].equalsIgnoreCase("null")
                                && t[2].equalsIgnoreCase("null") && t[3].equalsIgnoreCase("null") && t[4].equalsIgnoreCase("null")) {
                        }
                        else {
                            mainS.list.addElement(t[0] +  "/" +  t[1] + "/" + t[2] + "/" + t[3] + "/" + t[4]);
                        }
                    }
                }
                else if (line[0].compareTo(Protocol.ENTERLOGIN_NO) == 0) // 로그인 실패
                {
                    JOptionPane.showMessageDialog(this, line[1]);
                    System.out.println("Login Failed");
                }//오늘의 한마디 변경 되었을시
                else if(line[0].compareTo(Protocol.UPDATE_ME) == 0) {
                    mainS.stateMessage.setText(line[1]);
                }//서버에서 최신화 필요하다고 요청시
                else if(line[0].compareTo(Protocol.UPDATED) == 0) {
                    //서버에게 최신화 해달라고 요청
                    printW.println(Protocol.UPDATE_PLZ);
                    printW.flush();
                }//서버가 유저에게 최신화 데이터 가져오면
                else if(line[0].compareTo(Protocol.UPDATE_CONFIRM) == 0) {
                    //친구 목록 다시 설정
                    mainS.list.removeAllElements();
                    String text[] = line[1].split(":");
                    //만약 친구 없으면 안띄움
                    for (int i = 0; i < text.length; i++) {
                        String t[] = text[i].split("&");
                        if(t[0].equalsIgnoreCase("null") && t[1].equalsIgnoreCase("null") && t[2].equalsIgnoreCase("null")
                                && t[3].equalsIgnoreCase("null") && t[4].equalsIgnoreCase("null")) {
                        }
                        else {
                            mainS.list.addElement(t[0] +  "/" +  t[1] + "/" + t[2] + "/" + t[3] + "/" + t[4]);
                        }
                    }
                }//서버가 친구추가요청창의 유저목록 요청에 대한 정보 줄시
                else if(line[0].compareTo(Protocol.REQUEST_FRIEND_LIST_CONFIRM) == 0) {
                    aF.list.removeAllElements();
                    String text[] = line[1].split(":");
                    //만약 유저없으면 안띄움
                    for (int i = 0; i < text.length; i++) {
                        String t[] = text[i].split("&");
                        if(t[0].equalsIgnoreCase("null") && t[1].equalsIgnoreCase("null")
                                && t[2].equalsIgnoreCase("null") && t[3].equalsIgnoreCase("null")) {
                        }
                        else {
                            aF.list.addElement(t[0] +  "  " +  t[1] + "  " + t[2] + " " + t[3]);
                        }
                    }
                }//친구의 정보확인 요청에 대한 정보 받을 시
                else if(line[0].compareTo(Protocol.CONFIRM_FRIEND_INFO) == 0) {
                    String text[] = line[1].split("&");
                    String s = "offline";
                    if(text[6].equalsIgnoreCase("1")) {
                        s = "online";
                    }
                    JOptionPane.showMessageDialog(null, "ID : " + text[0] + "\nNICKNAME : " + text[1] + "\nNAME : " + text[2]
                            + "\nEMAIL : " + text[3] + "\nAGE : " + text[4] + "\nTODAY_LINE : " + text[5] + "\nSTATE : " + s);
                }//친구 삭제기능 추가
                else if(line[0].compareTo(Protocol.CONFIRM_FRIEND_DELETE) == 0) {
                    String text[] = line[1].split("&");

                    JOptionPane.showMessageDialog(null, text[0]+" is deleted!");
                    printW.println(Protocol.UPDATE_PLZ);
                    printW.flush();

                }//그룹채팅방 열때 친구 목록 전달에 대한 정보 받을 시
                else if(line[0].compareTo(Protocol.GROUPCHAT_LIST) == 0) {
                    mGC.list.removeAllElements();
                    String text[] = line[1].split(":");
                    for (int i = 0; i < text.length; i++) {
                        String t[] = text[i].split("&");
                        if(t[0].equalsIgnoreCase("null") && t[1].equalsIgnoreCase("null")
                                && t[2].equalsIgnoreCase("null") && t[3].equalsIgnoreCase("null") && t[4].equalsIgnoreCase("null")) {
                        }
                        else {
                            mGC.list.addElement(t[0] +  "/" +  t[1] + "/" + t[2] + "/" + t[3] + "/" + t[4]);
                        }
                    }
                }//채팅방 입장 요청 받을시
                else if(line[0].compareTo(Protocol.JOINROOM_REQUEST) == 0) {
                    int check = JOptionPane.showConfirmDialog(null, line[1] + " invited you. Do you Join Chat?", "REQUEST JOIN" ,JOptionPane.YES_NO_OPTION);
                    if(check == 0) {
                        //수락하면 수락했다고 서버에게 알림
                        printW.println(Protocol.JOINROOM_YES + "|" + line[1] + "|" +line[2]);
                        printW.flush();
                    }
                    else {
                        //거절했다고 서버에게 알림
                        printW.println(Protocol.JOINROOM_NO + "|" +  line[1] + "|" +line[2]);
                        printW.flush();
                    }
                }
                else if (line[0].compareTo(Protocol.ROOMMAKE_OK) == 0) // 방만들어짐
                {
                    //채팅스크린을 새롭게 만들고 채팅방 정보에 저장
                    System.out.println(Protocol.ROOMMAKE_OK);
                    chattingScreen chat = new chattingScreen(line[2]);
                    chat_room.put(Integer.parseInt(line[2]), chat);

                }
                else if (line[0].compareTo(Protocol.ENTERROOM_OK1) == 0) // 방입장 요청받은 유저가 입장시
                {
                    //채팅스크린을 새롭게 만들고 채팅방 정보에 저장
                    chattingScreen chat = new chattingScreen(line[2]);
                    chat_room.put(Integer.parseInt(line[2]), chat);
                }
                else if (line[0].compareTo(Protocol.ENTERROOM_USERLISTSEND) == 0) // 채팅방 유저 리스트 새로고침
                {

                    String roomMember[] = line[1].split("%");// 룸에 들어온사람들
                    String lineList = "";
                    for (int i = 0; i < roomMember.length; i++) {
                        lineList += (roomMember[i] + "\n");
                    }
                    System.out.println(lineList);
                    //채팅방 목록에서 해당방 추출해 설정해줌
                    chattingScreen temp = chat_room.get(Integer.parseInt(line[3]));
                    temp.textField.setEditable(true);
                    temp.partList.setText(lineList);
                    temp.messageArea.append(line[2] + "\n");
                }//상대방이 채팅친 정보가 서버를 통해 넘어올시
                else if (line[0].compareTo(Protocol.CHATTINGSENDMESSAGE_OK) == 0) {
                    //채팅방 목록에서 해당방 추출해 설정해줌
                    chattingScreen temp = chat_room.get(Integer.parseInt(line[3]));
                    temp.messageArea.append("[" + line[1] + "] :" + line[2] + "\n");
                }//상대방이 방입장 요청 거절시
                else if(line[0].compareTo(Protocol.ENTERROOM_REJECT) == 0) {
                    //채팅방 목록에서 해당방 추출해 설정해줌
                    chattingScreen temp = chat_room.get(Integer.parseInt(line[2]));
                    temp.frame.setVisible(false);
                    temp.partList.setText("");
                    JOptionPane.showMessageDialog(null, line[1] + " reject join chat.");
                }//유저가 채팅방을 나가면
                else if(line[0].compareTo(Protocol.EXIT_CHATTINGROOM) == 0) {
                    //채팅방 목록에서 해당방 추출해 설정해줌
                    chattingScreen temp = chat_room.get(Integer.parseInt(line[1]));
                    temp.frame.setVisible(false);
                    temp.partList.setText("");
                    temp.textField.setEditable(false);
                    JOptionPane.showMessageDialog(null, "All user exit.");
                }

            } catch (IOException io) {
                io.printStackTrace();
            }

        } // while


    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        // TODO Auto-generated method stub

    }
}