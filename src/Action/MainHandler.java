package Action;

import DO.Room;
import DO.User;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class MainHandler extends Thread {
    private BufferedReader chatReader;
    private BufferedReader privateKeyReader;
    private PrintWriter printW;
    private Socket socket;
    private Connection conn;
    private PreparedStatement pstmt;
    private User user;

    // 연결된 사용자 리스트
    private ArrayList<MainHandler> connUserList;
    // 전체 방 리스트
    private ArrayList<Room> roomtotalList;
    // 사용자가 존재하는 방
    private Room priRoom;
    // 사설 키 정의
    private String privateKey;

    // Parameter : 소켓, 연결된 사용자 리스트, 전체 방 리스트, JDBC와 연결
    public MainHandler(Socket socket, ArrayList<MainHandler> connUserList,
                       ArrayList<Room> roomtotalList, Connection conn) throws IOException {
        this.user = new User();
        this.priRoom = new Room();
        this.socket = socket;
        this.connUserList = connUserList;
        this.roomtotalList = roomtotalList;
        this.conn = conn;

        chatReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        printW = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

        //RSA
        String filepath = "./key2.txt"; // 파일 경로
        privateKeyReader = new BufferedReader(new InputStreamReader(new FileInputStream(filepath)));
        privateKey = privateKeyReader.readLine();
        //RSA

    }

    @Override
    public void run() {
        // 데이터 입력 받음 -> 데이터 파싱 -> 결과 실행해주어야함.
        try {

            String[] line = null;
            while (true) {
                line = chatReader.readLine().split("\\|");

                if (line == null) {
                    break;
                }
                if (line[0].compareTo(Protocol.REGISTER) == 0) { // 회원가입
                    String userContent[] = line[1].split("%");

                    String sql2 = "UPDATE id_generator SET seq_currval=LAST_INSERT_ID(seq_currval+1)";
                    pstmt = conn.prepareStatement(sql2);
                    pstmt.executeUpdate();

                    String sql = "Insert into UserContent(priNumber, id, password, nickName, name ,email, age, today_line, state) "
                            + "values(LAST_INSERT_ID(),?,?,?,?,?,?,?,?)";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, userContent[0]);
                    pstmt.setString(2, userContent[1]);
                    pstmt.setString(3, userContent[2]);
                    pstmt.setString(4, userContent[3]);
                    pstmt.setString(5, userContent[4]);
                    pstmt.setString(6, userContent[5]);
                    pstmt.setString(7, userContent[6]);
                    pstmt.setString(8, userContent[7]);
                    int su = pstmt.executeUpdate(); // 몇개 Insert 한지 갯수를 업데이트
                    System.out.println(su + "회원가입[DB]");

                } else if (line[0].compareTo(Protocol.IDSEARCHCHECK) == 0) { // 회원가입 ID 중복 체크
                    System.out.println(line[0] + "/" + line[1]);
                    //입력한 ID가 기존 DB에 존재하는지 확인
                    String sql = "select * from UserContent where id = '" + line[1] + "'";
                    pstmt = conn.prepareStatement(sql);
                    ResultSet rs = pstmt.executeQuery(sql);
                    String name = null;
                    int count = 0;
                    while (rs.next()) {
                        name = rs.getString("id");
                        if (name.compareTo(line[1]) == 0) {
                            count++;
                        }
                    }
                    System.out.println(count);
                    // ID가 중복 안되면 가입 가능
                    if (count == 0) {
                        printW.println(Protocol.IDSEARCHCHECK_OK + "|" + "MESSAGE");
                        printW.flush();
                    } else {
                        printW.println(Protocol.IDSEARCHCHECK_NO + "|" + "MESSAGE");
                        printW.flush();
                    }
                } else if (line[0].compareTo(Protocol.ENTERLOGIN) == 0) { // [login]

                    boolean con = true; // 로그인 유무 확인
                    System.out.println("login");
                    String userContent[] = line[1].split("%");

                    System.out.println(userContent[0] + "/" + userContent[1]);

                    for (int i = 0; i < connUserList.size(); i++) {
                        if ((connUserList.get(i).user.getIdName()).compareTo(userContent[0]) == 0) {
                            con = false;
                        }
                    }

                    //만약 로그인이 되어 있지 않다면
                    if (con) {
                        String sql = "select * from UserContent where id = '" + userContent[0] + "'";

                        pstmt = conn.prepareStatement(sql);
                        ResultSet rs = pstmt.executeQuery(sql);
                        String pw = null;
                        int count = 0;

                        while (rs.next()) { // 입력한 ID와 같은 데이터 DB에서 가져와 저장.
                            user.setPassword(userContent[1]);
                            user.setPryNumber(rs.getInt("priNumber"));
                            user.setIdName(rs.getString("ID"));
                            user.setPassword("secret");
                            user.setnickName(rs.getString("nickName"));
                            user.setName(rs.getString("NAME"));
                            user.setEmail(rs.getString("email"));
                            user.setAge(rs.getString("AGE"));
                            user.setToday_line(rs.getString("today_line"));
                            user.setState(rs.getInt("state"));
                            pw = RSA.decode(rs.getString("password"), privateKey); //비밀번호를 RSA 알고리즘으로 암호화
                        }
                        if (pw == null) ;
                        else if (pw.compareTo(RSA.decode(userContent[1], privateKey)) == 0) // 암호화 된 비밀번호가 기존 저장된 것과 같다면
                            count++;
                        System.out.println(count);

                        if (count == 0) // ID 및 printW 틀리면 세팅한 user 정보 초기화
                        {
                            printW.println(Protocol.ENTERLOGIN_NO + "|" + "로그인에 실패하였습니다.");
                            printW.flush();

                            user.setPryNumber(0);
                            user.setIdName("");
                            user.setPassword("");
                            user.setnickName("");
                            user.setName("");
                            user.setEmail("");
                            user.setAge("");
                            user.setToday_line("");
                            user.setState(0);

                        } else { // 로그인 되었을때
                            String sql1 = "UPDATE usercontent SET state = 1 where id = '" + user.getIdName() + "'"; //사용자의 상태를 online으로 바꿈
                            pstmt = conn.prepareStatement(sql1);
                            pstmt.executeUpdate();

                            sql1 = "UPDATE friendList SET friendState = 1 where friendid = '" + user.getIdName() + "'";
                            pstmt = conn.prepareStatement(sql1);
                            pstmt.executeUpdate();

                            connUserList.add(this); // 접속한 사용자 리스트에 추가
                            String userline = "";
                            String sql2 = "select friendId, friendName, friendNickname, friendtoday_line , friendState from friendList where id = '" + user.getIdName()
                                    + "'";
                            pstmt = conn.prepareStatement(sql2);
                            ResultSet rs1 = pstmt.executeQuery(sql2);

                            while (rs1.next()) { //친구 표시
                                String s = "offline";
                                if (rs1.getString("friendState").equalsIgnoreCase("1")) { //친구 상태가 1 : online일 경우
                                    s = "online"; //online으로 표시
                                }
                                userline += (rs1.getString("friendId") + "&" + rs1.getString("friendName") + "&" +
                                        rs1.getString("friendNickname") + "&" + rs1.getString("friendtoday_line") + "&" + s + ":");
                            }
                            if (userline.length() == 0) {
                                userline = "null&null&null&null&null";
                            }
                            System.out.println(Protocol.ENTERLOGIN_OK + "|" + user.getName() + "|" + user.getIdName() + "|" + user.getToday_line() +
                                    "|" + user.getEmail() + "|" + userline);
                            printW.println(Protocol.ENTERLOGIN_OK + "|" + user.getName() + "|" + user.getIdName() + "|" + user.getToday_line() +
                                    "|" + user.getEmail() + "|" + userline);
                            printW.flush();

                            for (int i = 0; i < connUserList.size(); i++) {
                                connUserList.get(i).printW.println(Protocol.UPDATED);
                                connUserList.get(i).printW.flush();
                            }
                        }
                        System.out.println(user.toString());

                    } else {
                        printW.println(Protocol.ENTERLOGIN_NO + "|" + "이미 로그인 중입니다.");
                        printW.flush();
                    }

                } else if (line[0].compareTo(Protocol.EXITMAINROOM) == 0) { // 메인에서 로그인 페이지(logout);

                    String sql2 = "UPDATE usercontent SET state = 0 where id = '" + user.getIdName() + "'"; // 로그아웃 시 userstate를 0으로 만들어 offline으로 표시
                    pstmt = conn.prepareStatement(sql2);
                    pstmt.executeUpdate();

                    sql2 = "UPDATE friendList SET friendState = 0 where friendid = '" + user.getIdName() + "'";
                    pstmt = conn.prepareStatement(sql2);
                    pstmt.executeUpdate();

                    connUserList.remove(this); //접속한 사용자 리스트에서 제거
                    user.setPryNumber(0);
                    user.setIdName("");
                    user.setPassword("");
                    user.setnickName("");
                    user.setName("");
                    user.setEmail("");
                    user.setAge("");
                    user.setToday_line("");
                    user.setState(0);
                    System.out.println(Protocol.EXITMAINROOM);

                    for (int i = 0; i < connUserList.size(); i++) { //접속한 사용자 리스트와 상태를 업데이트 후 다른 사용자에게 표시
                        connUserList.get(i).printW.println(Protocol.UPDATED);
                        connUserList.get(i).printW.flush();
                    }
                } else if (line[0].compareTo(Protocol.REQUEST_MAKE_GROUPCHAT) == 0) { // 방만들기
                    int l = line[1].length();
                    line[1] = line[1].substring(1, l - 1);
                    System.out.println(line[1]);

                    String userContent[] = line[1].split(", ");
                    String[][] person = new String[userContent.length][];
                    for (int i = 0; i < userContent.length; i++) {
                        person[i] = userContent[i].split("/");
                        for (int j = 0; j < person[i].length; j++) {
                            System.out.println(person[i][j]);
                        }
                    }
                    Room tempRoom = new Room(); //방 생성

                    tempRoom.setUserCount(1); //방 접속자 수 1로 설정
                    tempRoom.setMasterName(user.getIdName()); //방장 ID를 방 만든 사용자 ID로 설정

                    String sql2 = "UPDATE room_number SET room_num = LAST_INSERT_ID(room_num + 1);";
                    pstmt = conn.prepareStatement(sql2);
                    pstmt.executeUpdate();
                    String sql = "select room_num from room_number";
                    pstmt = conn.prepareStatement(sql);
                    ResultSet rs = pstmt.executeQuery(sql);
                    int priNumber = 0;
                    while (rs.next()) {
                        priNumber = rs.getInt("room_num");
                    }
                    System.out.println("userContent Length : " + userContent.length);
                    if (priNumber != 0) { //방 번호 불러왔으면
                        tempRoom.setrID(priNumber); //현재 방의 번호를 지정
                        tempRoom.setRoomInUserList(user.getIdName()); //방 사용자 리스트에 현재 사용자의 ID를 넣음
                        roomtotalList.add(tempRoom);
                        priRoom = tempRoom; // 현재 룸을 지정
                    }
                    System.out.println(roomtotalList.size());
                    printW.println(Protocol.ROOMMAKE_OK + "|" + tempRoom.getMasterName() + "|" + priNumber);
                    printW.flush();
                    for (int i = 0; i < connUserList.size(); i++) { //접속 유저 리스트의 사이즈 만큼 반복
                        System.out.println("IDDDd" + connUserList.get(i).user.getIdName());
                        for (int j = 0; j < userContent.length; j++) {
                            if (connUserList.get(i).user.getIdName().equals(person[j][0])) {
                                connUserList.get(i).printW.println(Protocol.JOINROOM_REQUEST + "|" + tempRoom.getMasterName()
                                        + "|" + priNumber);
                                connUserList.get(i).printW.flush();
                                System.out.println(Protocol.JOINROOM_REQUEST);
                            }
                        }
                    }
                } else if (line[0].compareTo(Protocol.JOINROOM_YES) == 0) { // 방 입장 버튼 클릭

                    String thisName = connUserList.get(connUserList.indexOf(this)).user.getIdName(); //접속자 리스트에서 사용자의 ID를 받아옴

                    int roomid = Integer.parseInt(line[2]); // 방 ID
                    System.out.println(roomid);
                    String roomUser = "";
                    int index = 0;
                    for (int i = 0; i < roomtotalList.size(); i++) { //전채 방 개수 만큼
                        System.out.println("Room ID L:" + roomtotalList.get(i).getrID());
                        if (roomtotalList.get(i).getrID() == roomid) { //방 ID가 같을 경우
                            int c = roomtotalList.get(i).getUserCount(); //현재 접속자 수를 받아오고
                            roomtotalList.get(i).setUserCount(c + 1); //+1
                            roomUser = roomtotalList.get(i).getRoomInUserList();
                            roomtotalList.get(i).setRoomInUserList(roomUser + "%" + user.getIdName()); //접속자 리스트에 현재 접속자를 추가
                            priRoom = roomtotalList.get(i);
                            index = i;
                            System.out.println(priRoom.toString());
                        }
                    }

                    System.out.println(thisName);
                    System.out.println("Index : " + index);
                    System.out.println(connUserList.size());
                    printW.println(Protocol.ENTERROOM_OK1 + "|" + line[1] + "|" + line[2]);
                    printW.flush();
                    String roomMember[] = roomtotalList.get(index).getRoomInUserList().split("%"); //방 인원
                    System.out.println(roomMember.length);
                    for (int i = 0; i < roomMember.length; i++) {
                        for (int j = 0; j < connUserList.size(); j++) {
                            if (connUserList.get(j).user.getIdName().equalsIgnoreCase(roomMember[i])) {
                                connUserList.get(j).printW.println(Protocol.ENTERROOM_USERLISTSEND + "|"
                                        + roomtotalList.get(index).getRoomInUserList() + "|" + user.getIdName() + "님이 입장하셨습니다." + "|" + line[2]); //��ȭ ���濡�� ���� ����ڰ� ���������� �˸�
                                connUserList.get(j).printW.flush();
                            }
                        }
                    }
                } else if (line[0].compareTo(Protocol.EXITCHATTINGROOM) == 0) // 방 나가기
                {

                    int roomIndex = 0;
                    boolean con = true;

                    for (int i = 0; i < roomtotalList.size(); i++) {
                        if (roomtotalList.get(i).getrID() == Integer.parseInt(line[2])) {

                            if (roomtotalList.get(i).getUserCount() == 2) // 나오는 사람이 마지막일 경우
                            {
                                roomIndex = i;
                                String roomUser = roomtotalList.get(roomIndex).getRoomInUserList();
                                String roomMember[] = roomUser.split("%");
                                for (int k = 0; k < roomtotalList.get(roomIndex).getUserCount(); k++) {
                                    for (int j = 0; j < connUserList.size(); j++) {
                                        if (connUserList.get(j).user.getIdName().equalsIgnoreCase(roomMember[k])) {
                                            connUserList.get(j).printW.println(Protocol.EXIT_CHATTINGROOM + "|" + roomtotalList.get(i).getrID());
                                            connUserList.get(j).printW.flush(); //방 전체 리스트 중에 현재 방이 몇번째 위치하는지 검색
                                        }
                                    }
                                }

                                System.out.println("방을 나갈 시 마지막으로 나올 경우");
                                Room tempRoom = roomtotalList.get(i);
                                roomtotalList.remove(tempRoom); //현재 방 제거
                                tempRoom = new Room();
                                con = false;


                            } else { // 최소 2명 이상일때
                                System.out.println("방을 나갈 시 마지막이 아닐 경우");
                                Room tempRoom = roomtotalList.get(i);
                                roomtotalList.get(i).setUserCount(roomtotalList.get(i).getUserCount() - 1); // 방에서 유저 제거
                                tempRoom = new Room();// 현재 방 비우기
                                roomIndex = i;
                            }

                        }

                        if (con) // 남아 있는 방에 최소 2명 이상일때
                        {
                            String roomUser = roomtotalList.get(roomIndex).getRoomInUserList(); //방의 총 인원을 받아옴
                            String roomMember[] = roomUser.split("%");
                            System.out.println("특정 방의 사람 수 : " + roomtotalList.get(roomIndex).getUserCount());
                            System.out.println(roomUser);
                            for (int k = 0; k < roomtotalList.get(roomIndex).getUserCount(); k++) { //방의 모든 인원에게 누군가 퇴장했음을 알림
                                for (int j = 0; j < connUserList.size(); j++) {
                                    if (connUserList.get(j).user.getIdName().equalsIgnoreCase(roomMember[k])) {
                                        connUserList.get(j).printW.println(Protocol.ENTERROOM_USERLISTSEND + "|"
                                                + roomUser + "|" + user.getIdName() + "님이 퇴장하셨습니다." + "|" + roomtotalList.get(i).getrID());
                                        connUserList.get(j).printW.flush();
                                    }
                                }
                            }
                        }

                        String roomListMessage = "";

                        System.out.println(roomListMessage);
                    }


                } else if (line[0].compareTo(Protocol.CHATTINGSENDMESSAGE) == 0) // 채팅방에서 메세지 보내기
                {
                    int index = 0;
                    for (int i = 0; i < roomtotalList.size(); i++) {
                        if (roomtotalList.get(i).getrID() == Integer.parseInt(line[2])) {
                            index = i; //현재 방 순서를 받아옴
                        }
                    }
                    System.out.println("RoomNumber : " + line[2]);
                    System.out.println("Index =" + index);
                    String roomUser = roomtotalList.get(index).getRoomInUserList(); //현재 방의 접속자 리스트를 받아옴
                    String roomMember[] = roomUser.split("%");
                    for (int i = 0; i < roomMember.length; i++) {
                        for (int j = 0; j < connUserList.size(); j++) {
                            if (connUserList.get(j).user.getIdName().equalsIgnoreCase(roomMember[i])) { //방 접속자들에게 메세지 전송
                                connUserList.get(j).printW.println(Protocol.CHATTINGSENDMESSAGE_OK +
                                        "|" + user.getIdName() + "|" + line[1] + "|" + roomtotalList.get(index).getrID());
                                connUserList.get(j).printW.flush();
                            }
                        }
                    }

                } else if (line[0].compareTo(Protocol.CHANGE_TODAY_LINE) == 0) { //오늘의 한마디 변경
                    String sql2 = "UPDATE usercontent SET today_line= '" + line[1] + "' where id = '" + user.getIdName() + "'";
                    pstmt = conn.prepareStatement(sql2);
                    pstmt.executeUpdate();
                    String sql1 = "UPDATE friendList SET friendtoday_line= '" + line[1] + " 'where friendId= '" + user.getIdName() + "'";
                    pstmt = conn.prepareStatement(sql1);
                    pstmt.executeUpdate(); //DB에 변경된 문장을 저장

                    for (int i = 0; i < connUserList.size(); i++) { //현재 접속자 리스트에게 오늘의 한마디가 업데이트 되었다고 전송
                        connUserList.get(i).printW.println(Protocol.UPDATED);
                        connUserList.get(i).printW.flush();
                    }
                    printW.println(Protocol.UPDATE_ME + "|" + line[1]);
                    printW.flush();
                } else if (line[0].compareTo(Protocol.UPDATE_PLZ) == 0) { //업데이트가 있음을 받으면 정보 요청
                    String userline = "";
                    String sql = "select friendId, friendName, friendNickname, friendtoday_line, friendState from friendList where id = '" + user.getIdName()
                            + "'";
                    pstmt = conn.prepareStatement(sql);
                    ResultSet rs1 = pstmt.executeQuery(sql);

                    while (rs1.next()) { //받아온 정보로 친구 목록 재작성
                        String s = "offline";
                        if (rs1.getString("friendState").equalsIgnoreCase("1")) {
                            s = "online";
                        }
                        userline += (rs1.getString("friendId") + "&" + rs1.getString("friendName") + "&"
                                + rs1.getString("friendNickname") + "&" + rs1.getString("friendtoday_line") + "&" + s + ":");
                    }
                    if (userline.length() == 0) {
                        userline = "null&null&null&null&null";
                    }
                    System.out.println(Protocol.UPDATE_CONFIRM + "|" + userline);
                    printW.println(Protocol.UPDATE_CONFIRM + "|" + userline);
                    printW.flush();
                } else if (line[0].compareTo(Protocol.REQUEST_FRIEND_LIST) == 0) { //친구 추가 창 띄우는 경우
                    String userline = "";
                    if (line[1].equalsIgnoreCase("ALL!@#")) { //전체 사용자의 정보 요청할 경우
                        String sql = "select id, name, nickName, state from usercontent";
                        pstmt = conn.prepareStatement(sql);
                        ResultSet rs1 = pstmt.executeQuery(sql);
                        while (rs1.next()) { //현재 DB에 있는 모든 사람의 정보를 받아오기
                            if (rs1.getString("id").equalsIgnoreCase(user.getIdName())) {
                            } else {
                                String s = "offline"; //친구 목록에 상태 추가
                                if (rs1.getString("state").equalsIgnoreCase("1")) {
                                    s = "online";
                                }
                                userline += (rs1.getString("id") + "&" + rs1.getString("name") + "&" +
                                        rs1.getString("nickName") + "&" + s + ":");
                            }

                        }
                    } else { //내가 선택한 사람의 정보를 받아오기
                        String sql = "select id, name, nickName from usercontent where id like'%" + line[1] + "%'";
                        pstmt = conn.prepareStatement(sql);
                        ResultSet rs1 = pstmt.executeQuery(sql);
                        while (rs1.next()) {
                            if (rs1.getString("id").equalsIgnoreCase(user.getIdName())) {
                            } else {
                                String s = "offline"; //친구 목록에 상태 추가
                                if (rs1.getString("state").equalsIgnoreCase("1")) {
                                    s = "online";
                                }
                                userline += (rs1.getString("id") + "&" + rs1.getString("name") + "&" +
                                        rs1.getString("nickName") + "&" + s + ":");
                            }

                        }
                    }
                    if (userline.length() == 0) {
                        userline = "null&null&null&null";
                    }
                    printW.println(Protocol.REQUEST_FRIEND_LIST_CONFIRM + "|" + userline);
                    printW.flush();
                } else if (line[0].compareTo(Protocol.REQUEST_FRIEND_ADD) == 0) {//친구 추가할 유저 정보 전송
                    String text[] = line[1].split(" ");
                    String sql = "select id, name, nickName, today_line, state from usercontent where id ='" + text[0] + "'";//동일 한 ID의 사용자 정보 가져옴

                    pstmt = conn.prepareStatement(sql);
                    ResultSet rs = pstmt.executeQuery(sql);
                    String sql1 = "";


                    while (rs.next()) {//받아온 사용자 정보를 친구리스트 DB에 저장
                        sql1 = "Insert into friendList values('" + user.getIdName() + "','" + rs.getString("id") + "','" + rs.getString("name")
                                + "','" + rs.getString("nickName") + "','" + rs.getString("today_line") + "','" + rs.getString("state") + "')";
                    }
                    pstmt = conn.prepareStatement(sql1);
                    pstmt.executeUpdate();

                    for (int i = 0; i < connUserList.size(); i++) { //접속중인 유저들 모두에게
                        connUserList.get(i).printW.println(Protocol.UPDATED); //변경사항 있음을 알림
                        connUserList.get(i).printW.flush();
                    }
                    printW.println(Protocol.UPDATED + "|" + line[1]);
                    printW.flush();
                } else if (line[0].compareTo(Protocol.REQUEST_FRIEND_DELETE) == 0) {//친구 삭제
                    String text[] = line[1].split("/");
                    String sql = "delete from friendlist where id='" + user.getIdName() + "' and friendId='" + text[0] + "'"; //친구 삭제 쿼리문

                    pstmt = conn.prepareStatement(sql);
                    pstmt.executeUpdate();

                    System.out.println(text[1] + " is deleted");
                    String sql2 = "select id, name, nickName, age, email, today_line , state from usercontent where id ='" + text[0] + "'";
                    pstmt = conn.prepareStatement(sql2);
                    ResultSet rs = pstmt.executeQuery(sql2);
                    String id = "";
                    String nickname = "";
                    String name = "";
                    String email = "";
                    String age = "";
                    String today_line = "";
                    int state = 0;
                    while (rs.next()) { //삭제된 친구의 정보를 받아와 저장
                        id = (rs.getString("ID"));
                        nickname = (rs.getString("nickName"));
                        name = (rs.getString("NAME"));
                        email = (rs.getString("email"));
                        age = (rs.getString("AGE"));
                        today_line = (rs.getString("today_line"));
                        state = (rs.getInt("state"));
                    }

                    printW.println(Protocol.CONFIRM_FRIEND_DELETE + "|" + id + "&" + nickname + "&"
                            + name + "&" + email + "&" + age + "&" + today_line + "&" + state); //친구가 삭제되었음을 알림
                    printW.flush();
                } else if (line[0].compareTo(Protocol.CHECK_FRIEND_INFO) == 0) { //친구 정보 받아오기
                    String text[] = line[1].split("/");
                    String sql = "select id, name, nickName, age, email, today_line , state from usercontent where id ='" + text[0] + "'"; //친구 정보 받아오기
                    pstmt = conn.prepareStatement(sql);
                    ResultSet rs = pstmt.executeQuery(sql);
                    String id = "";
                    String nickname = "";
                    String name = "";
                    String email = "";
                    String age = "";
                    String today_line = "";
                    int state = 0;
                    while (rs.next()) { //친구의 정보를 받아오 ㅏ저장
                        id = (rs.getString("ID"));
                        nickname = (rs.getString("nickName"));
                        name = (rs.getString("NAME"));
                        email = (rs.getString("email"));
                        age = (rs.getString("AGE"));
                        today_line = (rs.getString("today_line"));
                        state = (rs.getInt("state"));
                    }
                    printW.println(Protocol.CONFIRM_FRIEND_INFO + "|" + id + "&" + nickname + "&"
                            + name + "&" + email + "&" + age + "&" + today_line + "&" + state); //친구의 정보를 유저에게 출력하기 위해 전달
                    printW.flush();
                } else if (line[0].compareTo(Protocol.REQUEST_GROUPCHAT_LIST) == 0) { //그룹 채팅을 열기 위해 친구 목록 요청
                    String userline = "";
                    String sql = "select friendId, friendName, friendNickname, friendtoday_line, friendState from friendList where id = '" + user.getIdName()
                            + "'"; //현재 사용자의 친구 리스트들을 받아옴
                    pstmt = conn.prepareStatement(sql);
                    ResultSet rs1 = pstmt.executeQuery(sql);

                    while (rs1.next()) { //친구 목록 생성
                        String s = "offline";
                        if (rs1.getString("friendState").equalsIgnoreCase("1")) {
                            s = "online";
                        }
                        userline += (rs1.getString("friendId") + "&" + rs1.getString("friendName") + "&"
                                + rs1.getString("friendNickname") + "&" + rs1.getString("friendtoday_line") + "&" + s + ":");
                    }
                    if (userline.length() == 0) {
                        userline = "null&null&null&null&null";
                    }
                    System.out.println(Protocol.GROUPCHAT_LIST + "|" + userline);
                    printW.println(Protocol.GROUPCHAT_LIST + "|" + userline); //친구 목록 출력
                    printW.flush();
                } else if (line[0].compareTo(Protocol.JOINROOM_NO) == 0) { //채팅 초대 거절
                    int index = 0;
                    for (int i = 0; i < roomtotalList.size(); i++) {
                        if (roomtotalList.get(i).getrID() == Integer.parseInt(line[2])) {
                            index = i; //거절당한 채팅방의 인덱스를 얻어옴
                            System.out.println(priRoom.toString());
                        }
                    }
                    String roomMember[] = roomtotalList.get(index).getRoomInUserList().split("%");
                    System.out.println(roomMember.length);
                    for (int i = 0; i < roomMember.length; i++) {
                        for (int j = 0; j < connUserList.size(); j++) {
                            if (connUserList.get(j).user.getIdName().equalsIgnoreCase(roomMember[i])) {
                                connUserList.get(j).printW.println(Protocol.ENTERROOM_REJECT + "|" + user.getIdName() + "|" + line[2]); //채팅방 안에 있는 유저들에게 거절당했음을 알림
                                connUserList.get(j).printW.flush();
                            }
                        }
                    }
                }
            } // while
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                chatReader.close(); //정상 종료시 입력 버퍼를 닫음
            } catch (IOException e) {
                e.printStackTrace();
            }
            printW.close();
            try {
                socket.close(); //정상 종료시 소켓을 닫음
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
