package Action;

public class Protocol {
	public static final String REGISTER = "100"; // 회원가입(request)

	public static final String IDSEARCHCHECK = "111"; // (using 회원가입)ID 중복확인(request)

	public static final String IDSEARCHCHECK_OK = "112"; // (using 회원가입)ID 중복확인 (사용 가능) (ACK)

	public static final String IDSEARCHCHECK_NO = "113"; // (using 회원가입)ID 중복확인 (사용 불가능) (NACK)

	public static final String ENTERLOGIN = "120"; // 로그인(request)

	public static final String ENTERLOGIN_OK = "121"; // 로그인 성공(ACK)

	public static final String ENTERLOGIN_NO = "122"; // 로그인 실패(NACK)

	public static final String ROOMMAKE_OK = "201"; // 방만들기_ACK

	public static final String EXITMAINROOM = "203"; // 로그아웃

	public static final String ENTERROOM_OK1 = "302"; // 방입장 성공 (방만든 사용자)

	public static final String ENTERROOM_USERLISTSEND = "304"; // 방에 유저목록 보내기

	public static final String EXITCHATTINGROOM = "305"; // 방 나가기 (채팅방 나가기)

	public static final String CHATTINGSENDMESSAGE = "420"; // 채팅방에서 메시지 보내기 (Request)

	public static final String CHATTINGSENDMESSAGE_OK = "430"; // 서버가 채팅내용 해당 채팅방에 전달
	
	public static final String REQUEST_FRIEND_LIST = "560";	// 친구추가 창 띄울시 전체 유저 목록 요청
	
	public static final String REQUEST_FRIEND_LIST_CONFIRM = "561"; // 서버가 유저에게 유저 목록 전송
	
	public static final String REQUEST_FRIEND_ADD = "562"; // 친구 추가할 유저 정보 전송

	public static final String CHANGE_TODAY_LINE = "570"; // 오늘의 한마디 변경
	
	public static final String UPDATE_ME = "571"; // 오늘의 한마디 변경 당사자
	
	public static final String UPDATED = "580"; // 오늘의 한마디 변경, 친구 추가 등등 여러 업데이트 발생 시 최신화 위함. 서버에서 유저에게 업데이트 되었다고 보냄
	
	public static final String UPDATE_PLZ = "581"; //UPDATED 요청 받으면 유저는 업데이트 된 항목 최신화 요청
	
	public static final String UPDATE_CONFIRM = "582";//서버가 UPDATE_PLZ 받으면 정보를 유저에게 보냄
	
	public static final String CHECK_FRIEND_INFO = "590"; //친구의 정보 확인 요청
	
	public static final String CONFIRM_FRIEND_INFO = "591"; //친구의 정보 데이터 유저에게 전달
	
	public static final String REQUEST_GROUPCHAT_LIST = "600"; //그룹 채팅방 열기 위해 친구 목록 요청
	
	public static final String GROUPCHAT_LIST = "601"; //친구 목록 전달
	
	public static final String REQUEST_MAKE_GROUPCHAT = "602"; //해당 유저 포함한 채팅방 만들기 요청
	
	public static final String JOINROOM_REQUEST = "610"; //유저가 채팅 초대하면 초대 메시지 전송
	
	public static final String JOINROOM_YES = "611"; //채팅 요청 수락
	
	public static final String JOINROOM_NO = "612"; //채팅 요청 거절
	
	public static final String ENTERROOM_REJECT = "620"; //거절 했을시 입장해있는 유저에게 알림 보냄
	
	public static final String EXIT_CHATTINGROOM = "621"; //채팅방을 유저가 나갔을 시
	
	public static final String REQUEST_FRIEND_DELETE="630"; //친구 삭제
	
	public static final String CONFIRM_FRIEND_DELETE="631";//삭제 확인
	
}
