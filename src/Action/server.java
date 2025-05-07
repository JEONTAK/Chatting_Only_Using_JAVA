package Action;

import DO.Room;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class server {
	private ServerSocket serverSocket; // 서버 소켓
	private ArrayList<MainHandler> allUserList; // 전체 사용자
	private ArrayList<MainHandler> connUserList; // 접속 사용자
	private ArrayList<Room> roomtotalList;// 전체 방리스트

	private Connection conn;
	private String driver = "com.mysql.cj.jdbc.Driver";
    private String url = "jdbc:mysql://localhost/chatting?serverTimezone=UTC ";
    private String user = "root";
    private String password = "1111";

	public server() {

		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, password); // DB 연결

			serverSocket = new ServerSocket(9500);
			System.out.println("서버준비완료");

			allUserList = new ArrayList<MainHandler>(); // 전체 사용자
			connUserList = new ArrayList<MainHandler>(); // 접속 사용자
			roomtotalList = new ArrayList<Room>(); // 전체 방리스트
			while (true) {
				Socket socket = serverSocket.accept();
				MainHandler handler = new MainHandler(socket,connUserList, roomtotalList, conn);// 스레드 생성
				handler.start();// 스레드 시작
			} // while
		} catch (IOException io) {
			io.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new server();
	}
}
